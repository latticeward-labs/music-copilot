# UF-13 - Like and Liked Songs

**Stage:** Optional scope closeout (not a new cluster)  
**Priority:** P2 — first schedule cut  
**Updated:** 19 August 2026  
**Status:** Candidate complete; executable evidence and final sign-off recorded separately  
**Tracking:** Issues #3, #6, #15, #16, #17

## Ten-field traceability record

| Required field | Controlled value |
|---|---|
| Flow ID | UF-13 |
| User goal | Like a lawful local track and later play it from a simple Liked Songs screen. |
| Priority | P2. The whole surface is removable if Sprint capacity is unavailable; no P0/P1 flow may depend on it. |
| Product mapping | PRD FR-09; MVP Simple Like/P2; Phase-0 S-09 and D-027. |
| Surfaces and terminal states | Home/Library entry, Liked Songs Empty/Ready, Full Player; Empty, Ready, playback acknowledged, missing item reconciled. |
| Influencing ADRs | ADR-0002 service-owned playback; ADR-0003 local-first ownership; ADR-0006 no backend/account/sync/streaming. |
| Failure and recovery | Persistence failure preserves the last acknowledged state; stale media is removed from the playable projection; playback rejection returns Ready/Empty without false success. |
| Source of truth | App-owned local Like records for user intent; MediaStore/access layer for current media existence; playback service for queue/player state. |
| Analytics | No UF-13 event is added. Titles, artists, albums, paths, content URIs, and Like identifiers never enter telemetry. |
| Open downstream detail | Storage schema/migration, repository ordering, final copy/components, and production Media3 wiring remain #16/#15/#17/#6. |

## Scope boundary

UF-13 is a local favorites list. It is not a playlist system, recommendation feature, account profile, cloud backup, cross-device sync, online catalog, download, or streaming surface. Removing the P2 entry and Like control must leave every P0/P1 flow intact.

## Preconditions and entry

1. The P2 capability is included in the build/schedule.
2. Local-media access is currently available and the library projection can reconcile against MediaStore.
3. Liked Songs is opened from Home or Library. The Like control may also be used on the Full Player, but it does not navigate automatically.
4. If P2 is not shipped, the entry and Like control are absent; the user is not sent to a dead or disabled destination.

## Like / unlike path

1. The Full Player renders Like state from the observable app-owned repository, keyed through a stable domain media identifier.
2. **Like** requests one idempotent local insert. Repeated requests cannot create duplicates.
3. **Unlike** requests removal of only that app-owned Like record. It never deletes, moves, edits, or uploads the user's media file.
4. Visible success follows repository acknowledgement. A failed write leaves the last acknowledged state visible and exposes a concise Retry outside Driving Mode.
5. Like state persists locally across UI recreation and ordinary app restarts, subject to the approved repository/migration contract.

## Open Liked Songs

1. On entry, load acknowledged Like records and reconcile them against currently accessible local media.
2. Duplicate records collapse to one domain ID. Missing/inaccessible items are excluded from the playable projection and routed for safe local record reconciliation.
3. Preserve repository-defined order; UF-13 does not invent a sorting policy.
4. If no accessible liked item remains, show **Empty** with a concise explanation and Back.
5. Otherwise show **Ready** with the local tracks, Play list, per-row Play, Like/Unlike, and Back.

## Playback path

1. **Play list** submits the current accessible liked projection as a local queue with start index `0`.
2. Selecting a row submits the same queue with that row's index.
3. UI never owns playback. It waits for the service/controller acknowledgement.
4. On acknowledgement, transition to Full Player/Playing and observe service-owned state.
5. If an item becomes inaccessible before acknowledgement, reject the stale request, reconcile, and return to Ready or Empty. Current unrelated playback is not stopped merely because UF-13 failed.

## Empty and missing-item behavior

- Empty explains that liked local tracks appear here; it does not promote cloud content or recommendations.
- A removed, moved, permission-inaccessible, or otherwise invalid media item is never shown as playable after reconciliation.
- Reconciliation affects the app-owned Like projection only. It cannot delete media.
- If reconciliation leaves other valid items, remain Ready; if none remain, transition to Empty.

## Back, recreation, and schedule removal

- Back returns to the initiating Home/Library surface.
- UI recreation reloads repository state; it does not infer Like status from transient widget state.
- Active playback remains service-owned and continues according to UF-05/UF-06/UF-11.
- When the P2 capability is cut, no route, empty placeholder, analytics event, or P0 dependency remains.

## Accessibility and driving boundary

Liked Songs provides logical list semantics, announced Like state, clear selected/unselected labels, stable TalkBack order, scalable text, and at least 48dp interactive targets. Liked Songs is not a Driving Mode destination; no long list or retry flow is opened while actively driving.

## Completion boundary

UF-13 closes the implementation-independent flow, state, ownership, reconciliation, playback handoff, privacy, accessibility, and removability contract. It does not claim production UI, physical-device execution, final schema, or final Issue #3 complete-set approval.
