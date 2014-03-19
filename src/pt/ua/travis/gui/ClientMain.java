package pt.ua.travis.gui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.View;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.agimind.widget.SlideHolder;
import com.google.android.gms.maps.SupportMapFragment;
import pt.ua.travis.R;
import pt.ua.travis.utils.Tools;
import pt.ua.travis.utils.Validate;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class ClientMain extends SherlockFragmentActivity {

    private SlideHolder sideMenu;

    private PortraitFragment portraitFragment;
    private LandscapeFragment landscapeFragment;
    private TravisFragment currentlyShownFragment;



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

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            landscapeFragment = new LandscapeFragment();
            portraitFragment = new PortraitFragment();

            if(Validate.isLandscape(this)) {
                showFirstFragment(landscapeFragment);
            } else {
                showFirstFragment(portraitFragment);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            showFragment(landscapeFragment);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            showFragment(portraitFragment);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ActionBar bar = getSupportActionBar();
        bar.setDisplayShowTitleEnabled(false);

        if (Validate.isTablet(this)) {
            getSupportMenuInflater().inflate(R.menu.client_actions_tablet, menu);

        } else {
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
//                List<TravisTaxi> result = new ArrayList<>();
//
//                // looks for matches and adds them to a temporary list
//                for (TravisTaxi t : taxiList.getList()) {
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

    private void showFirstFragment(TravisFragment firstFragment){
        firstFragment.setArguments(getIntent().getExtras());

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, firstFragment)
                .commit();

        currentlyShownFragment = firstFragment;
    }

    private void showFragment(TravisFragment f){
        // Pass the currently selected taxi
        Bundle args = new Bundle();
//        args.putInt(, position);
        f.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, f);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();

        currentlyShownFragment = f;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return super.onMenuItemSelected(featureId, item);
    }
}