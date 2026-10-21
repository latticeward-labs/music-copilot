#!/usr/bin/env python3
"""Issue 5 P04: real Android UiAutomator hierarchy, accessible labels and taps.

Synthetic static UI strings only. Never collect music names, file paths, raw audio,
speech, account values or external telemetry.
"""
import argparse
import re
import subprocess
import tempfile
import time
import xml.etree.ElementTree as ET
from pathlib import Path

BOUNDS = re.compile(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]")

def adb(*args):
    return subprocess.run(["adb", *args], check=True, timeout=40,
                          stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, text=True).stdout

def nodes():
    adb("shell", "uiautomator", "dump", "/sdcard/issue5_p04.xml")
    with tempfile.TemporaryDirectory() as folder:
        local = Path(folder) / "screen.xml"
        subprocess.run(["adb", "pull", "/sdcard/issue5_p04.xml", str(local)],
                       check=True, timeout=20, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        return [
            {"text": n.get("text", "") or "", "desc": n.get("content-desc", "") or "",
             "bounds": n.get("bounds", ""), "enabled": n.get("enabled", "")}
            for n in ET.parse(local).getroot().iter("node")
        ]

def navigate(label, click=False, scroll=False, require_disabled=False):
    last = []
    for i in range(12):
        last = nodes()
        matches = [x for x in last if label in (x["text"], x["desc"])]
        if matches:
            obj = matches[0]
            if require_disabled and obj["enabled"] != "false":
                raise SystemExit("UI_FAIL: expected disabled: " + label + " got " + repr(obj))
            if click:
                if obj["enabled"] == "false":
                    raise SystemExit("UI_FAIL: cannot click disabled: " + label)
                bounds = BOUNDS.fullmatch(obj["bounds"])
                if not bounds: raise SystemExit("UI_FAIL: bounds unavailable for " + label)
                x1, y1, x2, y2 = map(int, bounds.groups())
                adb("shell", "input", "tap", str((x1+x2)//2), str((y1+y2)//2))
            print("UI_OK: " + label + (" CLICK" if click else "") +
                  (" DISABLED" if require_disabled else ""))
            return
        if scroll:
            adb("shell", "input", "swipe", "180", "680", "180", "280", "350")
        else:
            time.sleep(2)
    raise SystemExit("UI_FAIL: missing " + label + " after hierarchy scans")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--expect", required=True)
    parser.add_argument("--tap", action="store_true")
    parser.add_argument("--scroll", action="store_true")
    parser.add_argument("--disabled", action="store_true")
    a = parser.parse_args()
    navigate(a.expect, a.tap, a.scroll, a.disabled)
