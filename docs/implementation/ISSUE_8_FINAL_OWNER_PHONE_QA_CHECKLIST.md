# Issue #8 — Final Owner Handset Checklist (ONE P13 APK)

**Status:** P12 INTEGRATION PREPARED / ALL CASES NOT_RUN · **Updated:** 2026-10-24 · **Source:** [P00–P13 plan](ISSUE_8_PHASED_EXECUTION_PLAN.md) + Issue #8 AC01–AC12 + UF-11 / SYS-07..09.

## Important owner rules

- **DO NOT run this checklist before final P13.** All phases P00–P12 are coding/doc-only: no build or test. Only one integrated P13 debug APK from ALL implemented phases is an eligible candidate.
- Play one or two **disposable local** valid audio files in a known order. Enable media permission if asked. Avoid sharing track titles, paths, Bluetooth names/MAC, call numbers/contacts, voice recordings or sensitive logs.
- For each ID, send **PASS / FAIL / BLOCKED / NOT_RUN / N/A** and a brief explanation for FAIL/BLOCKED/N/A. Mark actual observation, never infer a PASS. Hardware scenarios not possible on the user's one phone are BLOCKED/NOT_RUN, not PASS.
- Bluetooth/headset tests require equipment only **if available**. Phone-call tests require a real call or a second cooperating person, but no second Android test phone is mandatory; unsupported hardware remains deferred. Run car/head-unit checks **parked only**, never while driving.
- When Android/OEM does not offer an action (for example manual Pause during the actual call), mark the specific case BLOCKED/N/A with note; do not claim equivalent testing. For focus interruptions from third-party apps, note app/interrupt type without personal data.
- If you hear playback on the **phone speaker after losing Bluetooth/wired headphones without explicitly playing**, STOP, record **FAIL** for the relevant case and do not repeat at loud volume.

## Final M01–M45, all from the same APK

| ID | Owner's action on final APK | Required expected observation |
|---|---|---|
| **M01** | Open app with a local track ready but do not press Play. | No surprise autoplay, truthful paused/idle state. |
| **M02** | Play valid local song normally on phone speaker for 15s. | Audible valid song; timer/Player reflect service state. |
| **M03** | Manual Pause, then wait 10s. | Audio stops and stays paused; progress does not continue as Playing. |
| **M04** | Manual Play again. | Resumes intended song at sensible position; exactly one audio stream. |
| **M05** | Lock/unlock, background/reopen while normal playback active. | Correct continued playback and consistent notification, lock screen (if shown), mini/full Player. |
| **M06** | Receive normal incoming phone call while song is playing. | Music pauses or attenuates per approved call policy without audible overlap, crash or wrong output. |
| **M07** | Answer incoming call; converse briefly (no call recording). | Call is intelligible; Music Copilot does not inappropriately play over call. |
| **M08** | End call started while music had been Playing, with output route still valid. | Guarded resume behavior only if focus restored, route unchanged and no manual Pause; else stays paused. |
| **M09** | Receive call while Music Copilot was already manually Paused; end call. | Does **not** start music after call. |
| **M10** | If available, manually Pause during temporary call interruption, then end call. | Explicit Pause wins; no automatic resume. If OS does not allow manual command, BLOCKED. |
| **M11** | Play music, start another audio app that takes lasting audio focus. | Music Copilot stops/pauses per permanent focus policy; no uncontrolled simultaneous playback. |
| **M12** | Close competing app / regain focus without manual Play after M11. | Permanent focus loss does not cause surprise autoplay. |
| **M13** | Use a short competing notification/audio prompt (transient focus) while playing. | Interruption handled sensibly (system-managed duck or temporary pause); no glitch/crash/overlap when prohibited. |
| **M14** | Let brief transient prompt finish without any intervening manual command or route change. | Resume allowed **only** with four valid guards (prior Playing, focus returned, route unchanged, no manual Pause). |
| **M15** | During a transient interruption manually Pause if possible; let prompt finish. | Remains paused, despite subsequent focus gain. |
| **M16** | During a temporary focus interruption change output route (or disconnect headset), then let focus return. | No stale auto-resume; stays safe paused. |
| **M17** | Play with navigation-style short spoken guidance if available. | Compatible duck/pause behavior; guidance audible, no permanent stuck volume or duplicated music; record exact observed behavior. |
| **M18** | Plug in available wired headphones while paused, then explicitly Play. | Music goes to correct headset after user command, not unexpectedly to speaker. |
| **M19** | Unplug wired headphones **during playback**. | Immediate safe pause, **no unrequested phone-speaker sound**. |
| **M20** | Leave wired headphones unplugged for ~10s. | Remains paused; no delayed resumed music on speaker. |
| **M21** | Reconnect wired headphones after M19. | No autoplay from reconnect alone. |
| **M22** | With headphones reconnected, tap app Play deliberately. | Same intended queue resumes only on user request and valid route/focus. |
| **M23** | Repeat wired unplug/replug twice while playing/paused, if hardware permits. | Each loss safely pauses; no stale route callback restarts playback. |
| **M24** | With available USB headphones/headset, play then unplug. | Same immediate safe pause/no speaker leakage as wired headset. BLOCKED if none. |
| **M25** | If wired/USB accessory has media button, Pause/Play/Next. | Actual single service queue responds, not duplicate media audio or stale UI. |
| **M26** | Connect Bluetooth headphones or speaker, leave app paused. | Connection alone does **not** start music. |
| **M27** | Explicitly Play through connected Bluetooth, confirm output audibly. | Music audibly goes to connected Bluetooth route; UI/notification agree. |
| **M28** | While Bluetooth music plays, power off/disconnect the device. | Immediate safe pause, no unintended phone-speaker leakage. |
| **M29** | Keep Bluetooth disconnected 10s, then reconnect. | No automatic resume just because device reconnected. |
| **M30** | Toggle Bluetooth off/on at Android OS level after M27. | Music pauses safely on loss; Bluetooth return alone does not restart. |
| **M31** | After reconnect, explicitly press Play on in-app UI. | Resumes selected song on valid route without wrong-speaker burst. |
| **M32** | After reconnect and a clearly intentional user action, use headset Play/Pause/Next media buttons. | Service queue changes correctly; unsolicited auto-Play from reconnect is not accepted as deliberate intent when detectable. |
| **M33** | Pause from Bluetooth media button while playback active. | App, Android notification and lock-screen (if shown) all show Paused and audio stops. |
| **M34** | Play from Android media notification while safely connected. | Service and app reflect exactly one resumed playback. |
| **M35** | Pause from notification while music active, then disconnect Bluetooth. | Manual Pause is preserved; no late focus/route auto-resume. |
| **M36** | During playing -> disconnect output -> reopen Full Player/MiniPlayer. | Both accurately show paused/interrupted state, not false Playing. |
| **M37** | Background app during Bluetooth playback, lock screen and disconnect headset. | Background service safely pauses; notification/system surfaces synchronize. |
| **M38** | Reopen app after disconnect/reconnect (do not tap Play). | Shows current genuine queue/position and Paused; no app-recreation autoplay. |
| **M39** | Trigger safely observable interrupted route/focus error, then open playback status if available. | Short non-sensitive recovery message/action; no personal device/media identifiers exposed. |
| **M40** | Connect/disconnect Bluetooth rapidly three times (at safe low volume) while paused. | No crash, phantom playback, double player or incorrect output. |
| **M41** | Trigger brief focus interruption during Bluetooth disconnect. | Highest-priority unsafe route wins; no focus regain plays through speaker. |
| **M42** | Press manual Pause just before/during Bluetooth disconnect and reconnect. | Manual intent preserved despite event ordering/races. |
| **M43** | While recovering route, press Next/Previous once (if queue has 2+ tracks) and inspect. | Exactly one command result, correct item/index, still safe-paused until deliberate Play. |
| **M44** | If the current MVP exposes active Voice listening/confirmation: start it, then simulate focus/call interruption. | Voice cancels or transitions to safe state; playback never wrongly restarts; if Voice not available mark BLOCKED (belongs to #10). |
| **M45** | If current MVP exposes Voice: start it over Bluetooth, disconnect BT during listening/ducking. | Voice cancels, no stuck ducking/unexpected speaker audio, manual recovery; BLOCKED if Voice feature unavailable. |

## Optional device/OEM & car follow-up — not part of the one-phone PASS assertion

Repeat the relevant M cases on **Pixel/stock Android**, **Samsung One UI**, and **Xiaomi HyperOS/MIUI**, each with physical device/API/OEM build recorded, when equipment becomes available. Record each model separately; owner's Honor x7d Android 15 covers only their actual device. For later **three distinct parked vehicle/head-unit** combos, repeat M26–M43 and the call/focus subset (M06–M17) where safe. Android Auto integration is **not** part of MVP; normal Bluetooth head-unit controls may be.

## P12 authored regression-source inventory — no tests executed

All new P12 tests are **repository source files**, not test results. They are deliberately **not** part of the P13 build-only command. The existing merged Issue #7 test sources have also been reconciled to the Issue #8 first-route-loss and receipt-time policies.

| GitHub test source | Authored @Test methods | Relevant deferred proof |
|---|---:|---|
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue8Phase12InterruptionReducerTest.kt` | 14 | T01–T03: focus episodes, four resume guards, manual Pause, stale gain; T05–T06 duplicate route/noisy; T08 overlap |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue8Phase12ReceiptOrderingTest.kt` | 5 | T06–T08: receipt order, duplicate ID, queued stale remote Play, preemption at arrival |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue8Phase12NoticeAndOwnershipTest.kt` | 9 | T04, T09–T12: one Media3 owner, P09 Voice cancellation seam, sanitized P10 errors, P11 API guard |
| Updated `Issue7Phase9RouteFocusGuardTest.kt` | 15 existing | P05/P06 first-loss deduplication and P11 API31 BLE guards; supersedes outdated Issue #7 assumption that duplicate loss creates two epochs |
| Updated `Issue7Phase12IntegratedBoundaryContractTest.kt` | 15 existing | P07 original receipt epoch, current named route callbacks, previous Issue #7 owner invariants |

**Total newly authored for #8: 28 methods; executed 0.** This inventory is not complete evidence of real Bluetooth routes, OS foreground eligibility, Media3 notifications, true Call detection or speaker leakage. T13 OEM/vehicle evidence and T14 APK provenance **cannot** be supplied by JVM source tests.

**P13 one integrated APK build-only:** The owner-facing build must use `feature/8-audio-focus-route-recovery` at its exact committed HEAD, compile `:app:assembleDebug` once as a unified candidate (exclude Gradle unit/lint/instrumentation tasks), upload the complete debug APK and record source SHA, APK SHA-256, Android variant, CI/job URL and download instructions. A failed compile requires a source fix and rerun *within P13*, not an invented artifact or P12 PASS. User installs this single final APK and runs M01–M45 on their own handset, submitting observed statuses to be recorded on GitHub.

## Technical T01–T15 — source-authored later, NOT executed without authorization

| ID | Required eventual technical proof | Current state |
|---|---|---|
| **T01** | Pure reducer focus GAIN/LOSS/LOSS_TRANSIENT/duck transition matrix and order | NOT_RUN |
| **T02** | All four transient resume guards, independent false-condition variations | NOT_RUN |
| **T03** | Manual Pause/Stop beating delayed regain/OS resume | NOT_RUN |
| **T04** | Media3 `setAudioAttributes(..., true)`, no second `AudioManager.requestAudioFocus`, API 35+ FGS eligibility | NOT_RUN |
| **T05** | Noisy/A2DP/wired/USB/BLE output-sink classification, no speaker leak guard | NOT_RUN |
| **T06** | Duplicate/noisy/remove and rapid route-flap events, idempotent epochs | NOT_RUN |
| **T07** | Stale queue decode/resolver/process/Voice callback cannot revive route-lost playback | NOT_RUN |
| **T08** | Focus loss + disconnect + manual Pause concurrent event orders; exactly one service player | NOT_RUN |
| **T09** | MediaSession notification/controller snapshot revision and system media-key parity | NOT_RUN |
| **T10** | Voice session/duck cancel contract (#10 dependent), no stale restart | NOT_RUN |
| **T11** | Stable typed privacy-safe focus/route errors; no media/device/contact identifiers or sensitive logging | NOT_RUN |
| **T12** | Source compatibility API 29/31/33/35/36; lifecycle/FGS/manifest/accessibility checks | NOT_RUN |
| **T13** | Actual OEM + parked vehicle field verification evidence matrix | NOT_RUN |
| **T14** | Final **BUILD-ONLY** P13 APK provenance / install hash; no JUnit, Lint or AndroidTest asserted | NOT_RUN |
| **T15** | All 12 AC linked to actual evidence; source review and release/merge decision, no false PASS | NOT_RUN |

**Technical status rule:** If only the final APK is compiled, T14 may record `BUILD_SUCCESS` with genuine log/hash; that is **not** proof that JVM, lint, instrumentation or physical safety tests ran. T01–T13/T15 require their respective independent evidence or remain NOT_RUN. Owner phone PASS does not magically create technical execution logs.

## Copyable phone report after P13

```text
Issue: #8
P13 Source Commit: <exact supplied GitHub SHA>
P13 APK SHA-256: <exact supplied hash>
Device: <your real phone model>
Android version / API: <actual>
Date:
M01: NOT_RUN
M02: NOT_RUN
...
M45: NOT_RUN
Accessory availability (wired / USB / BT / head unit):
Notes / FAIL / BLOCKED / N/A reasons:
```

**Interpretation:** This file is a future checklist, not a phone test run. No result is PASS as of planning. All phases before P13 prohibit testing and APK builds. The owner decides later when to authorize and supply actual results; assistant records them in GitHub with device, artifact hash and source provenance.
