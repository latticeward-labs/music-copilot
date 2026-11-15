# Issue #12 — P12 Settings, Independent Consent, Local Clear & Privacy Education

**Date:** 2026-11-15  
**Branch:** `feature/12-privacy-safe-telemetry-measurement`  
**Status:** `SOURCE_IMPLEMENTED_UNCOMPILED_NOT_RUN`. All changes below are committed source, NOT a compiled APK, runtime demonstration, verified Android accessibility result or accepted Product/Privacy signoff. **No Gradle, test invocation, GitHub Actions, simulator, APK, PR or merge executed.**

## 1. Exact upstream sources actually inspected

- Original user-uploaded **`MSM2(1)(1).rar`**, SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`. Opened the archive entries for all 12 original English PDFs and reread pertinent printed pages; the old extracted files were checked against the exact identical archive hash. **PRD v1 EN Clarified printed p.10**: only necessary diagnostics, control of history/diagnostic deletion, no default raw audio, and **separate** cloud STT destination/retention consent. **MVP v1 EN printed pp.6,9–11**: Settings (Data, Bluetooth, Privacy), non-audio diagnostics consent, user control and Privacy Copy approval as a GO dependency. **ADR-0003 revised pp.2–4**, **ADR-0004/0006**, **System Architecture** and **Technical Blueprint pp.34–37**: MediaStore is user-owned, Room index is derived, DataStore preferences are separate, Likes/short history are app-owned only if concretely implemented, Media3 service owns the live queue, diagnostics are non-authoritative; blueprint interface samples are *illustrative*, **not existing code**.
- **Phase-0 User Flow EN pp.10–11 and 19–20**: Settings outside active Driving; explicit permission/consent return; exact data ownership; TS-10 retention/history clear semantics **OPEN**, do not invent a duration or repository.
- Current live branch `docs/user-flows/UF-12_SETTINGS_PRIVACY.md` (P0): distinguish Cloud Speech Consent, OS permission and Diagnostics Consent; selected data categories start unchecked, the confirm dialog lists what is deleted and what is preserved; Back/Cancel does **not** erase; show real local/provider success/partial/failure/retry; preserve user-owned media, Likes, history not selected, settings, preference consents, live queue and playback.
- Current reviewed `docs/wireframes/app-states/WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION.md`: CE-23/CE-24: explicit selected categories → warning → confirm / cancel; 48dp touch, TalkBack headings and text status, no unapproved provider deletion claims.
- Current Issue #12 P04/P07/P09/P10 source: `DiagnosticsCollectionAuthorization` four independent approvals **FALSE** in production, `NoOpTelemetrySink` selected for both debug/release, bounded RAM-only diagnostic adapter, epoch-safe P09 queue/drain/clear, optional in-memory crash evidence and quality histograms. P11 is an **offline Product/QA aggregate report generator**, NOT an SDK or in-app analytics backend.

**Source distinction:** No original source grants an exact retention TTL, an online quality provider, its deletion SLA, a notice approval, or actual `short_history` database. We **do not invent** any of these to force a green P12 status.

## 2. Settings UX delivered — truthful non-audio privacy and independent Cloud Speech

Existing `AppNavHost` Settings route now contains a separate `PrivacyDiagnosticsSettingsSection` within the existing scrollable, headings-accessible `FirstLaunchBoundary`. It inherits prior navigation protection: Driving Mode's active one-layer surface does not expose Settings, clearing or consent controls.

Visible explanations (all in Android strings and fixed, nonpersonal copy):
1. On-device speech and **raw audio/transcripts never stored as diagnostics**; Microphone permission is an OS status and separate from user diagnostic choice.
2. Eight approved **coarse non-audio diagnostic families**, prohibited private artists/song titles/contacts, and why Media3/Voice operation never depends on quality collection.
3. Production **diagnostics OFF**: no analytics vendor, no cloud export, no user tracking, no permanent event store; app-scoped optional RAM collector remains source-disabled and P04 privacy-copy/retention/adapter/legal review flags default FALSE.
4. Current `DiagnosticsConsent` `NotAsked/Declined/Granted/Revoked` is read from actual DataStore state and displayed as a **decision** — even old `Granted` CANNOT authorize collection under the pending P04 gates. If DataStore is loading/failed, show **storage not ready**; no false successful save.
5. **No "Enable diagnostics" control** while final approved notice, exact positive retention and Legal/provider configuration are absent. Do not auto-grant from mic permission, initial onboarding, Player taps, cloud preference, route changes or opening Settings.
6. **Cloud STT separate:** existing provider is disabled, actual endpoint/purpose/retention are not approved. Its previous saved consent value is separate; existing Cloud consent Revoke button now has an app-scoped asynchronous DataStore `AppResult` acknowledgement and explicit `Saved` / `Retry on failure` status, not a false "external data deleted" confirmation.
7. **Just-in-time microphone notice:** existing Player/Driving `VoiceSessionPanel` permission rationale (before the Android permission continuation button) now says Microphone optional, on-device, no app-saved recordings, diagnostics OFF, Cancel retains manual music. This is an extra explanatory Text; it never starts STT or emits a telemetry event.

All UI text is in `res/values/strings.xml`, no plaintext personal voice/media/session data is rendered or logged.

## 3. WF-10 Delete — selection, confirmation, actual source ownership, real outcomes

### Current repository ownership audit

- **Diagnostic data**: real P09 `ApplicationBindings.clearLocalDiagnostics(): Boolean`. It synchronously erases the RAM `BoundedMemoryDiagnosticsStore`, current event-family counts, 64-queue backlog with epoch rejection, crash evidence, manual correction ticket, ephemeral Driving ticket and P10 latency histograms; idempotent and no remote provider. `true` means local in-process clear completed; `false` means **not proven** and user sees Retry. This does NOT change consent, playback, library or Cloud STT.
- **Short playback history**: no app-owned short-history entity, DAO, HistoryRepository or active storage record exists in current repo. `Room MusicCopilotDatabase` contains **only rebuildable `LibraryRow`**; `RestorableQueueStore` is **a queue/position restoration store**, and **must never be erased under a false history label**. `AppPreferences` has no shortHistory key. Therefore **P12 displays Short history as not stored in this version, checkbox disabled**; `ApplicationBindings.clearSelectedAppData(shortHistory=true)` fails closed with per-category `UNAVAILABLE`, not `CLEARED`. Source `Technical Blueprint` suggests a future short-history record but is explicitly illustrative; this is an open **Issue #16/Data Owner, Product/Privacy retention design blocker**, not a missing private data item that can safely be deleted from another table.
- **Likes, MediaStore media files, Room derived library, selected Bluetooth, DataStore settings and consent, live playback queue, Media3 Session/Service** are **protected categories**, never displayed as diagnostics deletion targets. No data schema, DAO, migration, media deletion API or playback command is added.

### Delivered confirmation/result sequence

1. Settings shows separate category rows. **Both unchecked by default**. The absent Short history category is visibly disabled/Not Stored. The local diagnostics category can be explicitly selected using one full-width text-labelled checkbox/toggleable Row (48dp minimum).
2. User taps `Review selected data`. The explicit `AlertDialog` names precisely selected local diagnostics, lists **all preserved categories**, warns that local memory clear is NOT consent revocation, and explains that **NO remote provider exists and NO remote deletion was requested or verified**.
3. `Cancel`, dismiss, Back, route change or unselected category do not call deletion. Only `Clear selected local data` inside the confirmation dialog submits a typed `LocalPrivacyClearSelection`.
4. The existing app-scoped `repositoryScope` owns the accepted clear; Compose shows `Clearing` and receives a per-category `LocalPrivacyClearReport` on `dispatchers.main`. `LocalClearOverall.CLEARED`, `PARTIALLY_CLEARED`, `FAILED` or `NOTHING_SELECTED` are derived from actual category acknowledgements, not UI transition. A failed/partial operation offers **Retry**.
5. Success unchecks the diagnostics checkbox; user may select it again later. It **does not revoke future consent**, nor can it claim remote erase. For current implementation, no remote category/provider exists; the `ExternalDiagnosticsDeletion.NOT_CONFIGURED` contract is separate from local success.

### Separate Diagnostics Consent Revocation

1. User taps `Revoke diagnostics consent and erase local diagnostics`, then explicitly confirms a second `AlertDialog`. The copy explains independent cloud speech, mic and playback preservation.
2. `ApplicationBindings.revokeDiagnosticsConsentWithResult` **synchronously** invokes P04 `requestImmediateRevocation()` (collection is denied BEFORE any async work) and `clearLocalDiagnostics()` (queue/epoch/persistent-free RAM scrub) on the actual app graph.
3. The **existing repositoryScope**, independent of Compose lifetime, writes `PreferenceUpdate.SetDiagnosticsConsent(DiagnosticsConsent.Revoked)` to the real DataStore. Awaited `AppResult.Success` is the sole basis for `persisted=true` — a queued coroutine is NOT proof of persistence. Exceptions other than cancellation are failure; no raw details reach UI.
4. On the main dispatcher, UI receives `DiagnosticsRevocationReport(localDiagnosticsCleared,persisted)`. **Both true** => fully completed; otherwise show **partial/failure and Retry**, never a misleading green Saved. The P04 denial latch remains active after a failed write for that process; with P04 production approvals false, a future process remains collection-disabled too. UI cannot change OS mic permission or cloud speech decision.
5. Duplicate clicks while an operation is in progress are disabled. App navigation/recomposition cannot start silent revocations; no automatic consent selection.

### Cloud Speech Consent Revocation

Cloud STT retains its own `CloudSpeechConsent`, and is not a diagnostic collection permission. In P12 the existing Cloud Revoke action is redirected to `revokeCloudSpeechConsentWithResult(callback)`, which performs the existing separate DataStore `SetCloudSpeechConsent(Revoked)` and reports success **only after actual `AppResult.Success`**; failure shows a Retryable message. This does not change `DiagnosticsConsent` or start a cloud service. The existing deprecated void convenience method remains for source compatibility with older call sites, but P12 UI uses the verified-result method.

## 4. Accessibility, Driving and failure constraints

- Uses existing scrollable `FirstLaunchBoundary`, Material 3 `AlertDialog`, explicit readable headings with `semantics { heading() }`, textual selection labels and status, minimum 48dp button/checkbox targets. No color-only success/failure states; screen description/checkbox order should be checked with TalkBack and 200% font scaling in **P14 on owner's device**, not falsely asserted in P12.
- **Not inside Driving:** no Settings destination from active Driving Mode; cancellation/back remains optional-safe and manual controls always available. The additional microphone copy appears only when the user explicitly encounters the mic rationale, not on each Listening/retry frame.
- Settings result callbacks never call Media3/PlaybackGateway/MediaStore/Room, do not start a Voice/STT session, do not request permissions or use remote analytics. Quality worker failure/revocation cannot interrupt service-owned playback.
- P12 does **NOT** implement any external provider-specific delete/status/retention code: that requires a new reviewed external adapter + notice + documented processor SLA, and a truthful independent remote deletion state with confirmation/error/timeout. Reporting local RAM clear as `external erased` is prohibited.
- The current unapproved product state remains an explicit blocker to opt-in grant. Showing a disabled explanation instead of a dangerous selectable Grant toggle is a safe **engineering interpretation**, not a hidden Product approval.

## 5. Changed source files and QA authored only

| Source | P12 change |
|---|---|
| `core/application/privacy/LocalPrivacyClearContract.kt` | Closed clear selection, per-category outcomes, external provider state, overall status and history-availability contract |
| `app/di/ApplicationBindings.kt` | App-scoped selected clear + denial-first Diagnostics revoke + awaited DataStore acknowledgement; separate acknowledged Cloud revoke |
| `feature/settings/privacy/PrivacyDiagnosticsSettingsSection.kt` | UF-12 / WF-10 Privacy text; off-by-default selection, explicit confirm/cancel, disabled short history, local deletion/revoke results and retry |
| `app/navigation/AppNavHost.kt` | Embeds privacy section into existing Settings route; passes existing DataStore/mic truth; Cloud consent result/retry |
| `app/MainActivity.kt` | Connects Settings actions to the single app-scoped owner; no duplicate repository/Media3 owner |
| `feature/voice/VoiceSessionPanel.kt` | Extra just-in-time privacy copy on mic rationale only |
| `res/values/strings.xml` | Accessibility/consent/retention/clear status labels, no invented TTL |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue12Phase12PrivacyClearContractTest.kt` | **14** pure policy/test-methods authored only |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue12Phase12PrivacySettingsWiringTest.kt` | **16** source/UF-12/ownership/consent/wiring/a11y method contracts authored only |
| `docs/implementation/ISSUE_12_P12_SETTINGS_PRIVACY_DELETION.md` | This owner/provenance/consent/delete/missingness report |
| `docs/implementation/ISSUE_12_PHASE_LEDGER.json` | Source status and unresolved Product/Privacy/QA signoffs |

**30 P12 QA methods authored / ZERO executed**. Actual Android 10–15 device, TalkBack, screen rotation, interrupted Settings/DataStore write, old pending queue vs clear/revoke race, explicitly denied mic, Cloud STT separate state, active playback continuity and OS Settings return must be verified ONLY in P14 by the owner.

## 6. Consent, privacy, retention and vendor sign-off register

| Approval or source decision | Current status | Runtime effect |
|---|---|---|
| Diagnostics privacy notice exact signed copy/version | **NOT APPROVED** (P04) | No Enable button; collection OFF |
| Positive persistent diagnostic retention duration/TTL | **NOT APPROVED** (Phase-0 TS-10) | No permanent event database; RAM mode source-only and disabled |
| Analytics adapter/vendor/transport, DPA/data residence | **NOT SELECTED / NOT REVIEWED** | No external events/uploads and no remote deletion action |
| Legal approval for collection | **NOT APPROVED** | P04 `canCollect()` defaults FALSE |
| Short history actual data owner/storage & retention | **NOT IMPLEMENTED / NOT APPROVED** | History shown unavailable; deleting queue/library is forbidden |
| Cloud STT processing destination/retention/consent copy | **NOT APPROVED / provider disabled** | No Cloud voice; separate local revocation preference only |
| Independent P12 Android accessibility, permission and deletion QA | **NOT TESTED** | P14 device acceptance required |
| P13 full authored QA package and final P14 integrated build | **NOT STARTED** | Issue #12 cannot yet be closed |
| 14 Issue #12 acceptance criteria | **NOT_VERIFIED** | No PASS, merge or release GO |

**Phase exit disposition:** The concrete P12 Settings privacy/source-of-truth wiring, diagnostic clear and consent revocation are written; the absent short-history owner and unapproved retention/provider/copy are honestly blocked. This is `SOURCE_IMPLEMENTED_UNCOMPILED_NOT_RUN`, not a fully approved Privacy/QA PASS. Next P13 source-only review and complete owner mobile QA checklist, then one P14 integrated APK/owner phone verification under the project rules.
