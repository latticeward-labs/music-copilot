# Issue #9 P07 — Driving Mic / Voice handoff: source audit

Date: 2026-10-24 | Gate: PASS_SCOPED_VOICE_HANDOFF_SOURCE_NOT_TESTED | Next: P08

## Source and authority

Re-uploaded MSM2(1)(1).rar verified SHA-256 0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8, unchanged from P00. Consulted English PRD/MVP, ADR-0004 replaceable PTT voice, ADR-0005 restricted intents, ADR-0002 service playback, and existing GitHub API/Voice code. P07 follows WF-07/WF-08/UF-07/UF-11; Issue #10 owns full live STT/ducking/E2E and #11 artist resolution.

## Actual P07 changed files and source boundaries

| Source | Implemented |
|---|---|
| app/src/main/java/com/latticeward/musiccopilot/core/application/driving/DrivingVoiceHandoffPolicy.kt (new) | Pure Driving origin, local-only STT support, OS-grant and session-collision gate; missing grant is an explicit user-initiated permission rationale, unavailable engine disables capture |
| app/src/main/java/com/latticeward/musiccopilot/app/MainActivity.kt (modified) | Reuses EXISTING VoicePracticeGate and single ActivityResult RECORD_AUDIO launcher; separate transient Driving permission presentation; OS grant re-observed; Android result NEVER calls coordinator.start; a fresh explicit second Mic tap is required. A pending native result may be restored without auto-start. Cancels Driving voice on Activity stop and permission revocation |
| app/src/main/java/com/latticeward/musiccopilot/app/navigation/AppNavHost.kt (modified) | App-scoped VoiceSessionCoordinator + actual VoiceSessionState passed through existing SINGLE Driving route. Real Mic requires the current active Driving origin, current media access, OS mic permission, available Android on-device SpeechRecognizer and an Idle coordinator. Exiting Driving or moving to system/permission route cancels only Driving-origin generations. Same-route Cancel/Retry, never an extra destination or Player |
| app/src/main/java/com/latticeward/musiccopilot/feature/driving/DrivingModeScreen.kt (modified) | 72dp+ Mic button with actual permission prompt or provider start; inline origin-owned WF-08 status (Listening, Processing, Executed, Failed, unavailable Confirming); explicit rationale, Continue/Not now, Cancel, bounded Retry, and manual fallback. Transport controls are not tappable under an active voice prompt |
| app/src/main/res/values/strings.xml (modified) | 16 explicit localized UI strings, no private media/audio/voice transcript or MAC data |
| app/src/test/java/com/latticeward/musiccopilot/foundation/Issue9Phase7DrivingVoiceHandoffPolicyTest.kt (new) | 19 pure Kotlin/JUnit source test methods AUTHOR-ONLY, NOT EXECUTED |

## Real bounded interaction contract

1. No passive microphone access: a deliberate Mic tap inside active Driving Mode is the only start path.
2. If Android 12+ on-device recognizer is supported but RECORD_AUDIO is missing, Mic shows an in-screen explanation. Only Continue launches existing native permission request. ActivityResult result rechecks the OS grant but NEVER starts Voice. User must tap Mic again.
3. At the next tap, current OS mic grant and on-device recognizer readiness are verified once more. An already active Player/Driving voice session cannot be silently replaced.
4. If eligible, the existing application-scoped VoiceSessionCoordinator receives start(VoiceOrigin.Driving). Only REAL coordinator state can display Listening/Processing/Executed/Failed; Execute is sourced from real PlaybackGateway acknowledgment, not optimistic UI.
5. Inline Voice Back cancels Voice, normal Driving Back/Exit exits and cancels the Driving generation. Route/focus interruption and revoked mic permission cancel via existing app-scoped owners. No autoplay, extra MediaSession, service or additional STT/TTS provider.
6. Android 10/11 and unsupported OEMs fail closed with disabled Mic and readable manual fallback. Cloud STT has no approved adapter and is NEVER invoked, regardless of old cloud-consent preferences.
7. The Voice confirmation choices/PLAY_ARTIST command remain nonoperational in the current coordinator. The UI explicitly avoids enabled fake choices, no raw transcript display or persistence.

## Explicit incomplete dependencies

| Dependency | Current P07 disposition |
|---|---|
| Issue #10 full six-intent STT/intent/TTS, focus/ducking, OEM Android behavior and GS-04 live proof | NOT_VERIFIED. Existing coordinator supports five directly mapped transport intents; PLAY_ARTIST is not implemented until Issue #11 |
| Real Android 10/11 fallback and Android 12+ on-device permission/providing | Code-authored ONLY, device QA NOT_RUN |
| Listen six-second bound, one retry, error fallback, route/focus interruption, PTT cancellation and TTS ducking | Source seam connected, full runtime and safety acceptance NOT_VERIFIED |
| No Track/manual playback and existing #8 Android media-card/AirPods/BT safety | No playback service/route/notification mutations in P07; regression verification pending P13 |
| WF-07/WF-08 Voice overlay TalkBack, 150/200% fonts and focus | P08 scope, static P07 not a pass |

## Honesty and phase gate

- Verified local source hash and live GitHub VoiceSessionCoordinator, DefaultVoiceSessionCoordinator, SpeechToTextEngine, VoiceBindings, ApplicationBindings, MainActivity permission gate, AndroidManifest and source code at P07.
- Source-only changes in P07: one pure core policy; narrow Activity/NavHost/Driving Compose wiring; string resources; 19 authored JUnit cases; this document and ledger.
- All previous P02 (21), P03 (12), P05 (21), P06 (23) JUnit methods remain NOT_RUN. P07 adds 19. Cumulative authored 96; executed ZERO.
- JVM/Gradle/Compose/Lint/Actions/emulator/device/mic/vehicle testing: NOT_RUN. Original 14 product acceptance criteria: NOT_VERIFIED. All 46 owner phone M cases and five technical groups: NOT_RUN. APK: NOT_BUILT.
- Issue #9 remains OPEN. No PR, merge, workflow or APK in P07. Only P13 assembles the cumulative build-only APK for the owner's hands-on tests.

Disposition: P07 source implementation and static GitHub readback COMPLETE; P08 accessibility NEXT. This is NOT a claim of full #10 Voice functionality or that the app compiled.
