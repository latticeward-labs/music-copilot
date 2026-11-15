# Issue #12 — P07 Player Error and Privacy-Safe Crash Evidence

**Date:** 2026-11-15  
**Branch:** `feature/12-privacy-safe-telemetry-measurement`  
**Status:** `SOURCE_IMPLEMENTED_UNCOMPILED_NOT_RUN` — no tests, Gradle, CI, emulator, APK or user-device verification.  
**Input:** exact user-uploaded `MSM2(1)(1).rar` verified SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`; the 12 original English PDFs (PRD, MVP, Architecture, Technical Blueprint, ADR-0001–0006, user flows) were inspected.

## 1. Normative product sources and architecture baseline

- **Music_Copilot_PRD_v1_EN_Clarified.pdf, printed pp.8–10**: safe recovery for corrupt audio files; at least **99% crash-free journeys**, **fewer than one critical Player error per 100 sessions**, no incorrect route output or dangerous distraction; eight privacy-safe event families including `player_error`; raw audio prohibited.
- **Music_Copilot_MVP_v1_EN.pdf, printed pp.9–11**: `player_error(error_code,route_type)` with stable code and coarse output route; informed consent for non-audio diagnostics; `>=99%` crash-free; no audio, full personal titles, precise location, contacts, unnecessary identifiers. Any reproducible serious Playback crash or incorrect output is P0; privacy copy and data-quality review before Beta GO.
- **Music_Copilot_System_Architecture_MVP_v1_EN.pdf**, approved architecture and **ADR-0002**: **one Media3 Service and its PlaybackSnapshot** are the only authority. Quality is observational, not another player/service or a source of playback changes.
- **ADR-0003** (local-first), **ADR-0004** (nonpersistent private Voice), **ADR-0005** (bounded intent), **ADR-0006** (no backend/account/streaming): no raw transcript, media URI, user/device/location or stacktrace in diagnostics. Remote provider, retention and data processing require separate Product/Privacy/Legal approval.
- Repository `docs/ANALYTICS_AND_PRIVACY.md`, `docs/implementation/ISSUE_12_EVENT_SCHEMA.md` (P03), `docs/implementation/ISSUE_12_P04_DIAGNOSTICS_CONSENT_AND_PRIVACY_GATE.md` (P04), `docs/implementation/ISSUE_12_METRIC_DICTIONARY.md` (P02), Issue #12 and approved Issue #16 dependency direction.

**Interpretation:** A recoverable Player error is NOT a crash; a raw thrown exception may contain a personal media path and cannot be forwarded; a process disappearing without reliable fatal evidence is UNKNOWN, never automatically crash-free or crash. The eight permitted outbound event families cannot by themselves identify complete participant-level crash-free journeys.

## 2. PlayerError source, single-writer, and dedup strategy

**Existing source, unchanged semantic ownership:** `MusicPlaybackService` already maps `PlaybackException` (Media3 numeric categories and bounded exception *type*, never exception text) into safe `AppError.MediaItem/Playback` or handles `AppError.Interruption` from real focus/noisy/route signals. The actual normalized state lives in `PlaybackSnapshot.lastError` and is published by the same `publishSnapshot()` that populates MediaSession extras. Media3's existing forward-recovery mechanism and Safe Paused state remain untouched.

P07 adds a **small observation seam *after* publication of the authoritative snapshot**:

1. The sole service Main-looper `publishSnapshot()` commits `confirmed = next`, calls `session.setSessionExtras(wrap(next))`, and retains the original `pausedCheckpoint` behavior.
2. New service-owned `ServicePlayerErrorEpisodeGate.observe(next.lastError)` consumes ONLY this already published typed error. Repeated `onEvents` or unchanged Snapshot cannot create another event.
3. Gate accepts only the existing AppError MediaItem/Playback/Interruption families, converts `code` into an **exact closed Kotlin `PlayerRuntimeErrorCode` enum**; drops null/unknown/forbidden codes without logging either the input or the discarded value. It **never forwards the original AppError** (which can contain a `MediaRef`), Media3 `PlaybackException`, URI, message, Throwable or stack.
4. New `playback/api/PlaybackRuntimeDiagnosticHost` callback is implemented by **MusicCopilotApplication**. No Playback -> Quality/DI dependency is introduced. Failure of optional callback is caught by service and never changes Media3, focus, route, queue, recovery or user-visible result.
5. The application root `ApplicationBindings.recordServicePlayerError` checks the live P04 independent diagnostics authorization and produces **only** `TelemetryEvent.PlayerError(errorCode.name, RouteType.Unknown)` into the existing P06 bounded, memory-only FIFO. P03 gate and P04 gated NoOp sink check again before delivery.

**Dedup definition:** identical stable code observed across a contiguous, non-cleared error episode produces **one** event, even if the service publishes 100 progress/route/recovery snapshots. A different allowed code starts another episode. A published `lastError=null` rearms the same code for a later independent episode. This chooses conservative **undercount over false duplicate claims** if two real identical failures occur without a visible clearing transition; P10 must label this limitation and preserve incident evidence from owner QA. No ID, timestamp or MediaRef is exported to force distinctness.

**Schema integrity:** only the original P03 `player_error(error_code,route_type)` family is used. The closed enum includes PLAYBACK_BUSY, PLAYBACK_REJECTED, PLAYBACK_FAILED, CONTROLLER_DISCONNECTED, CONTROLLER_CONNECT_FAILED, CONTROLLER_TIMEOUT, MEDIA_ITEM_MISSING, MEDIA_ITEM_CORRUPT, MEDIA_ITEM_UNPLAYABLE, FOCUS_NOT_RECOVERED, ROUTE_INVALIDATED, CALL_OR_ROUTE_SAFE_PAUSED. The *service* produces only the appropriate subset (e.g. MEDIA_ITEM and PLAYBACK/Interruption); disconnected-controller errors do not masquerade as service errors. No new ninth external event or free-form custom field.

**Known schema/source gap:** service `PERMISSION_REVOKED` is safe and relevant to interruption recovery, but **P03's currently frozen `PlayerError.error_code` allowlist does not accept it**. P07 does not widen that list without Product/Privacy signoff; this observation is currently **dropped** from `player_error` and must appear as coverage/missingness risk in P10. The app's existing permission UI remains authoritative and unaffected.

## 3. Route_type evidence policy

Android `AudioManager.getDevices(GET_DEVICES_OUTPUTS)` can identify which output categories are **present**, but cannot reliably prove which output is *actually carrying Media3 audio* on all API29+ OEMs. This is especially sensitive when a Bluetooth headset and built-in speaker are both present. The existing service's route listener is a **safety disconnect signal**, not a selected-route tracker.

**P07 reports `route_type=Unknown`**, which is explicitly allowed by P03's closed RouteType enum, rather than inventing `Bluetooth`, `Wired` or `BuiltIn`. No device names, MACs, endpoints, route token, call metadata or precise location are accessed by this Quality seam. An approved, service-authoritative route selector can improve this in a later scoped phase, but must preserve the safe fallback and require field-QA. P10 must report unknown route coverage, never count unknown as BuiltIn.

## 4. Minimal crash evidence, separate from PlayerError

No external Crashlytics/Sentry/vendor SDK, remote-config, handler that wraps exceptions, automatic breadcrumbs, uncaught-error stack collection, upload, file queue or app-owned exception logs have been installed.

**Safe enum only:** `SafeCrashCategory` allows `JAVA_FATAL`, `NATIVE_FATAL`, `ANR`, `SYSTEM_LOW_MEMORY`, `USER_OR_SYSTEM_TERMINATION`, `NORMAL_EXIT`, `UNKNOWN`. The `SafeCrashEvidenceSink` accepts ONLY this enum. `NoOpCrashEvidenceSink` always returns false. `ConsentGatedCrashEvidenceReporter` can hold at most one enum **in process memory** for explicitly consented diagnostic review and rechecks authorization before any sink call. It has no persistence or identity, and `clear()` discards its RAM evidence on revoke/close.

**Process lifecycle observation:** `MusicCopilotApplication.onCreate` invokes `ApplicationBindings.inspectPriorProcessExitIfApproved()` only on the existing single app composition root. Crucially, the function **returns before even reading OS exit history** if the independent diagnostics consent/retention/Legal/adapter approvals are missing (currently all production approvals FALSE). When explicitly permitted in a future reviewed configuration, an off-main call to `AndroidProcessExitClassification.previousExit()` reads only the **integer `ApplicationExitInfo.reason`** from `ActivityManager.getHistoricalProcessExitReasons(null,0,1)` on Android 11/API30+. Android 10/API29, missing history, unsupported OEM behavior or read failure yield UNKNOWN. The code NEVER reads `ApplicationExitInfo.traceInputStream`, `description`, `processName`, timestamps, PID, full exception, callback breadcrumbs or file paths.

**Reason mapping:** REASON_CRASH -> JAVA_FATAL; REASON_CRASH_NATIVE -> NATIVE_FATAL; REASON_ANR -> ANR; REASON_LOW_MEMORY -> SYSTEM_LOW_MEMORY; REASON_USER_REQUESTED/REASON_SIGNALED -> USER_OR_SYSTEM_TERMINATION; REASON_EXIT_SELF -> NORMAL_EXIT; all other reasons -> UNKNOWN. These are classification candidates for **historical process termination**, not validated Driving crashes. For example, a low-memory kill **is not a JVM exception**, and a NORMAL_EXIT **does not prove a particular Driving session was crash-free**.

**Honest product boundary:** There is **no P07 persistent, consented session marker** to correlate historical OS exit with an active Driving journey; that belongs to P08/P09 under reviewed TTL/deletion policy. We deliberately do NOT install a raw `Thread.UncaughtExceptionHandler`: forwarding Throwable/stack or writing to unapproved persistence would violate the privacy gate. Consequently, P07 cannot already prove true `>=99%` crash-free sessions; unknown/incomplete journeys remain UNKNOWN. A future approved collector must not auto-upload Android's raw crash traces or session/user/device metadata.

## 5. Crash vendor/provider gate — actual approvals required, none fabricated

| Gate | 2026-11-15 status | Effect |
|---|---|---|
| Independent non-audio diagnostics opt-in and accurate UX copy | **NOT APPROVED / UI P12 NOT IMPLEMENTED** | Runtime P04 denies all collection |
| Local storage TTL, cap, delete timing / user clear behavior | **NOT APPROVED / P09** | No persistent Player/crash history |
| Crash vendor, destination, Data Processing Agreement and data residency | **NO PROVIDER / NOT APPROVED** | No external SDK or upload |
| Disable crash SDK automatic collection, throwable/log/stack, breadcrumbs, MediaRef, identifiers | **NO PROVIDER CONFIGURED** | Can't claim any vendor is safe |
| Crash/no-crash session join method, on-device correlation, denominator/provenance | **P08–P10 PENDING** | No crash-free KPI GO |
| Review of permission-revoked error and route unknown coverage | **PRODUCT/PRIVACY/QA REVIEW NEEDED** | Keep current P03 allowlist and honest unknown |
| Actual tests/JVM/device and owner incident replay | **NOT_RUN; P13 source only / P14 owner QA** | No PASS or production readiness |

**Release config stays OFF** in `app/build.gradle.kts`: `QUALITY_EXTERNAL_DIAGNOSTICS_ENABLED=false`, `QUALITY_CRASH_UPLOAD_ENABLED=false`, `QUALITY_VERBOSE_LOGGING_ENABLED=false`. No dependency/Manifest/network permission changes are made. The existing P04 NoOpTelemetrySink and new NoOpCrashEvidenceSink remain installed. The absence of a vendor means no claim about external retention, deletion SLA, encryption keys or dashboard coverage is supportable.

## 6. Concrete P07 repository files

| Repo path | Change |
|---|---|
| `app/src/main/java/com/latticeward/musiccopilot/playback/api/PlaybackRuntimeDiagnosticHost.kt` | Neutral closed-code service -> Application port, no Quality import |
| `app/src/main/java/com/latticeward/musiccopilot/playback/service/ServicePlayerErrorEpisodeGate.kt` | New one-episode-per-typed-code dedup based on existing service error snapshot |
| `app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt` | Minimal non-authoritative callback **after** existing `publishSnapshot`; no audio mutation |
| `app/src/main/java/com/latticeward/musiccopilot/app/MusicCopilotApplication.kt` | Implement typed runtime error host; consent-aware OS prior-exit read at app startup |
| `app/src/main/java/com/latticeward/musiccopilot/app/di/ApplicationBindings.kt` | P04-gated approved `PlayerError` into existing P06 in-memory queue; approved prior-exit observation + RAM-only crash enum / revoke cleanup |
| `app/src/main/java/com/latticeward/musiccopilot/quality/api/SafeCrashEvidence.kt` | Internal-only closed crash category and sink port, no raw/identifier payload |
| `app/src/main/java/com/latticeward/musiccopilot/quality/noop/NoOpCrashEvidenceSink.kt` | Default external/persistent crash sink disabled |
| `app/src/main/java/com/latticeward/musiccopilot/quality/adapters/ConsentGatedCrashEvidenceReporter.kt` | Local in-memory safe-code only; double authorization gate and fail-closed exceptions |
| `app/src/main/java/com/latticeward/musiccopilot/app/platform/crash/AndroidProcessExitClassification.kt` | OS reason code classification for API30+, API29 unknown; never read raw trace |
| `app/src/test/java/com/latticeward/musiccopilot/playback/service/Issue12Phase7ServiceErrorEpisodesTest.kt` | **10 authored but NOT_RUN** source tests: dedup, source, privacy, schema, non-error classes |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue12Phase7CrashPrivacyBoundaryTest.kt` | **10 authored but NOT_RUN** source tests: reasons, denial/withdrawal, throwing provider, NoOp, Android10 unknown |

**Unchanged:** Media3 player owner, queue, service ACK, volume/focus/BT safety, existing player-error recoverForward flow, STT/TTS, six intents, DataStore schema, user MediaStore data, P03 outbound eight-family schema and P04 production approval defaults.

## 7. Source-authored QA requirements — execution intentionally prohibited

- [x] Read user-uploaded original source archive and source code of service, controller, snapshot, Application, P03 and P04.
- [x] Author typed service-authoritative player error seam after committed snapshot, not UI callback/recomposition or arbitrary throwable.
- [x] Author bounded episode dedup; media paths, Bluetooth identities and raw exceptions cannot cross the port.
- [x] Author safe process-exit classification + in-RAM consent-gated crash reporter with default NoOp.
- [x] Author 20 targeted tests, covering error storms, recovery clear, forbidden code, media ref absence, crash vs OS kill, consent revoke, unknown history and provider failure.
- [ ] **NOT_COMPILED, NOT_TESTED, NOT_LINTED, NOT_RUN_ON_DEVICE**, no Gradle, CI/Actions, emulator, APK, PR or merge.
- [ ] No human-reviewed crash SDK settings/retention/consent/provider approvals; no real telemetry active. **No crash-free metric verified**.
- [ ] No incomplete/process killed Driving journey may be marked `session_completed` (P08), or `CRASH_FREE` without corresponding authoritative evidence (P10).
- [ ] No Issue #12 AC can yet be marked PASS; fourteen remain NOT_VERIFIED until owner QA P14 and required signoffs.

**Next phase:** P08, Driving session lifecycle and exactly-once completion with truthful incomplete/UNKNOWN recovery. Issue #12 stays OPEN. Open quality follow-ups #80 and #82 remain independent.
