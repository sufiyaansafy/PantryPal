package com.safy.pantrypal;

import java.io.Serializable;

/**
 * One ingredient that a recipe needs, e.g. 250 g maize meal.
 * Serializable because it travels inside a Recipe when a recipe is passed in an Intent.
 */
public class RecipeIngredient implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;      // e.g. "tomatoes"
    private final double quantity;  // e.g. 3
    private final String unit;      // e.g. "pcs"

    public RecipeIngredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
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

    /** Text for the screen, e.g. "3 × tomatoes" or "250 g maize meal". */
    public String getLabel() {
        String amount = UnitConverter.format(quantity);
        if ("pcs".equals(UnitConverter.canonicalUnit(unit))) {
            return amount + " \u00D7 " + name;          // \u00D7 is the × sign
        }
        return amount + " " + unit + " " + name;
    }
}