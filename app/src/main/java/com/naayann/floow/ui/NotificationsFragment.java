package com.naayann.floow.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.naayann.floow.MainActivity;
import com.naayann.floow.NotificationReceiver;
import com.naayann.floow.R;
import com.naayann.floow.utils.PrefHelper;
import com.naayann.floow.utils.SoundManager;

public class NotificationsFragment extends Fragment {

    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notifications, container, false);
        
        prefs = requireContext().getSharedPreferences("floow_settings", Context.MODE_PRIVATE);
        PrefHelper prefHelper = new PrefHelper(requireContext());
        
        SwitchMaterial switchDaily = v.findViewById(R.id.switchDaily);
        SwitchMaterial switchGoals = v.findViewById(R.id.switchGoals);
        SwitchMaterial switchMascot = v.findViewById(R.id.switchMascot);
        
        if (switchDaily != null) switchDaily.setChecked(prefHelper.areNotificationsEnabled());
        if (switchGoals != null) switchGoals.setChecked(prefs.getBoolean("goal_completion", true));
        if (switchMascot != null) switchMascot.setChecked(prefs.getBoolean("mascot_messages", true));
        
        if (switchDaily != null) {
            switchDaily.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SoundManager.playTap(requireContext());
                prefHelper.setNotificationsEnabled(isChecked);
                if (isChecked) {
                    MainActivity.scheduleNotifications(requireContext());
                } else {
                    MainActivity.cancelNotifications(requireContext());
                }
            });
        }
            
        if (switchGoals != null) {
            switchGoals.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SoundManager.playTap(requireContext());
                prefs.edit().putBoolean("goal_completion", isChecked).apply();
            });
        }

        if (switchMascot != null) {
            switchMascot.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SoundManager.playTap(requireContext());
                prefs.edit().putBoolean("mascot_messages", isChecked).apply();
            });
        }
            
        View btnTestNotification = v.findViewById(R.id.btnTestNotification);
        if (btnTestNotification != null) {
            btnTestNotification.setOnClickListener(view -> {
                SoundManager.playTap(requireContext());
                Intent intent = new Intent(requireContext(), NotificationReceiver.class);
                requireContext().sendBroadcast(intent);
                Toast.makeText(requireContext(), "Test notification sent 🔔", Toast.LENGTH_SHORT).show();
            });
        }

        View btnBack = v.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(view -> {
                SoundManager.playTap(requireContext());
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                            .replace(R.id.fragmentContainer, new ProfileFragment())
                            .commit();
                }
            });
        }
            
        return v;
    }
}
