package com.mhv.stepcounter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;



public class Database extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "Pedometer.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "step-history";

    private static final String COLUMN_ID ="_id";
    private static final String COLUMN_STEPS = "_steps";
    private static final String COLUMN_DATE = "_date";

    public Database(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_STEPS + " INTEGER, "
                + COLUMN_DATE + " TEXT);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    void addSteps(int numOfSteps, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_STEPS, numOfSteps);
        cv.put(COLUMN_DATE, date);

        long result = db.insert(TABLE_NAME,null,cv);
        if (result == -1)
            Toast.makeText(context, "Failure",Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
    }
    Cursor readAllData() {
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }
}
