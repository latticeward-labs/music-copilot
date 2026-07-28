# Definition of Done - MVP v1.0

> Derived only from [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md).

## Task-level evidence

- Applicable acceptance criteria pass.
- No out-of-scope account, backend, streaming or advanced playlist behavior is introduced.
- Permission denial, empty/error states and recovery are handled.
- Background/lock-screen/Bluetooth playback remains synchronized.
- Calls, headset disconnect, Bluetooth disconnect and Audio Focus scenarios pass.
- Voice uses only the six commands, one short response, at most one retry and one-step ambiguity confirmation.
- No raw voice or prohibited personal data is stored or logged.
- Driving Mode has at least 48dp targets, suitable contrast, scalable text and TalkBack labels.
- No typing, deep navigation or long result lists appear in Driving Mode.

## Beta release gate

- Zero open P0
- Internal crash-free sessions at least 99%
- Golden scenario on three vehicles
- Android 10, Android 12 and Android 14+ evidence across Pixel/stock, Samsung/One UI and Xiaomi/MIUI
- Approved Privacy Copy
- Reliable Analytics
- Documented Rollback path
