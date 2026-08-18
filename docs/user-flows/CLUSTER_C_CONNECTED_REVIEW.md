# Cluster C - Connected Review and Exit Record

**Scope:** UF-07 Driving Mode entry, use, and exit  
**Date:** 18 August 2026  
**Tracking:** GitHub Issue #3  
**Source basis:** English `MSM2.rar` baseline plus current GitHub `main`  
**Decision:** **PASS / COMPLETE** at User Flow specification level

## Artifacts reviewed

- [`CLUSTER_C_SOURCE_AUTHORITY.md`](CLUSTER_C_SOURCE_AUTHORITY.md)
- [`UF-07_DRIVING_MODE.md`](UF-07_DRIVING_MODE.md)
- [`CLUSTER_C_STATE_INVENTORY.md`](CLUSTER_C_STATE_INVENTORY.md)
- [`CLUSTER_C_TRACEABILITY_MATRIX.md`](CLUSTER_C_TRACEABILITY_MATRIX.md)
- [`CLUSTER_C_EVIDENCE_APPLICABILITY_2026-08-18.md`](CLUSTER_C_EVIDENCE_APPLICABILITY_2026-08-18.md)
- [`CLUSTER_C_SIGNOFF_2026-08-18.md`](CLUSTER_C_SIGNOFF_2026-08-18.md)
- [`mermaid/UF-07_DRIVING_MODE.md`](mermaid/UF-07_DRIVING_MODE.md)
- [`mermaid/UF-07_DRIVING_MODE.mmd`](mermaid/UF-07_DRIVING_MODE.mmd)

## Connected path under review

1. UF-05/UF-06 supplies an authoritative Playing, Paused/Ended, or No Track snapshot from the one service-owned MediaSession.
2. The P0 manual action enters a bounded snapshot state before the one-layer Driving surface is ready.
3. A selected-device connection may expose a separable P1 suggestion, but entry occurs only after explicit acceptance and uses the same snapshot path.
4. Driving Mode renders title, artist, artwork/placeholder, four large controls, microphone, and immediate Exit on one layer.
5. Manual commands update only after service acknowledgement; failures refresh the actual state without blocking Exit.
6. No Track offers at most one valid revalidated last-item Play fallback, otherwise a concise Exit/manual recovery.
7. Microphone hands off to UF-08/UF-10 as an overlay/session state; interruptions/routes hand off to UF-11.
8. Exit/Back returns to the last non-driving surface or Home/Library fallback without issuing a playback command.

## Review matrix

| Review criterion | Evidence | Result |
|---|---|---|
| English-source authority | Source register and exclusion record | PASS |
| Current ADR status | Accepted ADR-0001 through ADR-0006 reflected | PASS |
| UF-05/06 entry seam | Complete MediaSession snapshot; no restart/second player | PASS |
| Manual P0 entry | Always independent of Bluetooth, network, mic, analytics | PASS |
| P1 suggestion separation | Selected device, explicit acceptance, never automatic, removable | PASS |
| Required Playing state | CC-05 | PASS |
| Required Paused state | CC-06; no surprise resume | PASS |
| Required No Track state | CC-07/15; bounded fallback, no long list | PASS |
| Four large controls | Previous, Play/Pause, Next, Seek Back controlled resolution | PASS |
| Microphone boundary | Explicit tap -> UF-08/10 overlay; no wake word | PASS |
| Immediate Exit/Back | CC-13; return target/fallback; playback unchanged | PASS |
| One-layer safety | No typing, Search, Queue list, nested menu, Settings, or long result list | PASS |
| Playback authority | Service/MediaSession only; acknowledged refresh | PASS |
| Failure/recovery | Entry, reconnect, command, access, telemetry, and downstream branches | PASS |
| Accessibility | 48dp+, larger primary controls, TalkBack, contrast, scalable text, limited motion | PASS |
| Analytics/privacy | Allowlisted start/complete; prohibited metadata/device/audio payloads | PASS |
| MVP exclusions | No Android Auto, motion detection, streaming, backend, account, sync, or v1.1 | PASS |
| Mermaid parity | Markdown embedded graph equals raw `.mmd` source | PASS |
| State/traceability completeness | CC-01 through CC-18 and all mapped authorities | PASS |
| Technical Spike applicability | No Phase 0 spike is assigned directly as a UF-07 exit blocker; implementation evidence remains Issues #9/#13 | PASS / N/A for a new UF-07 spike |
| Cross-role User Flow sign-off | Product, Design, Android-contract, QA-testability, Privacy, and Governance decisions recorded | PASS |

## Invariants confirmed

- Driving Mode is a presentation/controller surface, not a playback owner.
- Manual entry is P0; Bluetooth suggestion is P1 and suggestion-only.
- Entry never starts, restarts, pauses, or resumes playback by itself.
- Four fixed playback controls, microphone, and immediate Exit are available without nested navigation.
- No Track never opens a long list inside Driving Mode.
- Exit/Back is reachable from every pending or steady state and does not mutate playback.
- Personal media metadata, Bluetooth device identity, raw audio, transcript, and exact position are not approved telemetry.
- Voice execution and interruption/route policy remain downstream flows, not hidden Cluster C behavior.

## Verification boundary

Cluster C is a User Flow gate. Source alignment, state coverage, traceability, Mermaid parity, connected handoffs, Technical Spike applicability, and cross-role sign-off are verified here. Application implementation, exact Compose design, Android Bluetooth mechanics, stationary/in-car testing, accessibility instrumentation, and release evidence remain Issues #9, #13, #15, and #16. The absence of implementation evidence does not permit those later gates to be marked complete.

## Exit decision

**PASS - Cluster C / UF-07 is formally COMPLETE / SOURCE-ALIGNED at the User Flow stage, with evidence applicability and final cross-role sign-off recorded.**

This completion does not close Issue #3. The next drafting stage is **Cluster D - UF-08 Direct Command, UF-09 Ambiguity/Not Found, and UF-10 Timeout/Unavailable/Retry**. Final product/Android approval of the complete UF-01 through UF-13 set remains open.

