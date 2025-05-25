package com.example.finaltermproject;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class TitleScreenActivity extends AppCompatActivity {
    private static final String TAG = "TitleScreenActivity";
    private static final float MAX_VOLUME = 1.0f;
    private static final long FADE_DURATION = 1000;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private MediaPlayer mediaPlayer;
    private boolean isTransitioning = false;
    private Handler fadeHandler;
    private ObjectAnimator tapAnimator;
    private int retryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_screen);
        
        fadeHandler = new Handler(Looper.getMainLooper());
        setupTapAnimation();
        initializeMediaPlayer();
        setupTouchListener();
    }

    private void setupTapAnimation() {
        TextView tapAnywhereText = findViewById(R.id.tapAnywhereText);
        tapAnimator = ObjectAnimator.ofFloat(tapAnywhereText, "alpha", 0f, 1f);
        tapAnimator.setDuration(1000);
        tapAnimator.setRepeatMode(ValueAnimator.REVERSE);
        tapAnimator.setRepeatCount(ValueAnimator.INFINITE);
        tapAnimator.start();
    }

    private void initializeMediaPlayer() {
        try {
            if (mediaPlayer != null) {
                cleanupMediaPlayer();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build()
            );

            mediaPlayer.setDataSource(getResources().openRawResourceFd(R.raw.menu_music));
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(0f, 0f);
            
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
                handleMediaPlayerError();
                return true;
            });

            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                fadeInMusic();
            });

            mediaPlayer.prepareAsync();
            retryCount = 0;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing MediaPlayer", e);
            handleMediaPlayerError();
        }
    }

    private void handleMediaPlayerError() {
        if (retryCount < MAX_RETRY_ATTEMPTS) {
            retryCount++;
            Log.d(TAG, "Retrying MediaPlayer initialization, attempt " + retryCount);
            cleanupMediaPlayer();
            fadeHandler.postDelayed(this::initializeMediaPlayer, 1000);
        } else {
            Log.e(TAG, "Failed to initialize MediaPlayer after " + MAX_RETRY_ATTEMPTS + " attempts");
            Toast.makeText(this, "Background music unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupTouchListener() {
        ConstraintLayout titleRoot = findViewById(R.id.title_screen_root);
        titleRoot.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && !isTransitioning) {
                handleScreenTap();
            }
            return true;
        });
    }

    private void handleScreenTap() {
        isTransitioning = true;
        fadeOutMusic(() -> {
            Intent intent = new Intent(TitleScreenActivity.this, MainMenuActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void fadeInMusic() {
        if (mediaPlayer == null) return;
        
        ValueAnimator fadeAnimator = ValueAnimator.ofFloat(0f, MAX_VOLUME);
        fadeAnimator.setDuration(FADE_DURATION);
        fadeAnimator.addUpdateListener(animation -> {
            float volume = (float) animation.getAnimatedValue();
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(volume, volume);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error during fade in", e);
            }
        });
        fadeAnimator.start();
    }

    private void fadeOutMusic(Runnable onComplete) {
        if (mediaPlayer == null) {
            onComplete.run();
            return;
        }

        ValueAnimator fadeAnimator = ValueAnimator.ofFloat(MAX_VOLUME, 0f);
        fadeAnimator.setDuration(FADE_DURATION);
        fadeAnimator.addUpdateListener(animation -> {
            float volume = (float) animation.getAnimatedValue();
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(volume, volume);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error during fade out", e);
            }
        });
        fadeAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                cleanupMediaPlayer();
                onComplete.run();
            }
        });
        fadeAnimator.start();
    }

    private void cleanupMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error cleaning up MediaPlayer", e);
            } finally {
                mediaPlayer = null;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        if (tapAnimator != null) {
            tapAnimator.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
        if (tapAnimator != null) {
            tapAnimator.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tapAnimator != null) {
            tapAnimator.cancel();
        }
        fadeHandler.removeCallbacksAndMessages(null);
        cleanupMediaPlayer();
    }
}
