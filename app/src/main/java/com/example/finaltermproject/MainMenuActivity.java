package com.example.finaltermproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    private Button btnNewStory, btnContinue, btnExit, userButton;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        db = DatabaseHelper.getInstance(this);

        // Hook buttons to their XML IDs
        btnNewStory = findViewById(R.id.btnNewStory);
        btnContinue = findViewById(R.id.btnContinue);
        btnExit = findViewById(R.id.btnExit);
        userButton = findViewById(R.id.userButton);

        // Start new story
        btnNewStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkUserSelected()) {
                    Intent intent = new Intent(MainMenuActivity.this, GameActivity.class);
                    intent.putExtra("new_story", true);
                    startActivity(intent);
                }
            }
        });

        // Continue story
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkUserSelected()) {
                    Intent intent = new Intent(MainMenuActivity.this, GameActivity.class);
                    intent.putExtra("new_story", false);
                    startActivity(intent);
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
                Intent intent = new Intent(MainMenuActivity.this, UserManagementActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateButtonStates();
    }

    private boolean checkUserSelected() {
        User currentUser = UserManager.getCurrentUser(this);
        if (currentUser == null) {
            Toast.makeText(this, "Please select a user first!", Toast.LENGTH_LONG).show();
            // Automatically open user management
            Intent intent = new Intent(this, UserManagementActivity.class);
            startActivity(intent);
            return false;
        }
        return true;
    }

    private void updateButtonStates() {
        User currentUser = UserManager.getCurrentUser(this);
        boolean userSelected = currentUser != null;

        btnNewStory.setEnabled(userSelected);
        btnContinue.setEnabled(userSelected);

        if (userSelected) {
            userButton.setText("User: " + currentUser.getUsername());
        } else {
            userButton.setText("Select User");
        }
    }
}