# Issue #5 — P12 owner-only physical phone accessibility QA

**Target owner device:** HONOR X7d, Android 15. Final **P12 APK artifact #12181988538** ([GitHub Actions #39243014999](https://github.com/latticeward-labs/music-copilot/actions/runs/39243014999)); exact-source build SHA `a152312ca3ce77151df5df1329b804746e567333`, APK SHA-256 `ba4425364016ab9c969095756a01a57f5c814624c8166e247e707cd41cc23dc8` (35801343 bytes). Build `assembleDebug` SUCCESS; unit/Lint/native not run in APK workflow. **Never reuse the P11 artifact #12149209198 for P12 acceptance**.

This is a **manual-phone** checklist. P12 additionally requires executed JVM, Compose instrumentation, native OS/API matrix and lint/static checks; phone PASS alone does **not** confer overall P12 PASS. Do **not** upload, attach or link screenshots in GitHub; the owner explicitly requested no screenshots. A text-only report is sufficient to record owner observation.

## Before starting

1. Update-install P12 `app-debug.apk` from [artifact #12181988538](https://github.com/latticeward-labs/music-copilot/actions/runs/39243014999/artifacts/12181988538), NOT P11. Keep data/preferences; do not clear storage. Record installation method as update or clean.
2. In Android **Settings → Accessibility → TalkBack**, enable screen reading when the test calls for it. The exact navigation may vary on HONOR. Use swipe-right to traverse content and double tap to activate while TalkBack is enabled. If not comfortable, mark TalkBack cases NOT RUN.
3. For scaling cases, increase Android **Font size** to **200% if available**. If the phone cannot achieve or report 200%, record the maximum scale actually chosen; do **not** claim 200% proof from an unknown slider percentage. After tests, restore personal accessibility settings.
4. Do not drive while performing voice or UI tests. No recording or private music evidence is necessary.

## P12 tests (report PASS / FAIL / NOT RUN for each)

| ID | What to do on the installed P12 APK | Expected result |
| --- | --- | --- |
| P12-01 | Open updated app and enter Library/Settings without clearing existing data | No new onboarding, no unexpected permission/dialog, state remains truthful |
| P12-02 | Set large text up to actual **200%** if available; go Settings → Replay introduction, view slide 1 | Headline/body/Continue/Back remain legible and reachable by scrolling; nothing clips or overlays |
| P12-03 | Replay slide 2 (six English sample commands); scroll to bottom | Six example lines, Practice Voice, Skip, Back, Finish without Voice remain navigable; no forced microphone |
| P12-04 | Turn on TalkBack; enter Settings and Library | Screen names announced as headings; status text meaningful, actions are announced with names, not "unlabeled button" |
| P12-05 | TalkBack → Replay introduction → two slides | Voice reads "Page 1 of 2" / "Page 2 of 2" as applicable; heading then body then actionable controls in meaningful reading order |
| P12-06 | With TalkBack on, focus Back, Skip, Continue, Practice Voice, Settings buttons; double-tap relevant actions | Focus does not get trapped; controls activate once; manual-only/Skip works |
| P12-07 | Disable TalkBack if needed. Revoke Music/audio permission in Android settings, return, choose Review music access, then decline native prompt | Media rationale, Denied title, Try again/Not now are visible; no automatic scan or repeated prompt |
| P12-08 | From Settings-only (if reachable), activate Open Settings, grant music permission and return | Title "Access granted" announced/readable, explicit Continue shown; no scan before tap. If OEM cannot show Settings-only, mark NOT RUN |
| P12-09 | Return to Library and revoke media permission again | Unavailable status is stated in text, not conveyed only through color; no false Ready/playing |
| P12-10 | Replay slide 2 → Practice Voice; inspect rationale with TalkBack/large text then decline/allow as desired | Mic purpose readable, all actions at least comfortably touchable, no automatic speech recording/cloud request |
| P12-11 | Open internal Settings; check notification exemption text, cloud speech disabled and revoke saved consent | Descriptions and confirmation are understandable without color; no backend/login/remote STT |
| P12-12 | While large text or TalkBack is enabled, Home/return and supported rotation/recreation across Settings and media outcome screens | Focus/reading order remains navigable; no crash, lost essential action, duplicated Android dialog or silent auto-action |

**Separate visual check:** Buttons must be comfortably operable, no target too small or overlapping. Exact **48dp** size requires instrumented measurement; perception alone does not prove it. Text/background contrast should remain readable in dark mode; screen reader must announce status text rather than using colors alone.

If a feature isn't available (actual OS Settings-only, TalkBack setup, 200% font slider, rotation mode, speech provider) use **NOT RUN** and explain. A PASS on the ID does not automatically assert all conditional substeps or playback features beyond Issue #5.

## Copyable owner report

```text
Issue #5 P12 physical phone QA — OWNER-REPORTED
Device: HONOR X7d / Android 15
P12 APK Artifact ID:
Test date:
Install mode: update / clean / unknown
Font scale actually tested: 200% / max unknown / other
TalkBack tested: yes / no
P12-01: PASS / FAIL / NOT RUN
P12-02: PASS / FAIL / NOT RUN
P12-03: PASS / FAIL / NOT RUN
P12-04: PASS / FAIL / NOT RUN
P12-05: PASS / FAIL / NOT RUN
P12-06: PASS / FAIL / NOT RUN
P12-07: PASS / FAIL / NOT RUN
P12-08: PASS / FAIL / NOT RUN
P12-09: PASS / FAIL / NOT RUN
P12-10: PASS / FAIL / NOT RUN
P12-11: PASS / FAIL / NOT RUN
P12-12: PASS / FAIL / NOT RUN
Observations / unsupported subtests:
No screenshots to GitHub.
```

[Matrix and technical caveats](ISSUE_5_TEST_MATRIX.md) · [Accessibility audit](ISSUE_5_ACCESSIBILITY_AUDIT.md) · [validation ledger](ISSUE_5_PHASE12_VALIDATION.json).
