package com.example.mediastock.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.mediastock.R;
import com.example.mediastock.beans.Bean;
import com.example.mediastock.beans.MusicBean;
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
public class MusicFragment extends AbstractFragment implements DownloadResultReceiver.Receiver {
    public static final String MUSIC_RECEIVER = "mreceiver";
    private static Context context;
    private DownloadResultReceiver resultReceiver;
    private View view = null;
    private ProgressBar p_bar;
    private LinearLayout layout_p_bar;
    private RecyclerView recyclerView;
    private MusicVideoAdapter musicAdapter;

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

        if (!isOnline())
            return null;

        view = inflater.inflate(R.layout.music_fragment, container, false);
        p_bar = (ProgressBar) view.findViewById(R.id.p_bar);
        layout_p_bar = (LinearLayout) view.findViewById(R.id.layout_pBar);

        compute();

        return view;
    }

    /**
     * Initialize the UI components and get the recent music
     */
    private void compute() {

        // music list
        recyclerView = (RecyclerView) view.findViewById(R.id.list_music_galery);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        musicAdapter = new MusicVideoAdapter(context, 1);
        recyclerView.setAdapter(musicAdapter);
        musicAdapter.SetOnItemClickListener(new MusicVideoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                MusicBean b = (MusicBean) musicAdapter.getItemAt(position);
                Intent intent = new Intent(context, MusicPlayerActivity.class);
                intent.putExtra("url", b.getPreview());
                intent.putExtra("title", b.getTitle());

                startActivity(intent);
            }
        });

        showProgressBar();
        deleteItems();
        new WebRequest(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "getmusic");

    }

    /**
     * Method to get the recent music
     */
    public void getRecentMusic() {
        if (!isOnline())
            return;

        // show progress
        showProgressBar();
        deleteItems();

        new WebRequest(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "getmusic");
    }

    /**
     * It searches the music by one or two keys
     */
    public void searchMusicByKey(String key1, String key2) {
        showProgressBar();
        deleteItems();

        if (key2 != null) {
            new WebRequest(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "search", key1);
            new WebRequest(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "search", key2);
        } else
            new WebRequest(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "search", key1);
    }


    /**
     * Start the filter search. The bundle contains alla the users input.
     * We pass all the info to DownloadService service to start to download the images.
     */
    public void startFilterSearch(Bundle bundle) {
        if (!isOnline())
            return;

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
        recyclerView.setVisibility(View.GONE);
        layout_p_bar.setVisibility(View.VISIBLE);
        p_bar.setVisibility(View.VISIBLE);
    }

    /**
     * Method to dismiss the progress bar
     */
    private void dismissProgressBar() {
        p_bar.setVisibility(View.GONE);
        layout_p_bar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }


    /**
     * It handles the result of the DownloadService service. It updates the UI.
     */
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {

            case 1:
                dismissProgressBar();

                MusicBean bean = resultData.getParcelable(DownloadService.MUSIC_BEAN);

                // update UI with the music
                musicAdapter.addItem(bean);
                break;

            case 2:
                showProgressBar();
                break;

            default:
                Toast.makeText(context, "Search failed", Toast.LENGTH_LONG).show();
                break;
        }

    }

    /**
     * Static inner class to search for music and to get the recent music from the server.
     *
     * @author Dinu
     */
    private static class WebRequest extends AsyncTask<String, Bean, String> {
        private static WeakReference<MusicFragment> activity;
        private boolean searchSuccess = true;

        public WebRequest(MusicFragment activity) {
            WebRequest.activity = new WeakReference<>(activity);
        }


        @Override
        protected String doInBackground(String... params) {

            if (params[0].equals("getmusic"))
                getRecentMusic();
            else {
                searchMusicByKey(params[1]);
                return params[1];
            }

            return null;
        }

        /**
         * Method to get the music from the server. It gets the id, the title and the url preview.
         */
        private void getRecentMusic() {
            String urlStr = "https://api.shutterstock.com/v2/audio/search?per_page=50&added_date_start=2013-01-01";

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

                if (array.size() < 1) {
                    publishProgress((MusicBean) null);
                    return;
                }

                Iterator<JsonElement> iterator = array.iterator();
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
         * Method to search music by one or two keys or the union.
         *
         * @param key the key
         */
        private void searchMusicByKey(String key) {
            String urlStr = "https://@api.shutterstock.com/v2/audio/search?per_page=100&title=";
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

                if (array.size() == 0) {
                    searchSuccess = false;
                    return;
                }

                Iterator<JsonElement> iterator = array.iterator();
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
         * We update the UI
         */
        @Override
        protected void onProgressUpdate(Bean... bean) {
            if (activity.get().p_bar.isShown())
                activity.get().dismissProgressBar();

            if (bean[0] == null)
                Toast.makeText(activity.get().getActivity(), "No music was found", Toast.LENGTH_SHORT).show();
            else
                activity.get().musicAdapter.addItem(bean[0]);

        }


        @Override
        protected void onPostExecute(String result) {

            if (!searchSuccess) {
                activity.get().dismissProgressBar();
                Toast.makeText(activity.get().getActivity(), "Sorry, no music with " + result + " was found!", Toast.LENGTH_LONG).show();
            }
        }

    }


}
