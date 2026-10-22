# Issue #6 — Phase P10: Media selection validation and PlaybackGateway handoff

**Date:** 2026-10-22 · **Branch:** `feature/6-mediastore-library-browse-search` · **Issue #6:** OPEN · **Implementation result:** `PASS_SCOPED_SOURCE`; **native/compiled/product acceptance:** **NOT_RUN** (P12).

## 1. Source and accepted boundary

Exact owner archive `/mnt/data/MSM2(1)(1).rar`, SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, identifies the same 12 authoritative English PRD, MVP, ADR, architecture and user-flow PDFs already traced in P00–P09. The accepted [ADR-0002](https://github.com/latticeward-labs/music-copilot/blob/main/docs/adr/ADR-0002_Service_Owned_Media3_Playback.md) means **Player, MediaSession and queue belong to service**, not a ViewModel. ADR-0003 gives MediaStore source-of-truth and Room-derived index only. P10 requirements are in `docs/implementation/ISSUE_6_PHASED_EXECUTION_PLAN.md` §P10 and [Issue #7](https://github.com/latticeward-labs/music-copilot/issues/7), which is **OPEN** and explicitly owns service-based Media3 playback, queue, notification, background, Bluetooth and persistence.

**Actual code evidence:** `app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt` still deliberately `return rejected()` for `PlaybackCommand.ReplaceQueue`, documenting full queue validation as Issue #7. The P10 handoff must therefore **fail safely** on this service. A fake gateway can test correct accepted/rejected contract, but **an actual song will not start playing merely because a selection command was sent**. The service was **not** modified in Issue #6.

## 2. Committed production implementation

| Path | Responsibility |
|---|---|
| `core/application/playback/LibraryMediaSelectionUseCase.kt` | Real application boundary: (1) Android OS permission; (2) accepted positive Room index revision, Ready and positive items; (3) **`LibraryRepository.get(ref)` source re-query and readable descriptor probe**; (4) exact source MediaRef match; (5) permission/revision recheck; (6) connected PlaybackGateway only; (7) dispatch exactly one `PlaybackCommand.ReplaceQueue` with one selected Ref, index 0 and playWhenReady=true; (8) only acknowledge matching service-confirmed current Ref + queue + supported PlaybackState after permission/revision recheck; (9) distinguish actual Playing vs accepted-but-not-playing. Cancellation propagates; missing/permission/stale/controller/rejection errors fail closed. Fresh UUID-backed CommandId per user intent (deterministic injection for synthetic tests). No filesystem path read, download, remote lookup or Player creation in app |
| `app/di/ApplicationBindings.kt` | Owns **one** selection use case, existing app-scoped Repository + real `PlaybackGateway`; OS media permission checked with `READ_EXTERNAL_STORAGE` (API29–32) or `READ_MEDIA_AUDIO` (API33+) **at selection time**, not from cached Compose state |
| `app/MainActivity.kt` / `app/navigation/AppNavHost.kt` | Pass one shared selection port to two Nav destination ViewModels. No Activity-owned Player or duplicate queue owner |
| `feature/library/LibraryBrowseViewModel.kt` | P08 song tap now selects only a row belonging to currently accepted revision/page; delegates to the common use case, cancels stale in-flight jobs on new group/category, permission/index change, Back/Close and ViewModel teardown; obsolete async replies dropped. Existing one-argument P08 fixtures remain backward compatible and preview-only |
| `feature/search/LocalSearchViewModel.kt` | Same P09 typed Search hit must belong to current Results+accepted revision, delegates to common use case and cancels stale selections on query change, permission/index change, close or Nav scope cancellation. Older one-argument P09 test fake remains preview-only |
| `feature/library/SelectedSongUiStatus.kt` | Safe typed presentation: Idle, Validating, service-confirmed Playing, service-accepted-not-playing, PermissionRequired, ItemMissing, LibraryChanged, PlaybackUnavailable |
| `feature/library/SelectedSongFeedback.kt` + `LibraryBrowseScreen.kt` + `LocalSearchScreen.kt` + `strings.xml` | Feedback is **not optimistic**. Only a service-confirmed matching snapshot with `PlaybackState.Playing` prints Playing; Buffering/Paused says accepted/pending; rejection, revoked grant or missing file gets actionable sanitized copy; no personal path or user metadata is logged. Close cancels pending result and returns focus to local browsing |

**Safety truth table**

| Source / service condition | Handoff action | UI result |
|---|---|---|
| Permission denied/revoked, invalid/obsolete scan revision | NO command; no requery when already denied | Access / stale-library recovery |
| Valid indexed song but OS ref missing/moved/not readable | NO command | Item missing/unplayable, rescan manually |
| Source ref mismatch or OS permission/revision changes during requery | NO command | Fail closed |
| Controller disconnected/unready | NO command | Playback unavailable |
| Actual Issue #7 service (ReplaceQueue unsupported) | Command attempted after verification; gateway returns rejection | **Playback unavailable**, never Playing |
| Controlled fake returns matching Playing snapshot | One approved command, ack matches selected ref and queue | Playing reported **only from fake service acknowledgement** |
| Controlled fake returns matching Buffering/Paused snapshot | One approved command; ack accepted | Accepted, **not yet Playing** |
| Controlled fake returns wrong ref/empty queue/Idle/Error | Reject malformed ack | Playback unavailable, no fabricated success |
| OS grant/index invalidated after dispatch | No success projection; Issue #7 must independently protect service queue | Access or stale-index state |
| User navigates away/changes song while request in flight | Cancel owning ViewModel job, ignore obsolete response | No stale UI state |

The actual MediaStore scanner `MediaStoreScanner.requery(ref)` already verifies volume-specific IDs, current `IS_MUSIC`/MIME and read-only `openFileDescriptor`; `RoomLibraryRepository.get()` uses it (not cached row existence) and translates failures into sanitized `AppError.MediaItem`. **P10 does not rescan the whole library** and never directly mutates music, Room or the service queue.

## 3. Authored tests and P12 obligations

- `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue6Phase10SelectionBoundaryTest.kt` — **16 authored JVM cases, 0 executed**. Explicit denied / mid-flight revoked grant, stale / scanning / Empty index, source missing/mismatched ref, service disconnect/rejection, valid single-item ReplaceQueue and command ID on controlled fake, accepted Buffering (not Playing), wrong/missing ack, cancellation and permission/index changes after ack.
- `docs/implementation/ISSUE_6_PHASE10_SELECTION_QA_MATRIX.md` — **24 P12 synthetic/physical/device cases, all NOT_RUN**, including tap from Library/Search, missing file, multi-volume, Settings, lifecycle, UI semantics, no hidden data writes, and cross-Issue #7 service limitation.
- `docs/implementation/ISSUE_6_PHASE10_VALIDATION.json` — exact live GitHub source readback, source blob evidence and scope-limited structural checks; **NOT a compiler report**.

## 4. Exact #7 dependency handoff

To make real playback work **later under Issue #7**, service owner must implement `ReplaceQueue` for validated local `MediaRef` with service-side OS media revalidation, queue rules, Media3 item content URI construction, ExoPlayer prepare/start acknowledgement, monotonic snapshot revision, controlled corrupt/missing item Skip, focus/interruption/media-button parity, background/lockscreen notifications and explicit lifetime/persistence acceptance. Tests must verify OS revocation **inside service**, not only P10 app precheck. P10's current one-item selected source handoff is a minimal approved application contract; **multi-item browse queue rules and real playback remain Issue #7**, and this work does not close #7.

## 5. Unresolved validation and final status

- P10 **source implementation committed** and statically inspected. No Kotlin/Android Gradle compilation, JVM execution, Android instrumentation, physical phone, media button, actual service playback, CI, APK, screenshot or independent review was executed for P10.
- P06 stays `IN_PROGRESS` due missing *genuinely generated* Room V2 `2.json` and real V1→V2 preservation/migration tests and 5,000-row performance gate. P09 source acceptance remains scoped.
- As agreed, make **one consolidated APK at P12 only** after P11; then run T01–T10 and owner-phone M01–M34, plus P10-S01..24; never mark unfinished issue criteria PASS without executed evidence.
- No PR/merge/main modification/Issue #6 close; all original product acceptance criteria remain unchecked. Phase P11 is next.
