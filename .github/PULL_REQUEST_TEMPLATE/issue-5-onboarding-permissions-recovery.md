# Issue #5 — Final Onboarding / Permissions / Recovery PR Template

> This is a FUTURE P14 implementation delivery checklist, NOT a current PR or accepted review. Proposed base `main` requires explicit Issue #5 approval despite CONTRIBUTING.md preferring `develop`. No Issue #4 review exception is inherited.
> [Issue #5](https://github.com/music-copilot-organization/music-copilot/issues/5) · [Traceability](https://github.com/music-copilot-organization/music-copilot/blob/feature/5-onboarding-permissions-recovery/docs/implementation/ISSUE_5_PHASE0_TRACEABILITY.json) · [Evidence](https://github.com/music-copilot-organization/music-copilot/blob/feature/5-onboarding-permissions-recovery/docs/implementation/ISSUE_5_EVIDENCE_INDEX.md).

**Issue reference:** Refs #5. Do NOT claim `Closes #5` until 13/13 actual acceptance results and governance are verified.

## Scope and proof

- [ ] Two-slide (maximum) first-use onboarding, Continue/Skip/Back and safe persisted/replay state.
- [ ] Correct media/mic JIT rationale and API-correct deferred notification prompt.
- [ ] OS-owned Granted/Denied/Permanently Denied and explicit Settings recovery.
- [ ] Scanning/Ready/Empty/revoked GS-01 truth; service-owned playback unaffected.
- [ ] No account, identity, remote STT activation, new backend/streaming or unapproved UX.

## 13 Acceptance Criteria — each needs actual code/test evidence

| AC | Acceptance focus | Actual evidence URL | Result |
|---|---|---|---|
| AC-01 | Maximum two value slides | TBD | NOT_RUN |
| AC-02 | Contextual media/mic permission explanation | TBD | NOT_RUN |
| AC-03 | Android-version-specific notification permission | TBD | NOT_RUN |
| AC-04 | Approved Granted/Denied/Permanently Denied wireframes | TBD | NOT_RUN |
| AC-05 | Explicit Android Settings and recoverable return | TBD | NOT_RUN |
| AC-06 | Crash-free denial/cancel/recreation | TBD | NOT_RUN |
| AC-07 | Scan and optional voice education permission gating | TBD | NOT_RUN |
| AC-08 | Cloud STT separate consent, processor/destination/retention if ever enabled | TBD | NOT_RUN |
| AC-09 | No signup/login/network identity | TBD | NOT_RUN |
| AC-10 | Config-change persistence per architecture | TBD | NOT_RUN |
| AC-11 | TalkBack, focus, scaling and contrast | TBD | NOT_RUN |
| AC-12 | Unit/UI/integration coverage | TBD | NOT_RUN |
| AC-13 | No sensitive permission/media telemetry | TBD | NOT_RUN |

## Release/merge gates

- [ ] All T01..T17 tests and explicit platform/API/device matrix have real results and sanitized UI evidence.
- [ ] JVM, Compose UI/integration, negative/privacy and architecture checks pass; exact PR-head Android CI green.
- [ ] TalkBack, 100/150/200% scaling, focus restoration and 48dp targets verified (no borrowed design PASS).
- [ ] Review includes PRD/MVP, UF/GS/WF, ADR/Issue #16 and #17 handoff traceability.
- [ ] Independent human review or separately authorized Issue #5-specific documented exception; no sole self-approval.
- [ ] Proposed `main` integration target explicitly reviewed vs stale `develop` (not automatically approved).
- [ ] Privacy, diagnostics, migration, data/consent retention, rollback and recovery risk reviewed.
- [ ] GitHub actual merge and Issue state separately verified; Project V2 Done never inferred.

**Tested SHA:** TBD · **CI run:** TBD · **Device evidence:** TBD · **Reviewer approval:** TBD · **Final disposition:** NOT_READY.
