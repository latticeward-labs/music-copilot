# Voice Contract — MVP v1

## Interaction

Voice is push-to-talk only. The microphone starts after an explicit tap. Playback ducks while listening and returns predictably after execution/failure. TTS is at most one short sentence.

## State machine

`Idle → Listening → Processing → Confirming → Executed | Failed → Idle`

- Listening timeout: maximum 6 seconds
- Retry: once only
- High confidence + single match: execute directly
- Low confidence or multiple matches: one short confirmation
- Failed/not found: preserve current playback and return to manual controls
- Raw voice and transcript: never persisted

## Supported English intents

| Intent | Example phrases | Deterministic action |
|---|---|---|
| `PLAY` | “Play”, “Resume” | Resume current queue; if empty, offer last playable item |
| `PAUSE` | “Pause”, “Stop music” | Pause |
| `NEXT` | “Next”, “Next song” | Advance; announce end of queue if needed |
| `PREVIOUS` | “Previous”, “Previous song” | Restart current or go previous using one fixed position threshold |
| `SEEK_BACK` | “Go back 10 seconds” | Seek current position minus 10 seconds, clamped at zero |
| `PLAY_ARTIST` | “Play songs by Adele” | Normalize/fuzzy-match local artists, then replace/start queue |

No extra intent is part of MVP v1.

## Artist resolution

1. Normalize case, spacing and diacritics.
2. Search local indexed artists only.
3. Rank exact/normalized match, fuzzy score, then recent playback.
4. Single high-confidence result: execute.
5. Multiple plausible results: ask one short choice question.
6. No result: “I couldn't find that in your library.”
7. Never show a long results list in Driving Mode.

Confidence threshold is configurable behind a typed configuration interface with a build-config fallback. Bucketed confidence may be telemetry; raw transcript and artist text may not.

## Example responses

- PLAY_ARTIST executed: “Playing Adele.”
- Ambiguous: “Did you mean A or B?”
- Unclear: “Please say that again.”
- Not found: “I couldn't find that in your library.”
- End of queue: “You're at the end of the queue.”

## Golden scenario

Vehicle connected → Driving Mode → microphone tap → “Next song” → intent detected → command executed in <2.5s P95 → next title/art updates → one short TTS response → no new screen.
