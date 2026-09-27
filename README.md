# Color Territory 🎨🗺️

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.2-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2F%20Material%203-blue.svg)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-orange.svg)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-37-brightgreen.svg)](https://developer.android.com)

**Color Territory** is a polished local two-player strategy game built for Android using **Jetpack Compose** and **Material 3**. Players take turns selecting terrain colors on an $8 \times 8$ grid to expand their territory outward via flood-fill connections.

---

## 🎮 How to Play

1. **Starting Positions**:
   - **Player 1** starts at the bottom-left corner `(7, 0)`.
   - **Player 2** starts at the top-right corner `(0, 7)`.
2. **Turn Mechanics**:
   - On your turn, choose one available **Terrain Color**. You never tap individual board tiles.
   - Every neutral tile of that color adjacent to your existing territory is claimed—and the claim flows through all contiguous, matching-color tiles (orthogonal connections only: up, down, left, right).
3. **Blocking & Passing**:
   - Opponent territory blocks expansion.
   - Colors that yield zero tile gains are unavailable.
   - If a player has no available legal colors, their turn automatically passes.
4. **Winning Condition**:
   - The game ends when the entire grid is claimed or both players pass consecutively.
   - The player holding the most tiles wins!

---

## ✨ Features

- ⚔️ **Local 2-Player Pass & Play**: Optimized for shared screen strategy on phones and tablets.
- 🎨 **Dynamic Board Themes & Visual Styles**: Support for **Classic** and **Midnight** board themes alongside Light, Dark, and System appearance modes.
- 👁️ **Accessibility First**: Includes unique geometric symbols (**dot**, **line**, **diamond**, **cross**, **ring**) embedded within each color tile to ensure colorblind accessibility.
- 🔊 **Zero-Asset Procedural Audio**: Generates synthesized, pleasant sound effects programmatically at runtime via a custom `SoundEngine` and `SoundPool`—no external `.mp3` or `.wav` binary assets bloat the project repository.
- 📊 **Statistics & Persistence**: Tracks games played, wins, draws, largest territory captured, and best winning margin using Android **DataStore Preferences**.
- 🎬 **BFS Expansion Animations**: Animated, layer-by-layer expansion feedback with full **Undo** capability during gameplay.
- 💡 **Expansion Previews**: Optional visual hints showing potential tile gains (`+N`) for available colors.

---

## 📱 App Screenshots & Structure

```
ColorTerritory/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/colorterritory/
│   │   │   │   ├── audio/              # SoundEngine WAV renderer & SoundPool manager
│   │   │   │   ├── data/               # SettingsRepository & DataStore preferences
│   │   │   │   ├── game/               # Pure immutable TerritoryEngine rule logic & models
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/     # Board canvas, tile drawing, & control widgets
│   │   │   │   │   ├── navigation/     # AppNavHost Compose Navigation setup
│   │   │   │   │   ├── screens/        # Splash, Home, Game, HowToPlay, Settings, About
│   │   │   │   │   └── theme/          # Color palettes, typography & Material 3 theme
│   │   │   │   └── MainActivity.kt     # App entry point with edge-to-edge support
│   │   │   └── AndroidManifest.xml
│   │   └── test/                       # Unit tests for TerritoryEngine
```

---

## 🛠️ Architecture & Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/) (Coroutines, StateFlow, SharedFlow)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Design
- **Architecture Pattern**: MVVM + Clean Architecture with a pure, UI-independent game domain engine (`TerritoryEngine`)
- **Navigation**: Navigation Compose (`androidx.navigation.compose`)
- **Persistence**: Jetpack DataStore Preferences (`androidx.datastore.preferences`)
- **Audio Engine**: Custom procedural WAV synthesizer (`SoundEngine`)
- **Build System**: Gradle Version Catalogs (`libs.versions.toml`) with Android Gradle Plugin 9.4+

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK**: Java 11 or higher
- **Android SDK**: API Level 37 (Min SDK: 24)

### Installation & Build

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/Color-Territory.git
   cd Color-Territory
   ```

2. **Build debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
