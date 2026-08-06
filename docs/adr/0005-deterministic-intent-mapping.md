# ADR-0005: Restricted deterministic intent mapping

- **Status:** Accepted
- **Date:** 2026-07-27
- **Reviewed:** 2026-08-06
- **Authority:** [PRD v1.0](../PRD.md) and [MVP v1.0](../MVP.md)
- **Review evidence:** [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)
- **Outcome:** Accepted after revision of safety, configuration and ambiguity boundaries

## Context

The MVP tests reliable one-touch, one-sentence control rather than open conversation. Incorrect execution while driving is a release guardrail. The approved contract has exactly six English intents and requires confirmation for uncertainty or multiple artist matches.

## Decision drivers

- Predictable, exhaustively testable behavior
- Low latency and limited failure surface
- No open-ended conversational scope
- Explicit handling for uncertainty and artist ambiguity

## Decision

Support exactly six English intents: PLAY, PAUSE, NEXT, PREVIOUS, SEEK_BACK and PLAY_ARTIST.

The mapper is deterministic and separate from STT. High-confidence unambiguous commands execute directly. Low confidence, unsupported utterances or multiple plausible artist matches must not auto-execute; artist ambiguity uses one short confirmation, and failed speech allows at most one retry before manual fallback.

Artist resolution may use normalization, bounded fuzzy matching and playback-history priority. A threshold may come from Build Config or simple remote configuration, but a safe local default must exist. Remote configuration may tune only a bounded threshold; it may not add intents, change scope, transmit raw speech/personal titles or bypass confirmation.

Exact phrase grammar, thresholds, fuzzy bounds and confirmation ownership remain evidence-driven details for User Flows, tests and Issue #16.

## Consequences

### Positive

- Small set supports exhaustive unit/field tests
- Behavior is explainable and STT-vendor independent
- Confirmation reduces serious wrong execution

### Costs and risks

- Language coverage is deliberately narrow
- Names, accents and road noise reduce match rates
- Poor tuning can over-confirm or mis-execute
- History ranking must not expose personal titles

## Alternatives considered

- **Open conversational AI/LLM:** rejected as out of scope and less deterministic.
- **Provider-specific classification:** rejected due to coupling.
- **Always execute top match:** rejected because wrong execution is a guardrail.
- **One immutable threshold:** not selected; bounded tuning supports evidence while preserving a safe default.

## Validation and open evidence

- Unit corpus for all intents, unsupported and boundary phrases
- At least 85% direct-command success
- Serious wrong-command execution under 3%
- Similar-artist, no-match, timeout and retry scenarios
- Threshold/fuzzy-match spike and test corpus before Issue #16 approval
