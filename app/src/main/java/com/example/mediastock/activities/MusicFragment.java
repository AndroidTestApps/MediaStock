package com.example.mediastock.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.mediastock.R;
import com.example.mediastock.data.Bean;
import com.example.mediastock.data.MusicBean;
import com.example.mediastock.util.DownloadResultReceiver;
import com.example.mediastock.util.DownloadService;
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
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Fragment which displays a list of music
 *
 * @author Dinu
 */
public class MusicFragment extends AbstractFragment implements DownloadResultReceiver.Receiver {
    public static final String MUSIC_RECEIVER = "mreceiver";
    // the keywords to search for
    private static String keyWord1;
    private static String keyWord2;
    private static Context context;
    private static boolean working = false;
    private DownloadResultReceiver resultReceiver;
    private ImageView buttonRefresh;
    private ProgressBar progressBar;
    private ProgressBar progressbar_bottom;
    private RecyclerView recyclerView;
    private MusicVideoAdapter musicAdapter;
    private View view;


    /**
     * Method to create an instance of this fragment for the viewPager
     */
    public static MusicFragment createInstance() {
        return new MusicFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getActivity().getApplicationContext();

        view = inflater.inflate(R.layout.music_video_fragment, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        compute();
    }

    /**
     * Method to initialize the UI components and to get the recent music.
     */
    private void compute() {
        final MusicFragment fragment = this;

        buttonRefresh = (ImageView) view.findViewById(R.id.buttonRefreshInternet);

        if (Utilities.deviceOnline(context))
            buttonRefresh.setVisibility(View.GONE);

        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!Utilities.deviceOnline(context))
                    Toast.makeText(context.getApplicationContext(), "There is no internet connection", Toast.LENGTH_SHORT).show();
                else {
                    buttonRefresh.setVisibility(View.GONE);
                    getRecentMusic();
                }
            }
        });
        progressBar = (ProgressBar) view.findViewById(R.id.p_bar);
        progressbar_bottom = (ProgressBar) view.findViewById(R.id.p_bar_bottom);
        progressbar_bottom.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_gallery);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        musicAdapter = new MusicVideoAdapter(context, 1, false);
        recyclerView.setAdapter(musicAdapter);

        // on item click
        musicAdapter.setOnItemClickListener(new MusicVideoAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {

                goToMusicPLayerActivity(position);
            }
        });

        // endless list which loads more data when reaching the bottom of the view
        musicAdapter.setOnBottomListener(new MusicVideoAdapter.OnBottomListener() {

            @Override
            public void onBottomLoadMoreData(int loadingType, int loadingPageNumber) {
                progressbar_bottom.setVisibility(View.VISIBLE);

                // type 1 : recent music
                if (loadingType == 1)
                    new AsyncWork(fragment, 1, loadingPageNumber).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    // type 2 : search music by key
                else
                    new AsyncWork(fragment, 2, loadingPageNumber).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, keyWord1, keyWord2);
            }
        });

        getRecentMusic();
    }

    /**
     * Open MusicPlayerActivity activity
     */
    private void goToMusicPLayerActivity(int position) {
        if (!Utilities.deviceOnline(context)) {
            Toast.makeText(context.getApplicationContext(), "Not online", Toast.LENGTH_SHORT).show();
            return;
        }

        final Bundle bundle = new Bundle();
        final MusicBean bean = (MusicBean) musicAdapter.getBeanAt(position);

        Intent intent = new Intent(context, MusicPlayerActivity.class);
        bundle.putParcelable("bean", bean);
        intent.putExtra("bean", bundle);

        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);
    }


    /**
     * Method to get the recent music asynchronously
     */
    public void getRecentMusic() {
        if (!Utilities.deviceOnline(context) || working)
            return;

        showProgressBar();
        deleteItems();
        new AsyncWork(this, 1, 30).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * It searches asynchronously the music by one or two keys
     */
    public void searchMusicByKey(String key1, String key2) {
        if (working)
            return;

        keyWord1 = key1;
        keyWord2 = key2;

        showProgressBar();
        deleteItems();
        new AsyncWork(this, 2, 30).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, key1, key2);
    }


    /**
     * Start the filter search asynchronously. The bundle contains alla the users input.
     * We pass all the info to DownloadService service to start to download the images.
     */
    public void startFilterSearch(Bundle bundle) {
        if (!Utilities.deviceOnline(context) || working)
            return;

        musicAdapter.setLoadingType(3);
        showProgressBar();
        deleteItems();

        resultReceiver = new DownloadResultReceiver(new Handler());
        resultReceiver.setReceiver(this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, context, DownloadService.class);

        // query info
        intent.putExtra(MUSIC_RECEIVER, resultReceiver);
        intent.putExtra(FilterMusicFragment.ARTIST, bundle.getString(FilterMusicFragment.ARTIST));
        intent.putExtra(FilterMusicFragment.TITLE, bundle.getString(FilterMusicFragment.TITLE));
        intent.putExtra(FilterMusicFragment.GENRE, bundle.getString(FilterMusicFragment.GENRE));
        intent.putExtra(FilterMusicFragment.PER_PAGE, bundle.getString(FilterMusicFragment.PER_PAGE));

        getActivity().startService(intent);
    }


    /**
     * Method to delete the list of music and to notify the adapter
     */
    private void deleteItems() {
        musicAdapter.deleteItems();
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
     * It handles the result of the DownloadService service. It updates the UI.
     */
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {

            case 1:
                if (progressBar.isShown())
                    dismissProgressBar();

                MusicBean bean = resultData.getParcelable(DownloadService.MUSIC_BEAN);

                // update UI with the music
                musicAdapter.addItem(bean);
                break;

            case 2:
                showProgressBar();
                break;

            default:
                dismissProgressBar();
                Toast.makeText(context, "Search failed", Toast.LENGTH_LONG).show();
                break;
        }

    }

    /**
     * Static inner class to do asynchronous operations.
     * This class is used to search for music and to get the recent music from the server.
     *
     * @author Dinu
     */
    private static class AsyncWork extends AsyncTask<String, Bean, String> {
        private static WeakReference<MusicFragment> activity;
        private final int type;
        private final int loadingPageNumber;
        private boolean searchSuccess = true;

        public AsyncWork(MusicFragment activity, int type, int loadingPageNumber) {
            AsyncWork.activity = new WeakReference<>(activity);
            this.type = type;
            this.loadingPageNumber = loadingPageNumber;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            activity.get().working = true;
            activity.get().musicAdapter.setLoadingType(type);
            activity.get().musicAdapter.setPageNumber(loadingPageNumber);
        }

        @Override
        protected String doInBackground(String... params) {

            if (type == 1)
                getRecentMusic(0, loadingPageNumber);
            else {
                searchMusicByKey(params[0], params[1], loadingPageNumber);

                return params[1] != null ? params[0] + " " + params[1] : params[0];
            }

            return null;
        }

        /**
         * Method to get the recent music
         *
         * @param day it represents the day
         * @param loadingPageNumber the number of items to get
         */
        private void getRecentMusic(int day, int loadingPageNumber) {
            String urlStr = "https://api.shutterstock.com/v2/audio/search?per_page=";
            urlStr += loadingPageNumber + "&added_date_start=";
            urlStr += Utilities.getDate(day);

            Log.i("url", urlStr);

            InputStream is = null;
            HttpURLConnection con = null;
            try {
                URL url = new URL(urlStr);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestProperty("Authorization", "Basic " + Utilities.getLicenseKey());
                con.setConnectTimeout(25000);
                con.setReadTimeout(25000);

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    is = con.getInputStream();

                    BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                    String jsonText = Utilities.readAll(rd);
                    rd.close();

                    JsonElement json = new JsonParser().parse(jsonText);
                    JsonObject o = json.getAsJsonObject();
                    JsonArray array = o.get("data").getAsJsonArray();

                    if (array.size() < 1) {
                        int yesterday = day;
                        yesterday += 1;
                        con.disconnect();

                        getRecentMusic(yesterday, loadingPageNumber);
                        return;
                    }

                    for (int i = loadingPageNumber - 30; i < array.size(); i++) {
                        JsonObject jsonObj = array.get(i).getAsJsonObject();
                        JsonObject assets = jsonObj.get("assets").getAsJsonObject();
                        final MusicBean bean = new MusicBean();

                        if (assets != null) {
                            bean.setId(jsonObj.get("id") == null ? null : jsonObj.get("id").getAsString());
                            bean.setTitle(jsonObj.get("title") == null ? null : jsonObj.get("title").getAsString());
                            bean.setPreview(assets.get("preview_mp3") == null ? null : assets.get("preview_mp3").getAsJsonObject().get("url").getAsString());
                        }

                        bean.setPos(i);

                        // update the UI
                        publishProgress(bean);
                    }
                }
            } catch (SocketTimeoutException e) {
                if (con != null)
                    con.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null)
                        is.close();

                    if (con != null)
                        con.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Method to search music by one or two keys
         *
         * @param key1 the first key
         * @param key2 the second key
         * @param loadingPageNumber the number of items to get
         */
        private void searchMusicByKey(String key1, String key2, int loadingPageNumber) {
            String urlStr = "https://@api.shutterstock.com/v2/audio/search?per_page=";
            urlStr += loadingPageNumber + "&query=";

            if (key2 != null)
                urlStr += key1 + "/" + key2;
            else
                urlStr += key1;

            Log.i("url", urlStr);

            InputStream is = null;
            HttpURLConnection con = null;
            try {
                URL url = new URL(urlStr);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestProperty("Authorization", "Basic " + Utilities.getLicenseKey());
                con.setConnectTimeout(25000);
                con.setReadTimeout(25000);

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    is = con.getInputStream();

                    BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                    String jsonText = Utilities.readAll(rd);
                    rd.close();

                    JsonElement json = new JsonParser().parse(jsonText);
                    JsonObject o = json.getAsJsonObject();
                    JsonArray array = o.get("data").getAsJsonArray();

                    if (array.size() == 0) {
                        searchSuccess = false;

                        con.disconnect();
                        return;
                    }

                    for (int i = loadingPageNumber - 30; i < array.size(); i++) {
                        JsonObject jsonObj = array.get(i).getAsJsonObject();
                        JsonObject assets = jsonObj.get("assets").getAsJsonObject();
                        final MusicBean bean = new MusicBean();

                        if (assets != null) {
                            bean.setId(jsonObj.get("id") == null ? null : jsonObj.get("id").getAsString());
                            bean.setTitle(jsonObj.get("title") == null ? null : jsonObj.get("title").getAsString());
                            bean.setPreview(assets.get("preview_mp3") == null ? null : assets.get("preview_mp3").getAsJsonObject().get("url").getAsString());
                        }

                        bean.setPos(i);

                        // update the UI
                        publishProgress(bean);
                    }
                }
            } catch (SocketTimeoutException e) {
                if (con != null)
                    con.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    // clean resources
                    if (is != null)
                        is.close();

                    if (con != null)
                        con.disconnect();
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
            if (activity.get().progressBar.isShown())
                activity.get().dismissProgressBar();

            if (activity.get().progressbar_bottom.isShown())
                activity.get().progressbar_bottom.setVisibility(View.GONE);

            activity.get().musicAdapter.addItem(bean[0]);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (!searchSuccess) {
                activity.get().dismissProgressBar();
                Toast.makeText(activity.get().getActivity(), "Sorry, no music with " + result + " was found!", Toast.LENGTH_LONG).show();
            }

            activity.get().working = false;
            activity = null;
        }
    }
}
