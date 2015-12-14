package com.example.mediastock.activities;


import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.mediastock.R;
import com.example.mediastock.data.Bean;
import com.example.mediastock.data.DBController;
import com.example.mediastock.data.DBHelper;
import com.example.mediastock.data.MusicBean;
import com.example.mediastock.model.MusicSpinnerRowAdapter;
import com.example.mediastock.model.MusicVideoAdapter;
import com.example.mediastock.util.ExecuteExecutor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Activity to display the favorite music. It also filters the music by genre.
 */
public class FavoriteMusicActivity extends AppCompatActivity implements View.OnClickListener {
    private final ArrayList<String> rowsModel = new ArrayList<>();
    private MusicSpinnerRowAdapter spinnerRowAdapter;
    private FloatingActionButton fabFilter;
    private MusicVideoAdapter adapter;
    private int width;
    private int genreSelectedPosition;
    private DBController db;
    private Cursor cursor;
    private int cursorTempCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_music_video);
        width = getResources().getDisplayMetrics().widthPixels;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Favorite music");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // get music info
        db = new DBController(this);
        cursor = db.getMusicInfo();

        // create the spinner model rows and the adapter
        createSpinnerModelRows();

        fabFilter = (FloatingActionButton) this.findViewById(R.id.fab_options);
        fabFilter.setOnClickListener(this);
        RecyclerView recyclerView = (RecyclerView) this.findViewById(R.id.list_music_video_fav);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        adapter = new MusicVideoAdapter(this, 1, true);
        adapter.setLoadingType(4);   // no loading when reaching bottom (no endless list)
        recyclerView.setAdapter(adapter);

        // on item click
        adapter.setOnItemClickListener(new MusicVideoAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {

                goToMusicPlayerActivity(position);
            }
        });

        if (cursor.getCount() == 0)
            Toast.makeText(getApplicationContext(), "There is no music saved", Toast.LENGTH_SHORT).show();
        else
            new AsyncDBWork(this, 1).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); // get music info from db
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

        // refresh the music list
        if (item.getItemId() == R.id.item_refresh) {

            refreshList();
        }

        return true;
    }


    /**
     * Open MusicPlayerActivity activity
     */
    private void goToMusicPlayerActivity(int position) {
        final MusicBean bean = (MusicBean) adapter.getBeanAt(position);
        final Bundle bundle = new Bundle();

        Intent intent = new Intent(getApplicationContext(), MusicPlayerActivity.class);
        bundle.putParcelable("bean", bean);
        intent.putExtra("bean", bundle);
        bundle.putInt("type", 2);

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

        // get the current cursor
        cursor = db.getMusicInfo();

        if (cursor.getCount() == 0) {
            adapter.deleteItems();

            Toast.makeText(getApplicationContext(), "There is no music saved", Toast.LENGTH_SHORT).show();
            return;
        }

        // a music has been removed from favorites
        if (cursorTempCount > cursor.getCount())
            refreshList();

        // a new music has been added to favorites
        if (cursorTempCount < cursor.getCount())
            refreshList();
    }


    /**
     * Method to refresh the view. It loads the music from the storage
     */
    private void refreshList() {
        // first remove current music
        adapter.deleteItems();

        // get music info from db
        new AsyncDBWork(this, 1).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.fab_options) {
            fabFilter.setClickable(false);
            showPopupMenu();
        }
    }


    /**
     * Method to show a popup menu to filter the music
     */
    private void showPopupMenu() {
        int layoutWidth = (width / 2) + (width / 3);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.popup_window_music, null);

        Spinner rows = (Spinner) popupView.findViewById(R.id.spinner_rows);
        rows.setAdapter(spinnerRowAdapter);
        rows.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // the selected genre position
                genreSelectedPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final PopupWindow popupWindow = new PopupWindow(popupView, layoutWidth, RelativeLayout.LayoutParams.WRAP_CONTENT);
        TextView filterMusic = (TextView) popupView.findViewById(R.id.textFilterOption);
        filterMusic.setText("Filter music");
        TextView labelGenre = (TextView) popupView.findViewById(R.id.labelFirstOption);
        labelGenre.setText("Genre");
        Button buttonDismiss = (Button) popupView.findViewById(R.id.dismiss);
        Button buttonOK = (Button) popupView.findViewById(R.id.accept);


        // dismiss
        buttonDismiss.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {

                popupWindow.dismiss();
                fabFilter.setClickable(true);
            }
        });

        // ok
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // first remove the music from the list
                adapter.deleteItems();

                // thread to filter the music by genre
                new AsyncDBWork(FavoriteMusicActivity.this, 2).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, rowsModel.get(genreSelectedPosition));

                popupWindow.dismiss();
                fabFilter.setClickable(true);
            }
        });

        popupWindow.showAtLocation(fabFilter, Gravity.CENTER, 0, 0);
    }

    /*
    * Thread to create the spinner model rows. It gets from database the genre of each music
    */
    private void createSpinnerModelRows() {

        new ExecuteExecutor(this, 2, new ExecuteExecutor.CallableAsyncTask(this) {

            @Override
            public String call() {
                FavoriteMusicActivity context = (FavoriteMusicActivity) this.getContextRef();
                Cursor cursor = context.db.getMusicGenre();
                final Set<String> set = new HashSet<>();

                do {

                    set.add(cursor.getString(cursor.getColumnIndex(DBHelper.MUSIC_GENRE)));

                } while (cursor.moveToNext());

                cursor.close();

                context.rowsModel.addAll(set);

                // adapter for the spinner
                context.spinnerRowAdapter = new MusicSpinnerRowAdapter(context, R.layout.media_spinner_rows, context.rowsModel);

                return null;
            }
        });
    }


    /**
     * Class to get in background the music info from the database.
     */
    private static class AsyncDBWork extends AsyncTask<String, Bean, Void> {
        private static WeakReference<FavoriteMusicActivity> activity;
        private final int type;
        private boolean success = false;

        public AsyncDBWork(FavoriteMusicActivity context, int type) {
            activity = new WeakReference<>(context);
            this.type = type;
        }


        @Override
        protected Void doInBackground(String... params) {

            switch (type) {

                case 1:
                    getFavoriteMusic();
                    break;

                case 2:
                    filterMusicByGenre(params[0]);
                    break;

                default:
                    break;
            }

            return null;
        }

        /**
         * It gets the music info from database. The result is wrapped in a bean and then sent to the adapter to update the view.
         */
        private void getFavoriteMusic() {
            final FavoriteMusicActivity context = activity.get();
            int pos = 0;

            context.cursor.moveToFirst();
            do {

                final MusicBean bean = new MusicBean();
                bean.setPos(pos);
                bean.setId(String.valueOf(context.cursor.getInt(context.cursor.getColumnIndex(DBHelper.MUSIC_ID))));
                bean.setTitle(context.cursor.getString(context.cursor.getColumnIndex(DBHelper.TITLE_MUSIC)));
                bean.setPath(context.cursor.getString(context.cursor.getColumnIndex(DBHelper.MUSIC_PATH)));

                publishProgress(bean);

                pos++;
            } while (context.cursor.moveToNext());
        }


        /**
         * Method to iterate over all musics and to filter them by the genre with the name genre.
         *
         * @param genre the genre of the music
         */
        private void filterMusicByGenre(String genre) {
            FavoriteMusicActivity context = activity.get();
            int pos = 0;

            // get the genre of the music
            Cursor cursor = context.db.getMusicGenre();
            do {

                if (cursor.getString(cursor.getColumnIndex(DBHelper.MUSIC_GENRE)).equals(genre)) {
                    // move the main cursor to the position of the cursor genre to fetch the data
                    context.cursor.moveToPosition(cursor.getPosition());

                    final MusicBean bean = new MusicBean();
                    bean.setPos(pos);
                    bean.setId(String.valueOf(context.cursor.getInt(context.cursor.getColumnIndex(DBHelper.MUSIC_ID))));
                    bean.setTitle(context.cursor.getString(context.cursor.getColumnIndex(DBHelper.TITLE_MUSIC)));
                    bean.setPath(context.cursor.getString(context.cursor.getColumnIndex(DBHelper.MUSIC_PATH)));

                    // add music to the adapter to update the UI
                    publishProgress(bean);

                    success = true;
                    pos++;
                }

            } while (cursor.moveToNext());

            cursor.close();
        }

        @Override
        protected void onProgressUpdate(Bean... values) {
            super.onProgressUpdate(values);

            activity.get().adapter.addItem(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (type == 2) {
                if (!success)
                    Toast.makeText(activity.get().getApplicationContext(), "No music was found!", Toast.LENGTH_SHORT).show();
            }

            activity = null;
        }
    }
}
