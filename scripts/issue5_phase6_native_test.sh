#!/usr/bin/env bash
set -eEuo pipefail
API="$1"
OUT="app/build/outputs/issue5-p06-api${API}"
APK="com.latticeward.musiccopilot.debug"
PERMISSION="android.permission.READ_EXTERNAL_STORAGE"
if [ "$API" -ge 33 ]; then PERMISSION="android.permission.READ_MEDIA_AUDIO"; fi
mkdir -p "$OUT"
on_native_failure() {
    status="$?"
    adb exec-out screencap -p > "$OUT/failure_screen.png" 2>/dev/null || true
    adb shell dumpsys activity activities > "$OUT/failure_activity.txt" 2>/dev/null || true
    adb logcat -d -b crash > "$OUT/failure_crashes.txt" 2>/dev/null || true
    adb logcat -d -v threadtime -t 1600 > "$OUT/failure_recent_logcat.txt" 2>/dev/null || true
    adb shell dumpsys package "$APK" > "$OUT/failure_package_permission.txt" 2>/dev/null || true
    echo "ISSUE5_P06_NATIVE_REAL_UI_FAILED_EXIT=$status" >&2
    exit "$status"
}
trap on_native_failure ERR
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell pm clear "$APK"
# Android 10 emulator sometimes resumes Home after the first cold-start
# intent even when am start -W returns OK. Require the actual app screen,
# retry up to 3 real launches, and fail immediately for app crash logs.
python3 scripts/issue5_phase6_emulator_ui.py launch-first-use
adb exec-out screencap -p > "$OUT/initial_window.png"
# A real first-use UI, not a fabricated route, was just asserted.
python3 scripts/issue5_phase6_emulator_ui.py tap Continue
python3 scripts/issue5_phase6_emulator_ui.py tap Skip
python3 scripts/issue5_phase6_emulator_ui.py app 'Media access'
adb exec-out screencap -p > "$OUT/rationale.png"
python3 scripts/issue5_phase6_emulator_ui.py tap Continue
python3 scripts/issue5_phase6_emulator_ui.py system-deny
python3 scripts/issue5_phase6_emulator_ui.py app 'Access denied'
python3 scripts/issue5_phase6_emulator_ui.py app 'Manual controls remain available.'
adb exec-out screencap -p > "$OUT/denied_requestable.png"
adb shell dumpsys package "$APK" > "$OUT/denied_os_truth.txt"
! grep -Fq "$PERMISSION: granted=true" "$OUT/denied_os_truth.txt"
# Try again returns to explanation, not a coercive native prompt loop.
python3 scripts/issue5_phase6_emulator_ui.py tap 'Try again'
python3 scripts/issue5_phase6_emulator_ui.py app 'Media access'
# A genuine second user denial creates USER_FIXED / "don't ask again"
# under Android platform policy; do not fake Android grant/deny status.
python3 scripts/issue5_phase6_emulator_ui.py tap Continue
python3 scripts/issue5_phase6_emulator_ui.py system-never-ask-if-present
python3 scripts/issue5_phase6_emulator_ui.py system-deny-or-os-fixed
adb shell dumpsys package "$APK" > "$OUT/twice_denied_native_flags.txt"
! grep -Fq "$PERMISSION: granted=true" "$OUT/twice_denied_native_flags.txt"
# Distinguish actual Android runtime permission flags, never synthesize.
# API29 can remain requestable after two real denials (USER_SET only).
if grep -F "$PERMISSION: granted=false" "$OUT/twice_denied_native_flags.txt" | grep -Fq "USER_FIXED"; then
    python3 scripts/issue5_phase6_emulator_ui.py app 'Open Android Settings'
    python3 scripts/issue5_phase6_emulator_ui.py app 'Permission must be changed explicitly.'
    adb exec-out screencap -p > "$OUT/settings_only.png"
    adb shell dumpsys package "$APK" > "$OUT/settings_only_os_truth.txt"
    ! grep -Fq "$PERMISSION: granted=true" "$OUT/settings_only_os_truth.txt"
    python3 scripts/issue5_phase6_emulator_ui.py tap 'Open Settings'
    sleep 2
    adb shell dumpsys activity activities > "$OUT/native_settings_activity.txt"
    grep -Eiq 'com.android.settings|subsettings' "$OUT/native_settings_activity.txt"
    adb shell input keyevent 4
    python3 scripts/issue5_phase6_emulator_ui.py app 'Open Android Settings'
    python3 scripts/issue5_phase6_emulator_ui.py tap 'Not now'
    python3 scripts/issue5_phase6_emulator_ui.py app 'Your local music'
    echo "ISSUE5_P06_OS_USER_FIXED_SETTINGS_ONLY_VERIFIED_API$API"
else
    grep -F "$PERMISSION: granted=false" "$OUT/twice_denied_native_flags.txt" | grep -Fq "USER_SET"
    python3 scripts/issue5_phase6_emulator_ui.py app 'Access denied'
    python3 scripts/issue5_phase6_emulator_ui.py app 'Manual controls remain available.'
    adb exec-out screencap -p > "$OUT/second_denial_requestable.png"
    python3 scripts/issue5_phase6_emulator_ui.py tap 'Not now'
    python3 scripts/issue5_phase6_emulator_ui.py app 'Your local music'
    echo "ISSUE5_P06_OS_USER_SET_REQUESTABLE_VERIFIED_API$API"
fi
# A TRUE uninstall/reinstall resets any native permission flags. "pm clear"
# only wipes app storage and may preserve Android runtime permission flags.
# Do not bypass an OS denial with "pm grant" or global controller resets.
adb uninstall "$APK"
adb install -r app/build/outputs/apk/debug/app-debug.apk
python3 scripts/issue5_phase6_emulator_ui.py launch-first-use
python3 scripts/issue5_phase6_emulator_ui.py tap Continue
python3 scripts/issue5_phase6_emulator_ui.py tap Skip
python3 scripts/issue5_phase6_emulator_ui.py app 'Media access'
python3 scripts/issue5_phase6_emulator_ui.py tap Continue
python3 scripts/issue5_phase6_emulator_ui.py system-allow
python3 scripts/issue5_phase6_emulator_ui.py app 'Access granted'
python3 scripts/issue5_phase6_emulator_ui.py app 'Continue to the blocked action.'
adb exec-out screencap -p > "$OUT/granted_before_explicit_continue.png"
adb shell dumpsys package "$APK" > "$OUT/granted_os_truth.txt"
grep -Fq "$PERMISSION: granted=true" "$OUT/granted_os_truth.txt"
python3 scripts/issue5_phase6_emulator_ui.py tap Continue
python3 scripts/issue5_phase6_emulator_ui.py app 'Your local music'
python3 scripts/issue5_phase6_emulator_ui.py app 'No local music is indexed yet.'
adb exec-out screencap -p > "$OUT/post_grant_scan.png"
adb logcat -d -b crash > "$OUT/crash_buffer.txt"
! grep -Fq "Process: $APK" "$OUT/crash_buffer.txt"
# The outcome set is OS-dependent: API29 may remain USER_SET/requestable
# after two native denials. Never claim Settings-only if OS did not create it.
echo "ISSUE5_P06_ANDROID_API${API}_TRUTHFUL_NATIVE_RESULT_AND_GRANT_PASS"
