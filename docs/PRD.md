# Music Copilot - Product Requirements Document

- **Version:** 1.1
- **Status:** Approved for MVP
- **Decision date:** 2026-07-28
- **Owner:** Latticeward Labs
- **Product language:** English Beta; documentation may be maintained in English or Persian
- **Canonical companion:** [MVP v1.1](MVP.md)

This Markdown document is the canonical product source for Music Copilot v1.1. Repository documents, Issues, designs and implementation must conform to it. Material scope changes require Product Owner approval and updates to this document, the MVP and the traceability matrix. Technical reversals require a superseding ADR.

## 1. Executive decision

Music Copilot v1.1 is a native Android application for playing lawful audio already stored on a user's device. Its differentiating experience is a distraction-minimized Driving Mode controlled through one explicit microphone tap and six short English commands.

The first release does not attempt to become a streaming platform. It tests whether a focused, predictable Voice experience can reduce manual music interaction while driving and earn repeat use.

### Product hypothesis

If daily drivers can control frequent music actions with one tap and one short command, they will need fewer manual corrections and at least half of qualified Beta testers will reuse Driving Mode.

### Product promise

Control your music while driving with one tap and one sentence.

## 2. Problem and opportunity

Frequent actions such as changing a track, seeking backward or playing a local artist often require looking at the screen, navigating menus or typing. In a vehicle, small interaction costs become distraction.

Music Copilot reduces this friction through:

- a one-layer Driving Mode with only essential information and controls;
- restricted commands with deterministic behavior instead of open conversation;
- local-first playback that remains useful without an account or catalog connection;
- direct execution when confidence is high and one-step confirmation when ambiguous;
- immediate recovery to complete manual controls after any Voice failure.

## 3. Target user and context

### Primary user

A qualified first-Beta user:

- drives at least 30 minutes per day;
- regularly connects an Android phone to vehicle Bluetooth;
- has at least 50 lawful local audio files;
- wants fast control without navigating menus;
- can use the six English Beta commands;
- gives informed consent for approved non-audio diagnostics.

Streaming-only listeners are not representative of the first validation sample. This sampling bias must be recorded in Beta analysis.

### Job to be done

When I am driving, I want to control music with minimal looking and touching so the listening flow continues and my attention returns to the road quickly.

### Secondary users

Local-music listeners, accessibility-oriented users and commuters may benefit, but the first validation and design decisions optimize for the qualified driver above.

## 4. Product and experience principles

1. **Voice-first, not Voice-added.** Voice is central to the Driving flow, not a decorative feature on a conventional player.
2. **One glance, one decision.** Driving Mode shows only essential information and fixed controls.
3. **Restricted and predictable.** Exactly six intents are supported; open-ended or multi-turn conversation is excluded.
4. **Direct when certain.** A high-confidence single match executes without opening a new screen.
5. **Confirm ambiguity once.** Low confidence or multiple plausible artist matches cause one short confirmation.
6. **Short feedback.** TTS uses at most one short sentence.
7. **Safe fallback.** Failure must not destabilize playback; complete manual control remains available in one step.
8. **Local-first and privacy-minimal.** Core playback uses device-local media. Raw audio is not stored, and transcripts are memory-only for the active command.
9. **Explicit user action.** The microphone activates only after push-to-talk. There is no wake word.
10. **No safety overclaim.** The product reduces interaction; it does not claim to make driving safe. The driver remains responsible for road attention.

## 5. Core journeys

### First run

Value onboarding -> contextual media permission -> local scan -> Library ready/empty/error -> optional Voice education.

Microphone permission is not requested during generic onboarding. It is requested when Voice is first invoked, after a clear rationale.

### Manual playback

Library/Search -> select local track -> queue -> Mini Player/Full Player -> background or screen-off playback -> notification, lock-screen and Bluetooth controls.

### Driving Mode

Manual entry or optional suggestion after connecting to a user-selected Bluetooth device -> responsibility notice -> one-layer controls -> immediate exit.

The Bluetooth behavior is a suggestion only. Driving Mode never opens forcibly.

### Voice command

Driving Mode -> microphone tap -> Listening -> Processing -> direct execution or Confirming -> Executed/Failed -> return to Idle.

## 6. Scope and priority

### P0 - release blocking

- Native Android 8.0+ (`minSdk 26`), Kotlin and Jetpack Compose
- Planned 2026 build baseline: `compileSdk 36`, `targetSdk 36`
- Media access rationale, permission, denial and Settings recovery
- MediaStore discovery and incremental local index
- Songs, artists, albums, folders and local search
- Media3 playback service with play, pause, next, previous, seek and queue
- Queue/current item/position restoration
- Background, screen-off, notification, lock-screen and Bluetooth controls
- Deterministic Audio Focus, call, headset-disconnect and audio-route behavior
- Driving Mode with artwork, title, artist, four large controls, microphone and immediate exit
- Push-to-talk Voice with replaceable STT/TTS interfaces and a deterministic mapper
- On-device-first recognition; network-capable recognition disabled by default
- Clear, separate and revocable consent before any network-capable recognizer
- Complete manual playback when Voice is unavailable or consent is absent/revoked
- Six English intents: `PLAY`, `PAUSE`, `NEXT`, `PREVIOUS`, `SEEK_BACK`, `PLAY_ARTIST`
- One-step ambiguity confirmation, six-second listening timeout and one retry
- Corrupt/missing-file recovery without collapsing the queue
- Privacy-safe telemetry and crash diagnostics
- Accessibility, device, route, vehicle and P0 regression evidence

### P1 - required when schedule permits

- Manual Driving Mode entry
- Suggestion after connecting to one user-selected Bluetooth vehicle; never forced auto-open
- Short local playback history
- Settings for Bluetooth hint, Voice availability/consent, privacy and data clearing

Manual Driving Mode entry is functionally required for the P0 journey. The optional Bluetooth suggestion is the schedule-cuttable P1 portion.

### P2 - cut first

- Simple local Like/Unlike
- Liked Songs list and playback

### Explicit exclusions

- Online catalog, music streaming, download or casting
- Account, authentication, backend or cloud synchronization
- General recommendation/discovery feed
- Full playlist management or social features
- Public upload, distribution, advertising, subscription or royalty systems
- iOS, CarPlay or Android Auto
- Wake word, always-listening microphone or open/multi-turn conversation
- Motion detection
- Song/humming recognition or music generation
- Equalizer, DSP or crossfade

The application plays only audio already lawfully available on the user's device.

## 7. Functional requirements

| ID | Priority | Requirement |
|---|---|---|
| `FR-01` | P0 | With user permission, discover supported audio and present songs, albums, artists and folders through an incremental local index and search. |
| `FR-02` | P0 | Continue playback in background and with the screen off while maintaining canonical MediaSession state. |
| `FR-03` | P0 | Handle calls, Audio Focus, headset/Bluetooth disconnect and route changes without crash, overlap or unintended phone-speaker playback. |
| `FR-04` | P0 | Show title, artist, artwork, four large controls, microphone and immediate exit in a one-layer Driving Mode. |
| `FR-05` | P0 | After an explicit microphone tap, duck playback, run the approved STT provider and map the result to the restricted intent set. |
| `FR-06` | P0 | Execute a high-confidence deterministic intent directly; confirm low-confidence or ambiguous artist resolution once before execution. |
| `FR-07` | P0/P1 | Always allow manual Driving Mode activation; optionally suggest entry for a user-selected Bluetooth vehicle without forced opening. |
| `FR-08` | P0 | Restore queue, current item and playback position after UI closure and process recreation where Android permits. |
| `FR-09` | P2 | Allow simple local Like/Unlike and Liked Songs playback only if the week-8 capacity gate permits. |

## 8. Voice contract

### State model

`Idle -> Listening -> Processing -> Confirming -> Executed | Failed -> Idle`

### Supported commands

| Intent | Example | Deterministic result |
|---|---|---|
| `PLAY` | "Play", "Resume" | Resume the current queue; if empty, offer the last playable item. |
| `PAUSE` | "Pause", "Stop music" | Pause playback. |
| `NEXT` | "Next", "Next song" | Advance; announce end of queue if applicable. |
| `PREVIOUS` | "Previous song" | Restart current or move previous using one documented position threshold. |
| `SEEK_BACK` | "Go back 10 seconds" | Seek minus 10 seconds, clamped to zero. |
| `PLAY_ARTIST` | "Play songs by Adele" | Resolve only local indexed artists, then start/replace the queue. |

### Provider and consent policy

- Prefer on-device recognition when the platform reports it available.
- Keep providers behind replaceable interfaces.
- A provider that may transmit audio is disabled by default.
- Before enabling such a provider, explain the available processor, destination and retention behavior and obtain separate, revocable consent.
- Consent is not bundled with microphone/media permission or telemetry consent.
- When an acceptable provider is unavailable, Voice is unavailable and full manual control continues.
- Raw audio is never persisted by the app. The transcript exists only in memory for active intent resolution and is immediately discarded.

## 9. Screens and required states

| Surface | Required states |
|---|---|
| Splash | loading, migration error |
| Onboarding | first run, value, optional Voice education, skip |
| Permissions & Privacy | media/microphone rationale, granted, denied, permanently denied, Voice unavailable, optional network-recognizer consent, revocation |
| Home/Library | scanning, empty, ready, error |
| Search | query, results, no result, error |
| Full Player | loading, playing, paused, completed, corrupt/missing file |
| Driving Mode | no track, playing, paused, route warning, immediate exit |
| Voice Overlay | idle, listening, processing, confirming, executed, failed, timeout, unavailable |
| Liked Songs | empty, ready; P2/capacity-gated |
| Settings | Bluetooth hint, Voice availability/consent, privacy, data clearing |

## 10. Non-functional, accessibility and safety requirements

### Reliability

- Serious-crash-free sessions: at least 99%.
- Serious Player errors: fewer than 1 per 100 sessions.
- A corrupt/missing item yields a normalized error and controlled queue continuation.
- UI recreation does not own or terminate canonical playback.

### Performance

- Local deterministic command P95: under 1 second.
- Voice response P95: under 2.5 seconds after end of speech.
- Smooth Library behavior with 5,000 local files.
- Library scanning is incremental and does not block the UI.

### Accessibility and driving

- Minimum touch target: 48dp; primary Driving controls target 64-80dp.
- WCAG AA contrast for normal text and essential controls.
- TalkBack labels and logical focus order.
- Text scaling without clipping/overlap.
- Meaning is not communicated by color alone.
- No typing, deep navigation or long result lists in Driving Mode.
- Immediate exit remains visible and reachable.

### Resource behavior

- Microphone only after explicit user action.
- No unnecessary wake lock.
- No always-on listening.
- Route loss must not continue playback through an unintended output.

## 11. Data, privacy and measurement

### Data principles

- Minimize collection to a documented product or quality question.
- Do not persist raw audio or transcripts.
- Do not emit filenames, titles, artists or albums from personal media.
- Keep consent contextual, separate and revocable.
- Provide clear local data-clearing and diagnostic-disable behavior.
- Set and document retention before Beta.

### Canonical event allowlist

`driving_mode_started`, `voice_button_pressed`, `intent_detected`, `command_executed`, `intent_failed`, `manual_correction`, `player_error`, `session_completed`, `library_scan_completed`.

Only typed, documented and coarse properties may be emitted. Free-form personal content is prohibited.

### Success and guardrail metrics

| Metric | Target |
|---|---:|
| Weekly users with a Driving session containing at least one Voice command | at least 50% |
| Direct-command success | at least 85% |
| Manual correction after a command | under 15% |
| Serious wrong-command execution | under 3% |
| Voice response P95 | under 2.5 seconds |
| Serious-crash-free sessions | at least 99% |
| Serious Player error | under 1 per 100 sessions |
| Weekly return | at least 30% |
| Driving Mode reuse | at least 50% |

## 12. Product and technical baseline

- Native Android, Kotlin, Jetpack Compose
- Android 8.0+ (`minSdk 26`); `compileSdk 36` and `targetSdk 36`, revalidated before Play release
- Media3/ExoPlayer owned by `MediaSessionService`
- MediaStore for system-authoritative media discovery
- Room for the derived index and restorable app data
- DataStore for small typed settings/consent choices
- Android STT/TTS behind replaceable interfaces
- Restricted deterministic intent mapper
- Hilt, coroutines/Flow and unidirectional state
- Local-first product data; no application backend
- No explicit ABI filtering unless a native dependency is introduced
- Seven initial Gradle modules as defined by accepted technical decisions

Implementation detail is governed by [Technical Decisions](TECHNICAL_DECISIONS.md) and accepted [ADRs](adr/README.md), provided they do not expand product scope.

## 13. Risks and mitigations

| Risk | Severity | Mitigation |
|---|---|---|
| Artist recognition under road noise | High | Restricted commands, confidence buckets, one-step ambiguity confirmation and field fixtures |
| Player/route instability | High | Service-owned Media3, API/device/OEM/vehicle matrix and call/route P0 tests |
| Too few local-music users | High | Qualify Beta users and record sampling bias; test legal-service connectivity only after MVP evidence |
| Scope growth | Medium | P0 lock, ten-week timebox and formal change control |
| Voice privacy concern | High | Push-to-talk, on-device-first selection, no raw storage and separate explicit consent |
| Android/OEM fragmentation | High | Risk-based API matrix and representative Pixel/Samsung/Xiaomi physical evidence |

## 14. Release and decision model

Prototype with 10 users -> internal Alpha -> 50-driver Closed Beta for 14 days across at least 3 vehicles -> `GO`, `ITERATE` or `STOP`.

- **GO:** quality, repeat use and reduced correction meet the documented gates.
- **ITERATE:** users see value but accuracy, latency or stability misses targets.
- **STOP:** users do not return or Voice provides no material benefit over manual control.

Beta stops immediately for repeatable serious wrong execution, serious crash, unintended output route, ambiguous call behavior or unapproved Voice data handling.

Potential legal music-service connectivity and iOS discovery are post-Beta options, not committed roadmap items.
