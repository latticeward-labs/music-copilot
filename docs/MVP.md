# Music Copilot - Minimum Viable Product Plan

- **Version:** 1.0
- **Status:** Ready for Planning
- **Date:** July 2026
- **Platform:** Android only, Android 10+
- **Timebox:** 10 weeks
- **Validation:** 50-driver Closed Beta
- **Companion:** [PRD v1.0](PRD.md)

> **Authoritative source:** the attached English **Music Copilot Minimum Viable Product v1.0**. This Markdown file is a concise repository representation of that English source. It must not expand, reinterpret, or override the English MVP. Product intent and scope remain governed by the English PRD v1.0.

## 1. Hypothesis and final outcome

If a driver can control music with one touch and one short command, manual interactions will decrease and the driver will use Driving Mode again the following week.

The MVP must answer:

- **Usability:** Are at least 85% of direct commands completed without manual correction?
- **Value:** Do at least half of users use Driving Mode more than once?
- **Performance:** Does P95 voice-response time remain under 2.5 seconds?
- **Stability:** Do at least 99% of journeys finish without a serious Player crash?

## 2. Definition of Done for the MVP

- The local Player is stable in the background, on the Lock Screen, and over Bluetooth.
- Driving Mode is usable with large controls and an immediate exit.
- Six English commands execute, and ambiguity and error states are handled.
- Events required to test the hypothesis are recorded without storing raw audio.
- Internal testing and Closed Beta run across multiple phones and vehicles.

The build is locked to ten weeks. An incomplete P1 feature is removed; any incomplete P0 feature blocks Beta release.

## 3. Exact version scope and priorities

| Epic | Inside the MVP | Outside the MVP |
|---|---|---|
| Player | Local files, queue, background, Lock Screen, Bluetooth | Streaming, download, cast |
| Library | Songs, artists, albums, folders, search | Cloud sync and recommendations |
| Driving | Manual activation, Bluetooth suggestion, large controls | Android Auto and motion detection |
| Voice | Push-to-Talk, six intents, short TTS | Wake word and open conversation |
| User Data | Simple Like, local history, and settings | Account, backend, and sync |
| Audio | Audio Focus and system volume management | EQ, crossfade, and DSP |

Priority contract:

- **P0 - Driving:** Simplified interface and immediate exit
- **P0 - Voice:** Listen, detect intent, execute, and respond
- **P0 - Basic Player:** Play, pause, previous, next, seek, and queue
- **P0 - Stability:** Calls, headphones, and Audio Route
- **P1 - Bluetooth Hint:** Suggest entry; never open automatically
- **P2 - Like:** Included only if Sprint capacity remains

This MVP contains no online catalog, music upload, or content distribution. The app plays only lawful files already present on the user's device.

## 4. User stories and acceptance

| ID | User story | Key acceptance criterion |
|---|---|---|
| US-01 | As a new user, I want to grant permissions at the right time so I understand why each access is needed. | Permission denial does not cause a crash, and a path to Settings is shown. |
| US-02 | I want my files scanned quickly so I can play music without creating an account. | An incremental track, artist, and album index is built; later scans are incremental. |
| US-03 | I want music to continue with the screen off and over Bluetooth. | Media Notification and vehicle controls show the actual state. |
| US-04 | While driving, I want to see only essential controls. | Four primary controls, microphone, title, and exit appear on one screen. |
| US-05 | I want playback to change immediately when I speak a command. | A certain intent executes without a new screen and uses short TTS. |
| US-06 | If a command is ambiguous, I want the app to ask before executing the wrong action. | Low confidence or multiple matches triggers one-step confirmation. |
| US-07 | If STT fails, I want to return quickly to manual control. | One Retry, then return without stopping the music. |

## 5. Golden scenario

Vehicle connects -> Driving Mode -> microphone tap -> “Next song” -> executed in under 2.5 seconds.

Pass condition:

- The next track plays.
- Its title updates.
- The spoken response is at most one sentence.
- No additional screen opens.

## 6. Voice command contract

State model:

`Idle -> Listening -> Processing -> Confirming -> Executed | Failed`

| Intent | Example phrases | Action | Fallback |
|---|---|---|---|
| PLAY | Play / Resume | Start or resume | If queue is empty, use the last track |
| PAUSE | Pause / Stop music | Pause playback | No lengthy response |
| NEXT | Next / Next song | Next track | Announce end of queue |
| PREVIOUS | Previous song | Previous track or beginning | Fixed behavior based on position |
| SEEK_BACK | Go back 10 seconds | Seek -10 seconds | Clamp to zero |
| PLAY_ARTIST | Play songs by [artist] | Best local match | Confirm or Not found |

Quality rules:

- Music volume ducks during Listening.
- Listening timeout is at most six seconds, with only one Retry.
- Confidence Threshold is configurable through a simple Remote Config or Build Config.
- Intent and execution result are logged; raw audio is not recorded.
- Artist-name commands use normalization, fuzzy matching, and playback-history priority.

## 7. Experience map and required states

| # | Screen | Purpose | Required states |
|---:|---|---|---|
| 1 | Splash | Short preparation | Loading, Migration Error |
| 2 | Onboarding | Introduce value and Voice | First Run, Skip |
| 3 | Permissions | Files, microphone, notifications | Granted, Denied, Permanently Denied |
| 4 | Home / Library | Access music | Scanning, Empty, Ready |
| 5 | Search | Local search | No Result, Result, Error |
| 6 | Full Player | Playback and queue control | Playing, Paused, Corrupt File |
| 7 | Driving Mode | Minimal interaction | Playing, Paused, No Track |
| 8 | Voice Overlay | Listening and Result | Listening, Confirm, Failed |
| 9 | Liked Songs | Play favorites | Empty, Ready |
| 10 | Settings | Data, Bluetooth, and Privacy | Default, Updated |

Driving Mode design rules:

- High contrast, readable typography, and limited motion
- One layer; no nested menus or long lists during use
- Minimum 48dp touch target; primary controls should be larger

## 8. Ten-week build plan

| Week | Output | Gate |
|---:|---|---|
| 1 | Repository, architecture, Design Tokens, CI, and crash logging | Healthy build on two devices |
| 2 | Room, MediaStore, incremental scan, and Library | 5,000 files without blocker |
| 3 | Media3, queue, background, and notification | Playback with screen off |
| 4 | Audio Focus, calls, headphones, Bluetooth, and persistence | Edge-case matrix green |
| 5 | Home, Search, Full Player, and Empty/Error states | Complete manual flow |
| 6 | Driving Mode and Bluetooth connection suggestion | Stationary in-car test |
| 7 | STT/TTS, State Machine, and five control intents | 90% laboratory success |
| 8 | Play Artist, ambiguity, Analytics, and Privacy Controls | Complete golden scenario |
| 9 | Device/vehicle QA, Accessibility, and P0 fixes | Zero open P0 |
| 10 | Alpha, Beta package, testing guide, and release to 50 users | Go/No-Go Review |

If Week 8 falls behind, Like and Bluetooth Suggestion are removed. Player, Voice, and Driving Mode cannot be removed.

## 9. QA and release-blocking scenarios

Test levels:

- **Unit:** Intent Mapper, queue, seek, and name normalization
- **Integration:** Media Service, Room, STT, and Audio Focus
- **Field:** Road noise, Bluetooth, and different vehicles

| Scenario | Expected behavior | Pass criterion |
|---|---|---|
| Incoming call during playback | Pause/Duck and sensible return | No simultaneous playback or crash |
| Bluetooth disconnects | Immediate pause | No unintended playback through speaker |
| Corrupt file in queue | Controlled skip | Queue continues and error is logged |
| UI rotates/closes | Player continues | UI state remains synced with Service |
| Moderate road noise | Direct intent is recognized | At least 85% success |
| Two similar artists | Short confirmation | Low-confidence result never auto-executes |

Device and vehicle matrix:

- Pixel / Android Stock
- Samsung / One UI
- Xiaomi / MIUI
- Android 10
- Android 12
- Android 14+
- At least three vehicles or head units

Any reproducible Playback crash, playback through the wrong output, or incorrect Voice execution with serious impact is P0 and blocks release.

## 10. Closed Beta plan

### Sample and duration

- 50 qualified drivers
- 14 days of real-world use
- 3+ vehicle and Head Unit models

### Entry criteria

- At least 30 minutes of driving per day
- Uses the car's Bluetooth
- Has at least 50 local music files
- Can use the six English commands
- Provides informed consent for non-audio diagnostic data

### Protocol

1. Baseline interview and record the current music-control method.
2. Five-minute training and a stationary scenario run.
3. Natural use for 14 days with no daily-use requirement.
4. Short surveys on Days 3 and 7, followed by a final interview.
5. Analyze cohorts, intents, and manual-correction cases.

### Decision indicators

| Signal | Green | Yellow | Red |
|---|---:|---:|---:|
| Driving Mode reuse | >= 50% | 30% to 49% | < 30% |
| Command success | >= 85% | 70% to 84% | < 70% |
| Manual correction | < 15% | 15% to 25% | > 25% |
| Crash-free Session | >= 99% | 97% to 98.9% | < 97% |

## 11. Measurement and dashboard plan

| Event | Allowed parameters | Purpose |
|---|---|---|
| driving_mode_started | entry_source, bt_connected | Count and entry path |
| voice_button_pressed | session_id, playback_state | Voice start rate |
| intent_detected | intent_type, confidence_bucket | Command accuracy and distribution |
| command_executed | intent_type, latency_bucket | Success and speed |
| manual_correction | previous_intent, correction_type | Post-Voice friction |
| player_error | error_code, route_type | Player stability |
| session_completed | duration_bucket, commands_count | North Star and Retention |

Allowed with consent:

- Intent type
- Confidence and latency buckets
- Player error code
- General Audio Route type

Do not collect:

- Raw audio file
- Full titles of personal files
- Precise location
- Contacts or unnecessary identifiers

The weekly dashboard must answer three questions at a glance:

1. Is it used?
2. Does it work correctly?
3. Does the user return?

## 12. Ownership and final gate

| Area | Proposed owner | Deliverable |
|---|---|---|
| Product | Product Owner | Scope, criteria, Recruiting, and Decision Gate |
| Android | Android Engineer | Player, Voice, Driving, and Release Build |
| Design | Product Designer | 10 screens, Voice States, and Accessibility |
| QA | Team / QA | Device, vehicle, and Regression matrix |
| Privacy | Product + Legal Review | Consent, Retention, and permission copy |

### All required for GO

- Zero open P0
- Internal crash-free rate >= 99%
- Golden scenario on three vehicles
- Privacy Copy approved
- Analytics complete and reliable
- Rollback path defined

### Reasons for NO-GO

- Unintended playback through speaker
- Repeated incorrect command execution
- Undefined behavior during calls
- Audio data stored without consent
- Hypothesis cannot be measured

After a successful Beta, Discovery is limited to two topics: lawful integration with a music service and the possibility of an iOS version. Neither enters the committed Roadmap before Beta evidence is available.

## 13. Repository technical cross-reference

The authoritative English MVP defines delivery scope, priorities, acceptance, schedule, QA, Beta, measurement, and final gates. Detailed technical decisions such as service-owned Media3 playback, replaceable provider contracts, deterministic intent mapping, and local data ownership are maintained separately in the ADR and architecture documents. They are not silently promoted into this MVP source mirror.
