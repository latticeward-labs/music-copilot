# Issue #9 — P12 cross-feature hardening, traceability and P13 handoff audit

**Date:** 2026-10-24  
**Scope gate:** `PASS_SCOPED_P12_SOURCE_HARDENING_16_JUNIT_3_COMPOSE_AUTHORED_NOT_RUN`  
**Release gate:** `NOT_VERIFIED` — **P13 SINGLE CUMULATIVE APK NEXT; NO P12 BUILD OR TEST EXECUTION**.

## 1. Immutable product baseline and source provenance

- The user-supplied `MSM2(1)(1).rar` was re-verified using SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, identical to the source used in P00–P11. The approved clarified PRD FR-04 Driving P0 / FR-07 opt-in Bluetooth P1, UF-07/11/12, GS-04/05, WF-07 and WF-10/HF-10/CE-20, ADR-0002/0004/0006/0007/0008 still govern.
- GitHub repo: `latticeward-labs/music-copilot`; feature branch: `feature/9-driving-mode-bluetooth-suggestion`; Issue [#9](https://github.com/latticeward-labs/music-copilot/issues/9). Merge base/main not rewritten. Live comparison shows no changes to `playback/service` or the Issue #8 Android notification/headset focus/route implementation. Existing single MediaSession/ExoPlayer/queue remain the only transport authority.
- Android MVP: Android 10+, Kotlin/Compose, Media3, local MediaStore+Room+DataStore; local-only Voice seam where approved. No Android Auto, background launch, second player, cloud/streaming/backend, Bluetooth scanning or location access.

## 2. Concrete P12 source hardening committed

| Existing path | Change and why |
|---|---|
| `feature/settings/bluetooth/BluetoothPreferenceSettingsSection.kt` | Replaced `rememberSaveable` expansion with transient `remember`, so returning from OS Settings/navigation or process recreation does not resurface a stale expanded Bluetooth chooser without a new deliberate Open/Refresh. Kept existing selection draft RAM-only and explicit Save/Remove semantics. Replaced hard-coded accessibility Save/Remove labels and chosen/saved suffixes with localizable resources. |
| `app/src/main/res/values/strings.xml` | Added four English-first `i9_bt_a11y_*` strings; existing strings otherwise unchanged. |
| `feature/driving/BluetoothEntrySuggestionBanner.kt` | Added stable semantics test tags to **actual clickable** Enter/Not now `Button` nodes in both horizontal normal-font and vertical 150–200% layouts. No touch target dimensions or Play/Media3 code changed. |
| `app/src/androidTest/java/com/latticeward/musiccopilot/Issue9Phase11BluetoothInvitationUiTest.kt` | Fixed authored but unrun 200% font test: check `onNodeWithTag(...).assertHeightIsAtLeast(48.dp)` on actionable buttons, not `onNodeWithText` (which measures the text child and can produce false failures). |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue9Phase12CrossFeatureSafetyContractsTest.kt` **NEW** | 16 **authored, not executed** pure JUnit cases combining P0 manual origin navigation, P02/P05 PlaybackSnapshot/route-loss/call/focus/manual-resume, P10 selected A2DP verified reconnect and P11 opt-in/decline/invalidation. No device/BT/Media3 implementation instantiated. |
| `app/src/androidTest/java/com/latticeward/musiccopilot/Issue9Phase12BluetoothEntryIntegrationUiTest.kt` **NEW** | 3 **authored, not executed** Compose cases: nonmodal P1 cannot block original player action, 200% font button-level 48dp verification, and written offer that never claims an automatic Playing state. |
| `docs/implementation/ISSUE_9_FINAL_PHONE_QA_CHECKLIST.md` | Corrected M08 to approved **NoTrack Exit, no unverified Play Last**. Clarified M35 first observed Disconnected then new verified selected profile connection; M37 must allow five-minute Not now cooldown; M40 duplicate/profile/rotation/background stale event. Added concrete stationary-phone execution order and evidence rules; **all 46 remain NOT_RUN**. |
| `docs/implementation/ISSUE_9_P13_APK_BUILD_MANIFEST.json` **NEW** | P13-only build manifest **SKELETON** with source SHA, APK bytes/SHA, artifact link, runner and signature **null / NOT_BUILT** until actual P13. Exact single build-only task `:app:assembleDebug` from cumulative feature HEAD, no test/Lint tasks. |
| This report, issue plan, QA ledger and evidence index | Traceability and scope separated from runtime approval; 14 original AC still NOT_VERIFIED, P13 remains exclusively responsible for the one APK and user field tests. |

## 3. Cross-feature static safety review

| Surface or boundary | Static inspection and P12 disposition | Runtime verification |
|---|---|---|
| P03 manual Library/FullPlayer → Driving and immediate Exit/Back | Single `AppNavHost`; one `DrivingNavigationViewModel`. Origin remembered only in nonpersistent Activity-scoped ViewModel. System/permission bootstrap overrides Driving. No automatic entry due to Bluetooth. | M01–M03, M17–M21 **NOT_RUN** |
| P02/P04/P05 service-derived Playing/Paused/NoTrack | `DrivingPresentationPolicy.project` uses current service queue/index/item and connection; `DrivingRecoveryPolicy` distinguishes typed route/focus errors, controller failures and NoTrack; P0 controls are not fabricated. User Exit available even without track. | M04–M08, M18–M21 **NOT_RUN** |
| P06 same PlayerViewModel four service transports | Four large buttons call `PlayerViewModel.drivingTransport` via typed `DrivingCommandAdmissionPolicy` against **latest** gateway snapshot. Pending/late ACKs guarded; no new player/queue/service in Driving. Service's Play transport handles explicit focus/route safety. | M09–M15, #8 service regression **NOT_RUN** |
| Issue #8 phone calls, audio focus, output loss, headset, AirPods and media notification | Branch vs main diff contains **no `playback/service` files**; P12 does not rewrite route recovery, MediaSession, headset Play handlers or notification-intent routing. Bluetooth suggestion/profiles are observational only. Existing safe-paused policy remains independent of P1 selection/accept. | M22–M26; explicit Android media-card Play/card tap/AirPods Play **NOT_RUN** |
| P07 Voice/Mic | The same application-scoped Voice owner is used; P11 excludes active Voice and the Enter handler never invokes `voiceSessionCoordinator.start`, auto-requests RECORD_AUDIO, starts STT/TTS or uses a remote provider. Voice provider end-to-end GS-04 remains separate Issues #10/#11. | M16, M45 **NOT_RUN/BLOCKED when no live provider** |
| P08 accessibility and responsive state | Driving has fixed Exit, true status/action labels, 64dp transports/72dp Mic/56dp Exit, response to font 150/200%. P1 banner additionally has 48dp+ buttons, two-row reflow and textual Not now; corrected test measures buttons rather than text. | M27–M32 **NOT_RUN** |
| P09 local device preference and permission | One existing PreferencesRepository DataStore `selectedBluetoothHint` stores per-selection salted SHA256 marker, not raw device address/name. Settings enumerates only already-paired eligible audio devices on explicit Open/Refresh; Android 12+ `BLUETOOTH_CONNECT` is requested manually. Save rechecks OS state; Remove works without Bluetooth grant. | M33–M34, M41–M43 **NOT_RUN** |
| P10 actual selected profile, lifecycle, stale event | Foreground-only Android A2DP/HFP profile proxy observation plus fresh bonded whitelist and CONNECT permission. Not based on ACL alone or remembered preference. 2s profile event window/sequence+epoch dedupe; no background receiver/proxy when Activity stops. **HFP connected does NOT prove current Media3 audio output route.** | M35/M38–M40/M44 **NOT_RUN** |
| P11 Enter/Not now optional suggestion | Only one existing foreground actual Library/FullPlayer route; lifecycle STARTED only and no replay=0 prior invitation. Fresh grant+profile+preference+selection epoch checked again upon explicit Enter; event claimed one time, then P03 origin entry. Not now/Back decline with five-minute cooldown. No permission coercion, autoplay, mic or notification. | M35–M40, M44 **NOT_RUN** |
| P1 feature independence/cut | If Bluetooth disabled, denied, unavailable or preference removed, P0 manual Driving still derives only from app/media permissions and real Media3 state. No P1 state is required to enter from Library/FullPlayer. | M01, M41–M42 **NOT_RUN** |
| Restart/recreation | P0 origin and P11 cooldown retained over ordinary configuration change by Activity ViewModel; *not* persisted after process death. Bluetooth selection **only** persisted by P09 explicit Save. Foreground listener and stale event invalidated on onStop. | M18–M21, M40/M43 **NOT_RUN** |
| Golden scenarios | GS-04 requires Voice/intent/TTS #10/#11 to prove Next command; GS-05 requires parked real vehicle/head unit and safe output route. Mark BLOCKED when prerequisite absent, not PASS. | M45–M46 **NOT_RUN / potential BLOCKED** |

**Open risks deliberately not marked PASS:** Kotlin compiler/Gradle and instrumented source may reveal build errors; Android API 29/31/33/35 profile callback order and CONNECT revocation vary; OEM devices may not report both A2DP and Headset profile proxies; physical audio routing and 2-second source coalescing are not validated; TalkBack physical reading order/contrast, 200% OEM scaling, call/route/no-speaker-leak and AirPods/media-card Play regressions require phone checks. No honest verification of multi-OEM/three parked vehicle matrices from a single phone.

## 4. Original AC01–AC14: *source evidence only, not acceptance PASS*

| AC | Requirement | Phase source authority | Owner-phone evidence, unrun |
|---|---|---|---|
| AC01 | Driving state display, artwork, four large actions, Mic, Exit | P02, P04–P08 | M04, M09, M16 |
| AC02 | Three truthful Playing/Paused/NoTrack states | P02, P04, P05 | M04–M08 |
| AC03 | Service-owned commands, no UI queue/second player | P02, P06, P12 | M09–M16 + source review |
| AC04 | 48dp+ touch targets, high contrast, TalkBack | P08, P12 | M27–M30 |
| AC05 | Spoken reading order and font scaling | P08, P12 | M28–M30 |
| AC06 | Driving low-distraction UX | P04–P08, P12 | M31–M32 |
| AC07 | Deliberate manual entry from existing app screens | P03 | M01–M03 |
| AC08 | Selected Bluetooth suggests **never forces** | P09–P11 | M33–M40 |
| AC09 | P1 wholly removable, P0 unaffected | P09–P12 | M41–M43 |
| AC10 | Restore UI from service truth | P02, P03, P06, P12 | M18–M21 |
| AC11 | Immediate Exit/Back to appropriate manual screen | P03, P08 | M02–M03, M17 |
| AC12 | Golden parked-car journeys | P07, P12 | M45–M46, separate dependency/vehicle |
| AC13 | Road-attention responsibility note, no safety guarantee | P04, P08 | M32 |
| AC14 | UI/accessibility test coverage | P08, P11, P12 | **Written sources only**; executed tests NOT_RUN |

**All 14 above remain `NOT_VERIFIED`.** Cross-feature static review and authored regression tests are not runtime acceptance evidence.

## 5. P13 single combined APK owner handoff

1. Freeze the **exact feature branch HEAD after P12** and record SHA. Confirm no test/compile/Gradle/Actions already ran as part of P00–P12; do not label such an absence PASS.
2. **P13 only:** request/build **one cumulative debug APK** using `:app:assembleDebug` on the full P00–P12 feature HEAD (Android 10+/minSdk29), not separate APKs per phase. Prevent any task with test/connectedAndroidTest/lint/check; note Gradle's normal compilation as part of producing the APK is allowed **only in P13**.
3. Capture real build success/failure, source SHA, generated `app/build/outputs/apk/debug/app-debug.apk`, APK SHA256, byte size, signature/delivery provenance, ZIP artifact name and download link. Populate ONLY factual values in [P13 build manifest](ISSUE_9_P13_APK_BUILD_MANIFEST.json); do not fabricate.
4. Give user the **one actual downloadable cumulative APK** plus [all 46 manual phone cases](ISSUE_9_FINAL_PHONE_QA_CHECKLIST.md). On user report, record each M-case outcome *exactly* and include phone/Android/accessory/evidence; unchanged not-run/blocked cases remain visible.
5. Do NOT run JVM/Compose/instrumentation/Lint/Android tests as part of P13. A successful build is **not** test PASS. Issue close / PR / merge only after user explicitly requests them and any gaps are reconciled.

**P12 disposition: scoped STATIC SOURCE HARDENING COMPLETE; P13 BUILD HANDOFF NEXT. No build, APK, test execution, issue close or PR/merge occurred.**
