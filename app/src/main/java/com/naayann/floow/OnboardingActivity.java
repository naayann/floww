package com.naayann.floow;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.naayann.floow.utils.PrefHelper;
import com.naayann.floow.utils.SoundManager;
import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends AppCompatActivity {

    private PrefHelper pref;
    private LinearLayout container;
    private int step = 0;
    private String name = "", goal = "";
    private int age = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        pref = new PrefHelper(this);
        if (pref.isOnboardingDone()) {
            goToMain();
            return;
        }

        container = findViewById(R.id.onboardingContainer);
        showStep();
    }

    private void showStep() {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        switch (step) {
            case 0: {
                View v = inflater.inflate(R.layout.step_name, container, false);
                EditText et = v.findViewById(R.id.etName);
                MaterialButton btn = v.findViewById(R.id.btnContinue);
                btn.setOnClickListener(view -> {
                    SoundManager.playTap(this);
                    name = et.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(this, "Enter your full name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    step = 1;
                    showStep();
                });
                container.addView(v);
                break;
            }
            case 1: {
                View v = inflater.inflate(R.layout.step_age, container, false);
                EditText et = v.findViewById(R.id.etAge);
                MaterialButton btn = v.findViewById(R.id.btnContinue);
                btn.setOnClickListener(view -> {
                    SoundManager.playTap(this);
                    try {
                        age = Integer.parseInt(et.getText().toString().trim());
                        if (age < 13 || age > 99) throw new NumberFormatException();
                    } catch (Exception e) {
                        Toast.makeText(this, "Enter a valid age", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    step = 2;
                    showStep();
                });
                container.addView(v);
                break;
            }
            default: {
                View v = inflater.inflate(R.layout.step_goal, container, false);
                EditText et = v.findViewById(R.id.etGoal);
                MaterialButton btn = v.findViewById(R.id.btnContinue);
                btn.setOnClickListener(view -> {
                    SoundManager.playTap(this);
                    goal = et.getText().toString().trim();
                    if (TextUtils.isEmpty(goal)) {
                        Toast.makeText(this, "Enter your life goal", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    pref.saveUser(name, age, goal);
                    pref.setOnboardingDone(true);
                    goToMain();
                });
                container.addView(v);
                break;
            }
        }
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}