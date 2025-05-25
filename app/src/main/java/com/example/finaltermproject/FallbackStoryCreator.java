package com.example.finaltermproject;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Helper class to create fallback story data when the main story file is unavailable
 */
public class FallbackStoryCreator {
    private static final String TAG = "FallbackStoryCreator";

    public static void createMinimalStory(SQLiteDatabase db) {
        Log.w(TAG, "Creating minimal fallback story");

        try {
            long currentTime = System.currentTimeMillis();

            // Create a simple 3-dialog story with choices
            // Dialog 1: Introduction
            db.execSQL("INSERT INTO dialogs (id, text, created_at) VALUES (?, ?, ?)",
                    new Object[]{1,
                            "Welcome to Path of Choices!\n\n" +
                                    "This is a temporary story created because the main story file could not be loaded.\n\n" +
                                    "You find yourself at a crossroads. What do you choose?",
                            currentTime});

            // Dialog 2: First choice outcome
            db.execSQL("INSERT INTO dialogs (id, text, created_at) VALUES (?, ?, ?)",
                    new Object[]{2,
                            "You have chosen the path of adventure!\n\n" +
                                    "The road ahead is filled with possibilities. Your journey continues...",
                            currentTime});

            // Dialog 3: Second choice outcome
            db.execSQL("INSERT INTO dialogs (id, text, created_at) VALUES (?, ?, ?)",
                    new Object[]{3,
                            "You have chosen the path of wisdom!\n\n" +
                                    "Knowledge illuminates your way forward. Your story unfolds...",
                            currentTime});

            // Dialog 4: Ending
            db.execSQL("INSERT INTO dialogs (id, text, created_at) VALUES (?, ?, ?)",
                    new Object[]{4,
                            "Thank you for experiencing this brief story!\n\n" +
                                    "To access the full adventure, please ensure the complete story file is properly installed.\n\n" +
                                    "Your journey awaits...",
                            currentTime});

            // Create choices for dialog 1
            db.execSQL("INSERT INTO choices (dialog_id, choice_text, next_dialog_id, created_at) VALUES (?, ?, ?, ?)",
                    new Object[]{1, "Choose the path of adventure", 2, currentTime});

            db.execSQL("INSERT INTO choices (dialog_id, choice_text, next_dialog_id, created_at) VALUES (?, ?, ?, ?)",
                    new Object[]{1, "Choose the path of wisdom", 3, currentTime});

            // Create choices for dialogs 2 and 3 leading to ending
            db.execSQL("INSERT INTO choices (dialog_id, choice_text, next_dialog_id, created_at) VALUES (?, ?, ?, ?)",
                    new Object[]{2, "Continue your adventure", 4, currentTime});

            db.execSQL("INSERT INTO choices (dialog_id, choice_text, next_dialog_id, created_at) VALUES (?, ?, ?, ?)",
                    new Object[]{3, "Continue seeking wisdom", 4, currentTime});

            Log.d(TAG, "Minimal fallback story created successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error creating minimal story", e);
            throw new RuntimeException("Failed to create fallback story", e);
        }
    }

    public static void createEmergencyStory(SQLiteDatabase db) {
        Log.w(TAG, "Creating emergency single-dialog story");

        try {
            long currentTime = System.currentTimeMillis();

            // Create absolute minimum: single dialog with no choices (ending)
            db.execSQL("INSERT INTO dialogs (id, text, created_at) VALUES (?, ?, ?)",
                    new Object[]{1,
                            "Welcome to Path of Choices!\n\n" +
                                    "Unfortunately, the story content could not be loaded at this time.\n\n" +
                                    "Please check that all game files are properly installed and try again.\n\n" +
                                    "Thank you for your patience!",
                            currentTime});

            Log.d(TAG, "Emergency story created successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error creating emergency story", e);
            throw new RuntimeException("Failed to create emergency story", e);
        }
    }

    public static boolean validateStoryStructure(SQLiteDatabase db) {
        try {
            // Check basic story requirements
            android.database.Cursor dialogCursor = db.rawQuery("SELECT COUNT(*) FROM dialogs", null);
            dialogCursor.moveToFirst();
            int dialogCount = dialogCursor.getInt(0);
            dialogCursor.close();

            if (dialogCount == 0) {
                Log.e(TAG, "No dialogs found in database");
                return false;
            }

            // Check if dialog 1 exists (starting point)
            android.database.Cursor startCursor = db.rawQuery("SELECT id FROM dialogs WHERE id = 1", null);
            boolean hasStart = startCursor.moveToFirst();
            startCursor.close();

            if (!hasStart) {
                Log.e(TAG, "Starting dialog (id=1) not found");
                return false;
            }

            Log.d(TAG, "Story structure validation passed: " + dialogCount + " dialogs found");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error validating story structure", e);
            return false;
        }
    }
}
