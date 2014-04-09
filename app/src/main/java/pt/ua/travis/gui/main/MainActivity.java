package pt.ua.travis.gui.main;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.common.collect.Lists;
import pt.ua.travis.R;
import pt.ua.travis.backend.entities.CloudBackendManager;
import pt.ua.travis.gui.login.LoginActivity;
import pt.ua.travis.gui.ridelist.RideDeletedListener;

import java.util.List;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public abstract class MainActivity extends SherlockFragmentActivity implements RideDeletedListener {

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private static List<DrawerView> drawerViews;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        if(drawerViews==null) {
            drawerViews = Lists.newArrayList();
            fillDrawerNavigation(drawerViews);
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.sideMenu);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerList = (ListView) findViewById(R.id.left_drawer);
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

        ActionBar bar = getSupportActionBar();
        bar.setDisplayShowTitleEnabled(false);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                R.drawable.ic_navigation_drawer,
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
        CloudBackendManager.logout();
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
}