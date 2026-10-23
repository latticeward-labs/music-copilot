# Issue #8 — P02 Deterministic Interruption State Machine and Injectable Event Seams

**Date:** 2026-10-23  
**P02 disposition:** **PASS_SOURCE_ONLY / CODE AUTHORED; NOT BUILT, COMPILED OR TESTED**  
**Issue status:** #8 OPEN; AC01–AC12 UNVERIFIED. **Next: P03 — Media3 observation and service adapter**.  
**Source of truth:** Owner's verified \`MSM2(1)(1).rar\` SHA-256 \`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8\` (12 English PDFs), [P00 audit](ISSUE_8_PHASE0_SOURCE_AND_READINESS_AUDIT.md), [P01 contract](ISSUE_8_PHASE1_GITHUB_EXECUTION_CONTRACT.md), [Issue #16 Runtime §8](../architecture/RUNTIME_POLICIES.md), [UF-11](../user-flows/UF-11_INTERRUPTION_AUDIO_ROUTE.md) and [TS-04](../user-flows/evidence/TS-04_AUDIO_FOCUS_POLICY.md).

## 1. Exact implementation on existing branch

- **NEW** [\`app/src/main/java/com/latticeward/musiccopilot/playback/interruption/InterruptionPolicy.kt\`](../../app/src/main/java/com/latticeward/musiccopilot/playback/interruption/InterruptionPolicy.kt), pure Kotlin model + policy reducer, no Android/Media3 import.
- **REMOVED** the no-behavior \`playback/interruption/PackageBoundary.kt\` placeholder, superseded by the actual Kotlin package.
- The existing [\`MusicPlaybackService.kt\`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt), [\`PlaybackInterruptionGate.kt\`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/PlaybackInterruptionGate.kt), [\`ServiceAudioRouteMonitor.kt\`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/ServiceAudioRouteMonitor.kt), \`PlaybackCommandProcessor\`, ExoPlayer and MediaSession are **not changed by P02**. P03/P04/P05/P07 must integrate this pure reducer with the one real existing service. No parallel playback authority and no competing Audio Focus request.
- Source file contains \`InterruptionState\`, \`InterruptionEvent\`/Envelope, \`InterruptionReducer.reduce\`, \`ResumeEligibility\`, \`InterruptionTransition\`, pure effects, and \`InterruptionClock\`, \`InterruptionSequenceSource\`, \`InterruptionEventReceiver\` injectable adapter seams.

## 2. Normalized state and ownership

| State/port | Concrete model | Contract |
|---|---|---|
| Playback truth (last observed) | \`InterruptionPlayback.PLAYING/PAUSED/STOPPED\` | Only an **authoritative service Player** observation can establish real Playing. Policy effects do not fabricate audible Playing. |
| Focus | \`FocusCondition.AVAILABLE/TRANSIENT_LOST/PERMANENT_LOST/DUCKED\` | Names are **normalized logical events**. Android may not expose every transition to app code with Media3 as focus owner. P03 must prove the adapter's real signals. |
| Route | \`routeGeneration: Long\` + \`routeValid\` | Route loss invalidates current generation even when Player has no item; duplicate loss while already unavailable is idempotent. RouteValidated alone never autoplays. |
| Manual intent | \`manualRevision\`, \`requiresExplicitPlay\` | Manual Pause/Stop permanently defeats the old transient resume candidate. Only a verified user-origin Play with valid current route and available focus may release the explicit-play latch. |
| Transient resume | \`pendingResume: ResumeEligibility(focusEpisode,routeGeneration,manualRevision)\` | Captured exclusively on first eligible transient interruption. Never reconstructed from a later Player snapshot, cold restore, a notification or route reconnection. |
| Event ordering | \`InterruptionEnvelope(sequence,observedAtElapsedMs,event)\` | Service injects strictly increasing **receipt sequence**; duplicate/out-of-order \`sequence <= lastEventSequence\` is discarded without side effects. Elapsed timestamp is nonnegative metadata only; clock time cannot override sequence or resume guards. |
| Effects | \`PAUSE_SERVICE_PLAYER\`, \`RESUME_SERVICE_PLAYER\`, \`ALLOW_VERIFIED_USER_PLAY\`, \`REJECT_UNSAFE_PLAY\`, \`CANCEL_VOICE_FOR_FOCUS\`, \`CANCEL_VOICE_FOR_ROUTE\` | Values are proposed side effects for the **future P03+ adapter**, not actions executed in P02. Cancellation must precede unsafe Voice callback effects; no second Player, session, audio-focus owner, volume controller or telephony listener. |

A state reducer and service observation are **different** concepts: policy state tracks eligibility and normalized events; actual service playback, UI, MediaSession and notifications still use the existing authoritative Media3 snapshot. Because P02 does not wire the service, none of the new effects currently operate on real Android playback.

## 3. Transition decision table

| Current state/verified incoming event | Policy state transition | Emitted effect(s) | Resume permitted? |
|---|---|---|---|
| Genuine transient focus loss while previously Playing and route valid | Capture episode, route generation and manual revision; \`focus=TRANSIENT_LOST\`, \`playback=PAUSED\` | Pause; cancel active Voice via focus reason | **Candidate only**; not yet a Play |
| Transient loss while already Paused or explicit-pause latch set | \`focus=TRANSIENT_LOST\`, **no candidate** | Safety pause/cancel on first loss only | No |
| Duplicate transient loss | Preserve existing candidate/state except accepted sequence metadata | None | No additional/rearmed resume |
| Matching focus gain after eligible transient loss, same route epoch, unchanged manual revision, valid route and no explicit-play latch | Clear candidate; \`focus=AVAILABLE\` | **One** conditional \`RESUME_SERVICE_PLAYER\` | Yes, subject to P03's real focus/route/Player verification |
| Stale gain token, route changed, manual Pause/Stop after loss, route invalid or no candidate | Never create a new candidate; stale gain does not change focus; matching disqualified gain merely clears interrupted focus state | None | No |
| Permanent focus loss / denied focus | Safe Paused, clear candidate, set \`requiresExplicitPlay\` | Pause; cancel active Voice | Never automatically |
| OS duck requested while focus was available | \`focus=DUCKED\`, no candidate, do not alter Player volume | Cancel active Voice if focus invalidates it | No new playback; Media3/OS own attenuation |
| Gain after Duck | \`focus=AVAILABLE\`, clear candidate | None | No previously Paused track starts |
| Relevant wired/USB/Bluetooth route disconnect, first event | Invalidate route, **increment route epoch independent of Player/queue state**, set explicit-play latch, clear candidate, Safe Paused | Pause once; cancel Voice via route reason | No |
| Duplicate noisy+removed notification for same lost route | No second route bump or repeated unsafe pause | None | No |
| Route revalidated/reconnected after loss | Mark \`routeValid=true\` only, retain manual/route-safety latch | None | No |
| User Pause or Stop, including during interruption | Increment manual revision, clear candidate, set explicit-play latch; \`PAUSED\`/\`STOPPED\` | Pause | No until verified fresh Play |
| Verified user Play with correct current route generation, valid route and available focus | Clear explicit-play latch and candidate; keep last real observed playback status | \`ALLOW_VERIFIED_USER_PLAY\` (existing command processor executes after source checks) | Yes, by **explicit** user action, not auto-resume |
| Ambiguous media-key/AVRCP/head-unit Play, stale route stamp or unavailable focus | State unchanged | \`REJECT_UNSAFE_PLAY\` | No |
| Service observes unsolicited real Player Playing during invalid route, focus transient/permanent loss or explicit latch | Preserve policy's Safe Paused projection | \`PAUSE_SERVICE_PLAYER\` again, preventing unsafe audio | No |
| Equal/older envelope sequence or invalid negative elapsed timestamp | No change | None | No |

**Priority:** (1) physical route loss/unsafe output, (2) manual Pause/Stop, (3) permanent focus loss, (4) transient focus pause/possible guarded gain, (5) duck only through OS/Media3, (6) verified fresh Play. Priority is implemented by latches/episode/route sequence, **not** by depending on wall-clock timing or automatic replay. An earlier event cannot be "retroactively cancelled" when the adapter has already physically delivered it; P07 must serialize incoming receipt, stamp pending IO at command receipt, and safely preempt playback on route loss.

## 4. Four guarded transient auto-resume predicates

An automatic \`RESUME_SERVICE_PLAYER\` can be emitted only if:

1. \`FocusLostTransient.wasPlayingBeforeLoss\` was a **real, pre-loss** service snapshot (true).
2. \`FocusRestored.focusEpisode\` matches the **same** transient loss episode and the focus state was transiently lost.
3. \`routeValid == true\` and \`pendingResume.routeGeneration == state.routeGeneration\`.
4. \`manualRevision\` matches the candidate and \`requiresExplicitPlay == false\` (no later manual Pause/Stop, route loss or permanent focus loss).

Additional safety invariants: only the interruption that paused the previously Playing service may hold a candidate; the candidate is consumed at most once; a stale focus gain/duplicate noisy/removal can never emit resume; cold restore is Paused-only; route reconnect is never user intent; false Android call detection is prohibited. Matching \`FocusRestored\` without candidate cannot emit Resume.

**Important P03 prerequisite:** With \`ExoPlayer.setAudioAttributes(..., true)\`, the **real** Android Audio Focus requester is internal to Media3. \`Player.isPlaying\`, \`playWhenReady\` and suppression/change reasons do not necessarily distinguish an incoming call from all other interruptions, nor expose a 1:1 public gain/loss callback. The future adapter MUST map only **observable and defensible** signals to these events and preserve the pre-loss sample; otherwise it must fail closed and report the observation limitation, not invent a focus episode or install a competing \`AudioManager.requestAudioFocus\` listener. The pure reducer does NOT independently detect focus or telephony.

## 5. Injectable event/time/generation seams

- \`InterruptionEventReceiver.dispatch(InterruptionEnvelope)\`: a pure callback surface, suitable for service-owned adapter implementation and fake event streams in future QA.
- \`InterruptionSequenceSource.nextSequence()\`: a caller-supplied strictly increasing sequence, allowing deterministic race/replay inputs independent of wall clock. Sequence must be unique across connected event sources in **the same** service.
- \`InterruptionClock.elapsedRealtimeMs()\`: caller-supplied monotonic observation time; time records context only and can never grant Play.
- \`FocusLostTransient.wasPlayingBeforeLoss\`: pre-event truth supplied only by service adapter, not a secondary Player or fabricated UI boolean.
- \`FocusRestored.focusEpisode\`: must refer to a verified currently active focus episode; an unknown token is not proof of focus gain.
- \`PlayRequested(origin,expectedRouteGeneration)\`: origin is \`VERIFIED_USER\` only if deliberate user intent can be demonstrated. \`AMBIGUOUS_EXTERNAL\` is always rejected, notably unresolved AVRCP reconnect autoplay.
- All current state/effect classes are Kotlin-only and test-injectable. **No test files authored or run in P02**; P12 may author tests for later explicit authorization; T01–T15 remain NOT_RUN.

## 6. Static safety review (source inspection, **not** compiled or tested)

| Review item | P02 source conclusion | Open integration risk |
|---|---|---|
| Pure Kotlin & package isolation | No Android/media imports or external side effects. | P03 must implement one safe service adapter. |
| Old Player/Queue compatibility | Existing service/queue/session implementation untouched. | Adapter must preserve #7 API and serialized command pathway. |
| Idempotence | Repeated focus transient/permanent loss and duplicate route removal suppress duplicate effects; stale/equal sequence rejected. | P07 must coordinate noisy/device-removed signals and asynchronous IO events. |
| Manual pause priority | Clears candidate immediately; focus gain cannot rearm it. | Command receipt vs delayed MediaStore callback still needs P07 fencing. |
| Safe route while Player is idle | Pure RouteDisconnected increments generation even with empty/idle Player. | P05/P07 must remove service's current conditional that omits route bump on empty Player. |
| Bluetooth reconnection | RouteValidated cannot auto-resume; ambiguous remote Play rejected. | P06 must determine what media-key provenance is actually knowable. |
| Focus integrity | Four guards + episode token checked; no manual focus request. | P03/P04 must demonstrate reliable event sampling or conservative fallback. |
| Voice safety | Pure CANCEL_VOICE effect models app-scoped coordinator handoff. | P09 must wire real cancellation/release ducking. |
| Privacy | No call identity, device identifiers, media URIs, transcripts or telemetry in policy state. | P10 must define/publish sanitized interruption errors. |
| Overflow | Epoch counters saturate rather than wrap; eligibility never re-arms on saturated focus episode. | P07 must treat exhausted generations as terminal/fail-closed on an extraordinarily long-lived service. |
| QA provenance | No Gradle, Kotlin compilation, JVM unit tests, Android instrumentation, emulator, device, APK or Actions used in P02. | Build and user phone QA only in P13; technical proof deferred. |

## 7. Remaining phase handoff and known gaps

**P03 — required before runtime functionality:** build compatible Media3 event normalization/observation on the real \`MusicPlaybackService\`, account for Media3 Audio Focus internals, route signal delivery and control lifecycle, apply a single reducer state on Main, publish authoritative snapshot and ensure correct pre-loss Playing capture. Never create another AudioManager focus request or claim a call-specific callback without actual source. Keep errors visible when safe.

**P04:** focus/call/duck selection and exact Media3 semantics; no audible call overlap and preserve manual Pause; test on real phone only at P13.

**P05/P07:** urgent route event must invalidate epoch even when service Player is idle during a pending ReplaceQueue; event priority and pending command receipt epoch; rapid duplicates/out-of-order integration; preserved queue/recovery.

**P06:** deliberate user Play vs ambiguous reconnect/head-unit auto-Play, conservative safety policy when Android cannot prove origin.

**P08/P09/P10/P11:** consistent MediaSession/UI projection, Voice cancellation, stable privacy-safe errors and Android API/OEM compatibility.

**P12/P13:** author future technical test source without execution; build exactly one cumulative final APK in P13 and send to owner with M01–M45 checklist. Report owner results with exact APK and source SHA. Pixel/Samsung/Xiaomi/three vehicles are NOT_RUN/BLOCKED until independently exercised.

## 8. Completion and evidence gate

- [x] Verified P00/P01 source/authority and unchanged #7 baseline.
- [x] Created real pure Kotlin reducer with explicit state/events/transition effects.
- [x] Captured pre-loss resume provenance, four guarded predicates, old episode and route/manual invalidation.
- [x] Added injectable event, sequence, monotonic-clock and route generation seams.
- [x] Added source-controlled transition table and no-dual-focus/system/Voice ownership contract.
- [x] Preserved existing Media3 service and original strict Issue #8 ACs as UNVERIFIED.
- [x] Did not compile/build/test/dispatch Actions or produce an APK.
- [ ] P03 real Media3 adapter/integration and runtime behavior.
- [ ] P12 technical test source and eventual technical execution (requires separate approval).
- [ ] P13 integrated build/owner phone/OEM results.

**Decision: P02 SOURCE IMPLEMENTATION COMPLETE (NO QA PASS). P03 NEXT.**
