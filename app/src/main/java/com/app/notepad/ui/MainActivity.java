package com.app.notepad.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.notepad.R;
import com.app.notepad.database.AppDatabase;
import com.app.notepad.database.DatabaseClient;
import com.app.notepad.database.NoteData;
import com.app.notepad.databinding.ActivityMainBinding;
import com.app.notepad.databinding.NotesListIitemBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static List<NoteData> noteDataList = new ArrayList<>();
    ActivityMainBinding binding;
    NoteAdapter adapter;
    AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        database=DatabaseClient.getInstance(MainActivity.this).getAppDatabase();
        adapter = new NoteAdapter(noteDataList);
        binding.recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayout.VERTICAL));
        binding.recyclerView.setHasFixedSize(false);
        binding.recyclerView.setAdapter(adapter);
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
                noteDataList.addAll(database.noteDataDao().getSearchData("%"+query+"%"));
                adapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                noteDataList.clear();
                noteDataList.addAll(database.noteDataDao().getSearchData("%"+newText+"%"));
                adapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        noteDataList.clear();
        noteDataList.addAll(database.noteDataDao().getAll());
        adapter.notifyDataSetChanged();
    }

    /*   public String getDate(){
        SimpleDateFormat format=new SimpleDateFormat("EEE, MMM d, HH:mm:ss", Locale.getDefault());
        return format.format(new Date());
    }*/

    class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
        private List<NoteData> noteDataList;

        public NoteAdapter(List<NoteData> noteDataLis) {
            this.noteDataList = noteDataLis;
        }

        @NonNull
        @Override
        public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new NoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_list_iitem, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
            holder.itemBinding.data.setText(noteDataList.get(position).getNotes());
            holder.itemBinding.date.setText(noteDataList.get(position).getCreatedTime());
            holder.itemBinding.mainLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, AddActivity.class);
                    intent.putExtra("from", "edit");
                    intent.putExtra("data",noteDataList.get(position));
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