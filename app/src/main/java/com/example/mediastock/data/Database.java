package com.example.mediastock.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import com.example.mediastock.util.Utilities;

public class Database extends SQLiteOpenHelper {
    public final static String TABLE_FAVORITES = "favorites";
    public final static String IMAGE = "image";
    public final static String DESCRIPTION = "description";
    public final static String AUTHOR = "author";
    public final static String IMG_ID = "imageid";
    private final static String DB_NAME = "db_mediastock";
    private final Context context;

    public Database(Context context) {
        super(context, DB_NAME, null, 1);
        this.context = context;
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

    public void deleteTableFavorites() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
    }

    public void createTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        final String db_create = "create table " + TABLE_FAVORITES +
                "(_id integer primary key, " + IMAGE + " blob, " + IMG_ID + " integer, " + DESCRIPTION + " text, " + AUTHOR + " text)";

        db.execSQL(db_create);
    }


    public Cursor getImages() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_FAVORITES, null);
        res.moveToFirst();
        db.close();

        return res;
    }


    public void deleteImage(int img_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        // delete the image with id img_id
        db.delete(TABLE_FAVORITES, IMG_ID + " = ? ", new String[]{String.valueOf(img_id)});
        db.close();
    }


    public long insertImage(Bitmap image, int imgId, String description, String author) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(IMAGE, Utilities.convertToByteArray(image));
        contentValues.put(IMG_ID, new Integer(imgId));
        contentValues.put(DESCRIPTION, description);
        contentValues.put(AUTHOR, author);

        long id = db.insert(TABLE_FAVORITES, null, contentValues);
        db.close();

        return id;
    }


    public boolean checkExitingImage(int img_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select " + IMG_ID + " from " + TABLE_FAVORITES + " where " + IMG_ID + " = ? ", new String[]{String.valueOf(img_id)});

        if (res.getCount() != 0) {
            res.close();
            db.close();

            return true;
        }

        return false;
    }


}


