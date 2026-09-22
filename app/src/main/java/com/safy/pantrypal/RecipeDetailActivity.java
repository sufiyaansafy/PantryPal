package com.safy.pantrypal;

import android.os.Bundle;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * RECIPE DETAIL screen.
 *
 * It receives everything it needs through the Intent that opened it:
 *   EXTRA_RECIPE  – the whole Recipe object (Recipe is Serializable)
 *   EXTRA_MISSING – the names of any ingredients the pantry does not have enough of
 * So this screen does not need to ask the database for anything.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE = "extra_recipe";
    public static final String EXTRA_MISSING = "extra_missing_names";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> finish());

        // Read the data that came in with the Intent
        Recipe recipe = IntentCompat.getSerializableExtra(getIntent(), EXTRA_RECIPE, Recipe.class);
        if (recipe == null) {
            finish();
            return;
        }
        ArrayList<String> missingNames = getIntent().getStringArrayListExtra(EXTRA_MISSING);
        if (missingNames == null) {
            missingNames = new ArrayList<>();
        }

        toolbar.setTitle(recipe.getName());
        TextView description = findViewById(R.id.descriptionText);
        TextView info = findViewById(R.id.infoText);
        description.setText(recipe.getDescription());
        info.setText(getString(R.string.recipe_info,
                recipe.getMinutes(), recipe.getIngredients().size()));

        showStatus(missingNames.size());
        showIngredients(recipe.getIngredients(), missingNames);
        showSteps(recipe.getStepList());
    }

    /** Green "you have everything" banner, or orange "you are missing ..." banner. */
    private void showStatus(int missingCount) {
        TextView status = findViewById(R.id.statusText);
        if (missingCount == 0) {
            status.setText(R.string.status_can_cook);
            status.setBackgroundColor(ContextCompat.getColor(this, R.color.brand_container));
            status.setTextColor(ContextCompat.getColor(this, R.color.brand_dark));
        } else {
            status.setText(getResources().getQuantityString(
                    R.plurals.status_missing, missingCount, missingCount));
            status.setBackgroundColor(ContextCompat.getColor(this, R.color.accent_container));
            status.setTextColor(ContextCompat.getColor(this, R.color.status_warning));
        }
    }

    /** One line per ingredient: a green tick if you have it, a red cross if you do not. */
    private void showIngredients(List<RecipeIngredient> ingredients, List<String> missingNames) {
        LinearLayout container = findViewById(R.id.ingredientsContainer);
        for (RecipeIngredient ingredient : ingredients) {
            boolean missing = missingNames.contains(ingredient.getName());

            TextView line = new TextView(this);
            line.setText((missing ? "\u2717   " : "\u2713   ") + ingredient.getLabel());   // ✗ or ✓
            line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            line.setTextColor(ContextCompat.getColor(this,
                    missing ? R.color.status_expired : R.color.text_primary));
            line.setPadding(0, 8, 0, 8);
            container.addView(line);
        }
    }

    /** Numbers the steps: "1. ...", "2. ...". */
    private void showSteps(List<String> steps) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < steps.size(); i++) {
            if (i > 0) {
                text.append("\n\n");
            }
            text.append(i + 1).append(". ").append(steps.get(i));
        }
        TextView stepsView = findViewById(R.id.stepsText);
        stepsView.setText(text.toString());
    }
}