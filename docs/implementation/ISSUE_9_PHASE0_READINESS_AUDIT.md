# Issue #9 — P00 Source, dependency, current-code and readiness audit

**Audit date:** 2026-10-24 (UTC)  
**Disposition:** `PASS — SOURCE/DEPENDENCY/STATIC-CODE AUDIT ONLY`; **P01 NEXT**. This does **not** claim implementation, compilation, runtime compatibility, phone verification, technical-test execution or full Issue acceptance.  
**Issue:** https://github.com/latticeward-labs/music-copilot/issues/9  
**Repository:** `latticeward-labs/music-copilot`  
**Execution branch:** `feature/9-driving-mode-bluetooth-suggestion` (pre-existing, retained; P00 does not start a new branch or change production code).  
**Verified main baseline at audit:** `674d0cf19a897dad44b59729fb0afa2c635d802d`.  
**Pre-P00 feature head:** `d430e1cb3943d738ff8d61855a5cecfe9201e6a9`; three planning-only additions vs main: phase plan, 46-case final-owner checklist, NOT_RUN ledger. Revalidate merge base at future PR time.

## 1. Authoritative upload integrity and inventory

User-uploaded `MSM2(1)(1).rar` was directly opened, inventoried and its twelve English PDFs text-extracted for a source-only cross-check. **RAR SHA-256: `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`** (matches the established GitHub source lock). Nothing in the archive is an Android source checkout, APK or device-test result.

| Original PDF within archive | SHA-256 |
|---|---|
| `Music_Copilot_PRD_v1_EN_Clarified.pdf` | `486fbd5057e4abf570cd1a4865b6243ee5e31d26a48596414903916d6d81e9cf` |
| `Music_Copilot_MVP_v1_EN.pdf` | `98e89a1c9d530f4eb878af94f4f480fc8d4669ce2ed5193ba71f201b032d47b0` |
| `Music_Copilot_System_Architecture_MVP_v1_EN.pdf` | `b056e3b8d3125e6660c2fd9431f4f883128673ed5ad1a7f3f5184dd179715bfb` |
| `Music_Copilot_Code_and_Technical_Documentation_MVP_v1_EN.pdf` | `1d776396a88aebacbdb8b813fe8c6fe82418c951f301762ac594c55a7cd22cf3` |
| `Music_Copilot_User_Flow_Phase_0_Foundation_v1.0_EN.pdf` | `2393f443e5239a689f96e63d33648a696676a59d821858e12a5c57676b0b38e7` |
| `Music_Copilot_User_Flow_Phase_1_First_Readiness_v1.0_EN(4).pdf` | `d30dbe83808123852e31e26e4268d4b2b82f579cf9aa75b390ccf07f18d58e1e` |
| `ADR-0001_Native_Android_Platform_Revised.pdf` | `7569f77eeb9e10e5113d974244ceec0f6133e4cf29fbfa95907c022abfd4fd51` |
| `ADR-0002_Service_Owned_Media3_Playback.pdf` | `f9ba6adfac14d5c04a33de55cf845abc56dee06ea0301ae7e66f926e83e729c9` |
| `ADR-0003_Local_First_Data_Boundaries_Revised.pdf` | `1ceff4cd1f8d4021dd0e1c5ea3aec91c46b0b121d3f87919f913f3f53ce644a4` |
| `ADR-0004_Replaceable_Voice_Provider.pdf` | `b6acbaea7b7898306a6bd07254c24cec50e3de848d1d3631d5df357d3df29114` |
| `ADR-0005_Restricted_Deterministic_Intent_Mapping.pdf` | `92db78041240707928f0765cdb6873c4e224fee982c0eaf2cd7c1a92b0949c2a` |
| `ADR-0006_No_Backend_Account_or_Streaming.pdf` | `0d500e878742d0bb0e7a7ff90f25cd4ccd59e74b93e28f5414d370c7ea7a0baf` |

Binding repository authorities: [PRD](../PRD.md), [MVP](../MVP.md), [Software Architecture](../SOFTWARE_ARCHITECTURE.md), [Runtime Policies](../architecture/RUNTIME_POLICIES.md), accepted [ADR-0007](../adr/0007-same-process-playback-runtime-and-safe-paused-restore.md) and [ADR-0008](../adr/0008-minimal-modular-monolith-and-dependency-enforcement.md). Source-to-code priority: accepted current GitHub architecture/ADR and #16/#17 handoff clarify implementation details; the original twelve source PDFs govern MVP scope and do not authorize arbitrary scope growth.

## 2. Product/flow/design obligations now frozen for Issue #9

- **P0 Driving core:** PRD **FR-04**, MVP **US-04**, **UF-07**, WF-07 and HF-07: one screen with real title/artist/artwork or truthful placeholders, four **Previous / Play-Pause / Next / Seek Back** controls, separate Mic and immediate Exit. **Playing / Paused / No Track** reflect one MediaSessionService-owned snapshot and connection truth. No second player, queue or optimistic playback state.
- **P0 navigation:** manual entry from accepted Home/Library and Full Player surfaces; record most recent *non-driving* origin; Android Back and Exit equivalent, immediately return to origin or Home/Library fallback. No automatic entry after reconnect, rotation, app restart or Bluetooth event. An active Voice overlay is cancelled on Driving Exit before leaving, without stopping unrelated legitimate playback.
- **P0 ergonomics and privacy:** one layer, no keyboard, nested settings, long lists, deep navigation, motion distraction, permission dialogue or unsupported safety claim. Road attention remains the driver's responsibility. Minimum **48dp** touch area and complete TalkBack order; HF-07 sizes are **64dp transports / 72dp Mic / 56dp Exit**, 20dp Driving inset, readable reflow through **200%** font scaling.
- **P1 Bluetooth hint** is optional/removable: one *user-selected* eligible paired audio device, explicit local opt-in, suggestion only after eligible connection, never forced Activity/Driving/open playback/recording. App UI displays voluntary Enter/Not now; dismiss and stale reconnect events must be harmless. Bluetooth permission/availability/notification limitations must never disable P0.
- **Voice seam:** a real Mic affordance is required in #9; full six-intent voice execution, STT/TTS and voice-specific permissions belong to **#10** and must not be represented as complete when not present. No always-on listening or automatic mic activation.
- **Route safety inherited from #8:** Bluetooth/headset disconnection safe Pause and no unintended speaker leakage; route reattachment alone is **not** authorization to resume. Preserve corrected Android media-card Play, notification-card tap-to-open-app and AirPods media-key Play behavior.
- **Out of scope:** Android Auto/CarPlay, driving-motion detection, backend/account/streaming/downloads, vehicle motion testing on public roads, new playback engine, in-Driving Bluetooth settings, P2 extras or broad telemetry. Parking is mandatory for owner car/head-unit scenarios.

Required reference set:
[UF-07 source](../user-flows/CLUSTER_C_STATE_INVENTORY.md) ·
[WF-07 Playing](../wireframes/app-states/WF-07_DRIVING_MODE_PLAYING.md) /
[Paused](../wireframes/app-states/WF-07_DRIVING_MODE_PAUSED.md) /
[No Track](../wireframes/app-states/WF-07_DRIVING_MODE_NO_TRACK.md) ·
[HF-07 primary](../high-fidelity/WF-07/HF-07_DRIVING_MODE_PLAYING.md) ·
[Screen Specs](../handoff/SCREEN_SPECS.md) ·
[Component Specs](../handoff/COMPONENT_SPECS.md) ·
[State Action Matrix](../handoff/STATE_ACTION_MATRIX.md).

## 3. Actual GitHub dependency audit (not assumed from old issue-body metadata)

| Dependency | Actual GitHub issue state at audit | Readiness implication / residual evidence |
|---|---|---|
| [#7](https://github.com/latticeward-labs/music-copilot/issues/7) Media3 service, queue, background | **CLOSED** | Core code integrated in main; this is availability, **not** a new #9 Playback QA PASS. #7 closeout was owner-accepted with documented evidence limitations. |
| [#8](https://github.com/latticeward-labs/music-copilot/issues/8) Audio Focus/calls/route | **CLOSED**, [PR #77](https://github.com/latticeward-labs/music-copilot/pull/77) merged | Preserve final fixes. Its issue text reports 43 owner-self-reported phone PASS, three owner-reported correction PASS, **M44/M45 Voice BLOCKED**, and technical **T01–T15 NOT_RUN**. Do not silently assume broader field/OEM/independent verification. |
| [#16](https://github.com/latticeward-labs/music-copilot/issues/16) architecture | **CLOSED**, accepted | Ownership, Nav/Voice overlay and service authority contract available; P01/P02 must implement rather than redefine. |
| [#17](https://github.com/latticeward-labs/music-copilot/issues/17) UI/HF handoff | **CLOSED**, design accepted | WF-07/HF-07 and 126-State handoff available, but design acceptance is not Android UI implementation or device pass. |
| [#5/#6](https://github.com/latticeward-labs/music-copilot/issues/6) onboarding/library | **CLOSED** (owner MVP acceptance) | Local library and permissions exist; deferred proofs retained in **OPEN [#73](https://github.com/latticeward-labs/music-copilot/issues/73)** and **[ #74](https://github.com/latticeward-labs/music-copilot/issues/74)**. |
| [#10](https://github.com/latticeward-labs/music-copilot/issues/10) Voice implementation | Future integration | #9 implements an honest Mic seam; GS-04 full STT journey remains dependent on actual #10 delivery. |

**Conflict reconciled:** The #9 issue body still describes “Backlog (blocked)” and some DoR checkboxes as unchecked. These old descriptions do **not** override actual CLOSED dependencies and current main source. P00 concludes **ready for P01 code contract work**, not that #9 is implemented or that all its acceptance checkboxes should be checked. DoR historic ticks must not be rewritten as proof of runtime QA.

## 4. Static implementation inventory and exact gaps on current main

| Existing verified artifact | Actual capability | Issue #9 gap and owner phase |
|---|---|---|
| `app/navigation/Destination.kt` | Typed `Destination.Driving` declared; Voice absent by architectural design | Driving route not activated/wired; **P03** |
| `app/navigation/AppNavHost.kt` | One NavHost; live Library, Search, FullPlayer and limited Settings UI; shared nav-scoped `PlayerViewModel` | No Driving `composable`, no target/entry/Exit/back origin; Settings is currently first-use/privacy content, not BT chooser; **P03/P09** |
| `app/navigation/RootNavigationStateHolder.kt` | Startup/readiness projection only | No concrete “last non-driving origin” driving record yet; **P03** |
| `feature/driving/PackageBoundary.kt`, `core/application/driving/PackageBoundary.kt` | Structural placeholders only | Driving state projector/UI/use-case/feature orchestration absent; **P02–P08** |
| `playback/api/PlaybackGateway.kt`, `PlaybackSnapshot.kt`, `PlaybackCommand.kt` | Service snapshot `StateFlow`, connection `StateFlow`, typed execute/ACK; current item, queue, position/canSeek/canSkip, typed failure; Play/Pause/Next/Previous/SeekBy supported | Reuse for all four buttons; handle connection pending/error and no-track; do **not** create new Player; **P02/P06** |
| `feature/player/PlayerViewModel.kt` | Shared controller projection, actual ACK dispatch, `seekBackTenSeconds()` | Adapt without sharing mutable UI state as playback authority; explicit safe edge cases and Driving action semantics; **P02/P06** |
| `playback/command/PlaybackNavigationPolicy.kt` | 3-second Previous policy; `VOICE_SEEK_BACK_MS=10000`, bounded seekBack | Reuse **-10 seconds**; do not invent different seek semantics; **P02/P06** |
| `data/api/PreferencesRepository.kt`, `data/datastore/PreferencesDataStore.kt` | `selectedBluetoothHint`, `PreferenceUpdate.SetBluetoothHint`, local `bluetooth_hint` value | A persisted string is **not** paired-device enumeration, explicit opt-in, permission/revoke handling, signal observer, or optional UX; **P09–P11** |
| `app/src/main/AndroidManifest.xml` | Media3/media/record-audio/service permissions; no explicit Bluetooth permission declarations seen | P1 device connection on Android 12+ needs review of `BLUETOOTH_CONNECT` permission and API29–30 legacy declarations/gating, with no incidental scan/location; **P09/P10**, no permission changes in P00 |
| `voice/api/VoiceSessionCoordinator.kt`, `voice/session/DefaultVoiceSessionCoordinator.kt` | Typed `VoiceOrigin.Driving`, cancellable Voice state-contract and source implementation | Public API/structural readiness does **not** prove Activity/Driving live microphone+STT overlay integrated; protect against fake Listening; **P07** and #10 |
| `docs/wireframes`, `docs/high-fidelity`, `docs/handoff` | Approved WF-07 Playing/Paused/No Track and HF-07/accessibility state/actions | Must conform during Compose implementation and at eventual owner phone check; **P04/P05/P08/P12** |

**Static-only findings:** no Kotlin compilation, bytecode inspection, device/provider feature discovery, Bluetooth event observation, rendered-size measurement or audio playback test was conducted. The planning files are 3 ahead of main on the already-existing branch; they are **not** production implementation.

## 5. Acceptance-criteria traceability (14 original #9 ACs)

| AC | Binding expectation | Primary implementation phases | Evidence gate now |
|---|---|---|---|
| AC01 | Title, artist, artwork, four large transports, Mic, Exit | P04/P05/P07 | Not verified; final owner M04/M16/M27 |
| AC02 | Playing/Paused/No Track follow approved HF | P02/P04/P05 | Not verified; M04–M08 |
| AC03 | Shared service-authoritative playback commands only | P02/P06 | Not verified; M09–M15, inspection |
| AC04 | 48dp+, high contrast, TalkBack | P04/P08 | Not verified; M27/M28/M30 |
| AC05 | Focus order and 200% readable scaling | P08 | Not verified; M28/M29 |
| AC06 | No keyboard/deep menus/lists/motion | P04/P08 | Not verified; M31 |
| AC07 | Approved manual activation | P03 | Not verified; M01–M03 |
| AC08 | Selected BT connection suggests, never forces | P09/P10/P11 | Not verified; M33–M40 |
| AC09 | P1 removable without loss of P0 Driving | P01/P09–P12 | Not verified; M41/M42 |
| AC10 | UI recreated from service-owned state | P02/P03/P06/P12 | Not verified; M18–M21 |
| AC11 | Immediate Exit to approved manual destination | P03/P08 | Not verified; M02/M03/M17 |
| AC12 | Parked in-vehicle golden scenario | P12/P13 and #10 for Voice | Not verified; M45–M46, actual parked vehicle |
| AC13 | Accurate road-attention notice | P04/P08 | Not verified; M32 |
| AC14 | UI/accessibility tests cover core states and exit | P08/P12 source authored; execution separate | **NOT_RUN**; authored code is not an executed test |

Authoritative owner checklist: [M01–M46](ISSUE_9_FINAL_PHONE_QA_CHECKLIST.md). Outcome ledger: [QA_RESULTS.json](ISSUE_9_QA_RESULTS.json). Baseline AC statuses stay **NOT_VERIFIED**; phone and technical cases stay **NOT_RUN**.

## 6. Risk/decision register and P01 handoff

| ID | Severity | Exact risk | Safe resolution / owner |
|---|---|---|---|
| R09-01 | P0 | Existing app has a declared-but-not-active Driving route | P03 adds only the authorized NavHost route and explicit origin/Back/Exit with no auto-entry |
| R09-02 | P0 | Disconnected controller or stale playing UI could show incorrect action | P02/P06 always gate command/display via connection state and latest service snapshot; no optimistic success |
| R09-03 | P0 | Regression in Issue #8 notification Play, AirPods Play or route safe pause | P06/P12 keep service unchanged except narrowly essential audited changes; owner M22–M26 rechecks after P13 |
| R09-04 | P0 | Large text/short phone height could clip Exit/control | P04/P08 use HF tokens, safe insets and 200% text reflow; eventual owner accessibility evidence |
| R09-05 | P0 dependency | #10 real Voice overlay/STT not yet end-to-end evidenced | P07 displays an honest actionable or disabled Mic with truthful fallback; M45 remains BLOCKED when #10 unavailable |
| R09-06 | P1 | User device identity, Android 12+ BT permission, selected profile can be misinterpreted | P09/P10 explicit opt-in, only selected paired audio device, no scans, no identifying telemetry; fail closed |
| R09-07 | P1 | Connect duplication, background launch or false automatic driving prompt | P10/P11 dedupe state, no background activity launch, no autoplay, no forced entrance |
| R09-08 | release evidence | One user's phone or headset does not establish three vehicles/OEM matrices or JVM/UI tests | Record only actual owner results; preserve NOT_RUN/BLOCKED and defer with explicit acknowledgement |

### P01 Definition of Ready — source-only gate result

- [x] RAR physically opened; **12/12** PDFs enumerated and SHA-256 locked.
- [x] Current live GitHub main SHA and existing issue-specific branch identified; nonproduction branch delta inspected.
- [x] #7/#8/#16/#17 actual Issue states reconciled against stale “blocked” prose.
- [x] Approved FR/UF/WF/HF/ADR/architecture and 14 AC obligations mapped.
- [x] Actual Kotlin navigation, player, Voice interface, DataStore and manifest gaps recorded.
- [x] P0 core vs cuttable P1 Bluetooth boundary established.
- [x] Distinct owner P13-only final APK / actual owner-phone evidence / technical NOT_RUN policy preserved.
- [ ] #9 executable Driving implementation — **not yet started**.
- [ ] Android build, instrumented/JVM/Lint, one-device/vehicle owner QA, real Voice (#10) verification — **NOT_RUN / dependent**.

**Gate decision: P00 static audit COMPLETE; proceed to P01.** Remain on `feature/9-driving-mode-bluetooth-suggestion`. Future phases must use the one cumulative P13 build only, with **zero tests/builds in P00–P12**. No CI/Actions run, owner APK, merge or Issue closure was requested or performed by this audit.
