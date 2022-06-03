package com.mhv.stepcounter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    //Constructor
    public DBHelper(Context context) {
        super(context, "Step_history", null, 1);
    }

    //Creates table
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Step_history (id INTEGER primary key autoincrement, steps INTEGER, date TEXT)");
    }

    //Does drop function if the table already exists
    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int i1) {
        DB.execSQL("drop Table if exists Userdetails");
    }

    //Inserts data in the database
    public Boolean insertData(String steps, String date) {
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("steps", steps);
        contentValues.put("date", date);
        long result=DB.insert("Step_history", null, contentValues);
        return result != -1;
    }

    //Query on the database
    public Cursor getData ()
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        return DB.rawQuery("Select SUM(steps), date from Step_history group by date", null);

    }
}
