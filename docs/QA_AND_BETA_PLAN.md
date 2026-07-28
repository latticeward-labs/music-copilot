# QA and Closed Beta Plan — MVP v1.1

## Test levels

- **Unit:** intent mapper, artist normalization, queue, previous threshold, seek clamp, telemetry schema
- **Integration:** Room/MediaStore, MediaSessionService, on-device/network-consent STT adapters, Audio Focus, persistence
- **UI:** permission states, library/search/player, Driving Mode, Voice overlay, Settings
- **Field:** road noise, Bluetooth/head unit, route transitions and real vehicles

## P0 scenarios

| Scenario | Required result |
|---|---|
| Incoming call during playback | logical duck/pause and recovery; no overlap/crash |
| Bluetooth/headset disconnect | immediate pause; no unintended phone-speaker playback |
| Audio route changes | UI/session stay synchronized |
| Corrupt/missing queue item | controlled skip, normalized error, queue continues |
| UI rotation/closure | service continues; UI reconnects to canonical state |
| Screen off | playback and controls remain functional |
| Medium road noise | direct-intent success ≥85% in aggregate |
| Similar artists | short confirmation; no low-confidence auto-execution |
| STT unavailable/timeout/failure or consent revoked | typed state, at most one retry where applicable, then complete manual control; music remains stable |
| Permission denial | no crash; clear Settings recovery |
| 5,000 files | incremental scan and smooth scroll without blocking error |

## Device and vehicle matrix

Pull-request automation covers API 26, 33 and 36. Nightly automation covers API 29, 31 and 34. Physical evidence includes an older API 26–29 device, Android 12/13 and Android 16 where available. Representative vendor coverage includes Pixel/stock Android, Samsung/One UI and Xiaomi/HyperOS or MIUI. Test at least three vehicle/head-unit models and document phone/OS/head-unit combinations.

Any repeatable Playback crash, unintended output route or seriously wrong Voice execution is P0 and blocks release.

## Closed Beta sample

- 50 qualified drivers
- 14 days of natural use
- 3+ vehicle/head-unit models
- ≥30 minutes driving per day
- vehicle Bluetooth use
- ≥50 lawful local tracks
- ability to use six English commands
- informed consent for non-audio diagnostics

## Protocol

1. Baseline interview and current-control method
2. Five-minute training and stationary golden scenario
3. Natural use for 14 days without daily coercion
4. Short surveys on days 3 and 7
5. Final interview
6. Cohort, intent, latency, correction, route and retention analysis

## Decision thresholds

| Signal | Green | Yellow | Red |
|---|---:|---:|---:|
| Driving Mode reuse | ≥50% | 30–49% | <30% |
| Direct-command success | ≥85% | 70–84% | <70% |
| Manual correction | <15% | 15–25% | >25% |
| Serious-crash-free sessions | ≥99% | 97–98.9% | <97% |

## Go/No-Go

GO requires zero open P0, internal crash-free ≥99%, golden scenario on 3 vehicles, approved privacy copy, verified analytics and a documented rollback. Otherwise ITERATE or STOP according to the product hypothesis.
