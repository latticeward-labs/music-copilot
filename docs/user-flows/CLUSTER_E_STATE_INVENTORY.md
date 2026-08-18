# Cluster E - Shared State Inventory

**Scope:** UF-11 and UF-12  
**Date:** 18 August 2026  
**Status:** **PASS - normalized CE-01 through CE-26**

## Authority and ownership

- Service/MediaSession owns live playback/queue; the current platform route/focus state gates output.
- Preferences owns small settings/consent; app repositories own short history and queued diagnostics.
- Platform permissions and provider capability are re-read, never copied as permanent app truth.
- Telemetry/provider adapters never become product state authorities.

| ID | Flow | State | Trigger / precondition | Visible information / actions | Work, truth, persistence | Success | Failure / recovery | Privacy, accessibility, driving |
|---|---|---|---|---|---|---|---|---|
| CE-01 | 11 | Stable Playing | Service playing on valid route | Synced player surfaces | Service snapshot authoritative | System event -> CE-02/07/10 | Player failure -> CE-12 | No personal route identity |
| CE-02 | 11 | Transient focus/call handling | Call or transient loss | Short interrupted state; manual Pause/Exit | Snapshot prior Playing; cancel Voice | Pause/duck -> CE-03/04 | Callback error -> CE-11 | No call identity; concise feedback |
| CE-03 | 11 | Interrupted Paused / resume eligible | Policy-owned pause | Paused; manual action reachable | Four resume guards tracked transiently | Valid gain -> CE-05 | Manual pause/route change -> CE-06 | No surprise output |
| CE-04 | 11 | Temporarily Ducked | Duck-capable focus event | Playing at reduced level | Duck is service effect, not UI authority | Gain -> CE-01 | Loss/call/route -> CE-03/07 | Non-color state cue where shown |
| CE-05 | 11 | Conditional Resume acknowledged | All four guards pass | Playing snapshot after ack | Service executes one Resume | -> CE-01 | Reject -> CE-06 | No optimistic UI |
| CE-06 | 11 | Safe Paused / manual resume | Any guard fails or permanent loss | Paused + explicit Resume | Auto-resume eligibility cleared | Explicit Resume + valid route -> CE-01 | Invalid route remains CE-06 | Large manual action in Driving Mode |
| CE-07 | 11 | Route Disconnect handling | Wired/Bluetooth loss | Immediate Paused/route-lost cue | Invalidate route generation; cancel Voice | -> CE-08 | Callback disorder stays CE-08 | Prevent speaker leakage |
| CE-08 | 11 | Disconnected Safe Paused | No approved route | Paused; Exit/manual controls | No playback command may start on unsafe route | Reconnect -> CE-09 | Remain paused | Coarse route only |
| CE-09 | 11 | Reconnected Safe Paused | Route available again | Paused + explicit Resume | Recheck route; never auto-resume | Resume ack -> CE-01 | Route lost -> CE-08 | Device name/address prohibited telemetry |
| CE-10 | 11 | Permanent Focus Loss | Permanent loss | Paused/ended state | Eligibility cleared | Manual future start only | Service error -> CE-11 | No hidden retry |
| CE-11 | 11 | Interruption Policy Error | Typed callback/service rejection | Short failure; manual controls | Refresh service/route truth | Recover -> CE-06/08 | Persistent -> manual/Exit | Coarse error code only |
| CE-12 | 11 | Corrupt Current Item | Decoder/access error | Short skipping cue | Mark item unavailable; validate next | Next exists -> CE-13 | None -> CE-14 | Never log media identity |
| CE-13 | 11 | Controlled Skip | Valid next item | Updated playing/paused snapshot | One service-owned skip | -> CE-01/06 | New corrupt -> bounded re-evaluation | No loop/duplicate dispatch |
| CE-14 | 11 | Queue Ended by Corruption | No playable item remains | End/Paused + library recovery | Queue remains authoritative; no file deletion | Manual selection -> UF-04/05 | Permission -> UF-02/03 | Concise error |
| CE-15 | 12 | Settings Default | Settings opened outside driving | Current sections/values | Observe preferences/platform truth | Select section -> CE-16/18/20/22 | Load error -> CE-17 | TalkBack headings, scalable text |
| CE-16 | 12 | Setting Update Pending | Valid change submitted | Pending state, Cancel if safe | Atomic preference write | Ack -> CE-17 | Reject -> CE-15 | No value telemetry |
| CE-17 | 12 | Settings Updated / Error | Write/recheck result | Updated or retryable error | Previous value retained on failure | Continue -> CE-15 | Retry -> CE-16 | Clear non-color result |
| CE-18 | 12 | Android Settings Handoff | Explicit permission/provider action | Leaving app notice | No assumption of success | Return -> CE-19 | Cancel -> CE-15 | Never launched from active Driving Mode |
| CE-19 | 12 | Platform Recheck | Return to app | Short checking state | Re-read permission/provider | Ready -> CE-17 | Denied/unavailable -> CE-17 | Manual player preserved |
| CE-20 | 12 | Bluetooth Selection | P1 setup selected | Eligible devices/remove option | Store selected identity locally | Save -> CE-17 | Permission/unavailable -> CE-17/18 | No auto Driving launch |
| CE-21 | 12 | Privacy Details | Privacy opened | Categories, owners, allowed/prohibited data | Read-only approved policy | Back -> CE-15 | Missing policy disables collection | Plain-language, accessible |
| CE-22 | 12 | Consent Decision | Complete cloud disclosure exists | Accept/Decline/Revoke | Persist versioned decision locally | Accepted/declined -> CE-17 | Incomplete disclosure -> Manual Only | No dark pattern |
| CE-23 | 12 | Clear Selection | Clear data opened | History/diagnostics independent choices | Protected categories absent | Continue -> CE-24 | None selected -> remain | No preselected destructive choice |
| CE-24 | 12 | Clear Confirmation | At least one category selected | Names selected + preserved categories | No deletion yet | Confirm -> CE-25 | Cancel -> CE-23/15 | Explicit destructive confirmation |
| CE-25 | 12 | Clearing | Confirmed request | Progress; no duplicate submit | Idempotent local/provider plan | All done -> CE-26 | Partial/fail -> CE-26 | Playback unaffected |
| CE-26 | 12 | Clear Result | Plan settled | Cleared / partial / failed + retry | Per-boundary truth; retain unresolved work | Done -> CE-15 | Retry only failed boundary | Never claim remote deletion falsely |

## Coverage decision

CE-01 through CE-26 cover focus/call/duck/pause/resume, route loss/reconnect, corrupt media, Settings update, system recheck, Bluetooth suggestion setup, consent, privacy, selected clearing, partial failure, accessibility, and driving restrictions without adding a second playback authority or prohibited data path.
