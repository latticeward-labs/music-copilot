# TS-08 — Versioned Paused-Restore Evidence Protocol

**Status:** **PARTIAL — automated AOSP evidence PASS; mandatory physical-device rows and Android/QA sign-off remain BLOCKED**  
**Owner:** Android; QA verifies  
**Blocks:** Final P1 branch and Issue #16  
**Result root:** `docs/user-flows/evidence/results/TS-08/`

> This protocol is not executed evidence. PASS requires an installable Music Copilot build, required device/API/OEM coverage, build/repository SHAs, a timestamped state trace, expected-versus-actual results, immutable GitHub artifacts, and reviewer sign-off. Personal media data and unrelated logs are prohibited.

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

| ID | Scenario | Required result |
|---|---|---|
| R01 | Valid snapshot after real process loss | Restores once Paused; explicit Resume plays. |
| R02 | Captured while Playing | Relaunch still Paused; no audio. |
| R03 | Current item invalid | Deterministic survivor or Unavailable/Library. |
| R04 | Some non-current invalid | Remove invalid; preserve survivor order. |
| R05 | All invalid/revoked | No fabricated player; UF-02/03 recovery. |
| R06 | Invalid position | Reject negative; clamp only to verified duration. |
| R07 | Torn/corrupt snapshot | Fail closed safely. |
| R08 | Supported old migration | Deterministic and idempotent. |
| R09 | Unsupported future version | Fail closed; no unrelated loss/audio. |
| R10 | Repeated relaunch | No duplicate queue/player/position compounding/auto-play. |
| R11 | Live service exists | Ignore snapshot authority; reconnect to MediaSession. |
| R12 | Interrupted atomic write | Last complete snapshot or none; never partial. |

PASS requires implemented/versioned contract, migration/error tests, real process-loss instrumentation/device proof, explicit Resume, live-service precedence, and no personal payload. Unit tests alone cannot close R01/R02/R10/R11.
