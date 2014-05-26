package pt.ua.travis.ui.main;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.google.android.gms.maps.model.LatLng;
import pt.ua.travis.backend.*;
import pt.ua.travis.core.TravisLocation;
import pt.ua.travis.ui.currenttravel.CurrentTravelFragment;
import pt.ua.travis.ui.customviews.TransitionViewPager;
import pt.ua.travis.ui.navigationdrawer.DrawerItem;
import pt.ua.travis.ui.navigationdrawer.DrawerSeparator;
import pt.ua.travis.ui.navigationdrawer.DrawerUser;
import pt.ua.travis.ui.navigationdrawer.DrawerView;
import pt.ua.travis.R;
import pt.ua.travis.ui.addresspicker.AddressPickerDialog;
import pt.ua.travis.ui.ridelist.RideItem;
import pt.ua.travis.ui.ridelist.RideListFragment;
import pt.ua.travis.ui.taxichooser.TaxiChooserFragment;
import pt.ua.travis.ui.riderequest.*;
import pt.ua.travis.ui.taxiinstant.TaxiInstantFragment;
import pt.ua.travis.utils.*;

import java.util.List;

// TODO: BACK STACK NEEDS WORK!

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class MainClientActivity extends MainActivity implements ActionBar.TabListener {

    private static List<Taxi> nearTaxiList;
    private static List<Taxi> highestRatedTaxiList;
    private static List<Taxi> favoriteTaxiList;
    private static String lastSearchQuery;
    private static int currentlyShownTaxiListIndex = 0;

    private TaxiInstantFragment currentlyShownInstantFragment;
    private TaxiChooserFragment currentlyShownChooserFragment;
    private CurrentTravelFragment currentlyShownTravelFragment;
    private RideListFragment currentlyShownRideListFragment;
    private int currentlyShownFragmentIndex;

    private RideBuilder rideBuilder;
    private RideRequestTask rideRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rideBuilder = new RideBuilder(this);

        View scheduledRidesTab = getLayoutInflater().inflate(R.layout.tab_with_badge, null);
        TextView badge = (TextView) scheduledRidesTab.findViewById(R.id.badge);
        badge.setText("0");


        ActionBar bar = getSupportActionBar();
//        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.addTab(bar.newTab()
                .setIcon(R.drawable.ic_instant)
                .setTabListener(this));
        bar.addTab(bar.newTab()
                .setIcon(R.drawable.ic_map)
                .setTabListener(this));
        bar.addTab(bar.newTab()
                .setIcon(R.drawable.ic_travel)
                .setTabListener(this));
        bar.addTab(bar.newTab()
                .setIcon(R.drawable.ic_scheduled)
                .setCustomView(scheduledRidesTab)
                .setTabListener(this));

        currentlyShownInstantFragment = new TaxiInstantFragment();
        currentlyShownChooserFragment = new TaxiChooserFragment();
        currentlyShownTravelFragment = new CurrentTravelFragment();
        currentlyShownRideListFragment = RideListFragment.newInstance(RideItem.SHOW_TAXI);
        tabPager.setAdapter(new TabPagerAdapter() {

            @Override
            public Fragment getFragment(int position) {
                switch (position) {
                    case 0:
                        return currentlyShownInstantFragment;
                    case 1:
                        return currentlyShownChooserFragment;
                    case 2:
                        return currentlyShownTravelFragment;
                    case 3:
                        return currentlyShownRideListFragment;
                }
                return null;
            }

            @Override
            public int getCount() {
                return 4;
            }
        });
        tabPager.setOffscreenPageLimit(4);
        tabPager.setPagingEnabled(false);
        tabPager.setTransitionEffect(TransitionViewPager.TransitionEffect.ZoomIn);


        int selectedIndex = 0;
        Intent intent = getIntent();
        if (savedInstanceState != null) {
            selectedIndex = savedInstanceState.getInt(CommonKeys.SELECTED_INDEX, 0);
        } else if (intent != null && intent.getExtras() != null) {
            selectedIndex = intent.getIntExtra(CommonKeys.SELECTED_INDEX, 0);
            if (intent.getIntExtra(CommonKeys.GO_TO_RIDE_LIST, 0) == 1) {
                goToTab(3);
                return;
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        getSupportMenuInflater().inflate(R.menu.actionbar_with_search, menu);

//        configureSearchBar(menu);

//        return super.onPrepareOptionsMenu(menu);
        return true;
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
                getCurrentlyShownTaxiList(false, new Callback<List<Taxi>>() {
                    @Override
                    public void onResult(List<Taxi> result) {
                        // TODO
                    }
                });
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
            public boolean onQueryTextSubmit(String searchText) {
                if (searchText.isEmpty())
                    return false;

                PersistenceManager.query().taxis().withName(searchText.toLowerCase()).later(new Callback<List<Taxi>>() {
                    @Override
                    public void onResult(List<Taxi> result) {
                        // TODO!

                    }
                });

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    /**
     * Populates the drawer navigation menu.
     *
     * @param drawerViews the list that must be populated to translate into
     *                    items or indicators in the drawer navigation menu
     */
    @Override
    protected void fillDrawerNavigation(final List<DrawerView> drawerViews) {
        final Client loggedInClient = PersistenceManager.query().clients().loggedInThisDevice();

        drawerViews.add(new DrawerUser(loggedInClient));
        drawerViews.add(new DrawerItem(1, R.string.menu_logout, R.drawable.ic_logout));
        drawerViews.add(new DrawerSeparator());
        drawerViews.add(new DrawerItem(2, R.string.menu_settings, R.drawable.ic_settings));
    }


    @Override
    protected void onDrawerItemClick(int itemID) {
        switch (itemID){
            case 1: logout(null); break;
            case 2: // TODO: SETTINGS
                break;
            default: break;
        }

        super.onDrawerItemClick(itemID);
    }


    public RideBuilder getRideBuilder() {
        return rideBuilder;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        if(currentlyShownChooserFragment!=null){
            currentlyShownChooserFragment.attemptCloseSlidingPane(event);
        }

        return super.dispatchTouchEvent(event);
    }


    // --- Taxi list access -------------------------------------------------------------------------------------

    public void getCurrentlyShownTaxiList(boolean forceQuery, Callback<List<Taxi>> callback) {
        switch (currentlyShownTaxiListIndex){
            case 0: getNearTaxiList(forceQuery, callback); break;
            case 1: getHighestRatedTaxiList(forceQuery, callback); break;
            case 2: getFavoriteTaxiList(forceQuery, callback); break;
        }
    }

    public void getNearTaxiList(boolean forceQuery, final Callback<List<Taxi>> callback){
        if(nearTaxiList == null || forceQuery){

            new AsyncTask<Void, Void, List<Taxi>>(){
                @Override
                protected List<Taxi> doInBackground(Void... params) {
                    return PersistenceManager.query()
                            .taxis()
                            .near(TravisLocation.getCurrentLocation(MainClientActivity.this))
                            .limitNumberOfResultsTo(10)
                            .now();
                }

                @Override
                protected void onPostExecute(List<Taxi> result) {
                    super.onPostExecute(result);
                    nearTaxiList = result;
                    currentlyShownTaxiListIndex = 0;
                    finalizeTaxiListRetrieval(nearTaxiList, callback);
                }
            }.execute();

        } else {
            currentlyShownTaxiListIndex = 0;
            finalizeTaxiListRetrieval(nearTaxiList, callback);
        }
    }

    public void getHighestRatedTaxiList(boolean forceQuery, final Callback<List<Taxi>> callback){
        if(highestRatedTaxiList == null || forceQuery){

            new AsyncTask<Void, Void, List<Taxi>>(){
                @Override
                protected List<Taxi> doInBackground(Void... params) {
                    return PersistenceManager.query().taxis().sortedByRating().now();
                }

                @Override
                protected void onPostExecute(List<Taxi> result) {
                    super.onPostExecute(result);
                    highestRatedTaxiList = result;
                    currentlyShownTaxiListIndex = 1;
                    finalizeTaxiListRetrieval(highestRatedTaxiList, callback);
                }
            }.execute();
        } else {
            currentlyShownTaxiListIndex = 1;
            finalizeTaxiListRetrieval(highestRatedTaxiList, callback);
        }
    }

    public void getFavoriteTaxiList(boolean forceQuery, final Callback<List<Taxi>> callback) {
        if (favoriteTaxiList == null || forceQuery){
            final Client c = PersistenceManager.query().clients().loggedInThisDevice();

            new AsyncTask<Void, Void, List<Taxi>>(){
                @Override
                protected List<Taxi> doInBackground(Void... params) {
                    return PersistenceManager.query().taxis().favoritedBy(c);
                }

                @Override
                protected void onPostExecute(List<Taxi> result) {
                    super.onPostExecute(result);
                    favoriteTaxiList = result;
                    currentlyShownTaxiListIndex = 2;
                    finalizeTaxiListRetrieval(favoriteTaxiList, callback);
                }
            }.execute();

        } else {
            currentlyShownTaxiListIndex = 2;
            finalizeTaxiListRetrieval(favoriteTaxiList, callback);
        }
    }

    private void finalizeTaxiListRetrieval(List<Taxi> taxiList, final Callback<List<Taxi>> callback) {
        if (callback != null) {
            callback.onResult(taxiList);
        }
        PersistenceManager.stopWatchingTaxis();
        PersistenceManager.watchTaxis(taxiList, new WatchEvent<List<Taxi>>() {
            @Override
            public void onEvent(List<Taxi> changedObjects) {
                currentlyShownChooserFragment.updateTaxiLocations(changedObjects);
            }
        });
    }




    // --- Taxi chooser sliding pane operations -----------------------------------------------------------------

    public void onHereAndNowButtonClicked(View view){
        rideBuilder.resetToHereAndNow(this);
        Ride newRide = rideBuilder.build();

        // sends a request of the created ride to the associated taxi
        requestRideToTaxi(newRide);
    }


    public void onLaterButtonClicked(View view) {
        rideBuilder.resetToHereAndNow(this);
        currentlyShownChooserFragment.optionsPaneGoToPage(1);
    }


    public void onOriginButtonClicked(View view){
        AddressPickerDialog.newInstance(this, new AddressPickerDialog.OnDoneButtonClickListener() {
            @Override
            public void onClick(LatLng pickedPosition, String addressText) {
                rideBuilder.setOrigin(pickedPosition);
                TextView addressTextView = (TextView) findViewById(R.id.origin_address);
                addressTextView.setText(addressText);

            }
        }).show(getSupportFragmentManager(), "OriginAddressPickerDialog");
    }


    /**
     * Creates the ride based on the parameters set on the options pane.
     */
    public void onDoneButtonClicked(View view){
        TimePicker tp = (TimePicker) findViewById(R.id.timePicker);
        rideBuilder.setScheduledTime(tp.getCurrentHour(), tp.getCurrentMinute());
        Ride newRide = rideBuilder.build();

        // sends a request of created ride to the taxi
        requestRideToTaxi(newRide);
    }


    public void onCancelButtonClicked(View view){
        currentlyShownChooserFragment.optionsPaneGoToPage(0);
    }


    private void requestRideToTaxi(Ride newRide) {

        rideRequest = new RideRequestTask(MainClientActivity.this, getSupportFragmentManager(), newRide, new RideRequestTask.OnTaskFinished() {
            @Override
            public void onFinished(String result, Ride ride) {
                if (result.equals(RideRequestTask.RESPONSE_ACCEPTED)) {

                    PersistenceManager.stopWatchingTaxis();
                    Intent intent = new Intent(
                            MainClientActivity.this,
                            WaitForTaxiActivity.class);
                    intent.putExtra(CommonKeys.SCHEDULED_RIDE_ID, ride.id());
                    PersistenceManager.addToCache(ride);
                    startActivity(intent);

                } else if (result.equals(RideRequestTask.RESPONSE_REFUSED)) {
                    // THE TAXI DENIED THE REQUEST


                } else if (result.equals(RideRequestTask.RESPONSE_TIMEOUT)) {
                    // THE TAXI DID NOT RESPOND TO THE REQUEST


                }
            }
        });
        rideRequest.execute();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.remove(currentlyShownChooserFragment);
//        ft.commit();
//
//        outState.putInt(CommonKeys.SELECTED_INDEX, currentlyShownChooserFragment.getCurrentSelectedIndex());

        super.onSaveInstanceState(outState);
    }


    @Override
    public void onDeletedRide(){
        goToTab(3);
    }


    @Override
    public void logout(View view) {
        super.logout(view);
    }

    public void goToTab(int tabIndex) {
        Fragment fragmentToShow;

        ActionBar bar = getSupportActionBar();
        bar.selectTab(bar.getTabAt(tabIndex));

//        switch (tabIndex){
//            case 0:
//                if(currentlyShownInstantFragment ==null) {
//                    currentlyShownInstantFragment = new TaxiInstantFragment();
//                }
//                fragmentToShow = currentlyShownInstantFragment;
//                break;
//            case 1:
//                if(currentlyShownChooserFragment==null) {
//                    currentlyShownChooserFragment = new TaxiChooserFragment();
//                }
//                fragmentToShow = currentlyShownChooserFragment;
//                break;
//            case 2:
//                if(currentlyShownTravelFragment==null) {
//                    currentlyShownTravelFragment = new CurrentTravelFragment();
//                }
//                fragmentToShow = currentlyShownTravelFragment;
//                break;
//            case 3:
//                if(currentlyShownRideListFragment==null) {
//                    getRideList(false, new Callback<List<Ride>>() {
//                        @Override
//                        public void onResult(List<Ride> result) {
//                            currentlyShownFragmentIndex = 3;
//                            currentlyShownRideListFragment = RideListFragment.newInstance(RideItem.SHOW_TAXI, result);
//                            getSupportFragmentManager()
//                                    .beginTransaction()
//                                    .replace(R.id.content_frame, currentlyShownRideListFragment)
//                                    .addToBackStack(null)
//                                    .commit();
//                        }
//                    });
//                    return;
//                }
//                fragmentToShow = currentlyShownRideListFragment;
//                break;
//            default:
//                return;
//        }
//
//        currentlyShownFragmentIndex = tabIndex;
//
//
//        // Add the fragment to the 'fragment_container' FrameLayout
////        if (firstTime) {
////            getSupportFragmentManager()
////                    .beginTransaction()
////                    .add(R.id.content_frame, fragmentToShow)
////                    .addToBackStack(null)
////                    .commit();
////        } else {
//
////        Bundle args = new Bundle();
////        args.putInt(CommonKeys.SELECTED_INDEX, );
//
//        getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.content_frame, fragmentToShow)
////                .addToBackStack(null)
//                .commit();
////        }
////        fragmentTransaction.replace(R.id.content_frame, fragmentToShow).commit();
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        tabPager.setCurrentItem(tab.getPosition(), true);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }
}