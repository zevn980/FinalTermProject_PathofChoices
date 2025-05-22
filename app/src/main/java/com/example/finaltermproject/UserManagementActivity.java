package com.example.finaltermproject;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        db = DatabaseHelper.getInstance(this);

        recyclerView = findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        users = db.getAllUsers();  // you'll implement this in DatabaseHelper
        adapter = new UserAdapter(users, new UserAdapter.OnUserActionListener() {
            @Override
            public void onSelect(User user) {
                UserManager.setCurrentUser(UserManagementActivity.this, user);
                Toast.makeText(UserManagementActivity.this, "Selected: " + user.getUsername(), Toast.LENGTH_SHORT).show();
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

        Button backBtn = findViewById(R.id.btnBackToMenu);
        backBtn.setOnClickListener(v -> finish());
    }

    private void showRenameDialog(User user) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(user.getUsername());

        new AlertDialog.Builder(this)
                .setTitle("Rename User")
                .setView(input)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        db.renameUser(user.getId(), newName); // implement this in DB
                        reloadUsers();
                        Toast.makeText(this, "User renamed.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmation(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + user.getUsername() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deleteUser(user.getId()); // implement this in DB
                    if (UserManager.getCurrentUser(this) != null &&
                            UserManager.getCurrentUser(this).getId() == user.getId()) {
                        UserManager.clearCurrentUser(this);
                    }
                    reloadUsers();
                    Toast.makeText(this, "User deleted.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void reloadUsers() {
        users = db.getAllUsers();
        adapter.updateData(users);
    }
}
