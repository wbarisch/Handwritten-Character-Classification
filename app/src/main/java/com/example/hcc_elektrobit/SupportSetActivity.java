package com.example.hcc_elektrobit;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;

public class SupportSetActivity extends AppCompatActivity {

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
            Intent intent = new Intent(SupportSetActivity.this, JMainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supportsetmain);

        GridView gridView = findViewById(R.id.grid_view);
        Button clearHistory = findViewById(R.id.clear_set);
        Button createImgButton = findViewById(R.id.draw_item);
        SupportSet.getInstance().updateSet(this);
        SupportSetAdapter adapter = new SupportSetAdapter(this, R.layout.support_set_item, SupportSet.getInstance().getItems());
        gridView.setAdapter(adapter);

        createImgButton.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SupportSetActivity.this, SupportSetDrawingActivity.class);
                startActivity(intent);
            }
        });

        clearHistory.setOnClickListener(new  View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the clearHistory method from the History class
                SupportSet.getInstance().clearSet(SupportSetActivity.this);
                SupportSet.getInstance().updateSet(SupportSetActivity.this);
                SupportSetAdapter adapter = new SupportSetAdapter( SupportSetActivity.this, R.layout.support_set_item, SupportSet.getInstance().getItems());
                gridView.setAdapter(adapter);
            }
        });

    }
}
