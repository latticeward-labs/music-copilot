# GitHub-native Wireframe Workspace Index

**Physical workspace:** `docs/wireframes/`  
**Naming contract:** `WF-##_SCREEN_STATE`  
**Coverage authority:** [WIREFRAME_COVERAGE_MATRIX.md](WIREFRAME_COVERAGE_MATRIX.md)  
**Active contract:** [GitHub-native Wireframe Contract Addendum](GITHUB_NATIVE_WIREFRAME_CONTRACT_ADDENDUM_2026-08-25.md)

This index is the repository equivalent of the former single-page Figma topology. Logical workspaces are directories or indexed artifact groups, while priority and ownership lanes are metadata fields rather than canvas position.

| Logical workspace | Repository-native location / package | Status |
|---|---|---|
| `00_Index` | `docs/WIREFRAMES.md`, this file and `README.md` | Active |
| `01_Foundations` | `docs/wireframes/foundations/` | Phase 3 PASS |
| `02_Splash` | `WF-01_*` | Planned |
| `03_Onboarding` | `WF-02_*` | Planned |
| `04_Permissions` | `WF-03_*` | Planned |
| `05_Home_Library` | `WF-04_*` | Planned |
| `06_Search` | `WF-05_*` | Planned |
| `07_Full_Player` | `WF-06_*` | Planned |
| `08_Driving_Mode` | `WF-07_*` | Planned |
| `09_Voice` | `WF-08_*` | Planned |
| `10_Liked_Songs_P2` | `WF-09_*` | Planned / P2 |
| `11_Settings_Privacy` | `WF-10_*` | Planned |
| `12_System_Transitions` | `WF-*_SYSTEM_*` annotations and records | Planned / System-only |
| `13_Review` | PR evidence, validation records and Issue #15 | Active |

## Priority and ownership lanes

Every artifact carries exactly one priority (`P0`, `P1`, `P2`) and one ownership disposition (`App`, `Hybrid`, `Android/System`, or the more specific owner from the Coverage Matrix). `System-only` is an ownership/classification lane, never an app screen.

## Required artifact pair

```text
WF-##_SCREEN_STATE.md
WF-##_SCREEN_STATE.svg
```

The Markdown file controls behavior and the SVG controls low-fidelity visual structure. Both are reviewed in the same PR.

