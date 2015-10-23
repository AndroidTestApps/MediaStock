package com.example.mediastock.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by dinu on 21/10/15.
 */
public class Database extends SQLiteOpenHelper {
    private final static String DB_NAME = "db_mediastock";
    private final static String TABLE_FAVORITES = "favorites";

    public Database(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);

    }


    //TODO


    public void getData() {

    }


    public void deleteData() {

    }


    public void insertData() {

    }

    public void updateData() {

    }
}


