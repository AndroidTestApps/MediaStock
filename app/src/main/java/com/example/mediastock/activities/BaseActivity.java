package com.example.mediastock.activities;


import java.util.ArrayList;

import com.example.mediastock.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Parent activity. All the children activities share the same actionBar and consequently the search and filter.
 * 
 * @author Dinu
 */
public abstract class BaseActivity extends Activity{
	private String search_type;
	private static String key1;
	private static String key2;

	@Override
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch(item.getItemId()){
		case R.id.item_home:
			openHome(); return true;
		case R.id.item_filter: 
			openFilterSearch(null); return true;
		case R.id.item_search: 
			openSearch();
			return true;
		}

		return true;
	}


	/**
	 * Search button.
	 */
	private void openSearch() {
		showKeyboard();

		android.app.ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);

		LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflator.inflate(R.layout.actionbar, null);
		actionBar.setCustomView(v);

		Spinner filter = (Spinner) this.findViewById(R.id.spinner_filter);

		ArrayAdapter<String> adapter_filter = null;
		if(this instanceof VideoGaleryActivity)
			adapter_filter = new ArrayAdapter<String>(this, R.layout.spinner_item, getVideoList());
		else if(this instanceof MusicGaleryActivity)
			adapter_filter = new ArrayAdapter<String>(this, R.layout.spinner_item, getMusicList());
		else
			adapter_filter = new ArrayAdapter<String>(this, R.layout.spinner_item, getImageList());

		adapter_filter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		filter.setAdapter(adapter_filter);

		filter.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long arg3) {
				search_type = parent.getItemAtPosition(pos).toString();	
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});


		// search icon inside edittext listener
		final EditText input = (EditText ) v.findViewById(R.id.actionbar_search);
		input.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				showKeyboard();
				
				if(event.getAction() == MotionEvent.ACTION_UP) {
					if(event.getRawX() >= input.getRight() - input.getTotalPaddingRight()) {

						if(!input.getText().toString().isEmpty()){
							startSearch(input.getText().toString());
							hideKeyboard();
						}
						return true;
					}
				}
				return true;
			}
		});

		input.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				showKeyboard();
				
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {	
					startSearch(v.getText().toString());
					hideKeyboard();
					return true;
				}
				return false;
			}
		});
	}

	private ArrayList<String> getMusicList(){
		ArrayList<String> list = new ArrayList<String>();
		list.add("Music");
		list.add("Image");
		list.add("Video");

		return list;
	}

	private ArrayList<String> getVideoList(){
		ArrayList<String> list = new ArrayList<String>();
		list.add("Video");
		list.add("Music");
		list.add("Image");

		return list;
	}

	private ArrayList<String> getImageList(){
		ArrayList<String> list = new ArrayList<String>();
		list.add("Image");
		list.add("Video");
		list.add("Music");

		return list;
	}

	/**
	 * Start the ImageGaleryActivity or MusicGaleryActivity or VideoGaleryActivity.
	 * We pass the user input as key.
	 * 
	 * @param search the user input
	 */
	private void startSearch(String search){
		boolean twoWords = parseQuery(search);

		Intent intent = null;
		if(this.search_type.equals("Image"))
			intent = new Intent(getApplicationContext(), ImageGaleryActivity.class);
		else if(this.search_type.equals("Music"))
			intent = new Intent(getApplicationContext(), MusicGaleryActivity.class);
		else
			intent = new Intent(getApplicationContext(), VideoGaleryActivity.class);

		if(twoWords){
			intent.putExtra("twoWords", true);
			intent.putExtra("key1", key1);
			intent.putExtra("key2", key2);
		}else		
			intent.putExtra("key", search);

		intent.putExtra("search", true);

		if(this instanceof MainActivity)
			startActivity(intent);
		else {
			startActivity(intent);
			finish();
		}


	}


	/**
	 * We parse the text that the user wrote.
	 * If the user wrote two words, we parse the text and search for the words.
	 * 
	 * @param query the text of the user 
	 * @return true if the user wrote two words, false otherwise
	 */
	private boolean parseQuery(String query){
		boolean twoWords = false;

		for(int i = 0; i < query.length(); i++)
			if(query.charAt(i) == ' '){
				key1 = query.substring(0, i);
				key2 = query.substring(i+1, query.length());
				twoWords = true;
			}

		return twoWords;
	}

	private void openHome() {
		Intent intent = new Intent(getApplicationContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private void openFilterSearch(String type) {
		Intent intent = new Intent(getApplicationContext(), FilterActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private void hideKeyboard() {   
		View view = getCurrentFocus();
		if (view != null) {
			InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);	
		}
	}

	private void showKeyboard(){
		View view = getCurrentFocus();
		if (view != null) {
			InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);	
			inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
		}
	}

	/**
	 * Checks if the device is connected to the Internet
	 * 
	 * @return true if connected, false otherwise
	 */
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}


}
