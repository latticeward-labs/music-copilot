# Issue #10 — Phased execution plan: Push-to-Talk, Voice State Machine, five control intents

> **Execution status:** P00_STATIC_AUDIT_COMPLETED; P01_GOVERNANCE_SOURCE_DELIVERED_UNTESTED; **P02_SOURCE_IMPLEMENTED_UNBUILT; P03_SOURCE_IMPLEMENTED_UNBUILT; P04_SOURCE_IMPLEMENTED_UNBUILT; P05_SOURCE_IMPLEMENTED_UNBUILT; P06_SOURCE_IMPLEMENTED_UNBUILT; P07_SOURCE_IMPLEMENTED_UNBUILT; P08_SOURCE_IMPLEMENTED_UNBUILT; P09_SOURCE_IMPLEMENTED_UNBUILT; P10_SOURCE_IMPLEMENTED_UNBUILT; P11_SOURCE_IMPLEMENTED_UNBUILT; P12_NEXT**. Implementation P12–P13 **NOT_STARTED** in this branch. This is a plan, not proof of completed phases, a compiled APK, executed tests or Issue acceptance.
>
> **Repository baseline audited:** `main` at `e13172683ee8495e26b7c2d56f88444e4969891e` (2026-10-25); source package `MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`.
>
> **Work branch:** `feature/10-ptt-voice-five-control-intents`. **Issue:** [#10](https://github.com/latticeward-labs/music-copilot/issues/10). **Final phone checklist:** [ISSUE_10_FINAL_PHONE_QA_CHECKLIST_FA.md](ISSUE_10_FINAL_PHONE_QA_CHECKLIST_FA.md).

> **P00 evidence (2026-10-25):** [Readiness audit](ISSUE_10_PHASE0_READINESS_AUDIT.md) · [AC01–AC16 traceability/source SHA register](ISSUE_10_PHASE0_TRACEABILITY.json). Static source review completed with open implementation gaps; no build, tests, APK, or runtime PASS. All 16 Issue #10 AC remain NOT_VERIFIED.

> **P01 evidence (2026-10-25):** [P01 execution contract](ISSUE_10_PHASE1_EXECUTION_CONTRACT.md) · [Evidence index](ISSUE_10_EVIDENCE_INDEX.json) · [QA results ledger: 60 M cases, 5×10 L attempts, 16 AC, 10 technical groups — all NOT_RUN or NOT_VERIFIED](ISSUE_10_QA_RESULTS.json) · [Future PR template](../../.github/PULL_REQUEST_TEMPLATE/issue-10-ptt-voice-five-control-intents.md). No PR/Actions/tests/build/APK/merge/issue closure in P01.

## Governing inputs and dependencies

- Uploaded English PRD/MVP, technical blueprint, system architecture, ADR-0002/0004/0005/0006, User Flows Phase 0/1.
- Repository: [PRD](../PRD.md), [MVP](../MVP.md), [Voice Contract](../VOICE_CONTRACT.md), [Software Architecture](../SOFTWARE_ARCHITECTURE.md), [Issue #16 implementation contracts](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md), [Issue #17 handoff](../design-system/ISSUE_17_FINAL_ACCEPTANCE_AUDIT.md).
- Readiness: #7, #9, #16, #17 are closed/merged in repository as of baseline; #10 must independently verify current code and not inherit any unsupported runtime PASS. #11 owns PLAY_ARTIST, fuzzy artist matching and real one-step artist confirmation.
- Existing foundations: `VoiceSessionCoordinator`/States, `DefaultVoiceSessionCoordinator`, `AndroidOnDeviceSpeechToTextEngine`, `AndroidOfflineTextToSpeechEngine`, `RestrictedVoiceIntentMapper`, `VoiceBindings`, `PlaybackGateway`, and #9 Driving microphone handoff.
- **Known gaps to address:** current coordinator lacks a verified Voice-owned playback duck/restore mechanism; Full Player microphone entry must be audited/completed; mapper requires `musico` before direct phrases and rejects absent confidence; on-device recognizer currently supports API 31+ only, with Android 10/11 manual fallback; no complete #10 laboratory/phone execution evidence.
- Voice UI remains origin-owned overlay on Full Player or Driving; MediaSessionService remains sole playback/queue authority.

## Non-negotiable execution policy

1. **P00–P12: implementation, source review, documentation and authoring test source only. NEVER execute tests or compilation, Gradle build, GitHub Actions, emulator/device runs or APK packaging in these phases.** A phase may be source-reviewed/committed; that is NOT runtime PASS.
2. **P13 only:** freeze a cumulative branch HEAD covering **all** P00–P12 changes plus existing main baseline (#4–#9), then run only `:app:assembleDebug` (or a GitHub Actions workflow whose steps are strictly checkout/JDK/SDK/assembleDebug/upload). Do NOT run `test`, `connectedAndroidTest`, `check`, `lint`, emulator or phone test. Build may fail; fix source, rerun **build only in P13**, and record attempts truthfully.
3. Produce **one cumulative APK** (`app-debug.apk`) — not one APK per phase. Record source SHA, APK SHA-256, bytes, exact artifact link, build status and source-to-APK evidence. Do not invent downloadable paths.
4. User alone conducts physical phone tests and sends M01–M60 results. Record factual PASS/FAIL/BLOCKED/NOT_RUN/N/A in GitHub with evidence; no blanket PASS. Authoring JUnit/Compose tests is not equivalent to executing them.
5. Never merge/close #10 or turn a checked acceptance criterion into PASS without evidence or explicitly documented owner-approved exception. Preserve pending technical/laboratory debt accurately.

## Executable phases (P00–P13)

### P00 — Static baseline, scope, dependencies and traceability
**Work:** Compare uploaded authoritative PDFs against current main and #7/#8/#9 playback, route/focus, Driving and microphone code; inventory owner of Voice, permission, UI and Media3; map #10's **16 acceptance criteria** to code and manual/technical evidence; identify conflicts (`musico` prefix, missing confidence, API 29/30 coverage, six-intent source vs five-intent execution). Classify design and policy decisions rather than silently guessing.
**Output:** `ISSUE_10_PHASE0_READINESS_AUDIT.md`, initial AC01–AC16 traceability and actual dependency/source SHA register. No tests/build.

### P01 — Branch governance, evidence ledger and delivery contract
**Work:** Establish the issue-specific branch/file structure, status vocabulary and source ownership; define exact phase handoff rules, baseline, PR strategy and no-test/no-build guard, checklist M01–M60 and lab results schema; keep all product acceptance NOT_VERIFIED.
**Output:** issue-specific execution agreement, evidence index, QA JSON ledger (NOT_RUN), and future PR description template. No tests/build. **P01 SOURCE-DELIVERED 2026-10-25** ([contract](ISSUE_10_PHASE1_EXECUTION_CONTRACT.md)); not implementation/runtime PASS.

### P02 — Single Voice state machine, session generation and transition contracts
**Work:** Implement/repair `Idle -> Listening -> Processing -> [Confirming when applicable] -> Executed | Failed -> Idle`; direct five commands skip Confirming. One app-scoped generation, explicit invalidation, no stale callbacks, atomic start/retry/cancel and terminal cleanup; timeouts <=6,000ms **per listening attempt**, at most one retry, no repeated command dispatch after configuration change. Confirming type stays compatible with #11 but no PLAY_ARTIST handler added.
**Output:** coordinator/reducer contracts, bounded policies, lifecycle ownership and **authored-only** reducer/JUnit cases. No tests/build.

> **P02 evidence (2026-10-25):** [Voice state machine source audit](ISSUE_10_PHASE2_VOICE_STATE_MACHINE_AUDIT.md) · [P02 eight authored-only JUnit scenarios](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue10Phase2VoiceStateMachineTest.kt) · [evidence index](ISSUE_10_EVIDENCE_INDEX.json). Source only, NO compile or tests. AC remain NOT_VERIFIED.

### P03 — Explicit PTT entry for Full Player and Driving
**Work:** Wire actual Full Player microphone to the same app-scoped coordinator already used by #9 Driving; reuse microphone OS permission/VoicePracticeGate and existing NavHost; require a new deliberate tap after permission grant; never start Voice from startup, BT connect, background, navigation, ActivityResult or notification. Prevent double-tap double-session and preserve usable manual controls.
**Output:** Full Player + Driving one-tap entry and shared consent/permission presentation source, with no new Voice Activity or playback owner. No tests/build.

> **P03 evidence (2026-10-25):** [Full Player/Driving PTT source audit](ISSUE_10_PHASE3_PTT_FULL_PLAYER_DRIVING_AUDIT.md) · [12 authored-only PTT cases](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue10Phase3PushToTalkTest.kt) · [QA ledger](ISSUE_10_QA_RESULTS.json). One OS gate/launcher; no capture from ActivityResult. Source only; 16 AC remain NOT_VERIFIED and all manual/lab/technical executions NOT_RUN.

### P04 — Replaceable STT/TTS platform and capability gate
**Work:** Harden `SpeechToTextEngine`/Android on-device recognizer availability, English locale/language pack behavior, cancellation, teardown, exact listening timeout, missing confidence, OEM errors, and retry-safe session identity. Keep `TextToSpeechEngine` replaceable. **No implicit network/cloud STT fallback**; currently unsupported API29/30 should show truthful manual fallback unless separately approved and implemented with consent. Do not claim offline recognition on unverified hardware.
**Output:** robust provider adapters/fakes, typed errors and documented Android 10/12/14+/OEM matrix. No tests/build.

> **P04 evidence (2026-10-25):** [Static STT/TTS audit](ISSUE_10_PHASE4_REPLACEABLE_STT_TTS_AUDIT.md) · [12 new JUnit scenarios (AUTHORED_ONLY / NOT_RUN)](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue10Phase4SpeechProvidersTest.kt). English installed-model check on API33+, Android 10/11 manual-only, offline TTS without cloud fallback. No build, test, Actions, APK. All 16 AC NOT_VERIFIED.

### P05 — Deterministic mapper limited to five direct controls
**Work:** Version ordered normalization, punctuation/spacing and approved English phrases. Support `PLAY`, `PAUSE`, `NEXT`, `PREVIOUS`, `SEEK_BACK` only as executable #10 results. Resolve source discrepancy by accepting bare approved phrases on explicit PTT; `musico` may be accepted as an *optional spoken prefix after tapping*, NEVER an always-on wake listener. Apply bounded configurable BuildConfig threshold; never invent confidence if provider omits it, never guess unknown/unsupported commands or map them to nearest command. Explicitly document/handle low/absent confidence safely; validate direct recognition target at P13. Keep `PLAY_ARTIST` reserved for #11.
**Output:** pure mapper/policy, phrase dictionary, source-only contract checks and authored test cases. No tests/build.

> **P05 evidence (2026-10-25):** [P05 five-intent mapper source audit](ISSUE_10_PHASE5_DETERMINISTIC_FIVE_CONTROL_INTENTS_AUDIT.md) · [25 new JUnit grammar/threshold cases, AUTHORED_ONLY](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue10Phase5DeterministicIntentsTest.kt) · [Updated pre-existing Phase8 source-only grammar assertion](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Phase8VoiceFoundationTest.kt). 19 exact phrases/5 controls, optional *spoken* `musico`, no artist direct or cloud; all 16 AC remain NOT_VERIFIED, all QA NOT_RUN, no build or Actions.

### P06 — Typed playback dispatch, ACK and duplicate execution protection
**Work:** Dispatch exactly one typed `PlaybackCommand` through existing `PlaybackGateway`/MediaSession service; `PLAY` resuming safely, `PAUSE`, `NEXT`, `PREVIOUS` using existing fixed playback rules and `SEEK_BACK = -10,000ms` clamped to zero. Serialize command admission, preserve source snapshot authority, enforce one-shot command identity per session/result, ignore stale late ACK; only publish Executed after acknowledged outcome. Empty queue/end-of-queue/controller error are safe failures, not optimistic success.
**Output:** Voice->gateway adapter and idempotency policy plus authored-only integration cases. No tests/build.

> **P06 evidence (2026-10-25):** [Voice → Media3 ACK and idempotency static audit](ISSUE_10_PHASE6_MEDIA3_DISPATCH_ACK_IDEMPOTENCY_AUDIT.md) · [15 authored-only JUnit service/duplicate/ACK cases](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue10Phase6VoicePlaybackAckTest.kt) · [QA ledger](ISSUE_10_QA_RESULTS.json). Reuses the existing single Media3 Service/Processor, adds exact `command_ack_id` validation for Voice and rejects Voice-only NEXT at last occurrence. No compile/test/Actions/APK, all AC NOT_VERIFIED.

### P07 — Voice listening duck and exact restoration
**Work:** Implement a **Voice-scoped duck lease** through approved playback/audio-focus boundary (not direct UI volume or an extra player). Request before Listening; restore original, currently valid attenuation on success, failure, timeout, Retry switch, Back/cancel, TTS failure, recreation, background and permission interruption. Respect #8 external call/focus/route precedence, avoid double attenuation/un-duck and **never auto-resume a track paused by user or route loss**.
**Output:** source integration, lease ownership/release contract, exception-safe cleanup, authored-only policy tests. No tests/build.

> **P07 evidence (2026-10-25):** [Voice duck/restore/focus source audit](ISSUE_10_PHASE7_VOICE_DUCK_RESTORE_FOCUS_ROUTE_AUDIT.md) · [13 source-only JUnit lease and cancellation cases](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue10Phase7VoiceDuckingLeaseTest.kt). Service-owned gain lease via private Media3 commands; focus/route emergency revocation and 22s TTL; **no tests/Gradle/Actions/APK**. AC still NOT_VERIFIED.

### P08 — Short feedback and nonblocking TTS
**Work:** One brief English sentence maximum per terminal result, no repetitive speech, no audible overlapping utterances; gracefully handle TTS absent, failed or cancelled. TTS is after playback ACK and must not roll back a successful command, mute manual controls or keep the microphone open.
**Output:** feedback catalog/localized strings, fail-open optional TTS adapter and authored cases. No tests/build.

> **P08 evidence (2026-10-25):** [Short offline TTS feedback source audit](ISSUE_10_PHASE8_SHORT_TTS_FEEDBACK_AUDIT.md) · [15 authored-only P08 JUnit cases](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue10Phase8FeedbackTest.kt) · [updated source-only Phase8 test](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Phase8VoiceFoundationTest.kt). One resource-backed sentence on acknowledged success or final failure (retryable failures silent); 2s bound; no Gradle, test, Actions, APK. AC NOT_VERIFIED.

### P09 — Approved WF-08 Voice UI, accessibility and safe manual fallback
**Work:** Bind `Idle/Listening/Processing/Executed/Failed` and available `Confirming` presentation to origin, not navigation; truthful microphone/listening and attempt state; Cancel and one Retry only when allowed; state-specific TalkBack/live-region announcements, 48dp+ controls, 150–200% scaling, focus return to originating mic/manual control, no personal transcripts displayed/logged.
**Output:** Compose source for Full Player and Driving overlays aligned with #17 and authored-only UI tests. No tests/build.

> **P09 evidence (2026-10-25):** [Shared WF-08 Voice UI and accessibility source audit](ISSUE_10_PHASE9_WF08_VOICE_UI_ACCESSIBILITY_AUDIT.md) · [20 authored-only P09 policy/a11y JUnit scenarios](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue10Phase9VoiceOverlayAccessibilityTest.kt). Common inline Driving/Full Player panel; TalkBack labels, error-specific messaging and focus return. All 16 AC NOT_VERIFIED and phone/technical QA NOT_RUN; no Gradle/Actions/APK.

### P10 — Interruption, background and race hardening
**Work:** Cancel STT/TTS/duck on Back/Driving Exit, origin loss, app background according to approved lifecycle, permission revocation, call/focus invalidation and wired/headphone/Bluetooth route loss. Resolve rapid second tap, retry-vs-late-STT, callback-after-cancel, repeat/rotation, background/foreground and multiple ack race. Never leak playback to handset speaker.
**Output:** lifecycle/interruption policy, idempotent generation cleanup and authored-only race tests. No tests/build.

> **P10 evidence (2026-10-25):** [Lifecycle/Interruption/Race source audit](ISSUE_10_PHASE10_LIFECYCLE_INTERRUPTION_RACE_AUDIT.md) · [17 source-only JUnit race cases](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue10Phase10LifecycleRaceSafetyTest.kt). Exact recognizer and utterance teardown identity, app-start admission, no double tap/late mic, focus/BT route cancellation, bounded unDuck receipt drain. All AC NOT_VERIFIED; no Gradle/tests/Actions/APK.

### P11 — Privacy, cloud-consent boundary and diagnostics
**Work:** Reject audio/transcript/file-name persistence, logs, analytics and crash breadcrumbs; use allowlisted intent/result/coarse bucket metadata only if an approved local sink exists; do not require or enable backend/cloud. If cloud STT is proposed later, separate opt-in plus processor/destination/purpose/retention review MUST precede its activation. Ensure deny/manual-only never harms local music playback.
**Output:** explicit privacy invariants, provider/permission diagnostic status and authored-only privacy tests. No tests/build.

> **P11 evidence (2026-10-25):** [Voice privacy, offline consent boundary and diagnostics source audit](ISSUE_10_PHASE11_VOICE_PRIVACY_CONSENT_DIAGNOSTICS_AUDIT.md) · [19 authored-only privacy tests](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue10Phase11VoicePrivacyBoundaryTest.kt). STT result redacted on stringification; strict future cloud disclosure, no approved adapter, typed Settings status, zero-sink analytics; all AC NOT_VERIFIED, no Gradle/JUnit/Lint/Actions/APK.

### P12 — Cumulative source hardening and P13 handoff
**Work:** Static review all P00–P11 diffs, #7/#8/#9 regressions, API29/30 offline availability, STT/TTS interfaces, state mapping, disabled and stale states, source test coverage and all 16 original AC; prepare lab corpus for 5x10 direct commands and M01–M60 manual cases; freeze exclusions for #11. Record **AUTHOR_ONLY/NOT_RUN** for every unit/UI/integration test and **NOT_VERIFIED** for runtime criteria.
**Output:** `ISSUE_10_PHASE12_FINAL_SOURCE_AUDIT.md`, completed traceability and build-only handoff specification. **Still NO compile, build, tests, APK or CI run**.

### P13 — Only cumulative APK + owner-run phone/lab QA + evidence registration
**Work:** (1) Freeze full accumulated HEAD (P00–P12, no per-phase APK); (2) run strictly one **build-only workflow** with `:app:assembleDebug` (repeat only for actual compile repair), retrieve real GitHub artifact and expose a verified download; (3) record actual commit/size/SHA-256; (4) deliver [Persian owner checklist](ISSUE_10_FINAL_PHONE_QA_CHECKLIST_FA.md) and 50-attempt direct-command lab matrix; (5) owner tests on personal Android phone and reports observed outcomes; (6) log each result and issue AC evidence in GitHub; (7) identify any remaining unresolved technical tests as NOT_RUN/DEFERRED — never mark them PASS by assertion; (8) only when owner instructs and acceptance evidence permits, prepare PR, merge, close #10.
**Output:** **one single cumulative `app-debug.apk`**, real downloadable artifact + SHA-256 build manifest, annotated owner results and factual closeout/deferral report.

## Acceptance and quality gate

- AC01 state machine; AC02 explicit PTT; AC03 duck; AC04 <=6-second listening/one retry; AC05 replaceable STT/TTS; AC06 **exactly five** executable #10 control intents; AC07 direct execution no new page; AC08 UI ownership; AC09 short TTS; AC10 fallback; AC11 lifecycle; AC12 **laboratory >=90%** direct-command success before field QA; AC13 privacy; AC14 cloud consent if applicable; AC15 authored and eventually executed mapper/transition/timeout/duplicate tests; AC16 accessible visuals/TalkBack.
- **Owner indoor laboratory:** ten distinct attempts for each of the five approved English commands = **50 attempts**; log attempted transcript phrase, intended command, actual playback action, observed latency and PASS/FAIL without storing spoken audio/transcript in application telemetry. **At least 45/50 correct** for aggregate >=90%; also report **per-intent 10-case counts** rather than concealing one weak command behind aggregate. Do not classify a failed recognizer/unavailable device as success. Lab on supported hardware is a real owner-run test, not an AI-executed test.
- **Do not confuse the lab quality gate with source test execution.** Under the owner's workflow, JVM/Android/Compose tests can be authored but remain NOT_RUN until expressly authorized. A phone PASS does not prove automated tests PASS. Real Android 10/12/14+ and Pixel/Samsung/Xiaomi/vehicle coverage remain explicit open risks if not physically executed.
- #11 PLAY_ARTIST, artist matching/confirmation, wake-word background activation, open chat/LLM, new cloud provider, streaming, new MediaSession and account/backend are **outside #10**.

## Final owner reporting format

`Issue #10 | APK SHA: ... | Device/Android: ... | M01 PASS; M02 FAIL — observed ...; M03 BLOCKED — reason ... | Lab PLAY 9/10, PAUSE 10/10, NEXT 9/10, PREVIOUS 9/10, SEEK_BACK 10/10 => 47/50`.

All statuses start **NOT_RUN**. In GitHub, user observations must be dated, linked to the exact tested APK/source SHA, and not back-filled as fictional verification.
