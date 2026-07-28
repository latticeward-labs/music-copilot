# Music Copilot

> Voice-first local music control for driving, by **Latticeward Labs**.

Music Copilot MVP v1.0 is an Android 10+ application that plays lawful audio already on the user's device and tests whether a dedicated Driving Mode plus six short English push-to-talk commands can reduce manual music interaction.

## Source of truth

1. [PRD v1.0](docs/PRD.md) - approved product requirements
2. [MVP v1.0](docs/MVP.md) - execution and validation plan
3. Proposed [Technical Decisions](docs/TECHNICAL_DECISIONS.md) and [ADRs](docs/adr/README.md)
4. Derived execution documents, Figma and GitHub Issues

If a derived artifact conflicts with PRD v1.0 or MVP v1.0, the v1.0 product documents win.

## MVP scope

**In scope**

- Native Android 10+, Kotlin and Jetpack Compose
- Local audio through MediaStore; songs, artists, albums, folders and local search
- Media3 player, queue, seek, background playback, notification, lock-screen and Bluetooth controls
- Audio Focus, calls, headset disconnect and route handling
- Driving Mode with large controls and immediate exit
- Push-to-talk with six English intents: Play, Pause, Next, Previous, Seek Back 10s and Play Artist
- One-step ambiguity confirmation, one retry and short TTS
- Short local history; Bluetooth hint as P1; simple Like/Liked Songs as P2
- Privacy-safe telemetry, crash reporting, accessibility and field QA
- Closed Beta: 50 drivers, 14 days and at least 3 vehicle/head-unit models

**Out of scope**

Streaming, downloads, accounts, backend, cloud sync, advanced playlists/social, wake word, free conversation, Android Auto, CarPlay, iOS, DSP/EQ, crossfade, humming recognition and generative music.

## Success gates

- Direct-command success: **at least 85%**
- Manual correction after Voice: **under 15%**
- Serious wrong-command execution: **under 3%**
- Voice response P95: **under 2.5 seconds**
- Crash-free sessions: **at least 99%**
- Driving Mode reuse: **at least 50%**
- Weekly return: **at least 30%**

## Start here

1. [PRD v1.0](docs/PRD.md)
2. [MVP v1.0](docs/MVP.md)
3. [Product Scope](docs/PRODUCT_SCOPE.md)
4. [Technical Decisions](docs/TECHNICAL_DECISIONS.md)
5. [Figma Design Plan](docs/FIGMA_DESIGN_PLAN.md)
6. [10-week Roadmap](docs/ROADMAP.md)
7. [Voice Contract](docs/VOICE_CONTRACT.md)
8. [Analytics & Privacy](docs/ANALYTICS_AND_PRIVACY.md)
9. [QA & Beta Plan](docs/QA_AND_BETA_PLAN.md)
10. [Definition of Done](docs/DEFINITION_OF_DONE.md)

Development begins only from an Issue marked **Ready** on the project board. P0 Player, Voice, Driving and Stability work cannot be traded for P1/P2 features.

## Repository policy

- `main`: reviewed stable state
- `develop`: integration branch
- `feature/<issue>-<name>`, `fix/<issue>-<name>`, `docs/<issue>-<name>`
- No secrets, raw voice, personal media metadata, commercial audio or signing material in Git
- App code must build without private credentials

Built by **Latticeward Labs**.
