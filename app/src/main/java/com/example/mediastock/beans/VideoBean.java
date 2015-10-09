package com.example.mediastock.beans;

import android.os.Parcel;

public class VideoBean extends Bean {	
	private String id;
	private String description;
	private String preview;
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPreview() {
		return preview;
	}
	public void setPreview(String preview) {
		this.preview = preview;
	}
	
	
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(preview);
		dest.writeString(id);
		dest.writeString(description);
		
	}

}
