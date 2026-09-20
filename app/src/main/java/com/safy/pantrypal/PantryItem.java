package com.safy.pantrypal;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Model class for one row of the pantry_items table.
 * It implements Serializable so a whole item can be sent to the Edit screen inside an Intent.
 */
public class PantryItem implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault());

    private final long id;              // database id (0 = not saved yet)
    private final String name;          // e.g. "Tomatoes"
    private final double quantity;      // e.g. 3
    private final String unit;          // e.g. "pcs" – see UnitConverter.UNITS
    private final LocalDate expiryDate; // null when there is no expiry date

    public PantryItem(long id, String name, double quantity, String unit, LocalDate expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    /** Amount for the screen, e.g. "500 g" or "1.5 L". */
    public String getQuantityLabel() {
        return UnitConverter.format(quantity) + " " + unit;
    }

    /** Expiry date for the screen, e.g. "24 Sep 2026" ("" when there is none). */
    public String getExpiryLabel() {
        return expiryDate == null ? "" : expiryDate.format(DATE_FORMAT);
    }

    /**
     * Days until the item expires: 0 = today, 1 = tomorrow, negative = already expired.
     * Returns null when the item has no expiry date.
     */
    public Long daysUntilExpiry() {
        if (expiryDate == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }
}
