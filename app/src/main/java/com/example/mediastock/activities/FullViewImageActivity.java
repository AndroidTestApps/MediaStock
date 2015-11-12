package com.example.mediastock.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.example.mediastock.R;
import com.example.mediastock.data.ImageBean;
import com.example.mediastock.util.Utilities;
import com.squareup.picasso.Picasso;

/**
 * Created by dinu on 25/10/15.
 */
public class FullViewImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_view_activity);

        ImageView image = (ImageView) findViewById(R.id.full_view_image);

        if (getIntentType() == 1) {
            String url = getIntent().getStringExtra("image");

            Picasso.with(getApplicationContext()).load(url).placeholder(R.drawable.border).fit().centerInside().into(image);

        } else
            image.setImageBitmap(Utilities.convertToBitmap(getBeanFromIntent().getImage()));

    }

    private int getIntentType() {
        return getIntent().getBundleExtra("bean").getInt("type");
    }

    private ImageBean getBeanFromIntent() {
        return getIntent().getBundleExtra("bean").getParcelable("bean");
    }
}
