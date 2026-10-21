# Issue #6 — Phase P03: Permission-aware Android MediaStore Scanner

**Date:** 2026-10-22  
**Disposition:** **P03_IMPLEMENTED_STATICALLY_VERIFIED / P04_READY (scoped)**. This is a verified source implementation and GitHub readback, **NOT** final Android build, JVM-test, emulator, device or product acceptance.  
**Repo / branch:** [Issue #6](https://github.com/latticeward-labs/music-copilot/issues/6) / `feature/6-mediastore-library-browse-search`.  
**Source package:** attached `MSM2(1)(1).rar` (12 PDFs, SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`), unchanged from verified P00/P01/P02 English authority register.  
**Controlling sources:** English [PRD FR-01](../PRD.md), [MVP US-02](../MVP.md), accepted [ADR-0003](../adr/README.md), [#16 data/port contract](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md) §6.1, [runtime policy](../architecture/RUNTIME_POLICIES.md), [UF-02](../user-flows/UF-02_PERMISSION_RECOVERY.md), [UF-03](../user-flows/UF-03_LIBRARY_READINESS.md), [UF-04](../user-flows/UF-04_BROWSE_AND_SEARCH.md), [P02 design](ISSUE_6_PHASE2_DATA_CONTRACT.md).  
**External API confirmation:** [Android shared-media volumes and access](https://developer.android.com/training/data-storage/shared/media); [MediaStore volume names](https://developer.android.com/reference/android/provider/MediaStore); [Android MediaColumns](https://developer.android.com/reference/android/provider/MediaStore.MediaColumns).

## 1. Delivered code and dependency compatibility

- `app/src/main/java/com/latticeward/musiccopilot/data/mediastore/MediaStoreScanner.kt`: real scanner and one-item requery, platform-volume-aware local ID codec, pure input sanitation policy, extended scan DTO.
- `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue6Phase3MediaStoreContractTest.kt`: **nine synthetic JUnit test methods authored, NOT_RUN**; P12 T01/T05 must execute them with Android/version-specific provider tests.
- No `AndroidManifest.xml` change: it already declares `READ_EXTERNAL_STORAGE` (maxSdk 32) and `READ_MEDIA_AUDIO`, requested/handled only by Issue #5. No permission prompt or implicit scan added.
- No `LibraryRepository`, `LibraryScanUseCase`, Room schema, `LibraryRowMapper`, App UI, player/queue, DataStore or Quality/telemetry mutation. DTO adds **trailing nullable default properties**; existing 5-argument `ScannedAudio(...)` tests and callers remain source-compatible. Current Room adapter intentionally discards new metadata until P04/P06.

## 2. Actual scanner behavior

| Source concern | P03 implementation | Safety / future proof |
|---|---|---|
| Android 10–12 / API 29–32 | Check current OS `READ_EXTERNAL_STORAGE` before querying | No cache/data-store consent inference; OS rechecked |
| Android 13+ / API 33+ | Check `READ_MEDIA_AUDIO` | Permission request remains Issue #5; no JIT trigger here |
| Volume enumeration | `MediaStore.getExternalVolumeNames(context)`; iterate **specific mounted** volumes, never query IDs from synthetic aggregate `VOLUME_EXTERNAL` | Prevents same numeric MediaStore ID on two devices/volumes collapsing into one source ID; empty/invalid volume set is **recoverable failure**, not fabricated Empty |
| Supported audio only | Query `Audio.Media.getContentUri(specificVolume)` with `IS_MUSIC != 0`, and double-check each returned row flag; skip non-audio MIME when known | Null/blank MIME allowed for older providers, but only with positive music flag |
| Metadata | Read ID/title/artist/album/`ALBUM_ID`, duration, `MIME_TYPE`, `VOLUME_NAME`, `RELATIVE_PATH`, size and modified date; API 30+ `GENERATION_MODIFIED` guarded | No `DATA`/absolute filesystem path; unavailable fields nullable; dates/generation are hints, never authority |
| Missing/invalid rows | Reject negative/null ID, non-music, unusable/blank title, non-audio MIME, conflicting reported source volume; sanitize optional metadata and relative folder | P04 owns extended unknown-tag/display fallback; source rows never write/delete user files |
| Cross-volume ID key | New `audio:v:<specific-volume>:<nonnegative-id>` with validated volume syntax | Opaque internal string is still sensitive—never telemetry |
| Backwards-compatible legacy ref | `audio:<id>` accepted only as **primary volume** reference; `decode(ref)` retained for existing code/tests; `decodeScoped(ref)` for platform-safe queries | Legacy ID cannot ambiguously match a removable device. A completed P05/P06 reconcile replaces derived old IDs with scoped IDs |
| Requery before selection | Query one source item on correct mounted volume with music filter; open one read-only file descriptor; missing/unreadable/IO failure returns null, security failure propagates | Readability checked for **one selected item**, not 5,000 files during scan; MediaSessionService/#7 still owns actual playback |
| Cursor and coroutine | `cursor.use` ensures closure; `currentCoroutineContext().ensureActive()` in per-volume/per-row work; fresh access checks before/after and every 128 rows | Cancellation cannot commit partial results; underlying `ContentResolver.query()` may block until provider returns—cancellation-signal enhancement belongs P05 |
| Provider failure | Null Cursor, malformed query/schema or unavailable volume **throws**; aborts entire scan rather than interpreting as zero rows | Existing RoomLibraryRepository handles `SecurityException` as revoked and other provider errors as typed `LIBRARY_RECONCILE_FAILED`/Retry or Rebuild. No raw messages to UI or telemetry |

**Key consistency rule:** one broken/offline volume must not yield a successful scan containing only the others, because the existing P05 predecessor `replaceAll` would otherwise erase derived rows for an inaccessible volume and publish false Empty/Ready. The implementation throws before returning a partial collection and leaves the repository's existing recovery path responsible for previous trusted entries.

**Source of truth:** OS permission → MediaStore existence/volume/membership → transient `ScannedAudio` → Room **derived rebuildable projection**. Nothing here treats cached Room as proof of grant/existence, mutates the user's audio or lets the UI own the queue.

## 3. Static source/readback and test inventory

Verified from GitHub after writes:
- `READ_EXTERNAL_STORAGE` / `READ_MEDIA_AUDIO` level gate; no implicit permission request; mounted concrete volumes and namespaced IDs.
- `IS_MUSIC` predicate + per-row filter and MIME gate, API 29+ volume/relative path, `ALBUM_ID`, API 30+ generation conditional.
- Cursor `use`, per-row cooperative cancellation, mid-scan access recheck, null-cursor fail closed, no `DATA`, no source writes or media deletion.
- Requery targets the exact mounted volume and probes the file once read-only; v1 `audio:<id>` remains primary-only.
- Nine **authored** synthetic JUnit methods: legacy decode, cross-volume noncollision, malformed/untrusted refs, oversized IDs, backward DTO construction, extended metadata, relative path rejection, required/optional strings, MIME/duration policy.
- Android build/compilation, actual JVM run, native ContentResolver/Cursor instrumentation, runtime permission, OEM removable-volume test, failure injection, performance and physical device results are **NOT_RUN** for this P03 contract gate. Static proof cannot establish Android provider behavior.

## 4. Downstream phases / explicit limitations

- **P04:** normalize incomplete artist/album/title and folder policy for final display/index, dedupe metadata; treat Android unknown sentinel.
- **P05:** implement *actual* incremental and cancellable reconciliation/volume-generation/checkpoint strategy. Current `RoomLibraryRepository.reconcile()` still calls `replaceAll`; P03 generation values do **not** mean P05 is done. Strengthen cancellation propagation to long-blocking Android provider query if needed; handle removable/unmounted volume without false file deletions.
- **P06:** implement Room V1→V2 migration, change mapper and DAO/indexes/query. Current `@Database(version=1)` unchanged; new DTO fields are not yet persisted.
- **P07:** scan-ready/recovery and lifecycle coordination with OS callback authority.
- **P08/P09:** approved Home/Library and Search Compose views; no such UI delivered by P03.
- **P10/#7:** validate selected local item and typed PlaybackGateway handoff; #7 real Media3 playback/queue still OPEN. The read-only probe is not playback acknowledgement.
- **P11/P12:** full test matrix T01–T10, Android 29/31/33/35 device/emulator evidence, permission deny/revoke/races, fake source errors, corrupt metadata, multi-volume removable storage, 5,000-file measurement, privacy/a11y, and **one consolidated P00–P11 APK built at P12** for owner M01–M34. Do not mark any as PASS before actual execution.

## 5. P03 acceptance / audit

- [x] Real permission-aware, Android 10+ MediaStore discovery code committed in feature branch.
- [x] Volume-aware stable ID and backwards-compatible v1 ref parse/requery contract.
- [x] Source metadata for album/relative folder/volume/duration and guarded API30 generation.
- [x] No unauthorised scan; no filesystem media modifications; no personal media diagnostics.
- [x] Mid-scan permission recheck and cooperative cancellation; provider failure is not false Empty.
- [x] One-item read-only requery verifies current media source and readability.
- [x] Synthetic JUnit coverage authored for eventual P12 execution; full technical/device tests not claimed.
- [x] No premature Android build/APK, final PR, merge, Issue close or original AC check-off.
- [ ] Actual Android/JVM/real-volume/on-phone matrix, benchmarks, migrations and library UI — **P12 / later phases**.

**Decision:** `P03_PASS_SCOPED_STATIC_SOURCE / P04_READY`, contingent on final GitHub readback. This scoped engineering gate is not production certification. The 12 original Issue #6 acceptance checkboxes remain unchecked; optional Like/Liked stays P2-deferred and #73 remains separate.
