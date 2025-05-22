package com.example.finaltermproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private TextView textDialog;
    private Button btn1, btn2, btn3;
    private DatabaseHelper db;
    private User currentUser;
    private int currentDialogId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        textDialog = findViewById(R.id.textDialog);
        btn1 = findViewById(R.id.btnChoice1);
        btn2 = findViewById(R.id.btnChoice2);
        btn3 = findViewById(R.id.btnChoice3);

        db = DatabaseHelper.getInstance(this);
        currentUser = UserManager.getCurrentUser(this);

        if (currentUser == null) {
            Toast.makeText(this, "No user selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentDialogId = db.getUserDialogId(currentUser.getId()); // load saved progress
        Log.d("DEBUG", "Loading dialog ID: " + currentDialogId);
    }

    private void loadDialog(int dialogId) {
        DialogEntry dialog = db.getDialogById(dialogId);
        if (dialog == null) {
            textDialog.setText("The End.");
            hideChoices();
            return;
        }

        textDialog.setText(dialog.getText());
        List<Choice> choices = db.getChoicesForDialog(dialogId);
        showChoices(choices);
    }

    private void showChoices(List<Choice> choices) {
        Button[] buttons = {btn1, btn2, btn3};

        for (int i = 0; i < buttons.length; i++) {
            if (i < choices.size()) {
                Choice choice = choices.get(i);
                Button btn = buttons[i];
                btn.setVisibility(View.VISIBLE);
                btn.setText(choice.getChoiceText());
                btn.setOnClickListener(v -> {
                    db.updateUserProgress(currentUser.getId(), choice.getNextDialogId());
                    currentDialogId = choice.getNextDialogId();
                    loadDialog(currentDialogId);
                });
            } else {
                buttons[i].setVisibility(View.GONE);
            }
        }
    }

    private void hideChoices() {
        btn1.setVisibility(View.GONE);
        btn2.setVisibility(View.GONE);
        btn3.setVisibility(View.GONE);
    }
}
