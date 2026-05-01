# AutoClicker

AutoClicker is a minimal Android auto clicker app written in Kotlin. It uses Android AccessibilityService gesture dispatch to repeat taps at a user-selected screen position, with an overlay control for positioning and start/stop actions.

## Current App Features

- Configure click frequency in milliseconds.
- Save a single target click position.
- Show a floating overlay control after overlay permission is granted.
- Drag the floating `+` marker to choose the click position.
- Start repeated tapping from the main screen or floating control panel.
- Stop repeated tapping from the main screen or floating control panel.
- Persist the last click position and frequency across app launches.
- Provide shortcuts from the main screen to Android overlay permission and accessibility settings.

The app does not bypass Android security restrictions. The user must manually grant overlay permission and enable the accessibility service before automated tapping can run.

## Technology Stack

- **Language:** Kotlin
- **Platform:** Native Android
- **Build system:** Gradle Kotlin DSL with Android Gradle Plugin
- **Minimum SDK:** Android 8.0, API 26
- **Target SDK:** Android API 36
- **Core Android APIs:**
  - `AccessibilityService`
  - `GestureDescription`
  - `WindowManager`
  - `SharedPreferences`

This first version intentionally avoids extra UI frameworks and third-party runtime dependencies. The UI is built with standard Android views to keep the first runnable version small and easy to debug.

## Project Structure

```text
AutoClicker/
├─ app/
│  ├─ build.gradle.kts
│  └─ src/main/
│     ├─ AndroidManifest.xml
│     ├─ java/com/example/autoclicker/
│     │  ├─ AutoClickAccessibilityService.kt
│     │  ├─ ClickController.kt
│     │  ├─ FloatingControlManager.kt
│     │  ├─ MainActivity.kt
│     │  ├─ PermissionState.kt
│     │  └─ SettingsStore.kt
│     └─ res/
│        ├─ drawable/
│        ├─ values/
│        └─ xml/accessibility_service_config.xml
├─ gradle/wrapper/
├─ build.gradle.kts
├─ settings.gradle.kts
└─ gradlew.bat
```

## File Responsibilities

### Gradle and Project Files

- `settings.gradle.kts`
  - Defines the Gradle plugin repositories and includes the `:app` module.

- `build.gradle.kts`
  - Declares project-level Android and Kotlin Gradle plugins.

- `gradle.properties`
  - Contains Gradle JVM and Kotlin style settings.

- `app/build.gradle.kts`
  - Configures the Android app module.
  - Sets package namespace, SDK versions, app version, Java compatibility, and Kotlin JVM toolchain.

- `gradlew` and `gradlew.bat`
  - Gradle Wrapper launch scripts for Unix-like systems and Windows.

- `gradle/wrapper/gradle-wrapper.properties`
  - Configures the Gradle distribution used by the wrapper.

### Android Manifest and Resources

- `app/src/main/AndroidManifest.xml`
  - Declares the main activity.
  - Declares `SYSTEM_ALERT_WINDOW` for overlay controls.
  - Declares the accessibility service with `BIND_ACCESSIBILITY_SERVICE`.
  - Points Android to the accessibility service metadata file.

- `app/src/main/res/xml/accessibility_service_config.xml`
  - Defines the accessibility service configuration.
  - Enables gesture dispatch with `android:canPerformGestures="true"`.
  - Disables window content retrieval because this app only needs tap gestures.

- `app/src/main/res/values/strings.xml`
  - Contains app name and accessibility service description.

- `app/src/main/res/values/styles.xml`
  - Defines the basic app theme.

- `app/src/main/res/values/colors.xml`
  - Defines the app accent color.

- `app/src/main/res/drawable/ic_launcher_foreground.xml`
  - Simple vector icon used by the app.

### Kotlin Source Files

- `MainActivity.kt`
  - Builds the main app screen with standard Android views.
  - Shows current permission status.
  - Lets the user set the click interval.
  - Opens overlay permission settings.
  - Opens accessibility settings.
  - Sends start, stop, and show-controls commands to the accessibility service.

- `AutoClickAccessibilityService.kt`
  - Android accessibility service entry point.
  - Owns the click controller and floating control manager.
  - Registers a local command receiver for start, stop, show, and hide actions.
  - Starts the floating controls when the service is connected and overlay permission exists.

- `ClickController.kt`
  - Runs the repeated click loop on the main thread.
  - Loads the latest saved position and interval before each tap.
  - Uses `GestureDescription` and `dispatchGesture(...)` to perform each tap.

- `FloatingControlManager.kt`
  - Creates and manages overlay windows through `WindowManager`.
  - Shows a draggable `+` marker for the click point.
  - Shows a compact floating panel with status, Start, and Stop buttons.
  - Persists marker movement as the selected click position.

- `SettingsStore.kt`
  - Stores and loads click settings with `SharedPreferences`.
  - Persists:
    - target `x`
    - target `y`
    - click interval in milliseconds

- `PermissionState.kt`
  - Checks whether overlay permission is granted.
  - Checks whether this app's accessibility service is enabled.

## Runtime Architecture

```text
MainActivity
    │
    ├─ saves interval through SettingsStore
    ├─ opens system permission screens
    └─ sends broadcast commands
          │
          ▼
AutoClickAccessibilityService
    │
    ├─ FloatingControlManager
    │     ├─ draggable click marker
    │     └─ floating Start / Stop panel
    │
    └─ ClickController
          └─ dispatchGesture tap loop
```

The accessibility service is the component that can execute taps. The activity is mainly a configuration and permission entry point. The overlay is useful after the user leaves the app because it remains visible over other apps and provides direct start/stop controls.

## Permission Model

The app needs two user-controlled Android permissions/settings:

- **Overlay permission**
  - Required for the floating marker and control panel.
  - Granted through Android's "Display over other apps" settings screen.

- **Accessibility service**
  - Required for synthetic tap gestures.
  - Enabled through Android accessibility settings.
  - Detected through both Android's secure enabled-service list and `AccessibilityManager`, because different Android versions and vendor ROMs can expose the enabled service identity in slightly different formats.

Android does not allow normal apps to silently grant these permissions. The current implementation exposes buttons that take the user to the required system screens.

## Build

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

The repository ignores local build output and `local.properties`. If needed, create `local.properties` locally with the Android SDK path:

```properties
sdk.dir=D\:\\Android\\Sdk
```

## Install and Test

### 1. Build the debug APK

On Windows, run:

```powershell
cd E:\AutoClicker
.\gradlew.bat assembleDebug
```

The generated debug APK is:

```text
E:\AutoClicker\app\build\outputs\apk\debug\app-debug.apk
```

### 2. Install the APK

If Android Debug Bridge is available:

```powershell
adb install -r E:\AutoClicker\app\build\outputs\apk\debug\app-debug.apk
```

If ADB is not available, copy `app-debug.apk` to the phone and install it manually from the file manager. The phone may ask you to allow installing apps from the current file source.

### 3. Grant required permissions

Open the installed app, then:

1. Tap **Grant overlay permission**.
2. Allow Auto Clicker to display over other apps.
3. Return to Auto Clicker.
4. Tap **Open accessibility settings**.
5. Enable the **Auto Clicker** accessibility service.
6. Return to Auto Clicker.

Both permissions are required. Overlay permission shows the floating controls. Accessibility permission performs the actual tap gestures.

After returning to Auto Clicker, the status text should show:

```text
Overlay: granted
Accessibility: enabled
```

If Android shows the service as enabled but the app still reports it as disabled, make sure the latest APK is installed, then force stop and reopen Auto Clicker. The app checks accessibility state again whenever the main screen resumes.

### 4. Run a safe click test

Use a harmless test target first, such as Calculator, Notes, an empty text field, or a blank browser page.

1. Enter a click interval, for example `500` for one tap every 500 ms.
2. Tap **Save frequency**.
3. Tap **Show floating controls**.
4. Drag the floating `+` marker to the target position.
5. Tap **Start** in the floating panel.
6. Confirm that the selected position is tapped repeatedly.
7. Tap **Stop** in the floating panel to end clicking.

Avoid testing first on payment, delete, messaging, trading, or other irreversible screens.

## Basic Usage Summary

- Use the main screen to save the click interval and open Android permission pages.
- Use the floating `+` marker to choose the screen coordinate.
- Use either the main screen or floating panel to start clicking.
- Use either the main screen or floating panel to stop clicking.

## Troubleshooting

- **Start says accessibility is not enabled**
  - Confirm that the installed APK is the latest build from this repository.
  - Open Android accessibility settings and verify that **Auto Clicker** is enabled.
  - Return to Auto Clicker so the main screen can refresh the permission state.
  - If the status still shows disabled, force stop Auto Clicker, reopen it, and check again.

- **Floating controls do not appear**
  - Confirm that overlay permission is granted.
  - Tap **Show floating controls** after enabling the accessibility service.
  - Some phones hide overlays while permission dialogs or system settings pages are open; return to a normal app screen before testing.

- **Clicking starts but does not hit the expected target**
  - Drag the floating `+` marker again. The click coordinate is the center of that marker.
  - Use a larger interval such as `500` or `1000` ms during testing so it is easier to stop.

## Current Limitations

- Supports one click point only.
- Supports tap gestures only, not swipes or long presses.
- Does not yet expose advanced profiles, multiple target sequences, or foreground notification controls.
- Does not yet include automated UI tests.
