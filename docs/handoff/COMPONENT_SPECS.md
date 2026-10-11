# COMPONENT_SPECS — 23 Jetpack Compose Components

**Canonical source:** `docs/design-system/components/COMPONENT_TOKENS.json`. All values below are copied from its actual variants, states and geometry; local component styling is subordinate to `COMPOSE_TOKEN_MAPPING.md`. These are implementation contracts, NOT a second domain owner.

## Global minimums

Touch area ≥48×48dp, responsive `sp` at 100/150/200% text, visible focus 2dp outline with 2dp offset, non-color state text/icons, semantic label/role and accurate `enabled/selected/stateDescription`. Screen horizontal padding 16dp, component gap 12dp, control gap 8dp. Driving overrides: Playback 64dp, Mic 72dp, Exit 56dp. No direct ExoPlayer, Room DAO, MediaStore Cursor, Android SpeechRecognizer or analytics SDK calls from components.

## Exhaustive component source registry

| ID | Component | Variants | States | Exact geometry from component JSON | Approved screen families |
|---|---|---|---|---|---|
| `CMP-01` | ActionButton | Primary, Secondary, Tonal, Destructive, Text | Default, Pressed, Focused, Disabled, Loading | minHeightDp=48, horizontalPaddingDp=16, iconGapDp=8, radiusDp=12 | WF-01, WF-02, WF-03, WF-04, WF-05, WF-07, WF-08, WF-09, WF-10 |
| `CMP-02` | IconButton | Standard, Tonal, Selected, Danger | Default, Pressed, Focused, Selected, Disabled, Pending | targetDp=48, iconDefaultDp=24, iconPlayerDp=32, iconDrivingDp=36 | WF-04, WF-05, WF-06, WF-07, WF-08, WF-09, WF-10 |
| `CMP-03` | AppBar | TopLevelTitle, BackTitle, BackTitleAction | Default, Focused, Disabled | minHeightDp=56, actionTargetDp=48, horizontalPaddingDp=16 | WF-03, WF-04, WF-05, WF-06, WF-09, WF-10 |
| `CMP-04` | BottomNavigation | VisibleDestination, SelectedDestination, P2DestinationHidden | Default, Pressed, Focused, Selected, Disabled | minHeightDp=64, itemTargetMinDp=48 | WF-04, WF-05, WF-09, WF-10 |
| `CMP-05` | PlaybackControls | StandardTriple, DrivingQuad | Default, Pressed, Focused, Disabled, Pending | standardTargetMinDp=48, drivingTargetMinDp=64, playerIconDp=32, drivingIconDp=36 | WF-06, WF-07 |
| `CMP-06` | MicControl | Player, Driving | Idle, Pressed, Focused, Disabled, SessionActive | standardTargetMinDp=48, playerPreferredTargetDp=56, drivingTargetMinDp=72, standardIconDp=24, drivingIconDp=36 | WF-06, WF-07, WF-08 |
| `CMP-07` | TrackMetadata | Compact, Standard, Player, Driving | Default, MutedSupporting, StateLabeled | maxLinesTitleNonDriving=2, maxLinesMetaNonDriving=2, drivingMarquee=false | WF-04, WF-05, WF-06, WF-07, WF-09 |
| `CMP-08` | Artwork | Row, MiniPlayer, FullPlayer, Driving, Placeholder | Available, Missing | aspectRatio=1:1, radiusDp=12, rowPreferredDp=48, miniPlayerPreferredDp=48 | WF-04, WF-05, WF-06, WF-07, WF-09 |
| `CMP-09` | MiniPlayer | Playing, Paused, Connecting, Recoverable, HiddenNoItem | Default, Pressed, Focused, Pending, Error | minHeightDp=72, artworkDp=48, playPauseTargetDp=48, horizontalPaddingDp=12 | WF-04, WF-06 |
| `CMP-10` | SearchField | LocalLibrarySearch | Default, Focused, QueryPresent, Disabled | minHeightDp=48, horizontalPaddingDp=12, iconTargetDp=48, radiusDp=12 | WF-05 |
| `CMP-11` | MediaRow | Track, QueueTrack, SearchResult, LikedTrackP2, Unavailable | Default, Pressed, Focused, Selected, Disabled, Pending | minHeightDp=64, leadingArtworkPreferredDp=48, trailingTargetDp=48, contentGapDp=12 | WF-04, WF-05, WF-06, WF-09 |
| `CMP-12` | EntityRow | Artist, Album, Folder | Default, Pressed, Focused, Disabled | minHeightDp=64, leadingVisualPreferredDp=48, trailingTargetDp=48 | WF-04 |
| `CMP-13` | ProgressSeek | PlayerSeek | Enabled, Pressed, Focused, Disabled, Pending | interactiveBandMinHeightDp=48, visualTrackDp=4, thumbDp=20 | WF-06 |
| `CMP-14` | PermissionEducationCard | Rationale, DeniedRequestable, PermanentlyDenied, GrantedStatus | Default, Focused, Loading, Error | paddingDp=16, actionMinHeightDp=48, radiusDp=16 | WF-03 |
| `CMP-15` | StatusBlock | Loading, Empty, Error, Success, SafePaused, NoTrack | Default, Loading, Error | paddingDp=24, actionMinHeightDp=48, radiusDp=16 | WF-01, WF-03, WF-04, WF-05, WF-06, WF-07, WF-09, WF-10 |
| `CMP-16` | ConfirmationChoice | Candidate, Neither, Cancel | Default, Pressed, Focused, Disabled, Pending | minHeightDp=48, preferredHeightDp=56, verticalGapDp=8, maxCandidateCount=2 | WF-08 |
| `CMP-17` | SnackbarStatus | Info, Success, ErrorNonBlocking | Visible, ActionFocused | minHeightDp=48, horizontalPaddingDp=16, maxActions=1 | WF-04, WF-05, WF-06, WF-08, WF-10 |
| `CMP-18` | SettingsRow | Navigation, Value, Toggle, StatusAction, DestructiveAction | Default, Pressed, Focused, Selected, Disabled, Pending, Error | minHeightDp=64, trailingTargetMinDp=48, horizontalPaddingDp=16 | WF-10 |
| `CMP-19` | VoiceFeedbackOverlay | PlayerOrigin, DrivingOrigin | Idle, Listening, Processing, Confirming, Executed, Failed | cancelTargetMinDp=48, listeningMaxMs=6000, maxAttempts=2, maxConfirmationCandidates=2 | WF-08, WF-06, WF-07 |
| `CMP-20` | LikeToggleP2 | LikeUnlike | Unliked, Liked, Pending, ErrorReverted | targetDp=48, iconDp=24 | WF-06, WF-09 |
| `CMP-21` | SystemBoundaryAnnotation | AndroidSystemHandoff | Default | prefix=[ANDROID / SYSTEM] | WF-03, WF-06, WF-10 |
| `CMP-22` | PagerIndicator | Onboarding | Current, Remaining, Completed | visualDotDp=8, visualGapDp=8 | WF-02 |
| `CMP-23` | ConfirmationPanel | DestructiveConfirmation | Default, Focused, Pending, Error | paddingDp=24, radiusDp=16, actionMinHeightDp=48 | WF-10 |

## Per-component observable state, user actions, transitions, ownership and accessibility

### CMP-01 — ActionButton

**Allowed variants:** Primary, Secondary, Tonal, Destructive, Text. **Component states:** Default, Pressed, Focused, Disabled, Loading.
**Dimensions / layout:** `minHeightDp = 48`; `horizontalPaddingDp = 16`; `iconGapDp = 8`; `radiusDp = 12`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** RootNavigationStateHolder + per-destination ViewModel; no new destination or data authority. Source register owner: Presentation only; callback to owning screen/use case.
**Observable state, user action, transition:** onClick → approved owning use case; Loading from actual pending result, not optimistic success. Back belongs screen.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** NONE from this component; no arbitrary click, item, query or settings events.. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-01, WF-02, WF-03, WF-04, WF-05, WF-07, WF-08, WF-09, WF-10. **Invariants:** Loading retains accessible action name; No optimistic domain success.

### CMP-02 — IconButton

**Allowed variants:** Standard, Tonal, Selected, Danger. **Component states:** Default, Pressed, Focused, Selected, Disabled, Pending.
**Dimensions / layout:** `targetDp = 48`; `iconDefaultDp = 24`; `iconPlayerDp = 32`; `iconDrivingDp = 36`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** RootNavigationStateHolder + per-destination ViewModel; no new destination or data authority. Source register owner: Presentation only.
**Observable state, user action, transition:** verb+object contentDescription; Play/Pause label from PlaybackSnapshot; enabled reflects actual capability.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** NONE from this component; no arbitrary click, item, query or settings events.. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-04, WF-05, WF-06, WF-07, WF-08, WF-09, WF-10. **Invariants:** Non-decorative glyph requires content description; Play/Pause semantic label changes with action.

### CMP-03 — AppBar

**Allowed variants:** TopLevelTitle, BackTitle, BackTitleAction. **Component states:** Default, Focused, Disabled.
**Dimensions / layout:** `minHeightDp = 56`; `actionTargetDp = 48`; `horizontalPaddingDp = 16`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** RootNavigationStateHolder + per-destination ViewModel; no new destination or data authority. Source register owner: Navigation presentation only.
**Observable state, user action, transition:** approved Back via root; Voice cancel precedes navigation; native Android Back remains OS.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** NONE from this component; no arbitrary click, item, query or settings events.. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-03, WF-04, WF-05, WF-06, WF-09, WF-10. **Invariants:** May grow under 200% text; Does not create destinations.

### CMP-04 — BottomNavigation

**Allowed variants:** VisibleDestination, SelectedDestination, P2DestinationHidden. **Component states:** Default, Pressed, Focused, Selected, Disabled.
**Dimensions / layout:** `minHeightDp = 64`; `itemTargetMinDp = 48`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** RootNavigationStateHolder + per-destination ViewModel; no new destination or data authority. Source register owner: Approved root navigation presentation.
**Observable state, user action, transition:** select approved root destination only; announce selected state; P2 route conditional.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** NONE from this component; no arbitrary click, item, query or settings events.. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-04, WF-05, WF-09, WF-10. **Invariants:** Item set controlled by approved wireframes; Selected state must be announced.

### CMP-05 — PlaybackControls

**Allowed variants:** StandardTriple, DrivingQuad. **Component states:** Default, Pressed, Focused, Disabled, Pending.
**Dimensions / layout:** `standardTargetMinDp = 48`; `drivingTargetMinDp = 64`; `playerIconDp = 32`; `drivingIconDp = 36`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** MediaSessionService → PlaybackControllerConnection → PlaybackGateway.snapshot & execute. UI owns no queue, position or service error. Source register owner: PlaybackSnapshot observer + command callback; service remains authority.
**Observable state, user action, transition:** onPrevious/onPlayPause/onNext/onSeekBack → PlaybackGateway.execute(PlaybackCommand with CommandId). Pending until service ACK; Previous >3s restart, else previous; SeekBack=-10000ms.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** No UI click event; service/domain may emit typed PlayerError and acked Voice CommandExecuted. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-06, WF-07. **Invariants:** StandardTriple=Previous/PlayPause/Next; DrivingQuad adds SeekBack; Success only after authoritative acknowledgement.

### CMP-06 — MicControl

**Allowed variants:** Player, Driving. **Component states:** Idle, Pressed, Focused, Disabled, SessionActive.
**Dimensions / layout:** `standardTargetMinDp = 48`; `playerPreferredTargetDp = 56`; `drivingTargetMinDp = 72`; `standardIconDp = 24`; `drivingIconDp = 36`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** VoiceSessionCoordinator.state (session generation/token/options) and only PlaybackGateway for final command ACK; UI never owns mic/provider state. Source register owner: Requests VoiceSessionCoordinator start; does not own Voice state.
**Observable state, user action, transition:** explicit Mic push-to-talk → VoiceSessionCoordinator.start(origin), capability precheck, no auto-capture or wake word.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** VoiceButtonPressed(coarse TelemetryPlaybackState) only through domain/gate. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-06, WF-07, WF-08. **Invariants:** Push-to-Talk only; No wake word; No auto-capture; Detailed Voice state rendered by CMP-19.

### CMP-07 — TrackMetadata

**Allowed variants:** Compact, Standard, Player, Driving. **Component states:** Default, MutedSupporting, StateLabeled.
**Dimensions / layout:** `maxLinesTitleNonDriving = 2`; `maxLinesMetaNonDriving = 2`; `drivingMarquee = false`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** Context-bound: on Full Player, MiniPlayer and Driving, select the displayed `MediaRef` **exclusively from the latest acknowledged `PlaybackGateway.snapshot.currentItem`** owned by `MediaSessionService`; resolve safe display-only metadata for that confirmed reference through the local MediaStore/LibraryRepository projection. In Library/Search/Liked rows, Room/LibraryRepository projects local rows, with MediaStore as identity authority. `TrackMetadata` owns neither current playback selection nor queue/position, and never switches the displayed playing item based on an independent last-clicked/search/Room selection. Source register owner: Presentation of local metadata + authoritative state labels.
**Observable state, user action, transition:** read display metadata linked to current service MediaRef or Room row, max two lines non-driving, never truncate critical Driving state.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** NONE from this component; no arbitrary click, item, query or settings events.. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-04, WF-05, WF-06, WF-07, WF-09. **Invariants:** No Driving marquee; Critical state labels never ellipsized away; Personal metadata prohibited from telemetry.

### CMP-08 — Artwork

**Allowed variants:** Row, MiniPlayer, FullPlayer, Driving, Placeholder. **Component states:** Available, Missing.
**Dimensions / layout:** `aspectRatio = 1:1`; `radiusDp = 12`; `rowPreferredDp = 48`; `miniPlayerPreferredDp = 48`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** Presentation-only artwork. For Player/MiniPlayer/Driving, derive the artwork lookup key from the **service-confirmed `PlaybackGateway.snapshot.currentItem`** and resolve image bytes locally; do not derive current artwork from a transient Room selection, search result or prior UI click. In browse/search/optional P2 rows, artwork may project the corresponding MediaStore-validated local item. `Artwork` never owns playback/current queue or chooses navigation. Source register owner: Presentation only.
**Observable state, user action, transition:** 1:1 artwork / neutral local fallback; decorative if metadata already spoken; artwork never determines theme.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** NONE from this component; no arbitrary click, item, query or settings events.. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-04, WF-05, WF-06, WF-07, WF-09. **Invariants:** No dynamic theme from artwork; Driving artwork subordinate to controls/text.

### CMP-09 — MiniPlayer

**Allowed variants:** Playing, Paused, Connecting, Recoverable, HiddenNoItem. **Component states:** Default, Pressed, Focused, Pending, Error.
**Dimensions / layout:** `minHeightDp = 72`; `artworkDp = 48`; `playPauseTargetDp = 48`; `horizontalPaddingDp = 12`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** MediaSessionService → PlaybackControllerConnection → PlaybackGateway.snapshot & execute. UI owns no queue, position or service error. Source register owner: Observes one MediaSession/Playback authority.
**Observable state, user action, transition:** subscribe to same PlaybackGateway.snapshot and connectionState as FullPlayer; onTap navigates to FullPlayer; no second Player/queue.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** NONE from this component; no arbitrary click, item, query or settings events.. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-04, WF-06. **Invariants:** Opens Full Player; No second player/queue; Reconnect preserves last verified state.

### CMP-10 — SearchField

**Allowed variants:** LocalLibrarySearch. **Component states:** Default, Focused, QueryPresent, Disabled.
**Dimensions / layout:** `minHeightDp = 48`; `horizontalPaddingDp = 12`; `iconTargetDp = 48`; `radiusDp = 12`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** `SearchViewModel` owns only the **ephemeral, in-memory query text and input/focus state**, scoped to the current Search screen. `SearchLibraryUseCase`/LibraryRepository owns *derived local results* from the Room projection, with MediaStore authoritative for track identity. `SearchField` never persists query text to Room, DataStore, logs, crash reports or telemetry; no analytics SDK receives text, result IDs or selected media names. A valid selection must be freshly verified before sending a command to the single `PlaybackGateway`. Source register owner: UI query presentation state; repository controls results.
**Observable state, user action, transition:** SearchViewModel ephemeral query and Clear/Submit; SearchLibraryUseCase, Room derived local results; never log query.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** NONE from this component; no arbitrary click, item, query or settings events.. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-05. **Invariants:** No online catalog fallback; No query/result telemetry; Forbidden in active Driving.

### CMP-11 — MediaRow

**Allowed variants:** Track, QueueTrack, SearchResult, LikedTrackP2, Unavailable. **Component states:** Default, Pressed, Focused, Selected, Disabled, Pending.
**Dimensions / layout:** `minHeightDp = 64`; `leadingArtworkPreferredDp = 48`; `trailingTargetDp = 48`; `contentGapDp = 12`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** MediaStore for media identity, Room/LibraryRepository for derived local browse/search projection; UI only renders ephemeral text. Source register owner: Presentation + selection callback; owning repo/service remains authority.
**Observable state, user action, transition:** tap MediaRef → SelectTrackUseCase → fresh MediaStore validation → PlaybackGateway.ReplaceQueue; stale reference falls back to typed recovery.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** NONE from this component; no arbitrary click, item, query or settings events.. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-04, WF-05, WF-06, WF-09. **Invariants:** Current/selected uses non-color cue; Stale item routes to owning recovery; No optimistic playback success.

### CMP-12 — EntityRow

**Allowed variants:** Artist, Album, Folder. **Component states:** Default, Pressed, Focused, Disabled.
**Dimensions / layout:** `minHeightDp = 64`; `leadingVisualPreferredDp = 48`; `trailingTargetDp = 48`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** MediaStore for media identity, Room/LibraryRepository for derived local browse/search projection; UI only renders ephemeral text. Source register owner: Presentation + browse callback.
**Observable state, user action, transition:** browse existing local Songs/Artists/Albums/Folders only; no web catalog; never show inside Driving.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** NONE from this component; no arbitrary click, item, query or settings events.. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-04. **Invariants:** No long-list use in active Driving.

### CMP-13 — ProgressSeek

**Allowed variants:** PlayerSeek. **Component states:** Enabled, Pressed, Focused, Disabled, Pending.
**Dimensions / layout:** `interactiveBandMinHeightDp = 48`; `visualTrackDp = 4`; `thumbDp = 20`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** MediaSessionService → PlaybackControllerConnection → PlaybackGateway.snapshot & execute. UI owns no queue, position or service error. Source register owner: Playback position projection + seek callback; service remains position authority.
**Observable state, user action, transition:** read positionMs/durationMs/canSeek from PlaybackSnapshot; onSeek → PlaybackGateway.SeekTo/SeekBy; clamp in service.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** NONE from this component; no arbitrary click, item, query or settings events.. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-06. **Invariants:** Enabled only when canSeek/duration truth permits; Precise position prohibited from telemetry.

### CMP-14 — PermissionEducationCard

**Allowed variants:** Rationale, DeniedRequestable, PermanentlyDenied, GrantedStatus. **Component states:** Default, Focused, Loading, Error.
**Dimensions / layout:** `paddingDp = 16`; `actionMinHeightDp = 48`; `radiusDp = 16`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** DataStore PreferencesRepository for app preferences; native Android permission/provider check for actual grants/capabilities; use cases for mutations. Source register owner: App education/recovery; Android permission state remains authority.
**Observable state, user action, transition:** educate and request OS permission; OpenSettings native external; recheck on return; never emulate Android dialog.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** NONE from this component; no arbitrary click, item, query or settings events.. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-03. **Invariants:** Open Settings is external handoff; Never recreates Android permission dialog; Not shown in active Driving.

### CMP-15 — StatusBlock

**Allowed variants:** Loading, Empty, Error, Success, SafePaused, NoTrack. **Component states:** Default, Loading, Error.
**Dimensions / layout:** `paddingDp = 24`; `actionMinHeightDp = 48`; `radiusDp = 16`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** RootNavigationStateHolder + per-destination ViewModel; no new destination or data authority. Source register owner: Presentation of typed owning-state/recovery.
**Observable state, user action, transition:** render typed Loading/Empty/Error & bounded Retry/Manual; no critical Snackbar-only recovery.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** NONE from this component; no arbitrary click, item, query or settings events.. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-01, WF-03, WF-04, WF-05, WF-06, WF-07, WF-09, WF-10. **Invariants:** Written state required; Critical recovery not Snackbar-only; Empty does not invent online content.

### CMP-16 — ConfirmationChoice

**Allowed variants:** Candidate, Neither, Cancel. **Component states:** Default, Pressed, Focused, Disabled, Pending.
**Dimensions / layout:** `minHeightDp = 48`; `preferredHeightDp = 56`; `verticalGapDp = 8`; `maxCandidateCount = 2`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** VoiceSessionCoordinator.state (session generation/token/options) and only PlaybackGateway for final command ACK; UI never owns mic/provider state. Source register owner: Voice confirmation presentation; VoiceSessionCoordinator owns token/options.
**Observable state, user action, transition:** Confirming.options immutable 1..2 from coordinator; confirm(token,optionIndex); Neither/Cancel separate from candidate list, do not re-query Library.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** NONE from this component; no arbitrary click, item, query or settings events.. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-08. **Invariants:** Candidate count 1..2; Candidate order immutable; displayLabel ephemeral; Neither/Cancel are recovery actions, not candidate objects.

### CMP-17 — SnackbarStatus

**Allowed variants:** Info, Success, ErrorNonBlocking. **Component states:** Visible, ActionFocused.
**Dimensions / layout:** `minHeightDp = 48`; `horizontalPaddingDp = 16`; `maxActions = 1`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** RootNavigationStateHolder + per-destination ViewModel; no new destination or data authority. Source register owner: Transient presentation only.
**Observable state, user action, transition:** non-critical transient hint; no irreversible, permission-denied or route-safety information exclusively in Snackbar.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** NONE from this component; no arbitrary click, item, query or settings events.. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-04, WF-05, WF-06, WF-08, WF-10. **Invariants:** Not sole critical recovery; No hard short timeout relied upon for accessibility; No personal content in evidence.

### CMP-18 — SettingsRow

**Allowed variants:** Navigation, Value, Toggle, StatusAction, DestructiveAction. **Component states:** Default, Pressed, Focused, Selected, Disabled, Pending, Error.
**Dimensions / layout:** `minHeightDp = 64`; `trailingTargetMinDp = 48`; `horizontalPaddingDp = 16`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** DataStore PreferencesRepository for app preferences; native Android permission/provider check for actual grants/capabilities; use cases for mutations. Source register owner: Preferences/platform/provider projection + callback.
**Observable state, user action, transition:** observe PreferencesRepository.preferences and actual provider/platform truth; update only after DataStore ACK; Bluetooth P1 suggestion never auto-enters Driving.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** NONE from this component; no arbitrary click, item, query or settings events.. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-10. **Invariants:** Permission/provider truth is rechecked; P1 Bluetooth is hint only; No Driving auto-entry/resume.

### CMP-19 — VoiceFeedbackOverlay

**Allowed variants:** PlayerOrigin, DrivingOrigin. **Component states:** Idle, Listening, Processing, Confirming, Executed, Failed.
**Dimensions / layout:** `cancelTargetMinDp = 48`; `listeningMaxMs = 6000`; `maxAttempts = 2`; `maxConfirmationCandidates = 2`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** VoiceSessionCoordinator.state (session generation/token/options) and only PlaybackGateway for final command ACK; UI never owns mic/provider state. Source register owner: VoiceSessionCoordinator is sole active Voice-state owner.
**Observable state, user action, transition:** overlay on Player/Driving, NOT NavHost destination; VoiceSessionCoordinator.state; Back cancels first; one Retry; service ACK before Executed; TTS failure does not reverse success.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** VoiceButtonPressed / IntentDetected / IntentFailed / CommandExecuted / ManualCorrection ONLY at domain boundary. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-08, WF-06, WF-07. **Invariants:** Overlay not destination; Exactly one retry after first settled failure; Executed follows playback acknowledgement; Back/Driving Exit cancels first.

### CMP-20 — LikeToggleP2

**Allowed variants:** LikeUnlike. **Component states:** Unliked, Liked, Pending, ErrorReverted.
**Dimensions / layout:** `targetDp = 48`; `iconDp = 24`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** Approved P2 local repository owns confirmed liked state; PlaybackGateway owns any resulting play. Source register owner: Local acknowledged P2 Like state remains authority.
**Observable state, user action, transition:** P2 local Likes selected after acknowledged write; on failure restore last confirmed state; no effect on P0 player.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** NONE from this component; no arbitrary click, item, query or settings events.. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-06, WF-09. **Invariants:** P2 removable; Failure returns to last confirmed state; Cannot block P0.

### CMP-21 — SystemBoundaryAnnotation

**Allowed variants:** AndroidSystemHandoff. **Component states:** Default.
**Dimensions / layout:** `prefix = [ANDROID / SYSTEM]`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** Android / MediaSession / speech provider owns native surface and its semantics. Documentation only. Source register owner: Documentation/handoff only.
**Observable state, user action, transition:** documentation annotation ONLY, not production Composable or NavHost route; real Android/MediaSession owner and return/recheck.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** NONE from this component; no arbitrary click, item, query or settings events.. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-03, WF-06, WF-10. **Invariants:** Never a production app destination/control; Used for permission/Settings/notification/lock/vehicle/system surfaces.

### CMP-22 — PagerIndicator

**Allowed variants:** Onboarding. **Component states:** Current, Remaining, Completed.
**Dimensions / layout:** `visualDotDp = 8`; `visualGapDp = 8`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** RootNavigationStateHolder + per-destination ViewModel; no new destination or data authority. Source register owner: Presentation only.
**Observable state, user action, transition:** noninteractive pager step x/y; dots visual only (8dp); do not add interactive navigation without review.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** NONE from this component; no arbitrary click, item, query or settings events.. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-02. **Invariants:** Non-interactive by default; If made interactive later, target >=48dp requires reviewed change.

### CMP-23 — ConfirmationPanel

**Allowed variants:** DestructiveConfirmation. **Component states:** Default, Focused, Pending, Error.
**Dimensions / layout:** `paddingDp = 24`; `radiusDp = 16`; `actionMinHeightDp = 48`. Runtime dp for dimensions and sp for text. Screen uses 4dp grid, 16dp inset, 12dp component / 8dp control gaps; these are overrides only where named.
**Source of truth / owning architecture component:** DataStore PreferencesRepository for app preferences; native Android permission/provider check for actual grants/capabilities; use cases for mutations. Source register owner: App confirmation UI; owning use case performs actual clear/delete.
**Observable state, user action, transition:** Clear approved local app data/history only on explicit Confirm; Back=Cancel; preserve user audio files and session.
**Back behavior:** defer to originating WF root and overlay priority; a component cannot create its own navigation graph. Voice cancel happens before root navigation; Driving Exit immediate.
**System handoff:** only explicit permission/Settings/MediaSession/audio-route owners; no platform screen rendered inside a component. CMP-21 must not be shipped as a Composable.
**Analytics hook:** NONE from this component; no arbitrary click, item, query or settings events.. If a typed event is approved it flows from domain to `TelemetryPrivacyGate.sanitize`, then optional nonblocking `TelemetrySink.tryEmit`. No personal metadata/option labels/IDs.
**Accessibility semantics:** expose existing action with verb/object; role/button and enabled/pending truth; `stateDescription` reflects acknowledged owner, not local optimistic state. Semantics traversal matches Phase-7 per-State order; 200% text wraps/scrolls, noncolor label/icon for Selected/Error/Loading, focus ring visible. Touch region at least 48dp unless Driving larger.
**Approved WFs:** WF-10. **Invariants:** No destructive default; Back=Cancel where owning flow specifies; No remote deletion claim without provider confirmation.

## Binding Phase 11 cross-role implementation clarification (PH11-MAJ-001/002)

This section resolves two evidence-backed **Major** ambiguities found by the AI-assisted Issue #17 review; it does not change the approved component token register, ADRs, Screen Family identities or privacy allowlist.

- **Player/Driving/MiniPlayer current item and artwork:** always derive the current playing `MediaRef` from the latest service-acknowledged `PlaybackGateway.snapshot.currentItem`. A Room row may resolve display metadata only for that service-selected `MediaRef`; stale/changed/deleted rows must trigger typed recovery and must not take over playback truth. `CMP-07 TrackMetadata` and `CMP-08 Artwork` are stateless renderers, not authorities.
- **Search query boundaries:** `CMP-10 SearchField` is a stateless UI for the `SearchViewModel`'s ephemeral query. Repository search results are derived from a local Room index; the actual media identity is revalidated against MediaStore. Query/labels/result IDs are not written to Room, DataStore, analytics, structured diagnostics or crash reports. Re-entered Search state must not invent a restored personal query.
- **QA responsibility:** add a ViewModel/Compose contract test that switches `PlaybackSnapshot.currentItem` while a stale Room result remains selected: Full Player and MiniPlayer metadata/artwork must follow the service snapshot or present a typed unavailable fallback, **never the stale result**. Add a query-persistence/telemetry redaction test on Search → Back → Search and on search error/retry. These are *future runtime tests*; the documented contract correction itself is testable now.

## Critical shared-component compositions

**Full Player (WF-06):** AppBar, Artwork, TrackMetadata, ProgressSeek, PlaybackControls, MicControl, optional LikeToggleP2. `PlaybackGateway.snapshot` is the sole live playback/queue/position source; reconnect replaces full snapshot; runtime error `AppError.MediaItem|Playback|Interruption` only (corrected Phase 10 contract).
**Driving (WF-07):** four large PlaybackControls + Mic + Exit + readable metadata. Always one layer, no keyboard/search/queue/settings/deep lists. Conditional Play last requires a verified local MediaRef; exit is immediate.
**Voice (WF-08):** VoiceFeedbackOverlay over its recorded origin; while Confirming render 1..2 immutable ConfirmationChoice rows sourced directly from `Confirming.options`; Neither/Cancel are separate actions, not extra options. Exactly one eligible retry, no approximation; TTS failure after service ACK preserves Executed.
**Settings (WF-10):** SettingsRow/ConfirmationPanel use DataStore ACK; Clear defaults to Cancel; OS Settings/consent outside Driving. No cloud provider enabled without approved PH10-MIN-002 disclosure.

## Source references

- `docs/design-system/components/COMPONENT_TOKENS.json` (23 original CMP IDs); `docs/design-system/foundations/TOKENS.json` (all primitive and semantic values).
- `docs/SOFTWARE_ARCHITECTURE.md`, `docs/architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md`, `docs/architecture/review/ISSUE_16_CONTRACT_CORRECTIONS_2026-08-30.md`.
- `docs/high-fidelity/phase-7/ACCESSIBILITY_PRIVACY_SYSTEM_MATRIX.md`: individual 45 app / 20 native handoff semantics.

**Status:** Engineering documentation authored; PH10-MIN-001 Compose/200%/TalkBack, Android/Media3/STT hardware QA and independent human approval remain NOT RUN.
