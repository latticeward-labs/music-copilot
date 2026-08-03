# Music Copilot - User Flow Phase 1 First Readiness

**Version:** 1.0  
**Date:** 01 August 2026  
**Status:** Cluster A complete for review  
**Product version:** Music Copilot MVP v1.0  
**Tracking:** GitHub Issue #3  
**Figma section:** [01 / Phase 1 - First Readiness](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-2)  
**Product authority:** English PRD v1.0 and English MVP v1.0

> Phase 1 converts the approved Phase 0 foundation into the first complete connected User Flow cluster. It covers UF-01, UF-02, and UF-03. It does not approve low-fidelity Wireframes, the final Software Architecture, or implementation.

## 1. Completion decision

Cluster A is complete because the following artifacts now exist and agree:

- UF-01 First Launch and Education.
- UF-02 Permission Denial and Settings Recovery.
- UF-03 Local Scan and Library Readiness.
- A connected cross-surface swimlane.
- A Cluster A state inventory and requirement traceability record.
- A review checklist, approval boundary, and open-gate record.

Issue #3 remains open. UF-04 through UF-13 are not complete, and the complete User Flow set cannot be marked Approved until Issue #2 and the ADR register align.

## 2. Artifact register

### Figma

- [Phase 1 section](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-2)
- [UF-01](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-26)
- [UF-02](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-27)
- [UF-03](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-28)
- [Connected swimlane](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-29)
- [State inventory](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-30)
- [Review record](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-31)

### GitHub

- [Cluster A overview](CLUSTER_A_FIRST_READINESS.md)
- [UF-01 record](UF-01_FIRST_LAUNCH_AND_EDUCATION.md)
- [UF-02 record](UF-02_PERMISSION_RECOVERY.md)
- [UF-03 record](UF-03_LIBRARY_READINESS.md)
- [User Flow index](../USER_FLOWS.md)

## 3. Scope covered

- Splash loading and recoverable preparation failure.
- First-run and incomplete-onboarding routing.
- Two-slide value introduction and road-attention warning.
- Continue, Skip, and replay from Settings.
- Media-access explanation before first scan.
- Media permission Granted, Denied, and Permanently Denied.
- Retry, Not now, Android Settings, and return recheck.
- Initial scan, incremental reconciliation, and interrupted scan recovery.
- Home / Library Scanning, Empty, and Ready.
- Recoverable scan failure and permission revocation.
- Six-command education.
- Optional practice and just-in-time microphone permission.
- Microphone denial with manual controls preserved.
- Notification permission deferred to UF-06 / TS-01.
- Cloud-speech consent and truthful provider disclosure remain explicitly routed to TS-03 before any cloud-processed Voice session.

## 4. Fixed behavior

1. English PRD/MVP remain the product authorities.
2. Home / Library is one destination with Songs, Artists, Albums, and Folders internal views.
3. Onboarding is skippable and replayable.
4. Media permission is requested before the first scan and after explanation.
5. Microphone permission is requested only after explicit microphone action.
6. Notification permission is deferred until playback/background value exists.
7. Returning from Android Settings always rechecks permission.
8. Android permission state is authoritative.
9. MediaStore is authoritative for media existence and identity.
10. The local index is derived and rebuildable.
11. Permission revocation reconciles stale entries without a crash.
12. No account, backend, sync, catalog, streaming, upload, download, or forced cloud dependency exists.
13. No raw audio or personal media metadata is added to telemetry.
14. Cluster A introduces no new analytics events because the current allowlist does not define them.
15. If the selected STT provider performs cloud processing, separate consent must disclose the processing destination, purpose, and retention before the first cloud-processed session.
16. Product copy must not claim guaranteed offline speech recognition without validated provider evidence.

## 5. Valid endpoints

- Home / Library Ready.
- Home / Library Empty with Rescan and relevant permission recovery.
- Permission Denied with Retry and Not now.
- Permanently Denied with explicit Android Settings and return recheck.
- Recoverable startup or scan failure with Retry.
- Reconciled revoked/inaccessible media.
- Microphone declined with manual controls preserved.

## 6. Traceability

| Source requirement | Flow coverage |
|---|---|
| PRD first-use flow | UF-01, UF-02, UF-03 |
| FR-01 local scan and library views | UF-01, UF-02, UF-03 |
| US-01 permission timing and recovery | UF-01, UF-02 |
| US-02 account-free scan and incremental index | UF-01, UF-03 |
| Voice-provider privacy and truthful disclosure | UF-01, UF-02; TS-03 evidence remains open |
| Privacy principles | UF-01 through UF-03 |
| Accessibility requirements | UF-01 through UF-03 |

## 7. Open gates

- TS-01: notification and foreground media behavior across Android versions.
- TS-03: voice-provider availability, language support, network dependence, processing destination, separate cloud-speech consent, retention disclosure, and fallback behavior.
- TS-06: permission, scoped storage, move/delete, and reconciliation evidence.
- Issue #2 and ADR register alignment before full User Flow approval.
- UF-04 through UF-13 completion.
- Issue #15 Wireframes and Issue #16 final Software Architecture remain separate gates.

## 8. Exit decision

Phase 1 Cluster A is complete and registered. The next approved drafting task is Cluster B: UF-04 Browse/Search, UF-05 Manual Playback, and UF-06 Background/System Controls.
