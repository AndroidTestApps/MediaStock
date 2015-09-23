package com.example.mediastock.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.mediastock.R;

import java.util.ArrayList;

public class FilterImageActivity extends Activity implements OnItemSelectedListener, OnClickListener{
	private ArrayList<String> orientation_list = new ArrayList<>();
	public static final String CATEGORY = "category";
	public static final String ORIENTATION = "orientation";
	public static final String SORT_BY = "sortby";
	public static final String FILTER_SEARCH = "filterserach";
	public static final String PER_PAGE = "perpage";
	private SparseArray<String> query = new SparseArray<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filter_image);

		initList();

		Spinner category = (Spinner) this.findViewById(R.id.spinner_category);
		ArrayAdapter<CharSequence> adapter_category = ArrayAdapter.createFromResource(this, R.array.category_array, android.R.layout.simple_spinner_item);
		adapter_category.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		category.setAdapter(adapter_category);
		category.setOnItemSelectedListener(this);

		Spinner orientation = (Spinner) this.findViewById(R.id.spinner_orientation);
		ArrayAdapter<String> adapter_orientation =  new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, orientation_list);
		adapter_orientation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		orientation.setAdapter(adapter_orientation);
		orientation.setOnItemSelectedListener(this);

		Spinner sortBy = (Spinner) this.findViewById(R.id.spinner_sortBy);
		ArrayAdapter<CharSequence> adapter_sortBy = ArrayAdapter.createFromResource(this, R.array.sortby_array, android.R.layout.simple_spinner_item);
		adapter_sortBy.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sortBy.setAdapter(adapter_sortBy);
		sortBy.setOnItemSelectedListener(this);

		Spinner page = (Spinner) this.findViewById(R.id.spinner_per_page);
		ArrayAdapter<CharSequence> adapter_page = ArrayAdapter.createFromResource(this, R.array.per_page_array, android.R.layout.simple_spinner_item);
		adapter_page.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		page.setAdapter(adapter_page);
		page.setOnItemSelectedListener(this);

		Button search_button = (Button) this.findViewById(R.id.filter_button_search);
		search_button.setOnClickListener(this);
	}

	/**
	 * Spinner listener
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long arg3) {
		int id = parent.getId();

		switch(id){

		case R.id.spinner_category: 
			query.put(1, parent.getItemAtPosition(pos).toString());
			break;

		case R.id.spinner_orientation:
			query.put(3, parent.getItemAtPosition(pos).toString());
			break;

		case R.id.spinner_sortBy:
			query.put(5, parent.getItemAtPosition(pos).toString());
			break;

		case R.id.spinner_per_page:
			query.put(6, parent.getItemAtPosition(pos).toString());
			break;

		default: break;

		}
	}


	private void initList(){
		orientation_list.add("All");	
		orientation_list.add("Horizontal");
		orientation_list.add("Vertical");
	}


	/**
	 * Button search
	 */
	@Override
	public void onClick(View v) {
		Intent intent = new Intent(FilterImageActivity.this, ImageGalleryActivity.class);

		intent.putExtra(FILTER_SEARCH, true);

		intent.putExtra(CATEGORY, query.get(1));
		intent.putExtra(ORIENTATION, query.get(3));
		intent.putExtra(SORT_BY, query.get(5));
		intent.putExtra(PER_PAGE, query.get(6));

		startActivity(intent);		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {}
}
