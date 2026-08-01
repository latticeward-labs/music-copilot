# UF-03 - Local Scan and Library Readiness

**Product:** Music Copilot MVP v1.0  
**Priority:** P0 - Beta blocking  
**Date:** 01 August 2026  
**Status:** Complete for Cluster A review; cross-version reconciliation evidence remains routed to TS-06  
**Tracking:** GitHub Issue #3  
**Figma:** [UF-03 - Local Scan and Library Readiness](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-28)

## Authority and boundary

UF-03 defines the user-visible states and recovery behavior for initial scanning, incremental reconciliation, Empty/Ready outcomes, recoverable failure, and permission revocation.

The English PRD v1.0 and English MVP v1.0 are the product authorities. ADR-0003, the System Architecture, and the Technical Blueprint provide source-of-truth and verification constraints but do not add scope.

This flow does not approve the exact scanner implementation, Room schema, migration strategy, observer/worker choice, background scheduling, or package structure.

## Product mapping

- FR-01: consented scan and display by track, album, artist, and folder.
- US-02: local files are scanned without creating an account; the index supports song, artist, and album access and later scans are incremental.
- Phase 0 decisions: D-004, D-005, D-006, D-014, and D-020.
- Phase 0 system surfaces: S-03, S-04, SYS-01, and SYS-04.
- Technical spike: TS-06.

## Start states

1. Media access newly granted.
2. Existing derived library index on returning-user startup.
3. User-requested Rescan.
4. Permission restored after Android Settings.
5. Previous scan interrupted or app relaunched during preparation.
6. Permission revoked or media access changed.
7. Recoverable scan failure.

## Source-of-truth contract

- **MediaStore:** authoritative for accessible media existence and Android platform identity.
- **Local relational index:** derived, rebuildable, and optimized for browsing/search; never proof that the underlying media still exists.
- **DataStore/preferences:** small local settings and consent only; not a library database.
- **Telemetry:** non-authoritative and never a recovery source.

## Primary flow

1. **Entry condition arrives.**
2. **Recheck media access.**
   - If not granted, route to UF-02 permission recovery.
3. **Select scan mode.**
   - Initial scan for first consented setup or rebuild.
   - Incremental reconciliation for returning use, Rescan, metadata change, move, deletion, or stale-entry cleanup.
4. **Home / Library - Scanning.**
   - Visible preparation state.
   - Scan work must not block the UI.
   - Do not display fabricated Ready content.
   - Existing index data must not be treated as current playable truth without accessibility checks.
5. **Result decision.**
   - Supported accessible files found.
   - No supported accessible files.
   - Recoverable scan/index failure.
   - Access lost during scan or reconciliation.
6. **Files found -> Home / Library - Ready.**
   - One top-level destination with internal `Songs`, `Artists`, `Albums`, and `Folders` views.
   - Ready may proceed to browse, Search, playback, Driving Mode, or Settings in later flow clusters.
7. **No files -> Home / Library - Empty.**
   - Explain that Music Copilot works with supported local audio already present on the device.
   - Offer Rescan.
   - Show media permission recovery only when access is unavailable.
   - Do not offer account creation, streaming sign-in, upload, download, or online catalog paths.
8. **Recoverable failure.**
   - Show a concise error and Retry.
   - Preserve a verified prior library state where possible.
   - Never require reinstall as the default recovery.
9. **Access lost.**
   - Reconcile inaccessible entries.
   - Mark affected items unavailable.
   - Route to UF-02 without a crash.

## Incremental reconciliation outcomes

| Device/platform change | Required index behavior | User-visible endpoint |
|---|---|---|
| New accessible media | Add derived entry/tokens | Ready updated |
| Metadata changed | Update derived labels and search tokens | Ready updated |
| Media moved or deleted | Remove stale playable reference | Ready or Empty |
| Permission revoked | Mark inaccessible; do not trust stale rows | UF-02 recovery |
| Index/database problem | Rebuild or retry the derived projection | Recoverable scan state |
| Scan interrupted | Resume/restart safely without false Ready | Scanning or recoverable error |

## Required states

### Scanning

- System work is visible.
- UI remains responsive.
- No false Ready state.
- Interruption or failure has a retry/recovery path.

### Empty

- Valid non-error state.
- Explains local-file requirement.
- Preserves Rescan and relevant permission recovery.
- Does not imply supplied or licensed music.

### Ready

- Based on verified accessible local media.
- Supports internal Songs, Artists, Albums, and Folders views.
- Does not create a separate top-level Home and Library destination.

### Recoverable failure

- Concise message.
- Retry.
- No corruption of verified prior state.
- No personal-data upload as recovery.

### Revoked / inaccessible

- Stale entries reconciled.
- Affected media is not falsely playable.
- Routes to permission recovery.

## Performance and reliability constraints

- Initial and incremental scans are non-blocking.
- The product target includes smooth library behavior for approximately 5,000 files.
- Scanning must not occur on the main UI thread.
- Missing, moved, deleted, corrupt, or permission-bound media must not remain falsely playable.
- Analytics, crash reporting, or configuration-service failure must not block scanning, browsing, or manual playback.

## Privacy and analytics

- Do not upload filenames, full titles, artists, albums, folders, content URIs, paths, or library contents.
- Do not serialize arbitrary media entities into telemetry or crash payloads.
- The current approved event allowlist does not define library-scan events. UF-03 introduces no new telemetry event without a separate Privacy review and allowlist update.

## Accessibility

- Scanning, Empty, Ready, Retry, Rescan, and permission recovery are textually identified.
- All actionable controls have labels and TalkBack semantics.
- Text scales without hiding recovery actions.
- State meaning is not conveyed by color alone.

## Valid terminal states

- Home / Library - Ready.
- Home / Library - Empty with Rescan/recovery.
- Recoverable scan failure with Retry.
- Permission recovery through UF-02.
- Reconciled inaccessible media without crash or false playability.

## Open evidence gate

**TS-06** must validate MediaStore access, scoped-storage permission changes, moved/deleted files, incremental reconciliation, and rebuild behavior across supported Android versions before final flow and architecture approval.

## Review result

- Product scope and user-visible readiness states: approved for this cluster by the Product Owner on 01 August 2026.
- Product Design: initial, incremental, Empty, Ready, error, revocation, and reconciliation branches are represented in Figma.
- Android and QA: device/version evidence remains required under TS-06.
- This flow does not close Issue #3 or authorize implementation.
