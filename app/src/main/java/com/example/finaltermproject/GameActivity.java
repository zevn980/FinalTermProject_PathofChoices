package com.example.finaltermproject;

import android.media.MediaPlayer;
import android.media.AudioManager;
import android.media.AudioAttributes;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import android.content.res.Configuration;
import android.os.PersistableBundle;
import android.view.WindowManager;
import android.content.pm.ActivityInfo;
import android.widget.ImageView;
import androidx.core.content.res.ResourcesCompat;

import java.util.List;

public class GameActivity extends AppCompatActivity {
    private static final String TAG = "GameActivity";
    private static final String KEY_DIALOG_ID = "current_dialog_id";
    private static final String KEY_MUSIC_VOLUME = "music_volume";
    private static final String KEY_IS_CHOICE_CLICKABLE = "is_choice_clickable";

    private TextView textDialog;
    private LinearLayout choiceContainer;
    private ProgressBar loadingIndicator;
    private DatabaseHelper db;
    private User currentUser;
    private int currentDialogId;
    private MediaPlayer mediaPlayer;
    private boolean isChoiceClickable = true;
    private float currentMusicVolume = 0f;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Keep screen on during gameplay
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // Lock to portrait mode for consistent UI
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Initialize UI components
        textDialog = findViewById(R.id.textDialog);
        choiceContainer = findViewById(R.id.choiceContainer);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        // Initialize database
        db = DatabaseHelper.getInstance(this);

        // Validate story consistency
        if (!db.validateStoryConsistency()) {
            Toast.makeText(this, "Warning: Story consistency issues detected", Toast.LENGTH_LONG).show();
        }

        // Get current user
        currentUser = UserManager.getCurrentUser(this);
        if (currentUser == null) {
            Toast.makeText(this, "No user selected. Please select a user first.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Restore state or start new
        if (savedInstanceState != null) {
            currentDialogId = savedInstanceState.getInt(KEY_DIALOG_ID, 1);
            currentMusicVolume = savedInstanceState.getFloat(KEY_MUSIC_VOLUME, 0f);
            isChoiceClickable = savedInstanceState.getBoolean(KEY_IS_CHOICE_CLICKABLE, true);
        } else {
            boolean isNewStory = getIntent().getBooleanExtra("new_story", false);
            if (isNewStory) {
                db.updateUserProgress(currentUser.getId(), 1);
                currentDialogId = 1;
            } else {
                currentDialogId = db.getUserDialogId(currentUser.getId());
            }
        }

        initializeMediaPlayer();
        loadDialog(currentDialogId);

        // Create periodic backup
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                db.backupDatabase(GameActivity.this);
                handler.postDelayed(this, 300000); // Every 5 minutes
            }
        }, 300000);
    }

    private void initializeMediaPlayer() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build());
            mediaPlayer.setDataSource(getResources().openRawResourceFd(R.raw.game_music));
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(currentMusicVolume, currentMusicVolume);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                if (currentMusicVolume == 0f) {
                    fadeInMusic();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing MediaPlayer: " + e.getMessage());
        }
    }

    private void loadDialog(int dialogId) {
        showLoading(true);
        
        // Use a background thread for database operations
        new Thread(() -> {
            try {
                DialogEntry dialog = db.getDialogById(dialogId);
                List<Choice> choices = db.getChoicesForDialog(dialogId);
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    if (dialog == null) {
                        handleEndOfStory();
                    } else {
                        updateDialogUI(dialog, choices);
                    }
                    showLoading(false);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading dialog: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error loading story content", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        }).start();
    }

    private void updateDialogUI(DialogEntry dialog, List<Choice> choices) {
        // Fade out current text
        textDialog.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction(() -> {
                textDialog.setText(dialog.getText());
                textDialog.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
            }).start();

        // Update character portrait based on speaker
        updateCharacterPortrait(dialog.getText());

        showChoices(choices);
    }

    private void updateCharacterPortrait(String dialogText) {
        ImageView portrait = findViewById(R.id.imageCharacter);
        
        // Determine which character is speaking based on the dialog text
        if (dialogText.contains("Viren")) {
            portrait.setImageResource(R.drawable.portrait_viren);
        } else if (dialogText.contains("Lady Selene") || dialogText.contains("Selene")) {
            portrait.setImageResource(R.drawable.portrait_selene);
        } else if (dialogText.contains("Maskbearer")) {
            portrait.setImageResource(R.drawable.portrait_maskbearer);
        } else if (dialogText.contains("Elira")) {
            portrait.setImageResource(R.drawable.portrait_elira);
        } else if (dialogText.contains("Lustrine")) {
            portrait.setImageResource(R.drawable.portrait_lustrine);
        } else {
            portrait.setImageResource(R.drawable.portrait_traveler);
        }

        // Animate portrait change
        portrait.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .alpha(0.7f)
            .setDuration(100)
            .withEndAction(() -> {
                portrait.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(200)
                    .start();
            }).start();
    }

    private void handleEndOfStory() {
        textDialog.setText("The End.");
        hideChoices();

        Button backButton = new Button(this);
        backButton.setText("Back to Menu");
        backButton.setBackgroundResource(R.drawable.choice_button_background);
        backButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        backButton.setOnClickListener(v -> fadeOutMusicAndFinish(() -> finish()));
        
        choiceContainer.addView(backButton);
    }

    private void showChoices(List<Choice> choices) {
        choiceContainer.removeAllViews();

        for (Choice choice : choices) {
            Button choiceButton = createChoiceButton(choice);
            choiceContainer.addView(choiceButton);
        }
    }

    private Button createChoiceButton(Choice choice) {
        Button button = new Button(this);
        button.setText(choice.getChoiceText());
        button.setBackgroundResource(R.drawable.choice_button_background);
        button.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        button.setTextSize(16);
        button.setAllCaps(false);
        button.setTag(choice.getId());
        
        // Use custom font
        button.setTypeface(ResourcesCompat.getFont(this, R.font.genshin_font));
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 16, 0, 0);
        button.setLayoutParams(params);

        button.setOnClickListener(v -> handleChoiceClick(choice));
        
        // Add button with animation
        button.setAlpha(0f);
        button.animate()
            .alpha(1f)
            .setDuration(200)
            .setStartDelay(100)
            .start();
        
        return button;
    }

    private void handleChoiceClick(Choice choice) {
        if (!isChoiceClickable) return;
        
        isChoiceClickable = false;
        View clickedView = choiceContainer.findViewWithTag(choice.getId());
        if (clickedView instanceof Button) {
            Button clickedButton = (Button) clickedView;
            clickedButton.setEnabled(false); // Visual feedback
        }

        try {
            db.updateUserProgress(currentUser.getId(), choice.getNextDialogId());
            currentDialogId = choice.getNextDialogId();
            loadDialog(currentDialogId);
        } catch (Exception e) {
            Log.e(TAG, "Error updating progress: " + e.getMessage());
            Toast.makeText(this, "Error saving progress", Toast.LENGTH_SHORT).show();
        }

        // Re-enable after delay
        handler.postDelayed(() -> {
            isChoiceClickable = true;
            View view = choiceContainer.findViewWithTag(choice.getId());
            if (view instanceof Button) {
                ((Button) view).setEnabled(true);
            }
        }, 500);
    }

    private void showLoading(boolean show) {
        loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        choiceContainer.setVisibility(show ? View.GONE : View.VISIBLE);
        textDialog.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void hideChoices() {
        choiceContainer.removeAllViews();
    }

    private void fadeInMusic() {
        if (mediaPlayer == null) return;

        final float maxVolume = 0.7f; // Not too loud
        final int duration = 3000;
        final int step = 100;
        final float delta = maxVolume / (duration / step);

        handler.postDelayed(new Runnable() {
            float volume = currentMusicVolume;

            @Override
            public void run() {
                if (mediaPlayer != null && volume < maxVolume) {
                    volume += delta;
                    currentMusicVolume = volume;
                    mediaPlayer.setVolume(volume, volume);
                    handler.postDelayed(this, step);
                } else if (mediaPlayer != null) {
                    currentMusicVolume = maxVolume;
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
        final float delta = currentMusicVolume / (duration / step);

        handler.postDelayed(new Runnable() {
            float volume = currentMusicVolume;

            @Override
            public void run() {
                if (mediaPlayer != null && volume > 0f) {
                    volume -= delta;
                    currentMusicVolume = volume;
                    mediaPlayer.setVolume(volume, volume);
                    handler.postDelayed(this, step);
                } else {
                    releaseMediaPlayer();
                    if (onComplete != null) onComplete.run();
                }
            }
        }, step);
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing MediaPlayer: " + e.getMessage());
            }
            mediaPlayer = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_DIALOG_ID, currentDialogId);
        outState.putFloat(KEY_MUSIC_VOLUME, currentMusicVolume);
        outState.putBoolean(KEY_IS_CHOICE_CLICKABLE, isChoiceClickable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(0.3f * currentMusicVolume, 0.3f * currentMusicVolume); // Reduce volume when in background
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(currentMusicVolume, currentMusicVolume); // Restore volume
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null); // Clean up all pending handlers
        releaseMediaPlayer();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}