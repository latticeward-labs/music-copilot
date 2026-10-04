# HF-10_SETTINGS_DEFAULT — Settings — Default

- **Issue:** #17
- **Phase:** 4 — Primary High-Fidelity
- **Status:** **APPROVED_BASE_HIFI / PASS COMPLETE**
- **Screen family:** `WF-10`
- **Source Wireframe:** `WF-10_SETTINGS_DEFAULT`
- **Image:** `HF-10_SETTINGS_DEFAULT.png`
- **Priority:** **P0**
- **Ownership:** **App + platform/provider boundary**
- **Reference viewport:** **360×800dp**
- **Rendered PNG:** **1080×2400px (@3×)**
- **Source package SHA-256:** `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- **Phase-3 main baseline:** `b6113b0d094e0c9f5fd55842a80a90528cdc4e4f`
- **Structural deviation from approved Wireframe:** **NONE**

## Traceability

| Field | Value |
|---|---|
| User Flow | UF-12 |
| Canonical State IDs | CE-15, CE-21, CE-22 |
| Golden Scenario | — |
| Source Wireframe | `docs/wireframes/app-states/WF-10_SETTINGS_DEFAULT.md` |
| Source low-fidelity visual | `docs/wireframes/app-states/WF-10_SETTINGS_DEFAULT.svg` |
| Design Foundations | `docs/design-system/foundations/FOUNDATIONS.md` + `TOKENS.json` |
| Component System | `docs/design-system/components/COMPONENT_CATALOG.md` + `COMPONENT_TOKENS.json` |

## High-fidelity hierarchy

1. Back affordance + Settings app bar
2. Permissions SettingsRow
3. Bluetooth suggestion P1 SettingsRow
4. Privacy SettingsRow
5. Clear data / history SettingsRow
6. Approved four-item bottom navigation

The high-fidelity artifact preserves the approved source hierarchy. It does not add a destination, product capability, navigation branch, playback owner, Voice owner, system surface, or P0/P1/P2 scope.

## Exact UI copy

The image is approved only if these strings appear exactly where applicable. Android/system status-bar time and platform icons are review chrome and are not app-owned UI copy.

1. `Settings`
2. `Permissions`
3. `Current platform status`
4. `Open`
5. `Bluetooth suggestion`
6. `P1 preference`
7. `Open`
8. `Privacy`
9. `Local-first controls`
10. `Open`
11. `Clear data / history`
12. `Selected categories only`
13. `Open`
14. `Home`
15. `Search`
16. `Liked`
17. `Settings`

**Local rendered-copy validation:** **PASS** — the deterministic render uses the strings above directly rather than image-model text generation.

## Reusable component composition

- `CMP-03` — AppBar
- `CMP-18` — SettingsRow
- `CMP-04` — BottomNavigation

## Behavior

- **Primary action:** Open one settings section.
- **Entry:** Settings opened from non-driving navigation.
- **Exit:** Section/update/handoff; Back returns to prior surface.
- **Back:** Returns without changing values or playback.
- **Recovery:** Load/update error retains previous value and exposes Retry where safe.
- **Visible source of truth:** Preferences plus fresh platform/provider truth.

## Accessibility

- TalkBack: Settings heading → section headings → current values/actions → bottom navigation.
- Global app interaction target minimum remains **48dp**.
- 200% text scaling/reflow remains a required implementation proof; this static 360×800dp image does not replace runtime Compose verification.
- Focus, selected, disabled, error and pending meaning must remain non-color-only.

## Privacy / analytics

- Only approved local settings/consent are stored; Android Settings handoff stays external.
- Raw Voice audio is not persisted by default.
- Personal media titles/artists, search queries/results and Voice candidate labels are prohibited from repository analytics/evidence payloads.
- The sample local-media names visible in this review artifact are non-user test data.

## System boundary

Android-owned permission dialogs, Android Settings, notification/lock-screen/external controls and platform status chrome remain system-owned. A visual annotation or simulated status bar in this review image does not create an app-owned system screen.

## Review gate

Before Phase 4 is recorded as PASS / COMPLETE:

- [x] PNG visibly renders in GitHub.
- [x] PNG hierarchy matches `WF-10_SETTINGS_DEFAULT`.
- [x] Exact UI copy matches this file.
- [x] No new navigation, control, capability or destination was introduced.
- [x] Component usage remains compatible with Phase 3.
- [x] Accessibility/privacy/system-boundary notes remain intact.

