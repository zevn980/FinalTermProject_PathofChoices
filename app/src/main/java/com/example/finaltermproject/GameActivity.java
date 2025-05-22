package com.example.finaltermproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private TextView textDialog;
    private LinearLayout choiceContainer;
    private DatabaseHelper db;
    private User currentUser;
    private int currentDialogId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

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
            return;
        } else {
            Log.d("DEBUG", "Loaded dialog ID " + dialog.getId() + ": " + dialog.getText());
        }

        textDialog.setText(dialog.getText());
        List<Choice> choices = db.getChoicesForDialog(dialogId);
        showChoices(choices);
    }

    private void showChoices(List<Choice> choices) {
        choiceContainer.removeAllViews(); // Clear previous buttons

        for (Choice choice : choices) {
            Button choiceButton = new Button(this);
            choiceButton.setText(choice.getChoiceText());
            choiceButton.setBackgroundColor(getResources().getColor(android.R.color.holo_purple)); // customize color
            choiceButton.setTextColor(getResources().getColor(android.R.color.white));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 16, 0, 0); // spacing between buttons
            choiceButton.setLayoutParams(params);

            choiceButton.setOnClickListener(v -> {
                db.updateUserProgress(currentUser.getId(), choice.getNextDialogId());
                currentDialogId = choice.getNextDialogId();
                loadDialog(currentDialogId);
            });

            choiceContainer.addView(choiceButton);
        }
    }

    private void hideChoices() {
        choiceContainer.removeAllViews();
    }
}
