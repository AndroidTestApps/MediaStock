package com.example.mediastock.activities;

import com.example.mediastock.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class FilterMusicActivity extends Activity implements OnItemSelectedListener,OnClickListener {
	public static final String ARTIST = "artist";
	public static final String TITLE = "title";
	public static final String GENRE = "genre";
	public static final String FILTER_SEARCH = "filterserach";
	public static final String PER_PAGE = "perpage";
	private SparseArray<String> query = new SparseArray<String>();
	private EditText artist, title;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filter_music);
		
		artist = (EditText) this.findViewById(R.id.editTextMusic_artist);
		title = (EditText) this.findViewById(R.id.editTextMusic_title);
		
		Spinner genre = (Spinner) this.findViewById(R.id.spinnerMusic_genre);
		ArrayAdapter<CharSequence> adapter_genre= ArrayAdapter.createFromResource(this, R.array.genre_array, android.R.layout.simple_spinner_item);
		adapter_genre.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		genre.setAdapter(adapter_genre);
		genre.setOnItemSelectedListener(this);
		
		Spinner page = (Spinner) this.findViewById(R.id.spinnerMusic_per_page);
		ArrayAdapter<CharSequence> adapter_page = ArrayAdapter.createFromResource(this, R.array.per_page_array, android.R.layout.simple_spinner_item);
		adapter_page.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		page.setAdapter(adapter_page);
		page.setOnItemSelectedListener(this);
		
		Button search = (Button) this.findViewById(R.id.filterMusic_button);
		search.setOnClickListener(this);
	}

	/**
	 * Spinner listener
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3) {
		int id = parent.getId();

		switch(id){
		case R.id.spinnerMusic_genre:
			query.put(1, parent.getItemAtPosition(pos).toString());
			break;
			
		case R.id.spinnerMusic_per_page:
			query.put(2, parent.getItemAtPosition(pos).toString());
			break;

		default: break;

		}
		
	}

	/**
	 * Button search
	 */
	@Override
	public void onClick(View v) {
		Intent intent = new Intent(FilterMusicActivity.this, MusicGaleryActivity.class);

		intent.putExtra(FILTER_SEARCH, true);

		intent.putExtra(ARTIST, artist.getText().toString());
		intent.putExtra(TITLE, title.getText().toString());
		intent.putExtra(GENRE, query.get(1));
		intent.putExtra(PER_PAGE, query.get(2));

		startActivity(intent);
	}
	
	@Override
	public void onNothingSelected(AdapterView<?> arg0){}

}
