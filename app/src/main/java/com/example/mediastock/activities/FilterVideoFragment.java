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
import android.widget.EditText;
import android.widget.Spinner;
import com.example.mediastock.R;


public class FilterVideoFragment extends AbstractFragment implements OnItemSelectedListener, OnClickListener {
    public static final String CATEGORY = "category";
    public static final String WORD = "word";
    public static final String SORT = "sort";
    public static final String PER_PAGE = "perpage";
    private SparseArray<String> query = new SparseArray<>();
    private EditText word;
    private Context context;
    private FilterVideoMessage filterVideoMessage;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        filterVideoMessage = (FilterVideoMessage) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getContext();

        View view = inflater.inflate(R.layout.filter_video_fragment, container, false);

        word = (EditText) view.findViewById(R.id.editTextVideo_word);

        Spinner category = (Spinner) view.findViewById(R.id.spinnerVideo_category);
        ArrayAdapter<CharSequence> adapter_category = ArrayAdapter.createFromResource(context, R.array.category_array, android.R.layout.simple_spinner_item);
        adapter_category.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(adapter_category);
        category.setOnItemSelectedListener(this);

        Spinner sort = (Spinner) view.findViewById(R.id.spinnerVideo_sort);
        ArrayAdapter<CharSequence> adapter_sort = ArrayAdapter.createFromResource(context, R.array.sortby_array, android.R.layout.simple_spinner_item);
        adapter_sort.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sort.setAdapter(adapter_sort);
        sort.setOnItemSelectedListener(this);

        Spinner page = (Spinner) view.findViewById(R.id.spinnerVideo_per_page);
        ArrayAdapter<CharSequence> adapter_page = ArrayAdapter.createFromResource(context, R.array.per_page_array, android.R.layout.simple_spinner_item);
        adapter_page.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        page.setAdapter(adapter_page);
        page.setOnItemSelectedListener(this);

        Button search = (Button) view.findViewById(R.id.filterVideo_button);
        search.setOnClickListener(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * Button search
     */
    @Override
    public void onClick(View arg0) {
        if (!isOnline()) {
            showAlertDialog();
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString(WORD, word.getText().toString());
        bundle.putString(SORT, query.get(1));
        bundle.putString(PER_PAGE, query.get(2));
        bundle.putString(CATEGORY, query.get(3));

        filterVideoMessage.handleFilterVideo(bundle);
    }

    /**
     * Spinner listener
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3) {
        int id = parent.getId();

        switch (id) {
            case R.id.spinnerVideo_sort:
                query.put(1, parent.getItemAtPosition(pos).toString());
                break;

            case R.id.spinnerVideo_per_page:
                query.put(2, parent.getItemAtPosition(pos).toString());
                break;

            case R.id.spinnerVideo_category:
                query.put(3, parent.getItemAtPosition(pos).toString());
                break;

            default:
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    public interface FilterVideoMessage {
        void handleFilterVideo(Bundle bundle);
    }

}
