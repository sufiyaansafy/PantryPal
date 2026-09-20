package com.safy.pantrypal;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Talks to the Supabase (PostgreSQL) database through its REST API.
 */
public final class SupabaseClient {

    /** How a screen receives the answer. Both methods are called on the main thread. */
    public interface ResultCallback<T> {
        void onSuccess(T result);

        void onError(String message);
    }

    /** Turns the JSON text sent back by Supabase into Java objects. */
    private interface Parser<T> {
        T parse(String body) throws JSONException;
    }

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static SupabaseClient instance;

    private final OkHttpClient http = new OkHttpClient();
    private final Handler mainThread = new Handler(Looper.getMainLooper());
    private final String restUrl;
    private final String apiKey;

    private SupabaseClient() {
        // Both values come from local.properties through BuildConfig (see app/build.gradle.kts).
        String baseUrl = BuildConfig.SUPABASE_URL.trim();
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        restUrl = baseUrl + "/rest/v1/";
        apiKey = BuildConfig.SUPABASE_KEY.trim();
    }

    /** One shared client for the whole app (singleton pattern). */
    public static synchronized SupabaseClient get() {
        if (instance == null) {
            instance = new SupabaseClient();
        }
        return instance;
    }

    // =========================================================================
    //  PANTRY ITEMS – full CRUD
    // =========================================================================

    /** READ – every pantry item, sorted A–Z by name. */
    public void getPantryItems(ResultCallback<List<PantryItem>> callback) {
        if (keysMissing(callback)) return;
        Request request = newRequest("pantry_items?select=*&order=name.asc").get().build();
        send(request, callback, body -> parsePantryItems(new JSONArray(body)));
    }

    /** CREATE – adds a new pantry item. */
    public void addPantryItem(PantryItem item, ResultCallback<Void> callback) {
        if (keysMissing(callback)) return;
        Request request = newRequest("pantry_items")
                .post(RequestBody.create(toJson(item), JSON))
                .build();
        send(request, callback, body -> null);
    }

    /** UPDATE – saves the new values of an existing item (found by its id). */
    public void updatePantryItem(PantryItem item, ResultCallback<Void> callback) {
        if (keysMissing(callback)) return;
        Request request = newRequest("pantry_items?id=eq." + item.getId())
                .patch(RequestBody.create(toJson(item), JSON))
                .build();
        send(request, callback, body -> null);
    }

    /** DELETE – removes the pantry item with this id. */
    public void deletePantryItem(long id, ResultCallback<Void> callback) {
        if (keysMissing(callback)) return;
        Request request = newRequest("pantry_items?id=eq." + id).delete().build();
        send(request, callback, body -> null);
    }

    // =========================================================================
    //  RECIPES – read only
    // =========================================================================

    /**
     * READ – every recipe together with its ingredients, in ONE request.
     */
    public void getRecipes(ResultCallback<List<Recipe>> callback) {
        if (keysMissing(callback)) return;
        Request request = newRequest("recipes?select=*,recipe_ingredients(*)"
                + "&order=name.asc&recipe_ingredients.order=name.asc").get().build();
        send(request, callback, body -> parseRecipes(new JSONArray(body)));
    }

    // =========================================================================
    //  Helpers
    // =========================================================================

    private Request.Builder newRequest(String path) {
        Request.Builder builder = new Request.Builder()
                .url(restUrl + path)
                .header("apikey", apiKey);
        if (apiKey.startsWith("eyJ")) {
            builder.header("Authorization", "Bearer " + apiKey);
        }
        return builder;
    }

    private <T> void send(Request request, ResultCallback<T> callback, Parser<T> parser) {
        http.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainThread.post(() -> callback.onError(
                        "Can't reach the database. Check your internet connection."));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (ResponseBody responseBody = response.body()) {
                    String body = responseBody == null ? "" : responseBody.string();
                    if (!response.isSuccessful()) {
                        String message = errorText(response.code(), body);
                        mainThread.post(() -> callback.onError(message));
                        return;
                    }
                    T result = parser.parse(body);
                    mainThread.post(() -> callback.onSuccess(result));
                } catch (Exception e) {
                    mainThread.post(() -> callback.onError("Could not read the data: " + e.getMessage()));
                }
            }
        });
    }

    private boolean keysMissing(ResultCallback<?> callback) {
        if (!restUrl.startsWith("https://") || apiKey.isEmpty()) {
            callback.onError("Supabase URL or key is missing. Add them to local.properties, then rebuild.");
            return true;
        }
        return false;
    }

    private static String errorText(int code, String body) {
        String detail = body;
        try {
            detail = new JSONObject(body).getString("message");
        } catch (JSONException ignored) {
        }
        return "Database error " + code + ": " + detail;
    }

    private static String toJson(PantryItem item) {
        try {
            JSONObject json = new JSONObject();
            json.put("name", item.getName());
            json.put("quantity", item.getQuantity());
            json.put("unit", item.getUnit());
            json.put("expiry_date", item.getExpiryDate() == null
                    ? JSONObject.NULL
                    : item.getExpiryDate().toString());
            return json.toString();
        } catch (JSONException e) {
            throw new IllegalArgumentException("Pantry item could not be turned into JSON", e);
        }
    }

    private static List<PantryItem> parsePantryItems(JSONArray rows) throws JSONException {
        List<PantryItem> items = new ArrayList<>();
        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.getJSONObject(i);
            LocalDate expiry = row.isNull("expiry_date")
                    ? null
                    : LocalDate.parse(row.getString("expiry_date"));
            items.add(new PantryItem(
                    row.getLong("id"),
                    row.getString("name"),
                    row.getDouble("quantity"),
                    row.getString("unit"),
                    expiry));
        }
        return items;
    }

    private static List<Recipe> parseRecipes(JSONArray rows) throws JSONException {
        List<Recipe> recipes = new ArrayList<>();
        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.getJSONObject(i);

            List<RecipeIngredient> ingredients = new ArrayList<>();
            JSONArray ingredientRows = row.getJSONArray("recipe_ingredients");
            for (int j = 0; j < ingredientRows.length(); j++) {
                JSONObject ingredient = ingredientRows.getJSONObject(j);
                ingredients.add(new RecipeIngredient(
                        ingredient.getString("name"),
                        ingredient.getDouble("quantity"),
                        ingredient.getString("unit")));
            }

            recipes.add(new Recipe(
                    row.getLong("id"),
                    row.getString("name"),
                    row.getString("description"),
                    row.getInt("minutes"),
                    row.getString("steps"),
                    ingredients));
        }
        return recipes;
    }
}