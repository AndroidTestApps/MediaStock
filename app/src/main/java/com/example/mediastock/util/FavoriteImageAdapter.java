package com.example.mediastock.util;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.mediastock.R;
import com.example.mediastock.data.Database;

import java.lang.ref.WeakReference;


public class FavoriteImageAdapter extends RecyclerView.Adapter<FavoriteImageAdapter.ViewHolder> {
    private final int width;
    private final Context context;
    private final CursorAdapter cursorAdapter;
    private final RelativeLayout.LayoutParams layout_param;
    private OnImageClickListener image_listener;

    public FavoriteImageAdapter(final Context context, Cursor cursor) {
        this.context = context;
        this.width = context.getResources().getDisplayMetrics().widthPixels;
        this.layout_param = new RelativeLayout.LayoutParams(width / 2, width / 2);
        this.layout_param.setMargins(3, 2, 0, 2);

        this.cursorAdapter = new CursorAdapter(context, cursor, 0) {

            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.grid_image_favorites, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                ((ImageView) view).setImageBitmap(
                        Utilities.convertToBitmap(cursor.getBlob(cursor.getColumnIndex(Database.IMAGE))));
            }
        };
    }


    public CursorAdapter getCursorAdapter() {
        return cursorAdapter;
    }

    public void closeCursor() {
        cursorAdapter.getCursor().close();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = cursorAdapter.newView(context, cursorAdapter.getCursor(), parent);

        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursorAdapter.getCursor().moveToPosition(position);
        cursorAdapter.bindView(holder.ivIcon, context, cursorAdapter.getCursor());
    }


    @Override
    public int getItemCount() {
        return cursorAdapter.getCount();
    }


    public void setOnImageClickListener(final OnImageClickListener listener) {
        this.image_listener = listener;
    }

    /**
     * Interface used to register a callback when a click on the image is triggered
     */
    public interface OnImageClickListener {
        void onImageClick(View view, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView ivIcon;
        private WeakReference<FavoriteImageAdapter> ref;

        public ViewHolder(View itemView, FavoriteImageAdapter adapter) {
            super(itemView);

            ref = new WeakReference<>(adapter);
            ivIcon = (ImageView) itemView.findViewById(R.id.ivIconn);
            ivIcon.setLayoutParams(ref.get().layout_param);
            ivIcon.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (ref.get().image_listener != null)
                ref.get().image_listener.onImageClick(v, getAdapterPosition());
        }
    }
}
