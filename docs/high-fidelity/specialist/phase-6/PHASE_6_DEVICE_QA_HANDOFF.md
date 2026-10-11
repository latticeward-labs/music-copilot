# Phase 6 — runtime/manual specialist QA checklist

These are executable test specifications for Android/QA, not evidence of an APK/device run.

| Test | Situation | Action | Expected | Observed |
|---|---|---|---|---|
| T01 | Driving entry from player/library | Manual Enter; controller reconnect | Complete snapshot → CC-05/06/07; no auto Enter | NOT RUN (static Phase 6) |
| T02 | Driving Playing | Pause/Next/Prev/Seek | Actual MediaSession ack, no optimistic Playing | NOT RUN (static Phase 6) |
| T03 | Driving Paused | Manual Play | Only ack returns Playing; no auto-play | NOT RUN (static Phase 6) |
| T04 | Driving No Track | No last valid media | Play last absent; invalid transport inert; Exit + capability-gated Mic | NOT RUN (static Phase 6) |
| T05 | Driving optional Play last | MediaStore revalidation succeeds/fails | Exactly one valid local item guarded; failure stays No Track | NOT RUN (static Phase 6) |
| T06 | Immediate Driving Exit | Tap Exit or Back in pending/reconnect/no-track/voice | Immediate recorded origin; no service playback mutation | NOT RUN (static Phase 6) |
| T07 | Voice explicit start | Tap Mic while eligible | Only explicit tap opens 6s bounded Listening | NOT RUN (static Phase 6) |
| T08 | Voice STT timeout 1 | Wait 6s without result | Settled error → one Retry once, Manual available | NOT RUN (static Phase 6) |
| T09 | Voice second attempt timeout | Explicit Retry then 6s fail | No third Retry, terminal Manual | NOT RUN (static Phase 6) |
| T10 | Voice ambiguity 1 candidate | Present one immutable label | One choice + Neither/Cancel, one prompt only | NOT RUN (static Phase 6) |
| T11 | Voice ambiguity 2 candidates | Present two immutable labels | Exactly two current immutable options + Neither/Cancel | NOT RUN (static Phase 6) |
| T12 | Confirmation stale token | Cancel, then late confirm callback | No execution; ignore stale token | NOT RUN (static Phase 6) |
| T13 | Artist not found | Missing local artist | No nearest-match fallback, no command, no Retry loop | NOT RUN (static Phase 6) |
| T14 | Unsupported/unknown/missing artist | Speak out-of-grammar request | No approximation or second question; manual terminal | NOT RUN (static Phase 6) |
| T15 | Command acknowledgement | Service accepts command | Executed only after refreshed PlaybackSnapshot | NOT RUN (static Phase 6) |
| T16 | TTS port failure after acknowledgement | Disable/fail TTS after ack | Remain Executed, no rollback and no second command | NOT RUN (static Phase 6) |
| T17 | Provider unavailable/English unsupported | Mic capability precheck fails | Manual; no auto consent/Settings in Driving | NOT RUN (static Phase 6) |
| T18 | Permission denial/revocation | Denied mic while Driving | No fake Android dialog; Manual and immediate Exit | NOT RUN (static Phase 6) |
| T19 | Call/audio-focus and route disconnect | Loss while Driving / Voice active | Service safe paused; no speaker leak/surprise resume | NOT RUN (static Phase 6) |
| T20 | Cancellation cleanup | Back/Exit during Listening/Processing/Confirming | Release capture/duck; invalidate generation; no late speech action | NOT RUN (static Phase 6) |
| T21 | Accessibility 200% font | Compose on Android 10/12/14+ | No clipped critical actions; TalkBack order/labels, 48dp minimum | NOT RUN (static Phase 6) |
| T22 | Privacy/telemetry | Inspect logs at every Voice failure | No raw audio, transcript, candidate, score, title or Bluetooth ID | NOT RUN (static Phase 6) |

Only a real device/app test with evidence, reviewer name/date and results may replace NOT RUN. The static design gate does not claim these tests have passed.
