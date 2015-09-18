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

public class FilterVideoActivity extends Activity implements OnItemSelectedListener,OnClickListener{
	public static final String CATEGORY = "category";
	public static final String WORD = "word";
	public static final String SORT = "sort";
	public static final String FILTER_SEARCH = "filtersearch";
	public static final String PER_PAGE = "perpage";
	private SparseArray<String> query = new SparseArray<String>();
	private EditText word;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filter_video);

		word = (EditText) this.findViewById(R.id.editTextVideo_word);
		
		Spinner category = (Spinner) this.findViewById(R.id.spinnerVideo_category);
		ArrayAdapter<CharSequence> adapter_category = ArrayAdapter.createFromResource(this, R.array.category_array, android.R.layout.simple_spinner_item);
		adapter_category.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		category.setAdapter(adapter_category);
		category.setOnItemSelectedListener(this);
		
		Spinner sort = (Spinner) this.findViewById(R.id.spinnerVideo_sort);
		ArrayAdapter<CharSequence> adapter_sort= ArrayAdapter.createFromResource(this, R.array.sortby_array, android.R.layout.simple_spinner_item);
		adapter_sort.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sort.setAdapter(adapter_sort);
		sort.setOnItemSelectedListener(this);
		
		Spinner page = (Spinner) this.findViewById(R.id.spinnerVideo_per_page);
		ArrayAdapter<CharSequence> adapter_page = ArrayAdapter.createFromResource(this, R.array.per_page_array, android.R.layout.simple_spinner_item);
		adapter_page.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		page.setAdapter(adapter_page);
		page.setOnItemSelectedListener(this);
		
		Button search = (Button) this.findViewById(R.id.filterVideo_button);
		search.setOnClickListener(this);
	}


	/**
	 * Button search
	 */
	@Override
	public void onClick(View arg0) {
		Intent intent = new Intent(FilterVideoActivity.this, VideoGaleryActivity.class);

		intent.putExtra(FILTER_SEARCH, true);

		intent.putExtra(WORD, word.getText().toString());
		intent.putExtra(SORT, query.get(1));
		intent.putExtra(PER_PAGE, query.get(2));
		intent.putExtra(CATEGORY, query.get(3));

		startActivity(intent);
	}

	/**
	 * Spinner listener
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3) {
		int id = parent.getId();

		switch(id){
		case R.id.spinnerVideo_sort:
			query.put(1, parent.getItemAtPosition(pos).toString());
			break;
			
		case R.id.spinnerVideo_per_page:
			query.put(2, parent.getItemAtPosition(pos).toString());
			break;
			
		case R.id.spinnerVideo_category:
			query.put(3, parent.getItemAtPosition(pos).toString());
			break;

		default: break;

		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {}

}
