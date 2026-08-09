package com.example.smartpantry.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantry.models.PantryItem;
import com.example.smartpantry.models.Recipe;
import com.example.smartpantry.models.RecipeIngredient;
import com.example.smartpantry.utils.IngredientNormalizer;
import com.example.smartpantry.utils.UnitConverter;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "SmartPantry.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry_items (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, expiry_date TEXT)");
        db.execSQL("CREATE TABLE recipes (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, instructions TEXT NOT NULL)");
        db.execSQL("CREATE TABLE recipe_ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, recipe_id INTEGER NOT NULL, ingredient_name TEXT NOT NULL, required_quantity REAL NOT NULL, unit TEXT NOT NULL, FOREIGN KEY(recipe_id) REFERENCES recipes(id) ON DELETE CASCADE)");
        seedRecipesIfNeeded(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 is the initial schema. Future schema changes should be added here incrementally.
    }

    public long insertPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", item.getName());
        values.put("quantity", item.getQuantity());
        values.put("unit", item.getUnit());
        if (item.getExpiryDate() == null || item.getExpiryDate().isEmpty()) values.putNull("expiry_date");
        else values.put("expiry_date", item.getExpiryDate());
        return db.insert("pantry_items", null, values);
    }

    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query("pantry_items", null, null, null, null, null, "name COLLATE NOCASE ASC")) {
            while (c.moveToNext()) items.add(readPantryItem(c));
        }
        return items;
    }

    public PantryItem getPantryItemById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query("pantry_items", null, "id=?", new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? readPantryItem(c) : null;
        }
    }

    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", item.getName());
        values.put("quantity", item.getQuantity());
        values.put("unit", item.getUnit());
        if (item.getExpiryDate() == null || item.getExpiryDate().isEmpty()) values.putNull("expiry_date");
        else values.put("expiry_date", item.getExpiryDate());
        return db.update("pantry_items", values, "id=?", new String[]{String.valueOf(item.getId())});
    }

    public int deletePantryItem(int id) {
        return getWritableDatabase().delete("pantry_items", "id=?", new String[]{String.valueOf(id)});
    }

    public int deleteAllPantryItems() {
        return getWritableDatabase().delete("pantry_items", null, null);
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query("recipes", null, null, null, null, null, "name COLLATE NOCASE ASC")) {
            while (c.moveToNext()) recipes.add(readRecipe(c));
        }
        return recipes;
    }

    public Recipe getRecipeById(int id) {
        try (Cursor c = getReadableDatabase().query("recipes", null, "id=?", new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? readRecipe(c) : null;
        }
    }

    public List<RecipeIngredient> getRecipeIngredients(int recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query("recipe_ingredients", null, "recipe_id=?", new String[]{String.valueOf(recipeId)}, null, null, "id ASC")) {
            while (c.moveToNext()) ingredients.add(readRecipeIngredient(c));
        }
        return ingredients;
    }

    public long insertRecipe(Recipe recipe) {
        ContentValues values = new ContentValues();
        values.put("name", recipe.getName());
        values.put("instructions", recipe.getInstructions());
        return getWritableDatabase().insert("recipes", null, values);
    }

    public long insertRecipeIngredient(RecipeIngredient ingredient) {
        ContentValues values = new ContentValues();
        values.put("recipe_id", ingredient.getRecipeId());
        values.put("ingredient_name", ingredient.getIngredientName());
        values.put("required_quantity", ingredient.getRequiredQuantity());
        values.put("unit", ingredient.getUnit());
        return getWritableDatabase().insert("recipe_ingredients", null, values);
    }

    public boolean deductRecipeIngredients(int recipeId) {
        List<RecipeIngredient> requirements = getRecipeIngredients(recipeId);
        List<PantryItem> pantry = getAllPantryItems();
        boolean anyDeducted = false;

        for (RecipeIngredient req : requirements) {
            String target = IngredientNormalizer.normalize(req.getIngredientName());
            for (PantryItem item : pantry) {
                if (!target.equals(IngredientNormalizer.normalize(item.getName()))) continue;
                if (!UnitConverter.areCompatible(item.getUnit(), req.getUnit())) continue;

                double reqInItemUnit = UnitConverter.convert(req.getRequiredQuantity(), req.getUnit(), item.getUnit());
                double remaining = item.getQuantity() - reqInItemUnit;

                if (remaining <= 0.0001) {
                    deletePantryItem(item.getId());
                } else {
                    item.setQuantity(remaining);
                    updatePantryItem(item);
                }
                anyDeducted = true;
                break;
            }
        }
        return anyDeducted;
    }

    public void resetAndSeedRecipes() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("recipes", null, null);
            seedRecipesIfNeeded(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void seedRecipesIfNeeded() {
        seedRecipesIfNeeded(getWritableDatabase());
    }

    private void seedRecipesIfNeeded(SQLiteDatabase db) {
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM recipes", null)) {
            if (c.moveToFirst() && c.getInt(0) > 0) return;
        }

        db.beginTransaction();
        try {
            seed(db, "Scrambled Eggs", "Beat eggs with milk and salt. Cook gently in a pan, stirring until softly set.",
                    i("egg", 2, "pieces"), i("milk", 2, "tablespoons"), i("salt", 1, "teaspoons"));
            seed(db, "Omelette", "Whisk eggs with salt. Cook in a lightly oiled pan, add onion and tomato, fold and serve.",
                    i("egg", 2, "pieces"), i("onion", 0.5, "pieces"), i("tomato", 1, "pieces"), i("salt", 0.5, "teaspoons"));
            seed(db, "Tomato Pasta", "Boil pasta. Cook tomato, onion and garlic, then toss with pasta and season.",
                    i("pasta", 200, "grams"), i("tomato", 2, "pieces"), i("onion", 1, "pieces"), i("garlic", 2, "pieces"));
            seed(db, "Vegetable Rice", "Cook rice. Stir-fry mixed vegetables with onion, then combine with rice and season.",
                    i("rice", 200, "grams"), i("carrot", 1, "pieces"), i("peas", 50, "grams"), i("onion", 1, "pieces"));
            seed(db, "Fried Rice", "Cook rice, then stir-fry it with egg, peas, carrot and onion until hot.",
                    i("rice", 200, "grams"), i("egg", 2, "pieces"), i("peas", 50, "grams"), i("carrot", 1, "pieces"), i("onion", 1, "pieces"));
            seed(db, "Chicken Rice", "Cook chicken and onion until done. Add cooked rice and seasoning and heat through.",
                    i("chicken", 250, "grams"), i("rice", 200, "grams"), i("onion", 1, "pieces"));
            seed(db, "Pancakes", "Mix flour, milk and eggs into a smooth batter. Fry small portions in a lightly greased pan.",
                    i("flour", 150, "grams"), i("milk", 250, "ml"), i("egg", 1, "pieces"), i("sugar", 2, "tablespoons"));
            seed(db, "French Toast", "Whisk egg and milk. Dip bread in the mixture and fry both sides until golden.",
                    i("bread", 2, "pieces"), i("egg", 1, "pieces"), i("milk", 60, "ml"));
            seed(db, "Egg Sandwich", "Boil or fry eggs, slice them and place on bread with tomato and onion.",
                    i("bread", 2, "pieces"), i("egg", 2, "pieces"), i("tomato", 1, "pieces"), i("onion", 0.5, "pieces"));
            seed(db, "Tomato Sandwich", "Layer sliced tomato, onion and cheese between bread and toast if desired.",
                    i("bread", 2, "pieces"), i("tomato", 1, "pieces"), i("onion", 0.5, "pieces"), i("cheese", 40, "grams"));
            seed(db, "Pasta Primavera", "Cook pasta. Sauté carrot, peas, tomato and onion, then toss with pasta.",
                    i("pasta", 200, "grams"), i("carrot", 1, "pieces"), i("peas", 50, "grams"), i("tomato", 1, "pieces"), i("onion", 1, "pieces"));
            seed(db, "Garlic Pasta", "Cook pasta. Sauté chopped garlic in oil, add pasta and season before serving.",
                    i("pasta", 200, "grams"), i("garlic", 3, "pieces"), i("salt", 0.5, "teaspoons"));
            seed(db, "Chicken Pasta", "Cook pasta and chicken separately. Combine with tomato, onion and garlic and heat through.",
                    i("pasta", 200, "grams"), i("chicken", 200, "grams"), i("tomato", 2, "pieces"), i("onion", 1, "pieces"), i("garlic", 2, "pieces"));
            seed(db, "Vegetable Omelette", "Beat eggs, add chopped carrot, peas and onion, then cook until set.",
                    i("egg", 2, "pieces"), i("carrot", 1, "pieces"), i("peas", 30, "grams"), i("onion", 0.5, "pieces"));
            seed(db, "Potato Omelette", "Boil or fry sliced potato until tender. Add beaten eggs and cook until firm.",
                    i("egg", 3, "pieces"), i("potato", 2, "pieces"), i("onion", 0.5, "pieces"));
            seed(db, "Chicken Stir Fry", "Stir-fry chicken with carrot, peas, onion and garlic until the chicken is cooked.",
                    i("chicken", 250, "grams"), i("carrot", 1, "pieces"), i("peas", 50, "grams"), i("onion", 1, "pieces"), i("garlic", 2, "pieces"));
            seed(db, "Vegetable Stir Fry", "Stir-fry carrot, peas, onion and garlic until crisp-tender. Season and serve.",
                    i("carrot", 2, "pieces"), i("peas", 80, "grams"), i("onion", 1, "pieces"), i("garlic", 2, "pieces"));
            seed(db, "Rice and Eggs", "Cook rice. Fry or scramble eggs and serve them with the hot rice and onion.",
                    i("rice", 200, "grams"), i("egg", 2, "pieces"), i("onion", 0.5, "pieces"));
            seed(db, "Tomato Egg Rice", "Cook rice, sauté tomato and onion, add eggs and fold through the rice.",
                    i("rice", 200, "grams"), i("tomato", 2, "pieces"), i("egg", 2, "pieces"), i("onion", 1, "pieces"));
            seed(db, "Simple Pancakes", "Combine flour, milk, egg and sugar. Cook spoonfuls in a pan until golden on both sides.",
                    i("flour", 120, "grams"), i("milk", 200, "ml"), i("egg", 1, "pieces"), i("sugar", 1, "tablespoons"));
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void seed(SQLiteDatabase db, String name, String instructions, RecipeIngredient... ingredients) {
        ContentValues recipe = new ContentValues();
        recipe.put("name", name);
        recipe.put("instructions", instructions);
        long recipeId = db.insertOrThrow("recipes", null, recipe);
        for (RecipeIngredient ingredient : ingredients) {
            ContentValues values = new ContentValues();
            values.put("recipe_id", recipeId);
            values.put("ingredient_name", ingredient.getIngredientName());
            values.put("required_quantity", ingredient.getRequiredQuantity());
            values.put("unit", ingredient.getUnit());
            db.insertOrThrow("recipe_ingredients", null, values);
        }
    }

    private static RecipeIngredient i(String name, double quantity, String unit) {
        return new RecipeIngredient(0, 0, name, quantity, unit);
    }

    private PantryItem readPantryItem(Cursor c) {
        return new PantryItem(c.getInt(c.getColumnIndexOrThrow("id")), c.getString(c.getColumnIndexOrThrow("name")), c.getDouble(c.getColumnIndexOrThrow("quantity")), c.getString(c.getColumnIndexOrThrow("unit")), c.isNull(c.getColumnIndexOrThrow("expiry_date")) ? "" : c.getString(c.getColumnIndexOrThrow("expiry_date")));
    }

    private Recipe readRecipe(Cursor c) {
        return new Recipe(c.getInt(c.getColumnIndexOrThrow("id")), c.getString(c.getColumnIndexOrThrow("name")), c.getString(c.getColumnIndexOrThrow("instructions")));
    }

    private RecipeIngredient readRecipeIngredient(Cursor c) {
        return new RecipeIngredient(c.getInt(c.getColumnIndexOrThrow("id")), c.getInt(c.getColumnIndexOrThrow("recipe_id")), c.getString(c.getColumnIndexOrThrow("ingredient_name")), c.getDouble(c.getColumnIndexOrThrow("required_quantity")), c.getString(c.getColumnIndexOrThrow("unit")));
    }
}
