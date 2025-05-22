package com.example.finaltermproject;

import android.content.Context;
import android.content.SharedPreferences;

public class UserManager {
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USER_ID = "current_user_id";
    private static final String KEY_USERNAME = "current_username";

    public static void setCurrentUser(Context context, User user) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.apply();
    }

    public static User getCurrentUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int id = prefs.getInt(KEY_USER_ID, -1);
        String username = prefs.getString(KEY_USERNAME, null);
        if (id != -1 && username != null) {
            return new User(id, username);
        }
        return null;
    }

    public static void clearCurrentUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
