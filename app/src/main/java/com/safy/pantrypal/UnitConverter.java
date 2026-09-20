package com.safy.pantrypal;

import java.util.Locale;

/**
 * Converts quantities so that different units can be compared.
 * Every unit belongs to one "dimension", and each dimension has a base unit:
 *
 *   MASS   → grams (g)         1 kg = 1000 g
 *   VOLUME → millilitres (ml)  1 L = 1000 ml, 1 cup = 250 ml, 1 tbsp = 15 ml, 1 tsp = 5 ml
 *   COUNT  → pieces (pcs)
 *
 * Mass and volume are deliberately NOT converted into each other: that would need the
 * density of every ingredient (a cup of flour and a cup of sugar weigh different amounts),
 * and guessing could break the strict matching rule.
 */
public final class UnitConverter {

    /** The kinds of measurement. Only amounts of the same kind can be compared. */
    public enum Dimension { MASS, VOLUME, COUNT }

    /** The units the app offers – the same list as the CHECK rule in 01_schema.sql. */
    public static final String[] UNITS = {"g", "kg", "ml", "L", "tsp", "tbsp", "cup", "pcs"};

    private UnitConverter() {
        // Only static helper methods, so nobody needs to create an instance.
    }

    /** Maps spelling variations to one standard unit, e.g. "Grams" → "g", "litres" → "L". */
    public static String canonicalUnit(String unit) {
        String u = unit == null ? "" : unit.trim().toLowerCase(Locale.ROOT);
        switch (u) {
            case "g": case "gram": case "grams":
                return "g";
            case "kg": case "kgs": case "kilogram": case "kilograms":
                return "kg";
            case "ml": case "millilitre": case "millilitres": case "milliliter": case "milliliters":
                return "ml";
            case "l": case "litre": case "litres": case "liter": case "liters":
                return "L";
            case "tsp": case "teaspoon": case "teaspoons":
                return "tsp";
            case "tbsp": case "tablespoon": case "tablespoons":
                return "tbsp";
            case "cup": case "cups":
                return "cup";
            default:
                return "pcs";   // pcs, piece, pieces, each, whole ...
        }
    }

    /** Which dimension a unit measures, e.g. "kg" → MASS, "tbsp" → VOLUME. */
    public static Dimension dimensionOf(String unit) {
        switch (canonicalUnit(unit)) {
            case "g":
            case "kg":
                return Dimension.MASS;
            case "ml":
            case "L":
            case "tsp":
            case "tbsp":
            case "cup":
                return Dimension.VOLUME;
            default:
                return Dimension.COUNT;
        }
    }

    /** Converts an amount into its base unit (g, ml or pcs), e.g. 1.5 kg → 1500. */
    public static double toBaseAmount(double quantity, String unit) {
        switch (canonicalUnit(unit)) {
            case "kg":
            case "L":
                return quantity * 1000;
            case "cup":
                return quantity * 250;   // metric cup
            case "tbsp":
                return quantity * 15;
            case "tsp":
                return quantity * 5;
            default:
                return quantity;         // g, ml and pcs are already base units
        }
    }

    /** Shows 3.0 as "3" and 1.25 as "1.25" – always with a dot, whatever the phone's language. */
    public static String format(double value) {
        double rounded = Math.round(value * 100) / 100.0;
        if (rounded == Math.rint(rounded)) {
            return String.valueOf((long) rounded);
        }
        return String.valueOf(rounded);
    }
}