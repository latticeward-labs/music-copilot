# Issue #5 / P09 — Six-command education, optional microphone and provider boundary

Status: **PASS_SCOPED — VERIFIED REAL WHOLE-GREEN JVM + ANDROID13 CI** (18 October 2026), accepted commit `9fb0603ac6af9a0ca7ed552a873d5b1eba5a59cf`.

## Final P09 acceptance — 18 October 2026 (after personal-account repo transfer)

**PASS_SCOPED on one genuinely whole-green CI run**: [Actions #39122946667](https://github.com/latticeward-labs/music-copilot/actions/runs/39122946667) for production+tests commit `9fb0603ac6af9a0ca7ed552a873d5b1eba5a59cf`. Both `p09-jvm-lint-apk` and `p09-android-microphone-api33` are **SUCCESS**. CI artifact IDs: [JVM/Lint/APK #12135072645](https://github.com/latticeward-labs/music-copilot/actions/runs/39122946667/artifacts/12135072645), [native Android13 OS permission dump/screenshots #12134589753](https://github.com/latticeward-labs/music-copilot/actions/runs/39122946667/artifacts/12134589753).

**Actual JVM evidence:** `testDebugUnitTest`, `lintDebug`, `assembleDebug`, `detekt`, `spotlessCheck`, architecture and privacy self-tests completed; terminal `ISSUE5_P09_JVM_LINT_APK_PASS`. Unit tests verify exact six intents, no microphone on initial/Skip/Driving entry, explicit JIT one-shot grant path, denial, provider unavailable, language unsupported, remote consent declined, lifecycle restore and cancellation, stale callbacks and manual fallback.

**Actual Android 13 evidence:** clean install → native First Use → approved second slide / six commands → explicit Practice Voice → standalone purpose rationale → real Android **Don't allow** dialog, `RECORD_AUDIO: granted=false` → manual fallback → second explicit Practice → real OS **While using the app** selection, `RECORD_AUDIO: granted=true` → Skip → Media access rationale, no application crash. Logs include `P09_ANDROID_NATIVE_RECORD_AUDIO_ALLOW_TAPPED 'While using the app'` and terminal `ISSUE5_P09_NATIVE_JIT_MIC_AND_MANUAL_FALLBACK_PASS_API33`. Native screenshots and `dumpsys package` proofs are in the attached artifact, not substituted with mocked permission state.

**Scope boundary:** Voice capture/execution, real artist resolution, permission details on all Android/OEM devices, P10 cloud disclosures/notification, P12 accessibility and P13 physical QA remain later work. `VoiceSessionCoordinator` remains the single voice owner. No remote STT, auto recording, new account, backend or Media3 Player ownership changes. Full Issue #5 and all 13 original integrated ACs **remain OPEN/unaccepted**; **P10 NEXT**.

## Sources and exactly bounded scope

- Uploaded `ISSUE_5_PHASED_EXECUTION_PLAN(1).md` SHA-256 `32f8095cac6c4f85a9654bd006ca73e6c27f0db3644a5bd4adef847cf738276f`, Phase 9: exactly six approved intents, optional Practice, explicit JIT `RECORD_AUDIO`, separate provider capability/language/cloud consent, manual fallback; Voice execution belongs later issues.
- Uploaded `MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`: ADR-0004 replaceable STT/TTS/VoiceSessionCoordinator and only PTT microphone; ADR-0005 exact six deterministic English intents; UF-01/UF-02/CA-01 permission vs provider separation; Issue #16 Android architecture.
- No new onboarding page, streaming, remote speech, wake phrase listening, VoiceSessionCoordinator ownership, queue/player state, Audio Capture or live command execution added. Existing English `musico` prefix comes from the repository's restricted mapper and is faithfully displayed as examples.

## Implementation

1. `core/application/permission/VoicePracticeGate.kt` delegates one-use microphone authorization to the **existing** P02 `PermissionMachine` with `ExplicitVoicePractice`, `BlockedAction.StartVoicePractice`; future explicit Mic Tap path exposes `StartVoiceCapture` at the gate only, not any recorder. `Idle/Rationale/AwaitingAndroid/ReadyForVoiceHandoff/PermissionDenied/ProviderUnavailable/LanguageUnsupported/CloudConsentDeclined` are disjoint. Duplicate/stale callbacks ignored. Cancel/Back/Skip drop only the optional action; OS authorization is never persisted.
2. `MainActivity`: separate registered `ActivityResultContracts.RequestPermission` for `RECORD_AUDIO`; no launch during bootstrap, replay, Skip or foreground. Practice first shows on-slide purpose and only the explicit confirmation invokes Android. Callback re-reads `checkSelfPermission`; subsequent provider probe occurs **only if granted**. Resume rechecks revocation and existing VoiceSessionCoordinator is canceled on permission loss. Process recreation restores pending OS result registration without replaying a request.
3. `ApplicationBindings`: read-only on-device speech provider check using existing `SpeechToTextEngine`; no second session owner, `coordinator.start()`, remote adapter or cloud fallback. API29/30 correctly report no approved on-device recognizer; successful Android OS permission alone is NOT provider readiness.
4. `AppNavHost` / `FirstUseScreen`: maintain **two** value slides and app-independent manual controls. Show exactly six `PLAY, PAUSE, NEXT, PREVIOUS, SEEK_BACK, PLAY_ARTIST` mapped examples. Optional Practice → explicit rationale → Android permission → separate provider result or manual-only. Accessibility labels and scrollable text controls with 48dp minimum; detailed accessibility audit remains P12.
5. `strings.xml`: explicit English commands, purpose and separate denial/provider/language/cloud fallback messages. Cloud STT is disabled; `RemoteConsentRequired` always fails closed (consent implementation P10). Language support is **not** inferred from OS grant or recognizer presence; an unsupported outcome is separately modeled and fake-tested, while actual per-language detection is deferred to voice integration.

## Tests and source gates

- `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue5Phase9MicrophonePracticeTest.kt`: unit tests for no-mic on initial/Skip/Driving, rationale-before-launch, duplicate callback/request, grant vs denial, independent provider and language failures, cloud decline, OS revocation, interrupted ActivityResult, no capture/VoiceSession from onboarding, and exact six commands.
- `Issue5Phase4OnboardingTest.kt` P04 regression updated to assert optional interactive JIT practice instead of the deliberately disabled P04 placeholder.
- `scripts/issue5_phase9_native_test.sh` E2E API33: clean install; no mic before Practice; six-command slide; purpose and explicit tap; real native OS deny and manual fallback; repeated explicit Practice + OS allow; permission grant OS dump; no app crash/capture; Skip continues to media recovery.
- `.github/workflows/issue-5-phase9-microphone.yml`: architecture/privacy/Spotless/Detekt/lintDebug/testDebugUnitTest/assembleDebug plus Android13 real dialog UI screenshots and OS permission dumps. Runs on ubuntu-24.04 after ubuntu-22.04 failed before starting.

## Historical pre-transfer CI failures — resolved, preserved for traceability

- [GitHub Actions run #39031581585](https://github.com/latticeward-labs/music-copilot/actions/runs/39031581585): **failure; both jobs have null steps; no checkout, compiler or emulator ever started**. Triggered immediately on addition of P09 workflow.
- [GitHub Actions run #39031814318](https://github.com/latticeward-labs/music-copilot/actions/runs/39031814318): **failure; again both jobs have null steps** after changing to ubuntu-24.04. The same before-step failure is affecting multiple P02–P08 workflows from Issue #5. GitHub Actions/billing/runner allocation cannot be diagnosed with the repository-only app connector; cause UNKNOWN. Do not call it an app regression or claim anything was tested in these runs.
- At the October 17 historical checkpoint no JVM/Android13 tests had run. On October 18, run #39122946667 completed fully green as documented above; **those former gates are now accepted within P09**. P08 remains its separately accepted evidence-scoped phase. The user or organization administrator should review Actions failure banner, Actions permissions and billing/usage controls; only proceed with acceptance after a real job executes and passes.

## PASS gate

`Skip / Driving Entry / Onboarding Start -> zero mic request or capture`, `explicit Practice + OS grant -> single authorized readiness handoff only`, `PermissionDenied/ProviderUnavailable/LanguageUnsupported/CloudConsentDeclined -> truthful distinct manual controls`, `no session/recording before permission`, `no regression in previous onboarding/MediaStore flows`. Code, unit/JVM and real Android13 native evidence confirmed via whole-green run #39122946667.

**Parent Issue #5 remains OPEN, no PR merge, original 13 issue-wide ACs remain for integrated later-phase audit. P09 PASS_SCOPED; P10 NOT STARTED.**
