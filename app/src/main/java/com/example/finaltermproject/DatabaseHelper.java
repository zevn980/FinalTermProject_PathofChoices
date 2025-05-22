package com.example.finaltermproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "game.db";
    private static final int DB_VERSION = 1;

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null)
            instance = new DatabaseHelper(context.getApplicationContext());
        return instance;
    }

    private Context context;
    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DEBUG", "Creating tables...");
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL UNIQUE);");
        db.execSQL("CREATE TABLE progress (user_id INTEGER, current_dialog_id INTEGER, " +
                "FOREIGN KEY(user_id) REFERENCES users(id));");
        db.execSQL("CREATE TABLE dialogs (id INTEGER PRIMARY KEY, text TEXT NOT NULL);");
        db.execSQL("CREATE TABLE choices (id INTEGER PRIMARY KEY AUTOINCREMENT, dialog_id INTEGER, " +
                "choice_text TEXT NOT NULL, next_dialog_id INTEGER, " +
                "FOREIGN KEY(dialog_id) REFERENCES dialogs(id), " +
                "FOREIGN KEY(next_dialog_id) REFERENCES dialogs(id));");

        Log.d("DEBUG", "Seeding database...");
        executeSqlScript(db, context, "seed_story.sql");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop and recreate if upgrading schema
        db.execSQL("DROP TABLE IF EXISTS users;");
        db.execSQL("DROP TABLE IF EXISTS progress;");
        db.execSQL("DROP TABLE IF EXISTS dialogs;");
        db.execSQL("DROP TABLE IF EXISTS choices;");
        onCreate(db);
    }

    public boolean hasUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT 1 FROM users LIMIT 1", null).moveToFirst();
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, username FROM users", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String username = cursor.getString(1);
                users.add(new User(id, username));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return users;
    }

    public void renameUser(int id, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", newName);
        db.update("users", values, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteUser(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("users", "id = ?", new String[]{String.valueOf(id)});
        db.delete("progress", "user_id = ?", new String[]{String.valueOf(id)}); // cleanup progress
    }

    public long addUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        try {
            return db.insertOrThrow("users", null, values); // returns row ID
        } catch (Exception e) {
            return -1; // already exists
        }
    }

    public void initializeUserProgress(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("current_dialog_id", 1);
        db.insert("progress", null, values);
    }

    public int getUserDialogId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT current_dialog_id FROM progress WHERE user_id = ?",
                new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
        cursor.close();
        return 1; // fallback default
    }

    public void updateUserProgress(int userId, int newDialogId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("current_dialog_id", newDialogId);
        db.update("progress", values, "user_id = ?", new String[]{String.valueOf(userId)});
    }

    public DialogEntry getDialogById(int dialogId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, text FROM dialogs WHERE id = ?",
                new String[]{String.valueOf(dialogId)});
        if (cursor.moveToFirst()) {
            DialogEntry dialog = new DialogEntry(cursor.getInt(0), cursor.getString(1));
            cursor.close();
            return dialog;
        }
        Log.d("DEBUG", "No dialog found for ID " + dialogId);
        cursor.close();
        return null;
    }

    public List<Choice> getChoicesForDialog(int dialogId) {
        List<Choice> choices = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, dialog_id, choice_text, next_dialog_id FROM choices WHERE dialog_id = ?",
                new String[]{String.valueOf(dialogId)});
        if (cursor.moveToFirst()) {
            do {
                choices.add(new Choice(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getInt(3)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return choices;
    }

    private void executeSqlScript(SQLiteDatabase db, Context context, String filename) {
        try {
            Log.d("DEBUG", "Starting to seed story from " + filename);

            InputStream is = context.getAssets().open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder statement = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) continue; // skip comments

                statement.append(line);
                if (line.endsWith(";")) {
                    db.execSQL(statement.toString());
                    Log.d("DEBUG", "Executed SQL: " + statement);
                    statement.setLength(0); // reset
                }
            }

            reader.close();
            Log.d("DEBUG", "Finished seeding story.");
        } catch (Exception e) {
            Log.e("ERROR", "Failed to execute SQL script: " + e.getMessage());
        }
    }

    public int getDialogCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM dialogs", null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public List<Integer> getDanglingNextDialogIds() {
        List<Integer> missingDialogIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Get all distinct next_dialog_id values from choices
        Cursor cursor = db.rawQuery(
                "SELECT DISTINCT next_dialog_id FROM choices", null);

        if (cursor.moveToFirst()) {
            do {
                int nextId = cursor.getInt(0);

                // Check if that dialog exists
                Cursor check = db.rawQuery(
                        "SELECT 1 FROM dialogs WHERE id = ?", new String[]{String.valueOf(nextId)});

                if (!check.moveToFirst()) {
                    missingDialogIds.add(nextId);
                }
                check.close();

            } while (cursor.moveToNext());
        }
        cursor.close();

        return missingDialogIds;
    }
}
