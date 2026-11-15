# Issue #12 — P04 Independent Diagnostics Consent and Fail-Closed Collection

**Date:** 2026-11-15
**Implementation status:** SOURCE_IMPLEMENTED_UNCOMPILED_NOT_RUN.
**Branch:** feature/12-privacy-safe-telemetry-measurement
**Source archive:** exact owner-uploaded MSM2(1)(1).rar, SHA-256 0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8; extracted with libarchive and opened the English PDF sources.

## 1. Actual English source authority

- Music_Copilot_PRD_v1_EN_Clarified.pdf, p.10 Data/Privacy/Measurement: data minimization; microphone access just-in-time and clearly explained; history and diagnostic data are user-clearable; raw audio never stored/sent by default; cloud STT requires separate consent, processing destination and retention disclosure.
- Music_Copilot_MVP_v1_EN.pdf, p.9 Beta onboarding: informed consent for non-audio diagnostic data. p.10 Data Boundary: permitted only with consent: intent type, confidence/latency buckets, player error code and coarse audio route; forbidden: raw audio, full personal titles, precise location, contacts and identifiers. p.11: Privacy copy must be approved before Beta GO.
- ADR-0003_Local_First_Data_Boundaries_Revised.pdf: diagnostics are non-authoritative and minimally collected with consent; DataStore is settings data, not user music library; no account/backend/sync; service outage cannot alter playback; user must control local diagnostic data.
- ADR-0004_Replaceable_Voice_Provider.pdf §6: only user-initiated PTT; no raw speech logs; cloud STT requires distinct provider/destination/purpose/retention disclosure and grant; declining cloud voice must not remove manual playback.
- Repository docs/ANALYTICS_AND_PRIVACY.md, docs/user-flows/UF-12_SETTINGS_PRIVACY.md, docs/implementation/ISSUE_12_PHASED_EXECUTION_PLAN_FA.md P04, docs/implementation/ISSUE_12_EVENT_SCHEMA.md P03, issue #12 and approved #16 boundaries.

There is **no approved final diagnostic notice wording, consent-version, precise retention/TTL, provider/destination, Data Processing Agreement or vendor-specific deletion SLA** established by these sources. P04 must never fabricate such approvals or enable network diagnostics.

## 2. Independent consent lifecycle (source implemented)

| Case | State/action | Collection |
|---|---|---|
| First install, old install lacking key | DiagnosticsConsent.NotAsked (default) | DENIED |
| Explicit denial | DiagnosticsConsent.Declined | DENIED |
| Explicit withdrawal | DiagnosticsConsent.Revoked | DENIED |
| Persisted decision Granted, but any gate not reviewed | DiagnosticsConsent.Granted | DENIED |
| Preferences Loading or Failed | Any persisted consent | DENIED |
| Consent Granted + preferences Ready + approved legal, notice, retention and collection adapter | Positive conjunction **only in a future reviewed configuration** | Conditionally eligible for privacy-allowlisted local capture; never authorizes unapproved network transfer |
| Revocation requested before asynchronous DataStore edit | In-process revocation latch flips immediately | DENIED even with stale persisted Granted |
| Future consent regrant | Requires a reviewed user action and verified persistence, with reset policy/reconciliation designed in P12; no P04 UI permits this | NOT IMPLEMENTED |

- DiagnosticsConsent is **separate** from CloudSpeechConsent and separate from Android RECORD_AUDIO. A microphone permission grant is not non-audio diagnostics consent, and neither is cloud STT consent.
- The stable new DataStore preference key is **non_audio_diagnostics_consent**, not cloud_speech_consent. Old/unknown values decode to NotAsked. Existing preference records, onboarding, selected Bluetooth and cloud speech decisions keep their old semantics.
- Future UI must only treat a successful DataStore write as consent decision confirmation; a failed save cannot be shown as Granted or Revoked. P12 owns accessible user-facing choices, disclosure and honest failure reporting. **No consent UI or approval copy is invented during P04.**
- Current Graph exposes revokeDiagnosticsConsent(): synchronous immediate blocking latch then best-effort asynchronous DataStore update to Revoked. Asynchronous write failure leaves the process denied, but P12 must surface/retry failure. If a pre-existing persisted Granted is still present after a write failure and the process later restarts, a future approved build must resolve persistent revocation state before allowing capture; current production approvals are disabled and collection remains OFF regardless.
- Source-level granting via generic PreferencesRepository is NOT proof of a reviewed notice or legal authorization: all four production collection approvals default false and cannot be toggled by the user's persisted enum alone.

## 3. Actual collection gates and lifecycle

1. App owns one DiagnosticsCollectionAuthorization instance, bound to its existing PreferencesRepository and StateFlows.
2. DiagnosticsConsentPolicy.canCollect is false unless ALL conditions are true: preferences load READY, diagnosticsConsent Granted, reviewed privacy copy, retention approval, collection adapter approval, legal approval and no revocation latch.
3. BestEffortQualityEventPublisher checks authorization **before** privacy sanitization/event capture. It returns false immediately on denial and does not mutate playback, voice, route, UI or permission state.
4. Existing AllowlistedTelemetryPrivacyGate still checks the fixed eight-event schema and rejects Issue #11 ninth internal voice outcome. This schema step alone is deliberately not a consent authorization.
5. The ConsentGatedTelemetrySink wraps the existing NoOpTelemetrySink and re-checks authorization at **delivery/flush**. If an event was sanitized before withdrawal, the later sink call is still denied.
6. NoOpTelemetrySink remains installed for both debug and release; no analytics/crash SDK or transport, event database, non-NoOp store, background queue, user IDs, outbound channel or upload is introduced in P04.
7. Capture/sink errors are best effort and non-authoritative. A denied or missing consent must not change the Media3 service, STT request, Ducking, Audio Focus, Bluetooth recovery, library scan or manual playback.

**Current production state is OFF** even when a preference contains Granted: QualityBindings.authorization uses DiagnosticsCollectionApprovals() with all four flags false. This is intentional, pending real Product/Privacy/Legal review.

## 4. Files changed on Issue #12 feature branch

| Path | Effect |
|---|---|
| app/src/main/java/com/latticeward/musiccopilot/data/api/PreferencesRepository.kt | New DiagnosticsConsent enum, default-off AppPreferences value and distinct SetDiagnosticsConsent update |
| app/src/main/java/com/latticeward/musiccopilot/data/datastore/PreferencesDataStore.kt | New DataStore key, safe decode fallback and atomic persistence case without touching cloud consent |
| app/src/main/java/com/latticeward/musiccopilot/core/application/privacy/DiagnosticsConsentPolicy.kt | New pure policy, explicit approval gates and app-scoped immediate revoke latch |
| app/src/main/java/com/latticeward/musiccopilot/quality/privacy/ConsentGatedTelemetrySink.kt | New fail-closed sink check on every outgoing delivery |
| app/src/main/java/com/latticeward/musiccopilot/quality/adapters/BestEffortQualityEventPublisher.kt | Optional canCollect check before sanitation; production explicitly binds authorization, legacy isolated test fixture constructor preserved |
| app/src/main/java/com/latticeward/musiccopilot/app/di/QualityBindings.kt | Production authorization injection, gated NoOp sink, injection into publisher; no provider activated |
| app/src/main/java/com/latticeward/musiccopilot/app/di/ApplicationBindings.kt | One app-scoped authorization, live readiness/consent checks, immediate revoke entry |
| app/src/test/java/com/latticeward/musiccopilot/foundation/Issue12Phase4DiagnosticsConsentTest.kt | New **12 source-authored JVM scenarios; NOT COMPILED, NOT RUN** |

### Architecture #16 boundary confirmation

The normative dependency rules in docs/architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md (§9.8–9.9) forbid Quality implementation from calling Data. Accordingly, DiagnosticsConsentPolicy and DiagnosticsCollectionAuthorization are placed in **core/application/privacy** (Application/Domain may depend on data/api). Quality's publisher and guarded sink depend only on quality/api and an injected Boolean authorization callback. app/di/QualityBindings owns the cross-boundary dependency injection. No Quality → Data dependency has been introduced.

## 5. Privacy copy + provider/retention approval decision ledger

| ID | Required human sign-off | Status (2026-11-15) | Safe action |
|---|---|---|---|
| CP01 | Plain-language independent non-audio diagnostic purpose/fields/beta participation text; Product+Privacy+UX approved | **NOT APPROVED** | Do not offer a live grant UI or capture |
| CP02 | Retention TTL, on-device storage limits and deletion timing; Product+Privacy+Data | **NOT APPROVED** | Keep production sink NoOp; storage P09 |
| CP03 | Any local diagnostic collector implementation/storage approved; Product+Privacy | **NOT APPROVED** | No persistent collection |
| CP04 | Vendor/destination, processing purpose, residency, retention/deletion, privacy/legal review of remote upload or Crash SDK | **NO PROVIDER SELECTED / NOT APPROVED** | No network upload/SDK |
| CP05 | Separate cloud STT processor/purpose/destination/retention consent and approval | **NOT APPROVED; REMOTE STT DISABLED** | Local Moonshine/Silero only |
| CP06 | Consent UI with explicit decline, withdrawal, fresh UI state and truthful error/retry, including offline fallback; UX+Product | **P12 NOT STARTED** | Preference API/latch only, no UI |
| CP07 | Immediate withdrawal across process death; persisted-revoke failure recovery and eventual deletion from any future approved store | **REQUIRES DESIGN/QA REVIEW** | Production disabled; P09/P12 must reconcile |
| CP08 | Formal QA evidence for denial, allowlist and nonblocking behavior | **NOT_RUN** | P13 author-only + P14 owner QA |

No entry in this table is a fabricated signature. Issue #12 AC08 and AC09 are not verified just because the underlying policy exists.

## 6. Authored-only QA scenarios and acceptance boundary

Source file Issue12Phase4DiagnosticsConsentTest.kt contains authored cases for: old installations without new key, no/declined/revoked decisions, incomplete review gates, failing/loading preferences, Cloud STT independence, fresh StateFlow checks, immediate withdrawal despite pending DataStore write, capture-before-sanitize check, flush-after-withdrawal check, throwing sink, NoOp production wiring and key separation. **No compilation, JVM, CI, emulator, manual phone testing, APK or external transfer has been done.**

**P04 source deliverable is complete; functional QA remains NOT_VERIFIED.** P05 will add real authoritative Driving/PTT emitters; P06 onward will add correct outcomes; P09 retention and consented local store; P12 approved Settings UX and deletion; P13 test contracts; P14 only final integrated APK and owner-phone QA. Do not merge/close Issue #12 or treat review gates as passed. Inherited P0 follow-ups #80 and #82 remain open.
