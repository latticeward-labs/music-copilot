#!/usr/bin/env python3
"""P03 emulator smoke: inspect real Android accessibility XML, never OCR/private data."""
import argparse
import re
import subprocess
import tempfile
import time
import xml.etree.ElementTree as ET
from pathlib import Path

B = re.compile(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]")

def snapshot():
    subprocess.run(["adb", "shell", "uiautomator", "dump", "/sdcard/issue5_p03.xml"],
                   check=True, stdout=subprocess.DEVNULL, timeout=35)
    with tempfile.TemporaryDirectory() as folder:
        xml = Path(folder) / "window.xml"
        subprocess.run(["adb", "pull", "/sdcard/issue5_p03.xml", str(xml)],
                       check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=20)
        root = ET.parse(xml).getroot()
        return [
            {
                "text": n.get("text", "") or "",
                "description": n.get("content-desc", "") or "",
                "bounds": n.get("bounds", ""),
            }
            for n in root.iter("node")
        ]

def check(expect, click):
    last = []
    for _ in range(10):
        last = snapshot()
        texts = [s["text"] + " " + s["description"] for s in last]
        if all(any(e in x for x in texts) for e in expect):
            print("UI_VISIBLE: " + ", ".join(expect))
            if click:
                nodes = [x for x in last if x["text"] == click or x["description"] == click]
                if not nodes:
                    raise SystemExit("UI_SMOKE_FAIL: button not found: " + click)
                match = B.fullmatch(nodes[0]["bounds"])
                if not match:
                    raise SystemExit("UI_SMOKE_FAIL: no bounds for " + click)
                x1,y1,x2,y2 = map(int, match.groups())
                subprocess.run(["adb","shell","input","tap",str((x1+x2)//2),str((y1+y2)//2)],
                               check=True, timeout=20)
                print("UI_TAPPED: " + click)
            return
        time.sleep(2)
    summary = sorted({t["text"] for t in last if t["text"]})
    raise SystemExit("UI_SMOKE_FAIL: expected " + repr(expect) +
                     "; visible (synthetic screen strings only) " + repr(summary[:30]))

if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--expect", action="append", required=True)
    ap.add_argument("--tap")
    args = ap.parse_args()
    check(args.expect, args.tap)
