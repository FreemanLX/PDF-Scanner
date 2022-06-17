package com.example.pdfscanner;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PDF_Adapter extends RecyclerView.Adapter<PDF_Adapter.PDFViewHolder> {
    private final ArrayList<PDF_Scanned> objects;
    private int position;
    Context context;

    public PDF_Adapter(@NonNull ArrayList<PDF_Scanned> objects, @NonNull Context context) {
        this.objects = objects;
        this.context = context;
    }


    public class PDFViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private final TextView name;
        private final TextView size;

        PDFViewHolder(final View view) {
            super(view);
            name = view.findViewById(R.id.textlist);
            size = view.findViewById(R.id.textlist_size);
            position = getAdapterPosition();
            view.setOnCreateContextMenuListener(this);
        }


        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

        }
    }

    @Override
    public void onBindViewHolder(@NonNull PDF_Adapter.PDFViewHolder holder, int position){
        holder.name.setText(objects.get(position).getName());
        holder.size.setText(objects.get(position).getSize());
    }

    @NonNull
    @Override
    public PDF_Adapter.PDFViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View convertView = layoutInflater.inflate(R.layout.list_row, parent, false);
        return new PDF_Adapter.PDFViewHolder(convertView);
    }
    @Override
    public int getItemCount(){
        return objects.size();
    }

    @Override
    public long getItemId(int position){
       return super.getItemId(position);
    }

    public Context getContext(){
        return context;
    }

    public PDF_Scanned getItem(int position){
        return objects.get(position);
    }
}