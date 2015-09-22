package com.example.mediastock.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.example.mediastock.R;
import com.example.mediastock.beans.ImageBean;
import com.example.mediastock.util.DownloadResultReceiver;
import com.example.mediastock.util.DownloadService;
import com.example.mediastock.util.ImageAdapter;
import com.example.mediastock.util.Utilities;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;


/**
 * Activity which displays a gridView with images.
 * 
 * @author Dinu
 */
public class ImageGaleryActivity extends BaseActivity implements DownloadResultReceiver.Receiver {
	private int counter = 0;
	public static final String IMG_RECEIVER = "ireceiver";
	private ImageAdapter imgAdapter;
	private ArrayList<ImageBean> images = new ArrayList<>();
	private DownloadResultReceiver resultReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// check if online
		if(!isOnline()){
			setContentView(R.layout.no_internet);
			Toast.makeText(this, "Not online", Toast.LENGTH_SHORT).show();
		}else{

			setContentView(R.layout.activity_image_galery);

            showProgressDialog();

			GridView grid = (GridView) this.findViewById(R.id.gridView_displayImage);
			imgAdapter = new ImageAdapter(getApplicationContext(), images);
			grid.setAdapter(imgAdapter);
			grid.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> arg0, View v, int pos, long arg3) {
					goToDisplayImageActivity(images.get(pos));			
				}		
			});

			// we search the users input	
			if(search())
				searchForImages();

			// we do a filter search
			else if(filterSearch())
				startFilterSearch();

			// we fetch the recent images
			else
				new WebRequest(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "getImages");			
		}
	}


	/**
	 * We pass all the info to DownloadService service to start to download the images. 
	 */
	public void startFilterSearch() {
		this.dismissProgressDialog();

		Bundle bundle = getIntent().getExtras();

		resultReceiver = new DownloadResultReceiver(new Handler());
		resultReceiver.setReceiver(this);
		Intent intent = new Intent(Intent.ACTION_SYNC, null, getApplicationContext(), DownloadService.class);
		DownloadService.context = getApplicationContext();

		// query info
		intent.putExtra(IMG_RECEIVER, resultReceiver);	
		intent.putExtra(FilterImageActivity.CATEGORY, bundle.getString(FilterImageActivity.CATEGORY));
		intent.putExtra(FilterImageActivity.ORIENTATION, bundle.getString(FilterImageActivity.ORIENTATION));
		intent.putExtra(FilterImageActivity.SORT_BY, bundle.getString(FilterImageActivity.SORT_BY));
		intent.putExtra(FilterImageActivity.PER_PAGE, bundle.getString(FilterImageActivity.PER_PAGE));

		startService(intent);
	}


	/**
	 * Method to see if we have to do a search
	 * 
	 * @return true if we have to do a search false otherwise
	 */
	private boolean search(){
		return getIntent().getBooleanExtra("search", false);
	}

	/**
	 * Method to see if we have to do a filter search
	 * 
	 * @return true if we have to do a filter search false otherwise
	 */
	private boolean filterSearch(){
		return getIntent().getBooleanExtra(FilterImageActivity.FILTER_SEARCH, false);
	}


	/**
	 * Method which searches the images by one or two keys.
	 */
	private void searchForImages(){
		boolean twoWords = getIntent().getBooleanExtra("twoWords", false);

		if(twoWords){
			String key1 = getIntent().getStringExtra("key1");
			String key2 = getIntent().getStringExtra("key2");

			// get images by two different keys
			new WebRequest(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "search", key1);			
			new WebRequest(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "search", key2);

		}else{
			String key = getIntent().getStringExtra("key");

			// get images by one key
			new WebRequest(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "search", key);
		}
	}


	/**
	 * Method which redirects the user to DisplayImageActivity 
	 * 
	 * @param bean the image bean
	 */
	private void goToDisplayImageActivity(ImageBean bean) {
		Intent intent = new Intent(getApplicationContext(), DisplayImageActivity.class);

		intent.putExtra("id", bean.getId());
		intent.putExtra("description", bean.getDescription());
		intent.putExtra("url", bean.getUrl());
		intent.putExtra("contributor", bean.getIdContributor());

		startActivity(intent);
	}


	/**
	 * Static inner class to search for images and to get the recent images from the server.  
	 * 
	 * @author Dinu
	 */
	private static class WebRequest extends AsyncTask<String, ImageBean, String>{
		private static WeakReference<ImageGaleryActivity> activity;
		private boolean searchSuccess = true;

		public WebRequest(ImageGaleryActivity activity){
			WebRequest.activity = new WeakReference<>(activity);
		}

		@Override
		protected String doInBackground(String... params) {

			if(params[0].equals("getImages"))
				getRecentImages(0);
			else{
				searchImagesByKey(params[1]);
				return params[1];
			}
			return null;
		}

		/**
		 * Method to search images by a key
		 *
		 * @param key the key
		 */
		private void searchImagesByKey(String key){
			String urlStr = "https://@api.shutterstock.com/v2/images/search?per_page=24&query=";
			urlStr += key;

			InputStream is = null;

			try {
				URL url = new URL(urlStr);
				URLConnection conn = url.openConnection();
				conn.setRequestProperty("Authorization", "Basic " + Utilities.getLicenseKey());
				is = conn.getInputStream();

				BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
				String jsonText = Utilities.readAll(rd);

				JsonElement json = new JsonParser().parse(jsonText);
				JsonObject o = json.getAsJsonObject();
				JsonArray array = o.get("data").getAsJsonArray();

				JsonObject assets;
				JsonObject preview;

				if(array.size() < 1){
					searchSuccess = false;
					return;
				}				

				Iterator<JsonElement> iterator = array.iterator();
				while(iterator.hasNext()){
					JsonElement json2 = iterator.next();
					ImageBean ib = new ImageBean();

					assets = json2.getAsJsonObject().get("assets").getAsJsonObject();
					preview = assets.get("preview").getAsJsonObject();

					//ib.setImage(Picasso.with(activity.get()).load(Uri.parse(preview.get("url").getAsString())).resize(100,100).get());
					//ib.setImage(Utilities.decodeBitmapFromUrl(preview.get("url".getAsString(), 100 ,100));
					ib.setDescription(json2.getAsJsonObject().get("description").getAsString());
					ib.setId(json2.getAsJsonObject().get("id").getAsInt());
					ib.setIdContributor(json2.getAsJsonObject().get("contributor").getAsJsonObject().get("id").getAsInt());
					ib.setUrl(preview.get("url").getAsString());

					// update UI
					publishProgress(ib);

				}
			} catch (IOException e){
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
		 * Method to get the recent images.
		 * We get the image, description of the image, id of the image, url for the preview, and id of the contributor. 
		 *
		 * @throws IOException
		 */
		@SuppressWarnings("deprecation")
		private void getRecentImages(int day){
			Date d = new Date();	
			SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM");
			String date = ft.format(d) + "-" + (d.getDate() - day);

			String urlStr = "https://@api.shutterstock.com/v2/images/search?per_page=15&added_date=";
			urlStr += date;

			InputStream is = null;

			try {
				URL url = new URL(urlStr);
				URLConnection conn = url.openConnection();
				conn.setRequestProperty("Authorization", "Basic " + Utilities.getLicenseKey());
				is = conn.getInputStream();

				BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
				String jsonText = Utilities.readAll(rd);

				JsonElement json = new JsonParser().parse(jsonText);
				JsonObject o = json.getAsJsonObject();
				JsonArray array = o.get("data").getAsJsonArray();

				if(array.size() == 0){
					getRecentImages(activity.get().counter++);
					return;
				}
				
				JsonObject assets;
				JsonObject preview;

				Iterator<JsonElement> iterator = array.iterator();
				while(iterator.hasNext()){
					JsonElement json2 = iterator.next();
					ImageBean ib = new ImageBean();

					assets = json2.getAsJsonObject().get("assets").getAsJsonObject();
					preview = assets.get("preview").getAsJsonObject();

					//ib.setImage(Picasso.with(activity.get()).load(Uri.parse(preview.get("url").getAsString())).resize(100,100).get());
					//ib.setImage(Utilities.decodeBitmapFromUrl(preview.get("url").getAsString(), 100 ,100));
					ib.setId(json2.getAsJsonObject().get("id").getAsInt());
					ib.setDescription(json2.getAsJsonObject().get("description").getAsString());
					ib.setIdContributor( json2.getAsJsonObject().get("contributor").getAsJsonObject().get("id").getAsInt());
					ib.setUrl(preview.get("url").getAsString());

					// update UI
					publishProgress(ib);

				}
			} catch (IOException e) {
				getRecentImages(activity.get().counter++);
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
		 * We update the UI
		 */
		@Override
		protected void onProgressUpdate(ImageBean...bean) {
			if(activity.get().isProgressDilaogOn())
				activity.get().dismissProgressDialog();
		
			if(bean[0] == null)
				Toast.makeText(activity.get(), "No image was found",Toast.LENGTH_SHORT).show();
			else{
				activity.get().images.add(bean[0]);
				activity.get().imgAdapter.notifyDataSetChanged();
			}
		}


		@Override
		protected void onPostExecute(String result){
            if(activity.get().isProgressDilaogOn())
                activity.get().dismissProgressDialog();

			if(!searchSuccess)
				Toast.makeText(activity.get(), "Sorry, no image with " + result + " was found!", Toast.LENGTH_LONG).show();
		}
	}



	/**
	 * It handles the result of the DownloadService service.
	 * Method used when we do a filter search. It updates the UI.
	 */
	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		switch(resultCode){
		case 1:
			this.dismissProgressDialog();
			ImageBean bean = (ImageBean) resultData.getParcelable(DownloadService.IMG_BEAN);

			// update UI with the image
			images.add(bean);
			imgAdapter.notifyDataSetChanged();
			break;

		case 2:
			this.showProgressDialog();
			break;

		default:
			Toast.makeText(getApplicationContext(), "Search failed",Toast.LENGTH_LONG).show();
			break;
		}
	}



}
