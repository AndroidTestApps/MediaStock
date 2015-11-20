package com.example.mediastock.util;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mediastock.R;
import com.example.mediastock.data.MusicBean;
import com.example.mediastock.data.VideoBean;

import java.lang.ref.WeakReference;

public class MusicVideoAdapter extends AbstractMediaAdapter {
    private final Drawable icon_music;
    private final Drawable icon_video;
    private final boolean favorites;

    public MusicVideoAdapter(Context context, int type, boolean favorites) {
        super(context, type);

        this.favorites = favorites;
        icon_music = context.getResources().getDrawable(R.drawable.music);
        icon_video = context.getResources().getDrawable(R.drawable.video);
    }

    @Override
    public MediaHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item;

        if (favorites)
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_music_video_favorites, parent, false);
        else
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_music_video, parent, false);

        return new MusicVideoHolder(item, this);
    }

    @Override
    public void onBindViewHolder(MediaHolder holder, int position) {

        if (getType() == 1) {
            MusicBean item = (MusicBean) getBeanAt(position);
            ((MusicVideoHolder) holder).ivIcon.setImageDrawable(icon_music);
            ((MusicVideoHolder) holder).text.setText(item.getTitle());
        } else {
            VideoBean item_video = (VideoBean) getBeanAt(position);
            ((MusicVideoHolder) holder).ivIcon.setImageDrawable(icon_video);
            ((MusicVideoHolder) holder).text.setText(item_video.getDescription());
        }


        // scrolled to the bottom
        if (position >= getPageNumber() - 1)
            if (getLoadingType() == 1 || getLoadingType() == 2)
                getBottomListener().onBottomLoadMoreData(getLoadingType(), getPageNumber() + 30); // load more data
    }

    public static class MusicVideoHolder extends AbstractMediaAdapter.MediaHolder implements View.OnClickListener {
        public ImageView ivIcon;
        public TextView text;
        private WeakReference<MusicVideoAdapter> ref;

        public MusicVideoHolder(View itemView, MusicVideoAdapter adapter) {
            super(itemView);

            ref = new WeakReference<>(adapter);
            ivIcon = (ImageView) itemView.findViewById(R.id.ivIcon_music_video);
            text = (TextView) itemView.findViewById(R.id.textView_grid_music_video);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (ref.get().getItemClickListener() != null)
                ref.get().getItemClickListener().onItemClick(v, getAdapterPosition());
        }
    }
}
