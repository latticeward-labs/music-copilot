# Repository-native Low-fidelity Wireframe Index

**Status:** **ISSUE #15 IN PROGRESS — Phase 7 traceability/validation PASS; named cross-role approval pending**  
**Official editable workfile:** `docs/wireframes/` — Markdown + SVG + JSON  
**Complete count:** **45 Markdown + 45 SVG = 45 stable pairs / 90 files**  
**App frames:** **32**  
**System/recovery transitions:** **13**, covering **20/20** canonical System-only states  
**Machine index:** [ARTIFACT_REGISTER.json](ARTIFACT_REGISTER.json)  
**Phase 6 validation:** [PHASE_6_REPOSITORY_ARTIFACT_VALIDATION.md](PHASE_6_REPOSITORY_ARTIFACT_VALIDATION.md)  
**Tracking:** [Issue #15](https://github.com/latticeward-labs/music-copilot/issues/15)

This file is the complete active index for every repository-native low-fidelity `WF-*` pair. The existing identifiers and filenames from Phases 4 and 5 are preserved; Phase 6 creates no duplicate Frame and renames none.

## Active delivery contract

- Behavioral authority: paired Markdown record.
- Editable low-fidelity visual: paired text-based SVG.
- Former Figma Node URL: replaced by the immutable repository SVG URL recorded in each App Frame and in the machine register.
- Review/version history: construction PR and commit plus the current review disposition.
- Android/System surfaces remain transitions, not invented app screens.
- A pair is construction-complete only when both paths exist and identifiers match.
- Construction PASS is not Product/Design/Android/QA approval.

## Complete WF artifact index

| # | Kind | Behavioral record | Visual | UF | State IDs | GS | Priority | Review status |
|---:|---|---|---|---|---|---|---|---|
| 1 | App Frame | [`WF-01_SPLASH_LOADING`](app-states/WF-01_SPLASH_LOADING.md) | [SVG](app-states/WF-01_SPLASH_LOADING.svg) | UF-01 | S-01 | GS-01 | P0 | Construction PASS / approval pending |
| 2 | App Frame | [`WF-01_SPLASH_STARTUP_MIGRATION_ERROR`](app-states/WF-01_SPLASH_STARTUP_MIGRATION_ERROR.md) | [SVG](app-states/WF-01_SPLASH_STARTUP_MIGRATION_ERROR.svg) | UF-01 | S-01 | GS-01 | P0 | Construction PASS / approval pending |
| 3 | App Frame | [`WF-02_ONBOARDING_SLIDE_1`](app-states/WF-02_ONBOARDING_SLIDE_1.md) | [SVG](app-states/WF-02_ONBOARDING_SLIDE_1.svg) | UF-01 | S-02 | GS-01 | P0 | Construction PASS / approval pending |
| 4 | App Frame | [`WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE`](app-states/WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE.md) | [SVG](app-states/WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE.svg) | UF-01 | S-02, CA-01, CA-02 | GS-01 | P0 | Construction PASS / approval pending |
| 5 | App Frame | [`WF-03_PERMISSIONS_RATIONALE_REQUEST`](app-states/WF-03_PERMISSIONS_RATIONALE_REQUEST.md) | [SVG](app-states/WF-03_PERMISSIONS_RATIONALE_REQUEST.svg) | UF-02 / UF-10 / UF-12 | S-03, CA-02, SYS-01, SYS-02 | GS-01 | P0 | Construction PASS / approval pending |
| 6 | App Frame | [`WF-03_PERMISSIONS_GRANTED`](app-states/WF-03_PERMISSIONS_GRANTED.md) | [SVG](app-states/WF-03_PERMISSIONS_GRANTED.svg) | UF-02 / UF-10 / UF-12 | S-03, CA-02 | GS-01 | P0 | Construction PASS / approval pending |
| 7 | App Frame | [`WF-03_PERMISSIONS_DENIED`](app-states/WF-03_PERMISSIONS_DENIED.md) | [SVG](app-states/WF-03_PERMISSIONS_DENIED.svg) | UF-02 / UF-10 / UF-12 | S-03, CA-02 | GS-01 | P0 | Construction PASS / approval pending |
| 8 | App Frame | [`WF-03_PERMISSIONS_PERMANENTLY_DENIED`](app-states/WF-03_PERMISSIONS_PERMANENTLY_DENIED.md) | [SVG](app-states/WF-03_PERMISSIONS_PERMANENTLY_DENIED.svg) | UF-02 / UF-10 / UF-12 | S-03, CA-02, SYS-04 | GS-01 | P0 | Construction PASS / approval pending |
| 9 | App Frame | [`WF-04_HOME_LIBRARY_SCANNING`](app-states/WF-04_HOME_LIBRARY_SCANNING.md) | [SVG](app-states/WF-04_HOME_LIBRARY_SCANNING.svg) | UF-03 / UF-04 | S-04 | GS-01 / GS-02 | P0 | Construction PASS / approval pending |
| 10 | App Frame | [`WF-04_HOME_LIBRARY_EMPTY`](app-states/WF-04_HOME_LIBRARY_EMPTY.md) | [SVG](app-states/WF-04_HOME_LIBRARY_EMPTY.svg) | UF-03 / UF-04 | S-04 | GS-01 / GS-02 | P0 | Construction PASS / approval pending |
| 11 | App Frame | [`WF-04_HOME_LIBRARY_READY`](app-states/WF-04_HOME_LIBRARY_READY.md) | [SVG](app-states/WF-04_HOME_LIBRARY_READY.svg) | UF-03 / UF-04 | S-04, CB-01, CB-20 | GS-01 / GS-02 | P0 | Construction PASS / approval pending |
| 12 | App Frame | [`WF-04_HOME_LIBRARY_RECOVERABLE_ERROR`](app-states/WF-04_HOME_LIBRARY_RECOVERABLE_ERROR.md) | [SVG](app-states/WF-04_HOME_LIBRARY_RECOVERABLE_ERROR.svg) | UF-03 / UF-04 | S-04 | GS-01 / GS-02 | P0 | Construction PASS / approval pending |
| 13 | App Frame | [`WF-05_SEARCH_RESULTS`](app-states/WF-05_SEARCH_RESULTS.md) | [SVG](app-states/WF-05_SEARCH_RESULTS.svg) | UF-04 | CB-09, CB-10, CB-13 | GS-02 | P0 | Construction PASS / approval pending |
| 14 | App Frame | [`WF-05_SEARCH_NO_RESULTS`](app-states/WF-05_SEARCH_NO_RESULTS.md) | [SVG](app-states/WF-05_SEARCH_NO_RESULTS.svg) | UF-04 | CB-09, CB-11 | GS-02 | P0 | Construction PASS / approval pending |
| 15 | App Frame | [`WF-05_SEARCH_ERROR`](app-states/WF-05_SEARCH_ERROR.md) | [SVG](app-states/WF-05_SEARCH_ERROR.svg) | UF-04 | CB-09, CB-12 | GS-02 | P0 | Construction PASS / approval pending |
| 16 | App Frame | [`WF-06_FULL_PLAYER_PLAYING`](app-states/WF-06_FULL_PLAYER_PLAYING.md) | [SVG](app-states/WF-06_FULL_PLAYER_PLAYING.svg) | UF-05 / UF-06 / UF-11 | CB-15 | GS-02 / GS-03 / GS-05 | P0 | Construction PASS / approval pending |
| 17 | App Frame | [`WF-06_FULL_PLAYER_PAUSED`](app-states/WF-06_FULL_PLAYER_PAUSED.md) | [SVG](app-states/WF-06_FULL_PLAYER_PAUSED.svg) | UF-05 / UF-06 / UF-11 | CB-16 | GS-02 / GS-03 / GS-05 | P0 | Construction PASS / approval pending |
| 18 | App Frame | [`WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY`](app-states/WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY.md) | [SVG](app-states/WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY.svg) | UF-05 / UF-06 / UF-11 | CB-19, CE-12, CE-13, CE-14 | GS-02 / GS-03 / GS-05 | P0 | Construction PASS / approval pending |
| 19 | App Frame | [`WF-07_DRIVING_MODE_PLAYING`](app-states/WF-07_DRIVING_MODE_PLAYING.md) | [SVG](app-states/WF-07_DRIVING_MODE_PLAYING.svg) | UF-07 / UF-11 | CC-05, CC-14, CC-16 | GS-04 / GS-05 | P0 | Construction PASS / approval pending |
| 20 | App Frame | [`WF-07_DRIVING_MODE_PAUSED`](app-states/WF-07_DRIVING_MODE_PAUSED.md) | [SVG](app-states/WF-07_DRIVING_MODE_PAUSED.svg) | UF-07 / UF-11 | CC-06, CC-16 | GS-04 / GS-05 | P0 | Construction PASS / approval pending |
| 21 | App Frame | [`WF-07_DRIVING_MODE_NO_TRACK`](app-states/WF-07_DRIVING_MODE_NO_TRACK.md) | [SVG](app-states/WF-07_DRIVING_MODE_NO_TRACK.svg) | UF-07 / UF-11 | CC-07, CC-15 | GS-04 / GS-05 | P0 | Construction PASS / approval pending |
| 22 | App Frame | [`WF-08_VOICE_LISTENING`](app-states/WF-08_VOICE_LISTENING.md) | [SVG](app-states/WF-08_VOICE_LISTENING.svg) | UF-08 / UF-09 / UF-10 | CD-03, CC-14 | GS-04 recovery path | P0 | Construction PASS / approval pending |
| 23 | App Frame | [`WF-08_VOICE_PROCESSING`](app-states/WF-08_VOICE_PROCESSING.md) | [SVG](app-states/WF-08_VOICE_PROCESSING.svg) | UF-08 / UF-09 / UF-10 | CD-05 | GS-04 recovery path | P0 | Construction PASS / approval pending |
| 24 | App Frame | [`WF-08_VOICE_CONFIRMING`](app-states/WF-08_VOICE_CONFIRMING.md) | [SVG](app-states/WF-08_VOICE_CONFIRMING.svg) | UF-08 / UF-09 / UF-10 | CD-09, CD-10 | GS-04 recovery path | P0 | Construction PASS / approval pending |
| 25 | App Frame | [`WF-08_VOICE_EXECUTED`](app-states/WF-08_VOICE_EXECUTED.md) | [SVG](app-states/WF-08_VOICE_EXECUTED.svg) | UF-08 / UF-09 / UF-10 | CD-07 | GS-04 recovery path | P0 | Construction PASS / approval pending |
| 26 | App Frame | [`WF-08_VOICE_FAILED_RETRY`](app-states/WF-08_VOICE_FAILED_RETRY.md) | [SVG](app-states/WF-08_VOICE_FAILED_RETRY.svg) | UF-08 / UF-09 / UF-10 | CD-08, CD-17, CD-18, CD-19 | GS-04 recovery path | P0 | Construction PASS / approval pending |
| 27 | App Frame | [`WF-09_LIKED_SONGS_EMPTY`](app-states/WF-09_LIKED_SONGS_EMPTY.md) | [SVG](app-states/WF-09_LIKED_SONGS_EMPTY.svg) | UF-13 | UL-02, UL-03 | — (P2; not required by GS-01..05) | P2 / Removable | Construction PASS / approval pending |
| 28 | App Frame | [`WF-09_LIKED_SONGS_READY`](app-states/WF-09_LIKED_SONGS_READY.md) | [SVG](app-states/WF-09_LIKED_SONGS_READY.svg) | UF-13 | UL-04, UL-09, UL-10, UL-12 | — (P2; not required by GS-01..05) | P2 / Removable | Construction PASS / approval pending |
| 29 | App Frame | [`WF-10_SETTINGS_DEFAULT`](app-states/WF-10_SETTINGS_DEFAULT.md) | [SVG](app-states/WF-10_SETTINGS_DEFAULT.svg) | UF-12 | CE-15, CE-21, CE-22 | — (not on GS-01..05 path) | P0 | Construction PASS / approval pending |
| 30 | App Frame | [`WF-10_SETTINGS_BLUETOOTH_UPDATED`](app-states/WF-10_SETTINGS_BLUETOOTH_UPDATED.md) | [SVG](app-states/WF-10_SETTINGS_BLUETOOTH_UPDATED.svg) | UF-12 | CE-20, CE-16, CE-17 | — (not on GS-01..05 path) | P1 / Removable preference | Construction PASS / approval pending |
| 31 | App Frame | [`WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION`](app-states/WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION.md) | [SVG](app-states/WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION.svg) | UF-12 | CE-23, CE-24 | — (not on GS-01..05 path) | P0 | Construction PASS / approval pending |
| 32 | App Frame | [`WF-10_SETTINGS_CLEARED_PRIVACY_STATUS`](app-states/WF-10_SETTINGS_CLEARED_PRIVACY_STATUS.md) | [SVG](app-states/WF-10_SETTINGS_CLEARED_PRIVACY_STATUS.svg) | UF-12 | CE-21, CE-25, CE-26 | — (not on GS-01..05 path) | P0 | Construction PASS / approval pending |
| 33 | System/Recovery | [`WF-03_SYSTEM_MEDIA_PERMISSION_DIALOG`](recovery-system/WF-03_SYSTEM_MEDIA_PERMISSION_DIALOG.md) | [SVG](recovery-system/WF-03_SYSTEM_MEDIA_PERMISSION_DIALOG.svg) | UF-02 / UF-03 | SYS-01, S-03 | GS-01 | P0 | Construction PASS / approval pending |
| 34 | System/Recovery | [`WF-03_SYSTEM_MICROPHONE_PERMISSION_DIALOG`](recovery-system/WF-03_SYSTEM_MICROPHONE_PERMISSION_DIALOG.md) | [SVG](recovery-system/WF-03_SYSTEM_MICROPHONE_PERMISSION_DIALOG.svg) | UF-02 / UF-08 / UF-10 | SYS-02, S-03, CA-02, CD-21 | GS-04 recovery | P0 | Construction PASS / approval pending |
| 35 | System/Recovery | [`WF-06_SYSTEM_NOTIFICATION_PERMISSION_BOUNDARY`](recovery-system/WF-06_SYSTEM_NOTIFICATION_PERMISSION_BOUNDARY.md) | [SVG](recovery-system/WF-06_SYSTEM_NOTIFICATION_PERMISSION_BOUNDARY.svg) | UF-06 / UF-12 | SYS-03, CB-30 | GS-03 recovery | P0 | Construction PASS / approval pending |
| 36 | System/Recovery | [`WF-03_SYSTEM_ANDROID_SETTINGS_HANDOFF`](recovery-system/WF-03_SYSTEM_ANDROID_SETTINGS_HANDOFF.md) | [SVG](recovery-system/WF-03_SYSTEM_ANDROID_SETTINGS_HANDOFF.svg) | UF-02 / UF-10 / UF-12 | SYS-04, CE-18, CE-19 | GS-01 / GS-05 recovery | P0 | Construction PASS / approval pending |
| 37 | System/Recovery | [`WF-10_CLOUD_VOICE_CONSENT_OUTSIDE_DRIVING`](recovery-system/WF-10_CLOUD_VOICE_CONSENT_OUTSIDE_DRIVING.md) | [SVG](recovery-system/WF-10_CLOUD_VOICE_CONSENT_OUTSIDE_DRIVING.svg) | UF-02 / UF-10 / UF-12 | SYS-05, CD-23, CE-21, CE-22 | GS-04 recovery | P0 | Construction PASS / approval pending |
| 38 | System/Recovery | [`WF-06_SYSTEM_MEDIA_NOTIFICATION_CONTROLS`](recovery-system/WF-06_SYSTEM_MEDIA_NOTIFICATION_CONTROLS.md) | [SVG](recovery-system/WF-06_SYSTEM_MEDIA_NOTIFICATION_CONTROLS.svg) | UF-06 | CB-22, CB-26, CB-29, CB-30, CB-31 | GS-03 | P0 | Construction PASS / approval pending |
| 39 | System/Recovery | [`WF-06_SYSTEM_LOCK_SCREEN_CONTROLS`](recovery-system/WF-06_SYSTEM_LOCK_SCREEN_CONTROLS.md) | [SVG](recovery-system/WF-06_SYSTEM_LOCK_SCREEN_CONTROLS.svg) | UF-06 | CB-24, CB-27, CB-29, CB-31 | GS-03 | P0 | Construction PASS / approval pending |
| 40 | System/Recovery | [`WF-06_SYSTEM_EXTERNAL_MEDIA_CONTROLS`](recovery-system/WF-06_SYSTEM_EXTERNAL_MEDIA_CONTROLS.md) | [SVG](recovery-system/WF-06_SYSTEM_EXTERNAL_MEDIA_CONTROLS.svg) | UF-06 / UF-11 | CB-28, CB-29, CB-31 | GS-03 / GS-05 | P0 | Construction PASS / approval pending |
| 41 | System/Recovery | [`WF-06_UI_RECREATION_SERVICE_RECONNECT`](recovery-system/WF-06_UI_RECREATION_SERVICE_RECONNECT.md) | [SVG](recovery-system/WF-06_UI_RECREATION_SERVICE_RECONNECT.svg) | UF-05 / UF-06 | CB-21, CB-22, CB-23, CB-25, CB-32, CB-33 | GS-03 | P0 + P1 paused restore | Construction PASS / approval pending |
| 42 | System/Recovery | [`WF-06_SYSTEM_PHONE_CALL_AUDIO_FOCUS`](recovery-system/WF-06_SYSTEM_PHONE_CALL_AUDIO_FOCUS.md) | [SVG](recovery-system/WF-06_SYSTEM_PHONE_CALL_AUDIO_FOCUS.svg) | UF-11 / UF-07 | CC-16, CE-02, CE-03, CE-04, CE-05, CE-06, CE-10, CE-11 | GS-05 | P0 | Construction PASS / approval pending |
| 43 | System/Recovery | [`WF-06_SYSTEM_WIRED_HEADSET_ROUTE_RECOVERY`](recovery-system/WF-06_SYSTEM_WIRED_HEADSET_ROUTE_RECOVERY.md) | [SVG](recovery-system/WF-06_SYSTEM_WIRED_HEADSET_ROUTE_RECOVERY.svg) | UF-11 / UF-07 | CC-16, CE-07, CE-08, CE-09 | GS-05 | P0 | Construction PASS / approval pending |
| 44 | System/Recovery | [`WF-06_SYSTEM_BLUETOOTH_ROUTE_RECOVERY`](recovery-system/WF-06_SYSTEM_BLUETOOTH_ROUTE_RECOVERY.md) | [SVG](recovery-system/WF-06_SYSTEM_BLUETOOTH_ROUTE_RECOVERY.svg) | UF-06 / UF-07 / UF-11 | CB-28, CC-16, CE-07, CE-08, CE-09 | GS-03 / GS-05 | P0 | Construction PASS / approval pending |
| 45 | System/Recovery | [`WF-08_DRIVING_VOICE_RETRY_MANUAL_FALLBACK`](recovery-system/WF-08_DRIVING_VOICE_RETRY_MANUAL_FALLBACK.md) | [SVG](recovery-system/WF-08_DRIVING_VOICE_RETRY_MANUAL_FALLBACK.svg) | UF-07 / UF-08 / UF-10 | CC-14, CD-17, CD-18, CD-19, CD-20, CD-24 | GS-04 recovery | P0 | Construction PASS / approval pending |

## Supporting controls

- [GitHub-native contract](GITHUB_NATIVE_WIREFRAME_CONTRACT_ADDENDUM_2026-08-25.md)
- [Workspace topology](WORKSPACE_INDEX.md)
- [126-state Coverage Matrix](WIREFRAME_COVERAGE_MATRIX.md)
- [Foundations](foundations/README.md)
- [Phase 4 source register](app-states/FRAME_REGISTER.json) and [validation](app-states/PHASE_4_VALIDATION.md)
- [Phase 5 source register](recovery-system/SYSTEM_TRANSITION_REGISTER.json), [terminal matrix](recovery-system/RECOVERY_TERMINAL_MATRIX.md) and [validation](recovery-system/PHASE_5_VALIDATION.md)

## Phase 7 traceability and validation

- [Machine traceability register](PHASE_7_TRACEABILITY_REGISTER.json): UF 13/13, GS 5/5, screen families 10/10, State dispositions 126/126 and P0 106/106.
- [Navigation and Behavioral Audit](PHASE_7_NAVIGATION_BEHAVIOR_AUDIT.md): Entry/Exit/Back 32/32, System return paths 13/13, handoffs 8/8, Voice/Driving/playback PASS.
- [Link Audit](PHASE_7_LINK_AUDIT.md): 0 broken internal paths, 0 invalid validated GitHub repository paths and 0 active Figma dependencies.
- [Combined validation](PHASE_7_TRACEABILITY_VALIDATION.md): PASS / COMPLETE at repository validation level.

## Review gate

Phase 7 traceability and connected-behavior validation is PASS. All 126 State dispositions, variants, navigation/handoffs and active links are validated. Issue #15 remains open only until named Product, Design, Android and QA review is recorded.
