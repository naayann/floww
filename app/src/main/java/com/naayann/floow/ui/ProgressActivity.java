package com.naayann.floow.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.naayann.floow.R;
import com.naayann.floow.data.AppDatabase;
import com.naayann.floow.data.TodoDao;
import com.naayann.floow.data.TodoEntity;
import com.naayann.floow.utils.SoundManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProgressActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                SoundManager.playTap(this);
                finish();
            });
        }

        TodoDao dao = null;
        try {
            dao = AppDatabase.getInstance(this).todoDao();
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView tvStatTotal = findViewById(R.id.tvStatTotal);
        TextView tvStatStreak = findViewById(R.id.tvStatStreak);
        TextView tvStatToday = findViewById(R.id.tvStatToday);
        ProgressChartView chartProgress = findViewById(R.id.chartProgress);

        int totalCompletions = 0;
        List<String> completionDates = new ArrayList<>();
        int totalTodos = 0;
        int pendingCount = 0;

        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (dao != null) {
            try {
                totalCompletions = dao.getTotalCompletionsCount();
                List<String> dates = dao.getCompletionDates();
                if (dates != null) completionDates = dates;
                totalTodos = dao.getTodoCount();
                List<TodoEntity> pending = dao.getPendingToday(todayStr);
                if (pending != null) pendingCount = pending.size();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (tvStatTotal != null) {
            tvStatTotal.setText(String.valueOf(totalCompletions));
        }

        int streak = calculateStreak(completionDates);
        if (tvStatStreak != null) {
            tvStatStreak.setText(streak + "d");
        }

        int doneToday = Math.max(0, totalTodos - pendingCount);
        if (tvStatToday != null) {
            tvStatToday.setText(doneToday + "/" + totalTodos);
        }

        // Chart data for last 7 days
        List<ProgressChartView.DayData> chartData = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -6);

        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            String dayLabel = dayNameFormat.format(cal.getTime());
            String dateKey = keyFormat.format(cal.getTime());

            int count = 0;
            if (dao != null) {
                try {
                    count = dao.getCompletionsCountForDate(dateKey);
                } catch (Exception ignored) {}
            }
            boolean isToday = dateKey.equals(todayStr);

            chartData.add(new ProgressChartView.DayData(dayLabel, count, isToday));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (chartProgress != null) {
            chartProgress.setData(chartData);
        }

        // Goal breakdown list
        RecyclerView rv = findViewById(R.id.rvProgress);
        if (rv != null) {
            rv.setLayoutManager(new GridLayoutManager(this, 2));

            List<TodoEntity> all = new ArrayList<>();
            if (dao != null) {
                try {
                    List<TodoEntity> todos = dao.getAllTodos();
                    if (todos != null) all = todos;
                } catch (Exception ignored) {}
            }

            List<MultiplierAdapter.Item> items = new ArrayList<>();
            final TodoDao finalDao = dao;
            for (TodoEntity t : all) {
                int count = 0;
                if (finalDao != null) {
                    try {
                        count = finalDao.getCompletionCount(t.id);
                    } catch (Exception ignored) {}
                }
                items.add(new MultiplierAdapter.Item(t, count));
            }

            MultiplierAdapter adapter = new MultiplierAdapter(items);
            adapter.setOnItemClickListener(item -> {
                SoundManager.playTap(this);
                int pos = items.indexOf(item);
                showDeleteTaskDialog(finalDao, item, items, adapter, pos);
            });
            rv.setAdapter(adapter);
        }
    }

    private void showDeleteTaskDialog(TodoDao dao, MultiplierAdapter.Item item, List<MultiplierAdapter.Item> items, MultiplierAdapter adapter, int pos) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_delete_confirm, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(v)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setDimAmount(0.7f);
        }

        TextView tvTitle = v.findViewById(R.id.tvDeleteTitle);
        TextView tvMessage = v.findViewById(R.id.tvDeleteMessage);
        View btnCancel = v.findViewById(R.id.btnCancelDelete);
        View btnConfirm = v.findViewById(R.id.btnConfirmDelete);

        if (tvTitle != null) tvTitle.setText("Delete task?");
        if (tvMessage != null) tvMessage.setText("“" + item.todo.title + "” and all its completion records will be removed.");

        if (btnCancel != null) {
            btnCancel.setOnClickListener(view -> {
                SoundManager.playTap(this);
                dialog.dismiss();
            });
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(view -> {
                SoundManager.playThrow(this);
                if (dao != null) {
                    try {
                        dao.deleteCompletionsForTodo(item.todo.id);
                        dao.deleteTodo(item.todo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (pos != -1) {
                    items.remove(pos);
                    adapter.notifyItemRemoved(pos);
                    adapter.notifyItemRangeChanged(pos, items.size() - pos);
                }
                Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private int calculateStreak(List<String> completionDates) {
        if (completionDates == null || completionDates.isEmpty()) {
            return 0;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        String todayStr = sdf.format(cal.getTime());

        cal.add(Calendar.DATE, -1);
        String yesterdayStr = sdf.format(cal.getTime());

        boolean hasToday = completionDates.contains(todayStr);
        boolean hasYesterday = completionDates.contains(yesterdayStr);

        if (!hasToday && !hasYesterday) {
            return 0;
        }

        int activeStreak = 0;
        Calendar checkCal = Calendar.getInstance();
        if (!hasToday) {
            checkCal.add(Calendar.DATE, -1);
        }

        while (true) {
            String checkStr = sdf.format(checkCal.getTime());
            if (completionDates.contains(checkStr)) {
                activeStreak++;
                checkCal.add(Calendar.DATE, -1);
            } else {
                break;
            }
        }

        return activeStreak;
    }
}
