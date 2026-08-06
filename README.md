# Music Copilot

> Voice-first local music control for driving, by **Latticeward Labs**.

Music Copilot MVP v1.0 is an Android 10+ application for lawful local music and six short English push-to-talk commands.

## Source of truth

1. [PRD v1.0](docs/PRD.md)
2. [MVP v1.0](docs/MVP.md)
3. Accepted [ADRs](docs/adr/README.md)
4. Approved [Software Architecture](docs/SOFTWARE_ARCHITECTURE.md)
5. Derived User Flows, Wireframes, design artifacts and Issues

If a derived artifact conflicts with PRD/MVP v1.0, the product documents win.

## Current delivery gate

- Issue #1 - scope lock: **Done**
- Issue #2 - preliminary baseline and ADR review: **Done**
- Issue #3 - User Flows and state inventory: **In Progress**
- Wireframes and final Software Architecture approval follow before implementation.

See [Delivery Workflow](docs/DELIVERY_WORKFLOW.md).

## MVP scope

**In scope:** Native Android 10+, Kotlin/Compose, lawful local audio through MediaStore, Media3 background/system/Bluetooth playback, Audio Focus and route handling, Driving Mode, six deterministic push-to-talk intents, short TTS, ambiguity confirmation, local history, privacy-safe diagnostics, accessibility and Closed Beta.

**Out of scope:** Streaming, downloads, accounts, backend, cloud sync, catalog, social/advanced playlists, wake word, free conversation, Android Auto, CarPlay, iOS, DSP/EQ, crossfade, humming recognition and generative music.

## Success gates

- Direct-command success: at least 85%
- Manual correction: under 15%
- Serious wrong execution: under 3%
- Voice P95: under 2.5 seconds
- Crash-free sessions: at least 99%
- Driving Mode reuse: at least 50%
- Weekly return: at least 30%

## Start here

- [Product Scope](docs/PRODUCT_SCOPE.md)
- [Delivery Workflow](docs/DELIVERY_WORKFLOW.md)
- [User Flows](docs/USER_FLOWS.md)
- [Wireframes](docs/WIREFRAMES.md)
- [Technical Decisions](docs/TECHNICAL_DECISIONS.md)
- [ADR Register](docs/adr/README.md)
- [Software Architecture](docs/SOFTWARE_ARCHITECTURE.md)
- [Voice Contract](docs/VOICE_CONTRACT.md)
- [Analytics & Privacy](docs/ANALYTICS_AND_PRIVACY.md)
- [QA & Beta Plan](docs/QA_AND_BETA_PLAN.md)
- [Definition of Done](docs/DEFINITION_OF_DONE.md)

Development starts only from a Ready Issue. P0 Player, Voice, Driving and Stability work cannot be traded for P1/P2 features.

## Repository policy

Main is reviewed stable state; develop is integration. Use feature, fix and docs branches tied to Issues. No secrets, raw voice, personal media metadata, commercial audio or signing material in Git. Structural UX changes return to flow/wireframe review; consequential architecture changes require a new or superseding ADR.

Built by **Latticeward Labs**.
