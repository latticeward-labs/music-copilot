# Phase 7 system-only ownership handoff — SYS-05 / Speech provider/cloud-processing boundary

**Disposition:** SYSTEM_ONLY, NO APP SCREEN, runtime QA NOT RUN.
**Existing REAL state owner (unaltered):** Android/System + provider
**Platform screen owner (unaltered):** Speech provider / consent boundary
**Return/recovery owner (unaltered):** UF-10 / UF-12
**UF:** UF-02 / UF-10 / UF-12; **WF:** WF-03
**Source spec:** [SYS-05.md](../../system-handoffs/SYS-05.md)

## Actual platform transition and return

### WF-10_CLOUD_VOICE_CONSENT_OUTSIDE_DRIVING / Cloud Voice consent outside Driving Mode
- Trigger: An approved network-assisted Voice adapter is selected and complete disclosure exists.
- Handoff to REAL owner: App shows purpose, processing destination and retention disclosure outside Driving Mode; this is consent, not Android permission.
- Authoritative playback/service state: Decline or missing disclosure keeps Voice Manual Only and never affects local playback.
- Return recheck (never infer from onResume alone): Accept/Decline/Revoke persists a versioned local decision and returns to Settings/manual playback.
- Safe terminal: MANUAL_PLAYBACK
- Retry/recovery bounds: No coercive countdown, preselection or repeated prompt; future change is user-initiated in Settings.

## Android accessibility and privacy boundary

- App owns spoken purpose on its pre-handoff action only; Android/provider owns dialog/Settings/lockscreen/notification/vehicle TalkBack labels, focus and drawing.
- After system return, re-query actual grant/MediaSession/route/provider; restore focus to original app action or safe recovery status.
- Do not create a fake app UI representation of this native system surface or add a new navigation destination.
- No automatic microphone/Settings/consent request from Driving; no Android Auto MVP.
- EXCLUDE from repository evidence, event payload, logs, screenshots/crash reports: raw microphone audio, full transcript, actual user file titles/artist/paths/URI, Voice candidate labels/tokens, exact speech scores, precise playback position, exact location/GPS, and device/Bluetooth identifiers. Use generic synthetic fixtures and redact screenshots. Telemetry is limited to approved coarse status/intent enum/latency bucket after the privacy gate.
- Cloud processing disabled until separately approved provider/legal privacy review (PH10-MIN-002).

## Real QA evidence (NOT RUN)

- [ ] Native platform handoff/return tested on Android 10/12/14+.
- [ ] TalkBack OS ownership and app-origin focus restoration captured with redacted test content.
- [ ] Safe paused/manual terminal and fresh authoritative recheck verified.
- [ ] No invented app screen or personal data leakage in evidence.
