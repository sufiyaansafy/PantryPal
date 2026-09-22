package com.safy.pantrypal;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * ADD / EDIT screen.
 *
 *   Started with NO extras          -> CREATE a new pantry item
 *   Started with EXTRA_ITEM inside  -> UPDATE that item
 *
 * Nothing is sent to the database until validate() is happy with every field.
 */
public class AddEditItemActivity extends AppCompatActivity {

    /** The name of the extra that MainActivity puts inside the Intent. */
    public static final String EXTRA_ITEM = "extra_pantry_item";

    /** ROTATION FIX: key used to remember the chosen date while the screen is rebuilt. */
    private static final String STATE_EXPIRY_DATE = "state_expiry_date";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault());

    private TextInputLayout nameLayout;
    private TextInputLayout quantityLayout;
    private TextInputLayout unitLayout;
    private TextInputEditText nameInput;
    private TextInputEditText quantityInput;
    private MaterialAutoCompleteTextView unitInput;
    private TextView expiryValue;
    private MaterialButton clearDateButton;
    private MaterialButton saveButton;

    private PantryItem editingItem;   // null = we are adding, not editing
    private LocalDate expiryDate;     // null = no expiry date chosen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> finish());

        nameLayout = findViewById(R.id.nameLayout);
        quantityLayout = findViewById(R.id.quantityLayout);
        unitLayout = findViewById(R.id.unitLayout);
        nameInput = findViewById(R.id.nameInput);
        quantityInput = findViewById(R.id.quantityInput);
        unitInput = findViewById(R.id.unitInput);
        expiryValue = findViewById(R.id.expiryValue);
        clearDateButton = findViewById(R.id.clearDateButton);
        saveButton = findViewById(R.id.saveButton);

        // Did the pantry screen send us an item to edit CHECKS?
        editingItem = IntentCompat.getSerializableExtra(getIntent(), EXTRA_ITEM, PantryItem.class);
        if (editingItem == null) {
            toolbar.setTitle(R.string.title_add);
        } else {
            toolbar.setTitle(R.string.title_edit);
            nameInput.setText(editingItem.getName());
            quantityInput.setText(UnitConverter.format(editingItem.getQuantity()));
            unitInput.setText(editingItem.getUnit(), false);   // false = do not filter the list
            expiryDate = editingItem.getExpiryDate();
        }
        // ROTATION FIX: turning the phone destroys and rebuilds the screen. Android
        // restores the typed text by itself, but not the LocalDate, so it is restored here.
        if (savedInstanceState != null) {
            String saved = savedInstanceState.getString(STATE_EXPIRY_DATE, "");
            expiryDate = saved.isEmpty() ? null : LocalDate.parse(saved);
        }
        showExpiryDate();

        findViewById(R.id.pickDateButton).setOnClickListener(view -> showDatePicker());
        clearDateButton.setOnClickListener(view -> {
            expiryDate = null;
            showExpiryDate();
        });
        saveButton.setOnClickListener(view -> save());
    }

    /** ROTATION FIX: called just before the screen is destroyed, so we can save the date. */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_EXPIRY_DATE, expiryDate == null ? "" : expiryDate.toString());
    }

    /** Shows the chosen expiry date, or "Not set". */
    private void showExpiryDate() {
        if (expiryDate == null) {
            expiryValue.setText(R.string.expiry_none);
            clearDateButton.setVisibility(View.GONE);
        } else {
            expiryValue.setText(expiryDate.format(DATE_FORMAT));
            clearDateButton.setVisibility(View.VISIBLE);
        }
    }

    /** A calendar popup, so the date can never be typed in the wrong format. */
    private void showDatePicker() {
        LocalDate start = expiryDate == null ? LocalDate.now().plusDays(7) : expiryDate;

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (picker, year, month, dayOfMonth) -> {
                    expiryDate = LocalDate.of(year, month + 1, dayOfMonth);   // month starts at 0
                    showExpiryDate();
                },
                start.getYear(), start.getMonthValue() - 1, start.getDayOfMonth());

        if (editingItem == null) {
            // New food cannot already be out of date
            dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        }
        dialog.show();
    }

    /**
     * INPUT VALIDATION – checks every field and shows the problem under that field.
     * Returns true only when everything is acceptable.
     */
    private boolean validate() {
        boolean valid = true;

        nameLayout.setError(null);
        quantityLayout.setError(null);
        unitLayout.setError(null);

        // 1) Name: not empty, sensible length, letters only
        String name = textOf(nameInput);
        if (name.isEmpty()) {
            nameLayout.setError(getString(R.string.error_name_required));
            valid = false;
        } else if (name.length() < 2 || name.length() > 40) {
            nameLayout.setError(getString(R.string.error_name_length));
            valid = false;
        } else if (!name.matches("[\\p{L} '\\-]+")) {
            nameLayout.setError(getString(R.string.error_name_letters));
            valid = false;
        }

        // 2) Quantity: a real number, greater than 0, not silly large
        String quantityText = textOf(quantityInput).replace(',', '.');
        if (quantityText.isEmpty()) {
            quantityLayout.setError(getString(R.string.error_quantity_required));
            valid = false;
        } else {
            double quantity = parseQuantity(quantityText);
            if (Double.isNaN(quantity) || quantity <= 0) {
                quantityLayout.setError(getString(R.string.error_quantity_positive));
                valid = false;
            } else if (quantity > 100000) {
                quantityLayout.setError(getString(R.string.error_quantity_too_big));
                valid = false;
            }
        }

        // 3) Unit: must be one of the units the database accepts
        List<String> allowedUnits = Arrays.asList(getResources().getStringArray(R.array.units));
        if (!allowedUnits.contains(textOf(unitInput))) {
            unitLayout.setError(getString(R.string.error_unit));
            valid = false;
        }

        return valid;
    }

    /** CREATE or UPDATE, depending on how the screen was opened. */
    private void save() {
        if (!validate()) {
            return;   // the errors are already on screen
        }

        PantryItem item = new PantryItem(
                editingItem == null ? 0 : editingItem.getId(),
                capitalise(textOf(nameInput)),
                parseQuantity(textOf(quantityInput).replace(',', '.')),
                textOf(unitInput),
                expiryDate);

        saveButton.setEnabled(false);          // stops double taps saving twice
        saveButton.setText(R.string.saving);

        SupabaseClient.ResultCallback<Void> whenDone = new SupabaseClient.ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(AddEditItemActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
                finish();   // back to the pantry list, whose onResume() reloads the data
            }

            @Override
            public void onError(String message) {
                saveButton.setEnabled(true);
                saveButton.setText(R.string.save);
                Toast.makeText(AddEditItemActivity.this, message, Toast.LENGTH_LONG).show();
            }
        };

        if (editingItem == null) {
            SupabaseClient.get().addPantryItem(item, whenDone);
        } else {
            SupabaseClient.get().updatePantryItem(item, whenDone);
        }
    }

    /** Reads a text field safely (getText() can be null) and trims the spaces. */
    private static String textOf(TextView input) {
        CharSequence value = input.getText();
        return value == null ? "" : value.toString().trim();
    }

    /** Turns typed text into a number; returns NaN when it is not a number at all. */
    private static double parseQuantity(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    /** "tomatoes" -> "Tomatoes", so the list looks tidy however it was typed. */
    private static String capitalise(String text) {
        if (text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase(Locale.getDefault()) + text.substring(1);
    }
}