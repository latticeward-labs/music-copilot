# System-only Handoff — SYS-05 · Speech provider/cloud-processing boundary

**Phase 5 disposition:** SYSTEM_ONLY_HANDOFF / EVIDENCE-ONLY (NOT AN APP SCREEN)
**Priority:** P0 · **WF:** WF-03 · **UF:** UF-02 / UF-10 / UF-12 · **GS:** GS-01
**Real owner:** Android/System + provider · **Return/recovery owner:** UF-10 / UF-12

## Platform transition

- **Entry:** Explicit permission/Settings/provider handoff from existing app affordance; external surface has no app-authored PNG.
- **Return:** Platform/provider returns granted/denied/unavailable; app rechecks onResume and restores originating manual route.
- **Safe-terminal boundary:** Do not simulate platform dialog or auto-request; no microphone capture before explicit action/consent.
- **Back:** OS owns OS-surface Back; on return the app follows original screen's Back/Exit contract; no synthetic stack destination.
- **App high-fidelity PNG:** NONE BY DESIGN. A fabricated Android dialog/notification/lock-screen is prohibited.
- **App UI link:** owning WF-03 app surface is documented via associated app/variant coverage; never redraw OS UX.
- **State truth:** platform MediaSession, Android permission/route/focus/lifecycle or provider contract, not arbitrary Compose-local flags.
- **Access/consent:** no automatic permission or cloud consent inside active Driving.
- **Accessibility:** preserve system accessibility and focus on return; do not infer the OS dialog appearance.
- **Analytics:** aggregate category only; no raw audio/transcript, personal media metadata, precise position or Voice candidates.

## Source transition evidence

- https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/recovery-system/WF-10_CLOUD_VOICE_CONSENT_OUTSIDE_DRIVING.md

This state is a handoff/annotation and is deliberately not counted among the 45 app PNGs.
