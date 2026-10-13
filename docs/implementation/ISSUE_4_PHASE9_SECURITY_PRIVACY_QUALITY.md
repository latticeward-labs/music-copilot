# Issue #4 — Phase 9 Security, Privacy & Quality foundation

- **Recorded:** 13 October 2026
- **Verdict:** **PASS — security/privacy code baseline + independent CI/static/71 JVM tests, bounded foundation scope. NOT a physical debug boot or complete device security QA claim.**
- **Branch:** [feature/4-android-foundation](https://github.com/latticeward-labs/music-copilot/tree/feature/4-android-foundation)
- **Issue:** [#4 Android Foundation & CI](https://github.com/latticeward-labs/music-copilot/issues/4), intentionally OPEN.
- **Tested SHA:** [a5b52796c80120bbe4be84d6d440717412eb37b7](https://github.com/latticeward-labs/music-copilot/commit/a5b52796c80120bbe4be84d6d440717412eb37b7)
- **Reference source:** uploaded `MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8` plus [Issue #16 quality port](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md), [runtime privacy policies](../architecture/RUNTIME_POLICIES.md) and [approved event contract](../ANALYTICS_AND_PRIVACY.md).

## 1. Concrete implementation evidence

| Concern | Code | Enforced behavior |
|---|---|---|
| Strict telemetry privacy gate | [AllowlistedTelemetryPrivacyGate](../../app/src/main/java/com/latticeward/musiccopilot/quality/privacy/AllowlistedTelemetryPrivacyGate.kt) | Exactly the eight approved `TelemetryEvent` subclasses mapped to fixed schema `v1|event|key=allowed_value`. Text input rejected unless exact values from stable codes (intents, error codes, entry, correction, coarse duration), and commands count 0–1000. No arbitrary maps or escaped free-form fields. Invalid/unknown inputs map to typed `QUALITY_DROPPED`. |
| Typed model | [TelemetryEvent](../../app/src/main/java/com/latticeward/musiccopilot/quality/api/TelemetryEvent.kt), [TelemetryPorts](../../app/src/main/java/com/latticeward/musiccopilot/quality/api/TelemetryPorts.kt) | Closed eight-event model, no raw payloads, internal sanitized value class constructor and sink receiving **sanitized only**. |
| Failure isolation | [BestEffortQualityEventPublisher](../../app/src/main/java/com/latticeward/musiccopilot/quality/adapters/BestEffortQualityEventPublisher.kt) | Gate precedes sink; rejected event never reaches sink; exceptions thrown by test gate or test sink are caught and return `false`. No retry, persistent diagnostic backlog or product authority. |
| Production diagnostics sink | [NoOpTelemetrySink](../../app/src/main/java/com/latticeward/musiccopilot/quality/noop/NoOpTelemetrySink.kt), [QualityBindings](../../app/src/main/java/com/latticeward/musiccopilot/app/di/QualityBindings.kt) | App-scope gate now allows only approved sanitized events; **actual production sink always discards**. No crash/analytics SDK, provider credentials, remote endpoint, dashboard or collection implied. |
| Application boundary | [ApplicationBindings](../../app/src/main/java/com/latticeward/musiccopilot/app/di/ApplicationBindings.kt) | One telemetry gate + one NoOp sink + one best-effort publisher in the Application graph. Playback/Voice/permission status is not updated from Quality. |
| Build variants | [app/build.gradle.kts](../../app/build.gradle.kts) | `QUALITY_EXTERNAL_DIAGNOSTICS_ENABLED=false`, `QUALITY_CRASH_UPLOAD_ENABLED=false`, `QUALITY_VERBOSE_LOGGING_ENABLED=false` for both Debug and Release; Release `isDebuggable=false`; no external analytics/crash plugin. |
| Android privacy | [AndroidManifest.xml](../../app/src/main/AndroidManifest.xml), [legacy backup rules](../../app/src/main/res/xml/no_backup_legacy.xml), [Android 12+ extraction policy](../../app/src/main/res/xml/no_data_extraction.xml) | No INTERNET permission, no cleartext traffic, `allowBackup=false`, all-root exclusions for legacy backups and API31+ cloud-backup/device-transfer. No guarantee of OEM behavior until physical validation. |
| CI privacy guard | [check_phase9_privacy.py](../../scripts/check_phase9_privacy.py), [Android CI workflow](../../.github/workflows/issue-4-phase2-bootstrap.yml) | Mandatory Privacy static audit before Android Gradle compile. Independent self-test proves safe fixture passes and **three deliberately unsafe fixture variations fail**: backup re-enabled, external reporting SDK, free-form telemetry model. |

## 2. Exact privacy allowlist

Allowed events: `driving_mode_started`, `voice_button_pressed`, `intent_detected`, `intent_failed`, `command_executed`, `manual_correction`, `player_error`, `session_completed`.

- Intent codes: PLAY, PAUSE, NEXT, PREVIOUS, SEEK_BACK, PLAY_ARTIST.
- Confidence, latency, route and playback-state values are from closed enums.
- `entry_source`, `correction_type`, `duration_bucket` and error code are exact-case fixed sets (not user-controlled strings).
- `commands_count` accepts 0–1000 only; overflow/negative values reject rather than silently treating arbitrary numbers as identifiers.
- No raw audio, STT transcript, artist/album/title, filesystem path, content URI, MediaStore reference, query/result, queue, precise location/playback position, personal identifier, token, provider message, stack trace or unstructured JSON is ever copied into the **allowlisted quality encoding**. The sink receives only accepted sanitized values, and the production sink still drops all of them.
- The local documented source enums/code sets are deliberately conservative; any schema expansion requires Privacy/Product approval. No remote config may override the whitelist.

## 3. Tests and real CI evidence

[Phase9PrivacyQualityTest](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Phase9PrivacyQualityTest.kt) adds **13 JVM tests**: eight approved event schemas, sensitive/path/transcript/URI/token rejection, exact-code and case failures, correction/entry/duration denial, count bounds, no maps/property carriers, true NoOp sink, Application graph no product mutation, thrown Sink and Gate isolation, fake opt-in sink only sees approved sanitized values, both-build settings and backup exclusions. Existing earlier tests remain green; `ApplicationCompositionTest` now asserts the allowed event can be sanitized while the production NoOp publisher still drops.

**Actual successful CI:** [Run #38457642495](https://github.com/latticeward-labs/music-copilot/actions/runs/38457642495), job `115504584241`, source `a5b52796c80120bbe4be84d6d440717412eb37b7`, **completed/success**. Verified workflow log:
- `SELFTEST PASS: 24 fixtures including real rejected violations`
- `ARCHITECTURE PASS: one :app module; 52 package paths; 105 source files`
- `PRIVACY_SELFTEST PASS: safe fixture plus 3 real rejected violations`
- `PRIVACY_QUALITY PASS: typed allowlist, offline NoOp, debug/release flags, no external SDK, no network, excluded backup`
- `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:assembleRelease`, `ROOM_SCHEMA_EXPORT PASS`
- `BUILD SUCCESSFUL in 7m 28s`.

**Actual [CI artifact #11844128692](https://github.com/latticeward-labs/music-copilot/actions/runs/38457642495/artifacts/11844128692) inspected:** 11 JUnit XML suites with **71 tests, 0 failures/errors/skipped**, Debug APK `build/outputs/apk/debug/app-debug.apk`, unsigned release APK `build/outputs/apk/release/app-release-unsigned.apk` and Room v1 exported schema Git blob SHA `c3b1ba7affdaa1a9d6425d3b3dc7ddf10adf300b` matching committed Room source. No signing secret, deployment token or backend credential was requested to compile.

## 4. Strict limitations / remaining gates

1. No emulator or physical Android device cold boot/launch of the actual Debug APK was executed. **APK build success, the constructor-only JVM composition tests and credential-free build are not proof of a real on-device cold boot.** The original Phase 9 PASS text's `clean debug boot` is therefore **not independently field-verified**; this open test must be included in downstream device validation before overall Issue #4 acceptance.
2. No OS-level backup/transfer experiment or third-party vendor logcat/SDK instrumentation was performed. Source/Manifest/static checks only cover implemented baseline.
3. Measurement completeness, beta telemetry approvals, local clearing UI, analytics provider, dashboard and retention decisions remain in Issue #12 and related UX/security acceptance. NoOp is safe but **does not prove beta product metrics**.
4. Real background playback, audio focus, Bluetooth, route/call, Android 10/12/14+ physical matrix, microphone STT and accessibility privacy review remain independent downstream tests.
5. No backend, cloud sync, streaming or cloud STT is enabled. Quality cannot create an alternative playback authority.
6. Original Issue #4 global acceptance still **1/14 checked, 13 pending**. Do not merge or close Issue #4 based solely on this Phase 9 automated foundation.
