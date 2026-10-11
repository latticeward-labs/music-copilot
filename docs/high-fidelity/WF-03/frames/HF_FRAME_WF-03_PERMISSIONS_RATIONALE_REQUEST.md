# HF Frame Review — WF-03_PERMISSIONS_RATIONALE_REQUEST

**Issue/phase:** #17 / 5 · **Source:** WF-03_PERMISSIONS_RATIONALE_REQUEST · **Status:** Reviewable generated evidence (not separate human sign-off)
**Image:** HF_FRAME_WF-03_PERMISSIONS_RATIONALE_REQUEST.png · **SHA-256:** 8e16ecc0d46a9a093d726042ce5ff723d47270dcd07d0302a26d1225c3100640
**Priority:** P0 · **Ownership:** Hybrid
**Canonical State IDs:** S-03, CA-02, SYS-01, SYS-02 · **UF:** UF-02 / UF-10 / UF-12 · **GS:** GS-01
**Approved source:** https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-03_PERMISSIONS_RATIONALE_REQUEST.md and https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-03_PERMISSIONS_RATIONALE_REQUEST.svg
**Structural deviations:** None: exact source SVG hierarchy retained; approved Night Signal tokens applied.

## Exact rendered app copy

- "Back"
- "Permissions"
- "PERMISSION"
- "?"
- "Media access"
- "Index and play local music."
- "Continue"
- "Not now"
- "[ANDROID / SYSTEM] handoff"
- "Fresh result is authoritative"

## Behavioral boundary

- **Entry:** Media access is first required, or mic is explicitly tapped/practiced.
- **Primary action:** Continue to the Android-owned permission dialog.
- **Exit:** Android dialog handoff; return routes to Granted/Denied.
- **Back:** Back/Not now returns to a usable manual surface.
- **Truth:** Android permission state is authoritative.
- **Recovery:** Not now preserves manual controls; no coercive loop.
- **A11y / TalkBack:** Permission heading → purpose → Continue → Not now → system-boundary note.
- **System handoff:** [ANDROID / SYSTEM] dialog is not an app screen.
- **Minimum target:** ≥48dp; Driving previous/pause/next/seek-back ≥64dp, Mic ≥72dp, Exit ≥56dp.
- **At 200% font scaling:** Compose implementation must reflow; static screenshot does not prove runtime layout.
- **Privacy:** no raw audio, transcript, search query or local music metadata in analytics; sample names are synthetic.
- **Ownership:** service-owned playback; Voice coordinator-owned Voice session; platform surfaces excluded from generated app screenshots.
- **P2:** Liked Songs remains removable; no P0 build dependency.

## Evidence limitation

Deterministic restyling of the *approved* low-fidelity SVG into Night Signal produces this reviewable static visual. It is not an independently signed design approval, a Compose implementation, nor proof of precise physical tap-target or 200% reflow behavior.
