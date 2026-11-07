# GraceConnect

An Android app for church members: sermons, events, giving, community groups, a Bible reader and a personal profile, with email and password sign-in.

## Download

**[Download GraceConnect.apk](https://github.com/JhunDevRey/GraceConnect/releases/latest/download/GraceConnect.apk)** (always the latest version)

All versions are listed on the [Releases page](https://github.com/JhunDevRey/GraceConnect/releases).

## Installing on your phone

1. Open the download link on your Android phone. If Chrome warns about the file type, tap **Download anyway**.
2. Open the downloaded `GraceConnect.apk`.
3. When asked, allow installs from your browser or Files app.
4. If Play Protect says the app is unrecognized, tap **Install anyway**. This is normal for apps installed outside the Play Store.

To update, download and install the latest APK the same way. Your account stays signed in.

## How sign-in works (Firebase Authentication)

GraceConnect uses Firebase Authentication with email and password only. Firebase checks the details and stores the passwords. The app only sends what the user types and reacts to the answer.

### Linking the app to Firebase

- [`app/google-services.json`](app/google-services.json) tells the app which Firebase project to use (`graceconnect-a1737`). Download it from the Firebase console.
- [`app/build.gradle.kts`](app/build.gradle.kts) applies the `google-services` plugin when that file exists and adds the `firebase-auth` library. Firebase then starts automatically when the app launches.
- If the file is missing, the app still builds. Login and register then show "Firebase is not set up" instead of crashing.

### The `Auth` helper

[`Auth.kt`](app/src/main/java/com/example/graceconnect/Auth.kt):

- gets the `FirebaseAuth` object, or `null` if Firebase isn't set up
- checks who is signed in (Firebase remembers the login on the device, so users stay logged in after closing the app)
- handles logout and turns Firebase errors into plain messages

### Where each screen uses Firebase

| Screen | Firebase call | What it does |
|---|---|---|
| `LoginActivity` | `signInWithEmailAndPassword` | Logs in; skips straight to Home if already signed in |
| `LoginActivity` | `sendPasswordResetEmail` | "Forgot password?" emails a reset link |
| `RegisterActivity` | `createUserWithEmailAndPassword` + `updateProfile` | Creates the account and saves the user's name |
| `ProfileActivity` | `signOut` (through `Auth.logout`) | Logs out and returns to Login |

After login, the app saves the user's ID, name and email on the phone, then opens Home.

**Flow:** app opens → Login → Firebase checks the email and password → Home → Profile → Logout → back to Login.

### Firebase console setup

Under **Authentication → Sign-in method**, turn on **Email/Password**. Without it, sign-in fails and the app shows Firebase's own error message. To change the password reset email, go to **Authentication → Templates → Password reset**.
