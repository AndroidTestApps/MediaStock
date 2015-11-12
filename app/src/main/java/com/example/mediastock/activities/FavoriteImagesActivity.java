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
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.mediastock.R;
import com.example.mediastock.data.Database;
import com.example.mediastock.data.ImageBean;
import com.example.mediastock.util.FavoriteImageAdapter;
import com.example.mediastock.util.Utilities;

import java.lang.ref.WeakReference;

public class FavoriteImagesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FloatingActionButton fab_filter;
    private FavoriteImageAdapter adapter;
    private ProgressBar progressBar;
    private Database db;
    private Cursor cursor;
    private int width;
    private int imageID_temp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_images);

        db = new Database(this);
        cursor = db.getImages();

        // layout init
        width = getResources().getDisplayMetrics().widthPixels;
        recyclerView = (RecyclerView) this.findViewById(R.id.gridView_fav_images);
        recyclerView.setHasFixedSize(true);
        progressBar = (ProgressBar) this.findViewById(R.id.p_img_bar);
        fab_filter = (FloatingActionButton) this.findViewById(R.id.fab_fav_img_search);
        fab_filter = (FloatingActionButton) this.findViewById(R.id.fab_fav_img_search);
        GridLayoutManager grid = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(grid);
        adapter = new FavoriteImageAdapter(getApplicationContext());
        recyclerView.setAdapter(adapter);
        adapter.setOnImageClickListener(new FavoriteImageAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(View view, int position) {

                goToDisplayImageActivity(position + 1);
            }
        });

        new AsyncWork(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        Log.i("favorites", " invocato onCreate");
    }

    /**
     * Method to start the DisplayImageActivity to see the details of the image selected.
     *
     * @param position the position of the selected image
     */
    private void goToDisplayImageActivity(int position) {
        final ImageBean bean = new ImageBean();
        cursor.moveToPosition(position);

        bean.setId(cursor.getInt(cursor.getColumnIndex(Database.IMG_ID)));
        bean.setDescription(cursor.getString(cursor.getColumnIndex(Database.DESCRIPTION)));
        bean.setAuthor(cursor.getString(cursor.getColumnIndex(Database.AUTHOR)));
        bean.setByteArrayLength(cursor.getBlob(cursor.getColumnIndex(Database.IMAGE)).length);
        bean.setImage(cursor.getBlob(cursor.getColumnIndex(Database.IMAGE)));

        Intent intent = new Intent(getApplicationContext(), DisplayImageActivity.class);
        final Bundle bundle = new Bundle();
        bundle.putInt("type", 2);
        bundle.putParcelable("bean", bean);
        intent.putExtra("bean", bundle);

        // save the images position in the adapter in case the image will be removed from favorites
        imageID_temp = position - 1;

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

        // close the cursor
        cursor.close();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (cursor != null && cursor.isClosed()) {
            int numElements = cursor.getCount();

            cursor = db.getImages();

            // item has been removed
            if (numElements != cursor.getCount())
                adapter.deleteItemAt(imageID_temp);
        }
    }

    private static class AsyncWork extends AsyncTask<Void, Bitmap, Void> {
        private static WeakReference<FavoriteImagesActivity> activity;
        private int pos = 0;

        public AsyncWork(FavoriteImagesActivity context) {
            activity = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... params) {

            while (activity.get().cursor.moveToNext()) {

                // scale image and send it to the adapter
                publishProgress(Bitmap.createScaledBitmap(
                        Utilities.convertToBitmap(activity.get().cursor.getBlob(activity.get().cursor.getColumnIndex(Database.IMAGE)))
                        , activity.get().width / 2, activity.get().width / 2, false));
            }

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
