package com.app.notepad.ui;

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
import com.android.volley.toolbox.JsonObjectRequest;
import com.app.notepad.AppController;
import com.app.notepad.NetworkReceiver;
import com.app.notepad.database.DatabaseClient;
import com.app.notepad.database.NoteData;
import com.app.notepad.database.NoteDataDao;
import com.app.notepad.model.AddNoteResponse;
import com.google.gson.Gson;

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
        addNote();
        updateNote();
        Toast.makeText(BaseActivity.this, "" + isConnected, Toast.LENGTH_SHORT).show();
    }

    public void registerNetworkReceiver() {
        registerReceiver(networkReceiver, intentFilter);
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


 /*   public void insertNote() {
        List<NoteData> noteDataList = noteDataDao.getServerNotSync("newdata");
        if (noteDataList.size() > 0) {
            showLoading();

            List<AddNoteRequest> noteRequestList = new ArrayList<>();
            for (NoteData noteData : noteDataList) {
                AddNoteRequest request = new AddNoteRequest();
                request.setNotes(noteData.getNotes());
                request.setCreatedtime(noteData.getCreatedTime());
                request.setMobileno(appController.getString("user_mobile"));
            }
            String URL = AppController.ADD_NOTE;
            StringRequest request = new StringRequest(Request.Method.POST, URL, new com.android.volley.Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "onResponse: " + response);
                    AddNoteResponse noteResponse = new Gson().fromJson(response, AddNoteResponse.class);

                }
            }, new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    dismissLoading();
                    Toast.makeText(BaseActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json");
                    return params;
                }
            };
            appController.addToRequestQueue(request);
        }
    }*/

    public void addNote() {
        List<NoteData> noteDataList = noteDataDao.getServerNotSync("newdata");
        Log.d(TAG, "addNote: "+noteDataList.size());
        if (noteDataList.size() > 0) {
            try {
                NoteData noteData = noteDataList.get(0);
                JSONObject request = new JSONObject();
                request.put("notes", noteData.getNotes());
                request.put("createdtime", noteData.getCreatedTime());
                request.put("mobileno", appController.getString("user_mobile"));

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
                            data.setServerSync("sync");
                            noteDataDao.update(data);
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
                        AddNoteResponse noteResponse=new Gson().fromJson(response.toString(),AddNoteResponse.class);
                        if(noteResponse.getStatusCode().equals("200")){
                            NoteData data=new NoteData();
                            data.setKeyValues(noteResponse.getContent().getKey());
                            data.setCreatedTime(noteResponse.getContent().getCreatedtime());
                            data.setNotes(noteResponse.getContent().getNotes());
                            data.setId(noteData.getId());
                            data.setStatus("live");
                            data.setTextChanged(noteData.getTextChanged());
                            data.setServerSync("sync");
                            noteDataDao.update(data);
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
