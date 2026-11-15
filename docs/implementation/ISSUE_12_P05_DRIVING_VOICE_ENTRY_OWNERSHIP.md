# Issue #12 — P05: Authoritative Driving and PTT Entry Instrumentation

**Date:** 2026-11-15  
**Status:** SOURCE_IMPLEMENTED_UNCOMPILED_NOT_RUN  
**Issue:** https://github.com/latticeward-labs/music-copilot/issues/12  
**Branch:** feature/12-privacy-safe-telemetry-measurement  
**Verified archive:** exact owner-uploaded MSM2(1)(1).rar RAR5; SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, all 12 English PDFs directly inspected.

## 1. Source contract and interpretation

**English PRD v1 EN Clarified:** FR-07 manual Driving entry (optional selected-Bluetooth suggestion), FR-05 push-to-talk, 6 bounded intents, privacy-first Measurement and no voice recording by default (pp.4–10).

**English MVP v1 EN:** Driving Mode requires user action; microphone entry is explicit and permission-aware (pp.3–7). The approved telemetry table (printed p.10) names `driving_mode_started(entry_source,bt_connected)` for entry count/path and `voice_button_pressed(session_id,playback_state)` for Voice start rate; the optional `session_id` is **not exported** because its representation is not approved by Privacy.

**English System Architecture v1 EN**, printed pp.16–17: one Voice owner / Media3 service; typed event -> allowlist privacy gate -> nonblocking Quality adapter, not a second player. **ADR-0002:** playback state is service-owned. **ADR-0003:** consent, minimized local/quality data, no cloud profile. **ADR-0004:** on-device PTT without continuous recording; permission callback never starts speech. **ADR-0005:** deterministic six-intent mapping; a Voice recognition failure is not a successful command. **ADR-0006:** no backend/account/streaming. The attached first-readiness user flow separates microphone permission, Voice availability and independent consent.

**Implementation source**: Issue #9 root navigation, Issue #10 one app-owned Voice coordinator and permission guard, Issue #11 artist confirmation, Issue #12 P03 eight-event schema/P04 independent diagnostics consent. The original product sources prescribe event *names and coarse attributes*, **not** these Kotlin callback names or a session-ID algorithm; the following is the narrow engineering implementation.

## 2. Event ownership and exact accepted boundaries

| v1 event | Source-of-truth | When ONE event is *eligible* | Never eligible |
|---|---|---|---|
| `driving_mode_started` | Existing Activity-scoped `DrivingNavigationViewModel` / pure `DrivingNavigationPolicy.enter` | A user action accepted as one transition from inactive to active; `enterFrom(...)` returns `true`, subsequently passed through P04 consent/Quality | Compose recomposition, repeated tap while active, denied origin/route/permission, raw reconnect notification, declined/pending Bluetooth offer, rotation restoring the same active ViewModel, app bootstrap, settings, background replay |
| `voice_button_pressed` | Existing **single** app-owned `DefaultVoiceSessionCoordinator`, exposed via `ObservedVoicePttCoordinator` | Exact `AppResult.Success` of explicit `start(Player/Driving)` or permitted explicit `retry()`; callback runs only *after* admission, then P04 consent/Quality | Mere Mic button rendering, failed/rapid duplicate start, denied/absent RECORD_AUDIO, unavailable local provider, rationale Android permission dialog/result, app resume, network/cloud, artist `confirm`/`neither`/`cancelConfirmation`, failed/stale Retry |

**Accepted Retry semantics:** P02 defines each explicitly authorized Retry as a **new Voice attempt** within the same logical session. Thus one successful initial start plus one successful Retry creates **two** attempted-PTT events; a rapid duplicate start, an unsuccessful retry, or an artist confirmation creates **zero additional** events. P06 will handle one terminal outcome per attempt, not a fake second intent.

**Dedup without ID export:** Driving's existing in-memory `active` transition and the Voice owner's existing synchronized `VoiceSessionId` / generation / retry attempt state are already its authoritative admission guards. We add no secondary persistent/device/user ID or independent recognizer. The internal Voice owner claims a successful admission before its observer fires; duplicate/rejected requests yield `Failure`, never callbacks. Rapid legitimate exit then new explicit entry is a new Driving session, to be counted separately once. P08 will later own session completion and any recovery marker; P05 does NOT invent a completion.

## 3. Concrete wiring (narrow source changes only)

| File | Change |
|---|---|
| `app/navigation/DrivingNavigationViewModel.kt` | `enterFrom()` now returns `Boolean` indicating accepted inactive->active transition; pure policy and navigation state ownership unchanged |
| `app/navigation/AppNavHost.kt` | One `enterDrivingOnce` lambda used by manual Library, Full Player and the **explicitly accepted** Bluetooth suggestion; invokes a coarse callback only if `enterFrom` returns true. No `LaunchedEffect` or `onResume` event instrumentation. Mapping: Library -> `home`, Full Player -> `player`. Even Bluetooth-offer acceptance stays the actual origin, not an unapproved `bluetooth` entry_source value |
| `app/MainActivity.kt` | Binds the existing application-scoped callback once to AppNavHost; does not own a session or recorder |
| `core/application/voice/ObservedVoicePttCoordinator.kt` | New interface decorator delegates all Voice behavior to the SAME `DefaultVoiceSessionCoordinator`, intercepts `start` and `retry` result only; reports success once, swallows optional diagnostic callback failures, never changes Voice result or stage |
| `app/di/ApplicationBindings.kt` | Binds one wrapper to existing Voice owner; captures only current service-authoritative `PlaybackSnapshot.playbackState` as closed `TelemetryPlaybackState`, and accepted Driving source + coarse Bluetooth output. Checks P04 authorization **before forming an event** and schedules best-effort work off the UI/Voice path; P04 publisher re-checks on capture and gated NoOp sink re-checks before delivery |
| `app/platform/audio/CoarseBluetoothAudioOutput.kt` | New one-shot Android `AudioManager.getDevices(GET_DEVICES_OUTPUTS)` lookup at *accepted Driving entry only*. Reads only `AudioDeviceInfo.type` to identify connected Bluetooth SCO/A2DP/BLE output. Does not read device ID, name, MAC, bond state, selected token, location or call details; no permission/network/provider added |
| `app/src/test/.../Issue12Phase5EntryTelemetryTest.kt` | Ten **source-authored, NOT_RUN** acceptance/rejection/dedup/adapter boundary test cases |

**App-level Quality path (not a blocking inline provider):** accepted action → immutable, closed coarse event → P04 diagnostic consent check (currently OFF) → app scope async best-effort publisher → P03 schema gate → P04 gated `NoOpTelemetrySink` (still OFF). The callbacks are non-authoritative. If any callback throws, the action remains accepted, and Playback/Voice does not change.

## 4. Bluetooth truth and completeness caveat

The `bt_connected` property in English MVP is a coarse **Boolean**. Rather than treating lack of a *selected-device* connection as proof that the phone has no Bluetooth audio, P05 observes the platform's current **connected output device types** at the single accepted-entry moment.

- `true`: the OS output list includes a Bluetooth A2DP, SCO, BLE headset or BLE speaker type;
- `false`: a successful output list observation contains no such Bluetooth type;
- `null/UNKNOWN`: context or OS audio query was unavailable. **The entry event is dropped**, rather than sending a fictitious `false` into an approved binary-only field. This can produce a legitimate missing event; P10 must report a data-quality/coverage limitation and never silently treat it as no Driving use.

An output type check does **not** prove which physical vehicle/head unit the user used, whether playback currently routes through that device or whether a Bluetooth suggestion was shown. It is not source evidence of a vehicle model, device identity or a successful user session. No application-level Bluetooth connection changes, automatic driving entry, or pairing work are introduced.

## 5. Explicit rejected and accepted lifecycle matrix (implementation expectations)

| Sequence | Driving event | PTT event | Reason |
|---|---|---|---|
| Library explicit tap, eligible → inactive to active | 1 (if approved+OS evidence) | 0 | One accepted entry |
| Library rapid duplicate tap while already Driving | 0 extra | 0 | Existing active navigation guard |
| Player explicit eligible tap | 1 (if approved+OS evidence) | 0 | Entry source `player` |
| Accepted BT invitation on foreground Library/Player | 1 (if approved+OS evidence) | 0 | Real click + claim, not reconnect alone |
| Bluetooth reconnection, invitation displayed but ignored/not now | 0 | 0 | No user-approved route transition |
| Compose recomposition, Activity rotation retaining VM | 0 | 0 | No accepted new navigation/Voice action |
| Bootstrap, permission denial, invalid route, background suggestion | 0 | 0 | Ineligible |
| Driving exit, then a **new** explicit eligible entry | 1 new | 0 | Legitimate separate session |
| Player Mic explicit tap; OS grant + local provider ready; Coordinator start succeeds | 0 | 1 (if approved) | One admitted Voice attempt |
| Driving Mic explicit tap; same guards, start succeeds | 0 | 1 (if approved) | Same owner, origin-specific admission |
| Repeated Mic tap while Listening/Processing/Confirming | 0 | 0 extra | Coordinator rejects second active start |
| First Mic when permission missing → rationale/OS permission result | 0 | 0 | No Voice `start` |
| Explicit Retry accepted from `Failed(canRetry)` | 0 | 1 new (if approved) | One new admitted attempt |
| Failed/unavailable/stale Retry | 0 | 0 | No success |
| Artist one-step confirmation / Neither / cancellation | 0 | 0 | No new PTT |
| P04 consent not asked/declined/revoked, store Loading/Failed, or pending approvals | 0 collected | 0 collected | No event object formed; app function still works |
| Query of Android Bluetooth output types fails | 0 collected | unaffected | Unknown `bt_connected` cannot be faked |
| Quality sink/adapter failure | No effect on entry | No effect on PTT | Best-effort only |

Count entries and accepted attempts **only when informed diagnostic collection is actually approved/consented**. The table expresses source-expected events conditional on approval; P05 does not claim their live observation.

## 6. Safety and nonclaims

**Unchanged:** Media3 player/service, playback gateway, queue and focus/route logic; on-device Moonshine/Silero, VAD, deterministic intent mapping, artist resolver and confirmation; OS permission UX; cloud STT remains disabled; user-owned MediaStore files; P04 DataStore; P03 eight-event outbound allowlist and NoOp provider.

**Blocked/not yet ready:** approved diagnostic notice, retention TTL, vendor/legal review, independent Settings consent UI (P12), local diagnostic store (P09), truth of terminal intent outcomes and manual correction (P06), Player/crash error reporting (P07), Driving session completion (P08), measurement engine (P10) and dashboard (P11). Inherited #80 lab voice-quality failure and #82 artist ambiguity still require separate QA.

- [x] The P05 code for one accepted Driving entry and one accepted PTT/Retry callback has been committed to the Issue #12 feature branch.
- [x] No newly exported event family, session ID or personal media field.
- [x] Created a narrowly scoped, nonidentifying coarse Bluetooth output observation; unsupported OS read drops rather than invents data.
- [x] Authored ten QA scenarios for duplicate taps, denied admission, manual/BT entry, Retry, privacy and source wiring.
- [ ] **No compilation, Kotlin/JVM test, UI/integration test, GitHub Actions, Gradle, emulator, phone validation or APK build performed.**
- [ ] No real collection/sink/privacy approval; **P05 is source-implemented, not functionally verified**.
- [ ] All 14 Issue #12 acceptance criteria remain NOT_VERIFIED. No PR/merge/Issue close.

**Next:** P06 terminal Voice detection/failure/execution and explicitly attributable manual correction, with service-authoritative ACK. Preserve P05 single-owner admission boundaries.
