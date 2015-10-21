package com.example.mediastock.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.mediastock.R;
import com.example.mediastock.data.ImageBean;
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
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Activity which displays a gridView with images.
 *
 * @author Dinu
 */
public class ImagesFragment extends AbstractFragment implements DownloadResultReceiver.Receiver {
    public static final String IMG_RECEIVER = "ireceiver";
    private static Context context;
    private ImageAdapter imgAdapter;
    private ArrayList<ImageBean> images = new ArrayList<>();
    private DownloadResultReceiver resultReceiver;
    private View view;
    private ProgressBar progressBar;
    private LinearLayout layout_progress_bar;
    private GridView grid;


    /**
     * Method to create an instance of this fragment for the viewPager.
     */
    public static ImagesFragment createInstance() {
        return new ImagesFragment();
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

        view = inflater.inflate(R.layout.images_fragment, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.p_img_bar);
        layout_progress_bar = (LinearLayout) view.findViewById(R.id.layout_img_pBar);

        compute();

        return view;
    }

    /**
     * Initialize the UI components and get the recent images
     */
    private void compute() {

        // grid images
        grid = (GridView) view.findViewById(R.id.gridView_displayImage);
        imgAdapter = new ImageAdapter(context, images);
        grid.setAdapter(imgAdapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int pos, long arg3) {
                goToDisplayImageActivity(images.get(pos));
            }
        });

        deleteItems();
        new WebRequest(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "getImages");
    }


    /**
     * Method to get the recent images
     */
    public void getRecentImages() {
        if (!isOnline())
            return;

        showProgressBar();
        deleteItems();
        new WebRequest(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "getImages");
    }

    /**
     * It searches the images by one or two keys
     */
    public void searchImagesByKey(String key1, String key2) {
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
        intent.putExtra(IMG_RECEIVER, resultReceiver);
        intent.putExtra(FilterImageFragment.CATEGORY, bundle.getString(FilterImageFragment.CATEGORY));
        intent.putExtra(FilterImageFragment.ORIENTATION, bundle.getString(FilterImageFragment.ORIENTATION));
        intent.putExtra(FilterImageFragment.SORT_BY, bundle.getString(FilterImageFragment.SORT_BY));
        intent.putExtra(FilterImageFragment.PER_PAGE, bundle.getString(FilterImageFragment.PER_PAGE));

        getActivity().startService(intent);
    }


    /**
     * Method to delete the list of images and to notify the adapter
     */
    private void deleteItems() {
        if(!images.isEmpty()) {
            images.clear();
            imgAdapter.notifyDataSetChanged();
        }
    }


    /**
     * It shows the progress bar
     */
    private void showProgressBar() {
        grid.setVisibility(View.GONE);
        layout_progress_bar.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Method to dismiss the progress bar
     */
    private void dismissProgressBar() {
        progressBar.setVisibility(View.GONE);
        layout_progress_bar.setVisibility(View.GONE);
        grid.setVisibility(View.VISIBLE);
    }


    /**
     * Method which redirects the user to DisplayImageActivity
     *
     * @param bean the image bean
     */
    private void goToDisplayImageActivity(ImageBean bean) {
        Intent intent = new Intent(context, DisplayImageActivity.class);

        intent.putExtra("id", bean.getId());
        intent.putExtra("description", bean.getDescription());
        intent.putExtra("url", bean.getUrl());
        intent.putExtra("contributor", bean.getIdContributor());

        startActivity(intent);
    }

    /**
     * It handles the result of the DownloadService service.
     * Method used when we do a filter search. It updates the UI.
     */
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 1:
                if (progressBar.isShown())
                    dismissProgressBar();

                ImageBean bean = resultData.getParcelable(DownloadService.IMG_BEAN);

                // update UI with the image
                images.add(bean);
                imgAdapter.notifyDataSetChanged();
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
     * Static inner class to search for images and to get the recent images from the server.
     *
     * @author Dinu
     */
    private static class WebRequest extends AsyncTask<String, ImageBean, String> {
        private static WeakReference<ImagesFragment> activity;
        private boolean searchSuccess = true;

        public WebRequest(ImagesFragment activity) {
            WebRequest.activity = new WeakReference<>(activity);
        }

        @Override
        protected String doInBackground(String... params) {

            if (params[0].equals("getImages"))
                getRecentImages(0);
            else {
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
        private void searchImagesByKey(String key) {
            String urlStr = "https://@api.shutterstock.com/v2/images/search?per_page=100&query=";
            urlStr += key;

            InputStream is = null;
            try {
                URL url = new URL(urlStr);
                URLConnection conn = url.openConnection();
                conn.setRequestProperty("Authorization", "Basic " + Utilities.getLicenseKey());

                // get stream
                is = conn.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                String jsonText = Utilities.readAll(rd);

                // parse json text
                JsonElement json = new JsonParser().parse(jsonText);
                JsonObject o = json.getAsJsonObject();
                JsonArray array = o.get("data").getAsJsonArray();

                JsonObject assets;
                JsonObject preview;

                if (array.size() == 0) {
                    searchSuccess = false;
                    return;
                }

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

                    // update UI
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
         * Method to get the recent images.
         * We get the image, description of the image, id of the image, url for the preview, and id of the contributor.
         */
        private void getRecentImages(int day) {
            String urlStr = "https://@api.shutterstock.com/v2/images/search?per_page=100&added_date=";
            urlStr += Utilities.getDate(day);

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
                    int yesterday = day;
                    yesterday += 1;
                    getRecentImages(yesterday);
                    return;
                }

                JsonObject assets;
                JsonObject preview;

                Iterator<JsonElement> iterator = array.iterator();
                while (iterator.hasNext()) {
                    JsonElement json2 = iterator.next();
                    ImageBean ib = new ImageBean();

                    assets = json2.getAsJsonObject().get("assets").getAsJsonObject();
                    preview = assets.get("preview").getAsJsonObject();

                    ib.setId(json2.getAsJsonObject().get("id").getAsInt());
                    ib.setDescription(json2.getAsJsonObject().get("description").getAsString());
                    ib.setIdContributor(json2.getAsJsonObject().get("contributor").getAsJsonObject().get("id").getAsInt());
                    ib.setUrl(preview.get("url").getAsString());

                    // update UI
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
         * We update the UI
         */
        @Override
        protected void onProgressUpdate(ImageBean... bean) {
            if (activity.get().progressBar.isShown())
                activity.get().dismissProgressBar();

            if (bean[0] == null)
                Toast.makeText(context, "No image was found", Toast.LENGTH_SHORT).show();
            else {
                activity.get().images.add(bean[0]);
                activity.get().imgAdapter.notifyDataSetChanged();
            }
        }


        @Override
        protected void onPostExecute(String result) {

            if (!searchSuccess) {
                activity.get().dismissProgressBar();
                Toast.makeText(context, "Sorry, no image with " + result + " was found!", Toast.LENGTH_LONG).show();
            }
        }
    }


}
