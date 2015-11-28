package com.example.mediastock.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.mediastock.R;
import com.example.mediastock.data.DBController;
import com.example.mediastock.data.MusicBean;
import com.example.mediastock.util.ExecuteExecutor;
import com.example.mediastock.util.Utilities;

import java.io.FileInputStream;
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
    private double startTime = 0;
    private double finalTime = 0;
    private Button pause, play;
    private MediaPlayer mediaPlayer;
    private SeekBar seekbar;
    private TextView tx1, tx2, title;
    private ProgressDialog progressDialog;
    private FloatingActionButton favorites;
    private DBController db;
    private MusicBean bean;
    private String url;
    private int musicID;
    private boolean musicToDB = false;
    private boolean offlineWork = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_player_activity);

        // the database
        db = new DBController(this);

        favorites = (FloatingActionButton) this.findViewById(R.id.fab_favorites_music);
        favorites.setOnClickListener(this);
        final ImageView goBack = (ImageView) this.findViewById(R.id.imageView_goBack);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });

        constructProgressDialog();
        showProgressDialog("Loading...");

        // buttons play and stop
        pause = (Button) findViewById(R.id.button_mediaPlayer_pause);
        play = (Button) findViewById(R.id.button_mediaPlayer_play);

        tx1 = (TextView) findViewById(R.id.textView2_music);
        tx2 = (TextView) findViewById(R.id.textView3_music);
        title = (TextView) findViewById(R.id.TextView_mediaPlayer_title);

        // seekbar
        seekbar = (SeekBar) findViewById(R.id.seekBar_music);
        seekbar.setClickable(false);
        seekbar.setOnSeekBarChangeListener(this);
        seekbar.getThumb().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        seekbar.getProgressDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        // get bean infos
        bean = getBeanFromIntent();
        url = bean.getPreview();
        musicID = Integer.valueOf(bean.getId());

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
                pause.setVisibility(View.VISIBLE);
                play.setVisibility(View.GONE);
                playMusic();
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                play.setVisibility(View.VISIBLE);
                pause.setVisibility(View.GONE);
                mediaPlayer.pause();
            }
        });


        // check if online
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

    /**
     * Method to compute all the offline work. We play the music that is saved to the internal storage
     */
    private void computeOfflineWork() {
        offlineWork = true;

        // path of the music file
        String path = bean.getPath();

        try {

            FileInputStream fileInputStream = Utilities.loadMediaFromInternalStorage(Utilities.MUSIC_DIR, this, path);

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

        if (offlineWork) {
            Intent i = new Intent(getApplicationContext(), FavoriteMusicActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
            startActivity(i);
        }

        super.onBackPressed();
        overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);
    }

    private void constructProgressDialog() {
        progressDialog = new ProgressDialog(MusicPlayerActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
    }

    private void showProgressDialog(String message) {
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private int getIntentType() {
        return getIntent().getBundleExtra("bean").getInt("type");
    }

    private MusicBean getBeanFromIntent() {
        return getIntent().getBundleExtra("bean").getParcelable("bean");
    }

    private void playMusic() {
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
            favorites.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        }
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.fab_favorites_music) {

            // if the music is already saved in the db, it means we want to remove the music
            if (musicToDB) {
                musicToDB = false;
                favorites.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#838383")));

                showProgressDialog("Removing music from favorites...");

                // thread to remove the music from db
                new ExecuteExecutor(this, new ExecuteExecutor.CallableAsyncTask(this) {

                    @Override
                    public String call() {
                        MusicPlayerActivity context = (MusicPlayerActivity) getContextRef();

                        // path of the music
                        String path = context.db.getMusicPath(context.musicID);

                        // delete music infos
                        context.db.deleteMusicInfo(context.musicID);

                        // delete music from storage
                        Utilities.deleteSpecificMediaFromInternalStorage(Utilities.MUSIC_DIR, context, path);

                        context.progressDialog.dismiss();

                        return "Music removed from favorites";
                    }
                });


                // we have to add the music to favorites
            } else {
                musicToDB = true;
                favorites.setBackgroundTintList(ColorStateList.valueOf(Color.RED));

                showProgressDialog("Adding music to favorites...");

                // thread to save the music
                new ExecuteExecutor(this, new ExecuteExecutor.CallableAsyncTask(this) {

                    // thread to add the music to favorites
                    @Override
                    public String call() {
                        MusicPlayerActivity context = (MusicPlayerActivity) getContextRef();

                        HttpURLConnection con = null;
                        InputStream stream = null;
                        try {

                            URL urll = new URL(context.url);
                            con = (HttpURLConnection) urll.openConnection();
                            stream = con.getInputStream();

                            // save stream music to storage
                            String path = Utilities.saveMediaToInternalStorage(Utilities.MUSIC_DIR, context, stream, context.musicID);

                            // save music info to database
                            context.db.insertMusicInfo(path, context.musicID, context.bean.getTitle());

                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
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

                        context.progressDialog.dismiss();

                        return "Music added to favorites";
                    }
                });
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
            MusicPlayerActivity context = activity.get();

            if (context.mediaPlayer != null && context.mediaPlayer.isPlaying()) {
                context.startTime = context.mediaPlayer.getCurrentPosition();
                context.tx1.setText(String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes((long) context.startTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) context.startTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) context.startTime))));

                context.seekbar.setProgress((int) context.startTime);
                myHandler.postDelayed(this, 100);
            }
        }
    }
}

