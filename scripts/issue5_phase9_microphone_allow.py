#!/usr/bin/env python3
"""P09 native Android microphone permission acceptance, never ADB-grant.

Only a currently visible PermissionController button can be tapped. Do not
replace the OS dialog, set permission flags, or assume ActivityResult grants.
The shell harness subsequently verifies RECORD_AUDIO: granted=true via OS.
"""
import time
from issue5_phase6_emulator_ui import nodes, tap, BOUNDS

for attempt in range(24):
    observed = nodes()
    os_buttons = [
        n for n in observed
        if ("permissioncontroller" in n["package"].lower() or
            "packageinstaller" in n["package"].lower())
        and n.get("enabled") != "false" and BOUNDS.fullmatch(n["bounds"])
    ]
    def preferred(n):
        label = (n["text"] or n["content-desc"]).strip().casefold()
        resource = n["resource-id"].casefold()
        return (
            label in ("while using the app", "allow while using the app",
                      "allow only while using the app", "allow access", "allow")
            or "permission_allow_foreground_only_button" in resource
            or resource.endswith("/permission_allow_button")
        )

    permitted = [n for n in os_buttons if preferred(n)]
    if permitted:
        # Do not click "Only this time" when an ongoing foreground option exists.
        permitted.sort(key=lambda n: (
            0 if "foreground_only" in n["resource-id"] else 1,
            0 if "while using" in n["text"].casefold() else 1
        ))
        chosen = permitted[0]
        tap(chosen)
        print("P09_ANDROID_NATIVE_RECORD_AUDIO_ALLOW_TAPPED "
              + repr(chosen["text"] or chosen["resource-id"]), flush=True)
        break
    time.sleep(1)
else:
    labels = sorted({
        (n["text"] or n["content-desc"] or n["resource-id"])[:100]
        for n in observed if n["text"] or n["content-desc"] or n["resource-id"]
    })
    raise AssertionError(
        "No Android-owned RECORD_AUDIO allow button. Native UI labels="
        + repr(labels[:50])
    )
