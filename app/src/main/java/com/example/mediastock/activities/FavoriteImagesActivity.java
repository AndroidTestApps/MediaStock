package com.example.mediastock.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.example.mediastock.R;
import com.example.mediastock.data.Database;
import com.example.mediastock.data.ImageBean;
import com.example.mediastock.util.FavoriteImageAdapter;

public class FavoriteImagesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FloatingActionButton fab_filter;
    private FavoriteImageAdapter adapter;
    private ProgressBar progressBar;
    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_images);

        db = new Database(this);

        // layout init
        recyclerView = (RecyclerView) this.findViewById(R.id.gridView_fav_images);
        recyclerView.setHasFixedSize(true);
        progressBar = (ProgressBar) this.findViewById(R.id.p_img_bar);
        fab_filter = (FloatingActionButton) this.findViewById(R.id.fab_fav_img_search);
        fab_filter = (FloatingActionButton) this.findViewById(R.id.fab_fav_img_search);
        GridLayoutManager grid = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(grid);
        adapter = new FavoriteImageAdapter(getApplicationContext(), db.getImages());
        recyclerView.setAdapter(adapter);
        adapter.setOnImageClickListener(new FavoriteImageAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(View view, int position) {

                goToDisplayImageActivity(adapter.getCursorAdapter().getCursor(), position);
            }
        });

    }

    /**
     * Method to start the DisplayImageActivity to see the details of the image selected.
     *
     * @param cursor the cursor of the query
     * @param position the position of the selected image
     */
    private void goToDisplayImageActivity(Cursor cursor, int position) {
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

        startActivity(intent);
        overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // close the cursor
        adapter.closeCursor();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (adapter != null && adapter.getCursorAdapter().getCursor().isClosed()) {
            adapter.notifyDataSetChanged();
            adapter.getCursorAdapter().changeCursor(db.getImages());
        }
    }
}
