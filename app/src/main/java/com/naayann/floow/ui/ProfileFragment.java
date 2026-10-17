package com.naayann.floow.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.naayann.floow.R;
import com.naayann.floow.data.AppDatabase;
import com.naayann.floow.data.TodoDao;
import com.naayann.floow.data.TodoEntity;
import com.naayann.floow.utils.PrefHelper;
import com.naayann.floow.utils.SoundManager;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private TodoDao dao;
    private PrefHelper pref;
    private TextView tvGreeting, tvDate, tvUserName, tvStreak;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dao = AppDatabase.getInstance(requireContext()).todoDao();
        pref = new PrefHelper(requireContext());

        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvDate = view.findViewById(R.id.tvDate);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvStreak = view.findViewById(R.id.tvStreak);

        view.findViewById(R.id.btnStartFocus).setOnClickListener(v -> {
            SoundManager.playTap(requireContext());
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fade_in_fast, R.anim.fade_out_fast)
                    .replace(R.id.fragmentContainer, new FocusFragment())
                    .commit();
        });
            
        view.findViewById(R.id.btnViewProgress).setOnClickListener(v -> {
            SoundManager.playTap(requireContext());
            startActivity(new Intent(requireContext(), ProgressActivity.class));
        });

        view.findViewById(R.id.cardStreak).setOnClickListener(v -> {
            SoundManager.playTap(requireContext());
            startActivity(new Intent(requireContext(), StreaksActivity.class));
        });

        refresh();
    }

    private void refresh() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greet = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";
        tvGreeting.setText(greet);

        tvDate.setText(new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date()));
        tvUserName.setText(pref.getName());

        List<String> completionDates = dao.getCompletionDates();
        int streak = calculateStreak(completionDates);
        tvStreak.setText(String.valueOf(streak));
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

        int streak = 0;
        Calendar checkCal = Calendar.getInstance();
        if (!hasToday) {
            checkCal.add(Calendar.DATE, -1);
        }

        while (true) {
            String checkStr = sdf.format(checkCal.getTime());
            if (completionDates.contains(checkStr)) {
                streak++;
                checkCal.add(Calendar.DATE, -1);
            } else {
                break;
            }
        }

        return streak;
    }


    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }
}