# Cluster B — Connected Flow Review Record

**Product:** Music Copilot MVP v1.0  
**Scope:** UF-04 Browse/Search → UF-05 Manual Playback/Queue → UF-06 Background/System Controls  
**Review date:** 06 August 2026  
**Formal evidence closeout:** 18 August 2026  
**Tracking:** GitHub Issue #3  
**Connected-flow integration decision:** **PASS**  
**Shared State Inventory decision:** **PASS**  
**Shared Traceability decision:** **PASS**  
**Technical Spike evidence decision:** **PASS**  
**Android review:** **PASS — project-owner attestation recorded**  
**QA review:** **PASS — project-owner attestation recorded**  
**Product/Governance acceptance:** **PASS**  
**Formal Cluster B exit:** **PASS — COMPLETE**

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
- [`evidence/README.md`](evidence/README.md)
- [`evidence/CLUSTER_B_EVIDENCE_RETENTION_WAIVER_2026-08-18.md`](evidence/CLUSTER_B_EVIDENCE_RETENTION_WAIVER_2026-08-18.md)
- TS-01 / TS-02 / TS-06 / TS-08 accepted result bundles under `evidence/results/`

## Connected path under review

1. UF-03 supplies a truthful verified Library state to UF-04.
2. UF-04 permits local Browse/Search only from Ready, revalidates the selected item, and waits for service/MediaSession acknowledgement.
3. An acknowledged start enters UF-05, where the service owns the deterministic visible-context queue, current item, state, and position.
4. UF-05 routes background, UI closure/recreation, screen-off, lock, and system-controller transitions to UF-06 without creating another player.
5. UF-06 keeps P0 active continuity separate from P1 Paused Restore, synchronizes every controller from one MediaSession, and returns a complete live snapshot to UF-05.
6. Access/stale-item failures return to UF-02/UF-03; interruption and route behavior hands off to UF-11; unusable restore returns to UF-04.

## Final integration review matrix

| Review criterion | Result |
|---|---|
| UF-04 → UF-05 seam | PASS |
| UF-05 → UF-06 seam | PASS |
| UF-06 → UF-05 return seam | PASS |
| P0 vs P1 separation | PASS |
| Browse/Search coverage | PASS |
| Queue/manual controls | PASS |
| System surfaces | PASS |
| Permission/data truth | PASS |
| Failure and recovery | PASS |
| Downstream handoffs | PASS |
| MVP scope | PASS |
| Mermaid parity | PASS |
| Shared state normalization | PASS |
| Shared traceability | PASS |
| TS-01 evidence | PASS / approved N/A where recorded |
| TS-02 evidence | PASS |
| TS-06 evidence | PASS |
| TS-08 evidence | PASS |
| Android review | PASS — project-owner attestation |
| QA review | PASS — project-owner attestation |
| Privacy evidence boundary | PASS |

## Invariants confirmed

- Playback Service/MediaSession is the sole live owner of player, queue, item, state, and position.
- UI and system surfaces never create independent or optimistic playback truth.
- Search failure and No Result do not invalidate the verified library.
- Queue replacement occurs only after service acknowledgement and uses the visible local collection in visible order.
- Queue end does not wrap or invent Repeat; no Shuffle or playlist editing is introduced.
- Notification permission behavior is contextual and non-blocking where applicable; Samsung OEM behavior was reviewed and accepted as platform-consistent for the tested matrix.
- P1 restore is Paused and always requires explicit Resume.
- Personal media metadata is not approved for telemetry or evidence payloads.

## Technical Spike evidence closeout

Build, strict lint, host tests, and AOSP API 29/31/34 instrumentation are PASS. The accepted Cluster B result bundles record TS-01 N01-N10, TS-02 L01-L12, TS-06 M01-M14, and TS-08 R01-R12 as PASS/waived where applicable.

Some historical raw capture files were not retained before the later immutable-bundle contract was finalized. The project owner approved a one-time retrospective evidence-retention waiver for this Cluster B run only. The bundles therefore contain derived `state-trace.txt`, retrospective `logcat-redacted.txt`, visual-evidence indexes, artifact Git object IDs, and Android/QA approval attestations. Missing historical raw values are explicitly waived and are not fabricated.

Issue #16 architecture mechanics remain downstream architecture decisions and are **not a blocker to the completed Cluster B User Flow/evidence exit**.

## Decision

**PASS — Cluster B is formally complete.**

UF-04, UF-05, and UF-06 specifications, the canonical connected diagram, shared State Inventory, shared Traceability Matrix, Technical Spike execution/evidence, and accepted review decisions are complete for this stage.

This completion does not close Issue #3 as a whole. UF-07 through UF-13 remain to be completed and reviewed. The next active User Flow stage is **Cluster C beginning with UF-07 Driving Mode**.
