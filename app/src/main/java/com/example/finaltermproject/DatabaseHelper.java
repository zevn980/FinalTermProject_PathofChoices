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

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DB_NAME = "game.db";
    private static final int DB_VERSION = 2;  // Increment this when schema changes

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
            Log.d(TAG, "Creating database tables...");
            
            // Enable foreign key support first
            db.execSQL("PRAGMA foreign_keys = ON;");
            
            // Create tables in correct order due to foreign key constraints
            db.execSQL(CREATE_USERS_TABLE);
            db.execSQL(CREATE_DIALOGS_TABLE.replace("TIMESTAMP DEFAULT CURRENT_TIMESTAMP", "INTEGER DEFAULT 0"));
            db.execSQL(CREATE_PROGRESS_TABLE.replace("TIMESTAMP DEFAULT CURRENT_TIMESTAMP", "INTEGER DEFAULT 0"));
            db.execSQL(CREATE_CHOICES_TABLE.replace("TIMESTAMP DEFAULT CURRENT_TIMESTAMP", "INTEGER DEFAULT 0"));

            // Create indexes
            for (String index : CREATE_INDEXES) {
                db.execSQL(index);
            }

            // Seed initial data
            if (applicationContext != null) {
                executeSqlScript(db, applicationContext, "seed_story.sql");
                
                // Set initial timestamps
                long currentTime = System.currentTimeMillis();
                db.execSQL("UPDATE progress SET last_updated = ?", new String[]{String.valueOf(currentTime)});
                db.execSQL("UPDATE dialogs SET created_at = ?", new String[]{String.valueOf(currentTime)});
                db.execSQL("UPDATE choices SET created_at = ?", new String[]{String.valueOf(currentTime)});

                // Verify and repair story data
                verifyAndRepairStoryData();
            } else {
                Log.e(TAG, "ApplicationContext was null during database creation");
                throw new RuntimeException("ApplicationContext was null during database creation");
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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        db.beginTransaction();
        try {
            // Handle different version upgrades
            for (int version = oldVersion; version < newVersion; version++) {
                switch (version) {
                    case 1:
                        upgradeToVersion2(db);
                        break;
                    // Add more cases for future versions
                    default:
                        throw new IllegalStateException("Unknown database version: " + version);
                }
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

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Database operations with transaction support
    public boolean hasUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT 1 FROM users LIMIT 1", null);
            return cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Use parameterized query even though there are no parameters
            cursor = db.rawQuery("SELECT id, username FROM users ORDER BY id", null);
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
            throw new RuntimeException("Failed to get users", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
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

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Check if username already exists
            Cursor cursor = db.query("users", new String[]{"id"}, "username = ?",
                    new String[]{username}, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                cursor.close();
                throw new IllegalStateException("Username already exists: " + username);
            }
            if (cursor != null) {
                cursor.close();
            }

            ContentValues values = new ContentValues();
            values.put("username", username.trim());
            
            long userId = db.insertOrThrow("users", null, values);
            
            // Initialize progress for new user with first dialog
            if (userId != -1) {
                values = new ContentValues();
                values.put("user_id", userId);
                values.put("current_dialog_id", 1); // Assuming 1 is the first dialog ID
                db.insertOrThrow("progress", null, values);
            }
            
            db.setTransactionSuccessful();
            return userId;
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
            // Verify dialog exists
            Cursor dialogCheck = db.rawQuery("SELECT 1 FROM dialogs WHERE id = ?",
                    new String[]{String.valueOf(newDialogId)});
            boolean dialogExists = dialogCheck.moveToFirst();
            dialogCheck.close();

            if (!dialogExists) {
                // If the dialog doesn't exist, try to find the closest valid dialog
                Cursor nextValidDialog = db.rawQuery(
                    "SELECT id FROM dialogs WHERE id > ? ORDER BY id ASC LIMIT 1",
                    new String[]{String.valueOf(newDialogId - 1)}
                );

                if (nextValidDialog.moveToFirst()) {
                    newDialogId = nextValidDialog.getInt(0);
                    Log.w(TAG, "Redirecting to next valid dialog: " + newDialogId);
                } else {
                    // If no next dialog found, go to the first dialog
                    newDialogId = 1;
                    Log.w(TAG, "No valid next dialog found, returning to start");
                }
                nextValidDialog.close();
            }

            // Verify user exists
            Cursor userCheck = db.rawQuery("SELECT 1 FROM users WHERE id = ?",
                    new String[]{String.valueOf(userId)});
            boolean userExists = userCheck.moveToFirst();
            userCheck.close();

            if (!userExists) {
                throw new IllegalArgumentException("User does not exist: " + userId);
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
            logDatabaseOperation("getDialogById", "Dialog not found");
            return null;
        } catch (Exception e) {
            logDatabaseError("getDialogById", e);
            throw new RuntimeException("Failed to get dialog", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    private void executeSqlScript(SQLiteDatabase db, Context context, String filename) {
        if (context == null || filename == null) {
            throw new IllegalArgumentException("Context and filename cannot be null");
        }

        BufferedReader reader = null;
        InputStream inputStream = null;
        try {
            Log.d(TAG, "Executing SQL script: " + filename);
            inputStream = context.getAssets().open(filename);
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            StringBuilder statement = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) continue;

                statement.append(line);
                if (line.endsWith(";")) {
                    String sql = statement.toString();
                    try {
                        db.execSQL(sql);
                    } catch (SQLException e) {
                        Log.e(TAG, "Error executing SQL statement: " + sql, e);
                        throw e;
                    }
                    statement.setLength(0);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading SQL script: " + filename, e);
            throw new RuntimeException("Failed to read SQL script: " + filename, e);
        } catch (Exception e) {
            Log.e(TAG, "Error executing SQL script: " + filename, e);
            throw new RuntimeException("Failed to execute SQL script: " + filename, e);
        } finally {
            try {
                if (reader != null) reader.close();
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing resources", e);
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
                    "INNER JOIN dialogs d1 ON c.dialog_id = d1.id " +
                    "INNER JOIN dialogs d2 ON c.next_dialog_id = d2.id " +
                    "WHERE c.dialog_id = ? " +
                    "ORDER BY c.id",
                    new String[]{String.valueOf(dialogId)}
            );

            if (cursor.moveToFirst()) {
                do {
                    Choice choice = new Choice(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("dialog_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("choice_text")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("next_dialog_id"))
                    );
                    choices.add(choice);
                } while (cursor.moveToNext());
            }
            logDatabaseOperation("getChoicesForDialog", "Found " + choices.size() + " choices");
            return choices;
        } catch (Exception e) {
            logDatabaseError("getChoicesForDialog", e);
            throw new RuntimeException("Failed to get choices for dialog", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public int getUserDialogId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT current_dialog_id FROM progress WHERE user_id = ?",
                    new String[]{String.valueOf(userId)}
            );

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }

            // If no progress found, initialize it
            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("current_dialog_id", 1);
            db.insert("progress", null, values);

            return 1; // Default starting point
        } finally {
            if (cursor != null) {
                cursor.close();
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
        String backupFileName = String.format("backup_%s.db", timestamp);
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
                // Use try-with-resources to ensure proper channel closing
                try (FileChannel src = new FileInputStream(currentDB).getChannel();
                     FileChannel dst = new FileOutputStream(backupDB).getChannel()) {
                    dst.transferFrom(src, 0, src.size());
                }

                // Verify backup file exists and is readable
                if (!backupDB.canRead()) {
                    throw new IOException("Backup file not readable: " + backupPath);
                }

                // Clean up old backups (keep only last 5)
                File backupDir = new File(context.getFilesDir().toString());
                File[] backups = backupDir.listFiles((dir, name) ->
                        name.startsWith("backup_") && name.endsWith(".db"));
                if (backups != null && backups.length > 5) {
                    Arrays.sort(backups, (f1, f2) ->
                            Long.compare(f2.lastModified(), f1.lastModified()));
                    for (int i = 5; i < backups.length; i++) {
                        backups[i].delete();
                    }
                }

                Log.d(TAG, "Database backed up successfully to: " + backupPath);
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
    public void restoreFromBackup(Context context, String backupPath) {
        if (context == null || backupPath == null) {
            throw new IllegalArgumentException("Context and backup path cannot be null");
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Verify backup file exists and is readable
            java.io.File backupFile = new java.io.File(backupPath);
            if (!backupFile.exists() || !backupFile.canRead()) {
                throw new java.io.IOException("Backup file not accessible: " + backupPath);
            }

            // Close existing database connections
            db.close();

            // Copy backup file to database file
            java.io.File dbFile = context.getDatabasePath(DB_NAME);
            try (java.nio.channels.FileChannel source = new java.io.FileInputStream(backupFile).getChannel();
                 java.nio.channels.FileChannel destination = new java.io.FileOutputStream(dbFile).getChannel()) {
                destination.transferFrom(source, 0, source.size());
            }

            // Reopen database and verify integrity
            db = this.getWritableDatabase();
            if (!checkDatabaseIntegrity()) {
                throw new IllegalStateException("Database integrity check failed after restore");
            }

            db.setTransactionSuccessful();
            Log.d(TAG, "Database restored successfully from: " + backupPath);
        } catch (Exception e) {
            Log.e(TAG, "Failed to restore database", e);
            throw new RuntimeException("Database restore failed", e);
        } finally {
            db.endTransaction();
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
            // Check if we have all required dialogs
            Cursor dialogCheck = db.rawQuery(
                "WITH required_ids AS (" +
                "  SELECT 1 as id UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 " +
                "  UNION SELECT 111 UNION SELECT 112 UNION SELECT 113 UNION SELECT 121 UNION SELECT 122 UNION SELECT 123 " +
                "  UNION SELECT 131 UNION SELECT 132 UNION SELECT 133 UNION SELECT 141 UNION SELECT 142 UNION SELECT 143 " +
                "  UNION SELECT 151 UNION SELECT 152 UNION SELECT 153 UNION SELECT 301 UNION SELECT 302 " +
                "  UNION SELECT 401 UNION SELECT 402 UNION SELECT 403 UNION SELECT 404 " +
                "  UNION SELECT 501 UNION SELECT 502 UNION SELECT 503 UNION SELECT 504" +
                ") " +
                "SELECT r.id FROM required_ids r LEFT JOIN dialogs d ON r.id = d.id " +
                "WHERE d.id IS NULL",
                null
            );

            boolean needsRepair = dialogCheck.moveToFirst();
            dialogCheck.close();

            if (needsRepair) {
                Log.w(TAG, "Missing dialogs detected. Attempting to repair...");
                
                // Clear existing data
                db.execSQL("DELETE FROM choices");
                db.execSQL("DELETE FROM dialogs");
                
                // Reset sequences
                db.execSQL("DELETE FROM sqlite_sequence WHERE name='dialogs' OR name='choices'");
                
                // First execute only dialog inserts from seed_story.sql
                if (applicationContext != null) {
                    try {
                        // First pass: Insert dialogs
                        executeSqlStatementsFromFile(db, applicationContext, "seed_story.sql", true);
                        
                        // Second pass: Insert choices
                        executeSqlStatementsFromFile(db, applicationContext, "seed_story.sql", false);
                        
                        Log.d(TAG, "Story data reseeded successfully");
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading seed_story.sql", e);
                        throw new RuntimeException("Failed to read story data", e);
                    }
                } else {
                    throw new RuntimeException("ApplicationContext is null during story data repair");
                }
            }

            // Verify choices point to valid dialogs
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
                db.execSQL("DELETE FROM choices WHERE dialog_id NOT IN (SELECT id FROM dialogs) OR next_dialog_id NOT IN (SELECT id FROM dialogs)");
            }
            choiceCheck.close();

            db.setTransactionSuccessful();
            Log.d(TAG, "Story data verification and repair completed");
        } catch (Exception e) {
            Log.e(TAG, "Error during story data verification", e);
            throw new RuntimeException("Failed to verify story data", e);
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
}
