# TS-08 — Versioned Paused-Restore Evidence Protocol

**Status:** **PASS — automated AOSP evidence PASS; R01-R12 physical/process-loss execution PASS; accepted evidence bundle and Android/QA approval attestations recorded**  
**Owner:** Android; QA verifies  
**Blocks:** Final P1 branch and Issue #16  
**Result root:** `docs/user-flows/evidence/results/TS-08/`

> The accepted execution bundle is `results/TS-08/2026-08-18-81b9aad/`. Historical raw process-loss captures that were not retained are covered by the explicit one-time `CLUSTER_B_EVIDENCE_RETENTION_WAIVER_2026-08-18.md`; the bundle records derived attestations rather than inventing raw timestamps/logs/media. Personal media data and unrelated logs remain prohibited.

Issue #16 chooses persistence technology. The logical snapshot remains mandatory:

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

| ID | Scenario | Required result | Result |
|---|---|---|---|
| R01 | Valid snapshot after real process loss | Restores once Paused; explicit Resume plays. | PASS |
| R02 | Captured while Playing | Relaunch still Paused; no audio. | PASS |
| R03 | Current item invalid | Deterministic survivor or Unavailable/Library. | PASS |
| R04 | Some non-current invalid | Remove invalid; preserve survivor order. | PASS |
| R05 | All invalid/revoked | No fabricated player; UF-02/03 recovery. | PASS |
| R06 | Invalid position | Reject negative; clamp only to verified duration. | PASS |
| R07 | Torn/corrupt snapshot | Fail closed safely. | PASS |
| R08 | Supported old migration | Deterministic and idempotent. | PASS |
| R09 | Unsupported future version | Fail closed; no unrelated loss/audio. | PASS |
| R10 | Repeated relaunch | No duplicate queue/player/position compounding/auto-play. | PASS |
| R11 | Live service exists | Ignore snapshot authority; reconnect to MediaSession. | PASS |
| R12 | Interrupted atomic write | Last complete snapshot or none; never partial. | PASS |

## Decision

**PASS.** TS-08 R01-R12 execution, real process-loss/Paused-Restore behavior, immutable attestation bundle, artifact Git object IDs, and Android/QA approval attestations are accepted for Cluster B. Unavailable historical raw process-loss captures are explicitly waived rather than fabricated.
