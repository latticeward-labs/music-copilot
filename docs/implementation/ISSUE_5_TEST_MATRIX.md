# Issue #5 / P12 — Automated, accessibility and Android OS test matrix

**Authority:** user-uploaded `ISSUE_5_PHASED_EXECUTION_PLAN(1).md` Phase 12 and 17 scenario register, MSM2 PRD/MVP (48dp, scalable font, TalkBack, contrast), approved User Flow/Wireframes, native Android OS runtime, and the accepted design tokens in Issue #17.

**Cost/owner contract:** normal feature pushes have no full CI; `issue-5-apk-only.yml` builds only the APK after an explicitly committed request. The P12 verification workflow is opt-in and was **not run** at the time of authoring. No physical phone/ADB tests are impersonated. **Do not upload or register screenshots** (owner requested text-only device results).

## Implementation and test entry points

| Seam | Executable test or verification | Current status |
| --- | --- | --- |
| Pure reducers: Bootstrap/first-incomplete/replay | `Issue5Phase2StateMachineTest`, `Issue5Phase4OnboardingTest`, `Issue5Phase3FirstLaunchTest` | Historical tests exist; P12 final-head regression **NOT RUN** |
| Pending actions, media tri-state, revoke and Settings | `Issue5Phase5MediaPermissionTest`, `Issue5Phase6PermissionOutcomesTest`, `Issue5Phase7SettingsRevocationTest` and P11 lifecycle tests | Authored; P12 HEAD run **NOT RUN** |
| Cloud consent fail-closed, privacy negative | `Issue5Phase10NotificationCloudConsentTest`, `Issue5Phase11LifecyclePrivacyTest` | Authored; deliberately **NOT RUN** in APK-only CI |
| Contrast and no-color-only, 48dp source contracts | `Issue5Phase12AccessibilityPolicyTest` (6 pure JVM tests, including negative low-contrast fixture) | **AUTHORED / NOT RUN** |
| Compose rationale/outcome accessibility | `Issue5Phase12AccessibilityUiTest` (6 instrumented UI tests: action labels, disabled pending, Settings return, granted Continue, 200% font) | **AUTHORED / NOT RUN** |
| Native OS permission, denied/manual, explicit grant, no notification gate | `scripts/issue5_phase12_native_test.sh` and `issue-5-phase12-accessibility-validation.yml` on API29/31/33/35/36 | **AUTHORED / NONE RUN**; API36 only if emulator image/CI supported |
| Static package/privacy and format/lint | `scripts/check_architecture_dependencies.py`, `scripts/check_phase9_privacy.py`, Gradle `spotlessCheck detekt :app:lintDebug` | **NOT RUN** on P12 head |
| Compile/debug APK for owner's HONOR X7d | Existing APK-only workflow, runs only on request marker | P12 build **PENDING** at initial document authoring |
| TalkBack/200% font/real OEM UI manual QA | [Owner phone QA checklist](ISSUE_5_PHASE12_PHONE_QA_CHECKLIST.md) | **NOT RUN / owner report pending** |

## 17 source-registered scenarios (not falsely converted to PASS)

| Scenario | Expected invariant | P12 automation / phone target | Result |
| --- | --- | --- | --- |
| P-01 | Fresh run 2 slides, no early permission | onboarding JVM + owner P12-01/02 | P12 HEAD NOT RUN |
| P-02 | Skip keeps no Mic/Notification prompt | JVM & owner P12-03 | P12 HEAD NOT RUN |
| P-03 | Media grant correct OS on API29/31/33/35 | native smoke per supported API | P12 MATRIX NOT RUN |
| P-04 | Deny and Not now do not scan/crash | native smoke + owner P12-07 | P12 HEAD NOT RUN |
| P-05 | Retry is explicit, no dialog loop | outcome Compose + owner P12-07 | P12 HEAD NOT RUN |
| P-06 | Settings-only→Granted fresh OS truth | existing P07 suites + owner P12-08 | P12 HEAD NOT RUN |
| P-07 | Settings return Requestable | existing P07 native and source matrices | P12 HEAD NOT RUN |
| P-08 | Settings return StillSettingsOnly | existing P07 native and owner P12-08 | P12 HEAD NOT RUN |
| P-09 | Revocation destroys stale Ready | P08 JVM + owner P12-09 | P12 HEAD NOT RUN |
| P-10 | Optional mic denial/manual fallback | P09 JVM and owner P12-10 | P12 HEAD NOT RUN |
| P-11 | Mic provider unavailable distinctly | P09 policy + owner P12-10 where possible | P12 HEAD NOT RUN |
| P-12 | No Cloud STT without disclosure | P10/11 + P12 policy; owner P12-11 | P12 HEAD NOT RUN |
| P-13 | MediaSession exempt; no redundant notification prompt | P10 + P12 Android native check | P12 HEAD NOT RUN |
| P-14 | Rotation/Settings/foreground no double action | P11 JVM + owner P12-12 | P12 HEAD NOT RUN |
| P-15 | Education Replay no Queue/position reset | P04/11 JVM; ownership observation only if playback UI exists | P12 HEAD NOT RUN |
| P-16 | TalkBack and 200% text, order, 48dp | P12 UI test + owner P12-02/04/05/06/12 | P12 HEAD NOT RUN |
| P-17 | Crash and telemetry no forbidden values | P11 JVM, root privacy/architecture scripts | P12 HEAD NOT RUN |

## CI contract and non-claims

`.github/workflows/issue-5-phase12-accessibility-validation.yml`: **no automatic run on production source/docs commits**. When the user explicitly authorizes verification, either a separate commit touching `.github/issue5-p12-verify-request.txt` can launch just the JVM/Lint gate on this branch, or `workflow_dispatch` supports JVM or one chosen native API after this workflow is available on default. The native branch job runs one emulator API at a time, `connectedDebugAndroidTest` and the real OS smoke; it does not claim all API versions checked from one run.

- The phone APK build remains separate and never runs instrumentation, JVM unit tests, a device or an emulator.
- The new instrumentation dependencies and UI test sources are **not proven compilable** by `:app:assembleDebug`; compilation requires `:app:assembleDebugAndroidTest`/instrumented job.
- No screenshots, logcat bundles, personal audio/media or raw OS identifiers are uploaded by these P12 jobs.
- Historical P04–P09 successful CI remains scoped evidence only, not P12 final head approval.
- A genuine P12 PASS requires **all scoped tests executed green**, a real TalkBack/text-scale/device check, no P0 regressions, blocking accessibility issue count zero, and referenced CI run IDs. Merely authoring tests or building an APK does not satisfy it.

**Decision at initial authoring:** `P12 IMPLEMENTATION_IN_PROGRESS / VERIFICATION_PENDING — NOT FULL PASS`. Issue #5 OPEN, 13 integrated acceptance checkboxes unchanged, P13 NOT STARTED.
