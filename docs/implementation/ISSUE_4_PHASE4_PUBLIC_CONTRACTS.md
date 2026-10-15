# Issue #4 Phase 4 — Reviewed public architecture contracts

- **Date:** 13 October 2026
- **Scope:** Phase 4 only, no new runtime product feature
- **Status:** **PASS — typed contracts compile, JVM tests and SDK boundary guard green**
- **Tracked work:** [Issue #4](https://github.com/latticeward-labs/music-copilot/issues/4)
- **Branch:** [`feature/4-android-foundation`](https://github.com/latticeward-labs/music-copilot/tree/feature/4-android-foundation)
- **Code/test HEAD tested:** [`995050eb6ee02bdb7cfcb823a062b6fcc67fd775`](https://github.com/latticeward-labs/music-copilot/commit/995050eb6ee02bdb7cfcb823a062b6fcc67fd775)
- **Observed GitHub Actions:** [run 38427936983](https://github.com/latticeward-labs/music-copilot/actions/runs/38427936983), [job 115402348173](https://github.com/latticeward-labs/music-copilot/actions/runs/38427936983/job/115402348173), [artifact 11829238672](https://github.com/latticeward-labs/music-copilot/actions/runs/38427936983/artifacts/11829238672)
- **Original archive:** uploaded `MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, same as Phase 0 source authority register

## 1. Normative source reconciliation

The **accepted** [Issue #16 public signatures](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md), [Phase 10 contract corrections](../architecture/review/ISSUE_16_CONTRACT_CORRECTIONS_2026-08-30.md), [Software Architecture](../SOFTWARE_ARCHITECTURE.md), [runtime policies](../architecture/RUNTIME_POLICIES.md) and ADR-0001..0008 remain authoritative. This phase does not change topology, recovery ownership, local-first authority or six-intent product scope. The Phase 10 addendum overrides the two stale Phase 6 excerpts.

## 2. Implemented public contracts

All production classes are under `app/src/main/java/com/latticeward/musiccopilot/` inside the **one** `:app` module.

| Boundary | Compiled contract sources | Binding invariant |
|---|---|---|
| Shared leaf `core/model` | [Identifiers.kt](../../app/src/main/java/com/latticeward/musiccopilot/core/model/Identifiers.kt) | Typed opaque `CommandId`, `MediaRef`, `VoiceSessionId`, `VoiceCandidateToken` |
| Shared leaf `core/result` | [AppResult.kt](../../app/src/main/java/com/latticeward/musiccopilot/core/result/AppResult.kt), [AppError.kt](../../app/src/main/java/com/latticeward/musiccopilot/core/result/AppError.kt) | Closed typed error families, RecoveryAction, Recoverability; no Throwable/public exception text |
| `playback/api` | [PlaybackCommand.kt](../../app/src/main/java/com/latticeward/musiccopilot/playback/api/PlaybackCommand.kt), [PlaybackSnapshot.kt](../../app/src/main/java/com/latticeward/musiccopilot/playback/api/PlaybackSnapshot.kt), [PlaybackGateway.kt](../../app/src/main/java/com/latticeward/musiccopilot/playback/api/PlaybackGateway.kt) | Eight typed commands; StateFlow/execute authority seam; no Player constructed. Snapshot accepts only MediaItem, Playback or Interruption errors |
| `voice/api` | [SpeechToTextEngine.kt](../../app/src/main/java/com/latticeward/musiccopilot/voice/api/SpeechToTextEngine.kt), [TextToSpeechEngine.kt](../../app/src/main/java/com/latticeward/musiccopilot/voice/api/TextToSpeechEngine.kt), [VoiceSessionCoordinator.kt](../../app/src/main/java/com/latticeward/musiccopilot/voice/api/VoiceSessionCoordinator.kt) | Replaceable STT/TTS; one coordinator port; immutable/read-only presentation type for 1–2 confirmation options and current-token/index semantics |
| `data/api` | [LibraryRepository.kt](../../app/src/main/java/com/latticeward/musiccopilot/data/api/LibraryRepository.kt), [PreferencesRepository.kt](../../app/src/main/java/com/latticeward/musiccopilot/data/api/PreferencesRepository.kt), [RestorableQueueStore.kt](../../app/src/main/java/com/latticeward/musiccopilot/data/api/RestorableQueueStore.kt) | MediaStore source authority and Room derived projection (implementation pending); DataStore queue snapshot stays non-authoritative P1 |
| `quality/api` | [TelemetryEvent.kt](../../app/src/main/java/com/latticeward/musiccopilot/quality/api/TelemetryEvent.kt), [TelemetryPorts.kt](../../app/src/main/java/com/latticeward/musiccopilot/quality/api/TelemetryPorts.kt) | Closed typed event families, sanitized-only Sink type, internal SanitizedTelemetryEvent constructor; real allowlist validation deferred to Phase 9 |

**Phase 10 correction PH10-MAJ-001:** `VoiceSessionState.Confirming` now contains `options: List<VoiceConfirmationOption>` instead of the obsolete `optionCount`. Construction rejects 0/3 options and duplicate indexes. Kotlin `List` is a *read-only public contract*, **not a guarantee that a mutable backing list has been defensively copied**; the Phase 8 production coordinator must freeze/copy its published options and invalidate them on generation cancellation. No title/display label is logged or persisted by this phase.

**Phase 10 correction PH10-MAJ-002:** `PlaybackSnapshot.lastError: AppError?` permits only the service's three runtime families (`MediaItem`, `Playback`, `Interruption`), explicitly rejects `Controller` and unrelated families, and does not introduce a UI error bus. The exact real-world Service→Snapshot projection remains Phase 7.

**Narrow dependency correction:** `core.result.AppError.MediaItem` references `core.model.MediaRef` as required by the accepted public signature. The Phase 3 static checker now allows **only** the inward `core.result → core.model` leaf edge (never the reverse edge), with its own passing fixture. See [dependency rules](DEPENDENCY_RULES.md).

## 3. Real test coverage and execution

| Evidence | Recorded result |
|---|---|
| [CoreContractsTest.kt](../../app/src/test/java/com/latticeward/musiccopilot/foundation/CoreContractsTest.kt) | Typed results/failures, error recovery defaults, opaque IDs and stable code examples |
| [PlaybackVoiceContractsTest.kt](../../app/src/test/java/com/latticeward/musiccopilot/foundation/PlaybackVoiceContractsTest.kt) | All eight command shapes, three valid error families, invalid Controller/Library projection rejection, fake Gateway and STT/TTS/coordinator, one/two option shape, invalid option counts/indices and stale-token rejection |
| [DataQualityContractsTest.kt](../../app/src/test/java/com/latticeward/musiccopilot/foundation/DataQualityContractsTest.kt) | Fake Library, preferences, P1 Store and privacy-gate failure; typed event schema and sanitized-only sink port |
| [Static checker](../../scripts/check_architecture_dependencies.py) | `SELFTEST PASS: 24 fixtures including real rejected violations`; **7 allowed / 17 forbidden** |
| [Real CI](https://github.com/latticeward-labs/music-copilot/actions/runs/38427936983) | `ARCHITECTURE PASS: one :app module; 52 package paths; 68 source files`; executed `./gradlew projects :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease --no-daemon --stacktrace`, **BUILD SUCCESSFUL in 6m 6s**, conclusion `success` |
| [Artifact](https://github.com/latticeward-labs/music-copilot/actions/runs/38427936983/artifacts/11829238672) | Actual exported Debug and unsigned Release APK and JVM JUnit reports |

The command and test assertions **execute in a fake-port/JVM context**; they do not prove Media3 Player/service instantiation, real device lifecycle, remote cloud consent, runtime telemetry sanitization or Voice generation timing. Initial run [38427496212](https://github.com/latticeward-labs/music-copilot/actions/runs/38427496212) failed before the dependency checker correction. [38427770367](https://github.com/latticeward-labs/music-copilot/actions/runs/38427770367) was cancelled by a corrective push. These are preserved as genuine history, not misreported as passes.

## 4. Phase gate and explicit non-claims

- **PASS:** all five Phase 4 checklist deliverables, stable public type signatures, source-compiling ports, actual positive/negative dependency check, executed JVM contract tests and real Debug/Release build. Both Phase 10 corrections are present.
- **Deferred to Phase 5:** production DI composition, singleton/repository/Voice lifetimes, Activity recreation proofs.
- **Deferred to Phases 6–8:** DataStore/Room/MediaStore implementations, one real service-owned Player/MediaSession and coroutine/Voice cancellation behavior.
- **Deferred to Phase 9:** complete runtime typed telemetry allowlist/privacy gate and NoOp fault isolation.
- **Deferred to Phases 11–14:** full Android lint/formatter/check CI, two recorded device launches, independent approvals, PR merge and Issue closure.

**Original Issue #4 acceptance checklist:** only the upstream architecture-release criterion remains checked (**1/14**); the other **13** are still open because their full acceptance spans later phases. Issue #4 remains **OPEN / In Progress**. Next executable phase: **Phase 5 — composition root, scope and ownership**.
