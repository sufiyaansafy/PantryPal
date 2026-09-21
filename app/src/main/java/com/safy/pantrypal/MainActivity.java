package com.safy.pantrypal;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * HOME SCREEN – everything the user has in the pantry.
 *
 * Lifecycle note: the data is loaded in onResume(), not in onCreate(). onCreate() runs
 * once when the screen is built, but onResume() runs every single time the screen comes
 * back to the front, so the list refreshes automatically after adding, editing or
 * deleting an item on the other screen.
 *
 * This class implements PantryAdapter.Listener, which means the adapter can call
 * onItemClick() and onDeleteClick() here whenever a row is tapped.
 */
public class MainActivity extends AppCompatActivity implements PantryAdapter.Listener {

    private PantryAdapter adapter;
    private TextView emptyView;
    private TextView expiringBanner;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The toolbar menu is the navigation element of the app
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);

        progress = findViewById(R.id.progress);
        emptyView = findViewById(R.id.emptyView);
        expiringBanner = findViewById(R.id.expiringBanner);

        // Set up the list: a LinearLayoutManager stacks the rows vertically
        RecyclerView pantryList = findViewById(R.id.pantryList);
        adapter = new PantryAdapter(this);
        pantryList.setLayoutManager(new LinearLayoutManager(this));
        pantryList.setAdapter(adapter);

        // An explicit Intent with NO extras means "add a new item"
        findViewById(R.id.addButton).setOnClickListener(view ->
                startActivity(new Intent(this, AddEditItemActivity.class)));

        // The warning banner is a shortcut to the recipes that use that food
        expiringBanner.setOnClickListener(view ->
                startActivity(new Intent(this, SuggestionsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    /** READ – asks Supabase for every pantry item and shows the result. */
    private void loadPantry() {
        progress.setVisibility(View.VISIBLE);

        SupabaseClient.get().getPantryItems(new SupabaseClient.ResultCallback<List<PantryItem>>() {
            @Override
            public void onSuccess(List<PantryItem> items) {
                progress.setVisibility(View.GONE);

                int warnDays = AppPrefs.warnDays(MainActivity.this);
                adapter.setItems(items, warnDays);

                // Empty state: never leave the user looking at a blank screen
                emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                showExpiringBanner(items, warnDays);
            }

            @Override
            public void onError(String message) {
                progress.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Counts the items that expire within the number of days chosen in Settings. */
    private void showExpiringBanner(List<PantryItem> items, int warnDays) {
        int count = 0;
        for (PantryItem item : items) {
            Long days = item.daysUntilExpiry();
            if (days != null && days >= 0 && days <= warnDays) {
                count++;
            }
        }

        if (count == 0 || !AppPrefs.alertsEnabled(this)) {
            expiringBanner.setVisibility(View.GONE);
        } else {
            expiringBanner.setText(
                    getResources().getQuantityString(R.plurals.expiring_banner, count, count));
            expiringBanner.setVisibility(View.VISIBLE);
        }
    }

    /** A row was tapped opens the same form, but with the item inside the Intent. */
    @Override
    public void onItemClick(PantryItem item) {
        Intent intent = new Intent(this, AddEditItemActivity.class);
        intent.putExtra(AddEditItemActivity.EXTRA_ITEM, item);   // PantryItem is Serializable
        startActivity(intent);
    }

    /** The bin icon was tapped: always asks first, deleting cannot be undone. */
    @Override
    public void onDeleteClick(PantryItem item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, item.getName()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteItem(item))
                .show();
    }

    /** DELETE – removes the row from the database, then reloads the list. */
    private void deleteItem(PantryItem item) {
        SupabaseClient.get().deletePantryItem(item.getId(), new SupabaseClient.ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(MainActivity.this,
                        getString(R.string.deleted, item.getName()), Toast.LENGTH_SHORT).show();
                loadPantry();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Navigation: each menu item starts another Activity with an explicit Intent. */
    private boolean onMenuItemClick(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.action_suggestions) {
            startActivity(new Intent(this, SuggestionsActivity.class));
            return true;
        }
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return false;
    }
}