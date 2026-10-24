# Issue #8 — P09 Voice Cancellation on Call/Focus/Route Invalidation

**Date:** 2026-10-23 · **Issue:** [#8](https://github.com/latticeward-labs/music-copilot/issues/8) (OPEN)  
**Branch:** `feature/8-audio-focus-route-recovery` · **Target:** `main`  
**Disposition:** **PASS_SOURCE_ONLY_NOT_COMPILED_OR_TESTED**. **Next: P10**.  
**Hard owner rule:** No P09 compilation, Gradle, Kotlin/JVM/Android tests, emulator, handset, APK or GitHub Actions run. The sole cumulative APK is P13. Original AC01–AC12 remain UNVERIFIED; M01–M45 and T01–T15 remain NOT_RUN.

## 1. Actual source and architectural authority

- Current owner-uploaded `MSM2(1)(1).rar` rechecked: SHA-256 **`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`**, same verified set of 12 English source PDFs as P00–P08. Original [ADR-0004](../adr/0004-replaceable-voice-provider.md) requires replaceable STT/TTS, scoped push-to-talk, bounded listening and retry, no raw voice history, provider-failure isolation and manual fallback. Original architecture/technical docs specify provider and TTS cancellation to prevent stale/overlapping work during interruptions. [ADR-0002](../adr/0002-service-owned-media3-playback.md) keeps the ONE Player/Session and OS Audio Focus authority in the service.
- [UF-11](../user-flows/UF-11_INTERRUPTION_AUDIO_ROUTE.md), [Runtime Policies §8](../architecture/RUNTIME_POLICIES.md), [P02 deterministic interruption reducer](ISSUE_8_PHASE2_DETERMINISTIC_INTERRUPTION_POLICY.md), [P04 focus safety](ISSUE_8_PHASE4_CALL_FOCUS_DUCK_RECOVERY_AUDIT.md), [P05 route epoch](ISSUE_8_PHASE5_HEADSET_USB_NOISY_ROUTE_SAFETY_AUDIT.md), [P07 races](ISSUE_8_PHASE7_EVENT_ORDERING_AND_RACE_CONTAINMENT_AUDIT.md) and [P08 snapshot](ISSUE_8_PHASE8_UNIFIED_SESSION_NOTIFICATION_UI_AUDIT.md) govern.
- Real current Android code already contains **one** `VoiceSessionCoordinator` interface, `VoiceCancelReason.CallOrFocusInvalidated` and `VoiceCancelReason.RouteInvalidated`; `DefaultVoiceSessionCoordinator.cancel()` already invalidates its session generation and stops `SpeechToTextEngine` / `TextToSpeechEngine`. `ApplicationBindings.voiceSessionCoordinator` is ONE instance; `MusicCopilotApplication` constructs it exactly once before the MediaSessionService starts. Previously the P02 `CANCEL_VOICE_FOR_FOCUS` / `CANCEL_VOICE_FOR_ROUTE` effects were **model-only**, never dispatched to that real coordinator.
- The active Android manifest declares `.app.MusicCopilotApplication`. The service continues to use `PlaybackResolverHost` for its other dependencies. No app UI/Activity creates a second Voice engine, MediaSession, Player, queue or focus manager.

## 2. Production source changes (not just documentation)

| Path | Concrete P09 source change |
|---|---|
| [`playback/api/PlaybackVoiceInterruptionHost.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/api/PlaybackVoiceInterruptionHost.kt) | **New** typed playback-only `PlaybackVoiceInterruptionReason { FOCUS_INVALIDATED, ROUTE_INVALIDATED }` and `PlaybackVoiceInterruptionHost`. The playback service knows only this neutral interface; it never imports Voice STT/TTS or their implementation package. No global static singleton, Service-owned coordinator or dependency cycle. |
| [`app/MusicCopilotApplication.kt`](../../app/src/main/java/com/latticeward/musiccopilot/app/MusicCopilotApplication.kt) | Composition root implements the typed host, maps focus to `VoiceCancelReason.CallOrFocusInvalidated`, route to `VoiceCancelReason.RouteInvalidated` and **synchronously calls the already bound `bindings.voiceSessionCoordinator.cancel`**. The same singleton owned by `ApplicationBindings`; no new instance. |
| [`playback/interruption/InterruptionPolicy.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/interruption/InterruptionPolicy.kt) | Existing P02 transient, terminal, duck (pure model) and physical route-loss events already emit typed `CANCEL_VOICE_FOR_FOCUS/ROUTE` effects; P09 additionally emits focus cancellation for `FocusAutoResumeRejected` so a newer Voice generation begun during suppression is invalidated on a denied automatic return. P02 duplicate/stale reducer envelope filtering remains in effect. |
| [`playback/service/Media3FocusObservationAdapter.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/Media3FocusObservationAdapter.kt) | At **accepted reducer transition receipt** on the service Main callback looper, dispatches each existing `CANCEL_VOICE_FOR_FOCUS/ROUTE` effect to typed host callback. Duplicate focus and noisy/removal events lacking an accepted cancellation effect do not emit extra cancellation. Dispatch is synchronous and non-blocking with respect to the MediaStore command queue; a provider RuntimeException is isolated from Player.Listener, never allowed to crash the service or corrupt playback. |
| [`playback/service/MusicPlaybackService.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt) | Supplies the actual application-root callback to the existing `Media3FocusObservationAdapter`. Route removal `handlePersonalOutputDisconnected` and Media3 focus-related events already reach P02 through the P03–P07 adapter; therefore P09 requires **no new OS Audio Focus listener, TelephonyManager, BLUETOOTH_CONNECT, READ_PHONE_STATE, wake word or TTS-focus ownership**. |
| [`voice/session/DefaultVoiceSessionCoordinator.kt`](../../app/src/main/java/com/latticeward/musiccopilot/voice/session/DefaultVoiceSessionCoordinator.kt) | Strengthens real `cancel(reason)`: clears session generation and state to `Idle` **before** provider callbacks/teardown; independently attempts `stt.cancel(id)` and `tts.stop(id)`, even if one provider fails, with no transcript/route/device logging. Its existing `isCurrent(id, childJob, attempt)`, `ensureActive()`, recognizer cancellation and TTS stop still prevent late work from becoming active. `confirm()` remains an intentional Issue #10/#11 stub and cannot execute a stale confirmation. |

## 3. Real execution path and event matrix (source behavior only)

```text
Android/Media3 focus lost OR headset/USB/Bluetooth removed/noisy
         |
Existing ONE MusicPlaybackService (Main) + P05/P07 safety/epoch
         |
Existing Media3FocusObservationAdapter.accept(InterruptionEvent)
         |
P02 InterruptionReducer -> accepted typed effects (no duplicate event)
         |
CANCEL_VOICE_FOR_FOCUS             CANCEL_VOICE_FOR_ROUTE
         |                                      |
Typed PlaybackVoiceInterruptionHost (app composition root)
         |
Existing ONE ApplicationBindings.voiceSessionCoordinator
         |
DefaultVoiceSessionCoordinator.cancel(reason) (service Main)
         |
1) revoke current VoiceSessionId/generation & childJob; publish Idle
2) STT provider cancel/destroy; separately TTS stop
3) late callbacks cannot restore old Listening/Confirming/Executed
         |
Playback safety uses SAME Media3 ExoPlayer and SAME one serialized worker
```

| Interruption scenario | Voice policy effect | What remains to verify |
|---|---|---|
| Media3 exposes transient focus suppression | P02 `FocusLostTransient` emits focus cancellation; app coordinator revokes Listening/Processing/feedback | P13 owner manual M44–M45 and deferred technical T10 |
| Media3 changes `playWhenReady=false` with `AUDIO_FOCUS_LOSS` | P02 `FocusLostPermanent` emits focus cancellation; no claim this was definitely a telephone call | Physical call timing and manufacturer behavior |
| Media3 focus auto-resume is denied after suppression | P02 `FocusAutoResumeRejected` also emits focus cancellation, including a newly begun Voice generation | P13 call/focus overlap QA |
| Headset, USB or BT personal output removed | P05 urgent service event -> P02 `RouteDisconnected` -> route cancellation; **works at source level even with empty/idle Player** | Actual noisy+removed callback ordering and no speaker leak |
| Duplicate noisy + AudioDeviceCallback on one route episode | P05 gate and P02 reducer coalesce; no second effect for same episode | P12 technical source/P13 device scenarios |
| Voice while feedback TTS says short response | Coordinator cancels current job and stops matching session TTS | Real offline TTS engine/Android OEM proof |
| Late STT completion after focus/route invalidation | Session ID/child generation cleared before provider cancel; stale `isCurrent` fails | In-flight Voice-to-Playback command receipt race in P13 |
| User manually pauses music with no call/route/focus event | Existing manual Pause policy; does NOT fabricate `CallOrFocusInvalidated` or cancel Voice indiscriminately | P13 UX scenarios |
| Focus restored, Bluetooth reconnect only | **No Voice restart**, no STT microphone activation, no TTS replay, no new Player Play | P13 safety proof |
| Actual Voice `Confirming` state | `cancel` invalidates it and clears options/token, but current implementation's `confirm` returns stale-session always | Full confirmation lifecycle is future Issue #10/#11, not in P09 |

## 4. Important limitations and boundaries

1. **No Telephony/phone-call detection.** Media3's public suppression / `AUDIO_FOCUS_LOSS` reason cannot reliably distinguish calls from other apps or see a phone call when a service is idle and Media3 receives no focus callback. Therefore this source change **does not claim cancellation in every real call**; proving full call coverage requires owner/OEM handset evidence and possibly a separately reviewed architecture/policy if actual call observation is deemed essential.
2. **No app-side voice duck manager yet.** Current `DefaultVoiceSessionCoordinator` has **no independent `Player.volume` duck transaction**, so P09 does not restore a volume value or issue an automatic Player Play. OS/Media3 keep Audio Focus, suppression/ducking and playback permission. Adding dedicated app Voice ducking must be reviewed in the separately owned Voice issue; it may never bypass the P02 four-guard recovery.
3. **No guaranteed prevention of a brand-new user-started Voice session while focus is still lost.** P09 cancels already-active generations when a focus/route state transition becomes observable. A future request-start admission gate or UI state policy may be necessary for full Issue #10. Repeated initial focus-loss event is intentionally idempotent.
4. **No change to full Voice product scope:** no wake word, command-artist resolution, cloud STT, new speech provider/permission, six-intent expansion, recordings, hidden microphone start or voice telemetry. `VoiceSessionCoordinator.confirm` is currently a stale-session-only stub, not an implemented confirmation workflow.
5. **Provider exception isolation:** cancellation clears session generation and tries both provider teardowns. Exceptions from Android STT or TTS cancellation do not crash Media3. Actual device resource release must still be verified; P10 owns any privacy-safe diagnostics.
6. **No physical evidence:** No phone, headset, call, Bluetooth, Pixel/Samsung/Xiaomi or parked-car test has been run. No Android 10/12/14+/15 permission/API behavior verified. All 12 original AC remain UNVERIFIED until real evidence.

## 5. Owner handoff and completion checklist

- [x] Verified exact uploaded source archive and relevant ADR-0004/architecture/technical voice cancellation policy.
- [x] Located actual one app-scoped Voice coordinator, STT and TTS adapter cancellation contracts.
- [x] Added typed playback-to-app cancellation boundary with no service-to-Voice implementation dependency.
- [x] Consumed P02 accepted `CANCEL_VOICE_FOR_FOCUS/ROUTE` effects in the existing Media3 callback path.
- [x] Synchronously invalidated Voice session generation at the same Main receipt without a second work queue.
- [x] Made STT and TTS cancellation independent and idempotent in app-scoped coordinator.
- [x] Denied focus auto-return also cancels Voice; focus regain or BT reconnect never restarts Voice.
- [x] Retained privacy/permission/Media3/one-Player and Manual Fallback boundaries.
- [x] No Gradle, source compilation, tests, emulator, APK, Actions, PR/merge or Issue closure.
- [ ] P10 sanitized Interruption/Voice diagnostics (no transcript, raw audio or hardware identifiers).
- [ ] P11 OEM/Android constraints and correct call scope without unauthorised permissions.
- [ ] P12 write *only* STT/TTS cancellation, duplicate events, late callback, restoration and route/head-unit test source.
- [ ] P13 one cumulative `:app:assembleDebug` plus owner manual M44–M45. Technical T10 and all other tests remain NOT_RUN absent actual evidence/owner authorization.

**Conclusion: P09 source implementation complete, not compilation or device validated. NEXT P10.**
