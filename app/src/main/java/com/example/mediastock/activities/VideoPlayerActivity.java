package com.example.mediastock.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
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
import com.example.mediastock.util.Utilities;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/**
 * A simple video player activity
 *
 * @author Dinu
 */
public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback, OnSeekBarChangeListener {
    private static Handler myHandler = new Handler();
    public int oneTimeOnly = 0;
    private WeakReference<SurfaceHolder> surfaceHolder;
    private MediaPlayer mediaPlayer;
    private SurfaceView surfaceView;
    private Button play, pause;
    private double startTime = 0;
    private double finalTime = 0;
    private SeekBar seekbar;
    private TextView tx1, tx2;
    private ProgressDialog progressDialog;
    private boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if online
        if (!Utilities.deviceOnline(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "Not online", Toast.LENGTH_SHORT).show();
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

            seekbar = (SeekBar) findViewById(R.id.seekBar_video);
            seekbar.setClickable(false);
            seekbar.setOnSeekBarChangeListener(this);
            seekbar.getThumb().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            seekbar.getProgressDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

            getWindow().setFormat(PixelFormat.UNKNOWN);
            surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
            surfaceHolder = new WeakReference<>(surfaceView.getHolder());
            surfaceHolder.get().addCallback(this);
            surfaceHolder.get().setSizeFromLayout();

            mediaPlayer = new MediaPlayer();
            try {

                mediaPlayer.setDataSource(url);

            } catch (IOException e) {
                e.printStackTrace();
            }


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
        }
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
        super.onBackPressed();
        mediaPlayer.stop();

        finish();
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
