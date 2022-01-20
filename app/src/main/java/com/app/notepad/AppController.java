package com.app.notepad;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AppController extends Application {


    public static final String TAG = AppController.class
            .getSimpleName();

    private RequestQueue mRequestQueue;

    private static AppController mInstance;

    static SharedPreferences pref;
    static SharedPreferences.Editor editor;
    static String SHARED_NAME="user_pref";

    static String URL="https://3xs79s0rob.execute-api.us-west-1.amazonaws.com/";
    public static String GET_NOTE=URL+"dev/notes/get";
    public static String ADD_NOTE=URL+"dev/notes/add";
    public static String UPDATE_NOTE=URL+"dev/notes/update";
    public static String DELETE_NOTE=URL+"dev/notes/delete";

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }


    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }



    public static String getCurrentDate(){
        SimpleDateFormat format=new SimpleDateFormat("EEE, MMM d, HH:mm:ss", Locale.getDefault());
        return format.format(Calendar.getInstance().getTime());
    }

    public void setValues(String key, String value){
        pref=getSharedPreferences(SHARED_NAME,Context.MODE_PRIVATE);
        editor=pref.edit();
        editor.putString(key, value);
        editor.apply();
        editor.commit();
    }

    public void setValues(String key, Boolean value){
        pref=getSharedPreferences(SHARED_NAME,Context.MODE_PRIVATE);
        editor=pref.edit();
        editor.putBoolean(key, value);
        editor.apply();
        editor.commit();
    }

    public String getString(String key){
        pref=getSharedPreferences(SHARED_NAME,Context.MODE_PRIVATE);
        return pref.getString(key,"");
    }

    public Boolean userLogged(){
        pref=getSharedPreferences(SHARED_NAME,Context.MODE_PRIVATE);
        return pref.getBoolean("logged",false);
    }
}

/*
  public static String URL="https://3xs79s0rob.execute-api.us-west-1.amazonaws.com/";
@POST("dev/notes/add")
    Call<AddNoteResponse> postAddNote(List<AddNoteRequest> requestList);



    @POST("dev/notes/update")
    Call<AddNoteResponse> postUpdateNote(AddNoteRequest request);

    @POST("dev/notes/delete")
    Call<AddNoteResponse> deleteNote(String key);*/
