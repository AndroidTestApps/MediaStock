package com.example.mediastock.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.example.mediastock.data.Database;
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
    private int image_id;
    private int width;
    private boolean imageDB = false;
    private ScrollView sw;
    private ImageView imageView;
    private ImageAdapter adapter;
    private RecyclerView recyclerView;
    private TextView description, author, similarImg;
    private FloatingActionButton fab_favorites;
    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_image_activity);
        width = getResources().getDisplayMetrics().widthPixels;

        db = new Database(this);

        // handle the threads
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // layouts init
        fab_favorites = (FloatingActionButton) this.findViewById(R.id.fab_favorites);
        fab_favorites.setOnClickListener(this);
        sw = ((ScrollView) findViewById(R.id.scrollViewDisplayImage));
        imageView = (ImageView) this.findViewById(R.id.imageView_displayImage);
        description = (TextView) this.findViewById(R.id.textView_description_displayImage);
        similarImg = (TextView) this.findViewById(R.id.TextView_similar_displayImage);
        author = (TextView) this.findViewById(R.id.TextView_contributor_displayImage);
        recyclerView = (RecyclerView) this.findViewById(R.id.image_home_ScrollView);

        // main image layout
        RelativeLayout relativeLayout = (RelativeLayout) this.findViewById(R.id.Rel_layout);
        ViewGroup.LayoutParams param = relativeLayout.getLayoutParams();
        param.height = width;
        relativeLayout.setLayoutParams(param);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DisplayImageActivity.this, FullViewImageActivity.class);
                intent.putExtra("image", (String) imageView.getTag());
                startActivity(intent);
            }
        });

        // if offline
        if (getIntentType() == 2)
            computeOfflineWork();
        else
            computeOnlineWork();
    }

    /**
     * Compute the offline work. It gets from the local database the favorite image, the description and the authors name.
     */
    private void computeOfflineWork() {
        recyclerView.setVisibility(View.GONE);
        similarImg.setVisibility(View.GONE);

        ImageBean bean = getBeanFromIntent();
        image_id = bean.getId();
        checkExistingImageInDB(image_id);

        imageView.setImageBitmap(Utilities.convertToBitmap(bean.getImage()));
        description.setText(bean.getDescription());
        author.setText(bean.getAuthor());
    }

    /**
     * Compute the online work. It gets from the server the main image, the authors name, the description and the similar images.
     */
    private void computeOnlineWork() {

        // layout for the similar images
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(llm);
        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
        params.height = (width / 3) + 2;
        recyclerView.setLayoutParams(params);
        adapter = new ImageAdapter(this, 2);
        recyclerView.setAdapter(adapter);
        adapter.setOnImageClickListener(new ImageAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(View view, int position) {

                updateUI(adapter.getBeanAt(position));
            }
        });

        // handler to handle the UI updates
        handler = new MyHandler(this);

        // get main image
        getMainImage(getBeanFromIntent());

        // get the authors name
        AsyncWork thread1 = new AsyncWork(1);
        thread1.setAuthorID(getBeanFromIntent().getIdContributor());
        new Thread(thread1).start();

        // get similar images
        AsyncWork thread2 = new AsyncWork(2);
        thread2.setImageID(getBeanFromIntent().getId());
        new Thread(thread2).start();
    }


    /**
     * Favorites action button
     *
     * @param v the button
     */
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.fab_favorites) {

            // if the image is already in the database
            if (imageDB) {
                imageDB = false;
                fab_favorites.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#838383")));

                // delete img from db
                db.deleteImage(image_id);
                Toast.makeText(getApplicationContext(), "Image removed from favorites", Toast.LENGTH_SHORT).show();

                // if not
            } else {
                imageDB = true;
                fab_favorites.setBackgroundTintList(ColorStateList.valueOf(Color.RED));

                // thread used to get the favorite image and then save it to database
                AsyncWork thread = new AsyncWork(3);
                thread.setUrl((String) imageView.getTag());
                AsyncWork.setContext(this);
                new Thread(thread).start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imageView.setImageDrawable(null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);
    }

    private int getIntentType() {
        return getIntent().getBundleExtra("bean").getInt("type");
    }

    private ImageBean getBeanFromIntent() {
        return getIntent().getBundleExtra("bean").getParcelable("bean");
    }

    /**
     * Method to check in the local database to an existing image.
     *
     * @param id the id of the image
     */
    private void checkExistingImageInDB(int id) {
        if (db.checkExitingImage(id)) {
            imageDB = true;
            fab_favorites.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        }
    }

    /**
     * It downloads the main image from the server
     *
     * @param bean the image bean
     */
    private void getMainImage(ImageBean bean) {
        image_id = bean.getId();
        sw.fullScroll(View.FOCUS_UP);

        // if the image is already in the database we change the color of the fab favorites
        checkExistingImageInDB(image_id);

        if (bean.getUrl() != null) {

            // get main image
            Picasso.with(getApplicationContext()).load(bean.getUrl()).resize(width, width).placeholder(R.drawable.border).centerCrop().into(imageView);
            imageView.setTag(bean.getUrl());
        }

        // set description of the image
        description.setText(bean.getDescription());
    }

    /**
     * Method which updates the UI with the new image, authors name and the similar images.
     *
     * @param bean the image bean
     */
    private void updateUI(ImageBean bean) {
        checkExistingImageInDB(bean.getId());

        // get image
        getMainImage(bean);

        // get the authors name
        AsyncWork thread1 = new AsyncWork(1);
        thread1.setAuthorID(bean.getIdContributor());
        new Thread(thread1).start();

        // get the similar images to the current image
        AsyncWork thread2 = new AsyncWork(2);
        thread2.setImageID(bean.getId());
        new Thread(thread2).start();
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
                    context.author.setText(msg.getData().getString("name"));
                    break;

                // update the UI with the similar images
                case 2:
                    context.adapter.addItem((ImageBean) msg.getData().getParcelable("bean"));
                    break;

                // remove the old similar images
                case 3:
                    context.adapter.deleteItems();
                    break;

                // save favorite image to database
                case 4:
                    context.db.insertImage((Bitmap) msg.getData().getParcelable("img"), context.image_id, context.description.getText().toString(), context.author.getText().toString());
                    Toast.makeText(context, "Image added to favorites", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * Static inner class to download the authors name, the favorite image and the similar images.
     *
     * @author Dinu
     */
    private static class AsyncWork implements Runnable {
        private static WeakReference<DisplayImageActivity> activity;
        private final int type;
        private int authorID;
        private int imageID;
        private String url;

        /**
         * The constructor.
         *
         * @param type 1-get authors name, 2-get similar images
         */
        public AsyncWork(int type) {
            this.type = type;
        }

        public static void setContext(DisplayImageActivity context) {
            activity = new WeakReference<>(context);
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

                case 3:
                    getFavoriteImage(url);

                default:
                    break;
            }

        }

        private void getFavoriteImage(String url) {
            final Bundle bundle = new Bundle();
            final Message msg = new Message();

            try {

                bundle.putParcelable("img", Picasso.with(activity.get()).load(url).resize(activity.get().width, activity.get().width).get());

            } catch (IOException e) {
                e.printStackTrace();
            }

            msg.setData(bundle);
            msg.what = 4;

            // alert the handler to save the imgage to database
            handler.post(new Runnable() {

                @Override
                public void run() {

                    handler.dispatchMessage(msg);
                }
            });
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
                int i = 0;
                while (iterator.hasNext()) {
                    JsonObject jsonObj = iterator.next().getAsJsonObject();
                    ImageBean ib = new ImageBean();
                    assets = jsonObj.get("assets") == null ? null : jsonObj.get("assets").getAsJsonObject();

                    if (assets != null) {

                        ib.setId(jsonObj.get("id") == null ? null : jsonObj.get("id").getAsInt());
                        ib.setDescription(jsonObj.get("description") == null ? null : jsonObj.get("description").getAsString());
                        ib.setIdContributor(jsonObj.get("contributor") == null ? null : jsonObj.get("contributor").getAsJsonObject().get("id").getAsInt());
                        ib.setUrl(assets.get("preview") == null ? null : assets.get("preview").getAsJsonObject().get("url").getAsString());
                    }

                    ib.setPos(i);
                    i++;

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

        public void setUrl(String url) {
            this.url = url;
        }
    }
}

