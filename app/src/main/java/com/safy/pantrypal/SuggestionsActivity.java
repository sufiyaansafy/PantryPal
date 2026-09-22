package com.safy.pantrypal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * SUGGESTED RECIPES screen.
 *
 *   1. Load the pantry AND every recipe from Supabase.
 *   2. Run IngredientMatcher (the strict rule) on every recipe.
 *   3. Main list  = recipes with NOTHING missing.
 *      Empty list = show "No recipes match your pantry yet – add more ingredients."
 *   4. Separate "Almost there" list = recipes missing exactly one ingredient
 *      (can be switched off in Settings). These are never mixed into the main list.
 *
 * Matching runs again in onResume(), so after changing the pantry the lists update.
 */
public class SuggestionsActivity extends AppCompatActivity implements RecipeAdapter.Listener {

    private RecipeAdapter cookNowAdapter;
    private RecipeAdapter almostAdapter;
    private TextView summaryText;
    private TextView emptyView;
    private TextView almostHeader;
    private TextView almostNote;
    private RecyclerView almostList;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> finish());

        summaryText = findViewById(R.id.summaryText);
        emptyView = findViewById(R.id.emptyView);
        almostHeader = findViewById(R.id.almostHeader);
        almostNote = findViewById(R.id.almostNote);
        almostList = findViewById(R.id.almostList);
        progress = findViewById(R.id.progress);

        cookNowAdapter = new RecipeAdapter(this);
        RecyclerView cookNowList = findViewById(R.id.cookNowList);
        cookNowList.setLayoutManager(new LinearLayoutManager(this));
        cookNowList.setAdapter(cookNowAdapter);

        almostAdapter = new RecipeAdapter(this);
        almostList.setLayoutManager(new LinearLayoutManager(this));
        almostList.setAdapter(almostAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAndMatch();
    }

    /** Two READ requests: first the pantry, then the recipes, then the matching. */
    private void loadAndMatch() {
        progress.setVisibility(View.VISIBLE);
        SupabaseClient database = SupabaseClient.get();

        database.getPantryItems(new SupabaseClient.ResultCallback<List<PantryItem>>() {
            @Override
            public void onSuccess(List<PantryItem> pantry) {
                database.getRecipes(new SupabaseClient.ResultCallback<List<Recipe>>() {
                    @Override
                    public void onSuccess(List<Recipe> recipes) {
                        progress.setVisibility(View.GONE);
                        showMatches(pantry, recipes);
                    }

                    @Override
                    public void onError(String message) {
                        showError(message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                showError(message);
            }
        });
    }

    /** Runs the strict rule on every recipe and it will fill the two lists. */
    private void showMatches(List<PantryItem> pantry, List<Recipe> recipes) {
        IngredientMatcher matcher = new IngredientMatcher(pantry, AppPrefs.warnDays(this));
        List<IngredientMatcher.MatchResult> cookNow = new ArrayList<>();
        List<IngredientMatcher.MatchResult> almostThere = new ArrayList<>();

        for (Recipe recipe : recipes) {
            IngredientMatcher.MatchResult result = matcher.check(recipe);
            if (result.canCook()) {
                cookNow.add(result);            // strict rule passed: nothing missing
            } else if (result.isAlmostThere()) {
                almostThere.add(result);        // one short: bonus list only
            }                                   // two or more missing: not shown at all
        }

        // Recipes that use food expiring soon go to the top – less food wasted.
        // (The recipes arrive sorted A–Z, and this sort keeps that order inside each group.)
        cookNow.sort((a, b) -> Boolean.compare(b.usesExpiringFood, a.usesExpiringFood));

        // Main list
        cookNowAdapter.setResults(cookNow);
        if (cookNow.isEmpty()) {
            summaryText.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            summaryText.setText(getResources().getQuantityString(
                    R.plurals.cook_now_summary, cookNow.size(), cookNow.size()));
            summaryText.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }

        // Optional "Almost there" section
        boolean showAlmost = AppPrefs.showAlmostThere(this) && !almostThere.isEmpty();
        almostAdapter.setResults(almostThere);
        int almostVisibility = showAlmost ? View.VISIBLE : View.GONE;
        almostHeader.setVisibility(almostVisibility);
        almostNote.setVisibility(almostVisibility);
        almostList.setVisibility(almostVisibility);
    }

    private void showError(String message) {
        progress.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /** If a card was tapped then open the detail screen with the recipe inside the Intent. */
    @Override
    public void onRecipeClick(IngredientMatcher.MatchResult result) {
        ArrayList<String> missingNames = new ArrayList<>();
        for (RecipeIngredient ingredient : result.missing) {
            missingNames.add(ingredient.getName());
        }

        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE, result.recipe);
        intent.putStringArrayListExtra(RecipeDetailActivity.EXTRA_MISSING, missingNames);
        startActivity(intent);
    }
}