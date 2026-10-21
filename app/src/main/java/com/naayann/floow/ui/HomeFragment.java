package com.naayann.floow.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.naayann.floow.MainActivity;
import com.naayann.floow.R;
import com.naayann.floow.data.AppDatabase;
import com.naayann.floow.data.TodoDao;
import com.naayann.floow.utils.PrefHelper;
import com.naayann.floow.utils.SoundManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private RecyclerView rvDates;
    private TextView tvGreeting, tvDate, tvStreakBadge;
    private ImageView btnSearch, imgProfile;
    private View cardDailyChallenge, cardProfile, cardStreakHeader;
    private View cardFocusSession, cardJournal, cardProgressGraph, cardAffirmation;
    private TodoDao dao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            dao = AppDatabase.getInstance(requireContext()).todoDao();
        } catch (Exception e) {
            e.printStackTrace();
        }

        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvDate = view.findViewById(R.id.tvDate);
        tvStreakBadge = view.findViewById(R.id.tvStreakBadge);
        imgProfile = view.findViewById(R.id.imgProfile);
        cardProfile = view.findViewById(R.id.cardProfile);
        cardStreakHeader = view.findViewById(R.id.cardStreakHeader);
        cardDailyChallenge = view.findViewById(R.id.cardDailyChallenge);
        rvDates = view.findViewById(R.id.rvDates);

        cardFocusSession = view.findViewById(R.id.cardFocusSession);
        cardJournal = view.findViewById(R.id.cardJournal);
        cardProgressGraph = view.findViewById(R.id.cardProgressGraph);


        updateHeaderInfo();

        if (cardProfile != null) {
            cardProfile.setOnClickListener(v -> {
                SoundManager.playTap(requireContext());
                if (requireActivity() instanceof MainActivity) {
                    ((MainActivity) requireActivity()).selectProfileTab();
                } else {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.fade_in_fast, R.anim.fade_out_fast)
                            .replace(R.id.fragmentContainer, new ProfileFragment())
                            .commit();
                }
            });
        }

        if (cardStreakHeader != null) {
            cardStreakHeader.setOnClickListener(v -> {
                SoundManager.playTap(requireContext());
                startActivity(new Intent(requireContext(), StreaksActivity.class));
            });
        }

        if (cardDailyChallenge != null) {
            cardDailyChallenge.setOnClickListener(v -> openSlidableCardScreen());
        }

        if (cardFocusSession != null) {
            cardFocusSession.setOnClickListener(v -> {
                SoundManager.playTap(requireContext());
                if (requireActivity() instanceof MainActivity) {
                    ((MainActivity) requireActivity()).selectFocusTab();
                } else {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                            .replace(R.id.fragmentContainer, new FocusFragment())
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        if (cardJournal != null) {
            cardJournal.setOnClickListener(v -> {
                SoundManager.playTap(requireContext());
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.fragmentContainer, new JournalFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        if (cardProgressGraph != null) {
            cardProgressGraph.setOnClickListener(v -> {
                SoundManager.playTap(requireContext());
                startActivity(new Intent(requireContext(), ProgressActivity.class));
            });
        }

        if (cardAffirmation != null) {
            cardAffirmation.setOnClickListener(v -> {
                SoundManager.playTap(requireContext());
                Toast.makeText(requireContext(), "You're building great momentum! Keep going ✨", Toast.LENGTH_SHORT).show();
            });
        }

        setupDates();
    }

    private void updateHeaderInfo() {
        if (!isAdded()) return;
        PrefHelper pref = new PrefHelper(requireContext());
        String name = pref.getName();
        if (name == null || name.trim().isEmpty()) {
            name = "Friend";
        }
        if (tvGreeting != null) {
            tvGreeting.setText("Hello, " + name);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMM", Locale.getDefault());
        if (tvDate != null) {
            tvDate.setText(sdf.format(new Date()));
        }

        if (imgProfile != null) {
            imgProfile.setImageResource(pref.getMascotDrawableRes());
        }

        View view = getView();
        if (view != null) {
            ImageView imgDailyChallengeMascot = view.findViewById(R.id.imgDailyChallengeMascot);
            if (imgDailyChallengeMascot != null) {
                imgDailyChallengeMascot.setImageResource(pref.getMascotDrawableRes());
            }

            ImageView imgChallengeAvatar1 = view.findViewById(R.id.imgChallengeAvatar1);
            if (imgChallengeAvatar1 != null) {
                imgChallengeAvatar1.setImageResource(pref.getMascotDrawableRes());
            }
        }

        if (dao != null) {
            try {
                List<String> completionDates = dao.getCompletionDates();
                int streak = calculateStreak(completionDates);
                if (tvStreakBadge != null) {
                    tvStreakBadge.setText(streak + "d");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openSlidableCardScreen() {
        if (!isAdded()) return;
        SoundManager.playTap(requireContext());
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in_fast, R.anim.fade_out_fast)
                .replace(R.id.fragmentContainer, new TodayFragment())
                .addToBackStack(null)
                .commit();
    }

    private void setupDates() {
        if (rvDates == null || !isAdded()) return;
        List<DateItem> dates = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        int currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1 = Sunday, 2 = Monday...
        int daysFromMonday = (currentDayOfWeek + 5) % 7; // Mon -> 0, Tue -> 1, ..., Sun -> 6
        cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday);

        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dayNumFormat = new SimpleDateFormat("d", Locale.getDefault());
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayKey = keyFormat.format(new Date());

        int selectedIndex = 0;
        List<String> completionDates = new ArrayList<>();
        if (dao != null) {
            try {
                completionDates = dao.getCompletionDates();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < 7; i++) {
            String dayName = dayNameFormat.format(cal.getTime()).toUpperCase();
            String dayNum = dayNumFormat.format(cal.getTime());
            String key = keyFormat.format(cal.getTime());
            int completions = 0;
            if (dao != null) {
                try {
                    completions = dao.getCompletionsCountForDate(key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            boolean isCompleted = completions > 0 || (completionDates != null && completionDates.contains(key));
            boolean isToday = key.equals(todayKey);

            if (isToday) {
                selectedIndex = i;
            }

            dates.add(new DateItem(dayName, dayNum, key, completions, isCompleted, isToday));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        rvDates.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvDates.setAdapter(new DateAdapter(dates, selectedIndex, dateItem -> {
            SoundManager.playTap(requireContext());
            if (dateItem.isCompleted) {
                Toast.makeText(requireContext(), dateItem.dayName + " " + dateItem.dayNum + ": " + dateItem.completionsCount + " goals completed! 🔥", Toast.LENGTH_SHORT).show();
            } else if (dateItem.isToday) {
                Toast.makeText(requireContext(), "Today: Tap Daily Challenge to complete goals! ✨", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), dateItem.dayName + " " + dateItem.dayNum + ": No completed goals yet", Toast.LENGTH_SHORT).show();
            }
        }));
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

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            updateHeaderInfo();
            setupDates();
        }
    }

    // ========== Models & Adapters ==========

    public static class DateItem {
        public String dayName;
        public String dayNum;
        public String dateKey;
        public int completionsCount;
        public boolean isCompleted;
        public boolean isToday;

        public DateItem(String dayName, String dayNum, String dateKey, int completionsCount, boolean isCompleted, boolean isToday) {
            this.dayName = dayName;
            this.dayNum = dayNum;
            this.dateKey = dateKey;
            this.completionsCount = completionsCount;
            this.isCompleted = isCompleted;
            this.isToday = isToday;
        }
    }

    public interface OnDateClickListener {
        void onDateClick(DateItem item);
    }

    public class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> {
        private final List<DateItem> dates;
        private final OnDateClickListener listener;
        private int selectedPosition;

        public DateAdapter(List<DateItem> dates, int defaultSelected, OnDateClickListener listener) {
            this.dates = dates;
            this.selectedPosition = defaultSelected;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_strip_day, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            DateItem item = dates.get(currentPos);
            holder.tvDayName.setText(item.dayName);
            holder.tvDayNum.setText(item.dayNum);

            if (item.isCompleted) {
                holder.tvStreakDot.setVisibility(View.VISIBLE);
            } else {
                holder.tvStreakDot.setVisibility(View.GONE);
            }

            if (currentPos == selectedPosition) {
                holder.itemView.setBackgroundResource(R.drawable.bg_nav_pill);
                holder.tvDayName.setTextColor(Color.parseColor("#CCCCCC"));
                holder.tvDayNum.setTextColor(Color.parseColor("#FFFFFF"));
            } else if (item.isCompleted) {
                holder.itemView.setBackgroundResource(R.drawable.bg_mascot_card);
                holder.tvDayName.setTextColor(Color.parseColor("#7A4D00"));
                holder.tvDayNum.setTextColor(Color.parseColor("#1C1C1E"));
            } else {
                holder.itemView.setBackgroundResource(R.drawable.bg_rounded_edit);
                holder.tvDayName.setTextColor(Color.parseColor("#8E8E93"));
                holder.tvDayNum.setTextColor(Color.parseColor("#1C1C1E"));
            }

            holder.itemView.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    selectedPosition = pos;
                    notifyDataSetChanged();
                    if (listener != null) {
                        listener.onDateClick(dates.get(pos));
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return dates.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDayName, tvDayNum, tvStreakDot;

            ViewHolder(View itemView) {
                super(itemView);
                tvDayName = itemView.findViewById(R.id.tvDayName);
                tvDayNum = itemView.findViewById(R.id.tvDayNum);
                tvStreakDot = itemView.findViewById(R.id.tvStreakDot);
            }
        }
    }
}