package com.safy.pantrypal;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Day1Test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ask Supabase for every recipe. The answer arrives a moment later
        // in onSuccess (it worked) or onError (something went wrong).
        SupabaseClient.get().getRecipes(new SupabaseClient.ResultCallback<List<Recipe>>() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                int ingredientCount = 0;
                for (Recipe recipe : recipes) {
                    ingredientCount += recipe.getIngredients().size();
                }
                String message = "Connected! " + recipes.size() + " recipes, "
                        + ingredientCount + " ingredients";
                Log.d(TAG, message);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, message);   // the full message is visible in the Logcat tab
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}