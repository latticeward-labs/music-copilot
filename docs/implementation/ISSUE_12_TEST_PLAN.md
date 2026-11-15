# Issue #12 — P13 Final Source Integration & Authored Technical QA Plan

**Date:** 2026-11-15 · **Phase disposition:** `SOURCE_REVIEW_AND_QA_PACKAGE_AUTHORED_UNCOMPILED_NOT_RUN` · **GitHub branch:** `feature/12-privacy-safe-telemetry-measurement`

> This plan is NOT an executed test report, a privacy/legal signoff, a build record, or actual Closed Beta evidence. **P01–P13: NO Gradle, JVM/Python tests, Android instrumented tests, emulator, CI/Actions, APK, merge or issue closure.** In P14, only one owner-authorized integrated APK build may be performed; do not silently enable automated tests. Owner runs the phone checklist and returns individual observations, which are then recorded in GitHub.

## 1. Source hierarchy and actual inspected evidence

**Exact attached archive** `/mnt/data/MSM2(1)(1).rar` (not a guessed copy) **SHA-256** `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`. RAR archive reopened and 12 original English PDFs extracted/read for P13, including:
- `Music_Copilot_PRD_v1_EN_Clarified.pdf` printed pp.9–10: eight named required event families; no raw voice/personal data; successful Driving Voice North Star ≥50% weekly active; direct success ≥85%; correction strictly <15%; wrong execution strictly <3%; return ≥30%; Driving reuse ≥50%; voice response P95 strictly <2.5s **after real speech end**; local command P95 <1s; ≥99% crash-free started journeys; **critical Player incidents <1 per 100 started sessions**.
- `Music_Copilot_MVP_v1_EN.pdf` printed pp.9–10: actual Beta 50 eligible drivers for 14 days across ≥3 head-unit/vehicle models; distinct non-audio diagnostics informed consent; four source-defined threshold bands; **one-view Used / Works / Returns**. The printed MVP event table lists seven (omits `intent_failed`), whereas the PRD/Architecture and current Issue #12 require **eight**. Product discrepancy documented, not a license for a ninth family.
- `Music_Copilot_System_Architecture_MVP_v1_EN.pdf` (Quality privacy adapter, service-authoritative playback), `Music_Copilot_Code_and_Technical_Documentation_MVP_v1_EN.pdf` (illustrative code), ADR-0001–0006 and both English User Flow volumes. **No PDF establishes a signed-off production analytics vendor, positive retention TTL, short-history repository, concrete remote deletion SLA or actual Beta evidence.**

**Live GitHub** [Issue #12](https://github.com/latticeward-labs/music-copilot/issues/12), [Issue #16 public contracts](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md), [ANALYTICS_AND_PRIVACY](../ANALYTICS_AND_PRIVACY.md), [UF-12](../user-flows/UF-12_SETTINGS_PRIVACY.md), [P01 gap audit](ISSUE_12_SOURCE_AND_GAP_AUDIT.md), [P02 immutable metric dictionary](ISSUE_12_METRIC_DICTIONARY.md), [P03 event schema](ISSUE_12_EVENT_SCHEMA.md), [P04–P12 phase documents](ISSUE_12_PHASE_LEDGER.json) and actual branch-to-`main` GitHub Compare file list/code paths. Current Issue #12 has exactly **14 unchecked ACs**. Linkable source path inventory is in [ISSUE_12_EVIDENCE_INDEX.md](ISSUE_12_EVIDENCE_INDEX.md).

### Integrity principles

- **Service-owned playback** (ADR-0002): only real Media3/MediaController service-acknowledged execution. A UI render, STT transcript, guessed intent or queued result is not a command success.
- **Eight-family outbound, typed values only**: `driving_mode_started`, `voice_button_pressed`, `intent_detected`, `intent_failed`, `command_executed`, `manual_correction`, `player_error`, `session_completed`. Local Issue #11 ninth `VoiceIntentOutcome` is intentionally rejected by the outbound `AllowlistedTelemetryPrivacyGate`. No raw audio/transcript, private filenames/titles/artists, URI, location, contacts, car/BT names, user/device/session stable IDs, Throwable message, stack or arbitrary property maps.
- **Authorization applies twice**: P04 before event capture and at P09 sink delivery/flush; `DiagnosticsCollectionAuthorization.requestImmediateRevocation()` latches deny before async persistence; failed consent loads never authorize collection. Four source approvals remain false; `QualityBindings.PRODUCTION_LOCAL_DIAGNOSTICS_MODE=NoOp` for both release/debug. A saved Granted alone is insufficient.
- **Privacy clear** P12 deletes only approved app-owned RAM diagnostics/queued events/crash/latency. No current short-history DAO/table exists: the disabled History category and typed `UNAVAILABLE` reply do **not** prove AC07 complete. Retain user MediaStore audio, Room library projection, Likes, queue, Bluetooth preferences, separate Cloud Speech consent and playback.
- **Measurement completeness**: W1/W2 person counts cannot be inferred from eight event counts because no stable user/device ID may be exported. Wrong command and correction require QA-labelled actual intent + outcome, not just execution. P10 endpoint-signal→Compose draw is **uncalibrated proxy**, not PRD physical speech-end→perceivable-feedback P95. P11 standalone aggregate report is offline and synthetic fixture is permanently DEMO_ONLY.
- **Non-blocking Quality**: optional event, sink or crash reporter exceptions must not alter state/ACK, pause music, delay player, use a background upload, persist raw fields or grant Android permissions.

## 2. Static cross-phase source integration review (not executed)

| Boundary | Production source paths observed in branch | P13 review finding / pending risk |
|---|---|---|
| App/DI singleton | `app/di/ApplicationBindings.kt`, `app/MainActivity.kt`, `app/MusicCopilotApplication.kt` | One app-scoped quality attachment; P14 must compile/test source, lifecycle and callback threading |
| Quality capture and outgoing v1 | `quality/api/TelemetryEvent.kt`, `quality/privacy/OutboundTelemetrySchemaV1.kt`, `quality/privacy/AllowlistedTelemetryPrivacyGate.kt`, `quality/adapters/BestEffortQualityEventPublisher.kt` | Typed eight event families, ninth denied; P13 authored negative field/code tests; no real collector permission |
| Consent + sink | `core/application/privacy/DiagnosticsConsentPolicy.kt`, `quality/privacy/ConsentGatedTelemetrySink.kt`, `app/di/QualityBindings.kt` | Two checks, default NoOp/four approvals false; runtime capture/flush checks not observed |
| Voice attempt/ACK/correction | `voice/session/DefaultVoiceSessionCoordinator.kt`, `core/application/voice/ObservedVoicePttCoordinator.kt`, `core/application/voice/ExplicitManualCorrectionOwner.kt`, `app/navigation/AppNavHost.kt` | Generation/attempt owner and explicit correction source; ACTUAL ASR/artist stability under #80/#82 still unresolved |
| Driving entry/complete | `app/navigation/DrivingNavigationViewModel.kt`, `core/application/driving/DrivingSessionTelemetryOwner.kt` | Inactive→active and explicit exit; process death yields UNKNOWN, not completed; field dedup needs real owner QA |
| Playback error/crash | `playback/service/MusicPlaybackService.kt`, `playback/service/ServicePlayerErrorEpisodeGate.kt`, `quality/api/SafeCrashEvidence.kt`, `quality/adapters/ConsentGatedCrashEvidenceReporter.kt` | Service origin, typed crash category only, no raw stack; completeness/critical severity and crash→session join unverified |
| Local queue/clear | `quality/adapters/BoundedMemoryDiagnosticsStore.kt`, `quality/api/LocalDiagnosticsStore.kt`, `app/di/ApplicationBindings.kt` | RAM only, source queue drop/eviction counts, epoch purge; no durable Beta frame or finished remote delete path |
| Strict P95 and cohorts | `quality/measurement/QualityMetricProjection.kt`, `quality/measurement/ControlledBetaCohortProjection.kt`, `voice/api/SpeechToTextEngine.kt`, `feature/voice/VoiceSessionPanel.kt` | Real VAD/OEM endpoint signal is not a calibrated physical end; official P95 currently NOT_VERIFIED |
| Owner privacy UX | `feature/settings/privacy/PrivacyDiagnosticsSettingsSection.kt`, `core/application/privacy/LocalPrivacyClearContract.kt`, `app/navigation/AppNavHost.kt`, `app/MainActivity.kt` | Explicit no-default clear/revoke and DataStore ACK callbacks; short history not implemented; TalkBack/200% phone QA pending |
| Offline dashboard | `tools/quality/issue12_dashboard_core.py`, `tools/quality/issue12_dashboard_render.py`, `tools/quality/issue12_weekly_dashboard.py` | Offline/aggregate only; synthetic/unknown never PASS; operator approval declarations **not authenticated** |
| Scope | `app/build.gradle.kts`, ADR-0006 and Compare | No approved analytics SDK/account/backend introduced; no executable proof of no performance regressions |

**Audit limitation:** The compare inventory and relevant current file bodies were read using GitHub's source interface. This is an **uncompiled, non-executed static review**, not a successful Gradle/Kotlin syntax check, runtime race test, independent diff-security signoff, or APK acceptance. Additional unknown integration defects may surface only in P14 or authorized technical QA.

## 3. Test inventory and practical strategy

**Actual GitHub source inventory:** **16** `Issue12Phase3..Phase12` Kotlin source files with **211 authored `@Test fun`** cases; new `Issue12Phase13FinalIntegrationContractsTest.kt` adds **29**; P11 `tools/quality/test_issue12_weekly_dashboard.py` adds **31** `unittest` cases. **Total: 271 authored test methods, ZERO executed**. Source-reading/writing/generation of this plan does NOT count as execution or PASS. Earlier Issue #11/#10 technical tests are separate and **still not validated** by this count.

- **New P13 contracts**: unknown event/code/field injection, ninth outcome, raw URI/artist/transcript, output token length, count overflow, production NoOp, consent denial, post-sanitize revoke/flush, failed sink, queue-clear race, media preservation, Cloud revocation separation, crash safe enums, service authority, Driving explicit completion, P10 P95 proxy not falsely certified, P11 aggregate/Unknown and JIT privacy notices.
- **Existing P03/P04 suites**: schema, closed-code allowlist, denied collection, revoke gating, no exporter. **P05/P06**: current accepted Voice/Driving start, ACK-terminal, correction attribution and stale generation; **P07/P08**: Player/Crash and Driving session owner; **P09**: bounded FIFO/dropped events, full erase/revocation; **P10**: 10 metrics, strict P95, 14-day UTC W1/W2 and provenance; **P11**: source-only Python dashboard; **P12**: selection/confirmation, history nonexistence, persistent callback statuses and accessibility structure.
- **Owner device + independent product evidence:** [121 individually identified cases](ISSUE_12_PHONE_QA_CHECKLIST_FA.md) and [machine-readable all-NOT_RUN QA manifest](ISSUE_12_QA_RESULTS.json): 87 OWNER_PHONE, 15 TECHNICAL_QA, 12 PRODUCT_QA, 7 CONTROLLED_BETA.
- **Test classes have no intrinsic authority**: a `@Test` method can be false, stale or not compile; it must be compiled and run with a recorded tool/version/duration and inspected evidence before any independent QA verdict. Even passing fake-sink unit tests cannot prove actual user-phone events in production NoOp.

## 4. Test design: negative paths must fail closed

| Scenario family | Test contract & required observable | Authority / gates |
|---|---|---|
| Unknown family and ninth Issue #11 outcome | `sanitize`/publish rejects unknown and `voice_intent_outcome`; sink receives zero | Quality / Privacy / AC02/AC10 |
| Unexpected key, property order, arbitrary JSON/string injection | Only exact allowlisted ordered keys and stable token code vocabulary allowed; unknown, reordered or malicious rejected | Quality / Privacy / AC02/AC04/AC10 |
| Unexpected failure, artist, MediaStore identifier and Throwable message | No event includes private song/artist/path/URI, full transcript, precise location, token, raw stack, Bluetooth address or ID | Privacy / AC03–AC05/AC10 |
| Capture/flush & revoked consent | Every attempt checks current effective authorization; revoke closes before DataStore suspend; previously queued events cannot deliver | App / Data / Privacy / AC06/AC07 |
| Clear/queue overflow/oldest eviction/close | Bounded memory + drop counters; epoch prevents old event resurrection; clear on deny; no live queue/MediaStore deletion | Data / Quality / AC07/AC14 |
| Duplicate Driving/PTT on recomposition, recreation, BT hint, rapid tap | Exactly one accepted edge, no event from rejected entry; process force kill/unknown not completion | Android / Voice / AC01 |
| Late STT/Ack/artist retry and manual correction | Only current attempt/generation; service ACK is execution truth; manual correction requires explicit attributable action | Voice / Playback / AC01/AC11 |
| Playback callback storms, control disconnect, crash code | Service error episode dedup, only closed safe errors/crash category; no optional reporter exception impacts playback | Playback / QA / AC03/AC05 |
| Voice P95 under real speech end and real feedback | Strict nearest-rank ceil(0.95n), 2500ms equality FAIL; current VAD/OEM signal→draw is only PROXY, not source GO | Voice / QA / AC11/AC13 |
| Local command P95 and full paths | 1000ms equality FAIL, Media3 ACK boundary; visible UI taps alone insufficient for all local commands | Playback / QA / AC11/AC13 |
| Weekly cohort, corrections and wrong action | NS01 uses same-week distinct active, R01 W1 users, R02 all eligible enrolled; Q01/F01/G01 require ground truth | Product / QA / AC11–AC14 |
| Any missing or invalid evidence | D=0 NOT_MEASURABLE; UNKNOWN, unapproved retention, event loss or synthetic fixture NOT_VERIFIED | Product / Privacy / AC11–AC14 |
| Permission, accessibility & deletion UX | Noncoercive JIT explanation, 48dp, TalkBack, Back/Cancel safe, selected-only clear, partial persisted write retry | Android / UX / Privacy / AC07–AC09 |

## 5. Exact issue acceptance mapping and proof requirements

| AC | Required proof before marking VERIFIED_PASS | Current disposition |
|---|---|---|
| AC01 | Owner-validated unique authoritative event per transition across recreation/rapid taps/Voice retry/Player callbacks | **NOT_VERIFIED** — real emitter inaccessible in production NoOp; owner/device and authorized fake sink QA pending |
| AC02 | Real serialized eight-family envelopes match frozen source fields | **NOT_VERIFIED** — source allowlist observed; technical tests unrun |
| AC03 | Actual sampled allowlisted intent/bucket/route/error codes (not raw exception) | **NOT_VERIFIED** — test execution pending |
| AC04 | Privacy adversarial suite and confirmed no prohibited content, including future providers/crash/report | **NOT_VERIFIED** — privacy/legal review unapproved |
| AC05 | Independently verified non-sensitive crash context, typed reasons, complete crash/session attribution | **NOT_VERIFIED** — current NoOp and no durable crash attribution |
| AC06 | Real consent gating and no unsafe uploads/SDKs in built APK, no performance regression | **NOT_VERIFIED** — P04 approvals FALSE, P14 APK not built |
| AC07 | Settings actual owner-controlled **history and diagnostics** clearing, consent rollback and persistence failures, preservation proof | **NOT_VERIFIED** — short history owner DOES NOT EXIST; diagnostic source only |
| AC08 | P12 clear mic and non-audio diagnostic disclosures tested with real UI accessibility | **NOT_VERIFIED** — UI source only |
| AC09 | Separate approved Cloud provider consent + retention if enabled; or proved no cloud STT + truthful disabled copy | **NOT_VERIFIED** — no provider installed and no phone proof |
| AC10 | Tests **executed** and reject unknown values/fields / prohibited raw data, with logs | **NOT_VERIFIED** — 271 test sources authored, ZERO run |
| AC11 | Correctness/correction/wrong/P95/player/crash/reuse/return with real QA denominators, not approximate event counts | **NOT_VERIFIED** — incomplete/acoustic-only proxy and no real cohort |
| AC12 | One USED/WORKS/RETURNS dashboard output rendered and inspected; not only renderer source | **NOT_VERIFIED** — generator NOT executed |
| AC13 | Exact PRD/MVP thresholds incl equality, fixed over weeks and report evidence | **NOT_VERIFIED** — source fixed, not runtime verified |
| AC14 | Complete/reconciled 50/14/3 evidence + consent and governance sufficient for human Go/Iterate/Stop | **NOT_VERIFIED** — no actual Beta, blockers #80/#82 |

## 6. Blocking risks and explicit signoff register

**P0 product quality:** [#80](https://github.com/latticeward-labs/music-copilot/issues/80) open: owner controlled lab Moonshine/Silero Voice **24/50 = 48%**, below **45/50=90%** lab gate, not an Issue #12 retest. [#82](https://github.com/latticeward-labs/music-copilot/issues/82) open: artist punctuation/ambiguity instability, incomplete voice/model confidence and missing controlled connectivity QA; do not silently PASS.

**Privacy/retention:** no approved final exact notice version, positive retention TTL, diagnostic provider adapter, Legal decision, vendor transport or external deletion SLA. The **four P04 production approver booleans remain false**, so data is not collected. A disabled setup is safe but does **not** fulfill the data-completeness Beta decision by itself.

**Data coverage:** No independently approved 50 eligible drivers/14 days/≥3 head units, active unique-week person aggregates, Q01 intention truth, F01 observed correction ground truth, G01 wrong-action QA, crash-free cross-process joins, critical severity code classification, validated actual physical speech-end/acoustic-to-response latency or complete P02 accepted local controls. Unreviewed QA input remains NOT_VERIFIED.

**In-app clear:** current Room schema has `LibraryRow` only, not a ShortHistory owner. The UI correctly disables History rather than deleting private music or queue; **full AC07 remains blocked** until product explicitly resolves whether/when the data category exists and how it is retained/deleted.

**Build/review:** P13 authored source never compiled. P14 must verify actual app compile/integration and ensure no duplicate Kotlin signature, Android Compose import, instrumented permission, UI or runtime lifecycle errors. No GitHub PR/merge/Issue close before owner decision and transparent AC exceptions.

## 7. P14 handoff policy, evidence provenance and decision gate

1. Freeze actual feature-branch HEAD only after P13 docs/qa manifest are complete. Record commit SHA, origin `main` base, and changed-file inventory. **Do not change PRD/MVP, business definitions or privacy gate to mask test gaps.**
2. With owner approval, run **exactly one integrated build-only `app:assembleDebug`** without automated tests/check/lint/emulator. Record build mechanism, toolchain, real logs, APK SHA-256, filename, artifact ID and source commit. If compilation FAILS, do not fabricate an APK; correct source with exact failure evidence and reauthorize the next build if needed.
3. Give owner APK + [M001–M121 Persian checklist](ISSUE_12_PHONE_QA_CHECKLIST_FA.md). Owner tests on their own Android phone/parked vehicle, no intentional dangerous crash or interaction while driving, with privacy-sanitized evidence.
4. Record each observation to [`ISSUE_12_QA_RESULTS.json`](ISSUE_12_QA_RESULTS.json) using explicit **PASS / FAIL / REVIEW / BLOCKED / NOT_RUN**, checked-at timestamp, APK SHA and non-identifying summary. A code-authored test **is not** a passed test. Technical unit/integration execution requires a separate owner go-ahead; if not authorized, AC10 cannot be VERIFIED_PASS.
5. Ask Product/Privacy/Legal/Playback/Voice/QA to sign off genuine remaining gates, #80/#82 status, and AC07 history owner. For Go/Iterate/Stop require actual independent Beta datasets; if absent, result is **INSUFFICIENT_EVIDENCE/BLOCKED**.
6. Only on explicit owner approval, with truthful open AC/risk disposition, consider PR, merge and closure of Issue #12. A merge-by-exception is **not** Beta GO, cannot erase failures and never changes the locked thresholds.

**P13 EXIT:** P13 cross-phase source review, 29 new source-authored technical tests and 121-case QA handoff package committed. No test executions, no phone claims, no APK, and all 14 issue criteria remain NOT_VERIFIED. Next and ONLY APK/build phase is P14.
