package com.example.mediastock.activities;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
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
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

/**
 * Activity which displays a listView with videos.
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

        if (!isOnline())
            return null;

        view = inflater.inflate(R.layout.video_fragment, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.p_bar);
        progressBar_bottom = (ProgressBar) view.findViewById(R.id.p_bar_bottom);
        handler = new MyHandler(this);

        compute();

        return view;
    }

    /**
     * Initialize the UI components and get the recent videos
     */
    private void compute() {
        final VideosFragment fragment = this;

        // video list
        recyclerView = (RecyclerView) view.findViewById(R.id.list_video_galery);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        videoAdapter = new MusicVideoAdapter(context, 2);
        recyclerView.setAdapter(videoAdapter);
        videoAdapter.setOnMediaItemClickListener(new MusicVideoAdapter.OnMediaItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                VideoBean bean = (VideoBean) videoAdapter.getItemAt(position);

                Intent intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra("url", bean.getPreview());
                intent.putExtra("description", bean.getDescription());

                startActivity(intent);
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
                    getActivity().getLoaderManager().initLoader(0, bundle, fragment);

                } else
                    startSearching(keyWord1, keyWord2, loadingPageNumber); // search videos by key
            }
        });


        showProgressBar();
        deleteItems();

        Bundle bundle = new Bundle();
        bundle.putInt("pagenumber", 30);

        getActivity().getLoaderManager().initLoader(0, bundle, this);
    }


    /**
     * Method to get the recent videos
     */
    public void getRecentVideos() {
        if (!isOnline())
            return;

        showProgressBar();
        deleteItems();

        Bundle bundle = new Bundle();
        bundle.putInt("pagenumber", 30);

        getActivity().getLoaderManager().initLoader(0, bundle, this);
    }


    /**
     * It searches the videos by one or two keys
     */
    public void searchVideosByKey(String key1, String key2) {
        keyWord1 = key1;
        keyWord2 = key2;

        showProgressBar();
        deleteItems();
        startSearching(key1, key2, 20);
    }

    private void startSearching(String key1, String key2, int loadingPageNumber) {

        if (key2 != null) {
            Bundle bundle1 = new Bundle();
            bundle1.putString("key", key1);
            bundle1.putInt("pagenumber", loadingPageNumber);
            getActivity().getLoaderManager().initLoader(4, bundle1, this);

            Bundle bundle2 = new Bundle();
            bundle2.putString("key", key2);
            bundle2.putInt("pagenumber", loadingPageNumber);
            getActivity().getLoaderManager().initLoader(3, bundle2, this);
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("key", key1);
            bundle.putInt("pagenumber", loadingPageNumber);
            getActivity().getLoaderManager().initLoader(4, bundle, this);
        }
    }

    /**
     * Start the filter search. The bundle contains alla the users input.
     */
    public void startFilterSearch(Bundle bundle) {
        if (!isOnline())
            return;

        showProgressBar();
        deleteItems();
        getActivity().getLoaderManager().initLoader(5, bundle, this);
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

            case 0:
                data = new LoadData(this, 1, bundle);
                break;
            case 3:
            case 4:
                data = new LoadData(this, 2, bundle);
                break;
            case 5:
                data = new LoadData(this, 3, bundle);
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
                    Toast.makeText(context.getActivity(), "No video was found", Toast.LENGTH_SHORT).show();

                    break;

                case 3:
                    if (context.progressBar.isShown())
                        context.dismissProgressBar();

                    if (context.progressBar_bottom.isShown())
                        context.progressBar_bottom.setVisibility(View.GONE);

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
    private static class LoadData extends AsyncTaskLoader<Void> {
        private static WeakReference<VideosFragment> activity;
        private final Bundle bundle;
        private final int loadingPageNumber;
        private final int type;

        public LoadData(VideosFragment context, int type, Bundle bundle) {
            super(context.getActivity());

            activity = new WeakReference<>(context);
            this.type = type;
            this.loadingPageNumber = bundle.getInt("pagenumber");
            this.bundle = bundle;
        }

        @Override
        public Void loadInBackground() {
            activity.get().videoAdapter.setLoadingType(type);

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

                if (array.size() == 0) {
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            handler.sendEmptyMessage(2);
                        }
                    });
                    return;
                }

                for (JsonElement element : array) {
                    JsonElement json2 = element;
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
                    bundle.putParcelable("bean", vBean);
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

        private void searchVideosByKey(int loadingPageNumber) {
            String urlStr = "https://api.shutterstock.com/v2/videos/search?per_page=";
            urlStr += loadingPageNumber + "&query=";
            urlStr += bundle.getString("key");

            Log.i("url", urlStr);
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
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            handler.sendEmptyMessage(2);
                        }
                    });
                    return;
                }

                for (int i = loadingPageNumber - 20; i < array.size(); i++) {
                    JsonElement json2 = array.get(i);
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
                    bundle.putParcelable("bean", vBean);
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
        private void getRecentVideos(int day, int loadingPageNumber) {
            String urlStr = "https://api.shutterstock.com/v2/videos/search?per_page=";
            urlStr += loadingPageNumber + "&added_date_start=";
            urlStr += Utilities.getDate(day);

            Log.i("url", urlStr);

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
                    getRecentVideos(yesterday, loadingPageNumber);
                    return;
                }

                for (int i = loadingPageNumber - 30; i < array.size(); i++) {
                    JsonElement json2 = array.get(i);
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
                    bundle.putParcelable("bean", vBean);
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

