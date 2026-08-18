# Android Technical-Spike Harness

This isolated, non-production Android project executes the evidence protocols for Cluster B (`TS-01`, `TS-02`, `TS-06`, `TS-08`) and Cluster D (`TS-03`, `TS-07`, `TS-09`). It is not Issue #4's engineering foundation, a Wireframe, a product UI, or final Software Architecture approval in Issue #16.

## Locked boundaries

- Android 10+ (`minSdk 29`), Kotlin, and Jetpack Compose.
- One `ExoPlayer` and one `MediaSession`, both owned by `MediaSessionService`.
- UI and system surfaces connect only through `MediaController`.
- MediaStore is authoritative for accessible audio; the reconciliation result is a rebuildable projection.
- The local versioned snapshot restores only Paused and requires explicit Resume.
- Voice starts only from explicit push-to-talk, listens for at most six seconds, ducks during Listening, and allows one retry.
- STT/TTS/provider policy, deterministic mapping, and session coordination are isolated behind testable boundaries.
- Exactly six English intents exist; unknown/unsupported/not-found never executes a nearest candidate.
- Generic Android recognition is not labeled offline. Network-assisted use requires separate consent and complete reviewed provider/destination, purpose, and retention disclosure.
- No backend, account, streaming, catalog, cloud sync, raw voice, transcript, analytics SDK, or personal media metadata logging.
- `Previous item` is a transport probe only. It does not select numeric `T_PREVIOUS`.
- TS-07/TS-09 numeric values are conservative spike candidates; Issue #16 owns production adoption.

## Automated checks

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

Host tests verify:

- snapshot/migration/reconciliation behavior retained from Cluster B;
- all six intents, deterministic normalization, exact/clear/ambiguous/not-found artist resolution, and zero direct execution for negative corpus rows;
- safe confidence build default, bounded remote validation, last-valid fallback, and non-blocking remote failure;
- six-second Listening, duck/release, direct execution, one confirmation, exactly one retry, manual fallback, cancellation, and TTS non-rollback;
- fail-closed provider policy for on-device, network-assisted, disclosure, and consent states.

Instrumentation retains synthetic MediaStore/service checks and adds a platform capability assertion across AOSP API 29, 31, and 34. AOSP CI cannot establish OEM speech availability, processing destination, retention, microphone accuracy, or road-noise/Beta quality.

## Manual/device execution

1. Use only synthetic test media and controlled test phrases.
2. Install the debug APK and execute the relevant evidence protocol.
3. Clear the filtered trace before each run.
4. Capture a result bundle with `scripts/capture-spike-evidence.sh` from repository root where applicable.
5. Review and redact before committing. Never commit titles, artists, albums, folders, paths, content URIs/IDs, audio, transcripts, provider hypotheses, accounts, tokens, or unrelated logcat.

A successful build/emulator run alone never approves production provider enablement, final numeric thresholds, a real-device quality KPI, Issue #10/#11/#13, or Issue #16.
