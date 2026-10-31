# My Car Maintenance Log

A simple Android app to keep your car's service history in one place.

Every time you get an oil change, tuning, or any other work done, you add it here with the date, km reading, mechanic, cost, and details. Next time, you don't have to guess when it was done or who did it.

No login. No internet. No ads. Everything stays on your phone.

## Why I built it

I drive a 2008 Suzuki Cultus EFI. I kept forgetting when the last oil change or tuning was done, at how many km, and which mechanic did it.

I didn't want a big app with accounts and reminders. I just wanted a simple notebook for my car. So I built one.

I'm a Python developer, not a mobile developer. I built this in Kotlin with the help of Claude (AI) and Google's new Android CLI.

## Features

- Add more than one car (name, model year, variant, plate number)
- Add work entries under each car: work type, date, km reading, mechanic, cost, and details
- Quick buttons for common work: oil change, tuning, oil filter, air filter, brakes, tyres
- Newest work shows on top
- Search by work type, mechanic, or details
- Sort by newest, oldest, highest km, or work type
- Filter by date range
- Edit or delete any car or work entry
- Deleting a car also deletes all of its work history (with a confirm popup first)
- Works fully offline, all data saved on the phone

## Screenshots

_Add screenshots here._

## Tech stack

- **Kotlin**
- **Jetpack Compose** with Material 3 for the screens
- **Room** for the local database (built on SQLite)
- **Navigation 3** for moving between screens
- **Android CLI** to create, build, and test the project

## How the data is stored

Two tables in a local Room database:

- **cars**: id, name, model year, variant, plate number
- **work_entries**: id, car id, work type, date, km, mechanic, cost, details

Each work entry belongs to one car. When a car is deleted, its work entries are deleted with it (cascade delete).

## Project structure

```
app/src/main/kotlin/com/example/carlog/
├── MainActivity.kt          # App entry point
├── Navigation.kt            # Screen navigation
├── NavigationKeys.kt        # Screen keys (car list, car detail, work form)
├── data/
│   ├── Entities.kt          # Car and WorkEntry tables
│   ├── Daos.kt              # Database queries
│   └── AppDatabase.kt       # Room database setup
└── ui/
    ├── cars/
    │   ├── CarListScreen.kt     # List of cars + add/edit car dialog
    │   └── CarDetailScreen.kt   # Work history with search, sort, date filter
    ├── work/
    │   └── WorkFormScreen.kt    # Add or edit a work entry
    ├── common/
    │   └── Common.kt            # Date and number helpers, delete dialog
    └── theme/                   # Light theme and colors
```

## Build and run

### With Android CLI

From the `app` folder:

```bash
android build
```

The APK will be at:

```
app/build/outputs/apk/debug/app-debug.apk
```

To run it on a phone connected with USB (USB debugging on):

```bash
android run
```

### With Android Studio

Open the project folder in Android Studio and press Run.

### Without a phone

Upload `app-debug.apk` to [Appetize.io](https://appetize.io) and use the app in your browser.

## Install on your phone

Send `app-debug.apk` to your phone (USB, Google Drive, or WhatsApp to yourself), tap it, and allow "Install unknown apps" when asked.

## Notes

- The app does not send reminders. It can't know your current km, so you check the last entry and do the math yourself. This keeps the app simple.
- There is no backup yet. If you uninstall the app or lose your phone, the data is gone. Backup is planned for later.

## Built with AI

This app was built step by step with Claude. I made the decisions about what the app should do and how it should work. Claude wrote the Kotlin code, and we used Android CLI to create, build, and check the project after every change.
