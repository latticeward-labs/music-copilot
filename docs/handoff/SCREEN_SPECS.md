# SCREEN_SPECS — Music Copilot Android Jetpack Compose Engineering Handoff

**Issue #17 Phase 9.** Canonical product reference: English MSM2 archive SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`. Architecture: `docs/SOFTWARE_ARCHITECTURE.md`, corrected Issue #16 API `ISSUE_16_CONTRACT_CORRECTIONS_2026-08-30.md`. Android API 29+, Kotlin Compose, one :app module and single Activity/NavHost.

## Rules common to all screen families

- Fixed design baseline **Night Signal** (no new theme selector). Reference 360×800 **dp**, static 1080×2400 image is not runtime sizing; 16dp screen inset, 12dp component spacing, 8dp control spacing, 4dp grid. Typography `sp`, responsive scroll/wrap at 100%, 150%, 200% font scale, no clipped recovery.
- **Owner separation:** MediaSessionService owns live queue, current item, player position/state and service runtime error; VoiceSessionCoordinator owns session generation/candidate token/immutable 1–2 options; MediaStore owns media existence; Room is a derived index; DataStore owns preferences/consent/non-authoritative P1 Paused restore; root navigation owns route/Voice origin.
- Commands sent by screen ViewModels/use cases; never optimistic playback success. `PlaybackGateway.execute` returns `AppResult<PlaybackSnapshot>` and `snapshot` is the authoritative `StateFlow`. Corrected `lastError: AppError?` is restricted to MediaItem/Playback/Interruption; Controller errors stay connection-level.
- System permission dialogs, Settings, media notifications, lock screen, Bluetooth/headset media-key screens and vehicle surfaces are not Compose destinations. **Voice is an overlay**, not NavHost route. On Back, cancel Voice first; Driving Exit immediately returns to recorded origin.
- Analytics is closed typed `TelemetryEvent` → `TelemetryPrivacyGate.sanitize` → optional nonblocking `TelemetrySink.tryEmit`. No event is collected directly from Composable; no raw audio, transcript, title, MediaRef/URI, query/results, precise position, location or Bluetooth device identity.

## 10 Screen Family contracts (WF-08 is an overlay, not an independent navigation destination)

### WF-01 — Splash / Bootstrap

**Route:** `Bootstrap` · **Package:** `feature/onboarding` · **Owning architecture / source of truth:** BootstrapUseCase; verified migration/readiness, PreferencesRepository onboarding status.
**Observable projection (lifecycle-aware):** Loading/Ready/Typed Migration Error; recheck platform and local migration.
**Actual user actions → transitions:** Wait→validated Onboarding/Permissions/Library; Retry→re-evaluate error without deleting data.
**Back precedence:** Android Back exits launch task; no internal Back.
**Android/System handoff:** Android splash/system chrome OS owned.
**Approved analytics hook:** No typed startup analytics event.
**Layout/dimensions:** Default inset16dp, grid4dp, intercomponent gap12dp, minimum48dp hit regions; specific component overrides below. Typography values from COMPOSE_TOKEN_MAPPING.md, height grows at fontScale=2.0. No fixed-height clipping.
**TalkBack/semantics:** Heading first; explicit stateDescription & noncolor error/selection; actions expose verb+object, disabled/pending truth and focus restoration. For each individual dedicated State consult `docs/high-fidelity/phase-7/app-visible/{StateID}.md`.
**Allowed components:** `CMP-01`, `CMP-15`.
**Primary approved asset:** `docs/high-fidelity/WF-01/HF-01_SPLASH_LOADING.png` paired with `docs/high-fidelity/WF-01/HF-01_SPLASH_LOADING.md` (sha256 bbe9ab96d248b01374822c2d67c3678643dcf9d635ec30fe39dead1b7cefd58a).
**Canonical States covered:** `S-01`. **Phase-7 dedicated A11y dossiers:** 1.

### WF-02 — Onboarding and optional Voice education

**Route:** `Onboarding` · **Package:** `feature/onboarding` · **Owning architecture / source of truth:** PreferencesRepository DataStore onboardingComplete; transient local page index.
**Observable projection (lifecycle-aware):** Step/Skip/Resume/onboardingComplete.
**Actual user actions → transitions:** Continue or Skip→next approved step/Permissions; Back→prior step; PreferencesRepository.update only on ACK.
**Back precedence:** Previous step, no extra route.
**Android/System handoff:** Android media permission is deferred until WF-03.
**Approved analytics hook:** No approved onboarding telemetry event.
**Layout/dimensions:** Default inset16dp, grid4dp, intercomponent gap12dp, minimum48dp hit regions; specific component overrides below. Typography values from COMPOSE_TOKEN_MAPPING.md, height grows at fontScale=2.0. No fixed-height clipping.
**TalkBack/semantics:** Heading first; explicit stateDescription & noncolor error/selection; actions expose verb+object, disabled/pending truth and focus restoration. For each individual dedicated State consult `docs/high-fidelity/phase-7/app-visible/{StateID}.md`.
**Allowed components:** `CMP-01`, `CMP-22`.
**Primary approved asset:** `docs/high-fidelity/WF-02/HF-02_ONBOARDING_SLIDE_1.png` paired with `docs/high-fidelity/WF-02/HF-02_ONBOARDING_SLIDE_1.md` (sha256 4a84bfb50e6127e1d3d8838a42145a3abbdb0e52a5aeee526a1adbfb68ad4169).
**Canonical States covered:** `S-02`, `CA-01`. **Phase-7 dedicated A11y dossiers:** 2.

### WF-03 — Permissions / Native recovery

**Route:** `Permissions` · **Package:** `feature/onboarding` · **Owning architecture / source of truth:** Android permission capability current truth; RequestPermissionUseCase / RecheckPermissionUseCase.
**Observable projection (lifecycle-aware):** Granted / Denied-requestable / PermanentlyDenied / Revoked; result of actual Android permission recheck.
**Actual user actions → transitions:** Request→native OS grant/denial; OpenSettings→Android Settings→recheck; NotNow→truthful manual fallback.
**Back precedence:** Dismiss app rationale; system Back inside native dialog belongs Android.
**Android/System handoff:** SYS-01 media, SYS-02 mic, SYS-03 notification, SYS-04/CE-18 native Settings.
**Approved analytics hook:** No typed permission analytics event; do not emit OS payload.
**Layout/dimensions:** Default inset16dp, grid4dp, intercomponent gap12dp, minimum48dp hit regions; specific component overrides below. Typography values from COMPOSE_TOKEN_MAPPING.md, height grows at fontScale=2.0. No fixed-height clipping.
**TalkBack/semantics:** Heading first; explicit stateDescription & noncolor error/selection; actions expose verb+object, disabled/pending truth and focus restoration. For each individual dedicated State consult `docs/high-fidelity/phase-7/app-visible/{StateID}.md`.
**Allowed components:** `CMP-01`, `CMP-03`, `CMP-14`, `CMP-15`, `CMP-21`.
**Primary approved asset:** `docs/high-fidelity/WF-03/HF-03_PERMISSIONS_RATIONALE_REQUEST.png` paired with `docs/high-fidelity/WF-03/HF-03_PERMISSIONS_RATIONALE_REQUEST.md` (sha256 996c44a3ebf1eb2f177fc8b9db5b124848a14421b8516c3ceab1d971ee6c5332).
**Canonical States covered:** `S-03`, `CA-02`, `SYS-01`, `SYS-02`, `SYS-04`, `SYS-05`, `CD-21`. **Phase-7 dedicated A11y dossiers:** 1.

### WF-04 — Home / Local Library

**Route:** `HomeLibrary` · **Package:** `feature/library` · **Owning architecture / source of truth:** MediaStore identity; LibraryRepository/Room derived index; MiniPlayer observes PlaybackGateway.
**Observable projection (lifecycle-aware):** LibraryIndexSnapshot(status,revision,itemCount,lastError), observeLibrary() rows; PlaybackGateway.snapshot for mini player.
**Actual user actions → transitions:** Rescan/Retry→reconcile; Songs/Artists/Albums/Folders→local browse; select→MediaStore revalidate→ReplaceQueue command; Search→WF-05.
**Back precedence:** Root navigation; never stop service playback on Back.
**Android/System handoff:** Android permission revoke→WF-03 explanatory recovery then native OS.
**Approved analytics hook:** No event for library contents, query or scan; domain typed PlayerError only.
**Layout/dimensions:** Default inset16dp, grid4dp, intercomponent gap12dp, minimum48dp hit regions; specific component overrides below. Typography values from COMPOSE_TOKEN_MAPPING.md, height grows at fontScale=2.0. No fixed-height clipping.
**TalkBack/semantics:** Heading first; explicit stateDescription & noncolor error/selection; actions expose verb+object, disabled/pending truth and focus restoration. For each individual dedicated State consult `docs/high-fidelity/phase-7/app-visible/{StateID}.md`.
**Allowed components:** `CMP-01`, `CMP-02`, `CMP-03`, `CMP-04`, `CMP-07`, `CMP-08`, `CMP-09`, `CMP-11`, `CMP-12`, `CMP-15`, `CMP-17`.
**Primary approved asset:** `docs/high-fidelity/WF-04/HF-04_HOME_LIBRARY_READY.png` paired with `docs/high-fidelity/WF-04/HF-04_HOME_LIBRARY_READY.md` (sha256 b55498321e9fefb08f14b7223d93dba64a69fd6969375ac8341b23fe51c539ba).
**Canonical States covered:** `S-04`, `CB-01`, `CB-02`, `CB-03`, `CB-04`, `CB-05`, `CB-06`, `CB-07`, `CB-08`, `CB-13`, `CB-20`. **Phase-7 dedicated A11y dossiers:** 2.

### WF-05 — Local Search

**Route:** `Search` · **Package:** `feature/search` · **Owning architecture / source of truth:** SearchViewModel owns transient query; LibraryRepository.search gives derived results; MediaStore validates selection.
**Observable projection (lifecycle-aware):** query, pending, result/no-result/error, library revision.
**Actual user actions → transitions:** Enter/Clear query; search local only; select row→SelectTrackUseCase→PlaybackGateway, await ACK; Retry retains query.
**Back precedence:** Keyboard/focus close then approved Home/Library origin.
**Android/System handoff:** Permission revoke uses OS-only permission handoff outside Search.
**Approved analytics hook:** No query/result/title analytics (forbidden).
**Layout/dimensions:** Default inset16dp, grid4dp, intercomponent gap12dp, minimum48dp hit regions; specific component overrides below. Typography values from COMPOSE_TOKEN_MAPPING.md, height grows at fontScale=2.0. No fixed-height clipping.
**TalkBack/semantics:** Heading first; explicit stateDescription & noncolor error/selection; actions expose verb+object, disabled/pending truth and focus restoration. For each individual dedicated State consult `docs/high-fidelity/phase-7/app-visible/{StateID}.md`.
**Allowed components:** `CMP-01`, `CMP-02`, `CMP-03`, `CMP-04`, `CMP-07`, `CMP-08`, `CMP-10`, `CMP-11`, `CMP-15`, `CMP-17`.
**Primary approved asset:** `docs/high-fidelity/WF-05/HF-05_SEARCH_RESULTS.png` paired with `docs/high-fidelity/WF-05/HF-05_SEARCH_RESULTS.md` (sha256 b09a2829180d49a11d1ee19b2a027fd052ce51e1076a8982bbad6ac385047321).
**Canonical States covered:** `CB-09`, `CB-10`, `CB-11`, `CB-12`, `CB-13`. **Phase-7 dedicated A11y dossiers:** 4.

### WF-06 — Full Player / Queue

**Route:** `FullPlayer` · **Package:** `feature/player` · **Owning architecture / source of truth:** MediaSessionService sole authority: PlaybackGateway.snapshot + connectionState; MediaStore for validated metadata.
**Observable projection (lifecycle-aware):** revision,queueRevision,playbackState,queue,currentItem,currentIndex,positionMs,durationMs,canSeek,canSkipPrevious,canSkipNext,lastError typed.
**Actual user actions → transitions:** Play/Pause/Next/Previous/SeekBy(-10000)/SeekTo via PlaybackGateway.execute(CommandId); queue shows service queue; Driving→recorded origin WF-07; Mic→WF-08 overlay.
**Back precedence:** Active Voice cancel first; otherwise return Library/Search origin; service continues.
**Android/System handoff:** CB-22..CB-30 native MediaSession notification/lockscreen/media keys; focus/route handled by service.
**Approved analytics hook:** Domain-only typed PlayerError(errorCode,routeType), Voice CommandExecuted after ACK.
**Layout/dimensions:** Default inset16dp, grid4dp, intercomponent gap12dp, minimum48dp hit regions; specific component overrides below. Typography values from COMPOSE_TOKEN_MAPPING.md, height grows at fontScale=2.0. No fixed-height clipping.
**TalkBack/semantics:** Heading first; explicit stateDescription & noncolor error/selection; actions expose verb+object, disabled/pending truth and focus restoration. For each individual dedicated State consult `docs/high-fidelity/phase-7/app-visible/{StateID}.md`.
**Allowed components:** `CMP-02`, `CMP-03`, `CMP-05`, `CMP-06`, `CMP-07`, `CMP-08`, `CMP-09`, `CMP-11`, `CMP-13`, `CMP-15`, `CMP-17`, `CMP-19`, `CMP-20`, `CMP-21`.
**Primary approved asset:** `docs/high-fidelity/WF-06/HF-06_FULL_PLAYER_PLAYING.png` paired with `docs/high-fidelity/WF-06/HF-06_FULL_PLAYER_PLAYING.md` (sha256 a63c9341ef24537129ad3631765b9476af1408aec2b23c17d6e55272bce66f97).
**Canonical States covered:** `SYS-03`, `CB-14`, `CB-15`, `CB-16`, `CB-17`, `CB-18`, `CB-19`, `CB-20`, `CB-21`, `CB-22`, `CB-23`, `CB-24`, `CB-25`, `CB-26`, `CB-27`, `CB-28`, `CB-29`, `CB-30`, `CB-31`, `CB-32`, `CB-33`, `CD-01`, `CE-01`, `CE-02`, `CE-03`, `CE-04`, `CE-05`, `CE-06`, `CE-07`, `CE-08`, `CE-09`, `CE-10`, `CE-11`, `CE-12`, `CE-13`, `CE-14`, `UL-05`, `UL-06`, `UL-07`, `UL-08`, `UL-09`, `UL-10`, `UL-11`. **Phase-7 dedicated A11y dossiers:** 4.

### WF-07 — Driving Mode

**Route:** `Driving` · **Package:** `feature/driving` · **Owning architecture / source of truth:** RootNavigationStateHolder owns recorded origin; PlaybackGateway snapshot owns playback; VoiceSessionCoordinator owns Voice.
**Observable projection (lifecycle-aware):** Playing/Paused/No Track, connection pending, validated last local item, Mic capability.
**Actual user actions → transitions:** Explicit Enter only; Exit immediate; four large PlaybackCommands; optional PlayLast only revalidated; Mic→Voice overlay; no search/queue/settings.
**Back precedence:** Voice.cancel(UserBack|DrivingExit) first; immediate Exit→recorded origin.
**Android/System handoff:** CC-16/CE-07..10 Android audio route/focus; no fake system dialog, no Android Auto.
**Approved analytics hook:** Domain-only DrivingModeStarted(approved entrySource,bluetoothConnected bool), privacy gated.
**Layout/dimensions:** 20dp Driving margin; four transports ≥64dp, Mic ≥72dp, Exit ≥56dp; driving title 28sp/36sp and meta 18sp/26sp. Typography values from COMPOSE_TOKEN_MAPPING.md, height grows at fontScale=2.0. No fixed-height clipping.
**TalkBack/semantics:** Heading first; explicit stateDescription & noncolor error/selection; actions expose verb+object, disabled/pending truth and focus restoration. For each individual dedicated State consult `docs/high-fidelity/phase-7/app-visible/{StateID}.md`.
**Allowed components:** `CMP-01`, `CMP-02`, `CMP-05`, `CMP-06`, `CMP-07`, `CMP-08`, `CMP-15`, `CMP-19`.
**Primary approved asset:** `docs/high-fidelity/WF-07/HF-07_DRIVING_MODE_PLAYING.png` paired with `docs/high-fidelity/WF-07/HF-07_DRIVING_MODE_PLAYING.md` (sha256 f6a52c65fde0a5aacbccdcd8f5079874a74e0da6cd27daf80cfde440e296763d).
**Canonical States covered:** `CC-01`, `CC-02`, `CC-03`, `CC-04`, `CC-05`, `CC-06`, `CC-07`, `CC-08`, `CC-09`, `CC-10`, `CC-11`, `CC-12`, `CC-13`, `CC-15`, `CC-16`, `CC-17`, `CC-18`, `CD-01`, `CE-01`, `CE-02`, `CE-03`, `CE-04`, `CE-05`, `CE-06`, `CE-07`, `CE-08`, `CE-09`, `CE-10`, `CE-11`, `CE-12`, `CE-13`, `CE-14`. **Phase-7 dedicated A11y dossiers:** 4.

### WF-08 — Voice feedback Overlay (not destination)

**Route:** `OVERLAY ON WF-06/07 (NO NavHost route)` · **Package:** `voice + feature/player/driving` · **Owning architecture / source of truth:** VoiceSessionCoordinator.state is ONLY Voice authority; PlaybackGateway is command/ACK authority.
**Observable projection (lifecycle-aware):** Idle, Listening(attempt1/2), Processing, Confirming(token,options[1..2]), Executed after service ACK, Failed(canRetry).
**Actual user actions → transitions:** Mic explicit start; 6s timeout; one eligible Retry; confirm(token,optionIndex) from immutable options; Neither/Cancel→Manual; unsupported/not-found no approximate command; TTS ≤1 sentence.
**Back precedence:** Consume Back in overlay: coordinator.cancel(UserBack) releases capture/duck; return same Player/Driving origin; next Back follows origin.
**Android/System handoff:** SYS-02 Android mic permission; remote provider consent ONLY WF-10 outside Driving and blocked PH10-MIN-002.
**Approved analytics hook:** Domain-only VoiceButtonPressed, IntentDetected, IntentFailed, CommandExecuted, ManualCorrection; no audio/transcript/options/token.
**Layout/dimensions:** One overlay; choices min48dp/preferred56dp, gap8dp; 1–2 candidates; Cancel/Manual 48dp accessible. Typography values from COMPOSE_TOKEN_MAPPING.md, height grows at fontScale=2.0. No fixed-height clipping.
**TalkBack/semantics:** Heading first; explicit stateDescription & noncolor error/selection; actions expose verb+object, disabled/pending truth and focus restoration. For each individual dedicated State consult `docs/high-fidelity/phase-7/app-visible/{StateID}.md`.
**Allowed components:** `CMP-01`, `CMP-02`, `CMP-06`, `CMP-16`, `CMP-17`, `CMP-19`.
**Primary approved asset:** `docs/high-fidelity/WF-08/HF-08_VOICE_LISTENING.png` paired with `docs/high-fidelity/WF-08/HF-08_VOICE_LISTENING.md` (sha256 96769259e5b4655dfda41292b7ae71718ee0ae93055ec83081b1096432674da8).
**Canonical States covered:** `CC-14`, `CD-02`, `CD-03`, `CD-04`, `CD-05`, `CD-06`, `CD-07`, `CD-08`, `CD-09`, `CD-10`, `CD-11`, `CD-12`, `CD-13`, `CD-14`, `CD-15`, `CD-16`, `CD-17`, `CD-18`, `CD-19`, `CD-20`, `CD-21`, `CD-22`, `CD-24`. **Phase-7 dedicated A11y dossiers:** 18.

### WF-09 — Liked Songs (optional P2)

**Route:** `LikedSongs (P2 gate)` · **Package:** `feature/liked` · **Owning architecture / source of truth:** Approved local P2 Like repository owns confirmed Like; MediaStore validates; PlaybackGateway owns playback.
**Observable projection (lifecycle-aware):** Local liked list Empty/Ready, Like confirmed state or pending revert; service PlaybackSnapshot.
**Actual user actions → transitions:** Like/Unlike→local repository ACK; Play list→verified media and service ACK; Back→Library.
**Back precedence:** Back returns Library; P2 never blocks P0.
**Android/System handoff:** No online sync; permission loss→WF-03 native recheck.
**Approved analytics hook:** No typed Like analytics; no personal song collection.
**Layout/dimensions:** Default inset16dp, grid4dp, intercomponent gap12dp, minimum48dp hit regions; specific component overrides below. Typography values from COMPOSE_TOKEN_MAPPING.md, height grows at fontScale=2.0. No fixed-height clipping.
**TalkBack/semantics:** Heading first; explicit stateDescription & noncolor error/selection; actions expose verb+object, disabled/pending truth and focus restoration. For each individual dedicated State consult `docs/high-fidelity/phase-7/app-visible/{StateID}.md`.
**Allowed components:** `CMP-01`, `CMP-02`, `CMP-03`, `CMP-04`, `CMP-07`, `CMP-08`, `CMP-11`, `CMP-15`, `CMP-20`.
**Primary approved asset:** `docs/high-fidelity/WF-09/HF-09_LIKED_SONGS_READY.png` paired with `docs/high-fidelity/WF-09/HF-09_LIKED_SONGS_READY.md` (sha256 39610b81d84f514a52aa1c769974287404e8ced08b6910f09875f40372ca94c1).
**Canonical States covered:** `UL-01`, `UL-02`, `UL-03`, `UL-04`, `UL-05`, `UL-06`, `UL-07`, `UL-08`, `UL-09`, `UL-10`, `UL-11`, `UL-12`, `UL-13`, `UL-14`. **Phase-7 dedicated A11y dossiers:** 3.

### WF-10 — Settings / Privacy

**Route:** `Settings` · **Package:** `feature/settings` · **Owning architecture / source of truth:** PreferencesRepository DataStore (settings/consent/Bluetooth hint); native OS is current permission truth.
**Observable projection (lifecycle-aware):** AppPreferences(onboardingComplete,selectedBluetoothHint,cloudSpeechConsent) + actual OS/provider checks.
**Actual user actions → transitions:** Update setting→DataStore ACK; show privacy/consent OUTSIDE Driving; explicit clear→Confirm/Cancel; open Settings→OS native→recheck.
**Back precedence:** Clear confirmation Back=Cancel; otherwise previous route.
**Android/System handoff:** SYS-04 / CE-18 native Settings, SYS-03 notification, SYS-05 app-owned consent disclosure then provider gate.
**Approved analytics hook:** No arbitrary settings/consent/Bluetooth data event.
**Layout/dimensions:** Default inset16dp, grid4dp, intercomponent gap12dp, minimum48dp hit regions; specific component overrides below. Typography values from COMPOSE_TOKEN_MAPPING.md, height grows at fontScale=2.0. No fixed-height clipping.
**TalkBack/semantics:** Heading first; explicit stateDescription & noncolor error/selection; actions expose verb+object, disabled/pending truth and focus restoration. For each individual dedicated State consult `docs/high-fidelity/phase-7/app-visible/{StateID}.md`.
**Allowed components:** `CMP-01`, `CMP-02`, `CMP-03`, `CMP-04`, `CMP-15`, `CMP-17`, `CMP-18`, `CMP-21`, `CMP-23`.
**Primary approved asset:** `docs/high-fidelity/WF-10/HF-10_SETTINGS_DEFAULT.png` paired with `docs/high-fidelity/WF-10/HF-10_SETTINGS_DEFAULT.md` (sha256 e2c0579d2703fb2fdc10d396aa48a2a14194db7f5e041fc2206a53dda817661d).
**Canonical States covered:** `CD-22`, `CD-23`, `CE-15`, `CE-16`, `CE-17`, `CE-18`, `CE-19`, `CE-20`, `CE-21`, `CE-22`, `CE-23`, `CE-24`, `CE-25`, `CE-26`. **Phase-7 dedicated A11y dossiers:** 6.

## Exact 32 Low-Fi frame acceptance contracts

These are the original approved per-frame triggers, actions, Back, truth, recovery and OS handoff. They are normative for screen variants; the family summary above does not replace them.

| Frame | Entry | Existing action | Exit / transition | Back | Recovery | Authority / system handoff |
|---|---|---|---|---|---|---|
| `WF-01_SPLASH_LOADING` | Cold or warm app start. | Wait; no repeated action while routing is active. | Verified pending onboarding, permission recovery, or Home/Library state. | System Back exits the launch task; no in-app Back control. | A bounded failure changes to the migration-error frame with Retry. | Verified local migration/readiness and platform state.; System splash/status chrome remains Android-owned. |
| `WF-01_SPLASH_STARTUP_MIGRATION_ERROR` | Typed startup or local migration failure. | Retry startup once the user explicitly chooses Retry. | Successful Retry routes from freshly verified state. | System Back exits; Retry never traps focus. | Retry; repeated failure remains concise and non-destructive. | Local migration result; no fabricated Empty/Ready state.; No Android Settings handoff is implied. |
| `WF-02_ONBOARDING_SLIDE_1` | First run or explicit replay. | Continue to Slide 2. | Slide 2 / Skip Voice education. | Back exits or returns according to the owning first-run route without mutating completion. | Interrupted onboarding resumes the actual pending step. | Static approved product education.; No permission is requested on this slide. |
| `WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE` | Continue from Slide 1 or resume pending education. | Finish; optional Practice Voice is a separate explicit action. | Permission/library readiness path; Skip/Finish requests no microphone. | Back returns to Slide 1 without changing permission or playback. | Skip or provider decline returns to manual local control. | Exactly six supported intents; Voice provider/permission remains unrequested until explicit practice.; Microphone dialog may occur only after explicit Practice Voice. |
| `WF-03_PERMISSIONS_RATIONALE_REQUEST` | Media access is first required, or mic is explicitly tapped/practiced. | Continue to the Android-owned permission dialog. | Android dialog handoff; return routes to Granted/Denied. | Back/Not now returns to a usable manual surface. | Not now preserves manual controls; no coercive loop. | Android permission state is authoritative.; [ANDROID / SYSTEM] dialog is not an app screen. |
| `WF-03_PERMISSIONS_GRANTED` | Android reports Granted after request or Settings return. | Continue automatically or explicitly to the blocked app action. | Media grant → scan/readiness; mic grant → provider/capability check. | Back returns to the initiating surface. | If capability changes before use, re-enter the appropriate recovery state. | Fresh Android permission recheck.; Provider availability/cloud consent remains distinct from microphone grant. |
| `WF-03_PERMISSIONS_DENIED` | Android denies while another request remains permitted. | Try again; Not now remains equally reachable. | Try again → rationale/request; Not now → initiating manual surface. | Same as Not now; no automatic Settings launch. | Retry or manual fallback; local browse/playback remains available when mic is denied. | Fresh Android permission result.; No Android Settings handoff is required for requestable denial. |
| `WF-03_PERMISSIONS_PERMANENTLY_DENIED` | Android reports permanent denial or Settings-only recovery. | Open Settings; Not now preserves manual use. | Open Settings → Android-owned surface; return → three-way recheck. | Back/Not now returns to initiating manual surface. | Granted, requestable-denied, or still-permanent are separate return outcomes. | Android permission result after every return.; [ANDROID / SYSTEM] Settings is a handoff, never an app destination. |
| `WF-04_HOME_LIBRARY_SCANNING` | First scan, rescan, reconciliation, or verified rebuild. | Wait; Back/navigation remains available when safe. | Empty, Ready, or Recoverable Error. | Back leaves the surface without claiming scan cancellation unless supported. | Typed failure routes to Recoverable Error; prior verified state is preserved when possible. | MediaStore existence plus rebuildable local index progress.; Media permission loss hands off to WF-03; no fake Empty state. |
| `WF-04_HOME_LIBRARY_EMPTY` | Successful scan/reconciliation produces zero accessible tracks. | Rescan; permission recovery appears only when access is actually missing. | Ready after content appears, or permission recovery when revoked. | Normal app exit/back behavior; state remains truthful on return. | Rescan; no online catalog, recommendations or fabricated results. | MediaStore projection reconciled through the local index.; Android Settings is offered only for verified permission recovery. |
| `WF-04_HOME_LIBRARY_READY` | Verified non-empty index or returning route with valid current state. | Open a category, Search, or select a track. | Browse/Search/Full Player/Settings; mini-player opens Full Player. | Standard top-level navigation; Back does not stop service-owned playback. | Stale item → reconcile; revoked access → permissions; controller pending stays truthful. | Derived index supplies library; MediaSession supplies mini-player.; No system surface is represented as app navigation. |
| `WF-04_HOME_LIBRARY_RECOVERABLE_ERROR` | Typed recoverable index, storage or reconciliation failure. | Retry scan/reconciliation. | Scanning then Empty/Ready; permission issue routes to WF-03. | Back keeps any verified prior library/playback state intact. | Retry; no reinstall or destructive reset by default. | Typed repository/index failure plus fresh MediaStore/access check.; Permission recovery is a separate handoff, not embedded Settings UI. |
| `WF-05_SEARCH_RESULTS` | Local query returns one or more indexed matches. | Select a result to request service-owned playback. | Validated selection → Full Player; edit/clear remains in Search. | Back returns to Library with no playback mutation. | Stale result preserves query and reconciles; rejected playback keeps context. | Local derived-index query; selection revalidated against accessible media.; Typing and result lists are unavailable in active Driving Mode. |
| `WF-05_SEARCH_NO_RESULTS` | Local query completes with zero matches. | Edit query; Clear is secondary. | Updated query → Results/No Result/Error; Back → Library. | Preserves no mutation; returns to Library. | Preserve query and Browse access; subsequent error uses Search Error. | Completed local index query; no remote fallback.; No online catalog or network handoff. |
| `WF-05_SEARCH_ERROR` | Typed local query/index failure. | Retry; Edit and Clear remain available. | Retry → Results/No Result; Back → verified Library. | Returns to Library; unrelated playback is unchanged. | Retry/edit/clear; use UF-03 only if index truth is actually stale. | Local search repository failure; verified library remains authoritative.; No network fallback or Android surface. |
| `WF-06_FULL_PLAYER_PLAYING` | MediaSessionService acknowledges playback with a valid item. | Pause; Next/Previous/Seek/Queue remain available as supported. | Back keeps playback; Driving Mode requires explicit entry; Queue remains same authority. | Returns to initiating app surface without stopping playback. | Command/controller failure refreshes actual snapshot; corrupt item uses bounded recovery. | MediaSessionService snapshot: item, queue, state, position and supported actions.; Notification/lock/external controls are system-only synchronized surfaces. |
| `WF-06_FULL_PLAYER_PAUSED` | Service reports Paused after command, interruption or entry. | Play. | Acknowledged Play → Playing; Back preserves queue/position. | Returns without auto-playing. | Controller failure refreshes; interruption route rules may require safe manual resume. | MediaSessionService snapshot.; No optimistic resume after route/focus changes. |
| `WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY` | Prepare/advance/selection meets corrupt or inaccessible media. | Continue with bounded next-item validation; Library is always reachable. | Valid next → Playing/Paused; none → terminal Paused/Completed or Library. | Returns to Library/manual selection; no file deletion. | One bounded forward skip; permission/reindex recovery when appropriate. | Typed service failure plus MediaStore/index reconciliation.; No system dialog unless a separately verified permission recovery is needed. |
| `WF-07_DRIVING_MODE_PLAYING` | Explicit Driving Mode entry with a complete Playing snapshot. | Pause; four large controls, Mic and Exit remain fixed. | Exit is immediate; Mic opens an overlay, not a new destination. | Back behaves as Exit and returns to the prior non-driving surface. | Pending/rejected commands refresh actual state; route events hand off to UF-11. | Complete MediaSessionService snapshot and supported actions.; Calls/focus/routes are system events reflected through service truth. |
| `WF-07_DRIVING_MODE_PAUSED` | Complete snapshot reports Paused/Ended. | Play; Exit remains prominent. | Acknowledged Play → Playing; Exit → prior surface. | Back equals immediate Exit; never auto-resumes. | Unsafe route remains safely Paused; refresh/reconnect never surprises with audio. | MediaSessionService Paused snapshot.; Route/focus safety may disable Play with a textual reason. |
| `WF-07_DRIVING_MODE_NO_TRACK` | Complete snapshot has no current playable item. | Exit; Play last is shown only when a valid local reference exists. | Valid Play last → Playing/Paused; Exit → Home/Library. | Back equals Exit. | Failure remains No Track and routes to Library after Exit; no list/typing. | Service snapshot plus at most one revalidated MediaStore item.; No Android Auto, picker or system list is introduced. |
| `WF-08_VOICE_LISTENING` | Explicit microphone tap after permission/provider capability passes. | Cancel. | Result → Processing; failure → Failed/Retry; Cancel → initiating surface. | Back equals Cancel and releases capture/duck. | Timeout/no-match/provider failure preserves playback and exposes one Retry. | Voice coordinator transient session; playback remains service-owned.; Android mic indicator/dialog is system-owned; capture never starts on mode entry. |
| `WF-08_VOICE_PROCESSING` | STT result arrives and capture ends. | Cancel/manual fallback. | Certain → command pending/executed; ambiguous → Confirming; failure → Failed/Retry. | Back cancels stale callbacks and returns immediately. | Unknown/unsupported/not-found becomes safe failure, never approximation. | Restricted mapper supports exactly six intents; service not yet mutated.; Provider processing boundary follows approved disclosure/consent. |
| `WF-08_VOICE_CONFIRMING` | Deterministic resolver returns one low-confidence candidate or two artists. | Choose one of at most two large options; Neither/Cancel is reachable. | Presented selection → single command pending; reject/timeout → safe failure/manual. | Back equals Cancel; no execution. | One bounded question; never keyboard, long list or guessed execution. | Immutable session candidate set.; No new permission or system destination. |
| `WF-08_VOICE_EXECUTED` | Playback service acknowledges the submitted direct/confirmed command. | Dismiss/return to manual controls. | Returns to the initiating surface with actual refreshed state. | Back dismisses; it does not undo acknowledged playback. | TTS failure remains Executed; stale/rejected command uses Failed. | MediaSession refresh; optional TTS is non-authoritative.; No system surface is created. |
| `WF-08_VOICE_FAILED_RETRY` | Timeout/no-match/provider/language/network/audio or execution rejection. | Retry once; Manual/Cancel remains available. | Retry → second Listening; dismiss/manual → initiating surface; second failure is terminal. | Back returns immediately and exhausts no hidden action. | Exactly one explicit Retry; no auto-capture, coercive countdown or third attempt. | Typed Voice/session failure; actual playback remains service-owned.; Settings recovery is available only outside active Driving Mode. |
| `WF-09_LIKED_SONGS_EMPTY` | P2 entry load completes with zero accessible liked tracks. | Back to Library; no invented recommendations. | Back; future acknowledged Like can produce Ready. | Returns to initiating surface. | Load/reconcile failure exposes Retry/Back; missing media is reconciled without file mutation. | Local user-state repository projected through accessible MediaStore items.; No P0/P1 dependency; feature can be removed without placeholder. |
| `WF-09_LIKED_SONGS_READY` | P2 projection contains accessible liked tracks. | Play list or select a row; unlike preserves media files. | Playback acknowledgement → Full Player; Back → Library. | Returns without stopping unrelated playback. | Stale/rejected item refreshes projection; load failure preserves manual playback. | Local Like repository plus accessible MediaStore projection; service owns playback.; Feature remains optional/removable and never blocks P0/P1. |
| `WF-10_SETTINGS_DEFAULT` | Settings opened from non-driving navigation. | Open one settings section. | Section/update/handoff; Back returns to prior surface. | Returns without changing values or playback. | Load/update error retains previous value and exposes Retry where safe. | Preferences plus fresh platform/provider truth.; Android Settings handoff is explicit and never launched from active Driving Mode. |
| `WF-10_SETTINGS_BLUETOOTH_UPDATED` | P1 Bluetooth setup selected outside Driving Mode. | Save selection; Remove is explicit. | Acknowledged update → Settings; failure retains previous value. | Cancels unsaved changes. | Permission/unavailable routes to explicit outside-driving recovery; failure is textual. | Eligible platform devices plus locally stored selected preference.; Connection alone never launches Driving Mode; platform identity remains protected. |
| `WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION` | At least one non-preselected clear category is selected. | Confirm clear; Cancel is equally reachable. | Confirm → Clearing/Result; Cancel → selection or Settings. | Back equals Cancel; no deletion occurs. | No destructive default; partial/provider failure is reported truthfully later. | Selected local categories and documented provider boundaries.; Never claims remote deletion unless the provider confirms it. |
| `WF-10_SETTINGS_CLEARED_PRIVACY_STATUS` | Clear plan settles or Privacy details is opened after result. | Done; Retry only a failed boundary. | Done → Settings; Retry → failed boundary only. | Returns to Settings without repeating deletion. | Partial/failed results never claim success; protected media/playback remains unaffected. | Per-boundary repository/provider result; unresolved work remains explicit.; Remote/provider result is distinguished from local deletion. |

## Compose implementation skeleton (illustrative; runtime QA NOT executed)

```kotlin
@Composable fun FullPlayerRoute(vm: PlayerViewModel, onAction: (PlayerAction) -> Unit) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    // uiState is a projection of PlaybackGateway.snapshot, never a second queue/player.
    // User events reach use cases / PlaybackGateway.execute; UI waits for ACK.
}
// In root: if VoiceSessionState != Idle, render VoiceFeedbackOverlay over recorded
// Player/Driving origin; do NOT add a Voice NavHost destination.
// Back: first cancel VoiceSessionCoordinator (release mic/duck, discard stale)
// then Driving Exit -> recorded origin; Android OS surfaces handled externally.
```

**QA non-claim:** Runtime Compose 200% reflow/TalkBack/Media3/STT on physical devices has NOT been run; PH10-MIN-001 remains open.
