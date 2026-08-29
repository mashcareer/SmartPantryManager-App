package com.example.smartpantry.utils;

import com.example.smartpantry.models.PantryItem;
import com.example.smartpantry.models.Recipe;
import com.example.smartpantry.models.RecipeIngredient;
import com.example.smartpantry.models.RecipeIngredientMatch;
import com.example.smartpantry.models.RecipeMatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StrictRecipeMatcher {

    public List<RecipeMatch> evaluateAllRecipes(List<PantryItem> pantry, List<Recipe> recipes, RecipeIngredientProvider provider) {
        List<RecipeMatch> matches = new ArrayList<>();
        for (Recipe recipe : recipes) {
            List<RecipeIngredient> requirements = provider.getIngredients(recipe.getId());
            List<RecipeIngredientMatch> ingredientMatches = new ArrayList<>();

            for (RecipeIngredient requirement : requirements) {
                double available = 0.0;
                String target = IngredientNormalizer.normalize(requirement.getIngredientName());
                for (PantryItem item : pantry) {
                    if (!target.equals(IngredientNormalizer.normalize(item.getName()))) continue;
                    if (!UnitConverter.areCompatible(item.getUnit(), requirement.getUnit())) continue;
                    available += UnitConverter.convert(item.getQuantity(), item.getUnit(), requirement.getUnit());
                }
                ingredientMatches.add(new RecipeIngredientMatch(requirement, available));
            }

            matches.add(new RecipeMatch(recipe, ingredientMatches));
        }

        Collections.sort(matches, (m1, m2) -> {
            int comp = Integer.compare(m2.getMatchPercentage(), m1.getMatchPercentage());
            if (comp != 0) return comp;
            return m1.getRecipe().getName().compareToIgnoreCase(m2.getRecipe().getName());
        });

        return matches;
    }

    public List<Recipe> findMatches(List<PantryItem> pantry, List<Recipe> recipes, RecipeIngredientProvider provider) {
        List<RecipeMatch> matches = evaluateAllRecipes(pantry, recipes, provider);
        List<Recipe> fullMatches = new ArrayList<>();
        for (RecipeMatch match : matches) {
            if (match.isCanMakeFully()) {
                fullMatches.add(match.getRecipe());
            }
        }
        return fullMatches;
    }

    public interface RecipeIngredientProvider {
        List<RecipeIngredient> getIngredients(int recipeId);
    }
}
