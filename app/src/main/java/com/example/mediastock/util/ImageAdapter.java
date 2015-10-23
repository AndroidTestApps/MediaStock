package com.example.mediastock.util;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.mediastock.R;
import com.example.mediastock.data.ImageBean;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyHolder> {
    private final int width;
    private final int type;
    private ArrayList<ImageBean> images = new ArrayList<>();
    private LinearLayout.LayoutParams layout_param;
    private OnImageClickListener listener;
    private RecyclerView recyclerView;
    private Context activity;


    public ImageAdapter(Context context, int type, RecyclerView recyclerView) {
        this.type = type;
        this.recyclerView = recyclerView;
        activity = context;
        width = context.getResources().getDisplayMetrics().widthPixels;

        if (type == 2) {

            layout_param = new LinearLayout.LayoutParams(recyclerView.getLayoutParams().height, recyclerView.getLayoutParams().height);
            layout_param.setMargins(0, 0, 3, 0);

        } else
            layout_param = new LinearLayout.LayoutParams(width / 2, width / 2);
            layout_param.setMargins(1, 1, 1, 0);
    }

    public void setOnImageClickListener(final OnImageClickListener listener) {
        this.listener = listener;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_image, parent, false);

        return new MyHolder(item, this);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        ImageBean item = images.get(position);
        holder.ivIcon.setLayoutParams(layout_param);

        if (item.getUrl() != null)
            if (type == 2)
                Picasso.with(activity).load(Uri.parse(item.getUrl())).resize(recyclerView.getLayoutParams().height, recyclerView.getLayoutParams().height).placeholder(R.drawable.border).centerCrop().into(holder.ivIcon);
            else
                Picasso.with(activity).load(Uri.parse(item.getUrl())).resize(width / 2, width / 2).placeholder(R.drawable.border).centerCrop().into(holder.ivIcon);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public ImageBean getBeanAt(int position) {
        return images.get(position);
    }

    public void addItem(ImageBean bean) {
        images.add(bean);
        notifyDataSetChanged();
    }

    public void deleteItems() {
        if (!images.isEmpty()) {
            images.clear();
            notifyDataSetChanged();
        }
    }

    public interface OnImageClickListener {
        void onImageClick(View view, int position);
    }

    protected static class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected ImageView ivIcon;
        private WeakReference<ImageAdapter> ref;

        public MyHolder(View itemView, ImageAdapter adapter) {
            super(itemView);

            ref = new WeakReference<>(adapter);
            ivIcon = (ImageView) itemView.findViewById(R.id.ivIcon);
            ivIcon.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (ref.get().listener != null)
                ref.get().listener.onImageClick(v, getAdapterPosition());
        }
    }
}
