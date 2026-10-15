# Issue #5 — Evidence Index and Synthetic Test Contract

**Source:** [P00 canonical AC/test register](ISSUE_5_PHASE0_TRACEABILITY.json) · **Branch:** `feature/5-onboarding-permissions-recovery`.
**Status:** Phase-1 specification only. All Issue #5 runtime tests are NOT_RUN, and AC pass count is 0/13.

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
| P01 | Existing branch/ancestry, PR template, reviewer policy, machine CI gate and GitHub readback | PENDING_CI |
| P02–P04 | DataStore/application truth, lifecycle, UI navigation and API-aware media/mic policies | NOT_RUN |
| P05–P08 | Denial, Settings recheck, JIT mic and deferred notification recovery | NOT_RUN |
| P09–P10 | Cloud consent disabled, no account/identity, real local-library/GS-01 integration | NOT_RUN |
| P11–P12 | Rotation/recreation, privacy-negative tests, Compose TalkBack/scaling/contrast | NOT_RUN |
| P13 | API29/32/33/35 emulator/physical/native Settings and permission QA; sanitized artifact identity | NOT_RUN |
| P14 | 13/13 actual AC, exact-head green CI, independent review, approved base exception, real PR merge/Issue closure | NOT_RUN |

## Gates never waived by P01

PH10-MIN-001 actual Compose accessibility QA; PH10-MIN-002 Legal/Privacy before any cloud STT; PH10-MIN-003 OEM/vehicle release QA (Issue #13); deferred Issue #17 human visual review. Issue #4 single-handset and expedited-review exception applies ONLY to #4. Design/static checks cannot stand in for Android permission/runtime proof.

**P01 files:** [Execution contract](ISSUE_5_PHASE1_GITHUB_EXECUTION.md) · [Machine validation](ISSUE_5_PHASE1_VALIDATION.json) · [P00–P14 plan](ISSUE_5_PHASED_EXECUTION_PLAN.md).
