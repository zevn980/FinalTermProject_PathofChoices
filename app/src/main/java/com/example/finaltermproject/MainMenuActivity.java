package com.example.finaltermproject;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    private ImageButton userButton;
    private Button btnNewStory, btnContinue, btnExit;

    private MediaPlayer mediaPlayer;
    private final float maxVolume = 1.0f;
    private boolean isTransitioning = false;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        userButton = findViewById(R.id.userButton);
        btnNewStory = findViewById(R.id.btnNewStory);
        btnContinue = findViewById(R.id.btnContinue);
        btnExit = findViewById(R.id.btnExit);

        // Setup and play background music
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.menu_music);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(0f, 0f);
                mediaPlayer.start();
                fadeInMusic();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Show/hide User button
        boolean usersExist = DatabaseHelper.getInstance(this).hasUsers();
        if (!usersExist) {
            userButton.setVisibility(View.GONE);
        }

        //User Management
        userButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, UserManagementActivity.class);
            startActivity(intent);
        });

        //New Story
        btnNewStory.setOnClickListener(v -> showNewStoryDialog());

        //Continue Game
        btnContinue.setOnClickListener(v -> {
            if (isTransitioning) return;
            isTransitioning = true;

            if (UserManager.getCurrentUser(this) == null) {
                showNoUserDialog();
                isTransitioning = false; // reset if no user selected
            } else {
                fadeOutMusicAndFinish(() -> startActivity(new Intent(this, GameActivity.class)));
            }
        });

        //Exit
        btnExit.setOnClickListener(v -> finishAffinity());
    }

    private void fadeInMusic() {
        final int fadeDuration = 3000; // milliseconds
        final int fadeStep = 100;
        final float volumeStep = maxVolume / (fadeDuration / fadeStep);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            float volume = 0f;

            @Override
            public void run() {
                if (volume < maxVolume) {
                    volume += volumeStep;
                    mediaPlayer.setVolume(volume, volume);
                    handler.postDelayed(this, fadeStep);
                } else {
                    mediaPlayer.setVolume(maxVolume, maxVolume);
                }
            }
        }, fadeStep);
    }

    private void fadeOutMusicAndFinish(Runnable afterFade) {
        final int fadeDuration = 2000;
        final int fadeStep = 100;
        final float volumeStep = maxVolume / (fadeDuration / fadeStep);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            float volume = maxVolume;

            @Override
            public void run() {
                if (volume > 0f) {
                    volume -= volumeStep;
                    mediaPlayer.setVolume(volume, volume);
                    handler.postDelayed(this, fadeStep);
                } else {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    if (afterFade != null) afterFade.run();
                }
            }
        }, fadeStep);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void showNoUserDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No User Selected")
                .setMessage("Please create or select a user before continuing.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showNewStoryDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_new_story, null);
        EditText editName = dialogView.findViewById(R.id.editUserName);
        Button btnStart = dialogView.findViewById(R.id.btnStartNewStory);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnStart.setOnClickListener(v -> {
            String username = editName.getText().toString().trim();

            if (username.isEmpty()) {
                editName.setError("Please enter a name");
                return;
            }

            long userId = DatabaseHelper.getInstance(this).addUser(username);
            if (userId == -1) {
                editName.setError("Username already exists");
            } else {
                User newUser = new User((int) userId, username);
                UserManager.setCurrentUser(this, newUser);

                // Optionally initialize progress (set first dialog)
                DatabaseHelper.getInstance(this).initializeUserProgress((int) userId);

                dialog.dismiss();

                // Start game activity
                Intent intent = new Intent(this, GameActivity.class);
                startActivity(intent);
            }
        });

        dialog.show();
    }
}
