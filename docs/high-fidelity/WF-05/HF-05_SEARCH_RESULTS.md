# HF-05_SEARCH_RESULTS — Search — Results

- **Issue:** #17
- **Phase:** 4 — Primary High-Fidelity
- **Status:** **APPROVED_BASE_HIFI / PASS COMPLETE**
- **Screen family:** `WF-05`
- **Source Wireframe:** `WF-05_SEARCH_RESULTS`
- **Image:** `HF-05_SEARCH_RESULTS.png`
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
| User Flow | UF-04 |
| Canonical State IDs | CB-09, CB-10, CB-13 |
| Golden Scenario | GS-02 |
| Source Wireframe | `docs/wireframes/app-states/WF-05_SEARCH_RESULTS.md` |
| Source low-fidelity visual | `docs/wireframes/app-states/WF-05_SEARCH_RESULTS.svg` |
| Design Foundations | `docs/design-system/foundations/FOUNDATIONS.md` + `TOKENS.json` |
| Component System | `docs/design-system/components/COMPONENT_CATALOG.md` + `COMPONENT_TOKENS.json` |

## High-fidelity hierarchy

1. Back affordance + Search app bar
2. Local-library SearchField with active query
3. Written result count
4. Three approved result rows
5. Approved four-item bottom navigation

The high-fidelity artifact preserves the approved source hierarchy. It does not add a destination, product capability, navigation branch, playback owner, Voice owner, system surface, or P0/P1/P2 scope.

## Exact UI copy

The image is approved only if these strings appear exactly where applicable. Android/system status-bar time and platform icons are review chrome and are not app-owned UI copy.

1. `Search`
2. `aurora`
3. `Clear`
4. `3 local results`
5. `Aurora`
6. `Artist result`
7. `Open`
8. `Aurora Lights`
9. `Track · 03:42`
10. `Play`
11. `Northern Sky`
12. `Album result`
13. `Open`
14. `Home`
15. `Search`
16. `Liked`
17. `Settings`

**Local rendered-copy validation:** **PASS** — the deterministic render uses the strings above directly rather than image-model text generation.

## Reusable component composition

- `CMP-03` — AppBar
- `CMP-10` — SearchField
- `CMP-11` — MediaRow
- `CMP-04` — BottomNavigation

## Behavior

- **Primary action:** Select a result to request service-owned playback.
- **Entry:** Local query returns one or more indexed matches.
- **Exit:** Validated selection → Full Player; edit/clear remains in Search.
- **Back:** Back returns to Library with no playback mutation.
- **Recovery:** Stale result preserves query and reconciles; rejected playback keeps context.
- **Visible source of truth:** Local derived-index query; selection is revalidated against accessible media.

## Accessibility

- TalkBack: Search heading → query field → result count → rows → bottom navigation.
- Global app interaction target minimum remains **48dp**.
- 200% text scaling/reflow remains a required implementation proof; this static 360×800dp image does not replace runtime Compose verification.
- Focus, selected, disabled, error and pending meaning must remain non-color-only.

## Privacy / analytics

- Search query/results are local active-surface state and prohibited from telemetry/evidence.
- Raw Voice audio is not persisted by default.
- Personal media titles/artists, search queries/results and Voice candidate labels are prohibited from repository analytics/evidence payloads.
- The sample local-media names visible in this review artifact are non-user test data.

## System boundary

Android-owned permission dialogs, Android Settings, notification/lock-screen/external controls and platform status chrome remain system-owned. A visual annotation or simulated status bar in this review image does not create an app-owned system screen.

## Review gate

Before Phase 4 is recorded as PASS / COMPLETE:

- [x] PNG visibly renders in GitHub.
- [x] PNG hierarchy matches `WF-05_SEARCH_RESULTS`.
- [x] Exact UI copy matches this file.
- [x] No new navigation, control, capability or destination was introduced.
- [x] Component usage remains compatible with Phase 3.
- [x] Accessibility/privacy/system-boundary notes remain intact.

