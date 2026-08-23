# Stage 2 - Golden Scenarios

**Product:** Music Copilot MVP v1.0  
**Status:** **PASS / APPROVED / COMPLETE / SOURCE-ALIGNED**  
**Decision date:** 23 August 2026  
**Input gate:** [Phase 1 UF-01 through UF-13](../PHASE_1_TRACEABILITY_MATRIX.md) - PASS / COMPLETE  
**Tracking context:** [Issue #3](https://github.com/latticeward-labs/music-copilot/issues/3) is closed for the complete User Flow set  
**Authorities:** [English PRD v1.0](../../PRD.md), [English MVP v1.0](../../MVP.md), current GitHub User Flows, Accepted ADR register and Technical Spike evidence

The durable `golden-scenarios` workspace composes approved User Flows into five primary end-to-end outcomes for Wireframes, Software Architecture and QA planning. Persian/Farsi PRD/MVP translations are excluded as product authorities.

## Approved scenario set

| Scenario | Primary outcome | Flow coverage | Priority | Status |
|---|---|---|---:|---|
| [GS-01 - First Launch to Ready Library](GS-01_FIRST_LAUNCH_TO_READY_LIBRARY.md) | New user reaches a truthful account-free Ready local library | UF-01 -> UF-02 as needed -> UF-03; UF-04 handoff | P0 | APPROVED |
| [GS-02 - Find Music and Start Playback](GS-02_FIND_MUSIC_AND_START_PLAYBACK.md) | Ready user finds accessible local music and begins acknowledged playback | UF-04 -> UF-05; UF-02/03 recovery | P0 | APPROVED |
| [GS-03 - Continue Playback Outside the App](GS-03_CONTINUE_PLAYBACK_OUTSIDE_APP.md) | Background/screen-off/system controllers converge on one MediaSession | UF-05 -> UF-06; UF-11 handoff | P0 + P1 restore | APPROVED |
| [GS-04 - Drive and Execute a Direct Voice Command](GS-04_DRIVE_AND_EXECUTE_DIRECT_VOICE.md) | Explicit Driving Mode + mic tap executes one direct NEXT command | UF-07 -> UF-08; UF-09/10/11/12 recovery | P0 + optional P1 hint | APPROVED |
| [GS-05 - Recover Safely from an Interruption](GS-05_RECOVER_SAFELY_FROM_INTERRUPTION.md) | Calls/focus/routes settle to conditional Resume or Safe Paused | UF-11 with UF-05/06; UF-12 recovery | P0 | APPROVED |

Supporting controls:

- [Stage 2 traceability matrix](STAGE_2_TRACEABILITY_MATRIX.md)
- [Stage 2 sign-off](STAGE_2_SIGNOFF_2026-08-23.md)
- [Phase 1 complete-set sign-off](../PHASE_1_SIGNOFF_2026-08-23.md)
- [Technical Spike evidence index](../evidence/README.md)

## Coverage decision

- `US-01` through `US-07`: covered.
- P0 `FR-01` through `FR-06`: covered.
- P1 `FR-07` and `FR-08`: covered and remain separable/removable where specified.
- P2 `FR-09` / UF-13: already approved at User Flow/reference-contract level but intentionally not a required Golden Scenario; release inclusion remains capacity-dependent and non-blocking.
- UF-01 through UF-12: represented as a primary chain, handoff or mandatory recovery owner.
- ADR-0001 through ADR-0006: Accepted and mapped without changing their decisions.
- TS-01 through TS-10: mapped at their recorded evidence levels; no reference-policy result is promoted into an unperformed physical/OEM/provider/legal/vehicle/Beta claim.

## Required scenario record

Every `GS-##` document contains:

| Field | Controlled content |
|---|---|
| Identity and goal | Stable ID, outcome-oriented title and user value |
| Scope | P0/P1/P2 boundary and explicit exclusions |
| Entry / success | Verified preconditions, terminal state and source of truth |
| Traceability | Exact UF, US, FR, State, Permission/Consent, ADR and Spike mapping |
| Primary sequence | Numbered user/system path with acknowledgement boundaries |
| Recovery | Denial, empty, error, interruption, retry and terminal-safe references |
| Data/privacy | MediaStore/index/service/provider boundaries and prohibited payloads |
| Accessibility/driving | TalkBack, contrast, scalable text, 48dp and distraction limits |
| Review | Decision, date, evidence basis and non-claim boundary |

## Artifact convention and parity

Each scenario has a reviewable Markdown record and a canonical raw Mermaid source:

```text
GS-##_OUTCOME_NAME.md
GS-##_OUTCOME_NAME.mmd
```

The embedded Mermaid block is logically identical to the paired `.mmd` source. Parity was validated for all five pairs at the Stage 2 gate.

## Source-of-truth invariants

1. Android permission state controls access.
2. MediaStore controls accessible media existence/identity; the local index is derived/rebuildable.
3. Playback Service/MediaSession alone controls live queue, item, state and position.
4. Voice recognition/provider/TTS/analytics never become playback authority.
5. P0 live service continuity is distinct from P1 Paused Restore; restore requires explicit Resume.
6. Route disconnect pauses immediately; reconnect never auto-resumes.
7. Remote speech processing requires complete truthful disclosure and separate consent; otherwise Manual Only.
8. No account, backend, cloud sync, online catalog, streaming, upload/download, Android Auto, Cast, iOS or v1.1 capability is introduced.

## Stage 2 exit gate

| Criterion | Result |
|---|---|
| Five agreed Golden Scenario records | PASS |
| Exact US/FR/UF/state traceability | PASS |
| Explicit success authorities and recovery seams | PASS |
| Permission, consent, privacy and accessibility coverage | PASS |
| ADR/spike status and non-claim boundaries | PASS |
| Markdown/Mermaid parity | PASS |
| Scope/P1/P2 separation | PASS |
| Consolidated review decision and date | PASS |

**Final gate:** **PASS / APPROVED / COMPLETE.** These scenario contracts may proceed to Issue #15 low-fidelity Wireframes and Issue #16 final Software Architecture. They do not approve production implementation, physical vehicle execution, Release Candidate or Closed Beta results.
