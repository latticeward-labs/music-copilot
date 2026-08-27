# GitHub-native Wireframe Workspace Index

**Physical workspace:** `docs/wireframes/`  
**Naming contract:** `WF-##_SCREEN_STATE`  
**Coverage authority:** [WIREFRAME_COVERAGE_MATRIX.md](WIREFRAME_COVERAGE_MATRIX.md)  
**Phase 4 frame register:** [app-states/README.md](app-states/README.md)  
**Phase 5 transition register:** [recovery-system/README.md](recovery-system/README.md)  
**Phase 8 review:** [review/README.md](review/README.md)  
**Active contract:** [GitHub-native Wireframe Contract Addendum](GITHUB_NATIVE_WIREFRAME_CONTRACT_ADDENDUM_2026-08-25.md)

This index is the repository equivalent of the former single-page Figma topology. Logical workspaces are directories or indexed artifact groups, while priority and ownership lanes are metadata fields rather than canvas position.

| Logical workspace | Repository-native location / package | Status |
|---|---|---|
| `00_Index` | `docs/WIREFRAMES.md`, this file and `README.md` | Active |
| `01_Foundations` | `docs/wireframes/foundations/` | Phase 3 PASS |
| `02_Splash` | `app-states/WF-01_*` | Phase 4 PASS |
| `03_Onboarding` | `app-states/WF-02_*` | Phase 4 PASS |
| `04_Permissions` | `app-states/WF-03_*` | Phase 4 PASS |
| `05_Home_Library` | `app-states/WF-04_*` | Phase 4 PASS |
| `06_Search` | `app-states/WF-05_*` | Phase 4 PASS |
| `07_Full_Player` | `app-states/WF-06_*` | Phase 4 PASS |
| `08_Driving_Mode` | `app-states/WF-07_*` | Phase 4 PASS |
| `09_Voice` | `app-states/WF-08_*` | Phase 4 PASS |
| `10_Liked_Songs_P2` | `app-states/WF-09_*` | Phase 4 PASS / P2 |
| `11_Settings_Privacy` | `app-states/WF-10_*` | Phase 4 PASS |
| `12_System_Transitions` | `recovery-system/WF-*_SYSTEM_*` paired records/diagrams | Phase 5 PASS / 20 System-only states |
| `13_Review` | `review/` evidence, findings, validation, Phase 8 PR/commit and Issue #15 | Phase 8 PASS |

## Phase 5 recovery contract

The [Phase 5 register](recovery-system/README.md) contains 13 paired Markdown/SVG transition artifacts covering all 20 canonical Android/System-only states. The [terminal matrix](recovery-system/RECOVERY_TERMINAL_MATRIX.md) restricts recovery to reachable Manual Playback controls or service-owned Safe Paused. Android surfaces remain handoffs, never app-owned destination screens.

## Priority and ownership lanes

Every artifact carries exactly one priority (`P0`, `P1`, `P2`) and one ownership disposition (`App`, `Hybrid`, `Android/System`, or the more specific owner from the Coverage Matrix). `System-only` is an ownership/classification lane, never an app screen.

## Required artifact pair

```text
WF-##_SCREEN_STATE.md
WF-##_SCREEN_STATE.svg
```

The Markdown file controls behavior and the SVG controls low-fidelity visual structure. Both are reviewed in the same PR.

