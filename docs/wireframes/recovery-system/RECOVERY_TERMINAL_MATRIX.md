# Recovery Terminal Matrix

**Rule:** Every Phase 5 recovery ends with manual playback controls or a service-owned Safe Paused state. A valid acknowledged Playing continuation is allowed only when no failure terminal was entered or all Audio Focus guards pass.

| Recovery artifact | Trigger family | Mandatory terminal | Return rule |
|---|---|---|---|
| `WF-03_SYSTEM_MEDIA_PERMISSION_DIALOG` | Local media access is first required after a one-purpose explanation. | **MANUAL_PLAYBACK** | Granted resumes scan/readiness. Denied returns to app recovery with Retry and Not now. |
| `WF-03_SYSTEM_MICROPHONE_PERMISSION_DIALOG` | The user explicitly taps Mic or chooses Voice practice; never on app or Driving Mode entry. | **MANUAL_PLAYBACK** | Granted continues capability check. Denied returns to manual controls; permanent denial offers Settings only outside Driving Mode. |
| `WF-06_SYSTEM_NOTIFICATION_PERMISSION_BOUNDARY` | Only after acknowledged playback/background-control value exists. | **MANUAL_PLAYBACK** | Granted enables the surface; denied returns to app controls with truthful visibility status. |
| `WF-03_SYSTEM_ANDROID_SETTINGS_HANDOFF` | The user explicitly chooses Open Settings for permanent permission/provider recovery. | **MANUAL_PLAYBACK** | On return, the app rechecks Granted, requestable denial, or still permanent/unavailable as distinct outcomes. |
| `WF-10_CLOUD_VOICE_CONSENT_OUTSIDE_DRIVING` | An approved network-assisted Voice adapter is selected and complete disclosure exists. | **MANUAL_PLAYBACK** | Accept/Decline/Revoke persists a versioned local decision and returns to Settings/manual playback. |
| `WF-06_SYSTEM_MEDIA_NOTIFICATION_CONTROLS` | A valid active MediaSession exposes the platform notification surface. | **MANUAL_PLAYBACK** | Acknowledged command updates notification and app. Unavailable surface returns control to the app without fabricating stop. |
| `WF-06_SYSTEM_LOCK_SCREEN_CONTROLS` | Device locks or screen turns off while a valid MediaSession exists. | **MANUAL_PLAYBACK** | Unlock/app return reconnects to the complete current snapshot; surface absence is not playback failure. |
| `WF-06_SYSTEM_EXTERNAL_MEDIA_CONTROLS` | A supported Bluetooth/headset/vehicle media key issues a MediaSession command. | **MANUAL_PLAYBACK** | All visible surfaces refresh from the authoritative snapshot; unsupported/unavailable returns to app manual controls. |
| `WF-06_UI_RECREATION_SERVICE_RECONNECT` | UI recreation, return from background, task/OEM event or controller loss. | **MANUAL_PLAYBACK / SAFE_PAUSED** | Complete snapshot restores truthful Playing/Paused/Completed. Missing lifecycle may use validated P1 Paused Restore only. |
| `WF-06_SYSTEM_PHONE_CALL_AUDIO_FOCUS` | Transient/permanent Audio Focus loss, call, duck request or focus callback error. | **SAFE_PAUSED** | Valid transient gain may return to actual Playing; any failed guard or permanent loss remains Safe Paused with manual Resume. |
| `WF-06_SYSTEM_WIRED_HEADSET_ROUTE_RECOVERY` | Wired/headset route disconnects or reconnects. | **SAFE_PAUSED** | Reconnect rechecks route but never auto-resumes; explicit Play may resume after acknowledgement. |
| `WF-06_SYSTEM_BLUETOOTH_ROUTE_RECOVERY` | Selected/current Bluetooth output route disconnects or reconnects. | **SAFE_PAUSED** | Reconnect produces Safe Paused. Explicit manual Play revalidates route and waits for service acknowledgement. |
| `WF-08_DRIVING_VOICE_RETRY_MANUAL_FALLBACK` | The first explicit Voice attempt settles as timeout/no-match/provider/language/network/audio failure. | **MANUAL_PLAYBACK / SAFE_PAUSED** | Retry starts attempt two. Any second failure immediately dismisses to fixed manual controls; a new session needs a new mic tap. |

## Invariants

1. No failure creates a second player, queue or optimistic Playing state.
2. Manual playback means the user regains reachable manual controls over the actual service snapshot; it does not fabricate a playable item.
3. Safe Paused never resumes automatically after wired/Bluetooth loss or a failed focus guard.
4. Driving Voice has one explicit Retry; the second failure returns immediately to manual controls.
5. Android-owned dialogs, Settings, notification, lock screen and external controller surfaces remain system-only.
