# Phase 10 — Exhaustive Rendered High-Fi Asset Trace

Every rendered source PNG in docs/high-fidelity is indexed below. State links are
resolved against the frozen ledger, including contextual cross-WF Voice/OS annotations.
Summary contact boards intentionally have no individual State ID: they are supporting
montages of already registered screens, not orphan screens.

| Rendered asset | Class(es) | WF | Frozen State ID(s) | UF → GS bridge / P2 exception | PRD/MVP + architecture |
|---|---|---|---|---|---|
| docs/high-fidelity/PHASE_4_PRIMARY_BOARD.png | PHASE9_ASSET | aggregate | no independent State | Supporting QA/contact board; not a new State | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/PHASE_5_VISUAL_BOARD.png | PHASE9_ASSET | aggregate | no independent State | Supporting QA/contact board; not a new State | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-01/HF-01_SPLASH_LOADING.png | PHASE9_ASSET, PRIMARY_HF | WF-01 | S-01 | UF-01 → GS-01 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-01/frames/HF_FRAME_WF-01_SPLASH_LOADING.png | HF_FRAME, PHASE9_ASSET | WF-01 | S-01 | UF-01 → GS-01 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-01/frames/HF_FRAME_WF-01_SPLASH_STARTUP_MIGRATION_ERROR.png | HF_FRAME, PHASE9_ASSET | WF-01 | S-01 | UF-01 → GS-01 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-01/states/HF_STATE_S-01.png | DEDICATED_RENDER, PHASE9_ASSET | WF-01 | S-01 | UF-01 → GS-01 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-02/HF-02_ONBOARDING_SLIDE_1.png | PHASE9_ASSET, PRIMARY_HF | WF-02 | S-02 | UF-01 → GS-01 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-02/frames/HF_FRAME_WF-02_ONBOARDING_SLIDE_1.png | HF_FRAME, PHASE9_ASSET | WF-02 | S-02 | UF-01 → GS-01 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-02/frames/HF_FRAME_WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE.png | HF_FRAME, PHASE9_ASSET | WF-02, WF-03 | CA-01, CA-02, S-02 | UF-01,UF-02 → GS-01,GS-02 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-02/states/HF_STATE_CA-01.png | DEDICATED_RENDER, PHASE9_ASSET | WF-02 | CA-01 | UF-01 → GS-01 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-02/states/HF_STATE_S-02.png | DEDICATED_RENDER, PHASE9_ASSET | WF-02 | S-02 | UF-01 → GS-01 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-03/HF-03_PERMISSIONS_RATIONALE_REQUEST.png | PHASE9_ASSET, PRIMARY_HF | WF-03 | CA-02, S-03, SYS-01, SYS-02 | UF-02 → GS-01,GS-02 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-03/frames/HF_FRAME_WF-03_PERMISSIONS_DENIED.png | HF_FRAME, PHASE9_ASSET, VARIANT_ANCHOR | WF-03, WF-08 | CA-02, CD-21, S-03 | UF-02,UF-10,UF-12 → GS-01,GS-02,GS-04,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-03/frames/HF_FRAME_WF-03_PERMISSIONS_GRANTED.png | HF_FRAME, PHASE9_ASSET | WF-03 | CA-02, S-03 | UF-02 → GS-01,GS-02 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-03/frames/HF_FRAME_WF-03_PERMISSIONS_PERMANENTLY_DENIED.png | HF_FRAME, PHASE9_ASSET | WF-03 | CA-02, S-03, SYS-04 | UF-02 → GS-01,GS-02 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-03/frames/HF_FRAME_WF-03_PERMISSIONS_RATIONALE_REQUEST.png | HF_FRAME, PHASE9_ASSET, VARIANT_ANCHOR | WF-03 | CA-02, S-03, SYS-01, SYS-02 | UF-02 → GS-01,GS-02 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-03/states/HF_STATE_S-03.png | DEDICATED_RENDER, PHASE9_ASSET | WF-03 | S-03 | UF-02 → GS-01,GS-02 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-04/HF-04_HOME_LIBRARY_READY.png | PHASE9_ASSET, PRIMARY_HF | WF-04, WF-06 | CB-01, CB-20, S-04 | UF-03,UF-04,UF-05 → GS-01,GS-02,GS-03,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-04/frames/HF_FRAME_WF-04_HOME_LIBRARY_EMPTY.png | HF_FRAME, PHASE9_ASSET | WF-04 | S-04 | UF-03 → GS-01,GS-02 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-04/frames/HF_FRAME_WF-04_HOME_LIBRARY_READY.png | HF_FRAME, PHASE9_ASSET, VARIANT_ANCHOR | WF-04, WF-06 | CB-01, CB-02, CB-03, CB-04, CB-05, CB-06, CB-07, CB-08, CB-20, S-04 | UF-03,UF-04,UF-05 → GS-01,GS-02,GS-03,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-04/frames/HF_FRAME_WF-04_HOME_LIBRARY_RECOVERABLE_ERROR.png | HF_FRAME, PHASE9_ASSET | WF-04 | S-04 | UF-03 → GS-01,GS-02 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-04/frames/HF_FRAME_WF-04_HOME_LIBRARY_SCANNING.png | HF_FRAME, PHASE9_ASSET | WF-04 | S-04 | UF-03 → GS-01,GS-02 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-04/states/HF_STATE_CB-01.png | DEDICATED_RENDER, PHASE9_ASSET | WF-04 | CB-01 | UF-04 → GS-02 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-04/states/HF_STATE_S-04.png | DEDICATED_RENDER, PHASE9_ASSET | WF-04 | S-04 | UF-03 → GS-01,GS-02 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-05/HF-05_SEARCH_RESULTS.png | PHASE9_ASSET, PRIMARY_HF | WF-04, WF-05 | CB-09, CB-10, CB-13 | UF-04 → GS-02 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-05/frames/HF_FRAME_WF-05_SEARCH_ERROR.png | HF_FRAME, PHASE9_ASSET | WF-05 | CB-09, CB-12 | UF-04 → GS-02 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-05/frames/HF_FRAME_WF-05_SEARCH_NO_RESULTS.png | HF_FRAME, PHASE9_ASSET | WF-05 | CB-09, CB-11 | UF-04 → GS-02 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-05/frames/HF_FRAME_WF-05_SEARCH_RESULTS.png | HF_FRAME, PHASE9_ASSET, VARIANT_ANCHOR | WF-04, WF-05 | CB-09, CB-10, CB-13 | UF-04 → GS-02 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-05/states/HF_STATE_CB-09.png | DEDICATED_RENDER, PHASE9_ASSET | WF-05 | CB-09 | UF-04 → GS-02 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-05/states/HF_STATE_CB-10.png | DEDICATED_RENDER, PHASE9_ASSET | WF-05 | CB-10 | UF-04 → GS-02 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-05/states/HF_STATE_CB-11.png | DEDICATED_RENDER, PHASE9_ASSET | WF-05 | CB-11 | UF-04 → GS-02 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-05/states/HF_STATE_CB-12.png | DEDICATED_RENDER, PHASE9_ASSET | WF-05 | CB-12 | UF-04 → GS-02 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-06/HF-06_FULL_PLAYER_PLAYING.png | PHASE9_ASSET, PRIMARY_HF | WF-06 | CB-15 | UF-05 → GS-02,GS-03,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY.png | HF_FRAME, PHASE9_ASSET, VARIANT_ANCHOR | WF-06, WF-07 | CB-19, CE-12, CE-13, CE-14 | UF-05,UF-11 → GS-02,GS-03,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PAUSED.png | HF_FRAME, PHASE9_ASSET, VARIANT_ANCHOR | WF-06, WF-07 | CB-16, CB-32, CB-33, CE-03, CE-06, CE-11 | UF-05,UF-06,UF-11 → GS-02,GS-03,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-06/frames/HF_FRAME_WF-06_FULL_PLAYER_PLAYING.png | HF_FRAME, PHASE9_ASSET, VARIANT_ANCHOR | WF-06, WF-07, WF-09 | CB-14, CB-15, CB-21, CB-31, CD-01, CE-01, CE-02, CE-04, CE-05, UL-09, UL-10, UL-11 | UF-04,UF-05,UF-06,UF-08,UF-10,UF-11,UF-13 → GS-02,GS-03,GS-04,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-06/states/HF_STATE_CB-15.png | DEDICATED_RENDER, PHASE9_ASSET | WF-06 | CB-15 | UF-05 → GS-02,GS-03,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-06/states/HF_STATE_CB-16.png | DEDICATED_RENDER, PHASE9_ASSET | WF-06 | CB-16 | UF-05 → GS-02,GS-03,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-06/states/HF_STATE_CB-17.png | DEDICATED_RENDER, PHASE9_ASSET | WF-06 | CB-17 | UF-05 → GS-02,GS-03,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-06/states/HF_STATE_CB-18.png | DEDICATED_RENDER, PHASE9_ASSET | WF-06 | CB-18 | UF-05 → GS-02,GS-03,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-07/HF-07_DRIVING_MODE_PLAYING.png | PHASE9_ASSET, PRIMARY_HF | WF-07, WF-08 | CC-05, CC-14, CC-16 | UF-07,UF-08,UF-11 → GS-04,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-07/frames/HF_FRAME_WF-07_DRIVING_MODE_NO_TRACK.png | HF_FRAME, PHASE9_ASSET, VARIANT_ANCHOR | WF-07 | CC-04, CC-07, CC-15 | UF-07 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-07/frames/HF_FRAME_WF-07_DRIVING_MODE_PAUSED.png | HF_FRAME, PHASE9_ASSET, VARIANT_ANCHOR | WF-07 | CC-06, CC-09, CC-16 | UF-07,UF-11 → GS-04,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-07/frames/HF_FRAME_WF-07_DRIVING_MODE_PLAYING.png | HF_FRAME, PHASE9_ASSET, VARIANT_ANCHOR | WF-07, WF-08 | CC-01, CC-02, CC-03, CC-05, CC-08, CC-10, CC-12, CC-13, CC-14, CC-16, CC-17, CC-18 | UF-07,UF-08,UF-11 → GS-04,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-07/states/HF_STATE_CC-05.png | DEDICATED_RENDER, PHASE9_ASSET | WF-07 | CC-05 | UF-07 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-07/states/HF_STATE_CC-06.png | DEDICATED_RENDER, PHASE9_ASSET | WF-07 | CC-06 | UF-07 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-07/states/HF_STATE_CC-07.png | DEDICATED_RENDER, PHASE9_ASSET | WF-07 | CC-07 | UF-07 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-07/states/HF_STATE_CC-11.png | DEDICATED_RENDER, PHASE9_ASSET | WF-07 | CC-11 | UF-07,UF-12 → GS-04,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/HF-08_VOICE_LISTENING.png | PHASE9_ASSET, PRIMARY_HF | WF-08 | CC-14, CD-03 | UF-07,UF-08 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/frames/HF_FRAME_WF-08_VOICE_CONFIRMING.png | HF_FRAME, PHASE9_ASSET | WF-08 | CD-09, CD-10 | UF-09 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/frames/HF_FRAME_WF-08_VOICE_EXECUTED.png | HF_FRAME, PHASE9_ASSET | WF-08 | CD-07 | UF-08 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/frames/HF_FRAME_WF-08_VOICE_FAILED_RETRY.png | HF_FRAME, PHASE9_ASSET, VARIANT_ANCHOR | WF-08, WF-10 | CD-08, CD-17, CD-18, CD-19, CD-20, CD-22 | UF-08,UF-09,UF-10,UF-12 → GS-04,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/frames/HF_FRAME_WF-08_VOICE_LISTENING.png | HF_FRAME, PHASE9_ASSET, VARIANT_ANCHOR | WF-08 | CC-14, CD-02, CD-03, CD-24 | UF-07,UF-08,UF-09,UF-10 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/frames/HF_FRAME_WF-08_VOICE_PROCESSING.png | HF_FRAME, PHASE9_ASSET | WF-08 | CD-05 | UF-08 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/states/HF_STATE_CC-14.png | DEDICATED_RENDER, PHASE9_ASSET | WF-08 | CC-14 | UF-07,UF-08 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/states/HF_STATE_CD-03.png | DEDICATED_RENDER, PHASE9_ASSET | WF-08 | CD-03 | UF-08 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/states/HF_STATE_CD-04.png | DEDICATED_RENDER, PHASE9_ASSET | WF-08 | CD-04 | UF-10 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/states/HF_STATE_CD-05.png | DEDICATED_RENDER, PHASE9_ASSET | WF-08 | CD-05 | UF-08 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/states/HF_STATE_CD-06.png | DEDICATED_RENDER, PHASE9_ASSET | WF-08 | CD-06 | UF-08 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/states/HF_STATE_CD-07.png | DEDICATED_RENDER, PHASE9_ASSET | WF-08 | CD-07 | UF-08 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/states/HF_STATE_CD-08.png | DEDICATED_RENDER, PHASE9_ASSET | WF-08 | CD-08 | UF-08 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/states/HF_STATE_CD-09.png | DEDICATED_RENDER, PHASE9_ASSET | WF-08 | CD-09 | UF-09 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/states/HF_STATE_CD-10.png | DEDICATED_RENDER, PHASE9_ASSET | WF-08 | CD-10 | UF-09 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/states/HF_STATE_CD-11.png | DEDICATED_RENDER, PHASE9_ASSET | WF-08 | CD-11 | UF-09 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/states/HF_STATE_CD-12.png | DEDICATED_RENDER, PHASE9_ASSET | WF-08 | CD-12 | UF-09 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/states/HF_STATE_CD-13.png | DEDICATED_RENDER, PHASE9_ASSET | WF-08 | CD-13 | UF-09 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/states/HF_STATE_CD-14.png | DEDICATED_RENDER, PHASE9_ASSET | WF-08 | CD-14 | UF-09 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/states/HF_STATE_CD-15.png | DEDICATED_RENDER, PHASE9_ASSET | WF-08 | CD-15 | UF-09 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/states/HF_STATE_CD-16.png | DEDICATED_RENDER, PHASE9_ASSET | WF-08 | CD-16 | UF-09 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/states/HF_STATE_CD-17.png | DEDICATED_RENDER, PHASE9_ASSET | WF-08 | CD-17 | UF-10 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/states/HF_STATE_CD-18.png | DEDICATED_RENDER, PHASE9_ASSET | WF-08 | CD-18 | UF-10 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-08/states/HF_STATE_CD-19.png | DEDICATED_RENDER, PHASE9_ASSET | WF-08 | CD-19 | UF-10 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-09/HF-09_LIKED_SONGS_READY.png | PHASE9_ASSET, PRIMARY_HF | WF-06, WF-09 | UL-04, UL-09, UL-10, UL-12 | OPTIONAL P2 — FR-09, no mandatory GS | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-09/frames/HF_FRAME_WF-09_LIKED_SONGS_EMPTY.png | HF_FRAME, PHASE9_ASSET, VARIANT_ANCHOR | WF-09 | UL-01, UL-02, UL-03, UL-12, UL-13, UL-14 | OPTIONAL P2 — FR-09, no mandatory GS | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-09/frames/HF_FRAME_WF-09_LIKED_SONGS_READY.png | HF_FRAME, PHASE9_ASSET, VARIANT_ANCHOR | WF-06, WF-09 | UL-04, UL-05, UL-06, UL-07, UL-08, UL-09, UL-10, UL-12 | OPTIONAL P2 — FR-09, no mandatory GS | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-09/states/HF_STATE_UL-02.png | DEDICATED_RENDER, PHASE9_ASSET | WF-09 | UL-02 | OPTIONAL P2 — FR-09, no mandatory GS | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-09/states/HF_STATE_UL-03.png | DEDICATED_RENDER, PHASE9_ASSET | WF-09 | UL-03 | OPTIONAL P2 — FR-09, no mandatory GS | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-09/states/HF_STATE_UL-04.png | DEDICATED_RENDER, PHASE9_ASSET | WF-09 | UL-04 | OPTIONAL P2 — FR-09, no mandatory GS | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-10/HF-10_SETTINGS_DEFAULT.png | PHASE9_ASSET, PRIMARY_HF | WF-10 | CE-15, CE-21, CE-22 | UF-12 → GS-04,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-10/frames/HF_FRAME_WF-10_SETTINGS_BLUETOOTH_UPDATED.png | HF_FRAME, PHASE9_ASSET, VARIANT_ANCHOR | WF-10 | CE-16, CE-17, CE-20 | UF-12 → GS-04,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-10/frames/HF_FRAME_WF-10_SETTINGS_CLEARED_PRIVACY_STATUS.png | HF_FRAME, PHASE9_ASSET, VARIANT_ANCHOR | WF-10 | CE-21, CE-25, CE-26 | UF-12 → GS-04,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-10/frames/HF_FRAME_WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION.png | HF_FRAME, PHASE9_ASSET, VARIANT_ANCHOR | WF-10 | CE-23, CE-24, CE-25 | UF-12 → GS-04,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-10/frames/HF_FRAME_WF-10_SETTINGS_DEFAULT.png | HF_FRAME, PHASE9_ASSET, VARIANT_ANCHOR | WF-10 | CE-15, CE-16, CE-19, CE-21, CE-22 | UF-12 → GS-04,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-10/states/HF_STATE_CD-23.png | DEDICATED_RENDER, PHASE9_ASSET | WF-10 | CD-23 | UF-10,UF-12 → GS-04,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-10/states/HF_STATE_CE-15.png | DEDICATED_RENDER, PHASE9_ASSET | WF-10 | CE-15 | UF-12 → GS-04,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-10/states/HF_STATE_CE-20.png | DEDICATED_RENDER, PHASE9_ASSET | WF-10 | CE-20 | UF-12 → GS-04,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-10/states/HF_STATE_CE-21.png | DEDICATED_RENDER, PHASE9_ASSET | WF-10 | CE-21 | UF-12 → GS-04,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-10/states/HF_STATE_CE-22.png | DEDICATED_RENDER, PHASE9_ASSET | WF-10 | CE-22 | UF-12 → GS-04,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/WF-10/states/HF_STATE_CE-23.png | DEDICATED_RENDER, PHASE9_ASSET | WF-10 | CE-23 | UF-12 → GS-04,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/DRV_COMMAND_PENDING.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-07 | CC-08 | UF-07 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/DRV_LAST_VERIFIED.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-07 | CC-07 | UF-07 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/DRV_NO_TRACK.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-07 | CC-07 | UF-07 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/DRV_PAUSED.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-07 | CC-06 | UF-07 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/DRV_PLAYING.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-07 | CC-05 | UF-07 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/DRV_RECONNECT.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-07 | CC-03 | UF-07 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/DRV_ROUTE_SAFE.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-06, WF-07 | CE-06 | UF-11 → GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/PHASE_6_SPECIALIST_BOARD.png | PHASE9_ASSET | aggregate | no independent State | Supporting QA/contact board; not a new State | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/VO_ARTIST_NOT_FOUND.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-08 | CD-13 | UF-09 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/VO_ATTEMPT2_TERMINAL.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-08 | CD-19 | UF-10 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/VO_COMMAND_PENDING.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-08 | CD-06 | UF-08 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/VO_CONFIRMATION_FAILED.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-08 | CD-12 | UF-09 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/VO_CONFIRM_ONE.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-08 | CD-09 | UF-09 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/VO_CONFIRM_TWO.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-08 | CD-10 | UF-09 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/VO_EXECUTED.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-08 | CD-07 | UF-08 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/VO_EXECUTION_REJECTED.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-08 | CD-08 | UF-08 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/VO_LISTEN_1.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-08 | CD-03 | UF-08 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/VO_LISTEN_2.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-08 | CD-04 | UF-10 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/VO_MANUAL_ONLY.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-08 | CD-20 | UF-08,UF-09,UF-10 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/VO_MISSING_ARTIST.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-08 | CD-16 | UF-09 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/VO_PERMISSION_DENIED.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-03, WF-08 | CD-21 | UF-10,UF-12 → GS-04,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/VO_PROCESSING.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-08 | CD-05 | UF-08 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/VO_PROVIDER_UNAVAILABLE.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-08, WF-10 | CD-22 | UF-10,UF-12 → GS-04,GS-05 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/VO_RETRY_AVAILABLE.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-08 | CD-18 | UF-10 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/VO_STT_TIMEOUT.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-08 | CD-17 | UF-10 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/VO_TTS_UNAVAILABLE.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-08 | CD-07 | UF-08 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/VO_UNKNOWN_PHRASE.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-08 | CD-14 | UF-09 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
| docs/high-fidelity/specialist/phase-6/VO_UNSUPPORTED.png | PHASE9_ASSET, SPECIALIST_RENDER | WF-08 | CD-15 | UF-09 → GS-04 | docs/PRD.md + docs/MVP.md + docs/SOFTWARE_ARCHITECTURE.md |
