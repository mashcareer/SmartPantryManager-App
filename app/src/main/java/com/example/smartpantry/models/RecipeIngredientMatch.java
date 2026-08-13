package com.example.smartpantry.models;

public class RecipeIngredientMatch {
    private final RecipeIngredient requirement;
    private final double availableQuantity;
    private final boolean available;
    private final boolean partial;

    public RecipeIngredientMatch(RecipeIngredient requirement, double availableQuantity) {
        this.requirement = requirement;
        this.availableQuantity = availableQuantity;
        this.available = availableQuantity >= requirement.getRequiredQuantity() - 1e-9;
        this.partial = availableQuantity > 0 && !available;
    }

    public RecipeIngredient getRequirement() { return requirement; }
    public double getAvailableQuantity() { return availableQuantity; }
    public boolean isAvailable() { return available; }
    public boolean isPartial() { return partial; }

    public String getIngredientName() { return requirement.getIngredientName(); }
    public double getRequiredQuantity() { return requirement.getRequiredQuantity(); }
    public String getUnit() { return requirement.getUnit(); }
}
