# QA and Closed Beta Plan - MVP v1.0

> Derived only from [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md).

## Test levels

- Unit: queue, seek, intent mapper and name normalization
- Integration: Room, Media Service, STT and Audio Focus
- Field: road noise, Bluetooth and different vehicles

## P0 scenarios

| Scenario | Required result |
|---|---|
| Incoming call | Pause/Duck and logical return; no overlap/crash |
| Bluetooth disconnect | Immediate pause; no unintended speaker playback |
| Corrupt queue item | Controlled skip; queue continues and error is logged |
| UI rotation/closure | Player continues; UI stays synchronized |
| Medium road noise | At least 85% direct-intent success |
| Similar artists | Short confirmation; no low-confidence auto-execution |

## Device matrix

- Pixel / Android Stock
- Samsung / One UI
- Xiaomi / MIUI
- Android 10
- Android 12
- Android 14+
- At least three vehicles/head units

Any repeatable playback crash, wrong output route or serious wrong Voice execution is P0.

## Closed Beta

50 qualified drivers, 14 days, 3+ vehicle/head-unit models, at least 30 driving minutes per day, vehicle Bluetooth use, at least 50 local tracks, six-command English ability and informed consent for non-audio diagnostics.

Green thresholds: Driving Mode reuse at least 50%, command success at least 85%, manual correction under 15%, crash-free sessions at least 99%.
