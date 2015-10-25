package com.example.mediastock.activities;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mediastock.R;
import com.example.mediastock.data.ImageBean;
import com.example.mediastock.util.ImageAdapter;
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
 * Fragment to display the image that the user clicked. It displays the description of the image and the similar images.
 *
 * @author Dinu
 */
public class DisplayImageActivity extends AppCompatActivity implements View.OnClickListener {
    private static Handler handler;
    private ScrollView sw;
    private ImageView imageView;
    private ImageAdapter adapter;
    private RecyclerView recyclerView;
    private TextView description, contributorsName;
    private FloatingActionButton fab_favorites;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if online
        if (!isOnline()) {
            Toast.makeText(this, "Not online", Toast.LENGTH_SHORT).show();
            finish();
        } else {

            setContentView(R.layout.display_image_activity);

            // handle the threads
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            // layouts init
            fab_favorites = (FloatingActionButton) this.findViewById(R.id.fab_favorites);
            fab_favorites.setOnClickListener(this);
            sw = ((ScrollView) findViewById(R.id.scrollViewDisplayImage));
            imageView = (ImageView) this.findViewById(R.id.imageView_displayImage);
            description = (TextView) this.findViewById(R.id.textView_description_displayImage);
            contributorsName = (TextView) this.findViewById(R.id.TextView_contributor_displayImage);

            // main image
            RelativeLayout relativeLayout = (RelativeLayout) this.findViewById(R.id.Rel_layout);
            ViewGroup.LayoutParams param = relativeLayout.getLayoutParams();
            param.height = getResources().getDisplayMetrics().widthPixels + 5;
            relativeLayout.setLayoutParams(param);

            // similar images
            recyclerView = (RecyclerView) this.findViewById(R.id.image_home_ScrollView);
            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerView.setLayoutManager(llm);
            ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
            params.height = (getResources().getDisplayMetrics().widthPixels / 3) + 2;
            recyclerView.setLayoutParams(params);
            adapter = new ImageAdapter(this, 2);
            recyclerView.setAdapter(adapter);
            adapter.setOnImageClickListener(new ImageAdapter.OnImageClickListener() {
                @Override
                public void onImageClick(View view, int position) {

                    updateUI(adapter.getBeanAt(position));
                }
            });

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            // to handle the UI updates
            handler = new MyHandler(this);

            // get main image
            getMainImage(getBeanFromIntent());

            // get the authors name
            DownloadThread thread1 = new DownloadThread(1);
            thread1.setAuthorID(getBeanFromIntent().getIdContributor());
            new Thread(thread1).start();

            // get the similar images
            DownloadThread thread2 = new DownloadThread(2);
            thread2.setImageID(getBeanFromIntent().getId());
            new Thread(thread2).start();
        }
    }

    /**
     * Favorites action button
     *
     * @param v the button
     */
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.fab_favorites) {
            fab_favorites.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            Toast.makeText(this, "Image added to favorites", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imageView.setImageDrawable(null);
    }

    private ImageBean getBeanFromIntent() {
        return getIntent().getBundleExtra("bean").getParcelable("bean");
    }


    /**
     * It downloads the main image from the server
     *
     * @param bean the image bean
     */
    private void getMainImage(ImageBean bean) {
        sw.fullScroll(View.FOCUS_UP);

        if (bean.getUrl() != null)
            Picasso.with(getApplicationContext()).load(bean.getUrl()).placeholder(R.drawable.border).fit().centerInside().into(imageView);

        // set description of the image
        description.append(" " + bean.getDescription());
    }

    /**
     * Method which updates the UI with the new image, authors name and similar images
     *
     * @param bean the image bean
     */
    private void updateUI(ImageBean bean) {
        // get image
        getMainImage(bean);

        // get the authors name
        DownloadThread thread1 = new DownloadThread(1);
        thread1.setAuthorID(bean.getIdContributor());
        new Thread(thread1).start();

        // get the similar images to the current image
        DownloadThread thread2 = new DownloadThread(2);
        thread2.setImageID(bean.getId());
        new Thread(thread2).start();
    }


    /**
     * Checks if the device is connected to the Internet
     *
     * @return true if connected, false otherwise
     */
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    /**
     * Handler to update the UI
     */
    private static class MyHandler extends Handler {
        private static WeakReference<DisplayImageActivity> activity;

        public MyHandler(DisplayImageActivity context) {
            activity = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            DisplayImageActivity context = activity.get();

            switch (msg.what) {

                // update the UI with the authors name
                case 1:
                    context.contributorsName.append(" " + msg.getData().getString("name"));
                    break;

                // update the UI with the similar images
                case 2:
                    context.adapter.addItem((ImageBean) msg.getData().getParcelable("bean"));
                    break;

                // remove the old similar images
                case 3:
                    context.adapter.deleteItems();
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * Static inner class to download the authors name and the similar images.
     *
     * @author Dinu
     */
    private static class DownloadThread implements Runnable {
        private final int type;
        private int authorID;
        private int imageID;

        /**
         * The constructor.
         *
         * @param type 1-get authors name, 2-get similar images
         */
        public DownloadThread(int type) {
            this.type = type;
        }

        @Override
        public void run() {

            switch (type) {

                case 1:
                    getAuthor(authorID);
                    break;

                case 2:
                    getSimilarImages(imageID);
                    break;

                default:
                    break;
            }

        }


        private void getAuthor(int id) {
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
                author = json.getAsJsonObject().get("display_name") == null ? " - " : json.getAsJsonObject().get("display_name").getAsString();

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

            final Message msg = new Message();
            final Bundle bundle = new Bundle();
            bundle.putString("name", author);
            msg.setData(bundle);
            msg.what = 1;

            // update the UI with the author
            handler.post(new Runnable() {

                @Override
                public void run() {

                    handler.dispatchMessage(msg);
                }
            });
        }


        private void getSimilarImages(int id) {
            String urlStr = "https://@api.shutterstock.com/v2/images/";
            urlStr += id + "/similar?per_page=6";

            // remove the old similar images
            handler.post(new Runnable() {

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
                Iterator<JsonElement> iterator = array.iterator();
                while (iterator.hasNext()) {
                    JsonObject jsonObj = iterator.next().getAsJsonObject();
                    ImageBean ib = null;

                    assets = jsonObj.get("assets") == null ? null : jsonObj.get("assets").getAsJsonObject();

                    if (assets != null) {
                        ib = new ImageBean();
                        ib.setId(jsonObj.get("id") == null ? null : jsonObj.get("id").getAsInt());
                        ib.setDescription(jsonObj.get("description") == null ? null : jsonObj.get("description").getAsString());
                        ib.setIdContributor(jsonObj.get("contributor") == null ? null : jsonObj.get("contributor").getAsJsonObject().get("id").getAsInt());
                        ib.setUrl(assets.get("preview") == null ? null : assets.get("preview").getAsJsonObject().get("url").getAsString());
                    }

                    if (ib != null) {

                        // update the UI with the image
                        final Bundle bundle = new Bundle();
                        final Message msg = new Message();
                        bundle.putParcelable("bean", ib);
                        msg.setData(bundle);
                        msg.what = 2;

                        // update the UI with the new similar images
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                handler.dispatchMessage(msg);
                            }
                        });
                    }
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

        private void setImageID(int imageID) {
            this.imageID = imageID;
        }

        private void setAuthorID(int authorID) {
            this.authorID = authorID;
        }

    }
}

