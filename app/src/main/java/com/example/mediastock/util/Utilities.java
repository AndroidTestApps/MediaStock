
package com.example.mediastock.util;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import org.apache.commons.codec.binary.Base64;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;


public class Utilities {

	/**
	 * It reads the content of the input stream.
	 * @param rd
	 * @return
	 * @throws IOException
	 */
	public static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		
		return sb.toString();
	}


	public static String getLicenseKey(){
		String authString = "3bcee2a0f5bc8879f49f:3a24e63a54e171fe231ea2c777ea2dd21884c072";
		byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
		String authStringEnc = new String(authEncBytes);
	
		return authStringEnc;
	} 

	public static Bitmap decodeBitmapFromUrl(String imageUrl, int requiredWidth, int requiredHeight) throws IOException{
		URL url = new URL(imageUrl);

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(url.openConnection().getInputStream(),null, options);

		options.inJustDecodeBounds = false;
		options.inSampleSize = calculateInSampleSize(options, requiredWidth, requiredHeight);

		Bitmap img = BitmapFactory.decodeStream(url.openConnection().getInputStream(),null, options);

		return createScaledBitmap(img, requiredWidth, requiredHeight);

	}


	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;

		final float srcAspect = (float)width / (float)height;
		final float dstAspect = (float)reqWidth / (float)reqHeight;

		if (srcAspect > dstAspect) {
			return height / reqHeight;
		} else {
			return width / reqWidth;
		}
	}


	private static Bitmap createScaledBitmap(Bitmap unscaledBitmap, int dstWidth, int dstHeight) {
		Rect srcRect = calculateSrcRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(),
				dstWidth, dstHeight);

		Rect dstRect = calculateDstRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(),
				dstWidth, dstHeight);

		Bitmap scaledBitmap = Bitmap.createBitmap(dstRect.width(), dstRect.height(),Config.ARGB_8888);

		Canvas canvas = new Canvas(scaledBitmap);

		canvas.drawBitmap(unscaledBitmap, srcRect, dstRect, new Paint(Paint.FILTER_BITMAP_FLAG));

		return scaledBitmap;
	}

	public static Rect calculateSrcRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
		final float srcAspect = (float)srcWidth / (float)srcHeight;
		final float dstAspect = (float)dstWidth / (float)dstHeight;

		if (srcAspect > dstAspect) {
			final int srcRectWidth = (int)(srcHeight * dstAspect);
			final int srcRectLeft = (srcWidth - srcRectWidth) / 2;
			return new Rect(srcRectLeft, 0, srcRectLeft + srcRectWidth, srcHeight);
		} else {
			final int srcRectHeight = (int)(srcWidth / dstAspect);
			final int scrRectTop = (srcHeight - srcRectHeight) / 2;
			return new Rect(0, scrRectTop, srcWidth, scrRectTop + srcRectHeight);
		}
	}

	public static Rect calculateDstRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight){
		return new Rect(0, 0, dstWidth, dstHeight);
	}

}
