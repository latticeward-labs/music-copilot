# Issue #10 P13 — Replacement APK for immediate Voice unavailable (local STT only)

**Date:** 2026-10-25  
**Observed by owner:** Android microphone permission granted, Driving Mode Mic tapped, terminal **Voice command unavailable** immediately, with optional offline TTS "Use the manual playback controls"; no usable Listening interval observed. Root cause was not directly confirmed via device Logcat, so this is a **targeted probable-cause repair, pending a real phone retest**.

## Exact code-change scope

Only **two production source files** were changed after initial P13 build:

1. `app/src/main/java/com/latticeward/musiccopilot/voice/policy/OnDeviceEnglishSupportPolicy.kt`: New pure `canAttemptOfflineEnglish` predicate to distinguish installed-English support and **undetermined provider support** (all on-device support lists empty). Previously empty support lists were treated as definitive unsupported. Explicit downloadable/pending or unsupported English model evidence remains blocked.
2. `app/src/main/java/com/latticeward/musiccopilot/voice/android/AndroidOnDeviceSpeechToTextEngine.kt`: Handle API33+ **`SpeechRecognizer.ERROR_CANNOT_CHECK_SUPPORT`** by attempting a single `startListening` with the same strictly **`createOnDeviceSpeechRecognizer`** instance. Use the predicate for ambiguous empty support metadata; no extra recognition instance. Other errors and explicit missing model retain manual fallback.

**Not changed:** `MusicPlaybackService`, Media3, Player, Audio Focus, route recovery, Bluetooth, Queue/MediaStore, user permissions, Core Voice state machine, screen layout, wake phrase, five-intent mapping, confidence threshold, TTS, cloud consent, manifest or network permission. No fake transcript/confidence, no Internet permission, no automatic model download, no silent cloud fallback.

**Important limitation:** This removes a premature rejection when an OEM *cannot inspect* model metadata; it does not create or download an absent on-device recognizer/language model. If the device genuinely lacks English offline recognition, or Media3 duck ACK is refused for safety, the existing safe manual fallback remains. Real device behavior **NOT_VERIFIED**.

## Verified replacement build — use this for retesting, not the original P13 APK

- Source SHA: `cfb0abb9c3390d47c3afa3fa0513d7740aba0fa4`
- Build-only task: `:app:assembleDebug` (only Android packaging/compilation; no JVM, lint, emulator, Compose, connected or phone tests)
- GitHub Actions run: https://github.com/latticeward-labs/music-copilot/actions/runs/40218586084 — `completed/success`
- Artifact ID: `12608466440`
- Durable downloadable GitHub ZIP: https://github.com/latticeward-labs/music-copilot/actions/runs/40218586084/artifacts/12608466440
- Artifact ZIP bytes: `13564230`
- ZIP SHA-256: `3738e90480a2f8d0241e206b017094d9713f0efcb1a32364b0b807dfa7ad4700`
- Embedded **APK** name `app-debug.apk`, exactly `37285182` bytes
- **APK SHA-256:** `7a80cdc1c673d1c04372f9a81cbd2be0a998e4eedcdfe87fd132b5177d9a192f`
- ZIP CRC, embedded `app-debug.apk.sha256`, embedded size and embedded source SHA independently matched on extraction.
- **Prior P13 APK** SHA-256 `4c22acd630fd1c8b62044cd52d30ccf309d97fc787e6a5ecde7e4694f936b396` is **superseded only as the CURRENT Issue #10 owner Voice-retake candidate**. Original P13 build remains valid provenance.

## Focused owner phone retest before M11–M60

1. Replace the earlier **debug** APK with this one; normally install over the same debug package. If signature mismatch is observed, **stop** and report it before uninstalling: uninstall can delete app preferences/library cache.
2. On the same Android 15 phone, check that Mic permission is `Allow`, player displays a valid current track and manually plays, and app stays foreground.
3. Open Driving Mode, tap `Mic · Voice` **again** after the already-granted permission and watch for a real Listening interval. While Listening say **"Pause"** or **"Next song"** once. Verify actual Media3 action. Do not assume a transient Listening UI implies recognition success.
4. Repeat on Full Player. If `Voice command unavailable` is still immediate, report whether music ducked briefly, plus the **Settings Voice diagnostic**. A provider may be absent or media service ACK unavailable. Do NOT treat this as fixed merely due to compilation.
5. If the device has no installed on-device English model, app is manual-only by design; report `BLOCKED` for live Voice tests rather than marking incorrect PASS.

All **60 owner M tests, all 50 lab trials, all 16 AC and all 10 technical execution groups** remain **NOT_RUN/NOT_VERIFIED** for the replacement APK until the owner explicitly reports observations. No PR, merge or issue closure.
