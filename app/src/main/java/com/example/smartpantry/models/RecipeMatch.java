package com.example.smartpantry.models;

import java.util.ArrayList;
import java.util.List;

public class RecipeMatch {
    private final Recipe recipe;
    private final List<RecipeIngredientMatch> ingredientMatches;
    private final int matchedCount;
    private final int totalCount;
    private final boolean canMakeFully;

    public RecipeMatch(Recipe recipe, List<RecipeIngredientMatch> ingredientMatches) {
        this.recipe = recipe;
        this.ingredientMatches = ingredientMatches != null ? ingredientMatches : new ArrayList<>();
        this.totalCount = this.ingredientMatches.size();

        int count = 0;
        for (RecipeIngredientMatch match : this.ingredientMatches) {
            if (match.isAvailable()) {
                count++;
            }
        }
        this.matchedCount = count;
        this.canMakeFully = (totalCount > 0 && matchedCount == totalCount);
    }

    public Recipe getRecipe() { return recipe; }
    public List<RecipeIngredientMatch> getIngredientMatches() { return ingredientMatches; }
    public int getMatchedCount() { return matchedCount; }
    public int getTotalCount() { return totalCount; }
    public boolean isCanMakeFully() { return canMakeFully; }

    public int getMatchPercentage() {
        return totalCount == 0 ? 0 : Math.round(((float) matchedCount / totalCount) * 100);
    }

    public List<RecipeIngredientMatch> getMatchingIngredients() {
        List<RecipeIngredientMatch> list = new ArrayList<>();
        for (RecipeIngredientMatch m : ingredientMatches) {
            if (m.isAvailable()) list.add(m);
        }
        return list;
    }

    public List<RecipeIngredientMatch> getMissingIngredients() {
        List<RecipeIngredientMatch> list = new ArrayList<>();
        for (RecipeIngredientMatch m : ingredientMatches) {
            if (!m.isAvailable()) list.add(m);
        }
        return list;
    }
}
