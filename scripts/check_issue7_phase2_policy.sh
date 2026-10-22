#!/usr/bin/env bash
# Issue #7 P02: offline pure Kotlin contract checks; no Gradle/Android/Actions/APK.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
command -v kotlinc >/dev/null || { echo "kotlinc is required"; exit 2; }
command -v java >/dev/null || { echo "java is required"; exit 2; }
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
BASE="$ROOT/app/src/main/java/com/latticeward/musiccopilot"
kotlinc \
  "$BASE/core/model/Identifiers.kt" \
  "$BASE/playback/api/PlaybackCommand.kt" \
  "$BASE/playback/command/PlaybackQueuePolicy.kt" \
  "$BASE/playback/command/PlaybackNavigationPolicy.kt" \
  "$ROOT/scripts/issue7_phase2_policy_smoke.kt" \
  -include-runtime -d "$TMP/issue7-p02-policy.jar"
java -jar "$TMP/issue7-p02-policy.jar"
