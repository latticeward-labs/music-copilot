#!/usr/bin/env python3
"""Native Settings UI driver; fail closed, no simulated permission grant."""
import subprocess, sys, time
from issue5_phase6_emulator_ui import nodes, tap

def run(*args):
    return subprocess.run(["adb", *args], capture_output=True, text=True, check=True, timeout=45).stdout

def choose(*labels):
    for attempt in range(16):
        for n in nodes():
            text = (n["text"] or n["content-desc"]).strip()
            if text.casefold() in [v.casefold() for v in labels] and n["enabled"] != "false":
                tap(n)
                print("P07_REAL_ANDROID_SETTINGS_TAP", text, flush=True)
                return
        time.sleep(1)
    raise AssertionError("Missing native Settings control " + str(labels))

mode=sys.argv[1]
api=int(sys.argv[2])
assert mode in ("grant","revoke")
screen=run("shell", "dumpsys", "activity", "activities").lower()
assert "com.android.settings" in screen or "subsettings" in screen, "Not in Android Settings"
choose("Permissions", "App permissions")
if api >= 33:
    choose("Music and audio", "Music", "Audio")
else:
    choose("Storage", "Files and media")
choose("Allow", "Allow access") if mode=="grant" else choose("Don't allow","Don’t allow","Deny")
for i in range(3):
    run("shell", "input", "keyevent", "4")
    time.sleep(1.2)
print("P07_REAL_ANDROID_SETTINGS_"+mode.upper()+"_PASS",flush=True)
