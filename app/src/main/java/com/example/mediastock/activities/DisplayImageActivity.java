package com.example.mediastock.activities;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mediastock.R;
import com.example.mediastock.beans.ImageBean;
import com.example.mediastock.util.Utilities;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * Activity which displays the image that the user clicked. It displays all the informations about that image
 * and the similar images.
 * 
 * @author Dinu
 */
public class DisplayImageActivity extends BaseActivity {
    private ScrollView sw;
    private HorizontalScrollView hs;
	private static Handler handler;
	private LinearLayout layout;
	//private ProgressDialog progressDialog;
	private boolean newImage = false;
	private ImageBean imgBean;
	private ImageView imageView;
    private TextView description, contributorsName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// check if online
		if(!isOnline()){
			setContentView(R.layout.no_internet);
			Toast.makeText(this, "Not online", Toast.LENGTH_SHORT).show();
		}else{
			setContentView(R.layout.activity_display_image);

			// handle the threads 
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);

            /*
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("Loading...");
			progressDialog.show();
            */

            this.showProgressDialog();

			layout = (LinearLayout) this.findViewById(R.id.image_home_ScrollView).findViewById(R.id.scroll_image_linearLayout);
            sw = ((ScrollView) findViewById(R.id.scrollViewDisplayImage));
            hs = (HorizontalScrollView) this.findViewById(R.id.image_home_ScrollView);

			imageView = (ImageView) this.findViewById(R.id.imageView_displayImage);
			description = (TextView) this.findViewById(R.id.textView_description_displayImage);
            contributorsName = (TextView) this.findViewById(R.id.TextView_contributor_displayImage);

            // to handle the UI updates
            handler = new MyHandler(this);

			// get the image
			DownloadThread thread1 = new DownloadThread(1, this);
			thread1.setUrl(getUrl());
			new Thread(thread1).start();

			// get the authors name
			DownloadThread thread2 = new DownloadThread(2, this);
			thread2.setAuthorID(getContributorId());
			new Thread(thread2).start();

			// get the similar images 
			DownloadThread thread3 = new DownloadThread(3, this);
			thread3.setImageID(getId());
			new Thread(thread3).start();
		}
	}

    private static class MyHandler extends Handler{
        private static WeakReference<DisplayImageActivity> activity;

        public MyHandler(DisplayImageActivity context){
            activity = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            DisplayImageActivity context = activity.get();

            switch(msg.what){

                // update the UI with the image
                case 0:
                    context.dismissProgressDialog();
                    context.sw.fullScroll(View.FOCUS_UP);

                    context.imageView.setImageBitmap((Bitmap) msg.getData().getParcelable("image"));

                    if(!context.newImage)
                        context.description.setText("Description: " + context.getDescription());
                    else
                        context.description.setText("Description: " + context.imgBean.getDescription());

                    break;

                // update the UI with the authors name
                case 1:
                    context.contributorsName.setText("Author: " +  msg.getData().getString("name"));
                    break;

                // update the UI with the similar images
                case 2:
                    context.displayImg((ImageBean) msg.getData().getParcelable("bean"));
                    break;

                // remove the old similar images
                case 3:
                    ViewGroup v = (ViewGroup)context.hs.getChildAt(0);
                    v.removeAllViews();
                    break;

                default: break;
            }
        }
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
		imageView.setImageDrawable(null);
	}

	private String getUrl(){		
		return getIntent().getStringExtra("url");
	}

	private String getDescription(){
		return getIntent().getStringExtra("description");
	}

	private int getId(){
		return getIntent().getIntExtra("id", 0);
	}

	private int getContributorId(){
		return getIntent().getIntExtra("contributor", 0);
	}

	/**
	 * Method which updates the UI with the new image 
	 *  
	 * @param img the image bean
	 */
	private void updateUI(ImageBean img) {
		// get the image
		DownloadThread thread1 = new DownloadThread(1, this);
		thread1.setUrl(img.getUrl());
		new Thread(thread1).start();

		// get the authors name
		DownloadThread thread2 = new DownloadThread(2, this);
		thread2.setAuthorID(img.getIdContributor());
		new Thread(thread2).start();


		// get the similar images to the current image
		DownloadThread thread3 = new DownloadThread(3, this);
		thread3.setImageID(img.getId());
		new Thread(thread3).start();
	}

	/**
	 * Method to display the image into the main UI schrollView 
	 * 
	 * @param image the image bean
	 */
	private void displayImg(final ImageBean image){
		if(image == null)
			return;

		ImageView iv = new ImageView(getApplicationContext());
		iv.setLayoutParams(new LayoutParams(140,140));	
		iv.setId(image.getId());	
		iv.setImageBitmap(image.getImage());
		iv.setPadding(0, 0, 4, 0);
		iv.setBackgroundResource(R.drawable.border);

		layout.addView(iv);

		iv.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				newImage = true;
				imgBean = image;
				updateUI(image);	
			}		
		});
	}




	/**
	 * Static inner class to download the image, authors name and the similar images.
	 * 
	 * @author Dinu
	 */
	private static class DownloadThread implements Runnable{
		private static WeakReference<DisplayImageActivity> activity;
		private final int type;
		private String url;
		private int authorID;
		private int imageID;

		/**
		 * The constructor.
		 * @param type 1-get image, 2-get authors name, 3-get similar images
		 */
		public DownloadThread(int type, DisplayImageActivity context){
			this.type = type;
			activity = new WeakReference<>(context);
		}


		@Override
		public void run() {

			switch(type){
			
			case 1:
				getImage(url); break;

			case 2: 
				getAuthor(authorID);
				break;

			case 3:	
				getSimilarImages(imageID);
				break;

			default : break;
			}

		}


		private void getImage(String url){
			final Message msg = new Message();
			Bundle bundle = new Bundle();

			try {

				bundle.putParcelable("image", Picasso.with(activity.get()).load(Uri.parse(url)).resize(250, 250).get());

			} catch (IOException e1) {
				e1.printStackTrace();
			}

			/*
			try {

				bundle.putParcelable("image", Utilities.decodeBitmapFromUrl(url, 250 ,250)); 

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			 */
			msg.setData(bundle);
			msg.what = 0;

			// update the UI with the image
			handler.post(new Runnable(){

				@Override
				public void run() {
					handler.dispatchMessage(msg);
				}
			});
		}

		private void getAuthor(int id){
			String urlStr = "https://@api.shutterstock.com/v2/contributors/";
			urlStr += id;

			InputStream is = null;
			String author = null;
			try {
				URL url = new URL(urlStr);
				URLConnection conn = url.openConnection();
				conn.setRequestProperty("Authorization", "Basic " + Utilities.getLicenseKey());
				is = conn.getInputStream();

				BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
				String jsonText = Utilities.readAll(rd);

				JsonElement json = new JsonParser().parse(jsonText);
				author = json.getAsJsonObject().get("display_name").getAsString();

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

			final Message msg = new Message();
			Bundle bundle = new Bundle();
			bundle.putString("name", author);
			msg.setData(bundle);
			msg.what = 1;

			// update the UI with the author
			handler.post(new Runnable(){

				@Override
				public void run() {

					handler.dispatchMessage(msg);
				}
			});
		} 


		private void getSimilarImages(int id){
			String urlStr = "https://@api.shutterstock.com/v2/images/";
			urlStr += id + "/similar?per_page=6";

			// remove the old similar images
			handler.post(new Runnable(){

				@Override
				public void run() {
					handler.sendEmptyMessage(3);
				}
			});


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
				Bundle bundle = new Bundle();

				Iterator<JsonElement> iterator = array.iterator();
				while(iterator.hasNext()){					
					JsonElement json2 = iterator.next();					
					ImageBean ib = new ImageBean();

					assets = json2.getAsJsonObject().get("assets").getAsJsonObject();
					preview = assets.get("preview").getAsJsonObject();

					ib.setImage(Picasso.with(activity.get()).load(Uri.parse(preview.get("url").getAsString())).resize(100, 100).get());
					//ib.setImage(Utilities.decodeBitmapFromUrl(preview.get("url").getAsString(), 100 ,100));
					ib.setDescription(json2.getAsJsonObject().get("description").getAsString());
					ib.setId(json2.getAsJsonObject().get("id").getAsInt());
					ib.setIdContributor(json2.getAsJsonObject().get("contributor").getAsJsonObject().get("id").getAsInt());
					ib.setUrl(preview.get("url").getAsString());

					// update the UI with the image 
					bundle.putParcelable("bean", ib);
					final Message msg = new Message();
					msg.setData(bundle);
					msg.what = 2;

					// update the UI with the new similar images
					handler.post(new Runnable(){

						@Override
						public void run() {
							handler.dispatchMessage(msg);					
						}
					});

				}
			} catch (IOException e) {
				e.printStackTrace();
			} 
			finally {
				try {
					if(is!=null)
						is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}		


		private void setImageID(int imageID){
			this.imageID = imageID;
		}

		private void setUrl(String url){
			this.url = url;
		}

		private void setAuthorID(int authorID){
			this.authorID = authorID;
		}

	}



}

