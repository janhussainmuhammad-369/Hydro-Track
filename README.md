# HydroTrack 💧 • Daily Water Intake & Smart Reminders

[![Platform: Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20(M3)-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase%20Firestore-FFCA28.svg?style=flat&logo=firebase)](https://firebase.google.com/)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-blue.svg)](https://developer.android.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)

**HydroTrack** is a modern, privacy-focused Android application designed to help users establish consistent daily hydration habits. Built with **100% Jetpack Compose (Material Design 3)**, **Kotlin Coroutines & Flow**, **Firebase Firestore** for cloud sync, and **Jetpack Credential Manager** for secure Google Sign-In.

---

## 📲 Quick Download

You can download and install the app directly from this repository:

- 📦 **[Download HydroTrack-apk.zip](release/HydroTrack-apk.zip)** — Zipped APK package (ready to extract and install).
- 📱 **[Direct APK File](release/HydroTrack.apk)** — Direct `.apk` file for sideloading on Android devices.

---

## 🌟 Key Features

### 📊 Real-Time Daily Tracker & Progress
- **Animated Circular Hydration Ring:** Dynamic gauge that visually fills as you drink throughout the day, showing percentage, current volume, and remaining goal.
- **Goal Completion Celebrations:** Instant visual milestone confirmation when your daily target is achieved.
- **Streak Tracking:** Automatic calculation of consecutive days the hydration goal was met to reinforce healthy habits.

### ⚡ Quick Drink Logging & Custom Beverage Types
- **One-Tap Presets:** Quick logging buttons for standard containers:
  - 150 ml (Small Cup)
  - 250 ml (Standard Glass)
  - 330 ml (Can / Mug)
  - 500 ml (Water Bottle)
  - 750 ml (Sports Flask)
- **Custom Drink Logger:** Support for diverse beverage categories (**Pure Water, Herbal Tea, Coffee, Fruit Juice, Smoothie, Electrolytes**) with custom milliliter sliders and container presets.
- **Detailed Intake Timeline:** Chronological list of logged drinks with timestamps, beverage badges, and one-tap deletion.

### ⏰ Smart Reminders & Notification System
- **Flexible Scheduled Intervals:** Customizable intervals (**30m, 45m, 1 hr, 1.5 hr, 2 hr, 3 hr**).
- **Active Hours Scheduling:** Configurable wake-up and bedtime hours so reminders never disturb your sleep.
- **In-App Notification Testing:** Immediate "Send Test Reminder Alert" button to preview alert tones, vibration, and banners.
- **Android 13+ Notification Permissions:** Graceful runtime permission handling (`POST_NOTIFICATIONS`).

### 📈 7-Day Analytics & Health Insights
- **Weekly Intake Bar Chart:** Compare your daily water volume against your target across the past 7 days.
- **Metric Cards:** Weekly total volume, daily average, target completion rate, and total drinks logged.
- **Beverage Breakdown:** Distribution of water vs. other beverages consumed.
- **Hydration Tips:** Daily health insights on proper hydration timing and body metabolism.

### 🎯 Personalized Goal Calculator & Cloud Sync
- **Body Weight Calculator:** Enter body weight in kilograms to automatically calculate recommended daily hydration ($35\text{ ml per kg}$).
- **Cloud Backup:** Powered by Firebase Firestore with secure per-user rules so your data is never lost across device changes.
- **Google Sign-In:** Frictionless authentication using Android's modern Jetpack `CredentialManager` API.

---

## 🏗️ Architecture & Tech Stack

The app follows **Modern Android Architecture (MVVM & Clean Architecture)**:

- **UI Framework:** Jetpack Compose with Material 3 (M3) dynamic color palette and edge-to-edge system insets.
- **Architecture Pattern:** Model-View-ViewModel (MVVM) with reactive `StateFlow` and `collectAsStateWithLifecycle()`.
- **Concurrency:** Kotlin Coroutines (`viewModelScope`, `flow`, `callbackFlow`).
- **Cloud Database:** Google Firebase Firestore with real-time snapshot listeners (`snapshots()` flow).
- **Authentication:** Jetpack Credential Manager (`GetSignInWithGoogleOption`) and Firebase Authentication.
- **Notifications:** Android `NotificationManagerCompat`, `NotificationChannel`, and `AlarmManager`.
- **Unit & Rules Testing:** Robolectric for local Android JVM integration tests and Node.js security rules test runner (`@firebase/rules-unit-testing`).

---

## 📂 Project Directory Structure

```text
HydroTrack/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt                  # Edge-to-edge host & auth gating
│   │   │   │   ├── auth/
│   │   │   │   │   └── AuthManager.kt               # Jetpack Credential Manager & Google Sign-In
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── UserProfile.kt           # User profile & reminder settings
│   │   │   │   │   │   └── WaterLog.kt              # Water drink entry model
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── WaterRepository.kt       # Firestore CRUD & Flow streams
│   │   │   │   │   └── util/
│   │   │   │   │       └── FirestoreErrorHandler.kt # Structured error logging
│   │   │   │   ├── reminders/
│   │   │   │   │   ├── WaterReminderManager.kt      # Alarms & notification channels
│   │   │   │   │   └── WaterReminderReceiver.kt     # BroadcastReceiver for scheduled alerts
│   │   │   │   └── ui/
│   │   │   │       ├── components/
│   │   │   │       │   ├── CustomLogDialog.kt       # Beverage logger dialog
│   │   │   │       │   ├── HydrationGauge.kt        # Animated circular progress ring
│   │   │   │       │   └── ProfileSettingsDialog.kt # Goal adjuster & weight calculator
│   │   │   │       ├── screens/
│   │   │   │       │   ├── AuthScreen.kt            # Google Sign-In onboarding
│   │   │   │       │   ├── DashboardScreen.kt       # Daily tracker & quick presets
│   │   │   │       │   ├── HistoryScreen.kt         # 7-day chart & analytics
│   │   │   │       │   ├── MainAppScreen.kt         # NavigationBar scaffold
│   │   │   │       │   ├── MainViewModel.kt         # StateFlow viewmodel
│   │   │   │       │   └── RemindersScreen.kt       # Interval & notification settings
│   │   │   │       └── theme/
│   │   │   │           ├── Color.kt                 # Aquatic theme palette
│   │   │   │           ├── Theme.kt                 # Material 3 theme configuration
│   │   │   │           └── Type.kt                  # Typography definitions
│   │   │   ├── res/                                 # Drawables, mipmaps & launcher icons
│   │   │   └── AndroidManifest.xml                  # Permissions & receiver declarations
│   │   └── test/                                    # Robolectric & repository unit tests
│   └── build.gradle.kts                             # App module build dependencies
├── firestore.rules                                  # Zero-trust Firestore security rules
├── firestore.test.js                                # Automated security rules test suite
├── firebase-blueprint.json                          # Database schema intermediate representation
├── build.gradle.kts                                 # Project level Gradle configuration
├── settings.gradle.kts                              # Project settings & repositories
└── README.md                                        # Documentation
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Ladybug / Koala or newer recommended)
- **JDK 17** or **JDK 21**
- **Android SDK:** `compileSdk 36`, `minSdk 24`

### 1. Clone the Repository
```bash
git clone https://github.com/YOUR_USERNAME/HydroTrack.git
cd HydroTrack
```

### 2. Open in Android Studio
1. Launch Android Studio.
2. Select **File > Open...** and choose the `HydroTrack` directory.
3. Allow Gradle to synchronize dependencies.

### 3. Build & Run
- Connect a physical Android device (with USB debugging enabled) or start an Android Virtual Device (AVD).
- Press the **Run** button (`Shift + F10`) or run from terminal:
  ```bash
  gradle assembleDebug
  ```

The generated APK will be located at:
```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔒 Security & Data Privacy

HydroTrack uses **zero-trust security rules** (`firestore.rules`) with Attribute-Based Access Control (ABAC):
- Users can **only** read, create, update, or delete their own data (`request.auth.uid == userId`).
- Incoming data is strictly schema-validated for types, volume ranges, allowed fields, and server timestamps (`request.time`).
- No anonymous or insecure fallback authentications are permitted in production code.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
