package com.example.mediastock.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public final static String TABLE_IMAGES = "table_images";
    public final static String TABLE_MUSIC = "table_music";
    public final static String TABLE_VIDEOS = "table_video";
    public final static String TABLE_COLORS = "table_colors";
    // image attributes
    public final static String IMG_PATH = "path";
    public final static String DESCRIPTION_IMG = "description";
    public final static String AUTHOR_IMG = "author";
    public final static String IMG_ID = "imageid";
    // music attributes
    public final static String MUSIC_PATH = "path";
    public final static String TITLE_MUSIC = "title";
    public final static String MUSIC_ID = "musicid";
    // video attributes
    public final static String VIDEO_PATH = "path";
    public final static String DESCRIPTION_VIDEO = "description";
    public final static String VIDEO_ID = "videoid";
    // names of the color palette of an image
    public final static String VIBRANT = "vibrant";
    public final static String LIGHT_VIBRANT = "lightvibrant";
    public final static String DARK_VIBRANT = "darkvibrant";
    public final static String MUTED = "muted";
    public final static String LIGHT_MUTED = "lightmuted";
    public final static String DARK_MUTED = "darkmuted";
    private final static String DB_NAME = "db_mediastock";
    // the db instance
    private static DBHelper instance;

    // create tables
    private final String createTableImages = "create table " + TABLE_IMAGES +
            "(_id integer primary key autoincrement, " + IMG_PATH + " text, " + IMG_ID + " integer, " +
            DESCRIPTION_IMG + " text, " + AUTHOR_IMG + " text)";

    private final String createTableMusic = "create table " + TABLE_MUSIC +
            "(_id integer primary key autoincrement, " + MUSIC_PATH + " text, " + MUSIC_ID + " integer, " + TITLE_MUSIC + " text)";

    private final String createTableVideos = "create table " + TABLE_VIDEOS +
            "(_id integer primary key autoincrement, " + VIDEO_PATH + " text, " + VIDEO_ID + " integer, " + DESCRIPTION_VIDEO + " text)";

    private final String createTableColors = "create table " + TABLE_COLORS +
            "(_id integer primary key autoincrement, " + IMG_ID + " integer, " + VIBRANT + " integer, " + LIGHT_VIBRANT + " integer, " +
            DARK_VIBRANT + " integer, " + MUTED + " integer, " + LIGHT_MUTED + " integer, " + DARK_MUTED + " integer)";


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
        db.execSQL(createTableColors);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MUSIC);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VIDEOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COLORS);

        onCreate(db);
    }

}


