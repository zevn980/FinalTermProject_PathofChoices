package com.example.finaltermproject;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;

public class GameActivity extends AppCompatActivity {

    private TextView textDialog;
    private LinearLayout choiceContainer;
    private DatabaseHelper db;
    private User currentUser;
    private int currentDialogId;
    private MediaPlayer mediaPlayer;
    private boolean isChoiceClickable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize MediaPlayer
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.game_music);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(0f, 0f);
                mediaPlayer.start();
                fadeInMusic();
            } else {
                Log.e("GameActivity", "Failed to load game music.");
            }
        } catch (Exception e) {
            Log.e("GameActivity", "Error creating MediaPlayer: " + e.getMessage());
        }

        // Initialize UI components
        textDialog = findViewById(R.id.textDialog);
        choiceContainer = findViewById(R.id.choiceContainer);

        // Initialize database
        db = DatabaseHelper.getInstance(this);

        // Check for dangling dialog references
        List<Integer> missingDialogs = db.getDanglingNextDialogIds();
        if (missingDialogs.isEmpty()) {
            Log.d("DEBUG", "All next_dialog_ids have valid dialogs.");
        } else {
            Log.w("DEBUG", "Missing dialogs for the following next_dialog_ids: " + missingDialogs);
        }

        int count = db.getDialogCount();
        Log.d("DEBUG", "Dialog count: " + count);

        // Get current user
        currentUser = UserManager.getCurrentUser(this);
        Log.d("DEBUG", "Current user: " + (currentUser != null ? currentUser.getUsername() + " (ID: " + currentUser.getId() + ")" : "NULL"));

        if (currentUser == null) {
            Toast.makeText(this, "No user selected. Please select a user first.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Check if this is a new story or continue
        boolean isNewStory = getIntent().getBooleanExtra("new_story", false);

        if (isNewStory) {
            // Reset user progress to beginning
            db.updateUserProgress(currentUser.getId(), 1);
            currentDialogId = 1;
            Log.d("DEBUG", "Starting new story for user: " + currentUser.getUsername());
        } else {
            // Load saved progress
            currentDialogId = db.getUserDialogId(currentUser.getId());
            Log.d("DEBUG", "Continuing story from dialog ID: " + currentDialogId);
        }

        loadDialog(currentDialogId);
    }

    private void loadDialog(int dialogId) {
        Log.d("DEBUG", "=== loadDialog called with ID: " + dialogId + " ===");
        Log.d("DEBUG", "Current user: " + (currentUser != null ? currentUser.getUsername() + " (ID: " + currentUser.getId() + ")" : "NULL"));
        Log.d("DEBUG", "Database instance: " + (db != null ? "EXISTS" : "NULL"));

        DialogEntry dialog = db.getDialogById(dialogId);
        Log.d("DEBUG", "Dialog result: " + (dialog != null ? "FOUND - " + dialog.getText().substring(0, Math.min(50, dialog.getText().length())) + "..." : "NULL"));

        if (dialog == null) {
            Log.d("DEBUG", "DialogEntry is null for ID: " + dialogId);
            textDialog.setText("The End.");
            hideChoices();

            Button backButton = new Button(this);
            backButton.setText("Back to Menu");
            backButton.setOnClickListener(v -> {
                fadeOutMusicAndFinish(() -> finish());
            });
            choiceContainer.addView(backButton);
            return;
        } else {
            Log.d("DEBUG", "Loaded dialog ID " + dialog.getId() + ": " + dialog.getText());
        }

        textDialog.setAlpha(0f);
        textDialog.setText(dialog.getText());
        textDialog.animate().alpha(1f).setDuration(300).start();
        List<Choice> choices = db.getChoicesForDialog(dialogId);
        Log.d("DEBUG", "Found " + choices.size() + " choices for dialog " + dialogId);
        showChoices(choices);
    }

    private void showChoices(List<Choice> choices) {
        choiceContainer.removeAllViews(); // Clear previous buttons

        for (Choice choice : choices) {
            Button choiceButton = new Button(this);
            choiceButton.setText(choice.getChoiceText());
            choiceButton.setBackgroundResource(R.drawable.choice_button_background);
            choiceButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            choiceButton.setTextSize(16);
            choiceButton.setAllCaps(false);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 16, 0, 0); // spacing between buttons
            choiceButton.setLayoutParams(params);

            choiceButton.setOnClickListener(v -> {
                if (!isChoiceClickable) return;
                isChoiceClickable = false;

                Log.d("DEBUG", "Choice selected: " + choice.getChoiceText() + " -> Dialog ID: " + choice.getNextDialogId());
                db.updateUserProgress(currentUser.getId(), choice.getNextDialogId());
                currentDialogId = choice.getNextDialogId();
                loadDialog(currentDialogId);

                // Re-enable after short delay
                new Handler().postDelayed(() -> isChoiceClickable = true, 500);
            });

            choiceContainer.addView(choiceButton);
        }
    }

    private void hideChoices() {
        choiceContainer.removeAllViews();
    }

    private void fadeInMusic() {
        if (mediaPlayer == null) return;

        final float maxVolume = 1.0f;
        final int duration = 3000;
        final int step = 100;
        final float delta = maxVolume / (duration / step);

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            float volume = 0f;

            @Override
            public void run() {
                if (mediaPlayer != null && volume < maxVolume) {
                    volume += delta;
                    mediaPlayer.setVolume(volume, volume);
                    handler.postDelayed(this, step);
                } else if (mediaPlayer != null) {
                    mediaPlayer.setVolume(maxVolume, maxVolume);
                }
            }
        }, step);
    }

    private void fadeOutMusicAndFinish(Runnable onComplete) {
        if (mediaPlayer == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        final int duration = 2000;
        final int step = 100;
        final float delta = 1.0f / (duration / step);

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            float volume = 1.0f;

            @Override
            public void run() {
                if (mediaPlayer != null && volume > 0f) {
                    volume -= delta;
                    mediaPlayer.setVolume(volume, volume);
                    handler.postDelayed(this, step);
                } else {
                    if (mediaPlayer != null) {
                        try {
                            mediaPlayer.stop();
                            mediaPlayer.release();
                        } catch (Exception e) {
                            Log.e("GameActivity", "Error stopping MediaPlayer: " + e.getMessage());
                        }
                        mediaPlayer = null;
                    }
                    if (onComplete != null) onComplete.run();
                }
            }
        }, step);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e("GameActivity", "Error releasing MediaPlayer: " + e.getMessage());
            }
            mediaPlayer = null;
        }
    }
}