# Cluster B — Technical Spike Evidence Index

**Scope:** UF-04 → UF-05 → UF-06  
**Date:** 06 August 2026  
**Tracking:** Issue #3  
**Authority:** GitHub `main` only  
**Figma dependency:** None  
**Protocol readiness:** **PASS**  
**Executed evidence:** **BLOCKED**

## Repository audit

The repository has product, ADR, User Flow, Mermaid, and review documents, but no Android application or executable test target: no Gradle Android project, manifest, `src/main`, `src/androidTest`, MediaSessionService, persistence implementation, APK, emulator trace, or physical-device result. Therefore TS-01/02/06/08 cannot honestly be marked executed or PASS.

## Controlled protocols

| Spike | Protocol | Required execution | State |
|---|---|---|---|
| TS-01 | [Notification/foreground media](TS-01_NOTIFICATION_AND_FOREGROUND_MEDIA.md) | Android 10/12/14+, permission/exemption, background, Settings recovery, Pixel/Samsung/Xiaomi | BLOCKED |
| TS-02 | [Lifecycle boundaries](TS-02_LIFECYCLE_BOUNDARIES.md) | Recreation, UI close, task removal, service/reconnect/process death | BLOCKED |
| TS-06 | [MediaStore reconciliation](TS-06_MEDIASTORE_RECONCILIATION.md) | Permission/revocation/add/move/delete/corrupt/rebuild/5,000 files | BLOCKED |
| TS-08 | [Paused restore](TS-08_PAUSED_RESTORE.md) | Versioned contract, migration/error, real process-loss proof | BLOCKED |

## Required result layout

```text
docs/user-flows/evidence/results/<TS-ID>/<YYYY-MM-DD>-<build-sha>/
├── manifest.md
├── matrix.md
└── artifacts/
    ├── state-trace.txt
    ├── logcat-redacted.txt
    └── screenshots-or-screen-recording/
```

[Result bundle contract](results/README.md) is binding. A design, protocol, screenshot without trace, or emulator-only OEM claim is not execution evidence. Cluster B spike evidence passes only when every mandatory row passes or has an approved N/A, required matrix coverage exists, P0/P1 remain separate, no duplicate player/false state/stale playability/crash/ANR/privacy leak occurs, and Android/QA decisions link to immutable bundles in Issue #3. Figma is not a source, dependency, or exit criterion.
