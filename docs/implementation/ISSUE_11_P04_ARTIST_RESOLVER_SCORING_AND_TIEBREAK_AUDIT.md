# Issue #11 — P04: Artist Resolver, Versioned Fuzzy Scoring and Stable Tie-Breaking

**Date:** 2026-11-14  
**Repo / branch:** \`latticeward-labs/music-copilot\` / \`feature/11-play-artist-one-step-ambiguity\`  
**Phase status:** \`SOURCE_IMPLEMENTED_UNWIRED_UNBUILT\` — no compiler, test, Gradle, lint, Actions, emulator, device or APK execution.  
**Scope:** artist-name normalization/scoring, complete revision-consistent paged candidate resolution, stable ranking, conservative ambiguity/not-found outcomes; no confirmation, STT confidence, Media3 mutation, UI or telemetry wiring.

## 1. References actually inspected

The owner-attached \`MSM2(1)(1).rar\` was opened from the uploaded archive and its 12 English PDFs extracted, archive SHA-256 **\`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8\`**. Directly reviewed:

- \`ADR-0005_Restricted_Deterministic_Intent_Mapping.pdf\` §§6–11, 15–17: ordered/versioned normalization; normalized exact artist match first; one fixed, versioned fuzzy scoring algorithm; score > secondary playback history > stable artist identifier; near-equal scores require confirmation; No Match must not execute; ambiguous confirmations only one question and one answer.
- \`Music_Copilot_Code_and_Technical_Documentation_MVP_v1_EN.pdf\` pages 31–32, **Voice & Intent / Artist resolution and ambiguity**: normalize → exact → fuzzy → stable sort → separation → safe outcome, with no SQLite row-order dependency.
- \`Music_Copilot_PRD_v1_EN_Clarified.pdf\` ambiguity/error rules: similar artists → short confirmation; no match preserves current playback.
- \`Music_Copilot_MVP_v1_EN.pdf\` voice quality rules and artist-name command matching.
- Current GitHub [accepted ADR-0005](../adr/0005-deterministic-intent-mapping.md), [Voice Contract](../VOICE_CONTRACT.md), [Issue #11](https://github.com/latticeward-labs/music-copilot/issues/11), [P01 model/state rules](ISSUE_11_P01_PRODUCT_CONTRACT_AND_STATE_MATRIX.md), [P02 grammar](ISSUE_11_P02_PLAY_ARTIST_GRAMMAR_AND_SLOT_AUDIT.md), and [P03 index API](ISSUE_11_P03_ARTIST_INDEX_LOOKUP_AND_REVISION_AUDIT.md).

**Provenance:** the attached PDF specifies **ordering/decision principles**, but no calibrated edit-distance formula. The concrete algorithm described below is a newly **versioned Issue #11 P04 engineering implementation**, not a claim that the PDF prescribed normalized Levenshtein or that performance has already been tested. Threshold numbers are taken from existing approved GitHub \`VoicePolicy\`/Issue #16, not invented as PDF data.

## 2. Source delivered

| Source | Implementation |
|---|---|
| [\`ArtistMatchPolicy.kt\`](../../app/src/main/java/com/latticeward/musiccopilot/voice/intent/ArtistMatchPolicy.kt) | Pure symmetric name normalization, Unicode code-point Levenshtein fuzzy similarity, bounded cost, stable sorted Top-3 accumulator, conservative outcomes and optional **non-authoritative** tie-break policy |
| [\`ArtistResolver.kt\`](../../app/src/main/java/com/latticeward/musiccopilot/voice/intent/ArtistResolver.kt) | Suspended artist-only P03 Data-port orchestration: ExactNormalized SQL probe, revision-pinned CandidatePage traversal, failure/permission/revision propagation, completeness gate and safe typed result |
| [\`Issue11Phase4ArtistMatchPolicyTest.kt\`](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue11Phase4ArtistMatchPolicyTest.kt) | 20 JUnit **source-authored** normalization/score/history/ambiguity/threshold assertions; all \`NOT_RUN\` |
| [\`Issue11Phase4ArtistResolverPortTest.kt\`](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue11Phase4ArtistResolverPortTest.kt) | 14 JUnit **source-authored** paged Data-port/consistency/stale/incomplete/permission and privacy assertions; all \`NOT_RUN\` |

No code changes to Media3, data room schema, playback control phrases, existing Issue #10 state mapper, existing P03 SQL, STT/Moonshine, Compose, or telemetry. Newly introduced scorer and resolver are **not yet wired into runtime**. Calling a pure scoring method never issues any playback command.

## 3. Versioned normalization and formula

- Normalization version: \`issue11-artist-key-nfkc-punctuation-v1\`.
- Fuzzy scorer version: \`issue11-artist-levenshtein-codepoint-v1\`.
- Resolver version: \`issue11-artist-resolver-v1\`.
- Apply **the exact same pipeline** to P02's \`PlayArtistSlot.normalizedKey\` and **each P03 artist display label**: existing \`LibraryTextNormalization.display()\` (NFKC, safe spaces/format handling), then deterministic curly apostrophe/dash/quotation mark folding, \`LibraryTextNormalization.key()\` (\`Locale.ROOT\` case fold and normalized whitespace). This bridges P02 apostrophe folding to P03's original \`normalizedArtist\` SQL key without changing the shared grouping identity.
- Preserve accents, \`&\`, \`/\`, periods and other semantically meaningful punctuation: e.g. an accented artist is not silently collapsed into an unaccented exact identity. This is conservative and minimizes wrong playback.
- **Exact comparison** uses normalized Unicode keys; if the P03 raw normalized keys differ only in normalized apostrophe variant, the candidate pagination still discovers **all** exact canonical equivalents. Multiple different group IDs that become identical under P04 normalization **must not auto-select** one.
- **Fuzzy score:** \`score = 1 - d(a,b) / max(codePointLength(a),codePointLength(b))\`, where \`d\` is Levenshtein edit distance over NFKC-normalized Unicode **code points** (not UTF-16 units). Score ranges 0…1, larger is better. Null/blank or overlong names are not scored and never become an invented artist.
- **Cost bounds:** max 80 code points per target/candidate (P02 artist slot length already ≤80 UTF-16 units); dynamic programming uses two arrays, ≤80×80 cell updates per scored candidate, and early-aborts when the edit budget cannot meet the approved plausible threshold. Candidate volume capped at 5,000 real artist groups, paginated in 100-row Room SQL reads, with only Top-3 candidate records and a bounded ID set retained in Voice. No full song-library hydration, unbounded filename scan, UI copy, network call or streaming dependency.
- **No score invention:** STT provider confidence remains **independent and nullable**; this fuzzy similarity is never passed as \`SpeechRecognitionResult.providerConfidence\` or a Voice direct-confidence threshold. P05 will decide direct vs confirmation only after evaluating genuine provider evidence.

### Existing numeric policy (from \`VoicePolicy.kt\`, not from PDF)

| Constant | Value | Use in P04 |
|---|---:|---|
| \`ARTIST_CLEAR_SIMILARITY\` | 0.88 | Clear fuzzy score may be chosen **as a candidate only**, never direct Playback |
| \`ARTIST_PLAUSIBLE_SIMILARITY\` | 0.72 | Minimum similarity to be a candidate |
| \`MIN_TOP_TWO_SEPARATION\` | 0.08 | Clear fuzzy score requires top vs runner-up margin ≥0.08 |
| \`T_DIRECT\` | 0.90 | **NOT applied by P04**; provider policy belongs P05 |
| allowed provider threshold configuration | 0.80…0.98 | **NOT changed by P04** |

Floating comparisons apply a documented 1e-12 **numeric epsilon** only at policy boundaries to prevent platform one-ULP ambiguity; this does not change configured thresholds.

## 4. Final deterministic ranking and product outcome

The P04 tuple is exactly:

\`(exactNormalized DESC, fuzzySimilarity DESC, APPROVED_historyPriority DESC, stableArtistGroupId ASC)\`.

History is used **only for identical similarity scores** as a secondary display/tie ranking rule; it cannot turn an ambiguous decision into a direct candidate and cannot outweigh exact or higher fuzzy similarity. \`ArtistHistoryPriority\` is an injectable pure port for a future *documented and privacy-approved local-only* source.

**Current executable default: \`NoArtistHistoryPriority\` returns 0 for all artist groups.** Repository inspection of \`PreferencesRepository\`, \`PreferencesDataStore\`, \`PlaybackSnapshot\` and Room schema showed no current approved per-artist playback-history ranking source; Issue #11/P04 does **not invent** history, persist new playback records, read private titles for personalization, or use current queue as pseudo-history. Any future history adapter needs an explicit local-data and privacy decision and must retain ambiguous score behavior.

| Case | P04 typed outcome | Safety / next phase |
|---|---|---|
| Exactly one canonical exact artist | \`ArtistResolution.Clear\` with \`ExactNormalized\` evidence | P05 must still check provider confidence; P06 must requery files + get service ACK |
| Two canonically exact but distinct artist IDs | \`NeedsConfirmation\` (two options), stable order | P07 one short question/one response |
| More than two canonically exact identities | \`TooManyPlausible\` | Fail/manual; never show a truncated “complete” choice |
| Fuzzy score ≥0.88 and next score absent or ≥0.08 lower | \`Clear\` with \`Fuzzy(score, algorithmVersion)\` evidence | **Candidate selection only**; not direct playback |
| One plausible fuzzy score 0.72…0.88 | \`NeedsConfirmation\` (one option) | Do not auto-Play |
| Two plausible fuzzy candidates without clear margin | \`NeedsConfirmation\` (two options), stable | Do not auto-Play; show only bounded options |
| More than two plausible and top not clearly separated | \`TooManyPlausible\` | Fail/manual rather than silently dropping the third relevant choice |
| No candidate ≥0.72 after a **complete, valid** index scan | \`NotFound\` (includes revision only) | Preserve playback; P09 short feedback |
| Too many groups (>5000), incomplete pages, malformed indexed metadata or non-progressing cursor | Typed \`LIBRARY_BROWSE_UNAVAILABLE\` failure | **Never** incorrectly report \`NotFound\` from a partial scan |
| Revision changed, stale page or permission revoked mid-read | Existing typed \`LIBRARY_BROWSE_STALE\` or \`PERMISSION_REVOKED\` | Never publish incomplete candidate/queue |
| Unsupported/noncanonical/oversized user slot | Existing typed \`VOICE_NO_MATCH\` | Manual fallback; no lookup, no execution |

**Important:** \`ArtistResolution.Clear\` is **not** \`PlayArtistDecision.Direct\`, \`Executed\`, or a Media3 ACK. The names were deliberately separated so an artist similarity result cannot bypass P05 confidence, P06 MediaStore/PlaybackGateway, or P07 one-shot confirmation.

## 5. Bounded query, revision, cancellation and snapshot integrity

1. \`ArtistResolver.resolve(slot)\` canonically normalizes the P02 slot and calls P03's SQL \`ExactNormalized\` lookup first, retaining its accepted index \`revision\`; any typed P03 error propagates without converting to no match.
2. It then traverses \`CandidatePage\` **with \`expectedRevision\` set on every request, including first page**, max 100 artist groups per query. This deliberate second pass detects multiple P04-canonical exact identities even where P03's raw SQL key was stricter.
3. Every page must have the **same revision/totalCount**, a strictly increasing contiguous offset (no missing or duplicate group IDs), and consistent \`displayLabel\` / P03 indexed key. SQL count and page are read in the same Room read transaction per P03; candidate order comes from SQL but final rank never depends on it.
4. If \`totalCount > 5000\`, if the returned total is incomplete, or if a page fails/mismatches, return a **typed failure**, never misleading \`NotFound\` or an unsupported partial match. No invented confidence/artist.
5. Coroutine cancellation is propagated, not swallowed. P03 checks OS permission, index state, revision and access epoch both before/after each Room operation. After the last page, P04 also checks its repository snapshot revision/status; **P06 must still requery live MediaStore before playback** because future file deletion/permission loss is possible.
6. Only candidate IDs, display labels and local scores exist **ephemerally** in memory. All return objects with private labels have redacted diagnostic \`toString()\`; no raw STT, artist titles, local file paths, refs or candidate tokens enter logs, GitHub test artifacts or analytics.

**Performance caveat:** The edit-distance cell cap, 5,000-group hard limit, SQL limits and Top-3 retention are **source design bounds**, not measured response time. P13 must execute validation and assess p95 voice latency. If future larger libraries exceed 5,000 artist groups, increasing/removing that cap requires reviewed performance/error policy; P04 currently fails safely rather than making a potentially wrong choice.

## 6. Test inventory — authored only (NOT_RUN)

- **20 JUnit pure scorer fixtures:** normalization variants, Unicode/apostrophes/case, semantic punctuation, exact/fuzzy, 0.88/0.72/0.08 boundaries, negative/long values, order independence, exact over history, history tie only, 1–2 choices, 3+ plausible fail-closed, no match, privacy redaction and repeatability.
- **14 JUnit repository port fixtures:** exact-first + pinned second pass, cross-ID canonical exact collisions, non-substring fuzzy match, multi-page complete scan, valid empty index vs no match, malformed index, >5000 artists, truncated/missing page, stale later page, revoked final-page snapshot, one/two/many plausible, invalid slot, private diagnostics.
- **34 authored, 0 compiled, 0 executed; all \`NOT_RUN\`** under the owner's P00–P12 restriction. These cases are not PASS evidence.
- Original 13 Issue #11 acceptance boxes remain unverified/unrun. P04 provides source work toward AC02/AC03/AC05/AC07/AC11; no claim of fulfillment without P05–P13 integration and owner tests. M01–M48, T01–T10, L01–L06 all \`NOT_RUN\`.

## 7. Outstanding risks and handoff

- **P05:** \`MoonshineSileroSpeechToTextEngine.providerConfidence\` is \`null\`. Missing confidence must **not** be replaced with artist similarity or \`localVadEndpointVerified\`; P05 must explicitly decide which artist options may enter one-step confirmation and whether any may execute with real calibrated provider confidence.
- **P06:** gather selected artist's complete validated ref pages with P03 revision, re-query each \`MediaRef\` through MediaStore, respect bounded queue and safe failure, dispatch a single \`PlaybackGateway\` service-owned command and await ACK.
- **P07–P10:** one generation, single-use confirmation token and snapshot, at most two buttons/one response; cancellation, focus, route, call, process death, stale revision, duplicate events must never execute twice.
- **P11/P12:** privacy-allowlisted telemetry; end-to-end wiring/QA fixture sources (authored only).
- **P13 ONLY:** one cumulative APK; owner performs phone scenarios; technical QA executed only as separately authorized. No merge or Issue closure until actual evidence.
- **Quality blocker:** [Issue #80](https://github.com/latticeward-labs/music-copilot/issues/80) OPEN; last owner-reported #10 voice lab 24/50=48% vs ≥90% gate. P04 does not fix or claim STT accuracy.

**Disposition:** \`P04_SOURCE_IMPLEMENTED_UNWIRED_UNBUILT\`. New pure resolver ready for P05 policy integration; tests and build deliberately NOT_RUN.
