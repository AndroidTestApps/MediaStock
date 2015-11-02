package com.example.mediastock.util;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.example.mediastock.activities.FilterImageFragment;
import com.example.mediastock.activities.FilterMusicFragment;
import com.example.mediastock.activities.ImagesFragment;
import com.example.mediastock.activities.MusicFragment;
import com.example.mediastock.data.ImageBean;
import com.example.mediastock.data.MusicBean;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

/**
 * Class to fetch the data from the server.
 *
 * @author Dinu
 */
public class DownloadService extends IntentService {
    public static final String IMG_BEAN = "ibean";
    public static final String MUSIC_BEAN = "mbean";
    private ResultReceiver receiver;
    private int type;

    public DownloadService() {
        super("DownloadService");
    }

    /**
     * This is called asynchronously by Android. It downloads the images or music.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        // get the right receiver
        receiver = getReceiver(intent);

        // get the right url
        String url = sortQuery(intent);

        // get images
        if (type == 1) {

            // show progress dialog on the UI
            publishImageResult(null, 2, receiver);
            getImages(url, receiver);

            // or get music
        } else {

            // show progress dialog on the UI
            publishMusicResult(null, 2, receiver);
            getMusic(url, receiver);
        }
    }


    /**
     * Method to decide which receiver we are using. The receiver can be for the image context or music.
     *
     * @param intent the intent
     * @return the right receiver used for this service.
     */
    private ResultReceiver getReceiver(Intent intent) {

        if (intent.getParcelableExtra(ImagesFragment.IMG_RECEIVER) != null) {
            type = 1;
            return intent.getParcelableExtra(ImagesFragment.IMG_RECEIVER);
        } else if (intent.getParcelableExtra(MusicFragment.MUSIC_RECEIVER) != null) {
            type = 2;
            return intent.getParcelableExtra(MusicFragment.MUSIC_RECEIVER);
        } else
            return null;
    }

    /**
     * Method to prepare the url with the parameters.
     *
     * @param intent
     * @return
     */
    private String sortQuery(Intent intent) {
        Bundle bundle = intent.getExtras();

        if (type == 1) {
            String url = "https://@api.shutterstock.com/v2/images/search?safe=true";

            url += "&per_page=" + bundle.getString(FilterImageFragment.PER_PAGE);
            url += "&category=" + bundle.getString(FilterImageFragment.CATEGORY);
            url += "&sort=" + bundle.getString(FilterImageFragment.SORT_BY).toLowerCase();

            if (!bundle.getString(FilterImageFragment.ORIENTATION).equals("All"))
                url += "&orientation=" + bundle.getString(FilterImageFragment.ORIENTATION).toLowerCase();

            return url;

        } else {
            String url = "https://@api.shutterstock.com/v2/audio/search?safe=true";

            if (!bundle.getString(FilterMusicFragment.ARTIST).isEmpty())
                url += "&artist=" + bundle.getString(FilterMusicFragment.ARTIST);

            if (!bundle.getString(FilterMusicFragment.TITLE).isEmpty())
                url += "&title=" + bundle.getString(FilterMusicFragment.TITLE);

            if (!bundle.getString(FilterMusicFragment.GENRE).equals("None"))
                url += "&genre=" + bundle.getString(FilterMusicFragment.GENRE);

            url += "&per_page=" + bundle.getString(FilterMusicFragment.PER_PAGE);

            return url;
        }
    }


    /**
     * Method which downloads the images and the info.
     *
     * @param urlStr   the URL
     * @param receiver the result receiver
     */
    private void getImages(String urlStr, ResultReceiver receiver) {
        InputStream is = null;

        Log.i("url", urlStr);
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
                publishImageResult(null, 3, receiver);
                return;
            }

            int i = 0;
            JsonObject assets;
            for (JsonElement element : array) {
                JsonObject jsonObj = element.getAsJsonObject();
                ImageBean ib = null;

                assets = jsonObj.get("assets") == null ? null : jsonObj.get("assets").getAsJsonObject();

                if (assets != null) {
                    ib = new ImageBean();
                    ib.setId(jsonObj.get("id").getAsInt());
                    ib.setDescription(jsonObj.get("description").getAsString());
                    ib.setIdContributor(jsonObj.get("contributor").getAsJsonObject().get("id").getAsInt());
                    ib.setUrl(assets.get("preview") == null ? null : assets.get("preview").getAsJsonObject().get("url").getAsString());
                    ib.setPos(i);
                }

                i++;

                // update UI
                if (ib != null)
                    publishImageResult(ib, 1, receiver);

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
     * Method which downloads the music from the server.
     *
     * @param urlStr   the URL
     * @param receiver the result receiver
     */
    private void getMusic(String urlStr, ResultReceiver receiver) {
        InputStream is = null;

        Log.i("url", urlStr);

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
                publishMusicResult(null, 3, receiver);
                return;
            }

            int i = 0;
            for (JsonElement element : array) {
                JsonObject jsonObj = element.getAsJsonObject();

                String id = jsonObj.get("id").getAsString();
                String title = jsonObj.get("title").getAsString();
                JsonObject assets = jsonObj.get("assets").getAsJsonObject();
                String preview = assets.get("preview_mp3") == null ? null : assets.get("preview_mp3").getAsJsonObject().get("url").getAsString();

                final MusicBean mBean = new MusicBean();
                mBean.setId(id);
                mBean.setPreview(preview);
                mBean.setTitle(title);
                mBean.setPos(i);

                i++;

                // update the UI
                publishMusicResult(mBean, 1, receiver);
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
     * Method to update the main UI with the music
     *
     * @param bean     music bean
     * @param result   the result of the download operation. 1-success, 2-update with progress dialog 3-failure
     * @param receiver the result receiver
     */
    private void publishMusicResult(MusicBean bean, int result, ResultReceiver receiver) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(MUSIC_BEAN, bean);
        receiver.send(result, bundle);
    }


    /**
     * Method to update the main UI with the image
     *
     * @param bean     image bean
     * @param result   the result of the download operation. 1-success, 2-update with progress dialog 3-failure
     * @param receiver the result receiver
     */
    private void publishImageResult(ImageBean bean, int result, ResultReceiver receiver) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(IMG_BEAN, bean);
        receiver.send(result, bundle);
    }

}
