# ADR-0003: Local-first data boundaries

- **Status:** Accepted
- **Date:** 2026-07-27
- **Reviewed:** 2026-08-06
- **Authority:** [PRD v1.0](../PRD.md) and [MVP v1.0](../MVP.md)
- **Review evidence:** [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)
- **Outcome:** Accepted after revision of authority, cache and privacy boundaries

## Context

The MVP plays lawful audio already present on the device, scans incrementally, supports a 5,000-file library, stores simple app-owned state and has no account, product backend or cloud sync. Data ownership must distinguish device media from derived indexes, settings and telemetry.

## Decision drivers

- Respect Android media access and user consent
- Keep primary product data on-device
- Avoid treating a derived index as ownership of media
- Support incremental scanning and predictable rebuild behavior
- Prevent personal media metadata from entering analytics

## Decision

- **MediaStore** is the discovery authority for user-authorized device audio and platform identifiers.
- **Room** stores a rebuildable derived library index plus app-owned structured data such as Likes and short history when included by priority.
- **DataStore** stores small preferences and settings, not the media catalog.
- Audio bytes remain outside app databases; deleting a derived index does not delete user media.
- Primary product data remains on-device. MVP v1.0 has no product backend or cloud sync.
- Scanning is incremental and targets 5,000 files without blocking error.
- Raw voice, full personal-file titles and other prohibited personal data do not enter analytics.

Exact schemas, invalidation rules, permission-version handling, migrations and repository contracts remain for Issue #16.

## Consequences

### Positive

- Clear source-of-truth and rebuild boundaries
- Local operation without an account or cloud dependency
- Predictable data clearing and an index optimized for browse/search

### Costs and risks

- MediaStore changes, revocation and OEM behavior require reconciliation
- Derived indexes can become stale
- Schema/migration mistakes can break scanning or restore
- Metadata can be incomplete or inconsistent

## Alternatives considered

- **Catalog only in Room:** rejected because MediaStore remains authoritative.
- **Query MediaStore for every UI operation:** rejected because a derived normalized index supports browse/search and performance.
- **Files/shared preferences for structured indexes:** rejected due to query, migration and consistency limits.
- **Cloud database/sync:** rejected by locked scope.

## Validation and open evidence

- Initial/incremental scan on 5,000 files
- Add/remove/change reconciliation
- Permission denial, permanent denial and revocation recovery
- Corrupt/incomplete metadata handling
- Issue #16 spike: authority/cache matrix, schema, migrations and versioned permission behavior
