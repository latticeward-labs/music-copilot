# Cluster B — Connected Flow Review Record

**Product:** Music Copilot MVP v1.0  
**Scope:** UF-04 Browse/Search → UF-05 Manual Playback/Queue → UF-06 Background/System Controls  
**Review date:** 06 August 2026  
**Tracking:** GitHub Issue #3  
**Connected-flow integration decision:** **PASS**  
**Shared State Inventory decision:** **PASS**  
**Shared Traceability decision:** **PASS**  
**Formal Cluster B exit:** **OPEN**

## Authority and controlled artifacts

The official English PRD/MVP are product authorities. The current GitHub ADR register is the architecture-status authority; ADR-0001 through ADR-0006 are Accepted. Older PDF/RAR snapshots do not override the current repository record.

Reviewed inputs:

- [`UF-04_BROWSE_AND_SEARCH.md`](UF-04_BROWSE_AND_SEARCH.md)
- [`UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.md`](UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.md)
- [`UF-06_BACKGROUND_AND_SYSTEM_CONTROLS.md`](UF-06_BACKGROUND_AND_SYSTEM_CONTROLS.md)
- [`CLUSTER_B_STATE_INVENTORY.md`](CLUSTER_B_STATE_INVENTORY.md)
- [`CLUSTER_B_TRACEABILITY_MATRIX.md`](CLUSTER_B_TRACEABILITY_MATRIX.md)
- [`mermaid/CLUSTER_B_CONNECTED_FLOW.md`](mermaid/CLUSTER_B_CONNECTED_FLOW.md)
- [`mermaid/CLUSTER_B_CONNECTED_FLOW.mmd`](mermaid/CLUSTER_B_CONNECTED_FLOW.mmd)

## Connected path under review

1. UF-03 supplies a truthful verified Library state to UF-04.
2. UF-04 permits local Browse/Search only from Ready, revalidates the selected item, and waits for service/MediaSession acknowledgement.
3. An acknowledged start enters UF-05, where the service owns the deterministic visible-context queue, current item, state, and position.
4. UF-05 routes background, UI closure/recreation, screen-off, lock, and system-controller transitions to UF-06 without creating another player.
5. UF-06 keeps P0 active continuity separate from P1 Paused Restore, synchronizes every controller from one MediaSession, and returns a complete live snapshot to UF-05.
6. Access/stale-item failures return to UF-02/UF-03; interruption and route behavior hands off to UF-11; unusable restore returns to UF-04.

## Final integration review matrix

| Review criterion | Result | Evidence in connected diagram |
|---|---|---|
| UF-04 → UF-05 seam | PASS | Transition occurs only after accessible-item revalidation and authoritative playback acknowledgement. |
| UF-05 → UF-06 seam | PASS | Background/UI/system transitions preserve the service-owned queue and player. |
| UF-06 → UF-05 return seam | PASS | Returning UI reconnects and refreshes the complete authoritative snapshot; no restart or second player. |
| P0 vs P1 separation | PASS | Existing service/session uses P0 continuity; lifecycle loss evaluates local P1 restore and never auto-plays. |
| Browse/Search coverage | PASS | Songs, Artists, Albums, Folders and Search Result/No Result/Error are connected to common selection validation. |
| Queue/manual controls | PASS | Visible-context queue, Play/Pause/Next/Previous/Seek, queue selection, End of Queue, corrupt/inaccessible recovery, and reconnect are represented. |
| System surfaces | PASS | Notification, lock screen, Bluetooth/headset and compatible vehicle media keys remain controllers of one MediaSession. |
| Permission/data truth | PASS | Android access state, MediaStore identity/existence, derived index, and playback authority remain separate. |
| Failure and recovery | PASS | Revoked access, stale media, rejected start, item failure, controller loss, lifecycle loss, and unusable restore have explicit routes. |
| Downstream handoffs | PASS | UF-07 and UF-11 are linked without being absorbed into Cluster B. |
| MVP scope | PASS | No account, backend, sync, catalog, streaming, upload/download, Android Auto, Cast, iOS, or v1.1 behavior is added. |
| Mermaid parity | PASS after verification | Embedded Mermaid and raw `.mmd` are required to be byte-equivalent before publication. |
| Shared state normalization | PASS | CB-01 through CB-33 normalize every detailed UF-04/05/06 state without gaps or duplicate identities. |
| Shared traceability | PASS | Product requirements, screens/surfaces, all CB states, authority boundaries, failures/recoveries, downstream handoffs, and open evidence are mapped. |

## Invariants confirmed

- Playback Service/MediaSession is the sole live owner of player, queue, item, state, and position.
- UI and system surfaces never create independent or optimistic playback truth.
- Search failure and No Result do not invalidate the verified library.
- Queue replacement occurs only after service acknowledgement and uses the visible local collection in visible order.
- Queue end does not wrap or invent Repeat; no Shuffle or playlist editing is introduced.
- Notification permission behavior is contextual and non-blocking where applicable; media-session exemption behavior remains evidence-bound.
- P1 restore is Paused and always requires explicit Resume.
- Personal media metadata is not approved for telemetry.

## Open evidence preserved

| Open gate | Why it remains open |
|---|---|
| TS-01 | Notification/foreground-media behavior still requires supported Android/OEM evidence. |
| TS-02 | UI closure, task removal, service lifetime, and process-death boundaries still require execution evidence. |
| TS-06 | Permission, MediaStore move/delete, reconciliation, rebuild, and scale behavior still require device evidence. |
| TS-08 | Queue/position restore representation, migration/error, and paused-restore behavior still require evidence. |
| Issue #16 | Numeric `T_PREVIOUS` and service/controller/lifecycle/persistence mechanics remain architecture decisions. |
| Cross-role sign-off | Product, Design, Android, QA, and Privacy reviewers have not recorded approval. |

## Technical Spike protocol decision

**PASS — execution specifications are complete.** See the [evidence index](evidence/README.md). **Execution is BLOCKED** because no Android implementation/APK/instrumentation/device evidence exists. Figma is not part of this gate.

## Decision

**PASS — the connected Cluster B diagram, normalized shared State Inventory, and shared Traceability Matrix are complete and internally coherent for UF-04/UF-05/UF-06.**

This closes the repository documentation gaps for the connected review, shared State Inventory, and shared Traceability. It does not grant formal Cluster B exit, cross-role sign-off, executed Technical Spike evidence, Wireframe/final Software Architecture/implementation approval, or Issue #3 completion.
