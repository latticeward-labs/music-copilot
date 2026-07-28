# ADR-0001: Native Android 10+ platform

- **Status:** Proposed
- **Date:** 2026-07-27
- **Authority:** [PRD v1.0](../PRD.md) and [MVP v1.0](../MVP.md)
- **Approval:** Pending in [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)

## Context

The MVP must validate reliable local music, Bluetooth, Audio Focus and background playback within ten weeks.

## Proposed decision

Build a native Android 10+ application using Kotlin, Jetpack Compose, Coroutines/Flow and Hilt. Deliver through Android internal/closed testing.

Version 1.0 does not lock a separate numeric `minSdk`, `compileSdk`, `targetSdk`, ABI-filter policy or API 26 support matrix.

## Validation

- Healthy build on two representative devices in week 1
- Android 10, Android 12 and Android 14+ QA
- Pixel/stock, Samsung/One UI and Xiaomi/MIUI field coverage
- At least three vehicle/head-unit combinations
