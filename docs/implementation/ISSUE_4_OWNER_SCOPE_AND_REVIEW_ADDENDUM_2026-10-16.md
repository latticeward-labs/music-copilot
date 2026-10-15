# Issue #4 — Owner-directed foundation acceptance and delivery-policy addendum

**Decision date:** 16 October 2026  
**Scope:** Issue #4 Android 10+ *foundation only*, PR #71. Does **not** rewrite ADRs, PRD/MVP, original QA history or issue #13 OEM/vehicle certification.  
**Authority of decision:** the project owner/requester explicitly directed in this ChatGPT conversation: *one real handset test is sufficient for now; make Issue #4's 14 acceptance criteria pass, merge the PR and close the Issue*. This is a **user-directed scope/merge decision**, **not a GitHub human approval or independent QA sign-off**. Preserve the original two-phone gate in the pre-addendum git history, and display this addendum wherever the delivery status changes.

## Replaced acceptance criterion AC-11 — effective for Issue #4 only

**Original wording (superseded only for this foundation release):** `App launches on two representative devices.` The earlier Phase 13 plan also demanded two physically distinct phones with ADB-validated installed APK digest, manufacturer/API, UTC time, foreground, explicit force-stop and sanitized logs.

**Replacement agreed by the requester:** `The root Issue #4 foundation Debug APK visibly launches on at least one reported physical Android 10+ phone and returns from background or a deliberate app close; record the tester's model/version, a screenshot or comparable visible proof and explicitly characterize any uninstrumented observations. Two-device and ADB/OEM instrumentation are deferred to full field QA, not misreported as executed.`

**Evidence:** [HONOR X7d Android 15 manual smoke](ISSUE_4_HONOR_X7D_ANDROID15_MANUAL_SMOKE.md). The tester provided a screenshot of the app's root Bootstrap UI and reported successful repeated Home→foreground switching and a close/relaunch from Recents; the tester stated that no permission dialog appeared. User reported phone model and Android version. Actual OS version via `getprop`, exact installed APK SHA, five-minute stopwatch, raw crash buffer and ADB session were not measured. Screenshot SHA-256: `cb56e4825f7e78151c3bbec7b0392eb31d8a0b9773460b1c418b2c2e93c5e094`. **One user-reported physical handset, zero independently ADB-attested handsets.**

Independent clean-room GitHub Actions ran two **virtual** devices API29 and API35 (94 passing JVM tests each, both APKs, cold/resume/relaunch/synthetic logs): [run #38559652982](https://github.com/music-copilot-organization/music-copilot/actions/runs/38559652982). This proves technical Android 10–15 smoke in emulators, **not physical OEM breadth**.

**Decision:** revised foundation-specific AC-11 **PASS under the new user-authorized manual criterion**, but original two-device/hardware-ADB requirements remain **NOT EXECUTED / deferred to Issue #13 full OEM-device QA**. This cannot be used to claim full phone/BT/vehicle/permission/recovery production certification.

## Delivery review exception — explicit owner-directed accelerated acceptance

Existing [`CONTRIBUTING.md`](../../CONTRIBUTING.md) says *do not self-approve as the only reviewer*, *integrate into develop*, and *merged work moves to Testing, not automatically Done*. PR #71 has **no independent human reviews submitted** and does **not** have an independent Product/Android/QA/Accessibility/Privacy approval. **No such approval is claimed.**

The same requester expressly directed proceeding with this foundation merge and closing Issue #4 now on the narrower single-phone criteria. For this **one Issue #4 delivery only**, this addendum records an **owner-directed accelerated delivery exception** in place of the usual independent review hold and the repository's generic `develop` preference. The specific scope is this foundation merge to existing `main`, with all actual GitHub CI checks green and no technical Blocker/Major found by the documented machine/AI audit; remaining outside reviewers remain **not performed**, not magically checked as independent sign-offs. The exception does not change `CONTRIBUTING.md` for future PRs.

**Disclosure:** accepting the owner-directed accelerated route means independent peer review and hardware breadth have **not** occurred. This is narrower release confidence than the original procedure, and should not be confused with ready-to-ship customer software. Full QA and independent review should occur before wider external distribution; the already planned separate #13 test matrix remains open.

## Cross-discipline review record (AI-assisted, not external human approval)

| Role | Inspected evidence and conclusion | Independent human sign-off |
|---|---|---|
| Product/scope | Approved English PRD/MVP; excludes accounts, backend, streaming and full feature UI; root app scope and deferred work kept in #5–#13 | **None — owner fast-track exception** |
| Android architecture | Single production `:app`, version catalog/minSdk29, service-owned Player and controller port, Voice Quality replacement seams, package direction + negative fixtures | **None — owner fast-track exception** |
| QA/integration | Fresh root CI/94 JVM; two emulator APK installs API29/API35; one manually reported HONOR Android 15 Bootstrap; hardware breadth still deferred | **None — owner fast-track exception** |
| Accessibility | Bootstrap text visible, no full 200%/TalkBack feature-flow claim; downstream screen test obligations unchanged | **None — owner fast-track exception** |
| Privacy/security | Nonproduction diagnostics disabled, no cloud/Internet declaration, permissions not requested at startup, synthetic CI media, no archived raw voice/personal song data; further field privacy QA deferred | **None — owner fast-track exception** |

**CI gate:** [#38809650612](https://github.com/music-copilot-organization/music-copilot/actions/runs/38809650612) Android foundation, [#38809643136](https://github.com/music-copilot-organization/music-copilot/actions/runs/38809643136) Android foundation and [#38809650690](https://github.com/music-copilot-organization/music-copilot/actions/runs/38809650690) architecture/design all completed success on code/document head `4f51baf4d57a9dd699f70de4f0e19dbb967335ba`. All **later** documentation commits or PR updates must be rechecked at actual final HEAD. Zero branch commits behind `main` when checked 16 October 2026.

**Merge authorization:** owner explicitly requested merge and Issue close. Never invent a GitHub Review event or human approver identity. Use GitHub's actual merge result SHA and state before claiming success. Verify post-merge main and update Issue #4 and Project status **where an authorized interface is actually available**; if Project board cannot be edited, record **NOT VERIFIED** rather than Done.

## Final state accounting

- Fourteen **original** criteria were reviewed. Thirteen were supported as originally written. The 14th, `two representative devices`, was **NOT PASS under original wording**.
- Fourteen **effective Issue #4 foundation criteria** under this addendum: 13 unchanged plus one explicitly revised AC-11; all fourteen may be marked **PASS only under this documented acceptance contract**.
- Two physical-device coverage, instrumented physical test proof and independent human PR review remain **unperformed**. These are **deferred/not waived as having happened**.
- Issue #5 is **untouched**; only Issue #4 docs/PR/state and the required final merge are in scope.
