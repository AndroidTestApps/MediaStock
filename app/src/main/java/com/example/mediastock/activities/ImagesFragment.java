package com.example.mediastock.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


/**
 * Activity which displays a gridView with images.
 *
 * @author Dinu
 */
public class ImagesFragment extends AbstractFragment implements DownloadResultReceiver.Receiver {
    public static final String IMG_RECEIVER = "ireceiver";
    private static String keyWord1;
    private static String keyWord2;
    private static Context context;
    private DownloadResultReceiver resultReceiver;
    private View view;
    private ProgressBar progressBar;
    private ProgressBar progressBar_bottom;
    private RecyclerView recyclerView;
    private ImageAdapter adapter;


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
        progressBar_bottom = (ProgressBar) view.findViewById(R.id.p_img_bar_bottom);
        progressBar_bottom.getIndeterminateDrawable().setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);

        compute();

        return view;
    }

    /**
     * Initialize the UI components and get the recent images
     */
    private void compute() {
        final ImagesFragment fragment = this;

        // grid images
        recyclerView = (RecyclerView) view.findViewById(R.id.gridView_displayImage);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager grid = new GridLayoutManager(context, 2);
        recyclerView.setLayoutManager(grid);
        adapter = new ImageAdapter(context, 1);
        recyclerView.setAdapter(adapter);
        adapter.setOnImageClickListener(new ImageAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(View view, int position) {

                goToDisplayImageActivity(adapter.getBeanAt(position));
            }
        });

        // endless list. load more data when reaching the bottom of the view
        adapter.setOnBottomListener(new ImageAdapter.OnBottomListener() {
            @Override
            public void onBottomLoadMoreData(int loadingType, int loadingPageNumber) {
                progressBar_bottom.setVisibility(View.VISIBLE);

                // recent images
                if (loadingType == 1)
                    new WebRequest(fragment, 1, loadingPageNumber).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                else
                    startSearching(keyWord1, keyWord2, loadingPageNumber);  // search images by key
            }
        });

        deleteItems();
        new WebRequest(this, 1, 50).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    /**
     * Method to get the recent images
     */
    public void getRecentImages() {
        if (!isOnline())
            return;

        showProgressBar();
        deleteItems();
        new WebRequest(this, 1, 50).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * It searches the images by one or two keys
     */
    public void searchImagesByKey(String key1, String key2) {
        keyWord1 = key1;
        keyWord2 = key2;

        showProgressBar();
        deleteItems();
        startSearching(key1, key2, 30);
    }

    private void startSearching(String key1, String key2, int loadingPageNumber) {
        if (key2 != null) {
            new WebRequest(this, 2, loadingPageNumber).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, key1);
            new WebRequest(this, 2, loadingPageNumber).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, key2);

        } else
            new WebRequest(this, 2, loadingPageNumber).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, key1);
    }

    /**
     * Start the filter search. The bundle contains alla the users input.
     * We pass all the info to DownloadService service to start to download the images.
     */
    public void startFilterSearch(Bundle bundle) {
        if (!isOnline())
            return;

        adapter.setLoadingType(3);
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
        adapter.deleteItems();
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
     * Method which redirects the user to DisplayImageActivity
     *
     * @param bean the image bean
     */
    private void goToDisplayImageActivity(ImageBean bean) {
        Intent intent = new Intent(context, DisplayImageActivity.class);

        Bundle bundle = new Bundle();
        bundle.putParcelable("bean", bean);
        intent.putExtra("bean", bundle);

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
                adapter.addItem(bean);

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
        private final int type;
        private final int loadingPageNumber;
        private boolean searchSuccess = true;

        public WebRequest(ImagesFragment activity, int type, int loadingPageNumber) {
            this.type = type;
            this.loadingPageNumber = loadingPageNumber;
            WebRequest.activity = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            activity.get().adapter.setLoadingType(type);
            activity.get().adapter.setPageNumber(loadingPageNumber);
        }

        @Override
        protected String doInBackground(String... params) {

            if (type == 1)
                getRecentImages(0, loadingPageNumber);
            else {
                searchImagesByKey(params[0], loadingPageNumber);

                return params[0];
            }

            return null;
        }

        /**
         * Method to search images by a key
         *
         * @param key the key
         */
        private void searchImagesByKey(String key, int loadingPageNumber) {
            String urlStr = "https://@api.shutterstock.com/v2/images/search?per_page=";
            urlStr += loadingPageNumber + "&query=";
            urlStr += key;

            Log.i("url", urlStr + "\n");

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

                if (array.size() == 0) {
                    searchSuccess = false;
                    return;
                }

                JsonObject assets;

                // get objects
                for (int i = loadingPageNumber - 30; i < array.size(); i++) {
                    JsonObject jsonObj = array.get(i).getAsJsonObject();
                    ImageBean ib = null;

                    assets = jsonObj.get("assets") == null ? null : jsonObj.get("assets").getAsJsonObject();

                    if (assets != null) {
                        ib = new ImageBean();
                        ib.setId(jsonObj.get("id") == null ? null : jsonObj.get("id").getAsInt());
                        ib.setDescription(jsonObj.get("description") == null ? null : jsonObj.get("description").getAsString());
                        ib.setIdContributor(jsonObj.get("contributor") == null ? null : jsonObj.get("contributor").getAsJsonObject().get("id").getAsInt());
                        ib.setUrl(assets.get("preview") == null ? null : assets.get("preview").getAsJsonObject().get("url").getAsString());
                    }

                    // update UI
                    if (ib != null)
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
         * We get the description of the image, id of the image, url for the preview, and id of the contributor.
         */
        private void getRecentImages(int day, int loadingPageNumber) {
            String urlStr = "https://@api.shutterstock.com/v2/images/search?per_page=";
            urlStr += loadingPageNumber + "&added_date=";
            urlStr += Utilities.getDate(day);

            Log.i("url", urlStr);

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
                    getRecentImages(yesterday, loadingPageNumber);
                    return;
                }

                JsonObject assets;
                for (int i = loadingPageNumber - 50; i < array.size(); i++) {
                    JsonObject jsonObj = array.get(i).getAsJsonObject();
                    ImageBean ib = null;
                    assets = jsonObj.get("assets") == null ? null : jsonObj.get("assets").getAsJsonObject();

                    if (assets != null) {
                        ib = new ImageBean();
                        ib.setId(jsonObj.get("id") == null ? null : jsonObj.get("id").getAsInt());
                        ib.setDescription(jsonObj.get("description") == null ? null : jsonObj.get("description").getAsString());
                        ib.setIdContributor(jsonObj.get("contributor") == null ? null : jsonObj.get("contributor").getAsJsonObject().get("id").getAsInt());
                        ib.setUrl(assets.get("preview") == null ? null : assets.get("preview").getAsJsonObject().get("url").getAsString());
                    }

                    // update UI
                    if (ib != null)
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

            if (activity.get().progressBar_bottom.isShown())
                activity.get().progressBar_bottom.setVisibility(View.GONE);

            activity.get().adapter.addItem(bean[0]);
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
