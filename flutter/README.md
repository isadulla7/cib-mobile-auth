# CIB Flutter auth client

If Android/iOS platform folders are absent, bootstrap once:

```bash
flutter create .
flutter pub get
```

Android emulator:

```bash
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080
```

iOS simulator:

```bash
flutter run --dart-define=API_BASE_URL=http://127.0.0.1:8080
```

Dependencies:

- http 1.6.0
- flutter_secure_storage 11.0.0

The app creates a random, app-scoped device identifier and stores it in secure storage.
The backend persists only an HMAC of that identifier.

## Platform requirement

`flutter_secure_storage` 11.x requires Dart 3.8+; for Android use minSdk 23 or higher.
