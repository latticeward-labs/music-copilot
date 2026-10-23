#!/usr/bin/env bash
# Compiles the REAL repository P03 pure Kotlin resolver against synthetic fakes.
# No network, no Android SDK, no Gradle, no Actions usage, no APK generation.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
command -v kotlinc >/dev/null || { echo "kotlinc missing"; exit 2; }
command -v java >/dev/null || { echo "java missing"; exit 2; }
KOTLIN_BIN="$(readlink -f "$(command -v kotlinc)")"
COROUTINES="$(dirname "$KOTLIN_BIN")/../lib/kotlinx-coroutines-core-jvm.jar"
test -s "$COROUTINES" || { echo "local Kotlin coroutines jar missing; use Gradle JUnit later"; exit 2; }
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
BASE="$ROOT/app/src/main/java/com/latticeward/musiccopilot"
kotlinc \
    "$BASE/core/model/Identifiers.kt" \
    "$BASE/core/result/AppResult.kt" \
    "$BASE/core/result/AppError.kt" \
    "$BASE/core/dispatchers/DispatcherProvider.kt" \
    "$BASE/data/mediastore/MediaStoreRefCodec.kt" \
    "$BASE/data/mediastore/MediaStorePlaybackResolver.kt" \
    "$ROOT/scripts/issue7_phase3_resolver_smoke.kt" \
    -cp "$COROUTINES" -include-runtime -d "$TMP/issue7-p03-resolver.jar"
java -cp "$TMP/issue7-p03-resolver.jar:$COROUTINES" Issue7_phase3_resolver_smokeKt
