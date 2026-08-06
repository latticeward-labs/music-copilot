# UF-06 - Background, Screen-off, and System Controls

**Product:** Music Copilot MVP v1.0  
**Priority:** P0 - Beta blocking; P1 restore branch  
**Updated:** 06 August 2026  
**Status:** User Flow specification and traceability - **PASS**; TS-01, TS-02, TS-08, Figma synchronization, and reviewer evidence remain open  
**Tracking:** GitHub Issue #3  
**Canonical diagram:** [`mermaid/UF-06_BACKGROUND_AND_SYSTEM_CONTROLS.md`](mermaid/UF-06_BACKGROUND_AND_SYSTEM_CONTROLS.md)  
**Raw Mermaid source:** [`mermaid/UF-06_BACKGROUND_AND_SYSTEM_CONTROLS.mmd`](mermaid/UF-06_BACKGROUND_AND_SYSTEM_CONTROLS.mmd)

## Authority and boundary

UF-06 defines the implementation-independent contract after UF-05 has an authoritative current item and queue: what the user experiences when the app UI is backgrounded, closed, or recreated; when the screen is off or locked; when Android exposes media notification, lock-screen, Bluetooth/headset, or vehicle head-unit controls; and when the user returns to the app. Every surface observes and commands the same service-owned MediaSession.

The official English PRD/MVP are the product authorities. The current GitHub ADR register is the architecture authority for ADR status: ADR-0001 through ADR-0006 were reviewed and accepted on 06 August 2026. Older attached PDF or dated User Flow snapshots that still label these ADRs Proposed do not override the current repository record.

This flow approves user-visible continuity, synchronization, command routing, permission/recovery branches, and the separation of P0 active continuity from P1 Paused Restore. It does not approve the exact Media3 service class, foreground-service declaration, notification provider/layout, controller API, timeout/serialization policy, task-removal behavior, process-death limits, persistence representation, OEM battery configuration, or UI layout. Those mechanics remain TS-01, TS-02, TS-08, Issue #15, and Issue #16. Calls, Audio Focus, headset/Bluetooth disconnect/reconnect, route changes, and speaker-leak prevention belong to UF-11. Android Auto, Cast, streaming, and remote playback are outside MVP v1.0.

## Ten-field traceability record

| Required field | Controlled value |
|---|---|
| Flow ID | UF-06 |
| User goal | Keep lawful local playback truthful and controllable when the app UI is not visible or the screen is off, use available system controls without creating conflicting state, and return to the same actual playback state. |
| Priority | P0 - Active background/screen-off continuity and synchronized MediaSession surfaces; P1 - queue/position restoration only after the active lifecycle is lost. |
| User Story | **US-03:** music continues with the screen off and over Bluetooth; Media Notification and vehicle controls show the actual state. |
| Functional Requirement | Completes the background/system-control half of **FR-02** with UF-05 at User Flow specification level. Defines the user-visible **FR-08 P1** Paused Restore handoff without promoting it to P0. Interruption/route behavior remains **FR-03/UF-11**. |
| Screens and states | UF-05 Full Player/Home mini-player handoff; UI Background/Closed/Recreated; Screen Off/Locked; Media Notification/System Media Controls; Lock-screen Controls; Bluetooth/Headset/Vehicle Media Keys; System Command Pending/Acknowledged/Rejected; Controller Connecting/Reconnecting; Notification Permission/Visibility branch where applicable; P1 Paused Restore; Restore Unavailable. |
| Influencing ADRs | ADR-0001 Accepted - native Android/system behavior; ADR-0002 Accepted - service/MediaSession owns player, queue, position, commands, and external synchronization; ADR-0003 Accepted - local-only rebuildable/app-owned restore boundaries; ADR-0006 Accepted - no account, backend, sync, catalog, streaming, upload, or download. |
| Failure and recovery | Missing/unavailable system surface does not fabricate playback failure; unsupported/rejected command preserves verified state; UI reconnect refreshes the complete service snapshot; notification denial where applicable is non-blocking and has optional Settings recovery; active lifecycle loss enters P1 validation rather than surprise auto-resume; access/media failure hands off to UF-02/03/05; route/interruption events hand off to UF-11. |
| Data and permissions | Playback service/MediaSession is authoritative for current item, queue, index, Playing/Paused/Ended, duration, position, and supported actions. Android/system surfaces are controllers only. Platform notification state is authoritative where a non-exempt permission applies. P1 restore is local app-owned data only; representation remains open. Personal media metadata and precise position are prohibited from telemetry. |
| Open assumptions or evidence | TS-01 notification/foreground-media behavior on Android 10/12/14+ and OEMs; TS-02 UI close/recreation/task removal/service/process boundaries; TS-08 restore contract; Issue #16 service/controller, command, lifecycle, foreground-service, and persistence mechanics; Issue #15 reconnect/permission/error presentation; UF-11 route/interruption policy; Figma synchronization; Product/Design/Android/QA/Privacy review. ADR status itself is not open. |

## Preconditions and entry routes

UF-06 begins only from verified playback state or an existing active MediaSession; it never invents a session merely because a system surface is opened.

| Entry route | Required behavior |
|---|---|
| UF-05 Playing -> app background/UI closure | The service remains the live owner and playback continues while actual state is Playing. Losing the Activity/controller connection must not stop or duplicate playback. |
| UF-05 Playing -> screen off/device lock | Playback continues and the MediaSession remains the source for available system controls. |
| UF-05 Paused -> app background/screen off | State remains Paused with queue/current position retained by the active service; backgrounding never auto-plays. |
| UI/activity recreation or return to app | Connect/reconnect, obtain a complete authoritative snapshot, then render the real state. Do not restart the track or create another player. |
| System media-control action | Accept only actions advertised/supported by the MediaSession and route them to the same playback boundary. |
| Active lifecycle no longer exists | Evaluate the separate P1 restore branch. Never describe this as ordinary P0 background continuity. |

## P0 active continuity contract

### App background, UI closure, and recreation

1. Leaving Full Player, backgrounding the app, closing/recreating the Activity, or losing the UI controller does not transfer playback ownership to the UI and does not implicitly stop an active service.
2. If the service reports Playing, playback continues; if it reports Paused or Ended, it stays Paused or Ended. The transition itself never toggles playback.
3. A temporary UI-controller disconnect is represented as Connecting/Reconnecting, not as Stopped, Paused, empty Queue, zero position, or an assumed new session.
4. When the UI returns, it refreshes current item, queue, current index, Playing/Paused/Ended, duration, position, and supported actions before controls are treated as synchronized-ready.
5. Back and UI closure never issue Stop unless a later explicit product action is approved. No such stop action is approved in this flow.

### Screen off and device lock

1. Turning the screen off or locking the device does not pause a service-owned Playing session.
2. Available lock-screen/system controls display only MediaSession-derived state and supported actions.
3. Unlocking or foregrounding the app follows the same complete reconnect/refresh contract; it does not restart playback or reset position.
4. Device/OEM restrictions that terminate or constrain the service are observed as actual lifecycle outcomes, not hidden by optimistic UI. Their exact behavior and mitigation require TS-01/TS-02 evidence.

### Task removal and process boundaries

Task removal, Activity destruction, service stop, and process death are distinct events. UF-06 does not promise that every OEM or every task-removal gesture preserves the service, nor does it treat task removal as automatic process death. If the active service/MediaSession still exists, P0 continuity applies. If it no longer exists, the flow enters the P1 validation branch. The precise lifecycle matrix, restart policy, and stop semantics remain TS-02 and Issue #16.

## System-surface synchronization contract

### Single source of truth

- Media Notification/system media controls, lock-screen controls, Bluetooth/headset buttons, compatible vehicle head-unit media keys, Full Player, and mini-player are controller/observation surfaces for one MediaSession.
- No surface maintains an independent Playing/Paused flag, queue, current item, or position.
- A surface may be unavailable, delayed, restricted, or visually different by Android/OEM. Its absence does not prove the player stopped; only the authoritative service state does.
- Exact artwork, metadata truncation, button order, seek-bar availability, and platform visual treatment are not Wireframe or User Flow decisions.

### State publication

When the platform exposes a surface, it receives the actual current item, Playing/Paused/Ended state, position/duration when supported, and only the actions currently advertised by the MediaSession. A new state is shown after service acknowledgement. Stale or optimistic publication is not permitted.

### Command routing

| External command | Controlled behavior | Boundary / recovery |
|---|---|---|
| Play / Pause | Route through the same MediaSession and use UF-05 idempotent semantics. | If the session/current item is unavailable, keep the last verified visible state until the system refreshes; never create another player. |
| Next | Use UF-05 validation, controlled skip, and no-wrap/end-of-queue rules. | Corrupt/inaccessible recovery remains UF-05; route/access events remain UF-11/UF-02/03. |
| Previous | Use the UF-05 symbolic `T_PREVIOUS` branch. | The numeric threshold remains Issue #16; system surfaces do not invent a different rule. |
| Seek, when advertised by the platform | Use UF-05 clamping and acknowledgement semantics. | Do not advertise or simulate Seek when the session/surface does not support it. |
| Unsupported, stale, duplicated, or rejected action | Reject/ignore safely, preserve last verified state, then refresh from the service. | Exact ordering, de-duplication, timeout, and retry mechanics remain Issue #16. |

After an acknowledged command, every connected app/system surface converges on the same actual state. No surface receives a special queue, repeat, shuffle, streaming, Cast, or Android Auto behavior.

## Notification visibility and permission branch

The product sources require a Media Notification/system MediaSession experience, while Android platform behavior differs by API level, target SDK, notification type, device, and OEM. The flow therefore uses an evidence-safe branch:

1. After the first successful playback has demonstrated background value, evaluate whether the current platform/build has any applicable **non-exempt** notification permission path.
2. Media-session notifications that are exempt from Android 13+ notification-permission behavior must not be placed behind a fabricated blocking permission gate.
3. If the app later uses a genuinely non-exempt notification and a runtime permission is applicable, show a short contextual explanation and allow Grant or Not now/Denied.
4. Denial never clears the queue, stops or restarts playback, creates a false Stopped state, blocks manual controls, or loops into Settings.
5. Settings recovery is user-initiated and optional. Returning from Settings rechecks actual platform state and follows Granted, Denied-requestable, or still denied behavior without coercion.
6. The exact Android 10/12/14+ notification/foreground-service visibility, media-session exemption, denial, Task Manager, and OEM behavior remains TS-01 evidence. This specification does not claim those tests have run.

## Controller reconnect and return-to-app contract

1. On UI return/recreation, show a bounded Connecting/Reconnecting state until the controller has a complete snapshot.
2. Preserve the last verified snapshot only as pending context; never present it as newly confirmed or let it become playback authority.
3. On connection, replace pending UI values with current item, queue, index, Playing/Paused/Ended, duration, position, and supported actions from the service.
4. If commands occurred from notification, lock screen, or Bluetooth while the UI was absent, the returned UI shows their actual acknowledged result.
5. Reconnection failure preserves active service playback where it still exists and offers safe Retry/Back. It never constructs a replacement player.
6. If the service no longer exists, stop reconnecting as though it were merely delayed and evaluate P1 restore instead.

## P0 active continuity versus P1 paused restore

| Branch | Priority | User-visible contract |
|---|---:|---|
| Active service/MediaSession survives while UI is absent or recreated | P0 / FR-02 | Actual Playing continues; actual Paused/Ended remains so. System controls and returning UI synchronize to the same session. No restore prompt or second player exists. |
| UI controller temporarily disconnects but service is active | P0 | Show Connecting/Reconnecting, preserve truthful pending context, then refresh the complete actual state. Do not treat this as process death. |
| Active lifecycle is lost and a valid local snapshot exists | P1 / FR-08 | Validate queue/current item/position, reconstruct as **Paused Restore**, and require explicit Resume. Never auto-play. |
| Snapshot is partial or stale | P1 | Revalidate items, reconcile/drop invalid entries, preserve remaining order, identify a valid current item, and stay Paused until explicit Resume. |
| No playable restored item remains or restore fails | P1 | Discard only unusable app-owned restore state, not user media; route to Home / Library and UF-04 with concise recovery. |

Persisted fields, save timing, expiry, migration/error behavior, task-removal/process-death limits, and service reconstruction mechanics remain TS-02/TS-08/Issue #16. The prohibition on surprise auto-resume is approved.

## Detailed state inventory

| State | Trigger / entry | Visible state | Allowed actions | System action / source of truth | Success transition | Failure transition | Recovery | Persistence | Analytics / privacy | Accessibility / driving |
|---|---|---|---|---|---|---|---|---|---|---|
| Active handoff from UF-05 | Verified Playing/Paused/Ended state and UI/system transition | Last verified current item/state; no invented transition | Background, screen off, use available system controls, return | Service/MediaSession remains sole live owner | P0 background/screen-off or synchronized surface | Service/session absent, controller loss | Reconnect if service active; otherwise P1 evaluation | Live service state | No new lifecycle event approved | State meaning announced and not color-only |
| App background / UI absent | Activity not foreground or UI closed while service active | System surfaces where available; app UI absent | System Play/Pause/Next/Previous and advertised Seek | MediaSession publishes actual state and accepts supported commands | Remain synchronized; UI return | Command/session/surface unavailable | Preserve verified service state; retry through available surface/UI | P0 service-owned | Coarse reviewed error only | System accessibility supplied by platform; no driving list/navigation |
| Screen off / device locked | Screen turns off or device locks while service active | Audible actual state; lock/system media controls where exposed | Supported lock/system controls; unlock/return | Service continues actual Playing or retains Paused/Ended | Continued playback or acknowledged command state | OEM/system restriction or surface absent | Observe actual state; reconnect on return; TS-01/02 evidence | P0 service-owned | No personal media telemetry | Actions use platform semantics; state not inferred from visuals alone |
| Task/OEM lifecycle event | Task removal or system/OEM event | No fabricated promise of survival/stop | Return to app; use available system control | Determine whether service/session still exists | P0 continuity if active | Active lifecycle lost | Enter P1 validation; no auto-play | P0 live or P1 local boundary | Coarse lifecycle error only if approved | Concise truthful recovery |
| Media Notification / system controls | Platform exposes MediaSession surface | Actual item/state/actions supported by session | Play/Pause/Next/Previous; Seek only if advertised; open app where supported | MediaSession state is authoritative | Command acknowledged and all surfaces refresh | Surface unavailable, stale, or command rejected | Preserve verified state; refresh/retry without duplicate player | No surface-owned state | Media metadata must not enter app telemetry | Platform labels; app-provided content descriptions where applicable |
| Lock-screen controls | Device locked and platform exposes controls | Same actual MediaSession state | Supported media actions and unlock/open | Same session/controller path | Acknowledged common state | Surface absent/stale | Unlock/return and refresh; no false player error | No duplicate state | Same privacy rule | Platform focus/order; no custom long interaction |
| Bluetooth/headset/vehicle media keys | Compatible external key/controller action while route exists | External surface may show actual item/state | Supported media keys only | Command reaches the same MediaSession | Acknowledged common state | Unsupported/stale/route event | Unsupported action safe; route/disconnect -> UF-11 | No external queue ownership | No device identity/media payload telemetry | Minimal physical interaction; no Android Auto UI |
| System command pending | Supported command received | Last verified state until acknowledgement | Wait; repeat only according to platform behavior | Service serializes/handles command; exact mechanics open | All surfaces converge on acknowledged state | Rejected/timed out/session lost | Refresh state; safe retry; P1 only if lifecycle lost | Service remains authority | Coarse error only | Pending/failure not conveyed by color alone |
| Controller Connecting / Reconnecting | UI return, recreation, or controller loss | Bounded pending state; no fabricated queue/position | Retry if offered; Back | Connect and request complete snapshot | Full Player/mini-player actual state | Repeated failure or absent service | Preserve service; do not create player; evaluate P1 if absent | No UI-owned live state | Coarse connection error only | Announced status, focus not trapped |
| Notification permission/visibility evaluation | First playback value shown or Settings return, where applicable | Contextual explanation only for a real non-exempt path | Grant, Not now, Settings by explicit choice | Android platform state is authoritative | Applicable notification behavior available | Denied/still denied/exempt path | Continue playback/manual controls; optional later recovery | Preference only if needed | Permission decision only; no media payload | Clear purpose, equal Not now, no coercive loop |
| System surface unavailable | Platform/OEM does not expose or temporarily loses a surface | No false Stopped/Paused claim | Return to app; another available controller | Service state remains authoritative | Surface returns and refreshes | Continued unavailability | App controller reconnect; TS-01 evidence | No duplicate state | Coarse availability error only if approved | Explain without relying on absent icon/color |
| P1 Paused Restore | Active lifecycle lost and usable snapshot validates | Restored queue/item/position explicitly Paused | Resume, inspect Queue, back/discard | Validate local snapshot; service becomes live owner after acceptance | Explicit Resume -> active P0 session | Partial/stale/invalid snapshot | Reconcile/drop invalid entries; Home/Library if none | Local P1 representation; details TS-08 | Restore contents/position never telemetry | Clear Paused and Resume semantics; no surprise audio |
| Restore unavailable | No usable/playable snapshot | Concise no-restorable-playback state | Home / Library, UF-04 selection | Clear unusable app-owned restore state only | New verified selection | Repeated storage/access failure | UF-02/03/04 as applicable; never delete user media | No false queue | Coarse storage error only if approved | Recovery action labeled and reachable |

## Invariants

- Playback Service/MediaSession is the only live owner of player, current item, queue, current index, Playing/Paused/Ended, duration, position, and supported commands.
- App UI, notification, lock screen, Bluetooth/headset, vehicle media keys, Room, DataStore, and restored snapshots never become independent playback authority.
- Backgrounding, UI closure/recreation, and screen off never toggle actual playback by themselves.
- Task removal, service stop, and process death are not treated as the same event; actual service existence decides P0 versus P1 routing.
- Every surface publishes and acts on acknowledged service state; no optimistic Playing/Paused, queue, current item, or position is fabricated.
- Missing notification/lock-screen/Bluetooth UI does not prove playback stopped; unavailable surfaces recover without creating a second player.
- External commands preserve UF-05 semantics for end of queue, corrupt/inaccessible items, Previous, Seek, no wrap, and no Repeat/Shuffle invention.
- Notification permission is never requested as a fake requirement for exempt media-session behavior; any real non-exempt request is contextual, deferrable, and non-blocking.
- P0 active continuity and P1 Paused Restore remain distinct; P1 always requires explicit Resume and never surprises the user with audio.
- Calls, Audio Focus, Bluetooth/headset disconnect/reconnect, route change, and speaker-leak prevention are handed to UF-11 and are not silently decided here.
- Android Auto, Cast, account, backend, sync, catalog, streaming, upload/download, iOS, advanced playlists, EQ, crossfade, DSP, and v1.1 scope are excluded.
- No filename, title, artist, album, folder, URI, queue contents, precise position, external-device identity, or other personal media metadata enters telemetry.

## Open decisions and evidence

| Gate | Owner | Required evidence / decision | Effect on UF-06 status |
|---|---|---|---|
| TS-01 | Android + QA | Android 10/12/14+ and OEM matrix for media-session notification visibility, applicable permission/exemption behavior, foreground-media state, denial, Settings return, and Task Manager/system surfaces | Flow specification passes; executed platform evidence remains open |
| TS-02 | Android + QA | Lifecycle matrix for Activity recreation, UI closure, screen off, task removal, controller disconnect/reconnect, service lifetime, OEM restrictions, and process death | P0/P1 routing passes; executed lifecycle proof remains open |
| TS-08 | Android | Minimum local queue/current-item/position representation, validation, save timing, migration/error behavior, and paused-restore proof | P1 user-visible branch passes; persistence contract/evidence remains open |
| Issue #15 | Product Design | Connecting/Reconnecting, permission explanation, unavailable system surface, P1 restore, error presentation, return-to-app, accessibility, and link-opening behavior | Blocks Wireframe approval, not UF-06 traceability |
| Issue #16 | Product + Android | Exact Media3 service/controller and foreground-service design, command ordering/acknowledgement, notification integration, lifecycle/stop policy, process-death limits, and persistence mechanics | Blocks final architecture/implementation approval |
| UF-11 | Product + Android + QA | Calls, Audio Focus, Bluetooth/headset disconnect/reconnect, Audio Route changes, conditional resume, and no-speaker-leak policy | Blocks FR-03 and route-safety completion, not UF-06 background continuity |
| Figma synchronization | Product Design | Repository behavior copied into the controlled User Flows / State Inventory workfile | Visual parity and formal Cluster B review remain open |
| Role review | Product, Design, Android, QA, Privacy | Review against PRD/MVP, accepted ADRs, Android evidence, recovery, privacy, accessibility, and P0/P1 separation | Final Cluster B/Issue #3 approval remains open |

## Exit checklist and decision

- [x] One ten-field UF-06 traceability record exists.
- [x] UF-05 Playing/Paused, app background/UI closure, screen off/lock, UI recreation/return, system command, and active-lifecycle-loss entry routes are explicit.
- [x] Notification/system media controls, lock-screen controls, Bluetooth/headset, and compatible vehicle media keys observe and command one MediaSession.
- [x] System Play/Pause/Next/Previous and advertised Seek preserve UF-05 acknowledgement, queue, end, Previous, and recovery semantics.
- [x] Missing/unavailable/stale system surfaces and unsupported/rejected commands have truthful, non-duplicating recovery.
- [x] UI controller reconnect refreshes the complete service snapshot and never fabricates stop/pause/empty/zero state.
- [x] Android notification permission/exemption is represented as an evidence-safe, non-blocking, non-coercive branch.
- [x] Task removal, service lifetime, and process death remain distinct and route through actual service existence.
- [x] P0 active continuity is separated from P1 Paused Restore with explicit Resume.
- [x] UF-11 interruption/route boundary and UF-02/03/05 access/media recovery handoffs are explicit.
- [x] Markdown and raw Mermaid logic are synchronized.
- [x] Current GitHub Accepted ADR status is used.
- [x] Privacy, analytics, accessibility, Driving Mode, Android Auto, and outside-MVP boundaries are explicit.
- [x] FR-02 is complete at User Flow specification level across UF-05 and UF-06.
- [x] UF-04, UF-05, and UF-06 are each complete at specification/traceability level; Cluster B drafting coverage is complete.
- [ ] Figma synchronization and connected Cluster B review/exit evidence recorded.
- [ ] TS-01, TS-02, and TS-08 executed evidence recorded.
- [ ] Final service/controller/foreground-service/lifecycle/persistence mechanics approved in Issue #16.
- [ ] Product/Design/Android/QA/Privacy review recorded.
- [ ] Issue #3 complete flow-set acceptance criteria satisfied.

**Decision:** UF-06 is complete and passes at the User Flow specification and traceability level. FR-02 and the three constituent Cluster B flows are complete at that documentation level. This decision does not claim executed Android/OEM evidence, Figma parity, formal Cluster B sign-off, FR-03 route/interruption completion, Wireframes, final Software Architecture, implementation, or closure of Issue #3.
