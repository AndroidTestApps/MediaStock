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
import android.widget.Toast;

import com.example.mediastock.R;
import com.example.mediastock.util.Utilities;

public class FilterMusicFragment extends AbstractFragment implements OnItemSelectedListener, OnClickListener {
    public static final String ARTIST = "artist";
    public static final String TITLE = "title";
    public static final String GENRE = "genre";
    public static final String PER_PAGE = "perpage";
    private final SparseArray<String> query = new SparseArray<>();
    private EditText artist, title;
    private Context context;
    private FilterMusicMessage filterMusicMessage;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        filterMusicMessage = (FilterMusicMessage) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getContext();

        View  view = inflater.inflate(R.layout.filter_music_fragment, container, false);

        artist = (EditText) view.findViewById(R.id.editTextMusic_artist);
        title = (EditText) view.findViewById(R.id.editTextMusic_title);

        Spinner genre = (Spinner) view.findViewById(R.id.spinnerMusic_genre);
        ArrayAdapter<CharSequence> adapter_genre = ArrayAdapter.createFromResource(context, R.array.genre_array, R.layout.spinner_item);
        adapter_genre.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genre.setAdapter(adapter_genre);
        genre.setOnItemSelectedListener(this);

        Spinner page = (Spinner) view.findViewById(R.id.spinnerMusic_per_page);
        ArrayAdapter<CharSequence> adapter_page = ArrayAdapter.createFromResource(context, R.array.per_page_array, R.layout.spinner_item);
        adapter_page.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        page.setAdapter(adapter_page);
        page.setOnItemSelectedListener(this);

        Button search = (Button) view.findViewById(R.id.filterMusic_button);
        search.setOnClickListener(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * Spinner listener
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View arg1, int pos, long arg3) {
        int id = parent.getId();

        switch (id) {
            case R.id.spinnerMusic_genre:
                query.put(1, parent.getItemAtPosition(pos).toString());
                break;

            case R.id.spinnerMusic_per_page:
                query.put(2, parent.getItemAtPosition(pos).toString());
                break;

            default:
                break;

        }

    }

    /**
     * Button search
     */
    @Override
    public void onClick(View v) {
        if (!Utilities.deviceOnline(context)) {
            Toast.makeText(context.getApplicationContext(), "There is no internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString(ARTIST, artist.getText().toString());
        bundle.putString(TITLE, title.getText().toString());
        bundle.putString(GENRE, query.get(1));
        bundle.putString(PER_PAGE, query.get(2));

        filterMusicMessage.handleFilterMusic(bundle);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }


    public interface FilterMusicMessage {
        void handleFilterMusic(Bundle bundle);
    }


}
