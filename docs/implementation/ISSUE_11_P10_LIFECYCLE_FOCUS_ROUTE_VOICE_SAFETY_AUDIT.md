# Issue #11 — P10: Voice Lifecycle, Ducking, Call/Audio Focus, Route, Permission, Late ACK

**Date:** 2026-11-14. **Branch:** `feature/11-play-artist-one-step-ambiguity`.  
**Disposition:** `SOURCE_IMPLEMENTED_UNCOMPILED_NOT_RUN`. **No Gradle, build, APK, Android/JVM test execution, Lint, GitHub Actions, emulator or device test.** No acceptance PASS, PR, merge or Issue close.

## 1. Source-of-truth documents and original scope

The exact owner-provided RAR5 `MSM2(1)(1).rar` was opened, SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, with 12 English PDFs. P10 is grounded in:

- **ADR-0002** pp. 2–4: **one service-owned** Media3 ExoPlayer and MediaSession; Active audio must survive UI destruction/lock screen, while calls, Audio Focus and personal-output disconnection must not leak playback to speaker or cause unintended auto-resume; Activity must not own or stop Player.
- **ADR-0004** pp. 2–5: explicit PTT only, ≤6s capture, one retry, duck only while listening, optional single-sentence TTS, cancel stale provider work and preserve manual playback when STT/TTS fails.
- **Music Copilot System Architecture MVP EN** pp. 9, 15: P0 focus/call/BT interruption paths, service-owned state, structured concurrency, TTS cancellation and no optimistic Media3 acknowledgement.
- **Music Copilot PRD v1 EN Clarified** pp. 6, 8–9: safe one-touch recovery, P0 background continuity and route/call safety, Voice latency target.
- **Music Copilot MVP v1 EN** pp. 4–5: return quickly to manual controls without corrupting current audio/queue.
- Current `docs/implementation/ISSUE_11_PHASED_EXECUTION_PLAN_FA.md` **P10**, Issue #11, all previously committed P02–P09 code.

**P10 architectural interpretation:** explicit Media3/BT event hooks already existed from approved #8–#10; P10 strengthens live foreground/microphone permission and stale continuation admission. **It does not create a second listener, AudioFocus manager, ExoPlayer, service, receiver, wake-word path or process-restore command.**

## 2. Actual changes in feature branch

| GitHub source path | P10 modifications |
|---|---|
| `core/application/voice/VoiceLifecycleSafetyPolicy.kt` | Add pure `canContinueWithFreshOsGrant(uiStarted,recordAudioGranted)`, true only when BOTH conditions hold. `mustCancelOnSafetyBoundary` excludes Idle and includes Listening, Processing, Confirming, Executed and Failed. Existing `restoredStateAfterRecreation() = Idle` and explicit-Mic-from-Idle stay unchanged. |
| `app/di/ApplicationBindings.kt` | Production Voice `canBeginSession` now evaluates **fresh Android OS `RECORD_AUDIO`** from `ContextCompat.checkSelfPermission` and `uiVoiceStarted` on **every gate invocation**; never a cached permission, old ActivityResult, DataStore or background notification. Add `onForegroundMicrophonePermissionObserved(false)` → cancel the one application-scoped generation with `PermissionLost`. Existing `onUiStopped`: invalidate voice **before** detaching only the Activity controller, keep real Media3 playback. |
| `app/MainActivity.kt` | Route OS microphone-revoke `onResume` handling through the application binding (single Voice owner) rather than directly accessing the coordinator; original fresh permission read, UI state, and no automatic PTT start on resume remain. |
| `voice/session/DefaultVoiceSessionCoordinator.kt` | Add **lock-scoped `isCurrentAndAuthorizedLocked(session,generation,attempt)`**. At each STT/result/artist lookup/P06 callback/ACK/TTS continuation, check current identity + active generation **and** foreground/fresh OS grant. When an owned live generation loses authority, call `invalidateLocked()` synchronously (Idle + stop STT/TTS + cancel prompt/expiry/child job + release UUID duck lease). Apply guard before Processing, Dispatch claim, P06 direct/confirmed replay hooks, Confirming emission, choice/Neither/Cancel, timeout, Executed publication and final optional speech. This is an extra safety gate; P07 one-use token/Media3 ACK validation remains authoritative. |
| `Issue11Phase10VoiceLifecyclePolicyTest.kt` | 23 source-authored JVM methods (pure lifecycle truth table, Activity recreation/Bundle rules, OS permission root, callback wiring, Media3 focus/BT precedence, controller detachment, Duck/TTS cancellation). **NOT_RUN**. |
| `Issue11Phase10VoiceLifecycleRaceTest.kt` | 15 authored synthetic race methods (slow STT after revoke, direct/confirmed late ACK, no TTS/queue mutation on cancel, route/focus loss, explicit foreground re-entry, token reuse, process recreation and duplicate events). **NOT_RUN**. |

Only these source files and the new P10 MD+JSON reports are P10 changes. P07 token/one-step algorithm, P03 Room, P04 scorer, P05 confidence threshold, P06 MediaStore playback, P08 Compose and P09 text/TTS policy are preserved. The original Issue #10 five-command mapper/dispatcher and all underlying Media3 service/focus/route logic are not changed in P10.

## 3. Runtime interruption and cancellation matrix

| Scenario | Application Voice generation | Duck and TTS | Media3 service/queue |
|---|---|---|---|
| Fresh explicit Mic in visible Activity, OS grant valid | Start one session from Idle | Service ACKs voice-duck before STT; provider listens ≤6s | Same service-owned Player |
| Mic permission absent at PTT tap | Fail closed (no start) | No capture; no duck | Unchanged |
| OS Mic grant lost **during STT** (even if no onResume yet) | After next suspension, runtime gate invalidates old generation | cancel STT, release UUID duck | No Voice dispatch |
| App Home, screen off/Activity `onStop`, route change Player↔Driving, Back/Exit | Existing Active Voice canceled synchronously; Idle | cancel prompt+TTS+timeout, release duck before controller detachment | **Do not stop Player**; active foreground playback may continue |
| Device rotation/recreation | Old Activity onStop cancels; new Voice owner/state only via app graph | No token or transcript restored from Bundle | One Media3 service retained if music genuinely active |
| Process death/relaunch | New application-scoped coordinator starts **Idle**; no persisted pending token, no automatic microphone or command replay | No restored recording/TTS | Service/queue restoration governed by Media3/FR-08 only (independent of Voice) |
| Incoming call or Media3 Audio Focus invalidated | Existing focus observer → typed service host → `CallOrFocusInvalidated` → cancel current Voice | Revoke service duck first, then cancel STT/TTS | Existing service focus reducer decides pause/duck/recovery, not Voice |
| Wired/headset/BT output lost | Service route monitor/noisy observer → root `RouteInvalidated` cancel | Revoke duck immediately; never inject audio to speaker | Existing `PlaybackInterruptionGate` latches Safe Paused and requires explicit verified Play; reconnect alone not auto-resume |
| BT output available/reconnect | No new voice capture or background choice | No TTS/duck resurrection | Existing quarantine blocks unproven auto-Play |
| Pending confirmation expires/Neither/Cancel | One-shot token invalidated, no second question | Stop prompt and cancel timeout, no queue command | Unchanged |
| Direct or selected `PLAY_ARTIST` awaiting P06 ACK then interruption | Generation invalidated; later callback cannot publish Executed/Playing or retry | Stop in-flight TTS; release duck | A command **already accepted by Media3** cannot be retroactively unsent; service safety guards must protect actual route and queued command |
| TTS callback arrives after stopping UI or new session | Canceled generation cannot publish state | Existing Android TTS utterance ID/stop guard drops old callback | Unchanged |

### Proof and limitations

- `PlaybackVoiceInterruptionHost` is already implemented by `MusicCopilotApplication`; `MusicPlaybackService` already calls it on Media3 focus and AudioDeviceCallback route changes. The service's `ServiceVoiceDuckLease` owns token-scoped release and a bounded 22s expiry as last resort. The Activity controller's `detachUiController()` waits briefly on the pending un-duck receipt before releasing the binding; player stays owned by MediaSessionService.
- **Bluetooth availability is not evidence of the active output.** Existing service route-safety quarantine persists even after reconnection until a verified user Play. No new telephony permission or call-identity receiver is used; focus interruption is not asserted to reveal incoming-call identity.
- **No guarantee to reverse already-accepted commands.** P10 drops stale app acknowledgements, but Media3 may have already changed its queue. This is a residual physical-device P13 test gate, not an unqualified guarantee of no playback mutation.
- **No automatic resumed Voice:** `onResume`, `onStart`, permission callback, BT connect or UI recreation cannot call `coordinator.start()` without a fresh user gesture.
- **Optional TTS independent:** after ACK can only speak if the same live generation still owns foreground/mic authorization; a missing TTS engine cannot undo an already accepted service result. AudioFocus and PTT duck ownership remain service-side, not a secondary playback owner.

## 4. Authored regression evidence and P13 manual quality gates

Two new source-only fixture suites: **23 + 15 = 38 `@Test` methods authored, zero executed, zero PASS.** The test fakes purposefully simulate cancellation-resistant STT/ACK providers, but do not themselves prove on-device lifecycle, Bluetooth, focus, real phone calls, TTS audio or physical speaker route.

**P13 phone checklist additions (NOT_RUN):**
1. Start artist recognition on **Player** and **Driving**; Home/Back/Exit before STT, after STT and during Confirming: **no microphone left recording, no surprise TTS and no automatic command on returning**.
2. While `Play songs by [artist]` is confirming, revoke Mic permission in Android Settings and return: old token rejected; manual player still usable; regrant never auto-records.
3. Rotate/recreate the Activity mid-Confirming, or force-stop/kill/relaunch as a separate disruptive case: **no restored token, no automatic second queue change**.
4. On real headset/AirPods/Bluetooth route disconnect, inspect **physical output**: immediate safe pause on unexpected route loss, **no speaker leakage** or BT reconnect auto-resume. Then explicitly Play on valid route.
5. During a real call/Audio Focus loss, verify recording and prompt terminate; Media3 music follows approved pause/recovery policy with no voice overlap or crash.
6. Race positive artist option with route loss/Back/permission revocation while service work is pending; verify no duplicate dispatch and no **late voice “Playing”**. Check eventual actual service queue/state separately.
7. Compare Duck volume before/after cancellation; no persistent attenuation. Physical TTS failures must not block manual music.
8. Verify all six deterministic voice intents still operate as designed; previous Issue #10 STT reliability Issue #80 remains open and is **not** fixed by P10.

**Acceptance:** original #11 AC still NOT_VERIFIED; M01–M48 / T01–T10 / L01–L06 `NOT_RUN`. No builds, CI, lint or tests before P13. Real P95 and privacy/telemetry evidence remain pending.

## 5. Remaining work

P11: strict privacy telemetry allowlist. P12: final integration source audit + owner-phone QA matrix. **P13: the one authorized cumulative APK build and owner-run tests**, followed by GitHub results and eventual merge/close only after owner confirmation. Issue #11 stays OPEN; independent P0 [Issue #80](https://github.com/latticeward-labs/music-copilot/issues/80) remains OPEN (previous owner-reported #10 voice lab 24/50=48% vs 90% target).
