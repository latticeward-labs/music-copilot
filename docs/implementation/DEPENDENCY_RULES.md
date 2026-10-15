# Issue #4 — Phase 3 Dependency-Direction Rules and Package Ownership

**Status: PHASE 3 PASS — 13 October 2026.** Real GitHub Actions [run 38424771876](https://github.com/latticeward-labs/music-copilot/actions/runs/38424771876) at tested commit `813818c12af5a2d110b314ddcb534db30db452ae` executed all 23 fixture checks and compiled both Debug and unsigned Release successfully.  
**Last reviewed:** 13 October 2026. **Controlling sources:** [Issue #16 approved implementation structure, §9 and §11](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md), [ADR-0008](../adr/0008-minimal-modular-monolith-and-dependency-enforcement.md), [Runtime Policies](../architecture/RUNTIME_POLICIES.md), [Issue #4 phased execution](ISSUE_4_PHASED_EXECUTION_PLAN.md).

**Decision:** Exactly one physical Gradle module **`:app`**; seven **logical** packages below `app/src/main/java/com/latticeward/musiccopilot/`. The physically independent `spikes/android-validation` harness is not a production source root or dependency. No `:core`, `:playback`, `:data`, etc. Gradle modules are introduced.

## 1. Actual committed source tree

**Phase 3 materialization:** All 52 directories in the Issue #16 prescribed package tree have a minimal `PackageBoundary.kt` file containing only the correct package declaration and a short structural comment. These **compile-time package reservations have no symbols, implementations, DI bindings, runtime jobs, public interfaces or state**. Once genuine classes are introduced in Phases 4–10, remove redundant package markers. This is intentionally not an invented partial `PlaybackGateway` or `MediaSessionService`; their exact types and behavior belong to later Phase gates.

```text
app/src/main/java/com/latticeward/musiccopilot/
├── app/                         # root Application, Activity and composition
│   ├── navigation/              # NavHost / root-state direction, Phase 10
│   └── di/                      # ONLY cross-boundary production DI, Phase 5
├── core/                        # pure shared/domain/application abstractions
│   ├── model/                   # pure leaf
│   ├── result/                  # pure leaf
│   ├── time/                    # pure leaf
│   ├── dispatchers/             # pure leaf
│   ├── application/
│   │   ├── bootstrap/ permission/ library/ playback/
│   │   └── driving/ voice/ settings/
│   └── domain/                  # pure policies
├── feature/                     # UI/presentation, never concrete device owners
│   ├── onboarding/ permission/ library/ search/ player/
│   └── driving/ settings/ liked/   # liked is removable P2
├── playback/                    # authoritative playback service owner
│   ├── api/                     # public port ONLY
│   └── controller/ service/ media3/ command/ interruption/ restore/
├── voice/                       # single voice coordinator owner
│   ├── api/                     # public STT/TTS/session ports ONLY
│   └── session/ mapper/ android/
├── data/                        # MediaStore authority, Room projection, DataStore
│   ├── api/                     # public repository/store ports ONLY
│   └── mediastore/ room/migration/ datastore/ repository/ mapping/
└── quality/                     # typed privacy-gated NoOp/optional sink
    ├── api/                     # public event/gate/sink ports ONLY
    └── privacy/ noop/ adapters/
```

Entries on shared tree lines above are shorthand for **distinct** committed directories (e.g. `core/application/bootstrap` and `core/application/permission`); the machine-enforced list in [`scripts/check_architecture_dependencies.py`](../../scripts/check_architecture_dependencies.py) is the exact 52-path inventory.

## 2. Matrix of permitted production package dependencies

A package import edge is allowed **only if explicitly listed below**. Importing a same-boundary implementation is not the same as importing an externally owned implementation. The composition/root rule does *not* let a feature directly access a concrete implementation.

| From | Allowed internal dependencies | Forbidden representative dependency |
|---|---|---|
| `core/model`, `core/result`, `core/time`, `core/dispatchers` | own leaf package; Kotlin and coroutines primitives | Android, Compose, Activity, ViewModel, Media3, Room, DataStore, SpeechRecognizer, analytics SDK, other concrete packages |
| `core/domain` | same domain, core leaves; pure Kotlin | Feature/UI, Media3, Android framework, Room, Voice/Playback/Data implementation |
| `core/application/**` | own application orchestration, core leaves/domain, **public** Playback/Voice/Data/Quality APIs | `playback/service`, `voice/android`, `data/room`, Activity or analytics SDK |
| `playback/api`, `voice/api`, `data/api`, `quality/api` | own API + core leaves; pure Kotlin/coroutines | their own implementation packages, concrete providers, Media3/Room/Android/Compose |
| `feature/{family}/**` | same family, core/application, core leaves, public API types for rendering | other feature families, Player/Media3/MediaSession, Room/DAO, MediaStore/Cursor, speech provider, quality SDK, concrete service/data classes |
| `playback/{implementation}/**` | all own `playback` packages including `api`, core leaves, only `data.api.RestorableQueueStore`, Android/Media3 | Feature/UI, Room/DAO, Voice provider or analytics adapter |
| `voice/{implementation}/**` | all own `voice` packages including `api`, core leaves, Playback public API (Gateway/command types), `data.api.LibraryRepository`; Android Speech only in `voice/android` | Concrete Player/MediaSession, feature screens, Room, quality SDK |
| `data/{implementation}/**` | own `data` packages including `api`, core leaves; Android MediaStore/Cursor only in `data/mediastore`; Room/DataStore behind respective owners | Feature/UI, Playback concrete/service, Voice concrete, Quality concrete |
| `quality/{implementation}/**` | own `quality` packages including `api`, core leaves; approved optional analytics SDK only in `quality/adapters` | Feature/UI, Playback/Voice/Data, Room, Player, MediaStore, Android Speech |
| `app/di/**` | approved public APIs and concrete implementations **only for composition** | Business logic or alternate state-owner construction (manual/contract review remains necessary) |
| `app/` startup/navigation except `app/di` | app itself, feature, core/application, core leaves, public boundary APIs and Android UI | concrete Playback/Voice/Data/Quality package constructors; cross-boundary wiring outside `app/di` |

The `data.api.RestorableQueueStore` and `data.api.LibraryRepository` exceptions are intentionally narrow public-port carve-outs, reflecting binding architecture §9.5–9.6; they do not authorize `playback` to import the entire Data API or Voice to access concrete Data implementation.

## 3. Automated enforcement and executable negative tests

**Committed checker:** [`scripts/check_architecture_dependencies.py`](../../scripts/check_architecture_dependencies.py). Uses Python standard library, so **no dependency installer, third-party parser or credentials**. Checks source files under the single production root only; unlike the spike's own namespace, all package declarations must agree with their source directory. It reads both Kotlin and Java imports (including aliases/static imports) and fully qualified references outside literal/comment text. It validates the exact 52-directory skeleton, `settings.gradle.kts` single `:app` topology and no active Gradle `includeBuild`. It fails with a non-zero exit and readable `FORBIDDEN_INTERNAL`, `FORBIDDEN_SDK`, `PACKAGE_MISMATCH`, `FORBIDDEN_GLOBAL_SCOPE` or module topology error.

Run from a fresh checkout of this branch:

```bash
python3 scripts/check_architecture_dependencies.py --root . --self-test
./gradlew projects :app:assembleDebug --no-daemon --stacktrace
```

The `--self-test` flag **does not skip checking production source**: it first generates temporary Kotlin fixture files, verifies allowed imports really pass and *deliberately forbidden imports fail*, then scans all committed production files. Fixture files are created under a temporary directory, **not** checked into production source. The initial suite has **23 cases (six valid and seventeen invalid)**: feature-to-Media3, other-feature, Playback service, Room, Android Speech, analytics; pure-core-to-Compose; application-to-DAO; API-to-own-concrete; Voice-to-Player; Data-to-Feature; Quality-to-Playback; app/navigation-to-DAO; fully qualified bypass with no import; aliased import; incorrect package declaration; and `GlobalScope` bypass.

The real root [GitHub Actions bootstrap workflow](https://github.com/latticeward-labs/music-copilot/blob/feature/4-android-foundation/.github/workflows/issue-4-phase2-bootstrap.yml) now runs the checker and fixtures **before SDK download and Gradle**. It is also triggered by changes to `scripts/check_architecture_dependencies.py`; it builds Debug and unsigned Release only after the architecture check passes. There is deliberately no separate CI universe where the spike's green APK is treated as a production build.

## 4. What the guard DOES and DOES NOT prove

**Enforced now:** physical-module count; declared package/path consistency; 52 architectural owners present; import/FQN edges, including aliases and Java static imports; known concrete framework/provider package exclusions; no `GlobalScope` token in production; positive/negative regression fixtures actually run.

**Not a semantic Kotlin compiler:** reflection/dynamic loading, Gradle plugin-defined indirect dependencies, cleverly constructed names, runtime DI cycles, ownership of live queue/player/voice sessions, Android class construction, Kotlin typealiases through existing code, transitive library dependencies and code-generated sources cannot all be conclusively determined by this regular-expression scanner. This limitation is documented rather than asserting perfect enforcement. **Future gates:** Phase 4 typed public contract unit tests, Phase 5 DI graph/lifetime checks, Phase 7 service/MediaSession ownership tests, Phase 11 more comprehensive static/runtime tests and Phase 12 production full-format/lint/test CI.

**Architecture non-negotiables remain unchanged:** one same-process `MediaSessionService` owner, one Voice coordinator generation, MediaStore source-of-truth, Room rebuildable projection, non-authoritative paused restoration, Quality NoOp default and privacy gating. This Phase 3 scaffold has **no runtime authority classes** and does not implement any feature logic.

### ADR-0008 module-extraction control

No extra Gradle module is allowed simply because a logical directory exists. Any future extraction must cite at least one documented ADR-0008 criterion (independent ownership/build isolation/reused contract/measurable build or test improvement/package-enforcement limitation), before/after dependency edges, build/performance gain and migration/rollback risk. A material change to architecture must go through a superseding ADR; Phase 3 introduces none.

## 5. Acceptance and evidence

**Observed evidence (complete):** [run 38424771876](https://github.com/latticeward-labs/music-copilot/actions/runs/38424771876), checkout `813818c12af5a2d110b314ddcb534db30db452ae` (job `115391154822`), logged **`SELFTEST PASS: 23 fixtures including real rejected violations`** and **`ARCHITECTURE PASS: one :app module; 52 package paths; 54 source files; no forbidden imports or qualified references`**. Gradle `projects` showed only `:app`; `:app:compileDebugKotlin`, `:app:assembleDebug`, `:app:compileReleaseKotlin`, `:app:assembleRelease` ran, followed by **`BUILD SUCCESSFUL in 6m 2s`**. [Actual Debug/unsigned Release artifact ZIP 11828810490](https://github.com/latticeward-labs/music-copilot/actions/runs/38424771876/artifacts/11828810490) SHA-256 `25e1db4ecf9a1ca67eb6edfd758dc84d6b511f18d1dcfaa9346e514e161c686c`.
 
**Failure/correction history retained:** [run 38424386020](https://github.com/latticeward-labs/music-copilot/actions/runs/38424386020) passed the former 22 fixtures but failed the real source scan due to a false-positive for generated `R` in `app/MainActivity.kt`; this was fixed, and a positive regression fixture was added. The previous marker-only run was cancelled by a new branch push. All actual CI outcomes are in [Phase 3 Validation JSON](ISSUE_4_PHASE3_VALIDATION.json). **Phase 3 PASS is now recorded**. No original Issue #4 implementation AC is automatically completed solely by a package-only scaffold; typed APIs and integration behavior belong to later phases.
 
## Phase 4 typed leaf exception — 13 October 2026

The Phase 3 checker originally treated all four leaf packages as mutually
isolated. This was too restrictive for the **binding** Issue #16 contract
`AppError.MediaItem.mediaRef: MediaRef?`: `core.result` has precisely
one required inward edge to `core.model`. This is now an explicit narrow
allow rule and self-test fixture. The reverse edge and every concrete
Android/provider dependency remain forbidden. No new owner or Gradle module.

