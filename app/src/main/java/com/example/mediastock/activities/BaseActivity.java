package com.example.mediastock.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mediastock.R;
import com.example.mediastock.util.CustomPagerAdapter;
import com.example.mediastock.util.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity that contains a View Pager with six fragments
 */
public class BaseActivity extends AppCompatActivity implements FilterImageFragment.FilterImageMessage, FilterMusicFragment.FilterMusicMessage, FilterVideoFragment.FilterVideoMessage {
    // the keywords to search for
    private static String key1;
    private static String key2;
    private CustomPagerAdapter adapter;
    private ViewPager viewPager;
    private boolean isFocused = false;
    private EditText editText;
    private FloatingActionButton favorites;

    /**
     * We parse the users input.
     * If the user wrote two words, we parse the text and search the words.
     *
     * @param query the users input
     * @return true if the user wrote two words, false otherwise
     */
    private static boolean parseQuery(String query) {
        if (query.contains(" ")) {
            for (int i = 0; i < query.length(); i++)
                if (query.charAt(i) == ' ') {
                    key1 = query.substring(0, i);

                    key2 = query.substring(i + 1, query.length());
                    return !key2.isEmpty();
                }
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.main_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!Utilities.deviceOnline(getApplicationContext()))
            showAlertDialogNoInternet();

/*
        DBController db = new DBController(this);
        db.deleteTableColorAndImage();
        db.createTableColorAndImage();
        Utilities.deleteAllMediaFromInternalStorage(Utilities.IMG_DIR, this);
*/

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setTabTextColors(Color.parseColor("#FF9F9F9F"), Color.parseColor("#ff453f"));
        tabLayout.setSelectedTabIndicatorColor(Color.WHITE);
        tabLayout.addTab(tabLayout.newTab().setText("Images"));
        tabLayout.addTab(tabLayout.newTab().setText("Videos"));
        tabLayout.addTab(tabLayout.newTab().setText("Music"));
        tabLayout.addTab(tabLayout.newTab().setText("Filter images"));
        tabLayout.addTab(tabLayout.newTab().setText("Filter videos"));
        tabLayout.addTab(tabLayout.newTab().setText("Filter music"));

        favorites = (FloatingActionButton) this.findViewById(R.id.fab_fav);
        favorites.bringToFront();
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

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {

                    case 0:
                    case 1:
                    case 2:
                        favorites.setVisibility(View.VISIBLE);
                        break;

                    case 3:
                    case 4:
                    case 5:
                        favorites.setVisibility(View.GONE);
                        break;

                    default:
                        break;
                }
            }
        });

        // favorites button; it starts the activity
        favorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (viewPager.getCurrentItem()) {

                    // Images
                    case 0:
                        Intent imgIntent = new Intent(getApplicationContext(), FavoriteImagesActivity.class);
                        startActivity(imgIntent);
                        overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);
                        break;

                    // Videos
                    case 1:
                        Intent videoIntent = new Intent(getApplicationContext(), FavoriteVideosActivity.class);
                        startActivity(videoIntent);
                        overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);
                        break;

                    // Music
                    case 2:
                        Intent musicIntent = new Intent(getApplicationContext(), FavoriteMusicActivity.class);
                        startActivity(musicIntent);
                        overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);
                        break;

                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder msg = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Dialog));
        msg.setTitle("MediaStock");
        msg.setMessage("Are you sure you want to exit ?");
        msg.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                finish();
            }
        });

        msg.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        msg.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Callback method to do a filter search of the images
     *
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
     *
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
     *
     * @param bundle bundle that contains the users input
     */
    @Override
    public void handleFilterVideo(Bundle bundle) {
        VideosFragment frag = (VideosFragment) adapter.getItem(1);
        viewPager.setCurrentItem(1);
        frag.startFilterSearch(bundle);
    }

    /**
     * Method that returns the list of fragments: ImagesFragment, VideosFragment, MusicFragment
     */
    public List<AbstractFragment> getFragments() {
        List<AbstractFragment> list = new ArrayList<>();

        list.add(ImagesFragment.createInstance());
        list.add(VideosFragment.createInstance());
        list.add(MusicFragment.createInstance());

        return list;
    }

    private void showAlertDialogNoInternet() {
        AlertDialog.Builder msg = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Dialog));
        msg.setTitle("MediaStock");
        msg.setMessage("There is no internet connection!");
        msg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        msg.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handling the menu items
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {

            case R.id.item_search:

                if (Utilities.deviceOnline(getApplicationContext())) {

                    if (viewPager.getCurrentItem() == 0 || viewPager.getCurrentItem() == 1 || viewPager.getCurrentItem() == 2) {

                        // search view is opened
                        if (isFocused)
                            startSearch(editText.getText().toString(), editText); // start searching the users input
                        else
                            openSearchView();
                    }
                } else
                    Toast.makeText(getApplicationContext(), "There is no internet connection", Toast.LENGTH_SHORT).show();

                return true;


            case R.id.item_recent_images:

                if (Utilities.deviceOnline(getApplicationContext())) {
                    ImagesFragment frag = (ImagesFragment) adapter.getItem(0);
                    viewPager.setCurrentItem(0);
                    frag.getRecentImages();
                } else
                    Toast.makeText(getApplicationContext(), "There is no internet connection", Toast.LENGTH_SHORT).show();

                return true;

            case R.id.item_recent_videos:

                if (Utilities.deviceOnline(getApplicationContext())) {
                    VideosFragment frag_v = (VideosFragment) adapter.getItem(1);
                    viewPager.setCurrentItem(1);
                    frag_v.getRecentVideos();

                } else
                    Toast.makeText(getApplicationContext(), "There is no internet connection", Toast.LENGTH_SHORT).show();

                return true;

            case R.id.item_recent_music:

                if (Utilities.deviceOnline(getApplicationContext())) {
                    MusicFragment frag_m = (MusicFragment) adapter.getItem(2);
                    viewPager.setCurrentItem(2);
                    frag_m.getRecentMusic();

                } else
                    Toast.makeText(getApplicationContext(), "There is no internet connection", Toast.LENGTH_SHORT).show();

                return true;

            case R.id.item_favoriteImages:
                Intent favoriteImagesIntent = new Intent(getApplicationContext(), FavoriteImagesActivity.class);
                startActivity(favoriteImagesIntent);
                overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);

                return true;

            case R.id.item_favoriteVideos:
                Intent favoriteVideosIntent = new Intent(getApplicationContext(), FavoriteVideosActivity.class);
                startActivity(favoriteVideosIntent);
                overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);

                return true;

            case R.id.item_favoriteMusic:
                Intent favoriteMusicIntent = new Intent(getApplicationContext(), FavoriteMusicActivity.class);
                startActivity(favoriteMusicIntent);
                overridePendingTransition(R.anim.trans_corner_from, R.anim.trans_corner_to);

                return true;
        }

        return true;
    }

    /**
     * Search button action bar
     */
    private void openSearchView() {
        isFocused = true;

        setTitle("");
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.actionbar_search, null);
        actionBar.setCustomView(v);

        editText = (EditText) v.findViewById(R.id.actionbar_search);
        editText.requestFocus();
        Utilities.showKeyboard(getApplicationContext());

        editText.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {

                    // icon dismiss edit text
                    if (event.getRawX() >= editText.getRight() - editText.getTotalPaddingRight()) {
                        isFocused = false;
                        editText.setVisibility(View.GONE);
                        setTitle("MediaStock");
                        Utilities.hideKeyboard(getApplicationContext(), editText);

                    } else {
                        isFocused = true;
                        Utilities.showKeyboard(getApplicationContext());
                    }
                }

                return true;
            }
        });

        // keyboard icon search listener
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    startSearch(v.getText().toString(), editText);

                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Method to search images, videos, or music.
     *
     * @param query the users input
     * @param editText the view editText
     */
    private void startSearch(String query, View editText) {
        isFocused = false;

        this.editText.setVisibility(View.GONE);
        setTitle("MediaStock");
        Utilities.hideKeyboard(getApplicationContext(), editText);

        boolean twoWords = parseQuery(query);

        int fragment = viewPager.getCurrentItem();
        switch (fragment) {

            case 0:
                ImagesFragment images = (ImagesFragment) adapter.getItem(0);

                if (twoWords)
                    images.searchImagesByKey(key1, key2);
                else
                    images.searchImagesByKey(query, null);

                break;

            case 1:
                VideosFragment videos = (VideosFragment) adapter.getItem(1);

                if (twoWords)
                    videos.searchVideosByKey(key1, key2);
                else
                    videos.searchVideosByKey(query, null);

                break;

            case 2:
                MusicFragment music = (MusicFragment) adapter.getItem(2);

                if (twoWords)
                    music.searchMusicByKey(key1, key2);
                else
                    music.searchMusicByKey(query, null);

                break;

            default:
                break;
        }
    }

}
