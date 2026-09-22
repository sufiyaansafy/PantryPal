package com.safy.pantrypal;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

import java.util.List;

/**
 * SETTINGS screen.
 *
 * Three choices, each saved immediately through AppPrefs (SharedPreferences):
 *   1. warn about food that expires soon (used by the banner on the pantry screen)
 *   2. how many days of warning (used by the matcher and the pantry list colours)
 *   3. show the "Almost there" list on the suggestions screen
 *
 * These are personal display settings. The pantry and the recipes stay in the PostgreSQL database.
 */
public class SettingsActivity extends AppCompatActivity {

    private Slider warnDaysSlider;
    private TextView warnDaysLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> finish());

        MaterialSwitch alertsSwitch = findViewById(R.id.alertsSwitch);
        MaterialSwitch almostSwitch = findViewById(R.id.almostSwitch);
        warnDaysSlider = findViewById(R.id.warnDaysSlider);
        warnDaysLabel = findViewById(R.id.warnDaysLabel);

        // 1) Show the settings that were saved last time
        boolean alertsOn = AppPrefs.alertsEnabled(this);
        int warnDays = AppPrefs.warnDays(this);

        alertsSwitch.setChecked(alertsOn);
        almostSwitch.setChecked(AppPrefs.showAlmostThere(this));
        warnDaysSlider.setValue(warnDays);
        warnDaysSlider.setEnabled(alertsOn);
        showWarnDays(warnDays);

        // 2) Save every change as soon as the user makes it
        alertsSwitch.setOnCheckedChangeListener((button, isChecked) -> {
            AppPrefs.setAlertsEnabled(this, isChecked);
            warnDaysSlider.setEnabled(isChecked);
        });

        warnDaysSlider.addOnChangeListener((slider, value, fromUser) -> {
            int days = (int) value;
            AppPrefs.setWarnDays(this, days);
            showWarnDays(days);
        });

        almostSwitch.setOnCheckedChangeListener((button, isChecked) ->
                AppPrefs.setShowAlmostThere(this, isChecked));

        TextView versionText = findViewById(R.id.versionText);
        versionText.setText(getString(R.string.settings_version, BuildConfig.VERSION_NAME));
        loadRecipeCount();
    }

    /** Warns 3 days before the expiry date*/
    private void showWarnDays(int days) {
        warnDaysLabel.setText(getResources().getQuantityString(
                R.plurals.settings_warn_days, days, days));
    }

    /** A small reading from the database, so the user can see the recipe library is there. */
    private void loadRecipeCount() {
        TextView libraryText = findViewById(R.id.libraryText);
        libraryText.setText(R.string.settings_library_loading);

        SupabaseClient.get().getRecipes(new SupabaseClient.ResultCallback<List<Recipe>>() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                libraryText.setText(getString(R.string.settings_library, recipes.size()));
            }

            @Override
            public void onError(String message) {
                libraryText.setText(R.string.settings_library_error);
            }
        });
    }
}