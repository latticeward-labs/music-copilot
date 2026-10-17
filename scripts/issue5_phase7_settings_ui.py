#!/usr/bin/env python3
"""Navigate real Android AppInfo permission Settings, with OS-truth assertions.

Never label an Android Settings toggle successful until dumpsys reports the
requested permission state. Revocation may require a second native dialog.
"""
import subprocess
import sys
import time
from issue5_phase6_emulator_ui import nodes, tap

def run(*args):
    return subprocess.run(["adb", *args], capture_output=True, text=True, check=True, timeout=45).stdout

def choose(*labels):
    expected = {label.casefold() for label in labels}
    for _ in range(18):
        for node in nodes():
            label = (node["text"] or node["content-desc"]).strip()
            if label.casefold() in expected and node["enabled"] != "false":
                tap(node)
                print("P07_REAL_ANDROID_SETTINGS_TAP", label, flush=True)
                return
        time.sleep(1)
    raise AssertionError("Missing native Settings control " + str(labels))

def os_permission_granted(pkg, permission):
    rows = [line.strip() for line in run("shell", "dumpsys", "package", pkg).splitlines()
            if line.strip().startswith(permission + ": granted=")]
    if len(rows) != 1:
        raise AssertionError("Expected one runtime OS permission record; found " + str(len(rows)))
    return "granted=true" in rows[0]

def assert_os(pkg, permission, expected):
    for _ in range(12):
        if os_permission_granted(pkg, permission) == expected:
            return True
        time.sleep(0.7)
    return False

mode = sys.argv[1]
api = int(sys.argv[2])
assert mode in ("grant", "revoke")
pkg = "com.latticeward.musiccopilot.debug"
permission = ("android.permission.READ_MEDIA_AUDIO" if api >= 33
              else "android.permission.READ_EXTERNAL_STORAGE")
screen = run("shell", "dumpsys", "activity", "activities").lower()
assert "com.android.settings" in screen or "subsettings" in screen, "Not in Android Settings"
choose("Permissions", "App permissions")
if api >= 33:
    choose("Music and audio", "Music", "Audio")
else:
    choose("Storage", "Files and media")
if mode == "grant":
    choose("Allow", "Allow access")
    desired = True
else:
    choose("Don't allow", "Don’t allow", "Deny")
    desired = False
    if not assert_os(pkg, permission, desired):
        # Android 13 warns that changing permission could interrupt a running
        # app. Never dismiss that confirmation with Back (which cancels it).
        # Only accept an explicit positive confirm button in actual Settings.
        confirmations = {
            "don't allow anyway", "don’t allow anyway", "deny anyway",
            "change anyway", "don't allow", "don’t allow", "ok",
        }
        found = False
        for node in nodes():
            name = (node["text"] or node["content-desc"]).strip().casefold()
            if name in confirmations and node["enabled"] != "false":
                tap(node)
                print("P07_NATIVE_REVOCATION_CONFIRM", name, flush=True)
                found = True
                break
        if not found:
            labels = sorted({(n["text"] or n["content-desc"]).strip()
                             for n in nodes() if (n["text"] or n["content-desc"]).strip()})
            raise AssertionError("Native revoke not applied and no confirm dialog: "+ repr(labels[:25]))
if not assert_os(pkg, permission, desired):
    raise AssertionError("Settings action did not change Android OS permission: " + mode)
for _ in range(3):
    run("shell", "input", "keyevent", "4")
    time.sleep(1.2)
print("P07_REAL_ANDROID_SETTINGS_" + mode.upper() + "_OS_VERIFIED_PASS", flush=True)
