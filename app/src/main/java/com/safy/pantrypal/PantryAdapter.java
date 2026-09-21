package com.safy.pantrypal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Joins the pantry data to the list on screen.
 *
 * A RecyclerView only ever keeps about a screenful of rows in memory and re-uses
 * ("recycles") them as you scroll. The adapter does three jobs:
 *   onCreateViewHolder – blow up (inflate) item_pantry.xml into a real row
 *   onBindViewHolder   – put the data of one PantryItem into that row
 *   getItemCount       – tell the RecyclerView how many rows there are
 *
 * The screen that uses the adapter decides what a tap or a delete does, by
 * implementing the Listener interface below.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /** Lets MainActivity react to taps without the adapter knowing anything about it. */
    public interface Listener {
        void onItemClick(PantryItem item);

        void onDeleteClick(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final Listener listener;
    private int warnDays = 3;

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    /** Replaces everything on screen with fresh data from the database. */
    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<PantryItem> newItems, int warnDays) {
        this.warnDays = warnDays;
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);

        holder.name.setText(item.getName());
        holder.quantity.setText(item.getQuantityLabel());
        showExpiry(holder.expiry, item);


        holder.itemView.setOnClickListener(view -> listener.onItemClick(item));
        holder.deleteButton.setOnClickListener(view -> listener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Writes the expiry line and colours it: red = expired, orange = soon, grey = fine. */
    private void showExpiry(TextView view, PantryItem item) {
        Context context = view.getContext();
        Long days = item.daysUntilExpiry();

        if (days == null) {
            view.setText(R.string.no_expiry);
            view.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        } else if (days < 0) {
            view.setText(context.getString(R.string.expired_on, item.getExpiryLabel()));
            view.setTextColor(ContextCompat.getColor(context, R.color.status_expired));
        } else if (days == 0) {
            view.setText(R.string.expires_today);
            view.setTextColor(ContextCompat.getColor(context, R.color.status_warning));
        } else if (days <= warnDays) {
            view.setText(context.getResources().getQuantityString(
                    R.plurals.expires_in_days, days.intValue(), days.intValue()));
            view.setTextColor(ContextCompat.getColor(context, R.color.status_warning));
        } else {
            view.setText(context.getString(R.string.expires_on, item.getExpiryLabel()));
            view.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        }
    }

    /** Holds the views of one row so they are only looked up once (that is what makes it fast). */
    static class PantryViewHolder extends RecyclerView.ViewHolder {

        final TextView name;
        final TextView quantity;
        final TextView expiry;
        final ImageButton deleteButton;

        PantryViewHolder(@NonNull View row) {
            super(row);
            name = row.findViewById(R.id.itemName);
            quantity = row.findViewById(R.id.itemQuantity);
            expiry = row.findViewById(R.id.itemExpiry);
            deleteButton = row.findViewById(R.id.deleteButton);
        }
    }
}