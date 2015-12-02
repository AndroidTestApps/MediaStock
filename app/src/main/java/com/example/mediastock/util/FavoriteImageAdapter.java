package com.example.mediastock.util;


import android.app.Activity;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.mediastock.R;

import java.util.ArrayList;

public class FavoriteImageAdapter extends BaseAdapter {
    public final ArrayList<CheckBox> checkBoxesViews = new ArrayList<>();
    private final RelativeLayout.LayoutParams layout_param;
    private final int width;
    private final Context context;
    private final ArrayList<String> pathList = new ArrayList<>();
    private final ArrayList<Integer> filteredImagesPositions = new ArrayList<>();
    private final SparseArray<Integer> checkBoxes = new SparseArray<>();

    public FavoriteImageAdapter(Context context) {
        super();

        this.context = context;
        this.width = context.getResources().getDisplayMetrics().widthPixels;
        this.layout_param = new RelativeLayout.LayoutParams(width / 2, width / 2);
        this.layout_param.setMargins(1, 0, 0, 1);
    }

    @Override
    public int getCount() {
        return pathList.size();
    }

    @Override
    public Object getItem(int position) {
        return pathList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void addPath(String image, int position) {
        pathList.add(position, image);
        notifyDataSetChanged();
    }

    public void deletePathList() {
        if (!pathList.isEmpty()) {
            pathList.clear();
            notifyDataSetChanged();
        }
    }


    public void addFilteredImagesPosition(int position) {
        filteredImagesPositions.add(position);
    }

    public int getFilteredImagePositionAt(int position) {
        return filteredImagesPositions.get(position);
    }

    public void clearFilteredImagesPositions() {
        if (filteredImagesPositions.size() > 0)
            filteredImagesPositions.clear();
    }


    public void dismissCheckBoxes() {
        for (CheckBox checkBox : checkBoxesViews) {
            checkBox.setVisibility(View.GONE);

            if (checkBox.isChecked())
                checkBox.setChecked(false);
        }
    }

    public void showCheckBoxes() {
        for (CheckBox checkBox : checkBoxesViews)
            checkBox.setVisibility(View.VISIBLE);
    }


    public void addCheckBoxPosition(int position, int value) {
        checkBoxes.put(position, value);
    }

    public void removeCheckBoxPosition(int position) {
        checkBoxes.remove(position);
    }

    public int getSelectedCheckBoxPosition() {

        for (int i = 0; i < checkBoxes.size(); i++)
            if (checkBoxes.get(checkBoxes.keyAt(i)) != null)
                return checkBoxes.get(checkBoxes.keyAt(i));

        return -1;
    }

    public void clearCheckBoxes() {
        if (checkBoxes.size() > 0)
            checkBoxes.clear();
    }

    public int getCheckBoxesSize() {
        return checkBoxes.size();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageHolder holder;
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            view = inflater.inflate(R.layout.grid_image_favorites, parent, false);
            holder = new ImageHolder();
            holder.imageView = (ImageView) view.findViewById(R.id.ivIconn);
            holder.imageView.setLayoutParams(layout_param);
            view.setTag(holder);
        } else
            holder = (ImageHolder) view.getTag();

        // add the checkbox
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox_view);
        checkBoxesViews.add(checkBox);

        String path = pathList.get(position);

        Glide.with(context).load(Utilities.loadImageFromInternalStorage(context, path)).diskCacheStrategy(DiskCacheStrategy.RESULT).crossFade().centerCrop()
                .placeholder(R.drawable.border).error(R.drawable.border).into(holder.imageView);

        return view;
    }


    public static class ImageHolder {
        public ImageView imageView;
    }
}
