# TS-03 - Android Speech Provider, Language, Network, and Consent Feasibility

**Blocks:** final UF-08, UF-10, UF-12 contract and Voice Beta provider enablement  
**Date:** 18 August 2026  
**Automated result:** **PASS — workflow run #8 on head `38d71c7a3bf14b6a0afc99e507b07fdad5399a86`**  
**Provider/device result:** No physical/OEM provider claim; safe default is Manual Only unless capability and disclosure gates pass

## Question

Can Android expose enough information to decide whether English speech recognition is available, on-device or network-assisted, and whether Music Copilot can truthfully disclose processing destination and retention?

## Official platform findings

| Capability | Android contract | Cluster D decision |
|---|---|---|
| Any speech service | `SpeechRecognizer.isRecognitionAvailable` | Availability only; it does not prove English support, offline processing, destination, or retention. |
| On-device service | API 31+ `isOnDeviceRecognitionAvailable` and `createOnDeviceSpeechRecognizer` | This is the only generic platform path treated as explicit on-device capability. English must still be checked/validated. |
| Language support | API 33+ `checkRecognitionSupport`; installed/supported/pending on-device and online language lists | Use typed provider result. Unknown, cannot-check, unsupported, or unavailable fails safely. |
| Offline preference | `RecognizerIntent.EXTRA_PREFER_OFFLINE` | Not proof: official docs state recognizer implementations may ignore it. Never label the session offline from this flag. |
| Network behavior | Generic `SpeechRecognizer` may stream audio to remote servers | A generic recognizer is not enabled as a cloud path without separate reviewed provider metadata and consent. |
| Destination/purpose/retention | Not supplied as a complete legal disclosure by the generic capability API | Must come from the selected provider/legal review, outside this API. Missing any field -> Manual Only. |

Official references: [SpeechRecognizer](https://developer.android.com/reference/android/speech/SpeechRecognizer), [RecognitionSupport](https://developer.android.com/reference/android/speech/RecognitionSupport), [RecognizerIntent](https://developer.android.com/reference/android/speech/RecognizerIntent).

## Provider policy matrix

| Row | Device/provider evidence | English status | Disclosure + consent | Approved mode | Required fallback |
|---|---|---|---|---|---|
| P01 | Proven on-device service | Ready/validated | Not cloud-dependent | On Device | Typed failures still follow one retry/manual |
| P02 | Proven on-device service | Supported but not installed/pending | Not applicable | Manual Only in active Driving Mode | Optional model setup only outside driving |
| P03 | Proven on-device service | Unknown/cannot check | Not applicable | Manual Only | Short unavailable; no offline claim |
| P04 | Generic/network service only | Any | Missing provider/destination, purpose, retention, or consent | Manual Only | Manual controls preserved |
| P05 | Reviewed network-assisted adapter | English supported | Complete disclosure + separate consent | Network Assisted with Consent | Network failure gets at most one retry |
| P06 | Reviewed network-assisted adapter | English supported | User declines/withdraws | Manual Only | No degraded manual playback |
| P07 | No service / permission denied | Not available | Not applicable | Manual Only | UF-12 recovery outside active driving |

## Executable evidence

The isolated Android spike harness adds:

- `AndroidSpeechCapabilityProbe`, which reports service/on-device capability without inferring provider legal facts;
- `CloudDisclosure` and a fail-closed `approveMode` policy;
- host tests for proven on-device, missing disclosure, complete disclosure without consent, and approved network-assisted behavior;
- an AOSP instrumentation assertion that a platform probe without disclosure produces only On Device when explicitly reported, otherwise Manual Only.

The workflow runs the host tests plus AOSP API 29/31/34 instrumentation. AOSP availability is evidence of adapter safety, not evidence for Pixel/Samsung/Xiaomi provider availability or production accuracy.

## Exit criteria

- [x] Provider/capability matrix records availability, language, network, processing, consent, retention, and fallback decisions.
- [x] Copy contract is truthful: no generic offline claim; network mode needs destination/provider, purpose, retention, and separate consent.
- [x] On-device/network-assisted/manual-only selection is deterministic and fail-closed.
- [x] Host policy tests and the AOSP fallback assertion PASS; AOSP API 29/31/34 each completed 4/4 instrumentation tests in workflow run #8.
- [x] Missing real provider/OEM evidence is routed to Issues #10/#13/#16 and cannot silently enable Voice.

## Decision boundary

**PASS at the User Flow/provider-policy level.** Every unverified provider case resolves to Manual Only. This does not approve a particular OEM/provider, cloud processor, consent copy, or Voice Beta readiness without the downstream physical/legal evidence.
