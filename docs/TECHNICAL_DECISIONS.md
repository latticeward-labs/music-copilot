# Technical Decisions — MVP v1.0

- **Version:** MVP v1.0
- **Status:** Accepted technical baseline; synchronized with Issue #16 through Phase 9
- **Updated:** 30 August 2026
- **Tracking:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- **Authority:** [PRD v1.0](PRD.md), [MVP v1.0](MVP.md), accepted [ADRs](adr/README.md) and the current [Software Architecture Baseline](SOFTWARE_ARCHITECTURE.md)

This file is the compact decision index. It does not replace `SOFTWARE_ARCHITECTURE.md`; when this index and a detailed architecture artifact differ in level of detail, the accepted ADRs and the current Software Architecture baseline control implementation.

## Accepted baseline

| Concern | Accepted decision | ADR / baseline |
|---|---|---|
| Platform | Native Android 10+ with Kotlin | [ADR-0001](adr/0001-native-android-platform.md) |
| UI/default stack | Jetpack Compose; Coroutines/Flow; DI composition root with replaceable implementation tooling | [ADR-0001](adr/0001-native-android-platform.md), [ADR-0008](adr/0008-minimal-modular-monolith-and-dependency-enforcement.md) |
| Playback | Service-owned Media3/ExoPlayer outside UI lifecycle | [ADR-0002](adr/0002-service-owned-media3-playback.md), [ADR-0007](adr/0007-same-process-playback-runtime-and-safe-paused-restore.md) |
| Runtime | Same-process `MediaSessionService`, serialized command processor, structured concurrency and typed reconnect/recovery | [ADR-0007](adr/0007-same-process-playback-runtime-and-safe-paused-restore.md) |
| Data | MediaStore authority, rebuildable Room projection/app-owned data, DataStore settings/consent and P1 Safe-Paused snapshot | [ADR-0003](adr/0003-local-first-data-boundaries.md), [ADR-0007](adr/0007-same-process-playback-runtime-and-safe-paused-restore.md) |
| Voice | Replaceable STT/TTS interfaces; one active Voice session generation | [ADR-0004](adr/0004-replaceable-voice-provider.md), [ADR-0007](adr/0007-same-process-playback-runtime-and-safe-paused-restore.md) |
| Interpretation | Deterministic mapper with exactly six English executable intents and bounded ambiguity/retry policy | [ADR-0005](adr/0005-deterministic-intent-mapping.md) |
| Product services | No product backend, account, sync, catalog or streaming | [ADR-0006](adr/0006-no-backend-account-streaming.md) |
| Package/module strategy | One initial `:app` modular monolith with explicit logical packages and evidence-driven extraction | [ADR-0008](adr/0008-minimal-modular-monolith-and-dependency-enforcement.md) |
| Quality | Typed allowlisted diagnostics through `TelemetryPrivacyGate`; support adapters are non-authoritative and non-blocking | [Software Architecture](SOFTWARE_ARCHITECTURE.md), [Validation](architecture/ISSUE_16_VALIDATION.md) |

## Finalized logical boundaries

UI/Presentation, Application/Domain, Playback, Voice, Data and Quality/Diagnostics are separate logical boundaries. Their responsibilities, public contracts, dependency direction, state ownership, lifecycle, concurrency and persistence rules are finalized in:

- [Implementation Structure and Public Contracts](architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md)
- [Runtime Policies](architecture/RUNTIME_POLICIES.md)
- [Architecture Traceability Matrix](architecture/ARCHITECTURE_TRACEABILITY_MATRIX.md)
- [Software Architecture Baseline](SOFTWARE_ARCHITECTURE.md)

## Binding runtime and ownership rules

- `MediaSessionService` is the live Playback/Queue/Position authority; UI and system surfaces observe and submit commands.
- All playback mutation converges on one sequential command processor; no optimistic success is fabricated.
- P0 UI recreation/closure is distinct from P1 cold restoration. Persisted restoration prepares Paused only and never overrides a live session.
- MediaStore is device-media existence/identity truth; Room is a rebuildable derived projection; DataStore stores settings/consent and approved non-authoritative snapshot data.
- Voice is push-to-talk, owns one active generation, times out after six seconds and permits one explicit Retry.
- Voice provider output never bypasses deterministic mapping/confirmation or directly owns Player state.
- Call/focus/route recovery uses guarded resume; route disconnect pauses and reconnect never auto-resumes by itself.
- Raw voice/transcripts and prohibited personal media/identity data do not enter telemetry.
- Quality/analytics/configuration failure cannot block Playback, Voice cancellation, permission recovery or route safety.

## Numeric policy retained by the baseline

- Seek Back: **10 seconds**
- Previous: restart current item when position is **> 3 seconds**; otherwise previous valid item
- Voice listening timeout: **6 seconds**
- Retry: **one**
- Direct provider confidence default: **0.90**
- Future validated confidence range: **0.80..0.98**
- Artist clear similarity: **0.88**
- Plausible similarity: **0.72**
- Minimum top-two separation: **0.08**

Missing or invalid confidence never direct-executes. Tuning cannot add intents or bypass confirmation.

## Resolution of decisions previously deferred to Issue #16

| Formerly deferred concern | Current resolution |
|---|---|
| Navigation/back and Voice overlay ownership | Single-activity navigation; Voice is overlay state; Back cancels Voice first; Driving Exit returns to origin |
| Screen/shared state ownership | Explicit single-source ownership in the Software Architecture and state-ownership diagram |
| ViewModel/use-case boundaries | Explicit ports/use cases in the Phase 6 public contracts |
| Package/Gradle strategy | One `:app` modular monolith with package boundaries; extraction is evidence-driven |
| P1 queue restoration | Versioned non-authoritative snapshot; MediaStore revalidation; Paused-only restore |
| Error/concurrency policy | Typed `AppError`/`RecoveryAction`, injected scopes/dispatchers, serialized playback commands |
| Service/controller type and process boundary | Same-process `MediaSessionService` plus application-scoped controller connection |
| Process-death limits | P0 does not guarantee process-death survival; P1 cold restore is separate |
| Schemas/migrations | Explicit DataStore/Room ownership and migration/rebuild rules |
| Provider adapter details | Adapter remains replaceable; cloud/remote path requires separate approved consent/disclosure gate |

No implementation team should re-open these as local guesses. A material incompatible change requires an ADR review.

## Verification baseline

The test architecture is controlled by [ISSUE_16_VALIDATION.md](architecture/ISSUE_16_VALIDATION.md): unit seams for mapper/queue/seek/Previous/privacy, integration seams for MediaSessionService/Controller/Room/MediaStore/STT/DataStore/Quality, UI/service synchronization, recreation/reconnect, corrupt-file, permission, call/focus/route and Voice timeout/ambiguity tests, plus Android 10/12/14+, Pixel/Samsung/Xiaomi and 3+ real vehicle/head-unit downstream gates.

## Decision stages

1. **Issue #2:** **Done** — ADR-0001..0006 reviewed and Accepted.
2. **Issue #3:** **Done / Closed** — User Flows/state inventory approved 23 August 2026.
3. **Issue #15:** **Done / Closed** — low-fidelity Wireframes approved and closed 27 August 2026.
4. **Issue #16:** **In Progress** — architecture decisions, diagrams, contracts, runtime policies, validation and Phase 9 repository synchronization are complete; final cross-role review and exit/merge gate remain.
5. **Issue #4:** **Blocked** until final Issue #16 approval.

## Change-control rule

ADR-0001..0008 are Accepted. Phase 9 created no new ADR. If a future material choice conflicts with an Accepted ADR, preserve history and add a new ADR explicitly marked `Supersedes ADR-xxxx` rather than rewriting the accepted record.
