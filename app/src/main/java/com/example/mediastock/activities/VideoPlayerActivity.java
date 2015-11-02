package com.example.mediastock.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mediastock.R;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/**
 * A simple media player for videos.
 *
 * @author Dinu
 */
public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback, OnSeekBarChangeListener {
    private static Handler myHandler = new Handler();
    public int oneTimeOnly = 0;
    private MediaPlayer mediaPlayer;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Button play, pause;
    private double startTime = 0;
    private double finalTime = 0;
    private SeekBar seekbar;
    private TextView tx1, tx2;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if online
        if (!isOnline()) {
            Toast.makeText(this, "Not online", Toast.LENGTH_SHORT).show();
            finish();
        } else {

            setContentView(R.layout.video_player_activity);

            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Loading...");
            progressDialog.show();

            play = (Button) this.findViewById(R.id.button_playvideoplayer);
            pause = (Button) this.findViewById(R.id.button_pausevideoplayer);
            tx1 = (TextView) findViewById(R.id.textView2_video);
            tx2 = (TextView) findViewById(R.id.textView3_video);

            String url = getIntent().getStringExtra("url");
            TextView description = (TextView) this.findViewById(R.id.textView_video_player_title);
            description.setText(getIntent().getStringExtra("description"));

            mediaPlayer = new MediaPlayer();
            try {

                mediaPlayer.setDataSource(url);

            } catch (IOException e) {
                e.printStackTrace();
            }

            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    progressDialog.dismiss();
                    playVideo();
                }
            });

            seekbar = (SeekBar) findViewById(R.id.seekBar_video);
            seekbar.setClickable(false);
            seekbar.setOnSeekBarChangeListener(this);
            seekbar.getThumb().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            seekbar.getProgressDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

            getWindow().setFormat(PixelFormat.UNKNOWN);
            surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
            surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(this);
            surfaceHolder.setSizeFromLayout();

            play.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    pause.setTextColor(Color.BLACK);
                    play.setTextColor(Color.WHITE);
                    playVideo();
                }
            });

            pause.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    play.setTextColor(Color.BLACK);
                    pause.setTextColor(Color.WHITE);
                    mediaPlayer.pause();
                    pause.setEnabled(false);
                    play.setEnabled(true);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        surfaceView.getHolder().getSurface().release();
        surfaceView.destroyDrawingCache();
        surfaceView = null;
    }

    @Override
    public void onBackPressed() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;

        finish();
        overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);
    }

    private void playVideo() {
        play.setTextColor(Color.WHITE);

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.reset();
        }

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDisplay(surfaceHolder);

        finalTime = mediaPlayer.getDuration();
        startTime = mediaPlayer.getCurrentPosition();
        mediaPlayer.start();

        if (oneTimeOnly == 0) {
            seekbar.setMax((int) finalTime);
            oneTimeOnly = 1;
        }

        tx2.setText(String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime)))
        );

        tx1.setText(String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime)))
        );

        seekbar.setProgress((int) startTime);
        myHandler.postDelayed(new UpdateTime(this), 100);
        pause.setEnabled(true);
        play.setEnabled(false);
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser)
            mediaPlayer.seekTo(progress);
    }

    /**
     * Checks if the device is connected to the Internet
     *
     * @return true if connected, false otherwise
     */
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        holder.getSurface().release();
    }

    // not used
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    private static class UpdateTime implements Runnable {
        private static WeakReference<VideoPlayerActivity> activity;

        public UpdateTime(VideoPlayerActivity context) {
            activity = new WeakReference<>(context);
        }

        public void run() {

            if (activity.get().mediaPlayer != null && activity.get().mediaPlayer.isPlaying()) {
                activity.get().startTime = activity.get().mediaPlayer.getCurrentPosition();
                activity.get().tx1.setText(String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes((long) activity.get().startTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) activity.get().startTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) activity.get().startTime))));

                activity.get().seekbar.setProgress((int) activity.get().startTime);
                myHandler.postDelayed(this, 100);
            }
        }
    }

}
