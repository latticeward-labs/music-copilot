# Cluster B — Technical Spike Evidence Index

**Scope:** UF-04 → UF-05 → UF-06  
**Date:** 06 August 2026  
**Tracking:** Issue #3  
**Authority:** GitHub `main` only  
**Figma dependency:** None  
**Protocol readiness:** **PASS**  
**Harness implementation:** **PASS — merged to main**  
**Automated CI validation:** **PASS**  
**Executed protocol evidence:** **PARTIAL — physical OEM rows and Android/QA sign-off BLOCKED**

## Repository audit

The four controlled protocols and result-bundle contract are complete. An isolated, non-production Android project now exists at `spikes/android-validation/` to exercise service-owned Media3 playback, lifecycle/controller boundaries, MediaStore reconciliation, and versioned Paused Restore. It is a focused pre-architecture technical spike, not Issue #4's engineering foundation and not approval of Issue #16.

Build, unit, lint, and AOSP API 29/31/34 instrumentation are automated by `.github/workflows/cluster-b-android-spikes.yml`. [Workflow run #6](https://github.com/latticeward-labs/music-copilot/actions/runs/31322420324) passed on head `fbd6fcc2a3f8c5a378d7eb598d487762ce829a5e` and was squash-merged to main as `81b9aadcb67cbfcb7e7fcc849a4892755d5b891a`. Build, strict lint, host unit tests, and 9/9 AOSP instrumentation executions passed. See [automated CI evidence](AUTOMATED_CI_EVIDENCE_2026-08-09.md). Samsung/One UI, Xiaomi/MIUI or HyperOS, physical-device process loss, task removal, permission presentation, and Android/QA sign-off remain mandatory device work.

## Controlled protocols

| Spike | Protocol | Required execution | State |
|---|---|---|---|
| TS-01 | [Notification/foreground media](TS-01_NOTIFICATION_AND_FOREGROUND_MEDIA.md) | Android 10/12/14+, permission/exemption, background, Settings recovery, Pixel/Samsung/Xiaomi | PARTIAL — automated subset PASS; physical rows open |
| TS-02 | [Lifecycle boundaries](TS-02_LIFECYCLE_BOUNDARIES.md) | Recreation, UI close, task removal, service/reconnect/process death | PARTIAL — automated subset PASS; physical rows open |
| TS-06 | [MediaStore reconciliation](TS-06_MEDIASTORE_RECONCILIATION.md) | Permission/revocation/add/move/delete/corrupt/rebuild/5,000 files | PARTIAL — automated subset PASS; physical rows open |
| TS-08 | [Paused restore](TS-08_PAUSED_RESTORE.md) | Versioned contract, migration/error, real process-loss proof | PARTIAL — automated subset PASS; real process-loss rows open |

## Harness and result contract

- [Android spike harness boundary](ANDROID_SPIKE_HARNESS.md)
- [Harness implementation and commands](../../../spikes/android-validation/README.md)
- [Result bundle contract](results/README.md)

```text
docs/user-flows/evidence/results/<TS-ID>/<YYYY-MM-DD>-<build-sha>/
├── manifest.md
├── matrix.md
└── artifacts/
    ├── state-trace.txt
    ├── logcat-redacted.txt
    └── screenshots-or-screen-recording/
```

A design, protocol, successful compile, screenshot without trace, or emulator-only OEM claim is not executed protocol evidence. Cluster B spike evidence passes only when every mandatory row passes or has an approved N/A, required matrix coverage exists, P0/P1 remain separate, no duplicate player/false state/stale playability/crash/ANR/privacy leak occurs, and Android/QA decisions link to immutable bundles in Issue #3. Figma is not a source, dependency, or exit criterion.
