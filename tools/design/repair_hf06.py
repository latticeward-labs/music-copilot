#!/usr/bin/env python3
"""Repair the pre-existing WF-06 Driving affordance; no new app actions."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import hashlib, json

root = Path("docs/high-fidelity")
image_path = root / "WF-06/HF-06_FULL_PLAYER_PLAYING.png"
board_path = root / "PHASE_4_PRIMARY_BOARD.png"
register_path = root / "PHASE_4_PRIMARY_REGISTER.json"
spec_path = root / "WF-06/HF-06_FULL_PLAYER_PLAYING.md"
validation_path = root / "PHASE_4_VALIDATION.md"
audit_path = root / "PHASE_4_VISUAL_AUDIT_2026-10-11.md"

screen = Image.open(image_path).convert("RGB")
assert screen.size == (1080, 2400)
d = ImageDraw.Draw(screen)
d.rectangle((404, 1743, 701, 1805), fill="#090D12")
d.rectangle((331, 1984, 749, 2185), fill="#090D12")
d.rounded_rectangle((345, 2010, 735, 2166), radius=75, fill="#143646", outline="#67D8FF", width=5)
fonts = ["/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", "/usr/share/fonts/truetype/liberation2/LiberationSans-Bold.ttf"]
font_path = next((p for p in fonts if Path(p).exists()), None)
assert font_path, "TrueType font is required"
d.text((540, 2086), "Driving", fill="#67D8FF", font=ImageFont.truetype(font_path, 47), anchor="mm")
screen.save(image_path, "PNG", optimize=True)
with Image.open(image_path) as check:
    check.load()
    assert check.size == (1080, 2400)

board = Image.open(board_path).convert("RGB")
assert board.size == (2400, 1600)
board.paste(screen.resize((275, 614), Image.Resampling.LANCZOS), (133, 748))
board.save(board_path, "PNG", optimize=True)
with Image.open(board_path) as check:
    check.load()
    assert check.size == (2400, 1600)

sha = lambda path: hashlib.sha256(path.read_bytes()).hexdigest()
new_sha, board_sha = sha(image_path), sha(board_path)
register = json.loads(register_path.read_text())
record = next(item for item in register["artifacts"] if item["id"] == "HF-06_FULL_PLAYER_PLAYING")
old_sha = record["pngSha256"]
record["pngSha256"] = new_sha
record["phase4Correction"] = "2026-10-11: independent Driving target beneath Playing; no overlap with Next; font-rendered exact copy."
register["review"]["primaryBoardSha256"] = board_sha
register["review"]["visualOverlapRemediation"] = "PASS_CORRECTED_2026_10_11"
register_path.write_text(json.dumps(register, indent=2, ensure_ascii=False) + "\n")

spec = spec_path.read_text().replace(old_sha, new_sha)
spec = spec.replace("its approved source glyph was preserved", "the exact word is rendered with a font")
spec += "\n### Final font-render QA (2026-10-11)\nThe Driving label is drawn from the exact-copy string with a real TrueType font and remains independent of Previous/Pause/Next. Same WF-06 to WF-07 approved route; no new navigation, capability or owner.\n"
spec_path.write_text(spec)
val = validation_path.read_text().replace(old_sha, new_sha)
val += f"\n### Font-render legibility verification (2026-10-11)\nHF-06 SHA-256: {new_sha}\nBoard SHA-256: {board_sha}\nBoth PNGs read back at expected dimensions; no independent human sign-off claimed.\n"
validation_path.write_text(val)
audit = audit_path.read_text()
audit += f"\n## Final font-render evidence\nExact text Driving rendered with installed TrueType font; glyph extraction artifacts eliminated. HF-06 SHA-256: {new_sha}; board SHA-256: {board_sha}; both PNGs decoded successfully.\n"
audit_path.write_text(audit)
print("PASS", new_sha, board_sha)
