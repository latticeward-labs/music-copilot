# Issue #5 / Phase 12 — Accessibility & privacy-safe acceptance audit

**Scope:** Onboarding WF-02/HF-02, permission WF-03, Settings education replay and app-owned Cloud STT disabled status. No change to approved screen hierarchy, no extra feature/remote STT, no Player/Queue ownership.

**Source-derived requirements:** MSM2 PRD "minimum 48dp touch target / sufficient contrast and scalable text / complete TalkBack labels"; MVP dark high-contrast, 48dp+ controls and one-layer driving restrictions; first-readiness User Flows require scalable text, logical TalkBack reading order, explicit action labels, non-color-only state, reachable recovery. Uploaded P12 phase plan requires 200% text, TalkBack, focus return, tri-state native recovery, lint/unit/instrumented and cross-API evidence. Issue #17 Night Signal token mapping remains the visual authority.

## Source-level changes and audited target properties

| Requirement | Implementation | Confidence / remaining verification |
| --- | --- | --- |
| Semantic headings for Home/Library/Settings and fatal Bootstrap retry | `app/navigation/AppNavHost.kt` added `Modifier.semantics { heading() }` to `FirstLaunchBoundary` title. | Source change recorded; actual TalkBack tree still to be observed |
| Media rationale focus/reading-order on return | `MediaPermissionRationaleScreen.kt` adds remembered `FocusRequester` and one `LaunchedEffect(Unit)`, focusable heading marked `heading()`. Does not open native dialog. | Source change recorded; TalkBack/recomposition must be observed |
| Native media results focus | Existing `MediaPermissionOutcomeScreen.kt` retains focusable heading and polite live region tied to OS result; primary actions explicit. | Existing, to be regression-tested |
| 48dp targets incl. Back/Skip/Retry/Settings | Current Onboarding and media permission Compose screens consistently use `heightIn(min = 48.dp)`; Library/Settings use `MusicSize.touchTargetMinDp = 48.dp`. | Static policy suite authored, UI size assertion authored; native target measurement pending |
| 200% scalable text | `safeDrawingPadding` + `verticalScroll` in onboarding/rationale/result and Library/Settings; a 200% `LocalDensity(fontScale = 2f)` instrumented Settings-only Compose regression added. | Android instrumentation **NOT RUN**; phone validation pending |
| Non-color-only visible status | Text status "Access granted", "Access denied", "Open Android Settings"; spoken "Page 1/2" and "Page 2/2"; named buttons. | Authoring checked; TalkBack manual verification pending |
| Contrast | P12 JVM checker evaluates actual `MusicColors` constants against WCAG AA normal-text 4.5:1 and includes a failing synthetic low-contrast fixture. Enabled semantic roles are checked; disabled-only token not misclassified as regular body copy. | Automated checker **AUTHORED, NOT RUN**; native visual contrast checks pending |
| Privacy/Android permission separation | Manifest no INTERNET/POST_NOTIFICATIONS; no native permission launch from Composable; ownership remains Activity+OS; closed telemetry schema no remote STT. | Source-reviewed; final-head privacy negative tests **NOT RUN** |

## Measured source-token contrast (algorithm, not phone or UI test)

WCAG 2 relative luminance `(Lmax+0.05)/(Lmin+0.05)`; values below are calculated from the committed source token values, **not proof** of rendered alpha compositing on every platform:

| Pair | Ratio (approximate) | 4.5:1 |
| --- | ---: | --- |
| textPrimary #F5F7FA / background #090D12 | 18.15:1 | >= 4.5 |
| primary #67D8FF / background #090D12 | 11.91:1 | >= 4.5 |
| textSecondary #C8D0DA / surface #111720 | 11.56:1 | >= 4.5 |
| textSecondary #C8D0DA / surfaceVariant #18212C | 10.44:1 | >= 4.5 |
| onPrimary #031117 / primary #67D8FF | 11.71:1 | >= 4.5 |
| error #FF7088 / background #090D12 | 7.35:1 | >= 4.5 |

The disabled-only gray `#7C8998` against `#202A36` is ~4.07:1; this does not satisfy 4.5:1 for enabled normal copy and MUST NOT be used as regular body text without correction. The P12 negative checker specifically rejects that pair. Focus state and dynamic UI placement cannot be signed off from token math alone.

## Runtime evidence needed for pass

- Instrumented Compose tests at real emulator API29/31/33/35, API36 if available: native OS permission denial/grant, 200% text, focus/action semantics and 48dp touch targets. Test code and a single-API opt-in workflow have been authored; **no P12 CI run ID or test PASS exists yet**.
- On the actual HONOR X7d Android 15: [P12 owner checklist](ISSUE_5_PHASE12_PHONE_QA_CHECKLIST.md). Report screen reader focus/heading/reading order, maximal or actual 200% font scale, and whether any element clips or becomes inaccessible. No screenshots to GitHub per owner's standing restriction.
- Critical negative cases must include Denied/SettingsOnly/Granted, Retry/Not now, skipped voice, disabled cloud processing and no duplicate OS prompt/automatic scan; real status is OS-owned.

**Status at initial authoring:** `P12_ACCESSIBILITY_IMPLEMENTED_SOURCES_AND_TESTS_AUTHORED / NO_BLOCKING_ISSUES_CLAIMED_AS_VERIFIED / RUNTIME_QA_PENDING`. Open-blocker count is *unknown*, not zero. **No full Phase 12 PASS** until required evidence actually exists.

[17-scenario test matrix](ISSUE_5_TEST_MATRIX.md) · [owner phone checklist](ISSUE_5_PHASE12_PHONE_QA_CHECKLIST.md) · [machine ledger](ISSUE_5_PHASE12_VALIDATION.json).


## APK-only delivery — 19 October 2026

The final P12 debug APK built successfully: [run #39243014999](https://github.com/latticeward-labs/music-copilot/actions/runs/39243014999), commit `a152312ca3ce77151df5df1329b804746e567333`, [artifact #12181988538](https://github.com/latticeward-labs/music-copilot/actions/runs/39243014999/artifacts/12181988538), APK SHA-256 `ba4425364016ab9c969095756a01a57f5c814624c8166e247e707cd41cc23dc8` (35801343 bytes), checked against the artifact's SHA file and BUILD_PROVENANCE. **APK-only compilation is NOT proof** of compiled/executed Compose androidTest code, contrast unit tests, TalkBack runtime, 200% reflow or zero blocking violations. Owner physical tests remain pending. No screenshot files or links registered.


## Owner-confirmed accessibility phone tests — 21 October 2026

[Owner's direct phone QA report](ISSUE_5_PHASE12_OWNER_PHONE_TEST_REPORT_2026-10-21.md) attributes **P12-01..P12-12 PASS as self-reported (12/12, 0 FAIL, 0 NOT RUN)** to the HONOR X7d Android 15, APK artifact #12181988538. This includes test IDs for TalkBack (P12-04/05/06), font scaling, permissions and Settings, but no independent TalkBack traversal tree, exact 200% font-scale readout, measured 48dp targets, cross-API OS evidence or P12 automated CI results were supplied. Therefore **blocking accessibility defects have not been independently excluded**. No screenshot is registered/linked at owner's instruction. The author's 6 JVM + 6 Compose instrumented tests and multi-API validations are still **NOT RUN**; **P12 NOT FULL PASS**.
