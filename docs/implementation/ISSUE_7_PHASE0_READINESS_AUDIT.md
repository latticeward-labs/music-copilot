# Issue #7 — P00 source, dependency, requirement and code readiness audit

- **Date:** 2026-10-22 (UTC+03:30)
- **Scope:** P00 **static/source audit only**, no feature implementation, execution QA, APK or CI certification.
- **Result:** **P00 AUDIT PASS — READY FOR P01**, with the implementation gaps and deferred QA explicitly listed below. This is **not** an AC01–AC13/runtime PASS.
- **Issue:** [#7](https://github.com/latticeward-labs/music-copilot/issues/7)
- **Execution branch:** `feature/7-media3-playback-queue-session`
- **Frozen audited production base:** `main` at `5679ee5b5f68b09b7715f3c31c339a2201b3c960`
- **Pre-P00 planning branch head:** `12910326fc30d364891855b9e29482ecd71228b7`. GitHub compare main..feature returned 5 commits, 0 behind, **only four new implementation-planning files; no production Kotlin changes**.
- **Source attachment:** User-provided `MSM2(1)(1).rar`, verified SHA-256 **`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`**. RAR5 opened successfully and all twelve embedded English PDFs were extracted and their text inspected. Same archive bytes as the source identity recorded in [ADR-0007](../adr/0007-same-process-playback-runtime-and-safe-paused-restore.md). No PDF is copied into the repository.

## 1. Attached English source inventory (verified SHA-256)

| Source PDF from RAR | SHA-256 | P00 relevance |
|---|---|---|
| `ADR-0001_Native_Android_Platform_Revised.pdf` | `7569f77eeb9e10e5113d974244ceec0f6133e4cf29fbfa95907c022abfd4fd51` | Android 10+, Kotlin/Compose, UI-independent playback |
| `ADR-0002_Service_Owned_Media3_Playback.pdf` | `f9ba6adfac14d5c04a33de55cf845abc56dee06ea0301ae7e66f926e83e729c9` | Sole service Player/MediaSession, system controls, routes, background, corrupt skip |
| `ADR-0003_Local_First_Data_Boundaries_Revised.pdf` | `1ceff4cd1f8d4021dd0e1c5ea3aec91c46b0b121d3f87919f913f3f53ce644a4` | MediaStore vs Room vs on-device P1 state; no remote state authority |
| `ADR-0004_Replaceable_Voice_Provider.pdf` | `b6acbaea7b7898306a6bd07254c24cec50e3de848d1d3631d5df357d3df29114` | Ducking/Voice coordination boundary; no new Voice provider in #7 |
| `ADR-0005_Restricted_Deterministic_Intent_Mapping.pdf` | `92db78041240707928f0765cdb6873c4e224fee982c0eaf2cd7c1a92b0949c2a` | Six-intent client must use the playback boundary, not own queue |
| `ADR-0006_No_Backend_Account_or_Streaming.pdf` | `0d500e878742d0bb0e7a7ff90f25cd4ccd59e74b93e28f5414d370c7ea7a0baf` | No streaming, account, sync, backend |
| `Music_Copilot_Code_and_Technical_Documentation_MVP_v1_EN.pdf` | `1d776396a88aebacbdb8b813fe8c6fe82418c951f301762ac594c55a7cd22cf3` | Playback boundaries, testability, exception/telemetry contracts |
| `Music_Copilot_MVP_v1_EN.pdf` | `98e89a1c9d530f4eb878af94f4f480fc8d4669ce2ed5193ba71f201b032d47b0` | MVP P0 player/route, P1 restore, Beta QA, US-03 |
| `Music_Copilot_PRD_v1_EN_Clarified.pdf` | `486fbd5057e4abf570cd1a4865b6243ee5e31d26a48596414903916d6d81e9cf` | FR-02 P0 background/session; FR-03 P0 focus/calls/routes; FR-08 P1 persistence |
| `Music_Copilot_System_Architecture_MVP_v1_EN.pdf` | `b056e3b8d3125e6660c2fd9431f4f883128673ed5ad1a7f3f5184dd179715bfb` | Layer/port ownership and decisions left for #16 |
| `Music_Copilot_User_Flow_Phase_0_Foundation_v1.0_EN.pdf` | `2393f443e5239a689f96e63d33648a696676a59d821858e12a5c57676b0b38e7` | Required manual/system/route paths, corrupt/restore behavior |
| `Music_Copilot_User_Flow_Phase_1_First_Readiness_v1.0_EN(4).pdf` | `d30dbe83808123852e31e26e4268d4b2b82f579cf9aa75b390ccf07f18d58e1e` | Media/notification permission and denial/recovery handoffs |

**Source hierarchy:** English PDFs govern product intent; the currently **Accepted** repository [ADR-0002](../adr/0002-service-owned-media3-playback.md), [ADR-0003](../adr/0003-local-first-data-boundaries.md), [ADR-0007](../adr/0007-same-process-playback-runtime-and-safe-paused-restore.md), [Runtime Policies](../architecture/RUNTIME_POLICIES.md), [Software Architecture](../SOFTWARE_ARCHITECTURE.md), and accepted [#17 screen handoff](../handoff/SCREEN_SPECS.md) govern previously undecided implementation mechanics. Historical PDFs' “Proposed” statuses do not revoke present Accepted ADRs. Exact 32 command slots, same-process ownership, 3-second Previous policy, P1 Proto DataStore and 5-second checkpoint come from repository contracts, not the historical PDFs.

## 2. Verified GitHub dependencies (2026-10-22)

| Item | GitHub state | Readiness decision / honesty |
|---|---|---|
| [#4 Android foundation](https://github.com/latticeward-labs/music-copilot/issues/4) | CLOSED / completed | Service/controller and Android scaffolding present. Closure used an owner-approved expedited review exception; do not reuse it as #7 playback QA. |
| [#5 onboarding/permissions](https://github.com/latticeward-labs/music-copilot/issues/5) | CLOSED / completed with owner exception | Interface is usable as a dependency; deferred technical/independent review tracked in [#73](https://github.com/latticeward-labs/music-copilot/issues/73) OPEN. |
| [#6 local Library/Search](https://github.com/latticeward-labs/music-copilot/issues/6) | CLOSED / owner-accepted WITH deferred QA | 28 user handset PASS, six N/A; not strict full technical PASS. [#74](https://github.com/latticeward-labs/music-copilot/issues/74) OPEN. Selection currently truthfully rejects playback because #7 ReplaceQueue is not implemented. |
| [#16 architecture](https://github.com/latticeward-labs/music-copilot/issues/16) | CLOSED / completed | Accepted service ownership, runtime, ports, contracts; no open implementation-blocking P0 architecture decision identified at P00. |
| [#17 HiFi handoff](https://github.com/latticeward-labs/music-copilot/issues/17) | CLOSED / owner-accepted | Full Player, Queue, MiniPlayer, Driving state/Compose specs available. Human review is deferred and not represented as independent PASS. |
| [#7](https://github.com/latticeward-labs/music-copilot/issues/7) | OPEN | P00 may proceed; **all 13 product ACs require #7 implementation/evidence**. No release-level certification. |

**Dependency decision:** No dependency prevents **P01 contract work** or subsequent implementation; existing deferred #5/#6 QA and cross-OEM/vehicle coverage are carried openly, not silently marked resolved. P0 safety behavior must still receive credible actual evidence before strict final acceptance.

## 3. Audited real implementation surfaces and gaps

| Production source in `main` | Present today / evidence | Remaining Issue #7 work |
|---|---|---|
| `app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt` | One `MediaSessionService`, one `ExoPlayer`, `MediaSession`, typed command callback and snapshot publication. **`applyCommand(ReplaceQueue)` explicitly returns `rejected()`.** | MediaStore-safe URI resolution and actual queue construction/playback, system command parity, audio focus and route policy, service lifecycle, errors (P03–P10). |
| `.../core/application/playback/LibraryMediaSelectionUseCase.kt` | Rechecks permission, revision and `LibraryRepository.get()`, dispatches `ReplaceQueue(items=listOf(ref), startIndex=0, playWhenReady=true)`; never fakes audible PASS. | Coordinate validated playable replacement and visible-collection multi-track selection without corrupting #6 search/browse/revision guarantees (P02/P04/P05/P07). |
| `.../data/repository/RoomLibraryRepository.kt` | `get(ref)` re-queries platform source (`source.requery(ref)`) and surfaces typed missing/unplayable/permission results; Room is a projection. | **Service independently** validates each queued ref/access/URI immediately before playback; does not trust UI or Room (P03/P10). |
| `.../playback/api/{PlaybackCommand,PlaybackSnapshot,PlaybackGateway}.kt` | Typed commands include ReplaceQueue, Play/Pause/Next/Previous/Seek/Stop; service-owned state contract exists. | Implement correct queue/position/edge semantics, external parity and full state mapping (P02–P06). |
| `.../playback/command/PlaybackCommandProcessor.kt` | One coroutine worker, 32 *waiting* slots, typed busy/rejection, teardown completion. | Guarantee all external MediaSession Player actions converge on approved serialization, idempotent execution and ACK; test overlap/error paths (P02/P06). |
| `.../playback/command/PlaybackNavigationPolicy.kt` | `PREVIOUS_RESTART_THRESHOLD_MS=3000`, `VOICE_SEEK_BACK_MS=10000`; pure Previous/SeekBack helpers exist. | Service currently directly uses `hasPreviousMediaItem()`/seek and does **not** apply this Previous rule; absolute seek upper clamp and end behavior require alignment (P02/P04/P05). |
| `.../playback/controller/PlaybackControllerConnection.kt` and `.../playback/media3/Media3SnapshotProjector.kt` | Asynchronous app-scoped controller, atomic snapshot relay, connection states, revisions and service-state projection exist. | Verify monotonic queue/snapshot revisions, reconnect, UI observation and private/system command parity with integration/field tests (P06/P08). |
| `.../data/api/RestorableQueueStore.kt`, `.../data/datastore/RestorableQueueDataStore.kt` | Versioned restorable-queue model and Proto DataStore adapter exist. | Hook service checkpoints to changes + 5-second debounced active write; live-session precedence, cold MediaStore requery, invalid ref drop, clamp, **Paused-only**, no autoplay (P11). |
| `app/src/main/AndroidManifest.xml` | Media-read permissions, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, exported `MediaSessionService` with `foregroundServiceType=mediaPlayback` and same-process default. | Actual foreground notification behavior, API 29/31/34+, notification-permission policy, background/OEM/system media controls must be tested; manifest alone proves no runtime PASS (P06/P08/P12). |
| [`docs/handoff/SCREEN_SPECS.md`](../handoff/SCREEN_SPECS.md) | WF-04 MiniPlayer/WF-06 Player/Queue/WF-07 Driving explicitly consume playback gateway states. | Native UI implementation, pending/No Track/Buffering/Paused/Corrupt, accessibility and queue gestures (P07/P10). |

**Critical conformance mismatch (must be explicitly resolved during P02/P04, not silently waived):**
[UF-05](../user-flows/UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.md) and [GS-02](../user-flows/golden-scenarios/GS-02_FIND_MUSIC_AND_START_PLAYBACK.md) specify that choosing a track from a visible Songs/Artist/Album/Folder/Search collection replaces the service queue with that **visible collection in its visible order** and selects its true index; the existing Issue #6 use case sends **only the selected track**. The user-visible P0 multi-item queue, Next/Previous, and final phone tests M10–M14 cannot be inferred from the present one-item handoff. Preserve the #6 single-selection safety/ACK behavior while extending the handoff with a reviewed explicit collection contract; do not invent shuffle, repeat, queue reorder or append.

**Other code-level risk and verification findings:** `MusicPlaybackService.applyCommand` currently does not enforce duration upper-clamp for SeekTo/SeekBy, does not implement the accepted 3-second Previous behavior, and system Player commands are not demonstrated to traverse `PlaybackCommandProcessor`. `Media3SnapshotProjector` increments revision on capture and queueRevision on timeline events: validate monotonicity/concurrency and no false success. Lifecycle, focus/calls/route loss, corrupted media, notification, task removal, speaker leakage and any P1 cold restore need implementations and actual tests. Do not classify static scaffolding as verified audible playback.

## 4. AC01–AC13 requirement → source → phase → evidence map

The row numbers here preserve the existing 13 Issue #7 acceptance criteria, in order. **Implementation status is OPEN/NOT VERIFIED for every row**; P00 establishes traceability, not runtime compliance.

| AC | Required observable behavior | Normative sources | Implementation phases | Final evidence |
|---|---|---|---|---|
| AC01 | Play, Pause, Next, Previous, Seek, Queue | MVP Basic Player, UF-05, ADR-0002 | P02/P04/P05 | M03–M14; T01–T03 |
| AC02 | Sole service Player/MediaSession independent of UI | ADR-0002/0007, runtime policies | P05/P08 | M15–M19; T04 |
| AC03 | Background and screen-off audio continuity | PRD FR-02, UF-06, GS-03 | P08 | M15–M18; T04 |
| AC04 | Real notification, lock-screen, Bluetooth controls | US-03, FR-02/03, UF-06 | P06/P08/P09 | M20–M27; T05–T06 |
| AC05 | UI state correctly follows service after recreation | ADR-0002/0007, GS-03, #17 handoff | P06/P07/P08 | M17–M19/M28; T05 |
| AC06 | Selected local Library item builds approved playback queue | FR-01/02, UF-04/05, GS-02 | P03–P07 | M02–M06/M12; T07 |
| AC07 | Controlled corrupt queue-item skip and continue | ADR-0002, UF-05, GS-05 | P10 | M29–M31; T08 |
| AC08 | Playing/Paused/No Track/Corrupt UI matches service | MVP Player states, #17 WF-06 | P07/P10 | M01/M04/M29–M31; T09 |
| AC09 | Deterministic queue, seek, previous, end tests | UF-05, ADR-0007 | P02/P04/P12 | T01–T03 |
| AC10 | Service/controller integration tests | ADR-0002/0007, GS-03 | P06/P12 | T04–T06 |
| AC11 | P1 queue and position restoration after UI close/lifecycle loss | PRD FR-08, ADR-0007 | P11 | M32–M35; T10–T11 |
| AC12 | Honest process-death and restoration limitations | ADR-0007, UF-06, GS-03 | P08/P11 | M35; T10–T12 |
| AC13 | No streaming/network catalog dependency | ADR-0003/0006, PRD/MVP | All; P12 | M36; T13 |

Mapping further depends on [UF-11](../user-flows/UF-11_INTERRUPTION_AUDIO_ROUTE.md) and [GS-05](../user-flows/golden-scenarios/GS-05_RECOVER_SAFELY_FROM_INTERRUPTION.md) for call/focus/disconnect guards; [UF-12](../user-flows/UF-12_SETTINGS_PRIVACY.md) for explicit recovery and privacy. For the actual final case instructions use [M01–M36 + T01–T15](ISSUE_7_FINAL_PHONE_QA_CHECKLIST.md) exclusively.

## 5. Normative runtime/privacy constraints to preserve

1. Exactly **one same-process** service-owned ExoPlayer/MediaSession, no second UI Player and no independent ViewModel/Room/DataStore live queue authority.
2. OS permission, MediaStore identity and read-only local Content URI validation remain the actual source of available playback; Room is derived.
3. Serialize UI, Voice and Android system playback mutations, with at most 32 waiting command slots and typed completion/rejection.
4. Active service audio after UI closure/background/screen-off is P0. P1 **Safe-Paused** local restoration is separate, versioned/atomic with 5-second debounced active checkpoint; a live session wins; cold restoration never autoplays.
5. Focus/call auto-resume only after verified guards; wired/Bluetooth disconnect immediately safe-pauses, reconnect never causes surprise speaker output or automatic Play.
6. Never send media title/artist/path/URI, queue contents, precise position, device identity, private audio/transcript, raw exceptions to telemetry; no streaming/backend/account/cloud dependency.
7. Android 10+ and API/foreground-service/notification differences require execution evidence; no app startup autoplay; no Android Auto, remote catalog, playlist editor, shuffle/repeat expansion.
8. Keep Issue #5/#6 deferred QA in #73/#74 distinct; one device is a limited owner MVP field scope, not multi-OEM or multi-vehicle certification.

## 6. P00 exit-gate and unresolved implementation work

| P00 audit gate | Result / concrete evidence |
|---|---|
| Source archive present, hashes/readability verified | PASS — archive and all 12 PDFs enumerated and read |
| Product FR-02/03/08 and P0/P1 distinguished | PASS — English PRD/MVP and Accepted ADR hierarchy confirmed |
| User flows/Golden Scenarios/WF handoffs mapped | PASS — UF-05/06/11/12, GS-02/03/05, WF-04/06/07 |
| Dependency and deferred QA accurately recorded | PASS — #4/#5/#6/#16/#17 closed; #73/#74 open; original #7 remains open |
| Production code entrypoints and genuine blockers located | PASS — specific Kotlin paths; ReplaceQueue failure + one-track-vs-visible-queue mismatch confirmed |
| AC01–AC13 and final M/T traceability | PASS — matrix above, all runtime acceptance NOT VERIFIED |
| Scope/authority/one APK rule protected | PASS — no streaming/account; no new Player/process; P13 sole final owner APK |
| Runtime verification/test execution | **NOT RUN / not part of P00** |
| Build, Gradle, emulator, CI, Actions, phone QA, PR/merge | **NOT RUN / not part of P00** |

**P00 decision:** **AUDIT PASS / READY FOR P01.** No unresolved source-readability or architecture-ownership blocker prevents P01. **This is not a claim that Playback works or that Issue #7 acceptance is complete.** P01 must lock the GitHub delivery/evidence contract and preserve the P13 only-one-final-APK instruction; P02 must resolve exact approved queue-source/order, command parity, Previous/seek/end semantics before P03–P05 coding. P03–P12 implement/test, and P13 alone builds the integrated owner APK and collects real observations. All [QA results](ISSUE_7_QA_RESULTS.json) remain **NOT_RUN**; do not pre-fill PASS.
