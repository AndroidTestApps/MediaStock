package com.example.mediastock.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mediastock.R;
import com.example.mediastock.beans.Bean;
import com.example.mediastock.beans.MusicBean;
import com.example.mediastock.util.Adapter;
import com.example.mediastock.util.DownloadResultReceiver;
import com.example.mediastock.util.DownloadService;
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
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Activity which displays a listView with music.
 * 
 * @author Dinu
 */
public class MusicGalleryActivity extends BaseActivity implements DownloadResultReceiver.Receiver, OnItemClickListener {
	public static final String MUSIC_RECEIVER = "mreceiver";
	private Adapter musicAdapter;
	private ArrayList<Bean> music = new ArrayList<>();
	private DownloadResultReceiver resultReceiver;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// check if online
		if(!isOnline()){
			setContentView(R.layout.no_internet);
			Toast.makeText(this, "Not online", Toast.LENGTH_SHORT).show();

		}else{

			setContentView(R.layout.activity_music_video_galery);
            setTitle("Music gallery");

            showProgressDialog();

			// music list
			musicAdapter = new Adapter(this, music, 1);
			ListView listViewMusic = (ListView) this.findViewById(R.id.list_music_video_galery);
			listViewMusic.setAdapter(musicAdapter);
			listViewMusic.setOnItemClickListener(this);


			// we search the users input
			if(search())
				searchForMusic();

			// we do a filter search
			else if(filterSearch())
				startFilterSearch();

			// we fetch the recent music
			else
				new WebRequest(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "getmusic");

		}
	}

	/**
	 * We pass all the info to DownloadService service to start to download the images. 
	 */
	private void startFilterSearch() {
		this.dismissProgressDialog();

		Bundle bundle = getIntent().getExtras();

		resultReceiver = new DownloadResultReceiver(new Handler());
		resultReceiver.setReceiver(this);
		Intent intent = new Intent(Intent.ACTION_SYNC, null, this, DownloadService.class);

		// query info
		intent.putExtra(MUSIC_RECEIVER, resultReceiver);	
		intent.putExtra(FilterMusicActivity.ARTIST, bundle.getString(FilterMusicActivity.ARTIST));
		intent.putExtra(FilterMusicActivity.TITLE, bundle.getString(FilterMusicActivity.TITLE));
		intent.putExtra(FilterMusicActivity.GENRE, bundle.getString(FilterMusicActivity.GENRE));
		intent.putExtra(FilterMusicActivity.PER_PAGE, bundle.getString(FilterMusicActivity.PER_PAGE));

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
		return getIntent().getBooleanExtra(FilterMusicActivity.FILTER_SEARCH, false);
	}


	/**
	 * Method which searches the music by one or two keys.
	 */
	private void searchForMusic(){
		boolean twoWords = getIntent().getBooleanExtra("twoWords", false);

		if(twoWords){
			String key1 = getIntent().getStringExtra("key1");
			String key2 = getIntent().getStringExtra("key2");

			// get music by two different keys
			new WebRequest(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "search", key1);			
			new WebRequest(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "search", key2);

		}else{
			String key = getIntent().getStringExtra("key");

			// get music by one key
			new WebRequest(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "search", key);
		}
	}


	/**
	 * Listener on the item of the listView.
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
		TextView item = (TextView) view.findViewById(R.id.textView_grid_music_video);
		String title = item.getText().toString();
		String url = item.getTag().toString();

		Intent intent = new Intent(MusicGalleryActivity.this, MusicPlayerActivity.class);
		intent.putExtra("url", url);
		intent.putExtra("title", title);

		startActivity(intent);
	}


	/**
	 * Static inner class to search for music and to get the recent music from the server.
	 * 
	 * @author Dinu
	 */
	private static class WebRequest extends AsyncTask<String, Bean, String>{
		private static WeakReference<MusicGalleryActivity> activity;
		private boolean searchSuccess = true;

		public WebRequest(MusicGalleryActivity activity){
			WebRequest.activity = new WeakReference<MusicGalleryActivity>(activity);
		}

		@Override
		protected String doInBackground(String... params) {

			if(params[0].equals("getmusic"))
				getRecentMusic();
			else{
				searchMusicByKey(params[1]);
				return params[1];
			}

			return null;
		}

		/**
		 * Method to get the music from the server. It gets the id, the title and the url preview.
		 */
		private void getRecentMusic(){
			String urlStr = "https://api.shutterstock.com/v2/audio/search?per_page=30&added_date_start=2013-01-01";

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

				if(array.size() < 1){
					publishProgress((MusicBean)null);
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
					publishProgress(mBean);
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
		 * Method to search music by one or two keys or the union.
		 *
		 * @param key the key
		 */
		private void searchMusicByKey(String key){
			String urlStr = "https://@api.shutterstock.com/v2/audio/search?per_page=30&title=";
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

				if(array.size() < 1){
					searchSuccess = false;
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
					publishProgress(mBean);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if( is != null)
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
		protected void onProgressUpdate(Bean...bean) {
            if(activity.get().isProgressDilaogOn())
                activity.get().dismissProgressDialog();
				
			if(bean[0] == null)
				Toast.makeText(activity.get(), "No music was found",Toast.LENGTH_SHORT).show();
			else{
				activity.get().music.add(bean[0]);
				activity.get().musicAdapter.notifyDataSetChanged();
			}
		}


		@Override
		protected void onPostExecute(String result){
            if(activity.get().isProgressDilaogOn())
                activity.get().dismissProgressDialog();

			if(!searchSuccess)
				Toast.makeText(activity.get(), "Sorry, no music with " + result + " was found!", Toast.LENGTH_LONG).show();
		}

	}


	/**
	 * It handles the result of the DownloadService service. It updates the UI.
	 */
	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		switch(resultCode){
		case 1:
			this.dismissProgressDialog();
			MusicBean bean = resultData.getParcelable(DownloadService.MUSIC_BEAN);

			// update UI with the music
			music.add(bean);
			musicAdapter.notifyDataSetChanged();
			break;

		case 2:
			this.showProgressDialog();
			break;

		default:
			Toast.makeText(this.getApplicationContext(), "Search failed",Toast.LENGTH_LONG).show();
			break;
		}

	}


}
