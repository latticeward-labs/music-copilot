# ADR-0003: Local-first data boundaries

- **Status:** Accepted
- **Accepted:** 2026-07-28
- **Date:** 2026-07-27
- **Decision owner:** Latticeward Labs
- **Approval:** [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)
- **Authority:** PRD v1.1 FR-01, FR-02, FR-08 and section 09; MVP v1.1 US-01, US-02 and week 2

## Context

Music Copilot reads lawful audio already on the user's device. It needs fast local discovery, an incremental searchable index, durable application state and small preferences without introducing a server.

## Decision

Use three storage mechanisms with non-overlapping ownership:

| Store | Owns | Must not own |
|---|---|---|
| MediaStore | System-authoritative audio discovery, content URI and current media metadata | Likes, app history, app settings |
| Room | Derived searchable media index, scan bookkeeping, optional Like, short history, restorable playback/session references | Raw audio bytes, secrets, simple preferences |
| DataStore | Small typed preferences, onboarding state, privacy choices and selected Bluetooth hint | Large lists, relational media data, raw transcripts |

Additional rules:

- Media access is requested just in time and denial never crashes the app.
- The index refresh is incremental and reconciles added, changed, missing and corrupt items.
- Playback uses content URIs; application code does not copy personal audio into its own storage.
- Room migrations require automated migration tests.
- Personal media titles, artists, albums and filenames never enter telemetry.
- User-clearable data has an explicit deletion path.

## Consequences

The app remains useful without an account or backend and can support at least 5,000 indexed items smoothly. The trade-off is that device migration and cross-device sync are unavailable in MVP v1.

## Rejected alternatives

- **MediaStore as the only database:** insufficient for app-owned state and stable incremental indexing.
- **Room for preferences:** unnecessary relational overhead.
- **Filesystem crawling/copying:** weaker scoped-storage behavior and higher privacy risk.
- **Cloud index:** conflicts with local-first scope and privacy constraints.

## Validation

Integration tests cover initial and incremental scans, deletion/change reconciliation, corrupt files, permission denial, 5,000-item performance, Room migration and user data clearing.
