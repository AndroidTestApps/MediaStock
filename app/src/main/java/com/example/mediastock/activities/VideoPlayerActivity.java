package com.example.mediastock.activities;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
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
public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback, OnSeekBarChangeListener{
	private MediaPlayer mediaPlayer;
	private SurfaceView surfaceView;
	private Button play,pause,back,forward;
	private double startTime = 0;
	private double finalTime = 0;
	private static Handler myHandler = new Handler();
	private int forwardTime = 5000;
	private int backwardTime = 5000;
	private SeekBar seekbar;
	private TextView tx1,tx2;
	public int oneTimeOnly = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_player);

		forward = (Button) findViewById(R.id.button_videoPlayer_forward);
		back = (Button)findViewById(R.id.button_videoPlayer_back);
		play = (Button) this.findViewById(R.id.button_playvideoplayer);
		pause = (Button) this.findViewById(R.id.button_pausevideoplayer);
		tx1 = (TextView)findViewById(R.id.textView2_video);
		tx2 = (TextView)findViewById(R.id.textView3_video);
		seekbar = (SeekBar)findViewById(R.id.seekBar_video);
		seekbar.setClickable(false);
		seekbar.setOnSeekBarChangeListener(this);
		
		
		getWindow().setFormat(PixelFormat.UNKNOWN);
		surfaceView = (SurfaceView)findViewById(R.id.surfaceview);
        final SurfaceHolder surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setFixedSize(176, 144);

		String url = getIntent().getStringExtra("url");
		TextView description = (TextView) this.findViewById(R.id.textView_video_player_title);
		description.setText(getIntent().getStringExtra("description"));

		mediaPlayer = MediaPlayer.create(this.getApplicationContext(), Uri.parse(url));

		play.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				pause.setTextColor(Color.BLACK);
				play.setTextColor(Color.WHITE);
				playVideo(surfaceHolder);
			}		
		});


		pause.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				play.setTextColor(Color.BLACK);
				pause.setTextColor(Color.WHITE);
				Toast.makeText(VideoPlayerActivity.this, "Pausing video",Toast.LENGTH_SHORT).show();
				mediaPlayer.pause();
				pause.setEnabled(false);
				play.setEnabled(true);
			}
		});
		
		forward.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				forward.setTextColor(Color.WHITE);
				int temp = (int)startTime;

				if((temp+forwardTime)<=finalTime){
					startTime = startTime + forwardTime;
					mediaPlayer.seekTo((int) startTime);
				}
				else{
					Toast.makeText(VideoPlayerActivity.this,"Cannot jump forward 5 seconds",Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				back.setTextColor(Color.WHITE);
				int temp = (int)startTime;

				if((temp-backwardTime)>0){
					startTime = startTime - backwardTime;
					mediaPlayer.seekTo((int) startTime);
				}
				else{
					Toast.makeText(VideoPlayerActivity.this,"Cannot jump backward 5 seconds",Toast.LENGTH_SHORT).show();
				}
			}
		});

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
        surfaceView.getHolder().getSurface().release();
        surfaceView.destroyDrawingCache();
        surfaceView = null;
	}
	
	@Override
	public void onBackPressed(){
		mediaPlayer.stop();
		mediaPlayer.release();
		mediaPlayer = null;

		finish();
	}

	private void playVideo(SurfaceHolder holder){
		play.setTextColor(Color.WHITE);
		
		if(mediaPlayer.isPlaying()){
			mediaPlayer.reset();
		}

		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setDisplay(holder);

		try {

			mediaPlayer.prepare();

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mediaPlayer.start();
		Toast.makeText(VideoPlayerActivity.this, "Playing video",Toast.LENGTH_SHORT).show();
		
		finalTime = mediaPlayer.getDuration();
		startTime = mediaPlayer.getCurrentPosition();

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

		seekbar.setProgress((int)startTime);
		myHandler.postDelayed(new UpdateTime(this),100);
		pause.setEnabled(true);
		play.setEnabled(false);
	}


	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        playVideo(holder);
    }


	private static class UpdateTime implements Runnable{
		private static WeakReference<VideoPlayerActivity> activity;

		public UpdateTime(VideoPlayerActivity context){
			activity = new WeakReference<>(context);
		}

		public void run() {

			if(activity.get().mediaPlayer != null && activity.get().mediaPlayer.isPlaying()){
				activity.get().startTime = activity.get().mediaPlayer.getCurrentPosition();
				activity.get().tx1.setText(String.format("%d min, %d sec",
						TimeUnit.MILLISECONDS.toMinutes((long) activity.get().startTime),
						TimeUnit.MILLISECONDS.toSeconds((long) activity.get().startTime) -
								TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) activity.get().startTime))));

				activity.get().seekbar.setProgress((int)activity.get().startTime);
				myHandler.postDelayed(this, 100);
				activity.get().back.setTextColor(Color.BLACK);
				activity.get().forward.setTextColor(Color.BLACK);
			}
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
		if(fromUser)
			mediaPlayer.seekTo(progress);
	}


	// not used
	@Override
	public void onStartTrackingTouch(SeekBar seekBar){}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {}


	@Override
	public void surfaceDestroyed(SurfaceHolder holder) { holder.getSurface().release();}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {}


}
