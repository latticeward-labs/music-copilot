# Analytics and Privacy - MVP v1.0

> Derived only from [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md).

## Principles

Collect only what is required to learn whether Driving Mode is used, Voice works and users return. Raw voice is not stored or sent by default. Microphone permission is explained. Users can clear history and diagnostic data. If cloud STT is used, separate consent and transparent processing/retention disclosure are required.

## Event allowlist

| Event | Allowed properties |
|---|---|
| driving_mode_started | entry_source, bt_connected |
| voice_button_pressed | session_id, playback_state |
| intent_detected | intent_type, confidence_bucket |
| command_executed | intent_type, latency_bucket |
| manual_correction | previous_intent, correction_type |
| player_error | error_code, route_type |
| session_completed | duration_bucket, commands_count |

The PRD also requires `intent_failed`. Implement it with typed, non-personal failure data only.

## Prohibited

Raw audio, full personal-file titles, precise location, contacts and unnecessary identifiers.

## Dashboard

The weekly dashboard answers three questions: Is it used? Does it work correctly? Does the user return?
