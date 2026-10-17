package com.naayann.floow;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.naayann.floow.data.AppDatabase;
import com.naayann.floow.data.TodoEntity;
import com.naayann.floow.utils.PrefHelper;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class NotificationWorker extends Worker {
    private static final String CHANNEL_ID = "floow_notifications";

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        PrefHelper pref = new PrefHelper(context);
        if (!pref.areNotificationsEnabled()) {
            return Result.success();
        }

        // Check if there are pending tasks for today
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        List<TodoEntity> pending = AppDatabase.getInstance(context).todoDao().getPendingToday(today);
        if (pending.isEmpty()) {
            // All tasks completed! No need to send notification.
            return Result.success();
        }

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String slot;
        String title;
        String message;

        Random random = new Random();

        if (hour >= 6 && hour < 12) {
            slot = today + "_MORNING";
            title = "Morning Reality Check ⏰";
            String[] messages = {
                "Your goals are gathering dust while you waste hours. Get up and swipe before today becomes another regret.",
                "Tick-tock. Another morning wasted means another day you fall behind everyone else. Are you going to just watch time slip away?",
                "Wake up! Your dreams won't build themselves. If you leave your tasks untouched today, you're choosing failure."
            };
            message = messages[random.nextInt(messages.length)];
        } else if (hour >= 12 && hour < 18) {
            slot = today + "_AFTERNOON";
            title = "Afternoon Warning ⚠️";
            String[] messages = {
                "Half the day is gone. Your tasks are still staring at you. Are you really going to let laziness win again today?",
                "The clock is ticking and your goals are untouched. Do you even care about your future, or is quitting your habit?",
                "The afternoon slump is no excuse. Your unfinished tasks are judging you. Swipe them now before it's too late!"
            };
            message = messages[random.nextInt(messages.length)];
        } else {
            slot = today + "_NIGHT";
            title = "Night Alert: Final Warning 🚨";
            String[] messages = {
                "The day is ending, and you failed to finish your goals. Another day lost forever. How much longer will you disappoint yourself?",
                "Night falls, and your tasks remain mocking you. Tomorrow you'll regret doing nothing today. Fix it right now.",
                "Bedtime is approaching, and your list is still red with neglect. Sleep knowing you gave up today? Or prove you're not a quitter."
            };
            message = messages[random.nextInt(messages.length)];
        }

        // Check if we already sent notification for this slot today
        if (slot.equals(pref.getLastNotificationSlot())) {
            return Result.success();
        }
        pref.setLastNotificationSlot(slot);

        sendNotification(context, title, message);
        return Result.success();
    }

    private void sendNotification(Context context, String title, String message) {
        createNotificationChannel(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_nav_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "floow Reminders";
            String description = "Daily urgent reminders for your goals";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
