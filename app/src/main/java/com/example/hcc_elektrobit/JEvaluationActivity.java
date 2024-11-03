package com.example.hcc_elektrobit;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;


public class JEvaluationActivity extends AppCompatActivity {

    private EvaluationViewModel viewModel;
    Button eval_button;
    TextView result_text;
    Spinner modelSpinner;
    LinearLayout mispred;
    Button cancel_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(EvaluationViewModel.class);

        setContentView(R.layout.activity_eval);

        eval_button = findViewById(R.id.evaluateButton);
        result_text = findViewById(R.id.resultTextView);
        modelSpinner = findViewById(R.id.model_spinner);
        mispred = findViewById(R.id.mispredictions);
        cancel_button = findViewById(R.id.cancel_button);


        viewModel.setupSpinner(modelSpinner);

        viewModel.getEvaluationResult().observe(this, result -> {
            result_text.setText("Accuracy: " + result);
        });

        viewModel.getMispredictions().observe(this, result ->{
            Log.e("here", "here!");
            mispred.removeAllViews();
            TextView titleView = new TextView(this);
            titleView.setText("expected: output");
            titleView.setPadding(8, 8, 8, 8);
            mispred.addView(titleView);
            for (Map.Entry<String, List<String>> entry : result.entrySet()) {
                TextView tv = new TextView(this);
                tv.setText(entry.getKey() + ": " + entry.getValue());
                tv.setPadding(8, 8, 8, 8);
                mispred.addView(tv);
            }
        });


        eval_button.setOnClickListener(v1->{
            viewModel.start_test(modelSpinner.getSelectedItem().toString());
        });

        cancel_button.setOnClickListener(v1 ->{
            viewModel.cancelEval();
        });


    }


}

