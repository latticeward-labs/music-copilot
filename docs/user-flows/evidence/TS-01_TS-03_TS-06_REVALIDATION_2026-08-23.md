# TS-01 / TS-03 / TS-06 Automated Revalidation

**Date:** 23 August 2026  
**Repository:** `latticeward-labs/music-copilot`  
**Pull request:** [#29](https://github.com/latticeward-labs/music-copilot/pull/29)  
**Workflow:** [Cluster B Android spike harness - run #32](https://github.com/latticeward-labs/music-copilot/actions/runs/32640155802)  
**Workflow run ID:** `32640155802`  
**Validated head:** `c9f3f56619102a2a634211360a09d8beb2a826f4`  
**Merged main commit:** `c8f7e585ea54f530b021aa48c1891f04243de131`  
**Decision:** **PASS for the automated TS-01, TS-03, and TS-06 contracts**

## Purpose and authority

This execution revalidates the current automated Technical-Spike Harness after completion of the Phase 1 UF-01 through UF-13 traceability gate. The official English PRD/MVP and the Accepted ADR register remain authoritative. The execution does not change product scope or replace the previously accepted physical evidence/waiver records.

## Workflow result

| Job | Job ID | Result | Verified output |
|---|---:|---|---|
| Build, host unit tests, lint, APK | `97195898005` | PASS | `testDebugUnitTest`, `lintDebug`, and `assembleDebug`; BUILD SUCCESSFUL |
| AOSP instrumentation API 29 | `97195897919` | PASS | 4/4 tests; 0 failures/errors/skips |
| AOSP instrumentation API 31 | `97195898007` | PASS | 4/4 tests; 0 failures/errors/skips |
| AOSP instrumentation API 34 | `97195898156` | PASS | 4/4 tests; 0 failures/errors/skips |

### Build artifact verification

- Host test suites: 9
- Host tests: 59
- Host failures: 0
- Host errors: 0
- Host skipped: 0
- Android lint issues: 0
- Debug APK: generated successfully, 31,564,506 bytes
- Build duration reported by Gradle: 3m 57s

## TS-01 result - Notification and service ownership

Automated TS-01 coverage revalidated the service/session authority invariant on all three AOSP APIs:

| API | Test | Result |
|---:|---|---|
| 29 | `ServiceOwnershipTest.twoControllersObserveOneServiceOwnedQueue` | PASS |
| 31 | `ServiceOwnershipTest.twoControllersObserveOneServiceOwnedQueue` | PASS |
| 34 | `ServiceOwnershipTest.twoControllersObserveOneServiceOwnedQueue` | PASS |

**Automated decision:** PASS. Two controllers converged on one service-owned queue/session and no duplicate playback authority was created.

**Boundary:** This run did not repeat Samsung N01-N10, Pixel/Xiaomi physical rows, lock-screen visual inspection, notification-permission OEM behavior, or historical raw captures. The accepted 18 August physical evidence, explicit OEM waivers, and retention waiver remain the authority for those rows.

## TS-03 result - Speech provider feasibility

Host provider-policy tests:

- proven on-device capability without cloud consent -> On Device;
- generic network service without disclosure -> Manual Only;
- complete disclosure without separate consent -> Manual Only;
- reviewed disclosure plus consent -> Network Assisted with Consent;
- unknown English support -> Manual Only.

The four `SpeechProviderPolicyTest` methods passed with zero failures. The platform probe also passed on each AOSP API:

| API | Test | Result |
|---:|---|---|
| 29 | `AndroidSpeechCapabilityProbeTest.platformProbeNeverInfersCloudDisclosure` | PASS |
| 31 | `AndroidSpeechCapabilityProbeTest.platformProbeNeverInfersCloudDisclosure` | PASS |
| 34 | `AndroidSpeechCapabilityProbeTest.platformProbeNeverInfersCloudDisclosure` | PASS |

**Automated decision:** PASS at provider-policy/reference-contract level. Unverified capability or missing provider/disclosure/consent fails closed to Manual Only.

**Boundary:** This run does not approve a Pixel/Samsung/Xiaomi speech provider, English model availability, real microphone accuracy, processing destination, retention statement, cloud processor, legal copy, road-noise KPI, or Voice Beta enablement.

## TS-06 result - MediaStore and reconciliation

Host reconciliation tests:

- stale rows removed and duplicates collapsed;
- zero-duration rows not declared playable;
- 5,000 synthetic rows remain unique and stable.

All three `ReconciliationEngineTest` methods passed. The synthetic MediaStore round trip also passed on each AOSP API:

| API | Test | Result |
|---:|---|---|
| 29 | `MediaStoreRoundTripTest.appOwnedSyntheticAudioIsDiscoveredWithoutMetadataLogging` | PASS |
| 31 | `MediaStoreRoundTripTest.appOwnedSyntheticAudioIsDiscoveredWithoutMetadataLogging` | PASS |
| 34 | `MediaStoreRoundTripTest.appOwnedSyntheticAudioIsDiscoveredWithoutMetadataLogging` | PASS |

**Automated decision:** PASS for synthetic MediaStore discovery, stable identity/projection, stale-row reconciliation, duplicate control, zero-duration filtering, and the 5,000-row logical contract.

**Boundary:** This run did not repeat physical permission/revocation/removable-volume execution, real 5,000-file storage scans, ANR/memory profiling, or M12/M13 raw timing/changed-count measurement. Previously accepted physical M01-M14 results and the explicit retrospective measurement waiver remain separate historical evidence.

## Regression instrumentation

Each AOSP job also passed `AtomicSnapshotStoreTest.atomicWriteReturnsOnlyCompleteSnapshot`, retaining the TS-08 regression invariant. Total instrumentation result: **12/12 PASS**.

## Artifacts

| Artifact | ID | Digest |
|---|---:|---|
| Build/test/lint/APK | `9493433138` | `sha256:99e87f16d4801e6bac2b186edbb6ac5a0534115ca462df0407fe7aa34c878196` |
| API 29 instrumentation | `9493470827` | `sha256:4c12de6f0b6a3d7c981704d4ca59890404fc9b6cd939e0c883fe8922edeb1ade` |
| API 31 instrumentation | `9493471493` | `sha256:86241eec0e472dacde77d040dc7f4eade03a717b739c8ed6f646779e0ca8b23f` |
| API 34 instrumentation | `9493457242` | `sha256:49671d84ce4b899cc77f9619372046d276144dbe429159ab26ddcc504f12bb15` |

Artifacts are retained by GitHub Actions for 14 days. This durable record preserves the run/job/artifact identifiers and exact pass/failure counts without copying personal or sensitive runtime data into the repository.

## Final gate decision

**PASS / SOURCE-ALIGNED.** The automated portions of TS-01, TS-03, and TS-06 pass on the current post-Phase-1 code state. Their existing User Flow/Technical-Spike gates remain closed at the evidence levels stated above.

No physical/OEM/provider/legal/performance claim beyond the accepted historical records is created by this revalidation.
