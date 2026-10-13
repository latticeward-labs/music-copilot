#!/usr/bin/env python3
"""Issue #4 / ADR-0008: fail-closed source dependency and package topology guard.

Scans Kotlin/Java imports AND explicitly qualified symbols in root production app.
Uses Python stdlib only. This is a package-level safety net; it is not a Kotlin
compiler, and semantic state ownership/lifetimes still need tests and review.
"""
from __future__ import annotations

import argparse
import re
import sys
import tempfile
from dataclasses import dataclass
from pathlib import Path

PREFIX = "com.latticeward.musiccopilot"
PRODUCTION = Path("app/src/main/java/com/latticeward/musiccopilot")
LEAVES = ("core.model", "core.result", "core.time", "core.dispatchers")
PORTS = ("playback.api", "voice.api", "data.api", "quality.api")
STRUCTURE = tuple("""
app app/navigation app/di
core core/model core/result core/time core/dispatchers
core/application core/application/bootstrap core/application/permission
core/application/library core/application/playback core/application/driving
core/application/voice core/application/settings core/domain
feature feature/onboarding feature/permission feature/library feature/search
feature/player feature/driving feature/settings feature/liked
playback playback/api playback/controller playback/service playback/media3
playback/command playback/interruption playback/restore
voice voice/api voice/session voice/mapper voice/android
data data/api data/mediastore data/room data/room/migration data/datastore
data/repository data/mapping
quality quality/api quality/privacy quality/noop quality/adapters
""".split())

# The comment and string filter avoids false positives in comments and UI text.
LITERAL = re.compile(
    r'/\*[\s\S]*?\*/|"""[\s\S]*?"""|"(?:\\.|[^"\\])*"|'
    r"'(?:\\.|[^'\\])*'|//[^\r\n]*"
)
PACKAGE = re.compile(r"(?m)^[ \t]*package[ \t]+([A-Za-z_]\w*(?:\.[A-Za-z_]\w*)*)")
IMPORT = re.compile(
    r"(?m)^[ \t]*import[ \t]+(?:static[ \t]+)?"
    r"([A-Za-z_]\w*(?:\.[A-Za-z_*]\w*|\.\*)*)"
)
QUALIFIED = re.compile(
    r"(?<![\w.])(?:com\.latticeward\.musiccopilot|androidx|android|"
    r"com\.google\.firebase|com\.google\.android\.gms|io\.sentry)"
    r"(?:\.[A-Za-z_]\w*)+"
)
SETTINGS_INCLUDE = re.compile(r'(?m)^[ \t]*include[ \t]*\([ \t]*["\x27]:([\w-]+)["\x27][ \t]*\)')
SDK_DENY = (
    "androidx.media3", "androidx.room", "androidx.datastore",
    "android.speech", "android.database", "android.provider.MediaStore",
    "android.media.session", "com.google.firebase",
    "com.google.android.gms.analytics", "com.google.android.gms.ads",
    "io.sentry",
)
QUALITY_DENY = (
    "androidx.media3", "androidx.room", "androidx.datastore",
    "android.speech", "android.database", "android.provider.MediaStore",
    "androidx.compose", "androidx.activity",
)
MEDIA_DENY = ("androidx.room", "androidx.datastore", "android.speech",
              "androidx.compose", "com.google.firebase", "io.sentry")
DATA_DENY = ("androidx.media3", "androidx.compose", "androidx.activity",
             "android.speech", "com.google.firebase", "io.sentry")
VOICE_DENY = ("androidx.media3", "androidx.room", "androidx.datastore",
              "androidx.compose", "androidx.activity",
              "com.google.firebase", "io.sentry")


@dataclass(frozen=True)
class Violation:
    path: str
    detail: str

    def __str__(self) -> str:
        return f"{self.path}: {self.detail}"


def matches(target: str, prefix: str) -> bool:
    return target == prefix or target.startswith(prefix + ".")


def erase_literals(text: str) -> str:
    return LITERAL.sub(
        lambda m: "".join("\n" if c == "\n" else " " for c in m.group(0)), text
    )


def allowed_internal(source: str, target: str) -> bool:
    if target == "R" or matches(target, "R"):
        return source == "app" or source.startswith(("app.", "feature.", "playback.", "voice.", "data."))
    if target == "BuildConfig":
        return matches(source, "app.di")
    if matches(source, "app.di"):
        return True  # Sole concrete-binding composition root.
    if matches(source, "app"):
        return (
            matches(target, "app")
            or matches(target, "feature")
            or matches(target, "core.application")
            or any(matches(target, leaf) for leaf in LEAVES)
            or any(matches(target, port) for port in PORTS)
        )
    if any(matches(source, leaf) for leaf in LEAVES):
        # Phase 4: AppError.MediaItem in core.result must type its opaque
        # MediaRef from core.model. Permit this single leaf-to-leaf edge.
        return (any(matches(source, leaf) and matches(target, leaf) for leaf in LEAVES)
                or (matches(source, "core.result") and matches(target, "core.model")))
    if matches(source, "core.domain"):
        return matches(target, "core.domain") or any(
            matches(target, leaf) for leaf in LEAVES
        )
    if matches(source, "core.application"):
        return (
            matches(target, "core.application")
            or matches(target, "core.domain")
            or any(matches(target, leaf) for leaf in LEAVES)
            or any(matches(target, port) for port in PORTS)
        )
    if any(matches(source, port) for port in PORTS):
        return any(matches(source, port) and matches(target, port) for port in PORTS) or any(
            matches(target, leaf) for leaf in LEAVES
        )
    if matches(source, "feature"):
        owner = ".".join(source.split(".")[:2])
        return (
            matches(target, owner)
            or matches(target, "core.application")
            or any(matches(target, leaf) for leaf in LEAVES)
            or any(matches(target, port) for port in PORTS)
        )
    if matches(source, "playback"):
        return (
            matches(target, "playback")
            or any(matches(target, leaf) for leaf in LEAVES)
            or matches(target, "data.api.RestorableQueueStore")
        )
    if matches(source, "voice"):
        return (
            matches(target, "voice")
            or any(matches(target, leaf) for leaf in LEAVES)
            or matches(target, "playback.api")
            or matches(target, "data.api.LibraryRepository")
        )
    if matches(source, "data"):
        return matches(target, "data") or any(matches(target, leaf) for leaf in LEAVES)
    if matches(source, "quality"):
        return matches(target, "quality") or any(matches(target, leaf) for leaf in LEAVES)
    return False


def forbidden_external(source: str, symbol: str) -> bool:
    if source.startswith("core.") or any(matches(source, port) for port in PORTS):
        return matches(symbol, "android") or matches(symbol, "androidx") or any(
            matches(symbol, z) for z in SDK_DENY
        )
    if source.startswith("feature."):
        return any(matches(symbol, z) for z in SDK_DENY)
    if source.startswith("playback."):
        return any(matches(symbol, z) for z in MEDIA_DENY)
    if source.startswith("voice."):
        if any(matches(symbol, z) for z in VOICE_DENY):
            return True
        if matches(symbol, "android.speech"):
            return not matches(source, "voice.android")
        return False
    if source.startswith("data."):
        if any(matches(symbol, z) for z in DATA_DENY):
            return True
        if matches(symbol, "android.provider.MediaStore") or matches(
            symbol, "android.database"
        ):
            return not matches(source, "data.mediastore")
        return False
    if source.startswith("quality."):
        if any(matches(symbol, z) for z in QUALITY_DENY):
            return True
        if matches(symbol, "com.google.firebase") or matches(
            symbol, "io.sentry"
        ):
            return not matches(source, "quality.adapters")
        return False
    if matches(source, "app") and not matches(source, "app.di"):
        return any(matches(symbol, z) for z in SDK_DENY)
    return False


def check_file(path: Path, code: str, root: Path) -> list[Violation]:
    relative = path.relative_to(root).as_posix()
    clean = erase_literals(code)
    expected = PREFIX + "." + ".".join(path.relative_to(root).parts[:-1])
    declarations = PACKAGE.findall(clean)
    errors: list[Violation] = []
    if declarations != [expected]:
        return [Violation(relative, "PACKAGE_MISMATCH expected " + expected)]
    owner = expected.removeprefix(PREFIX + ".")

    if re.search(r"\b(?:kotlinx\.coroutines\.)?GlobalScope\b", clean):
        errors.append(Violation(relative, "FORBIDDEN_GLOBAL_SCOPE"))

    references = set(IMPORT.findall(clean))
    # FQN in expressions (including import aliases, which were already scanned).
    body = re.sub(r"(?m)^[ \t]*(?:package|import)[ \t]+[^\n]*", "", clean)
    references.update(QUALIFIED.findall(body))
    for ref in sorted(references):
        if matches(ref, PREFIX):
            target = ref.removeprefix(PREFIX).lstrip(".")
            if not allowed_internal(owner, target):
                errors.append(Violation(relative, "FORBIDDEN_INTERNAL " + ref))
        elif forbidden_external(owner, ref):
            errors.append(Violation(relative, "FORBIDDEN_SDK " + ref))
    return errors


def check_tree(repository: Path, enforce_structure: bool = True) -> tuple[list[Violation], int]:
    root = repository / PRODUCTION
    errors: list[Violation] = []
    if not root.is_dir():
        return [Violation(str(root), "NO_ROOT_SOURCE_TREE")], 0
    if enforce_structure:
        missing = [
            path for path in STRUCTURE if not (root / path).is_dir()
        ]
        errors.extend(Violation(x, "MISSING_ARCHITECTURE_PACKAGE") for x in missing)
        settings = repository / "settings.gradle.kts"
        if not settings.is_file():
            errors.append(Violation("settings.gradle.kts", "NO_PRODUCTION_SETTINGS"))
        else:
            # Keep quoted module names; strip only line comments, not string literals.
            text = "\n".join(line.split("//", 1)[0] for line in settings.read_text(encoding="utf-8").splitlines())
            included = SETTINGS_INCLUDE.findall(text)
            if included != ["app"]:
                errors.append(
                    Violation("settings.gradle.kts", "MODULE_TOPOLOGY expected only :app")
                )
            if re.search(r'(?m)^[ \t]*includeBuild[ \t]*\(', text):
                errors.append(
                    Violation("settings.gradle.kts", "FORBIDDEN_INCLUDE_BUILD")
                )
    files = sorted(p for p in root.rglob("*") if p.suffix in (".kt", ".java"))
    if not files:
        errors.append(Violation(str(root), "NO_KOTLIN_OR_JAVA"))
    for path in files:
        errors.extend(check_file(path, path.read_text(encoding="utf-8"), root))
    return errors, len(files)


# No mocks or tests are committed into app/src/main. Negative samples reside
# in temporary directories and prove the enforcement rule actually rejects.
CASES = (
    ("positive_leaf", "core/model/Allowed.kt",
     "import kotlin.collections.List\nclass Allowed", True),
    ("positive_feature_api", "feature/player/Allowed.kt",
     "import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot\nclass Allowed", True),
    ("positive_voice_gateway", "voice/session/Allowed.kt",
     "import com.latticeward.musiccopilot.playback.api.PlaybackGateway\nclass Allowed", True),
    ("positive_data_restore", "playback/restore/Allowed.kt",
     "import com.latticeward.musiccopilot.data.api.RestorableQueueStore\nclass Allowed", True),
    ("positive_di_concrete", "app/di/Allowed.kt",
     "import com.latticeward.musiccopilot.playback.service.MusicPlaybackService\nclass Allowed", True),
    ("positive_result_mediaref", "core/result/Allowed.kt",
     "import com.latticeward.musiccopilot.core.model.MediaRef\\nclass Allowed", True),
    ("positive_resource", "app/Allowed.kt",
     "import com.latticeward.musiccopilot.R\nclass Allowed", True),
    ("negative_feature_media3", "feature/player/Bad.kt",
     "import androidx.media3.exoplayer.ExoPlayer\nclass Bad", False),
    ("negative_feature_other_feature", "feature/player/Bad.kt",
     "import com.latticeward.musiccopilot.feature.search.SearchViewModel\nclass Bad", False),
    ("negative_feature_service", "feature/player/Bad.kt",
     "import com.latticeward.musiccopilot.playback.service.MusicPlaybackService\nclass Bad", False),
    ("negative_feature_room", "feature/player/Bad.kt",
     "import androidx.room.Dao\nclass Bad", False),
    ("negative_feature_speech", "feature/player/Bad.kt",
     "import android.speech.SpeechRecognizer\nclass Bad", False),
    ("negative_feature_quality_sdk", "feature/player/Bad.kt",
     "import com.google.firebase.analytics.FirebaseAnalytics\nclass Bad", False),
    ("negative_core_android", "core/model/Bad.kt",
     "import androidx.compose.runtime.Composable\nclass Bad", False),
    ("negative_core_concrete", "core/application/Bad.kt",
     "import com.latticeward.musiccopilot.data.room.MusicCopilotDatabase\nclass Bad", False),
    ("negative_port_concrete", "data/api/Bad.kt",
     "import com.latticeward.musiccopilot.data.room.LibraryDao\nclass Bad", False),
    ("negative_voice_player", "voice/session/Bad.kt",
     "import androidx.media3.common.Player\nclass Bad", False),
    ("negative_data_ui", "data/repository/Bad.kt",
     "import com.latticeward.musiccopilot.feature.player.PlayerScreen\nclass Bad", False),
    ("negative_quality_playback", "quality/privacy/Bad.kt",
     "import com.latticeward.musiccopilot.playback.service.MusicPlaybackService\nclass Bad", False),
    ("negative_app_outside_di", "app/navigation/Bad.kt",
     "import com.latticeward.musiccopilot.data.room.MusicCopilotDatabase\nclass Bad", False),
    ("negative_fqn_without_import", "feature/player/Bad.kt",
     "val x = com.latticeward.musiccopilot.playback.service.HiddenPlayer\n", False),
    ("negative_alias", "feature/player/Bad.kt",
     "import androidx.media3.exoplayer.ExoPlayer as Renamed\n", False),
    ("negative_mismatched_package", "core/model/Bad.kt",
     "package com.latticeward.musiccopilot.core.result\nclass Bad", False),
    ("negative_global_scope", "core/application/Bad.kt",
     "import kotlinx.coroutines.GlobalScope\nval job = GlobalScope\n", False),
)


def test_rules() -> tuple[list[str], int]:
    failures: list[str] = []
    positives = negatives = 0
    for name, relpath, body, should_pass in CASES:
        with tempfile.TemporaryDirectory(prefix="music-phase3-") as t:
            root = Path(t)
            target = root / PRODUCTION / relpath
            target.parent.mkdir(parents=True, exist_ok=True)
            if not body.startswith("package "):
                pkg = PREFIX + "." + ".".join(Path(relpath).parts[:-1])
                body = f"package {pkg}\n" + body
            target.write_text(body, encoding="utf-8")
            errors, count = check_tree(root, enforce_structure=False)
            if count != 1 or bool(errors) == should_pass:
                failures.append(f"{name}: expected pass={should_pass}, got {errors}")
            if should_pass:
                positives += 1
            else:
                negatives += 1
    return failures, positives + negatives


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path("."))
    parser.add_argument("--self-test", action="store_true",
                        help="exercise positive + deliberately invalid dependency fixtures")
    args = parser.parse_args()
    if args.self_test:
        broken, count = test_rules()
        if broken:
            for error in broken:
                print("SELFTEST FAIL:", error, file=sys.stderr)
            return 1
        print(f"SELFTEST PASS: {count} fixtures including real rejected violations")
    errors, scanned = check_tree(args.root.resolve())
    if errors:
        for item in errors:
            print("ARCHITECTURE FAIL:", item, file=sys.stderr)
        print(f"ARCHITECTURE FAIL: {len(errors)} violation(s) across {scanned} source files")
        return 1
    print(f"ARCHITECTURE PASS: one :app module; {len(STRUCTURE)} package paths; "
          f"{scanned} source files; no forbidden imports or qualified references")
    return 0


if __name__ == "__main__":
    sys.exit(main())

