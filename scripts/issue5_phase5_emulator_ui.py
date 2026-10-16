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
    # Android 10 may show a checkable "Don't ask again" checkbox on the
    # second native prompt. Never tap a broad text/label parent: its bounds
    # can cover the dialog action row and accidentally dismiss the prompt.
    ns = nodes()
    for n in ns:
        if "permissioncontroller" not in n["package"] and "packageinstaller" not in n["package"]:
            continue
        txt = (n["text"] + " " + n["content-desc"]).casefold()
        res = n["resource-id"].casefold()
        named = any(s in txt for s in
                    ("don't ask again", "don’t ask again", "never ask again"))
        native_box = "dont_ask" in res or "dontask" in res
        if (named or native_box) and n["checkable"] == "true":
            if n["checked"] != "true":
                tap(n)
                print("ANDROID_NATIVE_NEVER_ASK_CHECKBOX_CHECKED", n["resource-id"])
            else:
                print("ANDROID_NATIVE_NEVER_ASK_ALREADY_CHECKED")
            return
    # Some API29 PermissionController layouts expose "Don't ask again" as
    # a one-tap DENY OPTION rather than a separately checkable checkbox.
    # A real prior API29 failure screenshot confirmed this native option
    # immediately dismisses the OS dialog and shows Settings-only.
    for n in ns:
        if "permissioncontroller" not in n["package"] and "packageinstaller" not in n["package"]:
            continue
        label = (n["text"] or n["content-desc"]).strip().casefold()
        if label in ("don't ask again", "don’t ask again", "never ask again"):
            if BOUNDS.fullmatch(n["bounds"]):
                tap(n)
                print("ANDROID_NATIVE_DONT_ASK_DENY_OPTION_TAPPED")
                return
    print("ANDROID_NATIVE_NEVER_ASK_OPTION_NOT_AVAILABLE")


def native_denial_or_proven_os_fixed():
    # A native Android 10 checkbox may immediately dismiss the dialog on
    # some OS images. Accept that transition only with BOTH an app Settings-
    # only screen and Android's own USER_FIXED+granted=false for THIS permission.
    apk = "com.latticeward.musiccopilot.debug"
    sdk = int(adb("shell", "getprop", "ro.build.version.sdk").strip())
    permission = ("android.permission.READ_MEDIA_AUDIO" if sdk >= 33
                  else "android.permission.READ_EXTERNAL_STORAGE")
    native = [n for n in nodes() if "permissioncontroller" in n["package"] or
              "packageinstaller" in n["package"]]
    if native:
        system("deny")
        print("ANDROID_NATIVE_SECOND_DENIAL_TAPPED")
        return
    result = adb("shell", "dumpsys", "package", apk)
    permission_lines = [line for line in result.splitlines()
                        if permission in line and "granted=" in line]
    if not any("granted=false" in line and "USER_FIXED" in line
               for line in permission_lines):
        raise AssertionError(
            "No Android native denial dialog and no OS-owned user-fixed denial")
    app("Open Android Settings")
    print("ANDROID_OS_USER_FIXED_DENIAL_CONFIRMED")


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
    parser.add_argument("action", choices=["app", "tap", "system-deny", "system-allow", "system-never-ask-if-present", "system-deny-or-os-fixed"])
    parser.add_argument("label", nargs="?", default="")
    args = parser.parse_args()
    if args.action in ("app", "tap"): app(args.label, args.action == "tap")
    elif args.action == "system-never-ask-if-present": choose_never_ask_if_present()
    elif args.action == "system-deny-or-os-fixed": native_denial_or_proven_os_fixed()
    else: system(args.action.split("-")[1])
