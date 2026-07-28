# ADR-0006: No backend, account or streaming in MVP v1

- **Status:** Accepted
- **Accepted:** 2026-07-28
- **Date:** 2026-07-27
- **Decision owner:** Latticeward Labs
- **Approval:** [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)
- **Authority:** Canonical [PRD v1.1](../PRD.md) and [MVP v1.1](../MVP.md)

## Context

The first product question is whether drivers repeatedly use reliable push-to-talk control for lawful local music. Backend, identity, synchronization and licensed streaming do not answer that question and introduce legal, operational, privacy and delivery risk.

## Decision

MVP v1 contains no:

- application backend or production API,
- account, login, authentication or user profile,
- cloud synchronization,
- online music catalog, streaming, download, upload or distribution,
- subscription, advertising, royalty or rights-management system.

Primary product data stays on the device. Network access may be used only by separately approved, privacy-reviewed platform services such as crash diagnostics or telemetry; it must not become a product-data backend. App functionality has a local fallback where the approved scope requires offline operation.

## Consequences

The team can focus the ten-week timebox on Player, Driving Mode, Voice and field reliability. Users cannot sync devices or access online catalogs. Streaming/business-model discovery remains a post-Beta decision.

## Rejected alternatives

- **Backend scaffold “for later”:** creates maintenance, security and scope cost without MVP value.
- **Anonymous account:** still creates identity and policy obligations.
- **Third-party streaming integration:** adds licensing, API dependency and content-rights risk.

## Validation

A clean app build requires no backend URL or private credential. Dependency and network-security reviews find no catalog/account/sync path. The board contains no implementation item for these capabilities.
