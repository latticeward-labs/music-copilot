# ADR-0001: Native Android 10+ platform

- **Status:** Accepted
- **Date:** 2026-07-27
- **Reviewed:** 2026-08-06
- **Authority:** [PRD v1.0](../PRD.md) and [MVP v1.0](../MVP.md)
- **Review evidence:** [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)
- **Outcome:** Accepted after revision of rationale, consequences, alternatives and open evidence

## Context

The MVP must validate reliable local-file playback, Bluetooth and Audio Focus behavior, background playback, Driving Mode and six English push-to-talk intents within a ten-week Android-only Beta plan. The product sources require Android 10+ and explicitly exclude iOS and Android Auto from v1.0.

## Decision drivers

- Direct alignment with the locked Android-only product scope
- Access to Android media, MediaSession, service and Bluetooth capabilities
- Shortest credible path to the device and vehicle QA matrix
- Maintainable, testable implementation within the MVP timebox

## Decision

Build a native Android 10+ application using Kotlin. Use Jetpack Compose for UI, Coroutines/Flow for asynchronous and observable state, and Hilt for dependency injection. Distribute through Android internal testing and Closed Beta.

Native Android 10+ and Kotlin are binding v1.0 platform constraints. Compose, Coroutines/Flow and Hilt are accepted baseline implementation choices; a replacement that preserves scope and contracts requires architecture review but not a product-scope change.

This ADR does not decide navigation, screen-level state ownership, ViewModel boundaries, package layout or Gradle-module granularity. Those remain with [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16).

## Consequences

### Positive

- Direct use of Android media and lifecycle APIs
- One platform to test and stabilize during the MVP
- Strong support for observable state and dependency substitution
- No cross-platform abstraction cost before product value is proven

### Costs and risks

- Android-only code cannot be reused directly for iOS
- OEM and OS-version differences require the full QA matrix
- Compose and Hilt version compatibility must be controlled in the build
- Architecture can still become over-modularized unless Issue #16 sets explicit boundaries

## Alternatives considered

- **Kotlin Multiplatform:** rejected for v1.0 because iOS is out of scope and shared abstractions would add delivery risk before Beta evidence.
- **Flutter or React Native:** rejected because the MVP depends heavily on Android media, service, Audio Focus and Bluetooth behavior.
- **XML Views instead of Compose:** viable but not selected; Compose is the accepted baseline and supports the proposed Android stack.
- **Support below Android 10:** rejected because it expands compatibility cost beyond the approved target.

## Validation and open evidence

- Healthy build on two representative devices in Week 1
- Android 10, Android 12 and Android 14+ QA
- Pixel/stock, Samsung/One UI and Xiaomi/MIUI coverage
- At least three vehicle/head-unit combinations
- Exact SDK/library versions, navigation and module strategy remain open until Issue #16 or implementation planning
