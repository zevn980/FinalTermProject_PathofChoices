package com.example.finaltermproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;
import com.example.finaltermproject.User;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DB_NAME = "game.db";
    private static final int DB_VERSION = 6;  // Updated to match existing database version

    // Database creation SQL statements
    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL UNIQUE);";
    private static final String CREATE_PROGRESS_TABLE =
            "CREATE TABLE progress (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, current_dialog_id INTEGER NOT NULL, " +
                    "last_updated INTEGER DEFAULT 0, " +
                    "UNIQUE(user_id), " +
                    "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(current_dialog_id) REFERENCES dialogs(id) ON DELETE RESTRICT);";
    private static final String CREATE_DIALOGS_TABLE =
            "CREATE TABLE dialogs (id INTEGER PRIMARY KEY AUTOINCREMENT, text TEXT NOT NULL, " +
                    "created_at INTEGER DEFAULT 0);";
    private static final String CREATE_CHOICES_TABLE =
            "CREATE TABLE choices (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "dialog_id INTEGER NOT NULL, " +
                    "choice_text TEXT NOT NULL, " +
                    "next_dialog_id INTEGER NOT NULL, " +
                    "created_at INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(dialog_id) REFERENCES dialogs(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(next_dialog_id) REFERENCES dialogs(id) ON DELETE RESTRICT);";

    // Add indexes for better performance
    private static final String[] CREATE_INDEXES = {
            "CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);",
            "CREATE INDEX IF NOT EXISTS idx_progress_user_id ON progress(user_id);",
            "CREATE INDEX IF NOT EXISTS idx_progress_dialog_id ON progress(current_dialog_id);",
            "CREATE INDEX IF NOT EXISTS idx_choices_dialog_id ON choices(dialog_id);",
            "CREATE INDEX IF NOT EXISTS idx_choices_next_dialog_id ON choices(next_dialog_id);"
    };

    // Version 2 upgrade statements
    private static final String[] VERSION_2_UPGRADES = {
            "ALTER TABLE progress ADD COLUMN last_updated INTEGER DEFAULT 0;",
            "ALTER TABLE dialogs ADD COLUMN created_at INTEGER DEFAULT 0;",
            "ALTER TABLE choices ADD COLUMN created_at INTEGER DEFAULT 0;"
    };

    // Version 3 upgrade statements
    private static final String[] VERSION_3_UPGRADES = {
        "CREATE INDEX IF NOT EXISTS idx_progress_last_updated ON progress(last_updated);",
        "CREATE INDEX IF NOT EXISTS idx_dialogs_created_at ON dialogs(created_at);",
        "CREATE INDEX IF NOT EXISTS idx_choices_created_at ON choices(created_at);"
    };

    // Singleton implementation using atomic reference
    private static final AtomicReference<DatabaseHelper> instance = new AtomicReference<>();
    private final Context applicationContext;  // Store application context instead of WeakReference

    public static synchronized DatabaseHelper getInstance(@NonNull Context context) {
        DatabaseHelper current = instance.get();
        if (current == null) {
            current = new DatabaseHelper(context.getApplicationContext());
            if (!instance.compareAndSet(null, current)) {
                current = instance.get();
            }
        }
        return current;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.applicationContext = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            Log.d(TAG, "Starting database creation...");

            // Enable foreign key support first
            db.execSQL("PRAGMA foreign_keys = ON;");
            Log.d(TAG, "Foreign key support enabled");

            // Create tables in correct order
            db.execSQL(CREATE_USERS_TABLE);
            Log.d(TAG, "Users table created");
            
            db.execSQL(CREATE_DIALOGS_TABLE);
            Log.d(TAG, "Dialogs table created");
            
            db.execSQL(CREATE_PROGRESS_TABLE);
            Log.d(TAG, "Progress table created");
            
            db.execSQL(CREATE_CHOICES_TABLE);
            Log.d(TAG, "Choices table created");

            // Create indexes
            for (String index : CREATE_INDEXES) {
                db.execSQL(index);
            }
            Log.d(TAG, "All indexes created");

            // Create initial dialog and choices
            long currentTime = System.currentTimeMillis();
            
            try {
                // Create initial dialog
                Log.d(TAG, "Creating initial dialog...");
                ContentValues dialogValues = new ContentValues();
                dialogValues.put("id", 1);
                dialogValues.put("text", "The silver rain falls softly as you arrive at the Grand Hall of Echoes. Lord Viren stands beneath the statue of the First Arbiter.\n\n\"Traveler,\" he says, voice deep as thunder in a lake, \"the Aetherial Scale has faltered. The prophecy has begun to stir.\"\n\nHe shows you a fragment of an ancient tablet:\n\n\"When the last lie is drowned, the scale shall tip. Four trials, one truth, and a price no nation can bear.\"");
                dialogValues.put("created_at", currentTime);
                long dialogId = db.insert("dialogs", null, dialogValues);
                Log.d(TAG, "Initial dialog created with ID: " + dialogId);

                // Create initial choices
                Log.d(TAG, "Creating choices...");
                ContentValues choice1 = new ContentValues();
                choice1.put("dialog_id", 1);
                choice1.put("choice_text", "Swear your aid to Viren and help investigate the prophecy");
                choice1.put("next_dialog_id", 11);
                choice1.put("created_at", currentTime);
                long choiceId1 = db.insert("choices", null, choice1);
                Log.d(TAG, "First choice created with ID: " + choiceId1);

                ContentValues choice2 = new ContentValues();
                choice2.put("dialog_id", 1);
                choice2.put("choice_text", "Slip away to the coast and seek the source of the water's unrest");
                choice2.put("next_dialog_id", 12);
                choice2.put("created_at", currentTime);
                long choiceId2 = db.insert("choices", null, choice2);
                Log.d(TAG, "Second choice created with ID: " + choiceId2);

                ContentValues choice3 = new ContentValues();
                choice3.put("dialog_id", 1);
                choice3.put("choice_text", "Attend Lady Selene's public trial, hoping to read her intentions");
                choice3.put("next_dialog_id", 13);
                choice3.put("created_at", currentTime);
                long choiceId3 = db.insert("choices", null, choice3);
                Log.d(TAG, "Third choice created with ID: " + choiceId3);

                ContentValues choice4 = new ContentValues();
                choice4.put("dialog_id", 1);
                choice4.put("choice_text", "Follow rumors of the Maskbearer in the eastern district");
                choice4.put("next_dialog_id", 14);
                choice4.put("created_at", currentTime);
                long choiceId4 = db.insert("choices", null, choice4);
                Log.d(TAG, "Fourth choice created with ID: " + choiceId4);

                ContentValues choice5 = new ContentValues();
                choice5.put("dialog_id", 1);
                choice5.put("choice_text", "Seek the Lustrines directly and ask them about the prophecy");
                choice5.put("next_dialog_id", 15);
                choice5.put("created_at", currentTime);
                long choiceId5 = db.insert("choices", null, choice5);
                Log.d(TAG, "Fifth choice created with ID: " + choiceId5);

                // Create subsequent dialogs
                ContentValues dialog11 = new ContentValues();
                dialog11.put("id", 11);
                dialog11.put("text", "You and Viren study ancient codes. The prophecy wasn't part of Caelondria's divine law—it predates it.\n\nLord Viren's gaze sharpens. He nods, then turns to the towering Aetherial Scale. \"Then we begin,\" he says, and hands you the fragment.\n\nBy torchlight in the vaults below the Grand Hall, ancient scrolls whisper truths. You decipher glyphs that reference a \"Lost Cycle\"—a time when judgment was not bound to performance, but to memory.");
                dialog11.put("created_at", currentTime);
                db.insert("dialogs", null, dialog11);

                ContentValues dialog12 = new ContentValues();
                dialog12.put("id", 12);
                dialog12.put("text", "You leave the city in secret, boots splashing through rivulets as silver rain turns to mist. The coast groans beneath a luminous tide. The water sings to you, not in words—but in grief.\n\nA haunting vision grips you: Caelondria swallowed not by sea, but by silence.");
                dialog12.put("created_at", currentTime);
                db.insert("dialogs", null, dialog12);

                ContentValues dialog13 = new ContentValues();
                dialog13.put("id", 13);
                dialog13.put("text", "The marble amphitheater thrums with expectation. Lady Selene, draped in liquid silk, performs the accused's soliloquy. Her voice dances like wind over water, but when her eyes meet yours—they tremble.\n\nHer expression shifts: a coded plea for aid behind her act.");
                dialog13.put("created_at", currentTime);
                db.insert("dialogs", null, dialog13);

                ContentValues dialog14 = new ContentValues();
                dialog14.put("id", 14);
                dialog14.put("text", "In the veiled alleyways of the Eastern District, steam rises from grates like breath from a sleeping beast. You follow whispers of the Maskbearer's presence, each step drawing you deeper into the shadows of Caelondria's underbelly.");
                dialog14.put("created_at", currentTime);
                db.insert("dialogs", null, dialog14);

                ContentValues dialog15 = new ContentValues();
                dialog15.put("id", 15);
                dialog15.put("text", "The Lustrines' sanctuary glows with an ethereal blue light. These ancient water beings hover before you, their forms rippling like liquid crystal. Their voices resonate in your mind, a chorus of memories stretching back to Caelondria's founding.");
                dialog15.put("created_at", currentTime);
                db.insert("dialogs", null, dialog15);

                // Add choices for dialog 11
                ContentValues choice11_1 = new ContentValues();
                choice11_1.put("dialog_id", 11);
                choice11_1.put("choice_text", "Confront the Scale itself by requesting an Audience of Judgment");
                choice11_1.put("next_dialog_id", 111);
                choice11_1.put("created_at", currentTime);
                db.insert("choices", null, choice11_1);

                ContentValues choice11_2 = new ContentValues();
                choice11_2.put("dialog_id", 11);
                choice11_2.put("choice_text", "Seek the Lustrines for their memory of Caelondria before the courts");
                choice11_2.put("next_dialog_id", 112);
                choice11_2.put("created_at", currentTime);
                db.insert("choices", null, choice11_2);

                ContentValues choice11_3 = new ContentValues();
                choice11_3.put("dialog_id", 11);
                choice11_3.put("choice_text", "Attempt to modify the prophecy secretly");
                choice11_3.put("next_dialog_id", 113);
                choice11_3.put("created_at", currentTime);
                db.insert("choices", null, choice11_3);

                // Add choices for dialog 12
                ContentValues choice12_1 = new ContentValues();
                choice12_1.put("dialog_id", 12);
                choice12_1.put("choice_text", "Dive deeper, risking your breath, to find the source");
                choice12_1.put("next_dialog_id", 121);
                choice12_1.put("created_at", currentTime);
                db.insert("choices", null, choice12_1);

                ContentValues choice12_2 = new ContentValues();
                choice12_2.put("dialog_id", 12);
                choice12_2.put("choice_text", "Capture and analyze the harmonic energy in the water");
                choice12_2.put("next_dialog_id", 122);
                choice12_2.put("created_at", currentTime);
                db.insert("choices", null, choice12_2);

                ContentValues choice12_3 = new ContentValues();
                choice12_3.put("dialog_id", 12);
                choice12_3.put("choice_text", "Record and release the vision to the public");
                choice12_3.put("next_dialog_id", 123);
                choice12_3.put("created_at", currentTime);
                db.insert("choices", null, choice12_3);

                // Add choices for dialog 13
                ContentValues choice13_1 = new ContentValues();
                choice13_1.put("dialog_id", 13);
                choice13_1.put("choice_text", "Signal your support subtly during her performance");
                choice13_1.put("next_dialog_id", 131);
                choice13_1.put("created_at", currentTime);
                db.insert("choices", null, choice13_1);

                ContentValues choice13_2 = new ContentValues();
                choice13_2.put("dialog_id", 13);
                choice13_2.put("choice_text", "Study the judges' reactions to her performance");
                choice13_2.put("next_dialog_id", 132);
                choice13_2.put("created_at", currentTime);
                db.insert("choices", null, choice13_2);

                ContentValues choice13_3 = new ContentValues();
                choice13_3.put("dialog_id", 13);
                choice13_3.put("choice_text", "Investigate her family's history during the trial");
                choice13_3.put("next_dialog_id", 133);
                choice13_3.put("created_at", currentTime);
                db.insert("choices", null, choice13_3);

                // Add choices for dialog 14 and 15
                ContentValues choice14_1_story_branch = new ContentValues();
                choice14_1_story_branch.put("dialog_id", 14);
                choice14_1_story_branch.put("choice_text", "Follow the trail of discarded masks");
                choice14_1_story_branch.put("next_dialog_id", 141);
                choice14_1_story_branch.put("created_at", currentTime);
                db.insert("choices", null, choice14_1_story_branch);

                ContentValues choice15_1_story_branch = new ContentValues();
                choice15_1_story_branch.put("dialog_id", 15);
                choice15_1_story_branch.put("choice_text", "Ask about the prophecy's origin");
                choice15_1_story_branch.put("next_dialog_id", 151);
                choice15_1_story_branch.put("created_at", currentTime);
                db.insert("choices", null, choice15_1_story_branch);

                // Create story branch dialogs
                ContentValues dialog111_story_branch = new ContentValues();
                dialog111_story_branch.put("id", 111);
                dialog111_story_branch.put("text", "The Aetherial Scale looms before you, its mechanisms pulsing with ancient power...");
                dialog111_story_branch.put("created_at", currentTime);
                db.insert("dialogs", null, dialog111_story_branch);

                ContentValues dialog112_story_branch = new ContentValues();
                dialog112_story_branch.put("id", 112);
                dialog112_story_branch.put("text", "The Lustrines' chamber is a cascade of liquid memories...");
                dialog112_story_branch.put("created_at", currentTime);
                db.insert("dialogs", null, dialog112_story_branch);

                ContentValues dialog113_story_branch = new ContentValues();
                dialog113_story_branch.put("id", 113);
                dialog113_story_branch.put("text", "In the depths of the archives, you find a way to alter the prophecy's text...");
                dialog113_story_branch.put("created_at", currentTime);
                db.insert("dialogs", null, dialog113_story_branch);

                ContentValues dialog121_story_branch = new ContentValues();
                dialog121_story_branch.put("id", 121);
                dialog121_story_branch.put("text", "The depths welcome you with an eerie luminescence...");
                dialog121_story_branch.put("created_at", currentTime);
                db.insert("dialogs", null, dialog121_story_branch);

                ContentValues dialog122_story_branch = new ContentValues();
                dialog122_story_branch.put("id", 122);
                dialog122_story_branch.put("text", "Your instruments detect a complex pattern in the water's resonance...");
                dialog122_story_branch.put("created_at", currentTime);
                db.insert("dialogs", null, dialog122_story_branch);

                ContentValues dialog123_story_branch = new ContentValues();
                dialog123_story_branch.put("id", 123);
                dialog123_story_branch.put("text", "The public reacts with a mix of fear and fascination to your vision...");
                dialog123_story_branch.put("created_at", currentTime);
                db.insert("dialogs", null, dialog123_story_branch);

                ContentValues dialog131_story_branch = new ContentValues();
                dialog131_story_branch.put("id", 131);
                dialog131_story_branch.put("text", "Your subtle gestures catch Lady Selene's eye...");
                dialog131_story_branch.put("created_at", currentTime);
                db.insert("dialogs", null, dialog131_story_branch);

                ContentValues dialog132_story_branch = new ContentValues();
                dialog132_story_branch.put("id", 132);
                dialog132_story_branch.put("text", "The judges' faces betray more than they know...");
                dialog132_story_branch.put("created_at", currentTime);
                db.insert("dialogs", null, dialog132_story_branch);

                ContentValues dialog133_story_branch = new ContentValues();
                dialog133_story_branch.put("id", 133);
                dialog133_story_branch.put("text", "Deep in the court archives, you uncover records of Lady Selene's lineage...");
                dialog133_story_branch.put("created_at", currentTime);
                db.insert("dialogs", null, dialog133_story_branch);

                ContentValues dialog141_story_branch = new ContentValues();
                dialog141_story_branch.put("id", 141);
                dialog141_story_branch.put("text", "The discarded masks form a trail through the city's shadows...");
                dialog141_story_branch.put("created_at", currentTime);
                db.insert("dialogs", null, dialog141_story_branch);

                ContentValues dialog142_story_branch = new ContentValues();
                dialog142_story_branch.put("id", 142);
                dialog142_story_branch.put("text", "The merchants speak in whispers of a figure who moves between worlds...");
                dialog142_story_branch.put("created_at", currentTime);
                db.insert("dialogs", null, dialog142_story_branch);

                ContentValues dialog143_story_branch = new ContentValues();
                dialog143_story_branch.put("id", 143);
                dialog143_story_branch.put("text", "Your surveillance network captures glimpses of impossible things...");
                dialog143_story_branch.put("created_at", currentTime);
                db.insert("dialogs", null, dialog143_story_branch);

                ContentValues dialog151_story_branch = new ContentValues();
                dialog151_story_branch.put("id", 151);
                dialog151_story_branch.put("text", "The Lustrines share visions of the prophecy's birth...");
                dialog151_story_branch.put("created_at", currentTime);
                db.insert("dialogs", null, dialog151_story_branch);

                ContentValues dialog152_story_branch = new ContentValues();
                dialog152_story_branch.put("id", 152);
                dialog152_story_branch.put("text", "Ancient memories flood your consciousness...");
                dialog152_story_branch.put("created_at", currentTime);
                db.insert("dialogs", null, dialog152_story_branch);

                ContentValues dialog153_story_branch = new ContentValues();
                dialog153_story_branch.put("id", 153);
                dialog153_story_branch.put("text", "The truth of the Scale's connection to the Lustrines is revealed...");
                dialog153_story_branch.put("created_at", currentTime);
                db.insert("dialogs", null, dialog153_story_branch);

                // Add choices for dialog 151
                ContentValues choice151_1_story_branch = new ContentValues();
                choice151_1_story_branch.put("dialog_id", 151);
                choice151_1_story_branch.put("choice_text", "Commune with the water's memory");
                choice151_1_story_branch.put("next_dialog_id", 301);
                choice151_1_story_branch.put("created_at", currentTime);
                db.insert("choices", null, choice151_1_story_branch);

                ContentValues choice151_2_story_branch = new ContentValues();
                choice151_2_story_branch.put("dialog_id", 151);
                choice151_2_story_branch.put("choice_text", "Search for signs of the prophecy's fulfillment");
                choice151_2_story_branch.put("next_dialog_id", 302);
                choice151_2_story_branch.put("created_at", currentTime);
                db.insert("choices", null, choice151_2_story_branch);

                // Add choices for dialog 152
                ContentValues choice152_1_story_branch = new ContentValues();
                choice152_1_story_branch.put("dialog_id", 152);
                choice152_1_story_branch.put("choice_text", "Explore the original form of justice");
                choice152_1_story_branch.put("next_dialog_id", 301);
                choice152_1_story_branch.put("created_at", currentTime);
                db.insert("choices", null, choice152_1_story_branch);

                ContentValues choice152_2_story_branch = new ContentValues();
                choice152_2_story_branch.put("dialog_id", 152);
                choice152_2_story_branch.put("choice_text", "Investigate the nature of the compromise");
                choice152_2_story_branch.put("next_dialog_id", 302);
                choice152_2_story_branch.put("created_at", currentTime);
                db.insert("choices", null, choice152_2_story_branch);

                // Add choices for dialog 153
                ContentValues choice153_1_story_branch = new ContentValues();
                choice153_1_story_branch.put("dialog_id", 153);
                choice153_1_story_branch.put("choice_text", "Seek to unite the two powers");
                choice153_1_story_branch.put("next_dialog_id", 301);
                choice153_1_story_branch.put("created_at", currentTime);
                db.insert("choices", null, choice153_1_story_branch);

                ContentValues choice153_2_story_branch = new ContentValues();
                choice153_2_story_branch.put("dialog_id", 153);
                choice153_2_story_branch.put("choice_text", "Calculate the cost of change");
                choice153_2_story_branch.put("next_dialog_id", 302);
                choice153_2_story_branch.put("created_at", currentTime);
                db.insert("choices", null, choice153_2_story_branch);

                // Add choices for convergence points
                for (int convergenceDialogId : new int[]{111, 112, 113, 121, 122, 123, 131, 132, 133, 141, 142, 143, 151, 152, 153}) {
                    ContentValues convergenceChoice1 = new ContentValues();
                    convergenceChoice1.put("dialog_id", convergenceDialogId);
                    convergenceChoice1.put("choice_text", "Accept the truth and embrace change");
                    convergenceChoice1.put("next_dialog_id", 301);
                    convergenceChoice1.put("created_at", currentTime);
                    db.insert("choices", null, convergenceChoice1);

                    ContentValues convergenceChoice2 = new ContentValues();
                    convergenceChoice2.put("dialog_id", convergenceDialogId);
                    convergenceChoice2.put("choice_text", "Reject the revelation and maintain order");
                    convergenceChoice2.put("next_dialog_id", 302);
                    convergenceChoice2.put("created_at", currentTime);
                    db.insert("choices", null, convergenceChoice2);
                }

                // Add choices for final convergence points
                for (int finalDialogId : new int[]{501, 502, 503, 504}) {
                    ContentValues finalChoice1 = new ContentValues();
                    finalChoice1.put("dialog_id", finalDialogId);
                    finalChoice1.put("choice_text", "Return to the beginning");
                    finalChoice1.put("next_dialog_id", 1);
                    finalChoice1.put("created_at", currentTime);
                    db.insert("choices", null, finalChoice1);
                }

                // Create final convergence dialogs
                ContentValues dialog301_final = new ContentValues();
                dialog301_final.put("id", 301);
                dialog301_final.put("text", "The city is in chaos. Your actions have led to this moment of crisis. The waters rise, the Scale trembles, and the truth of Caelondria's past threatens to break free.");
                dialog301_final.put("created_at", currentTime);
                db.insert("dialogs", null, dialog301_final);

                ContentValues dialog302_final = new ContentValues();
                dialog302_final.put("id", 302);
                dialog302_final.put("text", "Time itself bends around you. Multiple realities converge at this point, each offering a different version of justice, a different path for Caelondria's future.");
                dialog302_final.put("created_at", currentTime);
                db.insert("dialogs", null, dialog302_final);

                // Create ending dialogs
                ContentValues dialog501_ending = new ContentValues();
                dialog501_ending.put("id", 501);
                dialog501_ending.put("text", "**Ending: The Sovereign of Rain**\n\nYou have become the Sovereign of Rain, a being of immense power and responsibility. The world bows to your justice, and the waters of change flow according to your will. Caelondria enters a new age of harmony between the rigid and the fluid, the measured and the free.");
                dialog501_ending.put("created_at", currentTime);
                db.insert("dialogs", null, dialog501_ending);

                ContentValues dialog502_ending = new ContentValues();
                dialog502_ending.put("id", 502);

                // Ensure all convergence dialogs have choices
                ensureChoicesForConvergenceDialogs(db);

                // Validate the story structure
                validateStoryStructure();

                db.setTransactionSuccessful();
                Log.d(TAG, "Database creation completed successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error creating initial data: " + e.getMessage(), e);
                throw e;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating database: " + e.getMessage(), e);
            throw new RuntimeException("Database creation failed", e);
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.beginTransaction();
        try {
            // Apply version upgrades sequentially
            if (oldVersion < 2) {
                for (String upgrade : VERSION_2_UPGRADES) {
                    db.execSQL(upgrade);
                }
            }
            if (oldVersion < 3) {
                for (String upgrade : VERSION_3_UPGRADES) {
                    db.execSQL(upgrade);
                }
            }
            // Add future version upgrades here

            // Ensure all convergence dialogs have choices
            ensureChoicesForConvergenceDialogs(db);

            // Validate the story structure
            validateStoryStructure();

            db.setTransactionSuccessful();
            Log.d(TAG, "Database upgrade completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database", e);
            throw new RuntimeException("Database upgrade failed", e);
        } finally {
            db.endTransaction();
        }
    }

    public void renameUser(int id, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Check if the new name already exists for a different user
            Cursor cursor = db.rawQuery(
                    "SELECT 1 FROM users WHERE username = ? AND id != ?",
                    new String[]{newName, String.valueOf(id)}
            );

            boolean nameExists = cursor.moveToFirst();
            cursor.close();

            if (nameExists) {
                throw new IllegalStateException("Username already exists: " + newName);
            }

            ContentValues values = new ContentValues();
            values.put("username", newName.trim());
            int rowsAffected = db.update("users", values, "id = ?",
                    new String[]{String.valueOf(id)});

            if (rowsAffected == 0) {
                throw new IllegalStateException("User not found with id: " + id);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteUser(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // First check if user exists
            Cursor cursor = db.rawQuery("SELECT 1 FROM users WHERE id = ?",
                    new String[]{String.valueOf(id)});
            boolean exists = cursor.moveToFirst();
            cursor.close();

            if (!exists) {
                throw new IllegalStateException("User not found with id: " + id);
            }

            // Progress will be automatically deleted due to ON DELETE CASCADE
            db.delete("users", "id = ?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public synchronized long addUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        String trimmedUsername = username.trim();

        // Validate username format
        if (trimmedUsername.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long");
        }
        if (trimmedUsername.length() > 30) {
            throw new IllegalArgumentException("Username cannot be longer than 30 characters");
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Check if username already exists
            Cursor cursor = db.query("users", new String[]{"id"}, "username = ?",
                    new String[]{trimmedUsername}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                cursor.close();
                throw new IllegalStateException("Username already exists: " + trimmedUsername);
            }
            if (cursor != null) {
                cursor.close();
            }

            ContentValues values = new ContentValues();
            values.put("username", trimmedUsername);

            long userId = db.insertOrThrow("users", null, values);

            // Initialize progress for new user with first dialog
            if (userId != -1) {
                // Find the first available dialog
                Cursor dialogCursor = db.rawQuery("SELECT MIN(id) FROM dialogs", null);
                int firstDialogId = 1; // Default
                if (dialogCursor.moveToFirst() && !dialogCursor.isNull(0)) {
                    firstDialogId = dialogCursor.getInt(0);
                }
                dialogCursor.close();

                values = new ContentValues();
                values.put("user_id", userId);
                values.put("current_dialog_id", firstDialogId);
                values.put("last_updated", System.currentTimeMillis());
                db.insertOrThrow("progress", null, values);

                Log.d(TAG, "User created successfully: " + trimmedUsername + " (ID: " + userId + ")");
            }

            db.setTransactionSuccessful();
            return userId;
        } catch (Exception e) {
            Log.e(TAG, "Error adding user: " + trimmedUsername, e);
            throw e; // Re-throw to maintain existing error handling
        } finally {
            db.endTransaction();
        }
    }

    private void logDatabaseError(String operation, Exception e) {
        Log.e(TAG, String.format("Database error during %s: %s", operation, e.getMessage()), e);
        if (e instanceof android.database.sqlite.SQLiteException) {
            Log.e(TAG, "SQLite error occurred during " + operation);
        }
    }

    private void logDatabaseOperation(String operation, String details) {
        // Debug logging for database operations
        Log.d(TAG, String.format("Database operation - %s: %s", operation, details));
    }

    public void updateUserProgress(int userId, int newDialogId) {
        logDatabaseOperation("updateUserProgress", "userId=" + userId + ", newDialogId=" + newDialogId);

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Verify user exists first
            Cursor userCheck = db.rawQuery("SELECT 1 FROM users WHERE id = ?",
                    new String[]{String.valueOf(userId)});
            boolean userExists = userCheck.moveToFirst();
            userCheck.close();

            if (!userExists) {
                throw new IllegalArgumentException("User does not exist: " + userId);
            }

            // Verify dialog exists, with fallback handling
            Cursor dialogCheck = db.rawQuery("SELECT 1 FROM dialogs WHERE id = ?",
                    new String[]{String.valueOf(newDialogId)});
            boolean dialogExists = dialogCheck.moveToFirst();
            dialogCheck.close();

            if (!dialogExists) {
                Log.w(TAG, "Dialog " + newDialogId + " does not exist, finding fallback");

                // Try to find the closest valid dialog
                Cursor nextValidDialog = db.rawQuery(
                        "SELECT id FROM dialogs WHERE id >= ? ORDER BY id ASC LIMIT 1",
                        new String[]{String.valueOf(newDialogId)}
                );

                if (nextValidDialog.moveToFirst()) {
                    newDialogId = nextValidDialog.getInt(0);
                    Log.w(TAG, "Using fallback dialog: " + newDialogId);
                } else {
                    // If no dialog found at or after the requested ID, try from the beginning
                    Cursor firstDialog = db.rawQuery("SELECT MIN(id) FROM dialogs", null);
                    if (firstDialog.moveToFirst() && !firstDialog.isNull(0)) {
                        newDialogId = firstDialog.getInt(0);
                        Log.w(TAG, "Using first available dialog: " + newDialogId);
                    } else {
                        throw new RuntimeException("No dialogs available in database");
                    }
                    firstDialog.close();
                }
                nextValidDialog.close();
            }

            ContentValues values = new ContentValues();
            values.put("current_dialog_id", newDialogId);
            values.put("last_updated", System.currentTimeMillis());

            int rowsAffected = db.update("progress", values, "user_id = ?",
                    new String[]{String.valueOf(userId)});

            if (rowsAffected == 0) {
                // If no progress record exists, create one
                values.put("user_id", userId);
                db.insertOrThrow("progress", null, values);
                logDatabaseOperation("updateUserProgress", "Created new progress record");
                } else {
                logDatabaseOperation("updateUserProgress", "Updated existing progress record");
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            logDatabaseError("updateUserProgress", e);
            throw new RuntimeException("Failed to update user progress", e);
        } finally {
            db.endTransaction();
        }
    }

    @Nullable
    public DialogEntry getDialogById(int dialogId) {
        logDatabaseOperation("getDialogById", "dialogId=" + dialogId);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT id, text, created_at FROM dialogs WHERE id = ?",
                    new String[]{String.valueOf(dialogId)}
            );

            if (cursor.moveToFirst()) {
                DialogEntry dialog = new DialogEntry(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("text"))
                );
                logDatabaseOperation("getDialogById", "Found dialog: " + dialog.getId());
                return dialog;
            }

            Log.w(TAG, "Dialog not found: " + dialogId);
            return null;
        } catch (Exception e) {
            logDatabaseError("getDialogById", e);
            return null; // Return null instead of throwing exception
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    private void createFallbackStoryData(SQLiteDatabase db) {
        Log.w(TAG, "Creating fallback story data");
        try {
            // Create minimal story structure
            db.execSQL("INSERT INTO dialogs (id, text, created_at) VALUES (1, 'Welcome to the story. This is a fallback dialog created when the main story file could not be loaded.', ?)",
                    new String[]{String.valueOf(System.currentTimeMillis())});

            db.execSQL("INSERT INTO choices (dialog_id, choice_text, next_dialog_id, created_at) VALUES (1, 'Start the adventure', 2, ?)",
                    new String[]{String.valueOf(System.currentTimeMillis())});

            db.execSQL("INSERT INTO dialogs (id, text, created_at) VALUES (2, 'Thank you for playing. The full story will be available once the story file is properly loaded.', ?)",
                    new String[]{String.valueOf(System.currentTimeMillis())});

            Log.d(TAG, "Fallback story data created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating fallback story data", e);
            throw new RuntimeException("Failed to create fallback story data", e);
        }
    }

    private boolean isAssetAvailable(Context context, String filename) {
        if (context == null || filename == null) return false;

        try {
            String[] assets = context.getAssets().list("");
            if (assets != null) {
                for (String asset : assets) {
                    if (asset.equals(filename)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking asset availability: " + filename, e);
            return false;
        }
    }

    private void executeSqlScript(SQLiteDatabase db, Context context, String filename) {
        if (context == null || filename == null) {
            throw new IllegalArgumentException("Context and filename cannot be null");
        }

        if (!isAssetAvailable(context, filename)) {
            Log.e(TAG, "Asset not found: " + filename + ". Creating fallback data.");
            createFallbackStoryData(db);
            return;
        }

        BufferedReader reader = null;
        InputStream inputStream = null;
        int statementCount = 0;

        try {
            Log.d(TAG, "Executing SQL script: " + filename);
            inputStream = context.getAssets().open(filename);
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            StringBuilder currentStatement = new StringBuilder();
                    String line;
            boolean inStatement = false;
            int parenthesesCount = 0;

            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();

                // Skip empty lines and comments
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("--")) {
                    continue;
                }

                // Count parentheses for multi-line statements
                parenthesesCount += countOccurrences(trimmedLine, '(') - countOccurrences(trimmedLine, ')');

                // Check if starting a new INSERT statement
                if (trimmedLine.toUpperCase().startsWith("INSERT INTO")) {
                    // Execute previous statement if exists
                    if (currentStatement.length() > 0) {
                        executeStatementSafely(db, currentStatement.toString(), statementCount);
                        statementCount++;
                    }
                    currentStatement.setLength(0);
                    inStatement = true;
                }

                if (inStatement) {
                    currentStatement.append(line).append("\n");

                    // Check if statement is complete (ends with semicolon and balanced parentheses)
                    if (trimmedLine.endsWith(";") && parenthesesCount <= 0) {
                        executeStatementSafely(db, currentStatement.toString(), statementCount);
                        statementCount++;
                        currentStatement.setLength(0);
                        inStatement = false;
                        parenthesesCount = 0;
                    }
                }
            }

            // Execute any remaining statement
            if (currentStatement.length() > 0) {
                executeStatementSafely(db, currentStatement.toString(), statementCount);
                statementCount++;
            }

            // Verify that we have story data
            if (!hasMinimumStoryData(db)) {
                Log.w(TAG, "Minimum story data not found after script execution, creating fallback");
                createFallbackStoryData(db);
                } else {
                Log.d(TAG, "SQL script execution completed successfully. Executed " + statementCount + " statements.");
                }

            } catch (IOException e) {
            Log.e(TAG, "Error reading SQL script: " + filename, e);
            createFallbackStoryData(db);
        } catch (Exception e) {
            Log.e(TAG, "Error executing SQL script: " + filename, e);
            createFallbackStoryData(db);
        } finally {
            closeResourcesSafely(reader, inputStream);
        }
    }

    private void executeStatementSafely(SQLiteDatabase db, String sql, int statementNumber) {
        try {
            String cleanSql = sql.trim();
            if (!cleanSql.isEmpty()) {
                // Enhanced logging for debugging
                boolean isChoiceInsert = cleanSql.toLowerCase().contains("insert into choices");
                boolean isDialogInsert = cleanSql.toLowerCase().contains("insert into dialogs");

                if (isChoiceInsert || isDialogInsert) {
                    Log.d(TAG, "Executing " + (isChoiceInsert ? "CHOICE" : "DIALOG") +
                            " statement " + statementNumber + ": " +
                            cleanSql.substring(0, Math.min(100, cleanSql.length())) + "...");
                }

                db.execSQL(cleanSql);

                if (isChoiceInsert || isDialogInsert) {
                    Log.d(TAG, "Successfully executed " + (isChoiceInsert ? "CHOICE" : "DIALOG") +
                            " statement " + statementNumber);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "ERROR executing SQL statement " + statementNumber + ": " +
                    sql.substring(0, Math.min(200, sql.length())), e);

            // Log the full statement for debugging
            Log.e(TAG, "Full failed statement: " + sql);
        }
    }

    private int countOccurrences(String str, char ch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }

    private void closeResourcesSafely(BufferedReader reader, InputStream inputStream) {
        try {
            if (reader != null) reader.close();
        } catch (IOException e) {
            Log.e(TAG, "Error closing reader", e);
        }

        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Error closing input stream", e);
        }
    }
    private void executeStatement(SQLiteDatabase db, String sql) {
        try {
            String cleanSql = sql.trim();
            if (!cleanSql.isEmpty()) {
                Log.d(TAG, "Executing SQL: " + cleanSql.substring(0, Math.min(100, cleanSql.length())) + "...");
                db.execSQL(cleanSql);
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error executing SQL statement: " + sql.substring(0, Math.min(200, sql.length())), e);
            // Don't throw - continue with other statements
        }
    }

    private void ensureMinimumChoicesExist(SQLiteDatabase db) {
        try {
            // Check if dialog 1 has choices
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM choices WHERE dialog_id = 1", null);
            cursor.moveToFirst();
            int choiceCount = cursor.getInt(0);
            cursor.close();

            Log.d(TAG, "Dialog 1 has " + choiceCount + " choices");

            if (choiceCount == 0) {
                Log.w(TAG, "No choices found for dialog 1, creating minimal choices");

                // Insert basic choices manually
                long currentTime = System.currentTimeMillis();

                db.execSQL("INSERT INTO choices (dialog_id, choice_text, next_dialog_id, created_at) VALUES (?, ?, ?, ?)",
                        new Object[]{1, "Swear your aid to Viren and help investigate the prophecy", 11, currentTime});

                db.execSQL("INSERT INTO choices (dialog_id, choice_text, next_dialog_id, created_at) VALUES (?, ?, ?, ?)",
                        new Object[]{1, "Slip away to the coast and seek the source of the water's unrest", 12, currentTime});

                db.execSQL("INSERT INTO choices (dialog_id, choice_text, next_dialog_id, created_at) VALUES (?, ?, ?, ?)",
                        new Object[]{1, "Attend Lady Selene's public trial, hoping to read her intentions", 13, currentTime});

                Log.d(TAG, "Inserted fallback choices for dialog 1");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error ensuring minimum choices exist", e);
        }
    }

    private boolean hasMinimumStoryData(SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            // Check dialogs
            cursor = db.rawQuery("SELECT COUNT(*) FROM dialogs", null);
            if (!cursor.moveToFirst() || cursor.getInt(0) == 0) {
                Log.e(TAG, "No dialogs found in database");
                return false;
            }
            int dialogCount = cursor.getInt(0);
            cursor.close();

            // Check choices
            cursor = db.rawQuery("SELECT COUNT(*) FROM choices", null);
            if (!cursor.moveToFirst()) {
                Log.e(TAG, "Cannot count choices");
                return false;
            }
            int choiceCount = cursor.getInt(0);
            cursor.close();

            // Check for starting dialog
            cursor = db.rawQuery("SELECT COUNT(*) FROM dialogs WHERE id = 1", null);
            if (!cursor.moveToFirst() || cursor.getInt(0) == 0) {
                Log.e(TAG, "Starting dialog (id=1) not found");
                return false;
            }

            Log.d(TAG, "Story validation passed: " + dialogCount + " dialogs, " + choiceCount + " choices");
            return dialogCount > 0;

        } catch (Exception e) {
            Log.e(TAG, "Error validating story data", e);
            return false;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    // Helper method to check database integrity
    public boolean checkDatabaseIntegrity() {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean isValid = true;
        Cursor cursor = null;

        try {
            // Check for orphaned progress records
            cursor = db.rawQuery(
                    "SELECT p.user_id FROM progress p LEFT JOIN users u ON p.user_id = u.id WHERE u.id IS NULL",
                    null
            );
            if (cursor.moveToFirst()) {
                Log.e(TAG, "Found orphaned progress records");
                isValid = false;
            }
            cursor.close();

            // Check for invalid dialog references in progress
            cursor = db.rawQuery(
                    "SELECT p.current_dialog_id FROM progress p LEFT JOIN dialogs d " +
                            "ON p.current_dialog_id = d.id WHERE d.id IS NULL",
                    null
            );
            if (cursor.moveToFirst()) {
                Log.e(TAG, "Found invalid dialog references in progress");
                isValid = false;
            }
            cursor.close();

            // Check for invalid dialog references in choices
            cursor = db.rawQuery(
                    "SELECT c.dialog_id, c.next_dialog_id FROM choices c " +
                            "LEFT JOIN dialogs d1 ON c.dialog_id = d1.id " +
                            "LEFT JOIN dialogs d2 ON c.next_dialog_id = d2.id " +
                            "WHERE d1.id IS NULL OR d2.id IS NULL",
                    null
            );
            if (cursor.moveToFirst()) {
                Log.e(TAG, "Found invalid dialog references in choices");
                isValid = false;
            }
            cursor.close();

            // Check for dialogs without choices (dead ends)
            cursor = db.rawQuery(
                    "SELECT d.id FROM dialogs d LEFT JOIN choices c ON d.id = c.dialog_id " +
                            "WHERE c.id IS NULL AND d.id != (SELECT MAX(id) FROM dialogs)",
                    null
            );
            if (cursor.moveToFirst()) {
                Log.e(TAG, "Found dialog(s) without choices (potential dead ends)");
                isValid = false;
            }
            cursor.close();

            // Check for circular references in choices
            cursor = db.rawQuery(
                    "WITH RECURSIVE choice_path(start_id, current_id, path) AS (" +
                            "  SELECT dialog_id, next_dialog_id, dialog_id || ',' || next_dialog_id " +
                            "  FROM choices " +
                            "  UNION ALL " +
                            "  SELECT cp.start_id, c.next_dialog_id, cp.path || ',' || c.next_dialog_id " +
                            "  FROM choice_path cp " +
                            "  JOIN choices c ON cp.current_id = c.dialog_id " +
                            "  WHERE path NOT LIKE '%' || c.next_dialog_id || '%'" +
                            ") " +
                            "SELECT DISTINCT start_id " +
                            "FROM choice_path " +
                            "WHERE path LIKE '%' || start_id || '%' " +
                            "  AND length(path) > 1",
                    null
            );
            if (cursor.moveToFirst()) {
                Log.e(TAG, "Found circular references in choices");
                isValid = false;
            }

            return isValid;
        } catch (Exception e) {
            Log.e(TAG, "Error checking database integrity", e);
            return false;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public boolean validateStoryConsistency() {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean isValid = true;
        Cursor cursor = null;

        try {
            // Check if we have at least one dialog
            cursor = db.rawQuery("SELECT COUNT(*) FROM dialogs", null);
            if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
                Log.e(TAG, "No dialogs found in the database");
                return false;
            }
            cursor.close();

            // Check if dialog IDs are sequential
            cursor = db.rawQuery(
                    "WITH RECURSIVE numbers(num) AS (" +
                            "  SELECT 1 " +
                            "  UNION ALL " +
                            "  SELECT num + 1 FROM numbers " +
                            "  WHERE num < (SELECT MAX(id) FROM dialogs)" +
                            ") " +
                            "SELECT num FROM numbers " +
                            "WHERE num NOT IN (SELECT id FROM dialogs) " +
                            "LIMIT 1",
                    null
            );
            if (cursor.moveToFirst()) {
                Log.e(TAG, "Non-sequential dialog IDs found");
                isValid = false;
            }
            cursor.close();

            // Check for unreachable dialogs
            cursor = db.rawQuery(
                    "WITH RECURSIVE reachable_dialogs(id) AS (" +
                            "  SELECT 1 " + // Start from dialog 1
                            "  UNION " +
                            "  SELECT DISTINCT c.next_dialog_id " +
                            "  FROM reachable_dialogs rd " +
                            "  JOIN choices c ON rd.id = c.dialog_id" +
                            ") " +
                            "SELECT d.id FROM dialogs d " +
                            "LEFT JOIN reachable_dialogs rd ON d.id = rd.id " +
                            "WHERE rd.id IS NULL",
                    null
            );
            if (cursor.moveToFirst()) {
                Log.e(TAG, "Found unreachable dialogs");
                isValid = false;
            }

            return isValid;
        } catch (Exception e) {
            Log.e(TAG, "Error validating story consistency", e);
            return false;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    // Add this method to validate choice references
    private boolean validateChoiceReference(int dialogId, int nextDialogId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            // Check if both the current dialog and next dialog exist
            cursor = db.rawQuery(
                "SELECT d1.id as current_exists, d2.id as next_exists " +
                "FROM dialogs d1 LEFT JOIN dialogs d2 ON d2.id = ? " +
                "WHERE d1.id = ?",
                new String[]{String.valueOf(nextDialogId), String.valueOf(dialogId)}
            );

            if (cursor.moveToFirst()) {
                boolean currentExists = !cursor.isNull(cursor.getColumnIndexOrThrow("current_exists"));
                boolean nextExists = !cursor.isNull(cursor.getColumnIndexOrThrow("next_exists"));
                
                if (!currentExists) {
                    Log.e(TAG, "Invalid choice: current dialog " + dialogId + " does not exist");
                }
                if (!nextExists) {
                    Log.e(TAG, "Invalid choice: next dialog " + nextDialogId + " does not exist");
                }
                
                return currentExists && nextExists;
            }
            return false;
            } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public List<Choice> getChoicesForDialog(int dialogId) {
        logDatabaseOperation("getChoicesForDialog", "dialogId=" + dialogId);

        List<Choice> choices = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT c.id, c.dialog_id, c.choice_text, c.next_dialog_id, c.created_at " +
                            "FROM choices c " +
                            "WHERE c.dialog_id = ? " +
                            "ORDER BY c.id",
                    new String[]{String.valueOf(dialogId)}
            );

            if (cursor.moveToFirst()) {
                do {
                    try {
                        int nextDialogId = cursor.getInt(cursor.getColumnIndexOrThrow("next_dialog_id"));
                        String choiceText = cursor.getString(cursor.getColumnIndexOrThrow("choice_text"));

                        // Enhanced validation with fallback
                        if (isValidDialog(nextDialogId)) {
                            Choice choice = new Choice(
                                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                                    cursor.getInt(cursor.getColumnIndexOrThrow("dialog_id")),
                                    choiceText,
                                    nextDialogId
                            );
                            choices.add(choice);
                        } else {
                            Log.w(TAG, "Invalid next_dialog_id: " + nextDialogId + " for choice: " + choiceText);

                            // Try to find a valid fallback dialog
                            int fallbackDialogId = findNearestValidDialog(nextDialogId);
                            if (fallbackDialogId > 0) {
                                Log.w(TAG, "Using fallback dialog " + fallbackDialogId + " for choice: " + choiceText);
                                Choice choice = new Choice(
                                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                                        cursor.getInt(cursor.getColumnIndexOrThrow("dialog_id")),
                                        choiceText + " (Recovered)",
                                        fallbackDialogId
                                );
                                choices.add(choice);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing choice row", e);
                        // Continue with other choices
                    }
                } while (cursor.moveToNext());
            }

            // Additional safety check - if no choices found, log more info
            if (choices.isEmpty()) {
                Log.w(TAG, "No valid choices found for dialog " + dialogId);
                // Check if choices exist but are invalid
                Cursor allChoicesCursor = db.rawQuery(
                        "SELECT COUNT(*) FROM choices WHERE dialog_id = ?",
                        new String[]{String.valueOf(dialogId)}
                );
                if (allChoicesCursor.moveToFirst()) {
                    int totalChoices = allChoicesCursor.getInt(0);
                    Log.w(TAG, "Total choices in DB for dialog " + dialogId + ": " + totalChoices);
                }
                allChoicesCursor.close();
            }

            logDatabaseOperation("getChoicesForDialog", "Found " + choices.size() + " valid choices");
            return choices;
        } catch (Exception e) {
            logDatabaseError("getChoicesForDialog", e);
            return choices;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    private boolean isValidDialog(int dialogId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT 1 FROM dialogs WHERE id = ? LIMIT 1",
                    new String[]{String.valueOf(dialogId)});
            return cursor.moveToFirst();
        } catch (Exception e) {
            Log.e(TAG, "Error validating dialog " + dialogId, e);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int findNearestValidDialog(int targetDialogId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            // Try to find the next valid dialog after the target
            cursor = db.rawQuery(
                    "SELECT id FROM dialogs WHERE id >= ? ORDER BY id ASC LIMIT 1",
                    new String[]{String.valueOf(targetDialogId)}
            );

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }

            // If no dialog found after target, try the last valid dialog
            cursor.close();
            cursor = db.rawQuery("SELECT MAX(id) FROM dialogs", null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }

            return 1; // Ultimate fallback
        } catch (Exception e) {
            Log.e(TAG, "Error finding nearest valid dialog", e);
            return 1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int getUserDialogId(int userId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT current_dialog_id FROM progress WHERE user_id = ?",
                    new String[]{String.valueOf(userId)}
            );

            if (cursor != null && cursor.moveToFirst()) {
                int dialogId = cursor.getInt(0);
                Log.d(TAG, "Found dialog ID " + dialogId + " for user " + userId);
                return dialogId;
            }

            // If no progress found, initialize it
            Log.d(TAG, "No progress found for user " + userId + ", initializing");
            try {
                ContentValues values = new ContentValues();
                values.put("user_id", userId);
                values.put("current_dialog_id", 1);
                values.put("last_updated", System.currentTimeMillis());
                db.insert("progress", null, values);
                return 1;
            } catch (Exception e) {
                Log.e(TAG, "Error creating progress for user " + userId, e);
                return 1; // Default starting point
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user dialog ID for user " + userId, e);
            return 1; // Default starting point
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error closing cursor", e);
                }
            }
        }
    }

    public int getDialogCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM dialogs", null);
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public List<Integer> getDanglingNextDialogIds() {
        List<Integer> missingDialogs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(
                    "SELECT DISTINCT next_dialog_id FROM choices WHERE next_dialog_id NOT IN (SELECT id FROM dialogs)",
                    null
            );

            if (cursor.moveToFirst()) {
                do {
                    missingDialogs.add(cursor.getInt(0));
                } while (cursor.moveToNext());
            }
            return missingDialogs;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // Helper method to backup the database
    public void backupDatabase(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String timestamp = String.format("%tF_%<tH%<tM%<tS", System.currentTimeMillis());
        String backupFileName = String.format("backup_%s.db.gz", timestamp);
        String backupPath = context.getFilesDir() + "/" + backupFileName;

        try {
            // Ensure database integrity before backup
            if (!checkDatabaseIntegrity()) {
                throw new IllegalStateException("Database integrity check failed before backup");
            }

            // Close database before copying
            db.close();

            // Get the current database file
            File currentDB = context.getDatabasePath(DB_NAME);
            File backupDB = new File(backupPath);

            if (currentDB.exists()) {
                // Use try-with-resources for compression
                try (FileInputStream fis = new FileInputStream(currentDB);
                     GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(backupDB))) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                        gzos.write(buffer, 0, length);
                    }
                    gzos.finish();

                    // Verify backup file exists and is readable
                    if (!backupDB.canRead()) {
                        throw new IOException("Backup file not readable: " + backupPath);
                    }

                    // Clean up old backups (keep only last 7 days of backups)
                    File backupDir = new File(context.getFilesDir().toString());
                    File[] backups = backupDir.listFiles((dir, name) ->
                            name.startsWith("backup_") && name.endsWith(".db.gz"));
                    
                    if (backups != null) {
                        long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
                        for (File backup : backups) {
                            if (backup.lastModified() < sevenDaysAgo) {
                                backup.delete();
                            }
                        }
                    }

                    Log.d(TAG, "Database backed up successfully to: " + backupPath);
                }
            } else {
                throw new FileNotFoundException("Source database does not exist");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to backup database", e);
            // Try to delete failed backup file
            try {
                new File(backupPath).delete();
            } catch (Exception deleteError) {
                Log.e(TAG, "Failed to delete failed backup file", deleteError);
            }
            throw new RuntimeException("Database backup failed", e);
        } finally {
            // Reopen the database
            db = this.getReadableDatabase();
        }
    }

    // Add method to restore from backup
    public void restoreFromBackup(Context context, String backupFileName) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        String backupPath = context.getFilesDir() + "/" + backupFileName;
        File backupFile = new File(backupPath);
        File currentDB = context.getDatabasePath(DB_NAME);

        if (!backupFile.exists()) {
            throw new IllegalArgumentException("Backup file does not exist: " + backupPath);
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.close();

        try {
            // Create temp file for decompression
            File tempFile = File.createTempFile("temp_db", ".db", context.getCacheDir());
            
            // Decompress backup
            try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(backupFile));
                 FileOutputStream fos = new FileOutputStream(tempFile)) {

                    byte[] buffer = new byte[1024];
                    int length;
                while ((length = gzis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                }
            }

            // Verify temp file integrity
            SQLiteDatabase verifyDb = SQLiteDatabase.openDatabase(
                tempFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);
            verifyDb.close();

            // Copy temp file to actual database location
            try (FileChannel src = new FileInputStream(tempFile).getChannel();
                 FileChannel dst = new FileOutputStream(currentDB).getChannel()) {
                dst.transferFrom(src, 0, src.size());
            }

            // Clean up temp file
            tempFile.delete();

            Log.d(TAG, "Database restored successfully from: " + backupPath);
        } catch (Exception e) {
            Log.e(TAG, "Failed to restore database", e);
            throw new RuntimeException("Database restore failed", e);
        } finally {
            // Reopen the database
            db = this.getWritableDatabase();
        }
    }

    // Add a new method for handling database upgrades with backup
    public void upgradeWithBackup(Context context) {
        try {
            // First create a backup
            backupDatabase(context);
            
            // Then get the database to trigger the upgrade
            getWritableDatabase();
            
            Log.d(TAG, "Database upgrade with backup completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error during database upgrade with backup", e);
            throw new RuntimeException("Database upgrade with backup failed", e);
        }
    }

    public void verifyAndRepairStoryData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Check if we have any dialogs at all
            Cursor dialogCheck = db.rawQuery("SELECT COUNT(*) FROM dialogs", null);
            dialogCheck.moveToFirst();
            int dialogCount = dialogCheck.getInt(0);
            dialogCheck.close();

            if (dialogCount == 0) {
                Log.w(TAG, "No dialogs found. Creating emergency story.");
                FallbackStoryCreator.createEmergencyStory(db);
                db.setTransactionSuccessful();
                return;
            }

            // Check if dialog 1 exists (required starting point)
            Cursor startCheck = db.rawQuery("SELECT 1 FROM dialogs WHERE id = 1", null);
            boolean hasStartDialog = startCheck.moveToFirst();
            startCheck.close();

            if (!hasStartDialog) {
                Log.w(TAG, "Starting dialog (id=1) missing. Creating fallback start.");
                try {
                    db.execSQL("INSERT INTO dialogs (id, text, created_at) VALUES (1, ?, ?)",
                            new Object[]{
                                    "Welcome! The story begins here.\n\nThis dialog was created to ensure the game can start properly.",
                                    System.currentTimeMillis()
                            });
                } catch (Exception e) {
                    Log.e(TAG, "Failed to create starting dialog", e);
                }
            }

            // Verify choices point to valid dialogs and clean up invalid ones
            Cursor choiceCheck = db.rawQuery(
                    "SELECT c.id, c.dialog_id, c.next_dialog_id " +
                            "FROM choices c " +
                            "LEFT JOIN dialogs d1 ON c.dialog_id = d1.id " +
                            "LEFT JOIN dialogs d2 ON c.next_dialog_id = d2.id " +
                            "WHERE d1.id IS NULL OR d2.id IS NULL",
                    null
            );

            if (choiceCheck.moveToFirst()) {
                Log.w(TAG, "Invalid choice references detected. Cleaning up...");
                do {
                    int choiceId = choiceCheck.getInt(choiceCheck.getColumnIndexOrThrow("id"));
                    Log.d(TAG, "Removing invalid choice: " + choiceId);
                    db.execSQL("DELETE FROM choices WHERE id = ?", new String[]{String.valueOf(choiceId)});
                } while (choiceCheck.moveToNext());
            }
            choiceCheck.close();

            db.setTransactionSuccessful();
            Log.d(TAG, "Story data verification and repair completed");
        } catch (Exception e) {
            Log.e(TAG, "Error during story data verification", e);
            // Don't throw exception - try to continue with whatever data we have
        } finally {
            db.endTransaction();
        }
    }

    private void executeSqlStatementsFromFile(SQLiteDatabase db, Context context, String filename, boolean dialogsOnly) throws IOException {
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = context.getAssets().open(filename);
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            
            StringBuilder statement = new StringBuilder();
            String line;
            int openParens = 0;
            boolean inStatement = false;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                
                // Count parentheses to handle multi-line statements
                openParens += countChar(line, '(') - countChar(line, ')');
                
                // Check if we're starting a new statement
                if (line.toUpperCase().startsWith("INSERT INTO")) {
                    inStatement = true;
                }
                
                if (inStatement) {
                    statement.append(line).append(" ");
                    
                    // Check if we've reached the end of a complete statement
                    if (openParens == 0 && line.endsWith(";")) {
                        String sql = statement.toString().trim();
                        boolean isDialogInsert = sql.toLowerCase().contains("insert into dialogs");
                        boolean isChoiceInsert = sql.toLowerCase().contains("insert into choices");
                        
                        // Execute statement based on pass
                        if ((dialogsOnly && isDialogInsert) || (!dialogsOnly && isChoiceInsert)) {
                            try {
                                // Handle multiple value sets in a single INSERT
                                if (sql.contains("VALUES") && sql.contains(",") && sql.contains("(")) {
                                    // Split into individual INSERT statements
                                    String baseInsert = sql.split("VALUES")[0].trim() + " VALUES ";
                                    String values = sql.split("VALUES")[1].trim();
                                    values = values.substring(0, values.length() - 1); // Remove semicolon
                                    
                                    String[] valueSets = values.split("\\),\\s*\\(");
                                    for (int i = 0; i < valueSets.length; i++) {
                                        String valueSet = valueSets[i];
                                        if (!valueSet.startsWith("(")) valueSet = "(" + valueSet;
                                        if (!valueSet.endsWith(")")) valueSet = valueSet + ")";
                                        
                                        String individualSql = baseInsert + valueSet + ";";
                                        Log.d(TAG, "Executing SQL: " + individualSql);
                                        db.execSQL(individualSql);
                                    }
                } else {
                                    Log.d(TAG, "Executing SQL: " + sql);
                                    db.execSQL(sql);
                                }
                            } catch (SQLException e) {
                                Log.e(TAG, "Error executing SQL: " + sql, e);
                                throw e;
                            }
                        }
                        
                        // Reset for next statement
                        statement.setLength(0);
                        inStatement = false;
                        openParens = 0;
                    }
                }
            }
            
            // Check for unclosed statements
            if (statement.length() > 0) {
                Log.w(TAG, "Unclosed SQL statement found: " + statement.toString());
            }
            
        } finally {
            if (reader != null) {
                try {
                    reader.close();
            } catch (IOException e) {
                    Log.e(TAG, "Error closing reader", e);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing input stream", e);
                }
            }
        }
    }

    private int countChar(String str, char c) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c) count++;
        }
        return count;
    }

    public int getLastWorkingDialogId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            // First try to get the user's current dialog
            cursor = db.rawQuery(
                "SELECT current_dialog_id FROM progress WHERE user_id = ?",
                new String[]{String.valueOf(userId)}
            );

            if (cursor.moveToFirst()) {
                int currentDialogId = cursor.getInt(0);
                cursor.close();

                // Verify if this dialog exists
                cursor = db.rawQuery(
                    "SELECT 1 FROM dialogs WHERE id = ?",
                    new String[]{String.valueOf(currentDialogId)}
                );

                if (cursor.moveToFirst()) {
                    return currentDialogId;  // Current dialog exists and is valid
                }
            }

            // If current dialog is invalid, find the last valid dialog
            cursor = db.rawQuery(
                "SELECT MAX(id) FROM dialogs WHERE id <= (SELECT COALESCE(MAX(current_dialog_id), 1) FROM progress WHERE user_id = ?)",
                new String[]{String.valueOf(userId)}
            );

            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                return cursor.getInt(0);
            }

            return 1;  // Default to first dialog if no valid dialog found
        } catch (Exception e) {
            Log.e(TAG, "Error getting last working dialog ID", e);
            return 1;  // Return to first dialog on error
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public boolean hasUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM users", null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking for users", e);
            return false;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("users", new String[]{"id", "username"}, null, null, null, null, "username ASC");
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                    users.add(new User(id, username));
                } while (cursor.moveToNext());
            }
            return users;
        } catch (Exception e) {
            Log.e(TAG, "Error getting all users", e);
            return users; // Return empty list on error
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    private void ensureChoicesForConvergenceDialogs(SQLiteDatabase db) {
        Log.d(TAG, "Ensuring choices exist for convergence dialogs...");
        long currentTime = System.currentTimeMillis();

        // Array of convergence dialog IDs and their choices
        int[][] convergenceDialogs = {
            {301, 501, 502, 503},  // dialog 301 leads to 501, 502, 503
            {302, 501, 502, 503},  // dialog 302 leads to 501, 502, 503
            {501, 1},  // dialog 501 leads back to start
            {502, 1},  // dialog 502 leads back to start
            {503, 1},  // dialog 503 leads back to start
            {504, 1}   // dialog 504 leads back to start
        };

        for (int[] dialogInfo : convergenceDialogs) {
            int dialogId = dialogInfo[0];
            
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM choices WHERE dialog_id = ?",
                new String[]{String.valueOf(dialogId)});
            cursor.moveToFirst();
            int choiceCount = cursor.getInt(0);
            cursor.close();

            if (choiceCount == 0) {
                Log.d(TAG, "Adding missing choices for dialog " + dialogId);

                if (dialogId == 301) {
                    ContentValues choice1 = new ContentValues();
                    choice1.put("dialog_id", dialogId);
                    choice1.put("choice_text", "Face the consequences of your choice");
                    choice1.put("next_dialog_id", 501);
                    choice1.put("created_at", currentTime);
                    db.insert("choices", null, choice1);

                    ContentValues choice2 = new ContentValues();
                    choice2.put("dialog_id", dialogId);
                    choice2.put("choice_text", "Reflect on the path that led here");
                    choice2.put("next_dialog_id", 502);
                    choice2.put("created_at", currentTime);
                    db.insert("choices", null, choice2);

                    ContentValues choice3 = new ContentValues();
                    choice3.put("dialog_id", dialogId);
                    choice3.put("choice_text", "Consider the implications for Caelondria's future");
                    choice3.put("next_dialog_id", 503);
                    choice3.put("created_at", currentTime);
                    db.insert("choices", null, choice3);
                } else if (dialogId == 302) {
                    ContentValues choice1 = new ContentValues();
                    choice1.put("dialog_id", dialogId);
                    choice1.put("choice_text", "Embrace the path of order and tradition");
                    choice1.put("next_dialog_id", 502);
                    choice1.put("created_at", currentTime);
                    db.insert("choices", null, choice1);

                    ContentValues choice2 = new ContentValues();
                    choice2.put("dialog_id", dialogId);
                    choice2.put("choice_text", "Choose the way of change and transformation");
                    choice2.put("next_dialog_id", 501);
                    choice2.put("created_at", currentTime);
                    db.insert("choices", null, choice2);

                    ContentValues choice3 = new ContentValues();
                    choice3.put("dialog_id", dialogId);
                    choice3.put("choice_text", "Seek a balance between the old and new");
                    choice3.put("next_dialog_id", 503);
                    choice3.put("created_at", currentTime);
                    db.insert("choices", null, choice3);
                } else {
                    // For ending dialogs (501-504), add option to return to start
                    ContentValues choice = new ContentValues();
                    choice.put("dialog_id", dialogId);
                    choice.put("choice_text", "Return to the beginning");
                    choice.put("next_dialog_id", 1);
                    choice.put("created_at", currentTime);
                    db.insert("choices", null, choice);
                }

                Log.d(TAG, "Added choices for dialog " + dialogId);
            }
        }
    }

    public void validateStoryStructure() {
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d(TAG, "Validating story structure...");

        // Check for missing dialogs in the sequence
        Cursor dialogCursor = db.rawQuery(
            "WITH RECURSIVE numbers(num) AS (" +
            "  SELECT MIN(id) FROM dialogs " +
            "  UNION ALL " +
            "  SELECT num + 1 FROM numbers " +
            "  WHERE num < (SELECT MAX(id) FROM dialogs)" +
            ") " +
            "SELECT num FROM numbers " +
            "WHERE num NOT IN (SELECT id FROM dialogs) " +
            "ORDER BY num",
            null
        );

        if (dialogCursor.moveToFirst()) {
            Log.w(TAG, "Missing dialogs found:");
            do {
                int missingId = dialogCursor.getInt(0);
                Log.w(TAG, "Missing dialog ID: " + missingId);
            } while (dialogCursor.moveToNext());
        }
        dialogCursor.close();

        // Check for dialogs without choices
        Cursor noChoicesCursor = db.rawQuery(
            "SELECT d.id, d.text FROM dialogs d " +
            "LEFT JOIN choices c ON d.id = c.dialog_id " +
            "WHERE c.id IS NULL " +
            "ORDER BY d.id",
            null
        );

        if (noChoicesCursor.moveToFirst()) {
            Log.w(TAG, "Dialogs without choices found:");
            do {
                int dialogId = noChoicesCursor.getInt(0);
                String dialogText = noChoicesCursor.getString(1);
                Log.w(TAG, "Dialog " + dialogId + " has no choices. Text: " + dialogText.substring(0, Math.min(50, dialogText.length())) + "...");
            } while (noChoicesCursor.moveToNext());
        }
        noChoicesCursor.close();

        // Check for choices pointing to non-existent dialogs
        Cursor invalidChoicesCursor = db.rawQuery(
            "SELECT c.id, c.dialog_id, c.next_dialog_id, c.choice_text " +
            "FROM choices c " +
            "LEFT JOIN dialogs d ON c.next_dialog_id = d.id " +
            "WHERE d.id IS NULL",
            null
        );

        if (invalidChoicesCursor.moveToFirst()) {
            Log.w(TAG, "Choices pointing to non-existent dialogs found:");
            do {
                int choiceId = invalidChoicesCursor.getInt(0);
                int dialogId = invalidChoicesCursor.getInt(1);
                int nextDialogId = invalidChoicesCursor.getInt(2);
                String choiceText = invalidChoicesCursor.getString(3);
                Log.w(TAG, "Choice " + choiceId + " in dialog " + dialogId + " points to non-existent dialog " + nextDialogId + ". Choice text: " + choiceText);
            } while (invalidChoicesCursor.moveToNext());
        }
        invalidChoicesCursor.close();

        // Check for unreachable dialogs (not connected to the story graph)
        Cursor unreachableCursor = db.rawQuery(
            "WITH RECURSIVE reachable(id) AS (" +
            "  SELECT 1 " + // Start from dialog 1
            "  UNION " +
            "  SELECT c.next_dialog_id " +
            "  FROM reachable r " +
            "  JOIN choices c ON r.id = c.dialog_id" +
            ") " +
            "SELECT d.id, d.text " +
            "FROM dialogs d " +
            "WHERE d.id NOT IN (SELECT id FROM reachable) " +
            "ORDER BY d.id",
            null
        );

        if (unreachableCursor.moveToFirst()) {
            Log.w(TAG, "Unreachable dialogs found (not connected to the story graph):");
            do {
                int dialogId = unreachableCursor.getInt(0);
                String dialogText = unreachableCursor.getString(1);
                Log.w(TAG, "Dialog " + dialogId + " is unreachable. Text: " + dialogText.substring(0, Math.min(50, dialogText.length())) + "...");
            } while (unreachableCursor.moveToNext());
        }
        unreachableCursor.close();

        Log.d(TAG, "Story structure validation complete");
    }
}
