package com.naayann.floow.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "journals")
public class JournalEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String title;
    public String content;
    public String date;
    public String bgColor;

    public JournalEntity(String title, String content, String date, String bgColor) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.bgColor = bgColor;
    }
}
