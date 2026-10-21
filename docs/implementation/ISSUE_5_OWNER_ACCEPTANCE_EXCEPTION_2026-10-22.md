# Issue #5 — Owner-authorized acceptance exception and deferred verification (22 October 2026)

**Source of authorization:** the project owner explicitly instructed in the current project conversation:

> «تست‌های فنی نهایی و Review مستقل را فعلا نگه دار اینو در آینده بررسی خواهد شد. الان Merge و بستن Issue را انجام بده و issue 5 رو پاس کن»

This is an affirmative instruction to **merge and close Issue #5 now, accepting the existing owner-reported physical-device test results while deferring technical and independent-review gates to a future work item**. This document records **an Issue-#5-specific exception**, not a retrospective assertion that those gates were successfully executed. The prior exact-head and review PASS conditions from P14 remain part of the historical contract; this addendum supersedes their timing as a prerequisite to owner acceptance for this Issue only.

## Ownership and disposition

- **Authorized delivery scope:** implementation of two-slide onboarding, local-media permission explanation/grant/denial/settings recovery, truthful library readiness handoff, optional six-command Voice education and JIT microphone permission, Cloud STT disabled and MediaSession-managed notifications.
- **Owner phone QA:** P10 7/7, P11 11/11, P12 12/12, P13 17/17 reported PASS. These are owner-provided manual reports only. The P13 second handset's model/OS and GS-01 primary positive indexed item count are not independently supplied.
- **Verified historical CI:** prior P02..09 phase-scoped runs (including P09 real Android 13); [P12 APK-only #39243014999](https://github.com/latticeward-labs/music-copilot/actions/runs/39243014999) SUCCESS for APK build only.
- **Deferred — NOT VERIFIED:** final-head JVM/lint/static/privacy/Compose UI/native matrix, independent reviewer approval, complete two-device identification/OS evidence, primary nonempty GS01 READY proof, exact production-grade accessibility measurements and full cross-phase regression.
- **User's instruction to defer** means **DO NOT trigger tests/Actions or invent review signoff** as part of this requested merge.
- **Release state after successful GitHub merge/closure:** `PASS_OWNER_ACCEPTED_WITH_DEFERRED_VERIFICATION`, as distinguished from `PASS_FULL_TECHNICAL_QA`. Owner acceptance is for Issue #5 product-delivery scope; not a declaration of release-ready public beta QA. Closed does not absolve later defects.
- **Future tracker (remains open):** [Issue #73 — Post-merge QA debt: Issue #5 deferred technical tests, independent review and GS-01 proof](https://github.com/latticeward-labs/music-copilot/issues/73). This issue owns all residual technical/review and true Ready verification, and will be triaged before claiming corresponding validated release gates.
- **Delivery vehicle:** [PR #72](https://github.com/latticeward-labs/music-copilot/pull/72); owner explicitly authorized Merge and close. `main` merge SHA and Issue close outcome must be populated **only after actual GitHub confirmation**.
- **Downstream dependency:** this owner-approved merge unblocks Issue #6 for *integration with the onboarding / permissions / local-readiness contract only*; it does NOT certify full Songs/Artists/Albums/Folders/Search, playback or real Voice execution, and does not erase follow-up #73.

## Original 13 ACs — owner acceptance policy, not test fabrication

All 13 remain individually mapped in [P14 final acceptance audit](ISSUE_5_FINAL_ACCEPTANCE_AUDIT.md). Under the owner's explicit **scope/timing amendment**, their product-delivery interpretation is approved for closeout **with deferred independent and technical verification**, not 13 independently green evidence gates. If the Issue checklist is marked complete, its checkbox signifies this OWNER acceptance **under this addendum**; an unchecked original historical state may be retained elsewhere for traceability. AC-08 is conditional: Cloud STT has no approved remote provider and is deliberately disabled/fail-closed. AC-12's unit/UI/integration **code coverage exists as authored tests and scoped historical jobs, but final-head executions remain not run** and must not be described as confirmed PASS. The same distinction holds for privacy/contrast verification.

## Safety and compliance

No account/backend/network identity or remote STT implementation claimed. The manifest currently has no INTERNET or POST_NOTIFICATIONS request; local permission and voice education gates in source were inspected previously, but this is not a security certification. Do not upload screenshots or personal-media metadata, do not trigger GitHub Actions in response to this owner closure request, and never backfill an invented review identity/signature, CI green run ID, physical phone model or GS01 indexed audio count.

**Owner-approved action:** Merge PR #72 and close Issue #5 as Completed with explicit exception; keep Issue #73 open. This document authorizes no undisclosed changes outside the existing Issue #5 scope.
