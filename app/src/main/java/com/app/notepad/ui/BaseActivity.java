package com.app.notepad.ui;

import static com.app.notepad.AppController.USER_MOBILE;

import android.app.ProgressDialog;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.app.notepad.AppController;
import com.app.notepad.NetworkReceiver;
import com.app.notepad.database.DatabaseClient;
import com.app.notepad.database.NoteData;
import com.app.notepad.database.NoteDataDao;
import com.app.notepad.model.AddNoteResponse;
import com.app.notepad.model.UpdateResponse;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class BaseActivity extends AppCompatActivity implements NetworkReceiver.ConnectivityReceiverListener {
    NoteDataDao noteDataDao;
    NetworkReceiver networkReceiver;
    ProgressDialog progressDialog;
    AppController appController;
    private String TAG = "BaseActivity";
    private IntentFilter intentFilter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkReceiver = new NetworkReceiver();
        intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        noteDataDao = DatabaseClient.getInstance(this).getAppDatabase().noteDataDao();
        appController = AppController.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading.. Please wait");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        if(isConnected){
           serverSync();
        }
       // Toast.makeText(BaseActivity.this, "" + isConnected, Toast.LENGTH_SHORT).show();
    }

    public void registerNetworkReceiver() {
        registerReceiver(networkReceiver, intentFilter);
    }

    private void serverSync(){
        addNote();
        updateNote();
        deleteNote();
    }

    void showLoading() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    void dismissLoading() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerNetworkReceiver();
        networkReceiver.setConnectivityListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet checking disconnect
        networkReceiver.setConnectivityListener(null);
        unregisterNetworkReceiver();
    }

    public void unregisterNetworkReceiver() {
        try {
            if (networkReceiver != null) {
                unregisterReceiver(networkReceiver);
            }
        } catch (Exception e) {
            Log.e(TAG, "unregisterNetworkReceiver: " + e.getMessage());
        }
    }


    public void addNote() {
        List<NoteData> noteDataList = noteDataDao.getServerNotSync("newdata");
        Log.d(TAG, "addNote: "+noteDataList.size());
        if (noteDataList.size() > 0) {
            try {
                NoteData noteData = noteDataList.get(0);
                JSONObject request = new JSONObject();
                request.put("notes", noteData.getNotes());
                request.put("createdtime", noteData.getCreatedTime());
                request.put("mobileno", appController.getString(USER_MOBILE));

                String URL = AppController.ADD_NOTE;
                JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, URL, request, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: "+response);
                        AddNoteResponse noteResponse=new Gson().fromJson(response.toString(),AddNoteResponse.class);
                        if(noteResponse.getStatusCode().equals("200")){
                            NoteData data=new NoteData();
                            data.setKeyValues(noteResponse.getContent().getKey());
                            data.setCreatedTime(noteResponse.getContent().getCreatedtime());
                            data.setNotes(noteResponse.getContent().getNotes());
                            data.setId(noteData.getId());
                            data.setServerSync(AppController.SERVER_SYNC);
                            data.setTextChanged("olddata");
                            data.setStatus(noteData.getStatus());
                            noteDataDao.update(data);
                            serverSync();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                appController.addToRequestQueue(req);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public void updateNote() {
        List<NoteData> noteDataList = noteDataDao.getServerNotSync("olddata");
        Log.d(TAG, "updateNote: "+noteDataList.size());
        if (noteDataList.size() > 0) {
            try {
                NoteData noteData = noteDataList.get(0);
                JSONObject request = new JSONObject();
                request.put("key", noteData.getKeyValues());
                request.put("notes", noteData.getNotes());
                Log.d(TAG, "updateNote: "+request.toString());
                String URL = AppController.UPDATE_NOTE;
                JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, URL, request, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "updateNote onResponse: "+response);
                        UpdateResponse noteResponse=new Gson().fromJson(response.toString(),UpdateResponse.class);
                        if(noteResponse.getStatusCode().equals("200")){
                            NoteData data=new NoteData();
                            data.setKeyValues(noteData.getKeyValues());
                            data.setCreatedTime(noteData.getCreatedTime());
                            data.setNotes(noteData.getNotes());
                            data.setId(noteData.getId());
                            data.setStatus(noteData.getStatus());
                            data.setTextChanged(noteData.getTextChanged());
                            data.setServerSync(AppController.SERVER_SYNC);
                            noteDataDao.update(data);
                            serverSync();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                appController.addToRequestQueue(req);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public void deleteNote() {
        List<NoteData> noteDataList = noteDataDao.getDeleteServerNotSync();
        Log.d(TAG, "deleteNote: "+noteDataList.size());
        if (noteDataList.size() > 0) {
            try {
                NoteData noteData = noteDataList.get(0);
                JSONObject request = new JSONObject();
                request.put("key", noteData.getKeyValues());
                Log.d(TAG, "deleteNote: "+request.toString());
                String URL = AppController.DELETE_NOTE;
                JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, URL, request, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "deleteNote onResponse: "+response);
                        UpdateResponse noteResponse=new Gson().fromJson(response.toString(),UpdateResponse.class);
                        if(noteResponse.getStatusCode().equals("200")){
                            noteDataDao.delete(noteData.getKeyValues());
                            serverSync();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                appController.addToRequestQueue(req);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
