#!/usr/bin/env python3
"""Regenerate Android launcher icons with adaptive-icon safe-zone padding."""

from __future__ import annotations

from pathlib import Path

from PIL import Image

REPO_ROOT = Path(__file__).resolve().parents[1]
RES_DIR = REPO_ROOT / "composeApp/src/androidMain/res"
SOURCE_FOREGROUND = RES_DIR / "drawable/ic_launcher_foreground.png"
BACKGROUND_COLOR = (226, 114, 91, 255)  # #E2725B terracotta

# Adaptive icons mask ~17% per edge; keep artwork inside the 66% center circle.
SAFE_ZONE_SCALE = 0.66

MIPMAP_SIZES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

FOREGROUND_CANVAS = 432  # 108dp @ xxxhdpi


def scale_to_safe_zone(image: Image.Image, canvas_size: int) -> Image.Image:
    content = image.convert("RGBA")
    target = int(round(canvas_size * SAFE_ZONE_SCALE))
    content.thumbnail((target, target), Image.Resampling.LANCZOS)
    canvas = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    offset = ((canvas_size - content.width) // 2, (canvas_size - content.height) // 2)
    canvas.paste(content, offset, content)
    return canvas


def compose_launcher_icon(foreground: Image.Image, size: int) -> Image.Image:
    fg = foreground.resize((size, size), Image.Resampling.LANCZOS)
    background = Image.new("RGBA", (size, size), BACKGROUND_COLOR)
    background.alpha_composite(fg)
    return background


def main() -> None:
    if not SOURCE_FOREGROUND.exists():
        raise SystemExit(f"Missing source foreground: {SOURCE_FOREGROUND}")

    original = Image.open(SOURCE_FOREGROUND)
    padded_foreground = scale_to_safe_zone(original, FOREGROUND_CANVAS)

    padded_foreground.save(SOURCE_FOREGROUND, optimize=True)
    padded_foreground.save(
        RES_DIR / "drawable-xxxhdpi/ic_launcher_foreground.png",
        optimize=True,
    )

    for folder, size in MIPMAP_SIZES.items():
        icon = compose_launcher_icon(padded_foreground, size)
        target_dir = RES_DIR / folder
        target_dir.mkdir(parents=True, exist_ok=True)
        icon.save(target_dir / "ic_launcher.png", optimize=True)
        icon.save(target_dir / "ic_launcher_round.png", optimize=True)

    print("Regenerated launcher foreground and mipmaps with safe-zone padding.")


if __name__ == "__main__":
    main()
