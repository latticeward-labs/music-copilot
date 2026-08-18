# Cluster D - Source Authority and Controlled Resolutions

**Scope:** UF-08 Direct Voice, UF-09 Ambiguity/Not Found, UF-10 Timeout/Unavailable/Retry  
**Date:** 18 August 2026  
**Tracking:** GitHub Issue #3; implementation Issues #10/#11/#12; architecture Issue #16  
**Status:** Controlled source baseline for final Cluster D review

## Authority order

1. English files inside the supplied `MSM2.rar` archive.
2. Current accepted GitHub ADRs and repository product documents where they do not conflict with the English archive.
3. Derived Cluster D flow, state, traceability, spike, test, and review records in this change.

The attached Persian PRD/MVP and any translated snapshot are excluded from decision authority. The reviewed archive SHA-256 is `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`.

## English records used

- `Music_Copilot_PRD_v1_EN_Clarified.pdf`
- `Music_Copilot_MVP_v1_EN.pdf`
- `Music_Copilot_User_Flow_Phase_0_Foundation_v1.0_EN.pdf`
- `Music_Copilot_User_Flow_Phase_1_First_Readiness_v1.0_EN(4).pdf`
- `Music_Copilot_System_Architecture_MVP_v1_EN.pdf`
- `Music_Copilot_Code_and_Technical_Documentation_MVP_v1_EN.pdf`
- `ADR-0004_Replaceable_Voice_Provider.pdf`
- `ADR-0005_Restricted_Deterministic_Intent_Mapping.pdf`

## Fixed product contract

- Voice starts only after an explicit microphone action; there is no wake word or continuous listening.
- Listening is bounded to six seconds; playback ducks during Listening; one retry is the maximum.
- The mapper supports exactly `PLAY`, `PAUSE`, `NEXT`, `PREVIOUS`, `SEEK_BACK`, and `PLAY_ARTIST` in English.
- A certain supported result executes directly. Uncertainty or multiple plausible candidates permits one short confirmation only.
- Unknown, unsupported, unclear confirmation, or missing artist never executes an approximate command.
- A terminal failure preserves playback and returns to immediately usable manual controls on the initiating surface.
- TTS is at most one short sentence; TTS failure cannot undo an acknowledged command.
- Voice is an overlay/session state, not a navigation destination.
- Raw audio is never stored by the app. Raw audio, transcripts, personal titles, artist names, filenames, library contents, and persistent identifiers are prohibited from telemetry.
- Cloud speech requires separate consent plus provider/destination, purpose, and retention disclosure. Declining it never removes manual playback.

## Android platform evidence used for TS-03

The following official Android API facts supplement provider feasibility; they do not override the product sources:

- `SpeechRecognizer.isRecognitionAvailable` reports whether a speech service exists.
- API 31+ exposes `isOnDeviceRecognitionAvailable` and `createOnDeviceSpeechRecognizer`.
- API 33+ exposes `checkRecognitionSupport` and installed/supported/online language categories.
- `RecognizerIntent.EXTRA_PREFER_OFFLINE` may be ignored by a recognizer implementation and is not proof of offline processing.
- The generic speech recognizer may stream audio to remote servers. Android's generic capability API does not supply a provider's legal destination/purpose/retention statement.

Sources: [SpeechRecognizer](https://developer.android.com/reference/android/speech/SpeechRecognizer), [RecognitionSupport](https://developer.android.com/reference/android/speech/RecognitionSupport), and [RecognizerIntent](https://developer.android.com/reference/android/speech/RecognizerIntent).

## Controlled Cluster D resolutions

| Topic | Resolution | Boundary |
|---|---|---|
| Provider selection | Use a proven on-device recognizer when available; otherwise permit a reviewed network-assisted adapter only after complete disclosure and separate consent; all other cases are Manual Only. | Never infer offline behavior from `EXTRA_PREFER_OFFLINE`. |
| Language support | English support must be returned by the selected provider/capability check or validated by an attempt. Unknown/unavailable support fails safely. | No automatic model download or Settings jump while Driving Mode is active. |
| Provider output | STT returns transcription, optional confidence, and typed failure only. | It never executes a command or defines an intent. |
| Confidence policy | Production flows use named boundaries `T_DIRECT` and `T_AMBIGUOUS`. The TS-09 harness uses a conservative build-default candidate `0.90`, bounded remote range `0.80..0.98`, and last-valid/build-default fallback. | Numeric adoption/tuning remains Issue #16 and field evidence; remote config cannot add intents or bypass confirmation. |
| Artist matching | Ordered normalization, exact match first, one fixed similarity function, deterministic stable ordering, clear-margin direct result, otherwise at most two presented candidates, or Not Found. | Harness candidates are `0.88` clear, `0.72` plausible, and `0.08` separation; production adoption remains #16. |
| Confirmation | One bounded selection from only the presented candidate set. | No free conversation, candidate expansion, repeated prompt, or multi-turn loop. |
| Retry | Only recognition timeout/failure on attempt one exposes Retry. Attempt two is final. | Unknown/unsupported/not-found is not converted into guessed execution. |

## Truth boundary

Automated fake/provider-policy and deterministic corpus tests can close the implementation-independent TS-03 fallback, TS-07 determinism, and TS-09 configuration contracts. They do not claim physical microphone accuracy, OEM provider availability, processing destination, retention, road-noise success, or Beta metrics. Those remain Issues #10/#11/#13/#16 and require reviewed real-device/provider evidence before release.
