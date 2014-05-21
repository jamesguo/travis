package pt.ua.travis.ui.main;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Lists;
import pt.ua.travis.backend.*;
import pt.ua.travis.core.TravisLocation;
import pt.ua.travis.ui.drawer.DrawerItem;
import pt.ua.travis.ui.drawer.DrawerSeparator;
import pt.ua.travis.ui.drawer.DrawerUser;
import pt.ua.travis.ui.drawer.DrawerView;
import pt.ua.travis.ui.taxichooser.SlidingLayer;
import pt.ua.travis.R;
import pt.ua.travis.ui.addresspicker.AddressPickerDialog;
import pt.ua.travis.ui.ridelist.RideItem;
import pt.ua.travis.ui.ridelist.RideListFragment;
import pt.ua.travis.ui.taxichooser.TaxiChooserFragment;
import pt.ua.travis.ui.taxichooser.TaxiChooserListFragment;
import pt.ua.travis.ui.taxichooser.TaxiChooserPagerFragment;
import pt.ua.travis.ui.taxiridesetup.*;
import pt.ua.travis.utils.*;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

// TODO: BACK STACK NEEDS WORK!

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class MainClientActivity extends MainActivity {

    private static List<Taxi> queriedTaxiList;
    private static List<Taxi> shownTaxiList;

    private RideBuilder rideBuilder;
    private RideRequestViewPager pager;
    private SlidingLayer slidingLayer;
    private TextView addressTextView;

    private TaxiChooserFragment currentlyShownChooserFragment;

    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: SHOW LOADING BAR

        if(queriedTaxiList ==null) {
//            final LatLng currentLoc = TravisLocation.getCurrentLocation(this);
//            queriedTaxiList = new AsyncTaskLoader<List<Taxi>>(this){
//
//                @Override
//                public List<Taxi> loadInBackground() {
//                    queriedTaxiList = PersistenceManager.query().taxis().near(currentLoc).now();
//                    return queriedTaxiList;
//                }
//
//                @Override
//                public void onContentChanged() {
//                    super.onContentChanged();
//                    currentlyShownChooserFragment.updateTaxis(queriedTaxiList);
//                }
//
//            }.loadInBackground();
            PersistenceManager.query().taxis().later(new Callback<List<Taxi>>() {
                @Override
                public void onResult(List<Taxi> result) {
                    queriedTaxiList = result;
                    shownTaxiList = Lists.newArrayList(result);
                    continueCreateProcess(savedInstanceState);
                }
            });

        } else {
            continueCreateProcess(savedInstanceState);
        }

        rideBuilder = new RideBuilder(this);
    }


    private void continueCreateProcess(Bundle savedInstanceState){
        int selectedIndex = 0;
        Intent intent = getIntent();
        if (savedInstanceState != null) {
            selectedIndex = savedInstanceState.getInt(CommonKeys.SELECTED_INDEX, 0);
        } else if(intent!=null && intent.getExtras()!=null){
            selectedIndex = intent.getIntExtra(CommonKeys.SELECTED_INDEX, 0);
            if(intent.getIntExtra(CommonKeys.GO_TO_RIDE_LIST, 0)==1){
                goToScheduledRidesList(null);
                return;
            }
        }

        pager = (RideRequestViewPager) findViewById(R.id.options_pane);
        pager.setPageTransformer(false, new SlidePageTransformer(pager));
        pager.setAdapter(new RideRequestPagerAdapter(getSupportFragmentManager()));
        pager.setOffscreenPageLimit(1);
        slidingLayer = (SlidingLayer) findViewById(R.id.sliding_layer);
        slidingLayer.setStickTo(SlidingLayer.STICK_TO_BOTTOM);

        showFilteredResults(selectedIndex);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.actionbar_with_search, menu);

        configureSearchBar(menu);

        return super.onPrepareOptionsMenu(menu);
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

                shownTaxiList = Lists.newArrayList();
                String queryLC = query.toLowerCase();

                // looks for matches and adds them to a temporary list
                for (Taxi t : queriedTaxiList) {
                    String nameLC = t.name().toLowerCase();

                    if (nameLC.contains(queryLC))
                        shownTaxiList.add(t);
                }


                // resets the fragments and subsequent adapter to show the filtered list withUser matches
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
//        if(Validate.isLandscape(this)) {
//            TaxiChooserListFragment landscapeFragment = new TaxiChooserListFragment();
//            landscapeFragment.setRetainInstance(false);
//            showTaxiChooserFragment(landscapeFragment, selectedIndex);
//        } else {
            TaxiChooserPagerFragment portraitFragment = new TaxiChooserPagerFragment();
            portraitFragment.setRetainInstance(false);
            showTaxiChooserFragment(portraitFragment, selectedIndex);
//        }
    }


    private void showTaxiChooserFragment(TaxiChooserFragment f, final int currentSelectedIndex) {
        Bundle args = new Bundle();
        args.putInt(CommonKeys.SELECTED_INDEX, currentSelectedIndex);
        f.setArguments(args);

        // Add the fragment to the 'fragment_container' FrameLayout
        if(currentlyShownChooserFragment ==null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.content_frame, f)
                    .addToBackStack(null)
                    .commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, f)
                    .addToBackStack(null)
                    .commit();
        }

        currentlyShownChooserFragment = f;
        PersistenceManager.stopWatchingTaxis();
        PersistenceManager.watchTaxis(shownTaxiList, new WatchEvent<List<Taxi>>() {
            @Override
            public void onEvent(List<Taxi> changedObjects) {
                currentlyShownChooserFragment.updateTaxis(changedObjects);
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
        int numOfFavorites = loggedInClient.numberOfFavorites();


        drawerViews.add(new DrawerUser(loggedInClient));

        final DrawerItem item = new DrawerItem(1, R.string.menu_rides, R.drawable.ic_action_alarms, 0);
        drawerViews.add(item);

        drawerViews.add(new DrawerItem(2, R.string.menu_logout, R.drawable.ic_action_about));
        drawerViews.add(new DrawerSeparator());
        drawerViews.add(new DrawerItem(3, R.string.menu_closest, R.drawable.ic_action_map));
        drawerViews.add(new DrawerItem(4, R.string.menu_best_rated, R.drawable.ic_action_not_important));
        drawerViews.add(new DrawerItem(5, R.string.menu_favorites, R.drawable.ic_action_favorite, numOfFavorites));
        drawerViews.add(new DrawerItem(6, R.string.menu_settings, R.drawable.ic_action_settings));

        PersistenceManager.query().rides().withUser(loggedInClient).scheduled().later(new Callback<List<Ride>>() {
            @Override
            public void onResult(List<Ride> result) {
                item.setItemCounter(result.size());
                updateDrawerList();
            }
        });
    }


    @Override
    protected void onDrawerItemClick(int itemID) {
        switch (itemID){
            case 1: goToScheduledRidesList(null); break;
            case 2: logout(null); break;
            case 3: sortByProximity(null); break;
            case 4: sortByRating(null); break;
            case 5: showFavorites(null); break;
            default: break;
        }

        super.onDrawerItemClick(itemID);
    }


    public void goToScheduledRidesList(View view){
        final Client loggedInClient = PersistenceManager.query().clients().loggedInThisDevice();

        PersistenceManager.query().rides().withUser(loggedInClient).scheduled().later(new Callback<List<Ride>>() {
            @Override
            public void onResult(List<Ride> result) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content_frame, RideListFragment.newInstance(RideItem.SHOW_TAXI, result))
                        .addToBackStack(null)
                        .commit();
            }
        });

    }


    public void sortByProximity(View view){
        shownTaxiList = Lists.newArrayList(queriedTaxiList);

        Utils.sortByProximity(shownTaxiList);

        showFilteredResults(0);
    }


    public void sortByRating(View view){
        PersistenceManager.query().taxis().sortedByRating().limitNumberOfResultsTo(10).later(new Callback<List<Taxi>>() {
            @Override
            public void onResult(List<Taxi> result) {
                queriedTaxiList = result;
                shownTaxiList = Lists.newArrayList(queriedTaxiList);
                showFilteredResults(0);
            }
        });

//        Collections.sort(shownTaxiList, new Comparator<Taxi>() {
//            @Override
//            public int compare(Taxi taxi1, Taxi taxi2) {
//                double t1 = ((double) taxi1.getRatingAverage());
//                double t2 = ((double) taxi2.getRatingAverage());
//
//                return t1 < t2 ? 1 : (t1 > t2 ? -1 : 0);
//            }
//        });

    }


    public void showFavorites(View view) {
        Client loggedInClient = PersistenceManager.query().clients().loggedInThisDevice();

        PersistenceManager.query().taxis().favoritedBy(loggedInClient, new Callback<List<Taxi>>() {
            @Override
            public void onResult(List<Taxi> result) {
                shownTaxiList = result;
                showFilteredResults(0);
            }
        });
    }


    // ----------------------------------------------
    // ------------ OPTIONS PANE METHODS ------------
    // ----------------------------------------------

    public void showOptionsPane(Taxi selectedTaxi){
        rideBuilder.setTaxi(selectedTaxi);
        pager.setCurrentItem(0, true);

        BootstrapButton hereAndNowButton = (BootstrapButton) findViewById(R.id.btHereAndNow);
        hereAndNowButton.setEnabled(selectedTaxi.isAvailable());
        hereAndNowButton.setClickable(selectedTaxi.isAvailable());

        slidingLayer.openLayer(true);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        if (slidingLayer.isOpened()){

            int location[] = { 0, 0 };
            slidingLayer.getLocationOnScreen(location);

            if(((int)event.getRawY()) < location[1]) {
                slidingLayer.closeLayer(true);
            }
        }

        return super.dispatchTouchEvent(event);
    }


    public void onHereAndNowButtonClicked(View view){
        rideBuilder.resetToHereAndNow(this);
        Ride newRide = rideBuilder.build();

        // sends a request of the created ride to the associated taxi
        requestRideToTaxi(newRide);
    }


    public void onLaterButtonClicked(View view) {
        LatLng pos = TravisLocation.getCurrentLocation(this);
        Address currentAddress = Utils.addressesFromLocation(this, pos.latitude, pos.longitude).get(0);

        rideBuilder.resetToHereAndNow(this);
        pager.setCurrentItem(1, true);
        addressTextView = (TextView) findViewById(R.id.origin_address);
        addressTextView.setText(Utils.addressToString(currentAddress));

        TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minute);
    }


    public void onOriginButtonClicked(View view){

        AddressPickerDialog.newInstance(this, new AddressPickerDialog.OnDoneButtonClickListener() {
            @Override
            public void onClick(LatLng pickedPosition, String addressText) {
                rideBuilder.setOrigin(pickedPosition);
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
        pager.setCurrentItem(0, true);
    }


    private void requestRideToTaxi(Ride newRide){

        new RideRequestTask(MainClientActivity.this, newRide, new RideRequestTask.OnTaskFinished() {
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
        }).execute();

    }


    public static List<Taxi> getCurrentTaxiListState() {
        return Collections.unmodifiableList(shownTaxiList);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(currentlyShownChooserFragment);
        ft.commit();

        outState.putInt(CommonKeys.SELECTED_INDEX, currentlyShownChooserFragment.getCurrentSelectedIndex());

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