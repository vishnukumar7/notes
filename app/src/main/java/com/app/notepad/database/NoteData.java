package com.app.notepad.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "notes_data")
public class NoteData implements Serializable {

    @PrimaryKey
    @NonNull
    @ColumnInfo
    private String id;

    @ColumnInfo
    private String createdTime;

    @ColumnInfo
    private String notes;

    @ColumnInfo
    private String textChanged;

    @ColumnInfo
    private String serverSync;

    @ColumnInfo
    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTextChanged() {
        return textChanged;
    }

    public void setTextChanged(String textChanged) {
        this.textChanged = textChanged;
    }

    public String getServerSync() {
        return serverSync;
    }

    public void setServerSync(String serverSync) {
        this.serverSync = serverSync;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
