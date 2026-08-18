# UF-09 - Ambiguity, Unsupported, and Artist Not Found

**Cluster:** D - Voice certainty and recovery  
**Priority:** P0  
**Updated:** 18 August 2026  
**Status:** **PASS / COMPLETE at User Flow contract level**  
**Tracking:** Issues #3, #11, #12, #16

## Ten-field traceability record

| Required field | Controlled value |
|---|---|
| Flow ID | UF-09 |
| User goal | Resolve one genuinely bounded ambiguity, or receive a truthful not-found result without an unintended command. |
| Priority | P0 |
| User Story | US-06: low confidence or multiple plausible local matches receives one short confirmation. |
| Functional Requirements | FR-06 direct-or-confirm certainty policy; deterministic local artist normalization/fuzzy resolution. |
| Screens and states | Voice Overlay Processing, Confirming, confirmed execution, Not Found/Unsupported, Failed-safe return. |
| Influencing ADRs | ADR-0004 provider separation; ADR-0005 exact six-intent mapper and one-step confirmation; ADR-0002 acknowledged playback. |
| Failure and recovery | Clear selection executes once; rejection/cancel/unclear reply ends safely; no result/unsupported preserves playback; manual controls remain available. |
| Data and permissions | Local artist corpus is read only for transient matching. No artist/title/transcript is telemetry or evidence. |
| Open implementation decisions | Final numeric fuzzy/confidence adoption and confirmation UI ownership remain #11/#16; the user-visible no-guess invariants are closed. |

## Deterministic resolution order

1. Normalize transcript and local artist labels using a fixed, locale-independent ordered pipeline.
2. Recognize only the fixed `PLAY_ARTIST` grammar and preserve the artist slot.
3. Prefer one exact normalized match.
4. Otherwise score with one versioned deterministic algorithm and fixed candidate policy.
5. Sort stably by exactness, score descending, history rank descending only as a secondary tie-breaker, then stable local artist ID ascending.
6. Direct-execute only one clear candidate above both provider and artist certainty boundaries.
7. If one or two candidates are plausible but not clearly separated, present at most those two in one short confirmation.
8. If no candidate is plausible, enter Not Found. Never execute the nearest item.

## One-step confirmation

- Confirming is an overlay state, not a new page or conversation.
- The prompt contains at most the presented candidate set and one question.
- The user may select only a presented candidate, reject/cancel, or fail to answer clearly.
- One clear selection submits exactly one service command, then uses the same acknowledged Executed contract as UF-08.
- Rejection, cancellation, timeout, or unclear response is terminal Failed-safe for this session. It does not ask a second question or expand candidates.
- Manual controls and immediate Driving Mode Exit remain available.

## Not Found, unknown, and unsupported

| Result | Required behavior |
|---|---|
| Missing artist slot | One short missing-artist response; playback unchanged; manual return. |
| No plausible local artist | One short Not Found response; playback unchanged; never approximate. |
| Unknown phrase | Do not map to a supported intent; concise failure and manual return. |
| Unsupported known request | Say it is unsupported; do not reinterpret as one of six intents. |
| Ambiguous candidate set larger than two | Present only the stable top two if the boundary permits; otherwise fail safely. Never show a long list while driving. |
| Candidate disappears before acknowledgement | Reject stale action, refresh actual library/playback snapshot, preserve playback, manual return. |

## Privacy, analytics, and accessibility

`intent_detected` may include only `intent_type` and a coarse `confidence_bucket`; `intent_failed` may include only a controlled coarse reason. Candidate names, artist IDs, rankings, similarity scores, history contents, transcript, titles, and library corpus are prohibited from telemetry and committed evidence.

The confirmation has stable TalkBack order, non-color-only focus/selection, large targets, short copy, and no keyboard or scrolling list. TTS remains one sentence maximum.

## Completion boundary

UF-09 closes ambiguity, missing artist, unknown, unsupported, and not-found behavior at User Flow level. The reference corpus verifies determinism and zero unsafe execution for its negative rows; field accuracy and final production constants remain downstream release evidence.
