#!/usr/bin/env python3
"""Validate human-collected two-PHYSICAL-device Phase 13 evidence.
GitHub emulators deliberately cannot satisfy this gate. No device serial,
Android ID, personal logs, microphone audio or installed songs are needed.
"""
import argparse
import json
from pathlib import Path

CHECKS = (
    "install_pass",
    "default_permissions_denied_pass",
    "cold_launch_pass",
    "background_foreground_pass",
    "force_stop_restart_pass",
    "app_crash_absent_pass",
    "synthetic_only",
)

def errors(records, expected_sha=None):
    problems = []
    if len(records) != 2:
        return ["TWO_RECORDS_REQUIRED"]
    identities = set()
    hashes = set()
    for i, obj in enumerate(records, 1):
        tag = f"DEVICE_{i}"
        for field in ("label","manufacturer","model","android_api","android_release",
                      "timestamp_utc","commit_sha","apk_sha256"):
            if not obj.get(field):
                problems.append(f"{tag}:MISSING_{field}")
        if obj.get("device_type") != "physical" or obj.get("ro_kernel_qemu") == "1":
            problems.append(f"{tag}:NOT_VERIFIED_PHYSICAL")
        if int(obj.get("android_api",0)) < 29:
            problems.append(f"{tag}:BELOW_ANDROID_10")
        for check in CHECKS:
            if obj.get(check) is not True:
                problems.append(f"{tag}:{check.upper()}_NOT_PASS")
        identities.add((obj.get("manufacturer","").lower(),obj.get("model","").lower()))
        hashes.add(obj.get("apk_sha256"))
        if expected_sha and obj.get("commit_sha") != expected_sha:
            problems.append(f"{tag}:WRONG_BUILD_COMMIT")
    if len(identities) < 2:
        problems.append("TWO_DISTINCT_MODELS_NOT_EVIDENCED")
    if len(hashes) != 1:
        problems.append("APK_MISMATCH_BETWEEN_DEVICES")
    apis = sorted(int(obj.get("android_api",0)) for obj in records)
    if 29 not in apis:
        problems.append("ANDROID_10_DEVICE_MISSING")
    if max(apis) < 34:
        problems.append("ANDROID_14_PLUS_DEVICE_MISSING")
    return problems

def self_test():
    a=dict(label="phone-a",manufacturer="Google",model="Pixel 4",android_api=29,
           android_release="10",timestamp_utc="2026-10-14T10:00:00Z",
           commit_sha="abcd",apk_sha256="sha",
           device_type="physical",ro_kernel_qemu="0",
           **{x:True for x in CHECKS})
    b=dict(a,label="phone-b",manufacturer="Samsung",model="Galaxy",android_api=34)
    assert not errors([a,b], "abcd")
    assert "DEVICE_1:NOT_VERIFIED_PHYSICAL" in errors([dict(a,device_type="emulator",ro_kernel_qemu="1"),b],"abcd")
    assert "TWO_DISTINCT_MODELS_NOT_EVIDENCED" in errors([a,dict(a,label="same")],"abcd")
    assert "ANDROID_14_PLUS_DEVICE_MISSING" in errors([a,dict(b,android_api=33)],"abcd")
    assert "APK_MISMATCH_BETWEEN_DEVICES" in errors([a,dict(b,apk_sha256="wrong")],"abcd")
    assert "DEVICE_2:WRONG_BUILD_COMMIT" in errors([a,dict(b,commit_sha="different")],"abcd")
    assert "TWO_RECORDS_REQUIRED" in errors([a],"abcd")
    print("PHASE13_PHYSICAL_VALIDATOR_SELFTEST PASS: positive fixture and 7 rejected physical-claim/correctness regressions")

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument("--first", type=Path)
    parser.add_argument("--second", type=Path)
    parser.add_argument("--expected-sha")
    parser.add_argument("--self-test", action="store_true")
    a=parser.parse_args()
    if a.self_test:
        self_test()
    if a.first or a.second:
        if not (a.first and a.second and a.expected_sha):
            parser.error("--first, --second, and --expected-sha required together")
        records=[json.loads(x.read_text(encoding="utf-8")) for x in (a.first,a.second)]
        found=errors(records, a.expected_sha)
        if found:
            for p in found:
                print("PHASE13_PHYSICAL_GATE FAIL:",p)
            raise SystemExit(1)
        print("PHASE13_PHYSICAL_GATE PASS: two distinct verified physical model/API records")
    elif not a.self_test:
        parser.error("Supply --self-test or both physical evidence JSON files + expected SHA")

if __name__ == "__main__":
    main()
