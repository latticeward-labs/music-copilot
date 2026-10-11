# Issue #17 Phase 10 — HF > WF > State > UF > GS > PRD/MVP > ADR/Architecture

English frozen source: 0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8

Direct GS means a literal State source link. UF bridge is a supporting Scenario/Recovery trace, NOT a new GS. P2 UF-13 / FR-09 is optional, GS intentionally N/A.

| State | Priority | WF | UF | Direct GS | Supporting UF-GS | PRD FR | MVP US | Source spec |
|---|---|---|---|---|---|---|---|---|
| S-01 | P0 | WF-01 | UF-01 | GS-01 | GS-01 | FR-01 | US-01,US-02 | docs/high-fidelity/WF-01/states/HF_STATE_S-01.md |
| S-02 | P0 | WF-02 | UF-01 | GS-01 | GS-01 | FR-01 | US-01,US-02 | docs/high-fidelity/WF-02/states/HF_STATE_S-02.md |
| S-03 | P0 | WF-03 | UF-02 | GS-01 | GS-01,GS-02 | FR-01,FR-05 | US-01 | docs/high-fidelity/WF-03/states/HF_STATE_S-03.md |
| S-04 | P0 | WF-04 | UF-03 | GS-01 | GS-01,GS-02 | FR-01 | US-02 | docs/high-fidelity/WF-04/states/HF_STATE_S-04.md |
| CA-01 | P0 | WF-02 | UF-01 | GS-01 | GS-01 | FR-01 | US-01,US-02 | docs/high-fidelity/WF-02/states/HF_STATE_CA-01.md |
| CA-02 | P0 | WF-03 | UF-02 | GS-01 | GS-01,GS-02 | FR-01,FR-05 | US-01 | docs/high-fidelity/state-variants/CA-02.md |
| SYS-01 | P0 | WF-03 | UF-02 | GS-01 | GS-01,GS-02 | FR-01,FR-05 | US-01 | docs/high-fidelity/system-handoffs/SYS-01.md |
| SYS-02 | P0 | WF-03 | UF-02 | not direct | GS-01,GS-02 | FR-01,FR-05 | US-01 | docs/high-fidelity/system-handoffs/SYS-02.md |
| SYS-03 | P0 | WF-06 | UF-06 | not direct | GS-03 | FR-02,FR-08 | US-03 | docs/high-fidelity/system-handoffs/SYS-03.md |
| SYS-04 | P0 | WF-03 | UF-02 | GS-01 | GS-01,GS-02 | FR-01,FR-05 | US-01 | docs/high-fidelity/system-handoffs/SYS-04.md |
| SYS-05 | P0 | WF-03 | UF-02,UF-10,UF-12 | GS-01 | GS-01,GS-02,GS-04,GS-05 | FR-01,FR-05,FR-06 | US-01,US-07 | docs/high-fidelity/system-handoffs/SYS-05.md |
| CB-01 | P0 | WF-04 | UF-04 | GS-01,GS-02 | GS-02 | FR-01 | US-02 | docs/high-fidelity/WF-04/states/HF_STATE_CB-01.md |
| CB-02 | P0 | WF-04 | UF-04 | GS-02 | GS-02 | FR-01 | US-02 | docs/high-fidelity/state-variants/CB-02.md |
| CB-03 | P0 | WF-04 | UF-04 | not direct | GS-02 | FR-01 | US-02 | docs/high-fidelity/state-variants/CB-03.md |
| CB-04 | P0 | WF-04 | UF-04 | not direct | GS-02 | FR-01 | US-02 | docs/high-fidelity/state-variants/CB-04.md |
| CB-05 | P0 | WF-04 | UF-04 | not direct | GS-02 | FR-01 | US-02 | docs/high-fidelity/state-variants/CB-05.md |
| CB-06 | P0 | WF-04 | UF-04 | not direct | GS-02 | FR-01 | US-02 | docs/high-fidelity/state-variants/CB-06.md |
| CB-07 | P0 | WF-04 | UF-04 | not direct | GS-02 | FR-01 | US-02 | docs/high-fidelity/state-variants/CB-07.md |
| CB-08 | P0 | WF-04 | UF-04 | not direct | GS-02 | FR-01 | US-02 | docs/high-fidelity/state-variants/CB-08.md |
| CB-09 | P0 | WF-05 | UF-04 | not direct | GS-02 | FR-01 | US-02 | docs/high-fidelity/WF-05/states/HF_STATE_CB-09.md |
| CB-10 | P0 | WF-05 | UF-04 | not direct | GS-02 | FR-01 | US-02 | docs/high-fidelity/WF-05/states/HF_STATE_CB-10.md |
| CB-11 | P0 | WF-05 | UF-04 | GS-02 | GS-02 | FR-01 | US-02 | docs/high-fidelity/WF-05/states/HF_STATE_CB-11.md |
| CB-12 | P0 | WF-05 | UF-04 | GS-02 | GS-02 | FR-01 | US-02 | docs/high-fidelity/WF-05/states/HF_STATE_CB-12.md |
| CB-13 | P0 | WF-04,WF-05 | UF-04 | GS-02 | GS-02 | FR-01 | US-02 | docs/high-fidelity/state-variants/CB-13.md |
| CB-14 | P0 | WF-06 | UF-04,UF-05 | GS-02 | GS-02,GS-03,GS-05 | FR-01,FR-02 | US-02,US-03 | docs/high-fidelity/state-variants/CB-14.md |
| CB-15 | P0 | WF-06 | UF-05 | GS-02,GS-03 | GS-02,GS-03,GS-05 | FR-02 | US-02,US-03 | docs/high-fidelity/WF-06/states/HF_STATE_CB-15.md |
| CB-16 | P0 | WF-06 | UF-05 | GS-03 | GS-02,GS-03,GS-05 | FR-02 | US-02,US-03 | docs/high-fidelity/WF-06/states/HF_STATE_CB-16.md |
| CB-17 | P0 | WF-06 | UF-05 | not direct | GS-02,GS-03,GS-05 | FR-02 | US-02,US-03 | docs/high-fidelity/WF-06/states/HF_STATE_CB-17.md |
| CB-18 | P0 | WF-06 | UF-05 | not direct | GS-02,GS-03,GS-05 | FR-02 | US-02,US-03 | docs/high-fidelity/WF-06/states/HF_STATE_CB-18.md |
| CB-19 | P0 | WF-06 | UF-05 | GS-02 | GS-02,GS-03,GS-05 | FR-02 | US-02,US-03 | docs/high-fidelity/state-variants/CB-19.md |
| CB-20 | P0 | WF-04,WF-06 | UF-05 | not direct | GS-02,GS-03,GS-05 | FR-02 | US-02,US-03 | docs/high-fidelity/state-variants/CB-20.md |
| CB-21 | P0 | WF-06 | UF-05,UF-06 | GS-02,GS-03 | GS-02,GS-03,GS-05 | FR-02,FR-08 | US-02,US-03 | docs/high-fidelity/state-variants/CB-21.md |
| CB-22 | P0 | WF-06 | UF-06 | GS-03 | GS-03 | FR-02,FR-08 | US-03 | docs/high-fidelity/system-handoffs/CB-22.md |
| CB-23 | P0 | WF-06 | UF-06 | GS-03 | GS-03 | FR-02,FR-08 | US-03 | docs/high-fidelity/system-handoffs/CB-23.md |
| CB-24 | P0 | WF-06 | UF-06 | GS-03 | GS-03 | FR-02,FR-08 | US-03 | docs/high-fidelity/system-handoffs/CB-24.md |
| CB-25 | P0 | WF-06 | UF-06 | GS-03 | GS-03 | FR-02,FR-08 | US-03 | docs/high-fidelity/system-handoffs/CB-25.md |
| CB-26 | P0 | WF-06 | UF-06 | GS-03 | GS-03 | FR-02,FR-08 | US-03 | docs/high-fidelity/system-handoffs/CB-26.md |
| CB-27 | P0 | WF-06 | UF-06 | not direct | GS-03 | FR-02,FR-08 | US-03 | docs/high-fidelity/system-handoffs/CB-27.md |
| CB-28 | P0 | WF-06 | UF-06 | not direct | GS-03 | FR-02,FR-08 | US-03 | docs/high-fidelity/system-handoffs/CB-28.md |
| CB-29 | P0 | WF-06 | UF-06 | GS-03 | GS-03 | FR-02,FR-08 | US-03 | docs/high-fidelity/system-handoffs/CB-29.md |
| CB-30 | P0 | WF-06 | UF-06 | GS-03 | GS-03 | FR-02,FR-08 | US-03 | docs/high-fidelity/system-handoffs/CB-30.md |
| CB-31 | P0 | WF-06 | UF-06 | not direct | GS-03 | FR-02,FR-08 | US-03 | docs/high-fidelity/state-variants/CB-31.md |
| CB-32 | P1 | WF-06 | UF-05,UF-06 | GS-03 | GS-02,GS-03,GS-05 | FR-02,FR-08 | US-02,US-03 | docs/high-fidelity/state-variants/CB-32.md |
| CB-33 | P1 | WF-06 | UF-05,UF-06 | GS-03 | GS-02,GS-03,GS-05 | FR-02,FR-08 | US-02,US-03 | docs/high-fidelity/state-variants/CB-33.md |
| CC-01 | P0 | WF-07 | UF-07 | GS-04 | GS-04 | FR-04,FR-07 | US-04 | docs/high-fidelity/state-variants/CC-01.md |
| CC-02 | P0 | WF-07 | UF-07 | GS-04 | GS-04 | FR-04,FR-07 | US-04 | docs/high-fidelity/state-variants/CC-02.md |
| CC-03 | P0 | WF-07 | UF-07 | GS-04 | GS-04 | FR-04,FR-07 | US-04 | docs/high-fidelity/state-variants/CC-03.md |
| CC-04 | P0 | WF-07 | UF-07 | not direct | GS-04 | FR-04,FR-07 | US-04 | docs/high-fidelity/state-variants/CC-04.md |
| CC-05 | P0 | WF-07 | UF-07 | GS-04 | GS-04 | FR-04,FR-07 | US-04 | docs/high-fidelity/WF-07/states/HF_STATE_CC-05.md |
| CC-06 | P0 | WF-07 | UF-07 | GS-04 | GS-04 | FR-04,FR-07 | US-04 | docs/high-fidelity/WF-07/states/HF_STATE_CC-06.md |
| CC-07 | P0 | WF-07 | UF-07 | GS-04 | GS-04 | FR-04,FR-07 | US-04 | docs/high-fidelity/WF-07/states/HF_STATE_CC-07.md |
| CC-08 | P0 | WF-07 | UF-07 | not direct | GS-04 | FR-04,FR-07 | US-04 | docs/high-fidelity/state-variants/CC-08.md |
| CC-09 | P0 | WF-07 | UF-07 | not direct | GS-04 | FR-04,FR-07 | US-04 | docs/high-fidelity/state-variants/CC-09.md |
| CC-10 | P0 | WF-07 | UF-07 | not direct | GS-04 | FR-04,FR-07 | US-04 | docs/high-fidelity/state-variants/CC-10.md |
| CC-11 | P1 | WF-07 | UF-07,UF-12 | not direct | GS-04,GS-05 | FR-04,FR-07 | US-04 | docs/high-fidelity/WF-07/states/HF_STATE_CC-11.md |
| CC-12 | P1 | WF-07 | UF-07 | not direct | GS-04 | FR-04,FR-07 | US-04 | docs/high-fidelity/state-variants/CC-12.md |
| CC-13 | P0 | WF-07 | UF-07 | not direct | GS-04 | FR-04,FR-07 | US-04 | docs/high-fidelity/state-variants/CC-13.md |
| CC-14 | P0 | WF-08 | UF-07,UF-08 | GS-04 | GS-04 | FR-04,FR-05,FR-06,FR-07 | US-04,US-05 | docs/high-fidelity/WF-08/states/HF_STATE_CC-14.md |
| CC-15 | P0 | WF-07 | UF-07 | not direct | GS-04 | FR-04,FR-07 | US-04 | docs/high-fidelity/state-variants/CC-15.md |
| CC-16 | P0 | WF-07 | UF-07,UF-11 | GS-04,GS-05 | GS-04,GS-05 | FR-03,FR-04,FR-07 | US-03,US-04 | docs/high-fidelity/system-handoffs/CC-16.md |
| CC-17 | P1 | WF-07 | UF-07 | not direct | GS-04 | FR-04,FR-07 | US-04 | docs/high-fidelity/state-variants/CC-17.md |
| CC-18 | P0 | WF-07 | UF-07 | GS-04 | GS-04 | FR-04,FR-07 | US-04 | docs/high-fidelity/state-variants/CC-18.md |
| CD-01 | P0 | WF-07,WF-06 | UF-08,UF-10 | GS-04 | GS-04 | FR-05,FR-06 | US-05,US-07 | docs/high-fidelity/state-variants/CD-01.md |
| CD-02 | P0 | WF-08 | UF-08,UF-10 | GS-04 | GS-04 | FR-05,FR-06 | US-05,US-07 | docs/high-fidelity/state-variants/CD-02.md |
| CD-03 | P0 | WF-08 | UF-08 | GS-04 | GS-04 | FR-05,FR-06 | US-05 | docs/high-fidelity/WF-08/states/HF_STATE_CD-03.md |
| CD-04 | P0 | WF-08 | UF-10 | not direct | GS-04 | FR-05,FR-06 | US-07 | docs/high-fidelity/WF-08/states/HF_STATE_CD-04.md |
| CD-05 | P0 | WF-08 | UF-08 | GS-04 | GS-04 | FR-05,FR-06 | US-05 | docs/high-fidelity/WF-08/states/HF_STATE_CD-05.md |
| CD-06 | P0 | WF-08 | UF-08 | GS-04 | GS-04 | FR-05,FR-06 | US-05 | docs/high-fidelity/WF-08/states/HF_STATE_CD-06.md |
| CD-07 | P0 | WF-08 | UF-08 | GS-04 | GS-04 | FR-05,FR-06 | US-05 | docs/high-fidelity/WF-08/states/HF_STATE_CD-07.md |
| CD-08 | P0 | WF-08 | UF-08 | not direct | GS-04 | FR-05,FR-06 | US-05 | docs/high-fidelity/WF-08/states/HF_STATE_CD-08.md |
| CD-09 | P0 | WF-08 | UF-09 | not direct | GS-04 | FR-06 | US-06 | docs/high-fidelity/WF-08/states/HF_STATE_CD-09.md |
| CD-10 | P0 | WF-08 | UF-09 | not direct | GS-04 | FR-06 | US-06 | docs/high-fidelity/WF-08/states/HF_STATE_CD-10.md |
| CD-11 | P0 | WF-08 | UF-09 | not direct | GS-04 | FR-06 | US-06 | docs/high-fidelity/WF-08/states/HF_STATE_CD-11.md |
| CD-12 | P0 | WF-08 | UF-09 | not direct | GS-04 | FR-06 | US-06 | docs/high-fidelity/WF-08/states/HF_STATE_CD-12.md |
| CD-13 | P0 | WF-08 | UF-09 | not direct | GS-04 | FR-06 | US-06 | docs/high-fidelity/WF-08/states/HF_STATE_CD-13.md |
| CD-14 | P0 | WF-08 | UF-09 | not direct | GS-04 | FR-06 | US-06 | docs/high-fidelity/WF-08/states/HF_STATE_CD-14.md |
| CD-15 | P0 | WF-08 | UF-09 | not direct | GS-04 | FR-06 | US-06 | docs/high-fidelity/WF-08/states/HF_STATE_CD-15.md |
| CD-16 | P0 | WF-08 | UF-09 | not direct | GS-04 | FR-06 | US-06 | docs/high-fidelity/WF-08/states/HF_STATE_CD-16.md |
| CD-17 | P0 | WF-08 | UF-10 | not direct | GS-04 | FR-05,FR-06 | US-07 | docs/high-fidelity/WF-08/states/HF_STATE_CD-17.md |
| CD-18 | P0 | WF-08 | UF-10 | not direct | GS-04 | FR-05,FR-06 | US-07 | docs/high-fidelity/WF-08/states/HF_STATE_CD-18.md |
| CD-19 | P0 | WF-08 | UF-10 | not direct | GS-04 | FR-05,FR-06 | US-07 | docs/high-fidelity/WF-08/states/HF_STATE_CD-19.md |
| CD-20 | P0 | WF-08 | UF-08,UF-09,UF-10 | not direct | GS-04 | FR-05,FR-06 | US-05,US-06,US-07 | docs/high-fidelity/state-variants/CD-20.md |
| CD-21 | P0 | WF-03,WF-08 | UF-10,UF-12 | GS-04 | GS-04,GS-05 | FR-05,FR-06 | US-07 | docs/high-fidelity/state-variants/CD-21.md |
| CD-22 | P0 | WF-08,WF-10 | UF-10,UF-12 | GS-04 | GS-04,GS-05 | FR-05,FR-06 | US-07 | docs/high-fidelity/state-variants/CD-22.md |
| CD-23 | P0 | WF-10 | UF-10,UF-12 | not direct | GS-04,GS-05 | FR-05,FR-06 | US-07 | docs/high-fidelity/WF-10/states/HF_STATE_CD-23.md |
| CD-24 | P0 | WF-08 | UF-08,UF-09,UF-10 | GS-04 | GS-04 | FR-05,FR-06 | US-05,US-06,US-07 | docs/high-fidelity/state-variants/CD-24.md |
| CE-01 | P0 | WF-06,WF-07 | UF-11 | GS-05 | GS-05 | FR-03 | US-03 | docs/high-fidelity/state-variants/CE-01.md |
| CE-02 | P0 | WF-06,WF-07 | UF-11 | GS-05 | GS-05 | FR-03 | US-03 | docs/high-fidelity/state-variants/CE-02.md |
| CE-03 | P0 | WF-06,WF-07 | UF-11 | GS-05 | GS-05 | FR-03 | US-03 | docs/high-fidelity/state-variants/CE-03.md |
| CE-04 | P0 | WF-06,WF-07 | UF-11 | GS-05 | GS-05 | FR-03 | US-03 | docs/high-fidelity/state-variants/CE-04.md |
| CE-05 | P0 | WF-06,WF-07 | UF-11 | GS-05 | GS-05 | FR-03 | US-03 | docs/high-fidelity/state-variants/CE-05.md |
| CE-06 | P0 | WF-06,WF-07 | UF-11 | GS-05 | GS-05 | FR-03 | US-03 | docs/high-fidelity/state-variants/CE-06.md |
| CE-07 | P0 | WF-06,WF-07 | UF-11 | GS-05 | GS-05 | FR-03 | US-03 | docs/high-fidelity/system-handoffs/CE-07.md |
| CE-08 | P0 | WF-06,WF-07 | UF-11 | GS-05 | GS-05 | FR-03 | US-03 | docs/high-fidelity/system-handoffs/CE-08.md |
| CE-09 | P0 | WF-06,WF-07 | UF-11 | GS-05 | GS-05 | FR-03 | US-03 | docs/high-fidelity/system-handoffs/CE-09.md |
| CE-10 | P0 | WF-06,WF-07 | UF-11 | GS-05 | GS-05 | FR-03 | US-03 | docs/high-fidelity/system-handoffs/CE-10.md |
| CE-11 | P0 | WF-06,WF-07 | UF-11 | GS-05 | GS-05 | FR-03 | US-03 | docs/high-fidelity/state-variants/CE-11.md |
| CE-12 | P0 | WF-06,WF-07 | UF-11 | GS-05 | GS-05 | FR-03 | US-03 | docs/high-fidelity/state-variants/CE-12.md |
| CE-13 | P0 | WF-06,WF-07 | UF-11 | not direct | GS-05 | FR-03 | US-03 | docs/high-fidelity/state-variants/CE-13.md |
| CE-14 | P0 | WF-06,WF-07 | UF-11 | GS-05 | GS-05 | FR-03 | US-03 | docs/high-fidelity/state-variants/CE-14.md |
| CE-15 | P0 | WF-10 | UF-12 | not direct | GS-04,GS-05 | PRD section 9 | MVP section 3/7 | docs/high-fidelity/WF-10/states/HF_STATE_CE-15.md |
| CE-16 | P0 | WF-10 | UF-12 | not direct | GS-04,GS-05 | PRD section 9 | MVP section 3/7 | docs/high-fidelity/state-variants/CE-16.md |
| CE-17 | P0 | WF-10 | UF-12 | not direct | GS-04,GS-05 | PRD section 9 | MVP section 3/7 | docs/high-fidelity/state-variants/CE-17.md |
| CE-18 | P0 | WF-10 | UF-12 | GS-05 | GS-04,GS-05 | PRD section 9 | MVP section 3/7 | docs/high-fidelity/system-handoffs/CE-18.md |
| CE-19 | P0 | WF-10 | UF-12 | not direct | GS-04,GS-05 | PRD section 9 | MVP section 3/7 | docs/high-fidelity/state-variants/CE-19.md |
| CE-20 | P1 | WF-10 | UF-12 | not direct | GS-04,GS-05 | PRD section 9 | MVP section 3/7 | docs/high-fidelity/WF-10/states/HF_STATE_CE-20.md |
| CE-21 | P0 | WF-10 | UF-12 | not direct | GS-04,GS-05 | PRD section 9 | MVP section 3/7 | docs/high-fidelity/WF-10/states/HF_STATE_CE-21.md |
| CE-22 | P0 | WF-10 | UF-12 | not direct | GS-04,GS-05 | PRD section 9 | MVP section 3/7 | docs/high-fidelity/WF-10/states/HF_STATE_CE-22.md |
| CE-23 | P0 | WF-10 | UF-12 | not direct | GS-04,GS-05 | PRD section 9 | MVP section 3/7 | docs/high-fidelity/WF-10/states/HF_STATE_CE-23.md |
| CE-24 | P0 | WF-10 | UF-12 | not direct | GS-04,GS-05 | PRD section 9 | MVP section 3/7 | docs/high-fidelity/state-variants/CE-24.md |
| CE-25 | P0 | WF-10 | UF-12 | not direct | GS-04,GS-05 | PRD section 9 | MVP section 3/7 | docs/high-fidelity/state-variants/CE-25.md |
| CE-26 | P0 | WF-10 | UF-12 | not direct | GS-04,GS-05 | PRD section 9 | MVP section 3/7 | docs/high-fidelity/state-variants/CE-26.md |
| UL-01 | P2 | WF-09 | UF-13 | not direct | N/A P2 | FR-09 | MVP section 3/7 | docs/high-fidelity/state-variants/UL-01.md |
| UL-02 | P2 | WF-09 | UF-13 | not direct | N/A P2 | FR-09 | MVP section 3/7 | docs/high-fidelity/WF-09/states/HF_STATE_UL-02.md |
| UL-03 | P2 | WF-09 | UF-13 | not direct | N/A P2 | FR-09 | MVP section 3/7 | docs/high-fidelity/WF-09/states/HF_STATE_UL-03.md |
| UL-04 | P2 | WF-09 | UF-13 | not direct | N/A P2 | FR-09 | MVP section 3/7 | docs/high-fidelity/WF-09/states/HF_STATE_UL-04.md |
| UL-05 | P2 | WF-06,WF-09 | UF-13 | not direct | N/A P2 | FR-09 | MVP section 3/7 | docs/high-fidelity/state-variants/UL-05.md |
| UL-06 | P2 | WF-06,WF-09 | UF-13 | not direct | N/A P2 | FR-09 | MVP section 3/7 | docs/high-fidelity/state-variants/UL-06.md |
| UL-07 | P2 | WF-06,WF-09 | UF-13 | not direct | N/A P2 | FR-09 | MVP section 3/7 | docs/high-fidelity/state-variants/UL-07.md |
| UL-08 | P2 | WF-06,WF-09 | UF-13 | not direct | N/A P2 | FR-09 | MVP section 3/7 | docs/high-fidelity/state-variants/UL-08.md |
| UL-09 | P2 | WF-09,WF-06 | UF-05,UF-13 | not direct | N/A P2 | FR-09 | MVP section 3/7 | docs/high-fidelity/state-variants/UL-09.md |
| UL-10 | P2 | WF-09,WF-06 | UF-05,UF-13 | not direct | N/A P2 | FR-09 | MVP section 3/7 | docs/high-fidelity/state-variants/UL-10.md |
| UL-11 | P2 | WF-09,WF-06 | UF-05,UF-13 | not direct | N/A P2 | FR-09 | MVP section 3/7 | docs/high-fidelity/state-variants/UL-11.md |
| UL-12 | P2 | WF-09 | UF-13 | not direct | N/A P2 | FR-09 | MVP section 3/7 | docs/high-fidelity/state-variants/UL-12.md |
| UL-13 | P2 | WF-09 | UF-13 | not direct | N/A P2 | FR-09 | MVP section 3/7 | docs/high-fidelity/state-variants/UL-13.md |
| UL-14 | P2 | WF-09 | UF-13 | not direct | N/A P2 | FR-09 | MVP section 3/7 | docs/high-fidelity/state-variants/UL-14.md |

## Authority chain
- Final official English PRD/MVP: SHA above, represented in repo by docs/PRD.md + docs/MVP.md.
- GS Stage-2 trace: docs/user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md.
- Accepted ADR-0001..0008: docs/adr/README.md.
- Final software authority: docs/SOFTWARE_ARCHITECTURE.md and Issue 16 corrected public contracts.
- Full machine-checkable art-to-State and prototype-node register: PHASE_10_TRACEABILITY_REGISTER.json.
- WF-09 and all UL States remain P2/Removable, outside five mandatory Golden Scenarios.

Static artifact metadata/hash audit is NOT a claim of compiled Android behavior or named human UI signoff.
