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

        mediaPlayer = MediaPlayer.create(this, R.raw.game_music);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(0f, 0f);
            mediaPlayer.start();
            fadeInMusic();
        } else {
            Log.e("GameActivity", "Failed to load game music.");
        }

        List<Integer> missingDialogs = DatabaseHelper.getInstance(this).getDanglingNextDialogIds();

        if (missingDialogs.isEmpty()) {
            Log.d("DEBUG", "All next_dialog_ids have valid dialogs.");
        } else {
            Log.w("DEBUG", "Missing dialogs for the following next_dialog_ids: " + missingDialogs);
        }

        textDialog = findViewById(R.id.textDialog);
        choiceContainer = findViewById(R.id.choiceContainer); // New dynamic layout

        db = DatabaseHelper.getInstance(this);
        currentUser = UserManager.getCurrentUser(this);

        int count = db.getDialogCount();
        Log.d("DEBUG", "Dialog count: " + count);

        if (currentUser == null) {
            Toast.makeText(this, "No user selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentDialogId = db.getUserDialogId(currentUser.getId()); // Load saved progress
        Log.d("DEBUG", "Loading dialog ID: " + currentDialogId);
        loadDialog(currentDialogId);
    }

    private void loadDialog(int dialogId) {
        DialogEntry dialog = db.getDialogById(dialogId);
        if (dialog == null) {
            Log.d("DEBUG", "DialogEntry is null for ID: " + dialogId);
            textDialog.setText("The End.");
            hideChoices();

            Button backButton = new Button(this);
            backButton.setText("Back to Menu");
            backButton.setOnClickListener(v -> fadeOutMusicAndFinish(this::finish));
            choiceContainer.addView(backButton);
            return;
        } else {
            Log.d("DEBUG", "Loaded dialog ID " + dialog.getId() + ": " + dialog.getText());
        }

        textDialog.setAlpha(0f);
        textDialog.setText(dialog.getText());
        textDialog.animate().alpha(1f).setDuration(300).start();
        List<Choice> choices = db.getChoicesForDialog(dialogId);
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
        final float maxVolume = 1.0f;
        final int duration = 3000;
        final int step = 100;
        final float delta = maxVolume / (duration / step);

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            float volume = 0f;

            @Override
            public void run() {
                if (volume < maxVolume) {
                    volume += delta;
                    mediaPlayer.setVolume(volume, volume);
                    handler.postDelayed(this, step);
                } else {
                    mediaPlayer.setVolume(maxVolume, maxVolume);
                }
            }
        }, step);
    }

    private void fadeOutMusicAndFinish(Runnable onComplete) {
        final int duration = 2000;
        final int step = 100;
        final float delta = 1.0f / (duration / step);

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            float volume = 1.0f;

            @Override
            public void run() {
                if (volume > 0f) {
                    volume -= delta;
                    mediaPlayer.setVolume(volume, volume);
                    handler.postDelayed(this, step);
                } else {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    if (onComplete != null) onComplete.run();
                }
            }
        }, step);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
