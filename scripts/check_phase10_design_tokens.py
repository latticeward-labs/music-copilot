#!/usr/bin/env python3
"""Issue #4 Phase 10: exact canonical Design System -> Compose token audit.
GitHub Issue #17 TOKENS.json / COMPOSE_TOKEN_MAPPING.md is the authority;
read actual Kotlin and reject approximations, unapproved routes or lost lifecycle.
"""
import argparse
import json
from pathlib import Path
import re
import sys

ROOT = "app/src/main/java/com/latticeward/musiccopilot/"
FILES = {
    "colors": ROOT + "app/theme/MusicColors.kt",
    "geometry": ROOT + "app/theme/MusicGeometry.kt",
    "typography": ROOT + "app/theme/MusicTypography.kt",
    "theme": ROOT + "app/theme/MusicCopilotTheme.kt",
    "activity": ROOT + "app/MainActivity.kt",
    "nav": ROOT + "app/navigation/AppNavHost.kt",
    "dest": ROOT + "app/navigation/Destination.kt",
    "state": ROOT + "app/navigation/RootNavigationStateHolder.kt",
    "gradle": "app/build.gradle.kts",
    "catalog": "gradle/libs.versions.toml",
    "manifest": "app/src/main/AndroidManifest.xml",
}


def block(source, name):
    matched = re.search(r"object\s+" + re.escape(name) + r"\s*\{(.*?)\n\}", source, re.S)
    return matched.group(1) if matched else ""


def expected_argb(hex_value):
    raw = hex_value.lstrip("#").upper()
    return ("FF" + raw) if len(raw) == 6 else (raw[-2:] + raw[:-2])


def verify(files, tokens):
    errors = []
    for name, file in FILES.items():
        if not files.get(name):
            errors.append("MISSING_OR_EMPTY_FILE:" + file)
    colors = files.get("colors", "")
    for obj, values in (
        ("MusicColors", tokens["color"]["semantic"]),
        ("MusicPrimitiveColors", tokens["color"]["primitive"]),
    ):
        contents = block(colors, obj)
        parsed = dict(re.findall(r"\bval\s+(\w+)\s*=\s*Color\(0x([0-9A-Fa-f]{8})\)", contents))
        if parsed != {key: expected_argb(val) for key, val in values.items()}:
            errors.append("EXACT_COLOR_DRIFT:" + obj)
    if "val pressedOverlay = Color(0x" + expected_argb(tokens["state"]["pressedOverlay"]) + ")" not in colors:
        errors.append("PRESSED_OVERLAY_DRIFT")

    g = files.get("geometry", "")
    for obj, data in (
        ("MusicSpacing", tokens["spacing"]), ("MusicGrid", tokens["grid"]),
        ("MusicRadius", tokens["radius"]), ("MusicElevation", tokens["elevation"]),
        ("MusicSize", tokens["size"]),
    ):
        actual = block(g, obj)
        for key, value in data.items():
            if isinstance(value, dict):
                for sub, num in value.items():
                    if f"val {key}_{sub} = {num}.dp" not in actual:
                        errors.append("GEOMETRY_DRIFT:" + key + "." + sub)
            elif obj == "MusicGrid" and key == "compactColumns":
                if f"const val compactColumns = {value}" not in actual:
                    errors.append("GRID_COLUMNS_DRIFT")
            elif f"val {key} = {value}.dp" not in actual:
                errors.append("GEOMETRY_DRIFT:" + obj + "." + key)
    for key, value in tokens["motion"].items():
        if isinstance(value, int) and f"const val {key} = {value}" not in block(g, "MusicMotion"):
            errors.append("MOTION_DRIFT:" + key)

    typ = files.get("typography", "")
    for role, v in tokens["typography"].items():
        if role == "family":
            continue
        expr = (rf"val\s+{re.escape(role)}\s*=\s*TextStyle\("
                rf"\s*fontFamily\s*=\s*FontFamily.SansSerif,"
                rf"\s*fontSize\s*=\s*{v['fontSizeSp']}\.sp,"
                rf"\s*lineHeight\s*=\s*{v['lineHeightSp']}\.sp,"
                rf"\s*fontWeight\s*=\s*FontWeight\({v['fontWeight']}\),"
                rf"\s*letterSpacing\s*=\s*{v['letterSpacingSp']}\.sp")
        if not re.search(expr, typ):
            errors.append("TYPOGRAPHY_DRIFT:" + role)
    theme = files.get("theme", "")
    mappings = ["primary", "onPrimary", "primaryContainer", "onPrimaryContainer",
                "secondary", "onSecondary", "background", "surface", "surfaceVariant",
                "error", "onError", "errorContainer", "onErrorContainer", "scrim"]
    for k in mappings:
        if not re.search(r"\b" + k + r"\s*=\s*MusicColors\." + k + r"\b", theme):
            errors.append("MATERIAL_COLOR_MAPPING_MISSING:" + k)
    for k, extra in (("onBackground", "textPrimary"), ("onSurface", "textPrimary"),
                     ("onSurfaceVariant", "textSecondary"), ("outline", "border"),
                     ("outlineVariant", "borderStrong")):
        if not re.search(r"\b" + k + r"\s*=\s*MusicColors\." + extra + r"\b", theme):
            errors.append("MATERIAL_SEMANTIC_MAPPING_MISSING:" + k)
    if "darkColorScheme(" not in theme or "dynamicDarkColorScheme" in theme or "isSystemInDarkTheme()" in theme:
        errors.append("THEME_NOT_FIXED_NIGHT_SIGNAL")

    activity = files.get("activity", "")
    if activity.count("collectAsStateWithLifecycle()") < 2:
        errors.append("LIFECYCLE_AWARE_STATE_MISSING")
    for forb in ("ApplicationBindings(", "ExoPlayer.Builder(", "MediaSession.Builder("):
        if forb in activity:
            errors.append("SECOND_AUTHORITY_IN_ACTIVITY")
    nav = files.get("nav", "")
    # Issue #4 initially approved Bootstrap alone. Issue #5 P03 activates ONLY
    # the four approved First Launch destinations, never Voice/Driving/player.
    installed_routes = set(re.findall(r"composable\(Destination\.(\w+)\.route\)", nav))
    first_launch_routes = {"Bootstrap", "Onboarding", "PermissionRecovery", "Library"}
    approved = (first_launch_routes | {"Settings"}) if "activeInPhase4:" in files.get("dest", "") else (
        first_launch_routes if ("activeInPhase3 = setOf(" in files.get("dest", "") or
                                "activeInPhase3: Set<Destination> get() = setOf(" in files.get("dest", ""))
        else {"Bootstrap"}
    )
    if (nav.count("    NavHost(") != 1 or installed_routes != approved or
            nav.count("composable(") != len(approved)):
        errors.append("UNAPPROVED_NAV_DESTINATION")
    if "composable(Destination.Bootstrap.route)" not in nav:
        errors.append("BOOTSTRAP_ROUTE_NOT_INSTALLED")
    dest = files.get("dest", "")
    if "data object Voice" in dest or "Destination(\"voice\")" in dest:
        errors.append("VOICE_MUST_BE_OVERLAY")
    expected = {"Bootstrap", "Onboarding", "PermissionRecovery", "Library", "Search",
                "FullPlayer", "Driving", "Settings", "LikedSongsP2"}
    found = set(re.findall(r"data object\s+(\w+)\s*:\s*Destination\(", dest))
    if found != expected or not ("activeInPhase10 = setOf(Bootstrap)" in dest or
                                "activeInPhase10: Set<Destination> get() = setOf(Bootstrap)" in dest):
        errors.append("DESTINATION_CONTRACT_DRIFT")
    gradle = files.get("gradle", "")
    catalog = files.get("catalog", "")
    for req in ("implementation(libs.androidx.navigation.compose)",
                "implementation(libs.androidx.lifecycle.runtime.compose)"):
        if req not in gradle:
            errors.append("MISSING_COMPOSE_DEPENDENCY")
    for req in ("androidx-navigation-compose = ", "androidx-lifecycle-runtime-compose = "):
        if req not in catalog:
            errors.append("MISSING_COMPOSE_CATALOG_ENTRY")
    if "minSdk = 29" not in gradle:
        errors.append("MIN_ANDROID_10_VIOLATION")
    if files.get("manifest", "").count('android:name=".app.MainActivity"') != 1:
        errors.append("SINGLE_ACTIVITY_MANIFEST_VIOLATION")
    return sorted(set(errors))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path("."))
    parser.add_argument("--self-test", action="store_true")
    args = parser.parse_args()
    root = args.root
    tokens = json.loads((root / "docs/design-system/foundations/TOKENS.json").read_text())
    files = {key: (root / path).read_text() if (root / path).exists() else ""
             for key, path in FILES.items()}
    problems = verify(files, tokens)
    if args.self_test:
        if problems:
            raise RuntimeError("Cannot run self-test against broken baseline: " + ",".join(problems))
        wrong_color = dict(files)
        wrong_color["colors"] = files["colors"].replace(
            "val primary = Color(0xFF67D8FF)", "val primary = Color(0xFF67D800)"
        )
        assert "EXACT_COLOR_DRIFT:MusicColors" in verify(wrong_color, tokens)
        unsafe_nav = dict(files)
        unsafe_nav["nav"] += "\ncomposable(Destination.Driving.route) { }"
        assert "UNAPPROVED_NAV_DESTINATION" in verify(unsafe_nav, tokens)
        stale_state = dict(files)
        stale_state["activity"] = files["activity"].replace(
            ".collectAsStateWithLifecycle()", ".collectAsState()"
        )
        assert "LIFECYCLE_AWARE_STATE_MISSING" in verify(stale_state, tokens)
        print("DESIGN_SELFTEST PASS: exact baseline plus 3 deliberately rejected regressions")
    if problems:
        print("DESIGN_TOKEN_AUDIT FAIL: " + ",".join(problems), file=sys.stderr)
        sys.exit(1)
    print("DESIGN_TOKEN_AUDIT PASS: exact Night Signal palette, SP, DP, navigation/voice boundary, lifecycle, API 29")
    return 0


if __name__ == "__main__":
    sys.exit(main())
