package com.example.finaltermproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {
    private static final String TAG = "MainMenuActivity";
    private Button btnNewStory, btnContinue, btnExit, userButton;
    private TextView userPrompt;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting MainMenuActivity");
        
        try {
            setContentView(R.layout.activity_menu);
            Log.d(TAG, "onCreate: Layout set successfully");

            try {
                db = DatabaseHelper.getInstance(this);
                Log.d(TAG, "onCreate: Database initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "onCreate: Failed to initialize database", e);
                Toast.makeText(this, "Error initializing database", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Hook buttons to their XML IDs
            try {
                btnNewStory = findViewById(R.id.btnNewStory);
                btnContinue = findViewById(R.id.btnContinue);
                btnExit = findViewById(R.id.btnExit);
                userButton = findViewById(R.id.userButton);
                userPrompt = findViewById(R.id.userPrompt);
                Log.d(TAG, "onCreate: Buttons initialized successfully");
            } catch (Exception e) {
                Log.e(TAG, "onCreate: Failed to initialize buttons", e);
                Toast.makeText(this, "Error initializing UI", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Start new story
            btnNewStory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (checkUserSelected()) {
                            Intent intent = new Intent(MainMenuActivity.this, GameActivity.class);
                            intent.putExtra("new_story", true);
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "New Story click: Error starting game", e);
                        Toast.makeText(MainMenuActivity.this, "Error starting new story", Toast.LENGTH_LONG).show();
                    }
                }
            });

            // Continue story
            btnContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (checkUserSelected()) {
                            Intent intent = new Intent(MainMenuActivity.this, GameActivity.class);
                            intent.putExtra("new_story", false);
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Continue click: Error continuing game", e);
                        Toast.makeText(MainMenuActivity.this, "Error continuing story", Toast.LENGTH_LONG).show();
                    }
                }
            });

            // Exit app
            btnExit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishAffinity(); // Closes all activities
                }
            });

            // Manage users
            userButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(MainMenuActivity.this, UserManagementActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "User button click: Error opening user management", e);
                        Toast.makeText(MainMenuActivity.this, "Error opening user management", Toast.LENGTH_LONG).show();
                    }
                }
            });

            // Initial button states
            updateButtonStates();
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Fatal error in MainMenuActivity", e);
            Toast.makeText(this, "Fatal error in menu", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            updateButtonStates();
            Log.d(TAG, "onResume: Button states updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "onResume: Error updating button states", e);
            Toast.makeText(this, "Error updating menu", Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkUserSelected() {
        try {
            User currentUser = UserManager.getCurrentUser(this);
            if (currentUser == null) {
                Toast.makeText(this, "Please select a player first!", Toast.LENGTH_LONG).show();
                // Automatically open user management
                Intent intent = new Intent(this, UserManagementActivity.class);
                startActivity(intent);
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "checkUserSelected: Error checking current user", e);
            Toast.makeText(this, "Error checking user", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void updateButtonStates() {
        try {
            User currentUser = UserManager.getCurrentUser(this);
            boolean userSelected = currentUser != null;

            btnNewStory.setEnabled(userSelected);
            btnContinue.setEnabled(userSelected);
            userPrompt.setVisibility(userSelected ? View.GONE : View.VISIBLE);

            if (userSelected) {
                userButton.setText("Player: " + currentUser.getUsername());
                Log.d(TAG, "updateButtonStates: Current user is " + currentUser.getUsername());
            } else {
                userButton.setText("Select Player");
                Log.d(TAG, "updateButtonStates: No user selected");
            }

            // Visual feedback for disabled buttons
            btnNewStory.setAlpha(userSelected ? 1.0f : 0.5f);
            btnContinue.setAlpha(userSelected ? 1.0f : 0.5f);
        } catch (Exception e) {
            Log.e(TAG, "updateButtonStates: Error updating button states", e);
            // Set safe default states
            btnNewStory.setEnabled(false);
            btnContinue.setEnabled(false);
            userButton.setText("Select Player");
            userPrompt.setVisibility(View.VISIBLE);
            btnNewStory.setAlpha(0.5f);
            btnContinue.setAlpha(0.5f);
        }
    }
}