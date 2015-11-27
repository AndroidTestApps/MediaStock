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
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    private final ArrayList<Integer> filteredPositions = new ArrayList<>();
    private CustomSpinnerRowAdapter spinnerRowAdapter;
    private FloatingActionButton fabFilter;
    private FavoriteImageAdapter adapter;
    private DBController db;
    private Cursor cursor;
    private int width;
    private boolean filteredImages = false;
    private int imageID_temp = 0;
    private int cursorTempCount = 0;
    private int colorSelectedPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_images);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Favorite images");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

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
        RecyclerView recyclerView = (RecyclerView) this.findViewById(R.id.gridView_fav_images);
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
            new AsyncDBWork(this, 1, 0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); // get the favorite images
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.favorites_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        // refresh the image list, get all images from the storage
        if (item.getItemId() == R.id.item_refresh) {
            filteredImages = false;

            // first remove the existing ones
            adapter.deleteBitmaps();

            // get the favorite images
            new AsyncDBWork(this, 1, 0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        return true;
    }


    /**
     * Method to start the DisplayImageActivity to see the details of the image selected.
     *
     * @param position the position of the selected image
     */
    private void goToDisplayImageActivity(int position) {
        final Bundle bundle = new Bundle();
        final ImageBean bean = new ImageBean();

        // move to cursor to the right position
        if (filteredImages)
            cursor.moveToPosition(filteredPositions.get(position));
        else
            cursor.moveToPosition(position);

        bean.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.IMG_ID)));
        bean.setDescription(cursor.getString(cursor.getColumnIndex(DBHelper.DESCRIPTION_IMG)));
        bean.setAuthor(cursor.getString(cursor.getColumnIndex(DBHelper.AUTHOR_IMG)));
        bean.setPath(cursor.getString(cursor.getColumnIndex(DBHelper.IMG_PATH)));

        Intent intent = new Intent(getApplicationContext(), DisplayImageActivity.class);
        bundle.putInt("type", 2);
        bundle.putParcelable("bean", bean);
        intent.putExtra("bean", bundle);

        // save the position that this image has in the adapter
        imageID_temp = position;

        startActivity(intent);
        overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // save cursor count and then close it
        cursorTempCount = cursor.getCount();
        cursor.close();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        // get the cursor
        cursor = db.getImagesInfo();

        // an image has been removed from favorites
        if (cursorTempCount > cursor.getCount())
            adapter.deleteBitmapAt(imageID_temp);

        // a new image has been added to favorites
        if (cursorTempCount < cursor.getCount()) {
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
                filteredImages = true;
                filteredPositions.clear(); // remove the old filtered positions

                // first remove the images from the adapter
                adapter.deleteBitmaps();

                // filter the images by color
                new AsyncDBWork(FavoriteImagesActivity.this, 2, colorSelectedPosition).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
     * Class to get in background the images from the storage.
     */
    private static class AsyncDBWork extends AsyncTask<Void, Bitmap, Void> {
        private static WeakReference<FavoriteImagesActivity> activity;
        private final int type;
        private final int color;
        private int position = 0;

        public AsyncDBWork(FavoriteImagesActivity context, int type, int color) {
            activity = new WeakReference<>(context);
            this.type = type;
            this.color = color;
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (type == 1)
                getFavoriteImages();
            else
                filterImages();

            return null;
        }

        private void getFavoriteImages() {
            FavoriteImagesActivity context = activity.get();

            context.cursor.moveToFirst();
            do {

                // load image from storage and send it to the adapter
                publishProgress(Utilities.loadImageFromInternalStorage(context,
                        context.cursor.getString(context.cursor.getColumnIndex(DBHelper.IMG_PATH)), context.width / 2));

            } while (context.cursor.moveToNext());
        }

        private void filterImages() {
            FavoriteImagesActivity context = activity.get();

            // get from the db the color palettes of the images
            Cursor cursorColor = context.db.getColorPalettes();

            do {

                ColorHelper colorHelper = new ColorHelper(color, cursorColor.getInt(cursorColor.getColumnIndex(DBHelper.VIBRANT)),
                        cursorColor.getInt(cursorColor.getColumnIndex(DBHelper.LIGHT_VIBRANT)), cursorColor.getInt(cursorColor.getColumnIndex(DBHelper.DARK_VIBRANT)),
                        cursorColor.getInt(cursorColor.getColumnIndex(DBHelper.MUTED)), cursorColor.getInt(cursorColor.getColumnIndex(DBHelper.LIGHT_MUTED)), cursorColor.getInt(cursorColor.getColumnIndex(DBHelper.DARK_MUTED)));

                // if the color palette of the current image is similar to the color chosen by the user
                // we add the image to the list adapter
                if (colorHelper.getColorSimilarity()) {
                    context.cursor.moveToPosition(cursorColor.getPosition());

                    // save the position of this image
                    context.filteredPositions.add(cursorColor.getPosition());

                    publishProgress(Utilities.loadImageFromInternalStorage(context,
                            context.cursor.getString(context.cursor.getColumnIndex(DBHelper.IMG_PATH)), context.width / 2));
                }

            } while (cursorColor.moveToNext());


            cursorColor.close();
        }

        @Override
        protected void onProgressUpdate(Bitmap... values) {
            super.onProgressUpdate(values);

            activity.get().adapter.addBitmap(values[0], position);
            position++;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            activity = null;
        }
    }
}
