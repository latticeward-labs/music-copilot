# Issue #6 P12 — ONE consolidated owner-phone APK and truthful acceptance audit

**2026-10-22 · Requested delivery mode:** **APK only**, **NO further GitHub Actions tests**. Original single-installable-APK requirement satisfied by extracting the already-built Android debug APK from the third P12 run artifact. All phone/manual tests are the owner's responsibility. The test-capable workflow has been changed to **manual dispatch + assembleDebug ONLY**, no push trigger, no lint/JVM/emulator/instrumentation stages and **has not been dispatched** after the owner's correction.

## Actual APK binary and frozen source

- **APK candidate file:** `app/build/outputs/apk/debug/app-debug.apk` inside artifact `issue6-p12-integrated-evidence-3`.
- **Frozen compiled source SHA:** `bdafa1241aa8cc56b9c9fca61e0fe618d3ba6f65` (all P00–P11 Android changes, including five inherited test assertion updates). Later commits only generated/checked-in Room V2 schema, QA/CI workflow/docs; the delivered APK must be identified by its **compiled source SHA**, not present branch HEAD.
- **Original GitHub Actions third run:** [39755773243](https://github.com/latticeward-labs/music-copilot/actions/runs/39755773243). **Run conclusion: FAILURE** because Android lint reported **10 issues**; this is not falsely called overall CI PASS.
- **Artifact download:** [12415363520](https://github.com/latticeward-labs/music-copilot/actions/runs/39755773243/artifacts/12415363520). GitHub artifact ZIP contained exactly one installable `app-debug.apk` (and evidence/reports). ZIP is NOT itself an APK. The owner-facing APK was extracted unchanged and attached to the ChatGPT response.
- **APK bytes:** `36,312,971` (36.3 MB decimal / ~34.6 MiB).
- **APK SHA-256:** `1587527f5a9a99c5cc664eb84e64a3865035a079f464e9cecbe0bb60d3fcb0de`.
- **Binary integrity checks performed locally without GitHub Actions:** APK ZIP integrity OK; AndroidManifest.xml present; 17 `classes*.dex` files present; APK Signing Block magic present; APK extracted without editing.
- **Room V2:** actual `2.json` produced by KAPT in a previous run and checked into branch, SHA-1 Git blob `aeb1f9f8a30a415f103e07ff78e80b485e8011d6`. Byte identity verified by GitHub readback. Android migration retention test still requires actual phone/natively tested evidence.
- **Existing run technical evidence (already consumed before owner's latest direction, NO new runs):** `:app:kaptDebugKotlin`, `:app:compileDebugKotlin`, `:app:testDebugUnitTest`, `:app:assembleDebug` finished. XML reports show **343 existing JVM cases: 343 passed, 0 failed/error**. `:app:lintDebug` reported **10 errors** (StateFlow.value in Compose, plurals, two unused resources, four Gradle version-catalog notices); therefore lint gate **FAILED** and later native/5k/TalkBack/phone test steps were not run. Do not claim those PASS.
- **Owner phone:** M01..M34 **NOT_RUN**, user to install the APK and report results. No phone model, test date or Android API claimed. No second device required for this specific owner QA.
- **Issue #7:** existing `MusicPlaybackService` deliberately rejects `ReplaceQueue`. App's Library/Search is browse+search testing; actual song playback NOT IMPLEMENTED yet and should be recorded as `BLOCKED_DEPENDENCY_7`, never as a fake success.
- **Issue #6:** OPEN pending user outcomes; no merge, no PR, no 12 AC checkboxes checked.

## Mapping of original acceptance criteria to delivered APK / owner phone plan

| AC | Required behavior | Source evidence / phone evidence | Result |
|---|---|---|---|
| AC01 | MediaStore discovers music only after grant | Scanner + Permission recovery; M01–M04, M11 | OWNER_NOT_RUN |
| AC02 | Room songs/artists/albums/folders | Actual compiler-generated Room V2 schema; M05–M08 | OWNER_NOT_RUN |
| AC03 | Incremental cancellable scans preserve index | P05 delta repository, M11–M14 | OWNER_NOT_RUN |
| AC04 | Truthful Scanning/Empty/Ready/Error | P07 flows; M10–M16 | OWNER_NOT_RUN |
| AC05 | Search result/no result/error | P09 source; M17–M22 | OWNER_NOT_RUN |
| AC06 | Rotation/navigation stable | P08/P09 VMs; M23–M24 | OWNER_NOT_RUN |
| AC07 | 5,000 files no main-thread blocking | P11 synthetic benchmark source authored, native NOT_RUN; M25 only if actual 5k | NOT_VERIFIED |
| AC08 | Missing/corrupt metadata safe | P04 normalization, M09/M26 | OWNER_NOT_RUN |
| AC09 | No personal-title analytics/crash metadata | No-network local design, M33/privacy manual | OWNER_NOT_RUN |
| AC10 | Repository and query seam unit/integration contracts | 343/343 already-run JVM PASS, native Room tests NOT_RUN | PARTIAL_REAL_JVM |
| AC11 | TalkBack/scalable text/list announcements | P11 LiveRegion code, M27–M29, native NOT_RUN | OWNER_NOT_RUN |
| AC12 | Likes/Liked Songs P2 only if capacity | Explicitly deferred (Issue #6 approved scope) | P2_DEFERRED |

## Owner phone checklist

Use `docs/implementation/ISSUE_6_FINAL_PHONE_QA_CHECKLIST.md` M01–M34, and the **Persian printable and interactive standalone checklists** delivered alongside this APK in ChatGPT. Each phone case is initially `NOT_RUN`. Return phone model/API and item-by-item `PASS / FAIL / BLOCKED / N/A`; never ask the owner to run GitHub Actions. No private filenames/screenshots required.

## Important gate distinction

**APK_DELIVERED_FOR_OWNER_TESTING != ISSUE_6_PRODUCT_ACCEPTED.**
APK installability on the owner's Android phone is unverified; prior lint FAIL and real playback dependency remain documented. No more GitHub Actions or CI test executions are authorized by the owner's latest request. Review and close only after the owner reports acceptable manual results and separately resolves or explicitly accepts remaining technical verification debt.


## Owner acceptance addendum — 2026-10-22

**This section supersedes the prior "owner tests NOT_RUN" statement as of this date.** Preserve the historical P12 build/lint failure details above. User provided the exported owner-phone checklist `music-copilot-issue6-phone-results.txt` (SHA-256 `ea1b579a88a8d77f40f96d80f227219e60d37fdf61c61c85f895cecce9592fe8`). Exactly **28 PASS**, **0 FAIL**, **6 N/A** from the original 34 scenarios are recorded in [the source-preserving case ledger](ISSUE_6_P12_OWNER_PHONE_RESULTS.json). All cases have empty free-text notes. Phone/model and Android version are unprovided, not inferred.

The six **N/A / not tested** cases — `M15`, `M16`, `M22`, `M25`, `M26`, `M28` — are deferred until **after completion of the MVP** in [Issue #74](https://github.com/latticeward-labs/music-copilot/issues/74). This is not six additional PASS. Real audio playback is still Issue #7. Existing 343 JVM PASS and the 10 Lint errors from the old build are unchanged; native migration and complete performance certification are not proven. No further GitHub Actions tests are authorized.

The owner **explicitly authorized PR, merge and Issue #6 closure despite deferred verification**. This should be recorded as `MVP_ACCEPTED_WITH_DEFERRED_QA`, not `ALL_12_ENGINEERING_AC_PASS`. Detailed scope and exception: [owner acceptance/merge manifest](ISSUE_6_P12_OWNER_ACCEPTANCE_AND_MERGE.md). After GitHub-confirmed merge, close Issue #6 as owner-accepted with follow-up #74 OPEN for missing QA and #7 OPEN for playback. Do not relabel the previous CI run SUCCESS or mark deferred tests PASS.
