package com.example.mediastock.util;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class DownloadResultReceiver extends ResultReceiver {
	private Receiver receiver;

	public DownloadResultReceiver(Handler handler) {
		super(handler);
	}

	public void setReceiver(Receiver receiver) {
		this.receiver = receiver;
	}

	public interface Receiver {
		void onReceiveResult(int resultCode, Bundle resultData);
	}

	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData) {
		if (this.receiver != null) {
			this.receiver.onReceiveResult(resultCode, resultData);
		}
	}
}
