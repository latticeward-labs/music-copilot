# HF-06_FULL_PLAYER_PLAYING — Full Player — Playing

- **Issue:** #17
- **Phase:** 4 — Primary High-Fidelity
- **Status:** **APPROVED_BASE_HIFI / PASS COMPLETE**
- **Screen family:** `WF-06`
- **Source Wireframe:** `WF-06_FULL_PLAYER_PLAYING`
- **Image:** `HF-06_FULL_PLAYER_PLAYING.png`
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
| User Flow | UF-05, UF-06, UF-11 |
| Canonical State IDs | CB-15 |
| Golden Scenario | GS-02, GS-03, GS-05 |
| Source Wireframe | `docs/wireframes/app-states/WF-06_FULL_PLAYER_PLAYING.md` |
| Source low-fidelity visual | `docs/wireframes/app-states/WF-06_FULL_PLAYER_PLAYING.svg` |
| Design Foundations | `docs/design-system/foundations/FOUNDATIONS.md` + `TOKENS.json` |
| Component System | `docs/design-system/components/COMPONENT_CATALOG.md` + `COMPONENT_TOKENS.json` |

## High-fidelity hierarchy

1. Back affordance + Now playing app bar + Queue action
2. 1:1 artwork
3. Title and artist metadata
4. Seek/progress band with elapsed/total time
5. Previous / Pause / Next controls
6. Written Playing state
7. Explicit Driving entry action

The high-fidelity artifact preserves the approved source hierarchy. It does not add a destination, product capability, navigation branch, playback owner, Voice owner, system surface, or P0/P1/P2 scope.

## Exact UI copy

The image is approved only if these strings appear exactly where applicable. Android/system status-bar time and platform icons are review chrome and are not app-owned UI copy.

1. `Now playing`
2. `Queue`
3. `ARTWORK`
4. `Aurora Lights`
5. `Local artist`
6. `01:26`
7. `03:42`
8. `PLAYING`
9. `Driving`

**Local rendered-copy validation:** **PASS** — the deterministic render uses the strings above directly rather than image-model text generation.

## Reusable component composition

- `CMP-03` — AppBar
- `CMP-08` — Artwork
- `CMP-07` — TrackMetadata
- `CMP-13` — ProgressSeek
- `CMP-05` — PlaybackControls — StandardTriple
- `CMP-01` — ActionButton — explicit Driving entry

## Behavior

- **Primary action:** Pause; Next/Previous/Seek/Queue remain available as supported. Driving entry is an explicit secondary action.
- **Entry:** MediaSessionService acknowledges playback with a valid item.
- **Exit:** Back keeps playback; `Driving` enters Driving Mode explicitly; Queue remains same authority.
- **Back:** Returns to initiating app surface without stopping playback.
- **Recovery:** Command/controller failure refreshes actual snapshot; corrupt item uses bounded recovery.
- **Visible source of truth:** MediaSessionService snapshot: item, queue, state, position and supported actions.

## Accessibility

- TalkBack: Now playing → title/artist → position → Previous → Pause → Next → Driving → Queue.
- Global app interaction target minimum remains **48dp**.
- 200% text scaling/reflow remains a required implementation proof; this static 360×800dp image does not replace runtime Compose verification.
- Focus, selected, disabled, error and pending meaning must remain non-color-only.

## Privacy / analytics

- Track title/artist and precise position are presentation-only; precise position/personal metadata are prohibited from telemetry.
- Raw Voice audio is not persisted by default.
- Personal media titles/artists, search queries/results and Voice candidate labels are prohibited from repository analytics/evidence payloads.
- The sample local-media names visible in this review artifact are non-user test data.

## System boundary

Android-owned permission dialogs, Android Settings, notification/lock-screen/external controls and platform status chrome remain system-owned. A visual annotation or simulated status bar in this review image does not create an app-owned system screen.

## Phase 4 Correction Note

The prior approved image/spec omitted the explicit entry affordance for Driving Mode even though WF-06 behavior requires explicit Driving Mode entry. This correction adds the `Driving` action as the visible entry to existing `WF-07_DRIVING_MODE_PLAYING`; it does not add a new product capability or destination beyond the approved WF-07 route.

## Review gate

Before Phase 4 is recorded as PASS / COMPLETE:

- [x] PNG visibly renders in GitHub.
- [x] PNG hierarchy matches `WF-06_FULL_PLAYER_PLAYING`.
- [x] Exact UI copy matches this file.
- [x] No new product capability was introduced; `Driving` exposes the already-approved explicit entry to WF-07.
- [x] Component usage remains compatible with Phase 3.
- [x] Accessibility/privacy/system-boundary notes remain intact.

