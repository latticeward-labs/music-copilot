# Music Copilot - Minimum Viable Product Plan

- **Version:** 1.1
- **Status:** Ready for Development
- **Decision date:** 2026-07-28
- **Owner:** Latticeward Labs
- **Timebox:** 10 build weeks plus a 14-day Closed Beta
- **Canonical companion:** [PRD v1.1](PRD.md)

This Markdown document is the canonical execution source for Music Copilot MVP v1.1. It converts the approved product requirements into build scope, acceptance criteria, delivery gates and validation evidence.

## 1. MVP hypothesis and questions

If a daily driver can control local music with one explicit tap and one short command, manual interaction will fall and the user will return to Driving Mode.

The MVP must answer:

1. **Value:** Do at least half of qualified Beta testers reuse Driving Mode?
2. **Usability:** Do at least 85% of direct commands succeed without manual correction?
3. **Performance:** Is Voice response P95 under 2.5 seconds after end of speech?
4. **Reliability:** Are at least 99% of sessions free from serious Player crashes?
5. **Safety guardrail:** Are serious wrong executions under 3%, with no unintended output-route behavior?

## 2. Definition of the delivered MVP

The MVP is complete only when:

- lawful local audio can be scanned, browsed, searched and played without an account;
- playback remains canonical in background, screen-off, notification, lock-screen and Bluetooth contexts;
- calls, Audio Focus, headset disconnect and route changes behave deterministically;
- Driving Mode provides large fixed manual controls and immediate exit;
- six English push-to-talk commands execute with short feedback and stable fallback;
- on-device-first Voice and separate, revocable network-recognizer consent are implemented;
- prohibited audio/transcript/personal-media telemetry cannot be emitted;
- accessibility, API, OEM, route and vehicle evidence satisfies the release gates;
- a signed Android App Bundle and rollback route are ready for the controlled Beta.

Any incomplete P0 blocks Beta. P2 Like/Liked Songs and then the optional P1 Bluetooth suggestion are cut before Player, Voice, Driving, Privacy or P0 Stability.

## 3. Scope by epic

| Epic | Included | Excluded |
|---|---|---|
| Player | local files, queue, seek, persistence, background, screen off, notification, lock screen, Bluetooth | streaming, download, cast |
| Library | songs, artists, albums, folders, incremental scan, local search | cloud sync, online recommendations |
| Driving | manual entry, one-layer controls, immediate exit; optional selected-Bluetooth suggestion | Android Auto, motion detection, forced auto-open |
| Voice | push-to-talk, on-device-first provider selection, six intents, short TTS, confirmation/fallback | wake word, always listening, free or multi-turn conversation |
| User data | local settings, short history, data clearing; P2 simple Like | account, profile, backend, sync |
| Audio | Audio Focus, call/headset/route behavior, system volume integration | equalizer, DSP, crossfade |
| Quality | allowlisted telemetry, non-sensitive crash diagnostics, accessibility and field QA | raw voice/transcript/personal media collection |

## 4. Priority contract

### P0

Local Library, stable Media3 Player, P0 audio routes, manual Driving Mode, six-command Voice, consent/fallback, Privacy, Accessibility, QA and release evidence.

### P1

Optional selected-Bluetooth entry suggestion, short history and supporting settings. Manual Driving Mode entry remains part of the P0 golden journey.

### P2

Simple Like/Unlike and Liked Songs, only after the week-8 capacity gate.

## 5. User stories and acceptance

| ID | User story | Key acceptance |
|---|---|---|
| `US-01` | As a new user, I want permissions requested when needed so I understand each access. | Denial never crashes; permanent denial offers Android Settings; manual functionality remains available. |
| `US-02` | I want my local files scanned quickly so I can play without an account. | Songs/artists/albums/folders are indexed; later scans are incremental; empty/error states exist. |
| `US-03` | I want playback with the screen off and over Bluetooth. | MediaSession, notification, lock screen and head-unit controls reflect canonical service state. |
| `US-04` | While driving, I want only essential controls. | One layer contains four large controls, microphone, current media and immediate exit; no typing or deep navigation. |
| `US-05` | I want a short spoken command to change playback immediately. | A high-confidence deterministic intent executes without opening another screen; feedback is at most one sentence. |
| `US-06` | If a command is ambiguous, I want confirmation before a wrong action. | Low confidence or multiple plausible artists cause one short confirmation. |
| `US-07` | If Voice fails, I want to return to manual control quickly. | Timeout is at most six seconds, retry occurs at most once, playback remains stable and manual control remains complete. |
| `US-08` | I want control over optional Voice processing and diagnostics. | Network-recognizer consent is separate and revocable; local data and diagnostic controls are understandable and testable. |

## 6. Golden scenario

Vehicle connected -> Driving Mode -> microphone tap -> "Next song" -> `NEXT` detected -> next item starts -> title/artwork updates -> one short response -> no new screen.

Acceptance:

- P95 response is under 2.5 seconds after end of speech;
- Player, MediaSession and UI remain synchronized;
- no raw audio/transcript is persisted;
- failure preserves current playback and manual controls.

## 7. Voice execution contract

### State machine

`Idle -> Listening -> Processing -> Confirming -> Executed | Failed -> Idle`

### Rules

- Explicit push-to-talk only; no wake word.
- Duck playback during listening/response and restore it predictably.
- Listening timeout is at most six seconds.
- Retry at most once.
- Use on-device recognition when available.
- Keep STT/TTS providers behind replaceable interfaces.
- A network-capable recognizer remains disabled without separate, clear and revocable consent.
- If acceptable recognition is unavailable, show a typed unavailable state and preserve complete manual control.
- Persist neither raw audio nor transcript.
- High-confidence single match executes; low confidence or multiple plausible matches confirms once.
- TTS is at most one short sentence.

### Intent acceptance

| Intent | Action | Fallback/edge behavior |
|---|---|---|
| `PLAY` | resume/start current queue | if empty, offer last playable item |
| `PAUSE` | pause | no long response |
| `NEXT` | advance | announce end of queue |
| `PREVIOUS` | restart current or move previous | one documented position threshold |
| `SEEK_BACK` | seek minus 10 seconds | clamp at zero |
| `PLAY_ARTIST` | normalize/fuzzy-match local artist and start queue | one confirmation or not-found; no long list |

Confidence threshold is typed/configurable with a build fallback. Remote Config is not on the MVP critical path.

## 8. Screen/state acceptance map

| Surface | Required acceptance states |
|---|---|
| Splash | loading, migration error |
| Onboarding | value, optional Voice education, skip |
| Permissions & Privacy | rationale, granted, denied, permanently denied, Voice unavailable, optional consent, revocation |
| Home/Library | scanning, empty, ready, error |
| Search | query, results, no result, error |
| Full Player | loading, playing, paused, completed, corrupt/missing file |
| Driving Mode | no track, playing, paused, route warning, immediate exit |
| Voice Overlay | idle, listening, processing, confirming, executed, failed, timeout, unavailable |
| Liked Songs | empty, ready; P2 only |
| Settings | selected-Bluetooth hint, Voice availability/consent, privacy, data clearing |

## 9. Ten-week delivery plan

| Week | Deliverable | Exit gate |
|---:|---|---|
| 1 | Figma journeys/foundations; Android seven-module baseline; CI; privacy-safe crash logging | Healthy design baseline and clean build on API 26 plus a current API |
| 2 | Contextual permissions; MediaStore; Room; incremental Library scan | 5,000-file library without blocking error |
| 3 | Media3 service; queue; persistence; background and notification | Playback continues with screen off |
| 4 | Audio Focus; call/headset/Bluetooth route behavior; edge cases | P0 route matrix green |
| 5 | Home/Library; Search; Full Player; empty/error states | Complete manual flow |
| 6 | Driving Mode and optional selected-Bluetooth suggestion | Stationary-vehicle test passes |
| 7 | Push-to-talk; provider boundary; consent-aware fallback; state machine; five control intents | At least 90% laboratory success on supported Voice sessions |
| 8 | `PLAY_ARTIST`; fuzzy matching; ambiguity; telemetry and privacy controls | Golden scenario complete |
| 9 | API/OEM/device/vehicle QA; accessibility; regression; P0 fixes | Zero open P0 |
| 10 | Internal Alpha; signed AAB; tester guide; rollback and rollout | Go/No-Go review for 50-driver Beta |

## 10. Technical execution baseline

- `minSdk 26`, `compileSdk 36`, `targetSdk 36`; Play target policy rechecked before release
- Kotlin, Jetpack Compose, coroutines/Flow and Hilt
- `MediaSessionService` owns Media3/ExoPlayer and canonical playback state
- MediaStore owns discovery; Room owns derived index/restorable structured data; DataStore owns small typed settings
- Seven initial modules: `:app`, `:core:model`, `:core:domain`, `:core:data`, `:core:playback`, `:core:voice`, `:core:ui`
- Feature areas begin as packages
- No explicit ABI filter unless a reviewed native dependency is introduced
- No backend URL or private credential required for a clean build
- Product networking is absent; approved diagnostic SDKs do not become a product backend

See [Technical Decisions](TECHNICAL_DECISIONS.md) and accepted [ADRs](adr/README.md).

## 11. Test strategy and release blockers

### Test levels

- **Unit:** intent mapper, artist normalization, queue, previous threshold, seek clamp, telemetry schema
- **Integration:** MediaStore/Room, MediaSessionService, STT provider contracts/consent, Audio Focus and persistence
- **UI:** permissions, Library/Search/Player, Driving Mode, Voice Overlay and Settings
- **Field:** road noise, Bluetooth/head units, route transitions and real vehicles

### Compatibility matrix

- Pull-request automation: API 26, 33 and 36
- Nightly automation: API 29, 31 and 34
- Physical: older API 26-29, Android 12/13, Android 16 where available
- OEM: Pixel/stock, Samsung/One UI, Xiaomi/HyperOS or MIUI
- Vehicle/head unit: at least three documented combinations

### P0 scenarios

| Scenario | Required result |
|---|---|
| Incoming call | logical duck/pause and recovery; no overlap/crash |
| Bluetooth/headset disconnect | immediate pause; no unintended speaker playback |
| Route change | service, notification and UI remain synchronized |
| Corrupt/missing item | normalized error, controlled skip and queue continuation |
| UI rotation/closure | service continues; UI reconnects |
| Screen off | playback and controls remain functional |
| Medium road noise | aggregate direct-intent success at least 85% |
| Similar artists | one short confirmation; no low-confidence auto-execution |
| Voice unavailable/timeout/failure/consent revoked | typed state, at most one retry, stable music and complete manual control |
| Permission denial | no crash; clear recovery |
| 5,000 files | incremental scan and smooth UI without blocking error |

Any repeatable serious playback crash, unintended output route, seriously wrong Voice execution, ambiguous call behavior or unapproved Voice data handling blocks release.

## 12. Telemetry and privacy acceptance

### Required allowlisted events

`driving_mode_started`, `voice_button_pressed`, `intent_detected`, `command_executed`, `intent_failed`, `manual_correction`, `player_error`, `session_completed`, `library_scan_completed`.

### Prohibited

- raw audio or microphone recordings;
- raw speech transcript;
- personal filename, title, artist or album;
- precise location, contacts or unnecessary identifiers;
- free-form exception content that can contain personal data.

Network-recognizer consent is separate from telemetry, media and microphone permission. Retention, disclosure, data clearing and disable behavior must be documented and verified before Beta.

## 13. Closed Beta plan

### Sample

- 50 qualified drivers
- 14 days of natural use
- at least 3 vehicle/head-unit models
- at least 30 minutes driving per day
- regular vehicle Bluetooth use
- at least 50 lawful local tracks
- ability to use the six English commands
- informed consent for approved non-audio diagnostics

### Protocol

1. Baseline interview and current control method
2. Five-minute stationary training and golden scenario
3. Natural use for 14 days without daily coercion
4. Short surveys on days 3 and 7
5. Final interview
6. Cohort, intent, latency, correction, route and retention analysis

### Decision bands

| Signal | Green | Yellow | Red |
|---|---:|---:|---:|
| Driving Mode reuse | at least 50% | 30-49% | under 30% |
| Direct-command success | at least 85% | 70-84% | under 70% |
| Manual correction | under 15% | 15-25% | over 25% |
| Serious-crash-free sessions | at least 99% | 97-98.9% | under 97% |

## 14. Final Go/No-Go gate

All are required for Beta entry:

- zero open P0 defects;
- at least 99% internal serious-crash-free sessions;
- golden scenario passes on at least three vehicles;
- approved privacy copy and matching Play data-safety answers;
- verified telemetry dashboard;
- signing custody, release notes, tester guide, incident stop rule and rollback route.

After Beta:

- **GO** when quality, repeat use and correction targets are met;
- **ITERATE** when value is demonstrated but accuracy, latency or stability misses targets;
- **STOP** when repeat use is weak or Voice offers no material value over manual control.

No streaming-service connection, iOS work or other scope expansion begins before this evidence and a documented product decision.
