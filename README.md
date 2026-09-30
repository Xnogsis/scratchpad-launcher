# Scratchpad Launcher

A minimal Android home screen that's also a piece of paper.

The top half is a plain text box that's always there — write something down without opening an app, and it survives reboots. The bottom half is a small, fixed list of apps. Nothing else.

### Install

Available on [F-Droid](https://f-droid.org/packages/app.scratchpad.launcher/). Signed release APKs are also attached to [GitHub Releases](https://github.com/mstcgalis/scratchpad-launcher/releases).

### Why

Sometimes you just want to write something down and have it always visible without opening any app. This is a fork of [Olauncher](https://github.com/tanujnotes/Olauncher) — a minimal, ad-free Android launcher — with the app grid cut down and a persistent scratchpad put in its place.

### Privacy

No accounts, no analytics, no network calls except the optional daily-wallpaper feature (off by default) and outbound links you tap yourself. Scratchpad text is stored locally in its own preferences file and excluded from Android auto-backup, so it never leaves the device.

### Development

Single Gradle module, built with `just` (see `justfile`):

- `just build` — assemble debug APK
- `just test` — unit tests
- `just lint` — Android lint
- `just run` — install debug build on a connected device/emulator and launch it
- `just release VERSION` — bump version, build/sign a release APK, tag, push, and publish a GitHub release (e.g. `just release 1.1.9`)

### Status

This is a personal fork of Olauncher with its own `applicationId` and icon. Published on F-Droid (merged into `fdroiddata` 2026-09-22; current version 1.1.9).

### License

[GNU GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html) — inherited from the upstream [Olauncher](https://github.com/tanujnotes/Olauncher) project by [tanujnotes](https://github.com/tanujnotes), licensed the same way. Source changes in this fork are on top of Olauncher v6.7.19.
