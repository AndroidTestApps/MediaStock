package com.example.mediastock.activities;

import com.example.mediastock.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;


public class FilterActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// check if online
		if(!isOnline()){
			setContentView(R.layout.no_internet);
			Toast.makeText(this, "Not online", Toast.LENGTH_SHORT).show();
		}else{
			
			setContentView(R.layout.activity_filter);
			
			Button image = (Button) this.findViewById(R.id.filter_filterImage);
			Button  music = (Button ) this.findViewById(R.id.filter_filterMusic);
			Button video = (Button) this.findViewById(R.id.filter_filterVideo);
			
			image.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(FilterActivity.this, FilterImageActivity.class);
					startActivity(intent);
				}
			});
			
			music.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(FilterActivity.this, FilterMusicActivity.class);
					startActivity(intent);			
				}			
			});
			
			video.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(FilterActivity.this, FilterVideoActivity.class);
					startActivity(intent);	
				}		
			});
			

		}
	}
	
	
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}

}
