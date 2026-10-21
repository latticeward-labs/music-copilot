# Issue #6 — P08 Library UI: screenshot, semantics and final-device QA contract

**Date:** 2026-10-22 · **Inputs:** approved WF-04/HF-04, S-04/CB-01/CB-20, UF-03/04, GS-01/02, ADR-0003, P07 verified readiness and P06 bounded Room Browse contract.  
**State:** Test cases authored. **No screenshot was captured, no emulator/device screenshot asserted, and no 5,000-track benchmark executed.** P12 is the sole APK/test delivery.

## Visual reference authority

- `docs/high-fidelity/WF-04/HF-04_HOME_LIBRARY_READY.png` approved source, at 360×800dp viewport and 1080×2400 pixels at 3x.
- `docs/high-fidelity/WF-04/frames/HF_FRAME_WF-04_HOME_LIBRARY_SCANNING.png`
- `docs/high-fidelity/WF-04/frames/HF_FRAME_WF-04_HOME_LIBRARY_EMPTY.png`
- `docs/high-fidelity/WF-04/frames/HF_FRAME_WF-04_HOME_LIBRARY_RECOVERABLE_ERROR.png`
- `docs/wireframes/app-states/WF-04_HOME_LIBRARY_READY.md`, its Scanning/Empty/RecoverableError siblings and `docs/design-system/foundations/TOKENS.json`.
- Implementation: `LibraryBrowseScreen.kt`, `LibraryBrowseViewModel.kt`; P08 inherits Night Signal `MusicColors`, `MusicSize`, `MusicTextStyles` tokens (not user artwork-driven dynamic themes).

## Required screenshot/semantic matrix — one consolidated P12 APK

| ID | Synthetic scenario | Visual/screenshot and a11y assertion | Status |
|---|---|---|---|
| P08-S01 | No permission, old Room contains songs | WF-03 permission handoff; Library never shows stale rows or a fake Empty | NOT_RUN |
| P08-S02 | Granted, no first scan | Unavailable; explicit Scan, not Ready | NOT_RUN |
| P08-S03 | First scan in progress | WF-04 Scanning text+spinner, no second Scan; Back works | NOT_RUN |
| P08-S04 | Verified zero-song reconcile | WF-04 Empty, Rescan visible; never show a fabricated artist | NOT_RUN |
| P08-S05 | 3 distinct accessible songs | WF-04 Ready: Library / Songs Artists Albums Folders / local media rows / 4 bottom slots | NOT_RUN |
| P08-S06 | Source scan recoverable failure | WF-04 typed Recoverable Error + Retry; previous truthful revision not destroyed | NOT_RUN |
| P08-S07 | Artist group with songs | Artist page → selected artist → member Songs → Back to Artists; no queuing/playing claim | NOT_RUN |
| P08-S08 | Album with cross-artist tracks | Album aggregate and members accurate; various-artist label from SQL | NOT_RUN |
| P08-S09 | Two folders with same basename, different full path | Distinct Folder group rows, no path collision | NOT_RUN |
| P08-S10 | 5,000 synthetic music rows | `LazyColumn` stable keys; SQL only 50 per request; frame jank/memory measured, no UI-thread Room work | NOT_RUN |
| P08-S11 | Rotate while loading page 2 | No double MediaStore scan; same ViewModel revision, no cross-category results | NOT_RUN |
| P08-S12 | Revoke music access in Android Settings | Immediate Unavailable on return, no personal rows; no auto-grant/auto-scan | NOT_RUN |
| P08-S13 | Process death and reentry | No cached `Ready` or saved private media ID. Explicit scan required | NOT_RUN |
| P08-S14 | 200% system font scale, 360dp viewport | Tabs horizontally scroll, track row wraps, recovery action stays reachable, no critical CTA clipping | NOT_RUN |
| P08-S15 | TalkBack full traversal | Library heading → four categories → rows (label+metadata) → real-only mini-player → bottom navigation; 48dp min targets | NOT_RUN |
| P08-S16 | P08 unsupported P09 Search / P2 Liked | Remain disabled with truthful semantics until owning phases; Settings functions | NOT_RUN |
| P08-S17 | Service disconnected/idle, user taps Song | Preview says **Not playing**; no fabricated playback-state / MediaSession action | NOT_RUN |
| P08-S18 | Service Ready with real current item | Only service-backed current-track state is shown; no synthetic name, state or queue mutation | NOT_RUN |
| P08-S19 | Index revision changes between pages | Old page response dropped, stale error prompts explicit Rescan, no mixed contents | NOT_RUN |
| P08-S20 | Long artist/song Unicode metadata | Full local text reflows/announces correctly; no private metadata in logs/screenshot evidence | NOT_RUN |

## Test code written

- `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue6Phase8LibraryBrowseViewModelTest.kt` — **12 JUnit methods authored**: revision-gating, first/next pages, 4 categories, nested Back, revocation, race cancellation, failed browse, empty/paging and selection without play.
- `app/src/androidTest/java/com/latticeward/musiccopilot/foundation/Issue6Phase8LibraryComposeInstrumentedTest.kt` — **4 Android Compose tests authored**: visible headings and tabs, group/back, selection state, revoked access.
- These test classes **have not run**, must compile successfully first, and native screenshot/semantic and 5,000 synthetic row performance coverage must be expanded based on actual device results at P12. No fabricated PASS or screenshot images are stored in this branch.

## Privacy and boundaries

Music titles, artists, album metadata, hashed group IDs and MediaStore references are **local-only**. Test outputs use exclusively fictitious names such as `Synthetic One`; real phone screenshot sharing and crash metadata must never expose private names/paths. P08 doesn't create or request audio permission, restart an OS scan, read raw MediaStore rows, or own Media3. P09 Search, P10 validated selection, and #7 MediaSession actual player are separately owned. No Cloud, account, streaming, analytics or P2 Likes are implemented.

**P08 source gate:** READY for P09 implementation subject to static readback; **P08 fully tested/product acceptance = P12 only**, including the unresolved P06 compiler-generated `Room V2 2.json` and real migration evidence.
