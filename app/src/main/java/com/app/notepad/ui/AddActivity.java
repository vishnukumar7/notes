package com.app.notepad.ui;

import static com.app.notepad.utils.AppController.getCurrentDate;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.app.notepad.utils.AppController;
import com.app.notepad.R;
import com.app.notepad.database.DatabaseClient;
import com.app.notepad.database.NoteData;
import com.app.notepad.databinding.ActivityAddBinding;


public class AddActivity extends BaseActivity implements TextWatcher {

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
            Log.d("TAG", "onCreate: "+noteData.getKeyValues());
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
        currentDate=getCurrentDate();
        binding.currentDateTime.setText(currentDate);
        timeHandler.postDelayed(runnable,1000);
        binding.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!from.equals("add")) {
                    if(noteData.getServerSync().equals(AppController.SERVER_NOT_SYNC)){
                        DatabaseClient.getInstance(AddActivity.this).getAppDatabase().noteDataDao().delete(noteData);
                    }else{
                        noteData.setServerSync(AppController.SERVER_NOT_SYNC);
                        noteData.setStatus("delete");
                        DatabaseClient.getInstance(AddActivity.this).getAppDatabase().noteDataDao().update(noteData);
                    }
                    Toast.makeText(AddActivity.this, "Notes delete successfully", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }


    @Override
    public void onBackPressed() {
        saveNotes();
    }

    public void saveNotes(){
        if(from.equals("edit")){
            if(binding.text.getText().toString().isEmpty()){
                noteData.setNotes(binding.text.getText().toString());
                noteData.setCreatedTime(getCurrentDate());
                noteData.setServerSync(AppController.SERVER_NOT_SYNC);
                noteData.setStatus("delete");
                DatabaseClient.getInstance(this).getAppDatabase().noteDataDao().insertOrUpdate(noteData);
            } else if(!noteData.getNotes().equals(binding.text.getText().toString())){
                noteData.setNotes(binding.text.getText().toString());
                noteData.setCreatedTime(getCurrentDate());
                noteData.setServerSync(AppController.SERVER_NOT_SYNC);
                noteData.setStatus("live");
                DatabaseClient.getInstance(this).getAppDatabase().noteDataDao().insertOrUpdate(noteData);
                Toast.makeText(AddActivity.this, "Updated", Toast.LENGTH_SHORT).show();
            }
        }else if(!binding.text.getText().toString().isEmpty()){
            noteData.setId("notes-"+System.currentTimeMillis()/100);
            noteData.setNotes(binding.text.getText().toString());
            noteData.setCreatedTime(getCurrentDate());
            noteData.setServerSync(AppController.SERVER_NOT_SYNC);
            noteData.setTextChanged("newdata");
            noteData.setStatus("live");
            DatabaseClient.getInstance(this).getAppDatabase().noteDataDao().insertOrUpdate(noteData);
            Toast.makeText(AddActivity.this, "Saved", Toast.LENGTH_SHORT).show();
        }
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
