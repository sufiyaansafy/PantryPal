package com.safy.pantrypal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Each test builds a small pantry, checks a recipe, and asserts the answer.
 * These run on the computer (no emulator needed) in a couple of seconds.
 */
public class IngredientMatcherTest {

    // ---- helpers to build test data quickly ----

    private static PantryItem item(String name, double quantity, String unit) {
        return new PantryItem(0, name, quantity, unit, null);
    }

    private static RecipeIngredient needs(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }

    /** Scrambled Eggs exactly as it is in the seed data. */
    private static Recipe scrambledEggs() {
        return new Recipe(1, "Scrambled Eggs", "", 10, "Whisk.\nCook.", Arrays.asList(
                needs("eggs", 3, "pcs"),
                needs("milk", 50, "ml"),
                needs("butter", 10, "g")));
    }

    /** French Toast needs FIVE ingredients – used for the "4 out of 5" rule in the brief. */
    private static Recipe frenchToast() {
        return new Recipe(2, "French Toast", "", 15, "Dip.\nFry.", Arrays.asList(
                needs("bread slices", 4, "pcs"),
                needs("eggs", 2, "pcs"),
                needs("milk", 100, "ml"),
                needs("sugar", 10, "g"),
                needs("butter", 20, "g")));
    }

    // ---- the strict rule ----

    @Test
    public void recipeIsSuggested_whenEveryIngredientIsInThePantry() {
        List<PantryItem> pantry = Arrays.asList(
                item("Eggs", 6, "pcs"), item("Milk", 500, "ml"), item("Butter", 250, "g"));

        assertTrue(new IngredientMatcher(pantry, 3).check(scrambledEggs()).canCook());
    }

    @Test
    public void recipeIsNotSuggested_whenOnlyFourOfFiveIngredientsArePresent() {
        List<PantryItem> pantry = Arrays.asList(             // no sugar
                item("Bread slices", 10, "pcs"), item("Eggs", 6, "pcs"),
                item("Milk", 1, "L"), item("Butter", 250, "g"));

        IngredientMatcher.MatchResult result = new IngredientMatcher(pantry, 3).check(frenchToast());

        assertFalse(result.canCook());
        assertTrue(result.isAlmostThere());
        assertEquals("sugar", result.missing.get(0).getName());
    }

    @Test
    public void recipeIsNotSuggested_whenThereIsNotEnoughOfAnIngredient() {
        List<PantryItem> pantry = Arrays.asList(             // 2 eggs, recipe needs 3
                item("Eggs", 2, "pcs"), item("Milk", 500, "ml"), item("Butter", 250, "g"));

        IngredientMatcher.MatchResult result = new IngredientMatcher(pantry, 3).check(scrambledEggs());

        assertFalse(result.canCook());
        assertEquals("eggs", result.missing.get(0).getName());
    }

    @Test
    public void exactlyTheRequiredAmountIsEnough() {
        List<PantryItem> pantry = Arrays.asList(
                item("Eggs", 3, "pcs"), item("Milk", 50, "ml"), item("Butter", 10, "g"));

        assertTrue(new IngredientMatcher(pantry, 3).check(scrambledEggs()).canCook());
    }

    @Test
    public void emptyPantry_cannotCookAnything() {
        IngredientMatcher.MatchResult result =
                new IngredientMatcher(new ArrayList<>(), 3).check(scrambledEggs());

        assertFalse(result.canCook());
        assertEquals(3, result.missing.size());
    }

    // ---- real-world messiness ----

    @Test
    public void unitsAreConverted_kgToG_andLitresToMl() {
        List<PantryItem> pantry = Arrays.asList(             // 1 L milk, 0.25 kg butter
                item("Eggs", 6, "pcs"), item("Milk", 1, "L"), item("Butter", 0.25, "kg"));

        assertTrue(new IngredientMatcher(pantry, 3).check(scrambledEggs()).canCook());
    }

    @Test
    public void spoonsAreConvertedToMillilitres() {
        Recipe needsTwoTablespoons = new Recipe(3, "Test", "", 5, "Mix.",
                Arrays.asList(needs("cooking oil", 2, "tbsp")));               // = 30 ml

        IngredientMatcher enough = new IngredientMatcher(
                Arrays.asList(item("Cooking oil", 30, "ml")), 3);
        IngredientMatcher tooLittle = new IngredientMatcher(
                Arrays.asList(item("Cooking oil", 20, "ml")), 3);

        assertTrue(enough.check(needsTwoTablespoons).canCook());
        assertFalse(tooLittle.check(needsTwoTablespoons).canCook());
    }

    @Test
    public void differentKindsOfUnit_areNeverMixedUp() {
        List<PantryItem> pantry = Arrays.asList(             // butter in ml, recipe wants grams
                item("Eggs", 6, "pcs"), item("Milk", 1, "L"), item("Butter", 500, "ml"));

        assertFalse(new IngredientMatcher(pantry, 3).check(scrambledEggs()).canCook());
    }

    @Test
    public void pluralsCapitalsAndSpaces_areIgnored() {
        assertEquals("tomato", IngredientMatcher.normaliseName("  Tomatoes "));
        assertEquals("tomato", IngredientMatcher.normaliseName("tomato"));
        assertEquals("egg", IngredientMatcher.normaliseName("EGGS"));
        assertEquals("potato", IngredientMatcher.normaliseName("potatoes"));
        assertEquals("berry", IngredientMatcher.normaliseName("Berries"));
        assertEquals("garlic clove", IngredientMatcher.normaliseName("Garlic cloves"));
    }

    @Test
    public void describingWordsAndLocalNames_areUnderstood() {
        assertEquals("tomato", IngredientMatcher.normaliseName("Fresh ripe tomatoes"));
        assertEquals("maize meal", IngredientMatcher.normaliseName("Mielie meal"));
        assertEquals("maize meal", IngredientMatcher.normaliseName("Mielie-meal"));
        assertEquals("pea", IngredientMatcher.normaliseName("frozen peas"));
        assertEquals("cooking oil", IngredientMatcher.normaliseName("Sunflower oil"));
    }

    @Test
    public void sameFoodEnteredTwice_isAddedTogether() {
        List<PantryItem> pantry = Arrays.asList(             // 2 + 1 = 3 eggs
                item("Egg", 2, "pcs"), item("eggs", 1, "pcs"),
                item("Milk", 50, "ml"), item("Butter", 10, "g"));

        assertTrue(new IngredientMatcher(pantry, 3).check(scrambledEggs()).canCook());
    }

    @Test
    public void recipesUsingFoodThatExpiresSoon_areFlagged() {
        List<PantryItem> pantry = Arrays.asList(
                item("Eggs", 6, "pcs"), item("Butter", 250, "g"),
                new PantryItem(0, "Milk", 1, "L", LocalDate.now().plusDays(1)));   // expires tomorrow

        assertTrue(new IngredientMatcher(pantry, 3).check(scrambledEggs()).usesExpiringFood);
        assertFalse(new IngredientMatcher(pantry, 0).check(scrambledEggs()).usesExpiringFood);
    }
}