package com.safy.pantrypal;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Decides which recipes can be cooked with ONLY what is in the pantry.
 *
 * STRICT RULE: a recipe can be cooked only if EVERY ingredient it needs is in the
 * pantry in AT LEAST the amount it needs. One missing ingredient, or too little of
 * one, and the recipe is not suggested.
 *
 * In real-world typing, both sides are cleaned up before comparing:
 *   1. Names are normalised: "  Fresh Tomatoes " and "tomato" both become "tomato",
 *      and local names are mapped: "Mielie meal" becomes "maize meal".
 *   2. Amounts are converted to a base unit: 1 kg = 1000 g, 1 L = 1000 ml, 2 tbsp = 30 ml.
 *      Only amounts of the same kind are compared (grams with grams, ml with ml,
 *      pieces with pieces) – see UnitConverter.
 *   3. If the same food is in the pantry twice ("Egg 2" and "Eggs 1"), the amounts
 *      are added together.
 *
 *
 */
public class IngredientMatcher {

    /** The answer for one recipe: can it be cooked, and if not, what is missing? */
    public static class MatchResult {
        public final Recipe recipe;
        public final List<RecipeIngredient> missing;   // empty = can cook right now
        public final boolean usesExpiringFood;         // uses something that expires soon

        MatchResult(Recipe recipe, List<RecipeIngredient> missing, boolean usesExpiringFood) {
            this.recipe = recipe;
            this.missing = missing;
            this.usesExpiringFood = usesExpiringFood;
        }

        /** True only when NOTHING is missing – the strict rule. */
        public boolean canCook() {
            return missing.isEmpty();
        }

        /** Exactly one ingredient short (shown separately, never as a suggestion). */
        public boolean isAlmostThere() {
            return missing.size() == 1;
        }
    }

    // Describing words that do not change WHAT the ingredient is
    private static final Set<String> FILLER_WORDS = new HashSet<>(Arrays.asList(
            "fresh", "large", "small", "medium", "big", "ripe", "chopped", "sliced",
            "diced", "grated", "frozen", "mild", "whole"));

    // Plurals that the simple rules in singular() would get wrong
    private static final Map<String, String> IRREGULAR_PLURALS = new HashMap<>();

    // Different names for the same ingredient (keys are already cleaned and singular)
    private static final Map<String, String> SYNONYMS = new HashMap<>();

    static {
        IRREGULAR_PLURALS.put("leaves", "leaf");
        IRREGULAR_PLURALS.put("loaves", "loaf");
        IRREGULAR_PLURALS.put("halves", "half");
        IRREGULAR_PLURALS.put("chillies", "chilli");
        IRREGULAR_PLURALS.put("chilies", "chilli");
        IRREGULAR_PLURALS.put("cookies", "cookie");

        // South African names
        SYNONYMS.put("mielie meal", "maize meal");
        SYNONYMS.put("mealie meal", "maize meal");
        SYNONYMS.put("courgette", "baby marrow");
        SYNONYMS.put("zucchini", "baby marrow");
        SYNONYMS.put("eggplant", "brinjal");
        SYNONYMS.put("aubergine", "brinjal");
        SYNONYMS.put("morogo", "spinach");
        SYNONYMS.put("swiss chard", "spinach");
        // Everyday alternatives
        SYNONYMS.put("flour", "cake flour");
        SYNONYMS.put("plain flour", "cake flour");
        SYNONYMS.put("all purpose flour", "cake flour");
        SYNONYMS.put("oil", "cooking oil");
        SYNONYMS.put("sunflower oil", "cooking oil");
        SYNONYMS.put("vegetable oil", "cooking oil");
        SYNONYMS.put("mince", "beef mince");
        SYNONYMS.put("minced beef", "beef mince");
        SYNONYMS.put("ground beef", "beef mince");
        SYNONYMS.put("cheddar", "cheese");
        SYNONYMS.put("cheddar cheese", "cheese");
        SYNONYMS.put("gouda", "cheese");
        SYNONYMS.put("gouda cheese", "cheese");
        SYNONYMS.put("bell pepper", "green pepper");
        SYNONYMS.put("sweet pepper", "green pepper");
        SYNONYMS.put("capsicum", "green pepper");
        SYNONYMS.put("green onion", "spring onion");
        SYNONYMS.put("scallion", "spring onion");
        SYNONYMS.put("chili", "chilli");
        SYNONYMS.put("cilantro", "coriander");
        SYNONYMS.put("bread", "bread slice");
        SYNONYMS.put("rolled oat", "oat");
        SYNONYMS.put("white sugar", "sugar");
        SYNONYMS.put("table salt", "salt");
        SYNONYMS.put("spaghetti", "pasta");
        SYNONYMS.put("penne", "pasta");
        SYNONYMS.put("creamed corn", "creamed sweetcorn");
    }

    // "tomato|COUNT" -> 5.0   (total amount in the pantry, in the base unit)
    private final Map<String, Double> stock = new HashMap<>();
    // cleaned names of pantry food that expires within the warning period
    private final Set<String> expiringNames = new HashSet<>();

    /**
     * Prepares the pantry once, so every recipe can then be checked quickly.
     *
     * @param pantry             everything currently in the pantry
     * @param expiringWithinDays food expiring within this many days counts as "use soon"
     */
    public IngredientMatcher(List<PantryItem> pantry, int expiringWithinDays) {
        for (PantryItem item : pantry) {
            String name = normaliseName(item.getName());
            double amount = UnitConverter.toBaseAmount(item.getQuantity(), item.getUnit());

            // Same food added twice? Add the amounts together.
            stock.merge(stockKey(name, item.getUnit()), amount, Double::sum);

            Long days = item.daysUntilExpiry();
            if (days != null && days >= 0 && days <= expiringWithinDays) {
                expiringNames.add(name);
            }
        }
    }

    /** Checks ONE recipe against the pantry using the strict rule. */
    public MatchResult check(Recipe recipe) {
        List<RecipeIngredient> missing = new ArrayList<>();
        boolean usesExpiringFood = false;

        for (RecipeIngredient needed : recipe.getIngredients()) {
            String name = normaliseName(needed.getName());
            double requiredAmount = UnitConverter.toBaseAmount(needed.getQuantity(), needed.getUnit());
            Double availableAmount = stock.get(stockKey(name, needed.getUnit()));

            if (availableAmount == null || availableAmount + 0.0001 < requiredAmount) {
                missing.add(needed);
            }
            if (expiringNames.contains(name)) {
                usesExpiringFood = true;
            }
        }
        return new MatchResult(recipe, missing, usesExpiringFood);
    }

    /** One key per ingredient AND kind of unit, so grams are never compared with pieces. */
    private static String stockKey(String normalisedName, String unit) {
        return normalisedName + "|" + UnitConverter.dimensionOf(unit);
    }

    /**
     * Cleans an ingredient name so small differences do not break matching:
     * "  Fresh Tomatoes " -> "tomato",  "Mielie-meal" -> "maize meal",  "Jalapeño" -> "jalapeno".
     */
    public static String normaliseName(String rawName) {
        if (rawName == null) {
            return "";
        }
        String text = Normalizer.normalize(rawName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")          // remove accents: é -> e
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z ]", " ");       // hyphens, digits, punctuation -> spaces

        StringBuilder cleaned = new StringBuilder();
        for (String word : text.trim().split("\\s+")) {
            if (word.isEmpty() || FILLER_WORDS.contains(word)) {
                continue;
            }
            if (cleaned.length() > 0) {
                cleaned.append(' ');
            }
            cleaned.append(singular(word));
        }

        String name = cleaned.toString();
        String synonym = SYNONYMS.get(name);
        return synonym != null ? synonym : name;
    }

    /** Simple English plural rules: tomatoes -> tomato, berries -> berry, eggs -> egg. */
    static String singular(String word) {
        String irregular = IRREGULAR_PLURALS.get(word);
        if (irregular != null) {
            return irregular;
        }
        if (word.length() <= 3) {
            return word;                                              // egg, oil, pea
        }
        if (word.endsWith("ies") && word.length() > 4) {
            return word.substring(0, word.length() - 3) + "y";        // berries -> berry
        }
        if (word.endsWith("oes")) {
            return word.substring(0, word.length() - 2);              // potatoes -> potato
        }
        if (word.endsWith("ches") || word.endsWith("shes")
                || word.endsWith("sses") || word.endsWith("xes")) {
            return word.substring(0, word.length() - 2);              // peaches -> peach
        }
        if (word.endsWith("s") && !word.endsWith("ss")
                && !word.endsWith("us") && !word.endsWith("is")) {
            return word.substring(0, word.length() - 1);              // onions -> onion
        }
        return word;
    }
}