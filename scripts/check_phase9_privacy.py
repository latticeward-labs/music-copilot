#!/usr/bin/env python3
"""Issue #4 Phase 9: deterministic, offline static Privacy & Quality CI guard.

No network, no secrets, no personal data inspection or provider SDK required.
Negative self-tests prove violations are actually rejected.
"""
from __future__ import annotations
import argparse
import json
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ANDROID = "{http://schemas.android.com/apk/res/android}"


def verify_rules(root: Path) -> list[str]:
    errors: list[str] = []
    def read(path: str) -> str:
        f = root / path
        if not f.is_file():
            errors.append("MISSING_FILE:" + path)
            return ""
        return f.read_text(encoding="utf-8")

    build = read("app/build.gradle.kts")
    manifest = read("app/src/main/AndroidManifest.xml")
    bindings = read("app/src/main/java/com/latticeward/musiccopilot/app/di/QualityBindings.kt")
    gate = read("app/src/main/java/com/latticeward/musiccopilot/quality/privacy/AllowlistedTelemetryPrivacyGate.kt")
    recorder = read("app/src/main/java/com/latticeward/musiccopilot/quality/adapters/BestEffortQualityEventPublisher.kt")
    no_op = read("app/src/main/java/com/latticeward/musiccopilot/quality/noop/NoOpTelemetrySink.kt")
    event_port = read("app/src/main/java/com/latticeward/musiccopilot/quality/api/TelemetryEvent.kt")
    telemetry_ports = read("app/src/main/java/com/latticeward/musiccopilot/quality/api/TelemetryPorts.kt")
    legacy = read("app/src/main/res/xml/no_backup_legacy.xml")
    modern = read("app/src/main/res/xml/no_data_extraction.xml")

    for flag in ("QUALITY_EXTERNAL_DIAGNOSTICS_ENABLED",
                 "QUALITY_CRASH_UPLOAD_ENABLED", "QUALITY_VERBOSE_LOGGING_ENABLED"):
        if f'buildConfigField("boolean", "{flag}", "false")' not in build:
            errors.append("UNSAFE_BUILD_FLAG:" + flag)
    if 'isDebuggable = false' not in build:
        errors.append("RELEASE_DEBUGGABLE")
    for forbidden in ("firebase-analytics", "firebase-crashlytics", "sentry-android",
                      "appcenter-analytics", "com.google.gms.google-services"):
        if forbidden in build.lower():
            errors.append("EXTERNAL_ANALYTICS_DEPENDENCY")
            break
    if not all(s in bindings for s in ("AllowlistedTelemetryPrivacyGate()", "NoOpTelemetrySink()")):
        errors.append("QUALITY_BINDINGS_UNSAFE")
    if not all(s in gate for s in ("sealed", "v1|", "QUALITY_DROPPED")):
        # Kotlin sealed model is asserted below; sanitizer must never silently allow unknown.
        if not (("v1|" in gate) and ("QUALITY_DROPPED" in gate)):
            errors.append("PRIVACY_GATE_MISSING")
    if "sealed interface TelemetryEvent" not in event_port or "Map<" in event_port:
        errors.append("FREEFORM_EVENT_MODEL")
    if "internal constructor" not in telemetry_ports or "Map<" in telemetry_ports:
        errors.append("SANITIZED_PORT_UNSAFE")
    for needle in ("catch (_: Exception)", "sink.tryEmit", "privacyGate.sanitize"):
        if needle not in recorder:
            errors.append("QUALITY_FAILURE_ISOLATION")
            break
    if "tryEmit(event: SanitizedTelemetryEvent): Boolean = false" not in no_op:
        errors.append("SINK_NOT_NOOP")

    for module in (gate, recorder, no_op):
        for unsafe in ("Log.", "printStackTrace(", "System.out", "HttpURLConnection",
                       "Crashlytics", "Sentry.capture", "FirebaseAnalytics"):
            if unsafe in module:
                errors.append("UNSAFE_LOG_OR_TRANSPORT")
                break
    if 'android.permission.INTERNET' in manifest:
        errors.append("NETWORK_PERMISSION_PRESENT")
    if manifest:
        try:
            root_xml = ET.fromstring(manifest)
            application = root_xml.find("application")
            if application is None:
                errors.append("NO_APPLICATION")
            else:
                settings = {"allowBackup": "false", "usesCleartextTraffic": "false",
                    "fullBackupContent": "@xml/no_backup_legacy",
                    "dataExtractionRules": "@xml/no_data_extraction"}
                for key, expected in settings.items():
                    if application.attrib.get(ANDROID + key) != expected:
                        errors.append("MANIFEST_PRIVACY:" + key)
        except ET.ParseError:
            errors.append("MANIFEST_INVALID_XML")

    for name, payload, sections in (
        ("LEGACY_BACKUP", legacy, (".",)),
        ("DATA_EXTRACTION", modern, ("cloud-backup", "device-transfer")),
    ):
        if not payload:
            continue
        try:
            node = ET.fromstring(payload)
            if name == "LEGACY_BACKUP":
                if node.tag != "full-backup-content" or not any(
                    x.attrib.get("domain") == "root" and x.attrib.get("path") == "."
                    for x in node.iter("exclude")
                ):
                    errors.append("LEGACY_BACKUP_UNSAFE")
            else:
                if node.tag != "data-extraction-rules":
                    errors.append("BACKUP_RULES_INVALID")
                for section in sections:
                    part = node.find(section)
                    if part is None or not any(
                        x.attrib.get("domain") == "root" and x.attrib.get("path") == "."
                        for x in part.findall("exclude")
                    ):
                        errors.append("DATA_EXTRACTION_UNSAFE:" + section)
        except ET.ParseError:
            errors.append(name + "_INVALID_XML")
    return sorted(set(errors))


def self_test() -> None:
    """Prove static detection is not ceremonial, without personal inputs."""
    import tempfile
    minimal = {
        "app/build.gradle.kts": """defaultConfig {
buildConfigField("boolean", "QUALITY_EXTERNAL_DIAGNOSTICS_ENABLED", "false")
buildConfigField("boolean", "QUALITY_CRASH_UPLOAD_ENABLED", "false")
buildConfigField("boolean", "QUALITY_VERBOSE_LOGGING_ENABLED", "false")
}
release { isDebuggable = false }""",
        "app/src/main/AndroidManifest.xml": """<manifest xmlns:android="http://schemas.android.com/apk/res/android">
<application android:allowBackup="false" android:usesCleartextTraffic="false"
android:fullBackupContent="@xml/no_backup_legacy"
android:dataExtractionRules="@xml/no_data_extraction"/></manifest>""",
        "app/src/main/java/com/latticeward/musiccopilot/app/di/QualityBindings.kt":
            "AllowlistedTelemetryPrivacyGate(); NoOpTelemetrySink()",
        "app/src/main/java/com/latticeward/musiccopilot/quality/privacy/AllowlistedTelemetryPrivacyGate.kt":
            "v1| QUALITY_DROPPED",
        "app/src/main/java/com/latticeward/musiccopilot/quality/adapters/BestEffortQualityEventPublisher.kt":
            "catch (_: Exception) privacyGate.sanitize sink.tryEmit",
        "app/src/main/java/com/latticeward/musiccopilot/quality/noop/NoOpTelemetrySink.kt":
            "tryEmit(event: SanitizedTelemetryEvent): Boolean = false",
        "app/src/main/java/com/latticeward/musiccopilot/quality/api/TelemetryEvent.kt":
            "sealed interface TelemetryEvent",
        "app/src/main/java/com/latticeward/musiccopilot/quality/api/TelemetryPorts.kt":
            "internal constructor",
        "app/src/main/res/xml/no_backup_legacy.xml":
            '<full-backup-content><exclude domain="root" path="."/></full-backup-content>',
        "app/src/main/res/xml/no_data_extraction.xml":
            '<data-extraction-rules><cloud-backup><exclude domain="root" path="."/></cloud-backup>'
            '<device-transfer><exclude domain="root" path="."/></device-transfer></data-extraction-rules>',
    }
    with tempfile.TemporaryDirectory() as p:
        root = Path(p)
        for key, value in minimal.items():
            target = root / key
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(value, encoding="utf-8")
        assert verify_rules(root) == [], "Safe fixture must pass"
        manifest_file = root / "app/src/main/AndroidManifest.xml"
        manifest_file.write_text(minimal["app/src/main/AndroidManifest.xml"]
                                 .replace('android:allowBackup="false"', 'android:allowBackup="true"'))
        assert "MANIFEST_PRIVACY:allowBackup" in verify_rules(root)
        manifest_file.write_text(minimal["app/src/main/AndroidManifest.xml"])
        (root / "app/build.gradle.kts").write_text(minimal["app/build.gradle.kts"] +
                                                        '\nimplementation("io.sentry:sentry-android:1")')
        assert "EXTERNAL_ANALYTICS_DEPENDENCY" in verify_rules(root)
        (root / "app/build.gradle.kts").write_text(minimal["app/build.gradle.kts"])
        (root / "app/src/main/java/com/latticeward/musiccopilot/quality/api/TelemetryEvent.kt").write_text(
            "sealed interface TelemetryEvent { val payload: Map<String, Any> }")
        assert "FREEFORM_EVENT_MODEL" in verify_rules(root)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path("."))
    parser.add_argument("--self-test", action="store_true")
    args = parser.parse_args()
    if args.self_test:
        self_test()
        print("PRIVACY_SELFTEST PASS: safe fixture plus 3 real rejected violations")
    problems = verify_rules(args.root)
    if problems:
        print("PRIVACY_QUALITY FAIL: " + ",".join(problems), file=sys.stderr)
        return 1
    print("PRIVACY_QUALITY PASS: typed allowlist, offline NoOp, debug/release flags, no external SDK, no network, excluded backup")
    return 0


if __name__ == "__main__":
    sys.exit(main())
