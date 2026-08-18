# TS-07 - Deterministic Artist Matching and Ambiguity Boundary

**Blocks:** final UF-09 contract and production threshold adoption in Issue #16  
**Date:** 18 August 2026  
**Automated result:** **PASS — workflow run #8 on head `38d71c7a3bf14b6a0afc99e507b07fdad5399a86`**

## Fixed algorithm under test

1. Locale-independent Unicode decomposition, combining-mark removal, lower-case, apostrophe normalization, non-semantic punctuation removal, whitespace collapse.
2. Fixed `PLAY_ARTIST` phrase prefixes only.
3. Exact normalized match first.
4. Otherwise normalized Levenshtein similarity `1 - distance / maxLength`.
5. Stable order: score descending, history rank descending as a secondary tie-breaker, stable local artist ID ascending.
6. Spike candidate boundaries: clear `>=0.88`, plausible `>=0.72`, and direct separation `>=0.08`.
7. Direct execution also requires provider confidence at `T_DIRECT`.
8. Otherwise expose at most two plausible candidates for one confirmation; no plausible candidate is Not Found.

These numeric values are conservative spike candidates, not a hidden amendment to ADR-0005. Final production adoption/tuning remains Issue #16 and must preserve the same no-guess boundary.

## Fixed synthetic corpus

The committed unit corpus contains:

- all six supported intent examples;
- punctuation/case/apostrophe normalization;
- one exact local artist;
- one clear long-name typo case;
- two similarly scored non-exact artists requiring confirmation;
- missing/absent artist producing Not Found;
- low/unavailable provider confidence blocking direct execution;
- unknown and known-unsupported requests remaining distinct;
- 100 repeated resolutions with shuffled input order producing the same stable result.

Only synthetic labels and stable IDs are used. No personal library data, titles, transcripts, or provider audio are evidence.

## Quality interpretation

| Metric | Automated corpus meaning | Release meaning |
|---|---|---|
| Direct-command success | Every supported certain row maps to its expected intent. | Product target >=85% requires real speech/device corpus. |
| Serious wrong execution | Negative/ambiguous/not-found rows must produce zero direct executions. | Product guardrail <3% requires field evidence. |
| Determinism | Same logical corpus/order permutations return the same result. | Production version/parameters must be pinned and regression tested. |
| Confirmation | Options are a subset of the presented stable top two only. | UX/road-noise confirmation testing remains #11/#13. |

## Exit criteria

- [x] Versionable fixed normalization and similarity algorithm exists.
- [x] Fixed synthetic corpus covers supported, ambiguous, missing, unknown, unsupported, and boundary behavior.
- [x] Direct/ambiguous/not-found boundaries are explicit and never default to top match.
- [x] History is only a secondary ordering input and cannot turn an unsafe tie into direct execution.
- [x] Host unit tests PASS, including 100-repeat determinism and zero direct execution in negative rows.
- [x] Final numeric adoption and field quality remain Issues #11/#13/#16.

## Decision boundary

**PASS at the deterministic reference-corpus level.** This closes UF-09 behavior/testability; it does not claim real accent/noise accuracy or the production KPI sample.
