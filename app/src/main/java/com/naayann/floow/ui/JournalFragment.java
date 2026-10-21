package com.naayann.floow.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.naayann.floow.MainActivity;
import com.naayann.floow.R;
import com.naayann.floow.data.AppDatabase;
import com.naayann.floow.data.JournalDao;
import com.naayann.floow.data.JournalEntity;
import com.naayann.floow.utils.SoundManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JournalFragment extends Fragment {

    private JournalDao dao;
    private RecyclerView rvJournals;
    private LinearLayout emptyState;
    private FloatingActionButton btnAddJournal;
    private TextView tvEntryCount;
    private EditText etSearchNotes;

    private List<JournalEntity> allJournals = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_journal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).setBottomNavVisible(false);
        }

        try {
            dao = AppDatabase.getInstance(requireContext()).journalDao();
        } catch (Exception e) {
            e.printStackTrace();
        }

        rvJournals = view.findViewById(R.id.rvJournals);
        emptyState = view.findViewById(R.id.emptyState);
        btnAddJournal = view.findViewById(R.id.btnAddJournal);
        tvEntryCount = view.findViewById(R.id.tvEntryCount);
        etSearchNotes = view.findViewById(R.id.etSearchNotes);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                SoundManager.playTap(requireContext());
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else if (requireActivity() instanceof MainActivity) {
                    ((MainActivity) requireActivity()).selectHomeTab();
                }
            });
        }

        if (rvJournals != null) {
            rvJournals.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        if (btnAddJournal != null) {
            btnAddJournal.setOnClickListener(v -> {
                SoundManager.playTap(requireContext());
                openJournalEntry(null);
            });
        }

        if (emptyState != null) {
            emptyState.setOnClickListener(v -> {
                SoundManager.playTap(requireContext());
                openJournalEntry(null);
            });
        }

        if (etSearchNotes != null) {
            etSearchNotes.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterJournals(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        loadJournals();
    }

    private void openJournalEntry(@Nullable JournalEntity journal) {
        if (!isAdded()) return;
        JournalEntryFragment fragment = journal != null ? JournalEntryFragment.newInstance(journal.id) : new JournalEntryFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.note_enter, R.anim.note_exit, R.anim.note_enter, R.anim.note_exit)
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void loadJournals() {
        if (!isAdded()) return;

        allJournals = new ArrayList<>();
        if (dao != null) {
            try {
                List<JournalEntity> all = dao.getAllJournals();
                if (all != null) allJournals = all;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String currentSearch = etSearchNotes != null ? etSearchNotes.getText().toString() : "";
        filterJournals(currentSearch);
    }

    private void filterJournals(String query) {
        if (!isAdded()) return;

        List<JournalEntity> filtered = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            filtered.addAll(allJournals);
        } else {
            String q = query.toLowerCase(Locale.getDefault()).trim();
            for (JournalEntity entry : allJournals) {
                if ((entry.title != null && entry.title.toLowerCase(Locale.getDefault()).contains(q)) ||
                    (entry.content != null && entry.content.toLowerCase(Locale.getDefault()).contains(q))) {
                    filtered.add(entry);
                }
            }
        }

        if (tvEntryCount != null) {
            int total = allJournals.size();
            tvEntryCount.setText(total == 1 ? "1 entry" : total + " entries");
        }

        if (filtered.isEmpty()) {
            if (rvJournals != null) rvJournals.setVisibility(View.GONE);
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
        } else {
            if (rvJournals != null) rvJournals.setVisibility(View.VISIBLE);
            if (emptyState != null) emptyState.setVisibility(View.GONE);

            JournalAdapter adapter = new JournalAdapter(filtered);
            adapter.setOnJournalClickListener(new JournalAdapter.OnJournalClickListener() {
                @Override
                public void onJournalClick(JournalEntity journal) {
                    SoundManager.playTap(requireContext());
                    openJournalEntry(journal);
                }

                @Override
                public void onJournalLongClick(JournalEntity journal) {
                    SoundManager.playThrow(requireContext());
                    showDeleteJournalDialog(journal);
                }
            });

            if (rvJournals != null) {
                rvJournals.setAdapter(adapter);
            }
        }
    }

    private void showDeleteJournalDialog(JournalEntity journal) {
        if (!isAdded()) return;

        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_confirm, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(v)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setDimAmount(0.7f);
        }

        TextView tvTitle = v.findViewById(R.id.tvDeleteTitle);
        TextView tvMessage = v.findViewById(R.id.tvDeleteMessage);
        View btnCancel = v.findViewById(R.id.btnCancelDelete);
        View btnConfirm = v.findViewById(R.id.btnConfirmDelete);

        if (tvTitle != null) tvTitle.setText("Delete entry?");
        if (tvMessage != null) tvMessage.setText("“" + journal.title + "” will be permanently removed.");

        if (btnCancel != null) {
            btnCancel.setOnClickListener(view -> {
                SoundManager.playTap(requireContext());
                dialog.dismiss();
            });
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(view -> {
                SoundManager.playThrow(requireContext());
                if (dao != null) {
                    try {
                        dao.deleteJournal(journal);
                    } catch (Exception ignored) {}
                }
                loadJournals();
                Toast.makeText(requireContext(), "Entry deleted", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).setBottomNavVisible(false);
        }
        if (isAdded()) loadJournals();
    }
}
