# Issue #6 — P10 validated media selection → PlaybackGateway: P12 QA cases

**Source date:** 2026-10-22. **Source authority:** supplied MSM2 archive SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, ADR-0002 service-owned Media3, ADR-0003 local-first, PRD FR-01, UF-04, GS-02, accepted Issue #16 playback boundaries, P06–P09 Browse and Search.

**Testing contract:** single aggregate APK only at P12; all cases below **NOT_RUN**. Fake accepted/rejected gateways in authored JVM tests are not real device playback evidence. Issue #7 owns implementation of real source-validated Media3 queue playback.

| ID | Scenario | Pass criterion for actual P12 test | State |
|---|---|---|---|
| P10-S01 | Library song tapped, authorized & cached | Requery OS MediaStore row on correct volume; verify readable descriptor before any gateway command | NOT_RUN |
| P10-S02 | Search song tapped | Same shared selection path, never a second Player, no cached-only execution | NOT_RUN |
| P10-S03 | Selected track deleted after Library scan | Typed Missing/Skip; no new service command, no optimistic Playing | NOT_RUN |
| P10-S04 | Selected item no longer readable after scan | Typed Unplayable/Missing; no command; user music files untouched | NOT_RUN |
| P10-S05 | MediaStore volume remounted/moved | Old volume-aware ref never cross-opens duplicate numeric ID on another volume | NOT_RUN |
| P10-S06 | Android OS permission denied before tap | No platform read and no command; review access | NOT_RUN |
| P10-S07 | OS revocation during MediaStore requery | Fail closed, no gateway command and no stale song detail | NOT_RUN |
| P10-S08 | Library indexing/revision changes during requery | Fail closed with stale-library status; require explicit later selection after rescan | NOT_RUN |
| P10-S09 | Controller disconnected | Do not send command, show recoverable unavailable; no fabricated Player | NOT_RUN |
| P10-S10 | Current #7 service rejects ReplaceQueue | Propagate PlaybackRejected as user-safe Unavailable, no Playing state | NOT_RUN |
| P10-S11 | Controlled fake gateway accepts correct Playing snapshot | Status Playing shown only if service explicitly acknowledges same source ref, item, queue and Playing | NOT_RUN |
| P10-S12 | Controlled fake acknowledges Buffering/Paused | Non-playing pending state, not an optimistic Playing | NOT_RUN |
| P10-S13 | Controlled fake acknowledges wrong media item | Reject as malformed acknowledgement | NOT_RUN |
| P10-S14 | Controlled fake acknowledges empty queue/Idle/Error | Reject as non-playable acknowledgement, maintain previous safe UI | NOT_RUN |
| P10-S15 | Permission revoked after command and before acknowledgment | UI rejects stale ack; service-own validation remains Issue #7 requirement | NOT_RUN |
| P10-S16 | New scan revision during pending service command | Drop/ignore obsolete selection feedback; no stale optimistic Playing | NOT_RUN |
| P10-S17 | User taps two items in quick succession | Old ViewModel job cancelled; only latest feedback appears; service queue state remains service-owned | NOT_RUN |
| P10-S18 | User leaves Search/Library or rotates during pending selection | No Activity-owned Player; no crash; request/feedback lifecycle follows Nav ViewModel | NOT_RUN |
| P10-S19 | Background, interruption, Bluetooth/media buttons | No Activity queue mutation; full service/system-media verification belongs Issue #7 | NOT_RUN |
| P10-S20 | Permission removed and regranted from Settings | No auto-start playback, explicit re-selection needed after fresh index verification | NOT_RUN |
| P10-S21 | Error/queued/playing status at 200% font and TalkBack | Clear safe status, readable/reflowing text and accessible Close; no private file path logs | NOT_RUN |
| P10-S22 | No backend, audio write, telemetry on tap | Local re-query/read-only probe; only typed service command dispatched | NOT_RUN |
| P10-S23 | Repeat tap after recoverable rejection | New user intent gets new command ID; no implicit loop/retry nor queue mutation in app layer | NOT_RUN |
| P10-S24 | Full QA on actual combined APK | T01–T10, M01–M34, Room generated 2.json, complete native migration and 5,000-song browsing; never assume from source audit | NOT_RUN |

## Written deterministic tests

`app/src/test/java/com/latticeward/musiccopilot/foundation/Issue6Phase10SelectionBoundaryTest.kt` has **16 authored JVM methods**, none executed. Covers permission, stale revision, missing/mismatched media ref, wrong ack, disconnected controller, actual service rejection simulation, accepted fake playing/pending, cancellation and permission/revision changes after service command.

P10 production code runs through `LibraryRepository.get(ref)` (real source `RoomLibraryRepository.get` delegates to `LocalAudioSource.requery`) before sending exactly one `PlaybackCommand.ReplaceQueue` with one confirmed MediaRef to the process-owned `PlaybackGateway`. Service `MusicPlaybackService` **deliberately rejects** `ReplaceQueue` until Issue #7. P10 never changes Service, ExoPlayer or MediaSession to force fake success. No monitoring, remote library, voice STT, account, metadata logging or file writes.

## Remaining issue-specific blockers

P06 missing **compiler-generated** Room V2 `2.json` and Android migration execution is still open. P11 quality-hardening and P12 consolidated tests, Android/JVM compilation and physical acceptance are pending. No CI, APK or individual-phase device run. Issue #6 stays OPEN with original product acceptance checkboxes not falsely checked.
