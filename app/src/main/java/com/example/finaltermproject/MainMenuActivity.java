package com.example.finaltermproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    private Button btnNewStory, btnContinue, btnExit, userButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu); // Make sure this XML file exists and is correct

        // Hook buttons to their XML IDs
        btnNewStory = findViewById(R.id.btnNewStory);
        btnContinue = findViewById(R.id.btnContinue);
        btnExit = findViewById(R.id.btnExit);
        userButton = findViewById(R.id.userButton);

        // Start new story
        btnNewStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, GameActivity.class);
                startActivity(intent);
            }
        });

        // Continue story (you can later add logic here to resume a saved game)
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // For now, same as new story
                Intent intent = new Intent(MainMenuActivity.this, GameActivity.class);
                startActivity(intent);
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
}
