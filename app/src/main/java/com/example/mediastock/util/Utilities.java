
package com.example.mediastock.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.mediastock.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.apache.commons.codec.binary.Base64.encodeBase64;


public class Utilities {


    /**
	 * It reads the content of the input stream.
     *
	 * @return the string from the stream
	 */
	public static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		
		return sb.toString();
	}

    /**
     * Method to get the date.
     *
     * @param day the day of a certain day
     * @return a date string
     */
    public static String getDate(int day){
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -day);

        Date d = calendar.getTime();

        return ft.format(d);
    }

    /**
     * It returns the license key of the shutterstock platform.
     */
    public static String getLicenseKey(){
		String authString = "3bcee2a0f5bc8879f49f:3a24e63a54e171fe231ea2c777ea2dd21884c072";
        byte[] authEncBytes = encodeBase64(authString.getBytes());
        String authStringEnc = new String(authEncBytes);
	
		return authStringEnc;
    }

    /**
     * Convert from bitmap to byte array
     */
    public static byte[] convertToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);

        Log.i("db", "encode -> " + String.valueOf(stream.size()));

        return stream.toByteArray();
    }

    /**
     * Convert from byte array to bitmap and then to drawable
     */
    public static Bitmap convertToBitmap(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    /**
     * Method to read the bytes from the stream.
     *
     * @param inputStream the stream
     *
     * @return a byte array of the stream
     * @throws IOException
     */
    public static byte[] covertStreamToByte(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        Log.i("db", " -> " + String.valueOf(byteBuffer.size()));

        return byteBuffer.toByteArray();
    }


    /**
     * Checks if the device is connected to the Internet
     *
     * @return true if connected, false otherwise
     */
    public static boolean deviceOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }


    /**
     * Method used for the floating action button behaviour.
     */
    public static int getToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        return toolbarHeight;
    }

    /**
     * Method used for the floating action button behaviour.
     */
    public static int getTabsHeight(Context context) {
        return (int) context.getResources().getDimension(R.dimen.tabsHeight);
    }


    /**
     * It displays the keyboard.
     */
    public static void showKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     * It removes the keyboard.
     */
    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
