# Issue #11 — P02: Deterministic PLAY_ARTIST Grammar & Slot Extraction
**Date:** 2026-11-14  
**Phase:** P02 — source implementation and authored-only regression scenarios  
**Status:** \`SOURCE_IMPLEMENTED_UNBUILT\` / \`TEST_SOURCE_AUTHORED_NOT_RUN\`  
**Branch:** \`feature/11-play-artist-one-step-ambiguity\`  
**Scope:** NO direct Playback dispatch, Room artist lookup, fuzzy score, confidence acceptance, confirmation, Media3 command, Activity/UI modification or APK. No tests, compilation, lint, CI or emulator/device execution.

## 1. Source and authority

- Owner-provided English \`MSM2(1)(1).rar\`: SHA-256 \`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8\` — same approved product-source package recorded in [P00](ISSUE_11_P00_READINESS_AND_GAPS.md).
- \`ADR-0005_Restricted_Deterministic_Intent_Mapping.pdf\` sections 3–11: six and only six English intents; deterministic normalization before fixed-intent classification; required artist slot; no NLU/synonym guessing; no unsupported actions; provider confidence separate from artist match; one confirmation only after matching.
- \`ADR-0004_Replaceable_Voice_Provider.pdf\` and current [Voice Contract](../VOICE_CONTRACT.md): tap-to-talk, replaceable STT/TTS, no wake-word/no continuous listening.
- \`Music_Copilot_MVP_v1_EN.pdf\` voice-command table and \`Music_Copilot_PRD_v1_EN_Clarified.pdf\` example specify the **only confirmed artist-command grammar** as \`Play songs by [artist]\`. Existing [Voice Contract](../VOICE_CONTRACT.md) gives \`Play songs by Adele\` as example; it does not approve \`play music by\`, \`play artist\`, or other equivalent phrases. **No new synonym was added**.
- GitHub [#11](https://github.com/latticeward-labs/music-copilot/issues/11), [P01 contract and state/result matrix](ISSUE_11_P01_PRODUCT_CONTRACT_AND_STATE_MATRIX.md), [Issue #16 architecture](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md), [privacy policy](../ANALYTICS_AND_PRIVACY.md).

**Version/source of truth:** \`PlayArtistPhrasePolicy.GRAMMAR_VERSION = "issue11-play-artist-en-v1"\`. The historical \`DirectControlPhrasePolicy.GRAMMAR_VERSION = "issue10-five-controls-v1"\` and its **19 allowlisted phrases** remain unchanged.

## 2. Delivered source changes

| File | Type | What changed | Runtime effect |
|---|---|---|---|
| [\`PlayArtistPhrasePolicy.kt\`](../../app/src/main/java/com/latticeward/musiccopilot/voice/intent/PlayArtistPhrasePolicy.kt) | NEW pure Kotlin | Versioned fixed artist grammar, bounded slot extraction and typed rejection | New **callable** pure parser, NOT yet invoked by production flow |
| [\`RestrictedVoiceIntentMapper.kt\`](../../app/src/main/java/com/latticeward/musiccopilot/voice/intent/RestrictedVoiceIntentMapper.kt) | Additive only | New \`classifyPlayArtist(SpeechRecognitionResult)\` delegates to pure classifier; existing \`map()\` is **unchanged** | All five Issue #10 commands keep their exact behavior; artist still not executable |
| [\`Issue11Phase2PlayArtistGrammarTest.kt\`](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue11Phase2PlayArtistGrammarTest.kt) | NEW JUnit source | 18 proposed regression test methods, source-authored | **18 AUTHORED, 0 RUN** |

No other production code has been intentionally modified in P02. No changes to \`DirectControlPhrasePolicy.kt\`, \`VoicePlaybackDispatcher.kt\`, \`DefaultVoiceSessionCoordinator.kt\`, \`SpeechToTextEngine.kt\`, \`MoonshineSileroSpeechToTextEngine.kt\`, \`PlaybackCommand.kt\`, Room schema, media service, UI or BuildConfig.

## 3. Grammar contract: exact prefix, no synonyms

| Input class | Example (non-personal fixtures) | Typed result | Notes |
|---|---|---|---|
| Canonical approved phrase | \`Play songs by Adele\` | \`Recognized(PlayArtistSlot)\` | Private \`displayLabel\` and \`normalizedKey\`; only recognizes grammar, **not artist identity** |
| Case/whitespace | \`  PLAY   SONGS BY  The Weeknd  \` | \`Recognized\` | Display retains case; normalized lookup uses \`Locale.ROOT\` via \`LibraryTextNormalization\` |
| Optional after-tap spoken prefix | \`musico play songs by Adele\` | \`Recognized\` | Existing explicitly-tapped PTT contract, never wake-listening |
| Unapproved equivalent | \`play music by Adele\` | \`NotArtistPhrase\` | No inferred synonyms; future vocabulary changes need owner/contract review |
| Blank/invalid artist | \`play songs by\` | \`Malformed(EMPTY_ARTIST_SLOT)\` | No half-built executable command |
| Input too long | longer than 120 UTF-16 units | \`Malformed(INPUT_TOO_LONG)\` | Matches current STT upper bound |
| Artist slot too long | longer than 80 UTF-16 units | \`Malformed(ARTIST_SLOT_TOO_LONG)\` | Local v1 safety bound, not a fuzzy-scoring decision |
| Unsafe invisible or control characters | NUL/bidi formatting in input | \`Malformed(UNSAFE_CHARACTER)\` | Fail closed; no personal input in error codes |
| Question/semicolon/multi-action input | \`play songs by Adele?\` / \`...; pause\` | \`Malformed(QUESTION_OR_COMPOUND)\` | No question interpretation |
| Explicit unsupported extra capability | \`play songs by Adele on Spotify\` | \`Unsupported\` | No remote catalog/streaming |
| Explicit chained transport request | \`play songs by Adele then pause\` | \`Unsupported\` | No double intent / auto-dispatch |
| Non-English detected language | provider reports \`fa-IR\` | \`NonEnglish\` | Null provider language is **unknown**, not fabricated English evidence |
| Other text, control command, wake attempt | \`pause\`, \`hey musico ...\` | \`NotArtistPhrase\` | The five control phrases still belong solely to the old mapper path |

### Normalization order (version 1)

1. Reject blank, >120-character and unsafe format/control input **before** processing.
2. If provider explicitly detected a non-English language, reject; language \`null\` is not proof of English.
3. Apply Unicode NFKC; canonicalize curly/common apostrophe/double-quote variants.
4. Collapse repeated whitespace and trim both ends using a fixed locale-independent pipeline.
5. Remove one balanced outer double-quote pair if present; remove **one** optional exact \`musico \` prefix after PTT.
6. Extract only after the exact case-insensitive prefix \`play songs by \`. No partial prefix or invented synonyms.
7. Reject \`?\`/semicolon, obvious chained transport commands, explicit on/from streaming-platform suffixes, empty/oversized/non-text slot.
8. Remove non-semantic end punctuation conservatively, **preserving internal name punctuation and initialisms such as \`R.E.M.\`**, then use existing \`LibraryTextNormalization.display()/key()\` to derive transient label and locale-stable key.
9. Emit only \`Recognized\`, \`NotArtistPhrase\`, \`Malformed(reason)\`, \`Unsupported\`, or \`NonEnglish\`. No database query/score/threshold/execution in this phase.

**Deliberate constraint:** slot display and lookup key are *syntactic* outputs. They are not \`LibraryGroupId\`, artist identity, a match score, a confidence bucket, or a successful playback result. P03/P04 must query *artist groups*, normalize the index consistently and handle punctuation in artist identities. Some legitimately punctuation-only artist names will safely reject pending an approved grammar update; this is a documented conservative trade-off.

## 4. Typed output + privacy

- \`PlayArtistPhraseResult.Recognized(slot)\`: \`slot.displayLabel\` is UI/private-memory-only; \`slot.normalizedKey\` is local artist-search-only. Their \`toString()\` redacts values, and callers MUST NOT log individual fields.
- \`PlayArtistPhraseResult.NotArtistPhrase\`: different command/unsupported synonym; does not guess an intent.
- \`Malformed(reason)\`: reason is a fixed enum, with no raw transcript/user name embedded.
- \`Unsupported\`: recognized out-of-scope remote/compound instruction; **no fallback to a supported transport command**.
- \`NonEnglish\`: explicit provider-detected non-English. Does not treat missing detection as calibrated language confidence.
- The parser reads \`detectedLanguageTag\` but does not alter \`providerConfidence\`, the \`localVadEndpointVerified\` flag, or numeric matching thresholds; provider confidence policy remains **P05**.
- No audio or transcript persistence, telemetry/analytics event, UI candidate list, network/backend/STT model change or Media3 command is added.

## 5. Five-control preservation / integration gating

\`RestrictedVoiceIntentMapper.map()\` remains **byte-for-byte functionally unchanged**: it still uses the exact 19-phrase \`DirectControlPhrasePolicy\`, Issue #10 language guard, direct confidence, and *only* the prior narrow local-VAD exception. Its five transports have no fuzzy matching. The new \`classifyPlayArtist()\` is an **additive source-only entry point** and does not dispatch: an accepted artist utterance still returns \`Rejected\` from the old runtime \`map()\` until P05/P07/P12 wiring. That prevents premature playback while the artist index, confidence and one-step confirmation are incomplete.

Likewise \`VoicePlaybackDispatcher.commandFor(PLAY_ARTIST)\` still returns \`null\`; \`DefaultVoiceSessionCoordinator.confirm()\` still fails stale; existing Compose overlay still reports \`ConfirmationUnavailable\`. These are **scheduled** P03–P12 dependencies, not P02 defects to hide.

## 6. Authored JUnit scenarios — all NOT_RUN

\`Issue11Phase2PlayArtistGrammarTest.kt\` contains **18** source-authored JUnit \`@Test\` methods, for:
- exact approved prefix and grammar version;
- case/whitespace/case-preserving slot and locale-independent key;
- existing post-tap \`musico\` prefix exactly once;
- Unicode quotes/apostrophes, terminal punctuation, internal punctuation/initialisms;
- empty/malformed slot and unapproved equivalent/wake/transport phrases;
- language classification, deterministic repeated inputs;
- transcript-length, slot-length and unsafe-control/format bounds;
- question/compound and explicit streaming/transport suffix rejection;
- punctuation-only slot refusal and privacy-redacted diagnostic \`toString\`;
- no change to the five previous \`map()\` intents;
- no use of \`localVadEndpointVerified\` as artist confidence or new artist execution authority.

**Evidence vocabulary:** \`SOURCE_AUTHORED\` ≠ \`COMPILED\` ≠ \`EXECUTED\` ≠ \`PASS\`. All tests are **NOT_RUN** until P13. No Gradle task, Actions workflow, emulator, Kotlin compiler, JVM or phone test was invoked.

## 7. AC mapping and handoff

| AC | P02 artifact status | Remaining task |
|---|---|---|
| AC01 supported PLAY_ARTIST phrase | **SOURCE_IMPLEMENTED_UNWIRED / NOT_VERIFIED** | P05/P07/P12 production wiring, P13 actual phone/lab evidence |
| AC02 normalization/fuzzy | **PARTIAL SOURCE ONLY / NOT_VERIFIED** | P03 artist index, P04 scored fuzzy artist resolution, P13 evidence |
| AC05 one-step ambiguity | **NOT_IMPLEMENTED / NOT_VERIFIED** | P05 policy, P07 owner state/token and P08 UI |
| AC08 unknown/unsupported fail safe | **GRAMMAR REJECTION SOURCE / NOT_VERIFIED** | P07–P12 output/feedback/queue invariants |
| AC09 threshold | **UNCHANGED / NOT_VERIFIED** | P05 provider vs similarity policy; no fake confidence |
| AC11 unit tests | **SOURCE_AUTHORED_NOT_RUN** | Execute with approval in P13; exact/fuzzy scoring tests belong to P04/P05/P12 |

All other Issue #11 AC retain initial \`NOT_VERIFIED\` / \`NOT_RUN\`; no Issue acceptance checkboxes should be marked complete. Existing [#80](https://github.com/latticeward-labs/music-copilot/issues/80) P0 reliability debt remains **OPEN**: #10 owner-reported five-command lab **24/50 = 48%** versus **45/50 = 90%**. This Phase did not measure or fix STT accuracy.

### Next: P03
Create a **bounded, permission-aware, revision-aware artist-group lookup API** from the Room/MediaStore local index. Do not reuse title-based \`resolveVoiceCandidates()\` as artist identity. Preserve the separation of P02 ephemeral slot, P03 real artist lookup, P04 fuzzy score, P05 provider confidence, P06 Media3 queue/ACK and P07 one-shot confirmation.

**P02 result:** source files committed to existing Issue #11 feature branch, code paths source-reviewed only; **no build/test/APK/QA, no PR/merge/closure, no claimed product success.**
