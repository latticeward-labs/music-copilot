# Cluster B Android Technical-Spike Harness

This is an isolated, non-production Android project for executing the evidence protocols for `TS-01`, `TS-02`, `TS-06`, and `TS-08`. It is not Issue #4's engineering foundation, a Wireframe, a product UI, or approval of the final Software Architecture in Issue #16.

## Locked boundaries

- Android 10+ (`minSdk 29`), Kotlin, and Jetpack Compose.
- One `ExoPlayer` and one `MediaSession`, both owned by `MediaSessionService`.
- UI and system surfaces connect only through `MediaController`.
- MediaStore is authoritative for accessible audio; the reconciliation result is a rebuildable projection.
- The local versioned snapshot restores only Paused and requires explicit Resume.
- No backend, account, streaming, catalog, cloud sync, raw voice, analytics SDK, or personal media metadata logging.
- `Previous item` is a transport probe only. It does not select numeric `T_PREVIOUS`.

## Automated checks

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

Host tests verify the snapshot contract, fail-closed decoding, torn/unsupported data, reconciliation, deduplication, stale removal, and a 5,000-item logical corpus. Instrumentation creates and deletes its own synthetic WAV, verifies MediaStore discovery, checks atomic persistence, and connects two controllers to one service-owned queue.

CI runs build/lint/unit checks once and instrumentation on AOSP API 29, 31, and 34. AOSP CI is not Samsung/One UI or Xiaomi/MIUI/HyperOS evidence and cannot close the OEM rows.

## Manual/device execution

1. Use only synthetic test media.
2. Install the debug APK and execute the protocol matrix from `docs/user-flows/evidence/`.
3. Clear the filtered trace before each run.
4. Capture a result bundle with `scripts/capture-spike-evidence.sh` from repository root.
5. Review and redact before committing. Never commit titles, artists, albums, folders, paths, content URIs/IDs, audio, voice, tokens, accounts, or unrelated logcat.

PASS remains controlled by the existing protocol documents. A successful build or emulator run alone is not a Technical Spike PASS.
