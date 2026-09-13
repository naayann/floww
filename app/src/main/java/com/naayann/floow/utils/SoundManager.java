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
        try {
            MediaPlayer mp = MediaPlayer.create(context, resId);
            mp.setVolume(volume, volume);
            mp.setOnCompletionListener(MediaPlayer::release);
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void vibrate(Context context, long duration) {
        Vibrator vibrator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
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
    }

    public static void playWelcome(Context context) {
        playSound(context, R.raw.welcome, 1.0f); // 1.0 is full, welcome is boosted (assumed original was lower)
    }

    public static void playTap(Context context) {
        playSound(context, R.raw.button_tap, 0.5f); // Reduced volume
        vibrate(context, 10); // Very short tap vibration
    }

    public static void playPop(Context context) {
        playSound(context, R.raw.pop_button, 0.8f);
        vibrate(context, 20);
    }

    public static void playThrow(Context context) {
        playSound(context, R.raw.delete_sound, 0.8f);
        vibrate(context, 30);
    }

    public static void playFahh(Context context) {
        playSound(context, R.raw.fahh, 1.0f);
        vibrate(context, 15);
    }

    public static void playDone(Context context) {
        playSound(context, R.raw.delete_sound, 1.0f);
        vibrate(context, 40); // Stronger vibration for success
    }

    public static void playCompleted(Context context) {
        playSound(context, R.raw.completed, 1.0f);
        vibrate(context, 60);
    }
}