package com.example.mediastock.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mediastock.R;
import com.example.mediastock.activities.FavoriteImagesActivity;

import java.util.ArrayList;


public class CustomSpinnerRowAdapter extends ArrayAdapter<String> {
    // black, white, red, blue, green, yellow, orange, magenta, grey, cyan
    private final static String[] colorsID = {"#000000", "#ffffff", "#dc020e", "#0226dc", "#15a415", "#ffea00", "#ff8800", "#ff00ff", "#888888", "#00ffff"};
    private final LayoutInflater inflater;
    private final ArrayList<String> data;

    public CustomSpinnerRowAdapter(FavoriteImagesActivity context, int textViewResourceId, ArrayList<String> objects) {
        super(context, textViewResourceId, objects);

        this.data = objects;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View covertView, ViewGroup parent) {
        View row = inflater.inflate(R.layout.spinner_rows, parent, false);

        TextView color = (TextView) row.findViewById(R.id.text_spinner_row);
        ImageView image = (ImageView) row.findViewById(R.id.im_spinner_row);

        color.setText(data.get(position));
        GradientDrawable gradientDrawable = (GradientDrawable) image.getDrawable();
        gradientDrawable.setColor(Color.parseColor(colorsID[position]));

        return row;
    }

}
