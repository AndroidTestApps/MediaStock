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

import com.example.mediastock.R;
import com.example.mediastock.data.Bean;
import com.example.mediastock.data.DBController;
import com.example.mediastock.data.MusicBean;
import com.example.mediastock.util.MusicVideoAdapter;

import java.lang.ref.WeakReference;

public class FavoriteMusicVideosActivity extends AppCompatActivity {
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

        //cursor = db.getMusic();

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
                final MusicBean bean = (MusicBean) adapter.getBeanAt(position);
                final Bundle bundle = new Bundle();

                Intent intent = new Intent(getApplicationContext(), MusicPlayerActivity.class);
                bundle.putParcelable("bean", bean);
                intent.putExtra("bean", bundle);
                bundle.putInt("type", 2);

                musicID_temp = position;

                startActivity(intent);
                overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);
            }
        });

        // new AsyncDBWork(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private static class AsyncWork extends AsyncTask<Void, Bean, Void> {
        private static WeakReference<FavoriteMusicVideosActivity> activity;

        public AsyncWork(FavoriteMusicVideosActivity context) {
            activity = new WeakReference<>(context);
        }


        @Override
        protected Void doInBackground(Void... params) {
            final FavoriteMusicVideosActivity context = activity.get();
            int pos = 0;

            while (activity.get().cursor.moveToNext()) {
                /*
                final MusicBean bean = new MusicBean();
                bean.setPos(pos);
                bean.setId(String.valueOf(context.cursorPart1.getInt(context.cursorPart1.getColumnIndex(DBHelper.MUSIC_ID))));
                bean.setTitle(context.getString(context.cursorPart1.getColumnIndex(DBHelper.TITLE_MUSIC)));
                bean.setByteArrayLength(context.cursorPart1.getBlob(context.cursorPart1.getColumnIndex(DBHelper.MUSIC_PATH)).length);
                bean.setByteMusic(context.cursorPart1.getBlob(context.cursorPart1.getColumnIndex(DBHelper.MUSIC_PATH)));

                publishProgress(bean);

                pos++; */
            }

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
