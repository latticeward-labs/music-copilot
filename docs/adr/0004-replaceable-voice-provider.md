# ADR-0004: Replaceable voice provider

- **Status:** Proposed
- **Date:** 2026-07-27
- **Authority:** [PRD v1.0](../PRD.md) and [MVP v1.0](../MVP.md)
- **Approval:** Pending in [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)

## Proposed decision

Keep Android STT/TTS behind a replaceable interface. Push-to-talk is the only microphone entry. Listening lasts at most six seconds, playback ducks and retry is allowed once. Raw voice is not stored. TTS is at most one short sentence.

If cloud STT is used, separate consent and transparent disclosure of processing destination and retention are required.
