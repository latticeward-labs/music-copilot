# Issue #11 — P05 Confidence Policy and Deterministic Direct / Confirm / Reject Admission

**Date:** 2026-11-14  
**Repository:** \`latticeward-labs/music-copilot\`  
**Branch:** \`feature/11-play-artist-one-step-ambiguity\`  
**P05 result:** \`SOURCE_IMPLEMENTED_UNWIRED_UNBUILT\` — **no Gradle, Kotlin compiler, Lint, JUnit, Android instrumentation, CI/Actions, emulator, phone run or APK**.

## 1. Source authority and evidence

**Owner upload:** \`MSM2(1)(1).rar\`, SHA-256 \`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8\`. The exact attachment was reopened; its 12 English PDF documents are available as extracted source text. The relevant materials include:

- \`ADR-0005_Restricted_Deterministic_Intent_Mapping.pdf\` **§§3–11**: supported intent and valid slot + confidence threshold before direct; missing/low confidence must not trigger guessed execution; malformed threshold falls back to safe default; provider and artist-match scores remain separate; one short confirmation for bounded plausible artist choices; no match/unsupported → safe no-action.
- \`ADR-0004_Replaceable_Voice_Provider.pdf\` **§§3–7**: speech-to-text engine returns transcript and confidence **when available**, not executable commands; provider can change; manual playback remains available; no raw audio.
- \`Music_Copilot_Code_and_Technical_Documentation_MVP_v1_EN.pdf\` **Voice & Intent pp.30–32**: supported/threshold-valid → execute, bounded/low-confidence → one confirmation, unknown/unsupported → no action; typed \`Recognized/Ambiguous/Unknown/Unsupported\`.
- \`Music_Copilot_MVP_v1_EN.pdf\`, \`Music_Copilot_PRD_v1_EN_Clarified.pdf\`: one-step ambiguity, short feedback and no wrong direct execution while driving.

**Current GitHub controlling authority:** [Accepted ADR-0005](../adr/0005-deterministic-intent-mapping.md), [Issue #11](https://github.com/latticeward-labs/music-copilot/issues/11), \`VoicePolicy.kt\`, \`SpeechToTextEngine.kt\`, \`DirectControlPhrasePolicy.kt\`, \`MoonshineSileroSpeechToTextEngine.kt\`, \`VoiceBindings.kt\`, [P01 state matrix](ISSUE_11_P01_PRODUCT_CONTRACT_AND_STATE_MATRIX.md), [P02 grammar](ISSUE_11_P02_PLAY_ARTIST_GRAMMAR_AND_SLOT_AUDIT.md), [P03 artist query](ISSUE_11_P03_ARTIST_INDEX_LOOKUP_AND_REVISION_AUDIT.md), [P04 resolver](ISSUE_11_P04_ARTIST_RESOLVER_SCORING_AND_TIEBREAK_AUDIT.md).

**Source hierarchy:** the English PRD/MVP are product authority, accepted GitHub ADRs are architectural authority, PDF technical blueprint is an engineering input; P05 safety choices below are **versioned implementations**, not a claim that the historical PDF prescribes their exact Kotlin class names.

## 2. Delivered source changes

| Path | What was added | Runtime effect at P05 |
|---|---|---|
| [\`ArtistConfidencePolicy.kt\`](../../app/src/main/java/com/latticeward/musiccopilot/voice/intent/ArtistConfidencePolicy.kt) | Pure \`Missing\`/\`Invalid\`/\`Calibrated\` classification; finite probability check; validation for bounded direct threshold; only calibrated provider confidence can permit direct | **Not connected to Voice execution** |
| [\`ArtistDecisionPolicy.kt\`](../../app/src/main/java/com/latticeward/musiccopilot/voice/intent/ArtistDecisionPolicy.kt) | Pure, typed mapping of P02 phrase result + P04 \`AppResult<ArtistResolution>\` + genuine nullable provider confidence → \`AppResult<PlayArtistDecision>\` | **No Media3 / no Coordinator state mutation** |
| [\`app/build.gradle.kts\`](../../app/build.gradle.kts) | Additional compile-time \`VOICE_ARTIST_DIRECT_CONFIDENCE_THRESHOLD=0.90\`, independent of existing #10 five-control field | **Not built** |
| [\`VoiceBindings.kt\`](../../app/src/main/java/com/latticeward/musiccopilot/app/di/VoiceBindings.kt) | Additive \`artistDecisionPolicy()\` factory reads artist BuildConfig value | **Factory not consumed by active coordinator until P07/P12** |
| [\`Issue11Phase5ArtistConfidencePolicyTest.kt\`](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue11Phase5ArtistConfidencePolicyTest.kt) | 10 source-authored JUnit fixtures | **NOT_RUN** |
| [\`Issue11Phase5ArtistDecisionPolicyTest.kt\`](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue11Phase5ArtistDecisionPolicyTest.kt) | 30 source-authored JUnit fixtures | **NOT_RUN** |

Versions: \`ArtistConfidencePolicy.POLICY_VERSION = issue11-artist-provider-confidence-v1\`; \`ArtistDecisionPolicy.POLICY_VERSION = issue11-play-artist-decision-v1\`. The new classes are **pure Kotlin**, no Android/Media3/Room API imports.

The existing #10 \`RestrictedVoiceIntentMapper.map()\`, \`DirectControlPhrasePolicy\`, \`VOICE_DIRECT_CONFIDENCE_THRESHOLD\`, STT engine, \`DefaultVoiceSessionCoordinator\`, Media3 service and playback dispatch are **unchanged**. No new supported commands, wake listening, remote config, backend, cloud speech, analytics events or UI fields.

## 3. BuildConfig/direct-confidence contract

| Policy | Value | Origin | Adjustment allowed in P05 |
|---|---:|---|---|
| Artist direct threshold | **0.90** | New \`BuildConfig.VOICE_ARTIST_DIRECT_CONFIDENCE_THRESHOLD\` | Bounded compile-time setting only |
| Minimum configured direct threshold | **0.80** | Existing \`VoicePolicy.T_DIRECT_MIN_ALLOWED\` | Hard safety bound; cannot be loosened by config |
| Maximum configured direct threshold | **0.98** | Existing \`VoicePolicy.T_DIRECT_MAX_ALLOWED\` | Hard safety bound; cannot be loosened by config |
| Malformed/out-of-range/NaN/Infinity configured threshold | **0.90 fallback** | Existing \`VoicePolicy.T_DIRECT\` | Deterministic fail-safe default; never uses 0 or raw invalid value |
| P04 clear artist similarity | 0.88 | Existing \`VoicePolicy.ARTIST_CLEAR_SIMILARITY\` | **Fixed P04 scorer parameter, not a P05 provider confidence score** |
| P04 plausible artist similarity | 0.72 | Existing \`VoicePolicy.ARTIST_PLAUSIBLE_SIMILARITY\` | As above |
| P04 top-two separation | 0.08 | Existing \`VoicePolicy.MIN_TOP_TWO_SEPARATION\` | As above |

The artist setting is deliberately **separate** from the five exact transport commands: changing it cannot change #10 behavior. No separate "artist similarity equals STT confidence" calculation is permitted. \`BuildConfig\` is a compile-time knob only; no Remote Config, client network fetch, server or telemetry endpoint was introduced.

### Real STT confidence classification

- \`providerConfidence == null\` → \`ArtistProviderConfidence.Missing\`.
- Finite \`0.0..1.0\` → \`ArtistProviderConfidence.Calibrated(probability)\`, when supplied by the provider contract. A numeric score must actually be **provider-supplied**: P05 never synthesizes it from local VAD, fuzzy match, transcript, artist history or a manually inserted override.
- \`NaN\`, \`Infinity\`, negative, or greater than 1.0 → \`ArtistProviderConfidence.Invalid\`; **no direct and no speculative confirmation**.
- A real calibrated value **≥ the validated artist direct threshold** can authorize a **candidate Direct decision**, only when P04 has already returned a single clear/separated match with proper evidence.
- **Current Moonshine Tiny + Silero integration** in \`MoonshineSileroSpeechToTextEngine\` returns **\`providerConfidence=null\`**. Thus the normal current production provider cannot trigger *direct* PLAY_ARTIST through this policy at all. It can offer **one bounded explicit confirmation** if P04 produced a valid one/two-candidate choice. This preserves the user's ability to select a local artist without an uncalibrated auto-play path, pending future calibrated provider QA.
- The Issue #10 **local-VAD-verbatim-five-command exception** is not copied into artist policy. \`localVadEndpointVerified\` signals endpointing, not artist identity recognition reliability.

**Known limitation:** \`Calibrated\` means a probability declared through the \`SpeechRecognitionResult.providerConfidence\` field and in range; this source-level class does not itself independently validate third-party statistical calibration. A future provider claiming that field needs separate calibration/field evidence; P13 must not interpret a synthetic high score as real-device validation.

## 4. Deterministic decision matrix

P05 admission receives **(phrase classification, provider evidence, completed P04 artist result)**. Each decision is **inert**; only P06 may dispatch after live MediaStore checks and P07 token/session ownership.

| Case | P02 / P04 input | STT provider confidence | P05 outcome | Why |
|---|---|---|---|---|
| 01 | Approved phrase + \`Clear(Exact)\` | valid ≥ threshold | \`Direct(candidate,revision)\` | Valid one artist + genuine high-confidence provider evidence |
| 02 | Approved phrase + \`Clear(Fuzzy≥0.88)\` | valid ≥ threshold | \`Direct\` | P04 guarantees clear/separated candidate; P05 rechecks scorer version/clear floor |
| 03 | Approved phrase + \`Clear\` | valid 0.0…below threshold | \`Ambiguous([one],LOW_PROVIDER_CONFIDENCE)\` | Never auto-play low confidence; one explicit selection |
| 04 | Approved phrase + \`Clear\` | **null** (Moonshine today) | \`Ambiguous([one],MISSING_PROVIDER_CONFIDENCE)\` | No invented confidence, one explicit selection |
| 05 | Approved phrase + \`NeedsConfirmation(1–2)\` | valid ≥ threshold | \`Ambiguous(1–2,CLOSE_ARTIST_MATCHES)\` | A high provider score cannot override artist ambiguity |
| 06 | Approved phrase + \`NeedsConfirmation(1–2)\` | valid below threshold | \`Ambiguous(1–2,LOW_PROVIDER_CONFIDENCE)\` | Low provider score and bounded plausible candidates |
| 07 | Approved phrase + \`NeedsConfirmation(1–2)\` | **null** | \`Ambiguous(1–2,MISSING_PROVIDER_CONFIDENCE)\` | One-step confirmation allowed, no auto-execution |
| 08 | Approved phrase + any artist result | **NaN / ∞ / outside [0,1]** | \`Unknown\` | Invalid provider data is **not** simply low confidence |
| 09 | Approved phrase + \`NotFound\`, complete/valid index | valid or null | \`NotFound\` | No local candidate; no queue mutation |
| 10 | Approved phrase + \`TooManyPlausible\` | any valid/null | \`Unknown\` (manual fallback) | Cannot reduce 3+ ambiguous options to an unsafe two-option list |
| 11 | Approved phrase + malformed P04 Clear fuzzy/unknown algorithm version | any valid/null | \`Unknown\` | Inconsistent ranking evidence must not become Direct/confirmation |
| 12 | Approved phrase + malformed P04 confirmation candidates (duplicates or sub-plausible) | valid/null | \`Unknown\` | No duplicate/made-up choices |
| 13 | Approved phrase + P03/P04 \`AppResult.Failure\` | any | **Same typed Failure** | Permission, stale, incomplete scan remain errors, not fake NotFound |
| 14 | Approved phrase + absent/null P04 result | any | \`LIBRARY_BROWSE_UNAVAILABLE\` Failure | Missing lookup is not proof of no artist |
| 15 | Unapproved phrase, malformed slot or explicitly non-English | any | \`Unknown\` | Preserve manual controls, no nearest intent |
| 16 | Understood remote/compound unsupported request | any | \`Unsupported\` | Do not reinterpret as local Play |
| 17 | Negative P04 revision | any | \`Unknown\` | No trusted snapshot to authorize a selection |

\`PlayArtistDecision.Direct\` is **not** \`PlayArtistDecision.Executed\`; no ACK or queue mutation occurs in P05. \`PlayArtistDecision.Ambiguous\` holds a defensively copied list of **at most two** distinct \`LibraryGroupId\` values and their original revision; it **does not create a \`VoiceCandidateToken\`, open a dialog or call STT again**. P07 must own a single-use token and exactly one response. Neither, cancel, stale, unknown, invalid and not-found must preserve current playback.

## 5. Separation from P04 similarity and #10 direct controls

P04 \`ArtistResolution.Clear\` means only that one local artist is ranked clearly. P05 has a separate provider gate; an exact similarity of 1.0 does **not** imply the STT recognized the requested artist correctly. The decision policy independently verifies plausible/clear fuzzy evidence and the registered \`ArtistMatchPolicy.SCORER_VERSION\`, so a fabricated/mismatched P04 Clear cannot authorize playback.

The existing #10 \`RestrictedVoiceIntentMapper.map()\` is untouched. That mapper preserves its **five exact control intents**, 19 allowlisted phrases, configured \`VOICE_DIRECT_CONFIDENCE_THRESHOLD=0.90\` and the narrowly constrained \`localVadEndpointVerified\` exception. It still rejects \`PLAY_ARTIST\` until later explicit wiring. P05's \`VoiceBindings.artistDecisionPolicy()\` is a **factory only**, unused by the current coordinator; P07/P12 will add full approved lifecycle integration.

## 6. Privacy and evidence boundaries

Private material includes the user's spoken transcript, audio, local MediaStore filenames/titles, artist display label, group ID, query key, URI/MediaRef, confirmation token and local history. None may enter analytics, crash strings, GitHub QA artifacts, network or error-code values. P05 uses only fixed enum reasons, bounded numeric policy values and redacted diagnostics inherited from P01/P04. No new telemetry schema is introduced; P11 will map to allowlisted coarse intent/confidence/latency/outcome buckets.

**No success is claimed:** the policy hasn't been compiled or run. All current/previous P02–P04 test fixtures also remain **NOT_RUN**. Existing issue-quality debt is retained: [#80](https://github.com/latticeward-labs/music-copilot/issues/80) **OPEN**, prior #10 owner-reported microphone lab **24/50 (48%)** vs 90% gate. The new Artist API cannot establish better STT reliability.

## 7. QA inventory / AC mapping

[Machine-readable P05 evidence register](ISSUE_11_P05_SOURCE_EVIDENCE_AND_QA_REGISTER.json) contains the **40 authored JUnit test methods**, all \`NOT_RUN\`:

- **10 confidence fixtures:** null vs invalid, valid bounds, threshold equality, safe fallback, provider-only evidence, no VAD/match substitution.
- **30 decision fixtures:** Direct vs Ambiguous vs Unknown vs Unsupported vs NotFound, null Moonshine, invalid values, near-ties, 2 candidates, more than 2, malformed scorer, typed permission/stale propagation, disallowed phrases, privacy and #10 regression.

Source work materially advances AC04 (Direct admission), AC05 (one bounded confirmation decision), AC09 (BuildConfig and bound), AC10 (privacy-safe values) and AC11 (authored cases). It does **not** verify any original Issue #11 acceptance criterion; implementation ≠ compile ≠ physical QA. The owner acceptance checklist remains **0 verified PASS**, technical T01–T10 **NOT_RUN**, phone M01–M48 **NOT_RUN**, 6×10 L01–L06 **NOT_RUN**.

## 8. Handoff to P06, P07 and beyond

**P06:** accept only a P05-authorized \`Direct\` or an authorized P07 selection; pin \`expectedLibraryRevision\`, re-query MediaStore for every selected track with \`LibraryRepository.get(ref)\`, build the stable bounded queue and dispatch **one** service-owned \`PlaybackGateway\` command; don't claim success before Media3 ACK. **P07:** own one app-scope \`VoiceSessionCoordinator\` generation and single-use \`VoiceCandidateToken\`, bounded option selection, \`Neither/Cancel/Stale\` without replay, no second clarification. **P08–P12:** UI/short TTS/safety/privacy/complete wiring, but no tests or app build yet. **P13 ONLY:** one integrated APK + owner phone tests and authorized technical checks; record real outcomes, and only then decide PR/merge/Issue close.

**P05 exit:** \`SOURCE_IMPLEMENTED_UNWIRED_UNBUILT\`. Ready to implement **P06** with an explicit confidence admission boundary and no change to five existing transport controls.
