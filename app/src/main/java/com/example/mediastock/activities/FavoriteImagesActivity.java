package com.example.mediastock.activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.mediastock.R;
import com.example.mediastock.data.DBController;
import com.example.mediastock.data.DBHelper;
import com.example.mediastock.data.ImageBean;
import com.example.mediastock.util.FavoriteImageAdapter;
import com.example.mediastock.util.Utilities;
import com.squareup.leakcanary.LeakCanary;

import java.lang.ref.WeakReference;

public class FavoriteImagesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FloatingActionButton fabFilter;
    private FavoriteImageAdapter adapter;
    private ProgressBar progressBar;
    private DBController db;
    private Cursor cursor;
    private int width;
    private int imageID_temp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_images);

        LeakCanary.install(getApplication());

        // get images from db
        db = new DBController(this);
        cursor = db.getImages();

        // layout init
        width = getResources().getDisplayMetrics().widthPixels;
        recyclerView = (RecyclerView) this.findViewById(R.id.gridView_fav_images);
        recyclerView.setHasFixedSize(true);
        progressBar = (ProgressBar) this.findViewById(R.id.p_img_bar);
        fabFilter = (FloatingActionButton) this.findViewById(R.id.fab_fav_img_search);
        fabFilter = (FloatingActionButton) this.findViewById(R.id.fab_fav_img_search);
        GridLayoutManager grid = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(grid);
        adapter = new FavoriteImageAdapter(getApplicationContext());
        recyclerView.setAdapter(adapter);

        // on item click
        adapter.setOnImageClickListener(new FavoriteImageAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(View view, int position) {

                goToDisplayImageActivity(position);
            }
        });

        if (cursor.getCount() == 0)
            Toast.makeText(getApplicationContext(), "There are no images saved", Toast.LENGTH_SHORT).show();
        else
            new AsyncWork(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Method to start the DisplayImageActivity to see the details of the image selected.
     *
     * @param position the position of the selected image
     */
    private void goToDisplayImageActivity(int position) {
        final Bundle bundle = new Bundle();
        final ImageBean bean = new ImageBean();
        cursor.moveToPosition(position);

        bean.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.IMG_ID)));
        bean.setDescription(cursor.getString(cursor.getColumnIndex(DBHelper.DESCRIPTION_IMG)));
        bean.setAuthor(cursor.getString(cursor.getColumnIndex(DBHelper.AUTHOR_IMG)));
        bean.setByteArrayLength(cursor.getBlob(cursor.getColumnIndex(DBHelper.IMAGE)).length);
        bean.setImage(cursor.getBlob(cursor.getColumnIndex(DBHelper.IMAGE)));

        Intent intent = new Intent(getApplicationContext(), DisplayImageActivity.class);

        bundle.putInt("type", 2);
        bundle.putParcelable("bean", bean);
        intent.putExtra("bean", bundle);

        // save the images position in the adapter in case the image will be removed from favorites
        imageID_temp = position;

        startActivity(intent);
        overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(false);
        overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        int cursor_count = cursor.getCount();
        cursor = db.getImages();

        // some image has been removed from favorites
        if (cursor_count != cursor.getCount())
            adapter.deleteItemAt(imageID_temp);

    }

    private static class AsyncWork extends AsyncTask<Void, Bitmap, Void> {
        private static WeakReference<FavoriteImagesActivity> activity;
        private int pos = 0;

        public AsyncWork(FavoriteImagesActivity context) {
            activity = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... params) {
            FavoriteImagesActivity context = activity.get();

            do {

                // scale image and send it to the adapter
                publishProgress(Bitmap.createScaledBitmap(
                        Utilities.convertToBitmap(context.cursor.getBlob(context.cursor.getColumnIndex(DBHelper.IMAGE)))
                        , context.width / 2, context.width / 2, false));

            } while (context.cursor.moveToNext());



            return null;
        }


        @Override
        protected void onProgressUpdate(Bitmap... values) {
            super.onProgressUpdate(values);

            activity.get().adapter.addItem(values[0], pos);
            pos++;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            activity = null;
        }
    }
}
