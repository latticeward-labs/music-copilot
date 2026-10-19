# Issue #5 / Phase 11 — Owner-reported physical phone QA (19 October 2026)

## Attribution, device and build provenance

- **Source:** Repository owner's explicit chat report dated 19 October 2026: P11-01 through P11-11 each **PASS**, device **HONOR X7d / Android 15**, APK **artifact #12149209198**, comment «موردی گزارش نشده». In a subsequent follow-up the owner clarified that the Settings-only `Open Settings` action opened Android's App info/Settings; the owner then reconfirmed «همه ی تست ها اوکی بوده».
- **Source type:** **OWNER-SELF-REPORTED PHYSICAL PHONE TEST**, not assistant-executed device testing or independent reviewer/ADB instrumentation.
- **APK:** [Artifact #12149209198](https://github.com/latticeward-labs/music-copilot/actions/runs/39163991680/artifacts/12149209198), [APK-only run #39163991680](https://github.com/latticeward-labs/music-copilot/actions/runs/39163991680), build commit `629caa6a1a3ebafd676cfa3fa8fb5b808c24de46`, `assembleDebug` **SUCCESS**. Artifact APK SHA-256 previously verified on downloaded build: `d6f93b5a5860c889ae02357d933c0584b7a40a6c1e1b388a14906bca4c4df978`. Owner did **not** independently supply the on-phone APK file hash or app install mode.
- **No images/screenshots or screenshots links are copied, uploaded, embedded, or indexed** in GitHub, per owner's explicit instruction. No screen recording, Android `logcat`, OS permission dump, personal audio/media, or raw user data is stored in this report.

## Test-level results, as explicitly reported

| Test ID | Test scope | Reported result |
|---|---|---|
| P11-01 | Update installation and retained onboarding/privacy settings | **PASS (owner-reported)** |
| P11-02 | Library/Settings background-foreground continuity | **PASS (owner-reported)** |
| P11-03 | Screen configuration and presentation recovery | **PASS (owner-reported)** |
| P11-04 | Read-only education Replay and navigation recovery | **PASS (owner-reported)** |
| P11-05 | Media denial/cancellation recovery | **PASS (owner-reported)** |
| P11-06 | Single Android media permission request across recreation | **PASS (owner-reported)** |
| P11-07 | Settings-only handoff; return without permission change | **PASS (owner-reported)** |
| P11-08 | Settings-only handoff; permission grant and explicit Continue | **PASS (owner-reported)** |
| P11-09 | Optional Voice Practice rationale lifecycle recovery | **PASS (owner-reported)** |
| P11-10 | Microphone result and manual/voice boundary recovery | **PASS (owner-reported)** |
| P11-11 | Locally revoked Cloud STT consent persists | **PASS (owner-reported)** |

**Owner report summary:** **11 / 11 PASS, 0 FAIL, 0 NOT RUN** at the test-ID level. Owner note: «موردی گزارش نشده»; later follow-up «همه ی تست ها اوکی بوده». This is the owner's self-attestation for the named checks; it **does not independently establish** every conditional substep (OS support for rotation, exact app install method, optional playback continuity, process-kill tracing, private logs or the absence of an automatic scan). Such granular steps were not supplied as structured logs.

## Focused Settings-only clarification

- **P11-07:** The owner directly confirmed that the in-app `Open Settings` opened the Android app settings. The owner subsequently reaffirmed **all** tests PASS; no independent event trace of unchanged permission or automatic scanning was submitted.
- **P11-08:** The owner submitted **PASS** and reaffirmed all tests successful. No screenshot is uploaded or referenced. The asserted absence of automatic scanning and explicit Continue safety is **owner-reported**, not independently inspected.
- These details improve attribution without upgrading owner testimony into native automated coverage.

## Acceptance boundary

- **Owner physical-phone QA:** `OWNER_REPORTED_PHONE_QA_11_OF_11_PASS`.
- **APK-only build:** real Gradle compilation and GitHub artifact upload `SUCCESS`; compilation **does not execute** phone tests, JVM privacy tests or instrumentation.
- **P11 Kotlin/JVM lifecycle/privacy suite:** 12 tests **AUTHORED / NOT RUN** under owner-requested cost control. Older P10 authored JVM suite also remains **NOT RUN**.
- **Static privacy/architecture/Lint/Detekt/accessibility and other Android API/OEM compatibility:** not verified by this owner's single-Android-15 report.
- **P11 overall:** `IMPLEMENTED / BUILD_SUCCESS / OWNER_REPORTED_PHONE_QA_11_OF_11_PASS / TECHNICAL_GATES_PENDING — NOT FULL P11 PASS`.
- **Issue #5:** OPEN, 13 original integrated issue-wide acceptance criteria unchecked; no PR merge, no final sign-off and no Phase 12 work implied.

See [P11 phone checklist](ISSUE_5_PHASE11_PHONE_QA_CHECKLIST.md), [P11 validation ledger](ISSUE_5_PHASE11_VALIDATION.json), [Evidence Index](ISSUE_5_EVIDENCE_INDEX.md), and [P11 audit](ISSUE_5_PHASE11_LIFECYCLE_PRIVACY_AUDIT.md).
