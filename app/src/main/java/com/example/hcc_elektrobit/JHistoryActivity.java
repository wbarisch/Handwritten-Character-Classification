package com.example.hcc_elektrobit;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;

public class JHistoryActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.menuButton);
        item.setTitle("Back");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.menuButton) {
            Intent intent = new Intent(JHistoryActivity.this, JMainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jhistory);

        GridView gridView = findViewById(R.id.grid_view);
        Button clearHistory = findViewById(R.id.clear_history);
        History.getInstance().updateHistory(this);
        HistoryAdapter adapter = new HistoryAdapter(this, R.layout.history_item, History.getInstance().getItems());
        gridView.setAdapter(adapter);

        clearHistory.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the clearHistory method from the History class
                History.getInstance().clearHistory(JHistoryActivity.this);
                History.getInstance().updateHistory(JHistoryActivity.this);
                HistoryAdapter adapter = new HistoryAdapter(JHistoryActivity.this, R.layout.history_item, History.getInstance().getItems());
                gridView.setAdapter(adapter);
            }
        });

    }
}
