package com.example.mediastock.data;

import android.os.Parcel;

public class MusicBean extends Bean {

	private String id;
	private String title;
	private String preview;
	

	public String getPreview() {
		return preview;
	}
	public void setPreview(String preview) {
		this.preview = preview;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(preview);
		dest.writeString(id);
		dest.writeString(title);
	}
}
