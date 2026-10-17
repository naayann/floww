package com.naayann.floow.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.naayann.floow.R;
import com.naayann.floow.data.AppDatabase;
import com.naayann.floow.data.JournalDao;
import com.naayann.floow.data.JournalEntity;
import com.naayann.floow.utils.SoundManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JournalFragment extends Fragment {

    private JournalDao dao;
    private RecyclerView rvJournals;
    private LinearLayout emptyState;
    private ExtendedFloatingActionButton btnAddJournal;
    private String selectedColor = "#FFF9C4"; // Default soft yellow

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_journal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dao = AppDatabase.getInstance(requireContext()).journalDao();

        rvJournals = view.findViewById(R.id.rvJournals);
        emptyState = view.findViewById(R.id.emptyState);
        btnAddJournal = view.findViewById(R.id.btnAddJournal);

        rvJournals.setLayoutManager(new LinearLayoutManager(requireContext()));

        btnAddJournal.setOnClickListener(v -> {
            SoundManager.playTap(requireContext());
            showAddJournalDialog();
        });

        loadJournals();
    }

    private void loadJournals() {
        List<JournalEntity> list = dao.getAllJournals();
        if (list.isEmpty()) {
            rvJournals.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvJournals.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            JournalAdapter adapter = new JournalAdapter(list);
            adapter.setOnJournalClickListener(new JournalAdapter.OnJournalClickListener() {
                @Override
                public void onJournalClick(JournalEntity journal) {
                    SoundManager.playTap(requireContext());
                    showViewJournalDialog(journal);
                }

                @Override
                public void onJournalLongClick(JournalEntity journal) {
                    SoundManager.playThrow(requireContext());
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Delete entry?")
                            .setMessage("“" + journal.title + "” will be removed.")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                dao.deleteJournal(journal);
                                loadJournals();
                                Toast.makeText(requireContext(), "Entry deleted", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Keep", null)
                            .show();
                }
            });
            rvJournals.setAdapter(adapter);
        }
    }

    private void showAddJournalDialog() {
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_journal, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(v)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setDimAmount(0.7f);
        }

        EditText etTitle = v.findViewById(R.id.etJournalTitle);
        EditText etContent = v.findViewById(R.id.etJournalContent);
        MaterialButton btnSave = v.findViewById(R.id.btnSaveJournal);
        ImageView btnClose = v.findViewById(R.id.btnClose);

        selectedColor = "#FFF9C4"; // Default soft yellow

        View[] colors = {
            v.findViewById(R.id.color1),
            v.findViewById(R.id.color2),
            v.findViewById(R.id.color3),
            v.findViewById(R.id.color4),
            v.findViewById(R.id.color5)
        };

        String[] hexColors = {"#FFF9C4", "#FFE5F1FF", "#FFE5F9E0", "#FFFFE5E5", "#FFF3E5F5"};

        for (int i = 0; i < colors.length; i++) {
            final int index = i;
            colors[i].setOnClickListener(cv -> {
                SoundManager.playTap(requireContext());
                selectedColor = hexColors[index];
                for (View c : colors) c.setAlpha(0.5f);
                colors[index].setAlpha(1.0f);
            });
            colors[i].setAlpha(i == 0 ? 1.0f : 0.5f);
        }

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = etTitle.getText().toString().trim().length() > 0 &&
                                  etContent.getText().toString().trim().length() > 0;
                btnSave.setEnabled(hasText);
                btnSave.setAlpha(hasText ? 1.0f : 0.5f);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        etTitle.addTextChangedListener(textWatcher);
        etContent.addTextChangedListener(textWatcher);

        btnClose.setOnClickListener(view -> {
            SoundManager.playTap(requireContext());
            dialog.dismiss();
        });

        btnSave.setOnClickListener(view -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            if (title.isEmpty() || content.isEmpty()) return;

            String dateStr = new SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(new Date());
            dao.insertJournal(new JournalEntity(title, content, dateStr, selectedColor));
            SoundManager.playPop(requireContext());
            Toast.makeText(requireContext(), "Journal saved ☕", Toast.LENGTH_SHORT).show();
            loadJournals();
            dialog.dismiss();
        });

        dialog.show();

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

    private void showViewJournalDialog(JournalEntity journal) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(journal.title)
                .setMessage(journal.date + "\n\n" + journal.content)
                .setPositiveButton("Close", null)
                .setNeutralButton("Delete", (dialog, which) -> {
                    SoundManager.playThrow(requireContext());
                    dao.deleteJournal(journal);
                    loadJournals();
                    Toast.makeText(requireContext(), "Entry deleted", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadJournals();
    }
}
