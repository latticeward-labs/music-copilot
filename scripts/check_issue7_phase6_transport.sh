#!/usr/bin/env bash
# Issue #7 P06: compiles exact in-repository pure Kotlin sources (no Android
# SDK, no network, no Gradle or Actions, no APK, no personal media).
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
command -v kotlinc >/dev/null || { echo "kotlinc missing"; exit 2; }
command -v java >/dev/null || { echo "java missing"; exit 2; }
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
SRC="$ROOT/app/src/main/java/com/latticeward/musiccopilot"
kotlinc \
  "$SRC/core/model/Identifiers.kt" \
  "$SRC/playback/api/PlaybackCommand.kt" \
  "$SRC/playback/command/SystemTransportPolicy.kt" \
  "$ROOT/scripts/issue7_phase6_system_transport_smoke.kt" \
  -include-runtime -d "$TMP/issue7-p06-transport.jar"
java -jar "$TMP/issue7-p06-transport.jar"
