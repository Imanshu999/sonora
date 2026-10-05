# 🎵 Sonora

<div align="center">

<img src="https://raw.githubusercontent.com/Imanshu999/sonora/main/docs/app-icon.svg" width="128" alt="Sonora app icon"/>

# Sonora

**A modern native Android music player built for discovery, playback, playlists and offline listening.**

![Version](https://img.shields.io/badge/version-2.0-a78bfa?style=for-the-badge)
![Android](https://img.shields.io/badge/Android-API%2024%2B-3DDC84?style=for-the-badge&logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-Native-7F52FF?style=for-the-badge&logo=kotlin)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge)

</div>

---

## 📱 About Sonora

**Sonora** is a native Android music application focused on a clean, immersive listening experience.

It combines a modern **Jetpack Compose + Material 3** interface with **AndroidX Media3 / ExoPlayer**, local library storage, provider-based music discovery, playlists, downloads, lyrics, smart mixes and background playback.

| Detail | Value |
|---|---|
| App | **Sonora** |
| Version | **2.0** |
| Platform | Android |
| Minimum Android | API 24 |
| Compile / Target SDK | 36 |
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Playback | AndroidX Media3 / ExoPlayer |
| Background Audio | MediaSessionService |

> Sonora is a music player and catalog client. It does not bundle a complete commercial music catalog. Music availability depends on the configured provider and applicable rights.

---

## 📸 App Screens

<table>
<tr>
<td><img src="https://raw.githubusercontent.com/Imanshu999/sonora/main/docs/screenshots/home.svg" width="260" alt="Sonora Home"/></td>
<td><img src="https://raw.githubusercontent.com/Imanshu999/sonora/main/docs/screenshots/player.svg" width="260" alt="Sonora Player"/></td>
<td><img src="https://raw.githubusercontent.com/Imanshu999/sonora/main/docs/screenshots/search.svg" width="260" alt="Sonora Search"/></td>
</tr>
<tr>
<td align="center"><b>Home</b></td>
<td align="center"><b>Now Playing</b></td>
<td align="center"><b>Search</b></td>
</tr>
<tr>
<td><img src="https://raw.githubusercontent.com/Imanshu999/sonora/main/docs/screenshots/library.svg" width="260" alt="Sonora Library"/></td>
<td><img src="https://raw.githubusercontent.com/Imanshu999/sonora/main/docs/screenshots/settings.svg" width="260" alt="Sonora Settings"/></td>
<td><img src="https://raw.githubusercontent.com/Imanshu999/sonora/main/docs/screenshots/queue.svg" width="260" alt="Sonora Queue"/></td>
</tr>
<tr>
<td align="center"><b>Library</b></td>
<td align="center"><b>Settings</b></td>
<td align="center"><b>Queue</b></td>
</tr>
</table>

> These are repository UI preview assets used for project documentation.

---

## ✨ Features

### 🎧 Music Playback
- Play / pause
- Seek
- Next / previous
- Queue management
- Queue reordering
- Shuffle
- Repeat
- Crossfade
- Sleep timer
- Playback progress
- Current track metadata
- Album artwork
- Background playback
- Android MediaSession integration

### 🔎 Search & Discovery
- Multi-source music discovery
- Provider-based search
- Recent searches
- Genre discovery
- Smart Mix
- Dedicated **🔥 Phonk** category
- Source filtering
- Artist / album / track discovery

### 📚 Personal Library
- Liked songs
- Listening history
- Playlists
- Downloaded tracks
- Offline library
- Queue management
- Local track status
- Playlist management

### ⚙️ Settings & Personalization
- Dark-first Material 3 interface
- Theme mode
- Dynamic colors
- Streaming quality controls
- Crossfade settings
- Equalizer presets
- Loudness normalization
- Language / regional focus
- Audio cache
- Artwork cache
- Bluetooth audio support

---

## 🎵 Background Playback

Sonora uses a real AndroidX Media3:

`MediaSessionService`

This allows music playback to continue while the application is backgrounded and lets Android expose the active media session through its system media controls.

The active `MediaItem` contains:

- Track title
- Artist
- Album
- Artwork
- Playback state

This is implemented as native Android media playback rather than a web-based audio workaround.

---

## 🌐 Music Sources

Sonora is designed around provider catalogs.

Current project integrations/configuration include:

- **YouTube**
- **Audius**
- **Jamendo**
- **Free To Use**

### Free To Use

The project contains a native Retrofit integration for the public Free To Use API:

`https://api.freetouse.com/v3/`

Example endpoints:

`/music/tracks/all`  
`/music/tracks/search`  
`/music/tracks/{id}`

Provider availability, API behavior and licensing terms can change. Always verify the current provider terms before distribution or monetization.

**Sonora does not claim to provide every commercial song in the world.**

---

## 🧩 Technology Stack

| Category | Technology |
|---|---|
| Programming | **Kotlin** |
| UI | **Jetpack Compose** |
| Design | **Material 3** |
| Playback | **AndroidX Media3 / ExoPlayer** |
| Background audio | **MediaSessionService** |
| Database | **Room** |
| Preferences | **DataStore** |
| Networking | **Retrofit + OkHttp** |
| Serialization | Kotlin Serialization / Moshi |
| Images | **Coil** |
| AI | Firebase AI SDK integration |
| Build | Gradle + Android Gradle Plugin |
| CI | GitHub Actions |
| Java | JDK 17 |

---

## 🏗️ Architecture

Sonora follows a modern Android architecture based around:

```text
Compose UI
   ↓
ViewModel / State
   ↓
Repository / Domain logic
   ↓
Room + DataStore + Network APIs
   ↓
Media3 / ExoPlayer
   ↓
MediaSessionService
   ↓
Android system playback
```

The application is designed so playback, local data, network sources and UI state remain separated instead of putting the entire application inside one screen.

---

## 🛠️ Build Locally

### Requirements

- Android Studio
- JDK 17
- Android SDK 36
- Android device or emulator
- Environment values required by selected integrations

### Steps

```bash
git clone https://github.com/Imanshu999/sonora.git
cd sonora
```

Open the project in **Android Studio**, allow Gradle synchronization to finish, configure any required environment values, then build the `app` module.

### GitHub Actions

The repository contains a CI workflow under:

`.github/workflows/build.yml`

The workflow builds the Android application and publishes build artifacts such as the debug APK and source ZIP.

---

## 📂 Project Structure

```text
sonora/
├── app/
│   └── src/main/
│       ├── java/                 # Kotlin application source
│       └── res/                  # Android resources
│
├── docs/
│   ├── app-icon.svg              # README app icon
│   └── screenshots/              # Six UI previews
│
├── .github/workflows/
│   └── build.yml                 # Android CI / APK build
│
├── CREDITS.md
├── LICENSE
├── SONORA_UPGRADES.md
├── metadata.json
└── README.md
```

---

## 🔐 Android Permissions

Depending on Android version and enabled features, Sonora uses permissions for:

- Internet access
- Network state
- Foreground media playback
- Notifications
- Bluetooth audio
- Vibration
- Wake lock

Permissions are used for the corresponding Android functionality and do not grant rights to third-party music catalogs.

---

## 🧪 Project Status

**Sonora 2.0** is an actively evolving Android music project.

The current codebase includes:

- Native Media3 background playback
- Multi-source discovery
- Search
- Playlists
- Favorites
- Listening history
- Downloads / offline support
- Lyrics
- Smart Mix
- Theme and audio settings
- Bluetooth-related support
- Dedicated Phonk discovery
- GitHub Actions APK builds

---

## 📜 Important Licensing Note

Sonora is software for interacting with supported music providers.

It does **not** include or redistribute a complete commercial music catalog.

Users and distributors are responsible for complying with:

- Provider API terms
- Music licensing requirements
- Copyright law
- Regional availability restrictions

Do not use Sonora to download, stream or redistribute content when you do not have the required rights.

---

## 📄 License & Credits

See [LICENSE](LICENSE) for project licensing information.

See [CREDITS.md](CREDITS.md) for third-party libraries, APIs, providers and attribution.

---

<div align="center">

### 🎵 Sonora 2.0

**Music, without the noise.**

</div>
