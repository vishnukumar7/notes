package com.app.notepad.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.notepad.AppController;
import com.app.notepad.R;
import com.app.notepad.database.AppDatabase;
import com.app.notepad.database.DatabaseClient;
import com.app.notepad.database.NoteData;
import com.app.notepad.databinding.ActivityMainBinding;
import com.app.notepad.databinding.NotesListIitemBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    public static List<NoteData> noteDataList = new ArrayList<>();
    ActivityMainBinding binding;
    NoteAdapter adapter;
    AppDatabase database;
    private int type = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        database = DatabaseClient.getInstance(MainActivity.this).getAppDatabase();
        adapter = new NoteAdapter(noteDataList);
        binding.recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayout.VERTICAL));
        binding.recyclerView.setHasFixedSize(false);
        binding.recyclerView.setAdapter(adapter);
        setSupportActionBar(binding.actionBar);

        adapter.notifyDataSetChanged();
        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                intent.putExtra("from", "add");
                startActivity(intent);
            }
        });

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                noteDataList.clear();
                if(AppController.SORT_TYPE==AppController.SORT_TEXT){
                    noteDataList.addAll(database.noteDataDao().getSearchDataOrderText("%"+query+"%"));
                }
                else if(AppController.SORT_TYPE==AppController.SORT_TIME){
                    noteDataList.addAll(database.noteDataDao().getSearchDataOrderTime("%"+query+"%"));
                }else{
                    noteDataList.addAll(database.noteDataDao().getSearchData("%"+query+"%"));
                }
                adapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                noteDataList.clear();
                if(AppController.SORT_TYPE==AppController.SORT_TEXT){
                    noteDataList.addAll(database.noteDataDao().getSearchDataOrderText("%"+newText+"%"));
                }
                else if(AppController.SORT_TYPE==AppController.SORT_TIME){
                    noteDataList.addAll(database.noteDataDao().getSearchDataOrderTime("%"+newText+"%"));
                }else{
                    noteDataList.addAll(database.noteDataDao().getSearchData("%"+newText+"%"));
                }
                adapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.list_item:
                type = 1;
                binding.recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                adapter.notifyDataSetChanged();
                break;
            case R.id.grid_item:
                type = 0;
                binding.recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayout.VERTICAL));
                adapter.notifyDataSetChanged();
                break;
            case R.id.text:
                AppController.SORT_TYPE=AppController.SORT_TEXT;
                noteDataList.clear();
                noteDataList.addAll(database.noteDataDao().getAllOrderText());
                adapter.notifyDataSetChanged();
                break;
            case R.id.time:
                AppController.SORT_TYPE=AppController.SORT_TIME;
                noteDataList.clear();
                noteDataList.addAll(database.noteDataDao().getAllOrderTime());
                adapter.notifyDataSetChanged();
                break;
        }
        return true;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        noteDataList.clear();
        noteDataList.addAll(AppController.SORT_TYPE == 0 ? database.noteDataDao().getAll() : AppController.SORT_TYPE == AppController.SORT_TEXT ? database.noteDataDao().getAllOrderText() : database.noteDataDao().getAllOrderTime());
        adapter.notifyDataSetChanged();
    }

    /*   public String getDate(){
        SimpleDateFormat format=new SimpleDateFormat("EEE, MMM d, HH:mm:ss", Locale.getDefault());
        return format.format(new Date());
    }*/

    class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
        private final List<NoteData> noteDataList;

        public NoteAdapter(List<NoteData> noteDataLis) {
            this.noteDataList = noteDataLis;
        }

        @NonNull
        @Override
        public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new NoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_list_iitem, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull NoteViewHolder holder, @SuppressLint("RecyclerView") int position) {
            holder.itemBinding.data.setMaxLines(type == 0 ? 10 : 3);
            holder.itemBinding.data.setText(noteDataList.get(position).getNotes());
            holder.itemBinding.date.setText(noteDataList.get(position).getCreatedTime());
            holder.itemBinding.mainLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, AddActivity.class);
                    intent.putExtra("from", "edit");
                    intent.putExtra("data", noteDataList.get(position));
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return noteDataList.size();
        }

        class NoteViewHolder extends RecyclerView.ViewHolder {

            NotesListIitemBinding itemBinding;

            public NoteViewHolder(@NonNull View itemView) {
                super(itemView);
                itemBinding = DataBindingUtil.bind(itemView);
            }
        }
    }


}