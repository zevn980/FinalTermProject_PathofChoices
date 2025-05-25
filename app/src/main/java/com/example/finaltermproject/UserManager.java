package com.example.finaltermproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.List;

public class UserManager {
    private static final String TAG = "UserManager";
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USER_ID = "current_user_id";
    private static final String KEY_USERNAME = "current_username";
    private static final String KEY_LAST_VALIDATION = "last_validation";
    private static final long VALIDATION_INTERVAL = 24 * 60 * 60 * 1000; // 24 hours

    public static void setCurrentUser(Context context, User user) {
        if (user == null) {
            clearCurrentUser(context);
            return;
        }

        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_USER_ID, user.getId());
            editor.putString(KEY_USERNAME, user.getUsername());
            editor.putLong(KEY_LAST_VALIDATION, System.currentTimeMillis());
            editor.apply();
            Log.d(TAG, "User set successfully: " + user.getUsername());
        } catch (Exception e) {
            Log.e(TAG, "Error setting current user", e);
        }
    }

    public static int getCurrentUserId(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            return prefs.getInt(KEY_USER_ID, -1);
        } catch (Exception e) {
            Log.e(TAG, "Error getting current user ID", e);
            return -1;
        }
    }

    public static User getCurrentUser(Context context) {
        if (context == null) {
            Log.e(TAG, "Context is null in getCurrentUser");
            return null;
        }

        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            int id = prefs.getInt(KEY_USER_ID, -1);
            String username = prefs.getString(KEY_USERNAME, null);
            long lastValidation = prefs.getLong(KEY_LAST_VALIDATION, 0);

            if (id == -1 || username == null) {
                Log.d(TAG, "No user found in preferences");
                return null;
            }

            // Check if validation is needed (periodically or on first access)
            boolean needsValidation = (System.currentTimeMillis() - lastValidation) > VALIDATION_INTERVAL;

            if (needsValidation) {
                Log.d(TAG, "Validating user against database");
                if (!validateUserInDatabase(context, id, username)) {
                    Log.w(TAG, "User validation failed, clearing preferences");
                    clearCurrentUser(context);
                    return null;
                }

                // Update validation timestamp
                prefs.edit().putLong(KEY_LAST_VALIDATION, System.currentTimeMillis()).apply();
            }

            return new User(id, username);
        } catch (Exception e) {
            Log.e(TAG, "Error getting current user", e);
            clearCurrentUser(context);
            return null;
        }
    }

    private static boolean validateUserInDatabase(Context context, int userId, String username) {
        try {
            DatabaseHelper db = DatabaseHelper.getInstance(context);
            List<User> users = db.getAllUsers();

            for (User user : users) {
                if (user.getId() == userId && user.getUsername().equals(username)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error validating user in database", e);
            return false; // Assume invalid on error to be safe
        }
    }

    public static void clearCurrentUser(Context context) {
        try {
            if (context != null) {
                SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                prefs.edit().clear().apply();
                Log.d(TAG, "Current user cleared");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing current user", e);
        }
    }

    public static boolean hasValidCurrentUser(Context context) {
        return getCurrentUser(context) != null;
    }

    public static void refreshUserValidation(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putLong(KEY_LAST_VALIDATION, 0).apply(); // Force validation on next access
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing user validation", e);
        }
    }
}