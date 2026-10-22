package com.naayann.floow.ui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.naayann.floow.R;
import com.naayann.floow.data.AppDatabase;
import com.naayann.floow.data.TodoDao;
import com.naayann.floow.utils.SoundManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StreaksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaks);

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            SoundManager.playTap(this);
            finish();
        });

        TodoDao dao = AppDatabase.getInstance(this).todoDao();
        List<String> completionDates = dao.getCompletionDates();
        int streak = calculateStreak(completionDates);

        TextView tvStreakNumber = findViewById(R.id.tvStreakNumber);
        tvStreakNumber.setText(streak + (streak == 1 ? " Day" : " Days"));

        RecyclerView rvCalendarGrid = findViewById(R.id.rvCalendarGrid);
        rvCalendarGrid.setLayoutManager(new GridLayoutManager(this, 7));

        List<CalendarDay> days = generateCurrentMonthDays(completionDates);
        rvCalendarGrid.setAdapter(new CalendarGridAdapter(days));

        TextView tvMonthTitle = findViewById(R.id.tvMonthTitle);
        String monthName = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        tvMonthTitle.setText("Activity (" + monthName + ")");
    }

    private List<CalendarDay> generateCurrentMonthDays(List<String> completionDates) {
        List<CalendarDay> days = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);

        cal.set(year, month, 1);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        // Calendar day of week: Sunday = 1, Monday = 2, ..., Saturday = 7
        // Adjust for Monday start: Mon = 0, Tue = 1, ..., Sun = 6
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int offset = (firstDayOfWeek + 5) % 7; // Monday = 0

        // Add empty padding for alignment
        for (int i = 0; i < offset; i++) {
            days.add(new CalendarDay("", false, false));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar dayCal = Calendar.getInstance();

        for (int d = 1; d <= maxDay; d++) {
            dayCal.set(year, month, d);
            String dateStr = sdf.format(dayCal.getTime());
            boolean isCompleted = completionDates.contains(dateStr);
            boolean isToday = isSameDay(dayCal, Calendar.getInstance());
            days.add(new CalendarDay(String.valueOf(d), isCompleted, isToday));
        }

        return days;
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
               c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
               c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);
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

    private static class CalendarDay {
        String dayText;
        boolean isCompleted;
        boolean isToday;

        CalendarDay(String dayText, boolean isCompleted, boolean isToday) {
            this.dayText = dayText;
            this.isCompleted = isCompleted;
            this.isToday = isToday;
        }
    }

    private static class CalendarGridAdapter extends RecyclerView.Adapter<CalendarGridAdapter.VH> {
        private final List<CalendarDay> days;

        CalendarGridAdapter(List<CalendarDay> days) {
            this.days = days;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            CalendarDay day = days.get(position);
            holder.tvDay.setText(day.dayText);

            if (day.dayText.isEmpty()) {
                holder.tvDay.setVisibility(View.INVISIBLE);
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                return;
            }

            holder.tvDay.setVisibility(View.VISIBLE);

            if (day.isCompleted) {
                holder.tvDay.setBackgroundResource(R.drawable.circle_yellow);
                holder.tvDay.setTextColor(Color.parseColor("#1C1C1E"));
                holder.tvDay.setTypeface(null, Typeface.BOLD);
            } else if (day.isToday) {
                holder.tvDay.setBackgroundResource(R.drawable.bg_rounded_edit);
                holder.tvDay.setTextColor(Color.parseColor("#000000"));
                holder.tvDay.setTypeface(null, Typeface.BOLD);
            } else {
                holder.tvDay.setBackgroundColor(Color.TRANSPARENT);
                holder.tvDay.setTextColor(Color.parseColor("#8E8E93"));
                holder.tvDay.setTypeface(null, Typeface.NORMAL);
            }
        }

        @Override
        public int getItemCount() {
            return days.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvDay;
            VH(View v) {
                super(v);
                tvDay = v.findViewById(R.id.tvDay);
            }
        }
    }
}
