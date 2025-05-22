package com.example.finaltermproject;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class TitleScreenActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_screen);
        // Tap Anywhere animation
        TextView tapAnywhereText = findViewById(R.id.tapAnywhereText);
        ObjectAnimator animator = ObjectAnimator.ofFloat(tapAnywhereText, "alpha", 0f, 1f);
        animator.setDuration(1000);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.start();

        // Music setup
        mediaPlayer = MediaPlayer.create(this, R.raw.menu_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(0f, 0f); // Start silent for fade-in
        mediaPlayer.start();

        fadeInMusic(); // Call fade in

        // Set up tap anywhere listener on the whole screen
        ConstraintLayout titleRoot = findViewById(R.id.title_screen_root);
        titleRoot.setOnClickListener(v -> {
            fadeOutMusicAndFinish(() -> {
                Intent intent = new Intent(TitleScreenActivity.this, MainMenuActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            });
        });
    }

    private void fadeInMusic() {
        final float maxVolume = 1.0f;
        final int fadeDuration = 3000; // milliseconds
        final int fadeStep = 100; // how often to step
        final float volumeStep = maxVolume / (fadeDuration / fadeStep);

        final Handler handler = new Handler();
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

    private void fadeOutMusicAndFinish(final Runnable onComplete) {
        final int fadeDuration = 2000;
        final int fadeStep = 100;
        final float volumeStep = 1.0f / (fadeDuration / fadeStep);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            float volume = 1.0f;

            @Override
            public void run() {
                if (volume > 0f) {
                    volume -= volumeStep;
                    mediaPlayer.setVolume(volume, volume);
                    handler.postDelayed(this, fadeStep);
                } else {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    if (onComplete != null) onComplete.run();
                }
            }
        }, fadeStep);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
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
