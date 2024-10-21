package com.eraysirdas.artbookproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eraysirdas.artbookproject.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class AdapterArt extends RecyclerView.Adapter<AdapterArt.ArtHolder> {
    ArrayList<ModelArt> arrayList;

    public AdapterArt(ArrayList<ModelArt> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ArtHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.binding.recyclerViewTv.setText(arrayList.get(position).name);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), ArtActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("artId",arrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });

        holder.binding.recyclerViewDeleteIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Silme işlemi
                int artId = arrayList.get(position).id;

                // Veritabanı işlemleri - SQLiteDatabase üzerinden doğrudan
                SQLiteDatabase database = holder.itemView.getContext().openOrCreateDatabase("Arts", Context.MODE_PRIVATE, null);
                database.execSQL("DELETE FROM arts WHERE id = " + artId); // Veritabanından sil

                // RecyclerView'deki listeden sil
                arrayList.remove(position); // Listedeki öğeyi kaldır
                notifyItemRemoved(position); // RecyclerView'i güncelle
                notifyItemRangeChanged(position, arrayList.size()); // Değişiklikleri bildir

                // Veritabanını kapat
                database.close();
            }
        });

        holder.binding.recyclerViewUpdateIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), ArtActivity.class);
                intent.putExtra("info","update");
                intent.putExtra("artId",arrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ArtHolder extends RecyclerView.ViewHolder{
        private RecyclerRowBinding binding;

        public ArtHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }
}
