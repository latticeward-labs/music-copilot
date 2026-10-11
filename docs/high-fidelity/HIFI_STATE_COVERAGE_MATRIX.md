# Issue #17 — HIFI_STATE_COVERAGE_MATRIX (Phase 5)

**Date:** 2026-10-11 · **Disposition:** AUTOMATED COVERAGE PASS / HUMAN SIGNOFF NOT YET CLAIMED
**Source archive SHA-256:** 0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8
**Visual authority:** 32 approved Issue #15 app frames + 10 existing Phase-4 primary images, Night Signal / Compose-oriented components.

## Verification summary

| Gate | Expected | Verified | Verdict |
|---|---:|---:|---|
| Canonical State IDs | 126 | 126 | PASS |
| App-visible image + exact-copy spec | 45 | 45 | PASS |
| Variant/component/annotation specs | 61 | 61 | PASS |
| System-only transition handoffs | 20 | 20 | PASS |
| Approved source app-frame variants rendered | 32 | 32 | PASS |
| New app UI for Android/System-only | 0 | 0 | PASS |
| Missing / duplicate / unassigned State ID | 0 / 0 / 0 | 0 / 0 / 0 | PASS |

**Definition:** State ID ≠ navigation destination. Different states may use the same approved WF frame with distinct bounded behavior; that reuse is explicitly linked in each spec. Source-frame visual coverage (32/32) exists in addition to the 45 per-ID review PNGs.

## Explicit 126-state disposition and linked evidence

| # | State | Name | Priority | WF | UF / GS | Disposition | Visual/spec/handoff |
|---:|---|---|---|---|---|---|---|
| 1 | S-01 | Splash - Loading, Migration Error | P0 | WF-01 | UF-01 / GS-01 | DEDICATED_HIFI | [PNG](WF-01/states/HF_STATE_S-01.png) · [Spec](WF-01/states/HF_STATE_S-01.md) |
| 2 | S-02 | Onboarding - First Run, Incomplete Resume, Skip, Replay | P0 | WF-02 | UF-01 / GS-01 | DEDICATED_HIFI | [PNG](WF-02/states/HF_STATE_S-02.png) · [Spec](WF-02/states/HF_STATE_S-02.md) |
| 3 | S-03 | Permissions - Granted, Denied-requestable, Permanently Denied | P0 | WF-03 | UF-02 / GS-01 | DEDICATED_HIFI | [PNG](WF-03/states/HF_STATE_S-03.png) · [Spec](WF-03/states/HF_STATE_S-03.md) |
| 4 | S-04 | Home / Library - Scanning, Empty, Ready, Recoverable Failure, Revoked-Reconciled | P0 | WF-04 | UF-03 / GS-01 | DEDICATED_HIFI | [PNG](WF-04/states/HF_STATE_S-04.png) · [Spec](WF-04/states/HF_STATE_S-04.md) |
| 5 | CA-01 | Six-Command Education - View, Skip, Replay, Practice Choice | P0 | WF-02 | UF-01 / GS-01 | DEDICATED_HIFI | [PNG](WF-02/states/HF_STATE_CA-01.png) · [Spec](WF-02/states/HF_STATE_CA-01.md) |
| 6 | CA-02 | Voice Access Boundary - Mic Purpose, Granted, Denied-requestable, Permanently Denied, Provider Unavailable, Cloud Consent | P0 | WF-03 | UF-02 / GS-01 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CA-02.md) · [Anchor PNG](WF-03/frames/HF_FRAME_WF-03_PERMISSIONS_RATIONALE_REQUEST.png) |
| 7 | SYS-01 | Android media permission dialog | P0 | WF-03 | UF-02 / GS-01 | SYSTEM_ONLY_HANDOFF | [System handoff](system-handoffs/SYS-01.md) |
| 8 | SYS-02 | Android microphone permission dialog | P0 | WF-03 | UF-02 / — (not on GS-01..05 path) | SYSTEM_ONLY_HANDOFF | [System handoff](system-handoffs/SYS-02.md) |
| 9 | SYS-03 | Notification permission boundary | P0 | WF-06 | UF-06 / — (not on GS-01..05 path) | SYSTEM_ONLY_HANDOFF | [System handoff](system-handoffs/SYS-03.md) |
| 10 | SYS-04 | Android Settings | P0 | WF-03 | UF-02 / GS-01 | SYSTEM_ONLY_HANDOFF | [System handoff](system-handoffs/SYS-04.md) |
| 11 | SYS-05 | Speech provider/cloud-processing boundary | P0 | WF-03 | UF-02 / UF-10 / UF-12 / GS-01 | SYSTEM_ONLY_HANDOFF | [System handoff](system-handoffs/SYS-05.md) |
| 12 | CB-01 | Home / Library Ready | P0 | WF-04 | UF-04 / GS-01, GS-02 | DEDICATED_HIFI | [PNG](WF-04/states/HF_STATE_CB-01.png) · [Spec](WF-04/states/HF_STATE_CB-01.md) |
| 13 | CB-02 | Songs | P0 | WF-04 | UF-04 / GS-02 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CB-02.md) · [Anchor PNG](WF-04/frames/HF_FRAME_WF-04_HOME_LIBRARY_SCANNING.png) |
| 14 | CB-03 | Artists | P0 | WF-04 | UF-04 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CB-03.md) · [Anchor PNG](WF-04/frames/HF_FRAME_WF-04_HOME_LIBRARY_SCANNING.png) |
| 15 | CB-04 | Artist tracks | P0 | WF-04 | UF-04 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CB-04.md) · [Anchor PNG](WF-04/frames/HF_FRAME_WF-04_HOME_LIBRARY_SCANNING.png) |
| 16 | CB-05 | Albums | P0 | WF-04 | UF-04 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CB-05.md) · [Anchor PNG](WF-04/frames/HF_FRAME_WF-04_HOME_LIBRARY_SCANNING.png) |
| 17 | CB-06 | Album tracks | P0 | WF-04 | UF-04 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CB-06.md) · [Anchor PNG](WF-04/frames/HF_FRAME_WF-04_HOME_LIBRARY_SCANNING.png) |
| 18 | CB-07 | Folders | P0 | WF-04 | UF-04 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CB-07.md) · [Anchor PNG](WF-04/frames/HF_FRAME_WF-04_HOME_LIBRARY_SCANNING.png) |
| 19 | CB-08 | Folder tracks | P0 | WF-04 | UF-04 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CB-08.md) · [Anchor PNG](WF-04/frames/HF_FRAME_WF-04_HOME_LIBRARY_SCANNING.png) |
| 20 | CB-09 | Search Idle / Query | P0 | WF-05 | UF-04 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-05/states/HF_STATE_CB-09.png) · [Spec](WF-05/states/HF_STATE_CB-09.md) |
| 21 | CB-10 | Search Result | P0 | WF-05 | UF-04 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-05/states/HF_STATE_CB-10.png) · [Spec](WF-05/states/HF_STATE_CB-10.md) |
| 22 | CB-11 | Search No Result | P0 | WF-05 | UF-04 / GS-02 | DEDICATED_HIFI | [PNG](WF-05/states/HF_STATE_CB-11.png) · [Spec](WF-05/states/HF_STATE_CB-11.md) |
| 23 | CB-12 | Search Error | P0 | WF-05 | UF-04 / GS-02 | DEDICATED_HIFI | [PNG](WF-05/states/HF_STATE_CB-12.png) · [Spec](WF-05/states/HF_STATE_CB-12.md) |
| 24 | CB-13 | Selection Validation | P0 | WF-04, WF-05 | UF-04 / GS-02 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CB-13.md) · [Anchor PNG](WF-04/frames/HF_FRAME_WF-04_HOME_LIBRARY_SCANNING.png) |
| 25 | CB-14 | Playback Start / Queue Acceptance / Preparing | P0 | WF-06 | UF-04 → UF-05 / GS-02 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CB-14.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 26 | CB-15 | Full Player Playing | P0 | WF-06 | UF-05 / GS-02, GS-03 | DEDICATED_HIFI | [PNG](WF-06/states/HF_STATE_CB-15.png) · [Spec](WF-06/states/HF_STATE_CB-15.md) |
| 27 | CB-16 | Full Player Paused | P0 | WF-06 | UF-05 / GS-03 | DEDICATED_HIFI | [PNG](WF-06/states/HF_STATE_CB-16.png) · [Spec](WF-06/states/HF_STATE_CB-16.md) |
| 28 | CB-17 | End of Queue / Paused-Completed | P0 | WF-06 | UF-05 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-06/states/HF_STATE_CB-17.png) · [Spec](WF-06/states/HF_STATE_CB-17.md) |
| 29 | CB-18 | Queue View | P0 | WF-06 | UF-05 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-06/states/HF_STATE_CB-18.png) · [Spec](WF-06/states/HF_STATE_CB-18.md) |
| 30 | CB-19 | Corrupt / Inaccessible Item Recovery | P0 | WF-06 | UF-05 / GS-02 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CB-19.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 31 | CB-20 | Home Mini-player | P0 | WF-04, WF-06 | UF-05 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CB-20.md) · [Anchor PNG](WF-04/frames/HF_FRAME_WF-04_HOME_LIBRARY_SCANNING.png) |
| 32 | CB-21 | Controller Connecting / Reconnecting | P0 | WF-06 | UF-05 / UF-06 / GS-02, GS-03 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CB-21.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 33 | CB-22 | Active Session Handoff | P0 | WF-06 | UF-06 / GS-03 | SYSTEM_ONLY_HANDOFF | [System handoff](system-handoffs/CB-22.md) |
| 34 | CB-23 | App Background / UI Absent | P0 | WF-06 | UF-06 / GS-03 | SYSTEM_ONLY_HANDOFF | [System handoff](system-handoffs/CB-23.md) |
| 35 | CB-24 | Screen Off / Device Locked | P0 | WF-06 | UF-06 / GS-03 | SYSTEM_ONLY_HANDOFF | [System handoff](system-handoffs/CB-24.md) |
| 36 | CB-25 | Task / OEM Lifecycle Event | P0 | WF-06 | UF-06 / GS-03 | SYSTEM_ONLY_HANDOFF | [System handoff](system-handoffs/CB-25.md) |
| 37 | CB-26 | Media Notification / System Controls | P0 | WF-06 | UF-06 / GS-03 | SYSTEM_ONLY_HANDOFF | [System handoff](system-handoffs/CB-26.md) |
| 38 | CB-27 | Lock-screen Controls | P0 | WF-06 | UF-06 / — (not on GS-01..05 path) | SYSTEM_ONLY_HANDOFF | [System handoff](system-handoffs/CB-27.md) |
| 39 | CB-28 | Bluetooth / Headset / Vehicle Media Keys | P0 | WF-06 | UF-06 / — (not on GS-01..05 path) | SYSTEM_ONLY_HANDOFF | [System handoff](system-handoffs/CB-28.md) |
| 40 | CB-29 | System Command Pending | P0 | WF-06 | UF-06 / GS-03 | SYSTEM_ONLY_HANDOFF | [System handoff](system-handoffs/CB-29.md) |
| 41 | CB-30 | Notification Permission / Visibility Evaluation | P0 | WF-06 | UF-06 / GS-03 | SYSTEM_ONLY_HANDOFF | [System handoff](system-handoffs/CB-30.md) |
| 42 | CB-31 | System Surface Unavailable | P0 | WF-06 | UF-06 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CB-31.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 43 | CB-32 | P1 Paused Restore | P1 | WF-06 | UF-05 / UF-06 / GS-03 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CB-32.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 44 | CB-33 | Restore Unavailable | P1 | WF-06 | UF-05 / UF-06 / GS-03 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CB-33.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 45 | CC-01 | Non-driving source | P0 | WF-07 | UF-07 / GS-04 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CC-01.md) · [Anchor PNG](WF-07/frames/HF_FRAME_WF-07_DRIVING_MODE_PLAYING.png) |
| 46 | CC-02 | Manual entry pending | P0 | WF-07 | UF-07 / GS-04 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CC-02.md) · [Anchor PNG](WF-07/frames/HF_FRAME_WF-07_DRIVING_MODE_PLAYING.png) |
| 47 | CC-03 | Controller reconnecting | P0 | WF-07 | UF-07 / GS-04 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CC-03.md) · [Anchor PNG](WF-07/frames/HF_FRAME_WF-07_DRIVING_MODE_PLAYING.png) |
| 48 | CC-04 | Entry cancelled/failed | P0 | WF-07 | UF-07 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CC-04.md) · [Anchor PNG](WF-07/frames/HF_FRAME_WF-07_DRIVING_MODE_PLAYING.png) |
| 49 | CC-05 | Driving Playing | P0 | WF-07 | UF-07 / GS-04 | DEDICATED_HIFI | [PNG](WF-07/states/HF_STATE_CC-05.png) · [Spec](WF-07/states/HF_STATE_CC-05.md) |
| 50 | CC-06 | Driving Paused/Ended | P0 | WF-07 | UF-07 / GS-04 | DEDICATED_HIFI | [PNG](WF-07/states/HF_STATE_CC-06.png) · [Spec](WF-07/states/HF_STATE_CC-06.md) |
| 51 | CC-07 | Driving No Track | P0 | WF-07 | UF-07 / GS-04 | DEDICATED_HIFI | [PNG](WF-07/states/HF_STATE_CC-07.png) · [Spec](WF-07/states/HF_STATE_CC-07.md) |
| 52 | CC-08 | Playback command pending | P0 | WF-07 | UF-07 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CC-08.md) · [Anchor PNG](WF-07/frames/HF_FRAME_WF-07_DRIVING_MODE_PLAYING.png) |
| 53 | CC-09 | Command rejected/timeout | P0 | WF-07 | UF-07 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CC-09.md) · [Anchor PNG](WF-07/frames/HF_FRAME_WF-07_DRIVING_MODE_PLAYING.png) |
| 54 | CC-10 | Acknowledged refresh | P0 | WF-07 | UF-07 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CC-10.md) · [Anchor PNG](WF-07/frames/HF_FRAME_WF-07_DRIVING_MODE_PLAYING.png) |
| 55 | CC-11 | P1 selected-device connected | P1 | WF-07 | UF-07 / UF-12 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-07/states/HF_STATE_CC-11.png) · [Spec](WF-07/states/HF_STATE_CC-11.md) |
| 56 | CC-12 | Bluetooth suggestion accepted | P1 | WF-07 | UF-07 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CC-12.md) · [Anchor PNG](WF-07/frames/HF_FRAME_WF-07_DRIVING_MODE_PLAYING.png) |
| 57 | CC-13 | Exit/back return | P0 | WF-07 | UF-07 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CC-13.md) · [Anchor PNG](WF-07/frames/HF_FRAME_WF-07_DRIVING_MODE_PLAYING.png) |
| 58 | CC-14 | Microphone handoff | P0 | WF-08 | UF-07 → UF-08 / GS-04 | DEDICATED_HIFI | [PNG](WF-08/states/HF_STATE_CC-14.png) · [Spec](WF-08/states/HF_STATE_CC-14.md) |
| 59 | CC-15 | Item unavailable / access failure | P0 | WF-07 | UF-07 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CC-15.md) · [Anchor PNG](WF-07/frames/HF_FRAME_WF-07_DRIVING_MODE_PLAYING.png) |
| 60 | CC-16 | Interruption/route handoff | P0 | WF-07 | UF-07 → UF-11 / GS-04, GS-05 | SYSTEM_ONLY_HANDOFF | [System handoff](system-handoffs/CC-16.md) |
| 61 | CC-17 | Suggestion declined/dismissed | P1 | WF-07 | UF-07 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CC-17.md) · [Anchor PNG](WF-07/frames/HF_FRAME_WF-07_DRIVING_MODE_PLAYING.png) |
| 62 | CC-18 | Analytics unavailable | P0 | WF-07 | UF-07 / GS-04 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CC-18.md) · [Anchor PNG](WF-07/frames/HF_FRAME_WF-07_DRIVING_MODE_PLAYING.png) |
| 63 | CD-01 | Idle/manual surface | P0 | WF-07, WF-06 | UF-08 / UF-10 / GS-04 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CD-01.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 64 | CD-02 | Capability check | P0 | WF-08 | UF-08 / UF-10 / GS-04 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CD-02.md) · [Anchor PNG](WF-08/frames/HF_FRAME_WF-08_VOICE_LISTENING.png) |
| 65 | CD-03 | Listening attempt 1 | P0 | WF-08 | UF-08 / GS-04 | DEDICATED_HIFI | [PNG](WF-08/states/HF_STATE_CD-03.png) · [Spec](WF-08/states/HF_STATE_CD-03.md) |
| 66 | CD-04 | Listening attempt 2 | P0 | WF-08 | UF-10 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-08/states/HF_STATE_CD-04.png) · [Spec](WF-08/states/HF_STATE_CD-04.md) |
| 67 | CD-05 | Processing | P0 | WF-08 | UF-08 / GS-04 | DEDICATED_HIFI | [PNG](WF-08/states/HF_STATE_CD-05.png) · [Spec](WF-08/states/HF_STATE_CD-05.md) |
| 68 | CD-06 | Direct command pending | P0 | WF-08 | UF-08 / GS-04 | DEDICATED_HIFI | [PNG](WF-08/states/HF_STATE_CD-06.png) · [Spec](WF-08/states/HF_STATE_CD-06.md) |
| 69 | CD-07 | Executed | P0 | WF-08 | UF-08 / GS-04 | DEDICATED_HIFI | [PNG](WF-08/states/HF_STATE_CD-07.png) · [Spec](WF-08/states/HF_STATE_CD-07.md) |
| 70 | CD-08 | Execution failed-safe | P0 | WF-08 | UF-08 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-08/states/HF_STATE_CD-08.png) · [Spec](WF-08/states/HF_STATE_CD-08.md) |
| 71 | CD-09 | Confirming one candidate | P0 | WF-08 | UF-09 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-08/states/HF_STATE_CD-09.png) · [Spec](WF-08/states/HF_STATE_CD-09.md) |
| 72 | CD-10 | Confirming two artists | P0 | WF-08 | UF-09 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-08/states/HF_STATE_CD-10.png) · [Spec](WF-08/states/HF_STATE_CD-10.md) |
| 73 | CD-11 | Confirmed command pending | P0 | WF-08 | UF-09 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-08/states/HF_STATE_CD-11.png) · [Spec](WF-08/states/HF_STATE_CD-11.md) |
| 74 | CD-12 | Confirmation failed-safe | P0 | WF-08 | UF-09 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-08/states/HF_STATE_CD-12.png) · [Spec](WF-08/states/HF_STATE_CD-12.md) |
| 75 | CD-13 | Artist not found | P0 | WF-08 | UF-09 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-08/states/HF_STATE_CD-13.png) · [Spec](WF-08/states/HF_STATE_CD-13.md) |
| 76 | CD-14 | Unknown phrase | P0 | WF-08 | UF-09 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-08/states/HF_STATE_CD-14.png) · [Spec](WF-08/states/HF_STATE_CD-14.md) |
| 77 | CD-15 | Unsupported request | P0 | WF-08 | UF-09 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-08/states/HF_STATE_CD-15.png) · [Spec](WF-08/states/HF_STATE_CD-15.md) |
| 78 | CD-16 | Missing artist | P0 | WF-08 | UF-09 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-08/states/HF_STATE_CD-16.png) · [Spec](WF-08/states/HF_STATE_CD-16.md) |
| 79 | CD-17 | Attempt-one failure | P0 | WF-08 | UF-10 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-08/states/HF_STATE_CD-17.png) · [Spec](WF-08/states/HF_STATE_CD-17.md) |
| 80 | CD-18 | Retry available | P0 | WF-08 | UF-10 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-08/states/HF_STATE_CD-18.png) · [Spec](WF-08/states/HF_STATE_CD-18.md) |
| 81 | CD-19 | Attempt-two terminal failure | P0 | WF-08 | UF-10 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-08/states/HF_STATE_CD-19.png) · [Spec](WF-08/states/HF_STATE_CD-19.md) |
| 82 | CD-20 | Manual fallback | P0 | WF-08 | UF-08 / UF-09 / UF-10 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CD-20.md) · [Anchor PNG](WF-08/frames/HF_FRAME_WF-08_VOICE_LISTENING.png) |
| 83 | CD-21 | Permission denied/revoked | P0 | WF-03, WF-08 | UF-10 / UF-12 / GS-04 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CD-21.md) · [Anchor PNG](WF-03/frames/HF_FRAME_WF-03_PERMISSIONS_RATIONALE_REQUEST.png) |
| 84 | CD-22 | Provider/language unavailable | P0 | WF-08, WF-10 | UF-10 / UF-12 / GS-04 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CD-22.md) · [Anchor PNG](WF-08/frames/HF_FRAME_WF-08_VOICE_LISTENING.png) |
| 85 | CD-23 | Cloud consent required/declined | P0 | WF-10 | UF-10 / UF-12 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-10/states/HF_STATE_CD-23.png) · [Spec](WF-10/states/HF_STATE_CD-23.md) |
| 86 | CD-24 | Cancelled/Exit | P0 | WF-08 | UF-08 / UF-09 / UF-10 / GS-04 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CD-24.md) · [Anchor PNG](WF-08/frames/HF_FRAME_WF-08_VOICE_LISTENING.png) |
| 87 | CE-01 | Stable Playing | P0 | WF-06, WF-07 | UF-11 / GS-05 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CE-01.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 88 | CE-02 | Transient focus/call handling | P0 | WF-06, WF-07 | UF-11 / GS-05 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CE-02.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 89 | CE-03 | Interrupted Paused / resume eligible | P0 | WF-06, WF-07 | UF-11 / GS-05 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CE-03.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 90 | CE-04 | Temporarily Ducked | P0 | WF-06, WF-07 | UF-11 / GS-05 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CE-04.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 91 | CE-05 | Conditional Resume acknowledged | P0 | WF-06, WF-07 | UF-11 / GS-05 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CE-05.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 92 | CE-06 | Safe Paused / manual resume | P0 | WF-06, WF-07 | UF-11 / GS-05 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CE-06.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 93 | CE-07 | Route Disconnect handling | P0 | WF-06, WF-07 | UF-11 / GS-05 | SYSTEM_ONLY_HANDOFF | [System handoff](system-handoffs/CE-07.md) |
| 94 | CE-08 | Disconnected Safe Paused | P0 | WF-06, WF-07 | UF-11 / GS-05 | SYSTEM_ONLY_HANDOFF | [System handoff](system-handoffs/CE-08.md) |
| 95 | CE-09 | Reconnected Safe Paused | P0 | WF-06, WF-07 | UF-11 / GS-05 | SYSTEM_ONLY_HANDOFF | [System handoff](system-handoffs/CE-09.md) |
| 96 | CE-10 | Permanent Focus Loss | P0 | WF-06, WF-07 | UF-11 / GS-05 | SYSTEM_ONLY_HANDOFF | [System handoff](system-handoffs/CE-10.md) |
| 97 | CE-11 | Interruption Policy Error | P0 | WF-06, WF-07 | UF-11 / GS-05 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CE-11.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 98 | CE-12 | Corrupt Current Item | P0 | WF-06, WF-07 | UF-11 / GS-05 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CE-12.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 99 | CE-13 | Controlled Skip | P0 | WF-06, WF-07 | UF-11 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CE-13.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 100 | CE-14 | Queue Ended by Corruption | P0 | WF-06, WF-07 | UF-11 / GS-05 | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CE-14.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 101 | CE-15 | Settings Default | P0 | WF-10 | UF-12 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-10/states/HF_STATE_CE-15.png) · [Spec](WF-10/states/HF_STATE_CE-15.md) |
| 102 | CE-16 | Setting Update Pending | P0 | WF-10 | UF-12 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CE-16.md) · [Anchor PNG](WF-10/frames/HF_FRAME_WF-10_SETTINGS_DEFAULT.png) |
| 103 | CE-17 | Settings Updated / Error | P0 | WF-10 | UF-12 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CE-17.md) · [Anchor PNG](WF-10/frames/HF_FRAME_WF-10_SETTINGS_DEFAULT.png) |
| 104 | CE-18 | Android Settings Handoff | P0 | WF-10 | UF-12 / GS-05 | SYSTEM_ONLY_HANDOFF | [System handoff](system-handoffs/CE-18.md) |
| 105 | CE-19 | Platform Recheck | P0 | WF-10 | UF-12 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CE-19.md) · [Anchor PNG](WF-10/frames/HF_FRAME_WF-10_SETTINGS_DEFAULT.png) |
| 106 | CE-20 | Bluetooth Selection | P1 | WF-10 | UF-12 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-10/states/HF_STATE_CE-20.png) · [Spec](WF-10/states/HF_STATE_CE-20.md) |
| 107 | CE-21 | Privacy Details | P0 | WF-10 | UF-12 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-10/states/HF_STATE_CE-21.png) · [Spec](WF-10/states/HF_STATE_CE-21.md) |
| 108 | CE-22 | Consent Decision | P0 | WF-10 | UF-12 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-10/states/HF_STATE_CE-22.png) · [Spec](WF-10/states/HF_STATE_CE-22.md) |
| 109 | CE-23 | Clear Selection | P0 | WF-10 | UF-12 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-10/states/HF_STATE_CE-23.png) · [Spec](WF-10/states/HF_STATE_CE-23.md) |
| 110 | CE-24 | Clear Confirmation | P0 | WF-10 | UF-12 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CE-24.md) · [Anchor PNG](WF-10/frames/HF_FRAME_WF-10_SETTINGS_DEFAULT.png) |
| 111 | CE-25 | Clearing | P0 | WF-10 | UF-12 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CE-25.md) · [Anchor PNG](WF-10/frames/HF_FRAME_WF-10_SETTINGS_DEFAULT.png) |
| 112 | CE-26 | Clear Result | P0 | WF-10 | UF-12 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/CE-26.md) · [Anchor PNG](WF-10/frames/HF_FRAME_WF-10_SETTINGS_DEFAULT.png) |
| 113 | UL-01 | P2 not shipped | P2 | WF-09 | UF-13 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/UL-01.md) · [Anchor PNG](WF-09/frames/HF_FRAME_WF-09_LIKED_SONGS_EMPTY.png) |
| 114 | UL-02 | Entry loading | P2 | WF-09 | UF-13 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-09/states/HF_STATE_UL-02.png) · [Spec](WF-09/states/HF_STATE_UL-02.md) |
| 115 | UL-03 | Empty | P2 | WF-09 | UF-13 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-09/states/HF_STATE_UL-03.png) · [Spec](WF-09/states/HF_STATE_UL-03.md) |
| 116 | UL-04 | Ready | P2 | WF-09 | UF-13 / — (not on GS-01..05 path) | DEDICATED_HIFI | [PNG](WF-09/states/HF_STATE_UL-04.png) · [Spec](WF-09/states/HF_STATE_UL-04.md) |
| 117 | UL-05 | Like write pending | P2 | WF-06, WF-09 | UF-13 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/UL-05.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 118 | UL-06 | Like acknowledged | P2 | WF-06, WF-09 | UF-13 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/UL-06.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 119 | UL-07 | Mutation failed | P2 | WF-06, WF-09 | UF-13 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/UL-07.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 120 | UL-08 | Unlike write pending | P2 | WF-06, WF-09 | UF-13 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/UL-08.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 121 | UL-09 | Playback command pending | P2 | WF-09, WF-06 | UF-13 → UF-05 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/UL-09.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 122 | UL-10 | Playback acknowledged | P2 | WF-09, WF-06 | UF-13 → UF-05 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/UL-10.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 123 | UL-11 | Playback rejected/stale | P2 | WF-09, WF-06 | UF-13 → UF-05 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/UL-11.md) · [Anchor PNG](WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png) |
| 124 | UL-12 | Load/reconcile failure | P2 | WF-09 | UF-13 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/UL-12.md) · [Anchor PNG](WF-09/frames/HF_FRAME_WF-09_LIKED_SONGS_EMPTY.png) |
| 125 | UL-13 | Missing item reconciliation | P2 | WF-09 | UF-13 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/UL-13.md) · [Anchor PNG](WF-09/frames/HF_FRAME_WF-09_LIKED_SONGS_EMPTY.png) |
| 126 | UL-14 | UI recreation | P2 | WF-09 | UF-13 / — (not on GS-01..05 path) | HIFI_VARIANT_ANNOTATION | [Variant](state-variants/UL-14.md) · [Anchor PNG](WF-09/frames/HF_FRAME_WF-09_LIKED_SONGS_EMPTY.png) |

## Required critical recovery families

- **Migration error:** S-01 anchored to WF-01_SPLASH_STARTUP_MIGRATION_ERROR; no destructive reset.
- **Permanently denied / platform return:** S-03 / CA-02 app education + SYS-01/02/04 handoffs; fresh Android permission recheck on return.
- **Scanning / Empty / recoverable library error:** 32/32 frame coverage includes WF-04_SCANNING, EMPTY, READY and RECOVERABLE_ERROR.
- **Search no-result/error:** CB-11 / CB-12 dedicated images; query and manual browsing remain intact.
- **Stale selection / controller reconnect:** CB-13 / CB-21 variant anchored to approved browse/player frame and actual MediaSession truth.
- **Corrupt file:** WF-06_CORRUPT_FILE_RECOVERY rendered; CB-19/CE-12/13/14 bounded skip/reconcile without deleting media.
- **Driving No Track, Paused, Playing:** CC-07/06/05; immediate Exit, fixed large controls, no text entry.
- **Voice ambiguity/retry/terminal:** CD-09 one candidate, CD-10 two candidates, CD-18 single Retry, CD-19 terminal Cancel/Manual; no third attempt.
- **Audio focus/route/lock/notifications:** only Android-owned handoff evidence; no fake OS screenshot.
- **Privacy & clear data:** CE-23/24/25/26; explicit destructive confirmation, never silent success.
- **P2 Like:** UL states are optional removable, not P0 acceptance blockers.

## QA assumptions, review limits and next gate

1. Generated frame screenshots are **deterministic skinning of frozen low-fi SVGs**. The app hierarchy is unchanged; text is direct source copy or documented bounded Voice specialization. New routes/controls are forbidden.
2. Voice and Driving requirements remain binding: six deterministic intents, one Retry only, 1–2 immutable confirmation choices, ≥48dp targets, Driving control ≥64dp, Mic ≥72dp, Exit ≥56dp, immediate Exit, no long lists/typing.
3. App visuals are static review artifacts, **not evidence of Compose 200% reflow, physical-device semantics, gesture/touch correctness, STT latency or accessibility sign-off**.
4. Reviewer names/signatures are **not** invented. GitHub final Issue #17 approval requires subsequent independent Product, Design, Android and QA review and the Phase-6 prototype/handoff work.
5. Canonical validation JSON: [HIFI_STATE_COVERAGE_REGISTER.json](HIFI_STATE_COVERAGE_REGISTER.json). Render proof: [PHASE_5_VISUAL_BOARD.png](PHASE_5_VISUAL_BOARD.png).

## Source contracts

- [Frozen 126-state register](../design-system/HIFI_TRACEABILITY_REGISTER.json)
- [Approved app-frame register](../wireframes/app-states/FRAME_REGISTER.json)
- [Low-fidelity app frames](../wireframes/app-states/README.md)
- [System transition source](../wireframes/recovery-system/SYSTEM_TRANSITION_REGISTER.json)
- [Component tokens](../design-system/components/COMPONENT_TOKENS.json)
- [Phase-4 primary board](PHASE_4_PRIMARY_BOARD.png)
- [Final software architecture](../SOFTWARE_ARCHITECTURE.md)
