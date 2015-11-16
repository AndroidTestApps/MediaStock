package com.example.mediastock.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

import com.example.mediastock.util.Utilities;

/**
 * Created by dinu on 13/11/15.
 */
public class DBController {
    private SQLiteDatabase db;
    private DBHelper dbHelper;


    public DBController(Context context) {
        open(context);
    }

    /**
     * Open the database
     */
    private void open(Context context) {
        dbHelper = DBHelper.getInstance(context);
        db = dbHelper.getWritableDatabase();
    }

    /**
     * Close the database. Done automatically by the android kernel
     */
 /*   public void close() {
        db.close();
        dbHelper.close();
    }
    */


    public Cursor getImages() {
        Cursor cursor = db.rawQuery("select * from " + DBHelper.TABLE_FAVORITES, null);
        cursor.moveToFirst();

        return cursor;
    }

    public boolean isOpened() {
        return db.isOpen();
    }

    public void reopen(Context context) {
        open(context);
    }


    public void deleteImage(int img_id) {

        // delete the image with id img_id
        db.delete(DBHelper.TABLE_FAVORITES, DBHelper.IMG_ID + " = ? ", new String[]{String.valueOf(img_id)});
    }


    public void insertImage(Bitmap image, int imgId, String description, String author) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(DBHelper.IMAGE, Utilities.convertToByteArray(image));
        contentValues.put(DBHelper.IMG_ID, new Integer(imgId));
        contentValues.put(DBHelper.DESCRIPTION, description);
        contentValues.put(DBHelper.AUTHOR, author);

        db.insert(DBHelper.TABLE_FAVORITES, null, contentValues);
    }


    public boolean checkExitingImage(int img_id) {
        Cursor res = db.rawQuery("select " + DBHelper.IMG_ID + " from " + DBHelper.TABLE_FAVORITES + " where "
                + DBHelper.IMG_ID + " = ? ", new String[]{String.valueOf(img_id)});

        if (res.getCount() != 0) {
            res.close();

            return true;
        }

        return false;
    }


    public void deleteTableFavorites() {
        db.execSQL("DROP TABLE IF EXISTS " + DBHelper.TABLE_FAVORITES);
    }

    public void createTable() {
        final String db_create = "create table " + DBHelper.TABLE_FAVORITES +
                "(_id integer primary key, " + DBHelper.IMAGE + " blob, " + DBHelper.IMG_ID + " integer, "
                + DBHelper.DESCRIPTION + " text, " + DBHelper.AUTHOR + " text)";

        db.execSQL(db_create);
    }

}
