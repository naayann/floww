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
            SoundManager.playFahh(requireContext());
            Toast.makeText(requireContext(), "Focus session coming soon! 🚀", Toast.LENGTH_SHORT).show();
        });
            
        view.findViewById(R.id.btnViewProgress).setOnClickListener(v -> {
            SoundManager.playTap(requireContext());
            startActivity(new Intent(requireContext(), ProgressActivity.class));
        });

        refresh();
    }

    private void refresh() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greet = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";
        tvGreeting.setText(greet);

        tvDate.setText(new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date()));
        tvUserName.setText(pref.getName());

        List<TodoEntity> all = dao.getAllTodos();
        int totalCompletions = 0;
        for (TodoEntity t : all) {
            totalCompletions += dao.getCompletionCount(t.id);
        }
        
        // Mocking some stats based on completions
        tvStreak.setText(String.valueOf(totalCompletions + 7)); // Just to make it look active
    }


    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }
}