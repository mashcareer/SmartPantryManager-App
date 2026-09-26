# Smart Pantry Manager

Smart Pantry Manager is an offline Android application for reducing food waste by recording ingredients available at home and showing recipes that can be made using those ingredients.

## Description

The application follows a strict recipe rule: a recipe is shown only when every required ingredient is present in the pantry in at least the required quantity and with a compatible unit.

This is a pantry-management and recipe-matching application. It does not provide grocery delivery, maps, GPS, payments, or external backend services.

## Features

- Add, view, edit and delete pantry ingredients.
- Store ingredient quantity, unit and optional expiry date.
- SQLite persistence using `SQLiteOpenHelper`.
- RecyclerView with custom pantry and recipe adapters.
- 20 seeded recipes on first database creation.
- Ingredient normalization for simple singular/plural differences.
- Compatible-unit conversion for count, mass and volume units.
- Strict recipe matching with no partial matches.
- Recipe detail screen with ingredients and preparation instructions.
- Expiring-soon reminder setting using local device date.
- Preferred-unit setting using SharedPreferences.
- Empty states and input validation.
- Offline operation with no location or internet dependency.

## Technologies

- Java
- Android Studio
- XML layouts
- SQLite / SQLiteOpenHelper
- RecyclerView
- Material Components
- SharedPreferences

## Database

The application uses a local SQLite database named `SmartPantry.db`.

### `pantry_items`

- `id INTEGER PRIMARY KEY AUTOINCREMENT`
- `name TEXT NOT NULL`
- `quantity REAL NOT NULL`
- `unit TEXT NOT NULL`
- `expiry_date TEXT`

### `recipes`

- `id INTEGER PRIMARY KEY AUTOINCREMENT`
- `name TEXT NOT NULL`
- `instructions TEXT NOT NULL`

### `recipe_ingredients`

- `id INTEGER PRIMARY KEY AUTOINCREMENT`
- `recipe_id INTEGER NOT NULL`
- `ingredient_name TEXT NOT NULL`
- `required_quantity REAL NOT NULL`
- `unit TEXT NOT NULL`

`recipe_ingredients.recipe_id` is a foreign key to `recipes.id` with cascade delete.

## Strict Matching Algorithm

For every recipe, the application checks every required ingredient:

1. Normalize the ingredient name.
2. Find pantry records with the same normalized name.
3. Check that the units are compatible.
4. Convert the pantry quantity to the recipe unit when possible.
5. Add compatible quantities for duplicate pantry records.
6. Compare available quantity with the required quantity.
7. If any ingredient is missing or insufficient, reject the recipe.
8. Only recipes that pass every ingredient check are displayed.

There is no percentage matching, close-enough matching, or automatic shopping suggestion in the core list.

## Project Structure

```text
SmartPantryManager/
├── app/
│   └── src/main/
│       ├── java/com/example/smartpantry/
│       │   ├── activities/
│       │   ├── adapters/
│       │   ├── database/
│       │   ├── models/
│       │   └── utils/
│       └── res/
│           ├── drawable/
│           ├── layout/
│           ├── menu/
│           └── values/
├── build.gradle
├── settings.gradle
├── gradle.properties
└── README.md
```

## Main Activities

- `MainActivity.java` — pantry list and navigation.
- `AddEditIngredientActivity.java` — create/update pantry records.
- `SuggestedRecipesActivity.java` — strict recipe suggestions.
- `RecipeDetailActivity.java` — complete recipe information.
- `SettingsActivity.java` — persistent application preferences.

## Requirements

- Android Studio with an Android SDK capable of compiling API 35.
- Java 17 for the project configuration.
- Android device or emulator running API 23 or newer.

## How to Run

1. Open the `SmartPantryManager` folder in Android Studio.
2. Allow Android Studio to synchronize the Gradle project.
3. If prompted, install the required Android SDK components.
4. Connect an Android device or start an emulator.
5. Run the `app` configuration.
6. The first database creation automatically inserts the initial 20 recipes.

## Testing Checklist

The project is designed to support these assignment tests:

- Create an ingredient and confirm it appears.
- Close and reopen the app and confirm pantry data persists.
- Edit an ingredient and confirm the list updates.
- Delete an ingredient and confirm it disappears.
- Add all ingredients for a recipe and confirm the recipe appears.
- Remove one required ingredient and confirm the recipe disappears.
- Test insufficient quantities.
- Test singular/plural normalization such as `tomatoes` and `tomato`.
- Test compatible units such as grams and kilograms.
- Test incompatible units such as pieces versus kilograms.
- Test an empty pantry.
- Test empty, zero, negative and invalid quantities.

## GitHub

Repository: `YOUR_GITHUB_REPOSITORY_LINK`

## Assignment Notes

The implementation deliberately uses simple Java classes and database methods so that the application can be explained clearly in a practical demonstration and written report. No Kotlin, Compose, Firebase, PostgreSQL, Supabase, MongoDB, maps, GPS, payments, or external backend is required.
