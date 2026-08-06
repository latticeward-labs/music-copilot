# Cluster B — Shared State Inventory

**Product:** Music Copilot MVP v1.0  
**Scope:** UF-04 Browse/Search → UF-05 Manual Playback/Queue → UF-06 Background/System Controls  
**Inventory date:** 06 August 2026  
**Tracking:** GitHub Issue #3  
**Shared State Inventory decision:** **PASS**  
**Formal Cluster B exit:** **OPEN**

## Authority and normalization rule

The official English PRD/MVP are the product authorities. The current GitHub ADR register is authoritative for architecture status; ADR-0001 through ADR-0006 are Accepted. Older PDF/RAR snapshots do not override the repository record.

This inventory normalizes the detailed state tables in UF-04, UF-05, and UF-06. A state that crosses flow boundaries receives one canonical CB identifier rather than being counted twice. The originating flow documents remain normative for detailed command semantics and open implementation decisions.

Controlled inputs:

- [UF-04 — Browse and Search Local Music](UF-04_BROWSE_AND_SEARCH.md)
- [UF-05 — Manual Playback and Queue Control](UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.md)
- [UF-06 — Background, Screen-off, and System Controls](UF-06_BACKGROUND_AND_SYSTEM_CONTROLS.md)
- [Cluster B Connected Review](CLUSTER_B_CONNECTED_REVIEW.md)
- [Cluster B Shared Traceability Matrix](CLUSTER_B_TRACEABILITY_MATRIX.md)

## Canonical shared inventory

| ID | State / scope | Trigger | Visible state | Allowed actions | System action / source of truth | Success transition | Failure transition | Recovery | Persistence | Analytics / privacy | Accessibility / driving |
|---|---|---|---|---|---|---|---|---|---|---|---|
| CB-01 | Home / Library Ready · UF-04 | UF-03 verifies accessible media | Songs, Artists, Albums, Folders and Search; existing mini-player if valid | Choose view, Search, select track, Rescan, open player | Derived index supplies browse data; service supplies live playback | CB-02/03/05/07/09 or existing CB-20 | Stale/unknown → UF-03; revoked → UF-02 | Reconcile or recover permission without false Ready | Verified index; live playback stays service-owned | No media names, paths, URIs or contents in telemetry | Labeled destinations, scalable text, TalkBack order; no driving typing/lists |
| CB-02 | Songs · UF-04 | User chooses Songs | Verified accessible track collection | Select track, Search, return | Query derived local index | CB-13 | Query/index failure or stale row | Retry or UF-03 reconciliation | No playback authority; view preference only if later approved | No new event approved | Accessible row name/action; meaning not color-only |
| CB-03 | Artists · UF-04 | User chooses Artists | Artist groupings | Open artist, Search, return | Query derived groupings | CB-04 | Query/index failure | Retry without deleting verified library | Same boundary as CB-02 | No personal media telemetry | Group/navigation labels exposed to TalkBack |
| CB-04 | Artist tracks · UF-04 | Artist selected | Local tracks for selected artist | Select track, return | Query derived index | CB-13 | Empty after reconcile or index error | Refresh; return CB-03; UF-03 if stale | Navigation context may be transient | No media payload telemetry | Clear heading/back; no driving long list |
| CB-05 | Albums · UF-04 | User chooses Albums | Album groupings | Open album, Search, return | Query derived groupings | CB-06 | Query/index failure | Retry without fabricated results | No new persistence requirement | No personal media telemetry | Accessible group labels/actions |
| CB-06 | Album tracks · UF-04 | Album selected | Local tracks for selected album | Select track, return | Query derived index | CB-13 | Empty after reconcile or index error | Refresh; return CB-05; UF-03 if stale | Navigation context may be transient | No media payload telemetry | Clear heading/back |
| CB-07 | Folders · UF-04 | User chooses Folders | Folder groupings for accessible audio | Open folder, Search, return | Derived index; paths never become telemetry | CB-08 | Query/index failure | Retry or UF-03 reconciliation | No new persistence requirement | Folder names/paths prohibited from telemetry | Readable labels; hierarchy not color-only |
| CB-08 | Folder tracks · UF-04 | Folder selected | Local tracks for selected folder | Select track, return | Query derived index | CB-13 | Empty after reconcile or index error | Refresh; return CB-07; UF-03 if stale | Navigation context may be transient | No media payload telemetry | Clear heading/back |
| CB-09 | Search Idle / Query · UF-04 | Search opened or query edited | Idle or current query | Type, edit, clear, submit, return | Local derived-index query | CB-10/11/12 | Local query/index failure | Retry/edit/clear; Browse remains | Query only within active Search | Query and matches never telemetry | Labeled input/clear; keyboard accessible; unavailable in Driving Mode |
| CB-10 | Search Result · UF-04 | Local query returns matches | Selectable local results | Select, edit/clear, return | Derived index supplies candidates; selection revalidated | CB-13 | Stale result or later start failure | Reconcile and retain Search context | Preserve active query | No query/result/media payload telemetry | Results and rows announced accessibly |
| CB-11 | Search No Result · UF-04 | Local query returns no matches | Explicit No Result with query | Edit, clear, retry, return | No remote fallback or fabricated result | CB-09/10/12 | Subsequent query error | Preserve query and Browse access | Preserve within active Search | No query logging | Textual state; focus not trapped |
| CB-12 | Search Error · UF-04 | Local query cannot complete | Concise error with query/library retained | Retry, edit, clear, return | Typed local failure; verified library remains intact | CB-10/11 | Repeated failure | Browse remains; UF-03 only if index truth is stale | Preserve verified library/query context | Coarse reviewed error only | Error/Retry announced; no color-only meaning |
| CB-13 | Selection Validation · UF-04 | Track chosen from Browse/Search | Bounded pending feedback if needed | Wait; cancel/back if supported | Android access plus MediaStore revalidation | CB-14 | Revoked, stale, moved, deleted or inaccessible | UF-02/UF-03; retain source context | No duplicate live state | No media identifiers in logs/analytics | Pending/error announced; navigation not trapped |
| CB-14 | Playback Start / Queue Acceptance / Preparing · UF-04→UF-05 | Valid selection submitted or queue item requested | Previous verified state plus bounded pending; never false Playing | Wait; back/cancel if supported | Service validates visible collection/order, accepts queue/current item and reports actual state | CB-15 or CB-16 | Start rejected, controller unavailable, invalid item | Preserve prior verified queue/context; retry or choose another | Accepted live queue belongs to service | Coarse player error only; no media payload | Pending/error announced; controls not falsely enabled |
| CB-15 | Full Player Playing · UF-05 | Service reports Playing with valid item | Actual item, queue, duration/position when known | Pause, Next, Previous, Seek, Queue, Driving Mode, Back | MediaSessionService owns player/queue/item/state/position | CB-16/17/18/19/21/22 | Command, controller or media failure | Preserve verified state; reconnect or controlled skip | P0 live service state | No command event; reviewed coarse player error only | 48dp targets, TalkBack labels, scalable text; no color-only state |
| CB-16 | Full Player Paused · UF-05 | Service reports Paused | Actual item, queue, position and Paused | Play, Next, Previous, Seek, Queue, Driving Mode, Back | Controller observes service-owned state | CB-15/17/18/21/22 | Command, controller or media failure | Preserve verified state; reconnect or controlled skip | Retain queue/position in active service | Same privacy boundary as CB-15 | Paused state and actions announced |
| CB-17 | End of Queue / Paused-Completed · UF-05 | Final item completes or Next at end | Final item retained; explicit non-playing end state | Play restarts final item; Previous, Seek, Queue, Back/Browse | Service reports completion; no wrap/repeat | CB-15/16/18/22 | Controller/media failure | Concise feedback; Library path; no invented repeat | Live queue service-owned; P1 only if later lifecycle loss | No personal media telemetry | Textual end state; not color-only |
| CB-18 | Queue View · UF-05 | User opens Queue | Authoritative ordered queue/current item | Select item, Back | Observe service queue; no UI duplicate | CB-14/15/16 | Invalid item or rejected command | Controlled skip/error; retain actual queue | Service-owned live state | Queue contents prohibited from telemetry | Current item/selection semantics exposed; no Driving Mode list |
| CB-19 | Corrupt / Inaccessible Item Recovery · UF-05 | Prepare, advance, selection or completion meets invalid item | Brief non-sensitive skip or terminal error | Continue, Queue, Library | Typed service failure plus MediaStore/index reconciliation | CB-15/16 or CB-17 | No playable item or revoked access | Bounded forward skip; UF-02/03; terminal Paused/Completed | Never persist false playability | Coarse player error only | Concise, announced, non-blocking error |
| CB-20 | Home Mini-player · UF-05 | Existing current item observed outside Full Player | Actual item and Playing/Paused/Completed state | Open Full Player; Play/Pause if exposed | Same MediaSession/controller state | CB-15/16/17 | Reconnect or no valid item | CB-21, hide truthfully, or Library recovery | No duplicate player state | No media metadata telemetry | Labeled player/action; not Driving Mode |
| CB-21 | Controller Connecting / Reconnecting · UF-05↔UF-06 | Entry, UI recreation/return or controller loss | Bounded pending; no fabricated queue/position/state | Wait; Retry if offered; Back | Connect then request complete service snapshot | CB-15/16/17/20/22 | Repeated failure or absent service | Preserve service; if lifecycle absent evaluate CB-32/33; never create player | No UI-owned live state | Coarse connection error only if approved | Status announced; focus not trapped |
| CB-22 | Active Session Handoff · UF-05→UF-06 | Verified state enters background/device/system transition | Last verified actual state | Background, screen off, system control, return | Service/MediaSession remains sole live owner | CB-23/24/26/27/28 | Session absent or controller loss | CB-21 if active; CB-32/33 if lifecycle lost | P0 service-owned | No new lifecycle event approved | State meaning announced; no color-only signal |
| CB-23 | App Background / UI Absent · UF-06 | Activity leaves foreground or UI closes while service active | System surfaces where available; UI absent | Supported system controls; return | MediaSession publishes/accepts actual state | Remain CB-23 or CB-21/26/27/28 | Command/session/surface unavailable | Preserve service state; return/reconnect | P0 live service state | Coarse reviewed error only | Platform accessibility; no driving list/navigation |
| CB-24 | Screen Off / Device Locked · UF-06 | Screen off or device lock while service active | Audible state and available lock/system controls | Supported controls; unlock/return | Service continues Playing or retains Paused/Ended | CB-27/29/21 | OEM restriction or absent surface | Observe actual state; reconnect on return | P0 live service state | No personal media telemetry | Platform semantics; state not inferred visually |
| CB-25 | Task / OEM Lifecycle Event · UF-06 | Task removal or system/OEM lifecycle event | No fabricated promise of survival or stop | Return; use available system surface | Detect whether service/session still exists | CB-23/24/26 if active | Active lifecycle lost | CB-32/33; no auto-play | P0 live or P1 local boundary | Coarse lifecycle error only if approved | Concise truthful recovery |
| CB-26 | Media Notification / System Controls · UF-06 | Platform exposes MediaSession surface | Actual item/state/actions supported by session | Play/Pause/Next/Previous; Seek only if advertised; open app | Same MediaSession is authoritative | CB-29 then synchronized surfaces | Surface stale/unavailable or command rejected | Preserve verified state; refresh/retry | No surface-owned state | Media metadata never enters app telemetry | Platform labels/content descriptions |
| CB-27 | Lock-screen Controls · UF-06 | Device locked and platform exposes controls | Same actual MediaSession state | Supported actions; unlock/open | Same session/controller path | CB-29 or CB-21 on return | Surface absent/stale | Unlock/return and refresh | No duplicate state | Same privacy boundary | Platform focus/order; no long custom interaction |
| CB-28 | Bluetooth / Headset / Vehicle Media Keys · UF-06 | Compatible external media key while route exists | External surface may show actual item/state | Supported media keys only | Command reaches same MediaSession | CB-29 | Unsupported/stale action or route event | Safe ignore/refresh; route/disconnect → UF-11 | No external queue ownership | No device identity or media payload telemetry | Minimal physical interaction; no Android Auto UI |
| CB-29 | System Command Pending · UF-06 | Supported external command received | Last verified state until acknowledgement | Wait; repeat only per platform behavior | Service handles/serializes; exact mechanics remain Issue #16 | CB-15/16/17 and all surfaces refresh | Reject, timeout or session loss | Refresh; safe retry; CB-32/33 only if lifecycle lost | Service remains authority | Coarse error only | Pending/failure not color-only |
| CB-30 | Notification Permission / Visibility Evaluation · UF-06 | First playback value or Settings return where applicable | Contextual explanation only for real non-exempt path | Grant, Not now, explicit Settings | Android platform state is authoritative | Applicable notification behavior or exempt continuity | Denied/still denied | Playback/manual controls continue; optional later recovery | Preference only if needed | Permission decision only; no media payload | Clear purpose; equal Not now; no coercive loop |
| CB-31 | System Surface Unavailable · UF-06 | Platform/OEM omits or loses a surface | No false Stopped/Paused claim | Return to app; use another controller | Service state remains authoritative | CB-21/26/27/28 when available | Continued unavailability | App reconnect; TS-01 evidence | No duplicate state | Coarse availability error only if approved | Explain without relying on absent icon/color |
| CB-32 | P1 Paused Restore · UF-05↔UF-06 | Active lifecycle lost and usable local snapshot validates | Restored queue/item/position explicitly Paused | Resume, inspect Queue, back/discard | Validate snapshot; service becomes live authority only after acceptance | Explicit Resume → CB-14/15; inspect → CB-18 | Partial/stale/invalid snapshot | Reconcile/drop invalid items; CB-33 if none remain | Local P1 representation; details TS-08 | Restore contents/position never telemetry | Clear Paused/Resume; no surprise audio |
| CB-33 | Restore Unavailable · UF-05↔UF-06 | No usable snapshot or playable restored item | Concise no-restorable-playback state | Home / Library | Clear unusable app-owned restore state only | CB-01 then new verified selection | Repeated storage/access failure | UF-02/03/04 as applicable; never delete user media | No false queue | Coarse storage error only if approved | Labeled reachable recovery |

## Cross-state invariants

- CB-14 through CB-31 never replace Playback Service/MediaSession as the live authority.
- A disconnected UI preserves the last verified state until CB-21 refreshes it; it never invents Stopped, Paused, an empty queue, or a second player.
- CB-09 through CB-12 never use an online catalog fallback and never invalidate a verified library merely because Search fails.
- Queue replacement begins only at CB-14 after authoritative acceptance and uses the visible local collection in visible order.
- CB-17 never wraps, repeats, or enables Shuffle; CB-19 uses bounded controlled skip only.
- CB-32 is P1, explicitly Paused, and requires Resume. Ordinary P0 background continuity never enters restore.
- Call, Audio Focus, route change, and disconnect/reconnect policy is handed to UF-11; Driving Mode UI is handed to UF-07.
- No account, backend, cloud sync, catalog, streaming, upload/download, Android Auto, Cast, iOS, or v1.1 scope is introduced.

## Completeness audit

| Check | Result |
|---|---|
| Canonical states assigned | PASS — CB-01 through CB-33, with no gap or duplicate ID |
| UF-04 detailed states normalized | PASS — 14 of 14 |
| UF-05 detailed states normalized | PASS — 10 of 10; shared handoff/reconnect/restore states are not double-counted |
| UF-06 detailed states normalized | PASS — 13 of 13; shared reconnect/restore states are not double-counted |
| Required state fields | PASS — trigger, visible state, actions, system/truth, success, failure, recovery, persistence, privacy/analytics, and accessibility/driving exist for every row |
| Authority separation | PASS — permission, MediaStore, derived index, and MediaSession truth remain distinct |
| P0/P1 separation | PASS |
| Scope boundary | PASS |

## Decision

**PASS — Cluster B now has one formal, normalized, complete shared State Inventory for UF-04, UF-05, and UF-06.**

This decision closes only the shared State Inventory documentation gap. It does not claim Figma parity, executed TS-01/02/06/08 evidence, numeric T_PREVIOUS approval, Issue #16 implementation mechanics, cross-role sign-off, formal Cluster B exit, or Issue #3 completion.
