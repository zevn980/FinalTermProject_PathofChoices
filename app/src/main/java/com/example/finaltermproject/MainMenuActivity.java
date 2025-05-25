package com.example.finaltermproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
    private Handler mainHandler;
    private boolean isActivityActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting MainMenuActivity");

        isActivityActive = true;
        mainHandler = new Handler(Looper.getMainLooper());

        try {
            setContentView(R.layout.activity_menu);
            Log.d(TAG, "onCreate: Layout set successfully");

            initializeDatabase();
            initializeViews();
            setupClickListeners();
            updateButtonStates();

        } catch (Exception e) {
            Log.e(TAG, "onCreate: Fatal error in MainMenuActivity", e);
            showErrorAndFinish("Error initializing menu: " + e.getMessage());
        }
    }

    private void initializeDatabase() {
        try {
            db = DatabaseHelper.getInstance(this);
            Log.d(TAG, "onCreate: Database initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private void initializeViews() {
        try {
            btnNewStory = findViewById(R.id.btnNewStory);
            btnContinue = findViewById(R.id.btnContinue);
            btnExit = findViewById(R.id.btnExit);
            userButton = findViewById(R.id.userButton);
            userPrompt = findViewById(R.id.userPrompt);

            if (btnNewStory == null || btnContinue == null || btnExit == null ||
                    userButton == null || userPrompt == null) {
                throw new RuntimeException("One or more required views not found");
            }

            Log.d(TAG, "onCreate: Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Failed to initialize views", e);
            throw new RuntimeException("View initialization failed", e);
        }
    }

    private void setupClickListeners() {
        // Start new story
        btnNewStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNewStoryClick();
            }
        });

        // Continue story
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleContinueClick();
            }
        });

        // Exit app
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d(TAG, "Exit button clicked");
                    finishAffinity(); // Closes all activities
                } catch (Exception e) {
                    Log.e(TAG, "Error exiting app", e);
                    finish();
                }
            }
        });

        // Manage users
        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleUserManagementClick();
            }
        });
    }

    private void handleNewStoryClick() {
        Log.d(TAG, "New Story button clicked");

        // Disable button to prevent multiple clicks
        btnNewStory.setEnabled(false);

        try {
            if (checkUserSelectedSafely()) {
                startGameActivity(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting new story", e);
            showToastSafely("Error starting new story: " + e.getMessage());
        } finally {
            // Re-enable button after delay
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (btnNewStory != null) {
                        btnNewStory.setEnabled(true);
                    }
                }
            }, 1000);
        }
    }

    private void handleContinueClick() {
        Log.d(TAG, "Continue button clicked");

        // Disable button to prevent multiple clicks
        btnContinue.setEnabled(false);

        try {
            if (checkUserSelectedSafely()) {
                startGameActivity(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error continuing story", e);
            showToastSafely("Error continuing story: " + e.getMessage());
        } finally {
            // Re-enable button after delay
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (btnContinue != null) {
                        btnContinue.setEnabled(true);
                    }
                }
            }, 1000);
        }
    }

    private void handleUserManagementClick() {
        try {
            Log.d(TAG, "User management button clicked");
            Intent intent = new Intent(MainMenuActivity.this, UserManagementActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening user management", e);
            showToastSafely("Error opening user management: " + e.getMessage());
        }
    }

    private void startGameActivity(boolean isNewStory) {
        try {
            Intent intent = new Intent(MainMenuActivity.this, GameActivity.class);
            intent.putExtra("new_story", isNewStory);
            startActivity(intent);
            Log.d(TAG, "GameActivity started successfully with new_story=" + isNewStory);
        } catch (Exception e) {
            Log.e(TAG, "Error starting GameActivity", e);
            throw new RuntimeException("Failed to start game", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityActive = true;
        try {
            updateButtonStates();
            Log.d(TAG, "onResume: Button states updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "onResume: Error updating button states", e);
            showToastSafely("Error updating menu");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityActive = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityActive = false;
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }

    private boolean checkUserSelectedSafely() {
        try {
            return checkUserSelected();
        } catch (Exception e) {
            Log.e(TAG, "Error in checkUserSelected", e);
            showToastSafely("Error checking user status. Please try again.");
            return false;
        }
    }

    private boolean checkUserSelected() {
        try {
            // Refresh user validation to ensure data consistency
            UserManager.refreshUserValidation(this);

            User currentUser = UserManager.getCurrentUser(this);
            if (currentUser == null) {
                Log.d(TAG, "No current user found");
                showToastSafely("Please select a player first!");

                // Automatically open user management
                try {
                    Intent intent = new Intent(this, UserManagementActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error opening user management from checkUserSelected", e);
                }
                return false;
            }

            // Validate that the database is accessible and has users
            if (db == null) {
                Log.e(TAG, "Database is null");
                showToastSafely("Database error. Please restart the app.");
                return false;
            }

            if (!db.hasUsers()) {
                Log.w(TAG, "No users found in database");
                UserManager.clearCurrentUser(this);
                showToastSafely("No players found. Please create a player.");

                try {
                    Intent intent = new Intent(this, UserManagementActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error opening user management for no users", e);
                }
                return false;
            }

            // Validate user dialog progress
            try {
                int dialogId = db.getUserDialogId(currentUser.getId());
                if (dialogId <= 0) {
                    Log.w(TAG, "Invalid dialog ID for user, resetting to 1");
                    db.updateUserProgress(currentUser.getId(), 1);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error validating user progress", e);
                // Continue anyway, GameActivity will handle this
            }

            Log.d(TAG, "User validation successful: " + currentUser.getUsername());
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Critical error in checkUserSelected", e);
            showToastSafely("Critical error checking user. Please restart the app.");
            return false;
        }
    }

    private void updateButtonStates() {
        try {
            boolean userSelected = UserManager.hasValidCurrentUser(this);
            User currentUser = UserManager.getCurrentUser(this);

            // Update button enabled states
            if (btnNewStory != null && btnContinue != null && userPrompt != null && userButton != null) {
                btnNewStory.setEnabled(userSelected);
                btnContinue.setEnabled(userSelected);
                userPrompt.setVisibility(userSelected ? View.GONE : View.VISIBLE);

                if (userSelected && currentUser != null) {
                    userButton.setText("Player: " + currentUser.getUsername());
                    Log.d(TAG, "updateButtonStates: Current user is " + currentUser.getUsername());
                } else {
                    userButton.setText("Select Player");
                    Log.d(TAG, "updateButtonStates: No user selected");
                }

                // Visual feedback for disabled buttons
                btnNewStory.setAlpha(userSelected ? 1.0f : 0.5f);
                btnContinue.setAlpha(userSelected ? 1.0f : 0.5f);
            }
        } catch (Exception e) {
            Log.e(TAG, "updateButtonStates: Error updating button states", e);
            // Set safe default states
            setSafeDefaultButtonStates();
        }
    }

    private void setSafeDefaultButtonStates() {
        try {
            if (btnNewStory != null) {
                btnNewStory.setEnabled(false);
                btnNewStory.setAlpha(0.5f);
            }
            if (btnContinue != null) {
                btnContinue.setEnabled(false);
                btnContinue.setAlpha(0.5f);
            }
            if (userButton != null) {
                userButton.setText("Select Player");
            }
            if (userPrompt != null) {
                userPrompt.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting safe default button states", e);
        }
    }

    private void showToastSafely(String message) {
        if (isActivityActive) {
            try {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e(TAG, "Error showing toast: " + message, e);
            }
        }
    }

    private void showErrorAndFinish(String message) {
        showToastSafely(message);
        finish();
    }
}