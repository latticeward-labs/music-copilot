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
