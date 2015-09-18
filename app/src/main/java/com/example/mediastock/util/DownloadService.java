package com.example.mediastock.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Iterator;

import com.example.mediastock.activities.FilterImageActivity;
import com.example.mediastock.activities.FilterMusicActivity;
import com.example.mediastock.activities.ImageGaleryActivity;
import com.example.mediastock.activities.MusicGaleryActivity;
import com.example.mediastock.beans.ImageBean;
import com.example.mediastock.beans.MusicBean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;

/**
 * Class to fetch the data from the server.
 * 
 * @author Dinu
 */
public class DownloadService extends IntentService {
	public static final String IMG_BEAN = "ibean";
	public static final String MUSIC_BEAN = "mbean";
	private ResultReceiver receiver;
	public static Context context;
	private int type;

	public DownloadService() {
		super("DownloadService");
	}

	/** 
	 * This is called asynchronously by Android. It downloads the images or music.
	 */	
	@Override
	protected void onHandleIntent(Intent intent) {
		// get the right receiver
		receiver = getReceiver(intent);

		// get the right url
		String url = sortQuery(intent);

		// get images
		if(type == 1){

			// update the UI with the progress dialog
			publishImageResult(null, 2, receiver);

			getImages(url, receiver);

			// or get music
		}else{

			// update the UI with the progress dialog
			publishMusicResult(null, 2, receiver);

			getMusic(url, receiver);
		}
	}


	/**
	 * Method to decide which receiver we are using. The receiver can be for the image context or music.
	 * 
	 * @param intent the intent
	 * @return the right receiver used for this service. 
	 */
	private ResultReceiver getReceiver(Intent intent){

		if(intent.getParcelableExtra(ImageGaleryActivity.IMG_RECEIVER) != null){
			type = 1;
			return intent.getParcelableExtra(ImageGaleryActivity.IMG_RECEIVER);
		}else if(intent.getParcelableExtra(MusicGaleryActivity.MUSIC_RECEIVER) != null){
			type = 2;
			return intent.getParcelableExtra(MusicGaleryActivity.MUSIC_RECEIVER);
		}else
			return null;
	}

	/**
	 * Method to prepare the url with the parameters.
	 * @param intent
	 * @return
	 */
	private String sortQuery(Intent intent){
		Bundle bundle = intent.getExtras();

		if(type == 1){
			String url = "https://@api.shutterstock.com/v2/images/search?safe=true";		

			url += "&per_page=" + bundle.getString(FilterImageActivity.PER_PAGE);
			url += "&category=" + bundle.getString(FilterImageActivity.CATEGORY);
			url += "&sort=" + bundle.getString(FilterImageActivity.SORT_BY).toLowerCase();

			if(!bundle.getString(FilterImageActivity.ORIENTATION).equals("All"))
				url += "&orientation=" + bundle.getString(FilterImageActivity.ORIENTATION).toLowerCase();

			return url;

		}else{
			String url = "https://@api.shutterstock.com/v2/audio/search?safe=true";	

			if(!bundle.getString(FilterMusicActivity.ARTIST).isEmpty())
				url += "&artist=" + bundle.getString(FilterMusicActivity.ARTIST);

			if(!bundle.getString(FilterMusicActivity.TITLE).isEmpty())
				url += "&title=" + bundle.getString(FilterMusicActivity.TITLE);

			if(!bundle.getString(FilterMusicActivity.GENRE).equals("None"))
				url += "&genre=" + bundle.getString(FilterMusicActivity.GENRE);

			url += "&per_page=" + bundle.getString(FilterMusicActivity.PER_PAGE);

			return url;
		}
	}


	/**
	 * Method which downloads the images and the info.
	 * 
	 * @param urlStr the URL
	 * @param receiver the result receiver
	 * @throws IOException
	 */
	private void getImages(String urlStr, ResultReceiver receiver){
		InputStream is = null;

		try {
			URL url = new URL(urlStr);
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("Authorization", "Basic " + Utilities.getKeyLicense());
			is = conn.getInputStream();

			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = Utilities.readAll(rd);

			JsonElement json = new JsonParser().parse(jsonText);
			JsonObject o = json.getAsJsonObject();
			JsonArray array = o.get("data").getAsJsonArray();

			JsonObject assets;
			JsonObject preview;

			Iterator<JsonElement> iterator = array.iterator();
			if(!iterator.hasNext()){
				publishImageResult(null, 3, receiver);
				return;
			}				

			while(iterator.hasNext()){
				JsonElement json2 = iterator.next();
				ImageBean ib = new ImageBean();

				assets = json2.getAsJsonObject().get("assets").getAsJsonObject();
				preview = assets.get("preview").getAsJsonObject();

				ib.setImage(Picasso.with(context).load(Uri.parse(preview.get("url").getAsString())).resize(100,100).get());
				//ib.setImage(Utilities.decodeBitmapFromUrl(preview.get("url").getAsString(), 100 ,100));
				ib.setDescription(json2.getAsJsonObject().get("description").getAsString());
				ib.setId(json2.getAsJsonObject().get("id").getAsInt());
				ib.setIdContributor(json2.getAsJsonObject().get("contributor").getAsJsonObject().get("id").getAsInt());
				ib.setUrl(preview.get("url").getAsString());

				// update UI
				publishImageResult(ib, 1, receiver);

			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(is !=null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Method which downloads the music from the server.
	 * 
	 * @param urlStr the URL
	 * @param receiver the result receiver
	 * @throws IOException
	 */
	private void getMusic(String urlStr, ResultReceiver receiver){
		InputStream is = null;

		try {
			URL url = new URL(urlStr);
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("Authorization", "Basic " + Utilities.getKeyLicense());
			is = conn.getInputStream();
	
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = Utilities.readAll(rd);

			JsonElement json = new JsonParser().parse(jsonText);
			JsonObject o = json.getAsJsonObject();
			JsonArray array = o.get("data").getAsJsonArray();

			if(array.size() < 1){
				publishMusicResult(null, 3, receiver);
				return;
			}

			Iterator<JsonElement> iterator = array.iterator();
			while(iterator.hasNext()){
				JsonElement json2 = iterator.next();
				JsonObject ob = json2.getAsJsonObject();

				String id = ob.get("id").getAsString();		
				String title = ob.get("title").getAsString();
				JsonObject assets = ob.get("assets").getAsJsonObject();
				String preview = assets.get("preview_mp3").getAsJsonObject().get("url").getAsString();

				MusicBean mBean = new MusicBean(); 
				mBean.setId(id);
				mBean.setPreview(preview);
				mBean.setTitle(title);

				// update the UI
				publishMusicResult(mBean, 1, receiver);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(is != null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Method to update the main UI with the music
	 * 
	 * @param bean music bean
	 * @param result the result of the download operation. 1-success, 2-update with progress dialog 3-failure
	 * @param receiver the result receiver
	 */
	private void publishMusicResult(MusicBean bean, int result, ResultReceiver receiver) {
		Bundle bundle = new Bundle();
		bundle.putParcelable(MUSIC_BEAN, bean);
		receiver.send(result, bundle);
	}


	/**
	 * Method to update the main UI with the image
	 * 
	 * @param bean image bean
	 * @param result the result of the download operation. 1-success, 2-update with progress dialog 3-failure
	 * @param receiver the result receiver
	 */
	private void publishImageResult(ImageBean bean, int result, ResultReceiver receiver) {
		Bundle bundle = new Bundle();
		bundle.putParcelable(IMG_BEAN, bean);
		receiver.send(result, bundle);
	}

}
