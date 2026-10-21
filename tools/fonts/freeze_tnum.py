#!/usr/bin/env python3
"""Builds "Inter Num": Inter with tabular figures (OpenType tnum) frozen into the default digits.

JavaFX can't switch OpenType features on, so amounts set in plain Inter don't line up in columns.
This maps the digit characters 0-9 to Inter's own tabular glyphs (zero.tf ... nine.tf) and renames
the family, as the SIL Open Font License asks of modified versions. Nothing else changes.

Usage (needs fontTools):  python3 tools/fonts/freeze_tnum.py
Reads and writes src/main/resources/com/uzima/ui/fonts/.
"""
from pathlib import Path

from fontTools.ttLib import TTFont

FONTS = Path(__file__).resolve().parents[2] / "src/main/resources/com/uzima/ui/fonts"
WEIGHTS = ["Regular", "Medium", "SemiBold"]
DIGITS = ["zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"]
FAMILY = "Inter Num"


def freeze(weight: str) -> Path:
    font = TTFont(FONTS / f"Inter-{weight}.ttf")
    glyphs = set(font.getGlyphOrder())
    for table in font["cmap"].tables:
        if not table.isUnicode():
            continue
        for i, name in enumerate(DIGITS):
            if ord("0") + i in table.cmap and f"{name}.tf" in glyphs:
                table.cmap[ord("0") + i] = f"{name}.tf"

    legacy_family = FAMILY if weight == "Regular" else f"{FAMILY} {weight}"
    full_name = FAMILY if weight == "Regular" else f"{FAMILY} {weight}"
    postscript = f"InterNum-{weight}"
    names = font["name"]
    for record in list(names.names):
        if record.nameID == 1:
            names.setName(legacy_family, 1, record.platformID, record.platEncID, record.langID)
        elif record.nameID == 3:
            names.setName(f"{postscript};tnum-frozen", 3, record.platformID, record.platEncID,
                          record.langID)
        elif record.nameID == 4:
            names.setName(full_name, 4, record.platformID, record.platEncID, record.langID)
        elif record.nameID == 6:
            names.setName(postscript, 6, record.platformID, record.platEncID, record.langID)
        elif record.nameID == 16:
            names.setName(FAMILY, 16, record.platformID, record.platEncID, record.langID)
        elif record.nameID in (21, 22):
            names.removeNames(nameID=record.nameID)
    out = FONTS / f"InterNum-{weight}.ttf"
    font.save(out)
    return out


if __name__ == "__main__":
    for w in WEIGHTS:
        print("wrote", freeze(w).relative_to(FONTS.parents[6]))
