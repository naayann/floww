package com.naayann.floow.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "todos")
public class TodoEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String title;
    public String emoji;
    public String bgColor;   // hex string
    public String category;
    public long createdAt;

    public TodoEntity(String title, String emoji, String bgColor, String category) {
        this.title = title;
        this.emoji = emoji;
        this.bgColor = bgColor;
        this.category = (category != null && !category.isEmpty()) ? category : "my activity";
        this.createdAt = System.currentTimeMillis();
    }

    @Ignore
    public TodoEntity(String title, String emoji, String bgColor) {
        this(title, emoji, bgColor, "my activity");
    }
}
