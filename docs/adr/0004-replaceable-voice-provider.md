# ADR-0004: Replaceable voice provider

- **Status:** Accepted
- **Date:** 2026-07-27
- **Reviewed:** 2026-08-06
- **Authority:** [PRD v1.0](../PRD.md) and [MVP v1.0](../MVP.md)
- **Review evidence:** [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)
- **Outcome:** Accepted after revision of provider, network and consent boundaries

## Context

The MVP uses push-to-talk, one retry, short TTS and a restricted intent mapper. Android speech services vary by device and may process speech locally or through a network service. Product behavior must not be coupled to one provider, and cloud STT requires explicit privacy treatment.

## Decision drivers

- Preserve the Voice contract across provider changes
- Limit microphone use and raw-audio exposure
- Permit device/OEM validation and substitution
- Keep intent interpretation separate from transcription

## Decision

Place STT and TTS behind replaceable application interfaces. Android STT/TTS is the initial adapter. Push-to-talk is the only microphone entry; listening lasts at most six seconds, playback ducks, one retry is allowed, raw voice is not stored by the app, and TTS is at most one short sentence.

The provider returns transcription/result and typed failures; it does not execute playback commands or define product intents. ADR-0005 owns interpretation.

No offline-STT guarantee is made until device/provider tests prove it. Cloud speech processing requires separate consent and disclosure of provider/destination, purpose and retention before use. Refusal preserves manual playback.

Exact interfaces, provider selection, availability checks, timeout mechanics and fallback UX remain for Issue #16 and User Flow validation.

## Consequences

### Positive

- Provider changes do not rewrite Voice/playback behavior
- Privacy and network behavior can be reviewed at the adapter boundary
- STT/TTS can be faked in tests

### Costs and risks

- Availability and accuracy vary by device
- Cloud processing adds consent, latency, retention and connectivity risks
- Abstraction design must not leak provider details

## Alternatives considered

- **Speech APIs directly from UI:** rejected due to lifecycle/provider coupling.
- **Commit to cloud STT now:** rejected because provider/consent evidence is not approved.
- **Custom speech model:** rejected due to timebox and device risk.
- **Always-on wake word:** rejected by scope and privacy constraints.

## Validation and open evidence

- Provider behavior across target Android/OEM matrix
- Permission denial/revocation, timeout, cancellation and one retry
- Ducking/restoration and short TTS
- Network loss for providers requiring connectivity
- Privacy/legal review before enabling cloud processing
