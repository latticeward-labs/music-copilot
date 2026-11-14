# Voice Contract - MVP v1.0

> Derived only from [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md).

Voice is push-to-talk. An explicit microphone tap starts Listening and ducks playback. TTS is at most one short sentence.

## State machine

`Idle -> Listening -> Processing -> Confirming -> Executed | Failed`

- Timeout: at most 6 seconds
- Retry: once
- Certain command: direct execution
- Low confidence or multiple matches: one short confirmation
- Failure/not found: preserve playback and return to manual control
- Log intent and result; do not store raw voice
- Confidence threshold: simple Remote Config or Build Config
- If cloud STT is used, obtain separate consent and disclose processor/destination and retention

## Supported intents

| Intent | Example | Action |
|---|---|---|
| PLAY | “Play”, “Resume” | Start/resume |
| PAUSE | “Pause”, “Stop music” | Pause |
| NEXT | “Next”, “Next song” | Next track |
| PREVIOUS | “Previous song” | Previous or beginning of current |
| SEEK_BACK | “Go back 10 seconds” | Seek -10 seconds, clamp at zero |
| PLAY_ARTIST | “Play songs by Adele” | Best local match |

Artist resolution uses normalization, fuzzy matching and playback-history priority. Multiple plausible matches cause one short confirmation; no result preserves current playback.

## Issue #10 implementation boundary — five direct PTT controls

The accepted MVP contract above retains **six** eventual intent types.
Issue #10 deliberately enables only `PLAY`, `PAUSE`, `NEXT`, `PREVIOUS`,
and `SEEK_BACK`. `PLAY_ARTIST` and its fuzzy match/confirmation are owned
by Issue #11 and **must not produce an executable Direct mapping in #10**.

- Microphone capture begins **only after an explicit tap** in Full Player or
  Driving. Bare approved phrases are valid after that tap. A spoken `musico`
  prefix is optional; this is not wake-word detection or background listening.
- Pure, versioned, exact English phrase bank: `DirectControlPhrasePolicy`
  (`issue10-five-controls-v1`). No LLM/fuzzy match, automatic synonym expansion,
  volume/streaming/artist or multi-command execution.
- Confidence threshold is local `BuildConfig.VOICE_DIRECT_CONFIDENCE_THRESHOLD`
  default **0.90**, validated to the approved bounds **0.80..0.98**. Invalid
  numbers/bounds fall back to 0.90; remote threshold services are not installed.
  A missing, invalid or below-threshold provider confidence is **not guessed**:
  no command is dispatched. No additional microphone activation is inferred.
- Unknown/unsupported text always returns `Rejected`; an exact phrase with
  insufficient confidence returns `RequiresConfirmation`, which the #10
  coordinator currently turns into a **safe failed/manual fallback**, rather
  than executing or showing a fictional confirmation UX. Artist confirmation
  belongs to #11.
- Actual playback correctness, service ACK, one-shot idempotency, 10-second
  seek clamping, audio duck, TTS UI and voice accuracy remain P06–P13
  obligations. The **50-attempt** voice lab must reach **45/50** before
  claiming the #10 quality gate; no tests or APK were executed in P05.
