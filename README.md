# SplitEasy

Monorepo for the SplitEasy Android app and FastAPI backend.

## Run Backend
```bash
cd backend
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

## Run Android
Open the `android` folder in Android Studio and run the app from there.

If you are using a physical phone over USB debugging, edit the debug API URL in `android/app/build.gradle.kts` under `buildTypes > debug > buildConfigField("String", "BASE_URL", ...)` and set it to your computer's LAN IP address, for example `http://192.168.1.50:8000`.

If you are using the Android emulator, leave the debug URL as `http://10.0.2.2:8000`.
