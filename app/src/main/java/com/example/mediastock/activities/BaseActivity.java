package com.example.mediastock.activities;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mediastock.R;
import com.example.mediastock.util.CustomPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity that contains a View Pager with has six fragments
 */
public class BaseActivity extends AppCompatActivity implements FilterImageFragment.FilterImageMessage, FilterMusicFragment.FilterMusicMessage, FilterVideoFragment.FilterVideoMessage {
    private static String key1;
    private static String key2;
    private static Context context;
    private CustomPagerAdapter adapter;
    private ViewPager viewPager;
    private EditText input;


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.main_activity);

        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setTabTextColors(ColorStateList.valueOf(Color.parseColor("#e7ff5800")));
        tabLayout.setSelectedTabIndicatorColor(Color.WHITE);
        tabLayout.addTab(tabLayout.newTab().setText("Images"));
        tabLayout.addTab(tabLayout.newTab().setText("Videos"));
        tabLayout.addTab(tabLayout.newTab().setText("Music"));
        tabLayout.addTab(tabLayout.newTab().setText("Filter images"));
        tabLayout.addTab(tabLayout.newTab().setText("Filter videos"));
        tabLayout.addTab(tabLayout.newTab().setText("Filter music"));

        viewPager = (ViewPager) findViewById(R.id.pager);
        adapter = new CustomPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), getFragments());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /**
     * Callback method to do a filter search of the images
     * @param bundle bundle that contains the users input
     */
    @Override
    public void handleFilterImage(Bundle bundle) {
        ImagesFragment frag = (ImagesFragment) adapter.getItem(0);
        viewPager.setCurrentItem(0);
        frag.startFilterSearch(bundle);
    }


    /**
     * Callback method to do a filter search of the music
     * @param bundle bundle that contains the users input
     */
    @Override
    public void handleFilterMusic(Bundle bundle) {
        MusicFragment frag = (MusicFragment) adapter.getItem(2);
        viewPager.setCurrentItem(2);
        frag.startFilterSearch(bundle);
    }


    /**
     * Callback method to do a filter search of the videos
     * @param bundle bundle that contains the users input
     */
    @Override
    public void handleFilterVideo(Bundle bundle) {
        VideosFragment frag = (VideosFragment) adapter.getItem(1);
        viewPager.setCurrentItem(1);
        frag.startFilterSearch(bundle);
    }

    /**
     * Method that returns a list of fragments
     */
    public List<AbstractFragment> getFragments() {
        List<AbstractFragment> list = new ArrayList<>();

        list.add(ImagesFragment.createInstance());
        list.add(VideosFragment.createInstance());
        list.add(MusicFragment.createInstance());

        return list;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (isOnline()) {

            switch (item.getItemId()) {

                case R.id.item_search:

                    if (viewPager.getCurrentItem() == 0 || viewPager.getCurrentItem() == 1 || viewPager.getCurrentItem() == 2)
                        openSearch();

                    return true;

                case R.id.item_recent_images:
                    ImagesFragment frag = (ImagesFragment) adapter.getItem(0);
                    viewPager.setCurrentItem(0);
                    frag.getRecentImages();

                    return true;

                case R.id.item_recent_videos:
                    VideosFragment frag_v = (VideosFragment) adapter.getItem(1);
                    viewPager.setCurrentItem(1);
                    frag_v.getRecentVideos();

                    return true;

                case R.id.item_recent_music:
                    MusicFragment frag_m = (MusicFragment) adapter.getItem(2);
                    viewPager.setCurrentItem(2);
                    frag_m.getRecentMusic();

                    return true;
            }
        }

        return true;
    }

    /**
     * Search button action bar
     */
    private void openSearch() {
        setTitle("");
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.actionbar_search, null);
        actionBar.setCustomView(v);

        // icon dismiss inside edit text
        input = (EditText) v.findViewById(R.id.actionbar_search);
        input.requestFocus();
        showKeyboard();

        input.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= input.getRight() - input.getTotalPaddingRight()) {
                        input.setVisibility(View.GONE);
                        setTitle("MediaStock");
                        hideKeyboard(input);
                        return true;
                    }
                }
                return true;
            }
        });

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    startSearch(v.getText().toString(), input);

                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Method to handle the users input for the search
     */
    private void startSearch(String search, View view) {
        input.setVisibility(View.GONE);
        setTitle("MediaStock");
        hideKeyboard(view);
        boolean twoWords = parseQuery(search);

        int fragment = viewPager.getCurrentItem();
        switch (fragment) {

            case 0:
                ImagesFragment images = (ImagesFragment) adapter.getItem(0);

                if (twoWords)
                    images.searchImagesByKey(key1, key2);
                else
                    images.searchImagesByKey(search, null);

                break;

            case 1:
                VideosFragment videos = (VideosFragment) adapter.getItem(1);

                if (twoWords)
                    videos.searchVideosByKey(key1, key2);
                else
                    videos.searchVideosByKey(search, null);

                break;

            case 2:
                MusicFragment music = (MusicFragment) adapter.getItem(2);

                if (twoWords)
                    music.searchMusicByKey(key1, key2);
                else
                    music.searchMusicByKey(search, null);

                break;

            default:
                break;
        }
    }


    /**
     * We parse the text that the user wrote.
     * If the user wrote two words, we parse the text and search for the words.
     *
     * @param query the text of the user
     * @return true if the user wrote two words, false otherwise
     */
    private boolean parseQuery(String query) {
        boolean twoWords = false;

        for (int i = 0; i < query.length(); i++)
            if (query.charAt(i) == ' ') {
                key1 = query.substring(0, i);
                key2 = query.substring(i + 1, query.length());
                twoWords = true;
            }

        return twoWords;
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);


    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     * Checks if the device is connected to the Internet
     *
     * @return true if connected, false otherwise
     */
    public boolean isOnline() {
        Context context = getApplicationContext();

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

}
