package com.example.finaltermproject;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import java.util.List;
import java.util.ArrayList;

public class UserManagementActivity extends AppCompatActivity {
    private static final String TAG = "UserManagementActivity";

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> users;
    private DatabaseHelper db;
    private TextView emptyStateMessage;
    private Button btnCreateUser;
    private Handler mainHandler;
    private boolean isActivityActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isActivityActive = true;
        mainHandler = new Handler(Looper.getMainLooper());

        try {
            setContentView(R.layout.activity_user_management);
            Log.d(TAG, "Layout set successfully");

            initializeDatabase();
            initializeViews();
            setupClickListeners();
            loadUsers();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            showErrorAndFinish("Error initializing user management: " + e.getMessage());
        }
    }

    private void initializeDatabase() {
        try {
            db = DatabaseHelper.getInstance(this);
            if (db == null) {
                throw new RuntimeException("Failed to initialize database");
            }
            Log.d(TAG, "Database initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private void initializeViews() {
        try {
            recyclerView = findViewById(R.id.recyclerViewUsers);
            emptyStateMessage = findViewById(R.id.emptyStateMessage);
            btnCreateUser = findViewById(R.id.btnCreateUser);

            if (recyclerView == null || emptyStateMessage == null || btnCreateUser == null) {
                throw new RuntimeException("Required views not found");
            }

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            throw new RuntimeException("View initialization failed", e);
        }
    }

    private void setupClickListeners() {
        try {
            // Set up Create User button
            btnCreateUser.setOnClickListener(v -> {
                try {
                    showCreateUserDialog();
                } catch (Exception e) {
                    Log.e(TAG, "Error in create user click", e);
                    showToastSafely("Error opening create user dialog");
                }
            });

            Button backBtn = findViewById(R.id.btnBackToMenu);
            if (backBtn != null) {
                backBtn.setOnClickListener(v -> {
                    try {
                        finish();
                    } catch (Exception e) {
                        Log.e(TAG, "Error finishing activity", e);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners", e);
            throw new RuntimeException("Click listener setup failed", e);
        }
    }

    private void loadUsers() {
        // Use background thread for database operations
        new Thread(() -> {
            try {
                List<User> loadedUsers = db.getAllUsers();

                runOnUiThread(() -> {
                    try {
                        users = loadedUsers != null ? loadedUsers : new ArrayList<>();
                        setupAdapter();
                        updateEmptyState();
                        Log.d(TAG, "Users loaded successfully: " + users.size() + " users");
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating UI with users", e);
                        showToastSafely("Error displaying users");
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error loading users from database", e);
                runOnUiThread(() -> {
                    users = new ArrayList<>();
                    setupAdapter();
                    updateEmptyState();
                    showToastSafely("Error loading users");
                });
            }
        }).start();
    }

    private void setupAdapter() {
        try {
            if (adapter == null) {
                adapter = new UserAdapter(users, new UserAdapter.OnUserActionListener() {
                    @Override
                    public void onSelect(User user) {
                        handleUserSelection(user);
                    }

                    @Override
                    public void onRename(User user) {
                        showRenameDialog(user);
                    }

                    @Override
                    public void onDelete(User user) {
                        showDeleteConfirmation(user);
                    }
                });
                recyclerView.setAdapter(adapter);
            } else {
                adapter.updateData(users);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up adapter", e);
            showToastSafely("Error setting up user list");
        }
    }

    private void handleUserSelection(User user) {
        try {
            UserManager.setCurrentUser(UserManagementActivity.this, user);
            showToastSafely("Selected: " + user.getUsername());
            Log.d(TAG, "User selected: " + user.getUsername());

            // Small delay before finishing to let user see the selection feedback
            mainHandler.postDelayed(() -> {
                try {
                    finish();
                } catch (Exception e) {
                    Log.e(TAG, "Error finishing after user selection", e);
                }
            }, 500);

        } catch (Exception e) {
            Log.e(TAG, "Error selecting user", e);
            showToastSafely("Error selecting user");
        }
    }

    private void updateEmptyState() {
        try {
            if (users == null || users.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyStateMessage.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyStateMessage.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating empty state", e);
        }
    }

    private void showCreateUserDialog() {
        try {
            EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setHint("Enter player name");

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Create New Player")
                    .setView(input)
                    .setPositiveButton("Create", null) // Set to null initially
                    .setNegativeButton("Cancel", null)
                    .create();

            dialog.setOnShowListener(dialogInterface -> {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(view -> {
                    String username = input.getText().toString().trim();
                    if (validateAndCreateUser(username, dialog)) {
                        dialog.dismiss();
                    }
                });
            });

            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing create user dialog", e);
            showToastSafely("Error opening create user dialog");
        }
    }

    private boolean validateAndCreateUser(String username, AlertDialog dialog) {
        if (username.isEmpty()) {
            showToastSafely("Player name cannot be empty");
            return false;
        }

        if (username.length() < 3) {
            showToastSafely("Player name must be at least 3 characters");
            return false;
        }

        if (username.length() > 30) {
            showToastSafely("Player name cannot be longer than 30 characters");
            return false;
        }

        // Disable button to prevent multiple clicks
        Button createButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        createButton.setEnabled(false);

        // Use background thread for database operation
        new Thread(() -> {
            try {
                long userId = db.addUser(username);
                User newUser = new User((int)userId, username);

                runOnUiThread(() -> {
                    try {
                        UserManager.setCurrentUser(this, newUser);
                        showToastSafely("Player created and selected!");
                        loadUsers(); // Refresh the list
                        Log.d(TAG, "User created successfully: " + username);
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating UI after user creation", e);
                        showToastSafely("User created but error updating display");
                    }
                });

            } catch (IllegalStateException e) {
                runOnUiThread(() -> {
                    showToastSafely("Player name already exists");
                    createButton.setEnabled(true);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error creating user", e);
                runOnUiThread(() -> {
                    showToastSafely("Error creating player");
                    createButton.setEnabled(true);
                });
            }
        }).start();

        return true;
    }

    private void showRenameDialog(User user) {
        try {
            EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(user.getUsername());
            input.selectAll();

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Rename Player")
                    .setView(input)
                    .setPositiveButton("Rename", null)
                    .setNegativeButton("Cancel", null)
                    .create();

            dialog.setOnShowListener(dialogInterface -> {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(view -> {
                    String newName = input.getText().toString().trim();
                    if (validateAndRenameUser(user, newName, dialog)) {
                        dialog.dismiss();
                    }
                });
            });

            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing rename dialog", e);
            showToastSafely("Error opening rename dialog");
        }
    }

    private boolean validateAndRenameUser(User user, String newName, AlertDialog dialog) {
        if (newName.isEmpty()) {
            showToastSafely("Player name cannot be empty");
            return false;
        }

        if (newName.equals(user.getUsername())) {
            return true; // No change needed
        }

        // Disable button to prevent multiple clicks
        Button renameButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        renameButton.setEnabled(false);

        // Use background thread for database operation
        new Thread(() -> {
            try {
                db.renameUser(user.getId(), newName);

                runOnUiThread(() -> {
                    try {
                        // Update current user if this is the selected user
                        User currentUser = UserManager.getCurrentUser(this);
                        if (currentUser != null && currentUser.getId() == user.getId()) {
                            UserManager.setCurrentUser(this, new User(user.getId(), newName));
                        }

                        loadUsers();
                        showToastSafely("Player renamed");
                        Log.d(TAG, "User renamed: " + user.getUsername() + " -> " + newName);
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating UI after rename", e);
                        showToastSafely("Renamed but error updating display");
                    }
                });

            } catch (IllegalStateException e) {
                runOnUiThread(() -> {
                    showToastSafely("Player name already exists");
                    renameButton.setEnabled(true);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error renaming user", e);
                runOnUiThread(() -> {
                    showToastSafely("Error renaming player");
                    renameButton.setEnabled(true);
                });
            }
        }).start();

        return true;
    }

    private void showDeleteConfirmation(User user) {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Player")
                    .setMessage("Are you sure you want to delete " + user.getUsername() + "?\n\nThis will permanently remove all progress for this player.")
                    .setPositiveButton("Delete", (dialog, which) -> deleteUser(user))
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing delete confirmation", e);
            showToastSafely("Error opening delete confirmation");
        }
    }

    private void deleteUser(User user) {
        // Use background thread for database operation
        new Thread(() -> {
            try {
                db.deleteUser(user.getId());

                runOnUiThread(() -> {
                    try {
                        // Clear current user if this was the selected user
                        User currentUser = UserManager.getCurrentUser(this);
                        if (currentUser != null && currentUser.getId() == user.getId()) {
                            UserManager.clearCurrentUser(this);
                        }

                        loadUsers();
                        showToastSafely("Player deleted");
                        Log.d(TAG, "User deleted: " + user.getUsername());
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating UI after delete", e);
                        showToastSafely("Deleted but error updating display");
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error deleting user", e);
                runOnUiThread(() -> {
                    showToastSafely("Error deleting player");
                });
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityActive = true;
        try {
            loadUsers();
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume", e);
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

    private void showToastSafely(String message) {
        if (isActivityActive) {
            try {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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