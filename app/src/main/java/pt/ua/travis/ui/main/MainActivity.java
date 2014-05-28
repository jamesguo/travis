package pt.ua.travis.ui.main;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.common.collect.Lists;
import pt.ua.travis.R;
import pt.ua.travis.backend.*;
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

    protected LockedTransitionViewPager tabPager;
    private PullToRefreshLayout pullToRefreshLayout;
    private BlurDrawerLayout drawerLayout;
    private static List<BlurDrawerObject> drawerItems;


    private static List<Ride> rideList;

    public void getRideList(boolean forceQuery, final Callback<List<Ride>> callback) {
        if (rideList == null || forceQuery) {
            final User u = PersistenceManager.getUserLoggedInThisDevice();

            new AsyncTask<Void, Void, List<Ride>>() {
                @Override
                protected List<Ride> doInBackground(Void... params) {
                    return PersistenceManager.query().rides().withUser(u).scheduled().sortedByTime().now();
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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);

        if(drawerItems == null) {
            drawerItems = Lists.newArrayList();
            User u = PersistenceManager.getUserLoggedInThisDevice();
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

            drawerItems.add(userItem);
            fillDrawerNavigation(drawerItems);
            drawerItems.add(settingsItem);
            drawerItems.add(logoutItem);
        }

        tabPager = (LockedTransitionViewPager) findViewById(R.id.tab_pager);

//        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
//        drawerList = (ListView) findViewById(R.id.left_drawer);
//        updateDrawerList();

        drawerLayout = new BlurDrawerLayout(this);
        drawerLayout.disableSide(BlurDrawerLayout.RIGHT_SIDE);
        drawerLayout.setBackground(R.drawable.bokeh_travis);
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

    /**
     * Populates the drawerLayout navigation menu.
     *
     * @param drawerItems the list that must be populated to translate into
     *                    items or indicators in the drawerLayout navigation menu
     */
    protected abstract void fillDrawerNavigation(List<BlurDrawerObject> drawerItems);

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
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.NO_AUTO_LOGIN, true);
        startActivity(intent);
//        drawerItems = null;
        PersistenceManager.logout();
        finish();
    }


    protected abstract class TabPagerAdapter extends FragmentPagerAdapter {

        public TabPagerAdapter() {
            super(getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f = getFragment(position);
            tabPager.setObjectForPosition(f, position);
            return f;
        }

        public abstract Fragment getFragment(int position);
    }
}