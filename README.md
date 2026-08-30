# Music Copilot

> Voice-first local music control for driving, by **Latticeward Labs**.

- **Version:** MVP v1.0
- **Repository status:** Current through Issue #16 Phase 11 final architecture approval
- **Updated:** 30 August 2026
- **Architecture tracking:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)

Music Copilot MVP v1.0 is an Android 10+ application that plays lawful audio already on the user's device and tests whether a dedicated Driving Mode plus six short English push-to-talk commands can reduce manual music interaction.

## Source of truth

1. [PRD v1.0](docs/PRD.md) — approved product requirements
2. [MVP v1.0](docs/MVP.md) — execution and validation plan
3. Accepted [Architecture Decision Records](docs/adr/README.md)
4. [Software Architecture Baseline](docs/SOFTWARE_ARCHITECTURE.md) — **final approved implementation-facing architecture authority from Issue #16**
5. [Technical Decisions](docs/TECHNICAL_DECISIONS.md) — compact decision index linked to the baseline
6. Approved User Flows, Golden Scenarios, Wireframes and their traceability evidence

If a derived artifact conflicts with the controlling English PRD/MVP source package, the controlled product sources win. Accepted ADR history is never silently rewritten.

## Current delivery gate

- Issue #1 — PRD/MVP scope lock: **Done**
- Issue #2 — preliminary technical baseline and ADR review: **Done**
- Issue #3 — User Flows and state inventory: **Done / Closed — 23 August 2026**
- Stage 2 Golden Scenarios and GitHub-native design synchronization: **Done**
- Issue #15 — low-fidelity Wireframes: **Done / Closed — 27 August 2026**
- Issue #16 — Software Architecture: **DONE — Phase 11 final acceptance PASS; PR #46 merged; open Blocker 0, open Major 0**
- Issue #4 — Android foundation: **UNBLOCKED / READY by the completed Issue #16 architecture gate**

See the complete [Delivery Workflow and Approval Gates](docs/DELIVERY_WORKFLOW.md), the final [Software Architecture Baseline](docs/SOFTWARE_ARCHITECTURE.md), the [Phase 11 Final Acceptance Audit](docs/architecture/PHASE11_FINAL_ACCEPTANCE_AUDIT.md), and the [Issue #16 Phase 10 Cross-role Review](docs/architecture/review/ISSUE_16_CROSS_ROLE_REVIEW_2026-08-30.md).

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

### Product

1. [PRD v1.0](docs/PRD.md)
2. [MVP v1.0](docs/MVP.md)
3. [Product Scope](docs/PRODUCT_SCOPE.md)

### Delivery and discovery

4. [Delivery Workflow](docs/DELIVERY_WORKFLOW.md)
5. [User Flows and State Inventory](docs/USER_FLOWS.md)
6. [Low-fidelity Wireframes](docs/WIREFRAMES.md)
7. [GitHub-native Wireframe Workspace](docs/wireframes/README.md)
8. [Delivery Roadmap](docs/ROADMAP.md)

### Architecture and engineering

9. [Software Architecture Baseline](docs/SOFTWARE_ARCHITECTURE.md)
10. [Architecture Traceability Matrix](docs/architecture/ARCHITECTURE_TRACEABILITY_MATRIX.md)
11. [Issue #16 Validation and Test Architecture](docs/architecture/ISSUE_16_VALIDATION.md)
12. [Phase 11 Final Acceptance Audit](docs/architecture/PHASE11_FINAL_ACCEPTANCE_AUDIT.md)
13. [Issue #16 Phase 10 Cross-role Review](docs/architecture/review/ISSUE_16_CROSS_ROLE_REVIEW_2026-08-30.md)
14. [Phase 10 Reviewed Contract Corrections](docs/architecture/review/ISSUE_16_CONTRACT_CORRECTIONS_2026-08-30.md)
15. [Technical Decisions](docs/TECHNICAL_DECISIONS.md)
16. [ADR Register](docs/adr/README.md)
17. [Voice Contract](docs/VOICE_CONTRACT.md)
18. [Analytics & Privacy](docs/ANALYTICS_AND_PRIVACY.md)
19. [QA & Beta Plan](docs/QA_AND_BETA_PLAN.md)
20. [Definition of Done](docs/DEFINITION_OF_DONE.md)

Development begins only from an Issue that satisfies the Definition of Ready in the Delivery Workflow. P0 Player, Voice, Driving and Stability work cannot be traded for P1/P2 features.

## Repository policy

- `main`: reviewed stable state
- `develop`: integration branch
- `feature/<issue>-<name>`, `fix/<issue>-<name>`, `docs/<issue>-<name>`
- No secrets, raw voice, personal media metadata, commercial audio or signing material in Git
- App code must build without private credentials
- Structural UX changes return to User Flow/Wireframe review
- Consequential architecture changes require a new or superseding ADR
- The current architecture status is controlled by `docs/SOFTWARE_ARCHITECTURE.md`; dated phase/history documents retain their historical status only

Built by **Latticeward Labs**.