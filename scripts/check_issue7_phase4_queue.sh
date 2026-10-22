#!/usr/bin/env bash
# Issue #7 P04: a repeatable, local, synthetic pure-Kotlin queue/worker smoke.
# No Android SDK, Gradle, CI, APK, real media files, or network access.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
command -v kotlinc >/dev/null || { echo "kotlinc is required"; exit 2; }
command -v java >/dev/null || { echo "java is required"; exit 2; }
KOTLIN_BIN="$(readlink -f "$(command -v kotlinc)")"
COROUTINES="$(dirname "$KOTLIN_BIN")/../lib/kotlinx-coroutines-core-jvm.jar"
test -s "$COROUTINES" || { echo "Kotlin coroutines core runtime missing"; exit 2; }
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
SRC="$ROOT/app/src/main/java/com/latticeward/musiccopilot"
kotlinc \
  "$SRC/core/model/Identifiers.kt" \
  "$SRC/core/dispatchers/DispatcherProvider.kt" \
  "$SRC/core/result/AppError.kt" \
  "$SRC/core/result/AppResult.kt" \
  "$SRC/playback/api/PlaybackCommand.kt" \
  "$SRC/playback/api/PlaybackSnapshot.kt" \
  "$SRC/playback/command/PlaybackQueuePolicy.kt" \
  "$SRC/playback/command/PlaybackNavigationPolicy.kt" \
  "$SRC/playback/command/PlaybackCommandProcessor.kt" \
  "$SRC/data/mediastore/MediaStoreRefCodec.kt" \
  "$SRC/data/mediastore/MediaStorePlaybackResolver.kt" \
  "$SRC/playback/service/ServiceQueueEngine.kt" \
  "$ROOT/scripts/issue7_phase4_queue_smoke.kt" \
  -cp "$COROUTINES" -include-runtime -d "$TMP/issue7-p04-smoke.jar"
java -cp "$TMP/issue7-p04-smoke.jar:$COROUTINES" Issue7_phase4_queue_smokeKt
