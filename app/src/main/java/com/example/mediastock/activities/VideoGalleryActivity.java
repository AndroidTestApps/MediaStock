package com.example.mediastock.activities;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mediastock.R;
import com.example.mediastock.beans.Bean;
import com.example.mediastock.beans.VideoBean;
import com.example.mediastock.util.Adapter;
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
 * Activity which displays a listView with videos.
 * 
 * @author Dinu
 */
public class VideoGalleryActivity extends BaseActivity implements LoaderCallbacks<Void>, OnItemClickListener{
	private int counter = 0;
	private Adapter videoAdapter;
	private ArrayList<Bean> videos = new ArrayList<>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// check if online
		if(!isOnline()){
			setContentView(R.layout.no_internet);
			Toast.makeText(this, "Not online", Toast.LENGTH_SHORT).show();

		}else{

			setContentView(R.layout.activity_music_video_galery);
            setTitle("Video gallery");

            showProgressDialog();

			// video list
			videoAdapter = new Adapter(this, videos, 2);
			ListView listViewVideo = (ListView) this.findViewById(R.id.list_music_video_galery);
			listViewVideo.setAdapter(videoAdapter);
			listViewVideo.setOnItemClickListener(this);

			// we search the users input
			if(search())
				searchForVideos();

			// we do a filter search
			else if(filterSearch())
				startFilterSearch();

			// we fetch the recent videos
			else
				getLoaderManager().initLoader(0, null, this);
		}
	}

	/**
	 * The method starts the loader with the bundle as info.
	 */
	private void startFilterSearch() {
		Bundle bundle = getIntent().getExtras();
		getLoaderManager().initLoader(5, bundle, this);	
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
		return getIntent().getBooleanExtra(FilterVideoActivity.FILTER_SEARCH, false);
	}

	/**
	 * Method which searches the video by one or two keys.
	 */
	private void searchForVideos(){
		boolean twoWords = getIntent().getBooleanExtra("twoWords", false);

		if(twoWords){
			String key1 = getIntent().getStringExtra("key1");
			String key2 = getIntent().getStringExtra("key2");

			Bundle bundle1 = new Bundle();
			Bundle bundle2 = new Bundle();

			bundle1.putString("key", key1);
			getLoaderManager().initLoader(1, bundle1, this);

			bundle2.putString("key", key2);
			getLoaderManager().initLoader(2, bundle2, this);

		}else{
			String key = getIntent().getStringExtra("key");
			Bundle bundle = new Bundle();
			bundle.putString("key", key);

			getLoaderManager().initLoader(4, bundle, this);
		}
	}


	/**
	 * Method to create a Loader.
	 */
	@Override
	public Loader<Void> onCreateLoader(int id, Bundle bundle) {
		AsyncTaskLoader<Void> data = null;
		switch(id){

		case 0: data = new LoadData(this, 0, null); break;
		case 1: case 2: case 3: case 4: data = new LoadData(this, 1, bundle); break;
		case 5: data = new LoadData(this, 2, bundle); break;

		default: break;
		}

		data.forceLoad();
		return data;
	}


	/**
	 * Listener on the item of the listView.
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
		TextView item = (TextView) view.findViewById(R.id.textView_grid_music_video);
		String description = item.getText().toString();
		String url = item.getTag().toString();

		Intent intent = new Intent(VideoGalleryActivity.this, VideoPlayerActivity.class);
		intent.putExtra("url", url);
		intent.putExtra("description", description);

		startActivity(intent);
	}



	/**
	 * Static inner class to search for videos, to get the recent videos and to do a filter search from the server.
	 * 
	 * @author Dinu
	 */
	private static class LoadData extends AsyncTaskLoader<Void> {
		private static WeakReference<VideoGalleryActivity> activity;
		private Bundle bundle;
		private int type;

		public LoadData(VideoGalleryActivity context, int type, Bundle bundle) {
			super(context);

			this.type = type;
			this.bundle = bundle;
			activity = new WeakReference<>( context);
		}

		@Override
		public Void loadInBackground() {

			switch(type){

			case 0: 			
				getRecentVideos(0);
				break;

			case 1:
				searchVideosByKey();
				break;

			case 2: 
				filterVideos();
				break;

			default: break;
			}

			return null;
		}

		private String parseUrlFilterSearch(){
			String url = "https://api.shutterstock.com/v2/videos/search?safe=true";
			String category = bundle.getString(FilterVideoActivity.CATEGORY);
			String word = bundle.getString(FilterVideoActivity.WORD);

			if(!category.isEmpty())			
				url += "&category=" + category.substring(0, 1).toUpperCase() + category.substring(1);

			if(!word.isEmpty())
				url += "&query=" + word;

			url += "&sort=" + bundle.getString(FilterVideoActivity.SORT).toLowerCase();
			url += "&per_page=" + bundle.getString(FilterVideoActivity.PER_PAGE);

			return url;
		}


		private void filterVideos(){
			String urlStr = parseUrlFilterSearch();

			InputStream is = null;

			try {
				URL url = new URL(urlStr);
				URLConnection conn = url.openConnection();
				conn.setRequestProperty("Authorization", "Basic " + Utilities.getLicenseKey());
				is = conn.getInputStream();
	
				String temp = "";
				int times = 1;

				BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
				String jsonText = Utilities.readAll(rd);

				JsonElement json = new JsonParser().parse(jsonText);
				JsonObject o = json.getAsJsonObject();
				JsonArray array = o.get("data").getAsJsonArray();


				if(array.size() < 1){
					runThread(null);
					return;
				}				

				Iterator<JsonElement> iterator = array.iterator();
				while(iterator.hasNext()){
					JsonElement json2 = iterator.next();
					JsonObject ob = json2.getAsJsonObject();

					String id = ob.get("id").getAsString();
					String description = ob.get("description").getAsString();		
					JsonObject assets = ob.get("assets").getAsJsonObject();
					String preview = assets.get("preview_mp4").getAsJsonObject().get("url").getAsString();

					final VideoBean vBean = new VideoBean();

					if(temp.length() == 0){
						temp = description;
						vBean.setDescription(description);

					}else{

						if(temp.equals(description)){
							StringBuilder s = new StringBuilder();
							s.append(description + " - " + times);
							vBean.setDescription(s.toString());

						}else{
							StringBuilder s = new StringBuilder();
							s.append(description + " - " + "1");
							vBean.setDescription(s.toString());
							temp = description;
							times = 1;
						}
					}

					vBean.setId(id);
					vBean.setPreview(preview);
					times++;

					runThread(vBean);
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

		private void searchVideosByKey(){
			String urlStr = "https://api.shutterstock.com/v2/videos/search?per_page=25&query=";
			urlStr += bundle.getString("key");

			InputStream is = null;

			try {
				URL url = new URL(urlStr);
				URLConnection conn = url.openConnection();
				conn.setRequestProperty("Authorization", "Basic " + Utilities.getLicenseKey());
				is = conn.getInputStream();

				String temp = "";
				int times = 1;

				BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
				String jsonText = Utilities.readAll(rd);

				JsonElement json = new JsonParser().parse(jsonText);
				JsonObject o = json.getAsJsonObject();
				JsonArray array = o.get("data").getAsJsonArray();

				if(array.size() < 1){
					runThread(null);
					return;
				}				

				Iterator<JsonElement> iterator = array.iterator();
				while(iterator.hasNext()){
					JsonElement json2 = iterator.next();
					JsonObject ob = json2.getAsJsonObject();

					String id = ob.get("id").getAsString();
					String description = ob.get("description").getAsString();		
					JsonObject assets = ob.get("assets").getAsJsonObject();
					String preview = assets.get("preview_mp4").getAsJsonObject().get("url").getAsString();

					final VideoBean vBean = new VideoBean();

					if(temp.length() == 0){
						temp = description;
						vBean.setDescription(description);

					}else{

						if(temp.equals(description)){
							StringBuilder s = new StringBuilder();
							s.append(description + " - " + times);
							vBean.setDescription(s.toString());

						}else{
							StringBuilder s = new StringBuilder();
							s.append(description + " - " + "1");
							vBean.setDescription(s.toString());
							temp = description;
							times = 1;
						}
					}

					vBean.setId(id);
					vBean.setPreview(preview);
					times++;

					runThread(vBean);
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
		 * Method to get the videos from the server. It gets the id, description and the url for the preview.
		 */
		private void getRecentVideos(int day){
			String urlStr = "https://api.shutterstock.com/v2/videos/search?per_page=25&added_date_start=";

			Date d = new Date();	
			SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM");
			@SuppressWarnings("deprecation")
			String date = ft.format(d) + "-" + (d.getDate() - day - 1); 
			urlStr += date;

			InputStream is = null;

			try {
				URL url = new URL(urlStr);
				URLConnection conn = url.openConnection();
				conn.setRequestProperty("Authorization", "Basic " + Utilities.getLicenseKey());
				is = conn.getInputStream();
	
				String temp = "";
				int times = 1;

				BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
				String jsonText = Utilities.readAll(rd);

				JsonElement json = new JsonParser().parse(jsonText);
				JsonObject o = json.getAsJsonObject();
				JsonArray array = o.get("data").getAsJsonArray();

				if(array.size() == 0){
					getRecentVideos(activity.get().counter++);
					return;
				}
				
				Iterator<JsonElement> iterator = array.iterator();
				while(iterator.hasNext()){
					JsonElement json2 = iterator.next();
					JsonObject ob = json2.getAsJsonObject();

					String id = ob.get("id").getAsString();
					String description = ob.get("description").getAsString();		
					JsonObject assets = ob.get("assets").getAsJsonObject();
					String preview = assets.get("preview_mp4").getAsJsonObject().get("url").getAsString();

					final VideoBean vBean = new VideoBean();

					if(temp.length() == 0){
						temp = description;
						vBean.setDescription(description);

					}else{

						if(temp.equals(description)){
							StringBuilder s = new StringBuilder();
							s.append(description + " - " + times);
							vBean.setDescription(s.toString());

						}else{
							StringBuilder s = new StringBuilder();
							s.append(description + " - " + "1");
							vBean.setDescription(s.toString());
							temp = description;
							times = 1;
						}
					}

					vBean.setId(id);
					vBean.setPreview(preview);
					times++;

					runThread(vBean);
				}
			} catch (IOException e) {
				getRecentVideos(activity.get().counter++);
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
		 * Method to run a thread to update the UI with the video.
		 * 
		 * @param bean the video bean
		 */
		private void runThread(final VideoBean bean){

			if (activity.get() != null) {
				activity.get().runOnUiThread(new Runnable(){

					@Override
					public void run() {
                        if(activity.get().isProgressDilaogOn())
                            activity.get().dismissProgressDialog();

						if(bean != null){
							activity.get().videoAdapter.notifyDataSetChanged();
							activity.get().videos.add(bean);
						}else{
							String msg = bundle.getString("key") == null ? " that input " : bundle.getString("key");
							Toast.makeText(activity.get(), "No video with " + msg + " was found",Toast.LENGTH_SHORT).show();
						}
					}
				});	
			}
		}
	}


	// not used
	@Override
	public void onLoadFinished(Loader<Void> arg0, Void arg1) {}
	@Override
	public void onLoaderReset(Loader<Void> arg0) {}


}

