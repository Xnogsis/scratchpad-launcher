# Scratchpad Plus

A minimal, portrait-first Android home screen with a persistent Markdown scratchpad and a small app grid.

Write something down without opening an app, and keep it visible through restarts. Format notes, tick off tasks, and give the clock and date a style of their own.

### Fork history

**[Olauncher](https://github.com/tanujnotes/Olauncher) → [Scratchpad Launcher](https://github.com/mstcgalis/scratchpad-launcher) → [Scratchpad Plus](https://github.com/Xnogsis/scratchpad-launcherPlus)**

Scratchpad Plus is a fork of mstcgalis's Scratchpad Launcher, which added a persistent scratchpad to tanujnotes's Olauncher. This fork builds on both projects with Markdown editing, manual note backups, clock/date customization, and simpler home-screen gestures.

### Install

Download signed APKs from [this fork's GitHub Releases](https://github.com/Xnogsis/scratchpad-launcherPlus/releases). Requires Android 7.0 or newer. Source on the default branch can include changes not yet in a release.

The [F-Droid listing](https://f-droid.org/packages/app.scratchpad.launcher/) belongs to upstream Scratchpad Launcher, not Scratchpad Plus. Both use `app.scratchpad.launcher`, but this fork has its own signing key: an upstream/F-Droid installation cannot be updated in place with this APK. Back up your note before uninstalling upstream, then install this fork and restore the note. Updates signed with this fork's same key retain your data.

### Features and controls

- **Scratchpad:** tap to edit; changes save automatically. Markdown symbols appear while editing and hide when reading. The toolbar supports bold, italic, headings, bullets, tasks, and indentation. Choose a format on an empty line and start typing; tap Done or dismiss the keyboard to read it.
- **Tasks and text selection:** tap a checkbox to toggle it. Hold note text to select and copy it.
- **Apps:** tap an empty app slot, then tap an app to add it. Tap a filled slot to launch it. Hold a home app for Uninstall, Rename, Hide, Info, or Remove.
- **Settings:** hold an empty slot, blank note space, margins, or the clock/date. A hold on note text keeps native text selection.
- **Appearance:** choose note fonts and an optional translucent backdrop. The clock/date editor has 16 presets, eight font choices, independent sizes and weights, date formats, letter spacing, a vertical gap, and a live preview. Bundled fonts work offline.
- **Backups:** export a plain Markdown file through Android's file picker. Restore validates UTF-8 text up to 1 MiB and asks before replacing the note. Backups contain note text only. Folder sync writes scratchpad.md (renamed from scratchpad.txt upstream; existing files are renamed).
- **Portrait:** the launcher requests portrait orientation; a landscape appearance editor is deferred.

### Privacy

No accounts or analytics. Scratchpad text is stored locally in its own preferences file and excluded from Android auto-backup. The launcher does not upload notes. Manual export and optional folder sync write only to a location you choose; another app or storage provider can sync that location if you configure it. Network access is limited to the optional daily-wallpaper feature (off by default) and outbound links you tap yourself. Fonts are bundled or provided by Android.

### Development

Single Kotlin/Gradle module. Use Java 21, Android SDK 35, and `just` (see `justfile`). Set `JAVA_HOME` and `ANDROID_HOME` for your installation.

- `just build` — assemble debug APK
- `just test` — unit tests
- `just lint` — Android lint
- `just run` — install debug build on a connected device/emulator and launch it
- `./gradlew assembleRelease` — build a signed release using `RELEASE_KEYSTORE_PATH`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, and `RELEASE_KEY_PASSWORD`

The inherited `just release` recipe still targets upstream and must not be used to publish this fork. Publish releases to `Xnogsis/scratchpad-launcherPlus` and reuse this fork's signing key. Never commit keystores or signing credentials.

The application ID remains `app.scratchpad.launcher` and the Kotlin namespace remains `app.olauncher`; renaming the app does not change its local data storage.

### License

[GNU GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html), inherited through [Scratchpad Launcher](https://github.com/mstcgalis/scratchpad-launcher) from [Olauncher](https://github.com/tanujnotes/Olauncher) by [tanujnotes](https://github.com/tanujnotes). The original launcher base is Olauncher v6.7.19. Bundled fonts include their SIL Open Font License notices in `app/src/main/assets/font-licenses/`.
