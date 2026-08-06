# UF-05 - Manual Playback and Queue Control

**Product:** Music Copilot MVP v1.0  
**Priority:** P0 - Beta blocking; P1 restore branch  
**Updated:** 06 August 2026  
**Status:** User Flow specification and traceability - **PASS**; TS-02, TS-06, TS-08, and reviewer evidence remain open  
**Tracking:** GitHub Issue #3  
**Canonical diagram:** [`mermaid/UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.md`](mermaid/UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.md)  
**Raw Mermaid source:** [`mermaid/UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.mmd`](mermaid/UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.mmd)

## Authority and boundary

UF-05 defines the implementation-independent manual playback contract after UF-04 selects an accessible local track: how the active queue is formed, how Full Player and the Home mini-player observe authoritative playback, how Play, Pause, Next, Previous, Seek, and queue-item selection behave, and how the flow recovers from end-of-queue, corrupt or inaccessible media, controller loss, and the optional P1 paused-restore branch.

The official English PRD/MVP are the product authorities. The current GitHub ADR register is the architecture authority: ADR-0001 through ADR-0006 were reviewed and accepted on 06 August 2026. Older attached PDF or dated User Flow snapshots that still label these ADRs Proposed do not override the current repository record.

This flow approves user-visible playback and queue behavior, not visual layout, artwork treatment, animation, navigation implementation, Media3 class design, service/controller APIs, coroutine scopes, error hierarchy, database schema, persistence representation, or a numeric `T_PREVIOUS` value. Those details remain with Issue #15, Issue #16, TS-02, TS-06, and TS-08. UF-06 owns background/system-control surfaces; UF-11 owns calls, Audio Focus, headset/Bluetooth disconnect, and route-change policy.

## Ten-field traceability record

| Required field | Controlled value |
|---|---|
| Flow ID | UF-05 |
| User goal | Start and control lawful local playback manually, understand the actual queue/player state, and recover without losing the remaining queue or creating duplicate playback. |
| Priority | P0 - Basic Player and active service-owned state; P1 - queue/position restoration after the active lifecycle is lost. |
| User Story | The MVP P0 Basic Player scope supplies Play, Pause, Previous, Next, Seek, and Queue. US-03 supplies the shared requirement that the visible state remain the actual service-owned state. No new User Story is invented. |
| Functional Requirement | Contributes the in-app/manual half of **FR-02** and hands active continuity/system surfaces to UF-06; routes call/route behavior to **FR-03/UF-11**; defines the user-visible **FR-08 P1** paused-restore branch without promoting it to P0. The P0 Basic Player scope is complete at User Flow specification level in this record. |
| Screens and states | S-06 Full Player - Preparing, Playing, Paused, End of Queue, brief Corrupt/Inaccessible Item recovery, Controller Reconnecting; Home mini-player - synchronized current state and Full Player entry; Queue view; P1 Paused Restore. |
| Influencing ADRs | ADR-0001 Accepted - native Android observable state; ADR-0002 Accepted - service/MediaSession owns player, queue, position, commands, acknowledgement, and controller synchronization; ADR-0003 Accepted - MediaStore authority and local-only rebuildable/app-owned persistence boundaries; ADR-0006 Accepted - no account, backend, sync, catalog, streaming, upload, or download. |
| Failure and recovery | Start/command acknowledgement failure preserves last verified state; controller loss shows Reconnecting and refreshes full state; moved/deleted/inaccessible media is reconciled; corrupt items are skipped while playable items remain; an exhausted queue ends paused/completed with a concise error; revoked access hands off to UF-02/UF-03; invalid P1 restore never auto-plays. |
| Data and permissions | Playback service/MediaSession is authoritative for current item, queue, current index, playing/paused/completed state, duration, and position. Android permission and MediaStore remain authoritative for access and existence. P1 restore is local app-owned data only; representation is open. Personal media metadata is prohibited from telemetry. |
| Open assumptions or evidence | TS-02 lifecycle/process evidence; TS-06 access/move/delete evidence; TS-08 restore contract/evidence; Issue #16 numeric `T_PREVIOUS`, command serialization, controller/error contracts, and persistence mechanics; Issue #15 Full Player/Queue/mini-player Wireframes; UF-06 and UF-11 handoffs; Product/Design/Android/QA/Privacy review. ADR status itself is not open. |

## Preconditions and entry routes

UF-05 can begin through four controlled routes.

| Entry route | Required behavior |
|---|---|
| UF-04 acknowledged selection | Accept the service-owned queue/current-item result and open Full Player only after authoritative acknowledgement. |
| Existing Home mini-player | Observe the existing service-owned current item and state; opening Full Player does not restart or duplicate playback. |
| Return from another in-app playback surface | Reconnect/observe and render the complete authoritative state before controls are treated as ready. |
| P1 restorable snapshot | Validate locally, present Paused Restore, and require explicit Resume; never surprise auto-play. |

If there is neither a valid current item nor a valid restorable queue, Full Player is not fabricated. The user returns to Home / Library and UF-04 to select music.

## Manual selection and queue contract

This section resolves the user-visible queue handoff that UF-04 intentionally left open.

1. A deliberate track selection from a UF-04 Songs, Artist tracks, Album tracks, Folder tracks, or Search Result collection **replaces** the active queue after service acknowledgement; it does not silently append to an older context.
2. The queue contains the currently represented track collection in its visible order, with the selected track as the current item. UF-05 does not decide the default browse/search sort; it preserves the order the user actually saw.
3. Before acceptance, the selected item and queue candidates are revalidated against current Android access and MediaStore availability. Inaccessible candidates are not presented as playable queue items.
4. If a new selection cannot be accepted, the previous verified active queue/playback remains authoritative and unchanged; the UI returns to the source context with a concise recoverable error.
5. Queue replacement, current-index change, and playback start become visible only after service/MediaSession acknowledgement. No optimistic Playing, queue, or position state is permitted.
6. The MVP Queue view exposes the authoritative ordered sequence, current item, and selection of another queue item. Reorder, remove, clear, append, shuffle, repeat, playlist editing, and save-queue behavior are not approved v1.0 requirements.

Selecting another item from the Queue view requests that service-owned item as current and starts it after acknowledgement. If the item fails validation or preparation, the corrupt/inaccessible recovery contract below applies.

## Full Player and mini-player contract

### Full Player

Full Player displays only controller-backed state for the current item, Playing/Paused/Preparing/Completed status, duration/position when known, and the active queue. It exposes Play/Pause, Next, Previous, Seek, open Queue, enter Driving Mode, and Back. Exact control placement, progress interaction, metadata fields, and artwork behavior remain Wireframe decisions.

### Home mini-player

The mini-player is a synchronized observation and re-entry surface for an existing current item. It may expose Play/Pause and opens Full Player. It never owns a second queue/player, never assumes stopped state during reconnect, and disappears or resolves to truthful recovery when no current/restorable item exists. Any additional inline control remains a later Wireframe decision.

### Navigation

- Full Player Back returns to the preceding non-driving app surface; if none exists, Home / Library.
- Back never stops active playback.
- Enter Driving Mode hands off to UF-07.
- UI closure, recreation, background, and screen-off hand off to UF-06 while preserving P0 service ownership.
- Calls, Audio Focus, headset/Bluetooth disconnect, and route changes hand off to UF-11.

## Manual command semantics

Every action is sent through the shared playback command boundary. The visible state changes only when the service/MediaSession reports the result.

| Command | Controlled behavior | Boundary / recovery |
|---|---|---|
| Play | From Paused, resume the valid current item at the authoritative position. From End of Queue, restart the retained final item from the beginning. Repeated Play while already Playing creates no duplicate player or queue. | If no valid current item exists, remain non-playing and route to Queue/UF-04 selection rather than inventing a track. |
| Pause | Pause at the authoritative current position and retain queue/current item. Repeated Pause is idempotent. | A failed acknowledgement preserves the last verified state and exposes recoverable controller/error feedback. |
| Seek | Request a position within the current item's known duration; clamp to `0..duration`. Seeking while Paused remains Paused. Seeking from Completed to before duration leaves the item Paused until Play. | Unknown duration or rejected seek leaves the verified position unchanged and reports a concise recoverable error. Exact gesture and preview are Wireframe decisions. |
| Previous | Use the fixed symbolic threshold `T_PREVIOUS`: before the threshold, move to the previous playable item; at/after the threshold, restart the current item at zero. At the first queue item, remain on it and restart at zero; never wrap. | The numeric threshold is deliberately unresolved and must be approved in Issue #16; implementations must not guess it. Invalid prior items use controlled skip/reconciliation. |
| Next | Advance to the next playable queue item. At the last item, remain at End of Queue / Paused-Completed; never invent repeat or wrap behavior. | A manual invocation at the end receives concise visual feedback. Voice announcement behavior belongs to UF-08. |
| Queue item selection | Request the selected authoritative queue item as current and start it after acknowledgement. | Invalid/corrupt/inaccessible selection follows controlled recovery; the UI does not fabricate the new current item. |

Natural completion advances to the next playable item. Natural completion of the final playable item enters End of Queue / Paused-Completed, retains the final item and queue for comprehension, and does not auto-repeat.

## Corrupt, inaccessible, and revoked-media recovery

1. A corrupt, unreadable, moved, deleted, or individually inaccessible current/candidate item emits a typed non-sensitive failure and is never shown as successfully Playing.
2. The service attempts a controlled forward skip through the remaining queue without wrapping.
3. If a playable item remains, it becomes current after acknowledgement, playback continues when the failed transition originated from an active play/advance request, and a brief error explains that an item was skipped.
4. If no playable item remains, playback ends in Paused/Completed, the queue is not reported as Playing, and a concise error offers Queue or Library recovery.
5. Stale derived rows are reconciled through UF-03. If Android media access itself is revoked, stop the affected attempt and hand permission recovery to UF-02 before any further playback claim.
6. Recovery must be bounded and must not loop forever over the same invalid items.

Only coarse reviewed `player_error` data such as `error_code` and general `route_type` may be emitted. Filenames, titles, artists, albums, folders, paths, URIs, queue contents, and positions are not telemetry.

## Controller connection and command acknowledgement

- While the controller is connecting or reconnecting, show a recoverable pending state and do not present controls as synchronized-ready.
- Never translate controller loss into a fabricated Paused, Stopped, empty Queue, zero position, or new player instance.
- On reconnection, refresh current item, queue, index, playing/paused/completed state, duration, and position before presenting Full Player or mini-player as ready.
- A rejected, timed-out, or failed command preserves the last verified snapshot and offers Retry where safe; commands are not stacked optimistically in the UI.
- Exact timeout, serialization, retry-count, and error-class mechanics remain Issue #16 decisions.

## P0 active continuity versus P1 paused restore

These are separate behaviors and must never be merged.

| Branch | Priority | User-visible contract |
|---|---:|---|
| Active service still owns playback while UI closes/recreates | P0 | Playback continues if it was Playing. Reconnected UI refreshes the complete actual state. No restore prompt and no second player are created. Detailed system surfaces belong to UF-06. |
| Active lifecycle is lost but a valid local snapshot exists | P1 / FR-08 | Reconstruct only the approved queue/current item/position as **Paused Restore**. Show what can be restored and require explicit Resume. Never auto-play. |
| Snapshot is partial or stale | P1 | Revalidate items, remove/reconcile inaccessible entries, preserve remaining order, and keep Paused. If the previous current item is invalid, identify the next valid item without playing it; explicit Resume is still required. |
| No playable restored item remains or restore fails | P1 | Discard the unusable playback restore state without deleting user media; route to Home / Library and UF-04. Show a concise recoverable explanation. |

The exact persisted fields, schema, migration behavior, save timing, expiry, task-removal/process-death limits, and same-process versus later-entry behavior remain TS-02/TS-08/Issue #16 evidence. The user-visible prohibition on surprise auto-resume is approved here.

## Detailed state inventory

| State | Trigger / entry | Visible state | Allowed actions | System action / source of truth | Success transition | Failure transition | Recovery | Persistence | Analytics / privacy | Accessibility / driving |
|---|---|---|---|---|---|---|---|---|---|---|
| Queue acceptance / Preparing | UF-04 selection or Queue item request | Existing verified state plus bounded pending feedback; no false Playing | Back/cancel if supported; wait | Service validates and accepts queue/current item | Full Player Playing or Paused according to actual result | Rejected, controller unavailable, corrupt/inaccessible item | Preserve old verified queue; source context Retry/another selection | Live queue belongs to service | Coarse error only; no media payload | Progress/error announced; controls not falsely enabled |
| Full Player Playing | Service reports Playing with valid current item | Actual metadata, position, duration when known, queue/current item | Pause, Next, Previous, Seek, Queue, Driving Mode, Back | MediaSessionService owns all live state | Paused, new item Playing, End, or handoff | Command failure, controller loss, media failure | Preserve last verified state; reconnect or controlled skip | P0 live service state | No new command event; reviewed `player_error` only | 48dp minimum, TalkBack labels, scalable text, state not color-only |
| Full Player Paused | Service reports Paused | Actual current item/queue/position and Paused | Play, Next, Previous, Seek, Queue, Driving Mode, Back | Controller-backed observation | Playing, another item, or End | Same as Playing | Same as Playing | Retain live queue/position in service | Same privacy rule | Paused state and each action announced clearly |
| End of Queue / Paused-Completed | Final playable item completes or Next is invoked at end | Final item retained; explicit end state; not Playing | Play restarts final item, Previous, Seek, choose Queue item, Back/Browse | Service reports completed/end with queue retained | Playing after explicit action or new selection | Controller/media failure | No repeat/wrap; concise feedback and Library path | Live queue remains service-owned; P1 snapshot only if approved | No personal metadata | Textual end state; no color-only meaning |
| Queue view | User opens Queue | Authoritative ordered sequence and current item | Select item; Back | Observe service queue; no local duplicate | Selected item acknowledged and plays | Item invalid or command rejected | Controlled skip/error; retain actual queue | Service-owned live state | Queue contents prohibited from telemetry | Current item and selection semantics exposed to TalkBack; no Driving Mode list |
| Corrupt / inaccessible item recovery | Prepare, advance, selection, or completion encounters invalid item | Brief non-sensitive skipped-item or terminal error feedback | Continue with next actual item; Queue; Library | Service emits typed failure; MediaStore/index reconciliation as applicable | Next playable item or recoverable queue state | No playable item; access revoked | End Paused/Completed or UF-02/UF-03 | Never persist false playability | Coarse `player_error` only | Error concise, announced, and non-blocking |
| Controller Connecting / Reconnecting | Entry or controller loss | Pending/recoverable state; no fabricated playback values | Retry if offered; Back | Reconnect then refresh full controller snapshot | Full Player/mini-player actual state | Repeated failure | Preserve service playback; do not create second player; return to prior surface | No UI-owned live state | Coarse connection error only if approved | Pending/error announced; focus not trapped |
| Home mini-player | Valid existing current item observed outside Full Player | Actual current item and Playing/Paused/Completed state | Open Full Player; Play/Pause if exposed | Same MediaSession/controller state | Full Player or updated state | Reconnect/no valid item | Truthful reconnect, hide, or Library recovery | No duplicate state | No media metadata telemetry | Labeled player/action; not the Driving Mode surface |
| P1 Paused Restore | Active lifecycle lost and valid snapshot recovered | Restored queue/current item/position clearly Paused | Explicit Resume, Queue, discard/back | Local snapshot is validated; service becomes live authority only after acceptance | Playing after explicit Resume or Paused queue inspection | Partial/stale/failed restore | Reconcile/drop invalid items; Library if none remain | Local P1 representation; details TS-08 | Restore contents/position never telemetry | Clear "paused" and Resume semantics; no surprise audio |
| Restore unavailable | No usable snapshot or no playable item | Concise no-restorable-playback state | Return to Home / Library | Clear unusable app-owned restore record only | UF-04 selection | Repeated storage failure | Library remains available; no media deletion | No false queue | Coarse storage error only if approved | Recovery action labeled |

## Invariants

- MediaSessionService is the only live owner of player, current item, queue, current index, playing/paused/completed state, duration, and position.
- UI, mini-player, Queue view, Room, DataStore, and restored snapshots never become live playback authority.
- A new manual selection replaces the queue only after service acknowledgement; failure preserves the previous verified state.
- Play/Pause are idempotent and never create a duplicate player or queue.
- Previous uses the approved symbolic branch around `T_PREVIOUS`; its numeric value is not guessed.
- Next and natural completion never wrap or invent Repeat; Shuffle and Repeat are absent from approved MVP scope.
- Seek is clamped to the valid item range and never fabricates a position after rejection.
- Corrupt or inaccessible media never terminates the remaining playable queue where controlled continuation is possible.
- Back never stops playback; UI closure/recreation is not equivalent to process-death restoration.
- P0 active continuity and P1 paused restore remain visibly distinct; P1 never auto-plays.
- No account, backend, sync, catalog, streaming, upload/download, Android Auto, iOS, Cast, playlist system, EQ, crossfade, DSP, or v1.1 capability is introduced.
- No personal media metadata, queue contents, or precise playback position enters telemetry.
- Queue and long-list interaction are unavailable inside Driving Mode.

## Open decisions and evidence

| Gate | Owner | Required evidence / decision | Effect on UF-05 status |
|---|---|---|---|
| TS-02 | Android + QA | Lifecycle matrix for rotation, Activity recreation, UI closure, task removal, service lifetime, controller reconnect, and process death | Flow specification passes; executed P0/P1 lifecycle proof remains open |
| TS-06 | Android + QA | Permission/revocation, moved/deleted/inaccessible media, stale queue/index reconciliation across supported Android/OEM versions | Flow specification passes; device evidence remains open |
| TS-08 | Android | Minimum local restore data, validation, migration/error behavior, save timing, and paused-restore proof | P1 user-visible branch passes; persistence contract/evidence remains open |
| Issue #15 | Product Design | Full Player, mini-player, Queue view, Preparing/Reconnecting/End/Error/Restore presentation, Back behavior, seek interaction, and accessibility | Blocks Wireframe approval, not UF-05 traceability |
| Issue #16 | Product + Android | Numeric `T_PREVIOUS`; command ordering/acknowledgement; service/controller contracts; error hierarchy; queue/persistence implementation; process-death limits | Blocks final architecture/implementation approval |
| UF-06 | Product + Design + Android | Notification, lock screen, Bluetooth/head-unit, screen-off/background, and system-control synchronization | Blocks FR-02 and Cluster B completion, not UF-05 manual-flow handoff |
| UF-11 | Product + Android + QA | Calls, Audio Focus, disconnect/reconnect, route changes, and conditional resume | Blocks FR-03 completion, not UF-05 manual controls |
| Role review | Product, Design, Android, QA, Privacy | Review against PRD/MVP, accepted ADRs, recovery, privacy, accessibility, and P0/P1 boundaries | Final Issue #3 approval remains open |

## Exit checklist and decision

- [x] One ten-field UF-05 traceability record exists.
- [x] UF-04 selection, existing mini-player, in-app return, and P1 restore entry routes are explicit.
- [x] The user-visible queue replacement/order/current-item contract is deterministic and implementation-independent.
- [x] Full Player, mini-player, Queue view, Preparing, Playing, Paused, End, Corrupt/Inaccessible, Reconnecting, and Paused Restore states are represented.
- [x] Play, Pause, Next, Previous, Seek, and queue-item selection have explicit acknowledgement and boundary behavior.
- [x] End of Queue does not invent Repeat/wrap behavior.
- [x] `T_PREVIOUS` branch semantics are explicit while the numeric value remains honestly open for Issue #16.
- [x] Corrupt, unreadable, moved, deleted, individually inaccessible, revoked-access, and no-playable-item recovery are explicit.
- [x] Controller loss/reconnect never fabricates Paused/Stopped/empty state or a second player.
- [x] P0 active continuity is separated from P1 paused restore with explicit Resume.
- [x] Markdown and raw Mermaid logic are synchronized.
- [x] Current GitHub Accepted ADR status is used.
- [x] Privacy, analytics, accessibility, navigation, and Driving Mode exclusions are explicit.
- [x] No outside-MVP playback, queue, platform, or service capability was introduced.
- [ ] recorded.
- [ ] TS-02, TS-06, and TS-08 executed evidence recorded.
- [ ] Numeric `T_PREVIOUS` and final service/controller/persistence mechanics approved in Issue #16.
- [ ] Product/Design/Android/QA/Privacy review recorded.
- [ ] UF-06 completed and Cluster B exit decision recorded.

**Decision:** UF-05 is complete and passes at the User Flow specification and traceability level. The P0 Basic Player manual behavior and the user-visible P1 paused-restore branch are fully mapped. This decision does not approve executed lifecycle/storage evidence, the numeric Previous threshold, Cluster B, Wireframes, final Software Architecture, implementation, or closure of Issue #3.
