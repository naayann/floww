package com.naayann.floow;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.naayann.floow.data.AppDatabase;
import com.naayann.floow.data.TodoDao;
import com.naayann.floow.data.TodoEntity;
import com.naayann.floow.ui.FocusFragment;
import com.naayann.floow.ui.HomeFragment;
import com.naayann.floow.ui.JournalEntryFragment;
import com.naayann.floow.ui.JournalFragment;
import com.naayann.floow.ui.ProfileFragment;
import com.naayann.floow.ui.TodayFragment;
import com.naayann.floow.utils.SoundManager;
import com.google.android.material.button.MaterialButton;
import com.naayann.floow.utils.PrefHelper;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ImageView navHome, navProfile;
    private View btnPlus;
    private TodoDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PrefHelper pref = new PrefHelper(this);
        if (!pref.isOnboardingDone()) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        try {
            dao = AppDatabase.getInstance(this).todoDao();
        } catch (Exception e) {
            e.printStackTrace();
        }

        navHome = findViewById(R.id.navHome);
        navProfile = findViewById(R.id.navProfile);
        btnPlus = findViewById(R.id.btnPlus);

        SoundManager.playWelcome(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        if (savedInstanceState == null) {
            selectHomeTab();
        }

        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                super.onFragmentResumed(fm, f);
                if (f instanceof FocusFragment ||
                    f instanceof JournalFragment ||
                    f instanceof JournalEntryFragment) {
                    setBottomNavVisible(false);
                } else {
                    setBottomNavVisible(true);
                }
            }
        }, true);

        if (pref.areNotificationsEnabled()) {
            scheduleNotifications(this);
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                SoundManager.playTap(this);
                selectHomeTab();
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                SoundManager.playTap(this);
                selectProfileTab();
            });
        }

        if (btnPlus != null) {
            btnPlus.setOnClickListener(v -> {
                SoundManager.playDone(this);
                v.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).withEndAction(() -> {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    showAddDialog();
                }).start();
            });
        }
    }

    public static void scheduleNotifications(Context context) {
        try {
            PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                    NotificationWorker.class, 3, TimeUnit.HOURS)
                    .addTag("notification_work")
                    .build();
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "notification_work",
                    ExistingPeriodicWorkPolicy.KEEP,
                    request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cancelNotifications(Context context) {
        try {
            WorkManager.getInstance(context).cancelAllWorkByTag("notification_work");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadFragment(Fragment fragment, boolean clearBackStack) {
        if (isFinishing() || isDestroyed()) return;
        if (clearBackStack) {
            try {
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } catch (Exception ignored) {}
        }
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in_fast, R.anim.fade_out_fast)
                .replace(R.id.fragmentContainer, fragment)
                .commitAllowingStateLoss();
    }

    public void setBottomNavVisible(boolean visible) {
        View bottomNavPill = findViewById(R.id.bottomNavPill);
        if (bottomNavPill != null) {
            bottomNavPill.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void selectHomeTab() {
        loadFragment(new HomeFragment(), true);
        highlight(navHome);
    }

    public void selectProfileTab() {
        loadFragment(new ProfileFragment(), true);
        highlight(navProfile);
    }

    public void selectFocusTab() {
        if (isFinishing() || isDestroyed()) return;
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragmentContainer, new FocusFragment())
                .commitAllowingStateLoss();
        highlight(null);
    }

    private void highlight(ImageView active) {
        int activeColor = ContextCompat.getColor(this, R.color.black);
        int inactiveColor = ContextCompat.getColor(this, R.color.nav_inactive);

        ImageView[] navIcons = {navHome, navProfile};
        for (ImageView nav : navIcons) {
            if (nav == null) continue;
            if (nav == active) {
                nav.setBackgroundResource(R.drawable.bg_nav_item_active);
                nav.setColorFilter(activeColor);
            } else {
                nav.setBackgroundResource(0);
                nav.setColorFilter(inactiveColor);
            }
        }
    }

    private String selectedColor = "#d4a373";
    private String selectedCategory = "Focus";

    public void showAddDialog() {
        if (isFinishing() || isDestroyed()) return;
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_add_todo, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(v)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setDimAmount(0.7f);
        }

        EditText etEmoji = v.findViewById(R.id.etEmoji);
        EditText etTitle = v.findViewById(R.id.etTitle);
        MaterialButton btnSave = v.findViewById(R.id.btnSave);
        View btnClose = v.findViewById(R.id.btnClose);
        CardView cardStickyNote = v.findViewById(R.id.cardStickyNote);

        TextView chipFocus = v.findViewById(R.id.chipFocus);
        TextView chipHealth = v.findViewById(R.id.chipHealth);
        TextView chipWork = v.findViewById(R.id.chipWork);
        TextView chipAddCategory = v.findViewById(R.id.chipAddCategory);

        selectedColor = "#d4a373";
        selectedCategory = "Focus";
        if (cardStickyNote != null) {
            try {
                cardStickyNote.setCardBackgroundColor(Color.parseColor(selectedColor));
            } catch (Exception ignored) {}
        }

        TextView[] categoryChips = {chipFocus, chipHealth, chipWork};
        String[] categoryNames = {"Focus", "Health", "Work"};

        Runnable updateCategoryUI = () -> {
            for (int i = 0; i < categoryChips.length; i++) {
                if (categoryChips[i] != null) {
                    if (categoryNames[i].equals(selectedCategory)) {
                        categoryChips[i].setBackgroundResource(R.drawable.bg_nav_pill);
                        categoryChips[i].setTextColor(ContextCompat.getColor(this, R.color.white));
                    } else {
                        categoryChips[i].setBackgroundResource(R.drawable.bg_rounded_edit);
                        categoryChips[i].setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                    }
                }
            }
        };

        updateCategoryUI.run();

        for (int i = 0; i < categoryChips.length; i++) {
            final int index = i;
            if (categoryChips[i] != null) {
                categoryChips[i].setOnClickListener(cv -> {
                    SoundManager.playTap(this);
                    selectedCategory = categoryNames[index];
                    updateCategoryUI.run();
                });
            }
        }

        if (chipAddCategory != null) {
            chipAddCategory.setOnClickListener(cv -> {
                SoundManager.playTap(this);
                EditText input = new EditText(this);
                input.setHint("Category name");
                new AlertDialog.Builder(this)
                        .setTitle("New Category")
                        .setView(input)
                        .setPositiveButton("Add", (d, w) -> {
                            String customCat = input.getText().toString().trim();
                            if (!customCat.isEmpty()) {
                                selectedCategory = customCat;
                                chipAddCategory.setText(customCat);
                                chipAddCategory.setBackgroundResource(R.drawable.bg_nav_pill);
                                chipAddCategory.setTextColor(ContextCompat.getColor(this, R.color.white));
                                for (TextView c : categoryChips) {
                                    if (c != null) {
                                        c.setBackgroundResource(R.drawable.bg_rounded_edit);
                                        c.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                                    }
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
        
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
            if (colors[i] != null) {
                colors[i].setOnClickListener(cv -> {
                    SoundManager.playTap(this);
                    selectedColor = hexColors[index];
                    if (cardStickyNote != null) {
                        try {
                            cardStickyNote.setCardBackgroundColor(Color.parseColor(selectedColor));
                        } catch (Exception ignored) {}
                    }
                    for (View c : colors) {
                        if (c != null) c.setAlpha(0.5f);
                    }
                    colors[index].setAlpha(1.0f);
                });
                colors[i].setAlpha(i == 0 ? 1.0f : 0.5f);
            }
        }

        if (etTitle != null) {
            etTitle.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (btnSave != null) {
                        if (s.length() > 0) {
                            btnSave.setEnabled(true);
                            btnSave.setAlpha(1.0f);
                        } else {
                            btnSave.setEnabled(false);
                            btnSave.setAlpha(0.5f);
                        }
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(view -> {
                SoundManager.playTap(this);
                v.animate()
                        .alpha(0f)
                        .scaleX(0.7f)
                        .scaleY(0.7f)
                        .setDuration(300)
                        .withEndAction(dialog::dismiss)
                        .start();
            });
        }
        
        if (btnSave != null) {
            btnSave.setOnClickListener(view -> {
                String title = etTitle != null ? etTitle.getText().toString().trim() : "";
                String emoji = etEmoji != null ? etEmoji.getText().toString().trim() : "";
                
                if (title.isEmpty()) return;
                if (emoji.isEmpty()) emoji = "✨";

                if (dao != null) {
                    try {
                        dao.insertTodo(new TodoEntity(title, emoji, selectedColor, selectedCategory));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                SoundManager.playPop(this);
                Toast.makeText(this, "Goal added!", Toast.LENGTH_SHORT).show();
                
                Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                if (current instanceof TodayFragment) {
                    ((TodayFragment) current).onResume();
                } else if (current instanceof HomeFragment) {
                    ((HomeFragment) current).onResume();
                }
                
                v.animate()
                        .alpha(0f)
                        .scaleX(0.7f)
                        .scaleY(0.7f)
                        .setDuration(300)
                        .withEndAction(dialog::dismiss)
                        .start();
            });
        }

        dialog.show();

        if (etEmoji != null) {
            etEmoji.requestFocus();
            etEmoji.post(() -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etEmoji, InputMethodManager.SHOW_IMPLICIT);
                }
            });
        }

        v.setAlpha(0f);
        v.setScaleX(0.7f);
        v.setScaleY(0.7f);
        v.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();
    }
}