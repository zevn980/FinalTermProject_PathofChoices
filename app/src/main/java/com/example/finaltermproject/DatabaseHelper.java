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
                dialogValues.put("text", "Welcome to Path of Choices! Your journey begins here.");
                dialogValues.put("created_at", currentTime);
                long dialogId = db.insert("dialogs", null, dialogValues);
                Log.d(TAG, "Initial dialog created with ID: " + dialogId);

                // Create subsequent dialogs
                Log.d(TAG, "Creating subsequent dialogs...");
                ContentValues dialog11 = new ContentValues();
                dialog11.put("id", 11);
                dialog11.put("text", "You decide to help Viren. His eyes gleam with approval as you step forward.");
                dialog11.put("created_at", currentTime);
                db.insert("dialogs", null, dialog11);

                ContentValues dialog12 = new ContentValues();
                dialog12.put("id", 12);
                dialog12.put("text", "The coastal winds beckon you. You feel the pull of the mysterious waters.");
                dialog12.put("created_at", currentTime);
                db.insert("dialogs", null, dialog12);

                ContentValues dialog13 = new ContentValues();
                dialog13.put("id", 13);
                dialog13.put("text", "The courthouse is packed as Lady Selene's trial begins.");
                dialog13.put("created_at", currentTime);
                db.insert("dialogs", null, dialog13);

                // Create dialog 14, 15, and 16 for the choices from dialog 13
                ContentValues dialog14 = new ContentValues();
                dialog14.put("id", 14);
                dialog14.put("text", "You speak up in Lady Selene's defense, citing her years of service to the realm.");
                dialog14.put("created_at", currentTime);
                db.insert("dialogs", null, dialog14);

                ContentValues dialog15 = new ContentValues();
                dialog15.put("id", 15);
                dialog15.put("text", "You remain silent, observing the proceedings carefully.");
                dialog15.put("created_at", currentTime);
                db.insert("dialogs", null, dialog15);

                ContentValues dialog16 = new ContentValues();
                dialog16.put("id", 16);
                dialog16.put("text", "You present evidence against Lady Selene, supporting the prosecution.");
                dialog16.put("created_at", currentTime);
                db.insert("dialogs", null, dialog16);

                // Create dialog 17, 18, and 19 for the choices from dialog 12
                ContentValues dialog17 = new ContentValues();
                dialog17.put("id", 17);
                dialog17.put("text", "You dive into the depths, following the strange underwater lights.");
                dialog17.put("created_at", currentTime);
                db.insert("dialogs", null, dialog17);

                ContentValues dialog18 = new ContentValues();
                dialog18.put("id", 18);
                dialog18.put("text", "You search the beach for clues about the water's strange behavior.");
                dialog18.put("created_at", currentTime);
                db.insert("dialogs", null, dialog18);

                ContentValues dialog19 = new ContentValues();
                dialog19.put("id", 19);
                dialog19.put("text", "You seek out the local fishermen to gather information about recent events.");
                dialog19.put("created_at", currentTime);
                db.insert("dialogs", null, dialog19);

                // Create dialog 20, 21, and 22 for the choices from dialog 11
                ContentValues dialog20 = new ContentValues();
                dialog20.put("id", 20);
                dialog20.put("text", "You examine the ancient scrolls in Viren's study, searching for clues about the prophecy.");
                dialog20.put("created_at", currentTime);
                db.insert("dialogs", null, dialog20);

                ContentValues dialog21 = new ContentValues();
                dialog21.put("id", 21);
                dialog21.put("text", "You visit the temple ruins where the prophecy was first discovered.");
                dialog21.put("created_at", currentTime);
                db.insert("dialogs", null, dialog21);

                ContentValues dialog22 = new ContentValues();
                dialog22.put("id", 22);
                dialog22.put("text", "You question the temple priests about their interpretation of the prophecy.");
                dialog22.put("created_at", currentTime);
                db.insert("dialogs", null, dialog22);

                // Add new dialogs for Viren's prophecy branch (20, 21, 22)
                ContentValues dialog23 = new ContentValues();
                dialog23.put("id", 23);
                dialog23.put("text", "The ancient scrolls reveal a hidden pattern. Symbols of water, fire, and earth intertwine with mentions of a great upheaval.");
                dialog23.put("created_at", currentTime);
                db.insert("dialogs", null, dialog23);

                ContentValues dialog24 = new ContentValues();
                dialog24.put("id", 24);
                dialog24.put("text", "Among the temple ruins, you discover an untouched chamber. Ancient murals depict a ritual involving three sacred artifacts.");
                dialog24.put("created_at", currentTime);
                db.insert("dialogs", null, dialog24);

                ContentValues dialog25 = new ContentValues();
                dialog25.put("id", 25);
                dialog25.put("text", "The priests speak of a guardian chosen by the elements themselves. They believe the time of choosing is near.");
                dialog25.put("created_at", currentTime);
                db.insert("dialogs", null, dialog25);

                // Add new dialogs for coastal mystery branch (17, 18, 19)
                ContentValues dialog26 = new ContentValues();
                dialog26.put("id", 26);
                dialog26.put("text", "Beneath the waves, you find an ancient underwater temple. Strange glyphs pulse with an ethereal blue light.");
                dialog26.put("created_at", currentTime);
                db.insert("dialogs", null, dialog26);

                ContentValues dialog27 = new ContentValues();
                dialog27.put("id", 27);
                dialog27.put("text", "Your beach investigation reveals peculiar crystals that seem to resonate with the tides. They form a pattern pointing to a hidden cave.");
                dialog27.put("created_at", currentTime);
                db.insert("dialogs", null, dialog27);

                ContentValues dialog28 = new ContentValues();
                dialog28.put("id", 28);
                dialog28.put("text", "The fishermen tell tales of ancient sea people and a pact broken long ago. They speak of a way to restore balance.");
                dialog28.put("created_at", currentTime);
                db.insert("dialogs", null, dialog28);

                // Add new dialogs for Lady Selene's trial branch (14, 15, 16)
                ContentValues dialog29 = new ContentValues();
                dialog29.put("id", 29);
                dialog29.put("text", "Your defense of Lady Selene reveals new evidence. She appears to be protecting an ancient secret rather than committing treason.");
                dialog29.put("created_at", currentTime);
                db.insert("dialogs", null, dialog29);

                ContentValues dialog30 = new ContentValues();
                dialog30.put("id", 30);
                dialog30.put("text", "Your careful observation uncovers a conspiracy. Someone is manipulating evidence to frame Lady Selene.");
                dialog30.put("created_at", currentTime);
                db.insert("dialogs", null, dialog30);

                ContentValues dialog31 = new ContentValues();
                dialog31.put("id", 31);
                dialog31.put("text", "The evidence against Lady Selene leads to a shocking discovery about her true identity and her connection to the realm's ancient magic.");
                dialog31.put("created_at", currentTime);
                db.insert("dialogs", null, dialog31);

                // Final convergence dialogs
                ContentValues dialog32 = new ContentValues();
                dialog32.put("id", 32);
                dialog32.put("text", "The prophecy, the sea's unrest, and Lady Selene's secret are all connected. You stand at the threshold of a momentous decision.");
                dialog32.put("created_at", currentTime);
                db.insert("dialogs", null, dialog32);

                ContentValues dialog33 = new ContentValues();
                dialog33.put("id", 33);
                dialog33.put("text", "You choose to embrace the ancient power, becoming the prophesied guardian. The realm enters a new age of harmony.");
                dialog33.put("created_at", currentTime);
                db.insert("dialogs", null, dialog33);

                ContentValues dialog34 = new ContentValues();
                dialog34.put("id", 34);
                dialog34.put("text", "You reject the ancient power, choosing to forge a new path. The old ways fade, but the realm finds strength in change.");
                dialog34.put("created_at", currentTime);
                db.insert("dialogs", null, dialog34);

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

                // Create choices for dialog 11
                ContentValues choice11_1 = new ContentValues();
                choice11_1.put("dialog_id", 11);
                choice11_1.put("choice_text", "Study the ancient scrolls in Viren's library");
                choice11_1.put("next_dialog_id", 20);
                choice11_1.put("created_at", currentTime);
                long choiceId11_1 = db.insert("choices", null, choice11_1);
                Log.d(TAG, "Dialog 11 first choice created with ID: " + choiceId11_1);

                ContentValues choice11_2 = new ContentValues();
                choice11_2.put("dialog_id", 11);
                choice11_2.put("choice_text", "Investigate the temple ruins");
                choice11_2.put("next_dialog_id", 21);
                choice11_2.put("created_at", currentTime);
                long choiceId11_2 = db.insert("choices", null, choice11_2);
                Log.d(TAG, "Dialog 11 second choice created with ID: " + choiceId11_2);

                ContentValues choice11_3 = new ContentValues();
                choice11_3.put("dialog_id", 11);
                choice11_3.put("choice_text", "Consult the temple priests");
                choice11_3.put("next_dialog_id", 22);
                choice11_3.put("created_at", currentTime);
                long choiceId11_3 = db.insert("choices", null, choice11_3);
                Log.d(TAG, "Dialog 11 third choice created with ID: " + choiceId11_3);

                // Create choices for dialog 12
                ContentValues choice12_1 = new ContentValues();
                choice12_1.put("dialog_id", 12);
                choice12_1.put("choice_text", "Dive into the mysterious waters");
                choice12_1.put("next_dialog_id", 17);
                choice12_1.put("created_at", currentTime);
                long choiceId12_1 = db.insert("choices", null, choice12_1);
                Log.d(TAG, "Dialog 12 first choice created with ID: " + choiceId12_1);

                ContentValues choice12_2 = new ContentValues();
                choice12_2.put("dialog_id", 12);
                choice12_2.put("choice_text", "Search the beach for clues");
                choice12_2.put("next_dialog_id", 18);
                choice12_2.put("created_at", currentTime);
                long choiceId12_2 = db.insert("choices", null, choice12_2);
                Log.d(TAG, "Dialog 12 second choice created with ID: " + choiceId12_2);

                ContentValues choice12_3 = new ContentValues();
                choice12_3.put("dialog_id", 12);
                choice12_3.put("choice_text", "Talk to the local fishermen");
                choice12_3.put("next_dialog_id", 19);
                choice12_3.put("created_at", currentTime);
                long choiceId12_3 = db.insert("choices", null, choice12_3);
                Log.d(TAG, "Dialog 12 third choice created with ID: " + choiceId12_3);

                // Create choices for dialog 13
                ContentValues choice13_1 = new ContentValues();
                choice13_1.put("dialog_id", 13);
                choice13_1.put("choice_text", "Speak in Lady Selene's defense");
                choice13_1.put("next_dialog_id", 14);
                choice13_1.put("created_at", currentTime);
                long choiceId13_1 = db.insert("choices", null, choice13_1);
                Log.d(TAG, "Dialog 13 first choice created with ID: " + choiceId13_1);

                ContentValues choice13_2 = new ContentValues();
                choice13_2.put("dialog_id", 13);
                choice13_2.put("choice_text", "Observe silently and gather information");
                choice13_2.put("next_dialog_id", 15);
                choice13_2.put("created_at", currentTime);
                long choiceId13_2 = db.insert("choices", null, choice13_2);
                Log.d(TAG, "Dialog 13 second choice created with ID: " + choiceId13_2);

                ContentValues choice13_3 = new ContentValues();
                choice13_3.put("dialog_id", 13);
                choice13_3.put("choice_text", "Present evidence against Lady Selene");
                choice13_3.put("next_dialog_id", 16);
                choice13_3.put("created_at", currentTime);
                long choiceId13_3 = db.insert("choices", null, choice13_3);
                Log.d(TAG, "Dialog 13 third choice created with ID: " + choiceId13_3);

                // Add choices for dialog 20 (Viren's scrolls)
                ContentValues choice20_1 = new ContentValues();
                choice20_1.put("dialog_id", 20);
                choice20_1.put("choice_text", "Study the elemental symbols more closely");
                choice20_1.put("next_dialog_id", 23);
                choice20_1.put("created_at", currentTime);
                db.insert("choices", null, choice20_1);

                // Add choices for dialog 21 (Temple ruins)
                ContentValues choice21_1 = new ContentValues();
                choice21_1.put("dialog_id", 21);
                choice21_1.put("choice_text", "Explore the hidden chamber");
                choice21_1.put("next_dialog_id", 24);
                choice21_1.put("created_at", currentTime);
                db.insert("choices", null, choice21_1);

                // Add choices for dialog 22 (Temple priests)
                ContentValues choice22_1 = new ContentValues();
                choice22_1.put("dialog_id", 22);
                choice22_1.put("choice_text", "Learn more about the prophecied guardian");
                choice22_1.put("next_dialog_id", 25);
                choice22_1.put("created_at", currentTime);
                db.insert("choices", null, choice22_1);

                // Add choices for dialog 17 (Underwater)
                ContentValues choice17_1 = new ContentValues();
                choice17_1.put("dialog_id", 17);
                choice17_1.put("choice_text", "Investigate the underwater temple");
                choice17_1.put("next_dialog_id", 26);
                choice17_1.put("created_at", currentTime);
                db.insert("choices", null, choice17_1);

                // Add choices for dialog 18 (Beach search)
                ContentValues choice18_1 = new ContentValues();
                choice18_1.put("dialog_id", 18);
                choice18_1.put("choice_text", "Follow the crystal pattern");
                choice18_1.put("next_dialog_id", 27);
                choice18_1.put("created_at", currentTime);
                db.insert("choices", null, choice18_1);

                // Add choices for dialog 19 (Fishermen)
                ContentValues choice19_1 = new ContentValues();
                choice19_1.put("dialog_id", 19);
                choice19_1.put("choice_text", "Learn about the ancient pact");
                choice19_1.put("next_dialog_id", 28);
                choice19_1.put("created_at", currentTime);
                db.insert("choices", null, choice19_1);

                // Add choices for dialog 14 (Defend Selene)
                ContentValues choice14_1 = new ContentValues();
                choice14_1.put("dialog_id", 14);
                choice14_1.put("choice_text", "Investigate the ancient secret");
                choice14_1.put("next_dialog_id", 29);
                choice14_1.put("created_at", currentTime);
                db.insert("choices", null, choice14_1);

                // Add choices for dialog 15 (Silent observation)
                ContentValues choice15_1 = new ContentValues();
                choice15_1.put("dialog_id", 15);
                choice15_1.put("choice_text", "Uncover the conspiracy");
                choice15_1.put("next_dialog_id", 30);
                choice15_1.put("created_at", currentTime);
                db.insert("choices", null, choice15_1);

                // Add choices for dialog 16 (Present evidence)
                ContentValues choice16_1 = new ContentValues();
                choice16_1.put("dialog_id", 16);
                choice16_1.put("choice_text", "Reveal Lady Selene's true identity");
                choice16_1.put("next_dialog_id", 31);
                choice16_1.put("created_at", currentTime);
                db.insert("choices", null, choice16_1);

                // Add convergence choices from each branch to the final choice
                ContentValues choice23_1 = new ContentValues();
                choice23_1.put("dialog_id", 23);
                choice23_1.put("choice_text", "Connect the prophecy to recent events");
                choice23_1.put("next_dialog_id", 32);
                choice23_1.put("created_at", currentTime);
                db.insert("choices", null, choice23_1);

                ContentValues choice24_1 = new ContentValues();
                choice24_1.put("dialog_id", 24);
                choice24_1.put("choice_text", "Understand the ritual's significance");
                choice24_1.put("next_dialog_id", 32);
                choice24_1.put("created_at", currentTime);
                db.insert("choices", null, choice24_1);

                ContentValues choice25_1 = new ContentValues();
                choice25_1.put("dialog_id", 25);
                choice25_1.put("choice_text", "Consider your role in the prophecy");
                choice25_1.put("next_dialog_id", 32);
                choice25_1.put("created_at", currentTime);
                db.insert("choices", null, choice25_1);

                ContentValues choice26_1 = new ContentValues();
                choice26_1.put("dialog_id", 26);
                choice26_1.put("choice_text", "Decipher the ancient glyphs");
                choice26_1.put("next_dialog_id", 32);
                choice26_1.put("created_at", currentTime);
                db.insert("choices", null, choice26_1);

                ContentValues choice27_1 = new ContentValues();
                choice27_1.put("dialog_id", 27);
                choice27_1.put("choice_text", "Connect the crystal's power to the prophecy");
                choice27_1.put("next_dialog_id", 32);
                choice27_1.put("created_at", currentTime);
                db.insert("choices", null, choice27_1);

                ContentValues choice28_1 = new ContentValues();
                choice28_1.put("dialog_id", 28);
                choice28_1.put("choice_text", "Consider restoring the ancient pact");
                choice28_1.put("next_dialog_id", 32);
                choice28_1.put("created_at", currentTime);
                db.insert("choices", null, choice28_1);

                ContentValues choice29_1 = new ContentValues();
                choice29_1.put("dialog_id", 29);
                choice29_1.put("choice_text", "Connect Lady Selene to the prophecy");
                choice29_1.put("next_dialog_id", 32);
                choice29_1.put("created_at", currentTime);
                db.insert("choices", null, choice29_1);

                ContentValues choice30_1 = new ContentValues();
                choice30_1.put("dialog_id", 30);
                choice30_1.put("choice_text", "Reveal the truth about the conspiracy");
                choice30_1.put("next_dialog_id", 32);
                choice30_1.put("created_at", currentTime);
                db.insert("choices", null, choice30_1);

                ContentValues choice31_1 = new ContentValues();
                choice31_1.put("dialog_id", 31);
                choice31_1.put("choice_text", "Understand Lady Selene's role in the prophecy");
                choice31_1.put("next_dialog_id", 32);
                choice31_1.put("created_at", currentTime);
                db.insert("choices", null, choice31_1);

                // Final choices
                ContentValues choice32_1 = new ContentValues();
                choice32_1.put("dialog_id", 32);
                choice32_1.put("choice_text", "Embrace the ancient power and become the guardian");
                choice32_1.put("next_dialog_id", 33);
                choice32_1.put("created_at", currentTime);
                db.insert("choices", null, choice32_1);

                ContentValues choice32_2 = new ContentValues();
                choice32_2.put("dialog_id", 32);
                choice32_2.put("choice_text", "Reject the ancient power and forge a new path");
                choice32_2.put("next_dialog_id", 34);
                choice32_2.put("created_at", currentTime);
                db.insert("choices", null, choice32_2);

                // Add choices for dialog 33 (Embracing the power ending)
                ContentValues choice33_1 = new ContentValues();
                choice33_1.put("dialog_id", 33);
                choice33_1.put("choice_text", "Begin your journey as the realm's guardian");
                choice33_1.put("next_dialog_id", 1);
                choice33_1.put("created_at", currentTime);
                db.insert("choices", null, choice33_1);

                ContentValues choice33_2 = new ContentValues();
                choice33_2.put("dialog_id", 33);
                choice33_2.put("choice_text", "Start a new story");
                choice33_2.put("next_dialog_id", 1);
                choice33_2.put("created_at", currentTime);
                db.insert("choices", null, choice33_2);

                // Add choices for dialog 34 (Rejecting the power ending)
                ContentValues choice34_1 = new ContentValues();
                choice34_1.put("dialog_id", 34);
                choice34_1.put("choice_text", "Reject the ancient power and forge a new path");
                choice34_1.put("next_dialog_id", 1);
                choice34_1.put("created_at", currentTime);
                db.insert("choices", null, choice34_1);

                // Add choices for ending 501 (Sovereign of Rain)
                ContentValues choice501_1 = new ContentValues();
                choice501_1.put("dialog_id", 501);
                choice501_1.put("choice_text", "Begin a new journey");
                choice501_1.put("next_dialog_id", 1);
                choice501_1.put("created_at", currentTime);
                db.insert("choices", null, choice501_1);

                ContentValues choice501_2 = new ContentValues();
                choice501_2.put("dialog_id", 501);
                choice501_2.put("choice_text", "Reflect on your choices");
                choice501_2.put("next_dialog_id", 1);
                choice501_2.put("created_at", currentTime);
                db.insert("choices", null, choice501_2);

                // Add choices for ending 502 (Hidden Verse)
                ContentValues choice502_1 = new ContentValues();
                choice502_1.put("dialog_id", 502);
                choice502_1.put("choice_text", "Start another story");
                choice502_1.put("next_dialog_id", 1);
                choice502_1.put("created_at", currentTime);
                db.insert("choices", null, choice502_1);

                ContentValues choice502_2 = new ContentValues();
                choice502_2.put("dialog_id", 502);
                choice502_2.put("choice_text", "Contemplate the hidden verse");
                choice502_2.put("next_dialog_id", 1);
                choice502_2.put("created_at", currentTime);
                db.insert("choices", null, choice502_2);

                // Add choices for ending 503 (Judgment Broken)
                ContentValues choice503_1 = new ContentValues();
                choice503_1.put("dialog_id", 503);
                choice503_1.put("choice_text", "Begin anew");
                choice503_1.put("next_dialog_id", 1);
                choice503_1.put("created_at", currentTime);
                db.insert("choices", null, choice503_1);

                ContentValues choice503_2 = new ContentValues();
                choice503_2.put("dialog_id", 503);
                choice503_2.put("choice_text", "Ponder the broken judgment");
                choice503_2.put("next_dialog_id", 1);
                choice503_2.put("created_at", currentTime);
                db.insert("choices", null, choice503_2);

                // Add choices for ending 504 (Floodlight Reborn)
                ContentValues choice504_1 = new ContentValues();
                choice504_1.put("dialog_id", 504);
                choice504_1.put("choice_text", "Start a new chapter");
                choice504_1.put("next_dialog_id", 1);
                choice504_1.put("created_at", currentTime);
                db.insert("choices", null, choice504_1);

                ContentValues choice504_2 = new ContentValues();
                choice504_2.put("dialog_id", 504);
                choice504_2.put("choice_text", "Meditate on rebirth");
                choice504_2.put("next_dialog_id", 1);
                choice504_2.put("created_at", currentTime);
                db.insert("choices", null, choice504_2);

                // Verify data insertion
                Cursor dialogCursor = db.rawQuery("SELECT COUNT(*) FROM dialogs", null);
                dialogCursor.moveToFirst();
                int dialogCount = dialogCursor.getInt(0);
                dialogCursor.close();
                Log.d(TAG, "Total dialogs in database: " + dialogCount);

                Cursor choiceCursor = db.rawQuery("SELECT COUNT(*) FROM choices", null);
                choiceCursor.moveToFirst();
                int choiceCount = choiceCursor.getInt(0);
                choiceCursor.close();
                Log.d(TAG, "Total choices in database: " + choiceCount);

                // Verify specific choices for dialog 1
                Cursor dialog1Choices = db.rawQuery("SELECT COUNT(*) FROM choices WHERE dialog_id = 1", null);
                dialog1Choices.moveToFirst();
                int dialog1ChoiceCount = dialog1Choices.getInt(0);
                dialog1Choices.close();
                Log.d(TAG, "Choices for dialog 1: " + dialog1ChoiceCount);

                // Verify specific choices for dialog 13
                Cursor dialog13Choices = db.rawQuery("SELECT COUNT(*) FROM choices WHERE dialog_id = 13", null);
                dialog13Choices.moveToFirst();
                int dialog13ChoiceCount = dialog13Choices.getInt(0);
                dialog13Choices.close();
                Log.d(TAG, "Choices for dialog 13: " + dialog13ChoiceCount);

            } catch (Exception e) {
                Log.e(TAG, "Error creating initial data: " + e.getMessage(), e);
                throw e;
            }

            db.setTransactionSuccessful();
            Log.d(TAG, "Database creation completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating database: " + e.getMessage(), e);
            throw new RuntimeException("Database creation failed", e);
        } finally {
            db.endTransaction();
        }
    }

    private void validateAndRepairAfterLoad(SQLiteDatabase db) {
        try {
            // Check dialog count
            Cursor dialogCursor = db.rawQuery("SELECT COUNT(*) FROM dialogs", null);
            dialogCursor.moveToFirst();
            int dialogCount = dialogCursor.getInt(0);
            dialogCursor.close();

            // Check choice count
            Cursor choiceCursor = db.rawQuery("SELECT COUNT(*) FROM choices", null);
            choiceCursor.moveToFirst();
            int choiceCount = choiceCursor.getInt(0);
            choiceCursor.close();

            Log.d(TAG, "Loaded " + dialogCount + " dialogs and " + choiceCount + " choices");

            if (dialogCount == 0) {
                Log.e(TAG, "No dialogs loaded, creating emergency story");
                FallbackStoryCreator.createEmergencyStory(db);
            } else if (!FallbackStoryCreator.validateStoryStructure(db)) {
                Log.w(TAG, "Story structure invalid, attempting repair");
                verifyAndRepairStoryData();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in post-load validation", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        db.beginTransaction();
        try {
            // Handle different version upgrades sequentially
            if (oldVersion < 2) {
                        upgradeToVersion2(db);
            }
            if (oldVersion < 3) {
                        upgradeToVersion3(db);
            }
            if (oldVersion < 4) {
                // Add version 4 upgrades if any
                Log.d(TAG, "Upgrading to version 4 - no schema changes required");
            }
            if (oldVersion < 5) {
                // Add version 5 upgrades if any
                Log.d(TAG, "Upgrading to version 5 - no schema changes required");
            }
            if (oldVersion < 6) {
                // Add version 6 upgrades if any
                Log.d(TAG, "Upgrading to version 6 - no schema changes required");
            }

            // Always recreate indexes after upgrade
            for (String index : CREATE_INDEXES) {
                db.execSQL(index);
            }

            db.setTransactionSuccessful();
            Log.d(TAG, "Database upgrade completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database", e);
            throw new RuntimeException("Database upgrade failed", e);
        } finally {
            db.endTransaction();
        }
    }

    private void upgradeToVersion2(SQLiteDatabase db) {
        Log.d(TAG, "Upgrading to database version 2");
        try {
            // Add timestamp columns to tables with default value 0
            for (String upgrade : VERSION_2_UPGRADES) {
                db.execSQL(upgrade);
            }

            // Update existing records with current timestamp
            long currentTime = System.currentTimeMillis();
            db.execSQL("UPDATE progress SET last_updated = ?", new String[]{String.valueOf(currentTime)});
            db.execSQL("UPDATE dialogs SET created_at = ?", new String[]{String.valueOf(currentTime)});
            db.execSQL("UPDATE choices SET created_at = ?", new String[]{String.valueOf(currentTime)});

            Log.d(TAG, "Version 2 upgrade completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error during version 2 upgrade", e);
            throw e;
        }
    }

    private void upgradeToVersion3(SQLiteDatabase db) {
        Log.d(TAG, "Upgrading to database version 3");
        try {
            // Add new indexes
            for (String upgrade : VERSION_3_UPGRADES) {
                db.execSQL(upgrade);
            }

            Log.d(TAG, "Version 3 upgrade completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error during version 3 upgrade", e);
            throw e;
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Database operations with transaction support
    public boolean hasUsers() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT 1 FROM users LIMIT 1", null);
            boolean hasUsers = cursor != null && cursor.moveToFirst();
            Log.d(TAG, "hasUsers: " + hasUsers);
            return hasUsers;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if users exist", e);
            return false;
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

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT id, username FROM users ORDER BY id", null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                        String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                        if (username != null && !username.trim().isEmpty()) {
                            users.add(new User(id, username));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading user from cursor", e);
                        // Continue with next user instead of failing completely
                    }
                } while (cursor.moveToNext());
            }

            Log.d(TAG, "Retrieved " + users.size() + " users from database");
            return users;
        } catch (Exception e) {
            Log.e(TAG, "Error getting all users", e);
            return users; // Return empty list instead of throwing exception
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error closing cursor", e);
                }
            }
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
}
