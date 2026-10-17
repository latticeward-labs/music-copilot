# Issue #5 / P09 — Six-command education, optional microphone and provider boundary

Status: **IMPLEMENTED — CI VALIDATION BLOCKED, NOT PASS** (17 October 2026).

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

## Validation status (no invented PASS)

- [GitHub Actions run #39031581585](https://github.com/music-copilot-organization/music-copilot/actions/runs/39031581585): **failure; both jobs have null steps; no checkout, compiler or emulator ever started**. Triggered immediately on addition of P09 workflow.
- [GitHub Actions run #39031814318](https://github.com/music-copilot-organization/music-copilot/actions/runs/39031814318): **failure; again both jobs have null steps** after changing to ubuntu-24.04. The same before-step failure is affecting multiple P02–P08 workflows from Issue #5. GitHub Actions/billing/runner allocation cannot be diagnosed with the repository-only app connector; cause UNKNOWN. Do not call it an app regression or claim anything was tested in these runs.
- No successful JVM / Android13 native test for P09 has been retrieved. Mark unit test, Android native, and formal P09 PASS criteria **PENDING_VALIDATION**. P08 remains its separately accepted evidence-scoped phase. The user or organization administrator should review Actions failure banner, Actions permissions and billing/usage controls; only proceed with acceptance after a real job executes and passes.

## PASS gate

`Skip / Driving Entry / Onboarding Start -> zero mic request or capture`, `explicit Practice + OS grant -> single authorized readiness handoff only`, `PermissionDenied/ProviderUnavailable/LanguageUnsupported/CloudConsentDeclined -> truthful distinct manual controls`, `no session/recording before permission`, `no regression in previous onboarding/MediaStore flows`. Code present; actual CI/native execution evidence outstanding.

**Parent Issue #5 remains OPEN, no PR merge, 13 issue-wide ACs remain for P09–P14. P10 NOT STARTED.**
