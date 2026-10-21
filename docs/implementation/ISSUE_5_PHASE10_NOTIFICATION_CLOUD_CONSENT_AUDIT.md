# Issue #5 P10 — Notification exemption and cloud consent privacy gate

Scope authority: uploaded `ISSUE_5_PHASED_EXECUTION_PLAN(1).md`, P10, AC-03/08/13; MSM2 RAR (PRD/MVP/ADR-0004), [TS-01](../user-flows/evidence/TS-01_NOTIFICATION_AND_FOREGROUND_MEDIA.md), UF-02/12 and approved Playback/Voice contracts.

## Platform matrix (actual shipped feature set)

| Android API | MediaSession playback notification | Runtime POST_NOTIFICATIONS in THIS APK | Non-exempt notification in scope |
|---|---|---|---|
| 29–32 | Media3 service-owned; no POST_NOTIFICATIONS runtime permission | Not applicable | None |
| 33–36 | MediaSession notification exemption (system may hide drawer notification if toggled off, but MediaSession/Playback state remains independent) | **No request; no manifest declaration** | None |

`NotificationPermissionPolicy` implements the P10 negative gate. A hypothetical future approved non-exempt notification would require API33+, value already demonstrated and a separate contextual gesture; it is **not** registered in production. There is no notification ActivityResult launcher or permission-value-to-playback mapping. OS/Media3 retain notification/service truth.

## Cloud speech explicit disabled mode

Production VoiceBindings uses only `AndroidOnDeviceSpeechToTextEngine` and `UnavailableSpeechToTextEngine` as appropriate; no remote adapter or network fallback is registered. `CloudSpeechConsentPolicy` requires: approved remote adapter, Legal/Privacy approval, disclosure with nonempty processor/destination/purpose/retention, explicit local consent, and not driving. Production `ApplicationBindings.remoteSpeechDecision` **always supplies false/no disclosed processor/no adapter** and stays DisabledNoApprovedAdapter even if DataStore historically contains Granted.

DataStore already persists `CloudSpeechConsent` locally, independently from microphone/notification Android permissions. Settings displays accurate disabled state and allows an explicit local revocation (`Revoked`); no misrepresentative opt-in is presented while remote provider is unapproved.

No new cloud endpoint, Internet permission, backend, identity, recording, VoiceSession, Media3 player or Queue owner. User denial/revocation cannot pause playback or enable remote processing.

## Acceptance

- Policy unit tests: API29..36 no superfluous prompt; hypothetical non-exempt contextual gate; every remote fail-closed reason, negative source wiring and local revoke/persistence integration.
- **APK build SUCCESS**: [Actions #39158645552](https://github.com/latticeward-labs/music-copilot/actions/runs/39158645552), commit `58150070e9ace936eda6d43f4d562903fb784996`, job `Single Gradle Debug APK - no emulator`; real Gradle `assembleDebug` printed `BUILD SUCCESSFUL`. [Artifact #12147657477](https://github.com/latticeward-labs/music-copilot/actions/runs/39158645552/artifacts/12147657477) contains `app-debug.apk`, SHA-256 and `BUILD_PROVENANCE.txt`, 7-day retention until 25 October 2026. **NO native/emulator test performed by assistant** per user instruction.
- Owner physical QA [checklist](ISSUE_5_PHASE10_PHONE_QA_CHECKLIST.md): **7/7 test-level PASS reported by the owner on HONOR X7d / Android 15** on 18 October 2026. See [attributed report](ISSUE_5_PHASE10_OWNER_PHONE_TEST_REPORT_2026-10-18.md). This is self-reported, not independently device-observed.
- P10 status: **IMPLEMENTED / APK BUILD SUCCESS / OWNER PHONE QA SELF-REPORTED 7/7 PASS / TECHNICAL VERIFICATION PENDING (NOT FULL PHASE PASS)**. No Phase 11 changes; original Issue #5 remains OPEN.


### Owner physical phone report (18 October 2026)

Owner explicitly submitted P10-01..P10-07 **PASS** on HONOR X7d / Android 15 against APK artifact #12147657477, noting «همه پاس شد». No FAIL or NOT RUN test ID. Recorded in [P10 owner report](ISSUE_5_PHASE10_OWNER_PHONE_TEST_REPORT_2026-10-18.md) and [Evidence Index](ISSUE_5_EVIDENCE_INDEX.md). No screenshots, APK local hash, Android logs or independently confirmed playback continuity accompanied the report. The APK-only workflow did not execute authored policy JVM tests. **No full phase or Issue #5 closeout claim**; P11 not started.
