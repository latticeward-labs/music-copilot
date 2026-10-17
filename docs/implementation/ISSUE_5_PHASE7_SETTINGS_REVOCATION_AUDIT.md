# Issue #5 / Phase 7 — OS Settings return & media permission revocation

Status: **IMPLEMENTED — verification pending**. Never call P07 PASS without real exact-head green JVM / native-Settings evidence.

## Authority and scope

- User-supplied MSM2 RAR (SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`) contains approved Phase 1 UF-02 permission denial and Settings recovery, and UF-03 revoked-library reconciliation. Permission result must come from Android, not the app.
- `docs/implementation/ISSUE_5_PHASED_EXECUTION_PLAN.md` P07 contract, #17 WF-03 / HF-03 recovery (no extra navigation destination), #16 architecture (Media3 service owns playback; local-first Room/MediaStore).
- Not in scope: P08 complete Library Readiness, P09 Microphone UX, P10 Notification/cloud-STT, P11 total process lifecycle, P12 formal accessibility, P13 physical OEM testing, P14 final Issue acceptance/PR merge.

## Implemented behavior and fail-closed guards

1. The actual Android app-info Settings screen is opened only by a user's `Open Settings` action, never from `onResume`. Intent launching failure preserves manual `Not now` recovery.
2. The one-shot Settings ticket records **only a non-sensitive handoff/departure flag**, not a grant. Android Activity `onPause` establishes a true departure. `SettingsReturnCoordinator` ignores an unlaunched or premature Resume. On actual return, Android checkSelfPermission and shouldShowRequestPermissionRationale supply current OS truth: **Granted**, **DeniedRequestable**, or **StillSettingsOnly**.
3. Granted only arms the already approved Granted WF-03 `Continue`. No MediaStore scan runs in onResume, Settings Intent return, ActivityResult callback, or ViewModel. The single blocked scan action is consumed once, guarded again by fresh Android grant in MainActivity and ApplicationBindings, then verified inside the MediaStore/Room adapters.
4. A previously granted media permission which disappears while the app was away—or an existing derived-index Ready/Empty/Scanning projection when OS access is lost—triggers immediate `LibraryRepository.invalidateMediaAccess()`. Room projection becomes `Unavailable` and count becomes 0. The in-flight scan's generation/OS checks reject stale Ready publication; retained derived rows are NOT treated as playable. UI returns to existing WF-03 permission rationale/manual recovery. A cold start following platform process kill also relies on fresh OS grant.
5. Microphone loss detected during foreground return cancels any current voice session with `VoiceCancelReason.PermissionLost`; no microphone re-request or cloud STT is created in P07. Playback's service/queue authority is not taken over.
6. Back/Not now and replay remain available; no account, upload, sensitive permission values in telemetry, or fabricated Android Settings screen.

## Test matrix / status

- `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue5Phase7SettingsRevocationTest.kt`: startup/no-handoff/no-early return, actual departure, native granted one-shot Continue, unchanged denied/cancelled, re-requestable return, failed Settings launch, SavedStateHandle handoff recreation, revocation and no implicit media scan, plus source assertions. These are *unit* tests, not substitutes for real OS tests.
- Dedicated `.github/workflows/issue-5-phase7-settings.yml`: JVM/Kotlin, Android lint, Detekt, Spotless, APK and native API33 Settings UI runner. The native scripts use Android's real PermissionController and Android AppInfo/permissions screen, capture OS `dumpsys` flags, screenshots/crash log and require explicit tap. Any runner failure remains RED. API29 native Settings-only branch needs additional independent evidence and must not be presumed from API33 results.
- **P07 exact-head CI:** PENDING. Do not mark 13 parent Issue #5 acceptance checkboxes; no PR or merge and Issue remains OPEN.
