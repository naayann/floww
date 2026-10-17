package com.naayann.floow.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.naayann.floow.R;
import com.naayann.floow.utils.SoundManager;
import java.util.Locale;

public class FocusFragment extends Fragment {

    private TextView tvHeader, tvSubtitle, tvTimer, tvStatus;
    private MaterialButton btnStartPause, btnReset, btn15m, btn25m, btn45m, btnChooseBg, btnChooseRingtone;
    private ImageView imgCustomBg;
    private View bgOverlay, timerCardContainer;

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 25 * 60 * 1000L; // default 25 min
    private boolean isTimerRunning = false;
    private Uri selectedRingtoneUri;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> ringtonePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            requireContext().getContentResolver().takePersistableUriPermission(
                                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            if (imgCustomBg != null) {
                                imgCustomBg.setImageURI(uri);
                                applyCustomBackgroundStyling(true);
                            }
                            Toast.makeText(requireContext(), "Cozy background set ✨", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        ringtonePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        selectedRingtoneUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                                data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri.class) :
                                data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                        Toast.makeText(requireContext(), "Ringtone updated 🎵", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_focus, container, false);

        tvHeader = v.findViewById(R.id.tvHeader);
        tvSubtitle = v.findViewById(R.id.tvSubtitle);
        tvTimer = v.findViewById(R.id.tvTimer);
        tvStatus = v.findViewById(R.id.tvStatus);
        btnStartPause = v.findViewById(R.id.btnStartPause);
        btnReset = v.findViewById(R.id.btnReset);
        btn15m = v.findViewById(R.id.btn15m);
        btn25m = v.findViewById(R.id.btn25m);
        btn45m = v.findViewById(R.id.btn45m);
        btnChooseBg = v.findViewById(R.id.btnChooseBg);
        btnChooseRingtone = v.findViewById(R.id.btnChooseRingtone);
        imgCustomBg = v.findViewById(R.id.imgCustomBg);
        bgOverlay = v.findViewById(R.id.bgOverlay);
        timerCardContainer = v.findViewById(R.id.timerCardContainer);

        // Preset buttons
        btn15m.setOnClickListener(view -> setTimerDuration(15 * 60 * 1000L));
        btn25m.setOnClickListener(view -> setTimerDuration(25 * 60 * 1000L));
        btn45m.setOnClickListener(view -> setTimerDuration(45 * 60 * 1000L));

        btnStartPause.setOnClickListener(view -> {
            SoundManager.playTap(requireContext());
            if (isTimerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnReset.setOnClickListener(view -> {
            SoundManager.playTap(requireContext());
            resetTimer();
        });

        btnChooseBg.setOnClickListener(view -> {
            SoundManager.playTap(requireContext());
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        btnChooseRingtone.setOnClickListener(view -> {
            SoundManager.playTap(requireContext());
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Focus Ringtone");
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM | RingtoneManager.TYPE_NOTIFICATION | RingtoneManager.TYPE_RINGTONE);
            if (selectedRingtoneUri != null) {
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedRingtoneUri);
            }
            ringtonePickerLauncher.launch(intent);
        });

        updateTimerText();
        return v;
    }

    private void applyCustomBackgroundStyling(boolean hasCustomBg) {
        if (hasCustomBg) {
            imgCustomBg.setVisibility(View.VISIBLE);
            bgOverlay.setVisibility(View.VISIBLE);
            tvHeader.setTextColor(Color.WHITE);
            tvSubtitle.setTextColor(Color.parseColor("#E5E5EA"));
            tvStatus.setTextColor(Color.parseColor("#E5E5EA"));
            tvTimer.setTextColor(Color.WHITE);
            if (timerCardContainer != null) {
                timerCardContainer.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4D000000")));
            }
        } else {
            imgCustomBg.setVisibility(View.GONE);
            bgOverlay.setVisibility(View.GONE);
            tvHeader.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            tvSubtitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            tvTimer.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            if (timerCardContainer != null) {
                timerCardContainer.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#19FFFFFF")));
            }
        }
    }

    private void setTimerDuration(long millis) {
        if (isTimerRunning) return;
        SoundManager.playTap(requireContext());
        timeLeftInMillis = millis;
        updateTimerText();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                tvStatus.setText("Session completed ✨");
                btnStartPause.setText("Start Focus");
                SoundManager.playCompleted(requireContext());
                playCompletionRingtone();
            }
        }.start();

        isTimerRunning = true;
        tvStatus.setText("Focusing...");
        btnStartPause.setText("Pause");
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        tvStatus.setText("Paused");
        btnStartPause.setText("Resume");
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        timeLeftInMillis = 25 * 60 * 1000L;
        tvStatus.setText("Ready to focus");
        btnStartPause.setText("Start Focus");
        applyCustomBackgroundStyling(false);
        updateTimerText();
    }

    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        tvTimer.setText(timeLeftFormatted);
    }

    private void playCompletionRingtone() {
        try {
            Uri ringtoneUri = selectedRingtoneUri;
            if (ringtoneUri == null) {
                ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                if (ringtoneUri == null) {
                    ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                }
            }
            Ringtone r = RingtoneManager.getRingtone(requireContext(), ringtoneUri);
            if (r != null) {
                r.play();
            }
        } catch (Exception r) {
            // Handled safely
        }
    }
}
