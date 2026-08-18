# Cluster D - Evidence Summary

**Scope:** TS-03, TS-07, TS-09 supporting UF-08/UF-09/UF-10  
**Date:** 18 August 2026  
**Current automated result:** **PASS — workflow run #8 on code head `38d71c7a3bf14b6a0afc99e507b07fdad5399a86`**

## Evidence matrix

| Evidence | Artifact | Current result |
|---|---|---|
| Official Android provider/capability research | `TS-03_SPEECH_PROVIDER_FEASIBILITY.md` | PASS - source decision |
| Fail-closed on-device/network/manual provider policy | `AndroidSpeechCapabilityProbe.kt` + host/instrumentation tests | PASS |
| Deterministic six-intent and artist mapper | `DeterministicIntentMapper.kt` + unit tests | PASS |
| One retry / six-second / TTS-nonrollback session contract | `VoiceSessionMachine.kt` + unit tests | PASS |
| Safe default/range/last-valid policy | `ConfidencePolicy.kt` + unit tests | PASS |
| User Flow/state/traceability/Mermaid parity | Cluster D documentation set | PASS |

## Executed workflow

- GitHub Actions run: #8 / `32152607787`
- Code head: `38d71c7a3bf14b6a0afc99e507b07fdad5399a86`
- Build, strict lint, host unit tests, and debug APK: PASS
- AOSP API 29: 4/4 instrumentation tests PASS
- AOSP API 31: 4/4 instrumentation tests PASS
- AOSP API 34: 4/4 instrumentation tests PASS
- Total AOSP instrumentation: 12/12 PASS

## Evidence truth

No physical device, OEM provider, microphone recording, cloud processor, real artist library, vehicle, road-noise, or Beta result is claimed in this bundle. AOSP automation can prove fail-closed adapter behavior and deterministic logic only. Production/OEM/provider/legal/field evidence remains Issues #10/#11/#13/#16.

## Completion rule

**PASS at the Cluster D User Flow/spike-contract level.** This does not close the implementation or release issues.
