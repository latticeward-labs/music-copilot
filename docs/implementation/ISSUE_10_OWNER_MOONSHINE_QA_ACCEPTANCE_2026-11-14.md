# Issue #10 — Owner-accepted implementation with failed voice-quality gate (2026-11-14)

## Scope and exact tested build
- Owner-requested integration candidate: **Moonshine Tiny v2 English + Silero VAD**, not later Vosk branches.
- Tested source commit: `eaebff42458a885995d336a8c517f744889d5bae` on `fix/issue10-moonshine-silero-vad`.
- GitHub Actions **build-only**: [run 40277406135, attempt 2](https://github.com/latticeward-labs/music-copilot/actions/runs/40277406135/attempts/2), job 129053262550, SUCCESS; no JVM, instrumentation, Compose, Lint or on-device automated tests were run.
- APK: `Music-Copilot-Issue10-Moonshine-VAD-debug.apk`, **99,628,974 bytes**.
- Verified APK SHA-256: `f24e6998e17613f69da0e297b9175dede16c5819c760e95b26724fece61b0ed2` (independently checked against checksum bundled in the ZIP; this is the APK hash, not the ZIP hash).
- [GitHub artifact 13657909808](https://github.com/latticeward-labs/music-copilot/actions/runs/40277406135/artifacts/13657909808); artifact availability is time-limited.
- Owner reported test results in ChatGPT on **2026-11-14**; tested handset specifics, timestamps, individual screenshots, speech transcripts, and per-attempt logs were not supplied with this report.

## Manual owner-phone QA
Owner explicitly reported **all M01–M60 PASS** on the Moonshine APK. This is **60/60 owner-asserted**, not 60 independently witnessed/evidenced per-case checks. Tests requiring additional physical devices/vehicles or technical proof are not upgraded to multi-device certification by this aggregate statement. See the original [M01–M60 checklist](ISSUE_10_FINAL_PHONE_QA_CHECKLIST_FA.md).

## Voice lab quality — actual owner-reported result

| Lab group | Intent | Succeeded | Failed | Rate |
|---|---|---:|---:|---:|
| L01 | PLAY | 2/10 | 8/10 | 20% |
| L02 | PAUSE | 1/10 | 9/10 | 10% |
| L03 | NEXT | 8/10 | 2/10 | 80% |
| L04 | PREVIOUS | 5/10 | 5/10 | 50% |
| L05 | SEEK_BACK | 8/10 | 2/10 | 80% |
| **TOTAL** | | **24/50** | **26/50** | **48%** |

**AC12 = VERIFIED_FAIL:** target >=45/50 = 90%, owner-reported actual 24/50 = 48%; 21 additional successes short of gate. All 50 attempts were reported in aggregate, but **which specific attempts passed/failed is unknown**. Do not invent individual attempt results, durations, confidence or detailed voice transcripts.

## Acceptance, deferred checks and authorization
- Source code implements Issue #10 five-intent Voice and Moonshine+Silero integration. **Implementation delivery owner-accepted despite failed AC12.**
- **AC12 explicitly FAILED**; other acceptance gates remain **NOT_VERIFIED** without independent execution evidence, even where M tests were owner-reported PASS.
- Technical groups **T01–T10 NOT_RUN / unverified**; 0 independently executed groups.
- Additional device/API/OEM, actual car/head unit and Issue #9 M45 (different from Issue #10 M45) remain unverified unless separately evidenced.
- This is **not** release-candidate, field-quality or closed beta approval.
- Owner explicitly instructed to **merge Issue #10 and close the implementation Issue to move to Issue #11**, accepting the disclosed low recognition quality as tracked follow-up, rather than falsely changing failures to PASS.
- P0 follow-up: [#80 — Moonshine 48% voice accuracy and technical QA debt](https://github.com/latticeward-labs/music-copilot/issues/80), must be resolved before claiming 90% voice accuracy or readiness for beta/release.
- No Vosk or later `fix/issue10-voice-heard-text-display` branch is included in this owner-tested merge.

**Record policy:** raw voice audio, personal local media names and per-trial logs are not stored in this summary. Aggregate user self-report is distinguished from independent reproducible QA.
