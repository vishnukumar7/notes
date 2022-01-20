package com.app.notepad.ui;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.app.notepad.R;
import com.app.notepad.database.DatabaseClient;
import com.app.notepad.database.NoteData;
import com.app.notepad.databinding.ActivityAddBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class AddActivity extends AppCompatActivity implements TextWatcher {

    ActivityAddBinding binding;
    NoteData noteData=new NoteData();
    String currentDate;
    String from="";
    Handler timeHandler=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            binding.currentDateTime.setText(currentDate);
            timeHandler.postDelayed(this,1000);
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this, R.layout.activity_add);
        from=getIntent().getStringExtra("from");
        binding.text.addTextChangedListener(this);
        if (from.equals("edit")){
            noteData= (NoteData) getIntent().getExtras().get("data");
            binding.text.setText(noteData.getNotes());
        }

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNotes();
            }
        });
        binding.check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNotes();
            }
        });
        currentDate=getDate();
        binding.currentDateTime.setText(currentDate);
        timeHandler.postDelayed(runnable,1000);
        binding.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!from.equals("add")) {
                    noteData.setServerSync("not sync");
                    noteData.setStatus("delete");
                    DatabaseClient.getInstance(AddActivity.this).getAppDatabase().noteDataDao().update(noteData);
                }
                finish();
            }
        });
    }


    public String getDate(){
        SimpleDateFormat format=new SimpleDateFormat("EEE, MMM d, HH:mm:ss", Locale.getDefault());
        return format.format(Calendar.getInstance().getTime());
    }


    @Override
    public void onBackPressed() {
        saveNotes();
    }

    public void saveNotes(){
        if(from.equals("edit")){
            if(!noteData.getNotes().equals(binding.text.getText().toString())){
                noteData.setNotes(binding.text.getText().toString());
                noteData.setCreatedTime(getDate());
                noteData.setServerSync("not sync");
                noteData.setStatus("live");
            }
        }else{
            noteData.setId("notes-"+System.currentTimeMillis()/100);
            noteData.setNotes(binding.text.getText().toString());
            noteData.setCreatedTime(getDate());
            noteData.setServerSync("not sync");
            noteData.setStatus("live");
        }
        DatabaseClient.getInstance(this).getAppDatabase().noteDataDao().insertOrUpdate(noteData);
        finish();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      //  if(binding.text.getText().toString().length()>0){
           binding.characters.setText("Characters : "+binding.text.getText().toString().length());
       // }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}