package com.example.mediastock.util;

import java.util.ArrayList;
import com.example.mediastock.R;
import com.example.mediastock.beans.ImageBean;
import com.squareup.picasso.Picasso;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<ImageBean> images;


	public ImageAdapter(Context context, ArrayList<ImageBean> grid){
		this.images = grid;
		this.context = context;
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

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = new ViewHolder();

		LayoutInflater inflater = LayoutInflater.from(context);
		convertView = inflater.inflate(R.layout.grid_image, parent, false);
		viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
		convertView.setTag(viewHolder);

		// update the item view
		ImageBean item = images.get(position);

		if(item.getImage() == null){
			Picasso.with(context).load(Uri.parse(item.getUrl())).resize(100,100).into(viewHolder.ivIcon);
			viewHolder.ivIcon.setBackgroundResource(R.drawable.border);
		}else{
			viewHolder.ivIcon.setBackgroundResource(R.drawable.border);
			viewHolder.ivIcon.setImageBitmap(item.getImage());
		}

		return convertView;
	}

	private static class ViewHolder{
		ImageView ivIcon;
	}

}
