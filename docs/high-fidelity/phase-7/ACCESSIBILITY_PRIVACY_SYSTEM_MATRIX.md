# Issue #17 Phase 7 — Accessibility / Privacy / System Boundary Matrix

**Date:** 11 October 2026. **Gate:** PASS (documentation coverage only). **Actual Compose/device QA:** NOT RUN.
**Authority:** Phase-5 126-state register; Phase-6 specialist UX; Issue-16 PH10-MIN-001; Android system transition register.

| Gate | Target | Verified |
|---|---:|---:|
| App-visible A11y disposition | 45 | 45 |
| System-only owner/return evidence | 20 | 20 |
| Missing / duplicate / unassigned | 0 | 0 |
| Fabricated Android/system app screens | 0 | 0 |

## 45 app-visible State contracts

| State | WF | Reading order/TalkBack/200%/48dp/Privacy | Source visual |
|---|---|---|---|
| S-01 | WF-01 | [A11y disposition](app-visible/S-01.md) | [review image](../WF-01/states/HF_STATE_S-01.png) |
| S-02 | WF-02 | [A11y disposition](app-visible/S-02.md) | [review image](../WF-02/states/HF_STATE_S-02.png) |
| S-03 | WF-03 | [A11y disposition](app-visible/S-03.md) | [review image](../WF-03/states/HF_STATE_S-03.png) |
| S-04 | WF-04 | [A11y disposition](app-visible/S-04.md) | [review image](../WF-04/states/HF_STATE_S-04.png) |
| CA-01 | WF-02 | [A11y disposition](app-visible/CA-01.md) | [review image](../WF-02/states/HF_STATE_CA-01.png) |
| CB-01 | WF-04 | [A11y disposition](app-visible/CB-01.md) | [review image](../WF-04/states/HF_STATE_CB-01.png) |
| CB-09 | WF-05 | [A11y disposition](app-visible/CB-09.md) | [review image](../WF-05/states/HF_STATE_CB-09.png) |
| CB-10 | WF-05 | [A11y disposition](app-visible/CB-10.md) | [review image](../WF-05/states/HF_STATE_CB-10.png) |
| CB-11 | WF-05 | [A11y disposition](app-visible/CB-11.md) | [review image](../WF-05/states/HF_STATE_CB-11.png) |
| CB-12 | WF-05 | [A11y disposition](app-visible/CB-12.md) | [review image](../WF-05/states/HF_STATE_CB-12.png) |
| CB-15 | WF-06 | [A11y disposition](app-visible/CB-15.md) | [review image](../WF-06/states/HF_STATE_CB-15.png) |
| CB-16 | WF-06 | [A11y disposition](app-visible/CB-16.md) | [review image](../WF-06/states/HF_STATE_CB-16.png) |
| CB-17 | WF-06 | [A11y disposition](app-visible/CB-17.md) | [review image](../WF-06/states/HF_STATE_CB-17.png) |
| CB-18 | WF-06 | [A11y disposition](app-visible/CB-18.md) | [review image](../WF-06/states/HF_STATE_CB-18.png) |
| CC-05 | WF-07 | [A11y disposition](app-visible/CC-05.md) | [review image](../specialist/phase-6/DRV_PLAYING.png) |
| CC-06 | WF-07 | [A11y disposition](app-visible/CC-06.md) | [review image](../specialist/phase-6/DRV_PAUSED.png) |
| CC-07 | WF-07 | [A11y disposition](app-visible/CC-07.md) | [review image](../specialist/phase-6/DRV_NO_TRACK.png) |
| CC-11 | WF-07 | [A11y disposition](app-visible/CC-11.md) | [review image](../WF-07/states/HF_STATE_CC-11.png) |
| CC-14 | WF-08 | [A11y disposition](app-visible/CC-14.md) | [review image](../WF-08/states/HF_STATE_CC-14.png) |
| CD-03 | WF-08 | [A11y disposition](app-visible/CD-03.md) | [review image](../specialist/phase-6/VO_LISTEN_1.png) |
| CD-04 | WF-08 | [A11y disposition](app-visible/CD-04.md) | [review image](../specialist/phase-6/VO_LISTEN_2.png) |
| CD-05 | WF-08 | [A11y disposition](app-visible/CD-05.md) | [review image](../specialist/phase-6/VO_PROCESSING.png) |
| CD-06 | WF-08 | [A11y disposition](app-visible/CD-06.md) | [review image](../specialist/phase-6/VO_COMMAND_PENDING.png) |
| CD-07 | WF-08 | [A11y disposition](app-visible/CD-07.md) | [review image](../specialist/phase-6/VO_EXECUTED.png) |
| CD-08 | WF-08 | [A11y disposition](app-visible/CD-08.md) | [review image](../specialist/phase-6/VO_EXECUTION_REJECTED.png) |
| CD-09 | WF-08 | [A11y disposition](app-visible/CD-09.md) | [review image](../specialist/phase-6/VO_CONFIRM_ONE.png) |
| CD-10 | WF-08 | [A11y disposition](app-visible/CD-10.md) | [review image](../specialist/phase-6/VO_CONFIRM_TWO.png) |
| CD-11 | WF-08 | [A11y disposition](app-visible/CD-11.md) | [review image](../WF-08/states/HF_STATE_CD-11.png) |
| CD-12 | WF-08 | [A11y disposition](app-visible/CD-12.md) | [review image](../specialist/phase-6/VO_CONFIRMATION_FAILED.png) |
| CD-13 | WF-08 | [A11y disposition](app-visible/CD-13.md) | [review image](../specialist/phase-6/VO_ARTIST_NOT_FOUND.png) |
| CD-14 | WF-08 | [A11y disposition](app-visible/CD-14.md) | [review image](../specialist/phase-6/VO_UNKNOWN_PHRASE.png) |
| CD-15 | WF-08 | [A11y disposition](app-visible/CD-15.md) | [review image](../specialist/phase-6/VO_UNSUPPORTED.png) |
| CD-16 | WF-08 | [A11y disposition](app-visible/CD-16.md) | [review image](../specialist/phase-6/VO_MISSING_ARTIST.png) |
| CD-17 | WF-08 | [A11y disposition](app-visible/CD-17.md) | [review image](../specialist/phase-6/VO_STT_TIMEOUT.png) |
| CD-18 | WF-08 | [A11y disposition](app-visible/CD-18.md) | [review image](../specialist/phase-6/VO_RETRY_AVAILABLE.png) |
| CD-19 | WF-08 | [A11y disposition](app-visible/CD-19.md) | [review image](../specialist/phase-6/VO_ATTEMPT2_TERMINAL.png) |
| CD-23 | WF-10 | [A11y disposition](app-visible/CD-23.md) | [review image](../WF-10/states/HF_STATE_CD-23.png) |
| CE-15 | WF-10 | [A11y disposition](app-visible/CE-15.md) | [review image](../WF-10/states/HF_STATE_CE-15.png) |
| CE-20 | WF-10 | [A11y disposition](app-visible/CE-20.md) | [review image](../WF-10/states/HF_STATE_CE-20.png) |
| CE-21 | WF-10 | [A11y disposition](app-visible/CE-21.md) | [review image](../WF-10/states/HF_STATE_CE-21.png) |
| CE-22 | WF-10 | [A11y disposition](app-visible/CE-22.md) | [review image](../WF-10/states/HF_STATE_CE-22.png) |
| CE-23 | WF-10 | [A11y disposition](app-visible/CE-23.md) | [review image](../WF-10/states/HF_STATE_CE-23.png) |
| UL-02 | WF-09 | [A11y disposition](app-visible/UL-02.md) | [review image](../WF-09/states/HF_STATE_UL-02.png) |
| UL-03 | WF-09 | [A11y disposition](app-visible/UL-03.md) | [review image](../WF-09/states/HF_STATE_UL-03.png) |
| UL-04 | WF-09 | [A11y disposition](app-visible/UL-04.md) | [review image](../WF-09/states/HF_STATE_UL-04.png) |

## 20 system-only owner contracts

| State | Real owner | Actual platform screen owner | Return owner | System handoff |
|---|---|---|---|---|
| SYS-01 | Android/System | Android permission/Settings | UF-02 | [owner + return contract](system-only/SYS-01.md) |
| SYS-02 | Android/System | Android permission/Settings | UF-02 | [owner + return contract](system-only/SYS-02.md) |
| SYS-03 | Android/System | Android notification permission | UF-06 / UF-12 | [owner + return contract](system-only/SYS-03.md) |
| SYS-04 | Android/System | Android permission/Settings | UF-02 | [owner + return contract](system-only/SYS-04.md) |
| SYS-05 | Android/System + provider | Speech provider / consent boundary | UF-10 / UF-12 | [owner + return contract](system-only/SYS-05.md) |
| CB-22 | Android/System + Playback Service | Android lifecycle/background | UF-06 | [owner + return contract](system-only/CB-22.md) |
| CB-23 | Android/System + Playback Service | Android lifecycle/background | UF-06 | [owner + return contract](system-only/CB-23.md) |
| CB-24 | Android/System + Playback Service | Android lifecycle/background | UF-06 | [owner + return contract](system-only/CB-24.md) |
| CB-25 | Android/System + Playback Service | Android lifecycle/background | UF-06 | [owner + return contract](system-only/CB-25.md) |
| CB-26 | Android/System + MediaSession | Android/system media surface | UF-06 | [owner + return contract](system-only/CB-26.md) |
| CB-27 | Android/System + MediaSession | Android/system media surface | UF-06 | [owner + return contract](system-only/CB-27.md) |
| CB-28 | Android/System + MediaSession | Android/system media surface | UF-06 | [owner + return contract](system-only/CB-28.md) |
| CB-29 | Android/System + MediaSession | System command handoff | UF-06 | [owner + return contract](system-only/CB-29.md) |
| CB-30 | Android/System | Notification permission/visibility | UF-06 / UF-12 | [owner + return contract](system-only/CB-30.md) |
| CC-16 | Android/System + Playback Service | Android interruption/route | UF-11 | [owner + return contract](system-only/CC-16.md) |
| CE-07 | Android/System + Playback Service | Android route/focus handoff | UF-11 | [owner + return contract](system-only/CE-07.md) |
| CE-08 | Android/System + Playback Service | Android route/focus handoff | UF-11 | [owner + return contract](system-only/CE-08.md) |
| CE-09 | Android/System + Playback Service | Android route/focus handoff | UF-11 | [owner + return contract](system-only/CE-09.md) |
| CE-10 | Android/System + Playback Service | Android route/focus handoff | UF-11 | [owner + return contract](system-only/CE-10.md) |
| CE-18 | Android/System | Android Settings | UF-12 | [owner + return contract](system-only/CE-18.md) |

## Downstream specs

- [PH10-MIN-001 Compose semantics, 200% reflow, focus](COMPOSE_A11Y_HANDOFF.md)
- [Privacy and safe evidence contract](PRIVACY_EVIDENCE_HANDOFF.md)
- [OS and service ownership boundary](SYSTEM_BOUNDARY_HANDOFF.md)
- [65 real-device test handoffs (NOT RUN)](PHASE_7_DEVICE_QA_MATRIX.md)
- [Machine-readable register](PHASE_7_REGISTER.json)
- [Automatic validation](PHASE_7_VALIDATION.md)

**Scope:** no new Screen Family, navigation, Android dialog or app-owned media control; Phase-5 and Phase-6 artwork retained. Static documentation does not close the implementation/runtime PH10-MIN-001 finding.
