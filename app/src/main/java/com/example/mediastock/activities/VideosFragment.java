package com.example.mediastock.activities;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.mediastock.R;
import com.example.mediastock.data.VideoBean;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Fragment which displays a list of videos.
 *
 * @author Dinu
 */
public class VideosFragment extends AbstractFragment implements LoaderCallbacks<Void> {
    private static String keyWord1;
    private static String keyWord2;
    private static Context context;
    private static Handler handler;
    private ProgressBar progressBar;
    private ProgressBar progressBar_bottom;
    private RecyclerView recyclerView;
    private MusicVideoAdapter videoAdapter;
    private View view;

    /**
     * Method to create an instance of this fragment for the viewPager
     */
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

        view = inflater.inflate(R.layout.video_fragment, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        compute();
    }

    /**
     * Method to initialize the UI components and to get the recent videos.
     */
    private void compute() {
        final VideosFragment fragment = this;

        handler = new MyHandler(this);
        progressBar = (ProgressBar) view.findViewById(R.id.p_bar);
        progressBar_bottom = (ProgressBar) view.findViewById(R.id.p_bar_bottom);
        progressBar_bottom.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
        recyclerView = (RecyclerView) view.findViewById(R.id.list_video_galery);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        videoAdapter = new MusicVideoAdapter(context, 2, false);
        recyclerView.setAdapter(videoAdapter);

        // on item click
        videoAdapter.setOnMediaItemClickListener(new MusicVideoAdapter.OnMediaItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                VideoBean bean = (VideoBean) videoAdapter.getItemAt(position);

                Intent intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra("url", bean.getPreview());
                intent.putExtra("description", bean.getDescription());

                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);
            }
        });

        // endless list. load more data when reaching the bottom of the view
        videoAdapter.setOnBottomListener(new MusicVideoAdapter.OnBottomListener() {

            @Override
            public void onBottomLoadMoreData(int loadingType, int loadingPageNumber) {
                progressBar_bottom.setVisibility(View.VISIBLE);

                // recent videos
                if (loadingType == 1) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("pagenumber", loadingPageNumber);
                    getActivity().getLoaderManager().initLoader(1, bundle, fragment);

                } else
                    startSearching(2, keyWord1, keyWord2, loadingPageNumber); // search videos by key
            }
        });


        getRecentVideos();
    }


    /**
     * Method to get the recent videos
     */
    public void getRecentVideos() {
        if (!Utilities.deviceOnline(context))
            return;

        showProgressBar();
        deleteItems();

        Bundle bundle = new Bundle();
        bundle.putInt("pagenumber", 30);

        getActivity().getLoaderManager().initLoader(1, bundle, this);
    }


    /**
     * It searches the videos by one or two keys
     */
    public void searchVideosByKey(String key1, String key2) {
        keyWord1 = key1;
        keyWord2 = key2;

        showProgressBar();
        deleteItems();
        startSearching(2, key1, key2, 30);
    }

    private void startSearching(int loaderType, String key1, String key2, int loadingPageNumber) {
        Bundle bundle = new Bundle();
        bundle.putString("key1", key1);
        bundle.putString("key2", key2);
        bundle.putInt("pagenumber", loadingPageNumber);

        getActivity().getLoaderManager().initLoader(loaderType, bundle, this);
    }

    /**
     * Start the filter search. The bundle contains alla the users input.
     */
    public void startFilterSearch(Bundle bundle) {
        if (!Utilities.deviceOnline(context))
            return;

        showProgressBar();
        deleteItems();
        getActivity().getLoaderManager().initLoader(3, bundle, this);
    }


    /**
     * Method to delete the list of videos and to notify the adapter
     */
    private void deleteItems() {
        videoAdapter.deleteItems();
    }

    /**
     * It shows the progress bar
     */
    private void showProgressBar() {
        recyclerView.scrollToPosition(0);
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Method to dismiss the progress bar
     */
    private void dismissProgressBar() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * Method to create a Loader.
     */
    @Override
    public Loader<Void> onCreateLoader(int id, Bundle bundle) {
        AsyncTaskLoader<Void> data = null;
        switch (id) {

            case 1:
                data = new AsyncWork(this, 1, bundle);
                break;
            case 2:
                data = new AsyncWork(this, 2, bundle);
                break;
            case 3:
                data = new AsyncWork(this, 3, bundle);
                break;

            default:
                break;
        }

        data.forceLoad();
        return data;
    }


    /**
     * The data was loaded and we destroy the loader
     */
    @Override
    public void onLoadFinished(Loader<Void> arg0, Void arg1) {
        getActivity().getLoaderManager().destroyLoader(arg0.getId());
    }

    @Override
    public void onLoaderReset(Loader<Void> arg0) {
    }

    /**
     * Handler to update the UI
     */
    private static class MyHandler extends Handler {
        private static WeakReference<VideosFragment> activity;

        public MyHandler(VideosFragment context) {
            activity = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            VideosFragment context = activity.get();

            switch (msg.what) {

                case 1:
                    VideoBean bean = msg.getData().getParcelable("bean");
                    context.videoAdapter.addItem(bean);

                    break;

                case 2:
                    context.dismissProgressBar();
                    Toast.makeText(context.getActivity(), "No video with " + msg.getData().getString("error") + " was found", Toast.LENGTH_SHORT).show();

                    break;

                case 3:
                    if (context.progressBar.isShown())
                        context.dismissProgressBar();

                    if (context.progressBar_bottom.isShown())
                        context.progressBar_bottom.setVisibility(View.GONE);

                    break;

                case 4:
                    context.dismissProgressBar();
                    Toast.makeText(context.getActivity(), "No video was found", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * Static inner class to search for videos, to get the recent videos and to do a filter search from the server.
     *
     * @author Dinu
     */
    private static class AsyncWork extends AsyncTaskLoader<Void> {
        private static WeakReference<VideosFragment> activity;
        private final Bundle bundle;
        private final int loadingPageNumber;
        private final int type;

        public AsyncWork(VideosFragment context, int type, Bundle bundle) {
            super(context.getActivity());

            activity = new WeakReference<>(context);
            this.type = type;
            this.loadingPageNumber = bundle.getInt("pagenumber");
            this.bundle = bundle;
        }

        @Override
        public Void loadInBackground() {
            activity.get().videoAdapter.setLoadingType(type);
            activity.get().videoAdapter.setPageNumber(loadingPageNumber);

            switch (type) {

                case 1:
                    getRecentVideos(0, loadingPageNumber);
                    break;

                case 2:
                    searchVideosByKey(loadingPageNumber);
                    break;

                case 3:
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

            if (!category.equals("None"))
                url += "&category=" + category.substring(0, 1).toUpperCase() + category.substring(1);

            if (!word.isEmpty())
                url += "&query=" + word;

            url += "&sort=" + bundle.getString(FilterVideoFragment.SORT).toLowerCase();
            url += "&per_page=" + bundle.getString(FilterVideoFragment.PER_PAGE);

            return url;
        }

        /**
         * Method to do a filter search of the videos
         */
        private void filterVideos() {
            String urlStr = parseUrlFilterSearch();

            InputStream is = null;

            Log.i("url", urlStr);
            try {
                URL url = new URL(urlStr);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestProperty("Authorization", "Basic " + Utilities.getLicenseKey());

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    is = con.getInputStream();

                    BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                    String jsonText = Utilities.readAll(rd);
                    rd.close();

                    JsonElement json = new JsonParser().parse(jsonText);
                    JsonObject o = json.getAsJsonObject();
                    JsonArray array = o.get("data").getAsJsonArray();

                    if (array.size() == 0) {
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                handler.sendEmptyMessage(4);
                            }
                        });

                        con.disconnect();
                        return;
                    }

                    StringBuilder temp = new StringBuilder();
                    temp.append("");
                    int times = 2;
                    int i = 0;

                    for (JsonElement element : array) {
                        JsonObject jsonObj = element.getAsJsonObject();
                        JsonObject assets = jsonObj.get("assets").getAsJsonObject();
                        final VideoBean bean = new VideoBean();

                        if (assets != null) {
                            bean.setId(jsonObj.get("id") == null ? null : jsonObj.get("id").getAsString());
                            bean.setPreview(assets.get("preview_mp4") == null ? null : assets.get("preview_mp4").getAsJsonObject().get("url").getAsString());
                            String description = jsonObj.get("description") == null ? null : jsonObj.get("description").getAsString();

                            if (description != null) {
                                if (temp.toString().equals(description)) {
                                    bean.setDescription(description + " - " + times);
                                    times++;

                                } else {
                                    bean.setDescription(description);
                                    temp.replace(0, temp.length(), description);
                                    times = 2;
                                }
                            }
                        }

                        bean.setPos(i);
                        i++;

                        // dismiss progress
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                handler.sendEmptyMessage(3);
                            }
                        });

                        final Bundle bundle = new Bundle();
                        final Message msg = new Message();

                        // set the video bean
                        bundle.putParcelable("bean", bean);
                        msg.setData(bundle);
                        msg.what = 1;

                        // update the UI with the video
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                handler.dispatchMessage(msg);
                            }
                        });
                    }
                }

                con.disconnect();

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
         * Method to search videos by one or two keys
         *
         * @param loadingPageNumber the number of items to get
         */
        private void searchVideosByKey(int loadingPageNumber) {
            String urlStr = "https://api.shutterstock.com/v2/videos/search?per_page=";
            urlStr += loadingPageNumber + "&query=";

            String key1 = bundle.getString("key1");
            String key2 = bundle.getString("key2");

            if (key2 != null)
                urlStr += key1 + "/" + key2;
            else
                urlStr += key1;

            Log.i("url", urlStr);

            InputStream is = null;
            try {
                URL url = new URL(urlStr);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestProperty("Authorization", "Basic " + Utilities.getLicenseKey());

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    is = con.getInputStream();

                    BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                    String jsonText = Utilities.readAll(rd);
                    rd.close();

                    JsonElement json = new JsonParser().parse(jsonText);
                    JsonObject o = json.getAsJsonObject();
                    JsonArray array = o.get("data").getAsJsonArray();

                    if (array.size() == 0) {
                        final Message msg = new Message();
                        final Bundle bundle = new Bundle();
                        bundle.putString("error", key2 != null ? key1 + " " + key2 : key1);
                        msg.setData(bundle);
                        msg.what = 2;

                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                handler.dispatchMessage(msg);
                            }
                        });

                        con.disconnect();
                        return;
                    }

                    StringBuilder temp = new StringBuilder();
                    temp.append("");
                    int times = 2;

                    for (int i = loadingPageNumber - 30; i < array.size(); i++) {
                        JsonObject jsonObj = array.get(i).getAsJsonObject();
                        JsonObject assets = jsonObj.get("assets").getAsJsonObject();
                        final VideoBean bean = new VideoBean();

                        if (assets != null) {
                            bean.setId(jsonObj.get("id") == null ? null : jsonObj.get("id").getAsString());
                            bean.setPreview(assets.get("preview_mp4") == null ? null : assets.get("preview_mp4").getAsJsonObject().get("url").getAsString());
                            String description = jsonObj.get("description") == null ? null : jsonObj.get("description").getAsString();

                            if (description != null) {
                                if (temp.toString().equals(description)) {
                                    bean.setDescription(description + " - " + times);
                                    times++;

                                } else {
                                    bean.setDescription(description);
                                    temp.replace(0, temp.length(), description);
                                    times = 2;
                                }
                            }
                        }

                        bean.setPos(i);

                        // dismiss progress
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                handler.sendEmptyMessage(3);
                            }
                        });

                        final Bundle bundle = new Bundle();
                        final Message msg = new Message();

                        // set the video bean
                        bundle.putParcelable("bean", bean);
                        msg.setData(bundle);
                        msg.what = 1;

                        // update the UI with the video
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                handler.dispatchMessage(msg);
                            }
                        });

                    }
                }

                con.disconnect();

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
         * Method to get the recent videos
         *
         * @param day it represents the day
         * @param loadingPageNumber the number of items to get
         */
        private void getRecentVideos(int day, int loadingPageNumber) {
            String urlStr = "https://api.shutterstock.com/v2/videos/search?per_page=";
            urlStr += loadingPageNumber + "&added_date_start=";
            urlStr += Utilities.getDate(day);

            Log.i("url", urlStr);

            InputStream is = null;
            try {
                URL url = new URL(urlStr);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestProperty("Authorization", "Basic " + Utilities.getLicenseKey());

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    is = con.getInputStream();

                    BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                    String jsonText = Utilities.readAll(rd);
                    rd.close();

                    JsonElement json = new JsonParser().parse(jsonText);
                    JsonObject o = json.getAsJsonObject();
                    JsonArray array = o.get("data").getAsJsonArray();

                    if (array.size() == 0) {
                        int yesterday = day;
                        yesterday += 1;
                        con.disconnect();

                        getRecentVideos(yesterday, loadingPageNumber);
                        return;
                    }

                    StringBuilder temp = new StringBuilder();
                    temp.append("");
                    int times = 2;

                    for (int i = loadingPageNumber - 30; i < array.size(); i++) {
                        JsonObject jsonObj = array.get(i).getAsJsonObject();
                        JsonObject assets = jsonObj.get("assets").getAsJsonObject();
                        final VideoBean bean = new VideoBean();

                        if (assets != null) {
                            bean.setId(jsonObj.get("id") == null ? null : jsonObj.get("id").getAsString());
                            bean.setPreview(assets.get("preview_mp4") == null ? null : assets.get("preview_mp4").getAsJsonObject().get("url").getAsString());
                            String description = jsonObj.get("description") == null ? null : jsonObj.get("description").getAsString();

                            if (description != null) {

                                if (temp.toString().equals(description)) {
                                    bean.setDescription(description + " - " + times);
                                    times++;

                                } else {
                                    bean.setDescription(description);
                                    temp.replace(0, temp.length(), description);
                                    times = 2;
                                }
                            }
                        }

                        bean.setPos(i);

                        // dismiss progress
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                handler.sendEmptyMessage(3);
                            }
                        });

                        final Bundle bundle = new Bundle();
                        final Message msg = new Message();

                        // set the video bean
                        bundle.putParcelable("bean", bean);
                        msg.setData(bundle);
                        msg.what = 1;

                        // update the UI with the video
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                handler.dispatchMessage(msg);
                            }
                        });

                    }
                }

                con.disconnect();

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
    }
}

