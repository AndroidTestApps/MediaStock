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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.mediastock.R;
import com.example.mediastock.data.DBController;
import com.example.mediastock.data.DBHelper;
import com.example.mediastock.data.ImageBean;
import com.example.mediastock.util.ColorHelper;
import com.example.mediastock.util.CustomSpinnerRowAdapter;
import com.example.mediastock.util.FavoriteImageAdapter;
import com.example.mediastock.util.Utilities;
import com.squareup.leakcanary.LeakCanary;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class FavoriteImagesActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String[] colors = {"Black", "White", "Red", "Blue", "Green", "Yellow", "Orange", "Magenta", "Grey", "Cyan"};
    private final ArrayList<String> rows = new ArrayList<>();
    private CustomSpinnerRowAdapter spinnerRowAdapter;
    private RecyclerView recyclerView;
    private FloatingActionButton fabFilter;
    private FavoriteImageAdapter adapter;
    private DBController db;
    private Cursor cursor;
    private int width;
    private int imageID_temp = 0;
    private int colorSelectedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_images);

        LeakCanary.install(getApplication());

        // get images from db
        db = new DBController(this);
        cursor = db.getImagesInfo();

        // create the spinner model rows and the adapter
        createSpinnerModelRows();

        // layout init
        fabFilter = (FloatingActionButton) this.findViewById(R.id.fab_fav_img_search);
        fabFilter.setOnClickListener(this);
        width = getResources().getDisplayMetrics().widthPixels;
        recyclerView = (RecyclerView) this.findViewById(R.id.gridView_fav_images);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager grid = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(grid);
        adapter = new FavoriteImageAdapter(getApplicationContext());
        recyclerView.setAdapter(adapter);

        // on item click
        adapter.setOnItemClickListener(new FavoriteImageAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {

                goToDisplayImageActivity(position);
            }
        });

        if (cursor.getCount() == 0)
            Toast.makeText(getApplicationContext(), "There are no images saved", Toast.LENGTH_SHORT).show();
        else
            new AsyncDBWork(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); // get the favorite images
    }

    /**
     * Method to start the DisplayImageActivity to see the details of the image selected.
     *
     * @param position the position of the selected image
     */
    private void goToDisplayImageActivity(int position) {
        final Bundle bundle = new Bundle();
        final ImageBean bean = new ImageBean();

        // move to position position
        cursor.moveToPosition(position);

        bean.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.IMG_ID)));
        bean.setDescription(cursor.getString(cursor.getColumnIndex(DBHelper.DESCRIPTION_IMG)));
        bean.setAuthor(cursor.getString(cursor.getColumnIndex(DBHelper.AUTHOR_IMG)));
        bean.setName(cursor.getString(cursor.getColumnIndex(DBHelper.IMG_PATH)));

        Intent intent = new Intent(getApplicationContext(), DisplayImageActivity.class);
        bundle.putInt("type", 2);
        bundle.putParcelable("bean", bean);
        intent.putExtra("bean", bundle);

        // save the images position (in the adapter) in case the image will be removed from favorites
        imageID_temp = position;

        startActivity(intent);
        overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        int cursorCountBefore = cursor.getCount();

        cursor = db.getImagesInfo();

        // an image has been removed from favorites
        if (cursorCountBefore > cursor.getCount())
            adapter.deleteBitmapAt(imageID_temp);

        // a new image has been added to favorites
        if (cursorCountBefore < cursor.getCount()) {
            cursor.moveToLast();

            adapter.addBitmap(Utilities.loadImageFromInternalStorage(
                    this, cursor.getString(cursor.getColumnIndex(DBHelper.IMG_PATH)), width / 2), adapter.getItemCount());
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.fab_fav_img_search) {
            fabFilter.setClickable(false);

            showPopupMenu();
        }
    }

    /**
     * Method to show a popup menu to filter the images
     */
    private void showPopupMenu() {

        int layoutWidth = (width / 2) + (width / 3);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_window, null);

        Spinner rows = (Spinner) popupView.findViewById(R.id.spinner_rows);
        rows.setAdapter(spinnerRowAdapter);
        rows.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // the selected color position
                colorSelectedPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final PopupWindow popupWindow = new PopupWindow(popupView, layoutWidth, RelativeLayout.LayoutParams.WRAP_CONTENT);

        Button buttonDismiss = (Button) popupView.findViewById(R.id.dismiss);
        Button buttonOK = (Button) popupView.findViewById(R.id.accept);

        buttonDismiss.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {

                popupWindow.dismiss();
                fabFilter.setClickable(true);
            }
        });

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // filter the images by color
                // new AsyncImageFilter(FavoriteImagesActivity.this, colorSelectedPosition).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                popupWindow.dismiss();
                fabFilter.setClickable(true);
            }
        });

        popupWindow.showAtLocation(fabFilter, Gravity.CENTER, 0, 0);
    }


    private void createSpinnerModelRows() {
        for (int i = 0; i < 10; i++)
            rows.add(colors[i]);

        // adapter for the spinner
        spinnerRowAdapter = new CustomSpinnerRowAdapter(FavoriteImagesActivity.this, R.layout.spinner_rows, rows);
    }


    /**
     * Class to get in background the images from the database.
     */
    private static class AsyncDBWork extends AsyncTask<Void, Bitmap, Void> {
        private static WeakReference<FavoriteImagesActivity> activity;
        private int pos = 0;

        public AsyncDBWork(FavoriteImagesActivity context) {
            activity = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... params) {
            FavoriteImagesActivity context = activity.get();

            do {

                // load image from storage and send it to the adapter
                publishProgress(Utilities.loadImageFromInternalStorage(context,
                        context.cursor.getString(context.cursor.getColumnIndex(DBHelper.IMG_PATH)),
                        context.width / 2));

            } while (context.cursor.moveToNext());

            return null;
        }


        @Override
        protected void onProgressUpdate(Bitmap... values) {
            super.onProgressUpdate(values);

            activity.get().adapter.addBitmap(values[0], pos);
            pos++;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            activity = null;
        }
    }

    /**
     * Async class to filter the images.
     */
    private static class AsyncImageFilter extends AsyncTask<Void, Void, Void> {
        private static WeakReference<FavoriteImagesActivity> activity;
        private final int color;
        private final Cursor cursor;

        public AsyncImageFilter(FavoriteImagesActivity context, int colorPosition) {
            activity = new WeakReference<>(context);
            color = colorPosition;
            cursor = activity.get().db.getColorPalettes();
        }

        @Override
        protected Void doInBackground(Void... params) {


            do {
                ColorHelper colorHelper = new ColorHelper(color, cursor.getInt(cursor.getColumnIndex(DBHelper.VIBRANT)),
                        0, 0, 0, 0, 0);

            } while (cursor.moveToNext());

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            // activity.get().adapter.deleteBitmapAt(pos);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            activity = null;
        }
    }


}
