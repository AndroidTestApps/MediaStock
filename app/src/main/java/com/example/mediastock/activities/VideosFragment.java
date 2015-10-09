package com.example.mediastock.activities;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mediastock.R;
import com.example.mediastock.beans.Bean;
import com.example.mediastock.beans.VideoBean;
import com.example.mediastock.util.MusicVideoAdapter;
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
 * Activity which displays a listView with videos.
 *
 * @author Dinu
 */
public class VideosFragment extends AbstractFragment implements LoaderCallbacks<Void>, OnItemClickListener {
    private MusicVideoAdapter videoAdapter;
    private ArrayList<Bean> videos = new ArrayList<>();
    private static Context context;
    private View view = null;
    private LinearLayout layout_p_bar;
    private ListView listViewVideo;
    private ProgressBar p_bar;

    public static VideosFragment createInstance() {
        return new VideosFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!isOnline()) {
            Toast.makeText(context, "Not online", Toast.LENGTH_SHORT).show();
            return;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getActivity().getApplicationContext();

        if(!isOnline())
            return null;

        view = inflater.inflate(R.layout.video_fragment, container, false);
        p_bar = (ProgressBar) view.findViewById(R.id.p_bar);
        layout_p_bar = (LinearLayout) view.findViewById(R.id.layout_pBar);

        compute();

        return view;
    }

    private void compute() {

        // video list
        videoAdapter = new MusicVideoAdapter(context, videos, 2);
        listViewVideo = (ListView) view.findViewById(R.id.list_music_video_galery);
        listViewVideo.setAdapter(videoAdapter);
        listViewVideo.setOnItemClickListener(this);

        if(!videos.isEmpty()){
            cancelListVisibility();
            videos.clear();
            videoAdapter.notifyDataSetChanged();
        }

        this.getActivity().getLoaderManager().initLoader(0, null, this);

    }

    private void cancelListVisibility() {
        listViewVideo.setVisibility(View.INVISIBLE);
        layout_p_bar.setVisibility(View.VISIBLE);
        p_bar.setVisibility(View.VISIBLE);
    }

    private void restoreListVisibility(){
        p_bar.setVisibility(View.GONE);
        layout_p_bar.setVisibility(View.GONE);
        listViewVideo.setVisibility(View.VISIBLE);
    }

    public void getRecentVideos() {
        if (!isOnline())
            return;

        cancelListVisibility();

        videos.clear();
        videoAdapter.notifyDataSetChanged();

        this.getActivity().getLoaderManager().initLoader(0, null, this);
    }

    public void searchVideosByKey(String key1, String key2) {
        cancelListVisibility();

        videos.clear();
        videoAdapter.notifyDataSetChanged();

        if (key2 != null) {
            Bundle bundle1 = new Bundle();
            bundle1.putString("key", key1);
            this.getActivity().getLoaderManager().initLoader(4, bundle1, this);

            Bundle bundle2 = new Bundle();
            bundle2.putString("key", key2);
            this.getActivity().getLoaderManager().initLoader(4, bundle2, this);
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("key", key1);
            this.getActivity().getLoaderManager().initLoader(4, bundle, this);
        }

    }

    /**
     * The method starts the loader with the bundle as info.
     */
    public void startFilterSearch(Bundle bundle) {
        if (!isOnline())
            return;

        cancelListVisibility();

        videos.clear();
        videoAdapter.notifyDataSetChanged();

        this.getActivity().getLoaderManager().initLoader(5, bundle, this);
    }


    /**
     * Method to create a Loader.
     */
    @Override
    public Loader<Void> onCreateLoader(int id, Bundle bundle) {
        AsyncTaskLoader<Void> data = null;
        switch (id) {

            case 0:
                data = new LoadData(this, 0, null);
                break;
            case 1:
            case 2:
            case 3:
            case 4:
                data = new LoadData(this, 1, bundle);
                break;
            case 5:
                data = new LoadData(this, 2, bundle);
                break;

            default:
                break;
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

        Intent intent = new Intent(context, VideoPlayerActivity.class);
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
        private static WeakReference<VideosFragment> activity;
        private Bundle bundle;
        private int type;

        public LoadData(VideosFragment context, int type, Bundle bundle) {
            super(context.getActivity());

            this.type = type;
            this.bundle = bundle;
            activity = new WeakReference<>(context);
        }

        @Override
        public Void loadInBackground() {

            switch (type) {

                case 0:
                    getRecentVideos(0);
                    break;

                case 1:
                    searchVideosByKey();
                    break;

                case 2:
                    filterVideos();
                    break;

                default:
                    break;
            }

            return null;
        }

        private String parseUrlFilterSearch() {
            String url = "https://api.shutterstock.com/v2/videos/search?safe=true";
            String category = bundle.getString(FilterVideoFragment.CATEGORY);
            String word = bundle.getString(FilterVideoFragment.WORD);

            if (!category.isEmpty())
                url += "&category=" + category.substring(0, 1).toUpperCase() + category.substring(1);

            if (!word.isEmpty())
                url += "&query=" + word;

            url += "&sort=" + bundle.getString(FilterVideoFragment.SORT).toLowerCase();
            url += "&per_page=" + bundle.getString(FilterVideoFragment.PER_PAGE);

            return url;
        }


        private void filterVideos() {
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


                if (array.size() < 1) {
                    runThread(null);
                    return;
                }

                Iterator<JsonElement> iterator = array.iterator();
                while (iterator.hasNext()) {
                    JsonElement json2 = iterator.next();
                    JsonObject ob = json2.getAsJsonObject();

                    String id = ob.get("id").getAsString();
                    String description = ob.get("description").getAsString();
                    JsonObject assets = ob.get("assets").getAsJsonObject();
                    String preview = assets.get("preview_mp4").getAsJsonObject().get("url").getAsString();

                    final VideoBean vBean = new VideoBean();

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

                    runThread(vBean);
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

        private void searchVideosByKey() {
            String urlStr = "https://api.shutterstock.com/v2/videos/search?per_page=50&query=";
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

                if (array.size() == 0) {
                    runThread(null);
                    return;
                }

                Iterator<JsonElement> iterator = array.iterator();
                while (iterator.hasNext()) {
                    JsonElement json2 = iterator.next();
                    JsonObject ob = json2.getAsJsonObject();

                    String id = ob.get("id").getAsString();
                    String description = ob.get("description").getAsString();
                    JsonObject assets = ob.get("assets").getAsJsonObject();
                    String preview = assets.get("preview_mp4").getAsJsonObject().get("url").getAsString();

                    final VideoBean vBean = new VideoBean();

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

                    runThread(vBean);
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
         * Method to get the videos from the server. It gets the id, description and the url for the preview.
         */
        private void getRecentVideos(int day) {
            String urlStr = "https://api.shutterstock.com/v2/videos/search?per_page=50&added_date_start=";
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

                    String id = ob.get("id").getAsString();
                    String description = ob.get("description").getAsString();
                    JsonObject assets = ob.get("assets").getAsJsonObject();
                    String preview = assets.get("preview_mp4").getAsJsonObject().get("url").getAsString();

                    final VideoBean vBean = new VideoBean();

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

                    runThread(vBean);
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
         * Method to run a thread to update the UI with the video.
         *
         * @param bean the video bean
         */
        private void runThread(final VideoBean bean) {
            final VideosFragment context = activity.get();

            if (context != null) {
                context.getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        if (context.p_bar.isShown())
                            context.restoreListVisibility();

                        if (bean != null) {
                            context.videoAdapter.notifyDataSetChanged();
                            context.videos.add(bean);
                        } else {
                            String msg = bundle.getString("key") == null ? " that input " : bundle.getString("key");
                            context.restoreListVisibility();
                            Toast.makeText(context.getActivity(), "No video with " + msg + " was found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }


    // not used
    @Override
    public void onLoadFinished(Loader<Void> arg0, Void arg1) {
    }

    @Override
    public void onLoaderReset(Loader<Void> arg0) {
    }


}

