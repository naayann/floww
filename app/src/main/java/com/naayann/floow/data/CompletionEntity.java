package com.naayann.floow.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "completions")
public class CompletionEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long todoId;
    public String date; // yyyy-MM-dd

    public CompletionEntity(long todoId, String date) {
        this.todoId = todoId;
        this.date = date;
    }
}