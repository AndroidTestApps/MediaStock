package com.example.mediastock.data;

import android.os.Parcel;
import android.os.Parcelable;

public class VideoBean extends Bean {
    public static final Parcelable.Creator<VideoBean> CREATOR =
            new Parcelable.Creator<VideoBean>() {
                public VideoBean createFromParcel(Parcel in) {
                    return new VideoBean(in);
                }

                public VideoBean[] newArray(int size) {
                    return new VideoBean[size];
                }
            };

    private String id;
    private String description;
	private String preview;
    private int pos;
    private String path;

    public VideoBean() {
    }

    public VideoBean(Parcel in) {
        readFromParcel(in);
    }

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
    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(preview);
        dest.writeString(id);
        dest.writeString(description);
        dest.writeString(path);
    }

    private void readFromParcel(Parcel in) {
        preview = in.readString();
        id = in.readString();
        description = in.readString();
        path = in.readString();
    }
}
