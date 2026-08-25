# Phase 4 — 32 GitHub-native App State Frames

**Status:** **PASS / COMPLETE at construction level; cross-role approval pending**  
**Issue:** [#15](https://github.com/latticeward-labs/music-copilot/issues/15)  
**Count:** **32 Markdown + 32 SVG = 64 paired artifacts**  
**Foundations:** [Phase 3](../foundations/README.md)  
**Coverage:** [126-state matrix](../WIREFRAME_COVERAGE_MATRIX.md)  
**Validation:** [PHASE_4_VALIDATION.md](PHASE_4_VALIDATION.md)  
**Machine-readable register:** [FRAME_REGISTER.json](FRAME_REGISTER.json)

Each row is one reviewable app-visible state. Variants/recoveries and Android/System-only transitions remain attached through State IDs and annotations; this phase does not invent platform-owned destinations.

| # | Behavioral record | Visual | State IDs | GS | Priority | Ownership |
|---:|---|---|---|---|---|---|
| 1 | [`WF-01_SPLASH_LOADING`](WF-01_SPLASH_LOADING.md) | [SVG](WF-01_SPLASH_LOADING.svg) | S-01 | GS-01 | P0 | App |
| 2 | [`WF-01_SPLASH_STARTUP_MIGRATION_ERROR`](WF-01_SPLASH_STARTUP_MIGRATION_ERROR.md) | [SVG](WF-01_SPLASH_STARTUP_MIGRATION_ERROR.svg) | S-01 | GS-01 | P0 | App |
| 3 | [`WF-02_ONBOARDING_SLIDE_1`](WF-02_ONBOARDING_SLIDE_1.md) | [SVG](WF-02_ONBOARDING_SLIDE_1.svg) | S-02 | GS-01 | P0 | App |
| 4 | [`WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE`](WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE.md) | [SVG](WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE.svg) | S-02, CA-01, CA-02 | GS-01 | P0 | App |
| 5 | [`WF-03_PERMISSIONS_RATIONALE_REQUEST`](WF-03_PERMISSIONS_RATIONALE_REQUEST.md) | [SVG](WF-03_PERMISSIONS_RATIONALE_REQUEST.svg) | S-03, CA-02, SYS-01, SYS-02 | GS-01 | P0 | Hybrid |
| 6 | [`WF-03_PERMISSIONS_GRANTED`](WF-03_PERMISSIONS_GRANTED.md) | [SVG](WF-03_PERMISSIONS_GRANTED.svg) | S-03, CA-02 | GS-01 | P0 | Hybrid |
| 7 | [`WF-03_PERMISSIONS_DENIED`](WF-03_PERMISSIONS_DENIED.md) | [SVG](WF-03_PERMISSIONS_DENIED.svg) | S-03, CA-02 | GS-01 | P0 | Hybrid |
| 8 | [`WF-03_PERMISSIONS_PERMANENTLY_DENIED`](WF-03_PERMISSIONS_PERMANENTLY_DENIED.md) | [SVG](WF-03_PERMISSIONS_PERMANENTLY_DENIED.svg) | S-03, CA-02, SYS-04 | GS-01 | P0 | Hybrid |
| 9 | [`WF-04_HOME_LIBRARY_SCANNING`](WF-04_HOME_LIBRARY_SCANNING.md) | [SVG](WF-04_HOME_LIBRARY_SCANNING.svg) | S-04 | GS-01 / GS-02 | P0 | App |
| 10 | [`WF-04_HOME_LIBRARY_EMPTY`](WF-04_HOME_LIBRARY_EMPTY.md) | [SVG](WF-04_HOME_LIBRARY_EMPTY.svg) | S-04 | GS-01 / GS-02 | P0 | App |
| 11 | [`WF-04_HOME_LIBRARY_READY`](WF-04_HOME_LIBRARY_READY.md) | [SVG](WF-04_HOME_LIBRARY_READY.svg) | S-04, CB-01, CB-20 | GS-01 / GS-02 | P0 | App |
| 12 | [`WF-04_HOME_LIBRARY_RECOVERABLE_ERROR`](WF-04_HOME_LIBRARY_RECOVERABLE_ERROR.md) | [SVG](WF-04_HOME_LIBRARY_RECOVERABLE_ERROR.svg) | S-04 | GS-01 / GS-02 | P0 | App |
| 13 | [`WF-05_SEARCH_RESULTS`](WF-05_SEARCH_RESULTS.md) | [SVG](WF-05_SEARCH_RESULTS.svg) | CB-09, CB-10, CB-13 | GS-02 | P0 | App |
| 14 | [`WF-05_SEARCH_NO_RESULTS`](WF-05_SEARCH_NO_RESULTS.md) | [SVG](WF-05_SEARCH_NO_RESULTS.svg) | CB-09, CB-11 | GS-02 | P0 | App |
| 15 | [`WF-05_SEARCH_ERROR`](WF-05_SEARCH_ERROR.md) | [SVG](WF-05_SEARCH_ERROR.svg) | CB-09, CB-12 | GS-02 | P0 | App |
| 16 | [`WF-06_FULL_PLAYER_PLAYING`](WF-06_FULL_PLAYER_PLAYING.md) | [SVG](WF-06_FULL_PLAYER_PLAYING.svg) | CB-15 | GS-02 / GS-03 / GS-05 | P0 | App + Playback Service |
| 17 | [`WF-06_FULL_PLAYER_PAUSED`](WF-06_FULL_PLAYER_PAUSED.md) | [SVG](WF-06_FULL_PLAYER_PAUSED.svg) | CB-16 | GS-02 / GS-03 / GS-05 | P0 | App + Playback Service |
| 18 | [`WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY`](WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY.md) | [SVG](WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY.svg) | CB-19, CE-12, CE-13, CE-14 | GS-02 / GS-03 / GS-05 | P0 | App + Playback Service |
| 19 | [`WF-07_DRIVING_MODE_PLAYING`](WF-07_DRIVING_MODE_PLAYING.md) | [SVG](WF-07_DRIVING_MODE_PLAYING.svg) | CC-05, CC-14, CC-16 | GS-04 / GS-05 | P0 | App + Playback Service |
| 20 | [`WF-07_DRIVING_MODE_PAUSED`](WF-07_DRIVING_MODE_PAUSED.md) | [SVG](WF-07_DRIVING_MODE_PAUSED.svg) | CC-06, CC-16 | GS-04 / GS-05 | P0 | App + Playback Service |
| 21 | [`WF-07_DRIVING_MODE_NO_TRACK`](WF-07_DRIVING_MODE_NO_TRACK.md) | [SVG](WF-07_DRIVING_MODE_NO_TRACK.svg) | CC-07, CC-15 | GS-04 / GS-05 | P0 | App + Playback Service |
| 22 | [`WF-08_VOICE_LISTENING`](WF-08_VOICE_LISTENING.md) | [SVG](WF-08_VOICE_LISTENING.svg) | CD-03, CC-14 | GS-04 recovery path | P0 | App + Voice Coordinator |
| 23 | [`WF-08_VOICE_PROCESSING`](WF-08_VOICE_PROCESSING.md) | [SVG](WF-08_VOICE_PROCESSING.svg) | CD-05 | GS-04 recovery path | P0 | App + Voice Coordinator |
| 24 | [`WF-08_VOICE_CONFIRMING`](WF-08_VOICE_CONFIRMING.md) | [SVG](WF-08_VOICE_CONFIRMING.svg) | CD-09, CD-10 | GS-04 recovery path | P0 | App + Voice Coordinator |
| 25 | [`WF-08_VOICE_EXECUTED`](WF-08_VOICE_EXECUTED.md) | [SVG](WF-08_VOICE_EXECUTED.svg) | CD-07 | GS-04 recovery path | P0 | App + Voice Coordinator |
| 26 | [`WF-08_VOICE_FAILED_RETRY`](WF-08_VOICE_FAILED_RETRY.md) | [SVG](WF-08_VOICE_FAILED_RETRY.svg) | CD-08, CD-17, CD-18, CD-19 | GS-04 recovery path | P0 | App + Voice Coordinator |
| 27 | [`WF-09_LIKED_SONGS_EMPTY`](WF-09_LIKED_SONGS_EMPTY.md) | [SVG](WF-09_LIKED_SONGS_EMPTY.svg) | UL-02, UL-03 | — (P2; not required by GS-01..05) | P2 / Removable | App |
| 28 | [`WF-09_LIKED_SONGS_READY`](WF-09_LIKED_SONGS_READY.md) | [SVG](WF-09_LIKED_SONGS_READY.svg) | UL-04, UL-09, UL-10, UL-12 | — (P2; not required by GS-01..05) | P2 / Removable | App |
| 29 | [`WF-10_SETTINGS_DEFAULT`](WF-10_SETTINGS_DEFAULT.md) | [SVG](WF-10_SETTINGS_DEFAULT.svg) | CE-15, CE-21, CE-22 | — (not on GS-01..05 path) | P0 | App + platform/provider boundary |
| 30 | [`WF-10_SETTINGS_BLUETOOTH_UPDATED`](WF-10_SETTINGS_BLUETOOTH_UPDATED.md) | [SVG](WF-10_SETTINGS_BLUETOOTH_UPDATED.svg) | CE-20, CE-16, CE-17 | — (not on GS-01..05 path) | P1 / Removable preference | App + platform/provider boundary |
| 31 | [`WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION`](WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION.md) | [SVG](WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION.svg) | CE-23, CE-24 | — (not on GS-01..05 path) | P0 | App + platform/provider boundary |
| 32 | [`WF-10_SETTINGS_CLEARED_PRIVACY_STATUS`](WF-10_SETTINGS_CLEARED_PRIVACY_STATUS.md) | [SVG](WF-10_SETTINGS_CLEARED_PRIVACY_STATUS.svg) | CE-21, CE-25, CE-26 | — (not on GS-01..05 path) | P0 | App + platform/provider boundary |

## Totals

| Package | Frames |
|---|---:|
| WF-01 Splash | 2 |
| WF-02 Onboarding | 2 |
| WF-03 Permissions | 4 |
| WF-04 Home/Library | 4 |
| WF-05 Search | 3 |
| WF-06 Full Player | 3 |
| WF-07 Driving Mode | 3 |
| WF-08 Voice Overlay | 5 |
| WF-09 Liked Songs | 2 |
| WF-10 Settings/Privacy | 4 |
| **Total** | **32** |
