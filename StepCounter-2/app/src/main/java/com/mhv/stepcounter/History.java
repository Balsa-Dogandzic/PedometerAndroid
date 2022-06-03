package com.mhv.stepcounter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

public class History extends AppCompatActivity {

    RecyclerView recycle;
    Database db;
    ArrayList<String> id;
    ArrayList<String> steps;
    ArrayList<String> date;
    CustomAdaptor custom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        recycle = findViewById(R.id.recycle);
        db = new Database(History.this);
        id = new ArrayList<>();
        steps = new ArrayList<>();
        date = new ArrayList<>();
        displayData();

        custom = new CustomAdaptor(this,id,steps,date);
        recycle.setAdapter(custom);
        recycle.setLayoutManager(new LinearLayoutManager(History.this));
    }


    void displayData() {
        Cursor cursor = db.readAllData();
        if (cursor.getCount() == 0) {
            Toast.makeText(History.this, "No data found", Toast.LENGTH_SHORT).show();
        }else {
            while(cursor.moveToNext()) {
                id.add(cursor.getString(0));
                steps.add(cursor.getString(1));
                date.add(cursor.getString(2));
            }
        }
    }
}