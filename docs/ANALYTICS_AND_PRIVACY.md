# Analytics and Privacy — MVP v1.1

> Derived from the canonical [PRD v1.1](PRD.md) and [MVP v1.1](MVP.md). If this document conflicts with either source, the canonical source wins.

## Principles

Collect only what is required to answer: is Driving Mode used, does Voice work, and do users return? Raw voice is never stored. Personal media metadata is not telemetry. Permissions are requested just in time and explained.

On-device recognition is preferred. The network-capable Android system recognizer is disabled by default and may be enabled only after clear, revocable consent describing the available processor, destination and retention behavior. Revocation immediately returns the app to on-device Voice when available, otherwise to complete manual control.

## Event allowlist

| Event | Allowed properties | Question |
|---|---|---|
| `driving_mode_started` | `entry_source`, `bt_connected` | How is Driving Mode entered? |
| `voice_button_pressed` | `session_id`, `playback_state` | Is push-to-talk used? |
| `intent_detected` | `intent_type`, `confidence_bucket` | Which intents and confidence ranges occur? |
| `command_executed` | `intent_type`, `latency_bucket`, `result` | Is execution fast and successful? |
| `intent_failed` | `intent_type`, `failure_code` | Why does restricted Voice fail? |
| `manual_correction` | `previous_intent`, `correction_type` | How often must users correct Voice? |
| `player_error` | `error_code`, `route_type` | Is Player/route behavior stable? |
| `session_completed` | `duration_bucket`, `commands_count_bucket` | Retention and North Star |
| `library_scan_completed` | `duration_bucket`, `item_count_bucket`, `result` | Can local libraries be prepared? |

No arbitrary event or property may be emitted. A central allowlist enforces names and typed properties.

## Prohibited data

Never collect or log:

- raw audio or microphone recordings,
- full filename, track title, artist or album from personal media,
- raw speech transcript,
- exact location,
- contacts, advertising ID or unnecessary device identifier,
- email, name, phone, account token or password,
- free-form exception text that may include personal content.

A short-lived transcript may exist in memory only for current intent resolution and must be discarded immediately afterward.

## Consent and control

- Media and microphone permissions are separate and contextual.
- Diagnostic telemetry requires clear disclosure and any legally required consent.
- Voice-provider mode and availability may be reported only as coarse allowlisted categories; provider identity, transcript and recognized artist text are prohibited.
- Network-recognizer consent is separate, revocable and never bundled with media or microphone permission.
- The user can clear history, Bluetooth preference and diagnostic identifiers/settings.
- Permission denial never crashes the app; permanent denial links to Android Settings.
- Retention must be set before Beta and documented in the privacy notice.
- Data deletion and collection-disable behavior must be verified.

## Dashboard

Weekly reporting must show:

- Driving Mode start and reuse
- sessions with at least one voice command
- direct-command success by intent
- manual-correction rate
- serious wrong-execution rate
- latency distribution and P95
- player errors by route type
- serious-crash-free sessions
- weekly return cohort

## Governance

Every change requires a product question, owner, allowlisted schema, retention decision, privacy review and DebugView/equivalent verification. Documentation and implementation must match before release.
