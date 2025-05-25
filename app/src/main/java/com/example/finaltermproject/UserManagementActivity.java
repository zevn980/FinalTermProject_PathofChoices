package com.example.finaltermproject;

import android.content.DialogInterface;
import android.os.Bundle;
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
import java.util.List;

public class UserManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> users;
    private DatabaseHelper db;
    private TextView emptyStateMessage;
    private Button btnCreateUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        db = DatabaseHelper.getInstance(this);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewUsers);
        emptyStateMessage = findViewById(R.id.emptyStateMessage);
        btnCreateUser = findViewById(R.id.btnCreateUser);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up Create User button
        btnCreateUser.setOnClickListener(v -> showCreateUserDialog());

        // Load users and set up adapter
        loadUsers();

        Button backBtn = findViewById(R.id.btnBackToMenu);
        backBtn.setOnClickListener(v -> finish());
    }

    private void loadUsers() {
        users = db.getAllUsers();
        adapter = new UserAdapter(users, new UserAdapter.OnUserActionListener() {
            @Override
            public void onSelect(User user) {
                UserManager.setCurrentUser(UserManagementActivity.this, user);
                Toast.makeText(UserManagementActivity.this, "Selected: " + user.getUsername(), Toast.LENGTH_SHORT).show();
                finish(); // Return to main menu after selection
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
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (users.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateMessage.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateMessage.setVisibility(View.GONE);
        }
    }

    private void showCreateUserDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter player name");

        new AlertDialog.Builder(this)
                .setTitle("Create New Player")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String username = input.getText().toString().trim();
                    if (!username.isEmpty()) {
                        try {
                            long userId = db.addUser(username);
                            User newUser = new User((int)userId, username);
                            UserManager.setCurrentUser(this, newUser);
                            Toast.makeText(this, "Player created and selected!", Toast.LENGTH_SHORT).show();
                            loadUsers();
                        } catch (IllegalStateException e) {
                            Toast.makeText(this, "Player name already exists", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(this, "Error creating player", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Player name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRenameDialog(User user) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(user.getUsername());

        new AlertDialog.Builder(this)
                .setTitle("Rename Player")
                .setView(input)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        try {
                            db.renameUser(user.getId(), newName);
                            loadUsers();
                            Toast.makeText(this, "Player renamed", Toast.LENGTH_SHORT).show();
                        } catch (IllegalStateException e) {
                            Toast.makeText(this, "Player name already exists", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(this, "Error renaming player", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Player name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmation(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Player")
                .setMessage("Are you sure you want to delete " + user.getUsername() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    try {
                        db.deleteUser(user.getId());
                        if (UserManager.getCurrentUser(this) != null &&
                                UserManager.getCurrentUser(this).getId() == user.getId()) {
                            UserManager.clearCurrentUser(this);
                        }
                        loadUsers();
                        Toast.makeText(this, "Player deleted", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error deleting player", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }
}
