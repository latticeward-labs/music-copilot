# Issue #5 / Phase 10 — Owner-led physical Android testing

**APK ONLY**. Testing is performed on the owner's phone and will be recorded only after the user sends actual results. No emulator/native test is authorized in this phase. Never infer a PASS from APK compilation.

App: `latticeward-labs/music-copilot`, `feature/5-onboarding-permissions-recovery`.

**Exact delivery:** [download APK artifact #12147657477](https://github.com/latticeward-labs/music-copilot/actions/runs/39158645552/artifacts/12147657477). ZIP contains `app-debug.apk`, `app-debug.apk.sha256`, and `BUILD_PROVENANCE.txt`. Successful build SHA `58150070e9ace936eda6d43f4d562903fb784996`. The artifact expires on 25 October 2026.
Device expected from previous QA: HONOR X7d, Android 15, **must be reconfirmed by owner**.
Install the exact APK from the P10 GitHub Actions artifact; record its `BUILD_PROVENANCE.txt`, artifact ID and SHA-256.

| ID | Step | Expected (owner marks PASS/FAIL/NOT RUN) |
|---|---|---|
| P10-01 | Install as update, open app, complete or Skip onboarding | No `Allow notifications` runtime dialog on Android 15; manual library remains available |
| P10-02 | In app tap **Settings** from Library; scroll below Replay introduction | Visible `Playback notifications: MediaSession-managed` and `Cloud speech recognition: disabled` text, no remote permission request |
| P10-03 | In Settings tap **Revoke saved cloud speech consent** | Visible `Any previously saved cloud speech consent has been revoked` |
| P10-04 | Back to Library; return Settings / fully close and reopen app then Settings | Revoked status remains (DataStore persisted); no crash, new account, remote STT or mic recording |
| P10-05 | With Android notification setting off (if Android offers it), open app, return Library | No in-app notification permission loop; Library/Playback state not mislabeled Stopped; if no music, note "Not testable" for audio continuity |
| P10-06 | Optional Practice Voice (if first-run education/replay reachable), decline mic | Manual music controls still work; no cloud speech consent prompt |
| P10-07 | Background/foreground and rotate at Settings | No duplicate notification/mic prompt, no accidental Cloud consent grant; UI legible |

**Safety:** Do not clear app data for update tests. Screenshots should not include personal media details, contact data or spoken transcripts. P10 is not a real cloud provider integration. DO NOT claim live remote STT tests are possible.

**Report template:** `Device / Android version / APK artifact ID / install update or clean / P10-01..07 PASS, FAIL or NOT RUN + observed difference / screenshots if safe`.
After reporting, results will be entered in Issue #5 and Evidence Index with attribution **owner-reported physical phone**. No AI-generated device attestation or invented PASS.


## Reported execution results — 18 October 2026

**Owner-reported** physical Android testing, device **HONOR X7d / Android 15**, APK artifact **#12147657477**: `P10-01 PASS`, `P10-02 PASS`, `P10-03 PASS`, `P10-04 PASS`, `P10-05 PASS`, `P10-06 PASS`, `P10-07 PASS`. Owner note: «همه پاس شد». **7/7 reported PASS, 0 FAIL, 0 NOT RUN.**

See [attributed report](ISSUE_5_PHASE10_OWNER_PHONE_TEST_REPORT_2026-10-18.md). **No independent device observations/screenshots or APK hash were supplied**. Exact installation mode, conditional playback audio continuity and rotation support were not separately described. P10 JVM policy tests remain authored but unexecuted in the APK-only build. Physical-phone owner QA accepted as **self-report**, not full phase/device matrix sign-off.
