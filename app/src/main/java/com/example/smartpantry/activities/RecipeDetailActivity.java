package com.example.smartpantry.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartpantry.R;
import com.example.smartpantry.database.DatabaseHelper;
import com.example.smartpantry.models.PantryItem;
import com.example.smartpantry.models.Recipe;
import com.example.smartpantry.models.RecipeIngredientMatch;
import com.example.smartpantry.models.RecipeMatch;
import com.example.smartpantry.utils.StrictRecipeMatcher;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private int recipeId = -1;
    private Recipe recipe;

    private TextView nameView, instructionsView, matchBadge, matchText;
    private LinearLayout ingredientsContainer;
    private View matchBanner;
    private MaterialButton btnCookRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        db = new DatabaseHelper(this);
        recipeId = getIntent().getIntExtra("recipe_id", -1);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        nameView = findViewById(R.id.detailName);
        ingredientsContainer = findViewById(R.id.ingredientsContainer);
        instructionsView = findViewById(R.id.detailInstructions);

        matchBanner = findViewById(R.id.matchBanner);
        matchBadge = findViewById(R.id.matchStatusBadge);
        matchText = findViewById(R.id.matchStatusText);
        btnCookRecipe = findViewById(R.id.btnCookRecipe);

        recipe = db.getRecipeById(recipeId);

        if (recipe == null) {
            nameView.setText("Recipe not found");
            matchBanner.setVisibility(View.GONE);
            btnCookRecipe.setVisibility(View.GONE);
            return;
        }

        nameView.setText(recipe.getName());
        instructionsView.setText(recipe.getInstructions());

        btnCookRecipe.setOnClickListener(v -> confirmCookRecipe());

        loadRecipeDetails();
    }

    private void loadRecipeDetails() {
        if (recipe == null) return;

        List<PantryItem> pantry = db.getAllPantryItems();
        List<RecipeMatch> matches = new StrictRecipeMatcher().evaluateAllRecipes(pantry, Collections.singletonList(recipe), db::getRecipeIngredients);

        if (!matches.isEmpty()) {
            RecipeMatch match = matches.get(0);
            matchBanner.setVisibility(View.VISIBLE);

            if (match.isCanMakeFully()) {
                matchBanner.setBackgroundResource(R.drawable.bg_badge_match);
                matchBadge.setText("✓ 100% Ready");
                matchBadge.setTextColor(ContextCompat.getColor(this, R.color.badge_match_text));
                matchText.setText("You have all ingredients in your pantry!");
                matchText.setTextColor(ContextCompat.getColor(this, R.color.badge_match_text));
                btnCookRecipe.setEnabled(true);
            } else if (match.getMatchedCount() > 0) {
                matchBanner.setBackgroundResource(R.drawable.bg_badge_partial);
                matchBadge.setText(match.getMatchedCount() + "/" + match.getTotalCount() + " Matched");
                matchBadge.setTextColor(ContextCompat.getColor(this, R.color.badge_partial_text));
                matchText.setText("You have " + match.getMatchedCount() + " of " + match.getTotalCount() + " ingredients in your pantry.");
                matchText.setTextColor(ContextCompat.getColor(this, R.color.badge_partial_text));
                btnCookRecipe.setEnabled(true);
            } else {
                matchBanner.setBackgroundResource(R.drawable.bg_badge_missing);
                matchBadge.setText("0/" + match.getTotalCount() + " Matched");
                matchBadge.setTextColor(ContextCompat.getColor(this, R.color.badge_missing_text));
                matchText.setText("Missing all ingredients from your pantry.");
                matchText.setTextColor(ContextCompat.getColor(this, R.color.badge_missing_text));
                btnCookRecipe.setEnabled(false);
            }

            // Populate ingredients breakdown
            ingredientsContainer.removeAllViews();
            List<RecipeIngredientMatch> ingredientMatches = match.getIngredientMatches();
            LayoutInflater inflater = LayoutInflater.from(this);

            for (RecipeIngredientMatch im : ingredientMatches) {
                View row = inflater.inflate(R.layout.item_recipe_ingredient_detail, ingredientsContainer, false);
                TextView iconView = row.findViewById(R.id.ingredientIcon);
                TextView nameViewRow = row.findViewById(R.id.ingredientNameAndQty);
                TextView statusSubView = row.findViewById(R.id.ingredientPantryStatus);
                TextView tagView = row.findViewById(R.id.ingredientStatusTag);

                nameViewRow.setText(format(im.getRequiredQuantity()) + " " + im.getUnit() + " — " + im.getIngredientName());

                if (im.isAvailable()) {
                    iconView.setText("✓");
                    iconView.setTextColor(ContextCompat.getColor(this, R.color.success));
                    statusSubView.setText("In Pantry: " + format(im.getAvailableQuantity()) + " " + im.getUnit());
                    statusSubView.setTextColor(ContextCompat.getColor(this, R.color.success));
                    tagView.setText("In Pantry");
                    tagView.setBackgroundResource(R.drawable.bg_badge_match);
                    tagView.setTextColor(ContextCompat.getColor(this, R.color.badge_match_text));
                } else if (im.isPartial()) {
                    iconView.setText("⚠");
                    iconView.setTextColor(ContextCompat.getColor(this, R.color.secondary));
                    statusSubView.setText("Insufficient: Have " + format(im.getAvailableQuantity()) + " " + im.getUnit());
                    statusSubView.setTextColor(ContextCompat.getColor(this, R.color.secondary));
                    tagView.setText("Need More");
                    tagView.setBackgroundResource(R.drawable.bg_badge_partial);
                    tagView.setTextColor(ContextCompat.getColor(this, R.color.badge_partial_text));
                } else {
                    iconView.setText("✗");
                    iconView.setTextColor(ContextCompat.getColor(this, R.color.danger));
                    statusSubView.setText("Missing from pantry");
                    statusSubView.setTextColor(ContextCompat.getColor(this, R.color.danger));
                    tagView.setText("Missing");
                    tagView.setBackgroundResource(R.drawable.bg_badge_missing);
                    tagView.setTextColor(ContextCompat.getColor(this, R.color.badge_missing_text));
                }

                ingredientsContainer.addView(row);
            }

        } else {
            matchBanner.setVisibility(View.GONE);
            btnCookRecipe.setEnabled(false);
        }
    }

    private void confirmCookRecipe() {
        new AlertDialog.Builder(this)
                .setTitle("Deduct Ingredients?")
                .setMessage("Deduct required ingredients for '" + recipe.getName() + "' from your pantry?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Cook & Deduct", (d, w) -> {
                    boolean result = db.deductRecipeIngredients(recipeId);
                    if (result) {
                        Snackbar.make(btnCookRecipe, "Ingredients deducted from pantry!", Snackbar.LENGTH_LONG).show();
                        loadRecipeDetails();
                    } else {
                        Snackbar.make(btnCookRecipe, "No matching pantry items to deduct.", Snackbar.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    private String format(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.format(java.util.Locale.getDefault(), "%.2f", v);
    }
}
