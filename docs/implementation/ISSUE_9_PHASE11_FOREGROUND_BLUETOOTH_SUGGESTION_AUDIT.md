# Issue #9 — P11 optional foreground Bluetooth invitation / Enter / Not now

**Date: 2026-10-24 · Gate: PASS_SCOPED_P11_FOREGROUND_INVITATION_SOURCE_ONLY_NOT_TESTED · Next: P12**

## Normative source lock

- User-supplied MSM2(1)(1).rar was SHA256-verified 0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8 against all prior phases.
- PRD FR-04 Driving P0; FR-07 optional selected-device Bluetooth suggestion P1; local-first Android MVP; ADR-0002 service-owned Media3; UF-07/UF-12; HF-10/CE-20 explicitly says connection must not launch Driving Mode. P11 remains cuttable without impacting P0 manual Driving.
- Existing P03 DrivingNavigationViewModel accepts only the same Library/FullPlayer origins and never changes playback. Existing P10 SelectedBluetoothConnectionCoordinator exposes a verified reconnect via a replay=0 SharedFlow, with independently observed A2DP/HEADSET connection/bond/permission and a selection epoch. This event is NOT a command, implicit user consent or proof of current Media3 output routing.

## Real code committed and narrow scope

| File | Implemented P11 behavior and bound |
|---|---|
| app/src/main/java/com/latticeward/musiccopilot/core/application/bluetooth/BluetoothEntrySuggestionPolicy.kt (new) | Pure disposable invitation state; source event ID, current selection epoch, actual connected profile/selected evidence and foreground allowed surface required; invalid events consumed, duplicates ignored, decline cooldown 5 minutes, pending invalidated on selection/route/permission/background change. No address, device name, source token or personal media in policy state. |
| app/src/main/java/com/latticeward/musiccopilot/app/platform/bluetooth/SelectedBluetoothConnectionCoordinator.kt (P11 extension to P10) | Replay=0 VerifiedAudioReconnectEvent SharedFlow carries only monotonic edge and private selection epoch; no old prompt restored on Activity recreation or process restart. `isUnclaimedOfferCurrent` checks saved selected token matches live private preference, readiness, foreground and currently connected actual audio profile. `claimOffer` serializes one explicit Enter; never navigates or plays. |
| app/src/main/java/com/latticeward/musiccopilot/app/navigation/BluetoothEntrySuggestionViewModel.kt (new) | One Activity-scoped mutable UI invitation via pure reducer; not persisted or SavedState-restored. `onReconnect`, `reconcile`, `notNow`, `accepted`, `invalidate`; no OS/Media3 side effect or secondary NavHost. |
| app/src/main/java/com/latticeward/musiccopilot/feature/driving/BluetoothEntrySuggestionBanner.kt (new) | Small in-place nonmodal Surface, written optional prompt + Enter/Not now actions; 48dp+ targets, two-row reflow at >=150% font, heading/live semantics. No raw Bluetooth ID or assertion that music is already routed/playing. |
| app/src/main/java/com/latticeward/musiccopilot/app/navigation/AppNavHost.kt (narrow P11 addition) | One lifecycle-aware event collector, latest allowed UI/permission/evidence state, only Library or FullPlayer existing routes and HomeLibrary/GRANTED with Settings/Search/Voice/permission/Driving excluded. Reconcile hides offers on loss of eligibility. Banner inserted inline above original full Library/FullPlayer contents, not a modal/dialog/new destination. Enter rechecks fresh OS BLUETOOTH_CONNECT, approved foreground surface, pending visible event identity and service-confirmed selected-profile evidence; claims event first, then calls EXACT P03 `drivingNavigation.enterFrom(origin, true)`. Not now only starts cooldown. |
| app/src/main/java/com/latticeward/musiccopilot/app/MainActivity.kt (narrow P11 addition) | Supplies existing singleton app-owned `selectedBluetoothConnection` and `freshBluetoothConnectPermission = ::hasBluetoothConnectPermission`. Never starts a background Activity on a receiver, never auto-requests permission. |
| app/src/main/res/values/strings.xml | Four plain-English, localizable offer strings: Driving Mode?, connected selected Bluetooth audio invitation, Enter, Not now. Explains no autoplay. |
| app/src/test/java/com/latticeward/musiccopilot/foundation/Issue9Phase11BluetoothEntrySuggestionPolicyTest.kt | 24 authored pure JUnit policy cases; **NONE EXECUTED**. |
| app/src/androidTest/java/com/latticeward/musiccopilot/Issue9Phase11BluetoothInvitationUiTest.kt | Four authored Compose UI cases for written optional prompt, independent decline, Enter only on direct click, >=48dp buttons at 200% font; **NONE EXECUTED**. |

## Safety and UX decision table

| Situation | Behavior (authored, NOT tested on phone) |
|---|---|
| No saved P09 selected device, storage not Ready, Bluetooth permission revoked or Android Bluetooth off | No offer; P0 manual Driving remains available. |
| A device other than the selected one connects; generic ACL event; old bonded-only status; session proxy arrives | No verified selected-device reconnect event from P10, no offer. |
| App is backgrounded, freshly launched while already connected, or selected profile recovers only from initial scan | No background Activity, push, autoplay or prompt replay; no unsolicited invitation. |
| Genuine selected A2DP or Headset profile reconnect after verified disconnected, app foreground on Library/FullPlayer with all P0 permissions and no Voice | A single nonblocking inline invitation. No navigation until explicit Enter. |
| User taps Not now | Keep current route, song/queue, focus and Mic unchanged; clear offer; suppress newly verified reconnect offers for 5 minutes in this UI lifetime; no persistent negative consent. |
| User explicitly taps Enter while same invitation/profile/selection/foreground/permission remains current | Claim one event, use P03 allowed origin to open existing P0 Driving state; no start/play/pause/seek/BT route change/Voice. The service remains the playback authority. |
| Connected profile drops / radio off / selected token changes / app background / Settings/Search/Onboarding/permission route appears / Voice starts | Invalidate visible invitation. Stale onClick callbacks recheck live event and fresh OS grant, cannot navigate. |
| Duplicate reconnect broadcast or wrong event/old epoch / pending other invitation | Event deduped; never stacks prompts. |
| Notification permission denied / app not foreground | No notification mechanism added; no permission coercion or forced foreground launch. |

**HFP caveat:** Bluetooth HEADSET profile connection is proof of a voice-capable Bluetooth profile, not evidence that Media3 music is routed to that device. UX offer says only that selected Bluetooth audio connected; P11 never guarantees an audio route. Android OEM physical profile and focus behavior remains unverified.

## Traceability and evidence-only acceptance

- Product: FR-07 P1, UF-12, HF-10 CE-20, approved route navigation from P03, safe #8 Audio Focus and route unchanged.
- Owner phone checks: M33–M44 (permission, Save/Remove, selected vs unselected, event dedupe, Not now, explicit Enter, background, notification-disabled, process restart). M35–M40 are especially required for P10→P11 integration. Physical tests remain NOT_RUN.
- [x] Uploaded RAR hash matched. Live GitHub P10 coordinator, P03 NavHost and HF-10 contract read.
- [x] P11 pure policy + one-shot event stream, existing root NavHost nonmodal Enter/Not now, fresh OS route/permission guard committed.
- [x] P0 Library/FullPlayer/Driving flows retained; no Media3, focus, notification, Voice, Android Auto, BT scan/location, playback or queued music mutation.
- [x] 24 JUnit + 4 UI source tests authored; **ZERO executed**.
- [ ] Gradle compile, JVM tests, Compose/instrumentation tests, lint, GitHub Actions, emulator, real Bluetooth/OEM phone and vehicle: **NOT_RUN**.
- [ ] All 14 Issue #9 product AC **NOT_VERIFIED**, 46 owner handset M **NOT_RUN**, five technical groups **NOT_RUN**, APK **NOT_BUILT**.
- [ ] P12 cross-feature static hardening, source traceability and final checklist; P13 SINGLE cumulative APK and owner phone QA. No PR, merge or Issue close in P11.

**Disposition:** P11 SOURCE-ONLY scoped implementation complete; P12 NEXT. This does not claim Bluetooth, a11y or app runtime PASS.
