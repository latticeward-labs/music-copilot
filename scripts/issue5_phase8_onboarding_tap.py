#!/usr/bin/env python3
"""P08 emulator-only bounded recovery from native Android SystemUI ANR.

An ANR from com.android.systemui can obscure an otherwise valid first-use
screen. Never suppress Music Copilot crashes, claim an app control that is not
visible, or inject permission state. Only click the system's explicit "Wait"
once, preserve diagnostics, then verify and tap the actual UI control.
"""
from pathlib import Path
import subprocess
import sys
import time
from issue5_phase6_emulator_ui import adb, nodes, tap

output = Path(sys.argv[1])
button = sys.argv[2]
assert button in ("Continue", "Skip")
output.mkdir(parents=True, exist_ok=True)
app = "com.latticeward.musiccopilot.debug"
handled_anr = False

def check_crash():
    crash_log = adb("logcat", "-d", "-b", "crash")
    if "Process: " + app in crash_log:
        raise AssertionError("Native test observed Music Copilot app crash")

for poll in range(18):
    check_crash()
    observed = nodes()
    anr = [n for n in observed if "System UI isn't responding" in
           (n["text"] + " " + n["content-desc"])]
    if anr:
        if handled_anr:
            raise AssertionError("SystemUI ANR recurred after one explicit Wait")
        wait = [n for n in observed if n["text"] == "Wait" and n["enabled"] != "false"]
        if len(wait) != 1 or "android" not in wait[0]["package"]:
            raise AssertionError("Native SystemUI ANR without unambiguous system Wait")
        (output / ("system_ui_anr_before_" + button.lower() + ".png")).write_bytes(
            subprocess.run(["adb", "exec-out", "screencap", "-p"],
                           check=True, capture_output=True, timeout=30).stdout)
        (output / ("system_ui_anr_before_" + button.lower() + ".txt")).write_text(
            adb("shell", "dumpsys", "activity", "activities"))
        tap(wait[0])
        handled_anr = True
        print("P08_NATIVE_SYSTEM_UI_ANR_WAIT_ONCE_" + button.upper(), flush=True)
        time.sleep(5)
        continue

    controls = [n for n in observed if (n["text"] == button or n["content-desc"] == button)
                and app in n["package"] and n["enabled"] != "false"]
    if controls:
        tap(controls[-1])
        check_crash()
        print("P08_NATIVE_APP_CONTROL_VERIFIED_TAPPED_" + button.upper(), flush=True)
        break
    time.sleep(2)
else:
    raise AssertionError("Approved app control did not become available: " + button)
