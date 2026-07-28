# Product Scope — Approved MVP v1.1

**Decision date:** 2026-07-27  
**Authority:** Canonical [PRD v1.1](PRD.md) + [MVP v1.1](MVP.md)  
**Status:** Locked summary for the 10-week MVP

This is a concise scope view derived from the canonical Markdown sources. It does not override them.

## Product statement

Music Copilot MVP v1.1 is a native Android application for daily drivers who use car Bluetooth and keep lawful music files on their phone. It lets them play local music and control frequent actions with one microphone tap and one short English command.

The version-one hypothesis is: if drivers can control music with one tap and a short command, manual interactions will fall and at least half of Beta testers will reuse Driving Mode.

## Primary user and context

A qualified Beta tester:

- drives at least 30 minutes per day,
- regularly uses vehicle Bluetooth,
- has at least 50 lawful local audio files,
- can use the six English commands,
- consents to non-audio diagnostic telemetry.

Streaming-only listeners are not representative of the first Beta and this sample bias must be recorded.

## Scope and priority

### P0 — release blocking

- Native Android 8.0+ (`minSdk 26`) with `compileSdk 36` and `targetSdk 36` for the planned 2026 release
- Just-in-time media and microphone permissions with denial/recovery states
- MediaStore scan and incremental index
- Songs, artists, albums, folders and local search
- Media3 player service: play, pause, next, previous, seek, queue
- Queue and position persistence after UI closure
- Background, screen-off, notification, lock-screen and Bluetooth controls
- Correct Audio Focus, incoming call, headset disconnect and audio-route behavior
- Driving Mode: artwork, title, artist, four large controls, microphone and immediate exit
- Push-to-talk: duck playback, on-device-first STT/TTS behind replaceable interfaces, restricted intent mapping, execution and short TTS
- Six English intents: PLAY, PAUSE, NEXT, PREVIOUS, SEEK_BACK, PLAY_ARTIST
- Ambiguity confirmation, low-confidence handling, six-second timeout and one retry
- Network-capable system recognizer disabled by default; clear, revocable consent and complete manual fallback
- Corrupt-file recovery without collapsing the queue
- Privacy-safe telemetry and crash diagnostics
- Device, accessibility, vehicle and regression QA

### P1 — required when schedule permits

- Bluetooth connection suggestion for a user-selected vehicle; never forced auto-open
- Short local playback history
- Settings for Bluetooth hint, privacy/data clearing and voice configuration

### P2 — cut first if schedule slips

- Simple local Like/Unlike and Liked Songs playback

## Explicitly out of scope

- Online catalog, streaming, downloads, casting
- Account, authentication, backend, cloud sync
- General recommendation/discovery feed
- Full playlist management or social features
- iOS, CarPlay, Android Auto
- Always-on wake word or open-ended/multi-turn conversation
- Motion detection
- Song/humming recognition or music generation
- DSP, equalizer, crossfade
- Public upload, distribution, advertising, subscription and royalty systems

No online catalog, content upload or distribution exists in this MVP. The application plays only audio already lawfully present on the user's device.

## Required screens and states

1. Splash: loading, migration error
2. Onboarding: first run, value, skip voice education
3. Permissions & Privacy: media/microphone granted, denied, permanently denied; on-device Voice unavailable; optional network-recognizer consent granted/revoked
4. Home/Library: scanning, empty, ready
5. Search: result, no result, error
6. Full Player: playing, paused, corrupt file
7. Driving Mode: playing, paused, no track
8. Voice Overlay: listening, processing, confirm, executed, failed
9. Liked Songs: empty, ready (P2)
10. Settings: default, updated; Bluetooth, privacy, data clearing

Manual Driving Mode activation is P0. The optional Bluetooth suggestion is P1.

Driving Mode uses one layer, high contrast, scalable text, fixed controls and touch targets of at least 48dp. It must not present typing, deep navigation or long result lists.

## Beta success metrics

| Metric | Target |
|---|---:|
| Driving session with ≥1 voice command | ≥50% of weekly users |
| Direct-command success | ≥85% |
| Manual correction after command | <15% |
| Serious wrong-command execution | <3% |
| Voice response P95 | <2.5 s |
| Local command P95 | <1 s |
| Serious-crash-free sessions | ≥99% |
| Serious player error | <1 per 100 sessions |
| Weekly return | ≥30% |
| Driving Mode reuse | ≥50% |

## Delivery and decision gates

- Timebox: 10 weeks
- Prototype: 10 users
- Alpha: internal team
- Closed Beta: 50 qualified drivers, 14 days, at least 3 vehicle models
- GO: quality, repeat use and reduced manual correction meet targets
- ITERATE: value exists but accuracy or stability misses target
- STOP: users do not return or Voice adds no material value

Any incomplete P0 blocks Beta. When schedule slips, P2 Like and then P1 Bluetooth suggestion are removed; Player, Voice and Driving Mode are never cut.
