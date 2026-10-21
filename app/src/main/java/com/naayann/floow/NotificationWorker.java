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

        if (hour >= 8 && hour < 13) {
            slot = today + "_MORNING";
            title = "Ready to start?";
            String[] messages = {
                    "Your goals won't chase themselves.",
                    "Potential is cute. Progress is better.",
                    "Still scrolling? Your future's waiting.",
                    "That syllabus isn't getting smaller.",
                    "Mood optional. Show up.",
                    "Study now. Flex later.",
                    "Less excuses. More receipts.",
                    "Your comeback starts today.",
                    "Win the morning. Own the day.",
                    "Five minutes. Prove it.",
                    "Someone's getting ahead. Why not you?",
                    "Future you is taking notes.",
                    "Don't let another day ghost your goals.",
                    "A little grind. A lot less panic.",
                    "One task ahead of yesterday."
            };
            message = messages[random.nextInt(messages.length)];
        } else if (hour >= 13 && hour < 19) {
            slot = today + "_AFTERNOON";
            title = "Afternoon Check!";
            String[] messages = {
                    "Half the day's gone. Move.",
                    "Your goals won't wait.",
                    "Others are grinding. You in? 👀",
                    "Stop scrolling. Start winning.",
                    "Deadlines don't care.",
                    "Make the comeback. 🔥",
                    "Less excuses. More progress.",
                    "Still time to get ahead.",
                    "Don't waste today.",
                    "Lock in. Thank yourself later."
            };
            message = messages[random.nextInt(messages.length)];
        } else if (hour >= 19) {
            slot = today + "_NIGHT";
            title = "Wrap-up!";
            String[] messages = {
                    "They're ahead. Catch up. 👀",
                    "One task. No regrets.",
                    "Don't sleep on your goals.",
                    "Tomorrow starts tonight. 🔥",
                    "Still time. Don't waste it.",
                    "Your streak is on the line.",
                    "Finish now. Flex tomorrow.",
                    "Sleep later. Win first. ⚡",
                    "Don't let today beat you.",
                    "Last chance to lock in."
            };
            message = messages[random.nextInt(messages.length)];
        } else {
            slot = today + "_EXTRA";
            title = "Floww Reminder ⚡";
            message = "Stay focused on your goals today!";
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
