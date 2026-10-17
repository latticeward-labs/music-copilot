#!/usr/bin/env bash
set -eEuo pipefail
OUT="app/build/outputs/issue5-p09-api33"
APK="com.latticeward.musiccopilot.debug"
MIC="android.permission.RECORD_AUDIO"
mkdir -p "$OUT"
fail() {
  code="$?"
  adb exec-out screencap -p > "$OUT/failure.png" 2>/dev/null || true
  adb shell dumpsys package "$APK" > "$OUT/failure_permission.txt" 2>/dev/null || true
  adb logcat -d -b crash > "$OUT/crash_log.txt" 2>/dev/null || true
  echo ISSUE5_P09_NATIVE_FAILED_API33 >&2
  exit "$code"
}
trap fail ERR
tap_app() {
  if python3 scripts/issue5_phase6_emulator_ui.py tap "$1"; then return 0; fi
  # Long approved second slide is vertically scrollable at large font sizes.
  adb shell input swipe 500 1500 500 380 540
  python3 scripts/issue5_phase6_emulator_ui.py tap "$1"
}
adb uninstall "$APK" >/dev/null 2>&1 || true
adb install -r app/build/outputs/apk/debug/app-debug.apk
python3 scripts/issue5_phase6_emulator_ui.py launch-first-use
adb shell dumpsys package "$APK" > "$OUT/first_run_no_mic.txt"
! grep -Fq "$MIC: granted=true" "$OUT/first_run_no_mic.txt"
# P09: Exactly six supported intents; no additional value slide.
python3 scripts/issue5_phase8_onboarding_tap.py "$OUT" Continue
python3 scripts/issue5_phase6_emulator_ui.py app '6 supported commands'
adb shell dumpsys package "$APK" > "$OUT/voice_slide_no_mic.txt"
! grep -Fq "$MIC: granted=true" "$OUT/voice_slide_no_mic.txt"
tap_app 'Practice Voice'
python3 scripts/issue5_phase6_emulator_ui.py app 'Optional microphone access'
python3 scripts/issue5_phase6_emulator_ui.py app 'Continue to microphone permission'
adb shell dumpsys package "$APK" > "$OUT/rationale_no_mic.txt"
! grep -Fq "$MIC: granted=true" "$OUT/rationale_no_mic.txt"
tap_app 'Continue to microphone permission'
python3 scripts/issue5_phase6_emulator_ui.py system-deny
python3 scripts/issue5_phase6_emulator_ui.py app 'Microphone access was not granted. Voice practice is optional; continue using manual music controls.'
adb shell dumpsys package "$APK" > "$OUT/denied_mic_truth.txt"
grep -Fq "$MIC: granted=false" "$OUT/denied_mic_truth.txt"
adb exec-out screencap -p > "$OUT/denied_manual_fallback.png"
tap_app 'Not now — use manual controls'
tap_app 'Practice Voice'
tap_app 'Continue to microphone permission'
python3 scripts/issue5_phase6_emulator_ui.py system-allow
adb shell dumpsys package "$APK" > "$OUT/granted_mic_truth.txt"
grep -Fq "$MIC: granted=true" "$OUT/granted_mic_truth.txt"
python3 scripts/issue5_phase6_emulator_ui.py app 'Optional microphone access'
# Provider readiness is independent of permission. API33 emulator may lack
# an installed on-device speech provider; do not falsely require availability.
adb exec-out screencap -p > "$OUT/post_grant_provider_state.png"
# Onboarding completion/Skip cannot record anything or undo manual playback.
tap_app 'Skip'
python3 scripts/issue5_phase6_emulator_ui.py app 'Media access'
adb logcat -d -b crash > "$OUT/crash_log.txt"
! grep -Fq "Process: $APK" "$OUT/crash_log.txt"
echo ISSUE5_P09_NATIVE_JIT_MIC_AND_MANUAL_FALLBACK_PASS_API33
