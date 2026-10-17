#!/usr/bin/env bash
set -eEuo pipefail
OUT="app/build/outputs/issue5-p08-api33"
APK="com.latticeward.musiccopilot.debug"
PERM="android.permission.READ_MEDIA_AUDIO"
mkdir -p "$OUT"
fail() {
  code="$?"
  adb exec-out screencap -p > "$OUT/failure.png" 2>/dev/null || true
  adb shell dumpsys package "$APK" > "$OUT/failure_os_permission.txt" 2>/dev/null || true
  adb logcat -d -b crash > "$OUT/failure_crash_log.txt" 2>/dev/null || true
  echo "ISSUE5_P08_NATIVE_FAILED_API33" >&2
  exit "$code"
}
trap fail ERR
adb uninstall "$APK" >/dev/null 2>&1 || true
adb install -r app/build/outputs/apk/debug/app-debug.apk
python3 scripts/issue5_phase6_emulator_ui.py launch-first-use
python3 scripts/issue5_phase6_emulator_ui.py tap Continue
python3 scripts/issue5_phase6_emulator_ui.py tap Skip
python3 scripts/issue5_phase6_emulator_ui.py app 'Media access'
adb shell dumpsys package "$APK" > "$OUT/denied_before_scan.txt"
! grep -Fq "$PERM: granted=true" "$OUT/denied_before_scan.txt"
# OS denial may never produce verified Empty or Ready.
python3 scripts/issue5_phase6_emulator_ui.py tap Continue
python3 scripts/issue5_phase6_emulator_ui.py system-allow
python3 scripts/issue5_phase6_emulator_ui.py app 'Access granted'
adb shell dumpsys package "$APK" > "$OUT/grant_os_truth.txt"
grep -Fq "$PERM: granted=true" "$OUT/grant_os_truth.txt"
python3 scripts/issue5_phase6_emulator_ui.py tap Continue
python3 scripts/issue5_phase6_emulator_ui.py app 'Your local music'
python3 scripts/issue5_phase6_emulator_ui.py app 'No local music is indexed yet.'
adb exec-out screencap -p > "$OUT/verified_empty_after_grant.png"
# A second scan is never automated: a new scan requires a real user Rescan tap.
python3 scripts/issue5_phase6_emulator_ui.py tap 'Rescan local music'
python3 scripts/issue5_phase6_emulator_ui.py app 'No local music is indexed yet.'
adb exec-out screencap -p > "$OUT/explicit_rescan_empty.png"
# Native PermissionController revocation is already covered in Phase 7.
# For P08, use an explicit test-only ADB revocation of OS permission truth:
# the tested app cannot alter this state or spoof MediaStore authorization.
# Do NOT represent this fixture as a manual Android Settings UI tap.
adb shell pm revoke "$APK" "$PERM"
adb shell dumpsys package "$APK" > "$OUT/revoked_os_truth.txt"
grep -Fq "$PERM: granted=false" "$OUT/revoked_os_truth.txt"
echo "P08_OS_REVOKE_ADB_FIXTURE_VERIFIED_NOT_MANUAL_SETTINGS"
adb shell am start -W -n "$APK/com.latticeward.musiccopilot.app.MainActivity"
python3 scripts/issue5_phase6_emulator_ui.py app 'Media access'
adb exec-out screencap -p > "$OUT/unavailable_after_revoke.png"
adb logcat -d -b crash > "$OUT/crash_log.txt"
! grep -Fq "Process: $APK" "$OUT/crash_log.txt"
echo ISSUE5_P08_NATIVE_PERMISSION_READINESS_PASS_API33
