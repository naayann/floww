package com.naayann.floow.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.naayann.floow.R;
import com.naayann.floow.data.TodoEntity;
import java.util.List;
import java.util.Locale;

public class MultiplierAdapter extends RecyclerView.Adapter<MultiplierAdapter.VH> {

    public static class Item {
        public TodoEntity todo;
        public int count;
        public Item(TodoEntity t, int c) { todo = t; count = c; }
    }

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    private final List<Item> items;
    private OnItemClickListener listener;

    public MultiplierAdapter(List<Item> items) {
        this.items = items;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_multiplier, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Item item = items.get(position);
        h.emoji.setText(item.todo.emoji);
        h.title.setText(item.todo.title);
        h.multiplier.setText(String.valueOf(item.count));

        try {
            int color = Color.parseColor(item.todo.bgColor);
            h.itemView.findViewById(R.id.cardBackground).setBackgroundColor(color);
        } catch (Exception e) {
            h.itemView.findViewById(R.id.cardBackground).setBackgroundColor(Color.WHITE);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView emoji, title, multiplier, subtitle;
        VH(View v) {
            super(v);
            emoji = v.findViewById(R.id.tvEmoji);
            title = v.findViewById(R.id.tvTitle);
            multiplier = v.findViewById(R.id.tvMultiplier);
            subtitle = v.findViewById(R.id.tvSubtitle);
        }
    }
}