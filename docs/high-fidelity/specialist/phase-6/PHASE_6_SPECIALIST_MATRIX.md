# Phase 6 — specialist Driving Mode and Voice UX pass

Result: PASS on source-locked STATIC reviewable design coverage; human/physical-device sign-off NOT asserted.

Source: frozen Issue 15 WF-07/08, GitHub UF-07..UF-10, Issue 16 accepted voice confirmation architecture, Phase 5 state IDs.
User reference RAR SHA-256: 0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8

## Acceptance invariants

- Driving Playing/Paused/No Track distinct; no screen-depth increase, no automatic entry, immediate Exit at all times.
- Last item absent means Play last hidden and all unsupported transport actions visibly disabled; with verified local item, Play last is conditional.
- Manual controls preserved by return from Voice; Exit remains exposed behind the overlay.
- Listening, Processing, Confirming, Executed and Failed have specialist images; all UF-08/09/10 error classes have dedicated evidence.
- Only eligible first settled STT failure offers one Retry, attempt 2 never offers another; no automatic capture.
- Only one confirmation with max two immutable options; neither/Cancel recover without loops; unsupported never approximated.
- TTS <=1 sentence after acknowledged execution, no success reversal on TTS failure; two executed rasters verified identical.
- No fake Android permission/system screen; no Settings/cloud-consent dialog inside Driving.
- All source IDs preserved; one MediaSession playback authority, one VoiceSessionCoordinator.

## 27 individually reviewable cases

| Scenario | WF | Existing State ID | PNG | Paired exact-copy spec |
|---|---|---|---|---|
| DRV_PLAYING | WF-07 | CC-05 | [DRV_PLAYING.png](DRV_PLAYING.png) | [DRV_PLAYING.md](DRV_PLAYING.md) |
| DRV_PAUSED | WF-07 | CC-06 | [DRV_PAUSED.png](DRV_PAUSED.png) | [DRV_PAUSED.md](DRV_PAUSED.md) |
| DRV_NO_TRACK | WF-07 | CC-07 | [DRV_NO_TRACK.png](DRV_NO_TRACK.png) | [DRV_NO_TRACK.md](DRV_NO_TRACK.md) |
| DRV_LAST_VERIFIED | WF-07 | CC-07 | [DRV_LAST_VERIFIED.png](DRV_LAST_VERIFIED.png) | [DRV_LAST_VERIFIED.md](DRV_LAST_VERIFIED.md) |
| DRV_RECONNECT | WF-07 | CC-03 | [DRV_RECONNECT.png](DRV_RECONNECT.png) | [DRV_RECONNECT.md](DRV_RECONNECT.md) |
| DRV_COMMAND_PENDING | WF-07 | CC-08 | [DRV_COMMAND_PENDING.png](DRV_COMMAND_PENDING.png) | [DRV_COMMAND_PENDING.md](DRV_COMMAND_PENDING.md) |
| DRV_ROUTE_SAFE | WF-07 | CC-16 | [DRV_ROUTE_SAFE.png](DRV_ROUTE_SAFE.png) | [DRV_ROUTE_SAFE.md](DRV_ROUTE_SAFE.md) |
| VO_LISTEN_1 | WF-08 | CD-03 | [VO_LISTEN_1.png](VO_LISTEN_1.png) | [VO_LISTEN_1.md](VO_LISTEN_1.md) |
| VO_LISTEN_2 | WF-08 | CD-04 | [VO_LISTEN_2.png](VO_LISTEN_2.png) | [VO_LISTEN_2.md](VO_LISTEN_2.md) |
| VO_PROCESSING | WF-08 | CD-05 | [VO_PROCESSING.png](VO_PROCESSING.png) | [VO_PROCESSING.md](VO_PROCESSING.md) |
| VO_COMMAND_PENDING | WF-08 | CD-06 | [VO_COMMAND_PENDING.png](VO_COMMAND_PENDING.png) | [VO_COMMAND_PENDING.md](VO_COMMAND_PENDING.md) |
| VO_CONFIRM_ONE | WF-08 | CD-09 | [VO_CONFIRM_ONE.png](VO_CONFIRM_ONE.png) | [VO_CONFIRM_ONE.md](VO_CONFIRM_ONE.md) |
| VO_CONFIRM_TWO | WF-08 | CD-10 | [VO_CONFIRM_TWO.png](VO_CONFIRM_TWO.png) | [VO_CONFIRM_TWO.md](VO_CONFIRM_TWO.md) |
| VO_EXECUTED | WF-08 | CD-07 | [VO_EXECUTED.png](VO_EXECUTED.png) | [VO_EXECUTED.md](VO_EXECUTED.md) |
| VO_TTS_UNAVAILABLE | WF-08 | CD-07 | [VO_TTS_UNAVAILABLE.png](VO_TTS_UNAVAILABLE.png) | [VO_TTS_UNAVAILABLE.md](VO_TTS_UNAVAILABLE.md) |
| VO_STT_TIMEOUT | WF-08 | CD-17 | [VO_STT_TIMEOUT.png](VO_STT_TIMEOUT.png) | [VO_STT_TIMEOUT.md](VO_STT_TIMEOUT.md) |
| VO_RETRY_AVAILABLE | WF-08 | CD-18 | [VO_RETRY_AVAILABLE.png](VO_RETRY_AVAILABLE.png) | [VO_RETRY_AVAILABLE.md](VO_RETRY_AVAILABLE.md) |
| VO_ATTEMPT2_TERMINAL | WF-08 | CD-19 | [VO_ATTEMPT2_TERMINAL.png](VO_ATTEMPT2_TERMINAL.png) | [VO_ATTEMPT2_TERMINAL.md](VO_ATTEMPT2_TERMINAL.md) |
| VO_ARTIST_NOT_FOUND | WF-08 | CD-13 | [VO_ARTIST_NOT_FOUND.png](VO_ARTIST_NOT_FOUND.png) | [VO_ARTIST_NOT_FOUND.md](VO_ARTIST_NOT_FOUND.md) |
| VO_UNKNOWN_PHRASE | WF-08 | CD-14 | [VO_UNKNOWN_PHRASE.png](VO_UNKNOWN_PHRASE.png) | [VO_UNKNOWN_PHRASE.md](VO_UNKNOWN_PHRASE.md) |
| VO_UNSUPPORTED | WF-08 | CD-15 | [VO_UNSUPPORTED.png](VO_UNSUPPORTED.png) | [VO_UNSUPPORTED.md](VO_UNSUPPORTED.md) |
| VO_MISSING_ARTIST | WF-08 | CD-16 | [VO_MISSING_ARTIST.png](VO_MISSING_ARTIST.png) | [VO_MISSING_ARTIST.md](VO_MISSING_ARTIST.md) |
| VO_CONFIRMATION_FAILED | WF-08 | CD-12 | [VO_CONFIRMATION_FAILED.png](VO_CONFIRMATION_FAILED.png) | [VO_CONFIRMATION_FAILED.md](VO_CONFIRMATION_FAILED.md) |
| VO_EXECUTION_REJECTED | WF-08 | CD-08 | [VO_EXECUTION_REJECTED.png](VO_EXECUTION_REJECTED.png) | [VO_EXECUTION_REJECTED.md](VO_EXECUTION_REJECTED.md) |
| VO_PROVIDER_UNAVAILABLE | WF-08 | CD-22 | [VO_PROVIDER_UNAVAILABLE.png](VO_PROVIDER_UNAVAILABLE.png) | [VO_PROVIDER_UNAVAILABLE.md](VO_PROVIDER_UNAVAILABLE.md) |
| VO_PERMISSION_DENIED | WF-08 | CD-21 | [VO_PERMISSION_DENIED.png](VO_PERMISSION_DENIED.png) | [VO_PERMISSION_DENIED.md](VO_PERMISSION_DENIED.md) |
| VO_MANUAL_ONLY | WF-08 | CD-20 | [VO_MANUAL_ONLY.png](VO_MANUAL_ONLY.png) | [VO_MANUAL_ONLY.md](VO_MANUAL_ONLY.md) |

## Confirmed Phase-5 defects and specialist overrides

- CD-13/14/15/16, CD-08 and CD-12 inherited a blanket Retry once button in generic Failed artwork; this specialist variant correctly returns to Manual and has no STT Retry.
- CC-07 previously rendered active Play last even without a verified last item. Specialist DRV_NO_TRACK omits it; DRV_LAST_VERIFIED shows the separate conditional same-state variant.
- CD-19 is terminal (no third Retry). TTS failure after CD-07 service ack does not change the Executed surface.
- This specialist artwork supersedes those conflicting generic cases in Phase 5; the original 126-state disposition remains unchanged.

## Important non-visual handoff requirements

- Cancel/Back/Exit invalidates Voice generation, releases microphone/duck and ignores late STT/TTS events.
- Audio-focus loss, reconnect, stale queue and route interruption are service-owned; safe paused with explicit manual resume if guard fails.
- Confirmation labels and local media metadata are for UI only, not logs, crash reports or analytics.
- P1 Bluetooth is only a dismissible suggestion outside Driving, never an automatic entrance.
- At 200 percent text scaling, Compose must reflow; screenshot review alone cannot prove runtime touch targets, actual TalkBack announcements or eyes-off-road safety.

## Complete canonical UF state trace

- [42-state UF-07..10 audit](PHASE_6_UF_STATE_AUDIT.md): all 18 Driving CC and 24 Voice CD IDs either specialist image or approved component/system owner spec.
- [22-case device QA handoff](PHASE_6_DEVICE_QA_HANDOFF.md): real-device execution pending.

## Evidence

- [Visual review board](PHASE_6_SPECIALIST_BOARD.png)
- [Machine-readable register and state transitions](PHASE_6_REGISTER.json)
- [Automated validation JSON](PHASE_6_VALIDATION.json)
- [Phase-5 parent matrix](../../HIFI_STATE_COVERAGE_MATRIX.md)

