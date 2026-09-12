package com.example.smartpantry.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.models.Recipe;
import com.example.smartpantry.models.RecipeIngredientMatch;
import com.example.smartpantry.models.RecipeMatch;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
    public interface Listener { void onRecipeClick(Recipe recipe); }
    private final Listener listener;
    private final List<RecipeMatch> recipeMatches = new ArrayList<>();

    public RecipeAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setRecipeMatches(List<RecipeMatch> list) {
        recipeMatches.clear();
        if (list != null) recipeMatches.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        RecipeMatch match = recipeMatches.get(position);
        Recipe recipe = match.getRecipe();
        Context context = h.itemView.getContext();

        h.name.setText(recipe.getName());

        int pct = match.getMatchPercentage();
        h.progress.setProgress(pct);

        if (match.isCanMakeFully()) {
            h.badge.setText("✓ 100% Ready");
            h.badge.setBackgroundResource(R.drawable.bg_badge_match);
            h.badge.setTextColor(ContextCompat.getColor(context, R.color.badge_match_text));
            h.progress.setIndicatorColor(ContextCompat.getColor(context, R.color.primary));
        } else if (match.getMatchedCount() > 0) {
            h.badge.setText(match.getMatchedCount() + "/" + match.getTotalCount() + " Ingredients");
            h.badge.setBackgroundResource(R.drawable.bg_badge_partial);
            h.badge.setTextColor(ContextCompat.getColor(context, R.color.badge_partial_text));
            h.progress.setIndicatorColor(ContextCompat.getColor(context, R.color.secondary));
        } else {
            h.badge.setText("0/" + match.getTotalCount() + " Ingredients");
            h.badge.setBackgroundResource(R.drawable.bg_badge_missing);
            h.badge.setTextColor(ContextCompat.getColor(context, R.color.badge_missing_text));
            h.progress.setIndicatorColor(ContextCompat.getColor(context, R.color.danger));
        }

        // Matching Ingredients
        List<RecipeIngredientMatch> matching = match.getMatchingIngredients();
        if (!matching.isEmpty()) {
            StringBuilder sb = new StringBuilder("✓ Have: ");
            for (int i = 0; i < matching.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(matching.get(i).getIngredientName());
            }
            h.matchingText.setText(sb.toString());
            h.matchingText.setVisibility(View.VISIBLE);
        } else {
            h.matchingText.setVisibility(View.GONE);
        }

        // Missing Ingredients
        List<RecipeIngredientMatch> missing = match.getMissingIngredients();
        if (!missing.isEmpty()) {
            StringBuilder sb = new StringBuilder("✗ Need: ");
            for (int i = 0; i < missing.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(missing.get(i).getIngredientName());
            }
            h.missingText.setText(sb.toString());
            h.missingText.setVisibility(View.VISIBLE);
        } else {
            h.missingText.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override public int getItemCount() { return recipeMatches.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, badge, matchingText, missingText;
        LinearProgressIndicator progress;
        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.recipeName);
            badge = v.findViewById(R.id.recipeBadge);
            matchingText = v.findViewById(R.id.matchingIngredientsText);
            missingText = v.findViewById(R.id.missingIngredientsText);
            progress = v.findViewById(R.id.matchProgress);
        }
    }
}
