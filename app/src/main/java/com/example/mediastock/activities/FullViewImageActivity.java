package com.example.mediastock.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.mediastock.R;
import com.example.mediastock.data.ImageBean;
import com.example.mediastock.util.Utilities;

/**
 * Class to display a full screen image.
 */
public class FullViewImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_view_activity);

        ImageView image = (ImageView) findViewById(R.id.full_view_image);

        if (getIntentType() == 1) {
            String url = getIntent().getStringExtra("image");

            Glide.with(this).load(url).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).placeholder(R.drawable.border).error(R.drawable.border).into(image);

        } else
            Glide.with(this).load(Utilities.loadImageFromInternalStorage(this, getBeanFromIntent().getPath()))
                    .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).placeholder(R.drawable.border).error(R.drawable.border).into(image);


    }

    private int getIntentType() {
        return getIntent().getBundleExtra("bean").getInt("type");
    }

    private ImageBean getBeanFromIntent() {
        return getIntent().getBundleExtra("bean").getParcelable("bean");
    }
}
