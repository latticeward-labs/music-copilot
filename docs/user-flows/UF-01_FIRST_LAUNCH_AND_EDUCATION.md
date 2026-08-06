# UF-01 - First Launch and Education

**Product:** Music Copilot MVP v1.0  
**Priority:** P0 - Beta blocking  
**Updated:** 04 August 2026  
**Status:** Stage 1 Traceability Matrix — PASS for this flow; final User Flow approval not granted  
**Tracking:** GitHub Issue #3  
**Canonical diagram:** [`mermaid/UF-01_FIRST_LAUNCH_AND_EDUCATION.md`](mermaid/UF-01_FIRST_LAUNCH_AND_EDUCATION.md)  

## Authority and boundary

The official English PRD v1.0 and MVP v1.0 are the product authorities. Phase 0, the six Proposed ADRs, System Architecture, Technical Documentation, GitHub may constrain interpretation but may not add product scope or manufacture approval evidence.

UF-01 defines first entry, interrupted-first-use resume, non-mutating education replay, readiness handoff, six-command education, and optional practice. It does not approve Wireframes, final visual design, architecture, provider selection, or implementation.

## Ten-field traceability record

| Required field | Controlled value |
|---|---|
| Flow ID | UF-01 |
| User goal | Understand the product, finish or skip first-use education, reach a truthful Home / Library state, and retain manual control if Voice is declined. |
| Priority | P0 |
| User Story | US-01, US-02 |
| Functional Requirement | PRD first-use journey; **FR-01 partial** — first-use consent and readiness only; complete Songs/Artists/Albums/Folders behavior requires UF-04. |
| Screens and states | S-01 Loading/Migration Error; S-02 First Run/Incomplete Resume/Skip/Replay; S-04 Empty/Ready handoff; CA-01 Six-Command Education; CA-02 Voice Access Boundary. |
| Influencing ADRs | ADR-0001 native Android/state; ADR-0002 active playback authority; ADR-0003 local data boundaries; ADR-0004 replaceable provider; ADR-0005 exactly six intents; ADR-0006 no account/backend/streaming. All remain Proposed pending Issue #2. |
| Failure and recovery | Startup Retry; resume actual pending step; non-mutating replay; permission/scan recovery; microphone denial, provider unavailability, or cloud-consent decline returns to manual controls. |
| Data and permissions | Local onboarding/education flags; Android media/microphone state; MediaStore/readiness observation; playback service observation; no account data; no raw audio, transcript, or personal-media telemetry. |
| Open assumptions or evidence | TS-03 provider/disclosure evidence; Issue #2 ADR outcomes; final reviewer sign-off; completion of remaining flows. |

## Entry states

1. Fresh installation with no onboarding-completion flag.
2. Interrupted or incomplete first-use flow.
3. Explicit education replay from Settings.
4. Recoverable startup preparation or migration failure.
5. Returning user with an existing permission, library, queue, and playback state.

## Controlled flow

1. **Launch → Splash - Loading.** Validate local preparation, Android permission state, service connection, and next route. Never fabricate library or playback state.
2. **Startup decision.** Success continues; a recoverable preparation/migration failure shows concise Retry.
3. **Onboarding decision.** Completed users resume the real pending/current state. Active playback is observed from the service and is never restarted by UI routing.
4. **Two-slide introduction.** Slide 1 explains local-music control value. Slide 2 gives the road-attention warning and Push-to-Talk explanation without claiming the app makes driving safe.
5. **Continue or Skip.** Both record onboarding completion locally. Skip never forces microphone or notification access.
6. **UF-02/UF-03 handoff.** Media access is explained before the first consented scan. UF-02 owns permission outcomes; UF-03 owns scan/readiness outcomes.
7. **Truthful readiness result.** Ready, Empty, or a recoverable state may lead to command education; none implies FR-01 is complete.
8. **Six-command education.** Exactly `PLAY`, `PAUSE`, `NEXT`, `PREVIOUS`, `SEEK_BACK`, and `PLAY_ARTIST`.
9. **Practice choice.** Skip requests no microphone. Practice requests microphone just-in-time through UF-02 after explicit action.
10. **Voice boundary.** Permission, provider availability, and any cloud-processing consent are separate checks. Every decline or failure preserves manual browsing and playback.

## Replay invariants

Replay from Settings is educational and non-mutating. It must preserve:

- current Android permission state;
- current verified library/index state;
- current queue;
- active playback and playback position owned by the playback service;
- onboarding completion and unrelated settings.

Replay never starts a fresh scan, clears data, requests permissions automatically, or restarts playback.

## Valid endpoints

- Home / Library - Ready or Empty, based on verified current state.
- Permission or scan recovery without a crash or dead end.
- Recoverable Splash failure with Retry.
- Manual controls preserved after microphone denial, provider unavailability, or cloud-consent decline.

## Source-of-truth and privacy contract

- Android permission state is authoritative for access.
- MediaStore is authoritative for accessible media existence and platform identity.
- The local index is derived and rebuildable.
- The playback service/MediaSession is authoritative for active playback.
- No account, backend, sync, catalog, streaming, upload, download, or forced cloud dependency is introduced.
- Raw audio, full transcripts, titles, filenames, artists, albums, folders, URIs, paths, precise location, contacts, and unnecessary identifiers are excluded from analytics/logging by default.
- No new onboarding, permission, or scan event is approved by this flow.

## Review boundary

- Stage 1 traceability completeness: **PASS**.
- TS-03 evidence: **Open**.
- ADR governance: **Open; all six ADRs remain Proposed**.
- Final role sign-off and Issue #3 closure: **Not recorded / not granted**.
- This record does not authorize Wireframes, final architecture, or implementation.
