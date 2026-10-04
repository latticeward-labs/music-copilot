# Issue #17 Phase 3 - Component Coverage Matrix

- **Base commit:** `ee7685b4520953b8bab47ce16091fb8924f053cc`
- **App-visible low-fidelity frames:** 32
- **Mapped frames:** 32
- **Unmapped frames:** 0
- **Component catalog:** `COMPONENT_CATALOG.md`

## Rule

Every approved app-visible frame must be constructible from reusable Phase-3 components plus ordinary non-interactive layout containers/text. A later high-fidelity screen may not invent a new recurring control locally. If a genuinely new recurring control is required, update the Component System first and record traceability.

| # | Approved app-state frame | WF family | Reusable component composition | Status |
|---:|---|---|---|---|
| 1 | `WF-01_SPLASH_LOADING` | `WF-01` | CMP-15 | **COVERED** |
| 2 | `WF-01_SPLASH_STARTUP_MIGRATION_ERROR` | `WF-01` | CMP-15, CMP-01 | **COVERED** |
| 3 | `WF-02_ONBOARDING_SLIDE_1` | `WF-02` | CMP-01, CMP-22 | **COVERED** |
| 4 | `WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE` | `WF-02` | CMP-01, CMP-22 | **COVERED** |
| 5 | `WF-03_PERMISSIONS_RATIONALE_REQUEST` | `WF-03` | CMP-03, CMP-14, CMP-01, CMP-21 | **COVERED** |
| 6 | `WF-03_PERMISSIONS_GRANTED` | `WF-03` | CMP-03, CMP-14, CMP-15 | **COVERED** |
| 7 | `WF-03_PERMISSIONS_DENIED` | `WF-03` | CMP-03, CMP-14, CMP-01 | **COVERED** |
| 8 | `WF-03_PERMISSIONS_PERMANENTLY_DENIED` | `WF-03` | CMP-03, CMP-14, CMP-01, CMP-21 | **COVERED** |
| 9 | `WF-04_HOME_LIBRARY_SCANNING` | `WF-04` | CMP-03, CMP-15, CMP-09, CMP-04 | **COVERED** |
| 10 | `WF-04_HOME_LIBRARY_EMPTY` | `WF-04` | CMP-03, CMP-15, CMP-01, CMP-09, CMP-04 | **COVERED** |
| 11 | `WF-04_HOME_LIBRARY_READY` | `WF-04` | CMP-03, CMP-12, CMP-11, CMP-09, CMP-04 | **COVERED** |
| 12 | `WF-04_HOME_LIBRARY_RECOVERABLE_ERROR` | `WF-04` | CMP-03, CMP-15, CMP-01, CMP-09, CMP-04 | **COVERED** |
| 13 | `WF-05_SEARCH_RESULTS` | `WF-05` | CMP-03, CMP-10, CMP-11, CMP-04 | **COVERED** |
| 14 | `WF-05_SEARCH_NO_RESULTS` | `WF-05` | CMP-03, CMP-10, CMP-15, CMP-04 | **COVERED** |
| 15 | `WF-05_SEARCH_ERROR` | `WF-05` | CMP-03, CMP-10, CMP-15, CMP-01, CMP-04 | **COVERED** |
| 16 | `WF-06_FULL_PLAYER_PLAYING` | `WF-06` | CMP-03, CMP-08, CMP-07, CMP-13, CMP-05, CMP-06, CMP-20, CMP-11 | **COVERED** |
| 17 | `WF-06_FULL_PLAYER_PAUSED` | `WF-06` | CMP-03, CMP-08, CMP-07, CMP-13, CMP-05, CMP-06, CMP-20, CMP-11 | **COVERED** |
| 18 | `WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY` | `WF-06` | CMP-03, CMP-08, CMP-07, CMP-05, CMP-15, CMP-01, CMP-11 | **COVERED** |
| 19 | `WF-07_DRIVING_MODE_PLAYING` | `WF-07` | CMP-08, CMP-07, CMP-05, CMP-06, CMP-01 | **COVERED** |
| 20 | `WF-07_DRIVING_MODE_PAUSED` | `WF-07` | CMP-08, CMP-07, CMP-05, CMP-06, CMP-01 | **COVERED** |
| 21 | `WF-07_DRIVING_MODE_NO_TRACK` | `WF-07` | CMP-07, CMP-15, CMP-05, CMP-06, CMP-01 | **COVERED** |
| 22 | `WF-08_VOICE_LISTENING` | `WF-08` | CMP-19, CMP-01 | **COVERED** |
| 23 | `WF-08_VOICE_PROCESSING` | `WF-08` | CMP-19 | **COVERED** |
| 24 | `WF-08_VOICE_CONFIRMING` | `WF-08` | CMP-19, CMP-16 | **COVERED** |
| 25 | `WF-08_VOICE_EXECUTED` | `WF-08` | CMP-19, CMP-17 | **COVERED** |
| 26 | `WF-08_VOICE_FAILED_RETRY` | `WF-08` | CMP-19, CMP-01, CMP-17 | **COVERED** |
| 27 | `WF-09_LIKED_SONGS_EMPTY` | `WF-09` | CMP-03, CMP-15, CMP-04 | **COVERED** |
| 28 | `WF-09_LIKED_SONGS_READY` | `WF-09` | CMP-03, CMP-01, CMP-11, CMP-20, CMP-04 | **COVERED** |
| 29 | `WF-10_SETTINGS_DEFAULT` | `WF-10` | CMP-03, CMP-18, CMP-04, CMP-21 | **COVERED** |
| 30 | `WF-10_SETTINGS_BLUETOOTH_UPDATED` | `WF-10` | CMP-03, CMP-18, CMP-17, CMP-04 | **COVERED** |
| 31 | `WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION` | `WF-10` | CMP-03, CMP-23, CMP-01 | **COVERED** |
| 32 | `WF-10_SETTINGS_CLEARED_PRIVACY_STATUS` | `WF-10` | CMP-03, CMP-18, CMP-15, CMP-17, CMP-04 | **COVERED** |

## Coverage checks

- WF-01: covered
- WF-02: covered
- WF-03: covered
- WF-04: covered
- WF-05: covered
- WF-06: covered
- WF-07: covered
- WF-08: covered
- WF-09: covered as P2/removable
- WF-10: covered

**Decision:** 32/32 app-visible frames have an explicit reusable component composition. No approved app-visible frame requires a one-off recurring control at Phase-3 baseline.
