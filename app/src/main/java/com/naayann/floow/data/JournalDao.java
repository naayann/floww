package com.naayann.floow.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface JournalDao {
    @Query("SELECT * FROM journals ORDER BY id DESC")
    List<JournalEntity> getAllJournals();

    @Insert
    long insertJournal(JournalEntity journal);

    @Delete
    void deleteJournal(JournalEntity journal);
}
