package com.naayann.floow.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "todos")
public class TodoEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String title;
    public String emoji;
    public String bgColor;   // hex string
    public long createdAt;

    public TodoEntity(String title, String emoji, String bgColor) {
        this.title = title;
        this.emoji = emoji;
        this.bgColor = bgColor;
        this.createdAt = System.currentTimeMillis();
    }
}