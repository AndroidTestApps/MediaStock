package com.example.mediastock.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.example.mediastock.R;
import com.example.mediastock.data.Bean;
import com.example.mediastock.data.DBController;
import com.example.mediastock.data.DBHelper;
import com.example.mediastock.data.VideoBean;
import com.example.mediastock.util.MusicVideoAdapter;

import java.lang.ref.WeakReference;


public class FavoriteVideosActivity extends AppCompatActivity {
    //private FloatingActionButton fabFilter;
    private MusicVideoAdapter adapter;
    private DBController db;
    private Cursor cursor;
    private int videoID_temp = 0;
    private int cursorTempCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_music_video);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Favorite videos");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // get video infos
        db = new DBController(this);
        cursor = db.getVideosInfo();

        RecyclerView recyclerView = (RecyclerView) this.findViewById(R.id.list_music_video_fav);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        adapter = new MusicVideoAdapter(this, 2, true);
        recyclerView.setAdapter(adapter);

        // on item click
        adapter.setOnItemClickListener(new MusicVideoAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {

                goToVideoPlayerActivity(position);
            }
        });

        if (cursor.getCount() == 0)
            Toast.makeText(getApplicationContext(), "There are no videos saved", Toast.LENGTH_SHORT).show();
        else
            new AsyncDBWork(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); //get video info from db

    }

    /**
     * Open VideoPlayerActivity activity
     */
    private void goToVideoPlayerActivity(int position) {
        final VideoBean bean = (VideoBean) adapter.getBeanAt(position);
        final Bundle bundle = new Bundle();

        Intent intent = new Intent(getApplicationContext(), VideoPlayerActivity.class);
        bundle.putParcelable("bean", bean);
        intent.putExtra("bean", bundle);
        bundle.putInt("type", 2);

        // save the position that this items has in the adapter
        videoID_temp = position;

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
        cursor = db.getVideosInfo();

        // a video has been removed from favorites
        if (cursorTempCount > cursor.getCount())
            adapter.deleteItemAt(videoID_temp);

        // a new video has been added to favorites
        if (cursorTempCount < cursor.getCount()) {
            cursor.moveToLast();

            final VideoBean bean = new VideoBean();
            bean.setPos(adapter.getItemCount());
            bean.setId(String.valueOf(cursor.getInt(cursor.getColumnIndex(DBHelper.VIDEO_ID))));
            bean.setDescription(cursor.getString(cursor.getColumnIndex(DBHelper.DESCRIPTION_VIDEO)));
            bean.setPath(cursor.getString(cursor.getColumnIndex(DBHelper.MUSIC_PATH)));

            adapter.addItem(bean);
        }
    }


    /**
     * Class to get in background the video info from the database.
     */
    private static class AsyncDBWork extends AsyncTask<Void, Bean, Void> {
        private static WeakReference<FavoriteVideosActivity> activity;

        public AsyncDBWork(FavoriteVideosActivity context) {
            activity = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... params) {
            final FavoriteVideosActivity context = activity.get();
            int pos = 0;

            do {

                final VideoBean bean = new VideoBean();
                bean.setPos(pos);
                bean.setId(String.valueOf(context.cursor.getInt(context.cursor.getColumnIndex(DBHelper.VIDEO_ID))));
                bean.setDescription(context.cursor.getString(context.cursor.getColumnIndex(DBHelper.DESCRIPTION_VIDEO)));
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