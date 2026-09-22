package com.example.smartpantry.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.R;
import com.example.smartpantry.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        MaterialSwitch alerts = findViewById(R.id.expirySwitch);
        alerts.setChecked(prefs.getBoolean("expiry_alerts", true));
        alerts.setOnCheckedChangeListener((button, checked) -> prefs.edit().putBoolean("expiry_alerts", checked).apply());

        AutoCompleteTextView unit = findViewById(R.id.preferredUnit);
        String[] units = {"pieces", "grams", "kg", "ml", "litres", "cups", "tablespoons", "teaspoons"};
        unit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, units));
        unit.setText(prefs.getString("preferred_unit", "pieces"), false);
        unit.setOnItemClickListener((p, v, pos, id) -> prefs.edit().putString("preferred_unit", units[pos]).apply());

        MaterialButton btnClearPantry = findViewById(R.id.btnClearPantry);
        MaterialButton btnResetRecipes = findViewById(R.id.btnResetRecipes);

        btnClearPantry.setOnClickListener(v -> confirmClearPantry(btnClearPantry));
        btnResetRecipes.setOnClickListener(v -> confirmResetRecipes(btnResetRecipes));

        setupBottomNav();
    }

    private void confirmClearPantry(MaterialButton anchor) {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Pantry Items?")
                .setMessage("Are you sure you want to remove all ingredients from your pantry?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear All", (dialog, which) -> {
                    db.deleteAllPantryItems();
                    Snackbar.make(anchor, "All pantry items cleared!", Snackbar.LENGTH_LONG).show();
                })
                .show();
    }

    private void confirmResetRecipes(MaterialButton anchor) {
        new AlertDialog.Builder(this)
                .setTitle("Reset Default Recipes?")
                .setMessage("Reset recipes database to default pre-loaded recipes?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Reset Recipes", (dialog, which) -> {
                    db.resetAndSeedRecipes();
                    Snackbar.make(anchor, "Default recipes restored successfully!", Snackbar.LENGTH_LONG).show();
                })
                .show();
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_settings);
        nav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_pantry) {
                startActivity(new android.content.Intent(this, MainActivity.class));
                return true;
            }
            if (item.getItemId() == R.id.nav_recipes) {
                startActivity(new android.content.Intent(this, SuggestedRecipesActivity.class));
                return true;
            }
            return item.getItemId() == R.id.nav_settings;
        });
    }
}
