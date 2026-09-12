package com.example.smartpantry.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.adapters.RecipeAdapter;
import com.example.smartpantry.database.DatabaseHelper;
import com.example.smartpantry.models.PantryItem;
import com.example.smartpantry.models.Recipe;
import com.example.smartpantry.models.RecipeMatch;
import com.example.smartpantry.utils.StrictRecipeMatcher;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private RecyclerView recycler;
    private View empty;
    private TextView emptyTitle, emptyHint;
    private ChipGroup filterChipGroup;
    private Chip chipCanMake, chipAlmostReady, chipAll;

    private List<RecipeMatch> allEvaluatedMatches = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        db = new DatabaseHelper(this);
        recycler = findViewById(R.id.recipeRecycler);
        empty = findViewById(R.id.recipeEmptyState);
        emptyTitle = findViewById(R.id.emptyTitle);
        emptyHint = findViewById(R.id.emptyHint);

        filterChipGroup = findViewById(R.id.filterChipGroup);
        chipCanMake = findViewById(R.id.chipCanMake);
        chipAlmostReady = findViewById(R.id.chipAlmostReady);
        chipAll = findViewById(R.id.chipAll);

        adapter = new RecipeAdapter(this::openRecipe);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilter());

        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMatches();
    }

    private void loadMatches() {
        List<Recipe> recipes = db.getAllRecipes();
        List<PantryItem> pantry = db.getAllPantryItems();

        allEvaluatedMatches = new StrictRecipeMatcher().evaluateAllRecipes(pantry, recipes, db::getRecipeIngredients);

        // Calculate chip counts
        int canMakeCount = 0;
        int almostReadyCount = 0;

        for (RecipeMatch match : allEvaluatedMatches) {
            if (match.isCanMakeFully()) {
                canMakeCount++;
            }
            if (match.getMatchPercentage() >= 50) {
                almostReadyCount++;
            }
        }

        chipCanMake.setText("Can Make Now (" + canMakeCount + ")");
        chipAlmostReady.setText("Almost Ready (" + almostReadyCount + ")");
        chipAll.setText("All Recipes (" + allEvaluatedMatches.size() + ")");

        applyFilter();
    }

    private void applyFilter() {
        int checkedId = filterChipGroup.getCheckedChipId();
        List<RecipeMatch> filtered = new ArrayList<>();

        if (checkedId == R.id.chipCanMake) {
            for (RecipeMatch match : allEvaluatedMatches) {
                if (match.isCanMakeFully()) filtered.add(match);
            }
            emptyTitle.setText("No fully matching recipes");
            emptyHint.setText("You need a few more ingredients to make these recipes. Check 'Almost Ready' or add items to your pantry.");
        } else if (checkedId == R.id.chipAlmostReady) {
            for (RecipeMatch match : allEvaluatedMatches) {
                if (match.getMatchPercentage() >= 50) filtered.add(match);
            }
            emptyTitle.setText("No partially matching recipes");
            emptyHint.setText("Add more ingredients to your pantry to discover recipes you can almost make!");
        } else { // chipAll or default
            filtered.addAll(allEvaluatedMatches);
            emptyTitle.setText("No recipes available");
            emptyHint.setText("No recipes are configured in the system yet.");
        }

        adapter.setRecipeMatches(filtered);
        boolean none = filtered.isEmpty();
        recycler.setVisibility(none ? View.GONE : View.VISIBLE);
        empty.setVisibility(none ? View.VISIBLE : View.GONE);
    }

    private void openRecipe(Recipe recipe) {
        Intent i = new Intent(this, RecipeDetailActivity.class);
        i.putExtra("recipe_id", recipe.getId());
        startActivity(i);
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_recipes);
        nav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_pantry) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            }
            if (item.getItemId() == R.id.nav_recipes) return true;
            if (item.getItemId() == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }
}
