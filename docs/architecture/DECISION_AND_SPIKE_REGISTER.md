# Decision and Technical Spike Register — Issue #16 Phase 3

- **Status:** **PHASE 3 PASS / COMPLETE / SOURCE-ALIGNED**
- **Date:** 27 August 2026
- **Tracking:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- **Branch:** `docs/16-software-architecture`
- **Inputs:** [Source/Authority Register](SOURCE_AND_AUTHORITY_REGISTER.md), [Architecture Traceability Matrix](ARCHITECTURE_TRACEABILITY_MATRIX.md), [Technical Spike evidence index](../user-flows/evidence/README.md), the ten controlled TS records, accepted ADR-0001..0006, approved UF/GS/WF evidence, and the English System Architecture/Technical Blueprint in the byte-identified MSM2 archive.
- **Authority exclusion:** Persian/Farsi PRD and MVP attachments are translations and were not used as product authority.

## 1. Gate decision

All ten Technical Spikes were reclassified by evidence strength and assigned exactly one Phase 3 action. No new focused Spike execution is required now: existing evidence is sufficient to make every implementation-blocking architecture decision, and gaps in provider/legal/field evidence are neutralized by explicit fail-closed defaults. `Extend` means carry the evidence into a production contract and downstream tests; it does **not** convert reference evidence into a physical claim.

- **Reuse:** 5
- **Extend:** 5
- **Execute now:** 0
- **N/A:** 0
- **Implementation-blocking decisions without owner/evidence/action:** **0**

No current Accepted ADR is contradicted or silently rewritten. Consequential new choices are assigned to Phase 4 ADR formalization before implementation begins.

## 2. Evidence classification contract

| Class | Meaning | Permitted claim |
|---|---|---|
| Accepted execution/attestation evidence | Automated and/or accepted physical matrix exists; some historical raw files or measurements may be covered by the explicit one-time retention waiver. | May support the recorded deterministic/platform contract only. The waiver must stay visible; absent artifacts or numbers are never reconstructed. |
| Executable reference/policy evidence | Host/synthetic/AOSP regression proves reducer, adapter-safety or policy determinism. | May support architecture logic and test seams; cannot claim OEM callbacks, audible routing, real provider/legal facts, production accuracy or vehicle behavior. |
| Fail-closed architecture default | Required external/provider/legal evidence is absent, so the architecture disables that path or selects Manual Only/NoOp. | May remove an implementation blocker without claiming the disabled capability is ready. |

## 3. TS-01..TS-10 action register

| Spike | Question | Evidence truth | Phase 3 action | Decision adopted | Remaining non-blocking evidence | Owner/action route | Evidence blob |
|---|---|---|---|---|---|---|---|
| TS-01 | Notification and foreground-media continuity | Accepted execution/attestation evidence: AOSP API 29/31/34 automated PASS; Samsung N01-N10 accepted PASS; Pixel/Xiaomi rows explicitly waived; historical raw-capture waiver applies | Reuse | Adopt one service/session truth and platform-correct system surfaces. No new Phase 3 run. | Release/OEM visual and permission behavior stays Issue #13; waiver is preserved, not expanded. | Android Architecture -> Issue #16; Field QA -> #13 | 2e11a3e5ad5f11af63d3ab50ad9335f9422669fa |
| TS-02 | UI/service/task/process lifecycle | Accepted execution/attestation evidence: AOSP automation plus L01-L12 lifecycle matrix; historical raw-capture/device-detail waiver applies | Extend | Reuse lifecycle proof and extend it into D-01..D-07: same-process production service, explicit lifecycle, app controller connection, serialized commands and process-death limits. No new spike run. | Production contract/integration tests are Issue #4; force-stop is never P0 proof. | Android Architecture -> Phase 4 ADR; Android implementation -> #4 | fabe0baff974ce0630a7b70cc99a2976ee08bb22 |
| TS-03 | Speech provider, language, network and consent feasibility | Policy/reference evidence plus AOSP adapter-safety tests; no OEM/provider/legal/accuracy approval | Extend | Adopt replaceable provider port and Manual Only fail-closed default unless capability + disclosure + consent pass. No cloud provider is selected. | Provider/OEM/legal/accuracy enablement stays #10/#12/#13; it cannot block manual P0 playback. | Voice Architecture -> #16/#4; Privacy -> #12; Field QA -> #13 | b4a646433d75fa7e46da87dfc712227ea541a451 |
| TS-04 | Conditional call/audio-focus policy | Executable reference-policy/host evidence; AOSP regression did not simulate real telephony/OEM callback order | Reuse | Adopt the four resume guards and manual-pause-wins policy in D-15. No new Phase 3 run. | Physical calls, callback ordering and OEM behavior stay #13. | Playback Architecture -> #16/#4; Field QA -> #13 | db69b0bdc6294b2cf25983a3884b06119786d4fa |
| TS-05 | Headset/Bluetooth route safety | Executable reference-policy/host evidence; no audible speaker-leak or vehicle proof | Reuse | Adopt disconnect-pause, generation invalidation and no reconnect auto-resume in D-15. No new Phase 3 run. | Audible leakage, Android device matrix and three head units stay #13. | Playback Architecture -> #16/#4; Field QA -> #13 | 04e8bcec4144da02acab0476899efc8ee60e0ea9 |
| TS-06 | MediaStore permission, reconciliation and rebuild | Accepted execution/attestation evidence: automated AOSP plus M01-M14 physical matrix; historical timing/raw-capture waiver applies | Reuse | Adopt MediaStore authority, Room as rebuildable projection, stable re-query references and controlled corrupt-item recovery. | Fresh performance numbers are not invented; production repository/schema tests remain #4. | Data Architecture -> #16/#4 | 5d3e50d3ff32a9b40dc990379cccda1ba7944ca9 |
| TS-07 | Deterministic artist matching and ambiguity | Synthetic fixed-corpus/unit reference evidence; no real accent/noise/KPI proof | Reuse | Formally adopt MVP constants: clear similarity 0.88, plausible 0.72, separation 0.08; no top-match guessing; direct execution still requires provider confidence. | Real microphone/library/road-noise quality and KPI stay #11/#13. | Voice Architecture -> #16/#4; Voice QA -> #11/#13 | 652429f97fb65e0cbb64652b1e43873b9e85f182 |
| TS-08 | Versioned Safe-Paused queue/position restoration | Accepted execution/attestation evidence: R01-R12 including real process-loss behavior; historical raw-capture waiver applies | Extend | Adopt D-04/D-07 production snapshot contract: atomic versioned local snapshot, live service wins, validate MediaStore, restore Paused only, never autoplay. | Production Proto DataStore adapter/migration/integration tests remain #4. | Playback/Data Architecture -> Phase 4 ADR; implementation -> #4 | 5637d7d7b0cc3407b783e631d1253df12eb41583 |
| TS-09 | Safe confidence default and remote failure | Policy/host reference evidence; no production provider/field tuning proof | Extend | Adopt local T_DIRECT 0.90, allowed future validated range 0.80..0.98, plausible 0.72, invalid/missing confidence fail-closed. Remote-config adapter is NoOp by default. | Any tuning/provider activation needs Issue #11/#12/#13 evidence and cannot widen grammar or bypass confirmation. | Voice Architecture -> #16/#4; Product/Privacy/QA -> #11/#12/#13 | 79799619fadae07a6efec478ced0022d27b435b3 |
| TS-10 | Retention and selected deletion semantics | Executable privacy-plan/host evidence; no exact duration, external processor or legal approval | Extend | Adopt D-11: retention-bound collection disabled without a positive approved duration; selected confirmed clearing; protected categories; provider deletion returns independent partial result. | Exact durations/provider obligations remain #12; NoOp quality adapters keep this non-blocking. | Privacy Architecture -> #16; Privacy approval -> #12; implementation -> #4 | 98d6f6068601a4b5ae73338087a4ac4c2afad84 |

## 4. Verified workflow records

| Run | Head SHA | Live GitHub result | Evidence scope |
|---|---|---|---|
| [#17](https://github.com/latticeward-labs/music-copilot/actions/runs/32154062200) | f0cee7606dd1c2899b7a145ea072c747041da444 | completed / success | TS-03/07/09 reference contracts |
| [#21](https://github.com/latticeward-labs/music-copilot/actions/runs/32189320160) | 8c0cddd298932f7499a69b0ee0ea391ea46583e4 | completed / success | TS-04/05/10 reference contracts + AOSP regression |
| [#25](https://github.com/latticeward-labs/music-copilot/actions/runs/32192810624) | 9134e90e923343d3551258cbb3c756ae4cc7db2e | completed / success | UF-13 P2 reference contract; non-blocking context |
| [#32](https://github.com/latticeward-labs/music-copilot/actions/runs/32640155802) | c9f3f56619102a2a634211360a09d8beb2a826f4 | completed / success | Latest TS-01/03/06 automated revalidation |

The live run status was re-read from GitHub on 27 August 2026. This verification confirms the recorded CI conclusions; it does not re-run historical physical/OEM evidence.

## 5. Historical open-decision closeout

The old English System Architecture intentionally left these items open. Each is now decided, bounded and assigned an implementation/formalization action.

| ID | Decision | Final Phase 3 resolution | Evidence | Required formalization / owner | Disposition |
|---|---|---|---|---|---|
| D-01 | Playback service process | Same app process; no `android:process`. UI lifecycle independence is provided by `MediaSessionService`, not IPC/process isolation. | ADR-0001/0002; TS-02; Android Service default process semantics | Phase 4: record ADR-0007. Issue #4: manifest/DI enforcement. | Resolved / no blocker |
| D-02 | Service startup/shutdown | No boot/app-start autoplay. Build Player+MediaSession in service `onCreate` on first legitimate controller/service connection. Foreground media lifecycle begins only from an accepted user/system Play/Resume. On task removal keep service only while playback is ongoing; otherwise stop. Explicit Stop/cleared queue/terminal unrecoverable state stops service and releases in `onDestroy`. | ADR-0002; TS-01/02; official MediaSessionService lifecycle | Phase 4 ADR-0007; Issue #4 service lifecycle tests. | Resolved / no blocker |
| D-03 | UI/MediaController reconnection | One application-scoped `PlaybackControllerConnection` exposes Disconnected/Connecting/Ready/RecoverableError and a complete `PlaybackSnapshot`. Connection is asynchronous. On reconnect atomically replace the entire snapshot before Ready. Screens/ViewModels observe and send commands; they never construct ExoPlayer or authoritative queue state. External controllers remain supported. | ADR-0002; TS-01/02; WF-06 controller states | Phase 4 ADR-0007; Issue #4 fake/real controller integration. | Resolved / no blocker |
| D-04 | Process-death limits | P0 covers UI close/recreation and active service continuity, not force-stop or guaranteed survival after process death. A cold start may offer P1 restoration only. A live MediaSession always wins over persisted data. Force-stop remains a negative control. | TS-02 L01-L12; TS-08 R01-R12; FR-02 vs FR-08 split | Phase 4 ADR-0007; Issue #4 process recreation tests. | Resolved / no blocker |
| D-05 | Playback command serialization | All UI/system/voice commands converge on one service-owned `PlaybackCommandProcessor`, single consumer on the player application looper/Main.immediate. Commands carry an ID and are processed sequentially; no parallel player mutation and no optimistic success. Acknowledgement is an authoritative snapshot/change or typed rejection/timeout. Use a bounded channel (capacity 32); overflow returns typed Busy and never drops an accepted command silently. | ADR-0002; TS-01/02; CB-14/21/29 | Phase 4 ADR-0007; Issue #4 ordering/duplicate/overflow tests. | Resolved / no blocker |
| D-06 | Coroutine scopes and dispatchers | Inject all dispatchers/scopes; never use `GlobalScope`. UI uses `viewModelScope`. Playback uses service `SupervisorJob + Main.immediate`; Player/MediaSession access stays on application looper. Voice uses one child SessionJob; starting/exiting/interruption cancels it. Library/repository work uses application/repository scope and restarts after process death. IO: MediaStore/Room/DataStore/blocking adapters; Default: normalization/matching; Main: state publication/player/UI. | TS-02/03/06; Android coroutine guidance | Phase 4 ADR-0007; Issue #4 dispatcher-injection and cancellation tests. | Resolved / no blocker |
| D-07 | Queue/position restoration | Use versioned `RestorableQueueSnapshot` in Proto DataStore (not live queue authority): schema version, queue revision, ordered MediaStore re-query references, current index, non-negative position and capture time/state; no title/artist/path. Write atomically on queue/current-item/pause changes and debounced at 5 seconds while active. On cold restore validate every item, drop invalid entries, clamp position to verified duration, reject corrupt/future schema, and prepare Paused only. | ADR-0002/0003; TS-08; TS-06 | Phase 4 ADR-0007; Issue #4 production store/migration tests. | Resolved / no blocker |
| D-08 | Voice cancellation/overlap | `VoiceSessionCoordinator` is the sole voice owner. Exactly one active generation token is allowed. Starting a new session cancels STT/TTS/timeout/ducking from the previous one. Call/focus/route loss, Driving exit, Back, permission loss and UI-origin invalidation cancel. Ignore stale-token callbacks. Playback command must be acknowledged before success TTS; TTS failure never rolls back playback. | ADR-0004/0005; TS-03/04/05/09; CD states | Phase 4 ADR-0007; Issue #4 session race/cancellation tests. | Resolved / no blocker |
| D-09 | Navigation, Back and Voice overlay | Single-activity Compose NavHost. Destinations: Bootstrap, Onboarding, Permission recovery, Home/Library, Search, Full Player, Driving, Settings and optional P2 Liked Songs. Voice is an overlay owned by the current Player/Driving origin plus `VoiceSessionCoordinator`, never a standalone navigation branch. Back dismisses/cancels Voice first; Driving Back/Exit immediately returns to origin; normal destinations use NavController; Android Settings/permission are external handoffs followed by re-check. | Approved WF-01..10 and GS-01..05; ADR-0001/0004/0005 | Phase 5 diagram + Phase 6 package contract; Issue #4 navigation tests. | Resolved / no blocker |
| D-10 | Typed error taxonomy | Use sealed `AppError`: Permission, Library, MediaItem, Playback, Controller, Voice, Interruption, Storage, Privacy and Quality. Each has stable non-sensitive code, recoverability, `RecoveryAction` (Retry/OpenSettings/Rebuild/Skip/ManualFallback/ManualResume/None) and safe diagnostic attributes. Throwables stay internal; UI receives typed state. Quality failures never block product; no raw audio/transcript/title/path/device ID. | Architecture traceability AP-01..12; TS-04/05/10; ADR-0003/0006 | Phase 6 contracts; Issue #4 mapping/property tests. | Resolved / no blocker |
| D-11 | Retention and Clear-data behavior | Default quality/crash/analytics/config adapters are NoOp. Short History, Local Diagnostics and Provider Diagnostics are not collected unless a positive approved duration and allowlist exist. Current consent choice is stored as a preference, not an event history. Clearing requires explicit category selection and confirmation. Likes, Settings, consent control, library projection, live queue and media files are protected from this operation. Provider failure yields Partial/Retryable, never false Cleared. | ADR-0003/0004/0006; TS-10 | Issue #12 approves durations/provider duties; Issue #4 implements fail-closed default. | Resolved safe default / exact duration non-blocking |
| D-12 | Package and Gradle-module strategy | Start production as one `:app` Gradle module (plus existing isolated non-production spike project), using enforced packages: `app`, `core`, `feature`, `playback`, `voice`, `data`, `quality`. `app` is composition root; features depend on ports/models; Android/provider/Media3/Room implementations do not leak upward. Extract a module only for independent ownership, reusable public contract, build isolation or measurable dependency enforcement; no premature module explosion. | 10-week MVP constraint; proposed technical blueprint; ADR-0001..0006 | Phase 4: ADR-0008. Phase 6: dependency/package map. | Resolved / no blocker |
| D-13 | State/shared-state placement | Playback/queue/position: MediaSessionService. Voice/pending confirmation: VoiceSessionCoordinator. Media existence: MediaStore. Derived index: Room repository. Settings/consent: DataStore repository. Screen state: destination ViewModel. Navigation/overlay: root navigation state holder. Quality session: application quality boundary. No duplicated authoritative state. | ADR-0002/0003/0004; architecture traceability matrix | Phase 5 ownership diagram; Phase 6 contracts. | Resolved / no blocker |
| D-14 | Numeric interaction/voice policies | MVP values: seek-back 10 seconds; PREVIOUS restarts current track when position >3 seconds, otherwise previous item; voice listening timeout 6 seconds; one retry; T_DIRECT 0.90; valid future tuning range 0.80..0.98; artist clear 0.88; plausible 0.72; minimum separation 0.08. Missing/invalid confidence never direct-executes. Tuning cannot add intents, widen grammar or bypass confirmation. | TS-07/09 executable reference policies; technical blueprint seek policy | Record constants in Phase 6 contract; field tuning only through #11/#13 evidence. | Resolved baseline / tuning non-blocking |
| D-15 | Post-call/focus/route recovery | Resume only when all guards hold: playback was Playing, focus returned, route generation is unchanged and valid, and no manual Pause occurred. Permanent loss safe-pauses. Disconnect immediately pauses, cancels voice and increments route generation. Reconnect remains Paused until explicit Resume. Duplicate/stale events are idempotent. | TS-04/05; CE-01..14; GS-05 | Issue #4 policy integration; Issue #13 physical/OEM/vehicle proof. | Resolved policy / field proof downstream |
| D-16 | Provider defaults | Speech: Android adapter behind replaceable port, but production mode is Manual Only unless capability/disclosure/consent gates pass; no generic offline claim. Cloud speech is disabled. Crash/analytics/remote-config/quality adapters are NoOp by default. Local confidence defaults are sufficient; P0 has no network dependency. | ADR-0004/0006; TS-03/09/10 | Provider/legal activation requires #10/#12/#13; not an Issue #4 foundation blocker. | Resolved safe default / activation non-blocking |
| D-17 | Room ownership and migrations | Room contains only rebuildable library projection/search tokens plus optional app-owned short history and P2 Likes. MediaStore remains media truth; active queue never lives in Room. Projection corruption may rebuild without deleting media. After release, user-owned Likes/history require explicit tested migrations; destructive migration is allowed only for rebuildable projection or pre-release development data. | ADR-0003; TS-06; UF-13 P2 contract | Phase 6 schema ownership; Issue #4 migration/rebuild tests. | Resolved / no blocker |

## 6. Initial production package boundary

The following is a **single production Gradle module** package map, not seven premature modules:

```text
:app
└── com.latticeward.musiccopilot
    ├── app/                 # Application, Activity, NavHost, Hilt composition root
    ├── core/                # models, AppResult/AppError, dispatcher qualifiers
    ├── feature/             # onboarding, library, search, player, driving, settings, liked(P2)
    ├── playback/            # api, media3, controller, command processor, restore
    ├── voice/               # api, android provider, intent mapper, session coordinator
    ├── data/                # api, MediaStore, Room, DataStore
    └── quality/             # api, privacy filter, NoOp adapters

spikes/android-validation/   # isolated non-production evidence harness; not an app dependency
```

Allowed dependency direction: `feature -> core/ports`; `playback/voice/data/quality implementations -> their own ports + core`; `app -> all implementations for composition only`. Forbidden: feature-to-feature implementation calls, UI-to-ExoPlayer/Room DAO/SpeechRecognizer, provider-to-playback authority, and quality adapters blocking product behavior.

## 7. ADR requirements for Phase 4

| Candidate | Decision recorded | Why ADR is required | Relationship to existing ADRs |
|---|---|---|---|
| ADR-0007 — Same-process runtime, service lifecycle and Safe-Paused restore | D-01..D-08 and D-15 | Process topology, long-running ownership, serialization, cancellation and P1 restoration are consequential implementation constraints. | Elaborates ADR-0002/0003; does not supersede them. |
| ADR-0008 — Minimal modular monolith and dependency enforcement | D-12/D-13/D-17 | The one-module start and extraction criteria govern repository structure and ownership. | Elaborates ADR-0001/0003/0006; does not supersede them. |

If Phase 4 review rejects either resolved decision, it must create a replacement ADR decision with explicit consequences; it may not silently alter this register or an Accepted ADR.

## 8. Official Android compatibility checks

The runtime decisions were checked against current official Android guidance on 27 August 2026:

- [Background playback with MediaSessionService](https://developer.android.com/media/media3/session/background-playback): Player/MediaSession live in the service; controller connections are supported independently of the UI; create/release resources in service lifecycle.
- [Control playback with MediaSession](https://developer.android.com/media/media3/session/control-playback): controllers send commands to the single session/player authority.
- [Android Service reference](https://developer.android.com/reference/android/app/Service.html): a Service is not a separate process unless explicitly configured, supporting the no-`android:process` decision.
- [Coroutine best practices](https://developer.android.com/kotlin/coroutines/coroutines-best-practices): inject dispatchers/scopes and avoid unmanaged GlobalScope.
- [State holders and UI state](https://developer.android.com/topic/architecture/ui-layer/stateholders): ViewModels own destination presentation state, while business/data truth remains in its owning layer.

## 9. Non-claims and downstream gates

This Phase 3 result does **not** claim new physical-device execution, Pixel/Xiaomi provider approval, real call callback ordering, audible route-leak proof, head-unit/vehicle execution, speech accuracy/KPI, cloud processor/legal approval, exact retention duration, production database migration, Beta readiness or release approval. Those claims remain with their named downstream issues.

Issue #4 remains blocked until Phase 4 ADRs and the remaining Issue #16 architecture/review/merge gates pass. UF-13/Like remains P2, removable and non-blocking.

## 10. Completeness audit

| Check | Expected | Observed | Result |
|---|---|---|---|
| TS records with one valid action | 10 | 10 | PASS |
| Reuse / Extend / Execute / N/A | 5 / 5 / 0 / 0 | 5 / 5 / 0 / 0 | PASS |
| Historical blocking decisions resolved | 17 | 17 | PASS |
| Resolved decisions without owner/action | 0 | 0 | PASS |
| Accepted ADR conflicts/silent supersession | 0 | 0 | PASS |
| New physical/provider/legal claims | 0 | 0 | PASS |
| Implementation-blocking provider/retention gaps | 0 | 0 — Manual Only/NoOp/fail-closed defaults | PASS |

**Phase 3 gate: PASS.** No implementation-blocking architecture decision is left open without a final resolution, evidence boundary, owner and action. Phase 4 may now formalize ADR-0007 and ADR-0008.

<!-- phase3-source-evidence-sha256: 93a1b5679402ae5fb4f21536bec2fe388a924437654705b88d50861e04474569 -->
