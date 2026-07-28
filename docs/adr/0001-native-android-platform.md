# ADR-0001: Native Android 8.0+ platform and API 36 release baseline

- **Status:** Accepted
- **Date:** 2026-07-27
- **Revised:** 2026-07-28
- **Decision owner:** Latticeward Labs
- **Approval:** [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2)
- **Authority:** PRD v1.1 sections 05, 07 and 09; MVP v1.1 sections 06-07

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
- Pull-request CI covers API 26, 33 and 36; Nightly coverage adds API 29, 31 and 34.
- Emulator coverage is acceptable for deterministic behavior. Playback routes, Bluetooth, audio focus and lifecycle require an older API 26-29 device, Android 12/13, Android 16, representative Pixel/Samsung/Xiaomi devices where available, and at least three head units.
- No explicit `abiFilters` are configured while the app has no native dependency. A native dependency triggers a 64-bit, package-size and device-coverage review before any ABI is filtered.

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
2. pull-request build/install/smoke evidence on API 26, 33 and 36;
3. nightly compatibility evidence on API 29, 31 and 34;
4. legacy and Android 13+ media-permission tests;
5. foreground-service and media-notification tests across the matrix;
6. physical-device evidence for Bluetooth, audio focus, route loss and background playback;
7. a pre-release check of the then-current Google Play target-API policy.
