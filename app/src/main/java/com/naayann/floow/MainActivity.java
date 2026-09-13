package com.naayann.floow;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.naayann.floow.data.AppDatabase;
import com.naayann.floow.data.TodoDao;
import com.naayann.floow.data.TodoEntity;
import com.naayann.floow.ui.InboxFragment;
import com.naayann.floow.ui.NotificationsFragment;
import com.naayann.floow.ui.ProfileFragment;
import com.naayann.floow.ui.TodayFragment;
import com.naayann.floow.utils.SoundManager;
import com.google.android.material.button.MaterialButton;
import com.naayann.floow.utils.PrefHelper;

public class MainActivity extends AppCompatActivity {

    private ImageView navHome, navInbox, navNotifications, navProfile;
    private View btnPlus;
    private TodoDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dao = AppDatabase.getInstance(this).todoDao();

        navHome = findViewById(R.id.navHome);
        navInbox = findViewById(R.id.navInbox);
        navNotifications = findViewById(R.id.navNotifications);
        navProfile = findViewById(R.id.navProfile);
        btnPlus = findViewById(R.id.btnPlus);

        SoundManager.playWelcome(this);

        // Default
        loadFragment(new TodayFragment());
        highlight(navHome);

        navHome.setOnClickListener(v -> {
            SoundManager.playTap(this);
            loadFragment(new TodayFragment());
            highlight(navHome);
        });

        navInbox.setOnClickListener(v -> {
            SoundManager.playTap(this);
            loadFragment(new InboxFragment());
            highlight(navInbox);
        });

        navNotifications.setOnClickListener(v -> {
            SoundManager.playTap(this);
            loadFragment(new NotificationsFragment());
            highlight(navNotifications);
        });

        navProfile.setOnClickListener(v -> {
            SoundManager.playTap(this);
            loadFragment(new ProfileFragment());
            highlight(navProfile);
        });

        btnPlus.setOnClickListener(v -> {
            SoundManager.playTap(this);
            showAddDialog();
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in_fast, R.anim.fade_out_fast)
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void highlight(ImageView active) {
        int activeColor = getResources().getColor(R.color.nav_active);
        int inactiveColor = getResources().getColor(R.color.nav_inactive);
        
        navHome.setColorFilter(inactiveColor);
        navInbox.setColorFilter(inactiveColor);
        navNotifications.setColorFilter(inactiveColor);
        navProfile.setColorFilter(inactiveColor);
        
        active.setColorFilter(activeColor);
    }

    private String selectedColor = "#d4a373";

    public void showAddDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_add_todo, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(v)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // Dim background
            dialog.getWindow().setDimAmount(0.7f);
        }

        EditText etEmoji = v.findViewById(R.id.etEmoji);
        EditText etTitle = v.findViewById(R.id.etTitle);
        MaterialButton btnSave = v.findViewById(R.id.btnSave);
        View btnClose = v.findViewById(R.id.btnClose);

        selectedColor = "#d4a373"; // Default
        
        View[] colors = {
            v.findViewById(R.id.color1),
            v.findViewById(R.id.color2),
            v.findViewById(R.id.color3),
            v.findViewById(R.id.color4),
            v.findViewById(R.id.color5)
        };
        
        String[] hexColors = {"#d4a373", "#E5F1FF", "#E5F9E0", "#FFE5E5", "#F3E5F5"};
        
        for (int i = 0; i < colors.length; i++) {
            final int index = i;
            colors[i].setOnClickListener(cv -> {
                SoundManager.playTap(this);
                selectedColor = hexColors[index];
                for (View c : colors) c.setAlpha(0.5f);
                colors[index].setAlpha(1.0f);
            });
            colors[i].setAlpha(i == 0 ? 1.0f : 0.5f);
        }

        etTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnSave.setEnabled(true);
                    btnSave.setAlpha(1.0f);
                } else {
                    btnSave.setEnabled(false);
                    btnSave.setAlpha(0.5f);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClose.setOnClickListener(view -> {
            SoundManager.playTap(this);
            dialog.dismiss();
        });
        
        btnSave.setOnClickListener(view -> {
            String title = etTitle.getText().toString().trim();
            String emoji = etEmoji.getText().toString().trim();
            
            if (title.isEmpty()) return;
            if (emoji.isEmpty()) emoji = "✨";

            dao.insertTodo(new TodoEntity(title, emoji, selectedColor));
            SoundManager.playPop(this);
            Toast.makeText(this, "Goal added!", Toast.LENGTH_SHORT).show();
            
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            if (current instanceof TodayFragment) {
                ((TodayFragment) current).onResume();
            }
            
            dialog.dismiss();
        });

        dialog.show();
    }

    private boolean isEmoji(String s) {
        int type = Character.getType(s.codePointAt(0));
        return type == Character.SURROGATE || type == Character.OTHER_SYMBOL;
    }
}