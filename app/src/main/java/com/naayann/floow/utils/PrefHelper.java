package com.naayann.floow.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.naayann.floow.R;

public class PrefHelper {
    private static final String PREF = "floow_pref";
    private final SharedPreferences sp;

    public PrefHelper(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void setOnboardingDone(boolean done) {
        sp.edit().putBoolean("onboarding_done", done).apply();
    }

    public boolean isOnboardingDone() {
        return sp.getBoolean("onboarding_done", false);
    }

    public void saveUser(String name, int age, String goal) {
        sp.edit()
                .putString("name", name)
                .putString("user_name", name)
                .putInt("age", age)
                .putString("goal", goal)
                .putString("user_goal", goal)
                .apply();
    }

    public String getName() {
        String n = sp.getString("user_name", null);
        if (n == null || n.trim().isEmpty()) {
            n = sp.getString("name", "Sandra Glam");
        }
        return n;
    }

    public int getAge() { return sp.getInt("age", 0); }

    public String getGoal() {
        String g = sp.getString("user_goal", null);
        if (g == null || g.trim().isEmpty()) {
            g = sp.getString("goal", "Become healthier and build discipline");
        }
        return g;
    }

    public void setMascotKey(String mascotKey) {
        sp.edit().putString("selected_mascot", mascotKey).apply();
    }

    public String getMascotKey() {
        return sp.getString("selected_mascot", "default");
    }

    public int getMascotDrawableRes() {
        return getMascotDrawableResForKey(getMascotKey());
    }

    public static int getMascotDrawableResForKey(String key) {
        if (key == null) return R.drawable.ic_mascot;
        switch (key) {
            case "hippo":
                return R.drawable.ic_hippo_mascot;
            case "froggy":
                return R.drawable.ic_froggy_mascot;
            case "piggy":
                return R.drawable.ic_piggy_mascot;
            case "owl":
                return R.drawable.ic_owl_mascot;
            default:
                return R.drawable.ic_mascot;
        }
    }

    public void setTutorialDone(boolean done) {
        sp.edit().putBoolean("tutorial_done", done).apply();
    }

    public boolean isTutorialDone() {
        return sp.getBoolean("tutorial_done", false);
    }

    public void setNotificationsEnabled(boolean enabled) {
        sp.edit().putBoolean("notifications_enabled", enabled).apply();
    }

    public boolean areNotificationsEnabled() {
        return sp.getBoolean("notifications_enabled", true);
    }

    public void setLastNotificationSlot(String slot) {
        sp.edit().putString("last_notification_slot", slot).apply();
    }

    public String getLastNotificationSlot() {
        return sp.getString("last_notification_slot", "");
    }
}