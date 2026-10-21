#!/usr/bin/env bash
# P12 Android emulator OS-truth test. Deliberately no screenshots or private logs.
set -euo pipefail
API="${1:?Android API required}"
APP=com.latticeward.musiccopilot.debug
ACT=com.latticeward.musiccopilot.app.MainActivity
if [[ "$API" -ge 33 ]]; then
    PERM=android.permission.READ_MEDIA_AUDIO
else
    PERM=android.permission.READ_EXTERNAL_STORAGE
fi
case "$API" in 29|31|33|35|36) ;; *) echo "UNSUPPORTED_P12_API=$API" >&2; exit 2;; esac

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

# These assertions depend on real OS/dialog UI, not Java mock grants.
adb uninstall "$APP" >/dev/null 2>&1 || true
adb install -r app/build/outputs/apk/debug/app-debug.apk >/dev/null
python3 scripts/issue5_phase6_emulator_ui.py launch-first-use
python3 scripts/issue5_phase6_emulator_ui.py tap Continue
python3 scripts/issue5_phase6_emulator_ui.py tap Skip
python3 scripts/issue5_phase6_emulator_ui.py app 'Media access'

# Denied must not produce an optimistic library or a hidden music scan.
adb shell dumpsys package "$APP" > "$TMP/before.txt"
! grep -F "$PERM: granted=true" "$TMP/before.txt" | grep -Fq granted=true
python3 scripts/issue5_phase6_emulator_ui.py tap Continue
python3 scripts/issue5_phase6_emulator_ui.py system-deny
python3 scripts/issue5_phase6_emulator_ui.py app 'Access denied'
adb shell dumpsys package "$APP" > "$TMP/denied.txt"
grep -F "$PERM: granted=false" "$TMP/denied.txt" > /dev/null
python3 scripts/issue5_phase6_emulator_ui.py tap 'Not now'
python3 scripts/issue5_phase6_emulator_ui.py app 'Your local music'

# Fresh install means a fresh OS permission boundary. Never bypass via pm grant.
adb uninstall "$APP" >/dev/null
adb install -r app/build/outputs/apk/debug/app-debug.apk >/dev/null
python3 scripts/issue5_phase6_emulator_ui.py launch-first-use
python3 scripts/issue5_phase6_emulator_ui.py tap Continue
python3 scripts/issue5_phase6_emulator_ui.py tap Skip
python3 scripts/issue5_phase6_emulator_ui.py app 'Media access'
python3 scripts/issue5_phase6_emulator_ui.py tap Continue
python3 scripts/issue5_phase6_emulator_ui.py system-allow
python3 scripts/issue5_phase6_emulator_ui.py app 'Access granted'
adb shell dumpsys package "$APP" > "$TMP/granted.txt"
grep -F "$PERM: granted=true" "$TMP/granted.txt" >/dev/null
python3 scripts/issue5_phase6_emulator_ui.py tap Continue
python3 scripts/issue5_phase6_emulator_ui.py app 'Your local music'

# Notification runtime permission must not be installed/requested for MediaSession-only.
! grep -Fq "android.permission.POST_NOTIFICATIONS: granted" "$TMP/granted.txt"

# Crash text checked ephemerally; never retained as an artifact.
adb logcat -d -b crash > "$TMP/crash.txt"
! grep -Fq "Process: $APP" "$TMP/crash.txt"
echo "ISSUE5_P12_API${API}_REAL_OS_PERMISSION_DENY_ALLOW_MANUAL_GATE_PASS"
