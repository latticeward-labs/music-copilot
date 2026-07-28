# Product Scope - Approved MVP v1.0

**Authority:** [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md)  
**Status:** Approved product scope; ready for planning  
**Timebox:** 10 weeks

Music Copilot v1.0 is an Android 10+ local-music player for drivers. It combines a stable player, a one-layer Driving Mode and six English push-to-talk commands. It validates whether one tap plus one short command reduces manual interaction and earns repeat use.

## P0 - release blocking

- Local Library: songs, artists, albums, folders and search
- Player: play, pause, next, previous, seek, queue, background, lock screen and Bluetooth
- Audio stability: Audio Focus, calls, headset and route handling
- Driving Mode: large controls, microphone and immediate exit
- Voice: push-to-talk, six intents, short TTS, one-step ambiguity confirmation and one retry
- Privacy-safe telemetry, crash reporting, accessibility and field QA

## P1

- Manual Driving Mode activation or a suggestion when the selected Bluetooth device connects
- Queue and track-position persistence after the UI closes
- Short local playback history

## P2

- Simple Like/Unlike and Liked Songs

## Out of scope

Streaming, downloads, cast, account, backend, cloud sync, online catalog, recommendations, advanced playlists/social, iOS, Android Auto, CarPlay, wake word, free/multi-step conversation, motion detection, humming/song recognition, music generation, equalizer, DSP and crossfade.

## Scope-cut rule

If delivery slips, Like and Bluetooth Suggestion are cut. Player, Voice and Driving Mode are not cut. Any incomplete P0 blocks Beta.
