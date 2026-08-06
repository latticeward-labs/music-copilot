# Music Copilot - User Flow Phase 1 First Readiness

**Version:** 1.0 — corrected repository edition  
**Updated:** 04 August 2026  
**Product:** Music Copilot MVP v1.0  
**Decision:** Stage 1 Traceability Matrix for Cluster A — **PASS**  
**Final User Flow status:** **Not approved; Issue #3 remains open**  
**Tracking:** GitHub Issue #3

> This document records a limited documentation gate. It does not approve complete FR-01, ADR outcomes, Technical Spike evidence, the complete UF-01 through UF-13 set, Wireframes, final Software Architecture, or implementation.

## 1. Controlled authority

| Rank | Source | Controlled use |
|---:|---|---|
| 1 | `Music_Copilot_PRD_v1_EN_Clarified.pdf` | Official product problem, first-use journey, FR-01, privacy, accessibility, and scope authority |
| 2 | `Music_Copilot_MVP_v1_EN.pdf` | Official priority, User Story, required-state, acceptance, QA, and release authority |
| 3 | Phase 0 Foundation | Approved UF catalog, fixed drafting decisions, contradiction handling, and Technical Spike routing |
| 4 | ADR-0001 through ADR-0006 | Architectural influence only; every source remains Proposed, including revised proposals ADR-0001/0003 |
| 5 | System Architecture and Technical Documentation | Technical interpretation and source-of-truth seams; cannot expand PRD/MVP scope |
| 6 | Corrected repository records | Canonical current Cluster A and Stage 1 evidence until |

The earlier Phase 1 readiness document was not evidence for the traceability corrections. This repository edition is synchronized from the corrected canonical records rather than using the old document to validate itself.

## 2. Gate decision

| Gate | Current state | Correct meaning |
|---|---|---|
| Product scope / Issue #1 | Done | English PRD/MVP v1.0 scope remains locked |
| Phase 0 Foundation | Complete for drafting | UF catalog, decisions, and evidence routing exist |
| Stage 1 Traceability Matrix | **PASS** | UF-01, UF-02, and UF-03 each contain all ten required fields |
| FR-01 | **Partial** | Cluster A covers consent, scan, index, readiness, and recovery; UF-04 must complete browsing/search |
| Issue #2 / ADR governance | **Open** | All six ADRs remain Proposed until explicit Accepted/Revised outcomes are recorded |
| TS-01 / TS-03 / TS-06 | **Open** | No executed evidence is inferred |
| Issue #3 / complete User Flow | **Open** | UF-04 through UF-13 and final review remain unfinished |
| Wireframes / final architecture / code | **Not authorized** | Separate downstream gates |

## 3. Canonical artifact register

| Artifact | Repository path | Result |
|---|---|---|
| UF-01 | [Direct record](UF-01_FIRST_LAUNCH_AND_EDUCATION.md) / [diagram](mermaid/UF-01_FIRST_LAUNCH_AND_EDUCATION.md) | Ten-field traceability and replay invariants synchronized |
| UF-02 | [Direct record](UF-02_PERMISSION_RECOVERY.md) / [diagram](mermaid/UF-02_PERMISSION_DENIAL_AND_SETTINGS_RECOVERY.md) | Three-way Settings return, Not now, provider separation, manual fallback |
| UF-03 | [Direct record](UF-03_LIBRARY_READINESS.md) / [diagram](mermaid/UF-03_LIBRARY_READINESS.md) | Returning current-state bypass, truthful readiness, reconciliation |
| 01.5 | [Combined Cluster A Flow](mermaid/01_5_COMBINED_CLUSTER_A_FLOW.md) | Canonical connected behavior |
| 01.6 | [State Inventory and Traceability](mermaid/01_6_STATE_INVENTORY_AND_TRACEABILITY.md) | Ten-field matrix, Partial/Open states, Stage 1 PASS |
| 01.7 | [Review and Approval Record](mermaid/01_7_REVIEW_AND_APPROVAL_RECORD.md) | Limited PASS without false role/final approval |

## 4. Corrected state and recovery contract

| ID | Surface/boundary | Controlled behavior |
|---|---|---|
| S-01 | Splash | Loading/Migration Error; Retry; never fabricate library or playback state |
| S-02 | Onboarding | First Run/Incomplete Resume/Skip/Replay; replay preserves permissions, library, queue, and active playback |
| S-03 | Permissions | Granted, Denied-requestable, Permanently Denied; Retry/Not now; explicit Settings |
| S-04 | Home / Library | Scanning, Empty, Ready, Recoverable Failure, Revoked-Reconciled; verified current Ready/Empty bypasses unnecessary scan |
| CA-01 | Six-Command Education | Exactly PLAY, PAUSE, NEXT, PREVIOUS, SEEK_BACK, PLAY_ARTIST |
| CA-02 | Voice Access Boundary | Permission, provider availability, and cloud consent are distinct; all failures preserve manual controls |
| SYS-04 | Android Settings | On return: Granted, Denied-requestable, or Still permanently denied; no automatic loop |
| SYS-05 | Speech provider/cloud | Provider unavailable is not permission failure; cloud processing requires separate truthful consent if applicable |

## 5. Per-flow traceability matrix

| Flow ID | User goal | Priority | User Story | Functional Requirement | Screens/states | Influencing ADRs | Failure/recovery | Data/permissions | Open assumptions/evidence |
|---|---|---|---|---|---|---|---|---|---|
| UF-01 | Understand the product, finish/skip education, reach truthful readiness, retain manual control | P0 | US-01, US-02 | First-use journey; FR-01 partial; UF-04 required | S-01, S-02, S-04 handoff, CA-01, CA-02 | ADR-0001..0006 as mapped influences; all Proposed | Startup Retry, resume, non-mutating replay, permission/scan recovery, Voice fallback | Local flags; Android state; MediaStore; playback service observation; sensitive telemetry excluded | TS-03, Issue #2, final review |
| UF-02 | Recover from permission/provider/consent failure without crash, coercion, or manual-control loss | P0 | US-01 | FR-01 access precondition partial; FR-05 mic precondition only | S-03, CA-02, SYS-01..05, Empty/manual fallback | ADR-0001/0002/0003/0004/0006; all Proposed | Retry/Not now; Settings three-way return; provider/consent fallback; revocation reconciliation | Android permission authority; MediaStore vs derived index; JIT mic; deferred notification; local consent if applicable | TS-01/03/06, Issue #2, final review |
| UF-03 | Build/reconcile an account-free library and see truthful readiness | P0 | US-02 | FR-01 scan/index/readiness partial; UF-04 required | S-04 and UF-02 handoff | ADR-0001/0002/0003/0006; all Proposed | Initial/incremental work, Retry, preserve verified state, reconcile inaccessible media | MediaStore authority; derived index; Android access; playback service; non-authoritative telemetry | TS-06, Issue #2, UF-04, final review |

## 6. Requirement status

| Requirement/story | Mapping | Status after Stage 1 |
|---|---|---|
| PRD first-use journey | UF-01, UF-02, UF-03 | Covered at flow/specification level |
| FR-01 | UF-01, UF-02, UF-03, **UF-04** | **Partial** until UF-04 completes Songs/Artists/Albums/Folders browsing/search |
| US-01 | UF-01, UF-02 | Covered at flow/specification level; platform/provider evidence remains open |
| US-02 | UF-01, UF-03 | Covered at flow/specification level; TS-06 remains open |

No claim is made that FR-02 through FR-09 are complete.

## 7. ADR influence and governance

| ADR | Influence | Governance state |
|---|---|---|
| ADR-0001 | Native Android/state/platform permission surfaces | Proposed - Revised source |
| ADR-0002 | Playback service owns active playback | Proposed |
| ADR-0003 | MediaStore authority and derived/rebuildable index | Proposed - Revised source |
| ADR-0004 | Replaceable provider and failure/consent isolation | Proposed |
| ADR-0005 | Exactly six deterministic Voice intents | Proposed |
| ADR-0006 | No account/backend/sync/catalog/streaming/upload/download | Proposed |

Traceability PASS does not convert any ADR to Accepted or Revised. Issue #2 must record the reviewed outcomes and synchronize the ADR register.

## 8. Open evidence and review register

| Item | State | Blocking effect |
|---|---|---|
| UF-04 | Open | Complete FR-01 |
| TS-01 | Open | Final UF-06 notification/foreground behavior |
| TS-03 | Open | Provider-specific Voice/cloud behavior and copy |
| TS-06 | Open | Final storage/reconciliation implementation claims |
| Issue #2 / ADR register | Open | Approved-architecture claims |
| UF-04 through UF-13 / Issue #3 | Open | Complete User Flow approval |
| Final Product/Design/Android/QA/Privacy/Governance sign-off | Not recorded | No role approval may be inferred |

## 9. Correct interpretation of PASS

The matrix is complete, explicit, internally consistent, and suitable for continuing controlled User Flow and Golden Scenario drafting. Stage 2 records remain Draft/Planned until their own gate is passed.

The PASS does not authorize Wireframes, final architecture, foundation implementation, production code, or Issue #3 closure.
