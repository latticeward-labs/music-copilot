# Issue #5 — Phase 11 owner-led Android phone QA

**Do not execute remotely or invent device proof.** Phase 11 APK delivery via [APK-only workflow](../implementation/ISSUE_5_LOW_COST_CI_PHONE_QA_PROTOCOL.md). Device expected: **HONOR X7d / Android 15**; owner reconfirms.

## Before testing

1. Download the **P11-specific** GitHub Actions artifact announced in Issue #5, not the older P10 #12147657477.
2. Verify `app-debug.apk`, `app-debug.apk.sha256` and `BUILD_PROVENANCE.txt` inside the ZIP if feasible. Report artifact ID, commit and whether this is an update/clean install. **Do not clear app data** for persistence tests.
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
