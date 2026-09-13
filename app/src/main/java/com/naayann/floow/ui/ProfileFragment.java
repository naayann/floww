package com.naayann.floow.ui;

import android.app.AlertDialog;
import android.content.Context;
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
    private RecyclerView rv;
    private TextView tvGreeting, tvDate, tvUserName, tvLifeGoal, tvStreak, tvEnergyValue, tvFocusValue;

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
        tvEnergyValue = view.findViewById(R.id.tvEnergyValue);
        tvFocusValue = view.findViewById(R.id.tvFocusValue);
        rv = view.findViewById(R.id.rvMultipliers);

        view.findViewById(R.id.btnStartFocus).setOnClickListener(v -> {
            SoundManager.playFahh(requireContext());
            Toast.makeText(requireContext(), "Focus session coming soon! 🚀", Toast.LENGTH_SHORT).show();
        });
            
        view.findViewById(R.id.btnViewAll).setOnClickListener(v -> {
            SoundManager.playTap(requireContext());
            showManageDialog();
        });

        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        refresh();
    }

    private void refresh() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greet = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";
        tvGreeting.setText(greet);

        tvDate.setText(new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date()));
        tvUserName.setText(pref.getName());

        List<TodoEntity> all = dao.getAllTodos();
        List<MultiplierAdapter.Item> items = new ArrayList<>();
        int totalCompletions = 0;
        for (TodoEntity t : all) {
            int count = dao.getCompletionCount(t.id);
            items.add(new MultiplierAdapter.Item(t, count));
            totalCompletions += count;
        }
        rv.setAdapter(new MultiplierAdapter(items));
        
        // Mocking some stats based on completions
        tvStreak.setText(String.valueOf(totalCompletions + 7)); // Just to make it look active
        tvEnergyValue.setText(Math.min(100, 50 + totalCompletions * 2) + "%");
        tvFocusValue.setText(Math.min(100, 60 + totalCompletions * 3) + "%");
    }

    public void showManageDialog() {
        // Keep the old manage dialog logic if needed, but updated
        List<TodoEntity> todos = dao.getAllTodos();
        String[] titles = new String[todos.size()];
        for (int i = 0; i < todos.size(); i++) {
            titles[i] = todos.get(i).emoji + "  " + todos.get(i).title;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Manage Goals")
                .setItems(titles, (d, which) -> {
                    SoundManager.playTap(requireContext());
                    new AlertDialog.Builder(requireContext())
                            .setMessage("Delete \"" + todos.get(which).title + "\"?")
                            .setPositiveButton("Delete", (d2, w) -> {
                                SoundManager.playThrow(requireContext());
                                dao.deleteTodo(todos.get(which));
                                refresh();
                            })
                            .setNegativeButton("Cancel", (d2, w) -> SoundManager.playTap(requireContext()))
                            .show();
                })
                .setNegativeButton("Close", (d, w) -> SoundManager.playTap(requireContext()))
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }
}