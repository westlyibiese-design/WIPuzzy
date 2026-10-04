# WIPuzzle (Android, Kotlin + Jetpack Compose)

Native rebuild of the WIPuzzle sliding picture puzzle. Same rules, images and name as the web version, with a new interface.

- 100 campaign levels, all 3x3, in four worlds
- Holding-slot mechanic (the single empty position starts above the board)
- Stars, score, saved progress, achievements
- Custom puzzle: pick a photo, frame it, play 3x3 or 4x4
- Sound effects and music, light/dark/system theme

Package: `com.westly.wipuzzle` | minSdk 26 | Compose BOM 2024.10.01

Images live in `app/src/main/assets/images/stageN.jpg`; sounds in `app/src/main/res/raw/`.
CI: `.github/workflows/build.yml` builds the debug APK (artifact `WIPuzzle-debug-apk`).
