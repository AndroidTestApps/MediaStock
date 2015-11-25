package com.example.mediastock.activities;


import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.mediastock.R;
import com.example.mediastock.data.Bean;
import com.example.mediastock.data.DBController;
import com.example.mediastock.data.DBHelper;
import com.example.mediastock.data.MusicBean;
import com.example.mediastock.util.MusicVideoAdapter;

import java.lang.ref.WeakReference;

public class FavoriteMusicActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FloatingActionButton fabFilter;
    private MusicVideoAdapter adapter;
    private ProgressBar progressBar;
    private DBController db;
    private Cursor cursor;
    private int musicID_temp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_music_video);

        db = new DBController(this);
        cursor = db.getMusicInfo();

        recyclerView = (RecyclerView) this.findViewById(R.id.list_music_video_fav);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        adapter = new MusicVideoAdapter(this, 1, true);
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
            new AsyncDBWork(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

        // save the position that this items has in the adapter
        musicID_temp = position;

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

        cursor = db.getMusicInfo();

        // a music has been removed from favorites
        if (cursorCountBefore > cursor.getCount())
            adapter.deleteItemAt(musicID_temp);

        // a new music has been added to favorites
        if (cursorCountBefore < cursor.getCount()) {
            cursor.moveToLast();

            final MusicBean bean = new MusicBean();
            bean.setPos(adapter.getItemCount());
            bean.setId(String.valueOf(cursor.getInt(cursor.getColumnIndex(DBHelper.MUSIC_ID))));
            bean.setTitle(cursor.getString(cursor.getColumnIndex(DBHelper.TITLE_MUSIC)));
            bean.setPath(cursor.getString(cursor.getColumnIndex(DBHelper.MUSIC_PATH)));

            adapter.addItem(bean);
        }
    }

    /**
     * Class to get in background the images from the database.
     */
    private static class AsyncDBWork extends AsyncTask<Void, Bean, Void> {
        private static WeakReference<FavoriteMusicActivity> activity;

        public AsyncDBWork(FavoriteMusicActivity context) {
            activity = new WeakReference<>(context);
        }


        @Override
        protected Void doInBackground(Void... params) {
            final FavoriteMusicActivity context = activity.get();
            int pos = 0;

            do {

                final MusicBean bean = new MusicBean();
                bean.setPos(pos);
                bean.setId(String.valueOf(context.cursor.getInt(context.cursor.getColumnIndex(DBHelper.MUSIC_ID))));
                bean.setTitle(context.cursor.getString(context.cursor.getColumnIndex(DBHelper.TITLE_MUSIC)));
                bean.setPath(context.cursor.getString(context.cursor.getColumnIndex(DBHelper.MUSIC_PATH)));

                publishProgress(bean);

                pos++;

            } while (context.cursor.moveToNext());

            return null;
        }

        @Override
        protected void onProgressUpdate(Bean... values) {
            super.onProgressUpdate(values);

            activity.get().adapter.addItem(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            activity = null;
        }
    }


}
