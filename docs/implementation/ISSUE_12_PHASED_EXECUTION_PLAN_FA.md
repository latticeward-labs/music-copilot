# Issue #12 — Privacy-safe Telemetry, Crash Reporting & Measurement | Phased execution plan

**Date:** 2026-11-15  
**Repository:** latticeward-labs/music-copilot  
**Issue:** https://github.com/latticeward-labs/music-copilot/issues/12  
**Status:** EXECUTION IN PROGRESS — P01/P02 source/documentation complete; P03/P04 SOURCE_IMPLEMENTED_UNCOMPILED_NOT_RUN; P05–P14 NOT_STARTED. This plan alone never proves QA or product acceptance; see the phase ledger.  
**Source archive:** User-uploaded `MSM2(1)(1).rar`, SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, directly extracted and reviewed.

## Governing source order

1. Attached English `Music_Copilot_PRD_v1_EN_Clarified.pdf` (p.10 Privacy & Metrics) — immutable product targets / event names.
2. Attached English `Music_Copilot_MVP_v1_EN.pdf` (pp.9–10 Beta indicators, Telemetry & Dashboard) — MVP delivery/measurement conditions and event properties.
3. Attached English `Music_Copilot_System_Architecture_MVP_v1_EN.pdf` and ADR-0003/0004/0005/0006 — Quality boundary, no product backend/account/sync, minimization/consent, replaceable provider.
4. GitHub `docs/ANALYTICS_AND_PRIVACY.md`, `docs/SOFTWARE_ARCHITECTURE.md`, `docs/architecture/RUNTIME_POLICIES.md`, `docs/architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md`; Issue #12 and approved #16 architecture.

Conflicts must be escalated for Product/Privacy review rather than silently changing approved event names, properties, denominator definitions, targets, retention or external providers.

## Verified starting point on main

- Issue #12 OPEN, no complete production analytics/crash provider/dashboard. Issue #16 closed/approved; Issue #4, #9, #10 and #11 closed as implementation milestones, **not** blanket device/quality acceptance.
- Existing code: `quality/api/TelemetryEvent.kt`, `TelemetryPorts.kt`, `quality/privacy/AllowlistedTelemetryPrivacyGate.kt`, `quality/adapters/BestEffortQualityEventPublisher.kt`, `quality/noop/NoOpTelemetrySink.kt`, `app/di/QualityBindings.kt`, Voice coordinator and Playback service.
- `QualityBindings.sink()` currently returns `NoOpTelemetrySink`: no production event collection is established. The typed allowlist and best-effort isolation are useful baselines, not completed Issue #12 acceptance.
- Issue #11 added a ninth *internal* `voice_intent_outcome` event and defined an STT-attempt-to-terminal latency bucket; **the locked v1.0 external allowlist contains eight event families**. Keep the ninth diagnostic internal/unexported unless an explicit Product/Privacy schema review authorizes any outbound addition. Prefer projecting approved statuses into the eight locked families without leaking or double-counting.
- P0 blockers #80 (actual five-intent lab 24/50, 48%) and #82 (artist matching/QA) are OPEN; future beta metrics must not mask these failures.

## Execution policy (owner workflow)

- This commit is planning documentation only. No implementation phase is implicitly done.
- **P01–P13:** source/design/doc updates only on this feature branch; prepare test source and manual QA specifications, but DO NOT execute automated tests, emulator/phone tests, Gradle compilation, GitHub Actions tests or APK builds. Record source-only as NOT_RUN/NOT_VERIFIED.
- **P14 FINAL ONLY:** one integrated build-only APK from all phases, with reproducible commit/size/checksum and complete Persian owner-phone QA checklist. Do not invoke Gradle test/check/lint or device automation unless separately authorized. Owner runs handset/vehicle tests and reports observed PASS/FAIL/BLOCKED; only then record evidence in GitHub and seek explicit permission for PR merge/Issue closure. Test cases authored but never run remain NOT_RUN and cannot satisfy a verified technical test gate.
- No unrelated changes to recognition models, intent matching, Media3 playback logic, routing/volume, local library or navigation. A necessary narrow hook must preserve existing authority and behavior.
- Analytics/crash failures must never delay or change playback/Voice/Audio Focus/permission/route safety.
- No unreviewed Firebase/third-party SDK, network endpoint, persistent user tracking, API key, product backend, account, streaming, sync, audio recording or raw diagnostic upload.

## P01 — Source baseline, traceability & DoR

**Actions:** Freeze source-authority table and archive checksum; inventory current typed events, DI/Quality, Voice P11, Driving #9, Player/Media3, Settings, #80/#82; identify all eight original AC groups/acceptance criteria, unresolved privacy/provider/legal approvals; create phase ledger and risk register. Preserve legacy ninth event as explicit discrepancy.
**Output:** `ISSUE_12_SOURCE_AND_GAP_AUDIT.md`, `ISSUE_12_PHASE_LEDGER.json` with P01–P14 initially NOT_DONE.
**Exit:** Every AC mapped to owner/source/path; unresolved approvals recorded as BLOCKED, never invented.

## P02 — Measurement dictionary & frozen success thresholds

**Actions:** Define unambiguous numerators/denominators, session/voice attempt boundaries, direct vs ambiguous commands, completed vs incomplete/crashed journeys, explicit user-corrected vs wrong-execution, event loss/unknown data, weekly cohort and beta enrollment denominator. Distinguish *speech end -> user-visible voice response / authoritative ACK* timing from pre-existing STT-start -> terminal diagnostic elapsed. Set consent-aware collection quality.
**Targets (English PRD, locked):** successful voice driving sessions >=50% of weekly users (North Star); direct-command success >=85%; manual correction <15%; incorrect execution <3%; weekly return >=30%; Driving Mode reuse >=50%; P95 voice response <2.5 s after end of speech. English MVP additionally specifies crash-free session >=99%, 50-driver/14-day Beta, and lab >=90% for voice. Exact MVP Green/Yellow/Red bands: reuse >=50 / 30–49 / <30; command >=85 / 70–84 / <70; correction <15 / 15–25 / >25; crash-free >=99 / 97–98.9 / <97.
**Output:** `ISSUE_12_METRIC_DICTIONARY.md` (timezone/window/cohort, censored/unknown handling, confidence intervals/small samples, quality flags).
**Exit:** No calculation can quietly treat missing/unknown as success, false-zero wrong executions or successful completed sessions.

## P03 — Locked event schema and privacy gate hardening

**Actions:** Implement the eight externally approved families exactly: `driving_mode_started(entry_source,bt_connected)`; `voice_button_pressed(ephemeral session context where approved,playback_state)`; `intent_detected(intent_type,confidence_bucket)`; `intent_failed(intent_type,failure_code,confidence_bucket,latency_bucket)`; `command_executed(intent_type,latency_bucket)`; `manual_correction(previous_intent,correction_type)`; `player_error(error_code,route_type)`; `session_completed(duration_bucket,commands_count)`. Reconcile internal `voice_intent_outcome` with this schema via **private-only mapping or documented approval gate**; enforce closed codes, max count/bucket definitions, versioned schema, unknown/free-form rejection, no secret/string payload. Avoid exported session UUID/device identity.
**Output:** existing `quality/api/*`, `quality/privacy/*` narrow changes plus `ISSUE_12_EVENT_SCHEMA.md`.
**Exit:** Every exported property/enum is traceable to v1.0 or a signed-off engineering bucket; no extra outbound family/field.

## P04 — Consent, mode gating & privacy-safe lifecycle

**Actions:** Add explicit separately stored opt-in for *non-audio diagnostics* (distinct from microphone OS grant and distinct from remote-cloud STT consent), default disabled/unavailable until reviewed copy/provider/retention. Enforce fresh consent at capture/flush, consent withdrawal and safe NoOp fallback. Preserve PTT just-in-time microphone disclosure and on-device-only STT; remote/cloud STT remains denied without separate provider/destination/purpose/retention/legal approval.
**Output:** `data/api/PreferencesRepository.kt`, `data/datastore/PreferencesDataStore.kt`, Quality consent gate / DI wiring as needed, copy sign-off ledger.
**Exit:** Denial or unset preference cannot leak, upload or affect basic music functions.

## P05 — Authoritative Driving + Voice entry instrumentation

**Actions:** Instrument `driving_mode_started` once on confirmed transition, not recomposition/Bluetooth hint; `voice_button_pressed` once per explicit accepted PTT initiation from both Player/Driving; no event for an unpressed/unauthorized attempt. Use ephemeral local generation/attempt keys for dedup (never transmit identity). Keep surface ownership with existing Driving/Voice components.
**Output:** narrow entry hooks + ownership table and lifecycle cases.
**Exit:** Recreation, foreground/background and rapid taps cannot create duplicate starts.

## P06 — Voice detection, failure, execution and manual correction

**Actions:** Wire `intent_detected` only after accepted deterministic intent mapping; `intent_failed` once for terminal typed failure; `command_executed` only after service-authoritative playback acknowledgement; explicit `manual_correction` only on attributable observed user correction (not automatic inference from unrelated touches or cancellation). Handle retry, one-step Confirming, stale/cancelled generations and six intents without double-counting. Preserve existing P11 safe diagnostic code as internal.
**Output:** narrow `DefaultVoiceSessionCoordinator`, playback receipt & UI instrumentation seams plus `ISSUE_12_VOICE_EVENT_OWNERSHIP.md`.
**Exit:** Late ACK, invalid confirmation and re-entry cannot create success claims or double events.

## P07 — Player error and crash-reporting boundaries

**Actions:** Emit `player_error` only from authoritative service error/recovery path using typed nonpersonal codes and coarse route; deduplicate repeated callback storms. Implement minimal *safe crash classification/reporting* at app/process lifecycle (fatal vs caught player error vs OS process kill). Local safe crash evidence if no approved provider; any remote Crash SDK must be specifically reviewed, disabled by default, and configured to suppress raw throwable/message/stack context, breadcrumbs, user/device/media identifiers and provider automatic collection when they can leak. A fatal/abrupt termination must not be mislabeled a clean session; absence of proof is UNKNOWN, not CRASH_FREE.
**Output:** Quality crash adapter seam, safe crash schema / provider review, and narrow service hooks.
**Exit:** No free-form crash payload, no product behavior change, no invented remote crash provider.

## P08 — Driving session lifecycle & reliable completion

**Actions:** Define start/finish for a *Driving Mode session* independent of Activity recreation. Track bounded command count and coarse duration; emit `session_completed` once only on a legitimate terminal boundary. Persist minimal recovery marker only if needed for consented completeness/possible crash classification; reconcile process death without fabricating completion. Handle explicit exit, rotation, call/focus/Bluetooth safety interruptions, duplicate lifecycle calls, no-media/permission recovery.
**Output:** one session-owner class + lifecycle matrix and session envelope.
**Exit:** No duplicate completion, no destructive MediaStore writes, no UI ownership of the authoritative player.

## P09 — Local event collection, retention and optional transport

**Actions:** Keep `TelemetryEvent -> TelemetryPrivacyGate -> sanitized event -> best-effort sink`. Add bounded app-owned local diagnostic store/aggregator behind consent (document whether events are persisted at all, TTL, pruning, storage limits, encryption/OS backup exclusion if applicable). Recording must be off critical threads. Keep debug/local builds NoOp or deliberately sanitized local-only. Do not upload until service, residence, processing/retention/deletion, privacy notice and legal review are approved. Offline capture/export path for opted-in **aggregates** can support Beta without a product backend.
**Output:** local adapter, deletion interfaces, DI provider switch, approved retention decision log.
**Exit:** Offline/no provider does not affect playback; queues bounded and deletable; no raw audio or user profile.

## P10 — Exact measurement projection & data quality

**Actions:** Build on-device privacy-preserving aggregate counters and fixed buckets for all approved metrics. For P95, instrument a monotonic **end-of-speech** boundary and chosen visible response/ACK end; preserve raw elapsed only transiently, export approved bucket/histogram rather than precise individual timings. Boundary must resolve strict <2500 ms threshold without silently redefining the existing P11 STT-start latency buckets. For incorrect execution, require an explicit user/QA-observed classification; telemetry alone cannot prove wrong execution. Weekly retention/reuse: approved per-device local weekly booleans/counters and **consented aggregate** collection via controlled Beta sampling, without exported stable user IDs; denominator from recruited/participating population, with missing responders separately reported.
**Output:** `ISSUE_12_AGGREGATION_AND_DATA_QUALITY.md`, metric aggregation implementation, completeness/reconciliation rules.
**Exit:** Eight allowlisted families + approved local aggregates can produce three headline answers with declared unknowns; no made-up 50-person cohort.

## P11 — Weekly measurement dashboard (non-product-backend)

**Actions:** Deliver a Product/QA-facing dashboard/report generator that reads approved sanitized aggregate Beta evidence only, no account, streaming or product data backend. Show three panels: USED (sessions, North Star, voice adoption), WORKS (direct success, correction, incorrect execution, P95, player errors, crash-free), RETURNS (weekly return, Driving reuse). Fixed v1.0 thresholds, denominators, sample size, date window, missingness and Go/Iterate/Stop evidence indicator. No green verdict on insufficient/missing data. For cloud quality sink/provider only if separately authorized.
**Output:** `docs/implementation/ISSUE_12_DASHBOARD_SPEC.md`, locally runnable report/dashboard artifact or generator, sample **synthetic only** aggregate fixture.
**Exit:** All three questions visible in one view; can reproduce totals from input with zero user-data leakage.

## P12 — Settings, deletion, education & retention UX

**Actions:** Add truthful Privacy/Diagnostics Settings section and accessible user-controlled deletion of app-owned local history plus local diagnostic data; separately revoke future consent. Confirm action, show status/failure/partial success, retain user-owned MediaStore audio and actual OS microphone permission. If an approved remote provider is later added, explain actual retention/deletion limits; do not falsely promise remote erase. Show just-in-time microphone/diagnostic disclosure and distinct cloud STT processing rules.
**Output:** Settings Compose flow, local clear APIs, accessibility copy, retention/consent sign-off statuses.
**Exit:** No coercion, no deletion of music files, clear consent remains revocable; review required before upload.

## P13 — Source integration, authored test contracts and final QA package (NO EXECUTION)

**Actions:** Review complete diff for scope, duplicate event ownership, eight-event export, privacy and non-blocking paths, 14 Issue #12 acceptance criteria, approved source hierarchy and unresolved #80/#82. Write but **do not execute** source-level JVM/integration/UI test cases for unknown event/field/code injection, personal metadata, crash context, unconsented upload, consent revocation, clear, session/voice dedup, player callback storms, event loss, weekly denominator, P95 boundaries, dashboard threshold and incomplete data. Compose complete Persian owner-device checklist M01... and evidence JSON initialized NOT_RUN, per-AC traceability and source-only review report.
**Output:** `ISSUE_12_TEST_PLAN.md`, `ISSUE_12_PHONE_QA_CHECKLIST_FA.md`, `ISSUE_12_QA_RESULTS.json`, `ISSUE_12_EVIDENCE_INDEX.md`.
**Exit:** No compilation, Actions, APK, test run or fabricated PASS; all known risks and prerequisites exposed.

## P14 — FINAL ONLY: integrated APK, owner phone/vehicle checks, evidence, merge gate

1. Freeze completed P01–P13 source HEAD. If authorized, **one** reproducible build-only `app:assembleDebug` via approved mechanism, no automated `test`, `check`, `lint`, emulator or device tests. Record true commit, APK filename/size/SHA-256/workflow/artifact and installation variant.
2. Supply APK + full Persian M-checklist. Owner personally verifies consent OFF/on/revocation, event boundaries, error/crash evidence, local diagnostics purge without touching music, offline behavior, Voice/Driving/Media3 regressions, dashboard/data-quality and QA in phone/parked vehicle as applicable; **never manufacture a fatal crash on a moving vehicle**.
3. Record owner feedback as actual PASS/FAIL/REVIEW/BLOCKED/NOT_RUN, with provenance. Technical unit/integration tests **remain NOT_RUN** if the owner doesn't separately authorize them; therefore the formal "schema tests catch..." acceptance criterion stays unverified.
4. Report each Issue #12 AC as VERIFIED_PASS / VERIFIED_FAIL / NOT_VERIFIED, name independent provider/privacy/legal gaps, and inherited #80/#82 P0 risk.
5. PR/merge/close Issue #12 only after explicit owner approval and accurate outstanding-gate disposition. Closing an implementation milestone by exception is not the same as a Beta GO.

## Required outbound event contract

| Event | Approved properties | One authoritative source |
| --- | --- | --- |
| `driving_mode_started` | `entry_source`, `bt_connected` | confirmed Driving entry transition |
| `voice_button_pressed` | approved ephemeral/coarse context, `playback_state` | accepted PTT initiation |
| `intent_detected` | `intent_type`, `confidence_bucket` | Voice coordinator accepted deterministic recognition |
| `intent_failed` | `intent_type`, `failure_code`, `confidence_bucket`, `latency_bucket` | Voice coordinator terminal failure |
| `command_executed` | `intent_type`, `latency_bucket` | playback-authoritative command ACK |
| `manual_correction` | `previous_intent`, `correction_type` | explicitly observed attributable UI action |
| `player_error` | `error_code`, `route_type` | Media3 service typed error/recovery |
| `session_completed` | `duration_bucket`, `commands_count` | single Driving session owner |

**Absolute prohibited fields** across analytics, crash, logs, export & repository evidence: microphone buffers, recordings, raw audio, recognized transcripts, artist/album/folder/song names, full or partial private titles, search terms/results, queue contents, file paths, URI/MediaStore IDs, exact position, precise location, contact/call/Bluetooth/vehicle identity, credentials/tokens, unnecessary persistent identifiers, raw throwable messages and arbitrary payload maps.

## Phase completion and evidence policy

- After each requested P01–P13 phase: source change/commit link + changed paths + explicit requirements mapping + pending gates; use SOURCE_COMPLETE_NOT_TESTED when appropriate.
- No change to PRD/MVP metrics or privacy schema without explicit Product/Privacy approval.
- Do not interpret #11's merge or P11's source-authored tests as executed evidence.
- Final acceptance requires owner-observed results and separately reported technical/provider/legal evidence. **As of this planning commit: 0/14 phases implemented here; Issue #12 remains OPEN.**
