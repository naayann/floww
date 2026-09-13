package com.naayann.floow.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface TodoDao {
    @Insert
    long insertTodo(TodoEntity todo);

    @Query("SELECT * FROM todos ORDER BY createdAt ASC")
    List<TodoEntity> getAllTodos();

    @Query("SELECT COUNT(*) FROM todos")
    int getTodoCount();

    @Delete
    void deleteTodo(TodoEntity todo);

    @Insert
    void insertCompletion(CompletionEntity completion);

    @Query("SELECT COUNT(*) FROM completions WHERE todoId = :todoId")
    int getCompletionCount(long todoId);

    @Query("SELECT * FROM todos WHERE id NOT IN (SELECT todoId FROM completions WHERE date = :today)")
    List<TodoEntity> getPendingToday(String today);

    @Query("SELECT COUNT(*) FROM completions WHERE date = :today AND todoId = :todoId")
    int isCompletedToday(long todoId, String today);
}