package com.example.mediastock.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.mediastock.R;
import com.example.mediastock.data.DBController;
import com.example.mediastock.data.VideoBean;
import com.example.mediastock.util.ExecuteExecutor;
import com.example.mediastock.util.Utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * A simple video player activity
 *
 * @author Dinu
 */
public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback, OnSeekBarChangeListener, View.OnClickListener {
    private static Handler myHandler = new Handler();
    public int oneTimeOnly = 0;
    private ProgressDialog progressDialog;
    private double startTime = 0;
    private double finalTime = 0;
    private WeakReference<SurfaceHolder> surfaceHolder;
    private MediaPlayer mediaPlayer;
    private SurfaceView surfaceView;
    private Button play, pause;
    private SeekBar seekbar;
    private TextView tx1, tx2;
    private boolean isPaused = false;
    private FloatingActionButton fabFavorites;
    private boolean videoToDB = false;
    private boolean offlineWork = false;
    private String url;
    private int videoID;
    private DBController db;
    private VideoBean bean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player_activity);

        // the database
        db = new DBController(this);

        fabFavorites = (FloatingActionButton) this.findViewById(R.id.fab_favorites);
        fabFavorites.setOnClickListener(this);

        constructProgressDialog();
        showProgressDialog("Loading...", 1);

        play = (Button) this.findViewById(R.id.button_playvideoplayer);
        pause = (Button) this.findViewById(R.id.button_pausevideoplayer);
        tx1 = (TextView) findViewById(R.id.textView2_video);
        tx2 = (TextView) findViewById(R.id.textView3_video);

        // get bean infos
        bean = getBeanFromIntent();
        url = bean.getPreview();
        videoID = Integer.valueOf(bean.getId());

        // set description of the video
        TextView description = (TextView) this.findViewById(R.id.textView_video_player_title);
        description.setText(bean.getDescription());

        // seekbar
        seekbar = (SeekBar) findViewById(R.id.seekBar_video);
        seekbar.setClickable(false);
        seekbar.setOnSeekBarChangeListener(this);
        seekbar.getThumb().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        seekbar.getProgressDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        // media player
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                progressDialog.dismiss();
                playVideo(surfaceHolder.get());
            }
        });

        play.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                pause.setTextColor(Color.BLACK);
                play.setTextColor(Color.RED);
                playVideo(surfaceHolder.get());
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

        // surface
        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        surfaceHolder = new WeakReference<>(surfaceView.getHolder());
        surfaceHolder.get().addCallback(this);
        surfaceHolder.get().setSizeFromLayout();

        // did we saved the video previously ? if so .. change the fab color
        checkExistingVideoInDB(videoID);
    }


    /**
     * Method to compute all the offline work. We play the video that is saved to the internal storage
     */
    private void computeOfflineWork() {
        offlineWork = true;

        // path of the video file
        String path = bean.getPath();

        try {

            FileInputStream fileInputStream = Utilities.loadMediaFromInternalStorage(Utilities.VIDEO_DIR, this, path);

            mediaPlayer.setDataSource(fileInputStream.getFD());

            fileInputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * We check in the database if the video with id videoID exists.
     * If exists we change the fab color.
     *
     * @param videoID the id of the video
     */
    private void checkExistingVideoInDB(int videoID) {
        if (db.checkExistingVideo(videoID)) {
            videoToDB = true;
            fabFavorites.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        }
    }

    private int getIntentType() {
        return getIntent().getBundleExtra("bean").getInt("type");
    }

    private VideoBean getBeanFromIntent() {
        return getIntent().getBundleExtra("bean").getParcelable("bean");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (surfaceView != null) {
            surfaceView.getHolder().getSurface().release();
            surfaceView.destroyDrawingCache();
            surfaceView = null;
            surfaceHolder = null;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPaused = true;
        }
    }

    @Override
    public void onBackPressed() {
        mediaPlayer.stop();

        if (offlineWork) {
            Intent i = new Intent(getApplicationContext(), FavoriteVideosActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
            startActivity(i);
        }

        super.onBackPressed();
        overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);
    }


    private void playVideo(SurfaceHolder holder) {
        play.setTextColor(Color.RED);

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.reset();
        }

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDisplay(holder);

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
        myHandler.postDelayed(new UpdateTime(this), 100);
        pause.setEnabled(true);
        play.setEnabled(false);
    }

    private void constructProgressDialog() {
        progressDialog = new ProgressDialog(VideoPlayerActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
    }

    private void showProgressDialog(String message, int where) {
        if (where == 1)
            progressDialog.getWindow().setGravity(Gravity.CENTER);
        else
            progressDialog.getWindow().setGravity(Gravity.BOTTOM);

        progressDialog.setMessage(message);
        progressDialog.show();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser)
            mediaPlayer.seekTo(progress);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if (isPaused) {
            playVideo(holder);
            isPaused = false;

        } else
            mediaPlayer.prepareAsync();
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.fab_favorites) {

            // if the video is already saved in the db, it means we want to remove the video
            if (videoToDB) {
                videoToDB = false;
                fabFavorites.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#838383")));

                showProgressDialog("Removing video to favorites...", 2);

                // thread to remove the video from db
                new ExecuteExecutor(this, new ExecuteExecutor.CallableAsyncTask(this) {

                    @Override
                    public String call() {
                        VideoPlayerActivity activity = (VideoPlayerActivity) getContextRef();

                        // path of the video
                        String path = activity.db.getVideoPath(activity.videoID);

                        // delete video infos
                        activity.db.deleteVideoInfo(activity.videoID);

                        // delete video from storage
                        Utilities.deleteSpecificMediaFromInternalStorage(Utilities.VIDEO_DIR, activity, path);

                        activity.progressDialog.dismiss();

                        return "Video removed from favorites";
                    }
                });

                // we have to add the video to favorites
            } else {
                videoToDB = true;
                fabFavorites.setBackgroundTintList(ColorStateList.valueOf(Color.RED));

                showProgressDialog("Adding video to favorites...", 2);

                // thread to add the video to favorites
                new ExecuteExecutor(this, new ExecuteExecutor.CallableAsyncTask(this) {

                    @Override
                    public String call() {
                        VideoPlayerActivity context = (VideoPlayerActivity) getContextRef();

                        HttpURLConnection con = null;
                        InputStream stream = null;
                        try {

                            URL urll = new URL(context.url);
                            con = (HttpURLConnection) urll.openConnection();
                            con.setConnectTimeout(25000);
                            stream = con.getInputStream();

                            // save stream video to storage
                            String path = Utilities.saveMediaToInternalStorage(Utilities.VIDEO_DIR, context, stream, context.videoID);

                            // save video info to database
                            context.db.insertVideoInfo(path, context.videoID, context.bean.getDescription());

                        } catch (SocketTimeoutException e) {
                            if (con != null)
                                con.disconnect();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();

                        } finally {

                            // clean the objects
                            if (stream != null) {
                                con.disconnect();

                                try {

                                    stream.close();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                if (con != null)
                                    con.disconnect();
                            }
                        }

                        context.progressDialog.dismiss();

                        return "Video added to favorites";
                    }
                });
            }
        }
    }


    /**
     * Thread to update the time of the video
     */
    private static class UpdateTime implements Runnable {
        private static WeakReference<VideoPlayerActivity> activity;

        public UpdateTime(VideoPlayerActivity context) {
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
}
