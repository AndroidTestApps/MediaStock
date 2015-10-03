package com.example.mediastock.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mediastock.R;
import com.example.mediastock.beans.Bean;
import com.example.mediastock.beans.ImageBean;
import com.example.mediastock.beans.MusicBean;
import com.example.mediastock.beans.VideoBean;
import com.example.mediastock.util.Adapter;
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
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Main activity which displays the image gallery, music gallery, video gallery, the recent images
 * the recent videos and the recent music.
 *
 * @author Dinu
 */
public class MainActivity extends BaseActivity implements ListView.OnTouchListener, OnItemClickListener, OnClickListener {
    private LinearLayout layout_images;
    private LinearLayout.LayoutParams layout_param;
    private Adapter musicAdapter;
    private Adapter videoAdapter;
    private ArrayList<Bean> music = new ArrayList<>();
    private ArrayList<Bean> videos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if online
        if (!isOnline()) {
            setContentView(R.layout.no_internet);
            Toast.makeText(this, "Not online", Toast.LENGTH_SHORT).show();
        } else {

            setContentView(R.layout.activity_main);
            showProgressDialog();

            layout_images = (LinearLayout) this.findViewById(R.id.image_main_ScrollView).findViewById(R.id.scroll_main_linearLayout);
            layout_param = new LinearLayout.LayoutParams(180, 180);
            layout_param.setMargins(0, 0, 3, 0);

            // music list listeners
            musicAdapter = new Adapter(getApplicationContext(), music, 1);
            ListView listViewMusic = (ListView) this.findViewById(R.id.list_music);
            listViewMusic.setAdapter(musicAdapter);
            listViewMusic.setFastScrollEnabled(true);
            listViewMusic.setOnTouchListener(this);
            listViewMusic.setOnItemClickListener(this);

            // video list listeners
            videoAdapter = new Adapter(getApplicationContext(), videos, 2);
            ListView listViewVideo = (ListView) this.findViewById(R.id.list_video);
            listViewVideo.setAdapter(videoAdapter);
            listViewVideo.setFastScrollEnabled(true);
            listViewVideo.setOnTouchListener(this);
            listViewVideo.setOnItemClickListener(this);


            // get images
            new WebRequest(1, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            // get music
            new WebRequest(2, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            // get videos
            new WebRequest(3, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


            // image gallery button
            final TextView imageGallery = (TextView) this.findViewById(R.id.TextView_imageGalery);
            imageGallery.setOnClickListener(this);

            // music gallery button
            final TextView musicGallery = (TextView) this.findViewById(R.id.TextView_musicGalery);
            musicGallery.setOnClickListener(this);

            // video gallery button
            final TextView videoGallery = (TextView) this.findViewById(R.id.TextView_videoGalery);
            videoGallery.setOnClickListener(this);
        }
    }


    /**
     * Method to display the image into the main UI schrollView
     *
     * @param bean the image bean
     */
    private void displayImg(final ImageBean bean) {
        ImageView iv = new ImageView(getApplicationContext());
        iv.setLayoutParams(layout_param);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Picasso.with(getApplicationContext()).load(bean.getUrl()).placeholder(R.drawable.border).resize(180, 180).centerCrop().into(iv);

        layout_images.addView(iv);

        iv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                goToDisplayImageActivity(bean);
            }
        });
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
     * Method to allow and disallow SchrollView to intercept touch events.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Disallow ScrollView to intercept touch events.
                v.getParent().requestDisallowInterceptTouchEvent(true);
                break;

            case MotionEvent.ACTION_UP:
                // Allow ScrollView to intercept touch events.
                v.getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }

        // Handle ListView touch events.
        v.onTouchEvent(event);
        return true;
    }

    /**
     * Listener method on listView items.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int arg2, long arg3) {
        int id = parent.getId();
        TextView item = (TextView) view.findViewById(R.id.textView_grid_music_video);
        switch (id) {

            case R.id.list_music:

                String title = item.getText().toString();
                String url = item.getTag().toString();

                Intent intent = new Intent(MainActivity.this, MusicPlayerActivity.class);
                intent.putExtra("url", url);
                intent.putExtra("title", title);

                startActivity(intent);
                break;

            case R.id.list_video:
                String description = item.getText().toString();
                String url_v = item.getTag().toString();

                Intent intent_v = new Intent(MainActivity.this, VideoPlayerActivity.class);
                intent_v.putExtra("url", url_v);
                intent_v.putExtra("description", description);

                startActivity(intent_v);
                break;

            default:
                break;
        }
    }


    /**
     * Listener method on the buttons image gallery, video gallery, music gallery.
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.TextView_imageGalery:
                Intent intent_i = new Intent(getApplicationContext(), ImageGalleryActivity.class);
                startActivity(intent_i);
                break;

            case R.id.TextView_videoGalery:
                Intent intent_v = new Intent(getApplicationContext(), VideoGalleryActivity.class);
                startActivity(intent_v);
                break;

            case R.id.TextView_musicGalery:
                Intent intent_m = new Intent(getApplicationContext(), MusicGalleryActivity.class);
                startActivity(intent_m);
                break;
        }
    }


    /**
     * Static inner class to get the recent images, music and videos from the server.
     *
     * @author Dinu
     */
    private static class WebRequest extends AsyncTask<String, Bean, Void> {
        private static WeakReference<MainActivity> activity;
        private int type;

        /**
         * The constructor
         *
         * @param type 1 represents images, 2 represents music, 3 represents video
         */
        public WebRequest(int type, MainActivity activity) {
            WebRequest.activity = new WeakReference<>(activity);
            this.type = type;
        }

        @Override
        protected Void doInBackground(String... params) {

            switch (type) {
                case 1:
                    getRecentImages(0);
                    break;

                case 2:
                    getRecentMusic();
                    break;

                default:
                    getRecentVideos(0);
                    break;
            }
            /*
            if(params[0].equals("getImages"))
				getRecentImages(0);

			else if(params[0].equals("getMusic"))
				getRecentMusic();

			else
				getRecentVideos(0);
            */
            return null;
        }


        /**
         * Method to get the recent images. It gets the id, description, id of the contributor and the url for the preview.
         */
        private void getRecentImages(int day) {
            String urlStr = "https://@api.shutterstock.com/v2/images/search?per_page=15&added_date=";
            urlStr += Utilities.getDate(day);

            InputStream is = null;

            try {
                URL url = new URL(urlStr);
                URLConnection conn = url.openConnection();
                conn.setRequestProperty("Authorization", "Basic " + Utilities.getLicenseKey());
                is = conn.getInputStream();

                // get stream
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                String jsonText = Utilities.readAll(rd);

                // parse JSON text
                JsonElement json = new JsonParser().parse(jsonText);
                JsonObject o = json.getAsJsonObject();
                JsonArray array = o.get("data").getAsJsonArray();

                if (array.size() == 0) {
                    int yesterday = day;
                    yesterday += 1;
                    getRecentImages(yesterday);
                    return;
                }

                JsonObject assets;
                JsonObject preview;

                // get objects
                Iterator<JsonElement> iterator = array.iterator();
                while (iterator.hasNext()) {
                    JsonElement json2 = iterator.next();
                    ImageBean ib = new ImageBean();

                    assets = json2.getAsJsonObject().get("assets").getAsJsonObject();
                    preview = assets.get("preview").getAsJsonObject();

                    ib.setDescription(json2.getAsJsonObject().get("description").getAsString());
                    ib.setId(json2.getAsJsonObject().get("id").getAsInt());
                    ib.setIdContributor(json2.getAsJsonObject().get("contributor").getAsJsonObject().get("id").getAsInt());
                    ib.setUrl(preview.get("url").getAsString());

                    // update the UI
                    publishProgress(ib);

                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null)
                        is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Method to get the recent music from the server. It gets the id, the title and the url preview.
         */
        private void getRecentMusic() {
            String urlStr = "https://api.shutterstock.com/v2/audio/search?per_page=15&added_date_start=2013-01-01";

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

                Iterator<JsonElement> iterator = array.iterator();
                if (array.size() == 0)
                    publishProgress((MusicBean) null);

                while (iterator.hasNext()) {
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
                    if (is != null)
                        is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        /**
         * Method to get the recent videos from the server. It gets the id, description and the url for the preview.
         */
        private void getRecentVideos(int day) {
            String urlStr = "https://api.shutterstock.com/v2/videos/search?per_page=15&added_date_start=";
            urlStr += Utilities.getDate(day);

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

                if (array.size() == 0) {
                    int yesterday = day;
                    yesterday += 1;
                    getRecentVideos(yesterday);
                    return;
                }

                Iterator<JsonElement> iterator = array.iterator();
                while (iterator.hasNext()) {
                    JsonElement json2 = iterator.next();
                    JsonObject ob = json2.getAsJsonObject();
                    VideoBean vBean = new VideoBean();

                    String id = ob.get("id").getAsString();
                    String description = ob.get("description").getAsString();
                    JsonObject assets = ob.get("assets").getAsJsonObject();
                    String preview = assets.get("preview_mp4").getAsJsonObject().get("url").getAsString();

                    if (temp.length() == 0) {
                        temp = description;
                        vBean.setDescription(description);

                    } else {

                        if (temp.equals(description)) {
                            StringBuilder s = new StringBuilder();
                            s.append(description + " - " + times);
                            vBean.setDescription(s.toString());

                        } else {
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

                    // update the UI
                    publishProgress(vBean);

                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null)
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
        protected void onProgressUpdate(Bean... bean) {

            // image
            if (type == 1) {
                if (activity.get().isProgressDilaogOn())
                    activity.get().dismissProgressDialog();

                if (bean[0] == null)
                    Toast.makeText(activity.get(), "No image was found", Toast.LENGTH_SHORT).show();
                else
                    activity.get().displayImg((ImageBean) bean[0]);

                // music
            } else if (type == 2) {
                if (bean[0] == null)
                    Toast.makeText(activity.get(), "No music was found", Toast.LENGTH_SHORT).show();
                else {
                    activity.get().music.add(bean[0]);
                    activity.get().musicAdapter.notifyDataSetChanged();
                }

                // video
            } else {
                if (bean[0] == null)
                    Toast.makeText(activity.get(), "No video was found", Toast.LENGTH_SHORT).show();
                else {
                    activity.get().videos.add(bean[0]);
                    activity.get().videoAdapter.notifyDataSetChanged();
                }
            }
        }


    }


}
