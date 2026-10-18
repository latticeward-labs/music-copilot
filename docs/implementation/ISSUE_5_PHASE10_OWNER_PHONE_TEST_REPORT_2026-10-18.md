# Issue #5 / Phase 10 — Owner-reported physical phone test results (18 Oct 2026)

## Attribution and provenance

- **Source:** The repository owner's direct report in the ChatGPT project conversation on 18 Oct 2026. This is owner self-attestation, **not** independently observed hardware testing or an AI-executed device test.
- **Device reported:** HONOR X7d, Android 15.
- **APK artifact reported:** [#12147657477](https://github.com/latticeward-labs/music-copilot/actions/runs/39158645552/artifacts/12147657477), P10 APK-only [workflow #39158645552](https://github.com/latticeward-labs/music-copilot/actions/runs/39158645552), build commit `58150070e9ace936eda6d43f4d562903fb784996`.
- **APK/source association:** The owner identified artifact ID 12147657477. The owner's separate local APK SHA-256, BUILD_PROVENANCE.txt contents and actual install method were **not supplied/independently verified**.
- **Owner notes (verbatim):** «همه پاس شد».

## Results reported by owner

| Test ID | Purpose | Owner-reported result |
| --- | --- | --- |
| P10-01 | Installation/onboarding; no unexpected notification runtime prompt | **PASS** |
| P10-02 | Settings shows MediaSession-managed notifications / cloud recognition disabled | **PASS** |
| P10-03 | Revoke previously stored cloud-speech consent in Settings | **PASS** |
| P10-04 | Consent revocation persists across Settings navigation/app restart | **PASS** |
| P10-05 | Android notification settings off; no permission loop / false playback state | **PASS** |
| P10-06 | Decline optional Voice Practice mic; preserve manual controls / no cloud prompt | **PASS** |
| P10-07 | Settings background/foreground and supported rotation; no duplicate prompts/consent grant | **PASS** |

**Summary:** 7/7 owner-reported PASS; 0 owner-reported FAIL; 0 NOT RUN.

This is a summary of the owner's test-level verdicts, **not** a claim that all individual conditional subchecks (e.g., notification toggle availability, actual music audio continuity, rotation support) were separately logged. No screenshots, OS permission dumps, logs, APK SHA-256, or independent reviewer observations were submitted.

## Acceptance boundary

- **Physical-phone manual QA:** 7/7 **PASS as reported by owner** on HONOR X7d / Android 15.
- **APK-only build:** Previously **SUCCESS**; proves `assembleDebug` only.
- **P10 authored Kotlin/JVM unit tests:** **NOT RUN** under APK-only CI, not marked passed.
- **API29/32/33+ notification/consent compatibility beyond reported Android 15 handset:** **NOT VERIFIED by this report**.
- **Actual cloud STT / remote disclosure/consent flow:** **NOT APPLICABLE** in this build; remote provider and approval absent, fail-closed.
- **P10 phase-wide verification:** **PARTIAL — owner phone QA passed; required technical gates still pending.**
- **Issue #5:** OPEN. Original 13 integrated issue-wide acceptance criteria remain unapproved pending P10 technical gates and P11–P14.

See [official phone checklist](ISSUE_5_PHASE10_PHONE_QA_CHECKLIST.md), [validation ledger](ISSUE_5_PHASE10_VALIDATION.json), and [evidence index](ISSUE_5_EVIDENCE_INDEX.md).
