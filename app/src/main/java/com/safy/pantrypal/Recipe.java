package com.safy.pantrypal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * One recipe from the recipes table plus the ingredients it needs.
 * Serializable so the Suggestions screen can pass a whole recipe to the Detail screen.
 */
public class Recipe implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long id;
    private final String name;
    private final String description;
    private final int minutes;                              // total cooking time
    private final String steps;                             // method, one step per line
    private final ArrayList<RecipeIngredient> ingredients;

    public Recipe(long id, String name, String description, int minutes,
                  String steps, List<RecipeIngredient> ingredients) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.minutes = minutes;
        this.steps = steps;
        this.ingredients = new ArrayList<>(ingredients);   // keep our own copy of the list
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getMinutes() {
        return minutes;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    /** The method as separate steps (the database stores one step per line). */
    public List<String> getStepList() {
        List<String> list = new ArrayList<>();
        for (String step : steps.split("\n")) {
            if (!step.trim().isEmpty()) {
                list.add(step.trim());
            }
        }
        return list;
    }
}