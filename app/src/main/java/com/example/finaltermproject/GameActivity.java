package com.example.finaltermproject;

import android.media.MediaPlayer;
import android.media.AudioManager;
import android.media.AudioAttributes;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.res.Configuration;
import android.os.PersistableBundle;
import android.view.WindowManager;
import android.content.pm.ActivityInfo;
import android.content.Intent;
import android.app.AlertDialog;
import android.text.SpannableString;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.graphics.Typeface;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    private static final String TAG = "GameActivity";
    private static final String KEY_DIALOG_ID = "current_dialog_id";
    private static final String KEY_MUSIC_VOLUME = "music_volume";
    private static final String KEY_IS_CHOICE_CLICKABLE = "is_choice_clickable";

    private TextView textDialog;
    private LinearLayout choiceContainer;
    private ProgressBar loadingIndicator;
    private View loadingCard;
    private View dialogCard;
    private View characterFrame;
    private ImageView imageCharacter;
    private TextView characterNameText;
    private DatabaseHelper db;
    private User currentUser;
    private int currentDialogId;
    private MediaPlayer mediaPlayer;
    private boolean isChoiceClickable = true;
    private float currentMusicVolume = 0f;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private int currentRetryAttempt = 0;
    private Handler retryHandler = new Handler(Looper.getMainLooper());
    private String currentCharacter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "GameActivity onCreate started");

        try {
            // Step 1: Set content view
            Log.d(TAG, "Setting content view");
            setContentView(R.layout.activity_game);
            Log.d(TAG, "Content view set successfully");

            // Step 2: Window flags
            try {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                Log.d(TAG, "Window flags set successfully");
            } catch (Exception e) {
                Log.w(TAG, "Error setting window flags (non-critical)", e);
            }

            // Step 3: Initialize components with detailed logging
            Log.d(TAG, "Starting component initialization");
            if (!initializeComponentsWithDebug()) {
                Log.e(TAG, "Component initialization failed");
                return;
            }
            Log.d(TAG, "Components initialized successfully");

            // Step 4: Get current user with validation
            Log.d(TAG, "Getting current user");
            currentUser = UserManager.getCurrentUser(this);
            if (currentUser == null) {
                Log.e(TAG, "No current user found");
                showErrorAndReturn("No user selected. Please select a user first.");
                return;
            }
            Log.d(TAG, "Current user: " + currentUser.getUsername());

            // Step 5: Validate game state
            Log.d(TAG, "Validating game state");
            if (!validateGameStateWithDebug()) {
                Log.e(TAG, "Game state validation failed");
                return;
            }
            Log.d(TAG, "Game state validated successfully");

            // Step 6: Initialize game state
            Log.d(TAG, "Initializing game state");
            initializeGameStateWithDebug(savedInstanceState);
            Log.d(TAG, "Game state initialized, currentDialogId: " + currentDialogId);

            // Step 7: Initialize media player (non-critical)
            Log.d(TAG, "Initializing media player");
            try {
                initializeMediaPlayerSafely();
                Log.d(TAG, "Media player initialized");
            } catch (Exception e) {
                Log.w(TAG, "Media player initialization failed (non-critical)", e);
            }

            // Step 8: Load initial dialog
            Log.d(TAG, "Loading initial dialog: " + currentDialogId);
            loadDialog(currentDialogId);

            // Step 9: Setup periodic backup (non-critical)
            try {
                setupPeriodicBackup();
                Log.d(TAG, "Periodic backup setup completed");
            } catch (Exception e) {
                Log.w(TAG, "Backup setup failed (non-critical)", e);
            }

            Log.d(TAG, "GameActivity onCreate completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Critical error in onCreate", e);
            showErrorAndReturn("Error starting game: " + e.getMessage());
        }
    }


    private boolean validateGameState() {
        try {
            // Validate story consistency
            if (!db.validateStoryConsistency()) {
                Log.w(TAG, "Story consistency issues detected");
                Toast.makeText(this, "Warning: Story consistency issues detected", Toast.LENGTH_LONG).show();

                // Try to repair story data
                try {
                    db.verifyAndRepairStoryData();
                    Log.d(TAG, "Story data repair attempted");
                } catch (Exception repairError) {
                    Log.e(TAG, "Failed to repair story data", repairError);
                    showErrorAndReturn("Story data is corrupted and cannot be repaired");
                    return false;
                }
            }

            // Check if we have minimum required dialogs
            if (db.getDialogCount() == 0) {
                showErrorAndReturn("No story content available");
                return false;
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error validating game state", e);
            showErrorAndReturn("Error validating game state");
            return false;
        }
    }

    private void initializeViews() {
        Log.d(TAG, "Looking for required views...");

        try {
            // Core required views - these MUST exist in layout
            Log.d(TAG, "Finding textDialog...");
            textDialog = findViewById(R.id.textDialog);
            if (textDialog == null) {
                throw new RuntimeException("textDialog view not found - check if R.id.textDialog exists in activity_game.xml");
            }
            Log.d(TAG, "textDialog found");

            Log.d(TAG, "Finding choiceContainer...");
            choiceContainer = findViewById(R.id.choiceContainer);
            if (choiceContainer == null) {
                throw new RuntimeException("choiceContainer view not found - check if R.id.choiceContainer exists in activity_game.xml");
            }
            Log.d(TAG, "choiceContainer found");

            // Optional views with safe casting
            Log.d(TAG, "Finding optional views...");

            loadingIndicator = findViewById(R.id.loadingIndicator);
            if (loadingIndicator == null) {
                Log.w(TAG, "loadingIndicator not found, creating fallback");
                loadingIndicator = new ProgressBar(this);
            } else {
                Log.d(TAG, "loadingIndicator found");
            }

            // Use View instead of CardView for compatibility
            loadingCard = findViewById(R.id.loadingCard);
            if (loadingCard == null) {
                Log.w(TAG, "loadingCard not found, creating fallback");
                loadingCard = new LinearLayout(this);
                loadingCard.setVisibility(View.GONE);
            } else {
                Log.d(TAG, "loadingCard found");
            }

            dialogCard = findViewById(R.id.dialogCard);
            if (dialogCard == null) {
                Log.w(TAG, "dialogCard not found, using textDialog parent as fallback");
                if (textDialog.getParent() instanceof View) {
                    dialogCard = (View) textDialog.getParent();
                } else {
                    dialogCard = new LinearLayout(this);
                }
            } else {
                Log.d(TAG, "dialogCard found");
            }

            characterFrame = findViewById(R.id.characterFrame);
            if (characterFrame == null) {
                Log.w(TAG, "characterFrame not found, creating fallback");
                characterFrame = new LinearLayout(this);
                characterFrame.setVisibility(View.GONE);
            } else {
                Log.d(TAG, "characterFrame found");
            }

            imageCharacter = findViewById(R.id.imageCharacter);
            if (imageCharacter == null) {
                Log.w(TAG, "imageCharacter not found, creating fallback");
                imageCharacter = new ImageView(this);
            } else {
                Log.d(TAG, "imageCharacter found");
            }

            characterNameText = findViewById(R.id.characterNameText);
            if (characterNameText == null) {
                Log.w(TAG, "characterNameText not found, creating fallback");
                characterNameText = new TextView(this);
                characterNameText.setVisibility(View.GONE);
            } else {
                Log.d(TAG, "characterNameText found");
            }

            // Set initial states
            if (textDialog != null) {
                textDialog.setText("Initializing story...");
            }

            Log.d(TAG, "All views initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Critical error initializing views", e);
            throw new RuntimeException("Failed to initialize required views: " + e.getMessage(), e);
        }
    }

    private boolean initializeComponents() {
        try {
            initializeViews();

            // Initialize database
            db = DatabaseHelper.getInstance(this);
            if (db == null) {
                showErrorAndReturn("Failed to initialize database");
                return false;
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing components", e);
            showErrorAndReturn("Error initializing game components");
            return false;
        }
    }

    private void initializeGameState(Bundle savedInstanceState) {
        try {
            // Restore state or start new
            if (savedInstanceState != null) {
                currentDialogId = savedInstanceState.getInt(KEY_DIALOG_ID, 1);
                currentMusicVolume = savedInstanceState.getFloat(KEY_MUSIC_VOLUME, 0f);
                isChoiceClickable = savedInstanceState.getBoolean(KEY_IS_CHOICE_CLICKABLE, true);
                Log.d(TAG, "Restored state from savedInstanceState");
            } else {
                boolean isNewStory = getIntent().getBooleanExtra("new_story", false);
                if (isNewStory) {
                    try {
                        db.updateUserProgress(currentUser.getId(), 1);
                        currentDialogId = 1;
                        Log.d(TAG, "Started new story for user: " + currentUser.getUsername());
                    } catch (Exception e) {
                        Log.e(TAG, "Error starting new story", e);
                        currentDialogId = 1; // Fallback
                    }
                } else {
                    try {
                        currentDialogId = db.getUserDialogId(currentUser.getId());
                        if (currentDialogId <= 0) {
                            Log.w(TAG, "Invalid dialog ID, resetting to 1");
                            currentDialogId = 1;
                            db.updateUserProgress(currentUser.getId(), 1);
                        }
                        Log.d(TAG, "Continuing story from dialog: " + currentDialogId);
                    } catch (Exception e) {
                        Log.e(TAG, "Error getting user dialog ID", e);
                        currentDialogId = 1; // Fallback to start
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing game state", e);
            currentDialogId = 1; // Safe fallback
        }
    }

    private void setupPeriodicBackup() {
        try {
            // Create periodic backup
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isFinishing() || isDestroyed()) {
                        return; // Don't backup if activity is finishing
                    }

                    try {
                        db.backupDatabase(GameActivity.this);
                        Log.d(TAG, "Periodic backup completed");
                    } catch (Exception e) {
                        Log.e(TAG, "Backup failed: " + e.getMessage());
                    }

                    // Schedule next backup
                    handler.postDelayed(this, 300000); // Every 5 minutes
                }
            }, 300000);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up periodic backup", e);
        }
    }

    private void initializeMediaPlayer() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build());

            // Check if raw resource exists
            try {
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
                Log.w(TAG, "Game music not found, continuing without audio");
                mediaPlayer = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing MediaPlayer: " + e.getMessage());
            mediaPlayer = null;
        }
    }

    private void initializeMediaPlayerSafely() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build());

            // Check if raw resource exists
            try {
                android.content.res.AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.game_music);
                if (afd != null) {
                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    afd.close();

                    mediaPlayer.setLooping(true);
                    mediaPlayer.setVolume(currentMusicVolume, currentMusicVolume);
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(mp -> {
                        try {
                            mp.start();
                            if (currentMusicVolume == 0f) {
                                fadeInMusic();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error starting music", e);
                        }
                    });

                    mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                        Log.e(TAG, "MediaPlayer error: what=" + what + ", extra=" + extra);
                        releaseMediaPlayer();
                        return true; // Error handled
                    });

                    Log.d(TAG, "MediaPlayer initialized successfully");
                } else {
                    Log.w(TAG, "Game music resource not available");
                    mediaPlayer = null;
                }
            } catch (Exception e) {
                Log.w(TAG, "Game music not found, continuing without audio", e);
                releaseMediaPlayer();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing MediaPlayer", e);
            mediaPlayer = null;
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
                    try {
                        if (dialog == null) {
                            handleDialogNotFound(dialogId);
                        } else if (isEndingDialog(dialog.getId())) {
                            updateDialogUI(dialog, choices);
                        } else {
                            updateDialogUI(dialog, choices);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating UI with dialog", e);
                        handleLoadError(dialogId, e);
                    } finally {
                        showLoading(false);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading dialog from database", e);
                runOnUiThread(() -> {
                    handleLoadError(dialogId, e);
                    showLoading(false);
                });
            }
        }).start();
    }

    private void updateDialogUI(DialogEntry dialog, List<Choice> choices) {
        String formattedText = formatDialogText(dialog.getText());

        // Check if this is an ending dialog (dialogs 501-504)
        boolean isEnding = isEndingDialog(dialog.getId());

        // Detect and display character
        String characterName = detectCharacterInDialog(dialog.getText());
        if (characterName != null && !isEnding) {
            currentCharacter = characterName;
            showCharacterImage(characterName);
        } else {
            // Hide character for endings or when no character detected
            if (currentCharacter != null && (!hasOngoingCharacterDialog() || isEnding)) {
                hideCharacterImage();
                currentCharacter = null;
            }
        }

        // Animate text change
        textDialog.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction(() -> {
                    textDialog.setText(createFormattedSpannable(formattedText));
                    textDialog.animate()
                            .alpha(1f)
                            .setDuration(400)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                }).start();

        // Show choices or ending button
        if (isEnding) {
            showEndingWithBackButton();
        } else {
            showChoices(choices);
        }
    }

    private boolean isEndingDialog(int dialogId) {
        // Ending dialogs are 501-504 based on the story structure
        return dialogId >= 501 && dialogId <= 504;
    }

    private void showEndingWithBackButton() {
        choiceContainer.removeAllViews();

        // Add back to menu button for endings
        handler.postDelayed(() -> addBackToMenuButton(), 2000); // Delay to let players read
    }

    private String formatDialogText(String rawText) {
        if (rawText == null) return "";

        // Break long paragraphs into readable chunks
        String formatted = rawText
                // Add breathing room after dialogue
                .replaceAll("\"([^\"]+)\"", "\"$1\"\n\n")
                // Break up long narrative sections - be more conservative
                .replaceAll("\\. ([A-Z][a-z]{3,})", ".\n\n$1")
                // Format speaker identification
                .replaceAll("([A-Z][a-z]+ [A-Z][a-z]+),", "\n**$1:**\n")
                .replaceAll("\"([^\"]+),\"", "\n*\"$1,\"*\n")
                // Handle existing newlines better
                .replaceAll("\\\\n", "\n")
                // Clean up multiple newlines
                .replaceAll("\n{3,}", "\n\n")
                .trim();

        return formatted;
    }

    private SpannableString createFormattedSpannable(String text) {
        SpannableString spannable = new SpannableString(text);

        try {
            // Apply formatting for better readability
            Pattern dialogPattern = Pattern.compile("\"([^\"]+)\"");
            Matcher dialogMatcher = dialogPattern.matcher(text);

            while (dialogMatcher.find()) {
                // Style dialogue text
                spannable.setSpan(new StyleSpan(Typeface.ITALIC),
                        dialogMatcher.start(), dialogMatcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Use default color if custom color not available
                try {
                    int dialogColor = ContextCompat.getColor(this, R.color.character_speech);
                    spannable.setSpan(new ForegroundColorSpan(dialogColor),
                            dialogMatcher.start(), dialogMatcher.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (Exception e) {
                    // Use default text color if custom color fails
                    Log.w(TAG, "Custom dialog color not available, using default");
                }
            }

            // Style character names
            Pattern namePattern = Pattern.compile("\\*\\*([^*]+):\\*\\*");
            Matcher nameMatcher = namePattern.matcher(text);

            while (nameMatcher.find()) {
                spannable.setSpan(new StyleSpan(Typeface.BOLD),
                        nameMatcher.start(), nameMatcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new RelativeSizeSpan(1.1f),
                        nameMatcher.start(), nameMatcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error formatting spannable text: " + e.getMessage());
        }

        return spannable;
    }

    private String detectCharacterInDialog(String dialogText) {
        if (dialogText == null) return null;

        // Simple character detection - extend this list based on your story
        String[] characters = {"Viren", "Selene", "Elira", "Maskbearer"};

        for (String character : characters) {
            if (dialogText.contains(character)) {
                return character.toLowerCase();
            }
        }
        return null;
    }

    private void showCharacterImage(String characterName) {
        try {
            int imageResource = getCharacterImage(characterName);
            if (imageResource != 0 && imageCharacter != null) {
                imageCharacter.setImageResource(imageResource);

                if (characterNameText != null) {
                    characterNameText.setText(formatCharacterName(characterName));
                }

                // Show character with animation
                if (characterFrame != null && characterFrame.getVisibility() != View.VISIBLE) {
                    characterFrame.setVisibility(View.VISIBLE);

                    if (characterNameText != null) {
                        characterNameText.setVisibility(View.VISIBLE);
                    }

                    // Animate appearance
                    characterFrame.setScaleX(0f);
                    characterFrame.setScaleY(0f);
                    characterFrame.setAlpha(0f);

                    characterFrame.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(600)
                            .start();

                    if (characterNameText != null) {
                        characterNameText.setAlpha(0f);
                        characterNameText.animate()
                                .alpha(1f)
                                .setDuration(400)
                                .setStartDelay(300)
                                .start();
                    }
                } else if (imageCharacter != null) {
                    // Character already visible, just update image
                    imageCharacter.setAlpha(0f);
                    imageCharacter.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .start();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing character image", e);
        }
    }

    private void hideCharacterImage() {
        try {
            if (characterFrame != null && characterFrame.getVisibility() == View.VISIBLE) {
                characterFrame.animate()
                        .scaleX(0.8f)
                        .scaleY(0.8f)
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(() -> {
                            if (characterFrame != null) {
                                characterFrame.setVisibility(View.GONE);
                            }
                            if (characterNameText != null) {
                                characterNameText.setVisibility(View.GONE);
                            }
                        })
                        .start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding character image", e);
        }
    }

    private String formatCharacterName(String name) {
        if (name == null) return "";

        // Format character names for display
        switch (name.toLowerCase()) {
            case "viren":
                return "Lord Viren";
            case "selene":
                return "Lady Selene";
            case "maskbearer":
                return "The Maskbearer";
            default:
                return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        }
    }

    private int getCharacterImage(String characterName) {
        if (characterName == null) return 0;

        // Add character images to res/drawable folder and map them here
        // For now, return default icon - replace with actual character images
        try {
            switch (characterName.toLowerCase()) {
                case "viren":
                    return R.drawable.character_viren;
                case "selene":
                    return R.drawable.character_selene;
                case "elira":
                    return R.drawable.character_elira;
                case "maskbearer":
                    return R.drawable.character_maskbearer;
                default:
                    return R.drawable.ic_person; // Default fallback
            }
        } catch (Exception e) {
            Log.w(TAG, "Character image not found for: " + characterName);
            return R.drawable.ic_person; // Fallback to default
        }
    }

    private boolean hasOngoingCharacterDialog() {
        // Simple logic - could be enhanced based on story structure
        // For now, assume character persists for 2-3 consecutive dialogs
        return false; // Default behavior - hide character between dialogs
    }

    private void handleEndOfStory() {
        textDialog.setText("The End.\n\nThank you for playing!");
        hideChoices();
        hideCharacterImage();

        addBackToMenuButton();
    }

    private void addBackToMenuButton() {
        Button backButton = new Button(this);
        backButton.setText("Back to Menu");

        // Use fallback if custom background not available
        try {
            backButton.setBackgroundResource(R.drawable.choice_button_background);
        } catch (Exception e) {
            backButton.setBackgroundColor(ContextCompat.getColor(this, R.color.choice_background));
        }

        backButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        backButton.setTextSize(16);
        backButton.setAllCaps(false);

        // Better button styling
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 24, 0, 0);
        backButton.setLayoutParams(params);

        // Add click animation and functionality
        backButton.setOnClickListener(v -> {
            // Disable button to prevent multiple clicks
            backButton.setEnabled(false);
            backButton.setAlpha(0.5f);

            // Fade out music and return to menu
            fadeOutMusicAndFinish(() -> returnToMainMenu());
        });

        // Add smooth appearance animation
        backButton.setAlpha(0f);
        choiceContainer.addView(backButton);
        backButton.animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(1000) // Delay to let players read the ending
                .start();
    }

    private void showChoices(List<Choice> choices) {
        choiceContainer.removeAllViews();

        if (choices != null && !choices.isEmpty()) {
            for (int i = 0; i < choices.size(); i++) {
                Choice choice = choices.get(i);
                Button choiceButton = createChoiceButton(choice, i);
                choiceContainer.addView(choiceButton);
            }
        }
    }

    private Button createChoiceButton(Choice choice, int index) {
        Button button = new Button(this);

        // Wrap long choice text for better readability
        String wrappedText = wrapChoiceText(choice.getChoiceText());
        button.setText(wrappedText);

        // Use fallback if custom background not available
        try {
            button.setBackgroundResource(R.drawable.choice_button_background);
        } catch (Exception e) {
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.choice_background));
        }

        button.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        button.setTextSize(14); // Slightly smaller for wrapped text
        button.setAllCaps(false);
        button.setTag(choice.getId());

        // Better button sizing for wrapped text
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 12, 0, 0);
        button.setLayoutParams(params);

        // Add subtle animation on creation
        button.setAlpha(0f);
        button.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(index * 100) // Stagger animations
                .start();

        button.setOnClickListener(v -> handleChoiceClick(choice));

        return button;
    }

    private String wrapChoiceText(String text) {
        if (text == null) return "";

        // Break long choice text into multiple lines
        if (text.length() > 50) {
            String[] words = text.split(" ");
            StringBuilder wrapped = new StringBuilder();
            int lineLength = 0;

            for (String word : words) {
                if (lineLength + word.length() > 45) {
                    wrapped.append("\n");
                    lineLength = 0;
                }
                wrapped.append(word).append(" ");
                lineLength += word.length() + 1;
            }
            return wrapped.toString().trim();
        }
        return text;
    }

    private void handleChoiceClick(Choice choice) {
        if (!isChoiceClickable) return;

        isChoiceClickable = false;
        View clickedView = choiceContainer.findViewWithTag(choice.getId());
        if (clickedView instanceof Button) {
            Button clickedButton = (Button) clickedView;
            clickedButton.setEnabled(false); // Visual feedback
            clickedButton.setAlpha(0.7f);
        }

        // Use background thread for database operations
        new Thread(() -> {
            try {
                db.updateUserProgress(currentUser.getId(), choice.getNextDialogId());
                currentDialogId = choice.getNextDialogId();

                runOnUiThread(() -> {
                    try {
                        loadDialog(currentDialogId);
                    } catch (Exception e) {
                        Log.e(TAG, "Error loading next dialog", e);
                        handleLoadError(currentDialogId, e);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error updating progress", e);
                runOnUiThread(() -> {
                    Toast.makeText(GameActivity.this, "Error saving progress", Toast.LENGTH_SHORT).show();
                    // Re-enable choice clicking
                    isChoiceClickable = true;
                    if (clickedView instanceof Button) {
                        ((Button) clickedView).setEnabled(true);
                        clickedView.setAlpha(1.0f);
                    }
                });
            }
        }).start();

        // Re-enable after delay (fallback)
        handler.postDelayed(() -> {
            isChoiceClickable = true;
            View view = choiceContainer.findViewWithTag(choice.getId());
            if (view instanceof Button) {
                ((Button) view).setEnabled(true);
                view.setAlpha(1.0f);
            }
        }, 2000);
    }

    private void showLoading(boolean show) {
        try {
            if (show) {
                if (loadingCard != null) {
                    loadingCard.setVisibility(View.VISIBLE);
                    loadingCard.setAlpha(0f);
                    loadingCard.animate()
                            .alpha(1f)
                            .setDuration(200)
                            .start();
                }

                // Hide choice container
                if (choiceContainer != null) {
                    choiceContainer.setVisibility(View.GONE);
                }

                // Hide dialog area
                if (dialogCard != null) {
                    dialogCard.setVisibility(View.GONE);
                }

            } else {
                if (loadingCard != null) {
                    loadingCard.animate()
                            .alpha(0f)
                            .setDuration(200)
                            .withEndAction(() -> {
                                if (loadingCard != null) {
                                    loadingCard.setVisibility(View.GONE);
                                }

                                // Show choice container
                                if (choiceContainer != null) {
                                    choiceContainer.setVisibility(View.VISIBLE);
                                }

                                // Show dialog area
                                if (dialogCard != null) {
                                    dialogCard.setVisibility(View.VISIBLE);
                                    dialogCard.setAlpha(0f);
                                    dialogCard.animate().alpha(1f).setDuration(300).start();
                                }
                            })
                            .start();
                } else {
                    // Fallback if loadingCard is null
                    if (choiceContainer != null) {
                        choiceContainer.setVisibility(View.VISIBLE);
                    }
                    if (dialogCard != null) {
                        dialogCard.setVisibility(View.VISIBLE);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in showLoading", e);
            // Fallback: just ensure basic views are visible
            if (!show) {
                if (choiceContainer != null) choiceContainer.setVisibility(View.VISIBLE);
                if (dialogCard != null) dialogCard.setVisibility(View.VISIBLE);
            }
        }
    }


    private void handleDialogNotFound(int dialogId) {
        Log.e(TAG, "Dialog not found: " + dialogId);

        try {
            // Try to find a valid fallback dialog
            int fallbackDialogId = db.getLastWorkingDialogId(currentUser.getId());
            if (fallbackDialogId > 0 && fallbackDialogId != dialogId) {
                Log.d(TAG, "Using fallback dialog: " + fallbackDialogId);
                currentDialogId = fallbackDialogId;
                loadDialog(fallbackDialogId);
                return;
            }

            // If no fallback found, check if we have any dialogs at all
            if (db.getDialogCount() > 0) {
                Log.d(TAG, "Resetting to start of story");
                currentDialogId = 1;
                db.updateUserProgress(currentUser.getId(), 1);
                loadDialog(1);
                return;
            }

            // No dialogs available at all
            handleEndOfStory();
        } catch (Exception e) {
            Log.e(TAG, "Error handling dialog not found", e);
            handleEndOfStory();
        }
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

    private void loadDialogWithRetry(final int dialogId) {
        try {
            DialogEntry dialog = db.getDialogById(dialogId);
            if (dialog != null) {
                displayDialog(dialog);
                currentRetryAttempt = 0; // Reset retry counter on success
            } else {
                handleLoadError(dialogId, new Exception("Dialog not found"));
            }
        } catch (Exception e) {
            handleLoadError(dialogId, e);
        }
    }

    private void handleLoadError(final int dialogId, Exception error) {
        Log.e(TAG, "Error loading dialog: " + error.getMessage(), error);

        if (currentRetryAttempt < MAX_RETRY_ATTEMPTS) {
            currentRetryAttempt++;
            String message = String.format("Retrying... Attempt %d of %d",
                    currentRetryAttempt, MAX_RETRY_ATTEMPTS);

            runOnUiThread(() -> {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            });

            retryHandler.postDelayed(() -> {
                try {
                    loadDialog(dialogId);
                } catch (Exception retryError) {
                    Log.e(TAG, "Retry failed", retryError);
                    handleLoadError(dialogId, retryError);
                }
            }, RETRY_DELAY_MS);
        } else {
            runOnUiThread(() -> {
                showErrorDialog("Error Loading Story",
                        "Unable to load the story content. Would you like to:\n" +
                                "1. Return to last working state\n" +
                                "2. Restart from beginning\n" +
                                "3. Return to main menu");
            });
        }
    }

    private void showErrorAndReturn(String message) {
        Log.e(TAG, "Showing error and returning: " + message);

        runOnUiThread(() -> {
            try {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                handler.postDelayed(() -> {
                    try {
                        Intent intent = new Intent(GameActivity.this, MainMenuActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        Log.e(TAG, "Error returning to main menu", e);
                        finish();
                    }
                }, 3000);
            } catch (Exception e) {
                Log.e(TAG, "Error in showErrorAndReturn", e);
                finish();
            }
        });
    }

    private void showErrorDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Return to Last State", (dialog, id) -> {
                    try {
                        int lastWorkingDialog = db.getLastWorkingDialogId(currentUser.getId());
                        if (lastWorkingDialog > 0) {
                            currentRetryAttempt = 0; // Reset retry counter
                            loadDialogWithRetry(lastWorkingDialog);
                        } else {
                            Toast.makeText(this, "Could not find last working state",
                                    Toast.LENGTH_LONG).show();
                            returnToMainMenu();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error returning to last state", e);
                        returnToMainMenu();
                    }
                })
                .setNeutralButton("Restart Story", (dialog, id) -> {
                    try {
                        db.updateUserProgress(currentUser.getId(), 1);
                        currentRetryAttempt = 0; // Reset retry counter
                        loadDialogWithRetry(1);
                    } catch (Exception e) {
                        Log.e(TAG, "Error restarting story", e);
                        returnToMainMenu();
                    }
                })
                .setNegativeButton("Main Menu", (dialog, id) -> returnToMainMenu());

        runOnUiThread(() -> {
            AlertDialog alert = builder.create();
            alert.show();
        });
    }

    private void returnToMainMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void displayDialog(DialogEntry dialog) {
        if (dialog == null) {
            Log.e(TAG, "Attempted to display null dialog");
            return;
        }

        runOnUiThread(() -> {
            List<Choice> choices = db.getChoicesForDialog(dialog.getId());
            updateDialogUI(dialog, choices);
        });
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
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.setVolume(0.3f * currentMusicVolume, 0.3f * currentMusicVolume); // Reduce volume when in background
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.setVolume(currentMusicVolume, currentMusicVolume); // Restore volume
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            handler.removeCallbacksAndMessages(null); // Clean up all pending handlers
            retryHandler.removeCallbacksAndMessages(null);
            releaseMediaPlayer();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy", e);
        }
    }

    private boolean initializeComponentsWithDebug() {
        try {
            Log.d(TAG, "Initializing views...");
            initializeViewsWithDebug();

            Log.d(TAG, "Initializing database...");
            db = DatabaseHelper.getInstance(this);
            if (db == null) {
                Log.e(TAG, "Database initialization returned null");
                showErrorAndReturn("Failed to initialize database");
                return false;
            }
            Log.d(TAG, "Database initialized successfully");

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing components", e);
            showErrorAndReturn("Error initializing game components: " + e.getMessage());
            return false;
        }
    }

    private void initializeViewsWithDebug() {
        Log.d(TAG, "Looking for required views...");

        // Check if layout was properly inflated
        View rootView = findViewById(R.id.gameRoot);
        if (rootView == null) {
            // Try alternative root IDs
            rootView = findViewById(android.R.id.content);
            Log.w(TAG, "gameRoot not found, using content root");
        }

        try {
            // Core required views
            Log.d(TAG, "Finding textDialog...");
            textDialog = findViewById(R.id.textDialog);
            if (textDialog == null) {
                throw new RuntimeException("textDialog view not found - check if R.id.textDialog exists in activity_game.xml");
            }
            Log.d(TAG, "textDialog found");

            Log.d(TAG, "Finding choiceContainer...");
            choiceContainer = findViewById(R.id.choiceContainer);
            if (choiceContainer == null) {
                throw new RuntimeException("choiceContainer view not found - check if R.id.choiceContainer exists in activity_game.xml");
            }
            Log.d(TAG, "choiceContainer found");

            // Optional views with fallbacks
            Log.d(TAG, "Finding optional views...");

            loadingIndicator = findViewById(R.id.loadingIndicator);
            if (loadingIndicator == null) {
                Log.w(TAG, "loadingIndicator not found, creating fallback");
                loadingIndicator = new ProgressBar(this);
            } else {
                Log.d(TAG, "loadingIndicator found");
            }

            loadingCard = findViewById(R.id.loadingCard);
            if (loadingCard == null) {
                Log.w(TAG, "loadingCard not found, creating fallback");
                loadingCard = new LinearLayout(this);
                loadingCard.setVisibility(View.GONE);
            } else {
                Log.d(TAG, "loadingCard found");
            }

            dialogCard = findViewById(R.id.dialogCard);
            if (dialogCard == null) {
                Log.w(TAG, "dialogCard not found, using fallback");
                // Use the parent of textDialog as dialogCard
                if (textDialog.getParent() instanceof View) {
                    dialogCard = (View) textDialog.getParent();
                } else {
                    dialogCard = new LinearLayout(this);
                }
            } else {
                Log.d(TAG, "dialogCard found");
            }

            characterFrame = findViewById(R.id.characterFrame);
            if (characterFrame == null) {
                Log.w(TAG, "characterFrame not found, creating fallback");
                characterFrame = new LinearLayout(this);
                characterFrame.setVisibility(View.GONE);
            } else {
                Log.d(TAG, "characterFrame found");
            }

            imageCharacter = findViewById(R.id.imageCharacter);
            if (imageCharacter == null) {
                Log.w(TAG, "imageCharacter not found, creating fallback");
                imageCharacter = new ImageView(this);
            } else {
                Log.d(TAG, "imageCharacter found");
            }

            characterNameText = findViewById(R.id.characterNameText);
            if (characterNameText == null) {
                Log.w(TAG, "characterNameText not found, creating fallback");
                characterNameText = new TextView(this);
                characterNameText.setVisibility(View.GONE);
            } else {
                Log.d(TAG, "characterNameText found");
            }

            // Set initial states
            if (textDialog != null) {
                textDialog.setText("Initializing story...");
            }

            Log.d(TAG, "All views initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Critical error initializing views", e);

            // Log layout debugging information
            logLayoutDebugInfo();

            throw new RuntimeException("Failed to initialize required views. Please check that activity_game.xml contains the required elements: " + e.getMessage(), e);
        }
    }

    private void logLayoutDebugInfo() {
        try {
            Log.d(TAG, "=== Layout Debug Information ===");

            // Check if the layout file exists and was loaded
            View contentView = findViewById(android.R.id.content);
            if (contentView != null) {
                Log.d(TAG, "Content view exists");
                if (contentView instanceof ViewGroup) {
                    ViewGroup group = (ViewGroup) contentView;
                    Log.d(TAG, "Content view has " + group.getChildCount() + " children");

                    // Log all child view IDs
                    for (int i = 0; i < group.getChildCount(); i++) {
                        View child = group.getChildAt(i);
                        String resourceName = "unknown";
                        try {
                            resourceName = getResources().getResourceEntryName(child.getId());
                        } catch (Exception e) {
                            // Resource name not found
                        }
                        Log.d(TAG, "Child " + i + ": " + child.getClass().getSimpleName() + " (ID: " + resourceName + ")");
                    }
                }
            } else {
                Log.e(TAG, "Content view is null!");
            }

            // Check specifically for our required views
            String[] requiredViews = {"textDialog", "choiceContainer"};
            for (String viewName : requiredViews) {
                try {
                    int resourceId = getResources().getIdentifier(viewName, "id", getPackageName());
                    if (resourceId != 0) {
                        View view = findViewById(resourceId);
                        Log.d(TAG, viewName + " resource ID found: " + resourceId + ", view: " + (view != null ? "found" : "NOT FOUND"));
                    } else {
                        Log.e(TAG, viewName + " resource ID not found in package");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error checking " + viewName, e);
                }
            }

            Log.d(TAG, "=== End Layout Debug Information ===");
        } catch (Exception e) {
            Log.e(TAG, "Error in layout debugging", e);
        }
    }

    private boolean validateGameStateWithDebug() {
        try {
            Log.d(TAG, "Checking story consistency...");
            if (!db.validateStoryConsistency()) {
                Log.w(TAG, "Story consistency issues detected, attempting repair");

                try {
                    db.verifyAndRepairStoryData();
                    Log.d(TAG, "Story data repair completed");
                } catch (Exception repairError) {
                    Log.e(TAG, "Failed to repair story data", repairError);
                    showErrorAndReturn("Story data is corrupted and cannot be repaired");
                    return false;
                }
            } else {
                Log.d(TAG, "Story consistency check passed");
            }

            Log.d(TAG, "Checking dialog count...");
            int dialogCount = db.getDialogCount();
            Log.d(TAG, "Found " + dialogCount + " dialogs in database");

            if (dialogCount == 0) {
                Log.e(TAG, "No dialogs found in database");
                showErrorAndReturn("No story content available");
                return false;
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error validating game state", e);
            showErrorAndReturn("Error validating game state: " + e.getMessage());
            return false;
        }
    }

    private void initializeGameStateWithDebug(Bundle savedInstanceState) {
        try {
            if (savedInstanceState != null) {
                currentDialogId = savedInstanceState.getInt(KEY_DIALOG_ID, 1);
                currentMusicVolume = savedInstanceState.getFloat(KEY_MUSIC_VOLUME, 0f);
                isChoiceClickable = savedInstanceState.getBoolean(KEY_IS_CHOICE_CLICKABLE, true);
                Log.d(TAG, "Restored state from savedInstanceState, dialogId: " + currentDialogId);
            } else {
                boolean isNewStory = getIntent().getBooleanExtra("new_story", false);
                Log.d(TAG, "isNewStory: " + isNewStory);

                if (isNewStory) {
                    try {
                        Log.d(TAG, "Starting new story for user: " + currentUser.getId());
                        db.updateUserProgress(currentUser.getId(), 1);
                        currentDialogId = 1;
                        Log.d(TAG, "New story initialized with dialog 1");
                    } catch (Exception e) {
                        Log.e(TAG, "Error starting new story", e);
                        currentDialogId = 1; // Fallback
                    }
                } else {
                    try {
                        Log.d(TAG, "Loading existing progress for user: " + currentUser.getId());
                        currentDialogId = db.getUserDialogId(currentUser.getId());
                        Log.d(TAG, "Loaded existing progress, dialogId: " + currentDialogId);

                        if (currentDialogId <= 0) {
                            Log.w(TAG, "Invalid dialog ID, resetting to 1");
                            currentDialogId = 1;
                            db.updateUserProgress(currentUser.getId(), 1);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error getting user dialog ID", e);
                        currentDialogId = 1; // Fallback to start
                    }
                }
            }

            Log.d(TAG, "Final currentDialogId: " + currentDialogId);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing game state", e);
            currentDialogId = 1; // Safe fallback
        }
    }
}
