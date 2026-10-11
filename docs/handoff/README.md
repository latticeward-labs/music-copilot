# Issue #17 Phase 9 — Kotlin/Jetpack Compose Engineering Handoff

**Status:** Source-validated design-to-engineering documentation PASS; Android implementation and device QA NOT RUN. The source PRD/MVP archive SHA-256 is `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`.

## Required five implementation contracts

- [SCREEN_SPECS.md](SCREEN_SPECS.md) — all 10 WF screen families (WF-08 is overlay) and 32 source Frame entry/action/exit/Back/recovery contracts, with ownership, layout, typography, allowed analytics and native handoff.
- [COMPONENT_SPECS.md](COMPONENT_SPECS.md) — CMP-01..CMP-23 exact geometry/variants/states, source-of-truth flows, callbacks, 48dp/Driving sizing, TalkBack and no-leak boundaries.
- [STATE_ACTION_MATRIX.md](STATE_ACTION_MATRIX.md) — 126/126 State dispositions, all 32 frame cases and 13 native/system transitions, with correct typed domain/event ownership.
- [COMPOSE_TOKEN_MAPPING.md](COMPOSE_TOKEN_MAPPING.md) — source-exact Night Signal palette, text roles, DP geometry, component overrides, motion/focus and 200% requirements.
- [ASSET_REGISTER.json](ASSET_REGISTER.json) — 149 source assets verified by Git blob SHA/size (117 PNG + 32 SVG), canonical Phase4/5/6 visuals, source SHA-256 where approved, and native system assets null.

## Starting implementation (without changing architecture)

1. Follow [accepted Software Architecture](../SOFTWARE_ARCHITECTURE.md) and [binding Phase-10 corrections](../architecture/review/ISSUE_16_CONTRACT_CORRECTIONS_2026-08-30.md) BEFORE reading older sample interface snippets. Correct `Confirming` uses immutable `options: List<VoiceConfirmationOption>` (1–2). Correct `PlaybackSnapshot.lastError: AppError?` permits ONLY service-owned MediaItem/Playback/Interruption.
2. Single Gradle `:app`, packages `app/`, `core/`, `feature/`, `playback/`, `voice/`, `data/`, `quality/`. Single Activity Compose `NavHost`. Root holds Voice origin and Back priority; Voice never gets its own destination.
3. Implement foundation theme and stateless controls first using COMPOSE_TOKEN_MAPPING and COMPONENT_SPECS. Do not copy screenshots as runtime layouts or create Android permission/lockscreen/notification composables.
4. Provide `LibraryRepository` (MediaStore identity and Room projection), `PreferencesRepository` (DataStore), `PlaybackGateway` (service owned via one controller), `VoiceSessionCoordinator` (replaceable STT/TTS, deterministic six intents) as accepted APIs. No UI direct ExoPlayer, DAO, Cursor, SpeechRecognizer/provider SDK.
5. Wire feature ViewModels to lifecycle-aware StateFlow presentation projections; send commands through application use cases. Success after actual service ACK only; Pending and typed recovery are explicit.
6. Implement GS-01..GS-05 and recovery transitions in STATE_ACTION_MATRIX; use [Phase-8 prototype](../prototype/index.html) only for design interaction walkthrough, never as an actual Android runtime test.
7. Bring Phase-7 [Accessibility/Privacy/System ownership evidence](../high-fidelity/phase-7/ACCESSIBILITY_PRIVACY_SYSTEM_MATRIX.md) into each implementation PR. Real Compose fontScale 200%, focus/TalkBack, 48dp bounds and vehicle/audio routing still require device QA (PH10-MIN-001 OPEN).

## Audit

Run from repository root: `python3 docs/handoff/VALIDATE_PHASE9.py`. It independently checks the five required files, ten screen headings, 23 component specs, 126 State rows, 32 source Frame rows, exact color/geometry entries, and the byte-for-byte Git blob SHA1/accepted image SHA256 of all 149 registered assets.

[PASS result](PHASE9_VALIDATION.json) · [Human-readable verification](PHASE9_VALIDATION.md).

**Non-claims:** Passing this documentation QA is not implementation completeness, Media3 service test, microphone/STT provider test, actual Compose/Accessibility QA or independent named reviewer approval. Do not close Issue #17 until remaining review/acceptance gates are passed.


## Phase 10 source and priority continuity

[Full repository audit](../audit/phase-10/README.md) reattaches this Compose Handoff to the frozen PRD/MVP, all 126 State IDs, 10 Screen Families, five mandatory GS and the accepted Architecture/ADR owner boundaries. Liked Songs remains removable P2/UF-13/FR-09 (not mandatory GS). The [traceability register](../audit/phase-10/PHASE_10_TRACEABILITY_REGISTER.json) covers all design artifacts; zero static scope/ownership mismatches were found. Compose device/200%/TalkBack QA remains pending.

## Issue #17 Phase 11 reviewed implementation corrections

The [AI-assisted six-role review](../design-system/review/ISSUE_17_CROSS_ROLE_REVIEW_2026-10-11.md) closed two implementation-contract **Major** findings before final design review: `CMP-07/08` now resolve Player/Driving/MiniPlayer metadata and artwork from the service-acknowledged `PlaybackGateway.snapshot.currentItem`, while `CMP-10` explicitly keeps the user search query ephemeral in `SearchViewModel`, separate from Room local results and MediaStore media identity. [Findings](../design-system/review/ISSUE_17_FINDINGS_REGISTER.json): zero open Blocker/Major, four owner/gated downstream Minors. [Validation](../design-system/review/ISSUE_17_REVIEW_VALIDATION.md) PASSED. Actual Compose/runtime/real-human acceptance is not claimed.
