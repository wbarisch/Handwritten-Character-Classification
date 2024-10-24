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
public class SupportSetAdapter extends ArrayAdapter<SupportSetItem> {

    List<SupportSetItem> sup_list;
    int custom_layout_id;
    public SupportSetAdapter(@NonNull Context context, int resource, @NonNull List<SupportSetItem> objects){
        super(context, resource, objects);
        sup_list = objects;
        custom_layout_id = resource;

    }

    @Override
    public int getCount(){
        return sup_list.size();
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

        SupportSetItem item = sup_list.get(position);

        imageView.setImageBitmap(item.getBitmap());
        String predictionStr = Integer.toString(item.getlabelId());
        textView.setText(predictionStr);

        return v;

    }
}
