#!/usr/bin/env python3
"""Regenerates the icon data bundled with the Android app.

Downloads the Material Icons font metadata and Font Awesome Free metadata and writes, under
uhabits-android/src/main:

  res/raw/material_icon_codepoints.txt   "name hex" per line (Material Icons font)
  res/raw/material_icon_keywords.txt     "name|keywords" per line, used by the icon search
  res/raw/fa_icon_codepoints.txt         "name hex|keywords" per line (Font Awesome Free, solid)
  assets/MaterialIcons-Regular.ttf, assets/fa-solid-900.ttf

Usage: tools/generate-icon-data.py
"""
import json
import urllib.request
from pathlib import Path

MATERIAL = "https://raw.githubusercontent.com/google/material-design-icons/master/font/"
FA = "https://raw.githubusercontent.com/FortAwesome/Font-Awesome/6.x/"
MS_METADATA = "https://fonts.google.com/metadata/icons?key=material_symbols&incomplete=true"
MAIN = Path(__file__).resolve().parent.parent / "uhabits-android" / "src" / "main"


# Words people use for habits that the upstream tags miss. Keys are icon names.
EXTRA_MATERIAL = {
    "no_adult_content": "porn pornography nofap nsfw masturbation",
    "18_up_rating": "porn pornography nsfw adult",
    "fitness_center": "gym workout weights",
    "self_improvement": "meditation meditate mindfulness yoga",
    "spa": "meditation relax",
    "smoking_rooms": "cigarette cigarettes tobacco",
    "smoke_free": "cigarette cigarettes tobacco quit stop",
    "vape_free": "vape vaping quit stop",
    "bedtime": "sleep",
    "grass": "weed",
    "water_drop": "water hydrate drink",
}
EXTRA_FA = {
    "tooth": "floss flossing brush brushing teeth",
    "eye-slash": "porn nofap nsfw",
    "ban-smoking": "cigarette cigarettes tobacco quit stop",
    "cannabis": "smoke smoking joint",
    "joint": "weed marijuana cannabis smoke smoking",
    "bong": "weed marijuana cannabis smoke smoking",
    "dumbbell": "workout",
    "person-praying": "meditation meditate prayer",
    "hands-praying": "meditation meditate prayer",
    "brain": "meditation mindfulness",
    "person-running": "jog jogging cardio",
}


def get(url: str) -> bytes:
    with urllib.request.urlopen(url) as r:
        return r.read()


def words(text: str) -> list[str]:
    return [w for w in text.lower().replace("-", " ").replace("_", " ").split() if w]


def main() -> None:
    (MAIN / "assets").mkdir(parents=True, exist_ok=True)
    (MAIN / "res" / "raw").mkdir(parents=True, exist_ok=True)

    (MAIN / "assets" / "MaterialIcons-Regular.ttf").write_bytes(get(MATERIAL + "MaterialIcons-Regular.ttf"))
    codepoints = get(MATERIAL + "MaterialIcons-Regular.codepoints").decode()
    (MAIN / "res/raw/material_icon_codepoints.txt").write_text(codepoints)
    names = {line.split()[0] for line in codepoints.splitlines() if line.strip()}

    metadata = json.loads(get(MS_METADATA).decode().split("\n", 1)[1])
    tags: dict[str, set[str]] = {}
    for icon in metadata["icons"]:
        if icon["name"] not in names:
            continue
        bucket = tags.setdefault(icon["name"], set())
        for tag in icon.get("tags", []) + icon.get("categories", []):
            bucket.update(words(tag))
    lines = []
    for name in sorted(names):
        extra = sorted((tags.get(name, set()) | set(words(EXTRA_MATERIAL.get(name, "")))) - set(words(name)))
        lines.append(f"{name}|{' '.join(extra)}")
    (MAIN / "res/raw/material_icon_keywords.txt").write_text("\n".join(lines) + "\n")

    (MAIN / "assets" / "fa-solid-900.ttf").write_bytes(get(FA + "webfonts/fa-solid-900.ttf"))
    fa = json.loads(get(FA + "metadata/icons.json"))
    lines = []
    for name, icon in sorted(fa.items()):
        if "solid" not in icon["free"]:
            continue
        extra = set(words(icon.get("label", ""))) | {
            w for term in icon.get("search", {}).get("terms", []) for w in words(term)
        }
        extra |= set(words(EXTRA_FA.get(name, "")))
        extra -= set(words(name))
        lines.append(f"{name} {icon['unicode']}|{' '.join(sorted(extra))}")
    (MAIN / "res/raw/fa_icon_codepoints.txt").write_text("\n".join(lines) + "\n")


if __name__ == "__main__":
    main()
