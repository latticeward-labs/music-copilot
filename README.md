# Music Copilot

> Voice-first local music control for driving, by **Latticeward Labs**.

Music Copilot MVP v1.1 is a native Android 8.0+ application that plays the user's lawful on-device audio and reduces manual interaction through a dedicated Driving Mode and six short English push-to-talk commands.

## Source of truth

Product and implementation decisions are governed by:

1. `Music_Copilot_PRD_v1.1_FA.pdf` — **Approved for MVP**
2. `Music_Copilot_MVP_v1.1_FA.pdf` — **Ready for Development**
3. Accepted ADR-0001 through ADR-0006 and `docs/TECHNICAL_DECISIONS.md`
4. This repository's derived execution documents and GitHub Issues

If a derived repository item conflicts with PRD v1.1 or MVP v1.1, the product sources win and the conflict must be corrected through change control or a superseding ADR.

## Locked MVP

**In scope**

- Native Android 8.0+ (`minSdk 26`), Kotlin and Jetpack Compose; `compileSdk 36` / `targetSdk 36` for the planned 2026 release
- Local audio discovery via MediaStore; songs, artists, albums, folders, local search
- Media3 playback service, queue, seek, persistence, background playback
- Media notification, lock-screen and Bluetooth controls
- Audio Focus, calls, headset disconnects and route changes
- Driving Mode with large fixed controls and an immediate exit
- Push-to-talk, on-device-first STT/TTS behind replaceable interfaces; network-capable recognition only after clear, revocable consent, with complete manual fallback
- Six English intents: Play, Pause, Next, Previous, Seek Back 10s, Play Artist
- One-step confirmation for ambiguity; one retry for failed speech
- Local history/settings and optional simple Liked Songs (P2)
- Privacy-safe telemetry, crash diagnostics, accessibility and field QA
- Closed Beta: 50 qualified drivers, 14 days, at least 3 car models

**Out of scope**

Streaming, downloads, accounts, backend, cloud sync, advanced playlists/social, wake word, open-ended conversation, Android Auto, CarPlay, iOS, DSP/EQ, crossfade, humming recognition and generative music.

## Success gates

- Direct-command success: **≥ 85%**
- Manual correction after voice command: **< 15%**
- Serious wrong-command execution: **< 3%**
- Voice response P95: **< 2.5 s** after end of speech
- Serious-crash-free sessions: **≥ 99%**
- Driving Mode reuse: **≥ 50%** of Beta testers
- Weekly return: **≥ 30%**
- Release blockers: zero open P0, no unintended speaker playback, no ambiguous call behavior

## Start here

1. [Product Scope](docs/PRODUCT_SCOPE.md)
2. [Technical Decisions](docs/TECHNICAL_DECISIONS.md)
3. [Figma Design Plan](docs/FIGMA_DESIGN_PLAN.md)
4. [10-week Roadmap](docs/ROADMAP.md)
5. [Voice Contract](docs/VOICE_CONTRACT.md)
6. [Analytics & Privacy](docs/ANALYTICS_AND_PRIVACY.md)
7. [QA & Beta Plan](docs/QA_AND_BETA_PLAN.md)
8. [Definition of Done](docs/DEFINITION_OF_DONE.md)

Development begins only from an Issue marked **Ready** on the private project board. P0 Player, Voice and Driving work cannot be traded away for P1/P2 features.

## Repository policy

- `main`: reviewed stable state
- `develop`: integration branch
- `feature/<issue>-<name>`, `fix/<issue>-<name>`, `docs/<issue>-<name>`
- No secrets, raw voice, personal media metadata, commercial audio or signing material in Git
- App code must build without private credentials

Built by **Latticeward Labs**.
