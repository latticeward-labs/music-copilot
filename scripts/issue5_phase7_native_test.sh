#!/usr/bin/env bash
set -eEuo pipefail
API="$1"
OUT="app/build/outputs/issue5-p07-api$API"
APK="com.latticeward.musiccopilot.debug"
PERM="android.permission.READ_MEDIA_AUDIO"
if [ "$API" -lt 33 ]; then PERM="android.permission.READ_EXTERNAL_STORAGE"; fi
mkdir -p "$OUT"
fail() {
 code="$?"
 adb exec-out screencap -p > "$OUT/failure.png" 2>/dev/null || true
 adb shell dumpsys activity activities > "$OUT/failure_activity.txt" 2>/dev/null || true
 adb shell dumpsys package "$APK" > "$OUT/failure_permissions.txt" 2>/dev/null || true
 adb logcat -d -b crash > "$OUT/failure_crashes.txt" 2>/dev/null || true
 echo "ISSUE5_P07_NATIVE_FAILED_$API" >&2
 exit "$code"
}
trap fail ERR
adb uninstall "$APK" >/dev/null 2>&1 || true
adb install -r app/build/outputs/apk/debug/app-debug.apk
python3 scripts/issue5_phase6_emulator_ui.py launch-first-use
python3 scripts/issue5_phase6_emulator_ui.py tap Continue
python3 scripts/issue5_phase6_emulator_ui.py tap Skip
python3 scripts/issue5_phase6_emulator_ui.py app 'Media access'
python3 scripts/issue5_phase6_emulator_ui.py tap Continue
python3 scripts/issue5_phase6_emulator_ui.py system-deny
python3 scripts/issue5_phase6_emulator_ui.py app 'Access denied'
python3 scripts/issue5_phase6_emulator_ui.py tap 'Try again'
python3 scripts/issue5_phase6_emulator_ui.py tap Continue
python3 scripts/issue5_phase6_emulator_ui.py system-never-ask-if-present
python3 scripts/issue5_phase6_emulator_ui.py system-deny-or-os-fixed
adb shell dumpsys package "$APK" > "$OUT/two_denials.txt"
! grep -Fq "$PERM: granted=true" "$OUT/two_denials.txt"
if grep -F "$PERM: granted=false" "$OUT/two_denials.txt" | grep -Fq USER_FIXED; then
 python3 scripts/issue5_phase6_emulator_ui.py app 'Open Android Settings'
 python3 scripts/issue5_phase6_emulator_ui.py tap 'Open Settings'
 sleep 2
 adb shell dumpsys activity activities > "$OUT/native_settings_started.txt"
 grep -Eiq 'com.android.settings|subsettings' "$OUT/native_settings_started.txt"
 adb exec-out screencap -p > "$OUT/settings_before_cancel.png"
 adb shell input keyevent 4
 python3 scripts/issue5_phase6_emulator_ui.py app 'Open Android Settings'
 adb exec-out screencap -p > "$OUT/returned_without_grant.png"
 if [ "$API" -ge 33 ]; then
   # Third OS-owned return outcome. ADB clears USER_FIXED while genuine
   # Settings is foreground: this exercises fresh OS rationale projection,
   # but is NOT represented as a human tapping a Settings switch.
   python3 scripts/issue5_phase6_emulator_ui.py tap 'Open Settings'
   sleep 2
   adb shell pm clear-permission-flags "$APK" "$PERM" user-fixed
   adb shell dumpsys package "$APK" > "$OUT/requestable_os_flags.txt"
   grep -F "$PERM: granted=false" "$OUT/requestable_os_flags.txt" | grep -Fq USER_SET
   ! grep -F "$PERM: granted=false" "$OUT/requestable_os_flags.txt" | grep -Fq USER_FIXED
   adb shell input keyevent 4
   python3 scripts/issue5_phase6_emulator_ui.py app 'Access denied'
   adb exec-out screencap -p > "$OUT/settings_denied_requestable.png"
   echo "P07_REAL_OS_REQUESTABLE_RETURN_API$API"
   python3 scripts/issue5_phase6_emulator_ui.py tap 'Try again'
   python3 scripts/issue5_phase6_emulator_ui.py app 'Media access'
   python3 scripts/issue5_phase6_emulator_ui.py tap Continue
   python3 scripts/issue5_phase6_emulator_ui.py system-never-ask-if-present
   python3 scripts/issue5_phase6_emulator_ui.py system-deny-or-os-fixed
   python3 scripts/issue5_phase6_emulator_ui.py app 'Open Android Settings'
 fi
 python3 scripts/issue5_phase6_emulator_ui.py tap 'Open Settings'
 sleep 2
 python3 scripts/issue5_phase7_settings_ui.py grant "$API"
 python3 scripts/issue5_phase6_emulator_ui.py app 'Access granted'
 adb shell dumpsys package "$APK" > "$OUT/settings_grant.txt"
 grep -Fq "$PERM: granted=true" "$OUT/settings_grant.txt"
 adb exec-out screencap -p > "$OUT/granted_before_continue.png"
 python3 scripts/issue5_phase6_emulator_ui.py tap Continue
 python3 scripts/issue5_phase6_emulator_ui.py app 'Your local music'
 python3 scripts/issue5_phase6_emulator_ui.py app 'No local music is indexed yet.'
 adb exec-out screencap -p > "$OUT/after_explicit_scan.png"
 echo "P07_REAL_SETTINGS_CANCEL_RETURN_AND_GRANT_API$API"
else
 grep -F "$PERM: granted=false" "$OUT/two_denials.txt" | grep -Fq USER_SET
 python3 scripts/issue5_phase6_emulator_ui.py app 'Access denied'
 echo "P07_SETTINGS_ONLY_NOT_AVAILABLE_FOR_API$API"
fi
if [ -f "$OUT/settings_grant.txt" ]; then
 adb shell am start -a android.settings.APPLICATION_DETAILS_SETTINGS -d "package:$APK"
 sleep 2
 python3 scripts/issue5_phase7_settings_ui.py revoke "$API"
 adb shell dumpsys package "$APK" > "$OUT/revoked_os_truth.txt"
 grep -Fq "$PERM: granted=false" "$OUT/revoked_os_truth.txt"
 adb shell am start -W -n "$APK/com.latticeward.musiccopilot.app.MainActivity"
 if python3 scripts/issue5_phase6_emulator_ui.py app 'Music access was removed'; then
  echo "P07_REVOCATION_RECONCILED_IN_PROCESS_API$API"
 else
  python3 scripts/issue5_phase6_emulator_ui.py app 'Media access'
  echo "P07_REVOCATION_RECOVERED_AFTER_OS_PROCESS_KILL_API$API"
 fi
 adb exec-out screencap -p > "$OUT/post_revocation.png"
 adb logcat -d -b crash > "$OUT/crash_log.txt"
 ! grep -Fq "Process: $APK" "$OUT/crash_log.txt"
fi
echo "ISSUE5_P07_NATIVE_END_API$API"
