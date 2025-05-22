package com.example.finaltermproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import androidx.appcompat.app.AppCompatActivity;

public class TitleScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_screen);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Detect any screen tap
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Intent intent = new Intent(TitleScreenActivity.this, MainMenuActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onTouchEvent(event);
    }
}
