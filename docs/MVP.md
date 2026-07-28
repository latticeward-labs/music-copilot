# Music Copilot - Minimum Viable Product Plan

- **Version:** 1.0
- **Status:** Ready for Planning
- **Date:** July 2026
- **Platform:** Android only, Android 10+
- **Timebox:** 10 weeks
- **Validation:** 50-driver Closed Beta
- **Companion:** [PRD v1.0](PRD.md)

This Markdown document is the repository version of the approved MVP v1.0. It contains the execution information defined in the approved Persian MVP and does not introduce later-version scope.

## 1. Hypothesis and questions

If a driver can control music with one tap and one short command, manual interactions will fall and the user will return to Driving Mode the following week.

The MVP must answer:

- **Value:** Do at least half of users use Driving Mode more than once?
- **Usability:** Do at least 85% of direct commands complete without manual correction?
- **Performance:** Does voice-response P95 stay under 2.5 seconds?
- **Reliability:** Are at least 99% of trips free of a serious player crash?

## 2. Definition of Done for the MVP

- The local player is stable in background, lock-screen and Bluetooth use.
- Driving Mode is usable with large controls and immediate exit.
- Six English commands execute and ambiguity/error states are managed.
- Events required for hypothesis measurement are recorded without storing raw voice.
- Internal testing and Closed Beta run across multiple phones and vehicles.
- Build is fixed to ten weeks.
- An incomplete P1 feature is removed; an incomplete P0 blocks Beta.

## 3. Exact scope and priorities

| Epic | In MVP | Outside MVP |
|---|---|---|
| Player | local files, queue, background, lock screen, Bluetooth | streaming, download, cast |
| Library | songs, artists, albums, folders, search | cloud sync, recommendations |
| Driving | manual activation, Bluetooth suggestion, large controls | Android Auto, motion detection |
| Voice | push-to-talk, six intents, short TTS | wake word, free conversation |
| User Data | simple Like, local history, settings | account, backend, sync |
| Audio | Audio Focus and system volume | equalizer, crossfade, DSP |

Priority contract:

- **P0:** base player, Voice, Driving and stability (calls, headset and audio route)
- **P1:** Bluetooth entry suggestion
- **P2:** Like, only if sprint capacity permits

There is no online catalog, upload or content distribution. The app plays only lawful files present on the user's device.

## 4. User stories

| ID | Story | Acceptance |
|---|---|---|
| US-01 | As a new user, I want permissions requested at the right time so I understand each access. | Denial does not crash and a Settings path is shown. |
| US-02 | I want my files scanned quickly so I can play music without an account. | Song/artist/album index is created and later scans are incremental. |
| US-03 | I want music to continue with screen off and Bluetooth. | Media notification and vehicle controls show real state. |
| US-04 | While driving, I want to see only essential controls. | Four primary controls, microphone, title and exit fit on one screen. |
| US-05 | I want playback to change immediately after speaking a command. | A certain intent executes without a new screen and returns short TTS. |
| US-06 | If a command is ambiguous, I want a question before a wrong action. | Low confidence or multiple matches cause one-step confirmation. |
| US-07 | If STT fails, I want to return quickly to manual control. | One retry, then return without stopping music. |

## 5. Golden scenario

Vehicle connected -> Driving Mode -> microphone tap -> “Next song” -> execution under 2.5 seconds -> next track starts -> title updates -> response is at most one sentence -> no extra screen opens.

## 6. Voice contract

State model:

`Idle -> Listening -> Processing -> Confirming -> Executed | Failed`

| Intent | Example | Action | Fallback |
|---|---|---|---|
| PLAY | Play / Resume | Start or continue | Last track if queue is empty |
| PAUSE | Pause / Stop music | Pause | No long response |
| NEXT | Next / Next song | Next track | Announce end of queue |
| PREVIOUS | Previous song | Previous or beginning | Fixed behavior based on position |
| SEEK_BACK | Go back 10 seconds | Seek -10 seconds | Clamp at zero |
| PLAY_ARTIST | Play songs by [artist] | Best local match | Confirmation or Not found |

Quality rules:

- Playback ducks while Listening.
- Listening timeout is at most six seconds and retry occurs only once.
- Confidence threshold is configurable through simple Remote Config or Build Config.
- Log intent and execution result, not raw voice.
- Resolve artist names with normalization, fuzzy matching and playback-history priority.
- If cloud STT is used, separate consent and transparent retention/processing disclosure are required.

## 7. Screen and state map

| # | Screen | Purpose | Required states |
|---:|---|---|---|
| 1 | Splash | short preparation | Loading, Migration Error |
| 2 | Onboarding | value and Voice introduction | First Run, Skip |
| 3 | Permissions | file, microphone, notification | Granted, Denied, Permanently Denied |
| 4 | Home / Library | music access | Scanning, Empty, Ready |
| 5 | Search | local search | No Result, Result, Error |
| 6 | Full Player | control and queue | Playing, Paused, Corrupt File |
| 7 | Driving Mode | minimal interaction | Playing, Paused, No Track |
| 8 | Voice Overlay | Listening and Result | Listening, Confirm, Failed |
| 9 | Liked Songs | play favorites | Empty, Ready |
| 10 | Settings | data, Bluetooth and Privacy | Default, Updated |

Driving Mode uses one layer, a dark high-contrast theme, readable typography, limited motion and touch targets of at least 48dp.

## 8. Ten-week plan

| Week | Deliverable | Gate |
|---:|---|---|
| 1 | Repository, architecture, CI, Design Tokens and crash logging | Healthy build on two devices |
| 2 | Room, MediaStore, incremental scan and Library | 5,000 files without blocking error |
| 3 | Media3, queue, background and notification | Playback with screen off |
| 4 | Audio Focus, calls, headset, Bluetooth and persistence | Edge-case matrix green |
| 5 | Home, Search, Full Player and empty/error states | Complete manual flow |
| 6 | Driving Mode and Bluetooth connection suggestion | Stationary-vehicle test |
| 7 | STT/TTS, state machine and five control intents | 90% laboratory success |
| 8 | Play Artist, ambiguity, Analytics and Privacy controls | Golden scenario complete |
| 9 | Device/vehicle QA, Accessibility and P0 fixes | Zero open P0 |
| 10 | Alpha, closed Beta package, tester guide and 50-person launch | Go/No-Go review |

If week 8 slips, Like and Bluetooth Suggestion are removed. Player, Voice and Driving Mode are not cut.

## 9. Proposed technical baseline

- Native Android 10+
- Kotlin and Jetpack Compose
- Media3/ExoPlayer and a service-owned player
- MediaStore, Room and DataStore
- Android STT/TTS behind a replaceable interface
- Restricted deterministic Intent Mapper
- Coroutines/Flow and Hilt
- Privacy-safe crash reporting and analytics
- No backend, account, cloud sync or streaming


## 10. QA matrix

Test levels:

- **Unit:** queue, seek, intent mapper and name normalization
- **Integration:** Room, Media Service, STT and Audio Focus
- **Field:** road noise, Bluetooth and different vehicles

P0 scenarios:

| Scenario | Expected | Acceptance |
|---|---|---|
| Incoming call during playback | Pause/Duck and logical return | No overlap or crash |
| Bluetooth disconnect | Immediate pause | No unintended speaker playback |
| Corrupt file in queue | Controlled skip | Queue continues and error is recorded |
| UI rotation/closure | Player continues | UI remains synchronized with service |
| Medium road noise | Direct intent detected | At least 85% success |
| Two similar artists | Short confirmation | No automatic low-confidence execution |

Device matrix: Pixel/Android Stock, Samsung/One UI, Xiaomi/MIUI; Android 10, Android 12 and Android 14+; at least three vehicles.

Any repeatable playback crash, wrong output route or serious wrong Voice execution is P0 and blocks release.

## 11. Closed Beta

Sample:

- 50 qualified drivers
- 14 days of natural use
- 3+ vehicle/head-unit models
- at least 30 minutes driving per day
- vehicle Bluetooth use
- at least 50 local music files
- ability to use the six English commands
- informed consent for non-audio diagnostic data

Protocol:

1. Baseline interview and current music-control method
2. Five-minute stationary training and golden scenario
3. Natural use for 14 days without daily coercion
4. Short surveys on days 3 and 7
5. Final interview and cohort/intent/manual-correction analysis

| Signal | Green | Yellow | Red |
|---|---:|---:|---:|
| Driving Mode reuse | at least 50% | 30-49% | under 30% |
| Command success | at least 85% | 70-84% | under 70% |
| Manual correction | under 15% | 15-25% | over 25% |
| Crash-free session | at least 99% | 97-98.9% | under 97% |

## 12. Telemetry

| Event | Allowed properties | Purpose |
|---|---|---|
| driving_mode_started | entry_source, bt_connected | Count and entry path |
| voice_button_pressed | session_id, playback_state | Voice-start rate |
| intent_detected | intent_type, confidence_bucket | Intent distribution and accuracy |
| command_executed | intent_type, latency_bucket | Success and speed |
| manual_correction | previous_intent, correction_type | Friction after Voice |
| player_error | error_code, route_type | Player stability |
| session_completed | duration_bucket, commands_count | Retention and North Star |

Allowed: intent type, confidence/latency buckets, player error code and coarse audio-route type.

Prohibited: raw audio, full titles of personal files, precise location, contacts and unnecessary identifiers.

## 13. Ownership and final gate

| Area | Suggested owner | Output |
|---|---|---|
| Product | Product Owner | Scope, metric, recruiting, Decision Gate |
| Android | Android Engineer | Player, Voice, Driving and release build |
| Design | Product Designer | 10 screens, Voice states and Accessibility |
| QA | QA / Team | device, vehicle and regression matrix |
| Privacy | Product + Legal Review | consent, retention and access copy |

GO requires zero open P0, internal crash-free at least 99%, golden scenario on three vehicles, approved Privacy Copy, reliable Analytics and a documented Rollback path.

No-Go reasons include unintended speaker playback, repeated wrong commands, unclear call behavior, voice-data storage without consent or inability to measure the hypothesis.

After successful Beta, Discovery is limited to one legal music-service connection and the possibility of iOS. Neither enters the committed roadmap before Beta evidence.
