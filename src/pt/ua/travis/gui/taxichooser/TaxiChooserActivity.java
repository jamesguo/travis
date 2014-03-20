package pt.ua.travis.gui.taxichooser;

import android.os.Bundle;
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
import pt.ua.travis.core.Taxi;
import pt.ua.travis.db.Geolocation;
import pt.ua.travis.db.TravisDB;
import pt.ua.travis.gui.taxichooser.TaxiChooserLandscapeFragment;
import pt.ua.travis.gui.taxichooser.TaxiChooserPortraitFragment;
import pt.ua.travis.gui.taxichooser.TaxiChooserFragment;
import pt.ua.travis.utils.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TaxiChooserActivity extends SherlockFragmentActivity {

    public static final String CURRENTLY_SELECTED_INDEX = "selected_index";

    private SlideHolder sideMenu;
    private static List<Taxi> taxiList;
    private static List<Taxi> filteredTaxiList;
    private TaxiChooserFragment currentlyShownFragment;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_main);

        if(taxiList==null) {
            taxiList = TravisDB.getTaxisFromDB();
            filteredTaxiList = new ArrayList<>(taxiList);
            Geolocation.sortByProximity(filteredTaxiList);
        }

        int selectedIndex = 0;
        if (savedInstanceState != null) {
            Log.e("ITS HAS A SAVE!", "");
            selectedIndex = savedInstanceState.getInt(CURRENTLY_SELECTED_INDEX, 0);
        }

        Log.e("HERE", filteredTaxiList.toString());
        showFilteredResults(selectedIndex);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
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

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        switch(item.getItemId()) {
            case android.R.id.home:
                return true;
        }

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



    private void showFilteredResults(int selectedIndex){
        if(Validate.isLandscape(this)) {
            TaxiChooserLandscapeFragment landscapeFragment = new TaxiChooserLandscapeFragment();
            landscapeFragment.setRetainInstance(false);
            showFragment(landscapeFragment, selectedIndex);
        } else {
            TaxiChooserPortraitFragment portraitFragment = new TaxiChooserPortraitFragment();
            portraitFragment.setRetainInstance(false);
            showFragment(portraitFragment, selectedIndex);
        }
    }

    private void showFragment(TaxiChooserFragment f, int currentSelectedIndex) {

        Bundle args = new Bundle();
        args.putInt(CURRENTLY_SELECTED_INDEX, currentSelectedIndex);
        f.setArguments(args);

        // Add the fragment to the 'fragment_container' FrameLayout
        if(currentlyShownFragment==null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, f)
                    .commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, f)
                    .addToBackStack(null)
                    .commit();
        }

        currentlyShownFragment = f;
    }

    public void logout(View view){

    }

    public void sortByProximity(View view){
        filteredTaxiList = new ArrayList<>(taxiList);

        Geolocation.sortByProximity(filteredTaxiList);


        showFilteredResults(0);
        sideMenu.toggle();
    }

    public void sortByRating(View view){
        filteredTaxiList = new ArrayList<>(taxiList);
        Collections.sort(filteredTaxiList, new Comparator<Taxi>() {
            @Override
            public int compare(Taxi taxi1, Taxi taxi2) {
                double t1 = ((double)taxi1.getRatingAverage());
                double t2 = ((double)taxi2.getRatingAverage());

                return t1 < t2 ? 1 : (t1 > t2 ? -1 : 0);
            }
        });

        showFilteredResults(0);
        sideMenu.toggle();
    }

    public void showFavorites(View view) {
        filteredTaxiList = TravisDB.getTaxiFavorites();

        showFilteredResults(0);
        sideMenu.toggle();
    }

    public static List<Taxi> getCurrentTaxiListState() {
        return Collections.unmodifiableList(filteredTaxiList);
    }
}