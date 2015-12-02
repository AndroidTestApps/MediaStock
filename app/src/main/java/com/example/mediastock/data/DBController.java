package com.example.mediastock.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Class to control the database operations.
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
     * Query to get the images from db.
     *
     * @return the cursor object
     */
    public Cursor getImagesInfo() {
        Cursor cursor = db.rawQuery("select * from " + DBHelper.TABLE_IMAGES, null);

        if (cursor.getCount() > 0)
            cursor.moveToFirst();

        return cursor;
    }

    /**
     * Query to get the videos info from db.
     *
     * @return the cursor object
     */
    public Cursor getVideosInfo() {
        Cursor cursor = db.rawQuery("select * from " + DBHelper.TABLE_VIDEOS, null);

        if (cursor.getCount() > 0)
            cursor.moveToFirst();

        return cursor;
    }


    /**
     * Query to get the color palettes from db.
     *
     * @return the cursor object
     */
    public Cursor getColorPalettes() {
        Cursor cursor = db.rawQuery("select * from " + DBHelper.TABLE_COLORS, null);

        if (cursor.getCount() > 0)
            cursor.moveToFirst();

        return cursor;
    }


    /**
     * Query to get the music info from db.
     *
     * @return the cursor object
     */
    public Cursor getMusicInfo() {
        Cursor cursor = db.rawQuery("select * from " + DBHelper.TABLE_MUSIC, null);

        if (cursor.getCount() > 0)
            cursor.moveToFirst();

        return cursor;
    }



    /**
     * Method to add to database the music infos
     *
     * @param path    the path of the music
     * @param musicId the id of the music
     * @param title    the title of the music
     */
    public long insertMusicInfo(String path, int musicId, String title) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(DBHelper.MUSIC_PATH, path);
        contentValues.put(DBHelper.MUSIC_ID, new Integer(musicId));
        contentValues.put(DBHelper.TITLE_MUSIC, title);

        return db.insert(DBHelper.TABLE_MUSIC, null, contentValues);
    }

    /**
     * Method to add to database the videos infos
     *
     * @param path        the path of the video
     * @param videoID     the id of the video
     * @param description the description of the video
     */
    public long insertVideoInfo(String path, int videoID, String description) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(DBHelper.VIDEO_PATH, path);
        contentValues.put(DBHelper.VIDEO_ID, new Integer(videoID));
        contentValues.put(DBHelper.DESCRIPTION_VIDEO, description);

        return db.insert(DBHelper.TABLE_VIDEOS, null, contentValues);
    }

    /**
     * Method to add to database the image infos
     *
     * @param imagePath   the path of the image
     * @param imgId       the id of the image
     * @param description the description of the image
     * @param author      the author of the image
     */
    public long insertImageInfo(String imagePath, int imgId, String description, String author) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(DBHelper.IMG_PATH, imagePath);
        contentValues.put(DBHelper.IMG_ID, new Integer(imgId));
        contentValues.put(DBHelper.DESCRIPTION_IMG, description);
        contentValues.put(DBHelper.AUTHOR_IMG, author);

        return db.insert(DBHelper.TABLE_IMAGES, null, contentValues);
    }

    /**
     * Method to add to database the colors of the image with id imageID
     *
     * @param imageID the id of the image
     */
    public long insertColorPalette(int imageID, int vibrant, int lightVibrant, int darkVibrant, int muted, int lightMuted, int darkMuted, int dominantColor) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(DBHelper.IMG_ID, imageID);
        contentValues.put(DBHelper.VIBRANT, vibrant);
        contentValues.put(DBHelper.LIGHT_VIBRANT, lightVibrant);
        contentValues.put(DBHelper.DARK_VIBRANT, darkVibrant);
        contentValues.put(DBHelper.MUTED, muted);
        contentValues.put(DBHelper.LIGHT_MUTED, lightMuted);
        contentValues.put(DBHelper.DARK_MUTED, darkMuted);
        contentValues.put(DBHelper.DOMINANT_COLOR, dominantColor);

        return db.insert(DBHelper.TABLE_COLORS, null, contentValues);
    }


    /**
     * It deletes the image with id img_id
     *
     * @param imageID the id of the image to delete
     */
    public void deleteImageInfo(int imageID) {
        db.delete(DBHelper.TABLE_IMAGES, DBHelper.IMG_ID + " = ? ", new String[]{String.valueOf(imageID)});
    }


    /**
     * It deletes the color palette of the image with id imageID
     *
     * @param imageID the id of the image
     */
    public void deleteColorPalette(int imageID) {
        db.delete(DBHelper.TABLE_COLORS, DBHelper.IMG_ID + " = ? ", new String[]{String.valueOf(imageID)});
    }


    /**
     * It deletes the music with id musicID
     *
     * @param musicId the id of the music
     */
    public void deleteMusicInfo(int musicId) {

        db.delete(DBHelper.TABLE_MUSIC, DBHelper.MUSIC_ID + " = ? ", new String[]{String.valueOf(musicId)});
    }


    /**
     * It deletes the videos with id videoID
     *
     * @param videoId the id of the video
     */
    public void deleteVideoInfo(int videoId) {

        db.delete(DBHelper.TABLE_VIDEOS, DBHelper.VIDEO_ID + " = ? ", new String[]{String.valueOf(videoId)});
    }

    /**
     * Method to get the path of the music with id musicID
     *
     * @param musicID the id of the music
     * @return the path of the music
     */
    public String getMusicPath(int musicID) {
        Cursor cursor = db.rawQuery("select " + DBHelper.MUSIC_PATH + " from " + DBHelper.TABLE_MUSIC +
                " where " + DBHelper.MUSIC_ID + " = ? ", new String[]{String.valueOf(musicID)});

        cursor.moveToFirst();

        String path = cursor.getString(cursor.getColumnIndex(DBHelper.MUSIC_PATH));

        cursor.close();

        return path;
    }

    /**
     * Method to get the path of the image with id imageID
     *
     * @param imageID the id of the image
     * @return the path of the image
     */
    public String getImagePath(int imageID) {
        Cursor cursor = db.rawQuery("select " + DBHelper.IMG_PATH + " from " + DBHelper.TABLE_IMAGES +
                " where " + DBHelper.IMG_ID + " = ? ", new String[]{String.valueOf(imageID)});

        cursor.moveToFirst();

        String path = cursor.getString(cursor.getColumnIndex(DBHelper.IMG_PATH));

        cursor.close();

        return path;
    }

    /**
     * Method to get the dominant color of the image with id imageID
     *
     * @param imageID the id of the image
     * @return the dominant color
     */
    public int getDominantColorOfImage(int imageID) {
        Cursor cursor = db.rawQuery("select " + DBHelper.DOMINANT_COLOR + " from " + DBHelper.TABLE_COLORS +
                " where " + DBHelper.IMG_ID + " = ? ", new String[]{String.valueOf(imageID)});

        cursor.moveToFirst();
        int color = cursor.getInt(cursor.getColumnIndex(DBHelper.DOMINANT_COLOR));
        cursor.close();

        return color;
    }

    /**
     * Method to get the path of the video with id videoID
     *
     * @param videoID the id of the video
     * @return the path of the video
     */
    public String getVideoPath(int videoID) {
        Cursor cursor = db.rawQuery("select " + DBHelper.VIDEO_PATH + " from " + DBHelper.TABLE_VIDEOS +
                " where " + DBHelper.VIDEO_ID + " = ? ", new String[]{String.valueOf(videoID)});

        cursor.moveToFirst();

        String path = cursor.getString(cursor.getColumnIndex(DBHelper.VIDEO_PATH));

        cursor.close();

        return path;
    }

    /**
     * Method to check if the image with id img_id exists in the database
     *
     * @param imgId the id of the image
     * @return true if exists , false otherwise
     */
    public boolean checkExistingImage(int imgId) {
        Cursor res = db.rawQuery("select " + DBHelper.IMG_ID + " from " + DBHelper.TABLE_IMAGES + " where "
                + DBHelper.IMG_ID + " = ? ", new String[]{String.valueOf(imgId)});

        if (res.getCount() != 0) {
            res.close();

            return true;
        }

        return false;
    }

    /**
     * Method to check if the video with id videoID exists in the database
     *
     * @param videoID the id of the videos
     * @return true if exists , false otherwise
     */
    public boolean checkExistingVideo(int videoID) {
        Cursor res = db.rawQuery("select " + DBHelper.VIDEO_ID + " from " + DBHelper.TABLE_VIDEOS + " where "
                + DBHelper.VIDEO_ID + " = ? ", new String[]{String.valueOf(videoID)});

        if (res.getCount() != 0) {
            res.close();

            return true;
        }

        return false;
    }

    /**
     * Method to check if the music with id music_id exists in the database
     *
     * @param musicId the id of the music
     * @return true if exists , false otherwise
     */
    public boolean checkExistingMusic(int musicId) {
        Cursor res = db.rawQuery("select " + DBHelper.MUSIC_ID + " from " + DBHelper.TABLE_MUSIC + " where "
                + DBHelper.MUSIC_ID + " = ? ", new String[]{String.valueOf(musicId)});

        if (res.getCount() != 0) {
            res.close();

            return true;
        }

        return false;
    }


    public void deleteTables() {
        db.execSQL("DROP TABLE IF EXISTS " + DBHelper.TABLE_IMAGES);
        db.execSQL("DROP TABLE IF EXISTS " + DBHelper.TABLE_MUSIC);
        db.execSQL("DROP TABLE IF EXISTS " + DBHelper.TABLE_VIDEOS);
        db.execSQL("DROP TABLE IF EXISTS " + DBHelper.TABLE_COLORS);
    }

    public void deleteTableColorAndImage() {
        db.execSQL("DROP TABLE IF EXISTS " + DBHelper.TABLE_IMAGES);
        db.execSQL("DROP TABLE IF EXISTS " + DBHelper.TABLE_COLORS);
    }

    public void createTableColorAndImage() {
        dbHelper.createTables(db);
    }

    public void createTables() {
        dbHelper.onCreate(db);
    }

    public void deleteAllRowsMusic() {
        db.execSQL("delete from " + DBHelper.TABLE_MUSIC);
    }


    /**
     * Close the database. Done automatically by the android kernel
     */
 /*   public void close() {
        db.close();
        dbHelper.close();
    } */


}
