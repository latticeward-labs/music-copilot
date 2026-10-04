# Music Copilot Component System - Issue #17 Phase 3

- **Product:** Music Copilot MVP v1.0
- **Issue:** #17
- **Phase:** 3 - Reusable Component System
- **Date:** 3 October 2026
- **Final repository sync:** 4 October 2026
- **Status:** **PASS / COMPLETE / MERGED**
- **Frozen Phase-3 input commit:** `ee7685b4520953b8bab47ce16091fb8924f053cc`
- **Attached English source package SHA-256:** `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- **Foundation authority:** `docs/design-system/foundations/FOUNDATIONS.md`
- **Foundation tokens:** `docs/design-system/foundations/TOKENS.json`
- **Low-fidelity structural authority:** Issue #15 Wireframes under `docs/wireframes/`
- **Final runtime/ownership authority:** `docs/SOFTWARE_ARCHITECTURE.md`
- **Phase-10 Voice correction authority:** `docs/architecture/review/ISSUE_16_CONTRACT_CORRECTIONS_2026-08-30.md`
- **Machine-readable component contract:** `COMPONENT_TOKENS.json`
- **Coverage evidence:** `COMPONENT_COVERAGE_MATRIX.md`
- **Validation:** `PHASE_3_VALIDATION.md`

> This phase defines reusable presentation components only. It does not create or approve any final high-fidelity screen. Component specifications may style and compose approved behavior; they must not invent business logic, navigation, playback ownership, Voice ownership, persistence behavior, system screens, product scope, or telemetry fields.

## 1. Source lock

The component system is derived from the attached English source package and the current GitHub baselines.

### Attached-package constraints used by this phase

- `Music_Copilot_MVP_v1_EN.pdf`
  - Android-only MVP, Android 10+.
  - Basic Player, queue, local Library/Search, Driving Mode, Push-to-Talk Voice, Settings/Privacy.
  - Voice state model: `Idle -> Listening -> Processing -> Confirming -> Executed | Failed`.
  - Driving Mode: high contrast, one layer, no nested menus/long lists, minimum 48dp target, primary controls larger.
  - Ten required screen families and their required states.
- `ADR-0001_Native_Android_Platform_Revised.pdf`
  - Native Android / Compose direction.
- `ADR-0002_Service_Owned_Media3_Playback.pdf`
  - Playback/queue/player state is service-owned; UI is not a second playback authority.
- `ADR-0003_Local_First_Data_Boundaries_Revised.pdf`
  - Local-first data boundaries.
- `ADR-0004_Replaceable_Voice_Provider.pdf`
  - Push-to-Talk; listening bounded to six seconds; only one retry; short TTS; provider failure must not destabilize playback.
- `ADR-0005_Restricted_Deterministic_Intent_Mapping.pdf`
  - Exactly six English intents; one-step confirmation; no guessing; unclear confirmation returns safely to manual control.
- `ADR-0006_No_Backend_Account_or_Streaming.pdf`
  - No account/backend/sync/streaming/catalog expansion.
- `Music_Copilot_User_Flow_Phase_0_Foundation_v1.0_EN.pdf`
  - Required permissions/library/search/player/driving/voice/settings states, 48dp minimum, scalable text, TalkBack, short feedback.

### Current GitHub constraints used by this phase

- `docs/design-system/foundations/FOUNDATIONS.md` and `TOKENS.json` control current visual values.
- `docs/wireframes/foundations/README.md` establishes the earlier reusable low-fidelity component contract.
- `docs/wireframes/app-states/README.md` supplies the 32 app-visible frame baseline.
- `docs/design-system/HIFI_TRACEABILITY_REGISTER.json` controls the 126-state disposition.
- `docs/SOFTWARE_ARCHITECTURE.md` controls final runtime ownership.
- Phase 10 Issue #16 correction requires `VoiceSessionState.Confirming.options` to contain **1..2 immutable user-visible labels**; `Neither` and `Cancel` are recovery actions, not synthetic candidates.

## 2. Component-system rules

1. **No one-off controls:** final high-fidelity screens must compose these components or record a reviewed component-system change.
2. **No hidden business logic:** components expose visual state and callbacks only; application/domain/service owners remain authoritative.
3. **Touch target:** every interactive target is at least 48x48dp.
4. **Driving overrides:** primary Driving playback controls >=64dp, Mic >=72dp, Exit >=56dp.
5. **200% text:** components grow/reflow rather than clipping critical content/actions.
6. **TalkBack:** every interactive or semantic component has an explicit accessible name/state/order contract.
7. **Non-color-only:** selection, disabled, loading, error, permission and Voice states require text/icon/shape in addition to color.
8. **Pending is not success:** controls may show a neutral pending state while waiting for authoritative acknowledgement; they do not optimistically fabricate playback/Voice success.
9. **System boundaries stay external:** Android dialogs, Settings, notification, lock-screen and vehicle controls are handoffs, not app components.
10. **Personal content privacy:** track/artist/album/search/candidate labels may be displayed locally but must not be copied into telemetry or repository evidence.

## 3. Global state vocabulary

The following reusable state names are available across the system where applicable:

| State | Meaning | Required treatment |
|---|---|---|
| `Default` | Ready and actionable | Normal semantic roles |
| `Pressed` | Pointer/touch acknowledgement | Foundation pressed overlay; `motion.fast` |
| `Focused` | Keyboard/switch/accessibility focus | 2dp focus ring + semantic focus |
| `Selected` | Current/active choice | Primary container + written/icon state cue |
| `Disabled` | Action unavailable | Disabled roles + semantic disabled state |
| `Loading` | Work is in progress | Written state + bounded indicator; not visually identical to Disabled |
| `Pending` | Command submitted, authoritative result not yet returned | Neutral pending affordance; last confirmed state remains truthful |
| `Error` | Typed failure/recovery state | Error role + written reason/recovery where owned |

Not every component supports every state. Unsupported state combinations must not be invented locally by a screen.

## 4. Component catalog

### CMP-01 - ActionButton

**Use:** primary, secondary and destructive text actions.

**Geometry**
- Minimum height: 48dp.
- Horizontal padding: 16dp.
- Icon/text gap: 8dp.
- Radius: 12dp (`radius.mdDp`).
- Minimum touch target: 48x48dp.

**Variants**
- `Primary`
- `Secondary`
- `Tonal`
- `Destructive`
- `Text`

**States**
- Default, Pressed, Focused, Disabled, Loading.
- `Destructive` uses the error semantic role but is not itself an Error state.

**Accessibility**
- Accessible name equals visible action label unless additional context is essential.
- Loading keeps the action name and announces progress; it must not become an unlabeled spinner.
- At 200% text, button height may grow.

**Typical use:** Continue, Not now, Open Settings, Retry, Rescan, Exit, Confirm clear, Cancel, Play list.

---

### CMP-02 - IconButton

**Use:** compact icon actions without inventing a new interaction style.

**Geometry**
- Standard target: 48dp.
- Standard glyph: 24dp.
- Player glyph: 32dp when high-emphasis.
- Driving glyph: 36dp only inside Driving variants.
- Radius: circular/pill geometry.

**Variants**
- `Standard`
- `Tonal`
- `Selected`
- `Danger`
- `P2Like` through CMP-20 only; do not duplicate Like logic here.

**States**
- Default, Pressed, Focused, Selected, Disabled, Pending.

**Accessibility**
- Every non-decorative icon requires a label such as `Play`, `Pause`, `Search`, `Back`, `More`.
- Play/Pause changes both glyph and semantic label.

---

### CMP-03 - AppBar

**Use:** approved top-level or back-navigation header.

**Geometry**
- Reference minimum height: 56dp.
- Leading/trailing action target: 48dp.
- Horizontal padding: 16dp.
- Height may grow under 200% text.

**Variants**
- `TopLevelTitle`
- `BackTitle`
- `BackTitleAction`

**States**
- Default, Focused action, Disabled trailing action where source behavior requires.

**Accessibility**
- Reading order: leading navigation/action -> title -> trailing action.
- Back semantics must describe destination only when it is known and stable.

**Constraint:** AppBar does not create a destination; destination set remains controlled by approved navigation.

---

### CMP-04 - BottomNavigation

**Use:** approved top-level navigation only.

**Geometry**
- Reference minimum height: 64dp.
- Each destination target >=48dp.

**Variants**
- `VisibleDestination`
- `SelectedDestination`
- `P2DestinationHidden` for the removable Liked Songs branch.

**States**
- Default, Pressed, Focused, Selected, Disabled/hidden only when source scope permits.

**Accessibility**
- Left-to-right task order.
- Selected destination is announced using state semantics and a non-color cue.

**Constraint:** item set comes from approved Wireframes; the component does not invent new destinations.

---

### CMP-05 - PlaybackControls

**Use:** service-backed manual playback commands.

**Standard variant**
- Previous / Play-Pause / Next.
- Minimum target: 48dp each.
- Player high-emphasis glyph: 32dp.

**Driving variant**
- Previous / Play-Pause / Next / Seek Back.
- Primary target: >=64dp.
- Driving glyph: 36dp.
- Placement remains stable across Playing/Paused/No Track.

**States**
- Default, Pressed, Focused, Disabled, Pending.
- Play and Pause are mutually truthful projections of authoritative playback state.
- Previous/Next/Seek availability derives from `PlaybackSnapshot` capabilities/state.

**Behavior boundary**
- Component emits callbacks/commands only.
- Success is not shown until acknowledged by Playback authority.
- Previous policy and Seek -10s policy remain domain/service rules, not component logic.

**Accessibility**
- Order: Previous -> Play/Pause -> Next -> Seek Back when present.
- Action names describe the action, not merely the glyph.

---

### CMP-06 - MicControl

**Use:** explicit Push-to-Talk entry.

**Geometry**
- Standard target: >=48dp.
- Player preferred high-emphasis target: 56dp.
- Driving target: >=72dp.
- Standard icon: 24dp; Driving icon: 36dp.

**Variants**
- `Player`
- `Driving`

**States**
- Idle, Pressed, Focused, Disabled, SessionActive.
- Voice-state detail is rendered by CMP-19; this control does not duplicate the Voice state machine.

**Behavior boundary**
- Tap requests Voice start after current permission/provider checks.
- No wake word, auto-capture or auto-retry.

**Accessibility**
- Label: `Voice command` or equivalent approved copy.
- Active session exposes state through CMP-19, not animation alone.

---

### CMP-07 - TrackMetadata

**Use:** consistent title/artist/album/state presentation.

**Variants**
- `Compact`
- `Standard`
- `Player`
- `Driving`

**Typography**
- Compact: `bodyLarge` title + `bodyMedium` metadata.
- Player: `titleLarge` title + `bodyLarge/bodyMedium` metadata.
- Driving: `drivingTitle` + `drivingMeta` + written `drivingState`.

**Rules**
- Title/artist values are presentation content only.
- No marquee in Driving Mode.
- Critical playback/Voice state labels may not be ellipsized away.
- At 200% text, layout reflows vertically.

---

### CMP-08 - Artwork

**Use:** album/track artwork or neutral placeholder.

**Rules**
- Aspect ratio: 1:1.
- Radius: 12dp.
- Missing artwork: neutral local placeholder + music glyph.
- No blur-heavy background or metadata-derived dynamic theme.

**Size roles**
- `Row`: 48dp preferred.
- `MiniPlayer`: 48dp preferred.
- `FullPlayer`: responsive; final size determined by Phase 4 layout while preserving approved hierarchy.
- `Driving`: subordinate to state/title/manual controls and allowed to shrink for text reflow.

**Accessibility**
- Decorative when metadata already identifies the item.
- If meaningful, use concise non-sensitive label.

---

### CMP-09 - MiniPlayer

**Use:** truthful observation/re-entry surface for an existing current item.

**Composition**
- Artwork
- TrackMetadata compact
- Play/Pause IconButton
- Optional connection/status label when truth requires it

**Geometry**
- Minimum height: 72dp.
- Artwork: 48dp.
- Play/Pause target: 48dp.

**Variants**
- `Playing`
- `Paused`
- `Connecting`
- `Recoverable`
- `HiddenNoItem`

**Behavior boundary**
- Observes one MediaSession/Playback authority.
- Opens Full Player.
- Never owns a second queue/player.
- On reconnect, preserves last verified state and does not fabricate Paused/Stopped/zero position.

---

### CMP-10 - SearchField

**Use:** local-library search outside active Driving Mode.

**Geometry**
- Minimum height: 48dp.
- Horizontal padding: 12dp.
- Search/Clear icon targets: 48dp.
- Radius: 12dp.

**States**
- Default, Focused, QueryPresent, Disabled.
- Result Loading/Error are rendered by owning result/status components rather than turning the field itself into a fabricated repository state.

**Behavior/privacy**
- Query is UI presentation state.
- No online-catalog fallback.
- Search query/results are prohibited from telemetry.
- Not shown in active Driving Mode.

---

### CMP-11 - MediaRow

**Use:** track rows across Library, Search, Queue and optional Liked Songs.

**Geometry**
- Minimum height: 64dp.
- Leading artwork: 48dp preferred.
- Trailing interactive target: 48dp.
- Horizontal gap: 12dp.

**Variants**
- `Track`
- `QueueTrack`
- `SearchResult`
- `LikedTrackP2`
- `Unavailable`

**States**
- Default, Pressed, Focused, Selected/Current, Disabled/Unavailable, Pending.

**Composition**
- Artwork
- TrackMetadata compact
- Optional trailing action (queue/current/Like where source scope permits)

**Behavior boundary**
- Selection requests playback; it does not claim playback success before service acknowledgement.
- Stale/missing references route to owning recovery flow.

---

### CMP-12 - EntityRow

**Use:** Artist, Album or Folder browse rows.

**Geometry**
- Minimum height: 64dp.
- Leading visual: 48dp preferred.
- Trailing navigation target: 48dp when present.

**Variants**
- `Artist`
- `Album`
- `Folder`

**States**
- Default, Pressed, Focused, Disabled/Unavailable.

**Accessibility**
- Accessible label contains entity role plus visible name where needed.
- No long-list use inside active Driving Mode.

---

### CMP-13 - ProgressSeek

**Use:** progress display and manual seek on permitted non-driving Player surfaces.

**Geometry**
- Whole interactive band: minimum 48dp high.
- Visual track: 4dp.
- Thumb: 20dp.
- Time labels use `labelMedium`.

**States**
- Enabled, Pressed/Dragging, Focused, Disabled, Pending.

**Behavior boundary**
- Enabled only when authoritative `canSeek` and verified duration allow it.
- Position is a projection of Playback authority.
- Drag/seek callback submits a command; the component does not become position authority.
- Precise position is prohibited from telemetry.

---

### CMP-14 - PermissionEducationCard

**Use:** app-owned explanation/recovery around Android-owned permission surfaces.

**Composition**
- Icon/status marker
- Heading
- Purpose/consequence copy
- Primary action
- Secondary `Not now`/Cancel action
- Optional system-boundary annotation

**Variants**
- `Rationale`
- `DeniedRequestable`
- `PermanentlyDenied`
- `GrantedStatus`

**States**
- Default, Focused, Loading while awaiting app-owned work, Error only for app recovery state.

**Behavior boundary**
- Android permission truth is rechecked after every return.
- `Open Settings` is an external handoff.
- The component never recreates an Android permission dialog.

---

### CMP-15 - StatusBlock

**Use:** reusable Loading, Empty, Error and Success/recovery presentation.

**Variants**
- `Loading`
- `Empty`
- `Error`
- `Success`
- `SafePaused`
- `NoTrack`

**Composition**
- State icon/symbol
- Written state heading
- Short explanation
- Optional primary/secondary action

**Rules**
- Loading is visibly distinct from Disabled.
- Error red is not the only cue.
- Empty state must not fabricate online results/recommendations.
- Critical recovery cannot depend only on transient Snackbar feedback.

---

### CMP-16 - ConfirmationChoice

**Use:** Voice ambiguity only.

**Geometry**
- Each choice >=56dp preferred, never below 48dp.
- Vertical gap: 8dp.
- One or two candidate choices only.

**Variants**
- `Candidate`
- `Neither`
- `Cancel`

**States**
- Default, Pressed, Focused, Disabled/Pending after submission.

**Binding Voice contract**
- Candidate labels come directly and only from the current `VoiceSessionState.Confirming.options`.
- `options.size` is 1..2.
- Candidate order is immutable for the active token.
- Candidate `displayLabel` is ephemeral personal media text; never persisted/logged/telemetry/repository evidence.
- `Neither` and `Cancel` are recovery actions, not candidate objects.
- A stale token/index cannot execute.

---

### CMP-17 - SnackbarStatus

**Use:** non-critical transient acknowledgement/status only.

**Variants**
- `Info`
- `Success`
- `ErrorNonBlocking`

**Composition**
- Short text
- Optional one action

**Rules**
- Never carries the only path to recover from a critical error.
- No personal media/search/Voice candidate content is copied into diagnostic evidence.
- Duration is implementation/platform-aware; accessibility must not rely on a hard short timeout.
- Voice/TTS/error copy remains concise; where the approved flow requires it, at most one short sentence.

---

### CMP-18 - SettingsRow

**Use:** Settings, Privacy, consent and P1 Bluetooth-hint rows.

**Geometry**
- Minimum height: 64dp.
- Trailing target >=48dp.
- Supporting value may wrap at 200% text.

**Variants**
- `Navigation`
- `Value`
- `Toggle`
- `StatusAction`
- `DestructiveAction`

**States**
- Default, Pressed, Focused, Selected/Updated, Disabled, Pending, Error.

**Behavior boundary**
- Fresh platform/provider truth controls permissions/capability.
- Stored preference is not used as fake permission truth.
- P1 Bluetooth value is a hint only; it never auto-opens Driving or auto-resumes playback.

---

### CMP-19 - VoiceFeedbackOverlay

**Use:** single reusable Voice presentation overlay on Player or Driving origin.

**State model**
`Idle -> Listening -> Processing -> Confirming -> Executed | Failed`

**Rendering contract**
- `Idle`: overlay absent/closed.
- `Listening`: written Listening state; bounded capture <=6s; Cancel reachable.
- `Processing`: written Processing state; manual/Exit continuity preserved.
- `Confirming`: compose CMP-16 using 1..2 immutable current options + Neither/Cancel.
- `Executed`: short success acknowledgement after playback authority acknowledges the command.
- `Failed`: short typed failure; one Retry only when `canRetry=true`; manual fallback remains reachable.

**Ownership**
- Active Voice state is owned by `VoiceSessionCoordinator`.
- Origin is Player or Driving; overlay is not a navigation destination.
- Back/Driving Exit cancels Voice before returning to origin.

**Privacy**
- Raw audio/transcript/candidate labels are not persisted or added to telemetry/evidence.

---

### CMP-20 - LikeToggleP2

**Use:** optional P2 Like/Unlike control only.

**Geometry**
- Target: 48dp.
- Glyph: 24dp.

**States**
- Unliked, Liked, Pending, Error/Reverted.

**Behavior boundary**
- Local acknowledged Like state is authoritative.
- Pending does not silently become acknowledged.
- Mutation failure returns to last confirmed state.
- Component is removable together with the P2 branch and must not block P0.

---

### CMP-21 - SystemBoundaryAnnotation

**Use:** repository design/handoff annotation only; **not a production Compose app control**.

**Purpose**
- Marks Android/System-owned permission dialogs, Settings, notifications, lock-screen, Bluetooth/head-unit and other system handoffs.
- Prevents high-fidelity work from inventing fake app destinations.

**Visual treatment**
- Dashed/outlined bounded note.
- Prefix: `[ANDROID / SYSTEM]`.

---

### CMP-22 - PagerIndicator

**Use:** Onboarding page progress only.

**Geometry**
- Individual visual dot/pill is non-interactive unless the approved screen explicitly makes it actionable.
- If interactive in a later reviewed design, each target must be >=48dp.

**States**
- Current, Remaining, Completed.

**Accessibility**
- Announces page position such as `Page 1 of 2`; state is not color-only.

---

### CMP-23 - ConfirmationPanel

**Use:** non-Voice explicit confirmation such as Settings clear-data/history confirmation.

**Composition**
- Warning/title
- Selected categories/details
- Preserved categories/details where required
- Destructive Confirm action
- Cancel action

**States**
- Default, Focused, Pending, Error result handled by owning screen/status component.

**Behavior boundary**
- No destructive action is preselected or automatically executed.
- Back equals Cancel where the approved flow says so.
- Component does not claim remote/provider deletion without provider confirmation.

## 5. Shared composition rules

### Player composition

`Artwork + TrackMetadata + ProgressSeek + PlaybackControls + MicControl + optional LikeToggleP2 + Queue MediaRows`

All playback-visible values remain projections of service truth.

### Home/Library composition

`AppBar + EntityRows/MediaRows + MiniPlayer + BottomNavigation + StatusBlock as needed`

### Search composition

`AppBar + SearchField + MediaRows + StatusBlock + BottomNavigation`

### Driving composition

`TrackMetadata(Driving) + Artwork + PlaybackControls(Driving) + MicControl(Driving) + immediate Exit ActionButton`

No SearchField, BottomNavigation, SettingsRow, long list, permission UI or nested menu.

### Voice composition

`VoiceFeedbackOverlay + ConfirmationChoice when Confirming + ActionButton/IconButton for Retry/Manual/Cancel as source permits`

### Settings composition

`AppBar + SettingsRows + ConfirmationPanel + StatusBlock/SnackbarStatus + BottomNavigation + SystemBoundaryAnnotation when handing off`

## 6. Compose-facing API guidance

These are design-facing signatures, not final production source files:

```kotlin
@Composable
fun McActionButton(
    label: String,
    variant: ActionButtonVariant,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
)

@Composable
fun McPlaybackControls(
    snapshot: PlaybackUiProjection,
    variant: PlaybackControlsVariant,
    pendingCommand: PendingPlaybackCommand?,
    onCommand: (PlaybackUiCommand) -> Unit,
)

@Composable
fun McVoiceOverlay(
    state: VoiceSessionState,
    onConfirm: (token: VoiceCandidateToken, optionIndex: Int) -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
)
```

The UI projection types above must be produced by ViewModel/presentation mapping. They must not become new authoritative domain state.

## 7. Phase 3 exit contract

Phase 3 can be marked PASS only when:

- [x] Reusable component IDs are defined for all recurring controls used by the 32 approved app-visible low-fidelity frames.
- [x] Required user-requested families are covered: Buttons, Icon Buttons, Playback Controls, Mic, Track Metadata, Artwork, Mini-player, Search, Library/Track Row, Artist/Album Row, Progress/Seek, Permission Education, Empty/Error/Loading, Confirmation Choice, Snackbar/Status, Settings Row and Voice Feedback.
- [x] Low-fidelity reusable contracts for App Bar, Bottom Navigation and System-only Annotation are preserved.
- [x] P2 Like has a removable dedicated component rather than leaking optional behavior into P0 controls.
- [x] Non-Voice destructive confirmation has a reusable component rather than a Settings-only one-off.
- [x] Applicable Default/Pressed/Focused/Selected/Disabled/Loading/Pending/Error behaviors are defined.
- [x] Every interactive component respects >=48dp and 200% text/TalkBack/non-color-only rules.
- [x] Driving component overrides preserve one layer, immediate Exit and fixed large controls.
- [x] Voice states match the final architecture contract.
- [x] Confirming renders exactly 1..2 current immutable candidate labels plus Neither/Cancel.
- [x] No component becomes a second playback, Voice, permission or data authority.
- [x] No backend/account/streaming/Android Auto/iOS/wake-word/open-conversation scope is introduced.
- [x] No final high-fidelity screen is claimed complete.

**Phase-3 decision:** PASS / COMPLETE / MERGED. PR #51 merged to `main` at `b6113b0d094e0c9f5fd55842a80a90528cdc4e4f`; Phase 4 may consume this approved component baseline.
