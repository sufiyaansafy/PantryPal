package com.safy.pantrypal;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Small helper around SharedPreferences so every screen reads the settings the same way.
 *
 * These are personal display choices for this phone, not shared
 * data. The pantry and the recipes live in PostgreSQL (Supabase); these three little
 * switches belong on the device, which is exactly what SharedPreferences is for.
 *
 * The Settings screen (Day 4) writes them; the Pantry and Suggestions screens read them.
 */
public final class AppPrefs {

    private static final String FILE = "pantrypal_settings";
    private static final String KEY_ALERTS = "expiry_alerts";
    private static final String KEY_WARN_DAYS = "warn_days";
    private static final String KEY_ALMOST = "show_almost_there";

    private AppPrefs() {
        // Only static helper methods.
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    /** Should the app warn about food that is about to expire? (on by default) */
    public static boolean alertsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ALERTS, true);
    }

    public static void setAlertsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_ALERTS, enabled).apply();
    }

    /** How many days before the expiry date the warning starts (3 by default). */
    public static int warnDays(Context context) {
        return prefs(context).getInt(KEY_WARN_DAYS, 3);
    }

    public static void setWarnDays(Context context, int days) {
        prefs(context).edit().putInt(KEY_WARN_DAYS, days).apply();
    }

    /** Show the "Almost there" recipe list? (on by default, used from Day 3) */
    public static boolean showAlmostThere(Context context) {
        return prefs(context).getBoolean(KEY_ALMOST, true);
    }

    public static void setShowAlmostThere(Context context, boolean show) {
        prefs(context).edit().putBoolean(KEY_ALMOST, show).apply();
    }
}