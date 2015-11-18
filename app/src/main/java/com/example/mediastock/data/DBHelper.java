package com.example.mediastock.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public final static String TABLE_IMAGES = "table_images";
    public final static String TABLE_MUSIC = "table_music";
    public final static String TABLE_VIDEOS = "table_video";
    // image attributes
    public final static String IMAGE = "image";
    public final static String DESCRIPTION_IMG = "description";
    public final static String AUTHOR_IMG = "author";
    public final static String IMG_ID = "imageid";
    // music attributes
    public final static String MUSIC = "music";
    public final static String TITLE_MUSIC = "title";
    public final static String MUSIC_ID = "musicid";
    // video attributes
    public final static String VIDEO = "video";
    public final static String DESCRIPTION_VIDEO = "description";
    public final static String VIDEO_ID = "videoid";
    private final static String DB_NAME = "db_mediastock";
    private static DBHelper instance;
    // create tables
    private final String createTableImages = "create table " + TABLE_IMAGES +
            "(_id integer primary key autoincrement, " + IMAGE + " blob, " + IMG_ID + " integer, " +
            DESCRIPTION_IMG + " text, " + AUTHOR_IMG + " text)";
    private final String createTableMusic = "create table " + TABLE_MUSIC +
            "(_id integer primary key autoincrement, " + MUSIC + " blob, " + MUSIC_ID + " integer, " + TITLE_MUSIC + " text)";
    private final String createTableVideos = "create table " + TABLE_VIDEOS +
            "(_id integer primary key autoincrement, " + VIDEO + " blob, " + VIDEO_ID + " integer, " + DESCRIPTION_VIDEO + " text)";


    private DBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null)
            instance = new DBHelper(context.getApplicationContext());

        return instance;
    }

    /**
     * Create tables
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(createTableImages);
        db.execSQL(createTableMusic);
        db.execSQL(createTableVideos);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MUSIC);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VIDEOS);

        onCreate(db);
    }

}


