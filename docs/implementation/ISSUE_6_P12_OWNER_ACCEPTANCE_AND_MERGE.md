# Issue #6 — Owner phone acceptance and deferred QA closeout (MVP)

**Date:** 2026-10-22  
**Decision:** The owner explicitly requested delivery closure and a PR merge of the Issue #6 local MediaStore / Room / Library / Browse / Search scope, with six N/A manual phone cases **deferred after completion of the MVP**. This is an **OWNER-ACCEPTED MVP EXCEPTION**, **not an assertion of complete engineering acceptance**.

## 1. Real user-reported phone test evidence

The original uploaded file `music-copilot-issue6-phone-results.txt` is the sole source for manual test outcomes, SHA-256: `ea1b579a88a8d77f40f96d80f227219e60d37fdf61c61c85f895cecce9592fe8`. The full case-by-case, machine-readable transcription is [ISSUE_6_P12_OWNER_PHONE_RESULTS.json](ISSUE_6_P12_OWNER_PHONE_RESULTS.json), preserving the Persian scenario names, all 34 case IDs and source statuses.

| Result reported by owner | Count | Meaning |
|---|---:|---|
| **PASS** | **28** | Owner self-reported success on their phone; no independent image/video log provided |
| **FAIL** | **0** | No failure was reported in the exported owner results |
| **N/A** | **6** | Not run or not applicable in this testing context; **not PASS**; explicitly postponed until after MVP |
| **Total** | **34** | M01 through M34 accounted for |

The uploaded file's **Phone** and **Android** fields are **blank**. Do not infer device model, API level or execution date. All case notes were also blank. The APK hash provided by the owner matches the previously extracted, already-built APK:
- SHA-256: `1587527f5a9a99c5cc664eb84e64a3865035a079f464e9cecbe0bb60d3fcb0de`.
- Compiled source: `bdafa1241aa8cc56b9c9fca61e0fe618d3ba6f65`.
- Existing single-APK artifact: [Actions run #39755773243](https://github.com/latticeward-labs/music-copilot/actions/runs/39755773243) / artifact #12415363520.
- Later branch commits were documentation/schema/workflow updates; owner test evidence applies to **that APK**, and must not be silently transferred to any rebuilt binary.

**Special interpretation:** M30, M31 and M32 are reported PASS for the described touch / removed-item / unavailable-service user flow. That **does not establish actual sound playback**, queue or MediaSession completion; [Issue #7](https://github.com/latticeward-labs/music-copilot/issues/7) remains the playback owner.

## 2. Six explicit after-MVP deferrals — never count as PASS

| Test | Owner's uploaded result | Deferral |
|---|---|---|
| **M15** | **N/A** | Inject MediaStore/storage-provider failure |
| **M16** | **N/A** | Verify Retry after recovery from provider failure |
| **M22** | **N/A** | Search read failure or concurrent index change |
| **M25** | **N/A** | Real 5,000-file scan and scroll test |
| **M26** | **N/A** | Corrupt/incomplete song metadata |
| **M28** | **N/A** | 200% system font accessibility |

**Follow-up tracking:** [Post-MVP QA debt — Issue #74](https://github.com/latticeward-labs/music-copilot/issues/74) OPEN, with an individual unchecked item for each of the six tests and known technical QA debt. This separate issue must only be closed upon genuine observed evidence later.

## 3. Already observed technical quality and missing coverage

- Earlier build compiled a single installable signed debug APK and its unit-test XML reported **343/343 JVM PASS**. This is actual past evidence, **not new testing triggered by this closeout**.
- The same workflow's overall conclusion was **FAILURE**, with **10 Android Lint errors**. Do not label the aggregate CI PASS; remediation tracked in #74.
- Kotlin/Room compiler created the real Room V2 `2.json`, but Android V1→V2 migration/data-preservation instrumentation was not executed and is part of #74.
- Native Android/Compose suite, device-API breadth, full actual 5k performance, 200% font and six cases marked N/A were not verified; no invented performance/compatibility claims.
- #7 Media3 service remains responsible for playable queue/real playback. P2 Likes is intentionally deferred.
- The user expressly prohibited new GitHub Actions tests or builds because of quota cost. Final commits / PR / merge use `[skip ci]` for supported push and pull_request events; **no new tests are being invoked**.

## 4. Closeout decision / remaining liability

The owner has authorized a **conditional MVP scope closeout**, with 28 self-reported PASS, 6 N/A deferred to #74, 0 reported FAIL and explicitly acknowledged technical verification debt. Issue #6's originally authored strict ACs concerning measured 5,000-file performance, corrupt metadata, full font scale and complete native QA **cannot truthfully be marked PASS** on this evidence. The integration decision is therefore **MVP_ACCEPTED_WITH_DEFERRED_QA**, not `FULL_12_OF_12_ENGINEERING_PASS`. 

A PR into `main` should document these limits and reference #74 / #7. On successful GitHub-confirmed merge, Issue #6 may be closed as owner-accepted MVP scope, while #74 and #7 stay OPEN. Retain this report and original result file reference; do not erase the historical failure/NOT_RUN records.
