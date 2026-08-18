# TS-09 - Safe Confidence Default and Remote-Failure Policy

**Blocks:** final UF-08/UF-09 contract and Issue #16  
**Date:** 18 August 2026  
**Automated result:** **PASS — workflow run #8 on head `38d71c7a3bf14b6a0afc99e507b07fdad5399a86`**

## Problem

Voice safety cannot depend on remote configuration being reachable or valid. The build needs a conservative local default, bounded validation, and deterministic fallback without allowing configuration to add intents or bypass confirmation.

## Spike candidate policy

| Input | Resolution |
|---|---|
| Valid finite remote threshold in `0.80..0.98` | Use it for the current policy and retain as last valid. |
| Missing, NaN/infinite, malformed, or out-of-range remote value | Ignore it. |
| Remote invalid/unavailable and a prior valid value exists | Use last valid. |
| No valid remote or last-valid value | Use conservative build candidate `0.90`. |
| Provider confidence missing/invalid | It cannot direct-execute; route to ambiguity/failure boundary. |

The separate plausible boundary candidate is `0.72`. Remote configuration may tune only `T_DIRECT` within the fixed range; it cannot change grammar, intent count, artist algorithm, retry count, confirmation policy, telemetry, or cloud consent.

The product target of >=85% command success is not used as the confidence threshold. Those are different measurements.

## Executable cases

- valid remote wins;
- malformed remote uses last valid;
- invalid remote and invalid last-valid use build default;
- remote absence is non-blocking;
- low confidence produces a bounded confirmation result;
- missing confidence fails safe and cannot directly execute.

## Exit criteria

- [x] Safe build-default candidate exists and is explicitly marked spike-only pending #16 adoption.
- [x] Range validation rejects invalid, NaN, infinite, and out-of-range values.
- [x] Last-valid/build-default fallback is deterministic and non-blocking.
- [x] Remote config cannot widen scope or bypass confirmation.
- [x] Host unit tests PASS on the validated code head.

## Decision boundary

**PASS at the safe-default/fallback contract level.** Issue #16 must formally adopt or evidence-adjust numeric values before production, while preserving the fail-closed behavior.
