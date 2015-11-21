package com.example.mediastock.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.mediastock.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class FavoriteImageAdapter extends AbstractMediaAdapter {
    private final RelativeLayout.LayoutParams layout_param;
    private final ArrayList<Bitmap> bitmaps = new ArrayList<>();

    public FavoriteImageAdapter(final Context context) {
        super(context, 0);

        this.layout_param = new RelativeLayout.LayoutParams(getWidth() / 2, getWidth() / 2);
        this.layout_param.setMargins(3, 2, 0, 2);
    }

    @Override
    public AbstractMediaAdapter.MediaHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.grid_image_favorites, parent, false);

        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(MediaHolder holder, int position) {
        ((ViewHolder) holder).ivIcon.setImageBitmap(bitmaps.get(position));
        ((ViewHolder) holder).ivIcon.setTag(position);
    }

    @Override
    public int getItemCount() {
        return bitmaps.size();
    }

    public void addBitmap(Bitmap image, int position) {
        bitmaps.add(position, image);

        // Android dev said it is a bug on the RecyclerView invoking notifyItemInserted() at pos 0
        if (position != 0)
            notifyItemInserted(position);
        else
            notifyDataSetChanged();
    }


    public Bitmap getBitmapAt(int pos) {
        return bitmaps.get(pos);
    }

    public void deleteBitmapAt(int position) {
        bitmaps.remove(position);

        // Android dev said it is a bug on the RecyclerView invoking notifyItemInserted() at pos 0
        if (position != 0)
            notifyItemRemoved(position);
        else
            notifyDataSetChanged();
    }


    public static class ViewHolder extends AbstractMediaAdapter.MediaHolder {
        public ImageView ivIcon;
        private WeakReference<FavoriteImageAdapter> ref;

        public ViewHolder(View itemView, FavoriteImageAdapter adapter) {
            super(itemView);

            ref = new WeakReference<>(adapter);
            ivIcon = (ImageView) itemView.findViewById(R.id.ivIconn);
            ivIcon.setLayoutParams(ref.get().layout_param);
            ivIcon.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (ref.get().getItemClickListener() != null)
                ref.get().getItemClickListener().onItemClick(v, getAdapterPosition());
        }
    }
}
