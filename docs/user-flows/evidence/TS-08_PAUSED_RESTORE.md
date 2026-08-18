# TS-08 — Versioned Paused-Restore Evidence Protocol

**Status:** **PARTIAL — automated AOSP evidence PASS; R01-R12 physical/process-loss execution operator-confirmed PASS; immutable evidence bundle and Android/QA sign-off remain OPEN**  
**Owner:** Android; QA verifies  
**Blocks:** Final P1 branch and Issue #16  
**Result root:** `docs/user-flows/evidence/results/TS-08/`

> Physical/process-loss execution status below is recorded from explicit project-owner/operator confirmation on 18 August 2026. This records the test result; it does not fabricate missing artifacts or reviewer signatures. Formal protocol closure still requires device/build/repository identifiers, timestamped state traces, expected-versus-actual evidence, immutable GitHub artifacts, and Android/QA sign-off. Personal media data and unrelated logs are prohibited.

Issue #16 chooses persistence technology. The logical snapshot is mandatory:

| Field | Contract |
|---|---|
| `schemaVersion` | Positive; unknown future version fails closed. |
| `queueRevision` | Monotonic; rejects torn/stale writes. |
| `queue[]` | Ordered MediaStore-requery references; no audio/personal display metadata. |
| `currentIndex` | In range or invalid. |
| `positionMs` | Non-negative; clamp only to verified duration. |
| `capturedState` | Informational; restore output is always Paused. |
| `savedAt` | Local diagnostics/expiry input; never auto-play trigger. |

Live MediaSessionService always wins. Writes are atomic. Restore rechecks permission/MediaStore, removes invalid entries deterministically, and publishes one Paused snapshot after validation. Paths, audio, personal metadata, voice, accounts, cloud IDs, and auto-play flags are forbidden.

| ID | Scenario | Required result | Execution |
|---|---|---|---|
| R01 | Valid snapshot after real process loss | Restores once Paused; explicit Resume plays. | PASS — operator confirmed |
| R02 | Captured while Playing | Relaunch still Paused; no audio. | PASS — operator confirmed |
| R03 | Current item invalid | Deterministic survivor or Unavailable/Library. | PASS — operator confirmed |
| R04 | Some non-current invalid | Remove invalid; preserve survivor order. | PASS — operator confirmed |
| R05 | All invalid/revoked | No fabricated player; UF-02/03 recovery. | PASS — operator confirmed |
| R06 | Invalid position | Reject negative; clamp only to verified duration. | PASS — operator confirmed |
| R07 | Torn/corrupt snapshot | Fail closed safely. | PASS — operator confirmed |
| R08 | Supported old migration | Deterministic and idempotent. | PASS — operator confirmed |
| R09 | Unsupported future version | Fail closed; no unrelated loss/audio. | PASS — operator confirmed |
| R10 | Repeated relaunch | No duplicate queue/player/position compounding/auto-play. | PASS — operator confirmed |
| R11 | Live service exists | Ignore snapshot authority; reconnect to MediaSession. | PASS — operator confirmed |
| R12 | Interrupted atomic write | Last complete snapshot or none; never partial. | PASS — operator confirmed |

The required real process-loss and restore scenario execution is recorded PASS at operator-confirmation level, alongside existing automated AOSP evidence. Formal TS-08 closure remains open until immutable result bundles demonstrate process-loss state traces, explicit Resume behavior, live-service precedence, device/build/repository identifiers, and Android/QA reviewer sign-off.
