# Issue #6 — P04: Metadata normalization and deterministic indexing

**Date:** 2026-10-22  
**Result:** `P04_PASS_SCOPED_SOURCE` **after actual GitHub read-back**, not runtime CI, JVM, Android emulator or hardware PASS.  
**Repo:** `latticeward-labs/music-copilot` · **Branch:** `feature/6-mediastore-library-browse-search` · **Issue:** [#6](https://github.com/latticeward-labs/music-copilot/issues/6)

## 1. Source authority and actual starting point

The owner-supplied **12-PDF** `MSM2(1)(1).rar` was opened from its exact uploaded path, all 12 PDFs extracted locally, and relevant pages read with extracted text. Its **SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`** matches the archived source register and earlier Issue #6 phases.

- `Music_Copilot_PRD_v1_EN_Clarified.pdf` p.8 **FR-01**: consent-gated supported local audio and song/artist/album/folder presentation; p.9: incremental scanning and 5,000-song scrolling goals.
- `Music_Copilot_MVP_v1_EN.pdf` p.3: Songs/Artists/Albums/Folders/Search; p.4 **US-02**: a local index and later incremental scans.
- `Music_Copilot_System_Architecture_MVP_v1_EN.pdf` p.16: **MediaStore → normalize/index → Room projection → Library UI**.
- `Music_Copilot_Code_and_Technical_Documentation_MVP_v1_EN.pdf` pp.34–35: derived local Room data, distinct media/metadata authority and privacy restrictions.
- `ADR-0003_Local_First_Data_Boundaries_Revised.pdf` p.3: MediaStore is existence/identity authority; Room index can rebuild; keep media titles/album/folder local and out of telemetry. **Accepted repository ADR and [#16 contracts](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md) outrank dated PDF status flags.**
- GitHub [P00 audit](ISSUE_6_PHASE0_READINESS_AUDIT.md), [P01 execution](ISSUE_6_PHASE1_EXECUTION_CONTRACT.md), [P02 data contract](ISSUE_6_PHASE2_DATA_CONTRACT.md), [P03 scanner](ISSUE_6_PHASE3_MEDIASTORE_IMPLEMENTATION.md), [UF-03](../user-flows/UF-03_LIBRARY_READINESS.md)/[UF-04](../user-flows/UF-04_BROWSE_AND_SEARCH.md), GS-01/02 and WF/HF-04/05 approved handoff.

At P04 entry: `LibraryRowMapper.normalize` trimmed/lowercased using ROOT and collapsed regex whitespace, and `fromSource` retained raw metadata. `RoomLibraryRepository.reconcile` deduplicated with input-order-dependent `distinctBy(mediaRef)`. P03 scanner rejected empty titles. **The index still uses existing Room V1** and is wholly derived from MediaStore.

## 2. Delivered, implementation-connected files

| Source | Actual P04 outcome |
|---|---|
| `app/src/main/java/com/latticeward/musiccopilot/data/api/LibraryTextNormalization.kt` | Pure, OS-independent shared display/search normalization: NFKC, `Locale.ROOT`, collapse Unicode spaces, scrub format/control characters, Android unknown sentinel, bounded metadata, safe folder validation and null/unknown distinction |
| `app/src/main/java/com/latticeward/musiccopilot/data/api/LibraryBrowseContracts.kt` | Reuses **exactly the same** normalization for Browse grouping; album's unknown metadata does not pretend to be a known platform-ID album; validated full relative folder path retains actual case and spacing so folders never accidentally merge |
| `app/src/main/java/com/latticeward/musiccopilot/data/mapping/LibraryRowMapper.kt` | `NormalizedLibraryMetadata` (including future P06 album/folder grouping/normalizedAlbum fields); `fromSource` maps safe V1 fields; `deriveIndexRows` verifies opaque refs, chooses a reproducible preferred row per **same ref**, sorts by normalized title+ref |
| `app/src/main/java/com/latticeward/musiccopilot/data/repository/RoomLibraryRepository.kt` | Existing reconciliation now calls `LibraryRowMapper.deriveIndexRows(scanned)` instead of input-order-dependent first duplicate; **retains existing transaction and Room V1**. Actual changed-row delta/P05 remains unfinished |
| `app/src/main/java/com/latticeward/musiccopilot/data/mediastore/MediaStoreScanner.kt` | Small P04 integration correction: valid `IS_MUSIC` source audio with a missing/invalid TITLE is emitted with `Unknown Title` rather than silently skipped; no non-music eligibility bypass |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue6Phase4MetadataNormalizationTest.kt` | **15 synthetic-only JVM test methods authored**, covering malformed/unknown metadata, Unicode/locale, duplicate behavior, stable keys, volume/folder disambiguation, duration, compatibility and privacy-safe output; **NOT_RUN** until P12 |
| `docs/implementation/ISSUE_6_PHASE4_VALIDATION.json` | Actual GitHub source readback and scoped static verification; never a fabricated executed test run |
| `docs/implementation/ISSUE_6_EVIDENCE_INDEX.json` | P04 stable change/evidence IDs and true unexecuted test status |

## 3. Normalization rules frozen for P05/P06

| Data | Rule / output | Why |
|---|---|---|
| Title | Trim/sanitize Unicode controls and spacing, use NFKC display, keep real text including punctuation/accents, **`Unknown Title` when absent or Android `<unknown>`** | Indexed supported audio remains discoverable even if title tag is broken |
| Artist | Known strings stay local, preserve display casing; missing/blank/`<unknown>` become `null`, not a literal `Unknown Artist` | Distinguish true missing from a user-authored value that happens to say Unknown Artist |
| Album | Same optional policy; derive `normalizedAlbum` for P06 search; native platform album-ID group only for **known** album, otherwise identity fallback | Avoid conflating missing vs literal `Unknown Album` |
| Folder | Require a true **media-relative path**, reject absolute, traversal, empty segment and control/formatted paths; preserve valid component case/spacing and complete path for distinct groups; display basename or `Unknown Folder` | Two directories sharing a visible basename or differing in case/spacing are not merged |
| Duration | `null`, zero, negative → `null`; positive `Long` preserved | No fictitious duration; malformed tags do not crash |
| Search and group keys | NFKC + ROOT lowercase + deterministic Unicode space/control rules; null key empty, display name not used as a database identifier | Locale-independent and privacy-safe. Search itself is indexed/bounded in P06, not claimed here |
| Metadata length | Over **2,048 UTF-16 chars** treated as unknown for optional fields (or fallback title), **not** logged | Bounded corrupt/provider metadata |
| Song identity | Stable scoped opaque `MediaRef`, not title or basename; reject malformed refs on reconciliation input | Distinct files with identical titles remain distinct |
| Duplicate identical ref | Prefer complete/valid metadata; tie-break in a deterministic order (normalized strings + raw values + duration), then stable sort by normalized title/ref | Shuffling provider rows cannot change winning derived record; no double index of one source ref |

`NormalizedLibraryMetadata` makes future derived fields explicit today but **does not persist them** into Room V1. `LibraryItem`, `LibraryIndexSnapshot`, `LibraryScanUseCase`, PlaybackGateway and #16 signatures remain unchanged. P06 must deliver a compatible DB V2 migration with populated, indexed album/folder fields and preserve any app-owned records.

**Important privacy caveat:** `LibraryGroupId` SHA-256 tokens and opaque MediaStore refs remain sensitive user-data-derived values; do not emit them in logs, exception messages, analytics, issue attachments, stacktraces or crash metadata. Test files use only synthetic audio titles and numeric references.

## 4. Contract edge cases / downstream handoff

- Title `<unknown>` maps to display fallback but the **existing MediaStore IS_MUSIC flag and MIME filter** still decide inclusion. An actual missing file still cannot become a valid song from Room; `get(ref)` retains OS re-query.
- Distinguishing `Music/Live  Session/` vs `Music/Live Session/`, `Music/Rock/` vs `Music/rock/` is required by P02's true full-path folder identity.
- Identically titled tracks with different scoped refs are retained separately; duplicate MediaStore rows with the **same ref** are collapsed deterministically. P05 will own per-item `insertedOrUpdated` accuracy, as current pre-P05 result still counts all rows after full-replacement scan.
- Scanner P03 may receive a corrupt tag or unreadable media. P04 only sanitizes metadata; unreadable/corrupt bytes and access failures are source/playback errors, **not** a fake metadata repair or physical-file write.
- Locale-independent index keys do not imply automatic removal of accents or tokenized/fuzzy search; P06 owns that measurable query decision and P11/P12 own performance validation.
- A provider query that returns a corrupt Cursor/schema or an offline volume remains a **typed recoverable error** via repository; P04 doesn't change those guarantees.

## 5. Real evidence, limits and PASS gate

**P04 source gate (GitHub readback):**
- [x] File attachment opened and authoritative English sources/actual pages checked.
- [x] Existing source compatibility and all new files read back from Issue #6 feature branch.
- [x] Shared display/search/group normalization; fallback/missing/null handling; deterministic ref/tie-break behavior in **committed Kotlin**.
- [x] Repository uses new deterministic derivation and retains Room V1 (no half migration).
- [x] 15 synthetic JVM test functions authored, but **never represented as executed**.
- [x] No cloud library/search backend, no physical media manipulation, no new analytics, no queue or actual playback ownership.
- [x] No phase-by-phase APK, no owner physical-device tests, no PR, no merge, no closing Issue #6.

**Not yet verified:** Actual Gradle/Kotlin Android compilation, P04 JVM test execution, Android MediaStore/Room integration, Room V1→V2 migration/DAO queries, P05 incremental/cancellable scan, physical 5,000-file performance and accessibility/API/OEM matrices. **All remain NOT_RUN or NOT_IMPLEMENTED as applicable.** P12 alone builds **one consolidated APK from all P00–P11 changes** for owner tests M01–M34 and runs/records technical suites T01–T10. Issue #73 deferred Issue #5 QA is a separate open risk; #7 owns full Media3 playback.

**Conclusion:** `P04_PASS_SCOPED_SOURCE / P05_READY`. This is code-and-static-contract completion **only**, not an Issue #6 AC PASS; all 12 original issue acceptance checkboxes remain unchecked and Issue stays OPEN.
