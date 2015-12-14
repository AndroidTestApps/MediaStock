package com.example.mediastock.model;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.example.mediastock.data.Bean;

import java.util.ArrayList;


public abstract class AbstractMediaAdapter extends RecyclerView.Adapter<AbstractMediaAdapter.MediaHolder> {
    private final Context context;
    private final int type;
    private final int width;
    private int pageNumber;
    // 1 - recent media ; 2 - search for media ; other number - no loading
    private int loadingType;
    private ArrayList<Bean> items = new ArrayList<>();
    private OnItemClickListener itemClickListener;
    private OnBottomListener bottomListener;

    public AbstractMediaAdapter(Context context, int type) {
        this.context = context;
        this.type = type;
        this.width = context.getResources().getDisplayMetrics().widthPixels;
    }

    @Override
    public abstract MediaHolder onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(MediaHolder holder, int position);


    @Override
    public int getItemCount() {
        return items.size();
    }

    public Bean getBeanAt(int position) {
        return items.get(position);
    }

    public void addItem(Bean bean) {
        items.add(bean.getPos(), bean);

        // Android dev said it is a bug on the RecyclerView invoking notifyItemInserted() at pos 0
        if (bean.getPos() != 0)
            notifyItemInserted(bean.getPos());
        else
            notifyDataSetChanged();
    }

    public void deleteItems() {
        if (!items.isEmpty()) {
            items.clear();
            notifyDataSetChanged();
        }
    }


    public void setOnBottomListener(final OnBottomListener listener) {
        this.bottomListener = listener;
    }

    public void setOnItemClickListener(final OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public int getWidth() {
        return width;
    }

    public OnItemClickListener getItemClickListener() {
        return itemClickListener;
    }

    public OnBottomListener getBottomListener() {
        return bottomListener;
    }

    public int getType() {
        return type;
    }

    public Context getContext() {
        return context;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int number) {
        this.pageNumber = number;
    }

    public int getLoadingType() {
        return loadingType;
    }

    public void setLoadingType(int loadingType) {
        this.loadingType = loadingType;
    }


    /**
     * Interface used to load more data when reaching the end of the current list items
     */
    public interface OnBottomListener {
        void onBottomLoadMoreData(int loadingType, int loadingPageNumber);
    }

    /**
     * Interface used to intercept a click on the item
     */
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public abstract static class MediaHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public MediaHolder(View itemView) {
            super(itemView);
        }

        @Override
        public abstract void onClick(View v);
    }
}
