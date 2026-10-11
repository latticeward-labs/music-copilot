# Phase 7 system-only ownership handoff — SYS-03 / Notification permission boundary

**Disposition:** SYSTEM_ONLY, NO APP SCREEN, runtime QA NOT RUN.
**Existing REAL state owner (unaltered):** Android/System
**Platform screen owner (unaltered):** Android notification permission
**Return/recovery owner (unaltered):** UF-06 / UF-12
**UF:** UF-06; **WF:** WF-06
**Source spec:** [SYS-03.md](../../system-handoffs/SYS-03.md)

## Actual platform transition and return

### WF-06_SYSTEM_NOTIFICATION_PERMISSION_BOUNDARY / Notification permission and visibility boundary
- Trigger: Only after acknowledged playback/background-control value exists.
- Handoff to REAL owner: Android permission/visibility result controls surface availability, not playback truth.
- Authoritative playback/service state: Playback and MediaSession continue when notification access is denied where platform policy permits.
- Return recheck (never infer from onResume alone): Granted enables the surface; denied returns to app controls with truthful visibility status.
- Safe terminal: MANUAL_PLAYBACK
- Retry/recovery bounds: Settings recovery is explicit and outside Driving Mode; no repeated prompt.

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
