package com.example.hcc_elektrobit.evaluation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.hcc_elektrobit.R;
import com.example.hcc_elektrobit.main.MainActivity;

import java.util.List;
import java.util.Map;


public class JEvaluationActivity extends AppCompatActivity {

    private EvaluationViewModel viewModel;
    Button eval_button;
    TextView result_text;
    Spinner modelSpinner;
    LinearLayout mispred;
    Button cancel_button;

    Button reloadTestData_button;

    Button siameseTester_button;

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
        reloadTestData_button = findViewById(R.id.reload_test_data);
        Button siameseTester_button = findViewById(R.id.siamese_test_screen_button);

        siameseTester_button.setOnClickListener(v -> {
            Intent intent = new Intent(JEvaluationActivity.this, SiameseTesterActivity.class);
            startActivity(intent);
        });

        viewModel.setupSpinner(modelSpinner);
        viewModel.loadTestData();

        viewModel.getEvaluationResult().observe(this, result -> {
            result_text.setText("Results:\n " + result);
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

        reloadTestData_button.setOnClickListener(v1 ->{
            viewModel.reinistializeTestData();
        });




    }


}

