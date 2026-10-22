# Issue #6 — P09 Search UI / SQL final QA scenarios (synthetic)

**Date:** 2026-10-22. **Delivery model:** one final integrated APK at **P12**, NOT one APK per phase. **All cases below: NOT_RUN**. No screenshots, personal search strings, real music titles or device paths are recorded here.

**Authority:** user-supplied MSM2 12-PDF source (SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`), approved `WF-05_SEARCH_RESULTS/NO_RESULTS/ERROR`, `HF-05_SEARCH_RESULTS`, matching frame images, UF-04 / GS-02, ADR-0003, P06 Room V2 and P08 four-category Library.

| ID | Synthetic stimulus | P12 device/UI/SQL assertion | Status |
|---|---|---|---|
| P09-S01 | Ready Library, first entry to Search | Search back appbar, text input, Clear disabled, Idle helper, Home/Search/Liked/Settings; no auto search/scan | NOT_RUN |
| P09-S02 | Type `aurora` slowly | Searching then Results; visible count; text retained | NOT_RUN |
| P09-S03 | Type `a`, `au`, `aurora` quickly | Only latest debounced request returned; no stale result flicker | NOT_RUN |
| P09-S04 | Synthetic artist `Aurora` | Artist-result row and Open; group drilldown goes to Artists members, not Playing | NOT_RUN |
| P09-S05 | Synthetic track `Aurora Lights` | Track row title/artist; preview explicitly **Not playing**, P10 handoff pending | NOT_RUN |
| P09-S06 | Synthetic album `Aurora Dreams` | Album-result row and Open; group drilldown shows member tracks | NOT_RUN |
| P09-S07 | Type nonmatching query | Verified **No local results**, not error, keep active query and editable field | NOT_RUN |
| P09-S08 | Clear nonmatching query | Return to Idle, no search for empty query, local Library unchanged | NOT_RUN |
| P09-S09 | Force recoverable Room search read error | Search failed, Retry + editable preserved query, NOT No Results | NOT_RUN |
| P09-S10 | Retry same query after transient error | Query re-executed, recovers with results or verified empty, without rescanning MediaStore | NOT_RUN |
| P09-S11 | Repository returns stale index revision | Reject returned rows, display Search failed/stale guidance, preserve query | NOT_RUN |
| P09-S12 | Rescan changes index during pending search | Cancel/ignore old response, re-query only against new accepted revision | NOT_RUN |
| P09-S13 | Revoke Android music access while searching | Hide all personal result rows, route permission recovery, no automatic scan | NOT_RUN |
| P09-S14 | Open Search from verified Empty Library | Typable query, verified No Results from Empty, never permission denial | NOT_RUN |
| P09-S15 | Rotate phone while search active and text present | ViewModel preserves query/route for active Search, no duplicate MediaStore scan | NOT_RUN |
| P09-S16 | Back from Search | Pop Search destination back to the existing Library route; dispose private query ViewModel instead of stacking another Library, without mutating playback | NOT_RUN |
| P09-S17 | 200% Android font scaling at 360dp width | SearchField, Clear, Retry and Back remain usable, rows reflow and scroll | NOT_RUN |
| P09-S18 | TalkBack full traversal | Search heading → query → clear → result count/results or error → bottom navigation; 48dp targets | NOT_RUN |
| P09-S19 | Search literal `%`, `_`, Unicode and mixed case | SQLite `instr` literal substring; NFKC/Locale.ROOT normalization; no wildcard injection | NOT_RUN |
| P09-S20 | 5,000 synthetic rows, text search and group matches | Measure local query latency/jank/memory, SQL `EXPLAIN QUERY PLAN`, at most 60 displayed results; no global Kotlin all-row materialization | NOT_RUN |
| P09-S21 | Return from Search to Library group then Back | Group opens only after revision-authorized handoff; Back returns to group list, never implicit playback | NOT_RUN |
| P09-S22 | Personal metadata or query entered on real phone | No query/results titles in diagnostic/telemetry/evidence uploads; screenshots use synthetic fixtures only | NOT_RUN |

| P09-S23 | Search → Settings → Back → Search → Back → Library | Returning from Settings reuses active Search and its query; exiting to Library pops Search and disposes query, without duplicate Library or stale Search destinations | NOT_RUN |

## Authored test sources (not executed)

- `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue6Phase9LocalSearchViewModelTest.kt` — 14 synthetic JVM tests.
- `app/src/androidTest/java/com/latticeward/musiccopilot/foundation/Issue6Phase9SearchComposeInstrumentedTest.kt` — 7 Compose UI tests.
- `app/src/androidTest/java/com/latticeward/musiccopilot/foundation/Issue6Phase9SearchSqlInstrumentedTest.kt` — 4 native SQLite Room tests.
- Existing `Issue6Phase8LibraryComposeInstrumentedTest.kt` updated so the Search tab must now be enabled with a verified grant.

### Strict acceptance conditions

- Do NOT call 14 JVM, 7 Compose or 4 SQLite tests PASS merely because their source exists; execute all at **P12**.
- No fabricated screenshot, no performance duration and no CI result. On first failed build, fix actual code, generate exact compiler Room schema and rerun before user APK.
- Do not claim 5,000-song performance until benchmark measured. `instr` may require an O(N) scan; limiting returned rows saves UI allocations but is not an indexed full-text engine.
- P06 *still* requires **genuine compiler-generated Room V2 `2.json` and native V1→V2 migration**; neither completed by P09.
- P09 does not implement service-owned Playback/MediaSession (#7/P10), Cloud search, fuzzy voice mapping (#11), account, streaming or P2 Likes. Search row **Select** is intentionally not **Play** until P10.
- Issue #6 original 12 ACs and all T01–T10 / M01–M34 remain unverified; Issue must stay OPEN until final gate.
