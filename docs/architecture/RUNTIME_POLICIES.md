# Issue #16 Phase 7 — Runtime Policies

- **Status:** **PHASE 7 NORMATIVE RUNTIME POLICY**
- **Date:** 30 August 2026
- **Tracking:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- **Branch:** `docs/16-software-architecture`
- **Attached English source:** `MSM2(2).rar`, SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- **Controlling decisions:** [ADR-0007](../adr/0007-same-process-playback-runtime-and-safe-paused-restore.md), [ADR-0008](../adr/0008-minimal-modular-monolith-and-dependency-enforcement.md)
- **Implementation contracts:** [Phase 6 Implementation Structure and Public Contracts](IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md)
- **Architecture views:** [Phase 5 Architecture View Audit](PHASE5_ARCHITECTURE_VIEW_AUDIT.md)
- **Behavior/recovery evidence:** approved [Wireframes](../WIREFRAMES.md), [Navigation/Behavior Audit](../wireframes/PHASE_7_NAVIGATION_BEHAVIOR_AUDIT.md), [Recovery Terminal Matrix](../wireframes/recovery-system/RECOVERY_TERMINAL_MATRIX.md)
- **Privacy evidence:** [Analytics and Privacy](../ANALYTICS_AND_PRIVACY.md)

## 1. Source and interpretation rule

This policy is a runtime consolidation of decisions already established by the attached English `MSM2(2).rar` source family, Accepted ADR-0001..0008, approved User Flow/Wireframe evidence, Phase 5 architecture views and Phase 6 implementation contracts. It does not silently change an Accepted ADR.

The attached English source fixes the architecture direction: service-owned Media3 playback, UI as observer rather than playback authority, local-first MediaStore/Room/DataStore boundaries, replaceable Voice provider, deterministic bounded intent mapping, privacy-safe non-authoritative telemetry, and P0 background/interruption stability. ADR-0007/0008 and the current Issue #16 evidence finalize the open runtime details.

**No Persian/Farsi PRD or MVP is an authority for Phase 7.**

If a later implementation needs to change process topology, playback authority, persistence authority, command concurrency, Voice ownership or another Accepted ADR decision, it requires architecture/ADR review rather than an implementation-local exception.

---

## 2. Non-negotiable runtime invariants

1. **Playback, live Queue and Position have one source of truth:** the service-owned Player/MediaSession state inside the Playback boundary.
2. **UI is Observer + Command sender only.** UI/ViewModels may project state; they may not create a second Player, Queue or optimistic playback truth.
3. **All playback mutation converges on one serialized authority.** UI, Voice and Android/system media controls ultimately mutate the same service-owned Player/MediaSession.
4. **UI lifecycle is not playback lifecycle.** Recreation, backgrounding, task UI closure and screen-off do not transfer playback ownership to UI.
5. **P0 active playback continuity and P1 paused cold restoration remain separate requirements.** Persisted restore never upgrades itself into P0 and never autoplays.
6. **Voice session state and pending confirmation have one owner:** `VoiceSessionCoordinator`.
7. **MediaStore, Room and DataStore have distinct ownership.** None becomes a substitute for live MediaSession state.
8. **Quality/telemetry is non-authoritative.** Analytics, crash/config adapter or privacy-sink failure cannot change product/safety outcome.
9. **Typed failures replace exception leakage.** Public architecture surfaces use `AppResult` / `AppError` and explicit `RecoveryAction`.
10. **Privacy filtering happens before optional external telemetry.** Prohibited Voice/media/identity content never enters telemetry, crash or configuration payloads.

---

## 3. Playback authority and lifecycle

### 3.1 Sole authority

The authoritative live state is owned by `MediaSessionService` and its service-owned Player/MediaSession:

- current queue and order;
- current item/index;
- current position;
- Playing/Paused/Buffering/Ended/Error state;
- skip/seek capability;
- authoritative command acknowledgement;
- interruption pause provenance needed for guarded resume.

`PlaybackSnapshot` is an observable projection of this authority. It is not a second source of truth.

Forbidden competing authorities:

- Compose state as live playback truth;
- ViewModel queue copies that can mutate independently;
- Room active queue tables;
- DataStore live playback truth;
- Voice provider state deciding that playback succeeded;
- notification/lock-screen/Bluetooth surface state treated as authoritative independently of MediaSession.

### 3.2 UI observer/command-sender contract

UI and destination ViewModels may:

- observe `PlaybackGateway.connectionState`;
- observe the complete authoritative `PlaybackSnapshot` projection;
- derive presentation-only labels/control enabled state;
- submit `PlaybackCommand` through Application/Domain or the public `PlaybackGateway`;
- render a neutral pending affordance while awaiting acknowledgement.

UI and ViewModels must not:

- instantiate ExoPlayer/MediaSession;
- mutate a local queue and later try to reconcile it with the service;
- switch to Playing before authoritative acknowledgement;
- infer Stop/Paused from a temporarily unavailable system surface;
- persist live queue/position as an alternate authority.

### 3.3 UI recreation and controller reconnect

The production `PlaybackControllerConnection` is application-scoped and therefore outlives an individual destination/ViewModel.

Reconnect sequence is fixed:

1. connection enters `Disconnected` or `Connecting`;
2. the new UI does not create a Player or rebuild playback truth from saved UI state;
3. controller reconnects to the existing service-owned MediaSession when available;
4. a **complete** `PlaybackSnapshot` is obtained;
5. the locally observed snapshot is atomically replaced;
6. only after replacement may connection state become `Ready`;
7. screens render the authoritative snapshot.

Partial field-by-field restore before `Ready` is forbidden because it can expose impossible mixed revisions.

If reconnect fails, expose typed `AppError.Controller` / `RecoverableError`; retain only the last confirmed snapshot as presentation context and do not fabricate successful commands.

### 3.4 UI closure, task removal and screen-off

UI closure and screen-off do not stop legitimate active playback.

- When playback is legitimately active, the service-owned MediaSession/Player remains the authority and Android media surfaces continue observing/controlling that session.
- Notification, lock-screen and supported external media controls converge on the same MediaSession.
- Closing/recreating a screen must not recreate the Player.
- On task removal, the service may remain only while playback is legitimately ongoing; otherwise it may stop according to ADR-0007.
- Explicit Stop, deliberately cleared queue or terminal unrecoverable playback state may stop the service.
- Player and MediaSession resources are released by service lifecycle, never by a screen disappearing.

Screen-off is therefore a presentation change, not a playback-state transition by itself.

### 3.5 Process death, force-stop and restoration boundary

P0 guarantees the approved active playback/background/UI-recreation behavior within supported Android service lifecycle behavior. P0 does **not** promise guaranteed survival after application process death or force-stop.

Cold-start rule:

1. if a live MediaSession is available, it wins;
2. otherwise P1 may read the versioned `RestorableQueueSnapshot`;
3. reject corrupt or unsupported future schema;
4. re-query MediaStore using stable references;
5. drop missing/invalid items;
6. clamp position to verified duration;
7. prepare the valid queue **Paused only**;
8. never autoplay because persisted state exists.

Force-stop remains a negative control, not a P0 restoration requirement.

### 3.6 P0 versus P1 separation

| Concern | Priority | Runtime rule |
|---|---|---|
| Active playback with UI recreated/closed/backgrounded/screen-off | **P0** | Service/MediaSession remains authority; controller/UI reconnect and observe |
| Android notification/lock/external controls while session exists | **P0** | Same MediaSession authority |
| Call/focus/route safety while active | **P0** | Service/interruption policy produces safe synchronized state |
| Cold queue/position recovery after process loss | **P1 FR-08** | Versioned snapshot, MediaStore validation, **Paused-only**, no autoplay |
| Guaranteed force-stop/process-death playback survival | **Not P0** | No guarantee |

No implementation may use the P1 snapshot to claim stronger P0 continuity.

---

## 4. Playback command concurrency

All playback mutation converges on the service-owned `PlaybackCommandProcessor`.

- one sequential consumer;
- bounded capacity: **32 pending commands**;
- commands carry `CommandId`;
- accepted commands are never silently dropped;
- overflow returns typed `PLAYBACK_BUSY`;
- Player/MediaSession mutation occurs on the application/player looper using Main/Main.immediate as appropriate;
- parallel Player mutation from UI, Voice, repository or another service helper is forbidden;
- success is authoritative snapshot change/acknowledgement, not enqueue success;
- rejected/timed-out command leaves observers on the last confirmed state until a newer authoritative snapshot exists.

Android external MediaController commands are subject to the same ultimate Player/MediaSession authority and ordering semantics even when they do not originate through the Kotlin `PlaybackGateway` interface.

---

## 5. Coroutine scope ownership

| Work | Owner/scope | Cancellation boundary | Must not be owned by |
|---|---|---|---|
| Destination UI/presentation work | destination ViewModel `viewModelScope` | destination/ViewModel destruction | service/global scope |
| Root navigation state collection | root/app navigation state holder | app process/root owner | individual destination when state must span destinations |
| Playback command processing / Player callbacks | `MediaSessionService` `SupervisorJob` + Main/Main.immediate | service destruction | UI/ViewModel |
| Controller connection/reconnect | application-scoped connection owner | app process / explicit connection teardown | individual screen |
| Active Voice session | one cancellable child job owned by application-scoped `VoiceSessionCoordinator` | replacement session, Back, Driving Exit, permission/interruption/route invalidation | UI screen coroutine or unmanaged global scope |
| MediaStore/Room/DataStore repository work that must outlive a screen | application/repository-owned restartable scope | repository/app process; restartable after process death | transient ViewModel when completion is required beyond the screen |
| One-shot screen-specific repository request | caller structured child scope | caller cancellation | `GlobalScope` |
| Quality/telemetry dispatch | application-scoped best-effort Quality boundary | event drop/adapter shutdown must not affect product | playback/service critical path |

`GlobalScope` and hidden service-locator-owned coroutine scopes are forbidden.

Structured cancellation must propagate to owned provider/storage work where cancellation is supported.

---

## 6. Dispatcher boundaries

### Main / Main.immediate

Use Main/application-player looper for:

- Compose/ViewModel state publication that requires Main;
- Player and MediaSession mutation;
- final publication of authoritative playback/controller state;
- lightweight Android callback coordination that must remain on Main.

Forbidden on Main:

- MediaStore scans/cursor traversal;
- Room query/migration work that can block;
- DataStore blocking adapters;
- CPU-heavy normalization/matching;
- blocking provider/network/storage calls.

### IO

Use injected IO dispatcher for:

- MediaStore queries/re-query;
- Room/database operations;
- DataStore reads/writes where dispatcher switching is required by the adapter;
- filesystem/provider/storage blocking adapters;
- other blocking I/O.

### Default

Use injected Default dispatcher for:

- metadata normalization;
- deterministic fuzzy/candidate matching;
- CPU-bound pure transformations;
- non-I/O index preparation that is materially CPU-bound.

Dispatchers are injected/testable. Hard-coded dispatcher use inside pure domain policy should be avoided.

---

## 7. Cancellation, timeout and stale-result suppression

### 7.1 Voice cancellation

`VoiceSessionCoordinator` owns exactly one active generation token and pending confirmation.

The current Voice generation is cancelled by:

- a replacement Voice session;
- Back while Voice overlay is active;
- Driving Exit;
- permission loss;
- call/focus invalidation;
- audio-route invalidation/disconnect;
- invalidated Player/Driving origin.

Cancellation stops/cancels owned STT, TTS, timeout and ducking/session work where applicable. Callback/result with an old generation token is ignored and cannot mutate Voice UI or playback.

### 7.2 Voice timeout/retry

- listening timeout: **6 seconds**;
- attempt 1 may expose one explicit Retry;
- maximum attempts per session: **2**;
- second timeout/failure ends that session and returns to manual controls/approved fallback;
- new attempt/session requires the approved user action;
- timeout or provider error never guesses a media candidate.

### 7.3 Confirmation staleness

A pending Voice confirmation is valid only for the current session generation and current candidate token. Replacement session, cancellation or invalidating event expires it. Confirmation must not survive recreation/process death as authoritative product state.

### 7.4 Playback/controller stale work

- a command acknowledgement must match the current command/session context and current service authority;
- reconnect replaces the complete snapshot before `Ready`;
- duplicate/stale interruption and route events are handled idempotently;
- route generation changes invalidate an earlier auto-resume eligibility;
- controller timeout/failure is typed; the exact adapter timeout duration is an implementation/test parameter unless separately approved, but the failure outcome is fixed: **no fabricated success and no alternate playback authority**.

Only source-fixed/user-visible timeout values are architecture constants here; Phase 7 does not invent an unsupported numeric controller timeout.

---

## 8. Interruption, focus and audio-route policy

Automatic resume after a transient interruption is allowed only when all four guards are true:

1. playback was Playing before the interruption;
2. Audio Focus returned;
3. route generation is unchanged and valid;
4. no manual Pause occurred after interruption began.

Otherwise remain **Safe Paused**.

Additional fixed rules:

- permanent focus loss -> Safe Paused;
- wired/Bluetooth route disconnect -> immediate Pause, cancel Voice, increment/invalidate route generation;
- route reconnect -> remain Paused until explicit Resume;
- manual Pause always wins over an old auto-resume candidate;
- surfaces synchronize from the resulting service snapshot;
- no event may produce unintended speaker playback as a recovery shortcut.

---

## 9. Persistence ownership

| Data/state | Authority | Persistence | Runtime rule |
|---|---|---|---|
| Device media existence/identity | **MediaStore / Android platform** | platform-owned | always re-check when permission/source truth changes |
| Derived library/search index | **Room repository projection** | local DB, rebuildable | may rebuild; never playback queue truth |
| Optional released Likes/history | Room app-owned records | local DB | require explicit tested migrations once released |
| Settings/onboarding/Bluetooth preference/consent | **DataStore repository** | local | values are preferences/control state, not Android capability truth |
| Android permission/provider availability | Android/provider | not cached as truth in DataStore | fresh capability re-check |
| Live playback/queue/position | **MediaSessionService** | service/session runtime state | never Room/DataStore authority |
| P1 queue/position restore snapshot | Proto DataStore via `RestorableQueueStore` | versioned, non-authoritative | validate against MediaStore; Paused-only |
| Voice transcript/audio/pending confirmation | `VoiceSessionCoordinator` only while active | **not persisted** | ephemeral and cancellable |
| Quality event dispatch state | Quality boundary | optional minimal diagnostics only if approved | never product authority |

### 9.1 P1 restore checkpoint policy

The approved ADR-0007 checkpoint contract remains:

- atomic versioned writes when queue/current item/pause state changes;
- debounced checkpoint of **5 seconds** while active where required by the approved restore implementation;
- no title, artist, path or unnecessary personal media metadata in the restoration payload.

### 9.2 Permission revocation

Permission/capability revocation triggers fresh platform truth and library reconciliation. Stale Room projection may be invalidated/removed/rebuilt. UI must present denied/empty/recoverable truth rather than a cached Ready state.

If currently playing media subsequently becomes unreadable, Playback handles the resulting access failure through typed MediaItem/Playback recovery; the UI does not decide that playback succeeded or stopped based on cached permission/index state.

---

## 10. Voice state ownership

`VoiceSessionCoordinator` is the sole owner of:

- active Voice session ID/generation;
- current attempt (1 or 2);
- Listening/Processing/Confirming/Executed/Failed state;
- pending confirmation token;
- retry availability;
- cancellation reason;
- STT/TTS child operation lifetime.

UI renders `VoiceSessionState`; it does not own a duplicate mutable Voice state machine.

The deterministic mapper/candidate resolver remains bounded to the approved six executable MVP intents. Ambiguous result gets at most one bounded confirmation with at most two candidates. Not-found/unsupported/invalid confidence never executes a guessed command.

Playback mutation from Voice occurs only through `PlaybackGateway`; success Voice/TTS state follows authoritative playback acknowledgement.

Raw audio, transcript and pending confirmation are not persisted.

---

## 11. Navigation destinations, Back and Voice Overlay

### 11.1 Destinations

The single-activity Compose `NavHost` owns the application destination graph:

- Bootstrap;
- Onboarding;
- Permission recovery;
- Home / Library;
- Search;
- Full Player;
- Driving Mode;
- Settings;
- optional/removable P2 Liked Songs.

Android permission dialogs, Android Settings, notification, lock-screen and external media controls are system-owned surfaces/handoffs, not app destinations.

### 11.2 Back behavior

| Current surface/state | Back / Exit result |
|---|---|
| Voice overlay active on Player/Driving | cancel Voice first, dismiss overlay, remain/return to recorded origin |
| Driving Mode | Back equals immediate Exit; return to recorded origin without changing service-owned playback truth |
| Search/Settings/Player ordinary destination | return through approved NavHost history/origin; do not fabricate service state |
| Android Settings/permission handoff return | app rechecks current capability/permission truth |
| Bootstrap/startup recovery | follow approved recovery/navigation contract; never mark an incomplete step successful |

### 11.3 Voice is Overlay/State, not navigation branch

Voice is rendered on top of its recorded Player or Driving origin and is coordinated by `VoiceSessionCoordinator`.

It must not become:

- an independent deep navigation graph;
- a long-lived standalone destination with its own playback copy;
- a separate history stack that survives origin invalidation;
- a route that bypasses Driving restrictions.

Driving remains one interaction layer with immediate Exit, no typing/deep menu/long results.

---

## 12. Typed failure and recovery policy

All public runtime failures use `AppResult.Failure(AppError)` with stable non-sensitive codes and an approved `RecoveryAction`.

| Category | Representative stable codes | Recovery/runtime outcome |
|---|---|---|
| `Permission` | `PERMISSION_DENIED`, `PERMISSION_PERMANENTLY_DENIED`, `PERMISSION_REVOKED` | Retry where requestable; explicit OpenSettings when required; fresh re-check on return; no cached grant truth |
| `Library` | `LIBRARY_SCAN_FAILED`, `LIBRARY_INDEX_CORRUPT`, `LIBRARY_RECONCILE_FAILED` | Retry or Rebuild derived projection; never modify user-owned media |
| `MediaItem` | `MEDIA_ITEM_MISSING`, `MEDIA_ITEM_CORRUPT`, `MEDIA_ITEM_UNPLAYABLE` | controlled Skip; continue with next valid queue item; if none, queue end/Safe Paused truthful UI |
| `Playback` | `PLAYBACK_BUSY`, `PLAYBACK_REJECTED`, `PLAYBACK_FAILED` | Retry/ManualResume as applicable; preserve last confirmed state; no optimistic success |
| `Controller` | `CONTROLLER_DISCONNECTED`, `CONTROLLER_CONNECT_FAILED`, `CONTROLLER_TIMEOUT` | reconnect/Retry; atomically replace complete snapshot before Ready |
| `Voice` | `VOICE_UNAVAILABLE`, `VOICE_TIMEOUT`, `VOICE_NO_MATCH`, `VOICE_SECOND_ATTEMPT_FAILED`, `VOICE_STALE_SESSION` | one Retry only after first eligible failure; then ManualFallback; stale result ignored |
| `Interruption` | `FOCUS_NOT_RECOVERED`, `ROUTE_INVALIDATED`, `CALL_OR_ROUTE_SAFE_PAUSED` | Safe Paused / ManualResume unless all guarded auto-resume conditions pass |
| `Storage` | `STORAGE_READ_FAILED`, `STORAGE_WRITE_FAILED`, `SNAPSHOT_CORRUPT`, `SNAPSHOT_FUTURE_SCHEMA` | Retry when safe; invalid restore snapshot is ignored/discarded; never autoplay or override live session |
| `Privacy` | `CONSENT_REQUIRED`, `CLEAR_PARTIAL`, `RETENTION_NOT_APPROVED` | required control/retry/None; remote processing remains disabled until gate satisfied |
| `Quality` | `QUALITY_DROPPED`, `QUALITY_ADAPTER_UNAVAILABLE` | **None for product behavior**; drop/defer diagnostics and continue product/safety path |

Raw infrastructure `Throwable`, exception messages, paths, media titles, transcripts or provider payloads must not cross the public architecture boundary.

Recovery terminals remain consistent with approved Wireframes: manual controls or service-owned Safe Paused for failure paths where playback safety is involved.

---

## 13. Privacy and telemetry policy

### 13.1 Mandatory event allowlist

Only the approved typed/coarse event families may cross the telemetry privacy boundary:

| Event | Allowed properties |
|---|---|
| `driving_mode_started` | `entry_source`, `bt_connected` |
| `voice_button_pressed` | ephemeral/coarse session context where approved, `playback_state` |
| `intent_detected` | `intent_type`, `confidence_bucket` |
| `intent_failed` | `intent_type`, typed non-personal `failure_code`, `confidence_bucket`, `latency_bucket` |
| `command_executed` | `intent_type`, `latency_bucket` |
| `manual_correction` | `previous_intent`, `correction_type` |
| `player_error` | typed non-personal `error_code`, coarse `route_type` |
| `session_completed` | `duration_bucket`, `commands_count` |

Properties use documented enums/buckets/stable codes. Implementation may not add arbitrary properties or unknown event families without Privacy review.

The Phase 6 public Quality port remains typed: `TelemetryEvent -> TelemetryPrivacyGate -> SanitizedTelemetryEvent -> TelemetrySink`.

### 13.2 Prohibited telemetry/crash/log fields

The following are forbidden from analytics, crash reports, logs that leave the protected local diagnostic boundary, remote config payloads/request attributes and repository evidence:

- raw audio, waveform or Voice recordings;
- full or partial transcript;
- artist, album, folder or personal media title;
- filesystem path;
- content URI;
- MediaStore ID/reference;
- Search query or results;
- library contents or full library metadata;
- queue contents;
- precise playback position;
- precise location;
- contacts;
- call identity;
- Bluetooth device identity;
- vehicle identity;
- provider credentials, account identifiers, access tokens, secrets;
- free-form diagnostic maps/JSON capable of containing prohibited values.

This specifically enforces the Phase 7 requirement that **raw audio, transcript, title and library content never enter telemetry**.

### 13.3 Cloud speech boundary

Remote/cloud STT remains disabled until the separate approved disclosure/consent/provider/destination/purpose/retention gate exists. Speech-provider data flow is separate from telemetry; enabling remote speech never authorizes Voice content in analytics.

---

## 14. Analytics, crash and Remote Config failure isolation

Quality/Diagnostics is a non-authoritative boundary.

### 14.1 Analytics/crash adapter

- `TelemetrySink.tryEmit` is best-effort and must not throw into product behavior;
- unknown/invalid/prohibited event is rejected/dropped;
- sink unavailable -> `QUALITY_ADAPTER_UNAVAILABLE`/drop semantics, product continues;
- no playback command waits for telemetry;
- no Voice cancellation waits for telemetry;
- no permission/interruption/route recovery waits for telemetry;
- telemetry cannot decide Playing/Paused, queue or route state.

### 14.2 Optional Remote Config

If a later approved optional configuration adapter is present in MVP infrastructure:

- it is behind the Quality/config boundary;
- P0 product/safety behavior uses compiled/local approved defaults when configuration is unavailable, malformed, late or rejected;
- Remote Config must not own Player/MediaSession, Voice session, permission truth, route safety, persistence authority or recovery state;
- Remote Config failure cannot stop/pause playback, suppress required recovery, or block app startup into manual/local P0 functionality;
- remote values may not silently widen the six-intent grammar, bypass confirmation, enable cloud speech without consent, or change an Accepted ADR runtime invariant;
- any remotely tunable threshold remains bounded by the separately approved validation range/policy.

Therefore **Analytics or Remote Config failure can never stop Playback** or replace the service-owned runtime authority.

---

## 15. Runtime state transition rules

### 15.1 Playback command path

`UI / system / Voice -> public command boundary -> PlaybackCommandProcessor -> MediaSessionService -> Player/MediaSession -> authoritative PlaybackSnapshot -> all observer surfaces`

No bypass is allowed.

### 15.2 Voice path

`Push-to-talk -> capability/permission check -> VoiceSessionCoordinator -> STT -> deterministic mapper/candidate policy -> direct OR one confirmation -> PlaybackGateway -> authoritative acknowledgement -> short TTS -> overlay state`

Timeout/not-found/provider failure -> one Retry when eligible -> manual fallback; no guessing.

### 15.3 Interruption path

`Call/Focus/Route event -> Interruption policy -> guards -> conditional resume OR Safe Paused -> authoritative snapshot -> UI/notification/lock/external synchronization`

Route reconnect alone is never sufficient for auto-resume.

### 15.4 Permission revocation path

`fresh Android permission truth -> cancel/stop affected scan/provider work -> MediaStore reconciliation -> stale Room projection invalidation -> typed state -> truthful UI recovery`

### 15.5 Corrupt media path

`Player error -> typed MediaItem/Playback failure -> bounded controlled skip -> next valid item OR Safe Paused/queue end -> synchronized snapshot`

### 15.6 Quality path

`typed TelemetryEvent -> TelemetryPrivacyGate -> sanitized event -> optional sink`

Any failure in this path terminates locally in the Quality boundary and does not propagate as a product failure.

---

## 16. Runtime implementation checks required in Issue #4

The Android foundation must make these policies executable/testable:

- only the Playback service package constructs Player/MediaSession;
- UI recreation does not create a second Player;
- controller reconnect publishes complete snapshot before Ready;
- active screen-off/background playback remains synchronized with system surfaces;
- command processor proves sequential ordering, capacity 32 and typed Busy overflow;
- no `GlobalScope`;
- dispatcher tests prove blocking MediaStore/Room/DataStore work is not on Main;
- Voice has one generation, six-second timeout, one retry and stale-result suppression;
- route disconnect cancels Voice and Safe-Pauses; reconnect does not auto-resume;
- P1 snapshot restore is schema-validated, MediaStore-revalidated, Paused-only and never overrides a live session;
- permission revocation removes stale Ready/index truth;
- corrupt-file recovery continues queue only through controlled service authority;
- typed failures map to documented RecoveryAction;
- telemetry schema rejects unknown/free-form/prohibited fields;
- analytics/config adapter failure leaves playback and all P0 safety/recovery behavior unchanged.

Physical OEM/vehicle/call/audio-route evidence remains a downstream validation gate; Phase 7 defines the policy and test seam, not the field-test result.

---

## 17. Phase 7 gate

Phase 7 is complete only when all six runtime policy families are implementation-ready:

| Policy family | Required result |
|---|---|
| Lifecycle | service authority, UI recreation/closure/screen-off/process-death boundary explicit |
| Concurrency | scopes, dispatchers, serialization, cancellation, timeout, stale suppression explicit |
| Persistence | MediaStore/Room/DataStore/live session/P1 snapshot ownership explicit |
| Navigation | destination graph, Back/Exit and Voice Overlay ownership explicit |
| Error | typed error families, stable representative codes and recovery actions explicit |
| Privacy | allowlist, prohibited fields, cloud-speech gate and Quality failure isolation explicit |

**Normative Phase 7 result:** no implementation team member is authorized to invent a competing playback/Voice/data authority or a new runtime recovery family inside Issue #4. A material deviation must return to Issue #16 architecture governance / ADR control.
