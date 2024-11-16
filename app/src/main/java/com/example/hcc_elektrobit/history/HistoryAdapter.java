package com.example.hcc_elektrobit.history;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.hcc_elektrobit.R;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

// Adapter class for the history grid view
public class HistoryAdapter extends ArrayAdapter<HistoryItem> {

    List<HistoryItem> hist_list;
    int custom_layout_id;
    Context ctx;
    public HistoryAdapter(@NonNull Context context, int resource, @NonNull List<HistoryItem> objects){
        super(context, resource, objects);
        ctx = context;
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
        TextView model = v.findViewById(R.id.model);

        HistoryItem item = hist_list.get(position);

        imageView.setImageBitmap(item.getBitmap());
        String predictionStr;
        if(item instanceof SMSHistoryItem){
            predictionStr = ((SMSHistoryItem)item).getPred();
            textView.setText(predictionStr);
            model.setText(item.getModel());
        } else if (item instanceof CNNHistoryItem) {
            predictionStr = ((CNNHistoryItem)item).getPred();
            textView.setText(predictionStr);
            model.setText(item.getModel());
        }


        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View dialogView = LayoutInflater.from(ctx).inflate(R.layout.tensor_output, null);

                // Build the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setView(dialogView);

                // Create and show the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();


                TextView tensor = dialogView.findViewById(R.id.tensor);
                if (Objects.equals(item.getModel(), "SMS")) {
                    tensor.setText(((SMSHistoryItem)item).getOutputCollection().toString());
                } else if (Objects.equals(item.getModel(), "CNN")) {
                    tensor.setText(Arrays.deepToString(((CNNHistoryItem) item).getOutputCollection()));
                }

                // Set up close button functionality
                Button closeButton = dialogView.findViewById(R.id.closeButton);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss(); // Close the dialog
                    }
                });
            }
        });

        return v;

    }
}
