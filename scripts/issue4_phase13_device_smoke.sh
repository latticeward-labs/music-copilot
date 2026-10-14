#!/usr/bin/env bash
# One actual adb-connected installation. "emulator" evidence NEVER counts as
# physical-device proof. Phone operators run the same script with "physical".
set -euo pipefail
kind="${1:?Usage: $0 emulator|physical output-directory expected-api label}"
out="${2:?Output directory required}"
expected_api="${3:?Expected Android API required}"
label="${4:?Non-sensitive evidence label required}"
app="com.latticeward.musiccopilot.debug"
activity="com.latticeward.musiccopilot.app.MainActivity"
[[ "$kind" == emulator || "$kind" == physical ]] || { echo "INVALID_KIND"; exit 1; }
[[ "$expected_api" =~ ^[0-9]+$ ]] || exit 1
[[ "$label" =~ ^[a-zA-Z0-9_-]{3,48}$ ]] || exit 1
mkdir -p "$out"
test -s app/build/outputs/apk/debug/app-debug.apk
adb start-server >/dev/null
timeout 150 adb wait-for-device
api="$(adb shell getprop ro.build.version.sdk | tr -d '\r')"
release="$(adb shell getprop ro.build.version.release | tr -d '\r')"
maker="$(adb shell getprop ro.product.manufacturer | tr -d '\r')"
model="$(adb shell getprop ro.product.model | tr -d '\r')"
qemu="$(adb shell getprop ro.kernel.qemu | tr -d '\r')"
[[ "$api" == "$expected_api" ]] || { echo "WRONG_API: got $api expected $expected_api"; exit 1; }
if [[ "$kind" == emulator ]]; then
  [[ "$qemu" == "1" ]] || { echo "NOT_AN_EMULATOR"; exit 1; }
else
  [[ "$qemu" != "1" ]] || { echo "EMULATOR_MUST_NOT_COUNT_AS_PHYSICAL"; exit 1; }
fi
# The app does not request INTERNET; no user songs are copied to the device.
! grep -q 'android.permission.INTERNET' app/src/main/AndroidManifest.xml
adb install -r app/build/outputs/apk/debug/app-debug.apk > "$out/install.txt"
grep -Fq 'Success' "$out/install.txt"
adb shell pm clear "$app" > "$out/clear.txt"
grep -Fq 'Success' "$out/clear.txt"
# Don't archive dumpsys package; only record allow-listed boolean evidence.
permission_state="$(adb shell dumpsys package "$app")"
for perm in android.permission.RECORD_AUDIO android.permission.READ_EXTERNAL_STORAGE android.permission.READ_MEDIA_AUDIO; do
  if [[ "$permission_state" == *"$perm: granted=true"* ]]; then
    echo "UNEXPECTED_PERMISSION_GRANT: $perm"
    exit 1
  fi
done
adb shell am start -W -n "$app/$activity" > "$out/cold_start.txt"
grep -Eq 'Status: ok|Status: OK' "$out/cold_start.txt"
sleep 6
[[ -n "$(adb shell pidof "$app" | tr -d '\r')" ]] || { echo "PROCESS_NOT_RUNNING"; exit 1; }
adb shell dumpsys activity activities | grep -Fq "$app" || { echo "ACTIVITY_NOT_REGISTERED"; exit 1; }
adb exec-out screencap -p > "$out/cold_start.png"
test "$(stat -c '%s' "$out/cold_start.png")" -gt 1000
# Activity/background smoke, not complete MediaSession or Bluetooth certification.
adb shell input keyevent KEYCODE_HOME
sleep 2
adb shell am start -W -n "$app/$activity" > "$out/foreground_return.txt"
[[ -n "$(adb shell pidof "$app" | tr -d '\r')" ]] || { echo "FOREGROUND_RETURN_PROCESS_MISSING"; exit 1; }
# This is an explicit simulated cold process restart, not a genuine low-memory kill.
adb shell am force-stop "$app"
sleep 2
adb shell am start -W -n "$app/$activity" > "$out/after_force_stop.txt"
grep -Eq 'Status: ok|Status: OK' "$out/after_force_stop.txt"
sleep 4
[[ -n "$(adb shell pidof "$app" | tr -d '\r')" ]] || { echo "PROCESS_RESTART_FAILED"; exit 1; }
crash="$(adb logcat -d -b crash)"
if [[ "$crash" == *"Process: $app"* ]]; then
  echo "APP_CRASH_IN_BUFFER"
  exit 1
fi
apk_hash="$(sha256sum app/build/outputs/apk/debug/app-debug.apk | cut -d' ' -f1)"
export PHASE13_OUT="$out" PHASE13_KIND="$kind" PHASE13_LABEL="$label" PHASE13_API="$api" PHASE13_RELEASE="$release"
export PHASE13_MAKER="$maker" PHASE13_MODEL="$model" PHASE13_QEMU="$qemu" PHASE13_APK_HASH="$apk_hash"
python3 - <<'PY'
import datetime, json, os, pathlib, subprocess
e=os.environ
record={
  "schema_version":1,
  "device_type":e["PHASE13_KIND"],
  "label":e["PHASE13_LABEL"],
  "manufacturer":e["PHASE13_MAKER"],
  "model":e["PHASE13_MODEL"],
  "android_api":int(e["PHASE13_API"]),
  "android_release":e["PHASE13_RELEASE"],
  "ro_kernel_qemu":e["PHASE13_QEMU"],
  "commit_sha":subprocess.check_output(["git","rev-parse","HEAD"],text=True).strip(),
  "apk_sha256":e["PHASE13_APK_HASH"],
  "timestamp_utc":datetime.datetime.now(datetime.timezone.utc).isoformat(),
  "install_pass":True,
  "default_permissions_denied_pass":True,
  "cold_launch_pass":True,
  "background_foreground_pass":True,
  "force_stop_restart_pass":True,
  "app_crash_absent_pass":True,
  "synthetic_only":True,
}
path=pathlib.Path(e["PHASE13_OUT"])/"evidence.json"
path.write_text(json.dumps(record,indent=2,ensure_ascii=False)+"\n")
print(f"PHASE13_{e['PHASE13_KIND'].upper()}_SMOKE PASS: {record['label']} API {record['android_api']} commit={record['commit_sha'][:10]}, no runtime permissions, no app crash; no user media or device serial captured")
PY
