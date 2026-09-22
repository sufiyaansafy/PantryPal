package com.safy.pantrypal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows a list of recipe cards. The Suggestions screen uses TWO of these adapters:
 * one for the recipes you can cook, one for the "Almost there" list.
 * Each card shows a small badge line when there is something useful to say:
 *   orange – uses food that expires soon
 *   red    – still needs one ingredient (Almost there list only)
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    /** The Suggestions screen decides what happens when a card is tapped. */
    public interface Listener {
        void onRecipeClick(IngredientMatcher.MatchResult result);
    }

    private final List<IngredientMatcher.MatchResult> results = new ArrayList<>();
    private final Listener listener;

    public RecipeAdapter(Listener listener) {
        this.listener = listener;
    }

    /** Replaces the cards on screen with a new set of match results. */
    @SuppressLint("NotifyDataSetChanged")
    public void setResults(List<IngredientMatcher.MatchResult> newResults) {
        results.clear();
        results.addAll(newResults);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View card = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(card);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        IngredientMatcher.MatchResult result = results.get(position);
        Recipe recipe = result.recipe;
        Context context = holder.itemView.getContext();

        holder.name.setText(recipe.getName());
        holder.description.setText(recipe.getDescription());
        holder.info.setText(context.getString(R.string.recipe_info,
                recipe.getMinutes(), recipe.getIngredients().size()));

        if (!result.canCook()) {
            holder.badge.setText(context.getString(R.string.still_need,
                    result.missing.get(0).getLabel()));
            holder.badge.setTextColor(ContextCompat.getColor(context, R.color.status_expired));
            holder.badge.setVisibility(View.VISIBLE);
        } else if (result.usesExpiringFood) {
            holder.badge.setText(R.string.uses_expiring);
            holder.badge.setTextColor(ContextCompat.getColor(context, R.color.status_warning));
            holder.badge.setVisibility(View.VISIBLE);
        } else {
            holder.badge.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(view -> listener.onRecipeClick(result));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    /** Holds the views of one recipe card. */
    static class RecipeViewHolder extends RecyclerView.ViewHolder {

        final TextView name;
        final TextView description;
        final TextView info;
        final TextView badge;

        RecipeViewHolder(@NonNull View card) {
            super(card);
            name = card.findViewById(R.id.recipeName);
            description = card.findViewById(R.id.recipeDescription);
            info = card.findViewById(R.id.recipeInfo);
            badge = card.findViewById(R.id.recipeBadge);
        }
    }
}