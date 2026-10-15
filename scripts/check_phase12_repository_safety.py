#!/usr/bin/env python3
"""Issue #4 Phase 12: sensitive-file/credential and personal-media commit guard.

Scans Git-tracked paths and production source/config only. No user media or
network access. Deliberately rejects dangerous fixtures in --self-test mode.
Not a substitute for organization secret scanning or incident response.
"""
from __future__ import annotations

import argparse
from pathlib import Path
import re
import subprocess
import sys

SENSITIVE_SUFFIXES = (
    ".jks", ".keystore", ".p12", ".pfx", ".pem", ".key", ".mobileprovision",
    ".mp3", ".m4a", ".aac", ".flac", ".wav", ".ogg", ".opus", ".wma",
    ".sqlite", ".sqlite3", ".db",
)
SENSITIVE_BASENAMES = {
    "google-services.json", "keystore.properties", "local.properties",
    "credentials.json", "service-account.json",
}
SECRET_PATTERNS = {
    "private_key": re.compile(r"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----"),
    "github_access_token": re.compile(r"\b(?:ghp|gho|ghu|ghs|ghr)_[A-Za-z0-9]{20,}\b"),
    "openai_api_key": re.compile(r"\bsk-(?:proj-)?[A-Za-z0-9_-]{24,}\b"),
    "slack_bot_token": re.compile(r"\bxoxb-[0-9A-Za-z-]{20,}\b"),
    "google_api_key": re.compile(r"\bAIza[0-9A-Za-z_-]{35}\b"),
}
SOURCE_SUFFIXES = (".kt", ".kts", ".java", ".properties", ".yml", ".yaml")
SOURCE_PREFIXES = (
    "app/", "gradle/", ".github/workflows/", "scripts/",
)
# Synthetic rule-test strings live only in our guard itself; exclude it.
SELF_PATH = "scripts/check_phase12_repository_safety.py"


def violations(entries: dict[str, bytes]) -> list[str]:
    errors: list[str] = []
    for name, contents in entries.items():
        path = name.replace("\\", "/")
        basename = Path(path).name.lower()
        lower = path.lower()
        if (basename in SENSITIVE_BASENAMES
                or basename.startswith("service-account")
                or (basename.startswith(".env") and basename != ".env.example")
                or lower.endswith(SENSITIVE_SUFFIXES)):
            errors.append("SENSITIVE_TRACKED_PATH:" + path)
        if path != SELF_PATH and path.startswith(SOURCE_PREFIXES) and lower.endswith(SOURCE_SUFFIXES):
            try:
                text = contents.decode("utf-8")
            except UnicodeError:
                errors.append("NON_TEXT_PRODUCTION_CONFIG:" + path)
                continue
            for label, pattern in SECRET_PATTERNS.items():
                if pattern.search(text):
                    errors.append("SECRET_PATTERN:" + path + ":" + label)
    return sorted(set(errors))


def self_test() -> None:
    safe = {"app/src/main/java/Safe.kt": b'package example\nclass Safe\n'}
    assert not violations(safe)
    assert any("SENSITIVE_TRACKED_PATH" in e for e in violations(
        {"app/signing/release.jks": b"fake-not-a-real-key"}))
    assert any("SENSITIVE_TRACKED_PATH" in e for e in violations(
        {"app/src/test/assets/private_voice.wav": b"fake-audio"}))
    assert any("SENSITIVE_TRACKED_PATH" in e for e in violations(
        {"app/google-services.json": b"{}"}))
    assert any("SECRET_PATTERN" in e for e in violations(
        {"app/src/main/java/Unsafe.kt": b'val token = "sk-proj-' +
         b'x' * 32 + b'"'}))
    assert any("SECRET_PATTERN" in e for e in violations(
        {"app/src/main/java/Unsafe.kt": b"-----BEGIN PRIVATE KEY-----"}))
    print("REPOSITORY_SAFETY_SELFTEST PASS: safe source and five rejected synthetic secret/media fixtures")


def tracked(root: Path) -> dict[str, bytes]:
    paths = subprocess.check_output(["git", "ls-files", "-z"], cwd=root)
    result: dict[str, bytes] = {}
    for raw in paths.split(b"\0"):
        if not raw:
            continue
        name = raw.decode("utf-8")
        p = root / name
        if p.is_file():
            result[name] = p.read_bytes()
    return result


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path("."))
    parser.add_argument("--self-test", action="store_true")
    args = parser.parse_args()
    if args.self_test:
        self_test()
    entries = tracked(args.root)
    errors = violations(entries)
    if errors:
        for issue in errors:
            print(issue, file=sys.stderr)
        print("REPOSITORY_SAFETY FAIL", file=sys.stderr)
        return 1
    print(f"REPOSITORY_SAFETY PASS: {len(entries)} tracked files scanned, no forbidden personal media, credentials or signing assets")
    return 0


if __name__ == "__main__":
    sys.exit(main())
