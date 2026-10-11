# Issue #17 Phase 4 — Primary High-Fidelity Screen Families

- **Date:** 4 October 2026
- **Issue:** #17
- **Phase:** 4 — Primary High-Fidelity
- **Status:** **APPROVED_BASE_HIFI / PASS COMPLETE**
- **Source archive SHA-256:** `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- **Phase-3 `main` baseline:** `b6113b0d094e0c9f5fd55842a80a90528cdc4e4f`
- **Visual direction:** **Night Signal**
- **Reference viewport:** **360×800dp**
- **Rendered review PNGs:** **1080×2400px (@3×)**

Phase 4 introduces the first repository-native high-fidelity screen images. Each of the ten required screen families has exactly one primary/happy-state artifact in this phase. Every image is paired with a Markdown specification that controls exact UI copy, traceability, behavior, ownership, accessibility, privacy and system boundaries.

## Phase-4 primary set

| Family | High-fidelity ID | Source Wireframe | Priority | PNG | Spec |
|---|---|---|---|---|---|
| `WF-01` | `HF-01_SPLASH_LOADING` | `WF-01_SPLASH_LOADING` | P0 | `HF-01_SPLASH_LOADING.png` | `HF-01_SPLASH_LOADING.md` |
| `WF-02` | `HF-02_ONBOARDING_SLIDE_1` | `WF-02_ONBOARDING_SLIDE_1` | P0 | `HF-02_ONBOARDING_SLIDE_1.png` | `HF-02_ONBOARDING_SLIDE_1.md` |
| `WF-03` | `HF-03_PERMISSIONS_RATIONALE_REQUEST` | `WF-03_PERMISSIONS_RATIONALE_REQUEST` | P0 | `HF-03_PERMISSIONS_RATIONALE_REQUEST.png` | `HF-03_PERMISSIONS_RATIONALE_REQUEST.md` |
| `WF-04` | `HF-04_HOME_LIBRARY_READY` | `WF-04_HOME_LIBRARY_READY` | P0 | `HF-04_HOME_LIBRARY_READY.png` | `HF-04_HOME_LIBRARY_READY.md` |
| `WF-05` | `HF-05_SEARCH_RESULTS` | `WF-05_SEARCH_RESULTS` | P0 | `HF-05_SEARCH_RESULTS.png` | `HF-05_SEARCH_RESULTS.md` |
| `WF-06` | `HF-06_FULL_PLAYER_PLAYING` | `WF-06_FULL_PLAYER_PLAYING` | P0 | `HF-06_FULL_PLAYER_PLAYING.png` | `HF-06_FULL_PLAYER_PLAYING.md` |
| `WF-07` | `HF-07_DRIVING_MODE_PLAYING` | `WF-07_DRIVING_MODE_PLAYING` | P0 | `HF-07_DRIVING_MODE_PLAYING.png` | `HF-07_DRIVING_MODE_PLAYING.md` |
| `WF-08` | `HF-08_VOICE_LISTENING` | `WF-08_VOICE_LISTENING` | P0 | `HF-08_VOICE_LISTENING.png` | `HF-08_VOICE_LISTENING.md` |
| `WF-09` | `HF-09_LIKED_SONGS_READY` | `WF-09_LIKED_SONGS_READY` | P2 / Removable | `HF-09_LIKED_SONGS_READY.png` | `HF-09_LIKED_SONGS_READY.md` |
| `WF-10` | `HF-10_SETTINGS_DEFAULT` | `WF-10_SETTINGS_DEFAULT` | P0 | `HF-10_SETTINGS_DEFAULT.png` | `HF-10_SETTINGS_DEFAULT.md` |

## Governing rules

1. The approved `WF-*` hierarchy is preserved. Phase 4 does not create new navigation, destinations, controls or capabilities.
2. Exact implementation-facing copy is controlled by the paired Markdown specification.
3. These PNGs are deterministic UI renders using repository Design Foundations and Component System values; they do not rely on image-model text generation.
4. Android/system-owned surfaces remain system-only. Status/navigation chrome in review PNGs is simulated platform chrome and is not app ownership.
5. Playback remains service-owned; UI only observes state and sends commands.
6. Voice remains an overlay, not a destination.
7. Driving Mode remains one fixed layer with immediate Exit, no typing, no nested/deep navigation and no long lists.
8. Liked Songs remains **P2 / Removable**.
9. Personal track/search/Voice values shown here are review/test data and may not enter analytics/evidence payloads.
10. Runtime Compose semantics, 200% reflow and physical device proof remain downstream implementation evidence.

## Review evidence

- [`PHASE_4_PRIMARY_BOARD.png`](PHASE_4_PRIMARY_BOARD.png) — one-board visual review of the ten primary screens.
- [`PHASE_4_PRIMARY_REGISTER.json`](PHASE_4_PRIMARY_REGISTER.json) — machine-readable registry and checksums.
- [`PHASE_4_VALIDATION.md`](PHASE_4_VALIDATION.md) — Phase-4 structural/copy/coverage validation.

## Exit gate

Phase 4 may be recorded as **PASS / COMPLETE** after the branch is uploaded and all ten PNGs visibly render in GitHub, paired specs are present, exact copy is confirmed, the PR diff contains only intended Phase-4 artifacts, and repository checks pass.

No Phase-5 recovery/state-expansion work is claimed by this phase.

## Phase 5 — 126-state completion (11 October 2026)

Phase 4 primary images remain unchanged. Phase 5 adds **45** State-ID reviewable PNG/spec pairs inside each WF package under /states/, **32** approved source-frame Night Signal PNG/spec pairs under /frames/, **61** component variants under /state-variants/, and **20** platform-only handoffs under /system-handoffs/. The authoritative disposition is [HIFI_STATE_COVERAGE_MATRIX.md](HIFI_STATE_COVERAGE_MATRIX.md); machine validation is [PHASE_5_VALIDATION.json](PHASE_5_VALIDATION.json). Automated 126/126 coverage does not imply independent human sign-off, physical runtime accessibility acceptance, or Issue #17 final closure.
