package com.naayann.floow.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
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
            SoundManager.playTap(this);
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Delete task?")
                    .setMessage("“" + item.todo.title + "” and all its completion records will be removed.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        SoundManager.playThrow(this);
                        dao.deleteCompletionsForTodo(item.todo.id);
                        dao.deleteTodo(item.todo);
                        int pos = items.indexOf(item);
                        if (pos != -1) {
                            items.remove(pos);
                            adapter.notifyItemRemoved(pos);
                            adapter.notifyItemRangeChanged(pos, items.size() - pos);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        rv.setAdapter(adapter);
    }
}
