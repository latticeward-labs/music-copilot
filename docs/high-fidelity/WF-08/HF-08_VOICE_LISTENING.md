# HF-08_VOICE_LISTENING — Voice Overlay — Listening

- **Issue:** #17
- **Phase:** 4 — Primary High-Fidelity
- **Status:** **APPROVED_BASE_HIFI / PASS COMPLETE**
- **Screen family:** `WF-08`
- **Source Wireframe:** `WF-08_VOICE_LISTENING`
- **Image:** `HF-08_VOICE_LISTENING.png`
- **Priority:** **P0**
- **Ownership:** **App + Voice Coordinator**
- **Reference viewport:** **360×800dp**
- **Rendered PNG:** **1080×2400px (@3×)**
- **Source package SHA-256:** `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- **Phase-3 main baseline:** `b6113b0d094e0c9f5fd55842a80a90528cdc4e4f`
- **Structural deviation from approved Wireframe:** **NONE**

## Traceability

| Field | Value |
|---|---|
| User Flow | UF-08, UF-09, UF-10 |
| Canonical State IDs | CD-03, CC-14 |
| Golden Scenario | GS-04 recovery path |
| Source Wireframe | `docs/wireframes/app-states/WF-08_VOICE_LISTENING.md` |
| Source low-fidelity visual | `docs/wireframes/app-states/WF-08_VOICE_LISTENING.svg` |
| Design Foundations | `docs/design-system/foundations/FOUNDATIONS.md` + `TOKENS.json` |
| Component System | `docs/design-system/components/COMPONENT_CATALOG.md` + `COMPONENT_TOKENS.json` |

## High-fidelity hierarchy

1. Dimmed initiating Player/Driving context
2. Non-destination Voice overlay
3. Written Listening state
4. Six-second attempt note
5. Active microphone visual
6. Cancel action
7. Manual/Exit reachability note

The high-fidelity artifact preserves the approved source hierarchy. It does not add a destination, product capability, navigation branch, playback owner, Voice owner, system surface, or P0/P1/P2 scope.

## Exact UI copy

The image is approved only if these strings appear exactly where applicable. Android/system status-bar time and platform icons are review chrome and are not app-owned UI copy.

1. `DRIVING / PLAYER CONTEXT`
2. `Listening`
3. `Six-second attempt`
4. `Cancel`
5. `Manual / Exit remains reachable`

**Local rendered-copy validation:** **PASS** — the deterministic render uses the strings above directly rather than image-model text generation.

## Reusable component composition

- `CMP-19` — VoiceFeedbackOverlay — Listening
- `CMP-06` — MicControl — SessionActive
- `CMP-01` — ActionButton — Cancel

## Behavior

- **Primary action:** Cancel.
- **Entry:** Explicit microphone tap after permission/provider capability passes.
- **Exit:** Result → Processing; failure → Failed/Retry; Cancel → initiating surface.
- **Back:** Back equals Cancel and releases capture/duck.
- **Recovery:** Timeout/no-match/provider failure preserves playback and exposes one Retry.
- **Visible source of truth:** VoiceSessionCoordinator transient session; playback remains service-owned.

## Accessibility

- TalkBack: Listening status → time limit → Cancel → underlying manual/Exit controls.
- Global app interaction target minimum remains **48dp**.
- 200% text scaling/reflow remains a required implementation proof; this static 360×800dp image does not replace runtime Compose verification.
- Focus, selected, disabled, error and pending meaning must remain non-color-only.

## Privacy / analytics

- Raw audio/transcript and attempt state are not persisted; no candidate/personal content is exported.
- Raw Voice audio is not persisted by default.
- Personal media titles/artists, search queries/results and Voice candidate labels are prohibited from repository analytics/evidence payloads.
- The sample local-media names visible in this review artifact are non-user test data.

## System boundary

Android-owned permission dialogs, Android Settings, notification/lock-screen/external controls and platform status chrome remain system-owned. A visual annotation or simulated status bar in this review image does not create an app-owned system screen.

## Review gate

Before Phase 4 is recorded as PASS / COMPLETE:

- [x] PNG visibly renders in GitHub.
- [x] PNG hierarchy matches `WF-08_VOICE_LISTENING`.
- [x] Exact UI copy matches this file.
- [x] No new navigation, control, capability or destination was introduced.
- [x] Component usage remains compatible with Phase 3.
- [x] Accessibility/privacy/system-boundary notes remain intact.

