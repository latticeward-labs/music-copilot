# ADR-0005: Restricted deterministic intent mapping

- **Status:** Proposed
- **Date:** 2026-07-27
- **Authority:** [PRD v1.0](../PRD.md) and [MVP v1.0](../MVP.md)
- **Approval:** Pending in [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)

## Proposed decision

Support exactly six English intents: PLAY, PAUSE, NEXT, PREVIOUS, SEEK_BACK and PLAY_ARTIST.

Certain commands execute directly. Low confidence or multiple artist matches cause one short confirmation. Artist resolution uses normalization, fuzzy matching and playback-history priority. Confidence threshold may use simple Remote Config or Build Config.
