package com.example.mediastock.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public final static String TABLE_FAVORITES = "favorites";
    public final static String IMAGE = "image";
    public final static String DESCRIPTION = "description";
    public final static String AUTHOR = "author";
    public final static String IMG_ID = "imageid";
    private final static String DB_NAME = "db_mediastock";
    private static DBHelper instance;

    private DBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null)
            instance = new DBHelper(context.getApplicationContext());

        return instance;
    }

    /**
     * Create table
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String db_create = "create table " + TABLE_FAVORITES +
                "(_id integer primary key, " + IMAGE + " blob, " + IMG_ID + " integer, " + DESCRIPTION + " text, " + AUTHOR + " text)";

        db.execSQL(db_create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }
}


