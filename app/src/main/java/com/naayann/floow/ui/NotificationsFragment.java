package com.naayann.floow.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.naayann.floow.R;
import com.naayann.floow.NotificationReceiver;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.content.Intent;

public class NotificationsFragment extends Fragment {

    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notifications, container, false);
        
        prefs = requireContext().getSharedPreferences("floow_settings", Context.MODE_PRIVATE);
        
        SwitchMaterial switchDaily = v.findViewById(R.id.switchDaily);
        SwitchMaterial switchGoals = v.findViewById(R.id.switchGoals);
        SwitchMaterial switchMascot = v.findViewById(R.id.switchMascot);
        
        switchDaily.setChecked(prefs.getBoolean("daily_reminders", true));
        switchGoals.setChecked(prefs.getBoolean("goal_completion", true));
        switchMascot.setChecked(prefs.getBoolean("mascot_messages", true));
        
        switchDaily.setOnCheckedChangeListener((buttonView, isChecked) -> 
            prefs.edit().putBoolean("daily_reminders", isChecked).apply());
            
        switchGoals.setOnCheckedChangeListener((buttonView, isChecked) -> 
            prefs.edit().putBoolean("goal_completion", isChecked).apply());

        switchMascot.setOnCheckedChangeListener((buttonView, isChecked) -> 
            prefs.edit().putBoolean("mascot_messages", isChecked).apply());
            
        v.findViewById(R.id.btnTestNotification).setOnClickListener(view -> {
            Intent intent = new Intent(requireContext(), NotificationReceiver.class);
            requireContext().sendBroadcast(intent);
        });
            
        return v;
    }
}