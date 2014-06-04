package pt.ua.travis.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.common.collect.Lists;
import pt.ua.travis.R;
import pt.ua.travis.backend.Callback;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Ride;
import pt.ua.travis.backend.User;
import pt.ua.travis.core.SplashScreenActivity;
import pt.ua.travis.ui.customviews.*;
import pt.ua.travis.ui.login.LoginActivity;
import pt.ua.travis.ui.ridelist.RideDeletedListener;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;

import java.lang.reflect.Method;
import java.util.List;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public abstract class MainActivity extends SherlockFragmentActivity implements RideDeletedListener {

    private FrameLayout container;
    protected LockedTransitionViewPager tabPager;
    private PullToRefreshLayout pullToRefreshLayout;
    private BlurDrawerLayout drawerLayout;
    private static List<BlurDrawerObject> drawerItems;


    private static List<Ride> rideList;

    public void getRideList(boolean forceQuery, final Callback<List<Ride>> callback) {
        if (rideList == null || forceQuery) {
            final User u = PersistenceManager.getCurrentlyLoggedInUser();

            new AsyncTask<Void, Void, List<Ride>>() {
                @Override
                protected List<Ride> doInBackground(Void... params) {
                    return PersistenceManager.query().rides().withUser(u).uncompleted().sortedByTime().now();
                }

                @Override
                protected void onPostExecute(List<Ride> result) {
                    super.onPostExecute(result);
                    rideList = result;
                    if (callback != null) {
                        callback.onResult(rideList);
                    }
                }
            }.execute();
        } else {
            if (callback != null) {
                callback.onResult(rideList);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);

        if(drawerItems == null) {
            drawerItems = Lists.newArrayList();
            User u = PersistenceManager.getCurrentlyLoggedInUser();
            BlurDrawerUser userItem = new BlurDrawerUser(this, u);

            BlurDrawerItem logoutItem = new BlurDrawerItem(this, R.drawable.ic_logout, R.string.logout);
            logoutItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logout(null);
                }
            });

            BlurDrawerItem settingsItem = new BlurDrawerItem(this, R.drawable.ic_settings, R.string.settings);
            settingsItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO
                }
            });

            CloseDrawerAction closeDrawerAction = new CloseDrawerAction() {
                @Override
                public void closeDrawer() {
                    drawerLayout.close();
                }
            };

            drawerItems.add(userItem);
            fillDrawerNavigation(drawerItems, closeDrawerAction);
            drawerItems.add(settingsItem);
            drawerItems.add(logoutItem);
        }

        tabPager = (LockedTransitionViewPager) findViewById(R.id.tab_pager);
        tabPager.setOffscreenPageLimit(0);

        drawerLayout = new BlurDrawerLayout(this);
        drawerLayout.disableSide(BlurDrawerLayout.RIGHT_SIDE);
        drawerLayout.setShadowVisible(true);
        drawerLayout.attachToActivity(this);
        for(BlurDrawerObject item : drawerItems) {
            drawerLayout.addDrawerObject(item, BlurDrawerLayout.LEFT_SIDE);
        }


        ActionBar bar = getSupportActionBar();
//        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#CCFFFFFF")));
//        bar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#CCFFFFFF")));
        bar.setDisplayShowTitleEnabled(true);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        forceTabs(); // Force tabs when activity starts.
        bar.setDisplayShowTitleEnabled(false);
        bar.setDisplayHomeAsUpEnabled(false);
        bar.setDisplayShowHomeEnabled(true);
        bar.setHomeButtonEnabled(true);
        bar.setIcon(R.drawable.ic_menu);

        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFFFF")));
        bar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFFFF")));

        super.onCreate(savedInstanceState);

//        drawerToggle = new ActionBarDrawerToggle(this,
//                drawerLayout,
//                R.drawable.ic_drawer,
//                R.string.drawer_open,
//                R.string.drawer_close) {
//
//            public void onDrawerClosed(View view) {
//                invalidateOptionsMenu();
//            }
//
//            public void onDrawerOpened(View drawerView) {
//                invalidateOptionsMenu();
//            }
//        };
//        drawerLayout.setDrawerListener(drawerToggle);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void forceTabs(){
        try {
            final android.app.ActionBar actionBar = getActionBar();
            final Method setHasEmbeddedTabsMethod = actionBar.getClass()
                    .getDeclaredMethod("setHasEmbeddedTabs", boolean.class);
            setHasEmbeddedTabsMethod.setAccessible(true);
            setHasEmbeddedTabsMethod.invoke(actionBar, true);
        }
        catch(final Exception e) {
            Log.e("---------", "", e);
            // Handle issues as needed: log, warn user, fallback etc
            // Alternatively, ignore this and default tab behaviour will apply.
        }
    }

    //    protected final void updateDrawerList(){
//        drawerList.setAdapter(new DrawerViewAdapter(this, drawerItems));
//        drawerList.setOnItemClickListener(new ListView.OnItemClickListener(){
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                DrawerView clickedView = (DrawerView) drawerList.getAdapter().getItem(position);
//                if(clickedView instanceof DrawerItem){
//                    onDrawerItemClick(((DrawerItem) clickedView).itemID);
//                }
//            }
//        });
//    }

    protected interface CloseDrawerAction {
        void closeDrawer();
    }

    /**
     * Populates the drawerLayout navigation menu.
     *
     * @param drawerItems the list that must be populated to translate into
     *                    items or indicators in the drawerLayout navigation menu
     */
    protected abstract void fillDrawerNavigation(List<BlurDrawerObject> drawerItems, CloseDrawerAction closeDrawerAction);

    /**
     * Overridden method to sync the toggle state of the navigation drawerLayout
     * after onRestoreInstanceState has occurred.
     *
     * {@inheritDoc}
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    /**
     * Overridden method to pass any configuration change to the navigation
     * drawerLayout toggle state.
     *
     * {@inheritDoc}
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        forceTabs();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            if(drawerLayout.isOpened()){
                drawerLayout.close();
            } else {
                drawerLayout.open(BlurDrawerLayout.LEFT_SIDE);
            }

            return true;
        }
        return false;
    }

    public void logout(View view){
        SharedPreferences prefs = this.getSharedPreferences("TravisPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SplashScreenActivity.DO_AUTO_LOGIN, false);
        editor.putString(SplashScreenActivity.AUTO_EMAIL, "");
        editor.putString(SplashScreenActivity.AUTO_PASS, "");
        editor.commit();
        PersistenceManager.logout();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    protected abstract class TabPagerAdapter extends FragmentPagerAdapter {

        public TabPagerAdapter() {
            super(getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f = getFragment(position);
//            tabPager.setObjectForPosition(f, position);
            return f;
        }

        public abstract Fragment getFragment(int position);
    }
}