# Issue #8 — P12 Cumulative Source Integration, Deterministic Test Sources and P13 Gate

**Recorded:** 2026-10-24 · **Issue:** [#8](https://github.com/latticeward-labs/music-copilot/issues/8) **OPEN**  
**Branch:** `feature/8-audio-focus-route-recovery` · **Target:** `main`  
**P12 disposition:** **PASS_SOURCE_ONLY_NOT_COMPILED_OR_TESTED**. **P13 NEXT — the only APK build phase.**

> P12 is a source/edit/documentation phase only. No app build, Gradle sync, JUnit tests, instrumented tests, lint, emulator, physical phone, CI/Actions, APK, PR/merge or Issue closure has been requested or performed. All test results below are NOT_RUN. A source file with an @Test method is NOT test execution, and code inspection does NOT prove compiler/type correctness.

## 1. Inputs and full upstream integration boundary

- **Actually rechecked current uploaded archive:** `MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, 12 original source PDFs under the MSM archive directory: ADR-0001–0006, Music Copilot PRD, MVP, architecture, code/technical documentation, User Flow Phase 0 and Phase 1. An archive directory entry is not a thirteenth PDF.
- **GitHub baseline at P12 start:** `main` was 0 commits ahead of the Issue #8 source branch; `feature/8-audio-focus-route-recovery` was 112 commits ahead / 0 behind main, with 37 changed paths including planned documentation, all source phases P02–P11. No P12 work performed on `main`.
- Reviewed and retained original Issue #7 sole service-owned `MusicPlaybackService`, `ServicePlaybackRuntimeOwner`, ExoPlayer, MediaSession, local MediaStore, `PlaybackCommandProcessor`, `ServiceQueueEngine`, `PlaybackWireProtocol`, `ServicePausedCheckpoint`, Media3 notification and controller. P01 frozen execution contract unchanged.
- **P02** pure `InterruptionReducer` + four guarded transient-focus resume conditions / manual route epochs.
- **P03–P04** Media3 actual focus/suppression observation and conservative call/focus auto-resume veto (no second `requestAudioFocus`).
- **P05–P06** headsets, USB and Bluetooth removal/noisy first-loss dedup; AVRCP auto-Play quarantine with two-stage external Play guard.
- **P07** receipt-time route/safety/command epochs, urgent Main-thread Pause/Stop, stale queued IO rejection.
- **P08** same Media3 Player/Snapshot/Controller revision authority; Full/Mini UI projection. Driving is still a placeholder, NOT claimed implemented.
- **P09** typed cancellation effects via existing ApplicationBindings `VoiceSessionCoordinator`; STT/TTS separate safe teardown. No whole-call telephony identity claim.
- **P10** three privacy-safe `AppError.Interruption` codes and explicit recovery/UI guidance.
- **P11** Android API29–36 manifest/FGS and API31 BLE classification guards; TalkBack/short UI messages; Honor/Pixel/Samsung/Xiaomi + three parked vehicles matrix **planned, NOT_RUN**. Android Auto remains explicitly outside ADR-0006 MVP.

## 2. P12 source integration defects identified and fixed

### 2.1 Preserve typed P10 interruption notice across ordinary commands

**File:** [`MusicPlaybackService.kt`](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt)

- Observed issue in the actual P10 source: generic command completion executed `if (command !is PlaybackCommand.ReplaceQueue) runtimeError = null` even when `runtimeError is AppError.Interruption`. This could erase an active focus/route safe-pause notice merely because a user pressed **Pause**, **Stop**, **Seek**, **Next**, or a selection stayed Paused.
- P12 preserves interruption notice on non-recovery commands. It is cleared **only** when an explicit accepted playback-intent command (`Play`, playable `SelectQueueIndex`, or `ReplaceQueue`) has an available P02 focus state, unlocked route gate and Media3 `playWhenReady`. The P02/P10 accepted actual Playing observation may also clear only the interruption notice; unrelated critical permission/media errors continue to have independent priority.
- A new paused queue no longer silently dismisses a prior unresolved focus/route interruption. This is a **source-level correction**, not proof of sound output, Audio Focus grant or actual headset routing.

### 2.2 Reconcile legacy Issue #7 test expectations with superseding P05/P07 safety model

- [`Issue7Phase9RouteFocusGuardTest.kt`](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue7Phase9RouteFocusGuardTest.kt) used to assume a duplicate `onHeadsetOrBluetoothLost` increments route generation twice. **P05** correctly treats noisy+device removal as one physical loss episode. Source test now expects one generation until explicit verified re-arming. BLE assertions now check API31 compatibility instead of assuming Android10/11 supports API31-only categories.
- [`Issue7Phase12IntegratedBoundaryContractTest.kt`](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue7Phase12IntegratedBoundaryContractTest.kt) used to expect `++restoreGeneration` at **dispatch** and the old positional `ServiceAudioRouteMonitor(this)`. **P07** now intentionally stamps generation at receipt, and P05/P06 use named callback wiring. Source-contract assertions updated, without modifying the original merged Issue #7 implementation.
- These legacy tests were **edited, not executed**. Previous PASS/FAIL (if any) does not transfer to Issue #8.

## 3. New authored deterministic regression source: 28 tests, 0 run

| Source file | Test methods | Source behaviors specified, NOT runtime evidence |
|---|---:|---|
| [`Issue8Phase12InterruptionReducerTest.kt`](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue8Phase12InterruptionReducerTest.kt) | **14** | Transient-focus candidate captured only from prior real Playing, pending four-guard gain, manual Pause precedence, reconnect with route invalidation, duplicate noisy/removed callbacks, permanent focus failure, stale gain episode, ambiguous external Play refusal, exact receipt route epoch, unauthorized audible observation safe pause, Voice cancellation on denied focus return, duck without extra Play, duplicate/out-of-order sequence and invalid elapsed time |
| [`Issue8Phase12ReceiptOrderingTest.kt`](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue8Phase12ReceiptOrderingTest.kt) | **5** | Main receipt-time urgent Pause preempts stuck IO, queued old Play rejection, exact duplicate ID receipt idempotency, stale route epoch for queued AVRCP Play, fail-closed invalid admission, same-ID different-payload safety |
| [`Issue8Phase12NoticeAndOwnershipTest.kt`](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue8Phase12NoticeAndOwnershipTest.kt) | **9** | Static code-aware and pure tests for `ROUTE_INVALIDATED`, `FOCUS_NOT_RECOVERED`, `CALL_OR_ROUTE_SAFE_PAUSED` recovery hints, duplicate notice suppression, no identifying diagnostics, one service ExoPlayer/MediaSession/Audio Focus/notification, real typed Voice cancellation, receipt-time and interruption warning retention, API31 BLE guard, manifest FGS permissions and P11 OEM checklist |

Tests use the repository's existing JUnit and `kotlinx-coroutines-test`/coroutines dependencies. New source does **not** create a second ExoPlayer or request Audio Focus. A local dummy MediaRef in pure tests is not a real user's song. Source-level tests are explicitly not counted toward any AC's PASS.

**Known limitations of tests:** The `Issue8Phase12ReceiptOrderingTest` simulates receiver stamps and suspended work with an injected fake; it cannot prove actual Player.Looper/SpeechRecognizer/Android notification semantics. Pure reducer tests do not prove a Media3 `AUDIO_FOCUS_LOSS` callback fires for every real phone call. Source tests do not replace instrumented tests or physical output-route no-leak listening.

## 4. Full T01–T15 evidence and implementation mapping

| Technical ID | Authored/related P12 source | Honest next execution requirement |
|---|---|---|
| T01 | Reducer test: transient/permanent focus, duck, stale event sequence | JVM test execution, not P12 |
| T02 | Reducer test: pending eligibility, manual revision, route validity/epoch, prior Playing | JVM test execution + actual Media3 policy field check |
| T03 | Reducer test: user Pause/Stop and denied auto resume | JVM + device focus interruption |
| T04 | Ownership source test + original Media3 `setAudioAttributes(..., true)` | Verified compile and Android 15 FGS/focus hardware |
| T05 | Existing Issue7 P09 route tests + new reducer route cancel | JVM + headset/USB/BT unplug speaker-leak QA |
| T06 | New reducer duplicate route + command-receipt test | JVM + rapid OEM callbacks |
| T07 | Receipt-order fake suspended IO test, unchanged `ServiceQueueEngine` after-await checks | JVM + long MediaStore / queue device |
| T08 | Reducer combined focus/route/manual policy + source ownership | JVM + physical overlap event order |
| T09 | Existing `Issue7Phase6ControllerSnapshotParityTest`, single-session source test | Controller/notification runtime validation |
| T10 | Voice seam source test + P09 STT/TTS lifecycle code | Actual runtime Voice capability; Issue #10 dependency |
| T11 | New pure `InterruptionNoticePolicy` code/hint tests | JVM/source lint and privacy review |
| T12 | P11 static API guard, FGS manifest tests | Real API29/31/33/35/36 compiles/handset data |
| T13 | P11 OEM/vehicle matrix and owner checklist | **Only** actual independent OEM/parked vehicle observations count |
| T14 | **No APK source or build artifact in P12** | P13 GitHub Actions build-only provenance SHA/APK hash/job |
| T15 | P01 acceptance ↔ file trace + P12 cumulative audit | Independent actual T/M/AC evidence and later review/merge decision |

**ALL T01–T15 NOT_RUN.** Test source methods are not correlated to GitHub Actions minutes or automatic CI PASS because all P12 commits carry `[skip ci]`.

## 5. Owner M01–M45 handoff and safety

[**Original and now expanded final owner checklist**](ISSUE_8_FINAL_OWNER_PHONE_QA_CHECKLIST.md) retains **every** M01–M45 step, expected observation and per-ID PASS/FAIL/BLOCKED/NOT_RUN/N/A status. Important representative scenarios: M06–M17 phone/competing focus, M18–M25 wired/USB, M26–M35 Bluetooth/AVRCP, M36–M39 unified UI/privacy, M40–M43 rapid focus/route commands and M44–M45 Voice (BLOCKED if feature is not exposed). User phone is Honor x7d Android 15 if unchanged; no OS/device identity inferred from upload alone. Android Auto, a physically available second handset, Pixel/Samsung/Xiaomi and three parked vehicles are **separately deferred**. Other Issue #4/#6 handset PASS statuses must not be transplanted to Issue #8.

**Safety rule:** A speaker leak after personal output disconnect is a FAIL and stop-now, not a reason to keep replaying loud audio. No vehicle testing while driving. Do not collect phone numbers, transcript, raw recordings, route MAC or personal song names.

## 6. Final P13 build-only handoff; do not initiate now

1. **Freeze actual Issue #8 P12 Branch HEAD** as the only source of the cumulative build, not intermediate phase branches or only P12 files. Check target `main` base and fresh compare.
2. **Use a single cumulative `:app:assembleDebug` operation** through a deliberately build-only GitHub Actions job (or equivalent approved builder), with all unit/instrumentation/lint tasks skipped and no tests executed. A Kotlin compile in **P13** is expected; P12 has done **none**.
3. Output the real debug APK (normally `app/build/outputs/apk/debug/app-debug.apk` but **do not assert path until build confirms it**); upload it as an artifact and record Actions run URL, head SHA, artifact name/size, SHA-256 checksum and compile result. Never invent download URLs or hashes; if the first build fails, correct the source **in P13**, then retry the integrated build. A single **final APK** means one owner-facing candidate after integration, not one APK per earlier phase.
4. Deliver one owner-friendly download path and the complete M01–M45 checklist. User installs/tests on phone and reports actual PASS/FAIL/BLOCKED/N/A. Assistant records provided results into [QA ledger](ISSUE_8_QA_RESULTS.json) with exact APK/source/device provenance.
5. T14 may eventually record **build-only evidence**, but T01–T13 and T15 remain NOT_RUN unless independently tested. No Issue close, PR merge or 12/12 AC PASS until corresponding real evidence/approval.

### P12 source-only checks completed

- [x] Confirmed exact original attachment hash and 12 PDF inputs.
- [x] Read GitHub Issue #8, branch, plan, approved source/file ownership and existing M/T/AC ledger.
- [x] Source-audited single Media3 Player/Session, focus reducer, route/AVRCP, command epoch, Voice, three interruption codes and Android API guards.
- [x] Fixed P10 lost interruption message after unrelated commands.
- [x] Updated two conflicting historical Issue #7 test sources rather than weakening #8 policy.
- [x] Authored 28 new deterministic JVM/static test methods across three actual files in `app/src/test`.
- [x] Preserved 45 M, 15 T and 12 AC provenance/status; P13 APK remains NOT_BUILT.
- [x] Prepared P13 cumulative build-only handoff and stable owner test report template.
- [x] No compilation, Gradle, test, Actions, APK, PR/merge or Issue close.
- [ ] P13 successful cumulative build, actual checksum and phone observations.
- [ ] Independently executed technical tests, additional OEM/vehicle QA and any remaining Driving/Voice scope beyond Issue #8.

**P12: PASS_SOURCE_ONLY_NOT_COMPILED_OR_TESTED. NEXT P13 ONE CUMULATIVE APK.**
