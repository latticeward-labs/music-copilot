# UF-08 - Direct Voice Command

**Cluster:** D - Voice certainty and recovery  
**Priority:** P0  
**Updated:** 18 August 2026  
**Status:** **PASS / COMPLETE at User Flow contract level**  
**Tracking:** Issues #3, #10, #12, #16

## Ten-field traceability record

| Required field | Controlled value |
|---|---|
| Flow ID | UF-08 |
| User goal | Issue one short supported command without leaving the current playback surface. |
| Priority | P0 |
| User Story | US-05: clear commands execute directly with short spoken feedback and no new screen. |
| Functional Requirements | FR-05 explicit mic/listening/ducking/restricted mapping; FR-06 direct execution for a certain supported command. |
| Screens and states | Driving Mode or Full Player initiating surface; Voice Overlay Listening, Processing, Executed, Failed-safe return. |
| Influencing ADRs | ADR-0002 playback authority; ADR-0004 replaceable STT/TTS; ADR-0005 deterministic six-intent mapping; ADR-0003/0006 privacy boundaries. |
| Failure and recovery | Permission/provider/language/network/recognition failures hand to UF-10; uncertain results hand to UF-09; execution rejection refreshes actual playback and returns manual control; TTS failure does not undo execution. |
| Data and permissions | Microphone begins only after explicit action. No raw audio storage. Provider confidence is transient. Telemetry is typed/allowlisted and contains no transcript or personal media metadata. |
| Open implementation decisions | Adapter/lifecycle ownership, final threshold adoption, exact TTS copy, and production analytics wiring remain #10/#12/#16 without changing this contract. |

## Preconditions

- The user is on Driving Mode or Full Player with manual controls available.
- Playback Service/MediaSession is the sole playback authority and supplies the last verified snapshot.
- Microphone permission and an approved speech mode are evaluated only after explicit mic action.
- If a reviewed network-assisted mode is selected, separate consent and complete disclosure already exist; otherwise that path is disabled.

## Direct path

1. The user taps the microphone once. No background trigger or wake word exists.
2. A Voice Overlay enters Listening, visually announces microphone activity, ducks playback, and starts a maximum six-second capture.
3. Cancel remains reachable. The initiating surface remains the return context and no navigation destination is pushed.
4. STT returns transient text, optional confidence, or a typed failure. Raw audio is not persisted.
5. Listening ends, ducking is released, and the overlay enters Processing.
6. The deterministic mapper normalizes and maps only the approved six English intents.
7. A supported result at or above `T_DIRECT`, with a clear artist resolution where applicable, submits exactly one command to the same service command boundary used by UF-05/UF-06.
8. The UI enters Executed only after service acknowledgement and refreshes from the authoritative snapshot.
9. At most one short TTS sentence announces the result. TTS failure leaves Executed unchanged.
10. The overlay dismisses to the initiating surface with manual controls immediately usable.

## Intent execution table

| Intent | Direct action | Guardrail |
|---|---|---|
| `PLAY` | Resume/start the current valid service-owned item. | No fabricated track; No Track stays safe. |
| `PAUSE` | Pause through the service. | Idempotent if already paused. |
| `NEXT` | Advance once. | At queue end, remain ended/paused-completed; never wrap or invent Repeat. |
| `PREVIOUS` | Apply symbolic `T_PREVIOUS`: previous item before threshold, restart current item after it. | Numeric threshold remains #16. |
| `SEEK_BACK` | Seek back the approved interval and clamp at zero. | No negative position or UI-owned authority. |
| `PLAY_ARTIST` | Play the one certain local artist match. | Ambiguity -> UF-09; no match preserves playback. |

## Failure/handoff rules

- Low confidence or multiple plausible matches -> UF-09 Confirming; never execute first.
- Missing/no artist, unknown phrase, or unsupported intent -> UF-09 safe Not Found/Unsupported; no nearest mapping.
- Timeout, no-match recognition, provider/language/network/audio failure -> UF-10, with one retry maximum where eligible.
- Microphone denial -> short recoverable failure and manual controls; permission recovery belongs to UF-12 and never auto-opens Settings while Driving Mode is active.
- Command rejection/stale queue -> Failed-safe, refresh actual service state, no second player and no optimistic success.
- Call/focus/route interruption -> UF-11; Voice does not override system playback policy.

## Telemetry and accessibility

Allowed events are `voice_button_pressed`, `intent_detected`, `command_executed`, `intent_failed`, and `manual_correction`, with only reviewed enums/buckets. Raw audio, transcript, personal media metadata, provider hypotheses, exact confidence, exact latency, location, contacts, and stable device/user identifiers are prohibited.

Listening/Processing/Executed/Failed meaning must not rely on color alone. TalkBack announces microphone state and the single terminal result. Motion is limited, copy is short, and manual controls/Exit remain reachable.

## Completion boundary

UF-08 fully specifies the direct-command User Flow and its safe seams. It does not claim production STT accuracy, physical microphone/provider validation, product UI implementation, or Beta KPIs.
