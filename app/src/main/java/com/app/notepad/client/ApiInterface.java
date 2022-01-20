package com.app.notepad.client;

import com.app.notepad.model.AddNoteRequest;
import com.app.notepad.model.AddNoteResponse;
import com.app.notepad.model.DataNoteResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.POST;

public interface ApiInterface {

    @POST("dev/notes/add")
    Call<AddNoteResponse> postAddNote(List<AddNoteRequest> requestList);

    @POST("dev/notes/get")
    Call<DataNoteResponse> getNote(String mobileNo);

    @POST("dev/notes/update")
    Call<AddNoteResponse> postUpdateNote(AddNoteRequest request);

    @POST("dev/notes/delete")
    Call<AddNoteResponse> deleteNote(String key);

}
