package com.example.hcc_elektrobit;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import java.util.ArrayList;

public class ReviewAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Bitmap> bitmaps;
    private ArrayList<Bitmap> selectedBitmaps;

    public ReviewAdapter(Context context, ArrayList<Bitmap> bitmaps, ArrayList<Bitmap> selectedBitmaps) {
        this.context = context;
        this.bitmaps = bitmaps;
        this.selectedBitmaps = selectedBitmaps;
    }

    @Override
    public int getCount() {
        return bitmaps.size();
    }

    @Override
    public Object getItem(int position) {
        return bitmaps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.image_view);
            holder.checkBox = convertView.findViewById(R.id.check_box);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Bitmap bitmap = bitmaps.get(position);
        holder.imageView.setImageBitmap(bitmap);
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedBitmaps.contains(bitmap));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedBitmaps.contains(bitmap)) {
                    selectedBitmaps.add(bitmap);
                }
            } else {
                selectedBitmaps.remove(bitmap);
            }
        });
        return convertView;
    }

    static class ViewHolder {
        ImageView imageView;
        CheckBox checkBox;
    }

    public void selectAll() {
        selectedBitmaps.clear();
        selectedBitmaps.addAll(bitmaps);
        notifyDataSetChanged();
    }

    public void deselectAll() {
        selectedBitmaps.clear();
        notifyDataSetChanged();
    }

}
