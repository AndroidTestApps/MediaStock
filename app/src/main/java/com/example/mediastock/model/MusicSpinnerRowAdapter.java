package com.example.mediastock.model;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mediastock.R;

import java.util.ArrayList;

public class MusicSpinnerRowAdapter extends AbstractSpinnerRowAdapter {
    private final ArrayList<String> data;

    public MusicSpinnerRowAdapter(Context context, int textViewResourceId, ArrayList<String> objects) {
        super(context, textViewResourceId, objects);

        this.data = objects;
    }

    @Override
    public View getCustomView(int position, View covertView, ViewGroup parent) {
        View row = getInflater().inflate(R.layout.media_spinner_rows, parent, false);

        TextView text = (TextView) row.findViewById(R.id.text_spinner_row);
        text.setText(data.get(position));

        return row;
    }
}
