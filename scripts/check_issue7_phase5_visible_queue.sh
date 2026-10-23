#!/usr/bin/env bash
# Issue #7 P05: pure Kotlin source contract, no Gradle/Actions/Android/APK.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
command -v kotlinc >/dev/null || { echo "kotlinc missing"; exit 2; }
command -v java >/dev/null || { echo "java missing"; exit 2; }
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
SRC="$ROOT/app/src/main/java/com/latticeward/musiccopilot"
kotlinc \
  "$SRC/core/model/Identifiers.kt" \
  "$SRC/core/application/playback/VisiblePlaybackQueuePolicy.kt" \
  "$ROOT/scripts/issue7_phase5_visible_queue_smoke.kt" \
  -include-runtime -d "$TMP/issue7-p05-visible-queue.jar"
java -jar "$TMP/issue7-p05-visible-queue.jar"
