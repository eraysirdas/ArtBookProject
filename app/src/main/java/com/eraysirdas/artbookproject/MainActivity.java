package com.eraysirdas.artbookproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.ColorSpace;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.eraysirdas.artbookproject.databinding.ActivityMainBinding;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    ArrayList<ModelArt> arrayList;
    AdapterArt adapterArt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        arrayList = new ArrayList<>();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapterArt = new AdapterArt(arrayList);
        binding.recyclerView.setAdapter(adapterArt);

        getData();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getData(){

        try {
            SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);

            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM arts",null);
            int nameIx = cursor.getColumnIndex("artname");
            int idIx = cursor.getColumnIndex("id");

            while(cursor.moveToNext()){
                String name = cursor.getString(nameIx);
                int id = cursor.getInt(idIx);

                ModelArt modelArt = new ModelArt(name,id);
                arrayList.add(modelArt);
            }

            adapterArt.notifyDataSetChanged();
            cursor.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.artmenu,menu);
         return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.addart)
        {
            Intent intent = new Intent(this,ArtActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}