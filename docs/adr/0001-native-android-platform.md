# ADR-0001: Native Android 8.0+ platform and API 36 release baseline

- **Status:** Proposed
- **Date:** 2026-07-27
- **Revised:** 2026-07-28
- **Decision owner:** Latticeward Labs
- **Approval:** [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)
- **Authority:** PRD v1.0 section 09; MVP v1.0 scope and week 1

## Context

MVP v1 must validate reliable, low-touch local music control in real vehicles within ten weeks. Playback lifecycle, Bluetooth behavior, audio focus and Android media controls are higher risks than cross-platform reach.

The original Android 10+ proposal reduced the test matrix but unnecessarily excluded usable older and budget devices. Installation compatibility and Google Play release compliance are separate concerns: `minSdk` controls which devices can install the app, while `targetSdk` controls the Android behavior contract and Play policy baseline.

## Decision

Build MVP v1 as a native Android application with:

- minimum supported OS: Android 8.0 (API 26; `minSdk 26`),
- initial 2026 compile SDK: API 36 (`compileSdk 36`),
- initial 2026 target SDK: API 36 (`targetSdk 36`),
- Kotlin as the implementation language,
- Jetpack Compose for application UI,
- coroutines and Flow for asynchronous/state streams,
- Hilt for dependency injection and test substitution,
- Android App Bundle (`.aab`) for Google Play distribution.

The target SDK must be checked against the current Google Play requirement before every release and raised when policy requires it. This release-setting update does not expand MVP product scope.

All user-visible strings remain externalized even though the Closed Beta voice contract and initial UI are English.

## Compatibility boundaries

- Android 8–12 use the legacy media-read permission path where required.
- Android 13+ uses `READ_MEDIA_AUDIO`.
- Foreground-service, media notification, runtime-permission and Bluetooth differences are isolated behind API-level adapters.
- The required OS test matrix is Android 8/API 26, Android 10/API 29, Android 13/API 33 and Android 16/API 36.
- Emulator coverage is acceptable for deterministic platform behavior; playback routes, Bluetooth, audio focus and device-specific lifecycle behavior require representative physical-device evidence.
- `arm64-v8a` is the baseline ABI. `armeabi-v7a` is enabled only after every native dependency supports it and coverage benefit is verified.

## Consequences

### Positive

- Direct access to Android media, lifecycle, permission and Bluetooth APIs.
- Broader coverage of older and budget devices than the Android 10+ proposal.
- Current Play-target baseline remains independent from installation compatibility.
- One native platform for the first Beta.

### Trade-offs

- Android 8 and 9 add permission, service, notification and lifecycle test paths.
- The QA matrix is larger than Android 10+.
- No iOS client or shared KMP domain layer in MVP v1.
- Target SDK and dependency compatibility must be revalidated for every Play release.

## Rejected alternatives

- **Android 10+ minimum:** simpler QA, but excludes compatible older devices without a demonstrated MVP requirement.
- **Android 6/7 minimum:** broader theoretical reach, but adds disproportionate permission, security, background-execution and testing complexity.
- **Kotlin Multiplatform now:** adds abstraction and integration cost before product value is proven.
- **Cross-platform UI framework:** increases risk in MediaSession, service lifecycle and route handling.
- **Using a low target SDK to preserve compatibility:** rejected because target SDK is a Play/platform compliance contract, not a device-coverage control.

## Validation

Approval requires:

1. a clean `minSdk 26`, `compileSdk 36`, `targetSdk 36` build baseline;
2. build/install/smoke evidence on API 26, 29, 33 and 36;
3. legacy and Android 13+ media-permission tests;
4. foreground-service and media-notification tests across the matrix;
5. physical-device evidence for Bluetooth, audio focus, route loss and background playback;
6. a pre-release check of the then-current Google Play target-API policy.
