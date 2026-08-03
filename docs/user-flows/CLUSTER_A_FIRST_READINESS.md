# Cluster A - First Readiness

**Product:** Music Copilot MVP v1.0  
**Phase:** User Flow Phase 1  
**Flows:** UF-01, UF-02, UF-03  
**Priority:** P0 - Beta blocking  
**Date:** 01 August 2026  
**Status:** Cluster complete for review and repository registration; the complete User Flow set remains open  
**Tracking:** GitHub Issue #3

## Figma artifacts

- [Phase 1 section](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-2)
- [UF-01 - First Launch and Education](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-26)
- [UF-02 - Permission Recovery](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-27)
- [UF-03 - Library Scan and Readiness](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-28)
- [Connected Cluster A swimlane](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-29)
- [State Inventory and Traceability](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-30)
- [Review and Exit Record](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-31)

## Repository artifacts

- [UF-01 - First Launch and Education](UF-01_FIRST_LAUNCH_AND_EDUCATION.md)
- [UF-02 - Permission Recovery](UF-02_PERMISSION_RECOVERY.md)
- [UF-03 - Library Readiness](UF-03_LIBRARY_READINESS.md)

## Purpose

Cluster A converts the Phase 0 foundation into the first complete connected journey. It covers the path from app launch to a truthful Home / Library readiness state, including onboarding, contextual permissions, Android Settings recovery, initial or incremental scanning, command education, microphone fallback, and permission revocation.

The cluster is implementation-independent. It records user-visible decisions, system-owned surfaces, state ownership, recovery, privacy, accessibility, and open evidence gates. It does not approve low-fidelity Wireframes, final Software Architecture, or application code.

## Authority order

1. Attached English PRD v1.0.
2. Attached English MVP v1.0.
3. Proposed ADR-0001 through ADR-0006 as constraints, not final accepted evidence.
4. System Architecture MVP v1.0.
5. Code and Technical Documentation Blueprint MVP v1.0.
6. GitHub and Figma as workflow/design evidence.

A lower-ranked source may narrow an implementation choice but may not add product scope. In a conflict, the English PRD/MVP win.

## Connected journey

1. User launches the app from a fresh install, incomplete onboarding, education replay, or returning-user state.
2. Splash validates preparation and routes or exposes recoverable startup failure.
3. Incomplete onboarding shows two value slides and a road-attention warning.
4. Continue or Skip remains available; Skip never forces microphone or notification access.
5. Media access is explained before the first scan.
6. Android media permission returns Granted, Denied, or Permanently Denied.
7. Denied preserves Retry and Not now; permanent denial exposes explicit Android Settings.
8. Returning from Settings always rechecks the real Android permission state.
9. Granted access begins an initial or incremental MediaStore scan/reconciliation.
10. Home / Library shows Scanning and resolves to Ready, Empty, recoverable error, or permission recovery.
11. Ready and Empty are truthful states; stale index rows never create false playability.
12. Six-command education may be viewed, skipped, or replayed.
13. Microphone permission is requested only after explicit practice or a real microphone tap.
14. Microphone denial preserves manual browsing and playback.
15. If the selected STT provider performs cloud processing, separate consent and disclosure of processing destination, purpose, and retention are required before the first cloud-processed session.
16. The product must not claim guaranteed offline speech recognition without validated provider evidence.
17. Notification permission remains deferred until playback/background-control value exists and is finalized in UF-06/TS-01.

## Valid endpoints

- Home / Library - Ready.
- Home / Library - Empty with Rescan and permission recovery where relevant.
- Permission Denied with Retry and Not now.
- Permanently Denied with explicit Android Settings and return recheck.
- Recoverable Splash or scan failure with Retry.
- Reconciled revoked/inaccessible media.
- Microphone declined with manual controls preserved.

## State inventory covered

| ID | Surface/system surface | Required states or contract |
|---|---|---|
| S-01 | Splash | Loading, Migration Error |
| S-02 | Onboarding | First Run, Skip, Replay |
| S-03 | Permissions | Granted, Denied, Permanently Denied |
| S-04 | Home / Library | Scanning, Empty, Ready, recoverable failure |
| SYS-01 | Android media dialog | Request after explanation; denial recovery |
| SYS-02 | Android microphone dialog | Just-in-time; manual fallback |
| SYS-03 | Android notification boundary | Deferred; denial cannot corrupt playback state |
| SYS-04 | Android Settings | Explicit entry; return always rechecks |

## Requirement traceability

| Requirement/story | Cluster coverage | Evidence |
|---|---|---|
| FR-01 - consented local scan and library views | UF-01, UF-02, UF-03 | Media permission before scan; MediaStore authority; Scanning/Empty/Ready/recovery |
| US-01 - understandable permission timing and recovery | UF-01, UF-02 | Contextual explanations; Denied/Permanent; Settings return recheck |
| US-02 - account-free local scan and incremental index | UF-01, UF-03 | No account/backend; initial and incremental scan; derived rebuildable index |
| Voice-provider privacy boundary | UF-01, UF-02 | Just-in-time microphone; separate cloud-speech consent and truthful provider disclosure; TS-03 remains open |
| Privacy principles | UF-01 through UF-03 | Just-in-time microphone; no raw audio or personal media telemetry |
| Accessibility | UF-01 through UF-03 | Labeled recovery, scalable text, TalkBack order, no color-only meaning |

## Fixed cross-cutting constraints

- Android 10+ native product for user-authorized local audio.
- No account, authentication, backend, cloud sync, online catalog, streaming, upload, download, or content distribution.
- Home / Library is one top-level destination.
- Android permission state is authoritative.
- MediaStore is authoritative for accessible media existence and platform identity.
- The local index is derived and rebuildable.
- Microphone access starts only after explicit user action.
- Manual controls remain available after voice denial/failure.
- If cloud STT is used, separate consent plus transparent disclosure of processing destination, purpose, and retention are required before use.
- Local-first must not be presented as guaranteed offline speech recognition without validated provider evidence.
- No raw audio, full transcript, personal title, filename, artist, album, folder, URI, path, location, contacts, or unnecessary identifier in telemetry/logging.
- No new Cluster A analytics events are introduced because the Phase 0 allowlist does not define them.

## Definition of Done

- [x] UF-01 first launch and education drawn and documented.
- [x] UF-02 media, microphone, and notification boundaries drawn and documented.
- [x] UF-03 initial/incremental scan and readiness drawn and documented.
- [x] Connected Cluster A swimlane created.
- [x] Denied and Permanently Denied separated.
- [x] Android Settings return rechecks permission.
- [x] Onboarding is skippable and replayable.
- [x] Microphone permission is just-in-time.
- [x] Notification permission is deferred to UF-06/TS-01.
- [x] Cloud STT consent/disclosure and no-guaranteed-offline-copy boundaries are recorded; TS-03 evidence remains open.
- [x] Permission revocation and stale-index reconciliation covered.
- [x] Scanning, Empty, Ready, recoverable failure, and permission-recovery endpoints covered.
- [x] P0 scope is visible and no v1.1 capability was introduced.
- [x] State inventory and requirement traceability completed.
- [x] Privacy and accessibility constraints recorded.
- [x] Open technical questions are routed rather than invented.

## Review and approval record

- **Product Owner:** the 01 August 2026 direction for Cluster A drafting and review has been incorporated; this is not final approval of the complete User Flow set.
- **Product Design:** Cluster A diagrams, state inventory, and traceability completed in Figma.
- **Android Engineering:** preliminary constraints recorded; formal evidence remains required for TS-01, TS-03, and TS-06.
- **QA:** required denial, Settings, scan, Empty/Ready, revocation, and recovery scenarios are recorded.
- **Privacy:** microphone, cloud-consent, telemetry, personal-media, and truthful-provider-disclosure boundaries are preserved; TS-03 evidence remains open.

## Open gates

1. Issue #2 and the ADR register must align on explicit reviewed outcomes before the complete User Flow set is marked Approved.
2. TS-01 must validate notification and foreground media behavior across Android versions.
3. TS-03 must validate voice-provider availability, language support, network dependence, processing destination, separate cloud-speech consent, retention disclosure, and fallback behavior.
4. TS-06 must validate permission, scoped-storage, move/delete, and reconciliation behavior.
5. UF-04 through UF-13 remain to be drafted and reviewed.
6. Issue #15 low-fidelity Wireframes, Issue #16 final Software Architecture, and implementation remain outside this approval.

## Exit decision

Cluster A is complete and registered. The project may proceed to **Cluster B - UF-04 Browse/Search, UF-05 Manual Playback, and UF-06 Background/System Controls** without treating Issue #3 as closed or the complete User Flow set as approved.
