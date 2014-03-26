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
import com.squareup.picasso.Picasso;
import pt.ua.travis.R;
import pt.ua.travis.core.Client;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.db.Geolocation;
import pt.ua.travis.db.PersistenceManager;
import pt.ua.travis.gui.login.LoginActivity;
import pt.ua.travis.gui.ridelist.RideDeletedListener;
import pt.ua.travis.gui.ridelist.RideItem;
import pt.ua.travis.gui.ridelist.RideListFragment;
import pt.ua.travis.gui.taxichooser.TaxiChooserFragment;
import pt.ua.travis.gui.taxichooser.TaxiChooserListFragment;
import pt.ua.travis.gui.taxichooser.TaxiChooserPagerFragment;
import pt.ua.travis.utils.CommonResources;
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
public class MainClientActivity extends MainActivity {

    private SlideHolder sideMenu;
    private static List<Taxi> taxiList;
    private static List<Taxi> filteredTaxiList;

    private TaxiChooserFragment currentlyShownChooserFragment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_client_activity);

        CommonResources.initialize(this);

        if(taxiList==null) {
            taxiList = PersistenceManager.selectAllTaxis();
            filteredTaxiList = new ArrayList<>(taxiList);
            Geolocation.sortByProximity(filteredTaxiList);
        }

        int selectedIndex = 0;
        Intent intent = getIntent();
        if (savedInstanceState != null) {
            selectedIndex = savedInstanceState.getInt(Keys.SELECTED_INDEX, 0);
        } else if(intent!=null && intent.getExtras()!=null){
            selectedIndex = intent.getIntExtra(Keys.SELECTED_INDEX, 0);
            if(intent.getIntExtra(Keys.GO_TO_RIDE_LIST, 0)==1){
                goToScheduledRidesList(null);
                return;
            }
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
        Client thisClient = PersistenceManager.selectThisClientAccount();
        int numOfRides = PersistenceManager.selectRidesFromClient().size();

        String imageUrl = thisClient.imageUrl;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            ImageView photoView = (ImageView) findViewById(R.id.photo);
            Picasso.with(this).load(imageUrl).into(photoView);
        }

        TextView nameView = (TextView) findViewById(R.id.name);
        nameView.setText(thisClient.realName);

        TextView ridesCounter = (TextView) findViewById(R.id.rides_counter);
        ridesCounter.setText("" + numOfRides);

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
                    String nameLC = t.realName.toLowerCase();

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

    private void showFilteredResults(int selectedIndex){
        if(Validate.isLandscape(this)) {
            TaxiChooserListFragment landscapeFragment = new TaxiChooserListFragment();
            landscapeFragment.setRetainInstance(false);
            showTaxiChooserFragment(landscapeFragment, selectedIndex);
        } else {
            TaxiChooserPagerFragment portraitFragment = new TaxiChooserPagerFragment();
            portraitFragment.setRetainInstance(false);
            showTaxiChooserFragment(portraitFragment, selectedIndex);
        }
    }

    private void showTaxiChooserFragment(TaxiChooserFragment f, int currentSelectedIndex) {

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
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, RideListFragment.newInstance(RideItem.SHOW_TAXI, PersistenceManager.selectRidesFromClient()))
                .addToBackStack(null)
                .commit();

        if(sideMenu!=null)
            sideMenu.close();
    }

    public void sortByProximity(View view){
        filteredTaxiList = new ArrayList<>(taxiList);

        Geolocation.sortByProximity(filteredTaxiList);


        showFilteredResults(0);
        sideMenu.close();
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
        sideMenu.close();
    }

    public void showFavorites(View view) {
        filteredTaxiList = PersistenceManager.getFavoritesFromClient();

        showFilteredResults(0);
        sideMenu.close();
    }

    public static List<Taxi> getCurrentTaxiListState() {
        return Collections.unmodifiableList(filteredTaxiList);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(currentlyShownChooserFragment);
        ft.commit();

        outState.putInt(Keys.SELECTED_INDEX, currentlyShownChooserFragment.getCurrentSelectedIndex());

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDeletedRide(){
        goToScheduledRidesList(null);
    }

    @Override
    public void logout(View view) {
        super.logout(view);
    }
}