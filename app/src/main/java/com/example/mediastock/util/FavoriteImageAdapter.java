package com.example.mediastock.util;


import android.app.Activity;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.mediastock.R;

import java.util.ArrayList;

public class FavoriteImageAdapter extends BaseAdapter {
    private final ArrayList<Integer> filteredImagesPositions = new ArrayList<>();
    private final SparseArray<Integer> selectedImages = new SparseArray<>();
    private final ArrayList<String> pathList = new ArrayList<>();
    private final RelativeLayout.LayoutParams layout_param;
    private final int width;
    private final Context context;


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


    public void addSelectedImagePosition(int position, int value) {
        selectedImages.put(position, value);
    }

    public void removeSelectedImagePosition(int position) {
        selectedImages.remove(position);
    }

    public int getSelectedImagePosition() {

        for (int i = 0; i < selectedImages.size(); i++)
            if (selectedImages.get(selectedImages.keyAt(i)) != null)
                return selectedImages.get(selectedImages.keyAt(i));

        return -1;
    }

    public void clearSelectedImages() {
        if (selectedImages.size() > 0)
            selectedImages.clear();
    }

    public int getSelectedImagesSize() {
        return selectedImages.size();
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

        String path = pathList.get(position);

        Glide.with(context).load(Utilities.loadImageFromInternalStorage(context, path)).diskCacheStrategy(DiskCacheStrategy.RESULT).crossFade().centerCrop()
                .placeholder(R.drawable.border).error(R.drawable.border).into(holder.imageView);

        return view;
    }


    public static class ImageHolder {
        public ImageView imageView;
    }
}
