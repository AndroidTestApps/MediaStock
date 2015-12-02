package com.example.mediastock.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.mediastock.R;
import com.example.mediastock.data.DBController;
import com.example.mediastock.data.ImageBean;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * Activity to display the image that the user clicked. It displays the description of the image and the similar images.
 * The image can be added to favorites or removed from favorites.
 *
 * @author Dinu
 */
public class DisplayImageActivity extends AppCompatActivity implements View.OnClickListener {
    private static Handler handler;
    private ProgressDialog progressDialog;
    private int imageId;
    private int width;
    private boolean offlineWork = false;
    private boolean imageToDB = false;
    private ImageView imageView;
    private ImageAdapter adapter;
    private RecyclerView recyclerView;
    private TextView description, author, similarImg;
    private FloatingActionButton fabFavorites;
    private DBController db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_image_activity);
        width = getResources().getDisplayMetrics().widthPixels;

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        constructProgressDialog();

        // the database
        db = new DBController(this);

        // handle the threads
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // layouts init
        imageView = (ImageView) findViewById(R.id.imageView_displayImage);
        ViewGroup.LayoutParams param = imageView.getLayoutParams();
        param.height = width;
        param.width = width;
        imageView.setLayoutParams(param);
        TextView labelDescription = (TextView) this.findViewById(R.id.textView_description_label);
        labelDescription.setTypeface(null, Typeface.BOLD);
        TextView labelAuthor = (TextView) this.findViewById(R.id.TextView_contributor_label);
        labelAuthor.setTypeface(null, Typeface.BOLD);
        TextView labelSimilarImg = (TextView) this.findViewById(R.id.TextView_similar_displayImage);
        labelSimilarImg.setTypeface(null, Typeface.BOLD);
        fabFavorites = (FloatingActionButton) this.findViewById(R.id.fab_favorites);
        fabFavorites.setOnClickListener(this);
        description = (TextView) this.findViewById(R.id.textView_description_displayImage);
        similarImg = (TextView) this.findViewById(R.id.TextView_similar_displayImage);
        author = (TextView) this.findViewById(R.id.TextView_contributor_displayImage);
        recyclerView = (RecyclerView) this.findViewById(R.id.image_home_ScrollView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DisplayImageActivity.this, FullViewImageActivity.class);
                final Bundle bundle = new Bundle();

                // offline work
                if (getIntentType() == 2) {
                    bundle.putInt("type", 2);
                    bundle.putParcelable("bean", getBeanFromIntent());

                    // online work
                } else {
                    bundle.putInt("type", 1);
                    intent.putExtra("image", (String) description.getTag());
                }

                intent.putExtra("bean", bundle);
                startActivity(intent);
            }
        });

        // check if we are offline
        if (getIntentType() == 2)
            computeOfflineWork();
        else
            computeOnlineWork();
    }

    /**
     * Compute the offline work. We display the image from the internal storage and the image infos
     */
    private void computeOfflineWork() {
        offlineWork = true;
        recyclerView.setVisibility(View.GONE);
        similarImg.setVisibility(View.GONE);

        ImageBean bean = getBeanFromIntent();
        imageId = bean.getId();

        imageToDB = true;
        fabFavorites.setBackgroundTintList(ColorStateList.valueOf(Color.RED));

        Glide.with(this).load(Utilities.loadImageFromInternalStorage(this, bean.getPath())).diskCacheStrategy(DiskCacheStrategy.RESULT).fitCenter().centerCrop().crossFade()
                .placeholder(R.drawable.border).error(R.drawable.border).into(imageView);

        description.setText(bean.getDescription());
        author.setText(bean.getAuthor());
    }


    /**
     * Compute the online work. It gets from the server the main image, the authors name, the description and the similar images.
     */
    private void computeOnlineWork() {

        // layout for the similar images
        GridLayoutManager grid = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(grid);
        recyclerView.setHasFixedSize(true);
        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
        params.height = ((width / 3) * 2) + 2;
        recyclerView.setLayoutParams(params);
        adapter = new ImageAdapter(this, 2);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {

                updateUI((ImageBean) adapter.getBeanAt(position));
            }
        });

        // handler to handle the UI updates
        handler = new MyHandler(this);

        // get images
        updateUI(getBeanFromIntent());
    }


    /**
     * Method to update the UI with the main image, authors name and the similar images.
     *
     * @param bean the image bean
     */
    private void updateUI(ImageBean bean) {

        // get main image
        getMainImage(bean);

        // get the authors name
        AsyncWork thread_author = new AsyncWork(1, this);
        thread_author.setAuthorID(bean.getIdContributor());
        thread_author.start();

        // get the similar images to the current image
        AsyncWork thread_similarImages = new AsyncWork(2, this);
        thread_similarImages.setImageID(bean.getId());
        thread_similarImages.start();
    }

    /**
     * Handle the favorites action button
     *
     * @param v the button
     */
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.fab_favorites) {

            // if the image is already saved in the database, it means we want to remove the image
            if (imageToDB) {
                imageToDB = false;
                fabFavorites.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#838383")));

                // remove image from favorites
                new AsyncDbWork(this, 1, imageId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                // we have to add the image to favorites
            } else {
                imageToDB = true;
                fabFavorites.setBackgroundTintList(ColorStateList.valueOf(Color.RED));

                showProgressDialog("Adding image to favorites...");

                // thread used to get the favorite image and then save it
                Glide.with(this).load((String) description.getTag()).asBitmap().into(new SimpleTarget<Bitmap>() {

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                        addImageToFavorites(resource);
                    }
                });
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imageView = null;
    }

    @Override
    public void onBackPressed() {

        if (offlineWork) {
            Intent i = new Intent(getApplicationContext(), FavoriteImagesActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
            startActivity(i);
        }

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
     * We check in the database if the image with id id exists.
     * If exists we change the fab color.
     *
     * @param id the id of the image
     */
    private void checkExistingImageInDB(int id) {
        if (db.checkExistingImage(id)) {
            imageToDB = true;
            fabFavorites.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        }
    }

    /**
     * It downloads the main image from the server and sets the description of the image
     *
     * @param bean the image bean
     */
    private void getMainImage(ImageBean bean) {
        imageId = bean.getId();

        // if the image is already in the database we change the color of the fab
        checkExistingImageInDB(imageId);

        // get main image
        if (bean.getUrl() != null) {
            Glide.with(this).load(bean.getUrl()).diskCacheStrategy(DiskCacheStrategy.RESULT).placeholder(R.drawable.border).crossFade().fitCenter().centerCrop().error(R.drawable.border).into(imageView);
            description.setTag(bean.getUrl());
        }

        // set description of the image
        description.setText(bean.getDescription());
    }

    /**
     * Method that start a thread to add the image to favorites
     *
     * @param resource the image
     */
    private void addImageToFavorites(Bitmap resource) {
        new AsyncDbWork(this, 2, resource, imageId, description.getText().toString(), author.getText().toString()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void constructProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    private void showProgressDialog(String message) {
        progressDialog.setMessage(message);
        progressDialog.show();
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
        public void handleMessage(final Message msg) {
            final DisplayImageActivity context = activity.get();

            switch (msg.what) {

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
     * Static inner class to download the authors name and the similar images in the background.
     *
     * @author Dinu
     */
    private static class AsyncWork extends Thread {
        private static WeakReference<DisplayImageActivity> activity;
        private final int type;
        private int authorID;
        private int imageID;

        /**
         * The constructor.
         *
         * @param type 1-get authors name; 2-get similar images;
         */
        public AsyncWork(int type, DisplayImageActivity context) {
            activity = new WeakReference<>(context);
            this.type = type;
        }


        @Override
        public void run() {
            super.run();

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
            final DisplayImageActivity context = activity.get();
            String urlStr = "https://@api.shutterstock.com/v2/contributors/";
            urlStr += id;

            InputStream is = null;
            String author = "";
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
                    author = json.getAsJsonObject().get("display_name") == null ? " - " : json.getAsJsonObject().get("display_name").getAsString();
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

            final String result = author;

            // display authors name
            context.author.post(new Runnable() {

                @Override
                public void run() {
                    context.author.setText(result);
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

                    JsonObject assets;
                    Iterator<JsonElement> iterator = array.iterator();
                    int i = 0;
                    while (iterator.hasNext()) {
                        JsonObject jsonObj = iterator.next().getAsJsonObject();
                        final ImageBean ib = new ImageBean();
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

        private void setImageID(int imageID) {
            this.imageID = imageID;
        }

        private void setAuthorID(int authorID) {
            this.authorID = authorID;
        }
    }


    /**
     * Class to handle the background work with the database
     */
    private static class AsyncDbWork extends AsyncTask<Void, Void, Void> {
        private static WeakReference<DisplayImageActivity> activity;
        private final int type;
        private final int imageID;
        private final String description;
        private final String author;
        private final Bitmap bitmap;

        public AsyncDbWork(DisplayImageActivity context, int type, Bitmap bitmap, int imageID, String description, String author) {
            activity = new WeakReference<>(context);
            this.type = type;
            this.imageID = imageID;
            this.bitmap = bitmap;
            this.description = description;
            this.author = author;

        }

        public AsyncDbWork(DisplayImageActivity context, int type, int imageID) {
            this(context, type, null, imageID, null, null);
        }


        @Override
        protected Void doInBackground(Void... params) {

            if (type == 1)
                removeImageFromFavorites();
            else
                addImageToFavorites();

            return null;
        }


        /**
         * Method to save an image to the storage and to save the color palette and the images info to database
         */
        private void addImageToFavorites() {
            final DisplayImageActivity context = activity.get();

            // get the color palette of the image
            Palette palette = Palette.from(bitmap).generate();

            // color palette
            int vibrant = 0;
            int lightVibrant = 0;
            int darkVibrant = 0;
            int muted = 0;
            int lightMuted = 0;
            int darkMuted = 0;
            int dominantColor = 0;

            // get the dominant color of the image
            Palette.Swatch dominantSwatch = Utilities.getDominantSwatch(palette);
            dominantColor = dominantSwatch.getRgb();

            Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
            Palette.Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();
            Palette.Swatch lightVibrantSwatch = palette.getLightVibrantSwatch();

            Palette.Swatch mutedSwatch = palette.getMutedSwatch();
            Palette.Swatch lightMutedSwatch = palette.getLightMutedSwatch();
            Palette.Swatch darkMutedSwatch = palette.getDarkMutedSwatch();

            if (vibrantSwatch != null)
                vibrant = vibrantSwatch.getRgb();

            if (lightVibrantSwatch != null)
                lightVibrant = lightVibrantSwatch.getRgb();

            if (darkVibrantSwatch != null)
                darkVibrant = darkVibrantSwatch.getRgb();

            if (mutedSwatch != null)
                muted = mutedSwatch.getRgb();

            if (darkMutedSwatch != null)
                darkMuted = darkMutedSwatch.getRgb();

            if (lightMutedSwatch != null)
                lightMuted = lightMutedSwatch.getRgb();

            // save the image into the internal storage
            String path = Utilities.saveImageToInternalStorage(context, bitmap, imageID);

            // add to db the info about the image
            context.db.insertImageInfo(path, imageID, description, author);

            // add to db the color palette of the image
            context.db.insertColorPalette(imageID, vibrant, lightVibrant, darkVibrant,
                    muted, lightMuted, darkMuted, dominantColor);
        }


        /**
         * Method to remove an image from favorites
         */
        private void removeImageFromFavorites() {
            DisplayImageActivity context = activity.get();

            // path of the image
            String path = context.db.getImagePath(imageID);

            // delete images info
            context.db.deleteImageInfo(imageID);

            // delete the images color palette
            context.db.deleteColorPalette(imageID);

            // delete image from storage
            Utilities.deleteSpecificMediaFromInternalStorage(Utilities.IMG_DIR, context, path);
        }


        @Override
        protected void onPostExecute(Void value) {
            super.onPostExecute(value);

            activity.get().progressDialog.dismiss();

            if (type == 2)
                Toast.makeText(activity.get().getApplicationContext(), "Image added to favorites", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(activity.get().getApplicationContext(), "Image removed from favorites", Toast.LENGTH_SHORT).show();

            activity = null;
        }
    }
}

