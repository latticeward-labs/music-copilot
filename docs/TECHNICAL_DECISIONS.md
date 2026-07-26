# Technical Decisions

This document records the proposed baseline. A decision becomes binding only when its corresponding GitHub issue is accepted.

## Proposed baseline

| Area | Proposed choice | Status |
|---|---|---|
| Mobile | Android, Kotlin, Jetpack Compose | Proposed |
| Architecture | Feature-oriented, layered modules | Proposed |
| UI state | Unidirectional data flow | Proposed |
| Async work | Kotlin coroutines and Flow | Proposed |
| Local storage | Room or DataStore by data type | Proposed |
| Networking | Retrofit/OkHttp or Ktor client | To evaluate |
| Backend | API-first; provider chosen after requirements | Open |
| Authentication | Provider chosen after privacy and cost review | Open |
| Crash reporting | Firebase Crashlytics | Proposed |
| Analytics | Minimal allowlisted events | Proposed |
| Distribution | Google Play Internal Testing | Proposed |
| Design | Figma with shared tokens and components | Proposed |

## Required decisions before implementation

1. Confirm native Android versus cross-platform.
2. Confirm authentication requirements for the first vertical slice.
3. Identify the legal source of MVP audio.
4. Define the smallest backend boundary.
5. Define offline and caching behavior.
6. Set minimum Android version and supported devices.
7. Approve analytics consent and data retention rules.
8. Define environments: local, test, staging, production.

## Architecture principles

- Build a vertical slice before broad infrastructure.
- Keep playback, catalog, library, identity, and telemetry boundaries explicit.
- Keep provider-specific SDKs behind adapters.
- Do not place secrets in the client or repository.
- Make telemetry opt-in or consent-aware where required.
- Prefer reversible decisions until usage data justifies complexity.

## Decision record format

For every consequential choice, add a short ADR under `docs/adr/`:

```markdown
# ADR-NNN: Decision title

## Context
What problem are we solving?

## Decision
What did we choose?

## Alternatives
What else was considered?

## Consequences
What improves, and what trade-offs remain?
```
