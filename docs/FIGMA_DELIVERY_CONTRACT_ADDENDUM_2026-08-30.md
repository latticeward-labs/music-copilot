# Figma Delivery Contract Addendum — Issue #17

- **Date:** 30 August 2026
- **Tracking:** [Issue #17](https://github.com/latticeward-labs/music-copilot/issues/17)
- **Branch:** `issue-17/figma-delivery-contract`
- **Status:** **ACTIVE — supersedes prior No-Figma delivery decisions for Issue #17 only**
- **Figma workfile:** [Music Copilot — Issue 17 Design Workfile](https://www.figma.com/design/2vwiPWEwRg3LdKkveMFRqV)
- **Figma file key:** `2vwiPWEwRg3LdKkveMFRqV`
- **GitHub repository:** `latticeward-labs/music-copilot`
- **Product authorities:** `docs/PRD.md` and `docs/MVP.md`

> Figma is the canonical visual design, component-library and interactive-prototype workspace. GitHub remains authoritative for product behavior, architecture, traceability, review evidence and approval history.

## 1. Purpose

This addendum formally activates Figma as the canonical visual delivery workspace for Issue #17 while preserving GitHub as the controlling behavioral, architecture, traceability, review and approval record.

The change is intentionally scoped to **Issue #17 — high-fidelity UI system, clickable prototype and handoff**. It does **not** retroactively alter the closed Issue #15 low-fidelity delivery record or delete any historical No-Figma evidence.

## 2. Authority split

| Concern | Canonical authority | Synchronization rule |
|---|---|---|
| Product scope, priority and acceptance semantics | GitHub PRD/MVP + Issue #17 | Figma must implement, not reinterpret, approved behavior. |
| User Flow, State and Golden Scenario behavior | GitHub | Figma frames/components/prototype interactions reference stable UF/State/GS IDs. |
| Architecture, lifecycle, ownership and runtime policy | GitHub architecture baseline + Accepted ADRs | Visual designs cannot invent runtime ownership or business logic. |
| Low-fidelity structure | Closed Issue #15 repository wireframe artifacts | High-fidelity structural deviations return to the Wireframe/flow gate. |
| Visual design, design tokens, component library | **Figma** | GitHub records links, identifiers, review evidence and implementation notes. |
| High-fidelity screens and critical states | **Figma** | Every reviewable screen/state has repository traceability. |
| Interactive prototype | **Figma** | GS-01..GS-05 and required recovery paths are linked from GitHub. |
| Handoff annotations and implementation-facing visual specs | **Figma + GitHub evidence** | Figma owns visual/component specification; GitHub owns approval history and behavioral truth. |
| Review disposition and approval history | GitHub | Figma approval alone is never sufficient evidence. |

## 3. Conflict and precedence rule

If Figma conflicts with an approved GitHub behavioral, architecture, traceability or acceptance record, **GitHub controls until a reviewed change resolves the conflict**. The resolved behavior is then synchronized into Figma.

If a high-fidelity design requires a structural UX change, it is routed back to Issue #15 / the approved Wireframe-flow gate. If it requires a consequential architecture change, it is routed to Issue #16 or a new/superseding ADR. No such change is silently embedded in Figma.

## 4. Figma workspace registration

The canonical Issue #17 design workfile is:

- **URL:** https://www.figma.com/design/2vwiPWEwRg3LdKkveMFRqV
- **File key:** `2vwiPWEwRg3LdKkveMFRqV`
- **Plan:** authenticated Figma Starter team workspace

Because the active Starter plan supports at most three pages, Issue #17 uses a three-page canonical structure without losing any required delivery area:

| Page | Responsibility | Required contents |
|---|---|---|
| `00 — Contract & Foundations` | Delivery contract + Foundations | canonical contract frame, color/typography/spacing/layout/iconography foundations, accessibility rules, Compose-ready token naming. |
| `01 — Components & Hi-Fi` | Reusable component library + high-fidelity screens | variants/states, all ten MVP screens, approved critical/recovery states, Driving Mode and Voice state coverage. |
| `02 — Prototype & Handoff` | Clickable prototype + engineering handoff | GS-01..GS-05, recovery branches, interaction transitions, annotations, analytics allowance notes, implementation handoff/review markers. |

The page consolidation is an organizational choice only. It does not reduce Issue #17 scope or Acceptance Criteria.

## 5. Workspace responsibilities

### Figma owns

- visual foundations and design tokens;
- reusable components, variants and visual states;
- high-fidelity layouts for all ten required screens;
- Voice feedback states: Listening, Processing, Confirming, Executed and Failed;
- Driving Mode visual rules, stable placement and interaction targets;
- clickable prototype routes for GS-01 through GS-05 and required recoveries;
- visual accessibility annotations;
- implementation-ready component/layout/prototype inspection.

### GitHub owns

- PRD/MVP scope and priority;
- User Flow, State and Golden Scenario truth;
- architecture and ADR truth;
- Wireframe structural baseline and change gate;
- P0/P1/P2 and removable-branch status;
- privacy, analytics and system-only boundaries;
- review findings, reviewer/date evidence and approval history;
- Issue #17 Acceptance Criteria and final gate status.

## 6. Historical No-Figma records

Historical No-Figma documents are **preserved**. They are not deleted or rewritten to pretend that Figma was always required.

For Issue #17, any earlier statement equivalent to:

- `Figma: N/A / Not Required`,
- `no Figma dependency`,
- `no external-workfile acceptance criterion`, or
- repository-only high-fidelity/prototype delivery

is superseded by this dated addendum from **30 August 2026 onward**.

Historical Issue #15 records remain valid evidence for the period and gate they describe. The closed low-fidelity work remains repository-native unless separately reopened by an approved change.

## 7. Issue #17 acceptance interpretation

Issue #17 Acceptance Criteria are interpreted under this contract as follows:

1. Figma is required for canonical visual design, component library and prototype delivery.
2. GitHub remains required for behavior, architecture, traceability, review evidence and approval.
3. A Figma artifact without GitHub traceability/review evidence does not pass.
4. A GitHub behavioral specification without the required corresponding Figma visual/prototype artifact does not pass visual/prototype criteria.
5. Structural UX or architecture changes must follow their existing upstream gates.
6. Final approval requires the Figma workfile and repository evidence to be synchronized.

## 8. Phase 0 evidence

- [x] `Figma: N/A / Not Required` removed from the active Issue #17 contract.
- [x] Figma activated as canonical visual/component/prototype workfile.
- [x] GitHub retained as behavioral/architecture/traceability/review/approval authority.
- [x] Canonical Figma workfile created and file key registered.
- [x] Canonical Figma pages created and responsibilities registered.
- [x] Historical No-Figma records preserved and superseded prospectively.
- [x] Dedicated Issue #17 branch created: `issue-17/figma-delivery-contract`.
- [x] `DESIGN_DELIVERY_PLAN.md` updated on the Issue #17 branch.
- [x] Issue #17 contract updated for the new authority model.

## 9. Phase 0 exit gate

**PASS condition:** no active Issue #17 Acceptance Criterion or delivery-plan statement may require repository-only high-fidelity delivery or deny the Figma dependency. Figma and GitHub must have non-overlapping, explicit authority responsibilities with a documented conflict-resolution rule.

**Disposition:** Phase 0 contract alignment is **PASS at branch level** once the addendum and delivery-plan changes are committed and Issue #17 body is synchronized. Project-board field status, if used, remains a separate GitHub Project metadata operation and must not be inferred from the issue body.