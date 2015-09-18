package com.example.mediastock.activities;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import com.example.mediastock.R;

/**
 * A simple media player for music
 * 
 * @author Dinu
 */
public class MusicPlayerActivity extends Activity implements OnSeekBarChangeListener {
	private Button forward,pause,play,back;
	private MediaPlayer mediaPlayer;
	private double startTime = 0;
	private double finalTime = 0;
	private static Handler myHandler = new Handler();
	private int forwardTime = 5000;
	private int backwardTime = 5000;
	private SeekBar seekbar;
	private TextView tx1,tx2,title;
	public int oneTimeOnly = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_player);
		
		forward = (Button) findViewById(R.id.button_mediaPlayer_forward);
		pause = (Button) findViewById(R.id.button_mediaPlayer_pause);
		play = (Button)findViewById(R.id.button_mediaPlayer_play);
		back = (Button)findViewById(R.id.button_mediaPlayer_back);

		tx1 = (TextView)findViewById(R.id.textView2_music);
		tx2 = (TextView)findViewById(R.id.textView3_music);
		title = (TextView)findViewById(R.id.TextView_mediaPlayer_title);
		seekbar = (SeekBar)findViewById(R.id.seekBar_music);
		seekbar.setClickable(false);
		seekbar.setOnSeekBarChangeListener(this);
	
		String url = getIntent().getStringExtra("url");
		String music_title = getIntent().getStringExtra("title");
		title.setText(music_title);
		
		mediaPlayer = MediaPlayer.create(this.getApplicationContext(), Uri.parse(url));
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	
		playMusic();

		play.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				pause.setTextColor(Color.BLACK);
				play.setTextColor(Color.WHITE);
				playMusic();
			}
		});

		pause.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				play.setTextColor(Color.BLACK);
				pause.setTextColor(Color.WHITE);
				Toast.makeText(MusicPlayerActivity.this, "Pausing music",Toast.LENGTH_SHORT).show();
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
					Toast.makeText(MusicPlayerActivity.this,"Cannot jump forward 5 seconds",Toast.LENGTH_SHORT).show();
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
					Toast.makeText(MusicPlayerActivity.this,"Cannot jump backward 5 seconds",Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	
	@Override
	public void onBackPressed(){
		mediaPlayer.stop();
		mediaPlayer.release();
		mediaPlayer = null;
		this.finish();
	}
	
	
	private void playMusic(){
		play.setTextColor(Color.WHITE);
		
		
		try {
			
			mediaPlayer.prepare();
			
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		mediaPlayer.start();
		Toast.makeText(MusicPlayerActivity.this, "Playing music",Toast.LENGTH_SHORT).show();
		
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

		seekbar.setProgress((int) startTime);
		myHandler.postDelayed(new UpdateSongTime(this), 100);
		pause.setEnabled(true);
		play.setEnabled(false);
	}


	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
		if(fromUser)
			mediaPlayer.seekTo(progress);
	}


	private static class UpdateSongTime implements Runnable{
		private static WeakReference<MusicPlayerActivity> activity;

		public UpdateSongTime(MusicPlayerActivity context){
			activity = new WeakReference<>(context);
		}

		public void run(){

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
	public void onStartTrackingTouch(SeekBar seekBar) {}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {}


}

