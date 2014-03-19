package pt.ua.travis.gui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.agimind.widget.SlideHolder;
import pt.ua.travis.R;
import pt.ua.travis.gui.taxichooser.LandscapeFragment;
import pt.ua.travis.gui.taxichooser.PortraitFragment;
import pt.ua.travis.gui.taxichooser.TaxiChooserFragment;
import pt.ua.travis.utils.Validate;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class ClientMain extends SherlockFragmentActivity {

    public static final String CURRENTLY_SELECTED_INDEX = "selected_index";

    private PortraitFragment portraitFragment;
    private LandscapeFragment landscapeFragment;
    private TaxiChooserFragment currentlyShownFragment;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_main);



//        // Check that the activity is using the layout version with
//        // the fragment_container FrameLayout
//        if (findViewById(R.id.fragment_container) != null) {

//            // However, if we're being restored from a previous state,
//            // then we don't need to do anything and should return or else
//            // we could end up with overlapping fragments.

        int selectedIndex = 0;
        if (savedInstanceState != null) {
            Log.e("ITS HAS A SAVE!", "");
            selectedIndex = savedInstanceState.getInt(CURRENTLY_SELECTED_INDEX, 0);
        }

        if(Validate.isLandscape(this)) {
            landscapeFragment = new LandscapeFragment();
            landscapeFragment.setRetainInstance(false);
            showFragment(landscapeFragment, selectedIndex);
        } else {
            portraitFragment = new PortraitFragment();
            portraitFragment.setRetainInstance(false);
            showFragment(portraitFragment, selectedIndex);
        }
//        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final SlideHolder sideMenu = (SlideHolder) findViewById(R.id.sideMenu);
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

    private void showFragment(TaxiChooserFragment f, int currentSelectedIndex) {

        Bundle args = new Bundle();
        args.putInt(CURRENTLY_SELECTED_INDEX, currentSelectedIndex);
        f.setArguments(args);

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, f)
                .commit();


        currentlyShownFragment = f;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(currentlyShownFragment);
        ft.commit();

        outState.putInt(CURRENTLY_SELECTED_INDEX, currentlyShownFragment.getCurrentSelectedIndex());

        super.onSaveInstanceState(outState);
    }
}