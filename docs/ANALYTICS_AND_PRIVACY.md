# Analytics and Privacy - MVP v1.0

> Derived only from [PRD v1.0](PRD.md), [MVP v1.0](MVP.md), and their approved User Flow/Golden Scenario privacy contracts.

## Principles

- Collect only what is required to learn whether Driving Mode is used, Voice works and users return.
- Microphone access is just-in-time, explained clearly and starts only after explicit user action.
- Raw voice is not stored or sent by default and never enters analytics, crash reports or repository evidence.
- Users can clear local history and diagnostic data through Settings.
- If cloud STT is used, separate consent and transparent provider/destination, purpose and retention disclosure are required before remote processing.
- Telemetry is non-authoritative: analytics failure must never delay permission recovery, playback acknowledgement, Voice cancellation, interruption handling or route safety.

## Event allowlist

| Event | Allowed properties |
|---|---|
| `driving_mode_started` | `entry_source`, `bt_connected` |
| `voice_button_pressed` | ephemeral/coarse `session_id`, `playback_state` |
| `intent_detected` | `intent_type`, `confidence_bucket` |
| `intent_failed` | `intent_type`, typed non-personal `failure_code`, `confidence_bucket`, `latency_bucket` |
| `command_executed` | `intent_type`, `latency_bucket` |
| `manual_correction` | `previous_intent`, `correction_type` |
| `player_error` | typed non-personal `error_code`, coarse `route_type` |
| `session_completed` | `duration_bucket`, `commands_count` |

Properties must use documented enums/buckets. Event names and properties cannot be expanded during implementation without a Privacy review against the PRD/MVP authority.

## Prohibited

The following must not enter analytics, crash reports, logs or repository evidence:

- raw audio, waveform/voice recordings or transcripts;
- artist/album/folder names, full or partial personal-file titles, filesystem paths, content URIs or MediaStore IDs;
- Search queries/results, queue contents or precise playback position;
- precise location, contacts, call identity, Bluetooth device identity or vehicle identity;
- provider credentials, account identifiers, access tokens, secrets or unnecessary persistent identifiers;
- free-form diagnostic payloads that could contain any prohibited value.

Coarse typed error, route, intent, confidence, latency, duration and count values are allowed only when they cannot reconstruct personal content or identity.

## Consent, retention and deletion

- Local microphone permission and any notification/media permission use truthful contextual education and denial recovery.
- Cloud STT remains disabled/unavailable until separate consent and provider/destination, purpose and retention disclosure are approved and implemented.
- Exact provider retention/deletion behavior cannot be claimed until the provider and legal terms are selected and reviewed.
- Local history and diagnostic-data clearing must be available from Settings without deleting user-owned media.

## Verification

- Schema tests reject unknown events, unknown properties, free-form payloads and prohibited fields.
- Crash-report tests verify that personal media and Voice content are absent.
- Consent tests verify that remote processing cannot start before the approved consent state.
- Clearing tests verify app-owned history/diagnostics removal without deleting MediaStore content.
- Data-quality checks verify that allowed events are sufficient for Beta decisions without relaxing privacy rules.

## Dashboard

The weekly dashboard answers three questions: Is it used? Does it work correctly? Does the user return? It must preserve the PRD/MVP thresholds and must not infer missing telemetry as successful product behavior.

