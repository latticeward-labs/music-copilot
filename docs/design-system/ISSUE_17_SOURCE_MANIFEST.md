# Issue #17 Phase 1 — Source Manifest and Frozen Design Baseline

- **Product:** Music Copilot MVP v1.0
- **Issue:** [#17](https://github.com/latticeward-labs/music-copilot/issues/17)
- **Phase:** 1 — Freeze Baseline and Design Input Manifest
- **Date:** 3 October 2026
- **Status:** **PASS / COMPLETE / SOURCE-FROZEN**
- **Execution branch:** `issue-17/phase-1-source-manifest`
- **Frozen repository baseline:** [`b4ac3bb3ccf299bd282e6d1ba3600b7136dd5669`](https://github.com/latticeward-labs/music-copilot/commit/b4ac3bb3ccf299bd282e6d1ba3600b7136dd5669)
- **Attached English source package SHA-256:** `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- **Active delivery contract:** [Repository-native High-Fidelity Contract](../ISSUE_17_REPOSITORY_HIFI_CONTRACT_ADDENDUM_2026-10-03.md)

## 1. Purpose

This manifest freezes the exact inputs that every Issue #17 high-fidelity, Design System, prototype and handoff artifact must follow. No new visual design is approved by this phase. The phase only establishes source authority, current repository provenance, the ten design-surface families and the initial disposition of all 126 canonical states.

A design artifact may style an approved structure; it may not silently change product scope, navigation, state ownership, recovery behavior, runtime authority or system/app boundaries.

## 2. Attached English source package identity

The user-attached archive used for this phase has SHA-256:

`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`

This byte identity matches the package already audited by the final Issue #16 Source and Authority Register. Therefore the audited English-member register remains applicable to this attachment.

| Package member | Pages | SHA-256 | Phase 1 role |
|---|---:|---|---|
| `Music_Copilot_PRD_v1_EN_Clarified.pdf` | 12 | `486fbd5057e4abf570cd1a4865b6243ee5e31d26a48596414903916d6d81e9cf` | **Controlling product authority** |
| `Music_Copilot_MVP_v1_EN.pdf` | 11 | `98e89a1c9d530f4eb878af94f4f480fc8d4669ce2ed5193ba71f201b032d47b0` | **Controlling delivery/priority authority** |
| `ADR-0001_Native_Android_Platform_Revised.pdf` | 7 | `7569f77eeb9e10e5113d974244ceec0f6133e4cf29fbfa95907c022abfd4fd51` | Binding platform decision input; current status from GitHub |
| `ADR-0002_Service_Owned_Media3_Playback.pdf` | 5 | `f9ba6adfac14d5c04a33de55cf845abc56dee06ea0301ae7e66f926e83e729c9` | Binding playback ownership decision input; current status from GitHub |
| `ADR-0003_Local_First_Data_Boundaries_Revised.pdf` | 6 | `1ceff4cd1f8d4021dd0e1c5ea3aec91c46b0b121d3f87919f913f3f53ce644a4` | Binding local-data decision input; current status from GitHub |
| `ADR-0004_Replaceable_Voice_Provider.pdf` | 6 | `b6acbaea7b7898306a6bd07254c24cec50e3de848d1d3631d5df357d3df29114` | Binding Voice-provider boundary; current status from GitHub |
| `ADR-0005_Restricted_Deterministic_Intent_Mapping.pdf` | 6 | `92db78041240707928f0765cdb6873c4e224fee982c0eaf2cd7c1a92b0949c2a` | Binding deterministic-intent boundary; current status from GitHub |
| `ADR-0006_No_Backend_Account_or_Streaming.pdf` | 5 | `0d500e878742d0bb0e7a7ff90f25cd4ccd59e74b93e28f5414d370c7ea7a0baf` | Binding external-service/scope boundary; current status from GitHub |
| `Music_Copilot_System_Architecture_MVP_v1_EN.pdf` | 25 | `b056e3b8d3125e6660c2fd9431f4f883128673ed5ad1a7f3f5184dd179715bfb` | **Historical/proposed engineering input; not final architecture** |
| `Music_Copilot_Code_and_Technical_Documentation_MVP_v1_EN.pdf` | 54 | `1d776396a88aebacbdb8b813fe8c6fe82418c951f301762ac594c55a7cd22cf3` | Proposed technical blueprint; not implementation proof |
| `Music_Copilot_User_Flow_Phase_0_Foundation_v1.0_EN.pdf` | 25 | `2393f443e5239a689f96e63d33648a696676a59d821858e12a5c57676b0b38e7` | Historical flow foundation; current GitHub flows control completion |
| `Music_Copilot_User_Flow_Phase_1_First_Readiness_v1.0_EN(4).pdf` | 21 | `d30dbe83808123852e31e26e4268d4b2b82f579cf9aa75b390ccf07f18d58e1e` | Historical Cluster-A checkpoint; current GitHub flows control completion |

## 3. High-fidelity design consumption order

For Issue #17 design decisions, consume sources in this order:

1. **English PRD v1.0 + English MVP v1.0** — product scope, user promise, P0/P1/P2, screens, accessibility/privacy and exclusions.
2. **ADR-0001 through ADR-0006** — platform, playback, local data, Voice provider, deterministic intent and no-backend/account/streaming boundaries.
3. **Current final UF-01 through UF-13** — detailed behavior, entry/exit, error/recovery, permissions, Back and state semantics.
4. **GS-01 through GS-05** — connected end-to-end scenario behavior and safe terminal outcomes.
5. **Completed Issue #15 Wireframes** — approved low-fidelity information hierarchy, controls, navigation, state presentation and system-only boundaries.
6. **Final Issue #16 Software Architecture** — implementation-facing runtime ownership, lifecycle, state source, navigation/Voice-overlay ownership, data/privacy and handoff constraints.

This is a design-consumption order, not permission for a lower item to expand a higher source. Product scope remains controlled by the English PRD/MVP. For runtime ownership or architecture questions, the final Issue #16 baseline controls over the proposed System Architecture/Code PDFs in the attached archive.

Current ADR-0007/0008 decisions introduced during final Issue #16 architecture are consumed through the final GitHub Architecture/ADR register. They refine implementation structure/runtime policy and do not expand the English PRD/MVP product scope.

## 4. Frozen GitHub source set

The Phase 1 design baseline is frozen at repository commit `b4ac3bb3ccf299bd282e6d1ba3600b7136dd5669`.

| Source | Blob SHA | Status / role |
|---|---|---|
| [`docs/PRD.md`](../PRD.md) | `330e14fcf95f6789cbc0c379a5adf4d5814052e1` | Approved concise representation of English PRD |
| [`docs/MVP.md`](../MVP.md) | `c948c5d755255d60c8fba4c5608f52e3197cddc7` | Ready-for-planning concise representation of English MVP |
| [`docs/adr/README.md`](../adr/README.md) | `10ba6b92145985dd1470a2d5d342f2b07fedacc6` | Current final ADR register synchronized with Issue #16 |
| [`docs/USER_FLOWS.md`](../USER_FLOWS.md) | `ca7092d74a4343c269a178d7c7db47dbc28af2ac` | UF-01..UF-13 COMPLETE / PASS |
| [Golden Scenario index](../user-flows/golden-scenarios/README.md) | `30471af1d588e59044451225b078af0909f9aff6` | GS-01..GS-05 PASS / APPROVED / COMPLETE |
| [`docs/WIREFRAMES.md`](../WIREFRAMES.md) | `88023a869a79e764400f91044e719ce3c14e3df7` | Issue #15 PASS / merged / closed |
| [Wireframe Coverage Matrix](../wireframes/WIREFRAME_COVERAGE_MATRIX.md) | `d987f2c0320f215e97546121c62c7bd9f5bed554` | 126/126 canonical state disposition |
| [Phase 7 Traceability Register](../wireframes/PHASE_7_TRACEABILITY_REGISTER.json) | `3210e24e21e3981ac0fe82e48aa6830c35c16ef9` | Machine-readable 126-state mapping |
| [`docs/SOFTWARE_ARCHITECTURE.md`](../SOFTWARE_ARCHITECTURE.md) | `47b1927e3d1efcb86bd7487409c82243010175ad` | FINAL / APPROVED / MERGED — Issue #16 |
| [Issue #16 Source/Authority Register](../architecture/SOURCE_AND_AUTHORITY_REGISTER.md) | `91c9bd2d9933b95f289585d2cbb5bb215841256a` | Source hierarchy and attached-package audit |
| [Issue #17 delivery contract](../ISSUE_17_REPOSITORY_HIFI_CONTRACT_ADDENDUM_2026-10-03.md) | `187d7e49e19e2843c47b4fefde625800c278c9a4` | Active repository-native high-fidelity contract |
| [Design Delivery Plan](../DESIGN_DELIVERY_PLAN.md) | `bbb6ee541b1fda9fcaebc95a6d4058f7b6b3e601` | Gate-D delivery mechanism and constraints |

## 5. Historical-PDF versus current-GitHub rule

The attached System Architecture and Code/Technical PDFs are dated/proposed engineering inputs. They do **not** control the current architecture status.

For Issue #17:

- final runtime/service/state ownership comes from current `docs/SOFTWARE_ARCHITECTURE.md`;
- Accepted ADR history comes from the current ADR register;
- current UF/GS/Wireframe completion comes from GitHub;
- a dated PDF may explain provenance but cannot roll a completed GitHub gate backward;
- high-fidelity design may not invent a different architecture to match a historical diagram or sample.

## 6. Frozen design-surface families

Exactly ten design-surface families are in the MVP baseline:

| WF family | Surface | Priority note | High-fidelity role |
|---|---|---|---|
| `WF-01` | Splash / startup | P0 | High-fidelity startup/loading/recovery presentation |
| `WF-02` | Onboarding / education | P0 | High-fidelity onboarding/education presentation |
| `WF-03` | Permissions / capability recovery | P0 | App education/recovery + explicit system handoffs |
| `WF-04` | Home / Library / browse | P0 | Library readiness/browse states |
| `WF-05` | Search | P0 | Local search results/no-result/error presentation |
| `WF-06` | Full Player / Queue / playback surfaces | P0 + bounded P1/P2 branches | Playback/queue presentation; service remains authority |
| `WF-07` | Driving Mode | P0 + separable P1 hint | One-layer driving presentation, immediate Exit |
| `WF-08` | Voice Overlay | P0 | Overlay states; not a separate navigation destination |
| `WF-09` | Liked Songs | **P2 / removable** | Optional favorite-song presentation; must not block P0 |
| `WF-10` | Settings / Privacy | P0 control surface + bounded P1 | Settings, consent, local clear and privacy status |

No eleventh app destination may be introduced by high-fidelity styling. Android permission dialogs, Android Settings, notification/lock-screen/external controls and interruption/route transitions remain system-owned or annotated handoffs as recorded by Issue #15/#16.

## 7. State-disposition baseline

The controlling Issue #15 machine register contains:

- **126** canonical state IDs / **126 unique**;
- **106 P0**, **6 P1**, **14 P2**;
- **45 App-visible Wireframe** states;
- **61 Variant/annotation inside a Wireframe** states;
- **20 Android/System-only transition** states.

The complete initial high-fidelity disposition is frozen in:

- [HIFI_TRACEABILITY_MATRIX.md](HIFI_TRACEABILITY_MATRIX.md)
- [HIFI_TRACEABILITY_REGISTER.json](HIFI_TRACEABILITY_REGISTER.json)

Disposition rules:

- `app-frame` → dedicated reviewable high-fidelity visual coverage is required, with paired specification.
- `variant-annotation` → represent as a variant/annotation in the owning high-fidelity screen/component; do not invent a destination screen.
- `system-transition` → system-only annotation/handoff; do not create an app high-fidelity destination.

## 8. Non-negotiable design constraints inherited from sources

- Android 10+ native product; Compose implementation target.
- Local authorized audio is the product media source; no streaming/catalog/account/backend expansion.
- Service/MediaSession remains playback/queue/position authority; UI observes and sends commands.
- Voice is Push-to-Talk, bounded/deterministic, exactly six MVP intents; no open conversation or wake word.
- Voice overlay is transient presentation state, not a separate navigation branch.
- Driving Mode is one layer, high contrast, fixed controls, immediate Exit, minimum 48dp targets, no typing/deep navigation/long lists.
- One Retry for Voice timeout/failure paths; ambiguity uses bounded confirmation; manual control remains reachable.
- TTS/error copy is at most one short sentence where required.
- No raw audio by default; prohibited personal-media/voice data must not enter analytics/evidence.
- System dialogs/Settings/notification/lock/external controls retain system ownership.
- P2 Liked Songs remains removable and cannot block P0 delivery.
- Structural UX change returns to Issue #15/User Flow review; consequential architecture change requires Issue #16/new ADR review.

## 9. Phase 1 exit gate

Phase 1 passes only when:

- [x] Attached English package identity is fixed by SHA-256.
- [x] Product and delivery authorities are the English PRD/MVP.
- [x] ADR-0001..0006 are registered as binding attached-package architecture decisions with current GitHub status controlling.
- [x] Current UF-01..UF-13 are frozen as detailed behavior input.
- [x] GS-01..GS-05 are frozen as connected scenario input.
- [x] Completed Issue #15 Wireframes are frozen as structural design input.
- [x] Final Issue #16 Software Architecture is frozen as runtime/ownership/handoff authority.
- [x] Ten and only ten WF design-surface families are registered.
- [x] All 126 canonical states have explicit WF / UF / State / GS / Priority / Ownership fields.
- [x] Every canonical state has an initial high-fidelity disposition.
- [x] Missing state mappings: **0**.
- [x] Duplicate canonical state IDs: **0**.
- [x] Unassigned WF mappings: **0**.

**Phase 1 decision: PASS / COMPLETE / SOURCE-FROZEN.** High-fidelity visual design may begin only from this frozen input set or from a later reviewed baseline change.
