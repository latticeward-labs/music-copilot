# Technical Spike Evidence Index — Completed Clusters B and D

**Scope:** Cluster B UF-04 through UF-06; Cluster D UF-08 through UF-10  
**Date:** 06 August 2026; formal evidence closeout 18 August 2026  
**Tracking:** Issue #3  
**Authority:** `MSM2.rar` English baseline + GitHub `main` only  
**Authority record:** [`../CLUSTER_B_SOURCE_AUTHORITY.md`](../CLUSTER_B_SOURCE_AUTHORITY.md)  
**Persian/Farsi source policy:** **EXCLUDED — Persian/Farsi PRD, MVP, architecture, technical, ADR, User Flow, and translated snapshots are not evidence authorities**  
**Figma dependency:** None  
**Protocol readiness:** **PASS**  
**Harness implementation:** **PASS — merged to main**  
**Automated CI validation:** **PASS**  
**Executed protocol evidence:** **PASS — TS-01/TS-02/TS-06/TS-08 accepted with immutable attestation bundles; unavailable historical raw captures are covered by an explicit retrospective evidence-retention waiver**  
**Android review:** **PASS — project-owner attestation recorded**  
**QA review:** **PASS — project-owner attestation recorded**  
**Formal Cluster B Technical Spike evidence gate:** **PASS**  
**Cluster D executable spike-contract gate:** **PASS — TS-03/TS-07/TS-09; workflow run #8**

## Source boundary

Technical Spike interpretation is bounded by the English MSM2/GitHub source set. In particular, the evidence must preserve service-owned Media3 playback, Android-only/local-first data boundaries, MediaStore authority, no backend/account/streaming, truthful lifecycle/system-control behavior, and the separation of P0 live continuity from P1 restore. Evidence may validate these constraints; it may not silently redefine product scope or architecture.

## Repository audit

The four controlled protocols and result-bundle contract are complete. The isolated non-production Android project at `spikes/android-validation/` exercises service-owned Media3 playback, lifecycle/controller boundaries, MediaStore reconciliation, and versioned Paused Restore.

Build, unit, lint, and AOSP API 29/31/34 instrumentation are automated by `.github/workflows/cluster-b-android-spikes.yml`. Workflow run #6 passed on head `fbd6fcc2a3f8c5a378d7eb598d487762ce829a5e` and was squash-merged to main as `81b9aadcb67cbfcb7e7fcc849a4892755d5b891a`. Build, strict lint, host unit tests, and 9/9 AOSP instrumentation executions passed.

## Executed protocol decisions

- **TS-01:** Samsung SM-M325FV / Android 13 / One UI 5.1 N01-N10 PASS; Samsung connected instrumentation 3/3 PASS; Pixel/stock and Xiaomi/MIUI-HyperOS physical rows approved N/A / waived for TS-01.
- **TS-02:** L01-L12 PASS across the required physical coverage by confirmed execution.
- **TS-06:** M01-M14 PASS across the required physical coverage by confirmed execution; M12/M13 PASS outcomes accepted, with unavailable historical raw timing/changed-count values covered by the retrospective retention waiver.
- **TS-08:** R01-R12 PASS, including real process-loss Paused Restore, explicit Resume/no auto-play, live-service precedence, corrupt/torn/future-version handling, and repeated relaunch behavior.

## Immutable evidence bundles

The accepted bundles are:

- `results/TS-01/2026-08-18-81b9aad/`
- `results/TS-02/2026-08-18-81b9aad/`
- `results/TS-06/2026-08-18-81b9aad/`
- `results/TS-08/2026-08-18-81b9aad/`

Each bundle now contains:

```text
manifest.md
matrix.md
review-signoff.md
artifacts/
├── execution-record.txt
├── state-trace.txt
├── logcat-redacted.txt
└── screenshots-or-screen-recording/
    └── README.md
```

The manifests record Git object IDs/checksums for retained text evidence. Derived traces and redacted log evidence are explicitly marked as retrospective attestations where the original raw capture was not retained.

## Retrospective evidence-retention waiver

See [`CLUSTER_B_EVIDENCE_RETENTION_WAIVER_2026-08-18.md`](CLUSTER_B_EVIDENCE_RETENTION_WAIVER_2026-08-18.md).

The project owner confirmed that the physical/manual executions were completed and approved, but some historical raw capture files were not retained before the later immutable-bundle contract was finalized. For this Cluster B run only, the project owner approved a one-time retention waiver covering unavailable original runtime timestamps, physical logcat captures, screenshot/recording binaries, exact historical device identifiers where absent, and TS-06 M12/M13 raw numeric measurements. The waiver does not invent those artifacts; it accepts derived/attested evidence instead.

Android and QA review decisions are recorded as PASS by project-owner attestation in each bundle's `review-signoff.md`; individual historical reviewer identity metadata was not retained and is covered by the same explicit waiver.

## Controlled protocols

| Spike | Protocol | Result |
|---|---|---|
| TS-01 | [Notification/foreground media](TS-01_NOTIFICATION_AND_FOREGROUND_MEDIA.md) | **PASS** — automated PASS + Samsung physical N01-N10 PASS + TS-01 OEM waivers + accepted evidence bundle/sign-off |
| TS-02 | [Lifecycle boundaries](TS-02_LIFECYCLE_BOUNDARIES.md) | **PASS** — automated PASS + L01-L12 physical execution PASS + accepted evidence bundle/sign-off |
| TS-06 | [MediaStore reconciliation](TS-06_MEDIASTORE_RECONCILIATION.md) | **PASS** — automated PASS + M01-M14 physical execution PASS + accepted retrospective measurement waiver + sign-off |
| TS-08 | [Paused restore](TS-08_PAUSED_RESTORE.md) | **PASS** — automated PASS + R01-R12/process-loss execution PASS + accepted evidence bundle/sign-off |

## Cluster D executable spike contracts

| Spike | Protocol | Result |
|---|---|---|
| TS-03 | [Speech provider feasibility](TS-03_SPEECH_PROVIDER_FEASIBILITY.md) | **PASS at User Flow/provider-policy level** — fail-closed on-device/network/manual policy; host + AOSP fallback tests PASS; physical/provider/legal enablement downstream |
| TS-07 | [Artist matching](TS-07_ARTIST_MATCHING.md) | **PASS at deterministic reference-corpus level** — fixed mapper/corpus, stable ordering, zero direct execution in negative rows |
| TS-09 | [Confidence default](TS-09_CONFIDENCE_DEFAULT.md) | **PASS at safe-default/fallback level** — build candidate, bounded remote validation, last-valid/default fallback |

[Cluster D evidence summary](CLUSTER_D_EVIDENCE_SUMMARY_2026-08-18.md) records workflow run #8 on code head `38d71c7a3bf14b6a0afc99e507b07fdad5399a86`: build, strict lint, host unit tests, and AOSP API 29/31/34 instrumentation 12/12 PASS.

This evidence closes deterministic User Flow and safe-fallback decisions only. It does not claim a physical OEM speech provider, cloud processor/legal disclosure, real microphone/library/road-noise result, final numeric production adoption, or Beta KPI. Those remain Issues #10/#11/#12/#13/#16.

## Result contract and privacy boundary

- [Android spike harness boundary](ANDROID_SPIKE_HARNESS.md)
- [Harness implementation and commands](../../../spikes/android-validation/README.md)
- [Result bundle contract](results/README.md)

No personal titles, artists, albums, folders, paths, URIs, MediaStore IDs, raw audio, transcripts, provider hypotheses, accounts, tokens, or unrelated logs are authorized in evidence.

## Decision

**Cluster B Technical Spike execution gate and Cluster D executable spike-contract gate: PASS / SOURCE-ALIGNED.**

This decision retains Cluster B TS-01/TS-02/TS-06/TS-08 closure and closes the Cluster D TS-03/TS-07/TS-09 User Flow/reference-contract evidence under the English MSM2/GitHub authority policy. It does not close Issue #3, approve UF-11 through UF-13, enable an unverified speech provider, or approve final Software Architecture/implementation.
