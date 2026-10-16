#!/usr/bin/env python3
"""P05 real Android permission-controller interaction; never read personal media."""
import argparse
import re
import subprocess
import tempfile
import time
import xml.etree.ElementTree as ET
from pathlib import Path

BOUNDS = re.compile(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]")

def adb(*args):
    return subprocess.run(["adb", *args], check=True, timeout=45, text=True,
                          capture_output=True).stdout

def nodes():
    # Android 13 UIAutomator sometimes reports a transient unavailable XML
    # while PermissionController initializes. Dump and pull must both succeed,
    # and the same attempt's fresh XML must parse, before examining UI state.
    error = None
    for attempt in range(6):
        with tempfile.TemporaryDirectory() as folder:
            dst = Path(folder) / "hierarchy.xml"
            try:
                adb("shell", "uiautomator", "dump", "/sdcard/issue5_p05.xml")
                subprocess.run(["adb", "pull", "/sdcard/issue5_p05.xml", str(dst)],
                               check=True, timeout=25, stdout=subprocess.DEVNULL,
                               stderr=subprocess.DEVNULL)
                return [{key: n.get(key, "") for key in
                         ("text", "content-desc", "package", "resource-id", "bounds", "enabled", "checked", "checkable")}
                        for n in ET.parse(dst).getroot().iter("node")]
            except (subprocess.CalledProcessError, subprocess.TimeoutExpired,
                    ET.ParseError, OSError) as failure:
                error = type(failure).__name__
                time.sleep(2)
    raise AssertionError("Native UI hierarchy unavailable after 6 fresh attempts: " + str(error))

def tap(n):
    found = BOUNDS.fullmatch(n["bounds"])
    if not found: raise AssertionError("No bounds for tappable system control")
    x, y, a, b = map(int, found.groups())
    adb("shell", "input", "tap", str((x+a)//2), str((y+b)//2))

def app(label, do_tap=False):
    for i in range(14):
        for n in nodes():
            if label == n["text"] or label == n["content-desc"]:
                if do_tap: tap(n)
                print("APP_OK", label, "TAP" if do_tap else "")
                return
        time.sleep(1)
    raise AssertionError("Missing approved app text: " + label)

def choose_never_ask_if_present():
    # Android 10 may show a checkbox on the second request; Android 13
    # may instead apply user-fixed after two real Deny presses.
    for n in nodes():
        if "permissioncontroller" not in n["package"] and "packageinstaller" not in n["package"]:
            continue
        txt = (n["text"] + " " + n["content-desc"]).casefold()
        if any(x in txt for x in ("don't ask again", "don’t ask again", "never ask again")):
            if n["checked"] != "true":
                tap(n)
                print("ANDROID_NATIVE_NEVER_ASK_CHECKED")
            return
    print("ANDROID_NATIVE_NEVER_ASK_CHECKBOX_ABSENT")

def system(option):
    # Android 10 and 13 permissioncontroller can use different localized text.
    # This test runs English google_apis AVDs; resource IDs are the fallback.
    for i in range(18):
        ns = nodes()
        candidates = [n for n in ns if
            ("permissioncontroller" in n["package"] or "packageinstaller" in n["package"])]
        if candidates:
            for n in candidates:
                label = n["text"].casefold().strip()
                res = n["resource-id"].casefold()
                if option == "deny":
                    hit = (label in ("deny", "don't allow", "don’t allow", "not now")
                           or "permission_deny_button" in res)
                else:
                    hit = (label in ("allow", "allow access", "allow only while using the app")
                           or ("permission_allow_button" in res
                               and "dont" not in res and "deny" not in res))
                if hit and BOUNDS.fullmatch(n["bounds"]):
                    tap(n)
                    print("ANDROID_NATIVE_PERMISSION_DIALOG", option.upper(), "TAPPED")
                    return
        time.sleep(2)
    raise AssertionError("Android-owned permission dialog/button missing: "+option)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("action", choices=["app", "tap", "system-deny", "system-allow", "system-never-ask-if-present"])
    parser.add_argument("label", nargs="?", default="")
    args = parser.parse_args()
    if args.action in ("app", "tap"): app(args.label, args.action == "tap")
    elif args.action == "system-never-ask-if-present": choose_never_ask_if_present()
    else: system(args.action.split("-")[1])
