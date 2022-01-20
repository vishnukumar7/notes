package com.app.notepad.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteDataDao {

    @Query("select * from notes_data where status='live'")
    List<NoteData> getAll();

    @Query("select * from notes_data where id=:id")
    List<NoteData> getData(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NoteData noteData);

    @Query("select * from notes_data where notes like :data AND status='live'")
    List<NoteData> getSearchData(String data);

    @Query("select * from notes_data where serverSync='not sync' AND textChanged=:data AND status='live'")
    List<NoteData> getServerNotSync(String data);

    @Query("select * from notes_data where serverSync='not sync' AND status='delete'")
    List<NoteData> getDeleteServerNotSync();

    @Update
    void update(NoteData noteData);

    @Delete()
    void delete(NoteData noteData);

    @Ignore
    default void insertOrUpdate(NoteData noteData){
        if(getData(noteData.getId()).size()>0)
            update(noteData);
        else
            insert(noteData);
    }


}
