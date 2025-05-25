package com.example.finaltermproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
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

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DB_NAME = "game.db";
    private static final int DB_VERSION = 1;  // Increment this when schema changes

    // Database creation SQL statements
    private static final String CREATE_USERS_TABLE = 
        "CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL UNIQUE);";
    private static final String CREATE_PROGRESS_TABLE = 
        "CREATE TABLE progress (user_id INTEGER, current_dialog_id INTEGER, " +
        "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE);";
    private static final String CREATE_DIALOGS_TABLE = 
        "CREATE TABLE dialogs (id INTEGER PRIMARY KEY, text TEXT NOT NULL);";
    private static final String CREATE_CHOICES_TABLE = 
        "CREATE TABLE choices (id INTEGER PRIMARY KEY AUTOINCREMENT, dialog_id INTEGER, " +
        "choice_text TEXT NOT NULL, next_dialog_id INTEGER, " +
        "FOREIGN KEY(dialog_id) REFERENCES dialogs(id) ON DELETE CASCADE, " +
        "FOREIGN KEY(next_dialog_id) REFERENCES dialogs(id));";

    // Singleton implementation using atomic reference
    private static final AtomicReference<DatabaseHelper> instance = new AtomicReference<>();
    private final WeakReference<Context> contextRef;

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
        this.contextRef = new WeakReference<>(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            Log.d(TAG, "Creating database tables...");
            db.execSQL(CREATE_USERS_TABLE);
            db.execSQL(CREATE_PROGRESS_TABLE);
            db.execSQL(CREATE_DIALOGS_TABLE);
            db.execSQL(CREATE_CHOICES_TABLE);
            
            // Enable foreign key support
            db.execSQL("PRAGMA foreign_keys = ON;");
            
            // Seed initial data
            Context context = contextRef.get();
            if (context != null) {
                executeSqlScript(db, context, "seed_story.sql");
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
            switch (oldVersion) {
                case 1:
                    // When upgrading from version 1 to 2, add new columns or tables here
                    // upgradeToVersion2(db);
                case 2:
                    // When upgrading from version 2 to 3, add changes here
                    // upgradeToVersion3(db);
                    break;
                default:
                    throw new IllegalStateException("Unknown database version: " + oldVersion);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database", e);
            throw new RuntimeException("Database upgrade failed", e);
        } finally {
            db.endTransaction();
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
            cursor = db.rawQuery("SELECT id, username FROM users", null);
            if (cursor.moveToFirst()) {
                do {
                    users.add(new User(cursor.getInt(0), cursor.getString(1)));
                } while (cursor.moveToNext());
            }
            return users;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void renameUser(int id, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("username", newName);
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
            // Progress will be automatically deleted due to ON DELETE CASCADE
            int rowsAffected = db.delete("users", "id = ?", 
                new String[]{String.valueOf(id)});
            
            if (rowsAffected == 0) {
                throw new IllegalStateException("User not found with id: " + id);
            }
            
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public long addUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("username", username);
            long userId = db.insertOrThrow("users", null, values);
            
            // Initialize progress for new user
            values.clear();
            values.put("user_id", userId);
            values.put("current_dialog_id", 1);
            db.insert("progress", null, values);
            
            db.setTransactionSuccessful();
            return userId;
        } finally {
            db.endTransaction();
        }
    }

    public void updateUserProgress(int userId, int newDialogId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("current_dialog_id", newDialogId);
            int rowsAffected = db.update("progress", values, "user_id = ?", 
                new String[]{String.valueOf(userId)});
            
            if (rowsAffected == 0) {
                // If no progress record exists, create one
                values.put("user_id", userId);
                db.insertOrThrow("progress", null, values);
            }
            
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Nullable
    public DialogEntry getDialogById(int dialogId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT id, text FROM dialogs WHERE id = ?",
                    new String[]{String.valueOf(dialogId)});
            
            if (cursor.moveToFirst()) {
                return new DialogEntry(cursor.getInt(0), cursor.getString(1));
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void executeSqlScript(SQLiteDatabase db, Context context, String filename) {
        BufferedReader reader = null;
        try {
            Log.d(TAG, "Executing SQL script: " + filename);
            InputStream is = context.getAssets().open(filename);
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder statement = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) continue;

                statement.append(line);
                if (line.endsWith(";")) {
                    db.execSQL(statement.toString());
                    statement.setLength(0);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error executing SQL script: " + filename, e);
            throw new RuntimeException("Failed to execute SQL script: " + filename, e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error closing reader", e);
            }
        }
    }

    // Helper method to check database integrity
    public boolean checkDatabaseIntegrity() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA integrity_check", null);
            if (cursor.moveToFirst()) {
                String result = cursor.getString(0);
                return "ok".equalsIgnoreCase(result);
            }
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public List<Choice> getChoicesForDialog(int dialogId) {
        List<Choice> choices = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                "SELECT id, dialog_id, choice_text, next_dialog_id FROM choices WHERE dialog_id = ?",
                new String[]{String.valueOf(dialogId)}
            );
            
            if (cursor.moveToFirst()) {
                do {
                    choices.add(new Choice(
                        cursor.getInt(0),  // id
                        cursor.getInt(1),  // dialog_id
                        cursor.getString(2),  // choice_text
                        cursor.getInt(3)   // next_dialog_id
                    ));
                } while (cursor.moveToNext());
            }
            return choices;
        } finally {
            if (cursor != null) {
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
        Cursor checkCursor = null;
        
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
        SQLiteDatabase db = this.getReadableDatabase();
        String backupPath = context.getFilesDir() + "/backup_" + System.currentTimeMillis() + ".db";
        
        try {
            db.rawQuery("VACUUM INTO ?", new String[]{backupPath});
            Log.d(TAG, "Database backed up successfully to: " + backupPath);
        } catch (Exception e) {
            Log.e(TAG, "Failed to backup database: " + e.getMessage());
        }
    }

    // Helper method to validate story consistency
    public boolean validateStoryConsistency() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            // Check for dialogs without choices (dead ends that aren't intentional)
            cursor = db.rawQuery(
                "SELECT d.id FROM dialogs d LEFT JOIN choices c ON d.id = c.dialog_id " +
                "WHERE c.id IS NULL AND d.id != (SELECT MAX(id) FROM dialogs)",
                null
            );
            
            boolean hasDeadEnds = cursor.moveToFirst();
            if (hasDeadEnds) {
                Log.w(TAG, "Found dialogs without choices (potential dead ends)");
                return false;
            }
            
            // Check for circular references
            cursor = db.rawQuery(
                "WITH RECURSIVE story_path(id, next_id, depth) AS (" +
                "  SELECT dialog_id, next_dialog_id, 1 FROM choices " +
                "  UNION ALL " +
                "  SELECT sp.id, c.next_dialog_id, sp.depth + 1 " +
                "  FROM story_path sp " +
                "  JOIN choices c ON sp.next_id = c.dialog_id " +
                "  WHERE sp.depth < 100" +
                ") " +
                "SELECT DISTINCT id FROM story_path " +
                "GROUP BY id HAVING COUNT(*) > 100",
                null
            );
            
            boolean hasCircularRefs = cursor.moveToFirst();
            if (hasCircularRefs) {
                Log.w(TAG, "Found potential circular references in story paths");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error validating story consistency: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
