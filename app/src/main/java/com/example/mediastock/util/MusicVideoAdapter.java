package com.example.mediastock.util;

import java.util.ArrayList;

import com.example.mediastock.R;
import com.example.mediastock.beans.*;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MusicVideoAdapter extends BaseAdapter {
	private ArrayList<Bean> list;
	private Context context;
	private int type;

	/**
	 * The constructor.
	 * 
	 * @param context the context
	 * @param list a list of beans
	 * @param type 1 represents MusicBean , 2 represents VideoBean
	 */
	public MusicVideoAdapter(Context context, ArrayList<Bean> list, int type){
		this.list = list;
		this.context = context;
		this.type = type;
	}

	@Override
	public int getCount() {
		return this.list.size();
	}

	@Override
	public Object getItem(int position) {
		return this.list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		ViewHolder viewHolder;

		if(convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(context);					
			convertView = inflater.inflate(R.layout.grid_music_video, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon_music_video);
			viewHolder.text = (TextView) convertView.findViewById(R.id.textView_grid_music_video);
			convertView.setTag(viewHolder);

		} else 
			// recycle the already inflated view 
			viewHolder = (ViewHolder) convertView.getTag();

		// music
		if(type == 1){
			MusicBean item = (MusicBean) list.get(position);

			@SuppressWarnings("deprecation")
			Drawable icon = context.getResources().getDrawable(R.drawable.music);
			viewHolder.ivIcon.setImageDrawable(icon);
			viewHolder.text.setText(item.getTitle());
			viewHolder.text.setTag(item.getPreview());
			
		// video
		}else{
			VideoBean item = (VideoBean)list.get(position);
			
			@SuppressWarnings("deprecation")
			Drawable icon = context.getResources().getDrawable(R.drawable.video);
			viewHolder.ivIcon.setImageDrawable(icon);
			viewHolder.text.setText(item.getDescription());	
			viewHolder.text.setTag(item.getPreview());
		}

		return convertView;
	}

	private static class ViewHolder{
		ImageView ivIcon;
		TextView text;
	}
}
