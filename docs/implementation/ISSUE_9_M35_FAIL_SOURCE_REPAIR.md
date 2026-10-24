# Issue #9 M35 — owner-reported failure and targeted source repair

2026-10-25: User reports saving one paired audio device, disconnecting and reconnecting it repeatedly, without seeing the optional Driving Mode? offer. **M35 FAIL**, AC08 NOT_VERIFIED. The shared image is an illustrative UI example, not a real app capture.

Old P13 APK: source 67f690483842694926f277f72000a7faf6d0db23, SHA-256 71a90627c8aa8393e2dbcdc699474cd479d26bfe6a04d0743d8dcdf0539d2d1e.

Source-level repairs:
- AppNavHost now supplies the coordinator's immediate StateFlow value and current selection epoch to the one-shot UI invitation policy. Previously, the collected Compose state could lag behind the event and consume it without showing the offer.
- SelectedBluetoothConnectionGate now retains a verified Disconnected baseline through transient WaitingForProfiles callbacks, while clearing it on a real connection or revocation/error.
- AndroidSelectedAudioProfileMonitor delays both connection and disconnection broadcasts 280ms to read settled A2DP/Headset profile proxy state.
- Two pure JUnit regression methods authored, not run.

These are credible defects, not proven device-specific root causes. A build-only replacement cumulative APK and owner M35 retest are required. No Media3, route, focus, Voice, scan/pair, autoplay or background behavior was intentionally changed. Other phone results stay NOT_RUN, original 14 AC NOT_VERIFIED, no PR/merge/close.
