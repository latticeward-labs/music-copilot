# Issue #9 — P09 optional Bluetooth selected-device preference in Settings

**Date:** 2026-10-24 · **Gate:** `PASS_SCOPED_SETTINGS_BLUETOOTH_SOURCE_18_JUNIT_AUTHORED_NOT_RUN` · **Next:** P10 verified selected-device connection gate (P1).

## Source lock and authoritative contracts

- Reattached `MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8` (same as P00–P08). See clarified PRD **FR-07 P1 Bluetooth suggestion optional**, Android local-first MVP, ADR-0002 Media3 service authority, ADR-0006 no cloud/backend.
- Live repo [approved HF-10 Settings default](../high-fidelity/WF-10/HF-10_SETTINGS_DEFAULT.md) and [state CE-20 Bluetooth selection](../high-fidelity/WF-10/states/HF_STATE_CE-20.md), [CE-20 accessibility](../high-fidelity/phase-7/app-visible/CE-20.md) and UF-12: *outside Driving*, P1 removable preference, local save/remove, optional connection suggestion and explicit cancellation. Never infer Driving entry from Bluetooth alone.
- Existing `data/api/AppPreferences.selectedBluetoothHint: String?` and `PreferenceUpdate.SetBluetoothHint` already persist to `DataStorePreferencesRepository` key `bluetooth_hint` with atomic `store.edit`; on `AppResult.Failure` the prior preference remains. P09 **reuses** these existing contracts, not a second DataStore or changed playback service.

## Committed P09 production source

| File | Actual change | Limits / safety boundary |
|---|---|---|
| `app/src/main/java/com/latticeward/musiccopilot/core/application/bluetooth/BluetoothSelectionContracts.kt` (new) | Pure `PairedAudioRead`/`PairedAudioDeviceSource` with immutable candidate type; versioned `BluetoothSelectionToken` uses per-save 128-bit secure random salt + SHA-256 over normalized BT address. Store only the opaque token (not raw MAC/name), and re-match only against future freshly paired candidates using constant-time `MessageDigest.isEqual`. Reject old unversioned/MAC strings and malformed tokens. | No Android API, network, BT monitoring, audio/media/Voice player, location or telemetry. Identity is lookup hint, *not proof of a current connection*. |
| `app/src/main/java/com/latticeward/musiccopilot/app/platform/bluetooth/AndroidPairedAudioDeviceSource.kt` (new) | Android API paired `BluetoothManager.adapter.bondedDevices` **only after fresh `BLUETOOTH_CONNECT` grant on 31+**, or legacy paired permission on 29–30. Requires `BOND_BONDED`, major `AUDIO_VIDEO` and allowed output category (car audio, handsfree, headphones, HIFI, loudspeaker, portable, wearable headset). Handles unavailable adapter, disabled radio, security/permission revocation and OEM exceptions without fake devices. | Explicit source `readPairedAudio` only; **no `startDiscovery`, BLE scan, bonding, socket connection, route modification, profile listener, foreground Activity/permission prompt or auto-play**. P10 separately owns actual profile/route connection eligibility. |
| `app/src/main/java/com/latticeward/musiccopilot/feature/settings/bluetooth/BluetoothSelectionViewModel.kt` (new) | User Open/Refresh reads current candidates. Staged address is **RAM-only**; Save re-reads OS list to ensure still bonded/eligible and fresh permission, generates opaque token, then awaits existing DataStore `update` ACK. Storage error, revocation or changed device never overwrite old preference or show false Saved. Remove writes null even with BT off/permission revoked and displays actual result. Busy serializes taps and protects from duplicate writes. | No item automatically selected/committed by connection; no auto-first-device default; no analytics. Back/Cancel discards unsaved draft. |
| `app/src/main/java/com/latticeward/musiccopilot/feature/settings/bluetooth/BluetoothPreferenceSettingsSection.kt` (new) | Existing WF-10 Settings route displays Bluetooth suggestion P1 row, explicit Open, CE-20 Selected Bluetooth device copy, compact paired audio choices, Save/Remove/Cancel, permission request/Android Settings recovery, manual Refresh and textual feedback. No new destination, 48dp+ buttons, wraps at 200% and no paired data written to analytics. | Connection does not open Driving Mode, start music or activate microphone. No hidden Bluetooth scan/location prompt. |
| `app/src/main/java/com/latticeward/musiccopilot/app/MainActivity.kt` (narrow change) | One separate native `RequestPermission(BLUETOOTH_CONNECT)` registration for **explicit Settings button** only, fresh OS-grant read on result/onResume, explicit Android app-settings handoff on request. Android 10/11 treat Bluetooth as legacy normal permission. | ActivityResult/onResume **never** reads bonded list, starts Driving, plays media, records Voice, auto-requests new permission or pairs device. |
| `app/src/main/java/com/latticeward/musiccopilot/app/navigation/AppNavHost.kt` (narrow change) | Mounts section within existing `Destination.Settings.route`; existing app-scoped preferences repository reused with one `BluetoothSelectionViewModel`, Android paired-device adapter created with app context only. Back cancels draft. No other NavHost destination or Driving callback modified. | Existing Media3 session / #7/#8 transport, audio route and notification files unchanged. |
| `app/src/main/AndroidManifest.xml` | Adds legacy `BLUETOOTH` with `maxSdkVersion=30`, and Android 12+ `BLUETOOTH_CONNECT`. | No `BLUETOOTH_SCAN`, `BLUETOOTH_ADMIN`, `ACCESS_FINE_LOCATION`, discovery, background permission, or exported Bluetooth receiver. |
| `app/src/main/res/values/strings.xml` | 26 English-first CE-20 selection, disclosure, permission, Saved/Removed/error/fallback strings; failure and permission state never color-only. | No actual device MAC/name in repo strings or logs. |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue9Phase9BluetoothSelectionTest.kt` (new) | **18 authored JUnit methods, 0 run.** Opaque salted token/strict matching, explicit Open vs no auto enumeration, staged-but-unsaved, commit-time revalidation, changed pairing, permission revoked, storage failure rollback, Remove without permission, Cancel and BT-off states. All candidate IDs in tests are synthetic. | No Gradle, JVM, CI, phone, Bluetooth hardware or emulator execution. |

## Honest OS permission and state matrix

| Scenario | P09 approved response |
|---|---|
| Android 10/11 + already paired eligible audio | Read bonded devices from explicit Settings Open/Refresh with legacy BLUETOOTH normal permission, no runtime dialog |
| Android 12+ before `BLUETOOTH_CONNECT` | Show explanation + explicit Allow button; no enumeration, no scanning/location |
| Permission granted in Android result | Re-observe OS grant; **do not auto enumerate**. User selects Refresh before listing devices |
| Permission denied/permanently denied/revoked | Show limited functionality + explicit Android app settings, normal manual playback unaffected; saved token alone cannot authorize BT access |
| Radio off, no adapter or no paired eligible audio | Textual disabled/unavailable/empty feedback; no attempt to enable Bluetooth, auto-pair, start Driving or choose first device |
| User stages then Cancel/Back | Discard draft, unchanged committed token; no logs |
| User taps Save on valid paired audio | Re-check latest bonded list & OS permission; save local salted token only when DataStore confirms success |
| Pairing removed, permission lost or storage write failed during Save | No token update, no optimistic Saved, prior selection remains |
| User taps Remove with radio off/permission revoked | Clear existing DataStore preference explicitly, no change to Android paired devices; rollback on write error |
| Selected device physically connects after Save | **P09 deliberately does nothing**. P10 profile eligibility and P11 opt-in suggestion still pending, no autoplay or microphone |

## Privacy and regression constraints

- No actual raw MAC or device name is persisted: DataStore `bluetooth_hint` contains an opaque salted hash. Raw address/name exist only while displaying the explicitly accessed paired list. Screens/phone QA must not upload actual names, MACs or Bluetooth diagnostics. Default Android backup is disabled by app Manifest.
- Versioned token is a privacy-preserving *matching hint*, not a cryptographic secret or authentication credential. Previously persisted non-v1 strings are not trusted; user can Remove and select again. P10 must revalidate OS grant, audio profile/connected status and token match.
- Existing `PreferencesRepository` and data migrations unchanged; playback service/audio focus/AirPods/Android Player/MediaSession implementation of Issues #7/#8 not edited. Cloud STT remains disabled, first-run and media permissions unaffected. P1 can be removed without harming P0 Driving.

## Source-only QA gate

- [x] Uploaded source SHA matched; live GitHub CE-20/HF-10/FR-07 and actual DataStore/Settings/Activity manifest source consulted.
- [x] Existing preference slot and atomic ACK/recovery reused; no raw BT identity stored or exported.
- [x] Fresh Android paired output class/OS permission guard, disabled radio/permission and storage rollback authored.
- [x] In-route Settings CE-20 Save, Remove, Back/Cancel, explicit Android permission and disclosure authored.
- [x] 18 JUnit test source methods committed, **none executed**.
- [ ] Kotlin/Java compile, Gradle/JVM, Lint, Compose/Android/instrumentation tests, Actions, emulator, phone Bluetooth tests and APK: **NOT_RUN / NOT_BUILT**.
- [ ] All original 14 Issue #9 AC: **NOT_VERIFIED**; 46 owner M: **NOT_RUN**; 5 technical groups: **NOT_RUN**. Verify Android 10/12/14+ and second device later as explicitly deferred.
- [ ] P10 actual connected-profile event matching, P11 suggestion UX and P12 audit, P13 single cumulative build-only APK/owner QA remain pending. No PR, merge or Issue closure.

**P09 disposition: source-only scoped PASS; P10 NEXT. Do not treat unrun code as real Bluetooth runtime acceptance.**
