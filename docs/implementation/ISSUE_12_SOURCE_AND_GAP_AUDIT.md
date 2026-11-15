# Issue #12 / P01 — Source, architecture, dependency, contract & gap audit

**Recorded:** 2026-11-15  
**Repository:** [latticeward-labs/music-copilot](https://github.com/latticeward-labs/music-copilot)  
**Issue:** [#12](https://github.com/latticeward-labs/music-copilot/issues/12) — OPEN  
**Branch:** `feature/12-privacy-safe-telemetry-measurement` (existing P01–P14 planning branch)  
**Reference main:** `2831de126a3968c57eeaac63c355c8b42ae7acac` (verified by GitHub compare before P01)  
**Status:** **P01 SOURCE/DOCUMENTATION COMPLETE; P02–P14 NOT STARTED; Issue #12 acceptance NOT VERIFIED.**

## 1. Scope & source verification

Exact owner-uploaded `MSM2(1)(1).rar`, SHA-256:

```text
0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8
```

RAR5 opened and its **12 English PDFs** directly extracted/read. The PDFs, not a Farsi retranslation, are the product/architecture authority. The repository's approved #16 baseline and current implementation are the engineering authority. No source, PDF or roadmap alone proves device functionality.

| Attached PDF (exact filename) | Controlled concern for Issue #12 |
|---|---|
| `Music_Copilot_PRD_v1_EN_Clarified.pdf` | Product privacy/metrics, eight named required event families (printed p.10); immutable success targets |
| `Music_Copilot_MVP_v1_EN.pdf` | Beta protocol 50 drivers / 14 days / 3+ head units, acceptance bands, telemetry table, one-view dashboard (printed pp.9–10) |
| `Music_Copilot_System_Architecture_MVP_v1_EN.pdf` | Quality adapter, typed event → privacy gate → non-authoritative sink, external-provider gates (printed pp.16–17) |
| `Music_Copilot_Code_and_Technical_Documentation_MVP_v1_EN.pdf` | Code-level architecture/context; supplementary to approved GitHub #16 baseline |
| `ADR-0001_Native_Android_Platform_Revised.pdf` | Android-only v1.0 boundary |
| `ADR-0002_Service_Owned_Media3_Playback.pdf` | Service owns real playback and error truth; Quality never owns the player |
| `ADR-0003_Local_First_Data_Boundaries_Revised.pdf` | Consent, local-data ownership, retention/deletion and forbidden telemetry (sections 5–11) |
| `ADR-0004_Replaceable_Voice_Provider.pdf` | Replaceable STT/TTS, cloud consent and coarse voice diagnostics |
| `ADR-0005_Restricted_Deterministic_Intent_Mapping.pdf` | Six bounded intents, no raw transcript in telemetry, result/latency buckets |
| `ADR-0006_No_Backend_Account_or_Streaming.pdf` | No product backend, account, streaming or data sync |
| `Music_Copilot_User_Flow_Phase_0_Foundation_v1.0_EN.pdf` | First-use, flow/permission and privacy ownership |
| `Music_Copilot_User_Flow_Phase_1_First_Readiness_v1.0_EN(4).pdf` | Microphone/provider/cloud consent separation, revocation and first-readiness data minimization |

Repository sources checked: [PRD](../PRD.md), [MVP](../MVP.md), [Analytics and Privacy](../ANALYTICS_AND_PRIVACY.md), [Software Architecture](../SOFTWARE_ARCHITECTURE.md), [Implementation Contracts](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md), [Runtime Policies](../architecture/RUNTIME_POLICIES.md), [UF-12 Settings/Privacy](../user-flows/UF-12_SETTINGS_PRIVACY.md), [Issue #12](https://github.com/latticeward-labs/music-copilot/issues/12), [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16), and [P01–P14 plan](ISSUE_12_PHASED_EXECUTION_PLAN_FA.md).

**Precedence:** approved attached English PRD/MVP > supplemental English System Architecture/ADRs > approved GitHub #16 implementation baseline > code as-is. A discrepancy is documented and sent to Product/Privacy owners, not silently reconciled.

## 2. Verified current dependency/module baseline

| Boundary | Observed baseline | P01 decision |
|---|---|---|
| App | Android `:app`, Kotlin/Compose, minSdk 29, target/compile SDK 36, Java 17 | Keep a single app modular monolith; add no module in P01 |
| Media | Media3 ExoPlayer + MediaSessionService | Playback/queue/ACK remains service-authoritative |
| Local persistence | Room, DataStore, MediaStore | Diagnostic store/retention **not implemented**; no media index in diagnostic payload |
| Voice | Local Moonshine/Silero via `sherpa-onnx-2.0.0.aar`, replaceable STT/TTS boundary | Do not modify recognizer, VAD, mapper or confidence policy in P01 |
| DI | Existing application composition root / manually wired `QualityBindings` | No new SDK/provider/dependency in P01; preserve root owner |
| Quality | Typed events + privacy gate + BestEffort publisher + `NoOpTelemetrySink` | Not a production measurement system |
| External diagnostics | `QUALITY_EXTERNAL_DIAGNOSTICS_ENABLED=false`; `QUALITY_CRASH_UPLOAD_ENABLED=false`; no installed crash/analytics SDK established in `app/build.gradle.kts` | No network/export/provider activation without consent, retention and Product/Privacy/Legal approval |
| Build/test policy | Existing source tests/workflows exist historically | **P01–P13: do not run tests, Gradle, Actions, emulator or APK**. P14 owner-authorized single integrated build only |

## 3. Path-by-path code inventory / planned narrow change points

The paths below were inspected on the Issue #12 branch (which initially differed from main only by the P01–P14 plan). No app source was changed for P01.

| Current path | Observed authority / gap | Planned phase(s); responsible role |
|---|---|---|
| `app/src/main/java/com/latticeward/musiccopilot/quality/api/TelemetryEvent.kt` | Eight original typed families **plus ninth** `VoiceIntentOutcome` from #11; some event strings remain code-allowlisted at gate | P03 schema and internal ninth-event disposition; Privacy + Android/Quality |
| `app/src/main/java/com/latticeward/musiccopilot/quality/api/TelemetryPorts.kt` | `SanitizedTelemetryEvent`, `TelemetryPrivacyGate`, `TelemetrySink` exist; no managed retention contract | P03/P09; Quality + Data |
| `app/src/main/java/com/latticeward/musiccopilot/quality/privacy/AllowlistedTelemetryPrivacyGate.kt` | Serializes **nine** families, including `v1\|voice_intent_outcome`. Closed strings/buckets checked for original events; `commands_count` max 1000 is a local engineering decision, not a signed-off product limit | P03: export allowlist = eight until approval; bucket/code review; Privacy + Product |
| `app/src/main/java/com/latticeward/musiccopilot/quality/privacy/VoiceIntentDiagnosticPolicy.kt` | #11 confidence/latency bucket policy; timing from STT attempt to terminal state is **not automatically** PRD speech-end → response P95 | P02/P06/P10: preserve diagnostic but define distinct response-time clock; Voice + QA |
| `app/src/main/java/com/latticeward/musiccopilot/quality/adapters/BestEffortQualityEventPublisher.kt` | Calls privacy gate then sink, catches non-cancellation failures, no event persistence/retry | P04/P09: consent gating, nonblocking local persistence, bounded loss policy; Quality + Data |
| `app/src/main/java/com/latticeward/musiccopilot/quality/noop/NoOpTelemetrySink.kt` | `tryEmit` always false, intentionally no analytics | P04/P09: keep safe fallback; Quality |
| `app/src/main/java/com/latticeward/musiccopilot/app/di/QualityBindings.kt` | `sink() = NoOpTelemetrySink()`, no provider installed | P04/P09: narrow DI switch after consent/provider review; Android/Quality |
| `app/src/main/java/com/latticeward/musiccopilot/app/di/ApplicationBindings.kt` and `app/di/VoiceBindings.kt` | One application-scoped Voice owner, typed P11 diagnostic callback → Quality publisher; not an eight-event measurement pipeline | P05/P06/P08: attach owner-controlled hooks; Android/Voice |
| `app/src/main/java/com/latticeward/musiccopilot/voice/session/DefaultVoiceSessionCoordinator.kt` | Single cancellable Voice generation and #11 artist outcome hook; no verified generic event lifecycle for eight telemetry families | P05/P06/P10: accepted PTT, terminal intent & ACK, monotonic response markers; Voice |
| `app/src/main/java/com/latticeward/musiccopilot/app/navigation/DrivingNavigationViewModel.kt` | Activity-scoped navigation state; recreation survives configuration; new process starts inactive. Not a persistent Driving telemetry session owner | P05/P08: authoritative confirmed entry/exit hook, dedicated non-UI Quality session owner; Android |
| `app/src/main/java/com/latticeward/musiccopilot/feature/driving/DrivingModeScreen.kt` and `feature/voice/VoiceSessionPanel.kt` | Render interactive UI; recomposition cannot be used to count unique starts | P05/P06/P12: UI only passes explicit accepted actions, never counts render/recomposition; Android/UX |
| `app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt` | Single MediaSessionService + service-owned runtime/recovery; error/ACK truth belongs here | P06/P07: narrow authoritative executed/error hook, preserve safety, Audio Focus/route; Playback |
| `app/src/main/java/com/latticeward/musiccopilot/data/api/PreferencesRepository.kt` | `AppPreferences` stores `cloudSpeechConsent`; **no separate non-audio diagnostics-consent field** | P04/P12: distinct default-off opt-in; Data/Privacy |
| `app/src/main/java/com/latticeward/musiccopilot/data/datastore/PreferencesDataStore.kt` | Persists cloud STT consent and other settings; no diagnostic consent/retention state | P04/P09/P12: approved local consent, revocation & clear API; Data |
| Settings Compose components + [UF-12](../user-flows/UF-12_SETTINGS_PRIVACY.md) | UX contract defines separate history/diagnostic clearing and consent; full production diagnostic clear/report surface **not demonstrated** by current Quality paths | P12; Android/UX/Privacy |
| `app/build.gradle.kts`, `gradle/libs.versions.toml` | Diagnostic-upload flags false; no approved cloud crash exporter integrated | P07/P09 only if reviewed; no dependency change in P01 |
| `docs/ANALYTICS_AND_PRIVACY.md` | Central reviewed eight-event allowlist, prohibited fields and nonauthoritative boundary | P02/P03/P04/P12; normative product/privacy scope, not permission to add event families |

No instrumentation must read transcript, artist name, song title, URI, queue, file path, Bluetooth identity or any arbitrary `Throwable.message` to build a diagnostic. No change may move playback truth to UI or Quality.

## 4. Event contract freeze and discrepancies

**External v1.0 approved: exactly eight families** (English PRD p.10; GitHub central privacy contract and Issue #12):

| Family | Currently permitted coarse fields |
|---|---|
| `driving_mode_started` | `entry_source`, `bt_connected` |
| `voice_button_pressed` | approved **ephemeral/coarse** `session_id` only if Privacy approves its representation; `playback_state` |
| `intent_detected` | `intent_type`, `confidence_bucket` |
| `intent_failed` | `intent_type`, typed `failure_code`, `confidence_bucket`, `latency_bucket` |
| `command_executed` | `intent_type`, `latency_bucket` |
| `manual_correction` | `previous_intent`, `correction_type` |
| `player_error` | typed `error_code`, coarse `route_type` |
| `session_completed` | `duration_bucket`, bounded `commands_count` |

**D01 — Source omission, not invented permission:** English MVP printed p.10 Telemetry table lists **seven rows** and omits `intent_failed`; English PRD printed p.10 *Required Events*, System Architecture printed p.16, repo `ANALYTICS_AND_PRIVACY.md` and Issue #12 each explicitly require **eight**. Preserve eight; Product/Privacy review may document the MVP table omission, not alter the approved PRD.

**D02 — Current code has a ninth local diagnostic:** Issue #11 P11 introduced `voice_intent_outcome` and the present sanitizer can encode it as a ninth event, but **no English product PDF specifies outbound export of this family**. Production sink remains NoOp, so no live export is asserted. P03 must block ninth-family *outbound* capture by default or keep it strictly internal while projecting to approved eight; **no new cloud sink may be attached until resolved and reviewed**. Do not erase #11 runtime behavior in P01.

**D03 — `session_id` ambiguity:** MVP allows a session_id in `voice_button_pressed`; code currently only emits `playback_state` (no session id). Avoid exporting a stable device/user/correlation identifier. Require a Privacy-approved non-identifying ephemeral strategy or documented omission, with no silently invented identifier.

**D04 — Bucket/measurement mismatch:** P11 `VoiceIntentDiagnosticPolicy` measures attempt/terminal latency buckets. PRD voice response target is **P95 strictly less than 2.5 seconds measured after end of speech**, so P10 needs a separate, truthful measurement boundary. Numerical confidence and latency bucket cutoffs are engineering policies, not automatically frozen PRD product definitions.

**D05 — False outcome risk:** Real command execution requires service-authoritative acknowledgement. No generic `command_executed` can be inferred from displaying Executed, an intent guess, a successful STT callback or merely tapping Play; likewise incorrect execution and manual correction require honest attribution.

## 5. Frozen product/Beta measurement obligations

| Measure | Locked requirement | Source |
|---|---|---|
| North Star | successful Driving session with ≥1 Voice command / weekly users ≥50% | English PRD p.10 |
| Direct-command success | ≥85% | English PRD p.10, MVP p.9 |
| Manual correction | **<15%** | English PRD p.10, MVP p.9 |
| Wrong/incorrect execution | **<3%** | English PRD p.10 |
| Weekly return | ≥30% | English PRD p.10 |
| Driving Mode reuse | ≥50% | English PRD p.10, MVP p.9 |
| Voice response P95 | **<2.5 s after speech end** | English PRD; repo PRD |
| Crash-free session | ≥99% (Green; 97–98.9% Yellow; <97% Red) | English MVP p.9 |
| Command success bands | ≥85% Green; 70–84% Yellow; <70% Red | English MVP p.9 |
| Reuse bands | ≥50% Green; 30–49% Yellow; <30% Red | English MVP p.9 |
| Correction bands | <15% Green; 15–25% Yellow; >25% Red | English MVP p.9 |
| Beta protocol | 50 qualified drivers, 14 days, ≥3 distinct vehicle/head-unit models; separate informed consent for non-audio diagnostics | English MVP p.9 |
| Dashboard | one view: **Is it used? Does it work correctly? Does the user return?** | English MVP p.10 |
| Local command latency / stability | local P95 <1 s; fewer than one critical player error per 100 sessions | English PRD non-functional requirements |

Green/Yellow/Red **bands** apply only to metrics for which MVP actually defines bands. P02/P10 must declare sampling units, denominator, censored/crash sessions, missed events and UNKNOWN rather than treating absent data as zero incidents.

## 6. Definition of Ready, inherited blockers and approval gate

| Gate | Observed state | Consequence |
|---|---|---|
| #16 architecture/privacy Quality contracts | CLOSED/APPROVED | Baseline exists; preserve ownership |
| #4 Android foundation | CLOSED; historic tests/exception caveats remain separate | Code baseline available, not new #12 QA evidence |
| #9 Driving | CLOSED with documented deferred checks | Hook seam exists; no blanket field/device PASS |
| #10 PTT | CLOSED by explicit implementation exception; **#80 OPEN** | Voice lab **24/50 (48%)**, required ≥45/50 (90%) **FAIL**; P0 Beta blocker |
| #11 PLAY_ARTIST | CLOSED by implementation exception; **#82 OPEN** | Artist edge-case FAIL/REVIEW/BLOCKED and lab/technical QA outstanding; P0 blocker |
| #12 P01 source authority and gap map | DOCUMENTED_SOURCE_ONLY | This phase's artifact is complete; not feature acceptance |
| Approved non-audio diagnostics consent copy | **BLOCKED / NOT EVIDENCED** | P04/P12 cannot claim user-approved copy |
| Exact diagnostic retention duration / deletion policy | **BLOCKED / NOT EVIDENCED** | P09 cannot enable storage/upload without approval |
| Cloud telemetry/crash provider & DPA/privacy/legal review | **BLOCKED / NOT EVIDENCED** | Keep local/NoOp fail-closed; cannot invent service/vendor |
| Cloud STT destination/purpose/retention + separate permission | **NOT APPROVED** for any remote processing | Remain local Moonshine/Silero; no cloud switch |
| Nine-vs-eight export discrepancy and ephemeral `session_id` | **OPEN PRODUCT/PRIVACY REVIEW** | P03 must fail closed on ninth export and risky IDs |
| Issue #12 14 acceptance criteria | **ALL NOT VERIFIED** | No tests/phone QA/measurement evidence yet |

Owners: Product/Delivery — threshold/sample/Go–Iterate–Stop and user consent copy; Privacy/Security — allowlist, retention, vendor/delete; Voice — PTT and response-timing truth; Playback — service errors/ACK; Data — consent/store/clear; Android/UX — Driving/Settings instrumentation; QA — evidence/reconciliation. These are role assignments for future decisions, **not fabricated human signatures or approvals**.

## 7. Per-Acceptance Criterion traceability (Issue #12 exact order)

`NOT_VERIFIED` means neither source implementation nor practical test evidence has been accepted for that Issue #12 criterion.

| AC | Issue #12 requirement | Planned authoritative seam/owner | Governing source | P-phase | State |
|---|---|---|---|---|---|
| AC01 | One authoritative emitter; no recreation/retry duplicates | DrivingNavigation / Voice coordinator / service; Android+Voice+Playback | #16, PRD, ADR-0002 | P05–P08, P13 | NOT_VERIFIED |
| AC02 | Allowed event properties, enums/buckets | TelemetryEvent / Allowlisted gate; Quality+Privacy | English PRD/MVP, analytics contract | P03/P13 | NOT_VERIFIED |
| AC03 | Intent/confidence/latency/error/coarse route only | Gate and mapping; Voice+Privacy | ADR-0003–0005 | P03/P06 | NOT_VERIFIED |
| AC04 | Reject raw voice, transcripts, media, precise location, identifiers | Gate, storage, crash adapter; Privacy+Security | ADR-0003, PRD, #16 | P03/P07/P09/P13 | NOT_VERIFIED |
| AC05 | Crash minimal non-sensitive context | Crash classification/adapter; Playback+Quality | PRD/Architecture | P07 | NOT_VERIFIED |
| AC06 | Safe nonproduction/no provider quality behavior | QualityBindings / BuildConfig; Android+Privacy | #16, privacy contract | P04/P07/P09 | NOT_VERIFIED |
| AC07 | Clear history and diagnostics in Settings | Preferences + history/diagnostic store; Data+UX | UF-12, ADR-0003 | P09/P12 | NOT_VERIFIED |
| AC08 | Clearly explain microphone/diagnostic collection | Settings + PTT disclosure; UX+Privacy | PRD/MVP, ADR-0004 | P04/P12 | NOT_VERIFIED |
| AC09 | Separate cloud STT consent+disclosure if used | CloudSpeechConsent + provider gate; Voice+Privacy | ADR-0004, UF-12 | P04/P12 | NOT_VERIFIED |
| AC10 | Schema validation/tests reject prohibited/invalid | Quality gate + source test contracts; Quality+QA | #12, ADR-0003 | P03/P13/P14 | NOT_VERIFIED; no tests run |
| AC11 | Cover command/correction/wrong execution/latency/player/crash/reuse/return | Measurement engine + owner QA; Product+QA | PRD/MVP | P02/P05–P10 | NOT_VERIFIED |
| AC12 | Dashboard answers three questions together | Aggregate report generator; Product+Data | English MVP p.10 | P10/P11 | NOT_VERIFIED |
| AC13 | Immutable PRD/MVP threshold semantics | Metric dictionary+dashboard; Product+QA | English PRD/MVP | P02/P10/P11 | NOT_VERIFIED |
| AC14 | Completeness sufficient for GO/ITERATE/STOP | Data-quality/consent-aware coverage & ledger; Product+QA | PRD/MVP | P10/P11/P13/P14 | NOT_VERIFIED |

## 8. Risk register & owner actions

| Risk | Severity / owner | Evidence | Follow-up / gate |
|---|---|---|---|
| R01 ninth `voice_intent_outcome` accepted by serializer, absent from official eight-family export | HIGH / Privacy+Voice | Current gate + English PDF/central contract | P03: keep internal or deny external export; review required |
| R02 missing separate diagnostics opt-in | HIGH / Privacy+Data | `AppPreferences` has cloud STT consent only | P04: distinct default-disabled gate before any capture |
| R03 undocumented retention/provider/deletion SLAs | HIGH / Product+Privacy | UF-12 explicitly defers these | P04/P09/P12: no unreviewed SDK/upload or invented retention |
| R04 missing crash-free proof; process death ≠ clean completion | HIGH / QA+Quality | No established crash service/session lifecycle | P07/P08/P10: UNKNOWN where unprovable |
| R05 UI recomposition/Voice retry/late ACK may double-count | HIGH / Android+Voice+Playback | Multiple lifecycle entry points | P05/P06/P08: single owner + dedup |
| R06 wrong P95 timing boundary | HIGH / Voice+QA | P11 STT-attempt timing vs PRD speech-end target | P02/P10: distinct monotonic clock/explicit unknowns |
| R07 weak voice model actual success | **P0** / Voice+QA | #80 24/50, 48%; below ≥90% lab gate | Keep Beta readiness blocked, never derive PASS from analytics |
| R08 artist ambiguity instability and unrun QA | **P0** / Voice+QA | #82 open with FAIL/REVIEW/BLOCKED | Track independently; don't hide in aggregates |
| R09 unstable denominators/small opt-in 50-driver cohort | HIGH / Product+QA | English Beta design vs uncollected actual evidence | P02/P10/P11: distinguish zero vs no data and selection bias |
| R10 SDK/network / raw stack traces leak personal data | HIGH / Privacy+Android | no provider approved; current safe flags false | P07/P09: provider-specific privacy/legal decision or local-only |

## 9. Phase P01 exit and strict non-claims

- [x] Exact archived source package read and SHA-256 fixed.
- [x] Original English PRD/MVP/ADRs and repo #16 architecture authority linked.
- [x] Current app dependencies, Quality, Voice, Driving, service, DI and Preferences entry points identified.
- [x] Eight approved export families compared with current nine typed/encoded families; discrepancies documented.
- [x] All fourteen Issue #12 ACs mapped to sources, existing paths/seams, owners and implementation phases.
- [x] DoR, #80/#82 inherited P0 problems and provider/privacy/legal approvals recorded without false signoff.
- [x] Machine-readable `ISSUE_12_PHASE_LEDGER.json` created; P02–P14 NOT_STARTED; all AC NOT_VERIFIED.

**No production code changed; no PR created, merge, Issue close, Gradle invocation, GitHub Actions, automated/manual test, emulator, APK build, device check, provider selection or release approval.** Documentation completeness is not telemetry quality, privacy/legal approval, or Beta evidence. Next permitted implementation stage is P02 per the frozen plan.
