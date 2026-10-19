# Issue #5 — Phase 11 owner-led Android phone QA

**Do not execute remotely or invent device proof.** Phase 11 APK delivery via [APK-only workflow](../implementation/ISSUE_5_LOW_COST_CI_PHONE_QA_PROTOCOL.md). Device expected: **HONOR X7d / Android 15**; owner reconfirms.

## Before testing

1. Download the final [artifact #12149209198](https://github.com/latticeward-labs/music-copilot/actions/runs/39163991680/artifacts/12149209198) from [final P11 APK-only run #39163991680](https://github.com/latticeward-labs/music-copilot/actions/runs/39163991680) (build SHA `629caa6a1a3ebafd676cfa3fa8fb5b808c24de46`), not the superseded P11 run #39163624612 or the older P10 artifact #12147657477.
2. Verify `app-debug.apk`, `app-debug.apk.sha256` and `BUILD_PROVENANCE.txt` inside the ZIP if feasible. Expected APK SHA-256: `d6f93b5a5860c889ae02357d933c0584b7a40a6c1e1b388a14906bca4c4df978` (the downloaded GitHub artifact was independently hash-checked after extraction). Report artifact ID, commit and whether this is an update/clean install. **Do not clear app data** for persistence tests.
3. Use only personal phone test observations. No raw audio, speech transcript, media titles, path/URI, Bluetooth address, account data or exact location in screenshots/logs.
4. If a test depends on already-granted permission, an OEM option, an available voice provider or screen rotation, say **NOT RUN / NOT TESTABLE** for that subcase rather than assuming PASS.

## Physical device test matrix

| Test ID | Owner's action | Expected result |
|---|---|---|
| P11-01 | Update-install P11 APK over P10, reopen | Previous onboarding completion and Cloud STT revoked/disabled status remain; no unexpected onboarding reset, login or new permissions |
| P11-02 | Open Library → Settings, go Home, reopen via recent apps | Same screen/visible settings state; no crash, duplicated dialog, scan or microphone request |
| P11-03 | On Settings, rotate device if supported (or change display/theme and return) | Controls/readability maintained, no consent silently granted or media playback status falsified |
| P11-04 | Open **Replay introduction** from Settings, go to slide 2, rotate/background and return | Remain at the appropriate slide without extra initial onboarding completion or duplicate media/microphone request; Back returns to Settings |
| P11-05 | Return to app, review media access rationale and deny/cancel (revoke media permission in Android settings first if needed), rotate/reopen | Denial/recovery remains accessible, no unsolicited native permission prompt, no automatic library scan |
| P11-06 | From explicit media permission button request OS access once; if available rotate during or immediately after system dialog | No second OS permission dialog launched by Activity recreation; safe Granted/Denied recovery, no crash |
| P11-07 | For Settings-only media recovery after repeated denial, tap **Open Android Settings**, leave permission unchanged and return | Still recoverable; no forced Settings loop or automatic scan. If OEM does not expose Settings-only state, mark NOT TESTABLE |
| P11-08 | Repeat P11-07 but explicitly grant media permission in Android Settings then return | One truthful Granted result; explicit Continue only (no auto-scan, no duplicate action). If no Settings-only route, mark NOT TESTABLE |
| P11-09 | Replay introduction → voice slide → **Practice Voice** rationale; rotate/background and return before confirming | Rationale or safe manual fallback remains; no mic dialog or recording until explicit rationale Continue |
| P11-10 | Explicitly request microphone in Practice Voice, deny (or grant), then rotate/re-enter/replay | Permission result appears safely once; no unrequested second Android dialog, no automatic microphone capture or cloud STT |
| P11-11 | With consent revoked in Settings, leave app and return or recreate Activity using Android Developer Options **Don't keep activities** when comfortable | Cloud STT remains disabled/revoked; app recovers, not granting remote STT, no login. Do not use Clear storage |

Optional observation: With a local playable audio item, background/foreground/recreation must not invent an autoplay, reset an authoritative MediaSession queue, or mark a blocked/unavailable library Ready. If no actual playback UI exists at this Issue #5 scope, mark this audio subcheck **NOT TESTABLE**, not PASS.

**Do not use Force stop as a substitute for pending ActivityResult restoration**: force stop deliberately terminates the Android lifecycle/result chain. It can be separately reported as cold-relaunch recovery.

## Owner report format

```text
P11 phone QA
Date:
Device / Android:
P11 APK artifact #:
P11 APK build SHA / checksum if known:
Install: update or clean:
P11-01: PASS / FAIL / NOT RUN
P11-02: ...
...
P11-11: PASS / FAIL / NOT RUN
Conditional checks not testable:
Observed errors (no personal data):
```

After the owner sends the results, record **OWNER-REPORTED PHYSICAL PHONE** in the Issue and Evidence Index. Do not claim independent hardware attestation. P11 JVM unit and privacy/static checks are **separate technical gates**, not phone tests.


## Owner-reported execution — 19 October 2026

**Owner:** HONOR X7d / Android 15, artifact `12149209198` (build run `39163991680`). Owner explicitly returned **P11-01..P11-11 PASS**, with «موردی گزارش نشده», then reconfirmed «همه ی تست ها اوکی بوده». Settings-only `Open Settings` was verbally confirmed to open Android App info. See [owner report](ISSUE_5_PHASE11_OWNER_PHONE_TEST_REPORT_2026-10-19.md) and [validation ledger](ISSUE_5_PHASE11_VALIDATION.json).

**Disposition:** all 11 named phone test IDs **OWNER-REPORTED PASS**; 0 reported FAIL, 0 NOT RUN. No screenshots uploaded, linked or registered, as expressly requested. No independently witnessed Android/OS trace, separate conditional subtest log, screenshot or device hash was supplied. **NOT a JVM, accessibility, cross-API or full-P11 PASS.**
