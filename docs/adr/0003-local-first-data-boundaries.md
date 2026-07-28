# ADR-0003: Local-first data boundaries

- **Status:** Proposed
- **Date:** 2026-07-27
- **Authority:** [PRD v1.0](../PRD.md) and [MVP v1.0](../MVP.md)
- **Approval:** Pending in [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)

## Proposed decision

- MediaStore discovers lawful local audio.
- Room stores the derived local index and app-owned structured data.
- DataStore stores small settings.
- Primary data remains on-device.
- No backend or cloud sync exists in MVP v1.0.
- Scanning is incremental and supports 5,000 files without blocking error.
- Personal file titles and raw voice do not enter analytics.
