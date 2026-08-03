# Music Copilot - Product Requirements Document

- **Version:** 1.0
- **Status:** Approved for MVP
- **Date:** July 2026
- **Owner:** Music Copilot Team
- **Platform:** Android 10+
- **Beta language:** English, with a localization-ready structure
- **Companion:** [MVP v1.0](MVP.md)

> **Authoritative source:** the attached English **Music Copilot Product Requirements Document v1.0**. This Markdown file is a concise repository representation of that English source. It must not expand, reinterpret, or override the approved English PRD. Delivery details, acceptance criteria, and the build plan belong to the English MVP document.

## 1. Executive decision

Music Copilot v1.0 is an Android application for playing user-authorized local music and controlling it through Push-to-Talk while driving.

The goal is to validate whether voice interaction reduces the touches required for frequent music actions and whether users return to Driving Mode. Version One focuses on Android, local files, a stable player, Driving Mode, and six English voice commands. General streaming, iOS, accounts, and conversational AI are outside the version.

### Version specification

| Component | Decision | Rationale |
|---|---|---|
| Platform | Android 10+ | Better access to local files and a shorter validation timeline |
| Interface language | English in Beta; localization-ready structure | Aligned with the initial market and limited command set |
| Voice input | Push-to-Talk | Controls battery use and protects privacy |
| Music source | User-authorized local files | Removes streaming-license risk from the first experiment |
| Release model | Closed Beta | Controlled measurement on real phones and in real cars |

### Primary user

A daily driver who:

- drives 30-90 minutes per day;
- regularly connects a phone to vehicle Bluetooth;
- keeps local music on the device;
- needs fast control without menu navigation;
- has basic familiarity with voice commands.

### Job to be done

When I am driving, I want to control music with the least possible looking and touching, so the listening flow is not interrupted and my attention returns to the road quickly.

### Secondary users

- Ride-hailing driver
- User with limited mobility
- Road-trip passenger
- Local-file enthusiast

Users who rely exclusively on streaming services are not representative candidates for the first Beta. This sampling bias must be recorded when results are analyzed.

### Product promise

Control your music while driving with one touch and one sentence.

### Success definition

- Direct-command success: at least 85%
- Manual correction after a command: under 15%
- Driving Mode reuse: at least 50% of Beta users
- Voice-response P95: under 2.5 seconds after the end of speech

## 2. Problem, opportunity, and differentiation

Changing tracks, seeking backward, or playing an artist can require looking at the screen, multiple touches, and menu navigation. In a vehicle, that small friction becomes distraction.

| Need | Current barrier | Product response |
|---|---|---|
| Change tracks quickly | Finding a button or opening the app | Large controls and direct commands |
| Play an artist | Typing and selecting a result | Voice search in the local library |
| Control with weak internet | Dependence on a cloud service | Base intents and local player |
| Trust the command | Wrong execution or a long response | Ambiguity confirmation and one-sentence reply |

Version One differentiation:

- **Voice-first, not voice-added:** Voice is central to the Driving flow.
- **Limited and reliable:** A small intent set replaces open conversation.
- **Local-first:** Core commands remain available without the internet; raw voice is not stored by default.
- **Measurable:** Command success, response time, manual correction, and repeat use inform the next-version decision.

## 3. Experience principles and core journeys

- **One glance, one decision:** Driving Mode shows only essential information and four primary controls.
- **Short response:** TTS announces the result in no more than one sentence.
- **Direct execution:** A certain command executes without an intermediate screen; only ambiguity requires confirmation.
- **Safe recovery:** On error, the player does not stop or break, and manual control remains reachable with one touch.

### First-use flow

Two-slide value introduction -> file and microphone permissions -> local-library scan -> six-command tutorial -> Home ready to play.

### Driving command flow

Driving Mode -> microphone tap -> spoken command -> intent detection -> execution and short response.

### Ambiguity and error rules

| Situation | Behavior | Example |
|---|---|---|
| Certain command | Execute immediately | “Playing Adele.” |
| Two similar results | Ask one short question | “Did you mean Adele or Adèle?” |
| Unclear speech | Ask once to repeat | “Please say that again.” |
| No local match | Preserve current playback | “I couldn't find that in your library.” |

## 4. Scope

### In scope

- Android 10+
- Scan and search supported local audio files
- Play, pause, next, previous, and seek
- Background, lock-screen, and Bluetooth playback/control
- Driving Mode and Push-to-Talk
- Six English voice commands
- Short playback history and simple Liked Songs
- Non-sensitive telemetry and crash reporting

### Out of scope

- iOS, Android Auto, and CarPlay
- Online streaming and downloads
- Sign-up, backend, and sync
- Equalizer, crossfade, and DSP
- Always-on wake word
- Multi-turn conversational commands
- Song or humming recognition
- Advanced playlists and social features

A new capability enters Version One only when it directly tests a Beta hypothesis or removes a blocker to reliable journey execution.

## 5. P0 voice commands

| Intent | Example command | Behavior |
|---|---|---|
| PLAY / PAUSE | Play / Pause | Control the player |
| NEXT | Next song | Move to the next track |
| PREVIOUS | Previous song | Previous track or start of current track |
| SEEK_BACK | Go back 10 seconds | Seek backward 10 seconds |
| PLAY_ARTIST | Play songs by Adele | Play the best local match |

## 6. Functional requirements

| ID | Priority | Requirement |
|---|---|---|
| FR-01 | P0 | With user consent, scan supported audio files and display them by track, album, artist, and folder. |
| FR-02 | P0 | Continue playback in the background and with the screen off, while updating the system Media Session. |
| FR-03 | P0 | Handle phone calls, headphone disconnection, and Audio Route changes without crashes and with sensible Pause/Resume behavior. |
| FR-04 | P0 | Driving Mode displays title, artist, artwork, and four large controls. |
| FR-05 | P0 | When the microphone is tapped, music volume ducks, STT starts, and the result maps to the limited intents. |
| FR-06 | P0 | A certain intent executes directly; an ambiguous intent is confirmed before execution. |
| FR-07 | P1 | The user can activate Driving Mode manually or receive a suggestion when a selected Bluetooth device connects. |
| FR-08 | P1 | Preserve playback queue and track position after the UI closes. |
| FR-09 | P2 | Let the user Like a track and play it from a simple Liked Songs screen. |

## 7. Version screens

The English PRD names these Version One screens:

1. Splash
2. Onboarding
3. Permissions
4. Home
5. Library
6. Search
7. Full Player
8. Driving Mode
9. Voice State
10. Settings

Detailed required states and delivery acceptance are maintained in the English MVP and downstream User Flow artifacts.

## 8. Non-functional, accessibility, and safety requirements

### Performance

- Local-command P95: under 1 second
- Voice-command P95: under 2.5 seconds after the end of speech
- Smooth scrolling for 5,000 files

### Stability

- At least 99% crash-free journeys
- Fewer than one critical player error per 100 sessions
- Recover from a corrupt file without stopping the entire queue

### Resource use

- Microphone activates only after user action
- No unnecessary Wake Lock
- Incremental library scanning

### Accessibility

- Minimum 48dp touch target
- Sufficient contrast and scalable text
- Complete TalkBack labels

### Driving-experience safety

- No typing or deep navigation in Driving Mode
- Short messages and fixed controls
- No long result lists while moving
- No claim of “safe driving”; the product only reduces interaction
- Clear warning that responsibility for attention to the road remains with the driver

If an incorrect command execution, crash, or Audio Route behavior creates serious distraction, Beta release is paused until the issue is resolved.

## 9. Data, privacy, and measurement

### Principles

- Collect only data required for quality and hypothesis measurement.
- Request microphone permission only when needed and explain it clearly.
- Let the user delete history and diagnostic data.
- Do not store or transmit raw audio by default.
- If cloud STT is used, obtain separate consent and disclose the processing destination and retention period.

### Metrics

| Metric | Beta target | Type |
|---|---:|---|
| Successful driving session with at least one voice command | At least 50% of weekly users | North Star |
| Direct-command success | At least 85% | Quality |
| Manual correction after a command | Less than 15% | Friction |
| Incorrect command execution | Less than 3% | Guardrail |
| Weekly return | At least 30% | Retention |
| Driving Mode reuse | At least 50% | Value |

### Required events

`driving_mode_started`, `voice_button_pressed`, `intent_detected`, `intent_failed`, `command_executed`, `manual_correction`, `player_error`, `session_completed`.

Do not collect raw audio, full titles of personal files, precise location, contacts, or unnecessary identifiers.

## 10. Proposed technology and architecture

| Layer | Proposed choice | Responsibility |
|---|---|---|
| UI | Jetpack Compose | Screens, Driving Mode, and Voice states |
| Playback | Media3 / ExoPlayer | Playback, Media Session, queue, and Audio Focus |
| Local Data | Room + DataStore | Library index, Likes, settings, and session state |
| Voice | Android STT/TTS + Intent Mapper | Speech conversion, limited classification, and response |
| DI | Hilt | Dependency separation and testability |
| Quality | Crash reporting + privacy-safe analytics | Beta stability and measurement |

Logical boundary: `UI -> Domain -> Playback -> Voice -> Data`.

- There is no backend in Version One; primary data remains on-device.
- Kotlin Multiplatform is deferred until Android value is proven.
- The Voice Provider stays behind an interface so the STT provider can be replaced.
- The player is independent of the UI lifecycle and managed in a Service.
- Product-scope changes require PRD/MVP approval; provider or library replacements require architecture review while preserving locked v1.0 constraints.

## 11. Risks, rollout, and next decision

| Risk | Severity | Mitigation |
|---|---|---|
| Low artist-name accuracy in road noise | High | Limited intents, confidence threshold, ambiguity confirmation, and in-car testing |
| Player and Audio Route instability | High | Media3, device matrix, and call/Bluetooth scenarios |
| Too few local files in the target market | High | Beta-user screening and validation of a legal service-integration path during Discovery |
| Scope expansion | Medium | P0 lock and a formal gate for every change |
| Voice privacy concerns | Medium | Push-to-Talk, no raw storage, and transparent explanation |

Rollout: 10-user Prototype -> internal Alpha -> 50-driver Closed Beta -> GO / ITERATE / STOP Decision Gate.

- **GO:** Quality, repeat use, and reduced touching meet targets; begin evaluating one licensed-service integration and iOS.
- **ITERATE:** Value is visible but accuracy or stability is low; run one technical-improvement cycle.
- **STOP:** Users do not return after initial novelty, or Voice provides no meaningful advantage over manual control.
