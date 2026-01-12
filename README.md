# MusicZone 🎵

A feature-rich Android local music player application that supports music scanning, playback, favorites, search, and user management.

## 📱 App Overview

MusicZone is a local music player developed with Kotlin, providing users with a smooth music playback experience. The app uses SQLite database for local data storage, supporting user registration and login, music file scanning, playback control, favorites management, multi-dimensional search, and more.

## ✨ Key Features

### 🔐 User System
- **User Registration**: Support username and password registration with automatic username uniqueness checking
- **User Login**: Secure login verification mechanism
- **User State Management**: Persistent user login state using SharedPreferences

### 🎶 Music Playback
- **Auto Scanning**: Scan music files on the device (supports common audio formats)
- **Playback Controls**:
  - Play/Pause
  - Previous/Next track
  - Progress bar drag to seek
  - Auto-play next track
- **Playlist**: Display all scanned music files
- **Real-time Progress**: Show current playback time, total duration, and remaining time

### ⭐ Favorites
- **Add to Favorites**: One-tap to favorite songs
- **Favorites List**: View all favorited songs
- **Remove from Favorites**: Remove favorites anytime

### 🔍 Search
- **Real-time Search**: Instant search results as you type
- **Multi-dimensional Search**:
  - Song search (title, artist, album)
  - Artist search
  - Album search
- **Category Filtering**: Filter search results by type
- **Smart Matching**: Supports fuzzy search, case-insensitive

### 💬 Feedback
- Submit user feedback and suggestions
- Persistent feedback data storage

## 🛠️ Tech Stack

### Development Language
- **Kotlin** - Primary development language

### Core Framework
- **Android SDK** - Android application development framework
- **AndroidX** - Android extension libraries
  - AppCompat
  - RecyclerView
  - ConstraintLayout
  - Activity KTX

### Database
- **SQLite** - Local database storage
  - Songs table
  - Users table
  - Favorites table
  - Feedback table

### Async Processing
- **Kotlin Coroutines** - Coroutines for async tasks

### Third-party Libraries
- **Glide** - Image loading and display
- **Timber** - Logging
- **Firebase** - Authentication and cloud storage (integrated)
  - Firebase Authentication
  - Firebase Firestore

### UI Design
- **Material Design** - Material Design component library

## 📋 System Requirements

- **Minimum Android Version**: Android 11 (API 30)
- **Target Android Version**: Android 14 (API 36)
- **Compile SDK Version**: 36
- **Java Version**: Java 11

## 🔑 Permissions

The app requires the following permissions:

- `READ_EXTERNAL_STORAGE` - Read external storage (Android 12 and below)
- `READ_MEDIA_AUDIO` - Read audio files (Android 13 and above)

These permissions are used to scan music files on the device.

## 📦 Project Structure

```
MusicZone/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/org/wit/musiczone/
│   │   │   │   ├── activities/          # Activity classes
│   │   │   │   │   ├── LoginActivity.kt
│   │   │   │   │   ├── MusicPlayerActivity.kt
│   │   │   │   │   ├── SearchActivity.kt
│   │   │   │   │   ├── MusicRecommendationActivity.kt
│   │   │   │   │   ├── FeedbackActivity.kt
│   │   │   │   │   └── ...
│   │   │   │   ├── database/            # Database related
│   │   │   │   │   └── MusicDBHelper.kt
│   │   │   │   ├── models/              # Data models
│   │   │   │   ├── adapters/            # RecyclerView adapters
│   │   │   │   └── utils/               # Utility classes
│   │   │   ├── res/                     # Resource files
│   │   │   └── AndroidManifest.xml
│   │   └── test/                        # Test code
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 🗄️ Database Design

### Table Structure

#### songs table (Music Information)
- `_id`: Primary key, auto-increment ID
- `title`: Song title
- `artist`: Artist name
- `album`: Album name
- `duration`: Duration (milliseconds)
- `file_path`: File path (unique)
- `file_size`: File size
- `date_added`: Date added
- `album_art_path`: Album art path

#### users table (User Information)
- `_id`: Primary key, user ID
- `username`: Username (unique)
- `password`: Password
- `email`: Email address
- `created_at`: Creation time

#### favorites table (Favorites)
- `user_id`: User ID (foreign key)
- `song_id`: Song ID (foreign key)
- `date_added`: Date added
- Composite primary key: `(user_id, song_id)`

#### feedback table (Feedback)
- `_id`: Primary key
- `content`: Feedback content
- `date_added`: Date added

## 🚀 Build and Run

### Prerequisites
- Android Studio Hedgehog or higher
- JDK 11 or higher
- Android SDK 36

### Build Steps

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd MusicZone
   ```

2. **Open the project**
   - Open the project directory in Android Studio

3. **Sync Gradle**
   - Android Studio will automatically sync Gradle dependencies
   - If not synced automatically, click `File > Sync Project with Gradle Files`

4. **Configure Firebase (Optional)**
   - If you need to use Firebase features, configure `google-services.json`
   - Place the Firebase configuration file in the `app/` directory

5. **Run the app**
   - Connect an Android device or start an emulator (API 30+)
   - Click the `Run` button or use the shortcut `Shift + F10`

### Build Release Version

```bash
./gradlew assembleRelease
```

The generated APK file will be located at: `app/build/outputs/apk/release/`

## 📱 Usage Guide

### First Time Use

1. **Register an account**
   - Open the app and click the "Register" button
   - Enter username and password (password must be at least 3 characters)
   - Complete registration

2. **Login**
   - Enter your registered username and password
   - Click the "Login" button

3. **Scan music**
   - After login, you'll enter the music player interface
   - The app will automatically scan music files on the device
   - The first scan may take some time

### Main Operations

- **Play Music**: Click on a song in the playlist to play
- **Favorite Songs**: Click the favorite button next to a song item
- **Search Music**: Use the search function to quickly find desired songs
- **View Favorites**: Check all favorited songs in the recommendation interface
- **Submit Feedback**: Submit opinions and suggestions through the feedback feature

## 🎯 Core Features

### 1. Smart Music Scanning
- Automatically identify audio files on the device
- Deduplication to avoid duplicate display
- Caching mechanism to improve loading speed

### 2. Smooth Playback Experience
- Asynchronous player preparation to avoid UI blocking
- Real-time progress updates
- Auto-play next track

### 3. Personalized Favorites
- User-based favorites system
- Persistent favorites data storage
- Quick access to favorites list

### 4. Powerful Search
- Real-time search feedback
- Debounce processing for performance optimization
- Multi-dimensional search support

## 🔧 Development Guide

### Database Operations

All database operations are performed through the `MusicDBHelper` class:

```kotlin
val dbHelper = MusicDBHelper(context)

// Get all songs
val songs = dbHelper.getAllSongs()

// Search songs
val results = dbHelper.searchSongs("keyword")

// Add to favorites
dbHelper.addToFavorite(songId)

// Get favorites list
val favorites = dbHelper.getAllFavorites()
```

### Music Scanning

Use the `MusicScanner` class to scan music files:

```kotlin
val scanner = MusicScanner(context)
val songs = scanner.scanMusicFiles()
```

### Playback Control

Use Android `MediaPlayer` API for audio playback:

```kotlin
val mediaPlayer = MediaPlayer()
mediaPlayer.setDataSource(filePath)
mediaPlayer.prepareAsync()
mediaPlayer.start()
```

## 📝 Changelog

### Version 2.0 (Current)
- ✅ User registration and login
- ✅ Music file scanning and playback
- ✅ Favorites functionality
- ✅ Search functionality
- ✅ Feedback functionality
- ✅ Database version management

## 🤝 Contributing

Contributions are welcome! Please feel free to submit Issues and Pull Requests.

## 👨‍💻 Authors

MusicZone Development Team

## 📞 Contact

For questions or suggestions, please contact us through:
- Submit an Issue
- Send feedback (in-app feedback feature)

---

**Enjoy Music, Enjoy Life!** 🎵
