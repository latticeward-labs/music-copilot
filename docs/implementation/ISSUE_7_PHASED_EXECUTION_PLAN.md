# Issue #7 — Executable phased plan: Media3 Playback, Queue, Background Playback and MediaSession

> **Status:** P00 **STATIC SOURCE/READINESS AUDIT PASS** (2026-10-22); **P01 NEXT**. No Issue #7 feature implementation, executed JVM/Android tests, CI runs, APK or phone-test PASS is claimed.
> **Issue:** https://github.com/latticeward-labs/music-copilot/issues/7
> **Execution branch:** `feature/7-media3-playback-queue-session`; based on `main` commit `5679ee5b5f68b09b7715f3c31c339a2201b3c960` (2026-10-22).
> **Owner requirement:** Implement P00–P12 sequentially on one feature branch; **build and hand off exactly ONE consolidated, installable debug APK from the combined P00–P12 code in P13.** There is **no per-phase user APK** or per-phase owner handset test. User performs the final phone checklist and reports results; record only reported outcomes.
> **Authority:** owner-attached English 12-PDF `MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`; repository `docs/PRD.md`, `docs/MVP.md`, `docs/SOFTWARE_ARCHITECTURE.md`, Accepted ADR-0002, ADR-0003 and ADR-0007; Issue #16 runtime/port contracts; Issue #17 Player/Driving/MiniPlayer handoff; Issue #6 actual MediaStore + Library/Search implementation and recorded MVP acceptance exception.

**P00 execution evidence:** [Source, dependency, AC and code readiness audit](ISSUE_7_PHASE0_READINESS_AUDIT.md) — verified 12-PDF archive, GitHub baseline, #4/#5/#6/#16/#17 and #73/#74 dependency state, real Kotlin code gaps, and AC01–AC13 mapping. Audit PASS permits P01; **all 13 runtime ACs and 51 final cases remain unverified/NOT_RUN.**

## Current starting point and honest dependency assessment

- #4 foundation and #16 architecture are closed; #17 design handoff is closed with human review explicitly deferred until after MVP.
- #6 is **closed as MVP owner-accepted WITH DEFERRED QA**, not strict all-criteria engineering PASS; six handset N/A and technical debt are tracked in #74. #5 technical QA debt remains separately tracked in #73. Do not relabel either as PASS.
- Production `MusicPlaybackService` already owns one same-process `ExoPlayer` and `MediaSession`; manifest declares media playback foreground service; `PlaybackControllerConnection`, `PlaybackGateway`, `PlaybackCommandProcessor` and `Media3SnapshotProjector` already exist. Preserve, extend and test these — do not build another independent player or new process.
- **Critical implementation gap:** `MusicPlaybackService.applyCommand(ReplaceQueue)` explicitly rejects queue replacement. Issue #6 `LibraryMediaSelectionUseCase` dispatches `ReplaceQueue` and correctly refuses to pretend audible playback succeeded. Issue #7 must close this seam with real local URI resolution/validation and executable queue, not bypass the rejection.
- `RestorableQueueStore`, `ProtoRestorableQueueStore` and versioned snapshot structures already exist but the service's P1 checkpoint/restore orchestration and on-device verification are still required. Media3 system controls and interruption policies require actual integration/field evidence.
- Current GitHub code, not a historical PDF prototype, decides names/interfaces; any departure from ADR-0007 needs explicit architecture review.

## Binding architecture and privacy rules

1. **One service-owned Player/MediaSession** in the existing `:app` modular-monolith; never one per screen; no `android:process` split.
2. Queue, current item, position, player state, audio focus and acknowledged mutation belong to `MusicPlaybackService`. All UI, Voice and system controls converge on this owner.
3. UI/VM observe the full `PlaybackGateway.snapshot` and connection state, not optimistic or Room/DataStore-owned player truth. Await service acknowledgement; reconnect with an atomic authoritative snapshot.
4. Preserve `PlaybackCommandProcessor` serialized semantics and 32 waiting-slot bound; accepted commands must eventually finish or return a typed failure. System-surface Player commands must maintain the same invariants as private custom commands.
5. P0: true local sound output, playback while backgrounded or display off, system media controls, audio focus/interruption/route safety, and controlled corrupt-item skip. **P1 only:** versioned Safe-Paused queue/position restoration beyond the active service; never autoplay on cold startup.
6. No stream/network catalogue, account, cloud sync, raw paths, personal titles/artists/URIs in analytics/crash/diagnostic payloads. Do not store prohibited metadata in P1 snapshots.
7. Preserve OS media permission checks and Issue #6 source-of-truth: MediaStore existence, Room index as projection; resolve and revalidate selectable media; never trust a stale Room row to open playback.
8. Foreground service start, Android 13 notification behavior and Android 14+ mediaPlayback rules must follow runtime OS requirements; do not request unneeded permissions or create persistent services at ordinary app startup.

## Sequential execution phases (one integrated deliverable)

| Phase | Executable implementation tasks | Required handoff / scoped PASS evidence |
|---|---|---|
| **P00 — Source & readiness audit** | Reconcile 12 English PDFs, PRD FR-02/03/08, UF-05/06/11/12, GS, ADR-0002/0007, Issues #4/#6/#16/#17; inspect current main and test debts. Map all original Issue #7 ACs and concrete code gaps. | `ISSUE_7_PHASE0_READINESS_AUDIT.md`, source hash, dependency/AC inventory and blockers; **audit PASS only**, not playback PASS. |
| **P01 — GitHub execution/evidence contract** | Reuse this branch; lock scope, naming, exact P00–P13 gates, no-per-phase-APK policy, rollback and final PR target; establish evidence and test IDs. | Execution contract, traceability/evidence ledger, PR plan; no premature merge or CI PASS. |
| **P02 — Playback policy & domain contracts** | Freeze queue identity/order/index, ReplaceQueue validity, empty/no-track, Next/end, Previous threshold, seek clamping, stop, repeat/shuffle defaults, command idempotence, error mapping, Media3 external-command serialization and controller reconnect policy against ADRs. | Reviewed Kotlin-facing policy/API contracts and deterministic unit-test specifications; threshold/edge policies explicitly documented, not guessed during later coding. |
| **P03 — Safe local-media URI resolution** | Build service-safe MediaRef → MediaStore Content URI resolver with per-item OS permission recheck, read-only content probe, missing/moved/revoked behavior, cancellation and cache invalidation; correctly handle distinct volumes. Never use raw file paths or a Room-only success. | Injectable resolver + fake tests; typed `MEDIA_ITEM_*` outcomes; no unintended media/network access. |
| **P04 — Deterministic queue engine** | Implement validated `ReplaceQueue` for one/many real refs; current index, queue revision, full queue replacement, Skip/Previous/Seek, no-track/end-of-queue, async revalidation, invalid-entry policy and bounded capacity; metadata on Media3 items without compromising privacy. | Executable queue policy and unit tests for order/edge conditions; service remains sole queue owner. |
| **P05 — Real service-owned audio playback** | Wire P03/P04 into the existing `MusicPlaybackService` and `ExoPlayer` through the existing serialized command processor; prepare/play/stop/pause and report actual `Buffering/Playing/Paused/Ended/Error` from real Media3 callbacks. Connect Issue #6 selection use case without faked acknowledgement or duplicate player. | Playable local song and acknowledged service snapshots in implementation/integration tests; no new standalone player. |
| **P06 — MediaSession & controller parity** | Verify/implement Android `MediaSession` commands, private wire authorization, controller lifecycle/reconnect, authoritative notifications, lock-screen and media-key state; ensure system `Player` callbacks and private commands have the same queue rules and error handling. | Service/controller integration tests, custom-command isolation, external control parity and monotonic snapshot/revision verification. |
| **P07 — Player/Queue/MiniPlayer Compose handoff** | Bind approved #17 full Player, MiniPlayer and queue interactions to service-backed ViewModels/Gateway; render Playing, Paused, No Track, Buffering, End, Corrupt/recovery; seek progress, current metadata, disabled states, talkback, 150–200% text; Library/Search tap enters real playback. | Usable native UI wired to accepted state/action matrix; no optimistic Playing nor additional UI queue authority. |
| **P08 — Background, lifecycle & system surfaces** | Media3 foreground lifecycle/notification, task removal, Home, back, screen-off, rotation and controller reconnect; guard no autoplay on first launch and no premature teardown of legitimate active playback. | Android instrumentation/lifecycle tests; service state survives ordinary UI closure; notification/system surfaces track the service. |
| **P09 — Audio Focus, calls & safe route changes** | Implement approved interruption reducer for duck/pause/resume; protect manual Pause; route-generation gating; immediate headset/Bluetooth loss safe-pause without speaker leakage; reconnect requires explicit resume; stale events idempotent. | Deterministic focus/route tests and device test hooks; preserve single playback owner and no uncontrolled resume. |
| **P10 — Corrupt/missing/permission recovery** | Skip unreadable/malformed items with bounded retry/skip (no infinite loop), continue viable queue, show typed non-sensitive error and concise recovery; reject invalid current/queue refs; handle media access revoked mid-play and storage removal safely. | Fault-injection tests and UI recovery mapping; no leaked paths, titles, IDs or exception text in telemetry. |
| **P11 — P1 Safe-Paused persistence** | Integrate existing `RestorableQueueStore` (Proto DataStore) into service/app orchestration: atomic state changes + 5-second debounced active checkpoint, version/queueRevision/index/position; prefer live session on reopen; cold requery/drop invalid/clamp time and restore **Paused only** with no boot/autoplay. Document process-death vs task/UI closure vs force-stop limits. | Restoration unit/integration tests for valid/invalid/future schema, stale items, no autoplay and live-session precedence. |
| **P12 — Full feature integration & technical QA preparation** | Cross-check flows from #6 Library/Search through selection/queue/session/UI and Voice gateway contract (no Issue #11 speech implementation), align handoff/accessibility, add JVM/Android instrumentation/fake/fault tests, privacy/manifest/lint checks, QA register and final acceptance traceability. | P00–P12 combined source ready for final build; tests **authored** are not labelled executed; no owner APK yet. |
| **P13 — SINGLE combined APK, owner field QA & gated delivery** | **Only now** build/sign exactly one installable `app-debug.apk` from final combined P00–P12 HEAD; record SHA-256, commit, Gradle/toolchain, build/log/lint/JVM results and Actions run if explicitly authorized. Deliver one APK link and `ISSUE_7_FINAL_PHONE_QA_CHECKLIST.md` to owner. Collect owner's M01–M36 results on that *exact hash*; execute/document technical T01–T15 as available. Audit every original AC, create PR, review/merge/close only with verified evidence or transparent owner-authorized deferred-QA exception. | One full APK (not APK-per-phase), true owner results ledger, technical evidence, final audit and release/PR decision; do not close solely because APK installed. |

## Phase exit gates

- Every phase leaves code/docs on the same feature branch with scoped evidence and a clear next-phase handoff. Source-only/checklist/unit-test-authored work cannot be labelled execution PASS.
- Regression against #6 permission and Library behavior must remain absent; separately track the six #74 deferred tests, without treating #7 phone results as retroactive #6 evidence.
- No new paid/quota-consuming GitHub Actions runs in the planning change. During execution, local Gradle tests can be run as needed; reserve the complete integrated build, artifact publication and owner phone testing for **P13**. If Actions is required for artifact delivery, obtain explicit authorization for that quota-consuming run.
- The owner may test on one Android handset for MVP acceptance; missing API 29/31/34+, Pixel/Samsung/Xiaomi, second-device and vehicle coverage must remain `NOT_RUN`/deferred with issue tracking, never fabricated as PASS.
- No audio playback claims until physically or instrumentally verified. A successful command ACK alone does **not** prove audible sound or correct route.
- Original Issue #7 acceptance checkboxes remain unchecked until corresponding implementation + evidence is actually verified; P1 FR-08 retains its lower priority without undermining P0.
- Project completion means feature code, system sync and regressions verified, real QA classifications saved, review/merge policy honored and any deferred QA explicitly tracked; never silently close unverified P0 safety criteria.

## Original Issue #7 AC01–AC13 traceability

| AC | Primary phases | Final tests |
|---|---|---|
| AC01 Play/pause/next/previous/seek/queue | P02,P04,P05 | M03–M14,T01–T03 |
| AC02 Service-owned lifecycle | P05,P08 | M15–M19,T04 |
| AC03 Background & screen-off | P08 | M15–M18,T04 |
| AC04 Notification/lock-screen/Bluetooth parity | P06,P08,P09 | M20–M27,T05–T06 |
| AC05 UI authoritative sync/recreation | P06,P07,P08 | M17–M19,M28,T05 |
| AC06 Library selected track creates queue | P03–P07 | M02–M06,M12,T07 |
| AC07 Corrupt media skipped safely | P10 | M29–M31,T08 |
| AC08 Playing/Paused/NoTrack/Corrupt UX | P07,P10 | M01,M04,M29–M31,T09 |
| AC09 Deterministic queue/seek/previous/end tests | P02,P04,P12 | T01–T03 |
| AC10 Service/controller integration | P06,P12 | T04–T06 |
| AC11 P1 queue & position restoration | P11 | M32–M35,T10–T11 |
| AC12 Honest process-death contract | P08,P11 | M35,T10–T12 |
| AC13 No streaming/network catalog | all | M36,T13 |

**Final QA companion:** [Issue #7 one-APK phone checklist](ISSUE_7_FINAL_PHONE_QA_CHECKLIST.md). All statuses initially `NOT_RUN`.
