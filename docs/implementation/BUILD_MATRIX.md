# Issue #4 / Phase 2 — Production Android Build Matrix

**Recorded:** 13 October 2026 · **Branch:** `feature/4-android-foundation` · **Scope:** new root production Gradle/Compose project, distinct from the spike.  
**Authority:** [English PRD/MVP source register](../architecture/SOURCE_AND_AUTHORITY_REGISTER.md), [ADR-0001](../adr/0001-native-android-platform.md), [ADR-0008](../adr/0008-minimal-modular-monolith-and-dependency-enforcement.md), [Issue #16 approved implementation contracts](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md).

## 1. Pinned toolchain

| Item | Root production value | Verification / reason |
|---|---|---|
| Physical Gradle modules | exactly `:app` | `settings.gradle.kts`: `include(":app")`; the separate `spikes/android-validation` project is *not* included |
| Gradle Wrapper | **8.11.1** | `gradle/wrapper/gradle-wrapper.properties`, distribution SHA-256 `f397b287023acdba1e9f6fc5ea72d22dd63669d59ed4a289a29b1a76eee151c6`. Wrapper jar and executable scripts copied by Git blob SHA from **already versioned** isolated Android spike; **no downloaded/random jar** |
| Gradle Wrapper binary provenance | JAR blob `2c3521197d7c4586c843d1d3e9090525f1898cde`; POSIX launcher blob `d95bf6131dc41079a1665889f84d4cfec92a2bc1` mode 100755; Windows launcher blob `640d68685c14950470c82031e02198e220b96e62` | Exact SHA tracked in repo; Android spike files untouched |
| Android Gradle Plugin | **8.10.1** | Google official AGP 8.10 compatibility notes permit API 36, require **Gradle 8.11.1**, **JDK 17** and default Build Tools 35.0.0: https://developer.android.com/build/releases/agp-8-10-0-release-notes |
| Kotlin | **2.0.21** | Pin shared by Kotlin Android and `org.jetbrains.kotlin.plugin.compose`. Kotlin official source: https://kotlinlang.org/docs/whatsnew20.html (Kotlin 2+ Compose Compiler Gradle plugin is version-aligned) |
| Java source/target/JDK | **17** | `app/build.gradle.kts` Java 17 and Kotlin JVM 17; CI `actions/setup-java@v5` Temurin 17 |
| `minSdk` | **29 (Android 10)** | PRD/ADR-0001 Android 10+ minimum; cannot be raised without product/architecture change review |
| `compileSdk` / `targetSdk` | **36 / 36** | Existing proven spike baseline also uses 36/36; AGP 8.10 supports up to API 36 |
| SDK Build Tools | **35.0.0** (AGP default) | GitHub bootstrap workflow installs `build-tools;35.0.0`; no root user-specific `local.properties` |
| Compose BOM | **2025.01.01** | Material3/Compose UI versions BOM-managed; Compose Compiler version is linked to Kotlin plugin, NOT the BOM |
| Activity Compose, AndroidX Core | **1.10.0 / 1.15.0** | Reuse tested spike version pins; avoid newer speculative framework versions |
| Lifecycle, Kotlin Coroutines | **2.8.7 / 1.9.0** | Compose / ViewModel boundaries and Flow-ready dependencies |
| Media3 ExoPlayer + Session | **1.5.1** | Foundations only in Phase 2; service owner implementation in Phase 7 |
| Room runtime + KTX | **2.6.1** | Declared but no DAOs/compiler generation until Phase 6 |
| DataStore core + preferences | **1.1.1** | Declared but no Proto schema, repositories or preference implementation until Phase 6 |
| Unit-test library | **JUnit 4.13.2** | Declared now; meaningful contract tests belong to Phases 3–11 |
| Static checks | **Android Lint (AGP built-in)** | `warningsAsErrors=true` and `abortOnError=true`; actual lint evidence at Phase 11 |
| Formatting | existing repo `.editorconfig`, official Kotlin code style | External automated formatter choice deliberately deferred to Phase 11 (not silently claimed as enforced in Phase 2) |

Toolchain versions are controlled centrally by `gradle/libs.versions.toml` and wrapper properties. No KSP, protobuf generator, DI SDK, telemetry/crash SDK, cloud service or backend dependencies were installed speculatively. Phase 6 must select Room codegen/Proto versions only when actual adapters are implemented, and verify them independently.

## 2. Root project ownership and startup

```text
/
├── settings.gradle.kts                 # one :app, no spike include
├── build.gradle.kts                    # root plugin declarations only
├── gradle.properties                   # no private keys or device paths
├── gradle/libs.versions.toml            # centralized dependency versions
├── gradle/wrapper/gradle-wrapper.jar
├── gradle/wrapper/gradle-wrapper.properties
├── gradlew                             # mode 100755
├── gradlew.bat
├── app/
│   ├── build.gradle.kts                # debug and unsigned release
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml         # only exported launcher Activity
│       ├── java/com/latticeward/musiccopilot/app/
│       │   ├── MusicCopilotApplication.kt
│       │   └── MainActivity.kt
│       └── res/values/                  # simple startup strings/theme
└── spikes/android-validation/          # unchanged separate Gradle project
```

The production application namespace is `com.latticeward.musiccopilot`. The separate spike uses `com.latticeward.musiccopilot.spike`. Debug adds `.debug` applicationId suffix and version name `-debug` and remains automatically debuggable; release has a non-debuggable unsigned default config and requires an explicit future signing/release gate. No release signing key or credential is needed to assemble Debug. The manifest has **no audio, microphone, network or media permissions** at this phase; features/permissions are explicitly separate later tasks.

Only a **temporary bootstrap Compose smoke surface** with app name and a foundation message is included to ensure the root APK can launch after compilation. It is *not* a feature screen, navigation contract, approved visual system or Phase 10 design-token delivery. Service ownership, Voice session, MediaStore, Room DAOs, DataStore Proto storage and Quality sink implementations remain unimplemented.

## 3. Commands and required evidence

From a new checkout of the **feature branch**:

```bash
java -version                               # JDK 17 required
./gradlew --version
./gradlew projects                          # should include root :app only
./gradlew :app:assembleDebug --no-daemon --stacktrace
./gradlew :app:assembleRelease --no-daemon --stacktrace  # unsigned; optional phase check
./gradlew :app:lintDebug --no-daemon        # separate later static/CI gate
```

On Windows: use `gradlew.bat` instead of `./gradlew`. Install Android SDK **platforms;android-36** and **build-tools;35.0.0**. `local.properties` may exist on a machine but must never be tracked; CI uses environment-managed SDK, no developer credentials.

**True root-app Build evidence is required for Phase 2 PASS:** [Issue #4 Phase 2 bootstrap GitHub Actions workflow](https://github.com/latticeward-labs/music-copilot/blob/feature/4-android-foundation/.github/workflows/issue-4-phase2-bootstrap.yml) triggers on branch `feature/4-android-foundation` when source/build paths change. It installs SDK/JDK17 and executes `./gradlew :app:assembleDebug` from repository root, then uploads actual APK as an artifact if present.

### Executed run results (write only after inspection)

- Initial attempt [run 38420506474](https://github.com/latticeward-labs/music-copilot/actions/runs/38420506474), commit `23b7b28038561e7b388bab626cd4bd29f6845256`: **FAIL before Gradle**, `android-actions/setup-android@v3` default tried removed SDK package `tools` (`Failed to find package 'tools'`). No APK or Gradle compile attempted. Corrected workflow to set `packages: platform-tools` in commit `c75db8562c7ba1dc9f5aa90e4882dd0aeb63ae12`.
- Corrected follow-up [run 38420696215](https://github.com/latticeward-labs/music-copilot/actions/runs/38420696215), commit `c75db8562c7ba1dc9f5aa90e4882dd0aeb63ae12`: **FAIL before Gradle** because an incorrect shell `grep` regex caused `Unmatched (`. This check was fixed in commit `ae54786528f984a8cf8f6a0ea62f489978300b58`.
- **Actual first green clean-checkout root Debug Build:** [run 38421023043](https://github.com/latticeward-labs/music-copilot/actions/runs/38421023043), tested commit `ae54786528f984a8cf8f6a0ea62f489978300b58`. Gradle log shows `> Task :app:compileDebugKotlin`, `> Task :app:packageDebug`, `> Task :app:assembleDebug`, `BUILD SUCCESSFUL in 2m 52s`. Artifact [11826493216](https://github.com/latticeward-labs/music-copilot/actions/runs/38421023043/artifacts/11826493216), ZIP SHA-256 `9ff13c6283854b307547f5d47d767ee7cc9319cd4c937cf793938b5eaff4ba85`.
- **Final expanded actual green clean-checkout Build:** [run 38421736045](https://github.com/latticeward-labs/music-copilot/actions/runs/38421736045), tested code/workflow SHA `04044a1c2338453a72f822c5ba0ac8fe98d881b6`. Gradle ran `projects :app:assembleDebug :app:assembleRelease --no-daemon --stacktrace`: output reports root project `music-copilot`, **only `:app`**, both `compileDebugKotlin` and `compileReleaseKotlin`, both `package/assemble` Debug and Release, and `BUILD SUCCESSFUL in 4m 24s`. Both APK paths, including **unsigned Release** (not deployable/release-approved without future signing), were uploaded to [artifact 11825939906](https://github.com/latticeward-labs/music-copilot/actions/runs/38421736045/artifacts/11825939906), 21,474,572 bytes, ZIP SHA-256 `0720902a82acd0de5fa60110f28a195149544cd55adaab6aa8dfe46a04f97226`.
- The initial two failed attempts are **not erased**; both were non-product bootstrap workflow errors and their corrective commits are linked. Source and changes are verified against the real GitHub Actions build logs.

**Phase 2 gate: PASS — production root APK build and Gradle projects verified in clean GitHub Actions.** This is not a physical device launch, a signed distribution build, a full test/lint CI gate, or final Issue #4 acceptance. Exact structured evidence: [Phase 2 Validation JSON](ISSUE_4_PHASE2_VALIDATION.json).

## 4. Constraints, reproducibility and open gates

- GitHub Actions runner is a clean checkout and is **independent of any user's local SDK**, but dependency resolution still requires Maven/Google/Gradle distribution networks. A network/transient downloader failure is recorded as a failure, not a build pass.
- Runtime/CI device execution, 2 physical launches, accessibility, privacy provider, service/Voice/data policy tests, full lint/unit/format CI, composition root, and user features remain intentionally **unproven** in Phase 2.
- The local assistant execution container has Java 21 but **no Android SDK and no outbound DNS/network for Gradle distributions**, so it cannot provide an independent local successful Gradle build. GitHub Actions is the actual reproducibility authority for this execution.
- Configuration cache is **not claimed** as validated until explicit `--configuration-cache` run; a build without configuration cache does not prove it. Future Phase 11/12 should exercise it after plugins stabilize.
- New Android-only build infrastructure does not establish Issue #4 final acceptance; 13 implementation criteria remain unchecked, and Phase 2 alone is insufficient to close Issue #4.

## 5. Phase 2 acceptance boundaries

| Condition | Required PASS evidence |
|---|---|
| Real root Gradle, wrapper jar/scripts and version catalog committed | GitHub tree + blob inspection with exact commit SHA, `gradlew` executable bit |
| Single root `:app`, Android 10+ config, debug/release | checked source/config plus real root debug build |
| No speculative product features, API keys or Spike module inclusion | branch diff/source checks |
| Clean checkout Gradle sync/debug assemble | **real successful root GitHub Actions run** and APK artifact/Gradle task logs |
| Build/provenance tracked to Issue #4 | update [Issue #4 Evidence Index](ISSUE_4_EVIDENCE_INDEX.md), Phase 2 validation, and phase checklist *after* actual results |

**Final Phase 2 status is assigned only after inspecting real GitHub Actions logs for this exact root project.**
