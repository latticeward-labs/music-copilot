# UF-03 - Local Scan and Library Readiness

**Product:** Music Copilot MVP v1.0  
**Priority:** P0 - Beta blocking  
**Updated:** 04 August 2026  
**Status:** Stage 1 Traceability Matrix — PASS for this flow; TS-06 and complete FR-01 remain open  
**Tracking:** GitHub Issue #3  
**Canonical diagram:** [`mermaid/UF-03_LIBRARY_READINESS.md`](mermaid/UF-03_LIBRARY_READINESS.md)  

## Authority and boundary

UF-03 defines initial scanning, returning-state routing, incremental reconciliation, truthful Empty/Ready outcomes, recoverable failure, and access-revocation behavior. The official English PRD/MVP are authoritative. Proposed ADR-0003 and technical documents constrain source-of-truth boundaries without adding product scope.

This flow does not approve scanner implementation, Room schema, migration strategy, observer/worker choice, background scheduling, or package structure.

## Ten-field traceability record

| Required field | Controlled value |
|---|---|
| Flow ID | UF-03 |
| User goal | Build or reconcile an account-free local library and see a truthful Scanning, Empty, Ready, failure, or revoked state. |
| Priority | P0 |
| User Story | US-02 |
| Functional Requirement | **FR-01 partial** — scan/index/readiness only; Songs/Artists/Albums/Folders browsing/search completion remains UF-04. |
| Screens and states | S-04 Scanning/Empty/Ready/Recoverable Failure/Revoked-Reconciled; SYS-01 media access; UF-02 recovery handoff. |
| Influencing ADRs | ADR-0001 observable Android state; ADR-0002 service-owned playback during routing; ADR-0003 MediaStore authority/derived index; ADR-0006 local-only/no account/backend/streaming. All remain Proposed pending Issue #2. |
| Failure and recovery | Initial/incremental scan; Retry after recoverable failure; preserve verified prior state where possible; reconcile moved/deleted/inaccessible media; never require reinstall by default. |
| Data and permissions | MediaStore = accessible media existence/identity; local DB index = derived/rebuildable; Android media access authoritative; telemetry non-authoritative and excludes personal media metadata. |
| Open assumptions or evidence | TS-06 storage/reconciliation evidence; Issue #2 ADR outcomes; UF-04 for complete FR-01; final reviewer sign-off. |

## Entry and returning-state decision

Entry may come from newly granted media access, an existing derived index, Rescan, Settings recovery, interrupted preparation, permission/media changes, or a prior recoverable failure.

After rechecking media access, do not force every returning user through visible Scanning:

| Verified current state | Required route |
|---|---|
| Ready and current | Show Ready directly; observe service-owned playback without restart |
| Empty and current | Show Empty directly with Rescan |
| Unknown, stale, interrupted, or reconciliation required | Run the appropriate initial or incremental scan |
| Recoverable index/migration problem | Show recoverable failure with Retry |
| Access denied or revoked | Reconcile false playability and hand off to UF-02 |

## Scan and reconciliation flow

1. Recheck Android media access. If unavailable, route to UF-02.
2. Decide whether the verified current state can be shown directly or scan/reconciliation is required.
3. Select initial scan for first consented setup/rebuild, or incremental reconciliation for Rescan, metadata change, move, deletion, interruption, or stale cleanup.
4. Show `Home / Library - Scanning` only while work is actually required. Keep the UI responsive and never fabricate Ready content.
5. Resolve to one of: verified Ready, current Empty, recoverable failure, or revoked/inaccessible recovery.
6. Reconcile new/changed/moved/deleted media and remove stale playable references.

## Source-of-truth contract

- **Android permission state:** authoritative for access.
- **MediaStore:** authoritative for accessible media existence and platform identity.
- **Local relational index:** derived, rebuildable, and optimized for browsing/search; never proof that media remains accessible.
- **Playback service/MediaSession:** authoritative for active playback while library routing changes.
- **DataStore/preferences:** small local settings and consent only; not the library database.
- **Telemetry:** non-authoritative and never a recovery source.

## Required states

- **Scanning:** visible only for real work; UI responsive; no false Ready; Retry/recovery after interruption.
- **Empty:** valid non-error state; local-file explanation; Rescan; permission recovery only when access is unavailable.
- **Ready:** verified accessible local media; one Home / Library destination with Songs, Artists, Albums, and Folders internal views. UF-04 must define their full browsing/search behavior.
- **Recoverable failure:** concise message and Retry; preserve verified prior state where possible; no reinstall default.
- **Revoked/reconciled:** inaccessible items are not falsely playable; safe UF-02 recovery.

## Reliability, privacy, and accessibility

- Initial and incremental work is non-blocking and targets smooth behavior around 5,000 files.
- Missing, moved, deleted, corrupt, or permission-bound media must not remain falsely playable.
- Analytics/configuration failure must not block scanning, browsing, or manual playback.
- Filenames, titles, artists, albums, folders, content URIs, paths, library contents, raw audio, and full transcripts are excluded from telemetry by default.
- Scanning, Empty, Ready, Retry, Rescan, failure, and permission recovery are textually identified with TalkBack semantics and scalable text.

## Review boundary

- Stage 1 traceability completeness: **PASS**.
- FR-01: **Partial until UF-04**.
- TS-06: **Open**.
- ADR governance: **Open; all six ADRs remain Proposed**.
- Final role sign-off and Issue #3 closure: **Not recorded / not granted**.
- This record does not authorize Wireframes, final architecture, or implementation.
