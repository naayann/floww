package com.naayann.floow.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.naayann.floow.R;
import com.naayann.floow.data.AppDatabase;
import com.naayann.floow.data.TodoDao;
import com.naayann.floow.data.TodoEntity;
import com.naayann.floow.utils.SoundManager;
import java.util.ArrayList;
import java.util.List;

public class ProgressActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            SoundManager.playTap(this);
            finish();
        });

        RecyclerView rv = findViewById(R.id.rvProgress);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        TodoDao dao = AppDatabase.getInstance(this).todoDao();
        List<TodoEntity> all = dao.getAllTodos();
        List<MultiplierAdapter.Item> items = new ArrayList<>();
        for (TodoEntity t : all) {
            int count = dao.getCompletionCount(t.id);
            items.add(new MultiplierAdapter.Item(t, count));
        }

        MultiplierAdapter adapter = new MultiplierAdapter(items);
        adapter.setOnItemClickListener(item -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Delete this goal?")
                    .setMessage("“" + item.todo.title + "” will be permanently removed.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        SoundManager.playThrow(this);
                        dao.deleteTodo(item.todo);
                        items.remove(item);
                        adapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("Keep it", null)
                    .setBackgroundInsetStart(24)
                    .setBackgroundInsetEnd(24)
                    .show();
        });
        rv.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0) {

            @Override
            public boolean isLongPressDragEnabled() {
                return false; // removes the long-press delay
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAbsoluteAdapterPosition();
                int toPosition = target.getAbsoluteAdapterPosition();
                adapter.onItemMove(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }
        });

        itemTouchHelper.attachToRecyclerView(rv);
        adapter.setItemTouchHelper(itemTouchHelper); // pass helper to adapter
    }
}