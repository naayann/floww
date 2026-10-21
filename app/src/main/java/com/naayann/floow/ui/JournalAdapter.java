package com.naayann.floow.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.naayann.floow.R;
import com.naayann.floow.data.JournalEntity;
import java.util.List;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.VH> {

    public interface OnJournalClickListener {
        void onJournalClick(JournalEntity journal);
        void onJournalLongClick(JournalEntity journal);
    }

    private final List<JournalEntity> journals;
    private OnJournalClickListener listener;

    public JournalAdapter(List<JournalEntity> journals) {
        this.journals = journals;
    }

    public void setOnJournalClickListener(OnJournalClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_journal, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        JournalEntity entry = journals.get(position);
        holder.tvDate.setText(entry.date);
        holder.tvTitle.setText(entry.title);
        holder.tvContentPreview.setText(entry.content);

        try {
            int color = Color.parseColor(entry.bgColor);
            holder.cardBackground.setBackgroundColor(color);
        } catch (Exception e) {
            holder.cardBackground.setBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onJournalClick(entry);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onJournalLongClick(entry);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return journals.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDate, tvTitle, tvContentPreview;
        LinearLayout cardBackground;

        VH(View v) {
            super(v);
            tvDate = v.findViewById(R.id.tvJournalDate);
            tvTitle = v.findViewById(R.id.tvJournalTitle);
            tvContentPreview = v.findViewById(R.id.tvJournalContent);
            cardBackground = v.findViewById(R.id.cardBackground);
        }
    }
}
