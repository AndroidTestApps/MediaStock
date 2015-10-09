package com.example.mediastock.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.mediastock.R;

import java.util.ArrayList;

public class FilterImageFragment extends AbstractFragment implements OnItemSelectedListener, OnClickListener{
	private ArrayList<String> orientation_list = new ArrayList<>();
	public static final String CATEGORY = "category";
	public static final String ORIENTATION = "orientation";
	public static final String SORT_BY = "sortby";
	public static final String PER_PAGE = "perpage";
	private SparseArray<String> query = new SparseArray<>();
    private Context context;
    private FilterImageMessage imageMessage;


    public interface FilterImageMessage {
        void handleFilterImage(Bundle bundle);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        imageMessage = (FilterImageMessage) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getContext();

        if(!isOnline())
            return null;

        View  view = inflater.inflate(R.layout.filter_image_fragment, container, false);

        initList();

        Spinner category = (Spinner) view.findViewById(R.id.spinner_category);
        ArrayAdapter<CharSequence> adapter_category = ArrayAdapter.createFromResource(context, R.array.category_array, android.R.layout.simple_spinner_item);
        adapter_category.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(adapter_category);
        category.setOnItemSelectedListener(this);

        Spinner orientation = (Spinner) view.findViewById(R.id.spinner_orientation);
        ArrayAdapter<String> adapter_orientation =  new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, orientation_list);
        adapter_orientation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orientation.setAdapter(adapter_orientation);
        orientation.setOnItemSelectedListener(this);

        Spinner sortBy = (Spinner) view.findViewById(R.id.spinner_sortBy);
        ArrayAdapter<CharSequence> adapter_sortBy = ArrayAdapter.createFromResource(context, R.array.sortby_array, android.R.layout.simple_spinner_item);
        adapter_sortBy.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortBy.setAdapter(adapter_sortBy);
        sortBy.setOnItemSelectedListener(this);

        Spinner page = (Spinner) view.findViewById(R.id.spinner_per_page);
        ArrayAdapter<CharSequence> adapter_page = ArrayAdapter.createFromResource(context, R.array.per_page_array, android.R.layout.simple_spinner_item);
        adapter_page.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        page.setAdapter(adapter_page);
        page.setOnItemSelectedListener(this);

        Button search_button = (Button) view.findViewById(R.id.filter_button_search);
        search_button.setOnClickListener(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if(!isOnline())
            return;
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
        Bundle bundle = new Bundle();
        bundle.putString(CATEGORY, query.get(1));
        bundle.putString(ORIENTATION, query.get(3));
        bundle.putString(SORT_BY, query.get(5));
        bundle.putString(PER_PAGE, query.get(6));

        imageMessage.handleFilterImage(bundle);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {}
}
