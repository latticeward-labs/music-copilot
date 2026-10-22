# Issue #6 — P11 Quality hardening and final integrated test readiness

**Date:** 22 October 2026 · **Branch:** `feature/6-mediastore-library-browse-search` · **Issue #6:** OPEN  
**Result:** `PASS_SCOPED_SOURCE_AND_TEST_PREPARED` — production hardening and test/checklist *authored and committed*, **not compiled or executed**. The only owner-facing APK and full test gate remain **P12**.

## 1. Actual source authority and traceability

The owner-provided `/mnt/data/MSM2(1)(1).rar` was verified at SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`. **All 12 contained English PDFs were extracted and read** for P11, including the clarified PRD (12 pages), MVP (11 pages), ADR-0002 MediaSessionService ownership (5 pages), ADR-0003 MediaStore/Room local-first boundaries (6 pages), architecture, code/technical blueprint (54 pages), user flows and remaining accepted ADRs.

Anchors: PRD nonfunctional gate: smooth scroll with 5,000 media files, >=48dp touch targets, scalable font, full TalkBack labels, incrementally indexed offline local media, no personal metadata upload; ADR-0002: Player/queue/MediaSession only inside service; ADR-0003: MediaStore is OS primary media/access truth, Room disposable derived index, no cloud/account/streaming; P11 execution plan `docs/implementation/ISSUE_6_PHASED_EXECUTION_PLAN.md` explicitly forbids per-phase APK. Actual existing Issue #6 ACs, 34 owner-phone IDs M01–M34 and 10 technical IDs T01–T10 were preserved.

## 2. Actual production code hardening

| Path | Source-level fix | Intended invariant |
|---|---|---|
| `feature/library/LibraryBrowseViewModel.kt` | Reset `SelectedSongUiStatus.Idle` whenever a selected song is cleared on invalid grant/revision, category/group changes and Back | Never leave stale Playing/Unavailable feedback attached to cleared selection |
| `feature/search/LocalSearchViewModel.kt` | Same selection-status reset on revoke, query change, revision switch and new query scheduling | Stale query/permission cannot leak old selection status into new Search results |
| `feature/library/LibraryBrowseScreen.kt` | Add `LiveRegionMode.Polite` semantics to dynamic selection feedback, Empty and Error messages | TalkBack can announce recoverable states as text; no reliance on color |
| `feature/search/LocalSearchScreen.kt` | Add polite live semantics to selected-song status, No Results and Search failed headings | Error and zero-result status accessible and announced |
| `core/application/playback/LibraryMediaSelectionUseCase.kt` | Reject a supposedly successful single-ref `ReplaceQueue` acknowledgement if service snapshot queue size is not exactly 1 | A matching first item with stale extra queue members is NOT command acknowledgement; no fabricated Playing |

These changes are on the Issue #6 feature branch only. No new Product backend/network, audio writes, Activity-owned Player, permissions or service queue implementation.

## 3. New authored tests and tooling — **NOT EXECUTED**

- **12 new JVM methods**: `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue6Phase11HardeningContractTest.kt`. Synthetic multi-volume ID collision, malformed ref/traversal, unknown metadata identity, duplicate vs distinct songs, 100-page bound, 60-Search cap, Locale/NFKC normalization, stale-library and revoked-permission feedback.
- **1 added P10 JVM regression method**: `Issue6Phase10SelectionBoundaryTest.kt` now contains **17** methods; new test rejects a returned multi-item stale queue when only one item was requested. None executed.
- **3 native SQLite 5,000-row methods**: `Issue6Phase11FiveThousandRoomBenchmarkTest.kt`: 100 pages × 50 with no duplicate refs, ≤60 grouped Search results, 3 category group pages. Timings exported **numeric-only** from actual instrumentation when P12 executes; no guessed performance numbers. This tests SQLite derived rows, not real OS file scanning or a Compose frame benchmark.
- **2 native MediaStore permission methods**: `Issue6Phase11MediaStorePermissionInstrumentedTest.kt`. Test expected Android 10–12 vs API33+ read grant and fail-closed scan on a truly denied profile. Denied-path test **skips** when permission is already granted; a skip is NOT a PASS.
- **2 added Compose semantics methods**: `Issue6Phase9SearchComposeInstrumentedTest.kt` now contains **9** authored tests; new cases assert polite TalkBack live regions on No Results and Search failed.
- **20 executable static guardrails prepared, not run**: `tools/qa/issue6_static_guardrails.py`. Manifest (no INTERNET/backup/cleartext), private same-UID MediaSession commands, Room schema and destructive-migration policy, readable FD probe, source requery before service dispatch, no UI Player, 48dp/list pagination/semantics, no private metadata logging, and required genuine Room `1.json` and `2.json`. `I6-G20` is known **blocked by missing** `2.json` (actual GitHub fetch 404); do not weaken the guard or fabricate the generated schema.

**Total new/extended source test methods in P11: 20** (12 JVM + 1 JVM P10 + 3 native SQLite + 2 native permission + 2 Compose). All 20 are **AUTHORED / NOT_RUN**. Existing P02–P10 test suites remain scheduled for final execution.

## 4. One final integrated APK and owner checklist

The existing `docs/implementation/ISSUE_6_FINAL_PHONE_QA_CHECKLIST.md` was **updated in place**, not overwritten. It still contains **M01–M34 exactly once** and the **10 technical categories T01–T10** with blank result/evidence fields. The P11 addendum specifies a synthetic dataset recipe, Android API29/31/33/35 matrix where available, tools and commands in order, error-injection, mobile app privacy, generated Room schema gate and exact Source-SHA/APK-SHA preservation. Owner is handed one **final combined installable APK** only in P12 after P00–P11 code is integrated. A defect fix after build invalidates the old candidate; build a clearly identified replacement, not one APK per phase.

To be executed in P12 only from frozen code:
```bash
python3 tools/qa/issue6_static_guardrails.py
./gradlew --no-daemon :app:kaptDebugKotlin :app:compileDebugKotlin :app:testDebugUnitTest :app:lintDebug
./gradlew --no-daemon :app:connectedDebugAndroidTest
./gradlew --no-daemon :app:assembleDebug
sha256sum app/build/outputs/apk/debug/*.apk
```
These are **commands, not logs**. `build.gradle.kts` has `lint.abortOnError = true` and `warningsAsErrors = true`. Detekt and Spotless are **NOT_CONFIGURED** and must never be described as passing. Source-level P11 review is no evidence of no ANR/real smooth scrolling/200% font/device TalkBack success.

## 5. Known blockers explicitly carried into P12

1. **P06 remains IN_PROGRESS**: the real compiler-generated `app/schemas/com.latticeward.musiccopilot.data.room.MusicCopilotDatabase/2.json` is missing (verified via live GitHub), native Room V1→V2 migration and validation not executed. P12 must run Gradle/KAPT, commit genuine schema, execute preservation/integrity tests, inspect SQL. No destructive fallback.
2. **Issue #7 OPEN**: service `MusicPlaybackService` still deliberately rejects `PlaybackCommand.ReplaceQueue`. P10 safely submits/handles rejected selections; actual sound playback, queue, background, Bluetooth and lockscreen require #7 and cannot be called PASS for #6.
3. **All T01–T10 and M01–M34 remain NOT_RUN.** JVM/Android tests and benchmark classes were authored only. No CI/Actions, emulator, Android APK, physical phone, independent review or real benchmark took place during P11.
4. Android 10–15 device breadth requires actual hardware/emulator when available; one owner handset suffices for specifically requested owner physical QA, not a cross-platform release certification.
5. P2 Liked Songs remains intentionally deferred; Issue #5/#73 debt remains separate; P0/P1 defects discovered during P12 must be fixed before product-level ACCEPTED.

## 6. Phase disposition

`P11: PASS_SCOPED_SOURCE_AND_TEST_PREPARED`. Source/QA/readiness reviewed on authenticated feature branch with structural invariants in `ISSUE_6_PHASE11_VALIDATION.json`; not a compiler or Android test PASS. **P12 is ready to start the final gate but is not cleared for APK/issue closeout without resolving or explicitly blocking the named dependencies.** No change to `main`, PR, merge, original Issue #6 acceptance checkboxes or closed state. 
