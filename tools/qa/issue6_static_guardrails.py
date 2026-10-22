#!/usr/bin/env python3
"""Issue #6 P11 fail-closed static guardrails. Run against repository root in P12.

Checks deliberately use source-level assertions, not simulated Android runtime
evidence. They cannot replace Gradle lint, KAPT, Room native tests, TalkBack or QA.
Print only control IDs and relative SOURCE paths, never user data.
"""
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[2]

FILES = {
    "manifest": "app/src/main/AndroidManifest.xml",
    "db": "app/src/main/java/com/latticeward/musiccopilot/data/room/MusicCopilotDatabase.kt",
    "reconcile": "app/src/main/java/com/latticeward/musiccopilot/data/repository/RoomLibraryRepository.kt",
    "scanner": "app/src/main/java/com/latticeward/musiccopilot/data/mediastore/MediaStoreScanner.kt",
    "selection": "app/src/main/java/com/latticeward/musiccopilot/core/application/playback/LibraryMediaSelectionUseCase.kt",
    "library_ui": "app/src/main/java/com/latticeward/musiccopilot/feature/library/LibraryBrowseScreen.kt",
    "search_ui": "app/src/main/java/com/latticeward/musiccopilot/feature/search/LocalSearchScreen.kt",
    "search_vm": "app/src/main/java/com/latticeward/musiccopilot/feature/search/LocalSearchViewModel.kt",
    "browse_vm": "app/src/main/java/com/latticeward/musiccopilot/feature/library/LibraryBrowseViewModel.kt",
    "room_dao": "app/src/main/java/com/latticeward/musiccopilot/data/room/LibraryDao.kt",
    "service": "app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt",
    "schemas_v1": "app/schemas/com.latticeward.musiccopilot.data.room.MusicCopilotDatabase/1.json",
    "schemas_v2": "app/schemas/com.latticeward.musiccopilot.data.room.MusicCopilotDatabase/2.json",
}

def main() -> int:
    text = {}
    missing = []
    for name, relative in FILES.items():
        path = ROOT / relative
        if path.is_file():
            text[name] = path.read_text(encoding="utf-8")
        else:
            missing.append(name)
            text[name] = ""
    checks = [
        ("I6-G01", "Android manifest contains no INTERNET permission",
         not re.search(r'<uses-permission\s+[^>]*android:name="android.permission.INTERNET"', text["manifest"])),
        ("I6-G02", "App Android backup disabled",
         'android:allowBackup="false"' in text["manifest"]),
        ("I6-G03", "No cleartext network traffic",
         'android:usesCleartextTraffic="false"' in text["manifest"]),
        ("I6-G04", "Exported service restricts typed commands to app UID",
         "controller.uid == android.os.Process.myUid()" in text["service"]),
        ("I6-G05", "Room DB exports schemas",
         "exportSchema = true" in text["db"]),
        ("I6-G06", "No destructive Room migration fallback",
         all("fallbackToDestructiveMigration" not in t for t in text.values())),
        ("I6-G07", "MediaStore selection has read-only FD probe",
         'openFileDescriptor(itemUri, "r")' in text["scanner"]),
        ("I6-G08", "No DATA absolute filesystem path in scanner",
         "MediaColumns.DATA" not in text["scanner"].replace(
             "Never project MediaColumns.DATA", "")),
        ("I6-G09", "Selection re-queries the platform before service send",
         0 <= text["selection"].find("library.get(ref)") <
         text["selection"].find("gateway.execute(")),
        ("I6-G10", "No Player instantiated from UI",
         not any("ExoPlayer.Builder" in text[name] or "MediaSession.Builder" in text[name]
                 for name in ("library_ui", "search_ui", "search_vm", "browse_vm"))),
        ("I6-G11", "Library row surface is LazyColumn",
         "LazyColumn(" in text["library_ui"]),
        ("I6-G12", "Search row surface is LazyColumn",
         "LazyColumn(" in text["search_ui"]),
        ("I6-G13", "UI offers TalkBack liveRegion feedback",
         "LiveRegionMode.Polite" in text["library_ui"] and
         "LiveRegionMode.Polite" in text["search_ui"]),
        ("I6-G14", "Permission loss clears library selection",
         "cancelSelection()" in text["browse_vm"]),
        ("I6-G15", "Permission loss clears search results",
         "entries = emptyList()" in text["search_vm"]),
        ("I6-G16", "SQL pages bounded by limits",
         "LIMIT :limit OFFSET :offset" in text["room_dao"]),
        ("I6-G17", "Search uses literal safe substring",
         "instr(normalizedTitle, :needle)" in text["room_dao"]),
        ("I6-G18", "No source user data logging in library/search pipeline",
         all(not re.search(r'\b(Log\.(?:v|d|i|w|e|wtf)\s*\(|println\s*\(|Timber\.)', text[n])
             for n in ("reconcile", "scanner", "selection", "library_ui",
                        "search_ui", "search_vm", "browse_vm"))),
        ("I6-G19", "Room V1 schema checked in",
         bool(text["schemas_v1"])),
        ("I6-G20", "Genuine generated Room V2 schema is checked in",
         bool(text["schemas_v2"])),
    ]
    failed = []
    for code, purpose, ok in checks:
        print(f"{code}: {'PASS_STATIC' if ok else 'FAIL_OR_MISSING'} — {purpose}")
        if not ok: failed.append(code)
    if missing:
        print("MISSING_SOURCE_KEYS:", ", ".join(missing), file=sys.stderr)
    print("RESULT:", len(checks)-len(failed), "/",len(checks),
          "static checks; native/CI/device evidence NOT PROVIDED")
    return 0 if not failed else 1

if __name__ == "__main__":
    raise SystemExit(main())
