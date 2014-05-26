package pt.ua.travis.ui.main;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.common.collect.Lists;
import pt.ua.travis.R;
import pt.ua.travis.backend.*;
import pt.ua.travis.ui.customviews.LockedTransitionViewPager;
import pt.ua.travis.ui.customviews.TransitionViewPager;
import pt.ua.travis.ui.login.LoginActivity;
import pt.ua.travis.ui.navigationdrawer.DrawerItem;
import pt.ua.travis.ui.navigationdrawer.DrawerView;
import pt.ua.travis.ui.navigationdrawer.DrawerViewAdapter;
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
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private static List<DrawerView> drawerViews;

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

        if(drawerViews==null) {
            drawerViews = Lists.newArrayList();
            fillDrawerNavigation(drawerViews);
        }

        tabPager = (LockedTransitionViewPager) findViewById(R.id.tab_pager);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        updateDrawerList();



        ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#CCFFFFFF")));
        bar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#CCFFFFFF")));
        bar.setDisplayShowTitleEnabled(true);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        forceTabs(); // Force tabs when activity starts.
        bar.setDisplayShowTitleEnabled(false);
        bar.setDisplayHomeAsUpEnabled(false);
        bar.setDisplayShowHomeEnabled(true);
        bar.setHomeButtonEnabled(true);
        bar.setIcon(R.drawable.ic_menu);


        drawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
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

    protected final void updateDrawerList(){
        drawerList.setAdapter(new DrawerViewAdapter(this, drawerViews));
        drawerList.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DrawerView clickedView = (DrawerView) drawerList.getAdapter().getItem(position);
                if(clickedView instanceof DrawerItem){
                    onDrawerItemClick(((DrawerItem) clickedView).itemID);
                }
            }
        });
    }

    /**
     * Populates the drawer navigation menu.
     *
     * @param drawerViews the list that must be populated to translate into
     *                    items or indicators in the drawer navigation menu
     */
    protected abstract void fillDrawerNavigation(List<DrawerView> drawerViews);

    /**
     * Overridden method to sync the toggle state of the navigation drawer
     * after onRestoreInstanceState has occurred.
     *
     * {@inheritDoc}
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    /**
     * Overridden method to pass any configuration change to the navigation
     * drawer toggle state.
     *
     * {@inheritDoc}
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        forceTabs();
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            if (drawerLayout.isDrawerOpen(drawerList)) {
                drawerLayout.closeDrawer(drawerList);
            } else {
                drawerLayout.openDrawer(drawerList);
            }

            return true;
        }
        return false;
    }

    public void logout(View view){
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(LoginActivity.NO_AUTO_LOGIN, true);
        startActivity(intent);
        drawerViews = null;
        PersistenceManager.logout();
        finish();
    }

    /**
     * EXTENDING CLASSES MUST OVERRIDE THIS
     * @param itemID
     */
    protected void onDrawerItemClick(int itemID){
        drawerList.setItemChecked(itemID, true);
        drawerLayout.closeDrawer(drawerList);
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