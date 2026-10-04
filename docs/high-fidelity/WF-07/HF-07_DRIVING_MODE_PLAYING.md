# HF-07_DRIVING_MODE_PLAYING — Driving Mode — Playing

- **Issue:** #17
- **Phase:** 4 — Primary High-Fidelity
- **Status:** **APPROVED_BASE_HIFI / PASS COMPLETE**
- **Screen family:** `WF-07`
- **Source Wireframe:** `WF-07_DRIVING_MODE_PLAYING`
- **Image:** `HF-07_DRIVING_MODE_PLAYING.png`
- **Priority:** **P0**
- **Ownership:** **App + Playback Service**
- **Reference viewport:** **360×800dp**
- **Rendered PNG:** **1080×2400px (@3×)**
- **Source package SHA-256:** `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- **Phase-3 main baseline:** `b6113b0d094e0c9f5fd55842a80a90528cdc4e4f`
- **Structural deviation from approved Wireframe:** **NONE**

## Traceability

| Field | Value |
|---|---|
| User Flow | UF-07, UF-11 |
| Canonical State IDs | CC-05, CC-14, CC-16 |
| Golden Scenario | GS-04, GS-05 |
| Source Wireframe | `docs/wireframes/app-states/WF-07_DRIVING_MODE_PLAYING.md` |
| Source low-fidelity visual | `docs/wireframes/app-states/WF-07_DRIVING_MODE_PLAYING.svg` |
| Design Foundations | `docs/design-system/foundations/FOUNDATIONS.md` + `TOKENS.json` |
| Component System | `docs/design-system/components/COMPONENT_CATALOG.md` + `COMPONENT_TOKENS.json` |

## High-fidelity hierarchy

1. Single-layer Driving header + immediate Exit
2. Subordinate artwork
3. Large title/artist + written Playing state
4. Fixed Previous / Pause / Next / Seek Back controls
5. Large push-to-talk Mic
6. Single-layer restriction note

The high-fidelity artifact preserves the approved source hierarchy. It does not add a destination, product capability, navigation branch, playback owner, Voice owner, system surface, or P0/P1/P2 scope.

## Exact UI copy

The image is approved only if these strings appear exactly where applicable. Android/system status-bar time and platform icons are review chrome and are not app-owned UI copy.

1. `DRIVING MODE`
2. `Exit`
3. `ARTWORK`
4. `Aurora Lights`
5. `Local artist`
6. `PLAYING`
7. `-10s`
8. `Mic · Voice`
9. `One layer · no typing · no lists`

**Local rendered-copy validation:** **PASS** — the deterministic render uses the strings above directly rather than image-model text generation.

## Reusable component composition

- `CMP-01` — ActionButton — Exit
- `CMP-08` — Artwork — Driving
- `CMP-07` — TrackMetadata — Driving
- `CMP-05` — PlaybackControls — DrivingQuad
- `CMP-06` — MicControl — Driving

## Behavior

- **Primary action:** Pause; four large controls, Mic and Exit remain fixed.
- **Entry:** Explicit Driving Mode entry with a complete Playing snapshot.
- **Exit:** Exit is immediate; Mic opens an overlay, not a destination.
- **Back:** Back behaves as Exit and returns to the prior non-driving surface.
- **Recovery:** Pending/rejected commands refresh actual state; route events hand off to UF-11.
- **Visible source of truth:** Complete MediaSessionService snapshot and supported actions.

## Accessibility

- TalkBack: Playing → title/artist → Previous → Pause → Next → Seek back → Mic → Exit. Driving control sizes use Phase-2 overrides.
- Global app interaction target minimum remains **48dp**.
- 200% text scaling/reflow remains a required implementation proof; this static 360×800dp image does not replace runtime Compose verification.
- Focus, selected, disabled, error and pending meaning must remain non-color-only.

## Privacy / analytics

- No typing, search, long list, Settings, permission or consent UI; personal metadata stays local presentation only.
- Raw Voice audio is not persisted by default.
- Personal media titles/artists, search queries/results and Voice candidate labels are prohibited from repository analytics/evidence payloads.
- The sample local-media names visible in this review artifact are non-user test data.

## System boundary

Android-owned permission dialogs, Android Settings, notification/lock-screen/external controls and platform status chrome remain system-owned. A visual annotation or simulated status bar in this review image does not create an app-owned system screen.

## Review gate

Before Phase 4 is recorded as PASS / COMPLETE:

- [x] PNG visibly renders in GitHub.
- [x] PNG hierarchy matches `WF-07_DRIVING_MODE_PLAYING`.
- [x] Exact UI copy matches this file.
- [x] No new navigation, control, capability or destination was introduced.
- [x] Component usage remains compatible with Phase 3.
- [x] Accessibility/privacy/system-boundary notes remain intact.

