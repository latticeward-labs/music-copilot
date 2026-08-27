# Phase 4 ADR Decision Audit — Issue #16

- **Status:** **PASS / COMPLETE / DIRECTLY REVALIDATED AGAINST ATTACHED ENGLISH MSM2 SOURCE**
- **Date:** 27 August 2026
- **Tracking:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- **Branch:** `docs/16-software-architecture`
- **Primary attached source:** directly attached `MSM2(2).rar`
- **Repository evidence:** [Source/Authority Register](SOURCE_AND_AUTHORITY_REGISTER.md), [Architecture Traceability Matrix](ARCHITECTURE_TRACEABILITY_MATRIX.md), [Decision and Technical Spike Register](DECISION_AND_SPIKE_REGISTER.md), current Accepted ADR-0001..0006, approved UF/GS/WF evidence

## 1. Source correction and integrity verification

Phase 4 was re-run as a source-conformance audit directly against the newly attached archive bytes rather than relying on any Persian/Farsi attachment or a translated summary.

`MSM2(2).rar` was inspected directly. Its SHA-256 is:

`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`

The archive contains the following twelve English PDF members relevant to the project baseline; no Persian/Farsi PRD or MVP is present inside this archive:

| Attached English member | SHA-256 | Phase 4 role |
|---|---|---|
| `Music_Copilot_PRD_v1_EN_Clarified.pdf` | `486fbd5057e4abf570cd1a4865b6243ee5e31d26a48596414903916d6d81e9cf` | Product authority |
| `Music_Copilot_MVP_v1_EN.pdf` | `98e89a1c9d530f4eb878af94f4f480fc8d4669ce2ed5193ba71f201b032d47b0` | Delivery/P0-P1-P2 authority |
| `ADR-0001_Native_Android_Platform_Revised.pdf` | `7569f77eeb9e10e5113d974244ceec0f6133e4cf29fbfa95907c022abfd4fd51` | Architecture decision input; current status from GitHub |
| `ADR-0002_Service_Owned_Media3_Playback.pdf` | `f9ba6adfac14d5c04a33de55cf845abc56dee06ea0301ae7e66f926e83e729c9` | Architecture decision input; current status from GitHub |
| `ADR-0003_Local_First_Data_Boundaries_Revised.pdf` | `1ceff4cd1f8d4021dd0e1c5ea3aec91c46b0b121d3f87919f913f3f53ce644a4` | Architecture decision input; current status from GitHub |
| `ADR-0004_Replaceable_Voice_Provider.pdf` | `b6acbaea7b7898306a6bd07254c24cec50e3de848d1d3631d5df357d3df29114` | Architecture decision input; current status from GitHub |
| `ADR-0005_Restricted_Deterministic_Intent_Mapping.pdf` | `92db78041240707928f0765cdb6873c4e224fee982c0eaf2cd7c1a92b0949c2a` | Architecture decision input; current status from GitHub |
| `ADR-0006_No_Backend_Account_or_Streaming.pdf` | `0d500e878742d0bb0e7a7ff90f25cd4ccd59e74b93e28f5414d370c7ea7a0baf` | Architecture decision input; current status from GitHub |
| `Music_Copilot_System_Architecture_MVP_v1_EN.pdf` | `b056e3b8d3125e6660c2fd9431f4f883128673ed5ad1a7f3f5184dd179715bfb` | Engineering architecture input; explicitly leaves final Issue #16 choices open |
| `Music_Copilot_Code_and_Technical_Documentation_MVP_v1_EN.pdf` | `1d776396a88aebacbdb8b813fe8c6fe82418c951f301762ac594c55a7cd22cf3` | Technical blueprint; candidate contracts/open-decision inventory |
| `Music_Copilot_User_Flow_Phase_0_Foundation_v1.0_EN.pdf` | `2393f443e5239a689f96e63d33648a696676a59d821858e12a5c57676b0b38e7` | Historical English flow-foundation evidence |
| `Music_Copilot_User_Flow_Phase_1_First_Readiness_v1.0_EN(4).pdf` | `d30dbe83808123852e31e26e4268d4b2b82f579cf9aa75b390ccf07f18d58e1e` | Historical English readiness evidence; current GitHub controls completion |

The archive hash and member hashes match the existing [Source/Authority Register](SOURCE_AND_AUTHORITY_REGISTER.md). Therefore the reattached file is byte-identical to the English source package already identified by Issue #16 source stabilization.

**Explicit exclusion:** Persian/Farsi PRD/MVP attachments are not used to derive, validate, approve or cite any Phase 4 architecture decision.

## 2. What the attached English source actually fixes vs leaves open

The direct source review shows a clean boundary between established architecture constraints and decisions intentionally deferred to Issue #16.

### Established by the attached English sources

- Native Android 10+; Kotlin/Compose baseline; no iOS/cross-platform product scope.
- Local lawful device audio; no product backend, account, cloud sync, online catalog or streaming.
- Media3/ExoPlayer with a service-owned Player/MediaSession; UI observes and never owns authoritative playback/queue state.
- P0 active playback/background/screen-off continuity is distinct from P1 FR-08 queue/position restoration.
- MediaStore is authoritative for device media; Room is derived/rebuildable; DataStore is for small app-owned local state/preferences.
- Voice provider is replaceable; STT is separate from deterministic intent mapping; exactly six supported executable intents remain bounded.
- Raw audio/full transcripts/personal media titles are outside telemetry; cloud speech, if ever enabled, needs separate disclosure/consent/retention handling.
- Logical UI/Application/Playback/Voice/Data/Quality boundaries and inward-facing ports are required.
- No blocking media/database work on main; long-running playback is service-owned; structured concurrency with explicit lifecycle ownership is required.
- P0 interruption recovery must avoid unintended speaker playback and preserve a stable queue/player.

### Explicitly left open by the attached English System Architecture / Technical Blueprint for Issue #16

- final navigation graph/back behavior/Voice-overlay ownership;
- exact coroutine scopes and dispatcher boundaries;
- shared-state placement details and error hierarchy;
- process-death restoration limits;
- Room schema/migrations and exact FR-08 representation;
- PREVIOUS threshold and exact post-call resume policy;
- confidence/fuzzy numeric thresholds;
- final speech/crash/analytics/remote-config providers;
- exact retention policy;
- package layout and Gradle-module granularity;
- same-process versus separate-process playback service.

This distinction is critical: the Phase 4 ADRs may finalize these items using current approved GitHub evidence, but they must not pretend the older PDFs had already decided them.

## 3. Consequential-decision classification and source reconciliation

Every Phase 3 decision was rechecked both for architectural consequence and for compatibility with the attached English source.

| Decision(s) | Attached English source position | Current GitHub resolution | Consequential? | Phase 4 disposition | Result |
|---|---|---|---:|---|---|
| D-01 | Same-process vs separate-process service explicitly open | Same app process; no `android:process` | Yes | ADR-0007 | PASS — source-compatible finalization |
| D-02/D-03 | Service ownership/reconnect completeness required; exact lifecycle/controller contract not fully fixed | Explicit startup/shutdown + one app-scoped controller connection | Yes as runtime contract | ADR-0007 | PASS |
| D-04 | P0 continuity vs P1 restore fixed; exact process-death limits open | Process death/force-stop not P0; live MediaSession wins | Yes | ADR-0007 | PASS |
| D-05 | One playback authority required; exact serialization mechanism/capacity not fixed | One sequential command processor, bounded capacity 32, typed Busy | Yes | ADR-0007 | PASS; capacity attributed to GitHub Phase 3, not PDF |
| D-06 | Structured concurrency/no global unmanaged scope required; exact scopes/dispatchers open | Explicit UI/service/voice/repository scopes and dispatcher boundaries | Yes | ADR-0007 | PASS |
| D-07 | FR-08 is P1; queue/position representation explicitly open | Versioned Proto DataStore Safe-Paused snapshot | Yes | ADR-0007 | PASS |
| D-08 | Voice provider/session cancellation required at high level | Single generation owner/token, stale callback suppression | Yes as cross-runtime contract | ADR-0007 | PASS |
| D-09 | Exact navigation/back/overlay explicitly open pending approved UX evidence | Single-activity NavHost; Voice overlay; Back/Exit rules | No separate ADR needed | Architecture baseline | PASS |
| D-10 | Typed results/error/recovery seam supported by technical blueprint; exact taxonomy not product scope | Sealed `AppError` + recovery actions | No separate ADR needed | Architecture baseline | PASS |
| D-11 | Exact retention explicitly open; privacy allowlist/fail-safe behavior fixed | Disabled/fail-closed collection until approved duration; selected clearing | No supersession | Architecture baseline | PASS |
| D-12 | Logical boundaries fixed; exact Gradle granularity explicitly open | One `:app` minimal modular monolith + extraction criteria | Yes | ADR-0008 | PASS |
| D-13 | High-level state owners fixed; exact shared-state placement open | Single-authority placement by boundary | Yes as structure/ownership group | ADR-0008 | PASS |
| D-14 | Seek-back 10s, Voice timeout 6s and one retry are source-fixed; PREVIOUS/confidence/fuzzy exact values left open | Adds >3s PREVIOUS, T_DIRECT 0.90, 0.88/0.72/0.08 matching constants | No separate ADR required | Architecture baseline | PASS; open numerics attributed to GitHub evidence |
| D-15 | Immediate disconnect pause/no speaker leakage and logical recovery fixed; exact post-call resume open | Four resume guards + manual-pause-wins | Yes with runtime safety | ADR-0007 | PASS |
| D-16 | Replaceable Android STT/TTS, no mandatory cloud, provider choice open | Fail-closed Manual-Only/NoOp foundation defaults until gates pass | No separate ADR needed | Architecture baseline | PASS with release caveat |
| D-17 | MediaStore authority/Room rebuildability fixed; exact schema/migrations open | Controlled Room ownership/migration policy | Yes as data/module ownership group | ADR-0008 | PASS |

## 4. Corrections made during the direct-source revalidation

The architecture decisions themselves remain valid, but two provenance/wording corrections were necessary to keep them exactly aligned with the attached source:

1. **Hilt is not made a binding ADR constraint.** The attached English Technical Blueprint explicitly classifies Hilt/DI as a replaceable implementation choice. ADR-0008 now defines a generic DI composition root; Hilt may be the initial implementation but can be replaced without superseding ADR-0008 when the approved contracts/dependency direction remain unchanged.
2. **Fail-closed provider/quality defaults are foundation defaults, not Beta acceptance.** The attached MVP makes Voice and required measurement P0/Done obligations. A Manual-Only Voice or NoOp analytics configuration can protect incomplete foundation work, but it cannot be used to claim Closed Beta readiness. Before Beta, an approved Voice path and required privacy-safe telemetry must satisfy the MVP acceptance gates.

These corrections remove any accidental overstatement and do not change the product scope or Accepted ADR-0001..0006.

## 5. Existing ADR history control

| ADR | Existing status | Phase 4 action | Conflict/supersession |
|---|---|---|---|
| ADR-0001 Native Android Platform | Accepted | Preserve unchanged | None |
| ADR-0002 Service-owned Media3 Playback | Accepted | Preserve unchanged; elaborated by ADR-0007 | None |
| ADR-0003 Local-first Data Boundaries | Accepted | Preserve unchanged; elaborated by ADR-0007/0008 | None |
| ADR-0004 Replaceable Voice Provider | Accepted | Preserve unchanged | None |
| ADR-0005 Restricted Deterministic Intent Mapping | Accepted | Preserve unchanged | None |
| ADR-0006 No Backend, Account or Streaming | Accepted | Preserve unchanged | None |
| ADR-0007 Same-process Runtime and Safe-Paused Restore | Accepted 2026-08-27 | Revalidated against attached English source; provenance clarified | Not a supersession |
| ADR-0008 Minimal Modular Monolith and Dependency Enforcement | Accepted 2026-08-27 | Revalidated; DI/Hilt replaceability clarified | Not a supersession |

If a future decision materially changes an Accepted ADR, the replacement must explicitly state `Supersedes ADR-xxxx`; the original file remains historical evidence.

## 6. Mandatory ADR-field audit

| ADR | Status | Context | Decision | Alternatives | Consequences | Validation | Relationship/supersession |
|---|---:|---:|---:|---:|---:|---:|---:|
| 0001 | PASS | PASS | PASS | PASS | PASS | PASS | PASS |
| 0002 | PASS | PASS | PASS | PASS | PASS | PASS | PASS |
| 0003 | PASS | PASS | PASS | PASS | PASS | PASS | PASS |
| 0004 | PASS | PASS | PASS | PASS | PASS | PASS | PASS |
| 0005 | PASS | PASS | PASS | PASS | PASS | PASS | PASS |
| 0006 | PASS | PASS | PASS | PASS | PASS | PASS | PASS |
| 0007 | PASS | PASS | PASS | PASS | PASS | PASS | PASS |
| 0008 | PASS | PASS | PASS | PASS | PASS | PASS | PASS |

## 7. No-hidden-decision audit

The Phase 4 PASS condition is evaluated against the current controlled artifacts:

- process topology and service lifecycle -> ADR-0007 + architecture baseline;
- command serialization/concurrency/cancellation -> ADR-0007 + architecture baseline;
- process-death and P1 restoration -> ADR-0007 + architecture baseline;
- interruption/focus/route recovery -> ADR-0007 + architecture baseline;
- Gradle/package/module strategy -> ADR-0008 + architecture baseline;
- state ownership and Room authority/migrations -> ADR-0008 + architecture baseline;
- navigation/Voice overlay, typed errors, numeric Voice policy, provider defaults and retention fail-closed behavior -> architecture baseline with controlling ADR links;
- diagrams in later phases may illustrate these decisions but may not become their only record.

**Observed material decisions existing only in a diagram: 0.**

## 8. Non-claims and release-boundary guard

This Phase 4 acceptance formalizes architecture decisions; it does not claim new physical-device/OEM/vehicle execution, speech-provider/legal approval, field accuracy, exact retention durations, production migration execution, final diagram approval, Product/Android/Design/QA sign-off, Issue #16 completion, or Issue #4 unblocking.

A fail-closed foundation configuration is also **not** a Beta pass:

- Closed Beta still requires the six-command Voice path and ambiguity/error behavior required by the attached MVP.
- Closed Beta still requires the privacy-safe measurement events needed to evaluate the hypothesis.
- P0 Player/Voice/Driving/Stability cannot be declared Done while replaced by Manual-Only/NoOp placeholders.

## 9. Phase 4 gate

- Attached archive directly inspected and hash-verified: **PASS**
- Attached English members hash-matched to Source Register: **12/12 PASS**
- Persian/Farsi authority used in Phase 4: **0**
- Consequential decisions reviewed: **17/17**
- Source-vs-GitHub attribution ambiguities remaining: **0**
- Existing Accepted ADRs silently rewritten: **0**
- Existing ADR conflicts requiring supersession: **0**
- New consequential ADRs required: **2**
- New consequential ADRs created and Accepted: **2/2**
- Required ADR fields complete: **8/8 ADRs PASS**
- Material decisions hidden only in diagrams: **0**

**PHASE 4: PASS / COMPLETE / SOURCE-CORRECTED.** ADR-0007 and ADR-0008 formalize the two consequential runtime and modular-monolith decision groups, ADR-0001..0006 history remains intact, open choices from the attached English System Architecture/Technical Blueprint are accurately attributed to current Issue #16 GitHub evidence, and no Persian/Farsi source participates in the decision chain.
