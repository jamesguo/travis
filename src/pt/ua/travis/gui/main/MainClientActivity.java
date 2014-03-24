package pt.ua.travis.gui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.agimind.widget.SlideHolder;
import com.nostra13.universalimageloader.core.ImageLoader;
import pt.ua.travis.R;
import pt.ua.travis.core.Client;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.db.Geolocation;
import pt.ua.travis.db.PersistenceManager;
import pt.ua.travis.gui.ridelist.RideListActivity;
import pt.ua.travis.gui.taxichooser.TaxiChooserFragment;
import pt.ua.travis.gui.taxichooser.TaxiChooserListFragment;
import pt.ua.travis.gui.taxichooser.TaxiChooserPagerFragment;
import pt.ua.travis.utils.Keys;
import pt.ua.travis.utils.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// TODO: BACK STACK NEEDS WORK!

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class MainClientActivity extends SherlockFragmentActivity {

    private SlideHolder sideMenu;
    private static List<Taxi> taxiList;
    private static List<Taxi> filteredTaxiList;

    private TaxiChooserFragment currentlyShownChooserFragment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_main_activity);

        if(taxiList==null) {
            taxiList = PersistenceManager.getTaxisFromDB();
            filteredTaxiList = new ArrayList<>(taxiList);
            Geolocation.sortByProximity(filteredTaxiList);
        }

        int selectedIndex = 0;
        Intent intent = getIntent();
        if (savedInstanceState != null) {
            selectedIndex = savedInstanceState.getInt(Keys.SELECTED_INDEX, 0);
        } else if(intent!=null && intent.getExtras()!=null){
            selectedIndex = intent.getExtras().getInt(Keys.SELECTED_INDEX, 0);
        }

        showFilteredResults(selectedIndex);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ActionBar bar = getSupportActionBar();
        bar.setDisplayShowTitleEnabled(false);
        getSupportMenuInflater().inflate(R.menu.actionbar_with_search, menu);

        configureSideMenu(menu);

        configureSearchBar(menu);

        return super.onPrepareOptionsMenu(menu);
    }



    private void configureSideMenu(Menu menu){
        sideMenu = (SlideHolder) findViewById(R.id.sideMenu);
        sideMenu.setDirection(SlideHolder.DIRECTION_LEFT);
        sideMenu.setAllowInterceptTouch(false);
        sideMenu.setEnabled(false);
        if(Validate.isTablet(this) && Validate.isLandscape(this)){
            sideMenu.setAlwaysOpened(true);
        } else {
            sideMenu.setOnSlideListener(new SlideHolder.OnSlideListener() {
                @Override
                public void onSlideCompleted(boolean b) {
                    sideMenu.setEnabled(b);
                }
            });
        }

        MenuItem toggleItem = menu.findItem(R.id.action_side_menu_toggle);
        if (toggleItem != null) {
            final int toggleButtonID = toggleItem.getItemId();
            toggleItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId() == toggleButtonID)
                        sideMenu.toggle();
                    return true;
                }
            });
        }

        // load client specific data in the side menu
        Client thisClient = PersistenceManager.getClientAccount();

        String imageUrl = thisClient.imageUrl;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            ImageView photoView = (ImageView) findViewById(R.id.photo);
            ImageLoader loader = Keys.getLoader(this);
            loader.displayImage(imageUrl, photoView);
        }

        TextView nameView = (TextView) findViewById(R.id.name);
        nameView.setText(thisClient.name);

        TextView favoritesCounter = (TextView) findViewById(R.id.favorites_counter);
        favoritesCounter.setText("" + thisClient.favorites.size());
    }



    private void configureSearchBar(Menu menu){

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();


        // if a search item is collapsed, resets the shown taxis
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                sortByProximity(null);
                return true;
            }
        });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                searchItem.collapseActionView();
            }
        });

        // sets a listener to filter results when the user submits a query in the search box
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.isEmpty())
                    return false;

                filteredTaxiList = new ArrayList<>();
                String queryLC = query.toLowerCase();

                // looks for matches and adds them to a temporary list
                for (Taxi t : taxiList) {
                    String nameLC = t.name.toLowerCase();

                    if (nameLC.contains(queryLC))
                        filteredTaxiList.add(t);
                }


                // resets the fragments and subsequent adapter to show the filtered list with matches
                showFilteredResults(0);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(currentlyShownChooserFragment);
        ft.commit();

        outState.putInt(Keys.SELECTED_INDEX, currentlyShownChooserFragment.getCurrentSelectedIndex());

        super.onSaveInstanceState(outState);
    }

    private void showFilteredResults(int selectedIndex){
        if(Validate.isLandscape(this)) {
            TaxiChooserListFragment landscapeFragment = new TaxiChooserListFragment();
            landscapeFragment.setRetainInstance(false);
            showFragment(landscapeFragment, selectedIndex);
        } else {
            TaxiChooserPagerFragment portraitFragment = new TaxiChooserPagerFragment();
            portraitFragment.setRetainInstance(false);
            showFragment(portraitFragment, selectedIndex);
        }
    }

    private void showFragment(TaxiChooserFragment f, int currentSelectedIndex) {

        Bundle args = new Bundle();
        args.putInt(Keys.SELECTED_INDEX, currentSelectedIndex);
        f.setArguments(args);

        // Add the fragment to the 'fragment_container' FrameLayout
        if(currentlyShownChooserFragment ==null) {
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

        currentlyShownChooserFragment = f;
    }

    public void goToScheduledRidesList(View view){
        Intent intent = new Intent(this, RideListActivity.class);
        startActivity(intent);
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
        filteredTaxiList = PersistenceManager.getTaxiFavorites();

        showFilteredResults(0);
        sideMenu.toggle();
    }

    public static List<Taxi> getCurrentTaxiListState() {
        return Collections.unmodifiableList(filteredTaxiList);
    }
}