# Issue #7 — P03 Safe Local MediaStore URI Resolver: implementation and acceptance audit

**Date:** 2026-10-22 · **Branch:** `feature/7-media3-playback-queue-session`  
**Scoped gate:** `PASS_SCOPED` **after GitHub read-back** — implementation files, injectable source contract, authored fake/JUnit coverage and source audit. **Native Android/Gradle/JUnit and per-case technical/phone runs remain NOT_RUN. This is not a playable Media3 milestone.**  
**Tracking:** [#7](https://github.com/latticeward-labs/music-copilot/issues/7), [P00 audit](ISSUE_7_PHASE0_READINESS_AUDIT.md), [P01 contract](ISSUE_7_PHASE1_EXECUTION_CONTRACT.md), [P02 queue semantics](ISSUE_7_PHASE2_PLAYBACK_QUEUE_POLICY.md).

## 1. Source provenance and integration baseline

Rechecked user-supplied `MSM2(1)(1).rar` SHA-256 **`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`**. Current GitHub product [PRD](../PRD.md), [MVP](../MVP.md), [software architecture](../SOFTWARE_ARCHITECTURE.md), Accepted [ADR-0002](../adr/0002-service-owned-media3-playback.md), [ADR-0003](../adr/0003-local-first-data-boundaries.md), [ADR-0007](../adr/0007-same-process-playback-runtime-and-safe-paused-restore.md), [runtime policy](../architecture/RUNTIME_POLICIES.md) and approved [UF-05](../user-flows/UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.md) remain authoritative. Specifically, **MediaStore is existence/read authority**, derived Room records are not proof of current playable media, and the **existing MediaSessionService alone owns playback**.

Pre-P03 source `data/mediastore/MediaStoreScanner.kt` already implemented API 29+ permissions, scoped/legacy MediaRef codec, mounted volume resolution, per-track requery and read-only descriptor verification for Issue #6 selection. The new service-consumable P03 resolver **reuses exactly the codec** and extends fail-closed behavior without replacing #6 scanner or loosening its existing selection constraints.

## 2. Actual files and responsibilities

| Path | Delivered behavior |
|---|---|
| [`MediaStoreRefCodec.kt`](../../app/src/main/java/com/latticeward/musiccopilot/data/mediastore/MediaStoreRefCodec.kt) | **Mechanical extraction** from existing scanner: legacy `audio:<decimal-id>` is **primary only**, scoped `audio:v:<volume>:<decimal-id>` uniquely identifies supported mounted volumes; rejects aggregate `external`, `internal`, path/URI and malformed numeric IDs. Existing #6 codec public API unchanged. |
| [`MediaStoreScanner.kt`](../../app/src/main/java/com/latticeward/musiccopilot/data/mediastore/MediaStoreScanner.kt) | Removed duplicate codec declaration only; keeps existing scanning/requery logic. |
| [`MediaStorePlaybackResolver.kt`](../../app/src/main/java/com/latticeward/musiccopilot/data/mediastore/MediaStorePlaybackResolver.kt) | **New injectable, pure policy** `PlaybackMediaProbe` + `LocalPlaybackMediaResolver`; validates MediaRef, fresh OS access, membership of concrete mounted volume, read-only probe; produces canonical content URI token only on confirmed success. No cached success. Cancellation propagates; `invalidate()` increments epoch, rejecting stale in-flight results. Typed sanitized errors. |
| [`AndroidMediaStorePlaybackProbe.kt`](../../app/src/main/java/com/latticeward/musiccopilot/data/mediastore/AndroidMediaStorePlaybackProbe.kt) | **Actual Android adapter**: API 29–32 `READ_EXTERNAL_STORAGE`, API 33+ `READ_MEDIA_AUDIO`; `MediaStore.getExternalVolumeNames`, item-specific `content://media/<volume>/audio/media/<id>`, platform ID/volume/MIME/music checks; `ContentResolver.query` and `openFileDescriptor(uri, "r", cancellationSignal)` with cursor/descriptor closure. Never opens writable or filesystem data path; reports null cursor as unavailable, not verified Empty. |
| [`Issue7Phase3MediaResolverTest.kt`](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue7Phase3MediaResolverTest.kt) | **12 authored JVM fake-probe tests** for isolation, multi-volume collisions, legacy primary, malformed/path input, permission denied/revoked, provider errors, no-cache recheck, cancellation and epoch invalidation. **JUnit execution NOT_RUN**; no test PASS inferred from the authored test count. |
| [`issue7_phase3_resolver_smoke.kt`](../../scripts/issue7_phase3_resolver_smoke.kt) and [`check_issue7_phase3_resolver.sh`](../../scripts/check_issue7_phase3_resolver.sh) | Independent optional **17-check synthetic Kotlin smoke**, compiles actual pure project resolver/codec/port sources using local `kotlinc`, JDK and local coroutines library. **Authored, NOT_RUN on full repository checkout**. Does not build or deliver an APK. |

**Component creation/wiring boundary:** Android adapter can be constructed with `AndroidMediaStorePlaybackProbe(applicationContext)`; consumer creates `MediaStorePlaybackResolver(platform, dispatchers)` **on service-side IO scope**. P04 creates the real service queue handoff and uses P02 `PlaybackQueuePolicy.validate/retainPlayable`; P05 feeds `VerifiedLocalMedia.contentUri` to the existing ExoPlayer/Media3 MediaItem. The resolver is intentionally not wired into `MusicPlaybackService` yet, whose `ReplaceQueue` still rejects in the current branch.

## 3. Deterministic safety semantics

1. **Identity and volume:** `MediaStoreRefCodec.decodeScoped` rejects unsupported input and never treats a numeric ID from a USB/removable volume as primary. An old unscoped `audio:<id>` maps **only** to `external_primary`. No storage path fallback, aggregate `external` lookup, file-scheme URI or `MediaStore.MediaColumns.DATA` projection.
2. **Fresh access:** Read the OS permission before any resolution, again after volume membership and after the specific item probe. The Android adapter also checks around its query and FD open. Denial at any point returns `AppError.Permission(PERMISSION_REVOKED)`; no descriptor is retained or success published.
3. **Existence vs readability:** Mounted volume membership + real item URI query must show matching row ID, correct volume, `IS_MUSIC != 0` and audio/unknown MIME. Then a **read-only** file descriptor is opened and closed. Missing rows/absent volumes are `MEDIA_ITEM_MISSING`; non-audio/unreadable/provider-failure are `MEDIA_ITEM_UNPLAYABLE`. A later Media3 decoding error remains a P05/P10 concern; this probe never claims the entire audio file can decode.
4. **Cancellation:** `currentCoroutineContext().ensureActive()` gates every stage; Android query and descriptor opening share platform `CancellationSignal` bound to coroutine Job completion; `CancellationException` is rethrown. A cancelled attempt never returns a fabricated Empty/Success.
5. **Invalidation and freshness:** No successful result cache: every call re-queries/opens. `invalidate()` increments `AtomicLong` epoch; a mid-flight result fails closed if the generation changed. Source/permission changes must call `invalidate()` from P04/P05 ownership; even without the explicit call, the per-call access checks still apply.
6. **Error/privacy:** Public errors carry fixed codes, not `Throwable.message`, user song name, private MediaRef, URI or filesystem path; `VerifiedLocalMedia.toString()` is redacted. Actual URI is internal and must never be logged or placed in telemetry/issue artifacts.
7. **TOCTOU honesty:** Existence+openable **at probe time** is not a durable grant; a file can disappear/revoke before Media3 opens it. P05 and P10 must catch prepare/open/decode errors and rebuild only from another fresh probe, never claim permanent readability or write to source.

## 4. Negative test matrix and evidence state

| Synthetic case | Expected | Source location | Executed? |
|---|---|---|---|
| Two mounted volumes, same numeric ID | Distinct canonical content URI, no cross-volume mix | `primaryAndRemovableSameIdResolveWithoutCollision` | JUnit NOT_RUN |
| Old unscoped ref and missing primary | Only primary; no substitution of removable volume | `legacyUnscopedRefOnlyResolvesPrimary`, `missingPrimaryNeverSilentlyUsesRemovableId` | JUnit NOT_RUN |
| Path, arbitrary URI, invalid volume/long number | Typed `MEDIA_ITEM_MISSING` without provider access | `malformedRefAndRawPathsFailWithoutTouchingProvider`, `oversizedIdAndUnavailableMountedVolumeCannotProbe` | JUnit NOT_RUN |
| Grant absent or revoked mid-query | `PERMISSION_REVOKED`, never Success | 2 permission methods | JUnit NOT_RUN |
| Provider missing/unplayable/unavailable/security/IO | Typed sanitized error; no raw exception | 2 error methods | JUnit NOT_RUN |
| Repeated resolve followed by deletion | Rechecks each time; no stale success cache | `noCacheRechecksEachRequestAndRedactsTransportToString` | JUnit NOT_RUN |
| Epoch invalidation during pending read | Failure even after fake returns Readable | `sourceInvalidationRejectsInFlightReadableResult` | JUnit NOT_RUN |
| Caller cancellation | Real `CancellationException` preserved | `cancellationPropagatesRatherThanReportingFakeMissingOrEmpty` | JUnit NOT_RUN |
| Full Android ContentResolver/MediaStore/FD and OEM runs | Requires actual SDK/provider instrumentation | T07 and P12/P13 | NOT_RUN |

**Static code review and GitHub read-back:** must verify 6 implementation/test/script artifacts and mechanical codec extraction, plus no extra permissions, Player or remote endpoints. This is **source acceptance** only. No Android SDK build, JVM JUnit, instrumented API29/31/33+/35, device, audible playback or specific M/T suite result is claimed. The existing [owner QA ledger](ISSUE_7_QA_RESULTS.json) must remain all **36 M + 15 T NOT_RUN**. All original #7 AC01–AC13 still **NOT_VERIFIED**.

## 5. P03 exit gate and P04 handoff

**Eligible P03 result:** `PASS_SCOPED` when the code and tests above exist on the correct feature branch, exact archive/ADRs reviewed, GitHub read-back confirms codec and Android adapter, and regression/scope concerns are listed. This is not `PASS_VERIFIED` Android compilation or functional playback.

**P04 required next integration:** service-side typed `ReplaceQueue` validation; pass every candidate through this resolver on an IO dispatcher with **no 5,000 FD probes on the Player/Main thread**; preserve order/selected occurrence, fail selected entry closed, only then atomically commit queue to the existing service Player. On permission revoke and volume changes invoke `invalidate()`; recheck before prepare, protect cancellation/epoch. Reconcile #6 visible-collection (not single-item) handoff in P04/P05/P07. Record actual reproducible Gradle/JUnit and Android MediaStore evidence before any AC or T07 PASS. Never build the **owner APK** before P13.

**Issue status:** OPEN; PR, merge, CI run and APK NOT_REQUESTED here.
