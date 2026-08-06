# ADR-0006: No backend, account or streaming in MVP v1.0

- **Status:** Accepted
- **Date:** 2026-07-27
- **Reviewed:** 2026-08-06
- **Authority:** [PRD v1.0](../PRD.md) and [MVP v1.0](../MVP.md)
- **Review evidence:** [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)
- **Outcome:** Accepted after revision of external-service and future-discovery boundaries

## Context

MVP v1.0 validates local-file playback and Voice/Driving behavior. Accounts, product backend, sync, online catalog, streaming and distribution do not test the immediate hypothesis and add licensing, privacy, security and schedule risk.

## Decision drivers

- Preserve locked v1.0 scope and timebox
- Avoid streaming licensing and account-security work
- Keep primary product data local
- Prevent future possibilities from becoming committed scope

## Decision

MVP v1.0 has no user account, authentication, product backend, cloud sync, online music catalog, music streaming, download, upload or content distribution. The app plays lawful audio already on-device.

This does not silently authorize external services. Privacy-safe diagnostics and optional speech processing are separate support processors only when approved data, consent and retention boundaries are satisfied. They must not become a product account, media backend, catalog or sync channel.

After successful Beta evidence, Discovery may evaluate one lawful music-service integration and iOS. Neither is committed to v1.0 or a future Roadmap here.

## Consequences

### Positive

- Smaller delivery/security surface
- No media licensing dependency for MVP
- No sign-up or sync required
- Beta can focus on playback, Voice and Driving value

### Costs and risks

- Streaming-only users are not representative Beta candidates
- No cross-device restore or account identity
- Diagnostics/cloud STT still require independent privacy/vendor review

## Alternatives considered

- **Accounts/backend/sync now:** rejected as unnecessary and outside scope.
- **Streaming integration in v1.0:** rejected due to licensing and delivery risk.
- **Upload local media:** rejected due to privacy/storage/content rights.
- **Commit iOS now:** rejected until Android Beta evidence.

## Validation and open evidence

- No account/backend/sync/catalog/streaming path in repository, UI or tests
- Beta recruiting records local-file sampling bias
- Privacy review covers diagnostics/speech processors separately
- Future service integration requires a new scope gate and ADR
