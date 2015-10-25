package com.example.mediastock.data;

import android.os.Parcel;
import android.os.Parcelable;

public class ImageBean extends Bean{
    public static final Parcelable.Creator<ImageBean> CREATOR =
            new Parcelable.Creator<ImageBean>() {
                public ImageBean createFromParcel(Parcel in) {
                    return new ImageBean(in);
                }

                public ImageBean[] newArray(int size) {
                    return new ImageBean[size];
                }
            };

    private int  id;
	private String description;
	private String url;
	private int idContributor;

	public ImageBean(){}

	public ImageBean(Parcel in) {
		readFromParcel(in);
	}

	public int getId() {
		return id;
	}

    public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getIdContributor() {
		return idContributor;
	}

	public void setIdContributor(int idContributor) {
		this.idContributor = idContributor;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(idContributor);
		dest.writeString(description);
		dest.writeString(url);
	}

	private void readFromParcel(Parcel in) {
		id = in.readInt();
		idContributor = in.readInt();
		description = in.readString();
		url = in.readString();
	}
}
