# HF-04_HOME_LIBRARY_READY — Home / Library — Ready

- **Issue:** #17
- **Phase:** 4 — Primary High-Fidelity
- **Status:** **APPROVED_BASE_HIFI / PASS COMPLETE**
- **Screen family:** `WF-04`
- **Source Wireframe:** `WF-04_HOME_LIBRARY_READY`
- **Image:** `HF-04_HOME_LIBRARY_READY.png`
- **Priority:** **P0**
- **Ownership:** **App**
- **Reference viewport:** **360×800dp**
- **Rendered PNG:** **1080×2400px (@3×)**
- **Source package SHA-256:** `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- **Phase-3 main baseline:** `b6113b0d094e0c9f5fd55842a80a90528cdc4e4f`
- **Structural deviation from approved Wireframe:** **NONE**

## Traceability

| Field | Value |
|---|---|
| User Flow | UF-03, UF-04 |
| Canonical State IDs | S-04, CB-01, CB-20 |
| Golden Scenario | GS-01, GS-02 |
| Source Wireframe | `docs/wireframes/app-states/WF-04_HOME_LIBRARY_READY.md` |
| Source low-fidelity visual | `docs/wireframes/app-states/WF-04_HOME_LIBRARY_READY.svg` |
| Design Foundations | `docs/design-system/foundations/FOUNDATIONS.md` + `TOKENS.json` |
| Component System | `docs/design-system/components/COMPONENT_CATALOG.md` + `COMPONENT_TOKENS.json` |

## High-fidelity hierarchy

1. Library app bar + More affordance
2. Approved category selector row
3. Three local media rows
4. Service-observing MiniPlayer
5. Approved four-item bottom navigation

The high-fidelity artifact preserves the approved source hierarchy. It does not add a destination, product capability, navigation branch, playback owner, Voice owner, system surface, or P0/P1/P2 scope.

## Exact UI copy

The image is approved only if these strings appear exactly where applicable. Android/system status-bar time and platform icons are review chrome and are not app-owned UI copy.

1. `Library`
2. `Songs`
3. `Artists`
4. `Albums`
5. `Folders`
6. `Aurora`
7. `Local artist · 03:42`
8. `Night Drive`
9. `Local artist · 04:10`
10. `Ocean Lines`
11. `Local artist · 02:58`
12. `Current track`
13. `Playing`
14. `Home`
15. `Search`
16. `Liked`
17. `Settings`

**Local rendered-copy validation:** **PASS** — the deterministic render uses the strings above directly rather than image-model text generation.

## Reusable component composition

- `CMP-03` — AppBar
- `CMP-11` — MediaRow
- `CMP-08` — Artwork
- `CMP-09` — MiniPlayer
- `CMP-04` — BottomNavigation

## Behavior

- **Primary action:** Open a category, Search, or select a track.
- **Entry:** Verified non-empty index or returning route with valid current state.
- **Exit:** Browse/Search/Full Player/Settings; mini-player opens Full Player.
- **Back:** Top-level Back does not stop service-owned playback.
- **Recovery:** Stale item → reconcile; revoked access → permissions; controller pending stays truthful.
- **Visible source of truth:** Derived local index supplies library; MediaSessionService supplies mini-player state.

## Accessibility

- TalkBack: Library heading → categories → tracks → mini-player → bottom navigation.
- Global app interaction target minimum remains **48dp**.
- 200% text scaling/reflow remains a required implementation proof; this static 360×800dp image does not replace runtime Compose verification.
- Focus, selected, disabled, error and pending meaning must remain non-color-only.

## Privacy / analytics

- Visible track metadata remains local presentation content and is prohibited from telemetry/evidence.
- Raw Voice audio is not persisted by default.
- Personal media titles/artists, search queries/results and Voice candidate labels are prohibited from repository analytics/evidence payloads.
- The sample local-media names visible in this review artifact are non-user test data.

## System boundary

Android-owned permission dialogs, Android Settings, notification/lock-screen/external controls and platform status chrome remain system-owned. A visual annotation or simulated status bar in this review image does not create an app-owned system screen.

## Review gate

Before Phase 4 is recorded as PASS / COMPLETE:

- [x] PNG visibly renders in GitHub.
- [x] PNG hierarchy matches `WF-04_HOME_LIBRARY_READY`.
- [x] Exact UI copy matches this file.
- [x] No new navigation, control, capability or destination was introduced.
- [x] Component usage remains compatible with Phase 3.
- [x] Accessibility/privacy/system-boundary notes remain intact.

