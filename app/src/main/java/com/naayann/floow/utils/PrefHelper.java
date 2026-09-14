package com.naayann.floow.utils;

import android.content.Context;
import android.content.SharedPreferences;

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
                .putInt("age", age)
                .putString("goal", goal)
                .apply();
    }

    public String getName() { return sp.getString("name", "Friend"); }
    public int getAge() { return sp.getInt("age", 0); }
    public String getGoal() { return sp.getString("goal", ""); }

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
}
