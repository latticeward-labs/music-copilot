# Issue #8 — P00 Source, Code and Implementation-Readiness Audit

**Audit date:** 2026-10-23  
**Phase:** P00 / source-and-contract audit **COMPLETE (source-only)**  
**Scope:** no production code changes; no APK/build/Gradle/tests/emulator/device/Actions; no empirical Android safety claim.  
**Repository:** `latticeward-labs/music-copilot`, `main` audited at `62c6115ac11af4cb2790b306f885f580b82a146b`; delivery on `feature/8-audio-focus-route-recovery`.  
**Issue status:** #8 remains OPEN; all 12 original AC **UNVERIFIED**. This P00 completion is not Issue #8 acceptance.

## 1. Evidence and authority chain

1. **Owner attachment actually opened:** `MSM2(1)(1).rar`, RAR5, **SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`**, with 12 readable English PDFs (no source omitted). This exactly matches the existing approved source/authority register and planning audit. The archive has not been imported wholesale into GitHub.
2. **Product:** [PRD FR-03](../PRD.md), [MVP v1.0](../MVP.md), [System Architecture](../SOFTWARE_ARCHITECTURE.md); call/focus response, immediate headset/Bluetooth pause and absence of speaker leak are P0.
3. **ADRs:** [ADR-0001–0006](../adr/README.md), especially [ADR-0002](../adr/0002-service-owned-media3-playback.md) single service-owned ExoPlayer/MediaSession and ADR-0003 privacy/local-first, ADR-0004 replaceable Voice, ADR-0006 no backend/account/streaming; [ADR-0007](../adr/0007-same-process-playback-runtime-and-safe-paused-restore.md) adds P1 Paused-only cold restore; [ADR-0008](../adr/0008-minimal-modular-monolith-and-dependency-enforcement.md) maintains existing package ownership.
4. **Approved implementation architecture:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16), [Runtime Policies §8](../architecture/RUNTIME_POLICIES.md), [UF-11](../user-flows/UF-11_INTERRUPTION_AUDIO_ROUTE.md), [TS-04](../user-flows/evidence/TS-04_AUDIO_FOCUS_POLICY.md). User-flow references: FR-03, UF-11, SYS-07/08/09, D-018/019/022. Voice invalidation is part of contract, but full Issue #10 Voice work must not be claimed finished.
5. **Dependency state:** [Issue #7](https://github.com/latticeward-labs/music-copilot/issues/7) CLOSED after [merged PR #76](https://github.com/latticeward-labs/music-copilot/pull/76). This delivered a real cumulative Media3 service and owner-reported QA acceptance. Independent per-case/JVM/device/call-route logs have **not** been provided as proof; do not elevate the owner report to independent verification. Issue #16 is architecturally APPROVED. Unfinished unrelated post-MVP QA from #5/#6 is separately tracked in [#73](https://github.com/latticeward-labs/music-copilot/issues/73) and [#74](https://github.com/latticeward-labs/music-copilot/issues/74).
6. **Branch check:** before P00, the existing feature branch was **4 commits ahead, 0 behind `main`**, containing four Issue #8 planning files only: phased plan, planning baseline, owner checklist and QA seed. The feature branch is the correct delivery location; do not create a competing Player/branch/CI workflow.
7. **Android documentation rechecked:** [Manage audio focus](https://developer.android.com/media/optimize/audio-focus) advises `ExoPlayer.setAudioAttributes(attributes, true)` to own Audio Focus and forbids a competing app-side focus request/listener; Android 15/API 35+ targets can request focus only as top app or foreground service. [ExoPlayer.Builder](https://developer.android.com/reference/androidx/media3/exoplayer/ExoPlayer.Builder) documents `setHandleAudioBecomingNoisy(true)` auto-pause during headset-to-speaker reroute. Android 12+/OEM OS may enforce fade/mute, which must not be mistaken for an app-proven call callback or a test PASS.

### 1.1 PDF inventory and content trace (SHA-256 per extracted original byte stream)

| Owner PDF (under `MSM/`) | SHA-256 | P00 authority/use |
|---|---|---|
| `ADR-0001_Native_Android_Platform_Revised.pdf` | `7569f77eeb9e10e5113d974244ceec0f6133e4cf29fbfa95907c022abfd4fd51` | Native Android platform, existing API support |
| `ADR-0002_Service_Owned_Media3_Playback.pdf` | `f9ba6adfac14d5c04a33de55cf845abc56dee06ea0301ae7e66f926e83e729c9` | One Media3 owner; focus/routes/headsets |
| `ADR-0003_Local_First_Data_Boundaries_Revised.pdf` | `1ceff4cd1f8d4021dd0e1c5ea3aec91c46b0b121d3f87919f913f3f53ce644a4` | Restricted local data/diagnostics |
| `ADR-0004_Replaceable_Voice_Provider.pdf` | `b6acbaea7b7898306a6bd07254c24cec50e3de848d1d3631d5df357d3df29114` | Voice cancellation boundary/provider replaceability |
| `ADR-0005_Restricted_Deterministic_Intent_Mapping.pdf` | `92db78041240707928f0765cdb6873c4e224fee982c0eaf2cd7c1a92b0949c2a` | Preserve six-intent grammar; no expansion |
| `ADR-0006_No_Backend_Account_or_Streaming.pdf` | `0d500e878742d0bb0e7a7ff90f25cd4ccd59e74b93e28f5414d370c7ea7a0baf` | No backend, account or streaming |
| `Music_Copilot_Code_and_Technical_Documentation_MVP_v1_EN.pdf` | `1d776396a88aebacbdb8b813fe8c6fe82418c951f301762ac594c55a7cd22cf3` | Technical service and Audio Focus contracts |
| `Music_Copilot_MVP_v1_EN.pdf` | `98e89a1c9d530f4eb878af94f4f480fc8d4669ce2ed5193ba71f201b032d47b0` | Week-4 focus/calls/headset/BT matrix |
| `Music_Copilot_PRD_v1_EN_Clarified.pdf` | `486fbd5057e4abf570cd1a4865b6243ee5e31d26a48596414903916d6d81e9cf` | FR-03; crash-free, sensible Pause/Resume |
| `Music_Copilot_System_Architecture_MVP_v1_EN.pdf` | `b056e3b8d3125e6660c2fd9431f4f883128673ed5ad1a7f3f5184dd179715bfb` | Playback lifecycle/route/focus table |
| `Music_Copilot_User_Flow_Phase_0_Foundation_v1.0_EN.pdf` | `2393f443e5239a689f96e63d33648a696676a59d821858e12a5c57676b0b38e7` | UF-11, SYS-07/08/09, D-018/019/022, no surprise resume |
| `Music_Copilot_User_Flow_Phase_1_First_Readiness_v1.0_EN(4).pdf` | `d30dbe83808123852e31e26e4268d4b2b82f579cf9aa75b390ccf07f18d58e1e` | First-run/permissions/readiness recovery; no fabricated state |

## 2. Live code/package survey — source observations, NOT executed claims

| Verified code/contract | Existing behavior and precise P00 qualification |
|---|---|
| [`MusicPlaybackService.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt) | One `MediaSessionService` creates one `ExoPlayer` and `MediaSession`; `setAudioAttributes(USAGE_MEDIA/MUSIC,true)`, `setHandleAudioBecomingNoisy(true)`, LOCAL wake mode, player listener and synchronized snapshot. No separate `AudioManager.requestAudioFocus` is needed. Source presence does **not** prove audio-route timing on a phone. |
| [`PlaybackInterruptionGate.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/PlaybackInterruptionGate.kt) | Has `routeGeneration`, `requiresExplicitResume` and `explicitlyPaused`. On route loss it increments epoch; on explicit play it clears latches; on manual pause it sets manual latch. It lacks a complete four-guard focus interruption reducer recording prior Playing/focus returned/interruption-owned pause and deterministic event identity. |
| [`ServiceAudioRouteMonitor.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/ServiceAudioRouteMonitor.kt) | `AudioDeviceCallback` registered with Main Handler, checks removed sink **type**, not active route identity; catches registration/teardown failures. Existing fail-closed heuristic may also pause for a disconnected non-active accessory. Unknown OEM callback timing remains a physical QA dependency. |
| [`PlaybackCommandProcessor.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/command/PlaybackCommandProcessor.kt) | Service-scoped serialized command handling, but queued command *receipt-time* provenance and route epochs need audit for disconnects while awaiting execution. |
| Service command/restore/recovery paths | `ReplaceQueue`, `SelectQueueIndex`, permission recovery and cold restore perform post-I/O generation/queue checks. However route callback only increments generation if a player already has media and is active/non-idle; a new selection pending against an empty idle Player is a **specific gap to eliminate** in P05/P07. Untrusted delayed external Play after reconnect can clear latches as an ostensibly explicit Play. |
| [`SystemTransportPolicy.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/command/SystemTransportPolicy.kt) | Media3 media-key play/pause are mapped to allowed transport commands; origin is not yet distinguished well enough to verify head-unit auto-play vs deliberate user Play. |
| [`Media3SnapshotProjector.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/media3/Media3SnapshotProjector.kt), [`PlaybackSnapshot.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/api/PlaybackSnapshot.kt), [`PlaybackControllerConnection.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/controller/PlaybackControllerConnection.kt) | One authoritative Player snapshot/revision and app observer/controller plumbing exist. Need real interruption reason projection and synchronized Player/MiniPlayer/Driving/system states, without split authority. |
| [`VoiceSessionCoordinator.kt`](../../app/src/main/java/com/latticeward/musiccopilot/voice/api/VoiceSessionCoordinator.kt), [`DefaultVoiceSessionCoordinator.kt`](../../app/src/main/java/com/latticeward/musiccopilot/voice/session/DefaultVoiceSessionCoordinator.kt) | `CallOrFocusInvalidated`/`RouteInvalidated` cancellation reasons and generation cancellation exist. The service route/focus callback does **not** currently wire those into the app-scoped Voice owner; #10 end-to-end behavior is unverified. |
| [`playback/interruption/PackageBoundary.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/interruption/PackageBoundary.kt) | Package is only a placeholder; no injectable pure focus/route event reducer/seam has been implemented there. |
| [`AndroidManifest.xml`](../../app/src/main/AndroidManifest.xml), [`app/build.gradle.kts`](../../app/build.gradle.kts) | minSdk 29, target/compile 36; `mediaPlayback` FGS and required media/Wake Lock permissions are present. No `READ_PHONE_STATE` or Bluetooth device-identity permission; do not add them for #8. |
| [#7 P09 audit](ISSUE_7_PHASE9_AUDIO_FOCUS_ROUTE_SAFETY_AUDIT.md) and [#7 PR](https://github.com/latticeward-labs/music-copilot/pull/76) | Existing source safety and owner attestation are useful starting points, not independently verified focus/route/call evidence or reuse of #7 APK for #8. |

### 2.1 Concrete risks, ordered for implementation

| ID | Severity | Exact gap/action | Owner phase |
|---|---|---|---|
| R08-01 | P0 | Four-guard transient resume and manual Pause precedence **not enforced by a complete observable event reducer**; reconcile Media3 internal focus without creating second focus owner. | P02–P04 |
| R08-02 | P0 | If route disappears during initial empty/idle Player while a new media request waits in the worker, route generation may not advance; stamp dangerous events at receipt and reject stale work. | P05, P07 |
| R08-03 | P0 | Reconnect/AVRCP `Play` may be indistinguishable from deliberate keypress and clear route latch; design conservative source/context fencing for ambiguous auto-Play. | P06–P07 |
| R08-04 | P0 | Callback timing/ordering is OEM-specific; noisy and device-removed may both arrive, or neither; ensure idempotent high-priority safe pause and do not claim zero leak before device tests. | P05, P07, P13 |
| R08-05 | P0 | Interruption does not currently cancel app-scoped active Voice session through documented reasons; ensure owned cancellation and stale generation invalidation without full #10 rewrite. | P09 |
| R08-06 | P1 | Removed device sink type is not necessarily currently routed sink; minimize nuisance pause when reliable public current-route data exists, default safe when not. | P05–P06 |
| R08-07 | P1 | Stable `AppError.Interruption` taxonomy exists in architecture but distinct focus/route reasons and safe UI/OS state cannot yet be traced end-to-end. | P08, P10 |
| R08-08 | P0 release evidence | Phone-call/duck, API29/31/33/35/36 and Pixel/Samsung/Xiaomi/3 vehicle behavior not independently demonstrated. Source review is **not** physical QA. | P11–P13 + deferred technical gate |

## 3. Definition of Ready — explicit, evidence-limited disposition

| Issue #8 DoR | P00 finding | Implication |
|---|---|---|
| DoR-01: #7 service-owned playback and MediaSession stable | **DEPENDENCY MERGED / OWNER-ACCEPTED; independently verified stability NOT ESTABLISHED** | P01/P02 may use source; Issue #8 must not claim regression-free stability until evidence. |
| DoR-02: #16 route, lifecycle, error policies approved | **PASS (documentation/architecture decision)** | No new architecture authority or competing focus requester needed. |
| DoR-03: focus and route test seams exist | **PARTIAL / BLOCKER for full deterministic implementation** | Existing gate and listener are not a fully injectable four-guard event seam. P02 must implement/reconcile before P03–P07 claims. |

**P00 source audit gate:** **PASS — completed documentary/code review**, known implementation blockers explicitly assigned; **P01 may start**. **Production/field DoR and all 12 AC remain unverified**. This is deliberately *not* a declaration that Issue #8 is implementation-complete or release-ready.

## 4. Acceptance-to-phase/source traceability (all original AC remain UNVERIFIED)

| AC | Required source proof now | Phases | Later verification |
|---|---|---|---|
| AC01 incoming call and deterministic Pause/Duck/recovery | FR-03, UF-11, Runtime §8 | P02–P04 | M06–M10; T01–T04 |
| AC02 focus gain/loss/transient/duck policy | ADR-0002, TS-04 | P02–P04 | M11–M17; T01–T04 |
| AC03 headset/BT immediate pause | ADR-0002, SYS-07–09 | P05–P06 | M18–M23, M27–M30; T05–T06 |
| AC04 no unintended speaker | FR-03, UF-11 | P05–P07 | M19–M23, M27–M30; T05–T07 |
| AC05 service/MediaSession/notification/UI parity | #16, ADR-0002 | P07–P08 | M33–M39; T07–T09 |
| AC06 reconnect no surprise auto-resume | D-018/019, Runtime §8 | P06–P07 | M21–M23, M29–M32; T05–T07 |
| AC07 rapid event/idempotent ordering | UF-11, TS-04 | P07 | M40–M43; T06–T08 |
| AC08 Voice interaction compatible with #10 | ADR-0004, #16 voice state ownership | P09 | M44–M45 if available; T10 |
| AC09 privacy-safe route/focus codes | ADR-0003, Runtime §12–13 | P10 | M39; T11 |
| AC10 unit/integration coverage | #16 test architecture | P12 (author test sources, no execution) | T01–T15 stay NOT_RUN until separately authorized |
| AC11 representative OEM field matrix | MVP QA, UF-11 | P11–P13 | Pixel, Samsung, Xiaomi and vehicles separately logged, not invented |
| AC12 no reproducible crash/unwanted output | FR-03, UF-11 | P07–P13 | Actual owner/field evidence; source audit is insufficient |

## 5. Binding implementation/QA contract

- **P00–P12:** no tests of any kind executed, no Gradle, no `assembleDebug`, no APK, no emulator, no device runs and no GitHub Actions dispatch. P12 may **author** test source without running it. Documentation/source commits use `[skip ci]`.
- **P13 ONLY:** one cumulative installable APK from **all** P00–P12 phases; if GitHub Actions is needed, manually requested **build-only `:app:assembleDebug`** without test/lint jobs. Record exact source commit and built APK SHA-256, run/artifact, then hand off M01–M45 for the owner to run.
- **No fabricated results:** 45 owner-phone M-cases and 15 technical T-cases retain `NOT_RUN`; 12 AC retain `UNVERIFIED`. Prior #7 phone/technical owner attestation cannot transfer. Missing OEM/vehicle hardware is NOT_RUN/BLOCKED, never PASS.
- **Preserve architecture/security:** one MediaSessionService, Media3 focus owner; no competing request/listener, second Player, READ_PHONE_STATE, BT identifiers, private media names/URIs/transcripts, background network, account or streaming. Android 15+ focus/FGS eligibility is mandatory to audit.
- **Field safety:** any driving/head-unit testing must be performed while safely parked. Developer documentation/host tests alone do not establish route no-leak guarantees.

## 6. P00 completion checklist (documentary, NOT device test)

- [x] Verified attached archive SHA-256 and read/extracted all 12 PDFs.
- [x] Reconciled PRD/MVP/ADR/UF-11/TS-04/#16 authority and 12 AC/3 DoR.
- [x] Verified #7 merged and identified owner-report evidence limitation.
- [x] Verified current `main` service focus/noisy/route/Voice/MediaSession/permission code and precise implementation risks.
- [x] Verified execution feature branch exists and started from current main.
- [x] Assigned every risk/AC to a downstream phase and existing final phone/technical case range.
- [x] Recorded no-build/no-test/P13-only APK contract; no user test results invented.
- [ ] Future P02: implement injectable interruption/focus event seams and four-guard behavior (NOT P00).
- [ ] Future P13: actual APK and user handset/OEM/technical evidence (NOT P00).

**Result:** **P00 COMPLETE / PASS_SOURCE_AUDIT_ONLY**. **Next: P01 — GitHub execution, ownership and traceability contract**. This audit can be merged with subsequent work; no PR/merge/Issue closure is authorized or claimed now.
