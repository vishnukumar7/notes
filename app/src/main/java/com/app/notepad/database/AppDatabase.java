package com.app.notepad.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {NoteData.class},version =2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract NoteDataDao noteDataDao();
}
