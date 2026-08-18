# UF-07 - Driving Mode Entry, Use, and Exit

**Product:** Music Copilot MVP v1.0  
**Cluster:** C - Driving  
**Priority:** P0 manual Driving Mode; P1 Bluetooth entry suggestion  
**Updated:** 18 August 2026  
**Status:** User Flow specification and traceability - **PASS / COMPLETE**  
**Tracking:** GitHub Issue #3  
**Source authority:** [`CLUSTER_C_SOURCE_AUTHORITY.md`](CLUSTER_C_SOURCE_AUTHORITY.md)  
**State inventory:** [`CLUSTER_C_STATE_INVENTORY.md`](CLUSTER_C_STATE_INVENTORY.md)  
**Traceability:** [`CLUSTER_C_TRACEABILITY_MATRIX.md`](CLUSTER_C_TRACEABILITY_MATRIX.md)  
**Canonical diagram:** [`mermaid/UF-07_DRIVING_MODE.md`](mermaid/UF-07_DRIVING_MODE.md)  
**Raw Mermaid source:** [`mermaid/UF-07_DRIVING_MODE.mmd`](mermaid/UF-07_DRIVING_MODE.mmd)

## Authority and boundary

UF-07 defines one implementation-independent journey: enter Driving Mode manually or by explicitly accepting an optional selected-device Bluetooth suggestion, use a single glance-minimized surface in Playing, Paused, or No Track state, and leave immediately without changing authoritative playback state.

The English PRD/MVP in `MSM2.rar` are the product authorities. The current GitHub ADR register records ADR-0001 through ADR-0006 as Accepted. Playback Service/MediaSession remains the sole owner of playback; Driving Mode observes it and submits commands through the same command boundary used by UF-05/UF-06.

This flow does not approve high-fidelity visuals, Android Auto, automatic launch, motion detection, voice execution, calls/routes policy, or implementation mechanics. Microphone action hands off to UF-08/UF-10. Calls, Audio Focus, headset/Bluetooth disconnect, route change, and speaker-leak prevention hand off to UF-11.

## Ten-field traceability record

| Required field | Controlled value |
|---|---|
| Flow ID | UF-07 |
| User goal | Control the current local music session with minimal looking and touching, while retaining an immediate return to the prior non-driving app surface. |
| Priority | P0: manual entry, Playing/Paused/No Track states, one-layer controls, microphone handoff, immediate Exit. P1: suggestion after a user-selected Bluetooth device connects; removable before P0. |
| User Story | **US-04:** While driving, I want to see only essential controls. |
| Functional Requirements | **FR-04 P0:** title, artist, artwork, and four large controls. **FR-07 P1:** manual activation or an explicit suggestion after a selected Bluetooth device connects. |
| Screens and states | Home/Library, Full Player, Driving Mode Playing, Driving Mode Paused, Driving Mode No Track, entry pending, command pending, Bluetooth suggestion, Exit/Back return, and downstream voice/interruption handoffs. |
| Influencing ADRs | ADR-0001 Accepted; ADR-0002 Accepted; ADR-0003 Accepted; ADR-0004/0005 only at the microphone handoff boundary; ADR-0006 Accepted. |
| Failure and recovery | Controller reconnect never fabricates state; rejected commands preserve the last verified state and refresh; No Track offers a valid last local item only when available; otherwise concise exit/manual recovery; voice failure returns to the same manual surface; route/interruption recovery is UF-11. |
| Data and permissions | Service/MediaSession is authoritative. Selected Bluetooth preference is local P1 setup owned by UF-12. No microphone capture begins until explicit microphone action. Telemetry is allowlist-only and never contains titles, artist names, artwork, device identity, route identity, or raw audio. |
| Open implementation decisions | Exact Compose layout and visual tokens (#15); navigation/controller/API mechanics and identity of the fourth large playback control if Product changes this controlled resolution (#16/#9); selected-device connection APIs/permissions (#9/#16); voice and route mechanics (UF-08 through UF-11). None changes the approved user-visible invariants. |

## Controlled resolution of the four large controls

The English sources require four large controls but do not name all four. To make UF-07 executable without inventing a new capability, Cluster C resolves the set from the existing P0 local-player and six-intent contracts as:

1. Previous
2. Play/Pause as one stateful control
3. Next
4. Seek Back

Microphone and Exit remain separate required actions. This resolution adds no Shuffle, Repeat, Queue editing, Browse, Search, Like, streaming, or playlist behavior. Exact placement, iconography, button order, and seek-back interval presentation remain downstream Wireframe/architecture decisions. The underlying Seek Back action uses the already approved bounded player behavior; it does not create a new seek surface.

## Preconditions and entry routes

| Entry route | Required behavior |
|---|---|
| Home/Library manual action | Always available when the app is at a non-driving surface. Connect to the active service/controller, obtain a complete snapshot, then enter Playing, Paused, or No Track. |
| Full Player manual action | Preserve the current service-owned queue, item, position, and Playing/Paused state. Enter without restarting or duplicating playback. |
| UF-06 returning synchronized surface | Use the verified MediaSession snapshot; do not create a second player or assume a state from stale UI. |
| Accepted P1 Bluetooth suggestion | Only a user-selected device connection may trigger a suggestion. Enter only after explicit acceptance. Never auto-open Driving Mode. |
| Dismissed/declined P1 suggestion | Remain on the current non-driving surface with playback unchanged. Do not repeat coercively during the same connection event. |
| Controller still connecting | Show a bounded entry-pending state; do not render fabricated metadata, zero position, empty queue, or guessed Playing/Paused state. Retry or cancel back to the prior surface. |

## P0 manual entry contract

1. Manual entry is the required P0 path and does not depend on Bluetooth, network access, microphone permission, or telemetry availability.
2. Entry requests a complete service/controller snapshot before Driving Mode controls are treated as ready.
3. Actual Playing enters Driving Mode Playing without restart or position reset.
4. Actual Paused/Ended enters Paused without auto-play.
5. No current playable item enters No Track; the UI never invents a queue or opens a long list.
6. The app records the last non-driving surface as the return target only. That return target is navigation context, not playback authority.
7. `driving_mode_started` is emitted only after the Driving Mode surface becomes active, and analytics failure never blocks entry.

## P1 selected-device Bluetooth suggestion

1. The user selects an eligible Bluetooth device in Settings/UF-12 before suggestions are allowed.
2. A connection event for that selected device may show one short suggestion outside active Driving Mode.
3. The suggestion offers explicit Enter and Not now/dismiss actions with equal, reachable treatment.
4. Acceptance follows the same snapshot-and-entry contract as manual entry.
5. Decline/dismiss keeps the current surface and playback state unchanged.
6. Connection never launches Driving Mode automatically, never starts playback, and never bypasses the lock screen or app navigation.
7. Device name/address/identifier is not approved telemetry. Only the Boolean `bt_connected` and controlled `entry_source` may accompany `driving_mode_started`.
8. Because FR-07 is P1, the suggestion may be removed if schedule capacity is unavailable; manual P0 entry and all Driving Mode states remain intact.

## One-layer Driving Mode contract

The surface contains only:

- title, artist, and artwork for the current verified item, or a concise No Track state;
- Previous, Play/Pause, Next, and Seek Back as four large fixed controls;
- one explicit microphone action;
- one immediate Exit action.

It contains no typing, Search field, Browse hierarchy, Queue list, Settings, nested menu, long result list, playlist editing, Like flow, content recommendation, or automatic navigation. Artwork absence uses a neutral placeholder and does not hide title/artist state. Metadata must remain readable, truncated safely, and never animate in a distracting way.

## Playback state and command contract

| State/action | Controlled behavior | Result/recovery |
|---|---|---|
| Playing | Show actual current item and Playing state from MediaSession. | Remain Playing until an acknowledged command or downstream system event changes the service state. |
| Paused/Ended | Show actual item and Play action; never auto-resume on entry. | Play requests service acknowledgement, then transitions to Playing only on success. |
| Previous | Submit the existing UF-05 Previous command. | The symbolic `T_PREVIOUS` policy remains Issue #16; UF-07 does not invent a different threshold. |
| Play/Pause | Submit state-aware Play or Pause to the same service boundary. | Update only after acknowledgement; rejection preserves and refreshes actual state. |
| Next | Submit the existing no-wrap Next command. | A valid next item enters Playing/Paused as reported; end of queue remains truthful and does not invent Repeat. |
| Seek Back | Submit the existing bounded Seek Back command. | Clamp through the playback boundary; no optimistic position authority in the UI. |
| Command pending | Keep the last verified state and mark the initiating control pending without blocking Exit. | Acknowledgement refreshes all fields; reject/timeout refreshes actual state with a short recoverable indication. |
| Microphone | Begin only after explicit tap and hand off to UF-08/UF-10. | Driving surface remains the initiating context; Voice is an overlay/state, not a new navigation destination. |

## No Track contract

1. No Track is a first-class terminal/usable state, not a crash or empty fake player.
2. No track metadata or artwork is fabricated.
3. If a valid, accessible last local item exists, the Play action may offer that single bounded fallback after revalidation.
4. If no valid last local item exists, show one short message and keep immediate Exit/manual recovery available.
5. Do not open Library, Search, Queue, Settings, or a long choice list inside Driving Mode.
6. The user may Exit to the last non-driving surface, fallback Home/Library, and recover through UF-03/UF-04.

## Immediate Exit and system Back

1. Exit and system Back are equivalent and always reachable, including entry pending, Playing, Paused, No Track, command pending, and after a failed voice overlay returns.
2. Exit never waits for telemetry, Bluetooth, artwork, controller metadata, or a non-critical UI task.
3. Exit returns to the most recent valid non-driving app surface; if it no longer exists, fallback is Home/Library.
4. Exit does not Stop, Pause, Play, restart, clear, or rebuild the queue. Playback continues or remains paused exactly as the service reports.
5. Exiting voice overlay first returns to the initiating Driving Mode state; explicit Driving Mode Exit then follows this same rule.

## Failure and downstream handoffs

| Condition | UF-07 behavior | Owner after handoff |
|---|---|---|
| Controller unavailable/reconnecting | Keep bounded pending context, allow Exit, retry snapshot; never create a replacement player. | UF-06 / Issue #16 |
| Media access revoked or current item inaccessible | Show concise unavailable/No Track behavior; Exit/manual recovery. | UF-02/UF-03/UF-05 |
| Corrupt item or queue skip | Render the service-acknowledged next/terminal state only. | UF-05/UF-11 |
| Microphone permission/provider/recognition failure | Manual controls and Exit remain available; Voice overlay dismisses safely. | UF-08/UF-10/UF-12 |
| Call, Audio Focus, headset/Bluetooth disconnect, route change | Do not invent local policy; immediately reflect the service state and preserve Exit. | UF-11 |
| Bluetooth suggestion API/permission unavailable | Manual P0 entry remains unaffected; omit the P1 suggestion. | Issue #9/#16 |
| Analytics/quality service unavailable | No product behavior changes and no control is blocked. | Quality adapter boundary |

## Analytics and privacy

| Event | When | Allowed properties |
|---|---|---|
| `driving_mode_started` | After successful visible entry | `entry_source` (`manual` or `bluetooth_suggestion`), `bt_connected` Boolean |
| `session_completed` | After a Driving Mode session ends | `duration_bucket`, `commands_count` |

Prohibited payloads include full title, artist, album, filename, artwork, library contents, selected Bluetooth device name/address/identifier, raw audio, transcript, exact position, precise location, contacts, and unnecessary persistent identifiers. Telemetry is non-blocking and cannot become a playback or navigation source of truth.

## Accessibility and driving constraints

- Minimum touch target is 48dp; the four primary controls should be larger.
- High contrast, scalable text, TalkBack labels, stable reading/focus order, and non-color-only Playing/Paused/pending state are required.
- Title/artist overflow is truncated without a marquee or high-motion treatment.
- Motion is limited and never delays Exit.
- Feedback is short; no modal chain, deep navigation, keyboard, or long list exists in Driving Mode.
- User-facing copy must not claim that Music Copilot makes driving safe. Attention to the road remains the driver's responsibility.

## Terminal states and exits

| Terminal/steady state | Required outcome |
|---|---|
| Playing | One-layer surface shows actual item and Playing state; manual controls, microphone, and Exit remain available. |
| Paused | Same surface shows Paused; Play is available; no surprise resume. |
| No Track | Concise truthful state; at most one valid-last-track Play fallback; immediate Exit/manual recovery. |
| Immediate Exit | Return to last non-driving surface or Home/Library fallback with playback unchanged. |
| Voice handoff | Overlay begins only after explicit microphone action; manual surface remains the return context. |
| Interruption/route handoff | Service result is reflected; detailed recovery remains UF-11. |

## Explicit exclusions

No Android Auto, CarPlay, Cast, motion detection, automatic Driving Mode launch, wake word, always-listening microphone, Search/typing, Browse/Queue lists, nested Settings, streaming, catalog, account, backend, sync, upload, download, recommendation, social feature, or v1.1 capability is approved here.

## UF-07 completion checklist

- [x] English `MSM2.rar` authority and current GitHub ADR status are fixed.
- [x] Manual P0 and P1 Bluetooth suggestion paths are separated.
- [x] Playing, Paused, No Track, entry pending, command pending, and immediate Exit are defined.
- [x] Four large controls, microphone, title/artist/artwork, and immediate Exit are represented on one layer.
- [x] Exit/Back returns to the last non-driving surface without changing playback.
- [x] No Track has a bounded fallback and no long list.
- [x] Service/MediaSession remains the sole playback authority.
- [x] Voice, route/interruption, and implementation boundaries have explicit handoffs.
- [x] Privacy-safe analytics and prohibited payloads are explicit.
- [x] Accessibility and driving constraints are testable.
- [x] Mermaid Markdown/raw-source parity is validated.
- [x] No outside-MVP capability is introduced.

## Completion boundary

**UF-07 / Cluster C passes the User Flow specification, shared state, traceability, diagram, source-alignment, and connected-review gates.** This does not claim that Issue #9 implementation, stationary/in-car QA, low-fidelity Wireframes, final Software Architecture, UF-08 through UF-13, or Issue #3 as a whole are complete.

