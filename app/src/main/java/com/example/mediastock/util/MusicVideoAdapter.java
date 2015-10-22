package com.example.mediastock.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mediastock.R;
import com.example.mediastock.data.Bean;
import com.example.mediastock.data.MusicBean;
import com.example.mediastock.data.VideoBean;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MusicVideoAdapter extends RecyclerView.Adapter<MusicVideoAdapter.MyHolder> {
    private final int type;
    private final Drawable icon_music;
    private final Drawable icon_video;
    private ArrayList<Bean> list = new ArrayList<>();
    private OnMediaItemClickListener listener;


    public MusicVideoAdapter(Context context, int type) {
        this.type = type;

        icon_music = context.getResources().getDrawable(R.drawable.music);
        icon_video = context.getResources().getDrawable(R.drawable.video);

        this.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
            }
        });
    }

    public void setOnMediaItemClickListener(final OnMediaItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_music_video, parent, false);

        return new MyHolder(item, this);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        if (type == 1) {
            MusicBean item = (MusicBean) list.get(position);
            holder.ivIcon.setImageDrawable(icon_music);
            holder.text.setText(item.getTitle());
        } else {
            VideoBean item_video = (VideoBean) list.get(position);
            holder.ivIcon.setImageDrawable(icon_video);
            holder.text.setText(item_video.getDescription());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void addItem(Bean bean) {
        list.add(bean);
        notifyDataSetChanged();
    }

    public void deleteItems() {
        if (!list.isEmpty()) {
            list.clear();
            notifyDataSetChanged();
        }
    }

    public Bean getItemAt(int position) {
        return list.get(position);
    }

    public interface OnMediaItemClickListener {
        void onItemClick(View view, int position);
    }

    public static class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected ImageView ivIcon;
        protected TextView text;
        private WeakReference<MusicVideoAdapter> ref;

        public MyHolder(View itemView, MusicVideoAdapter adapter) {
            super(itemView);

            ref = new WeakReference<>(adapter);
            ivIcon = (ImageView) itemView.findViewById(R.id.ivIcon_music_video);
            text = (TextView) itemView.findViewById(R.id.textView_grid_music_video);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (ref.get().listener != null)
                ref.get().listener.onItemClick(v, getAdapterPosition());
        }
    }
}
