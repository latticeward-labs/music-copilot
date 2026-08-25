# Wireframe Coverage Matrix — MVP v1.0

**Issue:** [#15 — Create and approve low-fidelity Wireframes](https://github.com/latticeward-labs/music-copilot/issues/15)  
**Inventory/sync date:** 25 August 2026  
**Status:** **PASS — coverage manifest complete; visual WF artifacts remain pending**  
**Canonical count:** **126/126 states classified; 0 missing; 0 duplicate; 0 unassigned**

## Authority and classification contract

This manifest was regenerated from the current GitHub-controlled UF-01 through UF-13 records, normalized State Inventories, and GS-01 through GS-05. GitHub remains the canonical behavioral/traceability/approval authority. [Figma — Music Copilot — MVP v1.0](https://www.figma.com/design/CC6I0e23Zwe6qMhRGaqRrT/Music-Copilot-%E2%80%94-MVP-v1.0?node-id=0-1) is the official editable low-fidelity visual workfile.

Every canonical State ID is assigned exactly one coverage class:

1. **App-visible Wireframe** — requires a visible app state in the named WF package.
2. **Variant/annotation inside a Wireframe** — behavior or transient state is represented inside the named WF package without inventing a destination screen.
3. **Android/System-only transition** — Android/platform-owned surface or lifecycle transition; the app WF shows only the handoff, return, or behavior annotation.

A GS value of **— (not on GS-01..05 path)** is explicit coverage metadata, not a missing value. It means the State is required by a UF/recovery contract but is not traversed by the five approved Golden Scenario paths.

## Planned WF package register

| Planned WF | Surface/package | Primary UF coverage |
|---|---|---|
| `WF-01` | Splash / startup | UF-01 |
| `WF-02` | Onboarding / education | UF-01 |
| `WF-03` | Permissions / capability recovery | UF-02/10/12 |
| `WF-04` | Home / Library / browse | UF-03/04/05 |
| `WF-05` | Search | UF-04 |
| `WF-06` | Full Player / Queue / playback surfaces | UF-05/06/11/13 |
| `WF-07` | Driving Mode | UF-07/11 |
| `WF-08` | Voice overlay | UF-08/09/10 |
| `WF-09` | Liked Songs (P2) | UF-13 |
| `WF-10` | Settings / Privacy | UF-12 |

## Coverage totals

| Check | Result |
|---|---:|
| Canonical State IDs | **126** |
| App-visible Wireframe | **45** |
| Variant/annotation inside a Wireframe | **61** |
| Android/System-only transition | **20** |
| P0 | **106** |
| P1 | **6** |
| P2 | **14** |
| Missing classification / owner / WF | **0** |
| Duplicate State IDs | **0** |

## Golden Scenario attachment summary

The counts below are strict references found in the approved GS Markdown records. States outside those explicit paths remain covered through their owning UF and recovery contract.

| Golden Scenario | Explicit canonical State references | Planned WF packages |
|---|---:|---|
| GS-01 First launch to ready library | 10 | `WF-01`, `WF-02`, `WF-03`, `WF-04` |
| GS-02 Find music and start playback | 9 | `WF-04`, `WF-05`, `WF-06` |
| GS-03 Continue playback outside app | 12 | `WF-06` plus Android/system annotations |
| GS-04 Drive and execute direct Voice | 18 | `WF-07`, `WF-08` |
| GS-05 Recover safely from interruption | 15 | `WF-06`, `WF-07`, `WF-10` plus Android/system annotations |

## Source inventory

- [Complete UF-01 through UF-13 traceability](../user-flows/PHASE_1_TRACEABILITY_MATRIX.md)
- [Cluster A normalized states](../user-flows/mermaid/01_6_STATE_INVENTORY_AND_TRACEABILITY.md)
- [Cluster B State Inventory](../user-flows/CLUSTER_B_STATE_INVENTORY.md)
- [Cluster C State Inventory](../user-flows/CLUSTER_C_STATE_INVENTORY.md)
- [Cluster D State Inventory](../user-flows/CLUSTER_D_STATE_INVENTORY.md)
- [Cluster E State Inventory](../user-flows/CLUSTER_E_STATE_INVENTORY.md)
- [UF-13 State Inventory](../user-flows/UF-13_STATE_INVENTORY.md)
- [Stage 2 Golden Scenario traceability](../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md)

## Cluster A — UF-01 to UF-03

| UF | GS | State ID | State | Priority | Screen owner | Recovery owner | App/System ownership | Coverage class | Planned WF |
|---|---|---|---|---:|---|---|---|---|---|
| UF-01 | GS-01 | `S-01` | Splash - Loading, Migration Error | P0 | UF-01 · Splash | UF-01 | App | App-visible Wireframe | `WF-01` |
| UF-01 | GS-01 | `S-02` | Onboarding - First Run, Incomplete Resume, Skip, Replay | P0 | UF-01 · Onboarding/Education | UF-01 | App | App-visible Wireframe | `WF-02` |
| UF-02 | GS-01 | `S-03` | Permissions - Granted, Denied-requestable, Permanently Denied | P0 | UF-02 · Permission recovery | UF-02 | Hybrid | App-visible Wireframe | `WF-03` |
| UF-03 | GS-01 | `S-04` | Home / Library - Scanning, Empty, Ready, Recoverable Failure, Revoked-Reconciled | P0 | UF-03 · Home/Library readiness | UF-03 | App | App-visible Wireframe | `WF-04` |
| UF-01 | GS-01 | `CA-01` | Six-Command Education - View, Skip, Replay, Practice Choice | P0 | UF-01 · Onboarding/Education | UF-01 | App | App-visible Wireframe | `WF-02` |
| UF-02 | GS-01 | `CA-02` | Voice Access Boundary - Mic Purpose, Granted, Denied-requestable, Permanently Denied, Provider Unavailable, Cloud Consent | P0 | UF-02 · Voice access boundary | UF-02 / UF-10 | Hybrid | Variant/annotation inside a Wireframe | `WF-03` |
| UF-02 | GS-01 | `SYS-01` | Android media permission dialog | P0 | Android permission/Settings | UF-02 | Android/System | Android/System-only transition | `WF-03 (handoff annotation)` |
| UF-02 | — (not on GS-01..05 path) | `SYS-02` | Android microphone permission dialog | P0 | Android permission/Settings | UF-02 | Android/System | Android/System-only transition | `WF-03 (handoff annotation)` |
| UF-06 | — (not on GS-01..05 path) | `SYS-03` | Notification permission boundary | P0 | Android notification permission | UF-06 / UF-12 | Android/System | Android/System-only transition | `WF-06 (handoff annotation)` |
| UF-02 | GS-01 | `SYS-04` | Android Settings | P0 | Android permission/Settings | UF-02 | Android/System | Android/System-only transition | `WF-03 (handoff annotation)` |
| UF-02 / UF-10 / UF-12 | GS-01 | `SYS-05` | Speech provider/cloud-processing boundary | P0 | Speech provider / consent boundary | UF-10 / UF-12 | Android/System + provider | Android/System-only transition | `WF-03 (boundary annotation)` |

## Cluster B — UF-04 to UF-06

| UF | GS | State ID | State | Priority | Screen owner | Recovery owner | App/System ownership | Coverage class | Planned WF |
|---|---|---|---|---:|---|---|---|---|---|
| UF-04 | GS-01, GS-02 | `CB-01` | Home / Library Ready | P0 | UF-04 · Home/Library | UF-03 / UF-04 | App | App-visible Wireframe | `WF-04` |
| UF-04 | GS-02 | `CB-02` | Songs | P0 | UF-04 · Browse | UF-04 | App | Variant/annotation inside a Wireframe | `WF-04` |
| UF-04 | — (not on GS-01..05 path) | `CB-03` | Artists | P0 | UF-04 · Browse | UF-04 | App | Variant/annotation inside a Wireframe | `WF-04` |
| UF-04 | — (not on GS-01..05 path) | `CB-04` | Artist tracks | P0 | UF-04 · Browse | UF-04 | App | Variant/annotation inside a Wireframe | `WF-04` |
| UF-04 | — (not on GS-01..05 path) | `CB-05` | Albums | P0 | UF-04 · Browse | UF-04 | App | Variant/annotation inside a Wireframe | `WF-04` |
| UF-04 | — (not on GS-01..05 path) | `CB-06` | Album tracks | P0 | UF-04 · Browse | UF-04 | App | Variant/annotation inside a Wireframe | `WF-04` |
| UF-04 | — (not on GS-01..05 path) | `CB-07` | Folders | P0 | UF-04 · Browse | UF-04 | App | Variant/annotation inside a Wireframe | `WF-04` |
| UF-04 | — (not on GS-01..05 path) | `CB-08` | Folder tracks | P0 | UF-04 · Browse | UF-04 | App | Variant/annotation inside a Wireframe | `WF-04` |
| UF-04 | — (not on GS-01..05 path) | `CB-09` | Search Idle / Query | P0 | UF-04 · Search | UF-04 | App | App-visible Wireframe | `WF-05` |
| UF-04 | — (not on GS-01..05 path) | `CB-10` | Search Result | P0 | UF-04 · Search | UF-04 | App | App-visible Wireframe | `WF-05` |
| UF-04 | GS-02 | `CB-11` | Search No Result | P0 | UF-04 · Search | UF-04 | App | App-visible Wireframe | `WF-05` |
| UF-04 | GS-02 | `CB-12` | Search Error | P0 | UF-04 · Search | UF-04 | App | App-visible Wireframe | `WF-05` |
| UF-04 | GS-02 | `CB-13` | Selection Validation | P0 | UF-04 · Browse/Search selection | UF-02 / UF-03 / UF-04 | Hybrid | Variant/annotation inside a Wireframe | `WF-04 / WF-05` |
| UF-04 → UF-05 | GS-02 | `CB-14` | Playback Start / Queue Acceptance / Preparing | P0 | UF-05 · Full Player | UF-05 | App + Playback Service | Variant/annotation inside a Wireframe | `WF-06` |
| UF-05 | GS-02, GS-03 | `CB-15` | Full Player Playing | P0 | UF-05 · Full Player/Queue | UF-05 | App + Playback Service | App-visible Wireframe | `WF-06` |
| UF-05 | GS-03 | `CB-16` | Full Player Paused | P0 | UF-05 · Full Player/Queue | UF-05 | App + Playback Service | App-visible Wireframe | `WF-06` |
| UF-05 | — (not on GS-01..05 path) | `CB-17` | End of Queue / Paused-Completed | P0 | UF-05 · Full Player/Queue | UF-05 | App + Playback Service | App-visible Wireframe | `WF-06` |
| UF-05 | — (not on GS-01..05 path) | `CB-18` | Queue View | P0 | UF-05 · Full Player/Queue | UF-05 | App + Playback Service | App-visible Wireframe | `WF-06` |
| UF-05 | GS-02 | `CB-19` | Corrupt / Inaccessible Item Recovery | P0 | UF-05 · Full Player | UF-03 / UF-05 / UF-11 | App + Playback Service | Variant/annotation inside a Wireframe | `WF-06` |
| UF-05 | — (not on GS-01..05 path) | `CB-20` | Home Mini-player | P0 | UF-05 · Mini-player on Home | UF-05 / UF-06 | App + Playback Service | Variant/annotation inside a Wireframe | `WF-04 / WF-06` |
| UF-05 / UF-06 | GS-02, GS-03 | `CB-21` | Controller Connecting / Reconnecting | P0 | UF-05/06 · Controller connection | UF-06 | App + Playback Service | Variant/annotation inside a Wireframe | `WF-06` |
| UF-06 | GS-03 | `CB-22` | Active Session Handoff | P0 | Android lifecycle/background | UF-06 | Android/System + Playback Service | Android/System-only transition | `WF-06 (lifecycle annotation)` |
| UF-06 | GS-03 | `CB-23` | App Background / UI Absent | P0 | Android lifecycle/background | UF-06 | Android/System + Playback Service | Android/System-only transition | `WF-06 (lifecycle annotation)` |
| UF-06 | GS-03 | `CB-24` | Screen Off / Device Locked | P0 | Android lifecycle/background | UF-06 | Android/System + Playback Service | Android/System-only transition | `WF-06 (lifecycle annotation)` |
| UF-06 | GS-03 | `CB-25` | Task / OEM Lifecycle Event | P0 | Android lifecycle/background | UF-06 | Android/System + Playback Service | Android/System-only transition | `WF-06 (lifecycle annotation)` |
| UF-06 | GS-03 | `CB-26` | Media Notification / System Controls | P0 | Android/system media surface | UF-06 | Android/System + MediaSession | Android/System-only transition | `WF-06 (system-surface annotation)` |
| UF-06 | — (not on GS-01..05 path) | `CB-27` | Lock-screen Controls | P0 | Android/system media surface | UF-06 | Android/System + MediaSession | Android/System-only transition | `WF-06 (system-surface annotation)` |
| UF-06 | — (not on GS-01..05 path) | `CB-28` | Bluetooth / Headset / Vehicle Media Keys | P0 | Android/system media surface | UF-06 | Android/System + MediaSession | Android/System-only transition | `WF-06 (system-surface annotation)` |
| UF-06 | GS-03 | `CB-29` | System Command Pending | P0 | System command handoff | UF-06 | Android/System + MediaSession | Android/System-only transition | `WF-06 (command annotation)` |
| UF-06 | GS-03 | `CB-30` | Notification Permission / Visibility Evaluation | P0 | Notification permission/visibility | UF-06 / UF-12 | Android/System | Android/System-only transition | `WF-06 (permission annotation)` |
| UF-06 | — (not on GS-01..05 path) | `CB-31` | System Surface Unavailable | P0 | UF-06 · System-surface fallback | UF-06 | Hybrid | Variant/annotation inside a Wireframe | `WF-06` |
| UF-05 / UF-06 | GS-03 | `CB-32` | P1 Paused Restore | P1 | UF-05/06 · Paused restore | UF-03 / UF-05 / UF-06 | App + Playback Service | Variant/annotation inside a Wireframe | `WF-06` |
| UF-05 / UF-06 | GS-03 | `CB-33` | Restore Unavailable | P1 | UF-05/06 · Paused restore | UF-03 / UF-05 / UF-06 | App + Playback Service | Variant/annotation inside a Wireframe | `WF-06` |

## Cluster C — UF-07

| UF | GS | State ID | State | Priority | Screen owner | Recovery owner | App/System ownership | Coverage class | Planned WF |
|---|---|---|---|---:|---|---|---|---|---|
| UF-07 | GS-04 | `CC-01` | Non-driving source | P0 | UF-07 · Driving entry | UF-07 | App + Playback Service | Variant/annotation inside a Wireframe | `WF-07` |
| UF-07 | GS-04 | `CC-02` | Manual entry pending | P0 | UF-07 · Driving entry | UF-07 | App + Playback Service | Variant/annotation inside a Wireframe | `WF-07` |
| UF-07 | GS-04 | `CC-03` | Controller reconnecting | P0 | UF-07 · Driving entry | UF-07 | App + Playback Service | Variant/annotation inside a Wireframe | `WF-07` |
| UF-07 | — (not on GS-01..05 path) | `CC-04` | Entry cancelled/failed | P0 | UF-07 · Driving entry | UF-07 | App + Playback Service | Variant/annotation inside a Wireframe | `WF-07` |
| UF-07 | GS-04 | `CC-05` | Driving Playing | P0 | UF-07 · Driving Mode | UF-07 | App + Playback Service | App-visible Wireframe | `WF-07` |
| UF-07 | GS-04 | `CC-06` | Driving Paused/Ended | P0 | UF-07 · Driving Mode | UF-07 | App + Playback Service | App-visible Wireframe | `WF-07` |
| UF-07 | GS-04 | `CC-07` | Driving No Track | P0 | UF-07 · Driving Mode | UF-07 | App + Playback Service | App-visible Wireframe | `WF-07` |
| UF-07 | — (not on GS-01..05 path) | `CC-08` | Playback command pending | P0 | UF-07 · Driving controls | UF-07 | App + Playback Service | Variant/annotation inside a Wireframe | `WF-07` |
| UF-07 | — (not on GS-01..05 path) | `CC-09` | Command rejected/timeout | P0 | UF-07 · Driving controls | UF-07 | App + Playback Service | Variant/annotation inside a Wireframe | `WF-07` |
| UF-07 | — (not on GS-01..05 path) | `CC-10` | Acknowledged refresh | P0 | UF-07 · Driving controls | UF-07 | App + Playback Service | Variant/annotation inside a Wireframe | `WF-07` |
| UF-07 / UF-12 | — (not on GS-01..05 path) | `CC-11` | P1 selected-device connected | P1 | UF-07 · Entry suggestion | UF-07 / UF-12 | Hybrid | App-visible Wireframe | `WF-07` |
| UF-07 | — (not on GS-01..05 path) | `CC-12` | Bluetooth suggestion accepted | P1 | UF-07 · Entry suggestion | UF-07 | App | Variant/annotation inside a Wireframe | `WF-07` |
| UF-07 | — (not on GS-01..05 path) | `CC-13` | Exit/back return | P0 | UF-07 · Exit/Back | UF-07 | App | Variant/annotation inside a Wireframe | `WF-07` |
| UF-07 → UF-08 | GS-04 | `CC-14` | Microphone handoff | P0 | UF-08 · Voice overlay | UF-08 / UF-10 | App | App-visible Wireframe | `WF-08` |
| UF-07 | — (not on GS-01..05 path) | `CC-15` | Item unavailable / access failure | P0 | UF-07 · Driving failure | UF-03 / UF-05 / UF-07 | App + Playback Service | Variant/annotation inside a Wireframe | `WF-07` |
| UF-07 → UF-11 | GS-04, GS-05 | `CC-16` | Interruption/route handoff | P0 | Android interruption/route | UF-11 | Android/System + Playback Service | Android/System-only transition | `WF-07 (handoff annotation)` |
| UF-07 | — (not on GS-01..05 path) | `CC-17` | Suggestion declined/dismissed | P1 | UF-07 · Entry suggestion | UF-07 | App | Variant/annotation inside a Wireframe | `WF-07` |
| UF-07 | GS-04 | `CC-18` | Analytics unavailable | P0 | Telemetry fail-safe | UF-07 | System adapter | Variant/annotation inside a Wireframe | `WF-07 (non-visible annotation)` |

## Cluster D — UF-08 to UF-10

| UF | GS | State ID | State | Priority | Screen owner | Recovery owner | App/System ownership | Coverage class | Planned WF |
|---|---|---|---|---:|---|---|---|---|---|
| UF-08 / UF-10 | GS-04 | `CD-01` | Idle/manual surface | P0 | Initiating manual surface | UF-08 / UF-10 | App | Variant/annotation inside a Wireframe | `WF-07 / WF-06` |
| UF-08 / UF-10 | GS-04 | `CD-02` | Capability check | P0 | UF-08 · Voice overlay | UF-02 / UF-10 / UF-12 | Hybrid | Variant/annotation inside a Wireframe | `WF-08` |
| UF-08 | GS-04 | `CD-03` | Listening attempt 1 | P0 | UF-08/09/10 · Voice overlay | UF-08 / UF-09 / UF-10 | App + Voice coordinator | App-visible Wireframe | `WF-08` |
| UF-10 | — (not on GS-01..05 path) | `CD-04` | Listening attempt 2 | P0 | UF-08/09/10 · Voice overlay | UF-08 / UF-09 / UF-10 | App + Voice coordinator | App-visible Wireframe | `WF-08` |
| UF-08 | GS-04 | `CD-05` | Processing | P0 | UF-08/09/10 · Voice overlay | UF-08 / UF-09 / UF-10 | App + Voice coordinator | App-visible Wireframe | `WF-08` |
| UF-08 | GS-04 | `CD-06` | Direct command pending | P0 | UF-08/09/10 · Voice overlay | UF-08 / UF-09 / UF-10 | App + Voice coordinator | App-visible Wireframe | `WF-08` |
| UF-08 | GS-04 | `CD-07` | Executed | P0 | UF-08/09/10 · Voice overlay | UF-08 / UF-09 / UF-10 | App + Voice coordinator | App-visible Wireframe | `WF-08` |
| UF-08 | — (not on GS-01..05 path) | `CD-08` | Execution failed-safe | P0 | UF-08/09/10 · Voice overlay | UF-08 / UF-09 / UF-10 | App + Voice coordinator | App-visible Wireframe | `WF-08` |
| UF-09 | — (not on GS-01..05 path) | `CD-09` | Confirming one candidate | P0 | UF-08/09/10 · Voice overlay | UF-08 / UF-09 / UF-10 | App + Voice coordinator | App-visible Wireframe | `WF-08` |
| UF-09 | — (not on GS-01..05 path) | `CD-10` | Confirming two artists | P0 | UF-08/09/10 · Voice overlay | UF-08 / UF-09 / UF-10 | App + Voice coordinator | App-visible Wireframe | `WF-08` |
| UF-09 | — (not on GS-01..05 path) | `CD-11` | Confirmed command pending | P0 | UF-08/09/10 · Voice overlay | UF-08 / UF-09 / UF-10 | App + Voice coordinator | App-visible Wireframe | `WF-08` |
| UF-09 | — (not on GS-01..05 path) | `CD-12` | Confirmation failed-safe | P0 | UF-08/09/10 · Voice overlay | UF-08 / UF-09 / UF-10 | App + Voice coordinator | App-visible Wireframe | `WF-08` |
| UF-09 | — (not on GS-01..05 path) | `CD-13` | Artist not found | P0 | UF-08/09/10 · Voice overlay | UF-08 / UF-09 / UF-10 | App + Voice coordinator | App-visible Wireframe | `WF-08` |
| UF-09 | — (not on GS-01..05 path) | `CD-14` | Unknown phrase | P0 | UF-08/09/10 · Voice overlay | UF-08 / UF-09 / UF-10 | App + Voice coordinator | App-visible Wireframe | `WF-08` |
| UF-09 | — (not on GS-01..05 path) | `CD-15` | Unsupported request | P0 | UF-08/09/10 · Voice overlay | UF-08 / UF-09 / UF-10 | App + Voice coordinator | App-visible Wireframe | `WF-08` |
| UF-09 | — (not on GS-01..05 path) | `CD-16` | Missing artist | P0 | UF-08/09/10 · Voice overlay | UF-08 / UF-09 / UF-10 | App + Voice coordinator | App-visible Wireframe | `WF-08` |
| UF-10 | — (not on GS-01..05 path) | `CD-17` | Attempt-one failure | P0 | UF-08/09/10 · Voice overlay | UF-08 / UF-09 / UF-10 | App + Voice coordinator | App-visible Wireframe | `WF-08` |
| UF-10 | — (not on GS-01..05 path) | `CD-18` | Retry available | P0 | UF-08/09/10 · Voice overlay | UF-08 / UF-09 / UF-10 | App + Voice coordinator | App-visible Wireframe | `WF-08` |
| UF-10 | — (not on GS-01..05 path) | `CD-19` | Attempt-two terminal failure | P0 | UF-08/09/10 · Voice overlay | UF-08 / UF-09 / UF-10 | App + Voice coordinator | App-visible Wireframe | `WF-08` |
| UF-08 / UF-09 / UF-10 | — (not on GS-01..05 path) | `CD-20` | Manual fallback | P0 | Initiating manual surface | UF-08 / UF-09 / UF-10 | App | Variant/annotation inside a Wireframe | `WF-08` |
| UF-10 / UF-12 | GS-04 | `CD-21` | Permission denied/revoked | P0 | Permission recovery / Voice overlay | UF-02 / UF-12 | Hybrid | Variant/annotation inside a Wireframe | `WF-03 / WF-08` |
| UF-10 / UF-12 | GS-04 | `CD-22` | Provider/language unavailable | P0 | Voice availability / Settings | UF-10 / UF-12 | Hybrid | Variant/annotation inside a Wireframe | `WF-08 / WF-10` |
| UF-10 / UF-12 | — (not on GS-01..05 path) | `CD-23` | Cloud consent required/declined | P0 | Settings · Cloud consent | UF-12 | App + provider boundary | App-visible Wireframe | `WF-10` |
| UF-08 / UF-09 / UF-10 | GS-04 | `CD-24` | Cancelled/Exit | P0 | Voice overlay return/exit | UF-08 / UF-09 / UF-10 | App + Voice coordinator | Variant/annotation inside a Wireframe | `WF-08` |

## Cluster E — UF-11 to UF-12

| UF | GS | State ID | State | Priority | Screen owner | Recovery owner | App/System ownership | Coverage class | Planned WF |
|---|---|---|---|---:|---|---|---|---|---|
| UF-11 | GS-05 | `CE-01` | Stable Playing | P0 | UF-11 · Playback/interruption feedback | UF-11 | Hybrid | Variant/annotation inside a Wireframe | `WF-06 / WF-07` |
| UF-11 | GS-05 | `CE-02` | Transient focus/call handling | P0 | UF-11 · Playback/interruption feedback | UF-11 | Hybrid | Variant/annotation inside a Wireframe | `WF-06 / WF-07` |
| UF-11 | GS-05 | `CE-03` | Interrupted Paused / resume eligible | P0 | UF-11 · Playback/interruption feedback | UF-11 | Hybrid | Variant/annotation inside a Wireframe | `WF-06 / WF-07` |
| UF-11 | GS-05 | `CE-04` | Temporarily Ducked | P0 | UF-11 · Playback/interruption feedback | UF-11 | Hybrid | Variant/annotation inside a Wireframe | `WF-06 / WF-07` |
| UF-11 | GS-05 | `CE-05` | Conditional Resume acknowledged | P0 | UF-11 · Playback/interruption feedback | UF-11 | Hybrid | Variant/annotation inside a Wireframe | `WF-06 / WF-07` |
| UF-11 | GS-05 | `CE-06` | Safe Paused / manual resume | P0 | UF-11 · Playback/interruption feedback | UF-11 | Hybrid | Variant/annotation inside a Wireframe | `WF-06 / WF-07` |
| UF-11 | GS-05 | `CE-07` | Route Disconnect handling | P0 | Android route/focus handoff | UF-11 | Android/System + Playback Service | Android/System-only transition | `WF-06 / WF-07 (handoff annotation)` |
| UF-11 | GS-05 | `CE-08` | Disconnected Safe Paused | P0 | Android route/focus handoff | UF-11 | Android/System + Playback Service | Android/System-only transition | `WF-06 / WF-07 (handoff annotation)` |
| UF-11 | GS-05 | `CE-09` | Reconnected Safe Paused | P0 | Android route/focus handoff | UF-11 | Android/System + Playback Service | Android/System-only transition | `WF-06 / WF-07 (handoff annotation)` |
| UF-11 | GS-05 | `CE-10` | Permanent Focus Loss | P0 | Android route/focus handoff | UF-11 | Android/System + Playback Service | Android/System-only transition | `WF-06 / WF-07 (handoff annotation)` |
| UF-11 | GS-05 | `CE-11` | Interruption Policy Error | P0 | UF-11 · Playback/interruption feedback | UF-11 | Hybrid | Variant/annotation inside a Wireframe | `WF-06 / WF-07` |
| UF-11 | GS-05 | `CE-12` | Corrupt Current Item | P0 | UF-11 · Playback/interruption feedback | UF-11 | Hybrid | Variant/annotation inside a Wireframe | `WF-06 / WF-07` |
| UF-11 | — (not on GS-01..05 path) | `CE-13` | Controlled Skip | P0 | UF-11 · Playback/interruption feedback | UF-11 | Hybrid | Variant/annotation inside a Wireframe | `WF-06 / WF-07` |
| UF-11 | GS-05 | `CE-14` | Queue Ended by Corruption | P0 | UF-11 · Playback/interruption feedback | UF-11 | Hybrid | Variant/annotation inside a Wireframe | `WF-06 / WF-07` |
| UF-12 | — (not on GS-01..05 path) | `CE-15` | Settings Default | P0 | UF-12 · Settings | UF-12 | App | App-visible Wireframe | `WF-10` |
| UF-12 | — (not on GS-01..05 path) | `CE-16` | Setting Update Pending | P0 | UF-12 · Settings result | UF-12 | App | Variant/annotation inside a Wireframe | `WF-10` |
| UF-12 | — (not on GS-01..05 path) | `CE-17` | Settings Updated / Error | P0 | UF-12 · Settings result | UF-12 | App | Variant/annotation inside a Wireframe | `WF-10` |
| UF-12 | GS-05 | `CE-18` | Android Settings Handoff | P0 | Android Settings | UF-12 | Android/System | Android/System-only transition | `WF-10 (handoff annotation)` |
| UF-12 | — (not on GS-01..05 path) | `CE-19` | Platform Recheck | P0 | UF-12 · Platform recheck | UF-12 | Hybrid | Variant/annotation inside a Wireframe | `WF-10` |
| UF-12 | — (not on GS-01..05 path) | `CE-20` | Bluetooth Selection | P1 | UF-12 · Bluetooth selection | UF-12 | Hybrid | App-visible Wireframe | `WF-10` |
| UF-12 | — (not on GS-01..05 path) | `CE-21` | Privacy Details | P0 | UF-12 · Privacy/data controls | UF-12 | App + provider boundary | App-visible Wireframe | `WF-10` |
| UF-12 | — (not on GS-01..05 path) | `CE-22` | Consent Decision | P0 | UF-12 · Privacy/data controls | UF-12 | App + provider boundary | App-visible Wireframe | `WF-10` |
| UF-12 | — (not on GS-01..05 path) | `CE-23` | Clear Selection | P0 | UF-12 · Privacy/data controls | UF-12 | App + provider boundary | App-visible Wireframe | `WF-10` |
| UF-12 | — (not on GS-01..05 path) | `CE-24` | Clear Confirmation | P0 | UF-12 · Clear data | UF-12 | App + provider boundary | Variant/annotation inside a Wireframe | `WF-10` |
| UF-12 | — (not on GS-01..05 path) | `CE-25` | Clearing | P0 | UF-12 · Clear data | UF-12 | App + provider boundary | Variant/annotation inside a Wireframe | `WF-10` |
| UF-12 | — (not on GS-01..05 path) | `CE-26` | Clear Result | P0 | UF-12 · Clear data | UF-12 | App + provider boundary | Variant/annotation inside a Wireframe | `WF-10` |

## UF-13 — Liked Songs (P2)

| UF | GS | State ID | State | Priority | Screen owner | Recovery owner | App/System ownership | Coverage class | Planned WF |
|---|---|---|---|---:|---|---|---|---|---|
| UF-13 | — (not on GS-01..05 path) | `UL-01` | P2 not shipped | P2 | Scope/release configuration | UF-13 | App build/scope | Variant/annotation inside a Wireframe | `WF-09 (scope annotation)` |
| UF-13 | — (not on GS-01..05 path) | `UL-02` | Entry loading | P2 | UF-13 · Liked Songs | UF-13 | App | App-visible Wireframe | `WF-09` |
| UF-13 | — (not on GS-01..05 path) | `UL-03` | Empty | P2 | UF-13 · Liked Songs | UF-13 | App | App-visible Wireframe | `WF-09` |
| UF-13 | — (not on GS-01..05 path) | `UL-04` | Ready | P2 | UF-13 · Liked Songs | UF-13 | App | App-visible Wireframe | `WF-09` |
| UF-13 | — (not on GS-01..05 path) | `UL-05` | Like write pending | P2 | Like control on player/list | UF-13 | App + local user-state repository | Variant/annotation inside a Wireframe | `WF-06 / WF-09` |
| UF-13 | — (not on GS-01..05 path) | `UL-06` | Like acknowledged | P2 | Like control on player/list | UF-13 | App + local user-state repository | Variant/annotation inside a Wireframe | `WF-06 / WF-09` |
| UF-13 | — (not on GS-01..05 path) | `UL-07` | Mutation failed | P2 | Like control on player/list | UF-13 | App + local user-state repository | Variant/annotation inside a Wireframe | `WF-06 / WF-09` |
| UF-13 | — (not on GS-01..05 path) | `UL-08` | Unlike write pending | P2 | Like control on player/list | UF-13 | App + local user-state repository | Variant/annotation inside a Wireframe | `WF-06 / WF-09` |
| UF-13 → UF-05 | — (not on GS-01..05 path) | `UL-09` | Playback command pending | P2 | Liked Songs playback | UF-05 / UF-13 | App + Playback Service | Variant/annotation inside a Wireframe | `WF-09 / WF-06` |
| UF-13 → UF-05 | — (not on GS-01..05 path) | `UL-10` | Playback acknowledged | P2 | Liked Songs playback | UF-05 / UF-13 | App + Playback Service | Variant/annotation inside a Wireframe | `WF-09 / WF-06` |
| UF-13 → UF-05 | — (not on GS-01..05 path) | `UL-11` | Playback rejected/stale | P2 | Liked Songs playback | UF-05 / UF-13 | App + Playback Service | Variant/annotation inside a Wireframe | `WF-09 / WF-06` |
| UF-13 | — (not on GS-01..05 path) | `UL-12` | Load/reconcile failure | P2 | UF-13 · Liked Songs | UF-13 | App | Variant/annotation inside a Wireframe | `WF-09` |
| UF-13 | — (not on GS-01..05 path) | `UL-13` | Missing item reconciliation | P2 | UF-13 · Liked Songs | UF-13 | App | Variant/annotation inside a Wireframe | `WF-09` |
| UF-13 | — (not on GS-01..05 path) | `UL-14` | UI recreation | P2 | UF-13 · Liked Songs | UF-13 | App | Variant/annotation inside a Wireframe | `WF-09` |

## Validation and exit decision

**Automated manifest checks: PASS**

- Canonical count: 126 expected / 126 present.
- ID uniqueness: 126 unique / 0 duplicate.
- Required fields: UF, GS disposition, State ID, Priority, Screen owner, Recovery owner, App/System ownership, coverage class, and planned WF are populated for every row.
- Classification enum: every row uses exactly one of the three approved groups.
- Priority isolation: P1 and P2 states are explicit; UF-13 remains P2 and non-blocking for P0.
- System boundary: Android dialogs, Settings, lifecycle, notification/lock-screen, media-key, route and focus transitions do not become invented app screens.
- Golden Scenario linkage: GS-01 through GS-05 resolve to planned WF packages; non-GS recovery states remain explicit.

**Phase 1 decision: PASS / COMPLETE.** This manifest authorizes creation of the low-fidelity WF packages. It does not approve the visual frames themselves and does not close Issue #15.
