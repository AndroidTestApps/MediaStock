package com.example.mediastock.data;

import android.os.Parcel;
import android.os.Parcelable;

public class MusicBean extends Bean {
    public static final Parcelable.Creator<MusicBean> CREATOR =
            new Parcelable.Creator<MusicBean>() {
                public MusicBean createFromParcel(Parcel in) {
                    return new MusicBean(in);
                }

                public MusicBean[] newArray(int size) {
                    return new MusicBean[size];
                }
            };

	private String id;
	private String title;
	private String preview;
    private int pos;
    private String path;

    public MusicBean() {
    }

    public MusicBean(Parcel in) {
        readFromParcel(in);
    }

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
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
        dest.writeString(path);
    }

    private void readFromParcel(Parcel in) {
        preview = in.readString();
        id = in.readString();
        title = in.readString();
        path = in.readString();
    }
}
