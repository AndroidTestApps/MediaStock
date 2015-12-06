package com.example.mediastock.model;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Abstract class for the spinner row model
 */
public abstract class AbstractSpinnerRowAdapter extends ArrayAdapter<String> {
    private final LayoutInflater inflater;

    public AbstractSpinnerRowAdapter(Context context, int textViewResourceId, ArrayList<String> objects) {
        super(context, textViewResourceId, objects);

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

    public abstract View getCustomView(int position, View covertView, ViewGroup parent);


    public LayoutInflater getInflater() {
        return inflater;
    }
}
