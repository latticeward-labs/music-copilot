# Issue #5 — Evidence Index and Synthetic Test Contract

**Source:** [P00 canonical AC/test register](ISSUE_5_PHASE0_TRACEABILITY.json) · **Branch:** `feature/5-onboarding-permissions-recovery`.
**Status:** Phase-0 traceability fixture IDs remain specifications; actual P03 runtime/Phase-4 UI evidence is logged per phase below. The original integrated Issue #5 AC pass count is still 0/13; do not rewrite specification-only T01–T17 as fully passed.

Each future PASS must record tested commit SHA, run/time, actual test and CI path, expected/actual outcome and whether emulator, instrumented physical, or user-reported manual. Only synthetic media, IDs and speech may be used. Never upload real tracks, paths, titles, content URI, raw voice/transcripts, Bluetooth device IDs, permission outcomes or sensitive telemetry. Sanitize UI screenshots and logs.

| Test ID | Required synthetic seam / expected follow-up proof | Status |
|---|---|---|
| T01 | 2-slide fresh run/skip; fake first-install DataStore + Compose UI | NOT_RUN |
| T02 | DataStore write ACK/failure/cancel with fake repository | NOT_RUN |
| T03 | ActivityScenario recreation/restore, service playback authoritative | NOT_RUN |
| T04 | Non-mutating Settings education replay and queue/index unchanged | NOT_RUN |
| T05 | Media permission policy API29/32/33/35; native system request | NOT_RUN |
| T06 | JIT mic tap/practice; skip never prompts; provider separate | NOT_RUN |
| T07 | Notification prompt only when OS needs it after acknowledged playback | NOT_RUN |
| T08 | Granted media + synthetic MediaStore index: Ready or Empty | NOT_RUN |
| T09 | Requestable denial Retry/Not now without auto-loop | NOT_RUN |
| T10 | Permanent denial explicit Android Settings / Not now | NOT_RUN |
| T11 | Settings return recheck: Granted/Denied/Still Permanent | NOT_RUN |
| T12 | Cancel/Back/rotation/repeated callbacks crash-free | NOT_RUN |
| T13 | Revoked/moved/deleted synthetic audio; no false playability | NOT_RUN |
| T14 | Offline/failing speech provider not mistaken for mic denial | NOT_RUN |
| T15 | Remote STT stays OFF; provider-specific consent/Legal gate; no identity | NOT_RUN |
| T16 | TalkBack, focus order, 100/150/200% scaling, 48dp targets/contrast | NOT_RUN |
| T17 | Privacy telemetry negative fixtures with fake song/transcript/permission data | NOT_RUN |

## Evidence ownership

| Phases | Required artifact | Status |
|---|---|---|
| P00 | SHA-locked 12-PDF English source, 51-path/13-AC/17-case baseline, 4/4 DoR | PASS_SPEC_ONLY |
| P01 | Existing branch/ancestry, PR template, reviewer policy and machine CI contract; [initial real run #38819186656](https://github.com/music-copilot-organization/music-copilot/actions/runs/38819186656) | PASS_DOC_CONTRACT_ONLY |
| P02 | Pure first-use/bootstrap/permission state machine, DataStore checkpoint, 19 JVM tests and real Android Gradle [CI #38864555346](https://github.com/music-copilot-organization/music-copilot/actions/runs/38864555346) | **PASS CONTRACT/JVM ONLY** |
| P03 | Bootstrap, first launch route and real API29 first-run [CI #38869470585](https://github.com/music-copilot-organization/music-copilot/actions/runs/38869470585), [emulator #38869470447](https://github.com/music-copilot-organization/music-copilot/actions/runs/38869470447) | PASS_BOOTSTRAP_SCOPE |
| P04 | Approved WF-02/HF-02 two-page UI, [Kotlin/JVM/Lint/APK #38872429334](https://github.com/music-copilot-organization/music-copilot/actions/runs/38872429334), [API29 UI/200% emulator #38873215678](https://github.com/music-copilot-organization/music-copilot/actions/runs/38873215678), [screenshots+APK artifact #12034777234](https://github.com/music-copilot-organization/music-copilot/actions/runs/38873215678/artifacts/12034777234) | **PASS_TWO_SLIDES_SCOPE** |
| P05–P06 | Android-owned media request and Granted/Denied/Permanently Denied native flows | NOT_RUN |
| P07–P08 | Android Settings/revocation and Library readiness | NOT_RUN |
| P09–P10 | Six-command education/JIT microphone and Notification/Cloud STT consent | NOT_RUN |
| P11–P12 | Rotation/recreation, privacy-negative tests, Compose TalkBack/scaling/contrast | NOT_RUN |
| P13 | API29/32/33/35 emulator/physical/native Settings and permission QA; sanitized artifact identity | NOT_RUN |
| P14 | 13/13 actual AC, exact-head green CI, independent review, approved base exception, real PR merge/Issue closure | NOT_RUN |

## Gates never waived by P01

PH10-MIN-001 actual Compose accessibility QA; PH10-MIN-002 Legal/Privacy before any cloud STT; PH10-MIN-003 OEM/vehicle release QA (Issue #13); deferred Issue #17 human visual review. Issue #4 single-handset and expedited-review exception applies ONLY to #4. Design/static checks cannot stand in for Android permission/runtime proof.

**P01 files:** [Execution contract](ISSUE_5_PHASE1_GITHUB_EXECUTION.md) · [Machine validation](ISSUE_5_PHASE1_VALIDATION.json) · [P00–P14 plan](ISSUE_5_PHASED_EXECUTION_PLAN.md).


## Phase 0–4 owner-plan re-audit (2026-10-16)

[Audit and corrections](ISSUE_5_REAUDIT_P00_P04.md) · [Machine ledger](ISSUE_5_REAUDIT_P00_P04.json). Owner's uploaded 15-phase order controls. Original P00 traceability AC phase numbers were rebased (13/13 now have owner, truth, recovery and expected evidence). Native OS prompt before P05 was **not** added. P02 fakeable permission gateway and one-shot BlockedAction: [CI #38876126473](https://github.com/music-copilot-organization/music-copilot/actions/runs/38876126473) SUCCESS. P03 foreground unsafe auto-scan removed: [CI #38876126464](https://github.com/music-copilot-organization/music-copilot/actions/runs/38876126464) SUCCESS and [original API29 smoke #38876126480](https://github.com/music-copilot-organization/music-copilot/actions/runs/38876126480) SUCCESS. P04 Settings replay/ACK safety: [CI #38876334876](https://github.com/music-copilot-organization/music-copilot/actions/runs/38876334876) SUCCESS, [API29 original replay #38876334672](https://github.com/music-copilot-organization/music-copilot/actions/runs/38876334672) SUCCESS. Extended [P04 150%/slide2-restart emulator #38876467719](https://github.com/music-copilot-organization/music-copilot/actions/runs/38876467719) PENDING. **Issue-wide AC accepted remains 0/13** until final native and physical/review gates.
