# UF-11 - Calls, Audio Focus, Audio Route, and Corrupt Queue Recovery

**Cluster:** E - Recovery and control  
**Priority:** P0  
**Updated:** 18 August 2026  
**Status:** **PASS / COMPLETE at User Flow and executable policy-contract level**  
**Tracking:** Issues #3, #8, #13, #16

## Ten-field traceability record

| Required field | Controlled value |
|---|---|
| Flow ID | UF-11 |
| User goal | Continue safely after a call/focus/route interruption without overlap, surprise resume, crash, or speaker leakage. |
| Priority | P0 |
| Product mapping | FR-03; Phase-0 decisions D-018/D-019/D-022; system states SYS-07/SYS-08/SYS-09. |
| Screens and states | System-only focus/call/route transitions; Full Player, Driving Mode, notification and lock screen observe the same service snapshot. |
| Influencing ADRs | ADR-0002 service-owned playback; ADR-0003 coarse diagnostics; ADR-0004 Voice cancellation; ADR-0006 no new backend dependency. |
| Failure and recovery | Conditional focus resume; disconnect immediate pause; reconnect explicit resume; controlled corrupt skip/end; manual controls always recover. |
| Source of truth | Playback Service/MediaSession, current valid audio route, and typed interruption policy. UI never owns playback. |
| Analytics | `player_error` may include controlled error code and coarse route type only; `session_completed` uses approved buckets. |
| Open implementation evidence | Final Android callback mapping, OEM ordering, phone-call integration, real speaker-leak proof, and vehicle matrix remain #8/#13/#16. |

## Preconditions and global invariants

- A single service-owned player/queue exists and has a current authoritative snapshot.
- Every system callback is normalized into one typed interruption event before policy evaluation.
- Voice Listening/Processing/Confirming is subordinate to calls/focus/routes and is cancelled on a safety interruption.
- Manual Pause clears any pending auto-resume eligibility.
- UI recreation, notification and controller reconnection cannot replay the system event or issue a duplicate command.

## Call and Audio Focus path

1. On incoming call or transient focus loss, snapshot whether playback was actually Playing.
2. Cancel active Voice capture/confirmation, invalidate its callback token, clear Voice ducking, and pause playback when required.
3. Publish the authoritative Paused/Interrupted state to all observers.
4. On focus gain, resume only if all are true: playback was Playing, interruption created the pause, no manual pause occurred, and the same valid route remains.
5. If any guard fails, remain Paused and show immediate manual Resume.
6. Permanent focus loss clears eligibility and never auto-resumes.
7. Duplicate gain/loss/call callbacks are idempotent.

## Route path

1. Wired-headset or Bluetooth disconnect immediately invalidates the route and pauses before phone-speaker continuation.
2. Active Voice is cancelled and stale recognition/TTS callbacks are ignored.
3. UI/system surfaces refresh to Paused with a short route-loss reason; no modal navigation is required.
4. Reconnect marks the new route available but never resumes automatically.
5. Explicit user Resume rechecks route validity before service acknowledgement.
6. Rapid connect/disconnect ordering cannot issue more than one Pause or any hidden Resume.

## Focus decision table

| Normalized event | Required result | Auto-resume eligibility |
|---|---|---|
| Transient loss while Playing | Pause and remember interruption ownership | Yes, only with all four guards |
| Transient loss while already Paused | Remain Paused | No |
| Duck-capable transient loss | Duck active playback; clear duck on terminal/gain | No new playback start |
| Incoming call | Cancel Voice, clear duck, pause if Playing | Conditional four-guard resume |
| Permanent loss | Pause/stop active progression according to service policy | Never |
| Focus gain after manual Pause | Remain Paused | Never |
| Focus gain after route generation changed | Remain Paused | Never |

## Corrupt queue item path

- Mark the current inaccessible/corrupt item unavailable without deleting the user's file.
- If a validated playable next item exists, skip once and continue from the same queue authority.
- If none remains, end/pause the queue and expose one concise error plus manual library recovery.
- Never loop indefinitely, rebuild a queue in UI, or report successful playback before service acknowledgement.

## Accessibility, driving, and privacy

Driving Mode keeps fixed controls and immediate Exit; interruption feedback is short, non-color-only, TalkBack-readable, and never demands typing or a long list. Route data is coarse (`bluetooth`, `wired`, `speaker`, `unknown`) and no device name/address, call identity, title, artist, path, or stable identifier enters telemetry.

## Completion boundary

UF-11 closes the deterministic User Flow and executable reference-policy seam. It does not claim physical call/focus/route behavior on Pixel/Samsung/Xiaomi or any vehicle/head unit; those remain release-blocking evidence in #8/#13.
