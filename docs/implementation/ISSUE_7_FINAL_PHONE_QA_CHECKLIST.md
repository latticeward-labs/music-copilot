# Issue #7 — FINAL single-APK owner phone QA (P13 only)

**State:** PLAN ONLY — all M01–M36 and T01–T15 are initially **NOT_RUN**. This document is **not** a record of successful testing.
**App:** ONE integrated, installable debug APK generated **after P00–P12**; NEVER a different per-phase APK. Capture exact `source_commit`, `apk_sha256`, `apk_url`, `build_run_url` (if any), `android_version`, `device_model`, `test_date`.
**Source:** https://github.com/latticeward-labs/music-copilot/issues/7 — `ISSUE_7_PHASED_EXECUTION_PLAN.md` — approved English MSM2 and ADR-0002/0007. No #7 APK was produced by planning.

## How the owner tests

1. Install **the final P13 APK** on your Android phone. Put at least **3–5 playable local audio files** on your device; preferably one with a clearly different duration and a recognizable order. Allow Music/Audio access when requested. Have headphones/Bluetooth available **only for their corresponding cases**; do not buy extra gear for testing.
2. For each M-case follow the action and compare to expected behavior. Send the case ID and `PASS`, `FAIL`, `BLOCKED`, `NOT_RUN` or `N/A`, with a brief observation on FAIL/BLOCKED. Do not mark unperformed cases PASS.
3. `PASS` = you actually observed the described behavior. `FAIL` = expected and actual differ. `BLOCKED` = setup/device/feature not available. `N/A` = genuinely not applicable; explain. `NOT_RUN` = not attempted.
4. **Safety note:** use headphones/Bluetooth disconnection or call tests only when safe; never perform a driving experiment while driving. No vehicle or second Android device is required for the owner's first phone pass. Mark their cases NOT_RUN/deferred.
5. Screenshots/logs optional; never include names/paths/URIs of personal songs or any raw recordings in public GitHub evidence. Do not infer audible playback from a successful UI animation.
6. To preserve results, send plain text e.g. `M03 PASS — audio heard on phone speaker`, `M27 BLOCKED — no Bluetooth device`. We will bind all reported cases to the **same APK hash** in the Issue and evidence ledger.

## Final phone cases — all from same consolidated APK

| ID | Test action (owner) | Expected observable result |
|---|---|---|
| **M01** | Fresh install / open Player before selecting music. | No music autoplays; clear No Track/empty transport state; no fake Playing. |
| **M02** | Complete media permission + Library browse or Search, then tap a valid local song. | Selected real file opens in Player and begins actual audible playback (or a truthful buffering/permission error); no stale simulated success. |
| **M03** | Listen to the selected song for 10–15 seconds. | Correct selected file is audible; title/progress/current item consistent with selected Library item. |
| **M04** | Tap Pause on Player. | Audible sound stops and UI shows Paused, without clearing selected song. |
| **M05** | Tap Play after Paused. | Same track audibly resumes near its paused position, not from a random file. |
| **M06** | While playing, tap another song in Search or Library. | New selection plays; current item and queue reflect that new explicit selection; no overlapping audio. |
| **M07** | Move Player seek slider to around the middle of a track. | Playback moves near selected timestamp; progress/display reflect real service position. |
| **M08** | Start near 0–5s and use Seek Back 10s. | Position clamps to 0; no negative time or crash. |
| **M09** | Seek near the end of a finite-duration song. | Position is clamped to valid duration; natural end-of-song state/navigation follows queue policy. |
| **M10** | Play a multi-track queue and press Next once. | Exactly next queued song plays; title/current index and actual audio agree. |
| **M11** | Press Previous near beginning of track. | Previous queued song (if one exists), else documented first-item policy; no accidental wrap. |
| **M12** | Play track > approved Previous threshold and press Previous. | Restarts current track at 0 as specified; does not jump unpredictably. |
| **M13** | Open queue from approved Player or Library/album context. | Queue order/current marker match playback source of truth; tapping queued track follows the same service. |
| **M14** | Move to last queued track and press Next / allow last track to finish. | Explicit end-of-queue behavior (no phantom next, random wrap or looping unless the reviewed policy explicitly permits it). |
| **M15** | Start song then press Android Home; stay elsewhere ~30 seconds. | Audio continues without UI visible. |
| **M16** | Lock/turn screen off during audible playback for ~30 seconds. | Playback continues without interruption while screen off. |
| **M17** | Rotate device or recreate Activity while playing (if rotation supported). | Audio continues, no second/overlapping player, synchronized current item/time after recreation. |
| **M18** | Swipe the app task away from Recents while playing, then listen. | Legitimate active playback continues on the service within supported task-closure behavior. |
| **M19** | Reopen app during M15–M18 active playback. | Full Player/MiniPlayer state returns from connected service, no autoplay restart or position reset. |
| **M20** | During playback pull down Android notifications. | Real media notification with recognizable track and valid media controls is present, subject to OS surface behavior. |
| **M21** | Press Pause then Play from system media notification. | Actual sound and in-app state follow both commands. |
| **M22** | Use Next/Previous notification actions when applicable. | Same current queue/current index shown in Player; no separate notification queue. |
| **M23** | Lock phone and use displayed lock-screen media controls (if this OS exposes them). | Lock-screen control changes real playback; app state matches on unlock; if OS hides controls note BLOCKED/N/A. |
| **M24** | With wired headset and media key if available, press Pause/Play. | Physical media key controls same Player, not a duplicate playback instance. |
| **M25** | Connect Bluetooth headset/speaker with controls; Play/Pause/Next via Bluetooth buttons. | Media keys control service state correctly and audio routes to connected device. |
| **M26** | During wired-headset playback unplug wired headphones. | Playback immediately safe-pauses; **no unintended switch to phone-speaker audio**. |
| **M27** | During Bluetooth playback disconnect/power off headset. | Playback safe-pauses, no unexpected phone speaker sound; reconnect does not auto-resume without user command. |
| **M28** | Receive a real incoming call or test temporary audio focus interruption and manual pause. | Playback obeys approved focus/call pause/duck/guarded resume policy; a manual Pause is never overridden by auto-resume. |
| **M29** | Select a deleted/moved file still visible in stale Library UI or removed from queue. | Safe error/skip; no crash, no fake sound, remaining valid queue continues where applicable. |
| **M30** | Include one deliberately invalid/truncated local audio test file within a queue. | Corrupt item is skipped with non-sensitive recoverable state; valid next file remains playable, no infinite skip loop. |
| **M31** | Revoke Music/Audio permission in Android Settings, then return to app/retry play. | Denied access is respected; recovery is displayed, no unauthorized read or stale playback-success claim. |
| **M32** | Build queue, pause, close UI, then reopen while service session remains alive. | Live service's actual queue/current index remains authoritative, not overwritten by DataStore. |
| **M33** | Persist queue/position, let service end normally, then relaunch app for a cold restore. | Valid queue and clamped position restore at P1 level as **Paused**, never autoplay. |
| **M34** | After cold restore from M33, tap Play manually. | Only explicit user Play resumes audio at the restored valid position. |
| **M35** | Optionally force-stop app in Android Settings, then relaunch to inspect behavior. | No claim that P0 playback survives process death/force-stop. If a P1 snapshot is restorable it is Paused and never silently autoplays; record OS limitation accurately. |
| **M36** | Use app in Airplane mode or with internet off for local playback. | Local tracks and player work offline; no login, streaming, network catalog or download required. |

**Prerequisite caution:** M10–M14 require a real multi-track queue source and the documented Previous rule; M24–M28 require optional external devices or second party. When unavailable, report **BLOCKED** rather than PASS. M29–M31 are fault-injection cases; do not corrupt your only copy of personal media.

## Technical QA ledger — execution evidence required, never inferred from APK installation

| ID | Technical verification | Required record |
|---|---|---|
| **T01** | Queue policy JVM tests: replace, order, indexes, first/last, end, duplicate, empty, bounded input, selected item. | Commands + pass counts + log link |
| **T02** | Previous threshold, seek ±10s/absolute, clamping, duration unknown/overflow. | JVM test report |
| **T03** | Command serialization 32-slot capacity, cancellation/overflow/error ACK, no silent drop; service/System control parity. | JVM/concurrency report |
| **T04** | Service owns exactly one Player, foreground/background/lifecycle and no UI recreation destruction; Android integration tests. | Instrumentation report |
| **T05** | Gateway/controller reconnect, atomic full snapshot, revision monotonicity, no optimistic UI, external control parity. | Service/controller integration tests |
| **T06** | MediaSession permission/export/internal custom action confinement, notification/media keys; Android target SDK restrictions. | Manifest/static/instrumentation evidence |
| **T07** | Issue #6 MediaStore re-query, URI/volume, permission revoke, stale Library selection → real queue and actual player. | Fake provider + integration tests |
| **T08** | Corrupt/missing/unreadable queue, controlled skip with bounded recovery, typed errors without raw sensitive metadata. | Fault injection test output |
| **T09** | Playing/Paused/No Track/Buffering/Error/UI semantics, TalkBack and 150/200% font where measurable. | Compose instrumentation & accessibility report |
| **T10** | P1 Proto DataStore versioned snapshot, atomic write/checkpoint (5s), schema/permission errors, live session precedence. | DataStore/restore tests |
| **T11** | P1 cold restore: media requery, missing item removal, position clamp, **Paused-only**, explicit manual resume. | Integration/field report |
| **T12** | Document UI close vs service termination vs process death/force-stop; no false P0 promises; one-device matrix exceptions. | Process-limit audit |
| **T13** | No streaming/catalog/secret endpoints, no forbidden personal metadata in errors, telemetry/logs or DataStore payloads. | Privacy/static tests |
| **T14** | Final build `assembleDebug`, APK installability/signature/SHA, Lint outcome and any compile/AndroidTest failures recorded, not hidden. | Commit/hash/build+lint logs |
| **T15** | Full Issue #7 AC traceability, regression against #6/#5, release/PR reviewer audit, explicit post-MVP test debt where authorized. | Signed-off evidence index, review/merge record |

## Result template (copy only when submitting real phone results)

```text
Issue: #7
APK SHA-256: [from final P13 evidence; never guess]
Device model:
Android version:
Date:
M01: NOT_RUN
M02: NOT_RUN
...
M36: NOT_RUN
Notes / observed failures:
```

**Closeout policy:** If the owner reports some tests later, record exact reported statuses and dates. Never bulk-PASS, treat N/A as PASS, merge on unverified safety behavior, or overwrite Issue #6/#74 historical evidence. CI/JVM/Android validation is a separate technical category and may be NOT_RUN even when a phone case is PASS.

## P12 source-only integration handoff — no early PASS

[Cross-phase P12 readiness report](ISSUE_7_PHASE12_CROSS_PHASE_INTEGRATION_READINESS_AUDIT.md) committed six fixes: Android WAKE_LOCK required by `C.WAKE_MODE_LOCAL`, command-receipt generation fencing of stale MediaStore queue requests, detached old-controller result rejection and longer bounded large-queue acknowledgements, no Application-constructor premature MediaSessionService bind, legitimate recovered-track playback surviving Recents with position checkpoints, correct WF-06 Queue shortcut with error panel.

**Do not execute this list on an interim build:** only the **one P13 cumulative debug APK** may be handed to the owner. Before performing M01–M36, write exact **APK SHA-256, commit SHA, Android device version and local install outcome**. The assistant has **not** run a P12 build/test on any platform, nor dispatched GitHub Actions. If the non-Actions Android SDK/Gradle toolchain is unavailable in P13, record **BLOCKED** rather than publish an unverified APK. Phone tests M01–M36 are owner-observed; technical T01–T15 need their own genuine logs/test evidence and cannot be inferred from tapping a screen.

Specifically check: **M15–M19** (Home/Recents/reconnect), **M20–M25** (OS notification/BT media keys), **M26–M28** (headset unplug/BT/call and manual Pause priority), **M29–M31** (real corrupt/removed/revoked local media using safe disposable copies), **M32–M35** (live session versus cold Safe-Paused/force-stop), and **M36** (airplane mode). New P12 source contracts: 15 JUnit + 2 Android read-only instrumentation cases **authored only; NOT_RUN**. Do not claim perfect no-speaker-leak or process-death playback guarantee based on static sources.
