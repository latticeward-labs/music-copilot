# Issue #5 / Phase 11 — Lifecycle, persistence and privacy audit

**Date:** 18 October 2026  
**Branch:** `feature/5-onboarding-permissions-recovery`  
**Controlled source:** user's uploaded `ISSUE_5_PHASED_EXECUTION_PLAN(1).md` § Phase 11; MSM2 PRD/MVP, ADR-0002/0003/0004/0006 and UF-01/02/03 first-readiness source; approved repository architecture and Issue #5 P00–P10 contracts.

## What P11 requires and where it is implemented

| Source contract | Implementation | Truth / recovery |
|---|---|---|
| Rotation, font/theme, Home/foreground, process death, OS Settings return preserve safe UI state | `MainActivity.kt` native pending ticket and safe `p11_voice_practice_stage` enum; `PermissionViewModel.kt` SavedStateHandle for denied/settings-only presentation; `FirstUseViewModel.kt` read-only replay page | Android OS permission is freshly checked on Resume and ActivityResult; Android ActivityResultRegistry restores pending result, **never re-launches** by lifecycle |
| Native media request one-shot | Existing P05 `MediaRequestGate` / `PermissionMachine`, P06 `PermissionViewModel`, P07 Settings return | Only explicit user Continue authorizes scan; no persisted granted status; a killed process loses stale one-use scan authorization |
| Voice Practice rationale and pending Android request | `VoicePracticeGate.restorePresentation()`, `restorePending()`, `MainActivity.onSaveInstanceState` | Safe rationale/denial/provider feedback restored; ReadyForVoiceHandoff is intentionally **not** restored; pending Android ticket is reattached, never relaunched |
| Onboarding persisted ACK vs replay | `FirstUseMachine`, `FirstUseViewModel.initialState` | Normal first run resumed from ACKed DataStore checkpoint; replay is read-only and its page may restore via SavedStateHandle; no request/scan/player action on initialization |
| State ownership | `ApplicationBindings`, `AppNavHost`, `MainActivity` | Media3 MediaSessionService/PlaybackGateway owns player and queue; VoiceSessionCoordinator owns real voice capture; UI only observes and displays; bootstrap/onResume do not scan or start voice |
| Typed recoverable errors and cancellation | `DataStorePreferencesRepository`, `RoomLibraryRepository`, P11 `DataBindings.DataComponents.verifyStorage`, `BestEffortQualityEventPublisher` | CancellationException is rethrown; other data/storage failures are projected as safe state / typed error, never as raw exception text |
| Data minimization and telemetry fail-closed | `AllowlistedTelemetryPrivacyGate`, `TelemetryEvent`, `NoOpTelemetrySink`, `PreferencesDataStore` | No raw audio, transcript, song title/artist/path/URI, device/Bluetooth identifier, location, or permission result forwarded as free-form telemetry; deny unknown event strings |
| No hidden internet or remote STT | `AndroidManifest.xml`, `VoiceBindings`, P10 `CloudSpeechConsentPolicy` | No INTERNET or POST_NOTIFICATIONS permission; no remote provider approved, no Cloud STT execution; existing consent can be revoked locally |

## Process restoration and safety rules

1. UI-only preference checkpoints are never grants. `SavedStateHandle` stores denied/settings-only display outcomes, attempted flag and UI-only explanation; it does not store the one-shot `scanAuthorized` state. The OS is authoritative.
2. After a killed process, a previously shown `Granted` UI projection restores to the **safe rationale** rather than a stale Continue that could try a forbidden scan. A new explicit Continue and fresh OS check are required to restore the one-use scan authorization.
3. A fresh foreground Android grant invalidates a stale restored denial/Settings-only screen when there is no active Settings ticket; this only clears UI, **never** auto-scans or marks a blocked action authorized.
4. A previously offered Settings-only recovery screen restores as a **non-authoritative presentation**. Settings return classification needs a prior explicit Settings tap, observed activity departure and fresh Android permission observation, then a single explicit Continue. Duplicate return is ignored.
5. A native microphone request interrupted by Activity recreation restores only its pending Android Result ticket. A saved `ReadyForVoiceHandoff` is discarded; rationale restores as a non-launching screen. Neither rotation nor resume starts actual capture or remote STT.
6. Normal onboarding completion/checkpoint remains **DataStore ACK-owned**. A read-only two-page Education Replay may restore its page without DataStore mutation. No UI owns Media3 playback/queue or persistent VoiceSession.
7. When Android revokes media access, `onMediaAccessRevoked()` invalidates the derived library projection and cancels active scan. No stale Ready/Playing can be synthesized from a SavedStateHandle.
8. CancellationException is propagated from blocking storage validation and diagnostic publishing. Raw exception text is not logged/shown; errors use the existing recoverable state and typed `AppError` contracts.

## Evidence and testing (never infer results)

**Authored test suite:** `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue5Phase11LifecyclePrivacyTest.kt`, including restore-denial, stale-denial fresh-OS reconciliation, post-process one-use authorization, Settings return, manual cancel, Voice rationale/pending/unknown restored stage, first-run DataStore checkpoint, read-only Replay restoration, negative synthetic telemetry payloads, static no-auto-launch and cancellation guards. P07 historical restoration expectation was updated to reflect safer P11 denied/settings screen projection without persisting a grant.

**Existing regression:** P07/P09 permission/Voice native and JVM suites, P08 scan and root architecture checks. These were previously evidenced per phase but are **NOT re-executed by P11 documentation or APK-only build**.

**Execution status:** P11 tests **AUTHORED / NOT RUN** under user's current low-cost APK-only instruction; static source/privacy review **DOCUMENTED / no independent approval**. No Android emulator or phone actions performed by assistant. Building an APK verifies compilation, **not** native lifecycle behavior or all P11 exit criteria.

## Phone QA and decision

[Owner's physical phone checklist](ISSUE_5_PHASE11_PHONE_QA_CHECKLIST.md) requires the owner to install the exact new P11 APK and return each test ID with PASS/FAIL/NOT RUN. Do not infer undocumented conditional substeps from a blanket PASS. Record owner self-report separately from actual logs/screenshots.

**Current P11 acceptance:** `IMPLEMENTED_PENDING_APK_BUILD_AND_OWNER_PHONE_QA_AND_TECHNICAL_GATES` until CI status and owner's report are recorded. A future APK-only SUCCESS and 11/11 reported phone PASS would **not** prove required JVM, negative policy regressions, automated access, cross-API matrix or P12/P13/P14 governance automatically. Issue #5 must remain OPEN; no merge and no reset of existing P10 outstanding technical gates.

## Remaining gates

- Exact-source Gradle compilation / debug APK: pending at initial authoring.
- Owner physical phone P11 manual tests: pending at initial authoring.
- P11 local Kotlin/JVM lifecycle/privacy tests: authored, **NOT RUN**.
- Privacy/architecture static checks and relevant regressions on final code: **NOT RUN** here.
- API29/31/33/35+ native matrix, Compose automated accessibility/permission focus: P12/P13, not substituted by a single device.
- Issue #5 original 13 integrated acceptance criteria / review / final merge: P14, not waived.
