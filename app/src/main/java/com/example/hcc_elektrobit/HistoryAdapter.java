package com.example.hcc_elektrobit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

// Adapter class for the history grid view
public class HistoryAdapter extends ArrayAdapter<HistoryItem> {

    List<HistoryItem> hist_list;
    int custom_layout_id;
    public HistoryAdapter(@NonNull Context context, int resource, @NonNull List<HistoryItem> objects){
        super(context, resource, objects);
        hist_list = objects;
        custom_layout_id = resource;

    }

    @Override
    public int getCount(){
        return hist_list.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View v = convertView;
        if (v == null){
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(custom_layout_id, null);
        }

        ImageView imageView = v.findViewById(R.id.bmp);
        TextView textView = v.findViewById(R.id.pred);

        HistoryItem item = hist_list.get(position);

        imageView.setImageBitmap(item.getBitmap());
        String predictionStr = (item.getPred());
        textView.setText(predictionStr);

        return v;

    }
}
