# Music Copilot - Product Requirements Document

- **Version:** 1.0
- **Status:** Approved for MVP
- **Date:** July 2026
- **Owner:** Music Copilot Team
- **Platform:** Android 10+
- **Beta language:** English, with a localization-ready structure
- **Companion:** [MVP v1.0](MVP.md)

This Markdown document is the repository version of the approved PRD v1.0. It contains the product information defined in the approved Persian PRD and does not introduce later-version scope.

## 1. Executive decision

Music Copilot v1.0 is an Android application for playing local music and controlling it through push-to-talk while driving.

The goal is to validate whether voice interaction reduces the touches required for frequent music actions and whether users return to Driving Mode. The first version focuses only on Android, lawful local files, a stable player, a driving interface and six voice commands. Public streaming, iOS, accounts and conversational AI are outside the version.

### Primary user

A driver who:

- drives at least 30 minutes per day;
- regularly connects a phone to vehicle Bluetooth;
- has local music on the device;
- wants fast control without navigating menus;
- has basic familiarity with voice commands.

Streaming-only listeners are not representative of the first Beta; this sampling bias must be recorded.

### Product promise

Control your music while driving with one tap and one sentence.

### Success definition

- Direct-command success: at least 85%
- Manual correction after a command: under 15%
- Driving Mode reuse: at least 50% of Beta users
- Voice-response P95: under 2.5 seconds after end of speech
- Weekly return: at least 30%
- Serious wrong-command execution: under 3%
- Crash-free sessions: at least 99%

## 2. Problem, opportunity and differentiation

Changing tracks, seeking backward or playing an artist can require looking at the screen, multiple touches and menu navigation. In a vehicle, this small friction becomes distraction.

Version-one differentiation:

- **Voice-first, not Voice-added:** Voice is central to the Driving flow.
- **Restricted and predictable:** a small intent set replaces open conversation.
- **Local-first:** core commands work without internet; raw voice is not stored by default.
- **Measurable:** command success, response time, manual correction and repeat use inform the next decision.
- **One glance, one decision:** Driving Mode shows essential information and four primary controls.
- **Short response:** TTS returns at most one sentence.
- **Direct execution:** certain commands execute without an intermediate screen.
- **Safe return:** failure does not destabilize the player and manual controls remain one touch away.

## 3. Core journeys

### First use

Two-slide value introduction -> file and microphone permissions -> local-library scan -> six-command education -> ready Home.

### Driving voice command

Driving Mode -> microphone tap -> spoken command -> intent detection -> execution and short response.

### Ambiguity and error rules

| Situation | Behavior | Example |
|---|---|---|
| Certain command | Execute immediately | “Playing Adele.” |
| Two similar results | Ask one short question | “Did you mean Adele or Adèle?” |
| Unclear speech | Ask once to repeat | “Please say that again.” |
| Not available locally | Preserve current playback | “I couldn't find that in your library.” |

## 4. Scope

### In scope

- Android 10+
- Scan and search supported local audio
- Songs, artists, albums and folders
- Play, pause, next, previous, seek and queue
- Background, lock-screen and Bluetooth playback/control
- Push-to-talk and Driving Mode
- Six English voice commands
- Short local playback history
- Simple Like/Liked Songs, subject to P2 priority
- Privacy-safe telemetry and non-sensitive crash reporting
- Closed Beta

### Out of scope

- Online streaming and downloads
- Registration, account, backend, cloud sync
- iOS, Android Auto and CarPlay
- Equalizer, crossfade and DSP
- Persistent wake word
- Multi-step conversational commands
- Song or humming recognition
- Advanced playlists and social features
- Public upload, music distribution or online catalog

The app plays only lawful audio already present on the user's device.

## 5. Priority and functional requirements

| ID | Priority | Requirement |
|---|---|---|
| FR-01 | P0 | With user consent, scan supported audio and present it by song, album, artist and folder. |
| FR-02 | P0 | Continue playback in background and with screen off, while updating Media Session state. |
| FR-03 | P0 | Handle calls, headset disconnect and audio-route changes without crash, using logical pause/resume behavior. |
| FR-04 | P0 | Driving Mode shows title, artist, artwork and four large controls. |
| FR-05 | P0 | A microphone tap ducks music, starts STT and maps the result to the restricted intent set. |
| FR-06 | P0 | A certain intent executes directly; an ambiguous intent is confirmed before execution. |
| FR-07 | P1 | The user can activate Driving Mode manually or receive a suggestion when the selected Bluetooth device connects. |
| FR-08 | P1 | Preserve queue and track position after the UI closes. |
| FR-09 | P2 | Let the user Like a track and play a simple Liked Songs list. |

New scope enters only when it directly tests an MVP hypothesis or blocks reliable execution.

## 6. Voice contract

State model:

`Idle -> Listening -> Processing -> Confirming -> Executed | Failed`

| Intent | Example | Result |
|---|---|---|
| PLAY | “Play”, “Resume” | Start or resume playback |
| PAUSE | “Pause”, “Stop music” | Pause playback |
| NEXT | “Next”, “Next song” | Move to the next track |
| PREVIOUS | “Previous song” | Previous track or start of current track |
| SEEK_BACK | “Go back 10 seconds” | Seek back 10 seconds, clamped at zero |
| PLAY_ARTIST | “Play songs by Adele” | Play the best local artist match |

Rules:

- Push-to-talk only; no wake word.
- Playback ducks during Listening.
- Listening timeout is at most six seconds.
- Retry is allowed once.
- A certain intent executes directly.
- Low confidence or multiple matches require one short confirmation.
- Artist matching uses normalization, fuzzy matching and playback-history priority.
- TTS is at most one short sentence.
- Intent and execution result may be logged; raw audio is not stored.
- Confidence threshold may use simple Remote Config or Build Config.
- If cloud STT is used, separate consent plus transparent disclosure of processing destination and retention is required.

## 7. Required screens and states

1. Splash - Loading, Migration Error
2. Onboarding - First Run, Skip
3. Permissions - file, microphone and notification; Granted, Denied, Permanently Denied
4. Home / Library - Scanning, Empty, Ready
5. Search - Result, No Result, Error
6. Full Player - Playing, Paused, Corrupt File
7. Driving Mode - Playing, Paused, No Track
8. Voice Overlay - Listening, Confirm, Failed
9. Liked Songs - Empty, Ready
10. Settings - data, Bluetooth and Privacy; Default, Updated

Driving Mode uses one layer, high contrast, readable typography, limited motion and touch targets of at least 48dp. It contains no typing, deep navigation or long result lists.

## 8. Non-functional, accessibility and safety requirements

### Performance and stability

- Local-command P95: under 1 second
- Voice-response P95: under 2.5 seconds after end of speech
- Smooth scrolling with 5,000 files
- Crash-free sessions: at least 99%
- Serious player errors: under 1 per 100 sessions
- Recover from a corrupt file without stopping the whole queue
- Incremental library scan
- No unnecessary wake lock
- Microphone only after user action

### Accessibility and driving

- Minimum 48dp touch target
- Suitable contrast and scalable text
- Complete TalkBack labels
- Short messages and fixed controls
- No typing or deep navigation in Driving Mode
- No long result list while driving
- No claim that the product makes driving safe
- Clear warning that road attention remains the driver's responsibility

A serious wrong command, crash or audio-route behavior that creates serious distraction stops Beta release until fixed.

## 9. Data, privacy and measurement

Principles:

- Collect only data required for quality and hypothesis measurement.
- Request microphone permission when needed and explain it clearly.
- Let the user clear history and diagnostic data.
- Do not store or send raw voice by default.
- If cloud STT is used, disclose processing destination and retention and obtain separate consent.

Required events:

`driving_mode_started`, `voice_button_pressed`, `intent_detected`, `intent_failed`, `command_executed`, `manual_correction`, `player_error`, `session_completed`.

Do not collect raw audio, full titles of personal files, precise location, contacts or unnecessary identifiers.

## 10. Proposed technology and architecture

- Kotlin + Jetpack Compose + Media3
- Coroutines/Flow
- Hilt
- Media3/ExoPlayer for playback, queue, Media Session and Audio Focus
- MediaStore for local audio discovery
- Room + DataStore for library index, Like, settings and session data
- Android STT/TTS + restricted Intent Mapper
- Privacy-safe crash reporting and analytics
- No backend in version one; primary data remains on-device
- Kotlin Multiplatform is deferred until after Android validation
- Voice Provider stays behind an interface
- Player is independent of UI lifecycle and managed in a service
- Logical boundaries: UI, Domain, Playback, Voice and Data

These are proposed implementation directions in v1.0 and require technical planning before they become accepted ADRs.

## 11. Risks, rollout and decision

| Risk | Severity | Mitigation |
|---|---|---|
| Low artist-name accuracy in road noise | High | Restricted intents, confidence, confirmation and vehicle tests |
| Player/audio-route instability | High | Media3, device matrix and call/Bluetooth scenarios |
| Too few local-file users | High | Screen Beta users and validate a legal service connection in Discovery |
| Scope growth | Medium | Lock P0 and use a formal gate for changes |
| Voice privacy concern | Medium | Push-to-talk, no raw storage and clear explanation |

Rollout: 10-user prototype -> internal Alpha -> 50-driver Closed Beta -> GO / ITERATE / STOP.

- **GO:** quality, repeat use and reduced touches meet targets; begin discovery for one legal music-service connection and iOS.
- **ITERATE:** value is visible but accuracy or stability is low; run one technical correction cycle.
- **STOP:** users do not return or Voice adds no material value over manual control.
