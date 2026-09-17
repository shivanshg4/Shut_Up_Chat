# Build Variants and Environment Configuration

This document outlines the build variant strategy and environment configuration for the Shut Up Chat application.

## Available Variants

| Variant | Environment | Build Type | Purpose |
| :--- | :--- | :--- | :--- |
| `devDebug` | Development | Debug | Local development with logging and debugging tools. |
| `devRelease` | Development | Release | Pre-production testing with minification enabled. |
| `prodDebug` | Production | Debug | Debugging production-pointing builds. |
| `prodRelease` | Production | Release | **Production Build** for Google Play Store. |

## Environment Configuration

Configuration is managed via `local.properties` (for local secrets) and `build.gradle.kts` (for environment-specific constants).

### Local Configuration (`local.properties`)
The following values must be defined in your `local.properties` file:

```properties
MAPS_API_KEY=AIzaSy...
Routes_API_KEY=AIzaSy...
AGORA_APP_ID=...
```

### BuildConfig Fields
These values are available at runtime via `BuildConfig`:

- `FIREBASE_DATABASE_URL`: Points to the environment-specific Firebase Realtime Database.
- `AGORA_APP_ID`: Externalized App ID for voice/video calls.
- `MAPS_API_KEY` / `Routes_API_KEY`: API keys for Google Maps services.

## Security

1. **Secrets:** Never hardcode API keys or secrets in Kotlin/Compose code.
2. **Minification:** R8 minification and resource shrinking are enabled for all `release` builds.
3. **Logging:** Use `AppLogger` instead of `android.util.Log` or `println`. `AppLogger.d` and `AppLogger.e` (with stack trace) are automatically disabled in release builds.

## Release Process

When building for production:
1. Ensure `local.properties` has the production API keys.
2. Select the `prodRelease` build variant.
3. Run `./gradlew assembleProdRelease`.
