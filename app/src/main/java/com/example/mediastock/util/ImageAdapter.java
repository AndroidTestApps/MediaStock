package com.example.mediastock.util;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.mediastock.R;
import com.example.mediastock.beans.ImageBean;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<ImageBean> images;
    private final int width;


	public ImageAdapter(Context context, ArrayList<ImageBean> grid){
		this.images = grid;
		this.context = context;
        width = context.getResources().getDisplayMetrics().widthPixels;
	}

	@Override
	public int getCount() {
		return images.size();
	}

	@Override
	public Object getItem(int position) {
		return images.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;

        if(convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.grid_image, parent, false);
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            convertView.setTag(viewHolder);
        }else
            viewHolder = (ViewHolder) convertView.getTag();

		ImageBean item = images.get(position);

        viewHolder.ivIcon.setLayoutParams(new LinearLayout.LayoutParams(width / 3, width / 3));

        // get image
        Picasso.with(context).load(Uri.parse(item.getUrl())).resize(width/3,width/3).placeholder(R.drawable.border).centerCrop().into(viewHolder.ivIcon);

		return convertView;
	}

	private static class ViewHolder{
		ImageView ivIcon;
	}

}
