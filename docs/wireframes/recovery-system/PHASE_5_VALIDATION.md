# Phase 5 Validation — Recovery and System-only Transitions

**Date:** 2026-08-25  
**Issue:** [#15](https://github.com/latticeward-labs/music-copilot/issues/15)  
**Result:** **PASS at construction and repository-validation level; cross-role approval pending**

## Automated validation result

| Check | Expected | Observed | Result |
|---|---:|---:|---|
| Transition records in JSON register | 13 | 13 | PASS |
| Paired Markdown records | 13 | 13 | PASS |
| Paired SVG transition diagrams | 13 | 13 | PASS |
| Unique transition IDs | 13 | 13 | PASS |
| Canonical Android/System-only states | 20 | 20 | PASS |
| Missing canonical system-only states | 0 | 0 | PASS |
| Unexpected canonical system-only states | 0 | 0 | PASS |
| SVG files successfully rendered | 14 | 14 | PASS |
| SVG scripts, embedded images or external `href` | 0 | 0 | PASS |
| Invalid terminal enum values | 0 | 0 | PASS |

The 14 rendered SVG files are the 13 transition diagrams plus the recovery-terminal overview. Rendering was performed from source at native dimensions: each transition is `960×560`; the overview is `1100×720`.

## Requirement validation

| Requirement | Evidence | Result |
|---|---|---|
| Android permission dialogs are system-owned | Media, microphone and notification boundary records distinguish app rationale from the Android-owned dialog/result | PASS |
| Settings is a handoff, not an app screen | Settings record uses explicit external intent, fresh recheck and no assumed success | PASS |
| Media Notification is documented | MediaSession command → service acknowledgement → refreshed surface | PASS |
| Lock Screen is documented | Android-owned media surface with one MediaSession authority | PASS |
| External media controls are documented | Headset/Bluetooth/key events are serialized through MediaSession | PASS |
| UI recreation reconnects to Playback Service | Recreated UI connects, requests one complete snapshot and never creates a second player | PASS |
| Phone Call and Audio Focus are covered | Pause/duck policy, four resume guards and Safe Paused fallback are explicit | PASS |
| Wired route loss/reconnect is covered | Immediate pause prevents speaker leakage; reconnect never auto-resumes | PASS |
| Bluetooth route loss/reconnect is covered | Immediate pause, no auto-resume and manual route revalidation are explicit | PASS |
| Cloud Voice consent is outside Driving Mode | Consent is separate from Android permission and prohibited in active Driving Mode | PASS |
| Recovery terminals are bounded | Every record terminates in `MANUAL_PLAYBACK`, `SAFE_PAUSED`, or the explicitly bounded union | PASS |
| Driving Mode has one Retry | Exactly one explicit Retry; second failure returns immediately to fixed manual controls; no third attempt | PASS |

## Ownership and safety assertions

- Android/System surfaces are modeled as transition boundaries, not recreated as app-owned destination screens.
- Playback Service and MediaSession remain the only playback/queue/state authority across app and system surfaces.
- Reconnect, permission return and route recovery refresh authoritative truth before showing success.
- A route reconnect never implies consent to resume playback.
- Any failed or ambiguous recovery settles as Safe Paused or exposes Manual Playback controls.
- Cloud Voice disclosure/consent, permission requests and Settings handoffs are never launched inside active Driving Mode.
- Driving Voice releases capture and duck before presenting one Retry or manual controls.

## Accessibility and privacy review

- Each artifact defines TalkBack/reading order and focus return.
- State is communicated by text and shape; color is not the only signal.
- App actions are annotated with a minimum `48dp` touch target.
- System-owned controls inherit Android accessibility semantics rather than being imitated by app UI.
- Evidence excludes media identity, file paths, device identity, raw audio and transcripts.

## Approval gate

Construction and repository checks are complete. Issue #15 must remain **In Progress** until Product, Design, Android and QA reviewers record approval for cross-frame navigation, copy, platform behavior and testability.

