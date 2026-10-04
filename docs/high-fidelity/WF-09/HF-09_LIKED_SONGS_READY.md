# HF-09_LIKED_SONGS_READY — Liked Songs — Ready

- **Issue:** #17
- **Phase:** 4 — Primary High-Fidelity
- **Status:** **APPROVED_BASE_HIFI / PASS COMPLETE**
- **Screen family:** `WF-09`
- **Source Wireframe:** `WF-09_LIKED_SONGS_READY`
- **Image:** `HF-09_LIKED_SONGS_READY.png`
- **Priority:** **P2 / Removable**
- **Ownership:** **App**
- **Reference viewport:** **360×800dp**
- **Rendered PNG:** **1080×2400px (@3×)**
- **Source package SHA-256:** `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- **Phase-3 main baseline:** `b6113b0d094e0c9f5fd55842a80a90528cdc4e4f`
- **Structural deviation from approved Wireframe:** **NONE**

## Traceability

| Field | Value |
|---|---|
| User Flow | UF-13 |
| Canonical State IDs | UL-04, UL-09, UL-10, UL-12 |
| Golden Scenario | — |
| Source Wireframe | `docs/wireframes/app-states/WF-09_LIKED_SONGS_READY.md` |
| Source low-fidelity visual | `docs/wireframes/app-states/WF-09_LIKED_SONGS_READY.svg` |
| Design Foundations | `docs/design-system/foundations/FOUNDATIONS.md` + `TOKENS.json` |
| Component System | `docs/design-system/components/COMPONENT_CATALOG.md` + `COMPONENT_TOKENS.json` |

## High-fidelity hierarchy

1. Back affordance + Liked Songs app bar
2. Visible P2 / REMOVABLE label
3. Play list action
4. Three liked local-media rows with unlike affordances
5. Approved four-item bottom navigation

The high-fidelity artifact preserves the approved source hierarchy. It does not add a destination, product capability, navigation branch, playback owner, Voice owner, system surface, or P0/P1/P2 scope.

## Exact UI copy

The image is approved only if these strings appear exactly where applicable. Android/system status-bar time and platform icons are review chrome and are not app-owned UI copy.

1. `Liked Songs`
2. `P2 / REMOVABLE`
3. `Play list`
4. `Aurora Lights`
5. `Liked · local track`
6. `Night Drive`
7. `Liked · local track`
8. `Ocean Lines`
9. `Liked · local track`
10. `Home`
11. `Search`
12. `Liked`
13. `Settings`

**Local rendered-copy validation:** **PASS** — the deterministic render uses the strings above directly rather than image-model text generation.

## Reusable component composition

- `CMP-03` — AppBar
- `CMP-01` — ActionButton — Play list
- `CMP-11` — MediaRow — LikedTrackP2
- `CMP-20` — LikeToggleP2
- `CMP-04` — BottomNavigation

## Behavior

- **Primary action:** Play list or select a row; unlike preserves media files.
- **Entry:** P2 projection contains accessible liked tracks.
- **Exit:** Playback acknowledgement → Full Player; Back → Library.
- **Back:** Returns without stopping unrelated playback.
- **Recovery:** Stale/rejected item refreshes projection; load failure preserves manual playback.
- **Visible source of truth:** Local Like repository plus accessible MediaStore projection; service owns playback.

## Accessibility

- TalkBack: P2/Removable label → Play list → rows → Like state/action → Back.
- Global app interaction target minimum remains **48dp**.
- 200% text scaling/reflow remains a required implementation proof; this static 360×800dp image does not replace runtime Compose verification.
- Focus, selected, disabled, error and pending meaning must remain non-color-only.

## Privacy / analytics

- Liked status and track metadata remain local; media files are never deleted by Unlike.
- Raw Voice audio is not persisted by default.
- Personal media titles/artists, search queries/results and Voice candidate labels are prohibited from repository analytics/evidence payloads.
- The sample local-media names visible in this review artifact are non-user test data.

## System boundary

Android-owned permission dialogs, Android Settings, notification/lock-screen/external controls and platform status chrome remain system-owned. A visual annotation or simulated status bar in this review image does not create an app-owned system screen.

## Review gate

Before Phase 4 is recorded as PASS / COMPLETE:

- [x] PNG visibly renders in GitHub.
- [x] PNG hierarchy matches `WF-09_LIKED_SONGS_READY`.
- [x] Exact UI copy matches this file.
- [x] No new navigation, control, capability or destination was introduced.
- [x] Component usage remains compatible with Phase 3.
- [x] Accessibility/privacy/system-boundary notes remain intact.

