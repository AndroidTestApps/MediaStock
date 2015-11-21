package com.example.mediastock.util;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.mediastock.R;
import com.example.mediastock.data.ImageBean;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;


public class ImageAdapter extends AbstractMediaAdapter {
    private final RelativeLayout.LayoutParams layout_param;

    public ImageAdapter(Context context, int type) {
        super(context, type);

        if (type == 1) {
            layout_param = new RelativeLayout.LayoutParams(this.getWidth() / 2, this.getWidth() / 2);
            layout_param.setMargins(3, 2, 0, 2);

        } else {
            layout_param = new RelativeLayout.LayoutParams(this.getWidth() / 3, this.getWidth() / 3);
            layout_param.setMargins(0, 2, 2, 2);
        }
    }

    @Override
    public AbstractMediaAdapter.MediaHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_image, parent, false);

        return new ImageHolder(item, this);
    }

    @Override
    public void onBindViewHolder(AbstractMediaAdapter.MediaHolder holder, int position) {
        ImageBean item = (ImageBean) this.getBeanAt(position);

        if (item.getUrl() != null) {
            if (getType() == 2)
                Picasso.with(this.getContext()).load(Uri.parse(item.getUrl())).resize(this.getWidth() / 3, this.getWidth() / 3).placeholder(R.drawable.border).centerCrop().into(((ImageHolder) holder).ivIcon);
            else
                Picasso.with(this.getContext()).load(Uri.parse(item.getUrl())).resize(this.getWidth() / 2, this.getWidth() / 2).placeholder(R.drawable.border).centerCrop().into(((ImageHolder) holder).ivIcon);
        } else
            ((ImageHolder) holder).ivIcon.setBackgroundResource(R.drawable.border);


        // scrolled to the bottom
        if (position >= getPageNumber() - 1) {
            if (getLoadingType() == 1 || getLoadingType() == 2)
                getBottomListener().onBottomLoadMoreData(getLoadingType(), getPageNumber() + 50);  // load more data
        }
    }

    public static class ImageHolder extends AbstractMediaAdapter.MediaHolder {
        public ImageView ivIcon;
        private WeakReference<ImageAdapter> ref;

        public ImageHolder(View itemView, ImageAdapter adapter) {
            super(itemView);

            ref = new WeakReference<>(adapter);
            ivIcon = (ImageView) itemView.findViewById(R.id.ivIcon);
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
