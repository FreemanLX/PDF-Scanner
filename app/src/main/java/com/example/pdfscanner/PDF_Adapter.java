package com.example.pdfscanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class PDF_Adapter extends ArrayAdapter<PDF_Scanned> {
    private final Context context;
    private final int resource;

    public PDF_Adapter(@NonNull Context context, int resource, @NonNull ArrayList<PDF_Scanned> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        convertView = layoutInflater.inflate(resource, parent, false);
        TextView name = convertView.findViewById(R.id.textlist);
        TextView size = convertView.findViewById(R.id.textlist_size);
        name.setText(getItem(position).getName());
        size.setText(getItem(position).getSize());
        return convertView;
    }
}