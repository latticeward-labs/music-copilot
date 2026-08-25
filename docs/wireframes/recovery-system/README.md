# Phase 5 — Recovery and System-only Transitions

**Status:** **PASS / COMPLETE at construction level; cross-role approval pending**  
**Issue:** [#15](https://github.com/latticeward-labs/music-copilot/issues/15)  
**Register:** **13 paired Markdown/SVG artifacts**  
**Canonical system-only coverage:** **20/20**  
**Terminal classes:** `MANUAL_PLAYBACK` and `SAFE_PAUSED` only  
**Validation:** [PHASE_5_VALIDATION.md](PHASE_5_VALIDATION.md)

The diagrams are ownership-separated transitions, not invented Android or app screens. Every recovery returns to manual playback controls or a Safe Paused service state.

| # | Record | Diagram | Canonical States | Classification | Safe terminal |
|---:|---|---|---|---|---|
| 1 | [`WF-03_SYSTEM_MEDIA_PERMISSION_DIALOG`](WF-03_SYSTEM_MEDIA_PERMISSION_DIALOG.md) | [SVG](WF-03_SYSTEM_MEDIA_PERMISSION_DIALOG.svg) | SYS-01, S-03 | Android/System-only transition | **MANUAL_PLAYBACK** |
| 2 | [`WF-03_SYSTEM_MICROPHONE_PERMISSION_DIALOG`](WF-03_SYSTEM_MICROPHONE_PERMISSION_DIALOG.md) | [SVG](WF-03_SYSTEM_MICROPHONE_PERMISSION_DIALOG.svg) | SYS-02, S-03, CA-02, CD-21 | Android/System-only transition | **MANUAL_PLAYBACK** |
| 3 | [`WF-06_SYSTEM_NOTIFICATION_PERMISSION_BOUNDARY`](WF-06_SYSTEM_NOTIFICATION_PERMISSION_BOUNDARY.md) | [SVG](WF-06_SYSTEM_NOTIFICATION_PERMISSION_BOUNDARY.svg) | SYS-03, CB-30 | Android/System-only transition | **MANUAL_PLAYBACK** |
| 4 | [`WF-03_SYSTEM_ANDROID_SETTINGS_HANDOFF`](WF-03_SYSTEM_ANDROID_SETTINGS_HANDOFF.md) | [SVG](WF-03_SYSTEM_ANDROID_SETTINGS_HANDOFF.svg) | SYS-04, CE-18, CE-19 | Android/System-only transition | **MANUAL_PLAYBACK** |
| 5 | [`WF-10_CLOUD_VOICE_CONSENT_OUTSIDE_DRIVING`](WF-10_CLOUD_VOICE_CONSENT_OUTSIDE_DRIVING.md) | [SVG](WF-10_CLOUD_VOICE_CONSENT_OUTSIDE_DRIVING.svg) | SYS-05, CD-23, CE-21, CE-22 | App/provider boundary recovery | **MANUAL_PLAYBACK** |
| 6 | [`WF-06_SYSTEM_MEDIA_NOTIFICATION_CONTROLS`](WF-06_SYSTEM_MEDIA_NOTIFICATION_CONTROLS.md) | [SVG](WF-06_SYSTEM_MEDIA_NOTIFICATION_CONTROLS.svg) | CB-22, CB-26, CB-29, CB-30, CB-31 | Android/System-only transition | **MANUAL_PLAYBACK** |
| 7 | [`WF-06_SYSTEM_LOCK_SCREEN_CONTROLS`](WF-06_SYSTEM_LOCK_SCREEN_CONTROLS.md) | [SVG](WF-06_SYSTEM_LOCK_SCREEN_CONTROLS.svg) | CB-24, CB-27, CB-29, CB-31 | Android/System-only transition | **MANUAL_PLAYBACK** |
| 8 | [`WF-06_SYSTEM_EXTERNAL_MEDIA_CONTROLS`](WF-06_SYSTEM_EXTERNAL_MEDIA_CONTROLS.md) | [SVG](WF-06_SYSTEM_EXTERNAL_MEDIA_CONTROLS.svg) | CB-28, CB-29, CB-31 | Android/System-only transition | **MANUAL_PLAYBACK** |
| 9 | [`WF-06_UI_RECREATION_SERVICE_RECONNECT`](WF-06_UI_RECREATION_SERVICE_RECONNECT.md) | [SVG](WF-06_UI_RECREATION_SERVICE_RECONNECT.svg) | CB-21, CB-22, CB-23, CB-25, CB-32, CB-33 | App recovery transition | **MANUAL_PLAYBACK / SAFE_PAUSED** |
| 10 | [`WF-06_SYSTEM_PHONE_CALL_AUDIO_FOCUS`](WF-06_SYSTEM_PHONE_CALL_AUDIO_FOCUS.md) | [SVG](WF-06_SYSTEM_PHONE_CALL_AUDIO_FOCUS.svg) | CC-16, CE-02, CE-03, CE-04, CE-05, CE-06, CE-10, CE-11 | Android/System-only transition | **SAFE_PAUSED** |
| 11 | [`WF-06_SYSTEM_WIRED_HEADSET_ROUTE_RECOVERY`](WF-06_SYSTEM_WIRED_HEADSET_ROUTE_RECOVERY.md) | [SVG](WF-06_SYSTEM_WIRED_HEADSET_ROUTE_RECOVERY.svg) | CC-16, CE-07, CE-08, CE-09 | Android/System-only transition | **SAFE_PAUSED** |
| 12 | [`WF-06_SYSTEM_BLUETOOTH_ROUTE_RECOVERY`](WF-06_SYSTEM_BLUETOOTH_ROUTE_RECOVERY.md) | [SVG](WF-06_SYSTEM_BLUETOOTH_ROUTE_RECOVERY.svg) | CB-28, CC-16, CE-07, CE-08, CE-09 | Android/System-only transition | **SAFE_PAUSED** |
| 13 | [`WF-08_DRIVING_VOICE_RETRY_MANUAL_FALLBACK`](WF-08_DRIVING_VOICE_RETRY_MANUAL_FALLBACK.md) | [SVG](WF-08_DRIVING_VOICE_RETRY_MANUAL_FALLBACK.svg) | CC-14, CD-17, CD-18, CD-19, CD-20, CD-24 | App recovery transition | **MANUAL_PLAYBACK / SAFE_PAUSED** |

## Required outcomes

- Android permission dialogs and Settings are system-owned handoffs.
- Notification, lock screen and external controls converge on one MediaSession.
- UI recreation reconnects to one complete service snapshot.
- Phone/focus and wired/Bluetooth route loss settle safely.
- Cloud Voice consent exists only outside active Driving Mode.
- Driving Voice exposes exactly one Retry, then immediate manual controls.
