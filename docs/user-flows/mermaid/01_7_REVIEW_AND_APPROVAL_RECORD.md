# 01.7 - Review and Approval Record

- **Product:** Music Copilot MVP v1.0
- **Phase:** User Flow Phase 1 - Cluster A First Readiness
- **Coverage:** UF-01, UF-02, UF-03, corrected `01.5` combined flow, corrected `01.6` state inventory and traceability
- **Figma source:** `01.7 / Review and Approval Record` - node `23:31`
- **Repository source:** [`01_7_REVIEW_AND_APPROVAL_RECORD.mmd`](01_7_REVIEW_AND_APPROVAL_RECORD.mmd)
- **Status:** Cluster A complete for review and repository registration; complete User Flow approval not granted
- **Tracking:** GitHub Issue #3
- **Approval boundary:** This record does not close Issue #3, approve UF-04 through UF-13, authorize low-fidelity Wireframes, approve final Software Architecture, or authorize implementation.

## 1. Decision summary

Cluster A is complete for review and repository registration because the following implementation-independent artifacts exist and are connected:

- UF-01 - First Launch and Education.
- UF-02 - Permission Denial and Settings Recovery.
- UF-03 - Local Scan and Library Readiness.
- Corrected `01.5 / Combined Cluster A Flow`.
- Corrected `01.6 / State Inventory and Traceability`.
- Privacy, analytics, accessibility, source-of-truth, recovery, and scope contracts.

This is not final approval of the complete MVP v1.0 User Flow set. GitHub Issue #2 remains open, GitHub Issue #3 remains open, UF-04 through UF-13 remain incomplete, and the named technical evidence gates remain open.

## 2. Authority and scope

Authority order for this review:

1. English PRD v1.0.
2. English MVP v1.0.
3. Phase 0 Foundation decisions and routed evidence gates.
4. Proposed ADR-0001 through ADR-0006 as constraints, not substitutes for current GitHub approval evidence.
5. System Architecture MVP v1.0 and Technical Blueprint MVP v1.0 as technical interpretation.
6. GitHub and Figma as workflow, state, traceability, and review evidence.

Fixed scope:

- Native Android 10+.
- User-authorized local audio.
- Push-to-Talk and six restricted commands.
- No account, authentication, backend, cloud sync, online catalog, streaming, upload, download, distribution, Android Auto, iOS, or v1.1 capability.
- No raw audio or personal media metadata in telemetry by default.

## 3. Reviewed artifact register

| ID | Artifact | Review result |
|---|---|---|
| `F-02 / G-UF01` | UF-01 First Launch and Education | Complete for Cluster A review. |
| `F-03 / G-UF02` | UF-02 Permission Recovery | Complete for Cluster A review; TS-01, TS-03, and TS-06 remain evidence gates where applicable. |
| `F-04 / G-UF03` | UF-03 Library Readiness | Complete for Cluster A review; TS-06 remains open. |
| `F-05 / 01.5` | Connected Cluster A flow | Corrected canonical repository flow; Figma must match the corrected resume, replay, permission recheck, returning-user, and cloud-consent branches. |
| `F-06 / 01.6` | State Inventory and Traceability | Corrected canonical repository record; Figma must include CA-01, CA-02, SYS-05, D-005, D-016, D-028, D-030, and TS-03. |
| `F-07 / 01.7` | Review and Approval Record | This file is the canonical repository review record and approval-gate diagram. |

## 4. Role-by-role review record

| Role | Current result | Accepted now | Still open |
|---|---|---|---|
| Product Owner | **Pass for Cluster A review** | MVP v1.0 scope, user-visible behavior, recovery paths, state ownership, and Cluster B exit direction. | Final approval of the complete UF-01 through UF-13 set. |
| Product Design | **Pass with synchronization action** | Cluster A coverage, terminal states, recovery behavior, and implementation-independent representation. | Synchronize Figma `01.5`, `01.6`, and `01.7` with the corrected canonical repository artifacts. |
| Android Engineering | **Conditional** | User-visible Android boundaries are recorded without inventing final implementation. | Issue #2 evidence and ADR-register alignment; TS-01, TS-03, and TS-06 validation. |
| QA | **Conditional** | Required happy, denial, empty, error, revocation, retry, and fallback scenarios are recorded. | Executed evidence across Android versions, OEM devices, and applicable provider/storage conditions. |
| Privacy | **Conditional pass** | Just-in-time microphone, no raw-audio default, no personal-media telemetry, manual fallback, and separate cloud-speech disclosure boundary. | TS-03 provider destination, purpose, retention, availability, network dependence, and fallback evidence. |
| Governance | **Open** | Cluster A may proceed to the next drafting cluster. | Issue #2 remains open; Issue #3 remains open; final cross-role approval is not recorded. |

## 5. Cluster A Definition of Done

| Result | Criterion |
|---|---|
| `PASS` | UF-01, UF-02, and UF-03 exist in Figma and GitHub. |
| `PASS` | Corrected connected Cluster A flow exists in `01.5`. |
| `PASS` | Denied and Permanently Denied are separate. |
| `PASS` | Android Settings opens only after explicit user action and return always rechecks the real condition. |
| `PASS` | Incomplete first use resumes from the pending step; Settings replay is non-mutating. |
| `PASS` | Continue and Skip record onboarding completion; Skip never forces microphone or notification access. |
| `PASS` | Returning users do not enter visible Scanning when a verified current state already exists. |
| `PASS` | Initial/incremental scan, Scanning, Empty, Ready, recoverable failure, and revoked/reconciled states are present. |
| `PASS` | Six-command education and just-in-time microphone behavior are explicit. |
| `PASS` | Manual controls remain after microphone denial, provider unavailability, or cloud-consent decline. |
| `PASS` | Corrected state inventory, traceability, privacy, accessibility, and source-of-truth records exist in `01.6`. |
| `PASS` | P0 scope is explicit and no v1.1 or excluded capability is introduced. |
| `SYNC` | Figma `01.5`, `01.6`, and `01.7` must be synchronized with their corrected repository records. |
| `OPEN` | TS-01 evidence. |
| `OPEN` | TS-03 evidence. |
| `OPEN` | TS-06 evidence. |
| `OPEN` | UF-04 through UF-13. |
| `OPEN` | Issue #2 / ADR register alignment and final complete-flow approval. |

## 6. Open gates

### Issue #2 and ADR register

GitHub Issue #2 is open. The ADR review has partial checked criteria, but repository evidence, explicit open assumptions, final register alignment, and closure criteria remain incomplete. Cluster A may remain complete for review, but complete User Flow approval remains blocked until Issue #2 and the ADR register agree.

### TS-01

Validate notification permission, foreground-media-service visibility, denial, recovery, and Settings behavior across Android 10, Android 12, and Android 14+.

### TS-03

Validate voice-provider availability, supported language behavior, network dependence, processing destination, separate consent, retention disclosure, provider-unavailable behavior, and manual fallback.

### TS-06

Validate MediaStore and scoped-storage permission changes, moved/deleted media, incremental reconciliation, interruption recovery, and rebuild behavior across supported Android versions.

### Remaining User Flows

GitHub Issue #3 is open. UF-04 through UF-13 and final cross-role review remain required before the complete User Flow set can be approved.

### Downstream gates

Low-fidelity Wireframes, final Software Architecture, Android foundation work, and implementation are not authorized by this record.

## 7. Approval semantics

| Term | Meaning in this project |
|---|---|
| **Pass for Cluster A review** | The reviewed Cluster A user-visible behavior and documentation are sufficient to continue User Flow drafting. |
| **Conditional** | The flow boundary is usable for drafting, but named technical or executed evidence remains open. |
| **Complete for repository registration** | Durable canonical records exist in GitHub and can be referenced by later work. |
| **Final complete-flow approval** | UF-01 through UF-13, cross-role approval, Issue #2/ADR alignment, and all required evidence gates are complete. This status is not granted here. |

## 8. Exit decision

**Approved next action:** proceed to Cluster B drafting:

- UF-04 - Browse and Search Local Music.
- UF-05 - Manual Playback and Queue Control.
- UF-06 - Background, Screen-off, and System Controls.

This exit decision preserves Issue #3 as open and does not authorize Wireframes, final architecture, or implementation.

## 9. Canonical approval-gate diagram

```mermaid
%%{init: {"theme": "base", "flowchart": {"curve": "stepAfter", "nodeSpacing": 52, "rankSpacing": 70, "htmlLabels": true}, "themeVariables": {"fontFamily": "Inter, Arial, sans-serif", "fontSize": "14px", "lineColor": "#667085", "textColor": "#1F2633"}}}%%
flowchart TB

    subgraph INPUTS["A. REVIEW INPUTS - CLUSTER A"]
        direction LR
        UF01["UF-01<br/>First Launch and Education"]
        UF02["UF-02<br/>Permission Recovery"]
        UF03["UF-03<br/>Library Readiness"]
        FLOW["01.5 Combined Cluster A Flow<br/>Corrected canonical repository flow"]
        STATE["01.6 State Inventory and Traceability<br/>Corrected canonical repository record"]
        PRIV["Privacy, analytics, accessibility<br/>Cluster A constraints and recovery"]
    end

    INPUTS_READY{"Required Cluster A artifacts complete and internally consistent?"}
    UF01 --> INPUTS_READY
    UF02 --> INPUTS_READY
    UF03 --> INPUTS_READY
    FLOW --> INPUTS_READY
    STATE --> INPUTS_READY
    PRIV --> INPUTS_READY

    INPUTS_READY -->|No| REWORK["Return to affected artifact<br/>Correct scope, state, traceability, recovery, or approval wording"]
    REWORK --> INPUTS_READY
    INPUTS_READY -->|Yes| ROLE_REVIEW

    subgraph ROLE_REVIEW["B. ROLE-BY-ROLE REVIEW RECORD"]
        direction LR
        PRODUCT["Product Owner<br/>PASS FOR CLUSTER A REVIEW<br/>User-visible behavior, recovery, scope, and exit direction accepted"]
        DESIGN["Product Design<br/>PASS WITH SYNCHRONIZATION ACTION<br/>Repository artifacts complete; Figma 01.5 / 01.6 / 01.7 must match corrected records"]
        ANDROID["Android Engineering<br/>CONDITIONAL<br/>User-visible boundaries recorded; TS-01, TS-03, TS-06 and Issue #2 evidence remain open"]
        QA["QA<br/>CONDITIONAL<br/>Required scenarios recorded; execution evidence across versions/devices remains open"]
        PRIVACY["Privacy<br/>CONDITIONAL PASS<br/>No raw audio/personal media telemetry; provider/cloud disclosure evidence remains TS-03"]
    end

    PRODUCT --> CLUSTER_DECISION
    DESIGN --> CLUSTER_DECISION
    ANDROID --> CLUSTER_DECISION
    QA --> CLUSTER_DECISION
    PRIVACY --> CLUSTER_DECISION

    CLUSTER_DECISION{"Cluster A complete for review and repository registration?"}
    CLUSTER_DECISION -->|No| REWORK
    CLUSTER_DECISION -->|Yes| CLUSTER_COMPLETE["CLUSTER A RESULT<br/>Complete for review and repository registration<br/>Not final approval of the complete User Flow set"]

    subgraph OPEN_GATES["C. OPEN GATES BEFORE FINAL COMPLETE-FLOW APPROVAL"]
        direction LR
        ISSUE2["Issue #2 / ADR register<br/>OPEN - alignment and repository evidence required"]
        TS01["TS-01 OPEN<br/>Notification and foreground-media behavior across Android 10 / 12 / 14+"]
        TS03["TS-03 OPEN<br/>Voice provider availability, network dependence, processing destination, consent, retention, fallback"]
        TS06["TS-06 OPEN<br/>Media permission changes, move/delete, reconciliation, rebuild across supported versions"]
        REMAINING["Issue #3 OPEN<br/>UF-04 through UF-13 and final cross-role review remain"]
        DOWNSTREAM["Issue #15 / #16 / implementation<br/>NOT AUTHORIZED by this decision"]
    end

    CLUSTER_COMPLETE --> ISSUE2
    CLUSTER_COMPLETE --> TS01
    CLUSTER_COMPLETE --> TS03
    CLUSTER_COMPLETE --> TS06
    CLUSTER_COMPLETE --> REMAINING
    CLUSTER_COMPLETE --> DOWNSTREAM

    FINAL_GATE{"All complete-flow gates closed and final cross-role approval recorded?"}
    ISSUE2 --> FINAL_GATE
    TS01 --> FINAL_GATE
    TS03 --> FINAL_GATE
    TS06 --> FINAL_GATE
    REMAINING --> FINAL_GATE

    FINAL_GATE -->|No| BLOCKED["FINAL USER FLOW APPROVAL BLOCKED<br/>Keep Issue #3 open<br/>Do not start low-fidelity Wireframes, final architecture, or implementation as approved work"]
    FINAL_GATE -->|Yes - future state only| FINAL_APPROVAL["FINAL COMPLETE USER FLOW APPROVAL<br/>Future decision; not granted by Cluster A record"]

    CLUSTER_COMPLETE --> EXIT["PHASE 1 EXIT DECISION<br/>Proceed with Cluster B drafting: UF-04 Browse/Search, UF-05 Manual Playback, UF-06 Background/System Controls<br/>No expansion beyond MVP v1.0"]

    AUTHORITY["AUTHORITY AND SCOPE<br/>English PRD v1.0 and MVP v1.0 remain product authorities<br/>No account, backend, sync, catalog, streaming, upload, download, Android Auto, iOS, or v1.1 scope"]
    CLUSTER_COMPLETE -. constrained by .-> AUTHORITY
    FINAL_GATE -. constrained by .-> AUTHORITY

    classDef input fill:#EAF2FF,stroke:#3776D6,color:#1F2633,stroke-width:1.5px;
    classDef decision fill:#FFF4D6,stroke:#C68817,color:#1F2633,stroke-width:1.5px;
    classDef pass fill:#EAF8EE,stroke:#2E8B4B,color:#1F2633,stroke-width:1.5px;
    classDef conditional fill:#F2EBFF,stroke:#7650C7,color:#1F2633,stroke-width:1.5px;
    classDef open fill:#FFE8E8,stroke:#C63B3B,color:#1F2633,stroke-width:1.5px;
    classDef note fill:#EEF1F5,stroke:#667085,color:#1F2633,stroke-width:1.5px;
    classDef final fill:#F4F7FB,stroke:#2E8B4B,color:#1F2633,stroke-width:2px;

    class UF01,UF02,UF03,FLOW,STATE,PRIV input;
    class INPUTS_READY,CLUSTER_DECISION,FINAL_GATE decision;
    class PRODUCT,CLUSTER_COMPLETE,EXIT pass;
    class DESIGN,ANDROID,QA,PRIVACY conditional;
    class ISSUE2,TS01,TS03,TS06,REMAINING,DOWNSTREAM,BLOCKED,REWORK open;
    class AUTHORITY note;
    class FINAL_APPROVAL final;

    style INPUTS fill:transparent,stroke:#C7CED9,stroke-width:1px
    style ROLE_REVIEW fill:transparent,stroke:#C7CED9,stroke-width:1px
    style OPEN_GATES fill:transparent,stroke:#C7CED9,stroke-width:1px
```

## 10. Change control

Any change that marks final User Flow approval, closes Issue #3, removes TS-01/TS-03/TS-06 without evidence, authorizes Wireframes/final architecture/implementation, or expands MVP v1.0 scope requires an explicit reviewed update to this record and the relevant GitHub issues/registers.
