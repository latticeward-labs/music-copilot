#!/usr/bin/env python3
"""Bounded Android SystemUI ANR recovery for the P07 emulator fixture only.

Do not hide app crashes or assume UI success. We only touch the native "Wait"
control on an OS-owned com.android.systemui ANR, then independently verify the
actual second onboarding slide before tapping Skip. Keep evidence of ANR.
"""
from pathlib import Path
import subprocess
import sys
import time
from issue5_phase6_emulator_ui import nodes, tap, adb

out = Path(sys.argv[1])
out.mkdir(parents=True, exist_ok=True)
app_pkg = "com.latticeward.musiccopilot.debug"

def check_app_crash():
    crash = adb("logcat", "-d", "-b", "crash")
    if "Process: " + app_pkg in crash:
        raise AssertionError("Actual Music Copilot app process crashed")

for attempt in range(1, 7):
    check_app_crash()
    observed = nodes()
    # Must be the approved app text, not a synthetic injected route/tap.
    skip_nodes = [n for n in observed if n["text"] == "Skip" and
                  app_pkg in n["package"] and n["enabled"] != "false"]
    anr_text = [n for n in observed if "System UI isn't responding" in
                (n["text"] + " " + n["content-desc"])]
    if anr_text:
        if (out / "system_ui_anr.png").exists():
            raise AssertionError("SystemUI ANR persisted after one native Wait recovery")
        (out / "system_ui_anr.png").write_bytes(subprocess.run(
            ["adb", "exec-out", "screencap", "-p"], check=True,
            stdout=subprocess.PIPE, timeout=30).stdout)
        (out / "system_ui_anr_activity.txt").write_text(
            adb("shell", "dumpsys", "activity", "activities"))
        wait_nodes = [n for n in observed if n["text"] == "Wait" and n["enabled"] != "false"]
        if len(wait_nodes) != 1:
            raise AssertionError("SystemUI ANR had no unique native Wait button")
        tap(wait_nodes[0])
        print("P07_EMULATOR_SYSTEM_UI_ANR_WAIT_TAPPED_ONCE", flush=True)
        time.sleep(7)
        continue
    if skip_nodes:
        tap(skip_nodes[0])
        check_app_crash()
        print("P07_REAL_SECOND_ONBOARDING_SKIP_TAPPED", flush=True)
        break
    time.sleep(4)
else:
    raise AssertionError("Actual second onboarding slide Skip not visible after bounded recovery")
