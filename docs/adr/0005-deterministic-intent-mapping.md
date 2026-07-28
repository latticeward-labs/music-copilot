# ADR-0005: Restricted deterministic intent mapping

- **Status:** Accepted
- **Accepted:** 2026-07-28
- **Date:** 2026-07-27
- **Decision owner:** Latticeward Labs
- **Approval:** [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)
- **Authority:** PRD v1.1 FR-05 to FR-07; MVP v1.1 Voice Contract

## Context

Wrong execution during driving is a safety and trust risk. MVP value can be tested with six English intents; open-ended conversation is unnecessary and harder to validate.

## Decision

Map recognized text to exactly six versioned intents:

`PLAY`, `PAUSE`, `NEXT`, `PREVIOUS`, `SEEK_BACK`, and `PLAY_ARTIST`.

- Mapping is deterministic, side-effect free and independently unit-testable.
- Direct control intents use an allowlisted phrase grammar.
- `SEEK_BACK` is fixed at ten seconds and clamps at zero.
- `PLAY_ARTIST` searches only the local Room index.
- One high-confidence match executes directly.
- Low confidence or multiple plausible artists requires one short confirmation.
- No match or one failed retry returns safely to manual control without disrupting playback.
- Confidence thresholds come through a typed local configuration boundary with a versioned `BuildConfig` fallback; Remote Config is not on the MVP critical path.
- Raw transcript and artist text are prohibited from analytics.

## Consequences

Behavior is predictable, measurable and safe to regression-test. The trade-off is reduced language flexibility and no open conversation.

## Rejected alternatives

- **LLM/open-ended command interpretation:** non-deterministic, unnecessary and outside scope.
- **Silent best-guess artist execution:** unacceptable wrong-command risk.
- **Expanding intent count opportunistically:** breaks the locked MVP and test matrix.

## Validation

A versioned phrase fixture covers all six intents, negative phrases, ambiguity, similar artists, accents/diacritics, no match, threshold boundaries, retry and latency measurement.
