# TS-01 Evidence Manifest

- Status: **PASS — execution evidence accepted; unavailable historical raw captures covered by approved retrospective evidence-retention waiver**
- Date: 2026-08-18
- Harness build SHA: `81b9aadcb67cbfcb7e7fcc849a4892755d5b891a`
- Documentation repository SHA at initial packet creation: `85b5803f6e456d264627dc4a7b70a3283663670a`
- Package: `com.latticeward.musiccopilot.spike`
- Automated AOSP coverage: API 29/31/34, 9/9 instrumentation PASS
- Physical device: Samsung SM-M325FV, Android 13 / API 33, One UI 5.1
- Connected instrumentation on Samsung: 3/3 PASS
- Physical scenarios: N01-N10 PASS
- Pixel/stock physical TS-01: Approved N/A / Waived by project-owner decision dated 2026-08-18
- Xiaomi/MIUI/HyperOS physical TS-01: Approved N/A / Waived by project-owner decision dated 2026-08-18
- Known OEM observation: disabling app notification permission while playing stops active media playback/control on the tested Samsung; the same behavior was reproduced across comparison music-player apps and accepted as OEM/platform-consistent for this matrix.
- Personal media metadata: prohibited from evidence
- Android review decision: **PASS — project-owner attestation recorded in `review-signoff.md`**
- QA review decision: **PASS — project-owner attestation recorded in `review-signoff.md`**
- Product/Governance decision: **PASS**
- Raw historical capture status: original timestamped runtime trace/logcat/recording was not retained; retention is explicitly waived by `../../../../CLUSTER_B_EVIDENCE_RETENTION_WAIVER_2026-08-18.md`; no raw data is fabricated.

## Artifact checksums / Git object IDs

- `artifacts/execution-record.txt`: `e3252ca3193de87b20a762d2b9225b60ac5e8930`
- `artifacts/state-trace.txt`: `701467459d88eb616d3f092a72898147fe644483`
- `artifacts/logcat-redacted.txt`: `3abfed6c9014e3cab49eaaf20877fc0f9e38961a`
- `artifacts/screenshots-or-screen-recording/`: tree `15d5f2160d5b2149a4284b3fbd95ebd92d285407`
- `review-signoff.md`: `449cfdfd00b3b0709c36c9ad3a93d4bef0d7be7c`

## Decision

**PASS.** TS-01 execution, OEM waiver decisions, derived evidence records, artifact object IDs, and Android/QA approval attestations are complete for Cluster B. Unavailable historical raw captures are transparently waived rather than reconstructed.
