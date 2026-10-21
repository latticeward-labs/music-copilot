# Issue #6 — P12 FINAL Single-APK Owner Phone QA Checklist

**Execution state:** `NOT_RUN` — this file is a future test protocol, NOT a report of testing or an existing APK.  
**Applies to:** **one APK built after all implementation phases P00–P11 are integrated**. Reuse the *same* installed APK across all manual scenarios. **Do not request/receive/install a new APK for each phase**.  
**Issue:** [#6](https://github.com/latticeward-labs/music-copilot/issues/6) · [Execution plan](ISSUE_6_PHASED_EXECUTION_PLAN.md)  
**Phone target:** One real owner phone; record actual manufacturer/model, Android API and build after owner reports. The known HONOR X7d / Android 15 is only a suggested target, not evidence this test was performed.  
**Statuses:** `PASS` = steps actually performed and expected result witnessed; `FAIL` = discrepancy; `BLOCKED` = needed fixture/Issue #7/system capability missing; `NOT_RUN` = not tried. Do not check a box just because code was authored.

## 0. Candidate control — filled only when P12 happens

| Field | Recorded value |
|---|---|
| Source commit SHA | PENDING |
| Consolidated APK filename & SHA-256 | PENDING |
| Single Actions run / artifact ID or reproducible local build | PENDING |
| Build/test suites executed (links or local hashes) | PENDING |
| Phone model + Android/API | PENDING_OWNER_REPORT |
| Real accessible audio count on phone | PENDING_OWNER_REPORT |
| Test session date | PENDING_OWNER_REPORT |
| Owner test result | NOT_RUN |

**Never link personal-media screenshots, exported library contents, private audio/file names, search queries, full filesystem paths, or raw exception stacks to GitHub.** Report only test IDs, counts, Android version and sanitized error codes.

## 1. How the owner prepares ONE handset / ONE APK

1. Once P12 provides its *final integrated APK*, install it. Confirm the build identifier matches the P12 candidate SHA. No intermediate phase APKs.
2. For the positive Ready test, have **at least 8 lawful local supported audio files** present **and indexed by Android MediaStore**, with:
   - at least 2 distinct artists;
   - at least 2 albums;
   - at least 2 folders;
   - two tracks sharing a title (to verify they remain distinct);
   - one item with missing/odd metadata if feasible.
   These are test-fixture requirements, not instructions to record/store actual track names.
3. For initial-denied tests, Android Settings → Apps → Music Copilot → Permissions → deny Music and audio. For fresh onboarding you may clear *this app's* local app data or reinstall the same APK; this can reset preferences/index but does not delete user music. Confirm media is not accidentally erased.
4. For Empty testing, use a genuine no-accessible-audio condition (or deterministic test fixture that explicitly represents Empty). **Do not pretend an ordinary populated library is Empty**; if unable to create the condition on the one phone, leave the corresponding case `BLOCKED` and use verified automated Empty tests.
5. Internet connection is not needed; use Airplane Mode for offline test (disable Bluetooth if desired). Never test typing long lists while driving; all tests must be performed safely while stationary.
6. Use accessibility settings (TalkBack, Font size 200%) as appropriate; return the phone to its original user settings afterward.
7. Send results in the format `M01 PASS, M02 PASS, M03 FAIL: [generic symptom] ...`. A blocked step is NOT a failure of the whole case unless it is a required function without alternate proof.

## 2. Manual phone matrix (all steps applied to the same final APK)

| ID | Action you perform on phone | Expected result to call PASS | Result |
|---|---|---|---|
| **M01** | Fresh-launch app with media permission NOT granted; reach access rationale. | No local track scan or invented Ready before permission; proper app rationale appears. | NOT_RUN |
| **M02** | Deny media permission; choose Not now / Retry / Settings according to visible flow. | No crash; no fake music; permission recovery remains available. | NOT_RUN |
| **M03** | Grant Music & audio permission and choose approved Start/Continue. | Real scan begins only after grant; `Scanning` visible; reaches actual `Ready` or `Empty` truthfully. | NOT_RUN |
| **M04** | With genuinely **zero accessible tracks**, complete the scan. | `Empty` with guidance/Rescan; **not** Ready or fake song rows. Only PASS when real empty fixture exists. | NOT_RUN |
| **M05** | With music present, open **Songs**. | Real song rows appear, stable count/order, no unexplained duplicate after navigation. | NOT_RUN |
| **M06** | Tap **Artists** tab. | At least 2 distinct local artists grouped correctly, with accessible names. | NOT_RUN |
| **M07** | Tap an artist, inspect songs and go Back. | Only that artist’s accessible songs; Back returns to Artists without restarting a scan. | NOT_RUN |
| **M08** | Tap **Albums** tab. | At least 2 albums grouped correctly; counts/names are consistent with Songs. | NOT_RUN |
| **M09** | Open an album and go Back. | Only that album’s tracks; correct Back navigation, no false player state. | NOT_RUN |
| **M10** | Tap **Folders**, open two actual music folders and return. | Folder names/groups and their tracks are correct; no blank or cross-folder mixing. | NOT_RUN |
| **M11** | Press Rescan twice in sequence with unchanged device music. | Accurate count, no duplicates or permanently spinning scan; Ready remains truthful. | NOT_RUN |
| **M12** | Add a supported song to local device audio (so Android MediaStore sees it), then Rescan. | New song appears in Songs, correct artist/album/folder; no full UI freeze. | NOT_RUN |
| **M13** | Delete or move a test track using a phone file manager; refresh/rescan. | Removed/moved identity is reconciled; stale row is not selectable as playable. | NOT_RUN |
| **M14** | During a long scan (if enough files), leave screen / rotate / interrupt safely. | No crash; cancelled/interrupted scan does not claim false Ready or corrupt previous usable index. If too fast, mark BLOCKED and rely on execution of cancellation tests. | NOT_RUN |
| **M15** | Trigger a recoverable scan/index failure using a verified P11 QA fixture (if available), press Retry. | Explicit recoverable error, Retry works, no personal filename in error. Without a fixture, mark BLOCKED. | NOT_RUN |
| **M16** | After actual Ready, open Android Settings, revoke audio access, return to app. | Real OS permission rechecked; cached songs not presented as playable; Settings recovery path offered. | NOT_RUN |
| **M17** | Regrant access, open Search with empty field. | Correct Idle state, no invented results and clear labeled Search control. | NOT_RUN |
| **M18** | Type part of a real song title (do not publish text). | Correct **local** song result; selecting it enters common validation. | NOT_RUN |
| **M19** | Search for part of a local artist name. | Relevant local artist / artist tracks appear according to approved result presentation. | NOT_RUN |
| **M20** | Search for part of a local album name. | Relevant local album / album tracks appear; unrelated online items never appear. | NOT_RUN |
| **M21** | Search a deliberately non-matching random string; then edit/clear it. | Explicit `No Result`; query stays editable, Clear works, normal search resumes. | NOT_RUN |
| **M22** | Trigger a local-search error via P11 QA fixture; press Retry and edit query. | `Error` retains query and previously verified library; retry works; no destructive index reset. If no fixture, BLOCKED. | NOT_RUN |
| **M23** | While browsing Artist/Album/Folder tracks, rotate screen or recreate Activity. | Current destination/category and real library state remain coherent; no duplicate or fake scan. | NOT_RUN |
| **M24** | Search with a nonempty query; rotate/recreate, then press Back. | Active query and correct Search context remain, Back returns to Library without playback mutation. | NOT_RUN |
| **M25** | With approximately 5,000 accessible supported tracks, scroll quickly, change categories, search/rescan. | No ANR/crash/freezing; list is lazy/responsive; index accurate. **If phone lacks 5,000, mark BLOCKED here; separate synthetic 5k benchmark must PASS.** | NOT_RUN |
| **M26** | Browse a track with missing artist/album/title or unusual characters, if available. | Safe display fallback, no crash, no absurd negative duration; items normalized/skipped consistently. If no fixture, BLOCKED. | NOT_RUN |
| **M27** | Enable TalkBack and move focus through Library & Search, category/rows, results and navigation. | Correct labels, group headings, order, action descriptions, loading/result/error announcements; no trapped focus. | NOT_RUN |
| **M28** | Set large font / 200% scaling in Android Settings; inspect Library/Search/Errors. | Content reflows or scrolls; controls remain accessible, no clipped essential actions, 48dp targets. | NOT_RUN |
| **M29** | Disable mobile data/Wi-Fi (Airplane Mode), reopen Library and Search. | Browsing, scan and local query work offline; no login/catalog/network gate. | NOT_RUN |
| **M30** | Enter Settings from Library, return; background app/reopen normally. | Approved navigation and readiness remain truthful; no unsolicited permission prompt or full scan. | NOT_RUN |
| **M31** | Select a song, then delete a *different* test file and attempt to select its old row before/after Refresh. | Stale selection is rejected/reconciled with recovery; never optimistic Playing. | NOT_RUN |
| **M32** | Select accessible track with playback service unavailable/not implemented (#7). | Correct, non-crashing pending/error/fallback; no fake Playing. If #7 is available and acknowledges start, transition is service-based. | NOT_RUN |
| **M33** | While in Search, type/clear without selecting anything; observe any pre-existing actual player state (if #7 available). | Browsing/search does not restart or pause currently service-owned playback. If #7 not implemented, mark BLOCKED_DEPENDENCY_7. | NOT_RUN |
| **M34** | Reopen app after interruption/normal process relaunch; recheck grant and library. | No invented permission, fake index Ready, or silent loss of valid data; app offers explicit recovery if required. | NOT_RUN |

### Scoring and reporting
- **Owner manual overall:** count `PASS`, `FAIL`, `BLOCKED`, `NOT_RUN` separately. **Never count BLOCKED or NOT_RUN as PASS.**
- **Critical P0:** M01–03, M05–13, M16–21, M23–24, M27–30, M31–32 must work on supported fixture; other cases may require approved synthetic/fault-fixture proof and honest status. M04 must be supported by a genuine Empty case somewhere; M25 by authentic 5,000-case benchmark; M22 by error-injection test; M33 by Issue #7 integration when available.
- A user's report can be recorded as `OWNER_REPORTED_PHYSICAL`; **not** `ADB_VERIFIED` unless actual ADB run evidence exists. App screens/playback claimed for Issue #7 must not be given false PASS in Issue #6.

## 3. Required non-phone technical suite (P12, actually executed)

| Suite | What must be checked | Evidence / Gate |
|---|---|---|
| **T01 JVM data** | Metadata normalization, duplicates, missing fields, same-title distinct refs, stable normalization, safe errors. | Test identifiers, real passing output, commit SHA. |
| **T02 JVM reconciliation** | Incremental add/update/delete, cancellation during scan, source throwing, overlapping scan, revocation epoch races. | No partial unusable index; no false Ready. |
| **T03 Room & migrations** | Actual DAO Songs/Artists/Albums/Folders, grouping/counts, query ordering/limits, persisted schema migration without losing app-owned records. | SQLite/Room executed tests PASS. |
| **T04 search** | Empty query, partial title/artist/album, accents/case, deterministic ordering, no result, error retains query, 5k bounded query. | Executed unit/integration/Compose tests PASS. |
| **T05 MediaStore/API** | Correct Android29–32 and 33+ capability behavior, cursor close, corrupt/unreadable item, content URI requery, media removal and grant/revoke. | At least supported API 29, 31, 33, 35 coverage recorded by actual emulator/device evidence; list gaps if unavailable. |
| **T06 Compose UI** | Library 4 categories, search states, Back, state after recreation, lazy lists, TalkBack semantics, 48dp controls and 200% font. | Executed instrumentation/semantics evidence, not just code existence. |
| **T07 5k performance** | 5,000 synthetic or lawful local media records initial scan, incremental rescan, browse scroll, search; main-thread no disk/cursor scan, no ANR and bounded memory. | Reproducible method, device/runtime, measurement, passing result. |
| **T08 architecture** | MediaStore authority; Room derived; DataStore small state; service-owned playback; dispatcher/scope policy; no forbidden direct UI DAO/MediaStore access. | Static/architecture checks PASS. |
| **T09 privacy** | No media titles, artist/album/folder, queries/results, content URIs/paths/IDs or raw audio in telemetry/logs/crash reports. | Code-review and executable negative tests PASS. |
| **T10 quality** | Gradle compile, JVM, Lint/Detekt/Spotless where configured, full final APK SHA, artifact checksum and dependency constraints. | Final candidate CI/build PASS; failures triaged. |

**A single APK build does NOT replace the JVM, Room, UI, privacy, performance or Android-version evidence.** These are run/verified in P12, then the *same consolidated candidate APK* is given to the owner for actual phone tests. A single physical phone is sufficient for the requested owner validation step, not evidence of cross-API release certification.

## 4. Final acceptance template — do not prefill with PASS

```text
Issue #6 — P12 final consolidated QA
Source SHA:
One APK filename / SHA256:
Build URL / run ID:
Phone model / Android / API:
Real accessible audio count:
M01 ... M34 (PASS / FAIL / BLOCKED / NOT_RUN):
T01 ... T10 (CI_VERIFIED PASS / FAIL / NOT_RUN):
5k proof type: (real device / synthetic instrumented / NOT_RUN)
#7 playback-handoff limitation:
P0/P1 unresolved bugs:
Owner report: OWNER_REPORTED_PHYSICAL / NOT_YET_REPORTED
Final decision: PASS / NOT_PASS / OWNER_ACCEPTED_WITH_DOCUMENTED_EXCEPTION
PR + merge SHA + Issue close URL (only after verified):
```

**No PR merge/Issue close without an evidence-based P12 final decision or an explicit, accurately labelled owner exception.**
