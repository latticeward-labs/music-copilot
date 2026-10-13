# Issue #4 — Phase 10 Compose Shell and Design System token bridge

- **Scope:** Phase 10 **real single-Activity Compose startup shell**, fixed dark Night Signal token bridge, lifecycle-aware observation, typed Navigation registry. Not feature implementation.
- **Implementation branch:** [feature/4-android-foundation](https://github.com/latticeward-labs/music-copilot/tree/feature/4-android-foundation)
- **Issue:** [#4 Android Foundation and CI](https://github.com/latticeward-labs/music-copilot/issues/4)
- **Normative visual source:** [Issue #17 foundations/TOKENS.json](../design-system/foundations/TOKENS.json), [Compose token mapping](../handoff/COMPOSE_TOKEN_MAPPING.md), [Issue #16 architecture](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md). Source RAR `MSM2(1)(1).rar`, SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`.
- **Night Signal baseline:** `1.0.0`, frozen 3 October 2026; fixed dark, minSdk 29, Kotlin/Compose/Material 3. Do not generate new imagery or substitute PNG screens for Compose.
- **Acceptance:** Build and token checks **PASS**; [real API29 emulator smoke](https://github.com/latticeward-labs/music-copilot/actions/runs/38463565982) **PENDING FINAL DEVICE RECHECK** (disk-size repair run #38469100082); original emulator run logged actual `Status: ok`, `LaunchState: COLD`, surviving Process, but reporter's multiline /bin/sh assertion caused non-app failure. Recheck final emulator run before marking Phase 10 PASS.

## 1. Production files and ownership

| File | Deliverable and boundary |
|---|---|
| [MainActivity](../../app/src/main/java/com/latticeward/musiccopilot/app/MainActivity.kt) | One real Activity uses `setContent`, app-owned [MusicCopilotApplication](../../app/src/main/java/com/latticeward/musiccopilot/app/MusicCopilotApplication.kt) graph, lifecycle-aware `collectAsStateWithLifecycle` of LibraryIndex and Media3 controller connection, and `onResume -> ApplicationBindings.onForeground`. No Player/MediaSession/Voice service owned by Activity. |
| [AppNavHost](../../app/src/main/java/com/latticeward/musiccopilot/app/navigation/AppNavHost.kt) | One real `NavHost` with **Bootstrap route only**, a non-optimistic status Surface and safe drawing insets, vertical scroll and token-based text styles. No full screenshots or fake feature screens. |
| [Destination](../../app/src/main/java/com/latticeward/musiccopilot/app/navigation/Destination.kt) | Typed contracts for Bootstrap, Onboarding, PermissionRecovery, Library, Search, FullPlayer, Driving, Settings, LikedSongsP2; exactly Bootstrap active. Voice is deliberately not a destination; it is an overlay owned by the active Voice origin. |
| [RootNavigationStateHolder](../../app/src/main/java/com/latticeward/musiccopilot/app/navigation/RootNavigationStateHolder.kt) | Pure non-authoritative readiness mapping: unavailable, scanning, empty, ready, recoverable-error; disconnected Media3 does not fabricate library access/playing success. |
| [MusicCopilotTheme](../../app/src/main/java/com/latticeward/musiccopilot/app/theme/MusicCopilotTheme.kt) | Fixed Material 3 dark `ColorScheme` + Shapes + Typography backed by exact Issue #17 token values, no Android wallpaper/dynamic color or unapproved light-mode toggle. |
| [MusicColors](../../app/src/main/java/com/latticeward/musiccopilot/app/theme/MusicColors.kt) | All **28 semantic** and **22 primitive** HEX colors from Issue #17. `#RRGGBBAA` is correctly transformed to Compose ARGB `0xAARRGGBB`. |
| [MusicGeometry](../../app/src/main/java/com/latticeward/musiccopilot/app/theme/MusicGeometry.kt) | Exactly mapped spacing, grid, radius, elevation, size and motion constants, original Android dp units; reference 360×800 is design dp, not static pixel layout. |
| [MusicTypography](../../app/src/main/java/com/latticeward/musiccopilot/app/theme/MusicTypography.kt) | All ten approved SP roles with fixed lineHeight, FontWeight and system sans-serif; system font scaling remains effective. |
| [App Gradle](../../app/build.gradle.kts), [version catalog](../../gradle/libs.versions.toml), [strings](../../app/src/main/res/values/strings.xml) | Navigation Compose + lifecycle-runtime-compose, typed neutral recovery messages as string resources; minSdk 29 maintained, no new backend, cloud provider or analytics dependency. |

## 2. Exact source mapping

Authoritative source fields are `docs/design-system/foundations/TOKENS.json` and the approved `docs/handoff/COMPOSE_TOKEN_MAPPING.md`:

- Colors: `color.semantic.* -> MusicColors.*` and `color.primitive.* -> MusicPrimitiveColors.*`. Material 3 `primary`, `background`, `surface`, `error`, `outline`, content and scrim map to exact Night Signal semantics. Example `primary = #67D8FF`, `background = #090D12`, `surface = #111720`.
- Typography: `typography.display/titleLarge/titleMedium/bodyLarge/bodyMedium/labelLarge/labelMedium/drivingTitle/drivingMeta/drivingState` -> `MusicTextStyles`, including 32/40 SP display and 24/32 SP large title. Driving-specific roles are exposed as tokens, **not** rendered as Driving feature screens.
- Geometry: `spacing.*Dp`, `grid.*Dp`, `radius.*Dp`, `elevation.*Dp`, `size.*Dp` -> matching `MusicSpacing/MusicGrid/MusicRadius/MusicElevation/MusicSize`; motion milliseconds -> `MusicMotion`.
- States: `state.pressedOverlay/focus/selected/disabled/error` -> `MusicStateColors`; pressed overlay `#FFFFFF1A` uses 0x1AFFFFFF ARGB.
- Voice remains a **state overlay**, not a navigation route. No duplicate Playback state or data persistence is created by the UI.
- A real source-to-source consistency guard is committed: [check_phase10_design_tokens.py](../../scripts/check_phase10_design_tokens.py), run automatically in [Android CI](../../.github/workflows/issue-4-phase2-bootstrap.yml). It validates all semantic/primitive palette values, every mapped typography/DP/motion role, Material color mappings, dark-only baseline, 9 typed destinations, only 1 active Bootstrap route, lifecycle-aware collection and minSdk 29. Three intentionally invalid fixtures (wrong HEX, extra feature route and wrong lifecycle collection) are required to **fail** the test.

## 3. Automated evidence

- [Phase10ComposeShellTest](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Phase10ComposeShellTest.kt) adds **nine pure JVM tests**: typed destination uniqueness, absent Voice navigation, no-permission/manual fallback truth, all library status projections, no invented service authority, lifecycle collectors, Night Signal theme, safe insets/scroll, minSdk 29.
- [GitHub Actions run #38460889744](https://github.com/latticeward-labs/music-copilot/actions/runs/38460889744) **completed/success**, job `115515446979`, source SHA `ba7edcbd146fc7b259eef594199e733979705ccf`. Actual CI log:
  - `SELFTEST PASS: 24 fixtures`, `ARCHITECTURE PASS: one :app module; 52 package paths; 112 source files`;
  - `PRIVACY_SELFTEST PASS`, `PRIVACY_QUALITY PASS`;
  - `DESIGN_SELFTEST PASS: exact baseline plus 3 deliberately rejected regressions`;
  - `DESIGN_TOKEN_AUDIT PASS: exact Night Signal palette, SP, DP, navigation/voice boundary, lifecycle, API 29`;
  - `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:assembleRelease`, `ROOM_SCHEMA_EXPORT PASS`, `BUILD SUCCESSFUL in 6m 52s`.
- [Real build/JUnit artifact #11845627770](https://github.com/latticeward-labs/music-copilot/actions/runs/38460889744/artifacts/11845627770) opened and counted: **80 tests across 12 JUnit suites, zero failures/errors/skipped**, **9 Phase 10 tests**, debug and unsigned release APKs, Room v1 schema.

## 4. Emulator evidence and boundaries

- [Initial emulator run #38461504919](https://github.com/latticeward-labs/music-copilot/actions/runs/38461504919) failed **before app launch** because the upstream emulator action uses POSIX `/bin/sh` rather than Bash `pipefail`. This is workflow setup error, not app error.
- [Next emulator run #38462629926](https://github.com/latticeward-labs/music-copilot/actions/runs/38462629926) successfully **installed** the APK and cleared app data; `adb shell am start -W` returned `Status: ok`, `LaunchState: COLD`, `Activity: com.latticeward.musiccopilot.debug/com.latticeward.musiccopilot.app.MainActivity`; Process and activity-state checks passed after 7 seconds. Subsequent **multiline if** syntax error from the runner's line-by-line shell prevented Crash Buffer assertion and screenshot completion. Failure is an automation syntax error, not a demonstrated app crash.
- [Run #38463565982](https://github.com/latticeward-labs/music-copilot/actions/runs/38463565982) and [run #38466010944](https://github.com/latticeward-labs/music-copilot/actions/runs/38466010944) failed **before installation** because the emulator process terminated during AVD initialization. The latter log pinpoints the exact root cause: `FATAL | Not enough space to create userdata partition. Available: 6805.21 MB ... need 7372.80 MB`. The repeated ADB errors were downstream of this **disk-space failure**, not an app crash.

- [Repaired emulator run #38469100082](https://github.com/latticeward-labs/music-copilot/actions/runs/38469100082), code SHA `a80630eb231ff26ca135b4d1f9429b5daa5a6569`, explicitly supplies the supported `disk-size: 2048M` emulator-runner input to avoid the 7.37 GB userdata requirement. This run also requires real denied runtime-permission assertions (`READ_EXTERNAL_STORAGE` and `RECORD_AUDIO`), cold boot, live Process/Activity, empty app crash buffer and a screenshot. **Do not mark full device acceptance until that CI run succeeds and its artifact is inspected.**
- **Not part of Phase 10:** full onboarding, runtime permission recovery screens, Playback UI, Driving controls, listening/voice overlays, real music index/search, external vehicle/OEM test matrix, art assets, and additional dynamic theme. Subsequent UI-heavy issues #5/#6/#7/#9/#10/#11 own those implementations, and Issue #13 owns broader field QA.
- No claims of a hardware phone run, all-API/OEM parity, 200%-font screenshot accessibility audit, full cross-process restoration or approved feature screen behavior from this foundation.
- Issue #4 overall cross-phase acceptance must remain OPEN/IN PROGRESS until the remaining phases are evidenced.
