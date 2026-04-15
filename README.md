<div align="center">
  <img src="app/src/main/res/drawable/app_logo.png" alt="AI Music Pro Logo" width="150"/>

  # AI Music Pro 🎧

  **A modern, premium Android music streaming application built for true audiophiles.** <br/>
  Featuring real-time synchronized listening rooms, a stunning Spotify-inspired dark aesthetic, and a robust offline-first architecture. 

  [![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android)](https://www.android.com/)
  [![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=flat&logo=kotlin)](https://kotlinlang.org/)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=flat&logo=android)](https://developer.android.com/jetpack/compose)

</div>

---

## ✨ Features

- **🎨 Premium UI & Dark Theme**: Hand-crafted, modern Jetpack Compose interfaces featuring fluid animations and a stunning high-contrast Black & Green signature theme.
- **🎧 High-Quality Music Streaming**: Engineered to fetch and stream dynamic audio via ExoPlayer, supporting extensive libraries and genres.
- **🤝 Sync & Listen (Live Rooms)**: Create or join virtual rooms. Stream music simultaneously with your friends using robust WebSockets (Socket.io). 
- **❤️ Seamless Library Management**: Star your favorite tracks and automatically save them locally. View your `Liked Songs` directly from a persistent offline database.
- **🔍 Quick Search & History**: Easily discover tracks. Explore the extensive library while automatically tracking recent search history.
- **📱 Responsive Layouts**: Immersive Player Screen, dynamic library tabming, and intuitive carousel banners.

## 🛠️ Tech Stack & Architecture

This application focuses on clean, scalable architecture following the **MVVM (Model-View-ViewModel)** design pattern.

* **UI Toolkit**: Jetpack Compose & Material Design 3
* **Language**: Kotlin Flow & Coroutines for asynchronous processing
* **Dependency Injection**: Dagger Hilt
* **Media Playback**: Media3 / ExoPlayer
* **Network & API**: Retrofit for REST APIs, Coil for seamless image loading
* **Real-time Sync**: Socket.io for live listener synchronization
* **Data Persistence**: Room Database for offline capabilities (Liked logic, Libraries, Search memory)

## 📌 Application Structure

* **`HomeScreen`**: Browse Top Picks, Trending, and Quick Access tabs. Join listening sessions right from the app bar.
* **`PlayerScreen`**: A highly interactive music controller holding advanced queue actions, seekbars, and artwork imagery.
* **`RoomScreen`**: Allows hosts to invite participants, queue tracks together, mutually pause/play music, and manage the live session.
* **`LibraryScreen`**: Categorized views covering local albums, saved queues, and recently liked records.
* **`ProfileScreen`**: Account settings, avatars, and configuration.

## 🚀 Getting Started

Since this is an actively maintained Android application, pulling the source code into Android Studio allows immediate compilation.

1. Clone or download the repository.
2. Open the project inside **Android Studio** (Flamingo or newer recommended).
3. Ensure JDK 17+ and the latest Android build tools are installed.
4. Let Gradle sync project dependencies.
5. Hit the **Run** button or execute `./gradlew build` in your terminal.
6. Make sure to have an un-blocked internet connection for API population and Live Room socket linking.

*(Ensure the accompanying Node.js backend is running if setting up localized test environments!)*

---

<div align="center">
  <sub>Built with ❤️ utilizing the latest Android Ecosystem practices.</sub>
</div>
