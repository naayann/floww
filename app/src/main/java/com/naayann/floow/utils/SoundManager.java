package com.naayann.floow.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import com.naayann.floow.R;

public class SoundManager {

    public static void playSound(Context context, int resId, float volume) {
        if (context == null) return;
        try {
            MediaPlayer mp = MediaPlayer.create(context.getApplicationContext(), resId);
            if (mp != null) {
                mp.setVolume(volume, volume);
                mp.setOnCompletionListener(mediaPlayer -> {
                    try {
                        mediaPlayer.release();
                    } catch (Exception ignored) {}
                });
                mp.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void vibrate(Context context, long duration) {
        if (context == null) return;
        try {
            Vibrator vibrator;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vibratorManager == null) return;
                vibrator = vibratorManager.getDefaultVibrator();
            } else {
                vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(duration);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playWelcome(Context context) {
        playSound(context, R.raw.magical_notification, 1.0f);
    }

    public static void playTap(Context context) {
        playSound(context, R.raw.tactile_click, 0.1f);
        vibrate(context, 5);
    }

    public static void playPop(Context context) {
        playSound(context, R.raw.ui_pop3, 0.8f);
        vibrate(context, 20);
    }

    public static void playThrow(Context context) {
        playSound(context, R.raw.ui_sound, 0.5f);
        vibrate(context, 30);
    }

    public static void playRight(Context context) {
        playSound(context, R.raw.good_girl, 1.0f);
        vibrate(context, 15);
    }

    public static void playDone(Context context) {
        playSound(context, R.raw.ui_close, 0.4f);
        vibrate(context, 40);
    }

    public static void playCompleted(Context context) {
        playSound(context, R.raw.completed, 1.0f);
        vibrate(context, 60);
    }
}