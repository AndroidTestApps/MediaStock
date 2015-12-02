
package com.example.mediastock.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.mediastock.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import static org.apache.commons.codec.binary.Base64.encodeBase64;


public class Utilities {
    // media directories
    public static final String MUSIC_DIR = "music";
    public static final String VIDEO_DIR = "video";
    public static final String IMG_DIR = "image";

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
     * It saves the image into the internal storage of the app.
     *
     * @param context the context
     * @param bitmap the image
     * @param id the id of the image
     * @return the path of the image
     */
    public static String saveImageToInternalStorage(Context context, Bitmap bitmap, int id) {
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());

        File imageDir = cw.getDir("imageDir", Context.MODE_PRIVATE);

        // Create file image
        File file = new File(imageDir, "image" + id);

        FileOutputStream fos;
        try {

            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file.getName();
    }

    /**
     * Method to save a media file inside the directory typeDir. It writes the bytes of the stream into the file.
     *
     * @param type the directory type: music, video or image
     * @param context the context
     * @param inputStream the stream
     * @param id the id of the media file
     * @return the path of the media file
     */
    public static String saveMediaToInternalStorage(String type, Context context, InputStream inputStream, int id) {
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());

        File dir = cw.getDir(type + "Dir", Context.MODE_PRIVATE);

        // Create media file
        File file = new File(dir, type + id);

        FileOutputStream fos;
        try {

            fos = new FileOutputStream(file);
            fos.write(Utilities.covertStreamToByte(inputStream));

            fos.flush();
            fos.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file.getName();
    }


    /**
     * Method to delete a media file within the typeDir directory. The directory is stored into the internal storage.
     *
     * @param type    the directory type: music, video or image
     * @param context the context
     * @param path    the path of the media file
     * @return true, if the file was deleted, false otherwise
     */
    public static boolean deleteSpecificMediaFromInternalStorage(String type, Context context, String path) {
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        File dir = cw.getDir(type + "Dir", Context.MODE_PRIVATE);

        final File[] fileNames = dir.listFiles();

        boolean result = false;
        for (int i = 0; i < fileNames.length; i++)
            if (fileNames[i].getName().equals(path))
                result = fileNames[i].delete();

        return result;
    }

    /**
     * It loads from the storage an image
     *
     * @param context the context
     * @param path    the path of the image
     * @return the file containing the blob image
     */
    public static File loadImageFromInternalStorage(Context context, String path) {
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        File imageDir = cw.getDir("imageDir", Context.MODE_PRIVATE);

        final File[] fileNames = imageDir.listFiles();

        File target = null;
        for (int i = 0; i < fileNames.length; i++)
            if (fileNames[i].getName().equals(path))
                target = fileNames[i];

        return target;
    }

    /**
     * It loads a media file from the storage.
     *
     * @param type    the typeDir directory, where type can be music or video
     * @param context the context
     * @param path    the path to the media file
     * @return a FileInputStream object that contains the media file
     */
    public static FileInputStream loadMediaFromInternalStorage(String type, Context context, final String path){
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        File dir = cw.getDir(type + "Dir", Context.MODE_PRIVATE);

        final File[] fileNames = dir.listFiles();

        File target = null;
        for (int i = 0; i < fileNames.length; i++)
            if (fileNames[i].getName().equals(path))
                target = fileNames[i];

        Log.i("path", target.getName());

        FileInputStream fis = null;
        try {

            fis = new FileInputStream(target);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fis;
    }


    /**
     * Method to delete all the files within the directory typeDir, where type can be music, image or video
     *
     * @param type    the directory type: music, video or image
     * @param context the context
     */
    public static void deleteAllMediaFromInternalStorage(String type, Context context) {
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        File dir = cw.getDir(type + "Dir", Context.MODE_PRIVATE);

        final File[] fileNames = dir.listFiles();

        if (fileNames.length > 0) {
            for (int i = 0; i < fileNames.length; i++)
                fileNames[i].delete();
        }
    }

    /**
     * Find most represented swatch of the palette, based on population.
     */
    public static Palette.Swatch getDominantSwatch(Palette palette) {

        return Collections.max(palette.getSwatches(), new Comparator<Palette.Swatch>() {
            @Override
            public int compare(Palette.Swatch sw1, Palette.Swatch sw2) {
                return Integer.compare(sw1.getPopulation(), sw2.getPopulation());
            }
        });
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

        inputStream.close();

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
