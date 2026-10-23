# Issue #7 — Planning baseline audit (22 October 2026)

**Classification:** READINESS PLANNING/STATIC SOURCE AUDIT ONLY. NOT P00 execution PASS, NOT Android feature test PASS. Implementation starts with P00 when requested.
**Attachment:** `MSM2(1)(1).rar` (12 English source PDFs) SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`; extracted, text-inspected locally.
**GitHub:** `latticeward-labs/music-copilot` main at `5679ee5b5f68b09b7715f3c31c339a2201b3c960`.
**Branch:** `feature/7-media3-playback-queue-session`.
**Controlling sources:** [PRD](../PRD.md) FR-02/03/08; [MVP](../MVP.md); [Software Architecture](../SOFTWARE_ARCHITECTURE.md); [ADR-0002](../adr/0002-service-owned-media3-playback.md); [ADR-0007](../adr/0007-same-process-playback-runtime-and-safe-paused-restore.md); [Runtime Policies](../architecture/RUNTIME_POLICIES.md); [Issue #17 handoff](../handoff/SCREEN_SPECS.md). English attached PDFs are historical product/technical sources; Accepted repository architecture resolves previously open decisions.

## Dependency truth

| Dependency | GitHub state verified | Limitation |
|---|---|---|
| Issue #4 Android foundation | CLOSED | Existing service/controller scaffolding != full playable #7 queue |
| Issue #6 local Library/Search | CLOSED, owner-accepted MVP exception | 28 owner phone PASS, six N/A deferred to [#74](https://github.com/latticeward-labs/music-copilot/issues/74); cannot transfer these outcomes to #7 |
| Issue #16 architecture | CLOSED; approved | Same-process MediaSessionService and ADR-0007 runtime policy binding |
| Issue #17 high-fidelity handoff | CLOSED | Human review deferred after MVP; approved implementation handoff usable now |
| Issue #5 QA debt | Separate [#73](https://github.com/latticeward-labs/music-copilot/issues/73) | Not silently assumed resolved |
| Issue #7 | OPEN | No implementation on this branch yet; original ACs not verified |

## Code read-back — present vs missing

| Existing real code (main) | Gap assigned to Issue #7 |
|---|---|
| `playback/service/MusicPlaybackService.kt` has one ExoPlayer, one MediaSession and `PlaybackCommandProcessor`; `ReplaceQueue` is explicitly rejected. | Real MediaStore-resolved and validated queue preparation, Media3 media items, audible playback, skipped bad entries, updated service policy. |
| `playback/controller/PlaybackControllerConnection.kt`, `playback/api/PlaybackGateway.kt` and `playback/media3/Media3SnapshotProjector.kt`. | Full system/session private-control parity, restoration/reconnect tests, notification/lock-screen and external control field tests. |
| `core/application/playback/LibraryMediaSelectionUseCase.kt` dispatches a single-track ReplaceQueue, validates Library and refuses fake accepted playback. | Service accepts correct selection; multi-item playback/queue source UX added without violating selection revision/permission contract. |
| `data/api/RestorableQueueStore.kt`, `data/datastore/RestorableQueueDataStore.kt`. | ADR-0007 5s debounced checkpoint, cold Safe-Paused requery and duration clamp, active-session precedence, no autoplay and process-death documentation. |
| `app/src/main/AndroidManifest.xml` includes FOREGROUND_SERVICE, FOREGROUND_SERVICE_MEDIA_PLAYBACK, MediaSessionService declaration. | Verify behavior on actual target SDK 36 / Android 10+ device and runtime system surfaces; declaration is not evidence of compatibility or playback continuity. |
| Issue #17 design handoff and #6 Library/Search screens. | Full Player, queue, notifications, states and non-optimistic UI-service coupling under #7. |

## Hard correctness constraints

- Playback P0 continues across Activity/UI closure and screen-off while service legitimately active. P0 **not** guaranteed across app process death/force-stop; same-process service by design.
- P1 FR-08: persisted queue/position aid in Proto DataStore; re-query referenced local files; **Paused-only** cold restoration; **never autoplay**, five-second debounced active checkpoint; no personal titles/paths in persisted snapshot.
- Playback mutations serialized under service authority, application/controller/UI only observe.
- Do not infer all #7 acceptance from earlier #4 foundation or #6 handset QA.
- Do not launch GitHub Actions as part of this planning work. Final-phase artifact is a **single user APK**; any instrumentation/test APK is internal test equipment only, not an additional user build.
- Lack of extra Android devices, head units and OEMs is labelled DEFERRED/NOT_RUN unless verified; owner may initially certify a single-device MVP exception but cannot claim unexecuted safety behavior PASS.

## Next execution operation

Start **P00** against this frozen branch/inputs, capture actual Issue #7 AC numbering **AC01–AC13** and implementation evidence. Then P01 → P12 sequential implementation and **P13 sole consolidated APK + owner M01–M36 + technical T01–T15 + final acceptance/merge decision**.
