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

## Phase 6 — specialist Driving/Voice review (11 October 2026)

- **Specialist gate:** PASS (static source-locked design evidence); independent Product/Design/Android/Accessibility/Privacy sign-off and on-device tests remain pending.
- [Driving and Voice specialist matrix](specialist/phase-6/PHASE_6_SPECIALIST_MATRIX.md) — 27 specialist reviewable screens, 7 Driving and 20 Voice, each paired with exact UI-copy/behavior Markdown.
- [42-state UF-07..10 audit](specialist/phase-6/PHASE_6_UF_STATE_AUDIT.md) — CC-01..CC-18 and CD-01..CD-24, including existing non-visual variants and system-only handoffs.
- [Visual board](specialist/phase-6/PHASE_6_SPECIALIST_BOARD.png) and [validation JSON](specialist/phase-6/PHASE_6_VALIDATION.json).
- [22 real-device QA handoff cases](specialist/phase-6/PHASE_6_DEVICE_QA_HANDOFF.md) explicitly marked NOT RUN.

**Specialist precedence:** for UF-07/08/09/10, the matching specialist image/spec supersedes contradictory generic Phase-5 visual treatments while keeping the 126 canonical State IDs and former Phase-5 records immutable. In particular, No Track no longer offers an unverified active Play last; not-found/unsupported do not show an ineligible Retry. Android/System-only transitions are not depicted as in-app screens. Phase 4 primary images and Phase 5 machine counts remain unchanged.

## Phase 7 — Accessibility / Privacy / System-boundary handoff (11 October 2026)

Phase 7 explicitly specifies per-State TalkBack reading order, actionable labels, non-color-only status, Compose semantics, focus restoration, 100/150/200% font reflow and 48dp targets (**Driving:** 64dp transport, 72dp Mic, 56dp Exit). It preserves all 20 Android/System-only owners and system transition return paths; no external Android permission dialog, Settings, notification, lock-screen or vehicle media interface is fabricated as an app screen.

- [Full 45 + 20 coverage matrix](phase-7/ACCESSIBILITY_PRIVACY_SYSTEM_MATRIX.md)
- [Compose semantics and PH10-MIN-001 implementation handoff](phase-7/COMPOSE_A11Y_HANDOFF.md)
- [Privacy/telemetry and sanitized evidence contract](phase-7/PRIVACY_EVIDENCE_HANDOFF.md)
- [System boundary and OS return ownership](phase-7/SYSTEM_BOUNDARY_HANDOFF.md)
- [Machine-readable source/ownership register](phase-7/PHASE_7_REGISTER.json)
- [Automated validation result](phase-7/PHASE_7_VALIDATION.md)
- [65 on-device checks — deliberately NOT RUN](phase-7/PHASE_7_DEVICE_QA_MATRIX.md)

**Gate:** PASS at specification/handoff level; PH10-MIN-001 remains OPEN for actual Compose semantics, focus and 200% reflow proof. Independent Product/UX/Android/Privacy and physical accessibility review is not claimed.

## Phase 8 — Repository-native clickable prototype (11 October 2026)

The high-fidelity designs are reused, not regenerated, by the [Phase-8 clickable prototype](../prototype/index.html). It covers [five approved Golden Scenarios and their recoveries](../prototype/PROTOTYPE_COVERAGE_MATRIX.md), with transparent image hotspots over only existing approved controls. OS permissions/Settings/notifications/lock-screen/MediaSession external controls remain explicitly non-app [system-only annotations](../prototype/README.md); a separate reviewer event simulator provides reproducible outcomes without adding in-app navigation.

- [Static route graph and source PNG references](../prototype/routes.js)
- [Node image, hotspot, GS path and ownership validation](../prototype/PHASE_8_VALIDATION.json)
- [Real Chromium click-flow smoke test](../prototype/PHASE_8_BROWSER_VALIDATION.json)
- [Synthetic-only responsive browser screenshot](../prototype/PHASE_8_BROWSER_REVIEW.png)
- [Manual/Android QA handoff](../prototype/TEST_PLAN.md)

**Scope:** Phase 8 is a reviewable interaction prototype, not a running Media3 app, mic/STT implementation, hosted web demo, actual Android system UI or physical test. Human sign-off and runtime accessibility (PH10-MIN-001) stay pending.

### Phase 8 strict acceptance correction

The [strict Phase-8 re-audit](../prototype/PHASE_8_STRICT_REAUDIT_2026-10-11.md) supersedes the earlier 35-branch global reachability count: 38/38 required scenario-local Recovery branches now have proven safe terminals, 14/14 Chromium clicks pass, and the approved PNGs are reused with corrected Empty/Denied/P1 Restore hotspots. [routes.json](../prototype/routes.json) is generated from the same [routes.js](../prototype/routes.js); external CB-22/23/24/25/27/28 remain system handoffs. This does not modify the 126 canonical State IDs.

## Issue #17 Phase 9 — Jetpack Compose Engineering Handoff (11 October 2026)

The [engineering handoff index](../handoff/README.md) links all five required files. Verified coverage: 10 WFs, 23 CMP components, 126 State IDs, 32 approved source frames, and 20 native Android/System-only States. All 149 registered PNG/SVG designs have repository integrity checks.

- [Screen specs](../handoff/SCREEN_SPECS.md)
- [Component specs](../handoff/COMPONENT_SPECS.md)
- [State/action matrix](../handoff/STATE_ACTION_MATRIX.md)
- [Exact Compose token mapping](../handoff/COMPOSE_TOKEN_MAPPING.md)
- [Verified design assets](../handoff/ASSET_REGISTER.json)
- [Independent audit](../handoff/PHASE9_VALIDATION.md)

MediaSessionService remains playback authority; VoiceSessionCoordinator remains Voice authority; Android-native surfaces remain OS owned. PH10-MIN-001 real Compose accessibility testing remains open.
