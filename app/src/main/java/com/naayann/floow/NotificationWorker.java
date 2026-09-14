package com.naayann.floow;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.naayann.floow.utils.PrefHelper;

public class NotificationWorker extends Worker {

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        PrefHelper pref = new PrefHelper(getApplicationContext());
        if (pref.areNotificationsEnabled()) {
            Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
            getApplicationContext().sendBroadcast(intent);
        }
        return Result.success();
    }
}
