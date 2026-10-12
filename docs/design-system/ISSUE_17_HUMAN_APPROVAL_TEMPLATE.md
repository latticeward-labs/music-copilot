# Issue #17 — real human approval evidence template

This is an **unsigned template**, not a review, approval, device test, Legal signoff or permission to merge. Independent accountable humans must submit GitHub reviews with real names/accounts, dates and links. Never fill placeholders with invented evidence. Actual GitHub review records take precedence over copied notes.

## Each human reviewer records

- **Responsibility:** Product / Design-UX / Android feasibility / QA / Accessibility / Privacy.
- **Verified GitHub review author profile:** supply the actual review-author URL from GitHub.
- **Submitted UTC date and time:** <actual timestamp>.
- **PR number and exact reviewed head SHA:** <actual PR and SHA>.
- **Link to submitted GitHub PR review:** <real review URL>.
- **GitHub review conclusion:** APPROVED / CHANGES_REQUESTED / COMMENTED. A comment is not an approval.
- **Reviewed source files and stable IDs:** <HF/WF/State/UF/GS path and IDs>.
- **What was actually reviewed:** 10 primary, 45 dedicated app-visible and 27 specialist Driving/Voice images where assigned; verify legible visible controls (including Player Next and Driving), source image-to-spec agreement, real hotspot coordinates, visible Exit, one-layer Driving layout and approved recovery transitions. Record which were not tested.
- **Accessibility/Privacy:** inspect 200% browser zoom, non-color status, control labels/reading order, system-owned boundaries and privacy-safe telemetry as applicable; explicitly distinguish visual/design specification from actual device/Compose/Legal testing.
- **Findings:** issue ID, severity, affected file/State, evidence, owner, resolution PR/commit and re-review URL, or confirmed no findings in reviewed scope.
- **Evidence limits and signed review decision:** <specific actual reviewed evidence; approval if warranted>.

## Accountable final closeout review

Before final PR merge, verify that named independent Product, Design/UX, Android and QA reviewers have genuinely **approved** and that Accessibility/Privacy review evidence is recorded. Resolve all newly reported Blockers/Majors; provide proof of the final head CI, prototype/asset/link revalidation, and update AC-12 in the Issue #17 acceptance audit **only after** the approval evidence exists. Do not mark human approval, issue closure, project Done or downstream design dependency cleared from this unsigned template.

Existing AI-assisted six-role review: [Phase 11 evidence](review/ISSUE_17_FINDINGS_REGISTER.json); historic human approvals recorded there = 0. Phase-12 [Final Acceptance Audit](ISSUE_17_FINAL_ACCEPTANCE_AUDIT.md) remains pending human.

When real review is ready, maintainers must submit actual GitHub review actions; merely changing this Markdown file or editing a JSON flag is not sufficient evidence.
