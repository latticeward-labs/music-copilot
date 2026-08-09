# Cluster B Automated CI Evidence — 2026-08-09

**Decision:** PASS for the automated Android spike harness only  
**Repository head tested:** `fbd6fcc2a3f8c5a378d7eb598d487762ce829a5e`  
**Merged main commit:** `81b9aadcb67cbfcb7e7fcc849a4892755d5b891a`  
**Workflow:** [Cluster B Android spike harness — run #6](https://github.com/latticeward-labs/music-copilot/actions/runs/31322420324)

## Results

| Gate | Result |
|---|---|
| Debug APK assemble | PASS |
| Strict Android Lint | PASS |
| Host unit tests | PASS |
| AOSP API 29 instrumentation | PASS — 3 tests |
| AOSP API 31 instrumentation | PASS — 3 tests |
| AOSP API 34 instrumentation | PASS — 3 tests |
| Total AOSP instrumentation executions | PASS — 9/9 |

The instrumentation suite covers atomic snapshot storage, synthetic MediaStore round-trip, and two-controller observation of one service-owned queue. It does not replace the complete manual protocol matrices.

## Artifact identity

| Artifact | ID | SHA-256 |
|---|---:|---|
| Build/APK/reports | 9040622528 | `00be4354eda85d0dcf0e08568613b7039e585b0fe81192cd67705d53b6387b14` |
| API 29 reports | 9040640571 | `c2292f738dbc5232090d79a68fff10ae86ecc4d3a5000233d53d345f348d1cb6` |
| API 31 reports | 9040637621 | `5dabdb2f79e5adbbae5ba338856f9886ee3b0100bd973bc2758bac4e701df236` |
| API 34 reports | 9040639855 | `8cd59f38e3b953598bfaaa3511d34860782b482b396459392e05d7caa0e3c740` |

## Boundary

This record establishes that the harness builds and its automated checks pass on AOSP API 29/31/34. It does not claim Samsung/One UI, Xiaomi/MIUI or HyperOS, physical Pixel, real process-loss, notification permission presentation, task-removal behavior, full mandatory row coverage, or Android/QA reviewer sign-off. Those remain controlled by TS-01, TS-02, TS-06 and TS-08.
