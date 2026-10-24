# Issue #9 — P05 Paused, No Track and safe error/recovery variants

**Date:** 2026-10-24 · **Gate:** `PASS_SCOPED_P05_RECOVERY_SOURCE_ONLY_NO_EXECUTION`  
**Issue:** https://github.com/latticeward-labs/music-copilot/issues/9 · **Working branch:** `feature/9-driving-mode-bluetooth-suggestion`  
**Source lock:** Reattached `MSM2(1)(1).rar`, verified SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`; its same 12 English PDFs underlie P00–P04 source authority. MVP defines Driving states Playing/Paused/No Track, FR-04 P0 four large controls, and a last-track fallback only when *actually available and verified*.

## Normative decisions checked against real GitHub files

- [Approved WF-07 Paused](../wireframes/app-states/WF-07_DRIVING_MODE_PAUSED.md) and [approved No Track](../wireframes/app-states/WF-07_DRIVING_MODE_NO_TRACK.md); source-checked explicit manual resume, safe Paused on bad route, exit to Library for No Track.
- [Actual HF-07 Paused frame](../high-fidelity/WF-07/frames/HF_FRAME_WF-07_DRIVING_MODE_PAUSED.md) and [actual No Track frame](../high-fidelity/WF-07/frames/HF_FRAME_WF-07_DRIVING_MODE_NO_TRACK.md); **not** nonexistent root `HF-07_DRIVING_MODE_PAUSED.md`/`HF-07_DRIVING_MODE_NO_TRACK.md`.
- [CC-06](../high-fidelity/WF-07/states/HF_STATE_CC-06.md), [CC-07](../high-fidelity/WF-07/states/HF_STATE_CC-07.md), [Phase-7 accessibility CC-06](../high-fidelity/phase-7/app-visible/CC-06.md) and [CC-07](../high-fidelity/phase-7/app-visible/CC-07.md), plus [CC-08 pending](../high-fidelity/state-variants/CC-08.md), [CC-09 rejected](../high-fidelity/state-variants/CC-09.md) and [CC-15 inaccessible](../high-fidelity/state-variants/CC-15.md).
- [System architecture](../architecture/RUNTIME_POLICIES.md): MediaSessionService authority; no UI-owned ExoPlayer, auto-resume, queues, routing, MediaStore cursor or Voice/provider; [P02 projected view](../../app/src/main/java/com/latticeward/musiccopilot/core/application/driving/DrivingPresentationPolicy.kt), [P04 Compose surface](../../app/src/main/java/com/latticeward/musiccopilot/feature/driving/DrivingModeScreen.kt).

## 1. Exact GitHub changes

| File | P05 actual source change | Scope |
|---|---|---|
| `app/src/main/java/com/latticeward/musiccopilot/core/application/driving/DrivingRecoveryPolicy.kt` (new) | Pure typed `DrivingRecoveryPolicy.project` for NoTrack, Paused, error, command pending/rejected, reconnect, stale-route and interruption; state from P02 and `AppError`/stable `AppErrorCodes` only. `DrivingRecoveryPresentation` controls variant flags; no service command. | No Media3, Android APIs, library lookup, Voice, Bluetooth, persistence, queue or playback mutation |
| `app/src/main/java/com/latticeward/musiccopilot/feature/driving/DrivingModeScreen.kt` | Dedicated CC-06 Paused and CC-07 No Track visual branches, service-first state heading, no invalid NoTrack controls, NoTrack exit/browse explanation, validated Playing/Paused metadata only; one clear status/error message; uses exact existing `i7_*` and `i8_*` safe interruption copy. | Retains P04 Playing design, four approved large transports for Playing/Paused, 72dp Mic and 56dp Exit. Disabled P06/P07 callbacks remain safe |
| `app/src/main/java/com/latticeward/musiccopilot/app/navigation/AppNavHost.kt` | Feeds current service `lastError` and existing shared `PlayerUiState.requestFailed` to Driving presentation; NoTrack **Exit and Back both route to Library**; in Playing/Paused they still restore the precisely recorded P03 Library/FullPlayer origin. | Existing one NavHost, entry gates, ViewModel origin and Exit callback; never invokes playback |
| `app/src/main/res/values/strings.xml` | Three localized English-first messages: exact approved “Exit to browse local music”, safe Paused manual-resume reminder, and unavailable-item fallback. | No private media filenames/transcripts or hardware identifiers |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue9Phase5DrivingRecoveryPolicyTest.kt` (new) | **21 pure JVM JUnit @Test methods AUTHORED, 0 RUN**. They exercise state-specific variants, route/focus errors, controller disconnection, stale lastError after real Playing, pending/reject prioritization and last-item absence. | No Gradle/JVM/instrumented test execution |

## 2. Truth and recovery decision table

| Inputs (P02 complete snapshot / shared PlayerUiState) | UI state and visible recovery | Existing transport UI | Exit |
|---|---|---|---|
| Ready + valid current + Playing | `PLAYING`, verified title/artist, no stale error after true Playing | Four approved large controls, **disabled until P06 binds ACK** | Recorded Library/FullPlayer origin |
| Ready + valid current + Paused or Ended, normal | `PAUSED`; explicit “Use Play when your audio output is safe.” **Never auto-play** | Four approved controls with Play label from P02, disabled until P06 | Recorded origin |
| Ready + queue empty, index absent, inconsistent current item or no valid current | `NO TRACK` + “No current track” + “Exit to browse local music”; no speculative title/artist/artwork | **Not rendered** in No Track (no invalid focus targets) | **Library** regardless previous FullPlayer origin; Back identical |
| No Track + actual failed `AppError.MediaItem` | No Track, one bounded “item unavailable” and browse message, no filename or private details | No controls; no unverified Play Last | Library |
| Controller Connecting / Disconnected / RecoverableError | Typed connection notice; no false Playing; reconnect never implicitly starts audio | P02 disables transport when not ready | Immediate Exit |
| Command pending | “Checking with playback service…”; no optimistic Playing or duplicate mutation | P02 disables controls; future P06 serializes actual execution | Exit always possible |
| Request rejected after service ACK/typed failure | Existing nonidentifying `i7_action_failed` message, unchanged source truth | No locally fabricated success; P06 owns ACK and retry | Immediate Exit |
| Playback Buffering or Error | Existing `i7_buffering` / `i7_playback_error` note; P02 remains Paused if valid track, all controls disabled | No route/player mutation | Exit always possible |
| Service media item missing/corrupt/unplayable | Existing neutral `i7_unavailable_help` message while preserving truthful selection; if missing current, No Track | Disabled by P02 when invalid | Exit to origin or Library |
| `AppError.Interruption` with `ROUTE_INVALIDATED` | Existing `i8_output_changed`, “Check ... before resuming”; **no auto-play on reconnect** | P06 existing service route guard still decides manual Play | Exit |
| `FOCUS_NOT_RECOVERED` | Existing `i8_focus_not_recovered`; safely remain Paused | Playback Service is sole route/focus authority | Exit |
| Other call/route safe paused | Existing `i8_audio_temporarily_paused`, no Android call details | Service determines readiness; never synthetic resumption | Exit |
| Service reports actual `PLAYING` after a previous MediaItem/route fault | **Suppress stale paused/error guidance** unless there is an actual new failed command; snapshot is authoritative | Four visible controls, P06 pending | Origin |

**Priority:** controller unavailable/pending truth > No Track terminal state > acknowledged Playing truth > route/focus typed error > MediaItem/Playback error > Buffering > command rejected > normal Paused. This avoids stale “paused safely” text after true service Playing. The recovery projector is **pure**, and the screen never initiates retry, resume, permission dialog or search.

## 3. Explicit last-track policy: not fabricated

The attached MVP and WF-07 No Track allow an **optional** “Play last” only when a trusted retained local reference exists and is revalidated against current MediaStore/library permission. The existing P05 `PlaybackSnapshot` contains `currentItem` and `queue`, but **no typed, verified `lastPlayedRef` contract**; the current P03/P04 implementation has no equivalent authorized last-item provider.

**Decision for P05 P0:** `verifiedLastItemAvailable=false`, render **no Play Last action**, no attempt to manufacture a queue from `Room`/DataStore or a previously clicked result, no implicit Play or forced Library picker. This follows the *optional* design rule and preserves service authority/privacy. If a future owner specifically requires Play Last, it needs a separate authoritative reference + permission-revalidation/ACK contract and cannot be claimed implemented in P05.

## 4. Accessibility and privacy contract (P08 runtime validation remains)

- CC-06: heading and immediate Exit, `PAUSED` written before title/artist, Previous/Play/Next/SeekBack, separate Mic. Real target sizes still use `MusicSize.drivingControlMinDp` 64dp, Mic 72dp, Exit 56dp, and `safeDrawingPadding`.
- CC-07: heading, immediate Exit, written `NO TRACK`, explicit explanation, no invisible invalid/fake controls; Mic remains visible but disabled until the actual P07 Voice seam. No long lists, keyboard, settings, Bluetooth picker, Android Auto, motion-detection controls or extra navigation route.
- Current Compose `Column.verticalScroll` and 4→2×2 transport reflow at ≥150% font are source-authored. **No device a11y/TalkBack order/focus/contrast/200% screenshots were tested.** P08 authors further semantics/UI test source and P13 owner runs the approved phone checklist.
- `MediaRef`/title/artist only displayed with current service selection and permission; recovery errors use stable sanitized categories and static localized copy. No names, file paths, MAC addresses, call IDs, transcripts, Voice scores, location or raw exceptions logged or sent.

## 5. Source-only evidence and honest status

- [x] Original RAR SHA and official English MVP/PRD / ADR source contract matched.
- [x] Correct HF-07 Paused/NoTrack source paths fetched; approved frame + CC-06/CC-07 accessibility boundaries reconciled.
- [x] Pure `DrivingRecoveryPolicy` and source-owned message enum committed.
- [x] Three distinct UI state variants now use P02 complete playback state and P05 typed recovery; no invalid NoTrack media/transport.
- [x] NoTrack Exit/Back returns Library, other states return P03 recorded origin.
- [x] 21 JUnit source cases authored, **zero executed**.
- [x] No fake last-track, permission recovery dialog, direct retry, optimistic resume or second Player.
- [ ] **Unrun:** Kotlin compile, Gradle/JUnit, lint, Compose/instrumented tests, emulator/phone/vehicle and GitHub Actions.
- [ ] **Unverified:** original AC01–AC14 product acceptance, M01–M46 owner phone cases, five technical groups, Android OEM/road route safety.
- [ ] **Pending:** P06 PlaybackGateway ACK and real enabled controls, P07 Voice/#10, P08 accessibility source, P09–P11 optional P1 BT, P12 audit and P13 **one integrated build-only APK**.

**P05 status: PASS_SCOPED_SOURCE_ONLY; P06 NEXT.** No PR, merge or Issue closure. Prior P02 (21) + P03 (12) authored JUnit methods also remain **NOT_RUN**; with P05's 21, **54 total authored across P02/P03/P05**, none executed.
