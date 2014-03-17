package pt.ua.travis.gui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.View;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.agimind.widget.SlideHolder;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import pt.ua.travis.R;
import pt.ua.travis.maps.MarkersTaxi;
import pt.ua.travis.utils.Validate;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class ClientMain extends SherlockFragmentActivity {

    private SlideHolder sideMenu;
    private FragmentTabHost mTabHost;



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_main);

        sideMenu = (SlideHolder) findViewById(R.id.sideMenu);
        sideMenu.setDirection(SlideHolder.DIRECTION_LEFT);
        sideMenu.setAllowInterceptTouch(false);
        sideMenu.setEnabled(false);
        if(Validate.isTablet(this)){
            sideMenu.setAlwaysOpened(true);
        } else {
            sideMenu.setOnSlideListener(new SlideHolder.OnSlideListener() {
                @Override
                public void onSlideCompleted(boolean b) {
                    sideMenu.setEnabled(b);
                }
            });
        }

        // Locate android.R.id.tabhost in main_fragment.xml
        mTabHost = (FragmentTabHost) findViewById(R.id.tabhost);

        // Create the tabs in main_fragment.xml
        mTabHost.setup(this, getSupportFragmentManager(), R.id.tabcontent);

        // Create Tab1 with a custom image in res folder
        mTabHost.addTab(mTabHost.newTabSpec("tab1")
                .setIndicator("",
                        getResources().getDrawable(R.drawable.ic_action_map)),
                SupportMapFragment.class, null);

        // Create Tab2
        mTabHost.addTab(mTabHost.newTabSpec("tab2")
                .setIndicator("",
                        getResources().getDrawable(R.drawable.ic_action_paste)),
                TaxiList.class, null);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        if(Validate.isTablet(this)){
            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportMenuInflater().inflate(R.menu.client_actions_tablet, menu);

        } else {
            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportMenuInflater().inflate(R.menu.client_actions, menu);

            final MenuItem toggleItem = menu.findItem(R.id.action_side_menu_toggle);
            toggleItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    sideMenu.toggle();
                    return true;
                }
            });
        }

//        configureSearchBar(menu);


        return super.onPrepareOptionsMenu(menu);
    }

    private void configureSearchBar(Menu menu){

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();


        // if a search item is collapsed, resets the listview to show every added task
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
//                taxiList.unfilter();
                return true;
            }
        });


        // if search box loses focus, collapse it and reset listview
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                searchItem.collapseActionView();
            }
        });

        // sets a query listener to filter results while user writes in the search box
        // result filtering is just setting the listview with a new adapter with the matching results
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                List<Taxi> result = new ArrayList<>();
//
//                // looks for matches and adds them to a temporary list
//                for (Taxi t : taxiList.getList()) {
//                    if (newText.isEmpty() || t.toString().contains(newText))
//                        result.add(t);
//                }
//
//
//                // sets the listview adapter with an adapter for the temporary list of matches
//                taxiList.filter(result);

                return true;
            }
        });
    }
}