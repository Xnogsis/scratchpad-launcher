"""Renders every Scratchpad Plus brand asset. Run from the repo root: python3 docs/brand/make_brand.py"""
import io
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
from matplotlib.font_manager import FontProperties
from matplotlib.patches import FancyBboxPatch, Polygon
from PIL import Image

INK, PAPER, FOLD, RED, RULE = "#1A1E1D", "#F2EBDD", "#CBBFA6", "#E8452C", "#262C2A"
RES = Path("app/src/main/res")
FONT = Path("app/src/main/res/font")
DENSITIES = {"mdpi": 1, "hdpi": 1.5, "xhdpi": 2, "xxhdpi": 3, "xxxhdpi": 4}


def rot(points, deg, center):
    t = np.radians(deg)
    m = np.array([[np.cos(t), -np.sin(t)], [np.sin(t), np.cos(t)]])
    return np.asarray(points) @ m.T + center


def scribble(x0, x1, y, seed, amp=1.0):
    rng = np.random.default_rng(seed)
    x = np.linspace(x0, x1, 80)
    phase = rng.uniform(0, 6)
    return np.c_[x, y + amp * (0.7 * np.sin(x * 0.85 + phase) + 0.3 * np.sin(x * 2.4 + phase))]


def draw_mark(ax, px, mono=False, layer="all"):
    """The mark in a 108-unit adaptive-icon space; px is the rendered canvas width.
    layer splits the mark so the plus can punch a transparent gap into the sheet."""
    lw = lambda units: units * px / 108 * 72 / 100
    paper, fold, ink, red = ("white", "#9A9A9A", "black", "white") if mono else (PAPER, FOLD, INK, RED)
    center, tilt = np.array([51.0, 56.0]), -7
    cx, cy, arm = 68, 38, 8
    plus = (([cx - arm, cx + arm], [cy, cy]), ([cx, cx], [cy - arm, cy + arm]))
    if layer in ("halo", "plus"):
        width, color = (11.5, "black") if layer == "halo" else (7, red)
        for xs, ys in plus:
            ax.plot(xs, ys, lw=lw(width), color=color, solid_capstyle="round")
        return
    sheet = [(-18, -21), (18, -21), (18, 12), (9, 21), (-18, 21)]
    ax.add_patch(Polygon(rot(sheet, tilt, center), closed=True, fc=paper, ec="none"))
    ax.add_patch(Polygon(rot([(9, 21), (9, 12), (18, 12)], tilt, center), closed=True, fc=fold, ec="none"))
    line = dict(color=ink, solid_capstyle="round", solid_joinstyle="round")
    ax.plot(*rot(scribble(-12, 4, 12, 1, amp=0.35), tilt, center).T, lw=lw(3.0), **line)
    box = [(-13, -0.5), (-7, -0.5), (-7, 5.5), (-13, 5.5), (-13, -0.5)]
    ax.plot(*rot(box, tilt, center).T, lw=lw(1.5), **line)
    ax.plot(*rot([(-12, 2.6), (-10.4, 0.9), (-7.4, 4.8)], tilt, center).T, lw=lw(1.7), color=red if not mono else ink,
            solid_capstyle="round", solid_joinstyle="round")
    ax.plot(*rot(scribble(-4, 12, 2.5, 2), tilt, center).T, lw=lw(1.9), **line)
    ax.plot(*rot(scribble(-12, 13, -7, 3), tilt, center).T, lw=lw(1.9), **line)
    ax.plot(*rot(scribble(-12, 1, -15, 4), tilt, center).T, lw=lw(1.9), **line)
    if layer == "all":
        for width, color in ((11.5, "black" if mono else INK), (7, red)):
            for xs, ys in plus:
                ax.plot(xs, ys, lw=lw(width), color=color, solid_capstyle="round")


def render(px, mono=False, background=None, layer="all"):
    fig = plt.figure(figsize=(px / 100, px / 100), dpi=100)
    ax = fig.add_axes([0, 0, 1, 1])
    ax.set_xlim(0, 108)
    ax.set_ylim(0, 108)
    ax.axis("off")
    if background:
        ax.add_patch(plt.Rectangle((0, 0), 108, 108, fc=background, ec="none"))
    draw_mark(ax, px, mono, layer)
    return to_image(fig)


def foreground(px):
    """Transparent mark: the plus sits in a see-through gap cut out of the sheet."""
    sheet = render(px, layer="sheet")
    halo = render(px, layer="halo").getchannel("A")
    alpha = np.minimum(np.asarray(sheet.getchannel("A")), 255 - np.asarray(halo))
    sheet.putalpha(Image.fromarray(alpha.astype(np.uint8)))
    sheet.alpha_composite(render(px, layer="plus"))
    return sheet


def to_image(fig, **kw):
    buf = io.BytesIO()
    fig.savefig(buf, format="png", transparent=True, **kw)
    plt.close(fig)
    return Image.open(buf).convert("RGBA")


def legacy(px):
    """Transparent icon for launchers without adaptive icons: the visible 72/108 crop of the foreground."""
    full = foreground(round(px * 1.5) * 2)
    o = full.width * 18 // 108
    return full.crop((o, o, full.width - o, full.width - o)).resize((px, px), Image.LANCZOS)


def monochrome(px):
    img = render(px * 2, mono=True, background="black").convert("L").resize((px, px), Image.LANCZOS)
    out = Image.new("RGBA", img.size, (255, 255, 255, 0))
    out.putalpha(img)
    return out


def save(img, path, rgb=False):
    path.parent.mkdir(parents=True, exist_ok=True)
    if rgb:
        bg = Image.new("RGB", img.size, INK)
        bg.paste(img, mask=img.getchannel("A"))
        img = bg
    if path.suffix == ".webp":
        img.save(path, lossless=True)
    elif path.suffix == ".jpg":
        img.save(path, quality=92, optimize=True)
    else:
        img.save(path, optimize=True)


def banner(w, h, path, tagline=True):
    fig = plt.figure(figsize=(w / 100, h / 100), dpi=100)
    ax = fig.add_axes([0, 0, 1, 1])
    ax.set_xlim(0, w)
    ax.set_ylim(0, h)
    ax.axis("off")
    ax.add_patch(plt.Rectangle((0, 0), w, h, fc=INK, ec="none"))
    for y in np.arange(h * 0.12, h, h * 0.12):
        ax.axhline(y, color=RULE, lw=1)
    ax.axvline(w * 0.07, color="#4A2722", lw=1.4)
    tile = min(h * 0.62, w * 0.2)
    x0, y0 = w * 0.1, (h - tile) / 2
    tx = x0 + tile + w * 0.045
    serif = FontProperties(fname=FONT / "fraunces_700.ttf")
    sans = FontProperties(fname=FONT / "space_grotesk_400.ttf")
    size = min(h * 0.2, w * 0.0625) * 72 / 100
    title = ax.text(tx, h * 0.52, "Scratchpad", fontproperties=serif, fontsize=size, color=PAPER, va="baseline")
    fig.canvas.draw()
    bb = title.get_window_extent().transformed(ax.transData.inverted())
    ax.text(bb.x1 + w * 0.012, h * 0.52, "Plus", fontproperties=serif, fontsize=size, color=RED, va="baseline")
    ax.text(tx, h * 0.36, "A home screen that's mostly a notepad.", fontproperties=sans,
            fontsize=size * 0.3, color="#B8B0A0", va="baseline")
    if tagline:
        ax.text(tx, h * 0.24, "Olauncher \u2192 Scratchpad Launcher \u2192 Scratchpad Plus", family="DejaVu Sans Mono",
                fontsize=size * 0.17, color="#6F6A60", va="baseline")
    img = to_image(fig)
    mark = legacy(int(tile))
    img.alpha_composite(mark, (int(x0), int(h - y0 - tile)))
    save(img, path, rgb=True)


def main():
    for name, scale in DENSITIES.items():
        d = RES / f"mipmap-{name}"
        save(foreground(round(108 * scale)), d / "ic_launcher_foreground.webp")
        save(monochrome(round(108 * scale)), d / "ic_launcher_monochrome.webp")
        save(legacy(round(48 * scale)), d / "ic_launcher.webp")
        save(legacy(round(48 * scale)), d / "ic_launcher_round.webp")
    save(legacy(192), RES / "drawable-nodpi" / "ic_brand.webp")
    save(legacy(1024), Path("icon.png"))
    save(legacy(512), Path("app/src/main/ic_launcher-playstore.png"))
    save(legacy(512), Path("fastlane/metadata/android/en-US/images/icon.png"))
    banner(1280, 400, Path("docs/brand/banner.png"))
    banner(1280, 640, Path("docs/brand/social-preview.png"))
    banner(1024, 500, Path("fastlane/metadata/android/en-US/images/featureGraphic.jpg"), tagline=False)
    save(foreground(1024), Path("docs/brand/adaptive-full.png"))


if __name__ == "__main__":
    main()
