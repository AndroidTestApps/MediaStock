package com.example.mediastock.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.mediastock.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class FavoriteImageAdapter extends RecyclerView.Adapter<FavoriteImageAdapter.ViewHolder> {
    private final int width;
    private final Context context;
    private final RelativeLayout.LayoutParams layout_param;
    private final ArrayList<Bitmap> list = new ArrayList<>();
    private OnImageClickListener image_listener;


    public FavoriteImageAdapter(final Context context) {
        this.context = context;
        this.width = context.getResources().getDisplayMetrics().widthPixels;
        this.layout_param = new RelativeLayout.LayoutParams(width / 2, width / 2);
        this.layout_param.setMargins(3, 2, 0, 2);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_image_favorites, parent, false);

        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Bitmap bitmap = list.get(position);
        holder.ivIcon.setImageBitmap(bitmap);
        holder.ivIcon.setTag(position);
        holder.ivIcon.setTag(bitmap);
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public void addItem(Bitmap image, int pos) {
        list.add(pos, image);
        notifyItemInserted(pos);
    }

    public void deleteItems() {
        if (!list.isEmpty()) {
            list.clear();
            notifyDataSetChanged();
        }
    }

    public void deleteItemAt(int position) {
        list.remove(position);
        notifyItemRemoved(position);
    }

    public void setOnImageClickListener(final OnImageClickListener listener) {
        this.image_listener = listener;
    }

    /**
     * Interface used to register a callback when a click on the image is triggered
     */
    public interface OnImageClickListener {
        void onImageClick(View view, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
            if (ref.get().image_listener != null)
                ref.get().image_listener.onImageClick(v, getAdapterPosition());
        }
    }
}
