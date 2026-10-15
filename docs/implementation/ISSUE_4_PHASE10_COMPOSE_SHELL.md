# Issue #4 — Phase 10 Compose Shell and Design System token bridge

- **Scope:** Phase 10 **real single-Activity Compose startup shell**, fixed dark Night Signal token bridge, lifecycle-aware observation, typed Navigation registry. Not feature implementation.
- **Implementation branch:** [feature/4-android-foundation](https://github.com/latticeward-labs/music-copilot/tree/feature/4-android-foundation)
- **Issue:** [#4 Android Foundation and CI](https://github.com/latticeward-labs/music-copilot/issues/4)
- **Normative visual source:** [Issue #17 foundations/TOKENS.json](../design-system/foundations/TOKENS.json), [Compose token mapping](../handoff/COMPOSE_TOKEN_MAPPING.md), [Issue #16 architecture](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md). Source RAR `MSM2(1)(1).rar`, SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`.
- **Night Signal baseline:** `1.0.0`, frozen 3 October 2026; fixed dark, minSdk 29, Kotlin/Compose/Material 3. Do not generate new imagery or substitute PNG screens for Compose.
- **Acceptance: PHASE 10 PASS (source + build + emulator).** The original 80/80 JVM + design/static audit CI was green and the later API29 emulator cold-start workflow #38475056625 also completed **success** with an examined screenshot, no app crash, denied runtime permissions, process alive, and resumed MainActivity. See the final evidence addendum below. This is not two physical devices or whole-Issue completion.

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

- [Previous emulator run #38469100082](https://github.com/latticeward-labs/music-copilot/actions/runs/38469100082), code SHA `a80630eb231ff26ca135b4d1f9429b5daa5a6569`, explicitly supplies the supported `disk-size: 2048M` emulator-runner input to avoid the 7.37 GB userdata requirement. This run also requires real denied runtime-permission assertions (`READ_EXTERNAL_STORAGE` and `RECORD_AUDIO`), cold boot, live Process/Activity, empty app crash buffer and a screenshot. **Do not mark full device acceptance until that CI run succeeds and its artifact is inspected.**
- **Not part of Phase 10:** full onboarding, runtime permission recovery screens, Playback UI, Driving controls, listening/voice overlays, real music index/search, external vehicle/OEM test matrix, art assets, and additional dynamic theme. Subsequent UI-heavy issues #5/#6/#7/#9/#10/#11 own those implementations, and Issue #13 owns broader field QA.
- No claims of a hardware phone run, all-API/OEM parity, 200%-font screenshot accessibility audit, full cross-process restoration or approved feature screen behavior from this foundation.
- Issue #4 overall cross-phase acceptance must remain OPEN/IN PROGRESS until the remaining phases are evidenced.

### Emulator disk-space follow-up — 13 October 2026

[Run #38469100082](https://github.com/latticeward-labs/music-copilot/actions/runs/38469100082) **failed before app install/launch**, despite `disk-size: 2048M`. Its logs prove `disk.dataPartition.size=2048M` was written to the AVD `config.ini`, but the installed emulator build still rejected initialization because only **6805.10 MB** was free and **7372.80 MB** was required. Merely extending `emulator-boot-timeout` or changing the userdata config cannot repair insufficient runner disk.

[Latest run #38471999196](https://github.com/latticeward-labs/music-copilot/actions/runs/38471999196) adds deterministic runner cleanup **after building the APK** and **before AVD installation/boot**: removes unused preinstalled .NET/CodeQL/PowerShell/Android NDK/CMake, prints `df -h` before and after, and requires **≥10 GiB free** prior to starting the emulator. The native Kotlin/Compose build is already complete when unused SDK parts are removed. The explicit pre-boot failure is preferable to a false app-crash accusation or silently timing out. **Do not declare device-smoke PASS without the job's final conclusion and uploaded runtime files.**


## Final verified closeout — 13 October 2026 (supersedes earlier PENDING notices)

**PASS — bounded Phase 10 Compose Shell and Design System startup.** Actual latest source: commit [`df23268a288813e5a4b5a59dd59ee72b47f61dcd`](https://github.com/latticeward-labs/music-copilot/commit/df23268a288813e5a4b5a59dd59ee72b47f61dcd). Real GitHub Actions [run #38475056625](https://github.com/latticeward-labs/music-copilot/actions/runs/38475056625), job `115563579785`, **completed/success**; Android API **29**, Google APIs x86_64 emulator, Pixel 3a profile; production `app-debug.apk` built from a clean Actions checkout without cloud/account credentials and installed on the actual emulator. Runtime [Artifact #11853821575](https://github.com/latticeward-labs/music-copilot/actions/runs/38475056625/artifacts/11853821575) downloaded and independently examined:

| Assertion | Inspected evidence | Result |
|---|---|---|
| Real Android 10 bootstrap | `phase10/launch.txt`: `Status: ok`, `LaunchState: COLD` | PASS |
| Actual foreground UI | `phase10/activity_state.txt`: `mResumedActivity` and `ResumedActivity` = `com.latticeward.musiccopilot.debug/com.latticeward.musiccopilot.app.MainActivity` | PASS |
| Runtime process | Same activity-state record + live `pidof` after 7 seconds | PASS |
| No upfront media/voice consent | `phase10/permission_state.txt`: `READ_EXTERNAL_STORAGE` and `RECORD_AUDIO` both `granted=false` | PASS |
| Crash safety | `phase10/crash_buffer.txt` **0 bytes** | PASS (smoke only) |
| Actual Compose UI | `phase10/bootstrap_api29.png`, real PNG, **1080×2220**, **74,600 bytes**, SHA-256 `4d61cf50694b2a6463e8bc31a4e9a2ea5b021379f349c1f2210aaa2adf40bf88` | PASS; manually inspected |
| Exact Design System contract | Phase 10 design audit `DESIGN_TOKEN_AUDIT PASS`; 28 semantic + 22 primitive color mappings, 10 typography roles, DP/route/lifecycle contract | PASS |
| Build | Same emulator workflow `BUILD SUCCESSFUL in 4m 2s`; original full Debug+Release / 80-test CI [#38460889744](https://github.com/latticeward-labs/music-copilot/actions/runs/38460889744) | PASS |

**Visual review:** actual screenshot shows the Night Signal dark startup UI, “Music Copilot”, neutral foundation text and explicit local-media-unavailable/permission recovery-in-preparation copy. It is a real Compose render, **not** a high-fidelity PNG substituted for the UI. The `am start -W` output also includes an incidental Android launcher `Activity:` line; foreground confirmation is sourced to **`mResumedActivity` in the separately captured activity-state dump** and screenshot, not that launcher line.

**Transparent failed-run history:** [#38471999196](https://github.com/latticeward-labs/music-copilot/actions/runs/38471999196) booted/emulator-installed but incorrectly assumed a never-granted RECORD_AUDIO permission must have a `granted=false` runtime entry. [#38473240996](https://github.com/latticeward-labs/music-copilot/actions/runs/38473240996) failed in the runner’s automatic ADB animation-disable step before the app smoke script (exit 255), after a successful emulator boot. The former was corrected to test absence of `granted=true` and requested permission presence; the latter to disable optional runner animation toggling. [Final successful run #38475056625](https://github.com/latticeward-labs/music-copilot/actions/runs/38475056625) confirms both fixes. Earlier out-of-disk attempts remain documented above, not renamed as green.

**Scope of PASS:** Android 10 *emulator* single Activity startup/Bootstrap shell + canonical design token plumbing. It does **not** establish two representative physical/device launches, font scale 200%, TalkBack, full navigation/feature screens, real media playback, Voice-on-Android-10 STT, or complete Issue #4 acceptance. Phase 13 owns two-device smoke; other feature issues own full UX. Issue #4 **remains OPEN**, Phases 11–14 still pending.
