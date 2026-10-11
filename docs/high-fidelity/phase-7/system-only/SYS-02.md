# Phase 7 system-only ownership handoff — SYS-02 / Android microphone permission dialog

**Disposition:** SYSTEM_ONLY, NO APP SCREEN, runtime QA NOT RUN.
**Existing REAL state owner (unaltered):** Android/System
**Platform screen owner (unaltered):** Android permission/Settings
**Return/recovery owner (unaltered):** UF-02
**UF:** UF-02; **WF:** WF-03
**Source spec:** [SYS-02.md](../../system-handoffs/SYS-02.md)

## Actual platform transition and return

### WF-03_SYSTEM_MICROPHONE_PERMISSION_DIALOG / Android microphone permission dialog
- Trigger: The user explicitly taps Mic or chooses Voice practice; never on app or Driving Mode entry.
- Handoff to REAL owner: App launches the Android-owned microphone dialog after purpose text.
- Authoritative playback/service state: Playback remains service-owned and usable; capture starts only after a fresh Granted result.
- Return recheck (never infer from onResume alone): Granted continues capability check. Denied returns to manual controls; permanent denial offers Settings only outside Driving Mode.
- Safe terminal: MANUAL_PLAYBACK
- Retry/recovery bounds: Requestable denial may retry. No automatic dialog loop and no capture during Retry choice.

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
