package com.example.mediastock.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mediastock.R;
import com.example.mediastock.data.DBController;
import com.example.mediastock.data.MusicBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * A simple music player activity.
 *
 * @author Dinu
 */
public class MusicPlayerActivity extends Activity implements OnSeekBarChangeListener, View.OnClickListener {
    private static Handler myHandler = new Handler();
    public int oneTimeOnly = 0;
    private Button pause, play;
    private MediaPlayer mediaPlayer;
    private double startTime = 0;
    private double finalTime = 0;
    private SeekBar seekbar;
    private TextView tx1, tx2, title;
    private ProgressDialog progressDialog;
    private boolean musicToDB = false;
    private FloatingActionButton fabFavorites;
    private DBController db;
    private String url = "";
    private int musicID;
    private MusicBean bean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_player_activity);

        // the database controller
        db = new DBController(this);

        fabFavorites = (FloatingActionButton) this.findViewById(R.id.fab_favorites_music);
        fabFavorites.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        // buttons play and stop
        pause = (Button) findViewById(R.id.button_mediaPlayer_pause);
        play = (Button) findViewById(R.id.button_mediaPlayer_play);

        tx1 = (TextView) findViewById(R.id.textView2_music);
        tx2 = (TextView) findViewById(R.id.textView3_music);
        title = (TextView) findViewById(R.id.TextView_mediaPlayer_title);

        seekbar = (SeekBar) findViewById(R.id.seekBar_music);
        seekbar.setClickable(false);
        seekbar.setOnSeekBarChangeListener(this);
        seekbar.getThumb().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        seekbar.getProgressDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        // get bean
        bean = getBeanFromIntent();
        url = bean.getPreview();                 // url
        musicID = Integer.valueOf(bean.getId()); // music id

        // set the title of the music
        title.setText(bean.getTitle());

        // the media player
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                progressDialog.dismiss();
                playMusic();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                pause.setTextColor(Color.BLACK);
                play.setTextColor(Color.RED);
                playMusic();
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                play.setTextColor(Color.BLACK);
                pause.setTextColor(Color.RED);
                mediaPlayer.pause();
                pause.setEnabled(false);
                play.setEnabled(true);
            }
        });


        // we are offline ?
        if (getIntentType() == 2) {
            computeOfflineWork();

        } else {


            try {
                // set online source
                mediaPlayer.setDataSource(url);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mediaPlayer.prepareAsync();

        // did we saved the music previously ? if so .. change the fab color
        checkExistingMusicInDB(musicID);
    }

    private void computeOfflineWork() {
        try {
            // create temp file that will hold byte array
            File tempMusicFile = File.createTempFile("temp" + bean.getId(), "mp3", getCacheDir());
            tempMusicFile.deleteOnExit();

            FileOutputStream fileOutputStream = new FileOutputStream(tempMusicFile);
            fileOutputStream.write(bean.getByteMusic());
            fileOutputStream.close();

            //MediaPlayer mediaPlayer = new MediaPlayer();
            FileInputStream fileInputStream = new FileInputStream(tempMusicFile);

            mediaPlayer.setDataSource(fileInputStream.getFD());

            fileInputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        this.finish();
        overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);
    }

    private int getIntentType() {
        return getIntent().getBundleExtra("bean").getInt("type");
    }

    private MusicBean getBeanFromIntent() {
        return getIntent().getBundleExtra("bean").getParcelable("bean");
    }

    private void playMusic() {
        play.setTextColor(Color.RED);
        finalTime = mediaPlayer.getDuration();
        startTime = mediaPlayer.getCurrentPosition();

        mediaPlayer.start();

        if (oneTimeOnly == 0) {
            seekbar.setMax((int) finalTime);
            oneTimeOnly = 1;
        }

        tx2.setText(String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime)))
        );

        tx1.setText(String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime)))
        );

        seekbar.setProgress((int) startTime);
        myHandler.postDelayed(new UpdateSongTime(this), 100);
        pause.setEnabled(true);
        play.setEnabled(false);
    }

    /**
     * We check in the database if the music with id music_id exists.
     * If exists we change the fab color.
     *
     * @param music_id the id of the music
     */
    private void checkExistingMusicInDB(int music_id) {
        if (db.checkExistingMusic(music_id)) {
            musicToDB = true;
            fabFavorites.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        }
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.fab_favorites_music) {

            // if the music il already saved in the db, it means we want to remove the music
            if (musicToDB) {
                musicToDB = false;
                fabFavorites.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#838383")));

                // thread to remove the music from db
                // new AsyncDbWork(this, 1).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, musicID);


                // the music is not in the database, so we have to save it
            } else {
                musicToDB = true;
                fabFavorites.setBackgroundTintList(ColorStateList.valueOf(Color.RED));

                // thread to save the music to database
                // new AsyncDbWork(this, 2).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser)
            mediaPlayer.seekTo(progress);
    }


    // not used
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }


    /**
     * Thread to update the time of the music.
     */
    private static class UpdateSongTime implements Runnable {
        private static WeakReference<MusicPlayerActivity> activity;

        public UpdateSongTime(MusicPlayerActivity context) {
            activity = new WeakReference<>(context);
        }

        public void run() {

            if (activity.get().mediaPlayer != null && activity.get().mediaPlayer.isPlaying()) {
                activity.get().startTime = activity.get().mediaPlayer.getCurrentPosition();
                activity.get().tx1.setText(String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes((long) activity.get().startTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) activity.get().startTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) activity.get().startTime))));

                activity.get().seekbar.setProgress((int) activity.get().startTime);
                myHandler.postDelayed(this, 100);
            }
        }
    }


    private static class AsyncDbWork extends AsyncTask<Integer, Void, Long> {
        private static WeakReference<MusicPlayerActivity> activity;
        private final int type;

        public AsyncDbWork(MusicPlayerActivity context, int type) {
            activity = new WeakReference<>(context);
            this.type = type;
        }

        @Override
        protected Long doInBackground(Integer... params) {

            if (type == 1)
                return removeMusicFromFavorites(params[0]);
            else
                return addMusicToFavorites();
        }

        private long addMusicToFavorites() {
            MusicPlayerActivity context = activity.get();

            HttpURLConnection con = null;
            InputStream stream = null;
            long result = 0;
            try {

                URL urll = new URL(context.url);
                con = (HttpURLConnection) urll.openConnection();
                stream = con.getInputStream();

                // save music to database
                //result = context.db.insertMusic(Utilities.covertStreamToByte(stream), context.musicID, context.bean.getTitle());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.i("music to db", " music covertStreamToByte");
                e.printStackTrace();

            } finally {

                // and finally we close the objects
                if (con != null && stream != null) {
                    con.disconnect();

                    try {

                        stream.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return result;
        }

        private long removeMusicFromFavorites(int id) {

            activity.get().db.deleteMusic(id);

            return 0;
        }

        @Override
        protected void onPostExecute(Long value) {
            super.onPostExecute(value);

            if (type == 2) {
                if (value > 0)
                    Toast.makeText(activity.get().getApplicationContext(), "Music added to favorites", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(activity.get().getApplicationContext(), "Error adding the music to favorites", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(activity.get().getApplicationContext(), "Music removed from favorites", Toast.LENGTH_SHORT).show();

            activity = null;
        }
    }
}

