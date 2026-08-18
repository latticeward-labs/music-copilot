# TS-05 - Headset/Bluetooth Route Safety

**Question:** Can the flow prevent unintended speaker leakage and surprise reconnect playback?  
**Result:** **PASS at executable reference-policy level; physical speaker-leak proof remains #8/#13**

## Executable contract

- Disconnect invalidates the route generation and immediately issues Pause when active.
- Active Voice is cancelled and stale callbacks are rejected.
- The disconnected state cannot accept auto-resume.
- Reconnect marks a route available but remains Paused until explicit Resume and service acknowledgement.
- Rapid/duplicate connect-disconnect events are idempotent.
- Diagnostics use coarse route category only.

## Automated rows

Disconnect while Playing, disconnect while Paused, duplicate disconnect, reconnect, focus gain after disconnect, explicit resume on valid route, corrupt-current with a playable next item, and no-playable-item end behavior are covered by host policy tests.

## Non-claim

An event-policy test cannot hear phone-speaker leakage. Physical Android 10/12/14+, Pixel/Samsung/Xiaomi, and at least three head-unit/vehicle executions remain Issue #13 release evidence.
