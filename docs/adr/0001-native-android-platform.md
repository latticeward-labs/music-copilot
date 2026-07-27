# ADR-0001: Native Android 10+ platform baseline

- **Status:** Proposed
- **Date:** 2026-07-27
- **Decision owner:** Latticeward Labs
- **Approval:** [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)
- **Authority:** PRD v1.0 section 09; MVP v1.0 scope and week 1

## Context

MVP v1 must validate reliable, low-touch local music control in real vehicles within ten weeks. Playback lifecycle, Bluetooth behavior, audio focus and Android media controls are higher risks than cross-platform reach.

## Decision

Build MVP v1 as a native Android application with:

- minimum supported OS: Android 10 (API 29),
- Kotlin as the implementation language,
- Jetpack Compose for application UI,
- coroutines and Flow for asynchronous/state streams,
- Hilt for dependency injection and test substitution.

All user-visible strings remain externalized even though the Closed Beta voice contract and initial UI are English.

## Consequences

### Positive

- Direct access to Android media, lifecycle, permission and Bluetooth APIs.
- Lower delivery and debugging risk for background playback.
- One platform matrix for the first Beta.

### Trade-offs

- No iOS client in MVP v1.
- No shared KMP domain layer during validation.
- Cross-platform reuse is deferred until the Beta decision gate.

## Rejected alternatives

- **Kotlin Multiplatform now:** adds abstraction and integration cost before product value is proven.
- **Cross-platform UI framework:** increases risk in MediaSession, service lifecycle and route handling.
- **Earlier Android versions:** expand QA cost without being required by the approved scope.

## Validation

Approval requires a clean Android 10+ build baseline and an explicit device test matrix covering Android 10, 12 and 14.
