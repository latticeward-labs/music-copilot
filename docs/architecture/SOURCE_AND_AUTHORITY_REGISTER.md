# Source and Authority Register - Issue #16

- **Product:** Music Copilot MVP v1.0
- **Phase:** Issue #16 / Phase 1 - Source and authority stabilization
- **Status:** PASS / COMPLETE / SOURCE-ALIGNED
- **Date:** 27 August 2026
- **Tracking:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- **Execution branch:** [`docs/16-software-architecture`](https://github.com/latticeward-labs/music-copilot/tree/docs/16-software-architecture)
- **Frozen Phase 0 baseline:** [`281e1abda39a78ee0d80051c154ed6eb4af33361`](https://github.com/latticeward-labs/music-copilot/commit/281e1abda39a78ee0d80051c154ed6eb4af33361)

## 1. Purpose and controlling rule

This register fixes the source hierarchy for the final implementation-ready Software Architecture baseline. It separates product authority, accepted architecture decisions, approved derived behavior, engineering input, historical workflow evidence and translations so that implementation cannot select whichever source is most convenient.

The controlling rules are:

1. The attached English PRD v1.0 and English MVP v1.0 are the only product authorities.
2. The PRD governs product problem, users, committed capability, functional scope, privacy, quality intent and excluded product scope.
3. The MVP governs priority, timebox, delivery acceptance, QA, Beta and cut rules. It cannot expand or contradict PRD product scope.
4. Current Accepted ADRs translate the product scope into binding architecture boundaries. They cannot create a product capability.
5. Approved User Flows, Golden Scenarios and low-fidelity Wireframes are controlled derived inputs for behavior, recovery, navigation evidence and state coverage. They cannot override PRD/MVP or an Accepted ADR.
6. Technical Spikes and review records supply evidence. They do not become product or architecture authority by themselves.
7. The proposed System Architecture and Code/Technical PDFs are engineering inputs for Issue #16, not final approval evidence.
8. For workflow status, approval date, current artifact state and repository provenance, current GitHub evidence controls over a dated PDF snapshot.
9. A material conflict with an Accepted ADR requires a new superseding ADR. Accepted history is never silently rewritten.
10. A missing implementation detail remains open for Issue #16; it must not be inferred from a translation, illustration, placeholder date or proposed code sample.

## 2. Authority tiers

| Tier | Source class | Governs | Cannot do |
|---:|---|---|---|
| 1 | English PRD v1.0 | Product intent, scope, FR-01 through FR-09, privacy, quality and exclusions | Be expanded by a derived artifact |
| 1 | English MVP v1.0 | P0/P1/P2, ten-week delivery, acceptance, QA, Beta and measurement | Introduce product scope absent from the PRD |
| 2 | Current Accepted ADR-0001 through ADR-0006 | Platform, playback, data, Voice, intent and external-service boundaries | Override Tier 1 or hide a material revision |
| 3 | Approved UF-01 through UF-13, GS-01 through GS-05 and Wireframes | Behavior, states, recovery, permissions, handoffs, navigation evidence and P0/P1/P2 separation | Change product scope or architecture authority |
| 4 | Technical Spike, validation and review evidence | Feasibility, testability, non-claims and implementation evidence | Approve product scope or fabricate physical execution |
| 5 | Proposed System Architecture and Code/Technical synthesis PDFs | Candidate components, contracts, policies, seams and open-decision inventory | Claim final Issue #16 approval or current workflow state |
| 6 | Farsi translations and historical external design snapshots | Human-readable reference and provenance only | Control an English product or architecture decision |

When two Tier 1 documents appear to differ, the PRD controls product intent and scope, the MVP controls delivery priority and validation, and the discrepancy must be recorded rather than resolved by assumption.

## 3. Attached English package identity

The attached English package used for this audit has SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`. The upload is available in this run as `MSM2.rar` and is referenced in earlier project evidence as `MSM2(2).rar`. Filename suffix variation is not treated as identity or authority; the archive hash and member hashes below identify the audited bytes.

| Package member | Pages | SHA-256 | Authority role | Phase 1 disposition |
|---|---:|---|---|---|
| `Music_Copilot_PRD_v1_EN_Clarified.pdf` | 12 | `486fbd5057e4abf570cd1a4865b6243ee5e31d26a48596414903916d6d81e9cf` | Tier 1 product authority | Controlling PRD source |
| `Music_Copilot_MVP_v1_EN.pdf` | 11 | `98e89a1c9d530f4eb878af94f4f480fc8d4669ce2ed5193ba71f201b032d47b0` | Tier 1 delivery authority | Controlling MVP source |
| `ADR-0001_Native_Android_Platform_Revised.pdf` | 7 | `7569f77eeb9e10e5113d974244ceec0f6133e4cf29fbfa95907c022abfd4fd51` | Tier 2 decision input | Current status resolved from GitHub |
| `ADR-0002_Service_Owned_Media3_Playback.pdf` | 5 | `f9ba6adfac14d5c04a33de55cf845abc56dee06ea0301ae7e66f926e83e729c9` | Tier 2 decision input | Current status resolved from GitHub |
| `ADR-0003_Local_First_Data_Boundaries_Revised.pdf` | 6 | `1ceff4cd1f8d4021dd0e1c5ea3aec91c46b0b121d3f87919f913f3f53ce644a4` | Tier 2 decision input | Current status resolved from GitHub |
| `ADR-0004_Replaceable_Voice_Provider.pdf` | 6 | `b6acbaea7b7898306a6bd07254c24cec50e3de848d1d3631d5df357d3df29114` | Tier 2 decision input | Current status resolved from GitHub |
| `ADR-0005_Restricted_Deterministic_Intent_Mapping.pdf` | 6 | `92db78041240707928f0765cdb6873c4e224fee982c0eaf2cd7c1a92b0949c2a` | Tier 2 decision input | Current status resolved from GitHub |
| `ADR-0006_No_Backend_Account_or_Streaming.pdf` | 5 | `0d500e878742d0bb0e7a7ff90f25cd4ccd59e74b93e28f5414d370c7ea7a0baf` | Tier 2 decision input | Current status resolved from GitHub |
| `Music_Copilot_System_Architecture_MVP_v1_EN.pdf` | 25 | `b056e3b8d3125e6660c2fd9431f4f883128673ed5ad1a7f3f5184dd179715bfb` | Tier 5 engineering input | Proposed synthesis; not final baseline |
| `Music_Copilot_Code_and_Technical_Documentation_MVP_v1_EN.pdf` | 54 | `1d776396a88aebacbdb8b813fe8c6fe82418c951f301762ac594c55a7cd22cf3` | Tier 5 engineering input | Proposed blueprint and candidate contracts |
| `Music_Copilot_User_Flow_Phase_0_Foundation_v1.0_EN.pdf` | 25 | `2393f443e5239a689f96e63d33648a696676a59d821858e12a5c57676b0b38e7` | Tier 4 historical derived input | Approved for flow drafting only; current GitHub controls completion |
| `Music_Copilot_User_Flow_Phase_1_First_Readiness_v1.0_EN(4).pdf` | 21 | `d30dbe83808123852e31e26e4268d4b2b82f579cf9aa75b390ccf07f18d58e1e` | Tier 4 historical derived input | Limited Cluster A checkpoint; superseded for current completion status |

## 4. English PRD to repository alignment audit

- **Official PDF:** `Music_Copilot_PRD_v1_EN_Clarified.pdf`, status `Approved for MVP`
- **Repository representation:** [`docs/PRD.md`](../PRD.md), blob `330e14fcf95f6789cbc0c379a5adf4d5814052e1`, status `Approved for MVP`

| Control point | English PDF | Repository representation | Result |
|---|---|---|---|
| Product/platform | Music Copilot v1.0, native Android 10+ | Same | PASS |
| Product objective | Reduce driving-related manual music interaction through a stable local player, Driving Mode and bounded Voice | Same | PASS |
| Media boundary | Lawful user-authorized local audio | Same | PASS |
| Functional scope | FR-01 through FR-09 | FR-01 through FR-09 represented | PASS |
| Voice scope | Push-to-talk and exactly six English intents | Same six-intent contract | PASS |
| Required surfaces | Ten screen/surface families and required recovery states | Same surface inventory, with detail delegated to approved flows | PASS |
| Quality targets | Local response, Voice P95, crash-free, reuse and correction goals | Same quantitative targets | PASS |
| Privacy | No raw audio by default; no full personal titles, precise location, contacts or unnecessary identifiers | Same prohibited-data boundary | PASS |
| Explicit exclusions | Streaming/download, account/backend/sync, iOS/Android Auto/CarPlay, always-on wake word and conversational AI | Same exclusions | PASS |
| Future discovery | Service integration and iOS only after Beta evidence | Same non-authority boundary | PASS |

**Decision:** `docs/PRD.md` is source-aligned as a concise repository representation. It is not a verbatim replacement and cannot narrow, reinterpret or expand the English PDF. For omitted nuance, the English PRD controls.

## 5. English MVP to repository alignment audit

- **Official PDF:** `Music_Copilot_MVP_v1_EN.pdf`, status `Ready for Planning`
- **Repository representation:** [`docs/MVP.md`](../MVP.md), blob `c948c5d755255d60c8fba4c5608f52e3197cddc7`, status `Ready for Planning`

| Control point | English PDF | Repository representation | Result |
|---|---|---|---|
| Timebox and validation | Ten weeks; 50-driver Closed Beta | Same | PASS |
| Priority contract | P0 Player/Voice/Driving/Stability; P1 Bluetooth hint/restore; P2 Like | Same, including cut order | PASS |
| Voice contract | Six intents, at most six-second listening, one retry, one confirmation and short TTS | Same | PASS |
| Screen/state inventory | Ten required screen families with loading, empty, error and recovery states | Same | PASS |
| Delivery plan | Week 1 through Week 10 gates | Same | PASS |
| QA matrix | Android 10/12/14+, Pixel/Samsung/Xiaomi and at least three vehicles/head units | Same | PASS |
| Success gates | At least 85% command success, under 2.5-second Voice P95, at least 99% crash-free and at least 50% Driving Mode reuse | Same | PASS |
| Telemetry | Allowlisted coarse events and parameters; no raw audio or personal media titles | Same | PASS |
| Scope exclusions | No product backend, account, sync, online catalog, streaming, wake word, Android Auto or iOS | Same | PASS |
| Release governance | Zero open P0; provider/privacy/field evidence remains a later release gate | Same | PASS |

**Decision:** `docs/MVP.md` is source-aligned as a concise repository representation. The English MVP remains controlling for delivery nuance and cannot override PRD product scope.

## 6. ADR status and decision crosswalk

All six PDF cover/header records are historical proposals (`Proposed` or `Proposed - Revised`). Their final Decision Outcome sections say `Accepted` but retain the template approval date `YYYY-MM-DD`. Current GitHub records the actual review, revision and acceptance through Issue #2 on 6 August 2026. Therefore GitHub controls current ADR status and approval provenance; the PDFs remain useful decision inputs.

| ADR | PDF header/body state | Current controlling record | Current blob | Binding decision |
|---|---|---|---|---|
| ADR-0001 | `Proposed - Revised` / outcome `Accepted`, placeholder date | [`Accepted`](../adr/0001-native-android-platform.md) | `7872c01dfcf645c2cc8b730fdc1b20b6b4993cd4` | Native Android 10+; Kotlin/Compose; lifecycle-aware asynchronous state; no cross-platform abstraction |
| ADR-0002 | `Proposed` / outcome `Accepted`, placeholder date | [`Accepted`](../adr/0002-service-owned-media3-playback.md) | `6f6641bbf213015864b53c434c8d6053b0b85ee1` | One service-owned Media3 player, queue and MediaSession independent of UI lifecycle |
| ADR-0003 | `Proposed - Revised` / outcome `Accepted`, placeholder date | [`Accepted`](../adr/0003-local-first-data-boundaries.md) | `b2e81f9942b3c5f77ea62adc54671c6c7716dcbc` | MediaStore authority, rebuildable Room projection/app state and DataStore settings/consent boundaries |
| ADR-0004 | `Proposed` / outcome `Accepted`, placeholder date | [`Accepted`](../adr/0004-replaceable-voice-provider.md) | `9b28a847177b1c78fe4232f50f0ad10e5f26a56d` | STT/TTS behind replaceable ports; explicit failure, consent and manual-control fallback boundaries |
| ADR-0005 | `Proposed` / outcome `Accepted`, placeholder date | [`Accepted`](../adr/0005-deterministic-intent-mapping.md) | `8b354d6c35c2e41d360755d0b8866c5804683b97` | Exactly six deterministic intents; bounded normalization, confidence, ambiguity and no guessing |
| ADR-0006 | `Proposed` / outcome `Accepted`, placeholder date | [`Accepted`](../adr/0006-no-backend-account-streaming.md) | `60c0c9355a203c3bb82cef2f6f1e240c3428bd14` | No product backend, account, sync, catalog or streaming; only reviewed non-authoritative support processors |

The current [`ADR Register`](../adr/README.md), blob `a837aa0806f92aa75906895ce712be772c1ca0bc`, records all six as Accepted. No ADR is superseded at this checkpoint.

## 7. Engineering inputs: permitted use and hard limits

| Engineering document | Dated state | Permitted Issue #16 use | Prohibited interpretation |
|---|---|---|---|
| System Architecture synthesis | Proposed; GitHub snapshot 1 August 2026; explicitly not final | Candidate context/component views, playback/Voice/data ownership, data-flow inventory, privacy boundary, test seams and open decisions | Final architecture approval or current ADR/Issue status |
| Code and Technical Documentation blueprint | Proposed; GitHub snapshot 1 August 2026 | Candidate package layout, interfaces, error types, test seams, build/release controls and traceability starting points | Evidence of implemented code or approved module/API details |
| User Flow Phase 0 Foundation | Complete for drafting foundation | Terminology, conflict handling, initial state/recovery/spike routing and source rules | Complete-set User Flow or final architecture approval |
| User Flow Phase 1 First Readiness | Cluster A Stage 1 PASS; Issue #3 open at snapshot | Historical Cluster A correction provenance and early traceability constraints | Current status of Issue #2/#3, complete UF coverage or current Wireframe state |

Candidate code and diagrams from these documents must be revalidated against current approved GitHub behavior before adoption. Their labels, class names, package names, thresholds and code signatures are not binding unless Issue #16 explicitly approves them.

## 8. Current approved GitHub-derived input register

| Controlled input | Current decision | Repository evidence | Role in Issue #16 |
|---|---|---|---|
| Product scope | Approved / locked | [`docs/PRD.md`](../PRD.md), [`docs/MVP.md`](../MVP.md) | Tier 1 repository representations |
| ADR baseline | 6/6 Accepted on 6 August 2026 | [`docs/adr/README.md`](../adr/README.md) | Binding architecture boundaries |
| User Flow set | UF-01 through UF-13 PASS / COMPLETE | [`docs/USER_FLOWS.md`](../USER_FLOWS.md), blob `ca7092d74a4343c269a178d7c7db47dbc28af2ac` | Behavior, state, recovery and permission input |
| Complete traceability | US/FR/State/Recovery/Permission/ADR/Spike PASS | [`PHASE_1_TRACEABILITY_MATRIX.md`](../user-flows/PHASE_1_TRACEABILITY_MATRIX.md), blob `5a1b2cf93b43e15b17dcd7523a036ae14e4cf909` | Requirement and evidence crosswalk |
| Golden Scenarios | GS-01 through GS-05 PASS / APPROVED | [`golden-scenarios/README.md`](../user-flows/golden-scenarios/README.md), blob `30471af1d588e59044451225b078af0909f9aff6` | Five critical connected architecture paths |
| Scenario traceability | PASS / COMPLETE / SOURCE-ALIGNED | [`STAGE_2_TRACEABILITY_MATRIX.md`](../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md), blob `fec5a3394ed32120888e8c0bd8360678c57c8c31` | GS/UF/FR/State/Permission/ADR/Spike mapping |
| Wireframe gate | Issue #15 merged, Project Done and closed | [`docs/WIREFRAMES.md`](../WIREFRAMES.md), blob `88023a869a79e764400f91044e719ce3c14e3df7` | Approved low-fidelity navigation, state and recovery input |
| Wireframe coverage | 126/126 state dispositions; 10 WF families | [`WIREFRAME_COVERAGE_MATRIX.md`](../wireframes/WIREFRAME_COVERAGE_MATRIX.md), blob `d987f2c0320f215e97546121c62c7bd9f5bed554` | Complete state-to-WF coverage manifest |
| Wireframe review | PASS; Blocker 0, Major 0; two downstream Minor non-claims | [`PHASE_8_CROSS_ROLE_REVIEW_2026-08-27.md`](../wireframes/review/PHASE_8_CROSS_ROLE_REVIEW_2026-08-27.md), blob `d2b3e05670309353e9f8b6d274b034b0d9691b04` | Product/UX/Android/accessibility/privacy feasibility evidence |
| Wireframe closeout | PASS / merged / synchronized | [`PHASE_9_MERGE_CLOSEOUT_2026-08-27.md`](../wireframes/PHASE_9_MERGE_CLOSEOUT_2026-08-27.md), blob `9d75d779441960437e06fbee5f30471e44a85037` | Upstream release and provenance evidence |

Current workflow truth at Phase 1: Issues #2, #3 and #15 are Closed / Completed; Issue #16 is Open / In Progress; Issue #4 remains blocked until Issue #16 approval.

## 9. Historical-to-current discrepancy register

| ID | Historical statement | Current verified state | Resolution |
|---|---|---|---|
| SRC-DIFF-01 | System Architecture PDF: Issue #2 open; all ADRs proposed | Issue #2 closed; ADR-0001 through ADR-0006 Accepted on 6 August 2026 | GitHub controls workflow and ADR status |
| SRC-DIFF-02 | Code/Technical PDF: Issue #3 ready, Issue #15 backlog, Issue #16 backlog | Issue #3 and #15 completed; Issue #16 In Progress | GitHub controls current gate status |
| SRC-DIFF-03 | User Flow Phase 1 PDF: only Cluster A complete; Issue #3 open | UF-01 through UF-13 and complete traceability approved on 23 August 2026 | Current User Flow files control completion |
| SRC-DIFF-04 | User Flow PDFs: Wireframes not authorized | Repository-native Wireframes completed and Issue #15 closed on 27 August 2026 | Current Wireframe closeout controls |
| SRC-DIFF-05 | ADR covers say Proposed while body outcomes say Accepted with `YYYY-MM-DD` | GitHub ADR files say Accepted and Issue #2 records the actual date | PDF placeholder is never approval evidence |
| SRC-DIFF-06 | Proposed architecture leaves navigation/state evidence unavailable | Approved User Flows, GS and Wireframes now supply that evidence | Issue #16 must derive final decisions from current artifacts |
| SRC-DIFF-07 | Earlier design records depend on or discuss Figma | GitHub Markdown/SVG/JSON is the controlling low-fidelity workspace; Figma is historical and non-blocking | Repository-native artifacts control |
| SRC-DIFF-08 | Source filenames have historical suffix variants such as `(1)`, `(2)` or `(4)` | Audited package members are identified by SHA-256 | Hash plus document title/version controls identity |

No difference above changes product scope. Each difference is a workflow, provenance or approval-status update.

## 10. Architecture decision authority matrix

| Architecture decision area | Governing source | Supporting controlled input | Issue #16 rule |
|---|---|---|---|
| Product capability and exclusions | English PRD | English MVP | No derived artifact may add v1.1 scope |
| Priority, timebox and cut order | English MVP | PRD hypothesis and quality gates | P0 cannot be traded for P1/P2 |
| Android platform baseline | ADR-0001 | PRD/MVP Android 10+ constraint | Exact replaceable libraries may be refined without weakening the boundary |
| Playback, queue and MediaSession ownership | ADR-0002 | UF-05/06/11, GS-02/03/05 and WF-06/07 | One service-owned authority; UI and system surfaces observe/control it |
| Media identity and local persistence | ADR-0003 | UF-02/03/04/12/13 and relevant Wireframes | MediaStore authoritative; Room derived/app state; DataStore small settings/consent |
| Speech provider boundary | ADR-0004 | UF-02/08/10/12, GS-04 and WF-03/08/10 | Provider replaceable; manual playback survives provider failure |
| Intent interpretation and ambiguity | ADR-0005 | UF-08/09/10, GS-04 and WF-08 | Exactly six deterministic intents; no guessed or LLM execution |
| Backend/account/streaming prohibition | ADR-0006 plus PRD/MVP | Every UF/GS/WF scope audit | External quality processors stay non-authoritative and non-blocking |
| Screen, state and recovery coverage | Approved UF/GS/WF set | Phase 1/Stage 2 matrices and 126-state manifest | Architecture assigns ownership and paths without redesigning approved behavior |
| Navigation/back/Voice overlay evidence | Approved Wireframes and User Flows | GS handoffs and recovery records | Final ownership is decided in Issue #16; proposed PDF navigation is not binding |
| Concurrency, process death, error taxonomy and module granularity | Accepted ADR boundaries plus Issue #16 evidence | Proposed engineering PDFs and Technical Spikes | Remain open until explicitly approved in the final architecture |
| Current status, review and provenance | GitHub Issue/PR/commit records | Dated PDF snapshots | Live GitHub evidence controls |
| New consequential architecture choice | New Issue #16 ADR | Source/traceability/spike evidence | Must be explicit and may supersede, never silently edit, Accepted history |

## 11. Explicit exclusions from controlling authority

- `Music_Copilot_PRD_v1_FA.pdf` and `Music_Copilot_MVP_v1_FA.pdf` are translations and are not used to decide PRD/MVP scope.
- The proposed System Architecture PDF is not the final `docs/SOFTWARE_ARCHITECTURE.md` approval.
- Illustrative Kotlin, package trees and interface signatures in the Code/Technical PDF are not implemented or approved merely because they are detailed.
- Historical `Proposed`, `Open`, `Backlog` or `Not Authorized` labels in dated PDFs do not override current GitHub status.
- A visual snapshot, Figma node, SVG alone or archive filename does not override the paired, approved repository behavioral record.
- Technical Spike policy/reference PASS does not imply unrecorded physical-device, OEM, vehicle, provider/legal or Beta execution.

## 12. Phase 1 validation and exit decision

- [x] English PRD inspected and aligned to `docs/PRD.md`.
- [x] English MVP inspected and aligned to `docs/MVP.md`.
- [x] ADR-0001 through ADR-0006 inspected, cross-referenced and resolved to current Accepted GitHub status.
- [x] System Architecture, Code/Technical, User Flow Phase 0 and User Flow Phase 1 classified as engineering or historical derived inputs rather than higher product authority.
- [x] Approved User Flow, Golden Scenario and Wireframe inputs registered with current links, statuses and blob provenance.
- [x] Historical RAR/GitHub status differences recorded and resolved without changing product scope.
- [x] Every material architecture decision area has a named governing source and conflict rule.
- [x] Farsi translations and proposed/historical artifacts are excluded from controlling authority.
- [x] No Issue #16 Acceptance Criterion is claimed complete merely from this source audit.

**Phase 1 result: PASS / COMPLETE / SOURCE-ALIGNED.** The source hierarchy is unambiguous and sufficient to begin Phase 2 architecture traceability. The final Software Architecture, implementation decisions, new ADR requirements, production provider/legal approval and physical/OEM/vehicle evidence remain downstream gates.

Recorded by **OpenAI Codex**, type **AI-assisted source and authority audit**, on **27 August 2026**.
