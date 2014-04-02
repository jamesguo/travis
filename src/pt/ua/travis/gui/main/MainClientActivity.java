package pt.ua.travis.gui.main;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.agimind.widget.SlideHolder;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.maps.model.LatLng;
import com.slidinglayer.SlidingLayer;
import com.squareup.picasso.Picasso;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import pt.ua.travis.R;
import pt.ua.travis.core.Client;
import pt.ua.travis.core.Ride;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.db.Geolocation;
import pt.ua.travis.db.PersistenceManager;
import pt.ua.travis.gui.addresspicker.AddressPickerDialog;
import pt.ua.travis.gui.ridelist.RideItem;
import pt.ua.travis.gui.ridelist.RideListFragment;
import pt.ua.travis.gui.taxichooser.TaxiChooserFragment;
import pt.ua.travis.gui.taxichooser.TaxiChooserListFragment;
import pt.ua.travis.gui.taxichooser.TaxiChooserPagerFragment;
import pt.ua.travis.gui.taxiridesetup.*;
import pt.ua.travis.utils.*;

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

    private RideBuilder rideBuilder;
    private RideRequestViewPager pager;
    private SlidingLayer slidingLayer;
    private TextView addressTextView;

    private TaxiChooserFragment currentlyShownChooserFragment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_client_activity);
        CommonRes.initialize(this);
        rideBuilder = new RideBuilder(this);

        if(taxiList==null) {
            taxiList = PersistenceManager.selectAllTaxis();
            filteredTaxiList = new ArrayList<Taxi>(taxiList);
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

        pager = (RideRequestViewPager) findViewById(R.id.options_pane);
        pager.setPageTransformer(false, new SlidePageTransformer(pager));
        pager.setAdapter(new RideRequestPagerAdapter(getSupportFragmentManager()));
        pager.setOffscreenPageLimit(1);
        slidingLayer = (SlidingLayer) findViewById(R.id.sliding_layer);
        slidingLayer.setStickTo(SlidingLayer.STICK_TO_BOTTOM);

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

        String imageUrl = thisClient.imageUri;
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

                filteredTaxiList = new ArrayList<Taxi>();
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
        filteredTaxiList = new ArrayList<Taxi>(taxiList);

        Geolocation.sortByProximity(filteredTaxiList);


        showFilteredResults(0);
        sideMenu.close();
    }

    public void sortByRating(View view){
        filteredTaxiList = new ArrayList<Taxi>(taxiList);
        Collections.sort(filteredTaxiList, new Comparator<Taxi>() {
            @Override
            public int compare(Taxi taxi1, Taxi taxi2) {
                double t1 = ((double) taxi1.getRatingAverage());
                double t2 = ((double) taxi2.getRatingAverage());

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

    // ----------------------------------------------
    // ------------ OPTIONS PANE METHODS ------------
    // ----------------------------------------------

    public void showOptionsPane(Taxi selectedTaxi){
        rideBuilder.setTaxi(selectedTaxi);
        pager.setCurrentItem(0, true);

        BootstrapButton hereAndNowButton = (BootstrapButton) findViewById(R.id.btHereAndNow);
        hereAndNowButton.setEnabled(selectedTaxi.isAvailable);
        hereAndNowButton.setClickable(selectedTaxi.isAvailable);

        slidingLayer.openLayer(true);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (slidingLayer.isOpened() && event.getRawY() < slidingLayer.getHeight()) {
            slidingLayer.closeLayer(true);
        }

        return super.dispatchTouchEvent(event);
    }

    public void onHereAndNowButtonClicked(View view){
        rideBuilder.resetToHereAndNow();
        Ride newRide = rideBuilder.build();

        // sends a request of created ride to the taxi
        requestRideToTaxi(newRide);
    }

    public void onLaterButtonClicked(View view){
        Location currentLocation = Tools.getCurrentLocation(this);
        String currentAddress = Tools.latlngToAddressString(this,
                currentLocation.getLatitude(), currentLocation.getLongitude());

        rideBuilder.resetToHereAndNow();
        pager.setCurrentItem(1, true);
        addressTextView = (TextView) findViewById(R.id.origin_address);
        addressTextView.setText(currentAddress);

        TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        LocalTime now = LocalTime.now();
        timePicker.setCurrentHour(now.getHourOfDay());
        timePicker.setCurrentMinute(now.minuteOfHour().get());
    }

    public void onOriginButtonClicked(View view){
//        Intent intent = new Intent(context, AddressPickerDialog.class);
//        startActivityForResult(intent, Keys.REQUEST_ORIGIN_COORDS);

        AddressPickerDialog.newInstance(this, new AddressPickerDialog.OnDoneButtonClickListener() {
            @Override
            public void onClick(LatLng pickedPosition, String addressText) {
                rideBuilder.setOrigin(pickedPosition.latitude, pickedPosition.longitude, addressText);
                addressTextView.setText(addressText);

            }
        }).show(getSupportFragmentManager(), "AddressPickerDialog");
    }

    /**
     * Creates the ride based on the parameters set on the options pane.
     */
    public void onDoneButtonClicked(View view){
        TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
        rideBuilder.setScheduledTime(new LocalTime(DateTimeZone.forID("GMT"))
                .withHourOfDay(timePicker.getCurrentHour())
                .withMinuteOfHour(timePicker.getCurrentMinute()));
        Ride newRide = rideBuilder.build();

        // sends a request of created ride to the taxi
        requestRideToTaxi(newRide);
    }

    public void onCancelButtonClicked(View view){
        pager.setCurrentItem(0, true);
    }

    private void requestRideToTaxi(final Ride newRide){

        new RideRequestTask(MainClientActivity.this, newRide, new Returner() {
            @Override
            public void onResult(int result) {
                if (result == RideRequestTask.OK_RESULT) {
                    PersistenceManager.addRide(newRide);

                    Intent intent = new Intent(
                            MainClientActivity.this,
                            WaitForTaxiActivity.class);
                    intent.putExtra(Keys.SCHEDULED_RIDE, newRide);
                    startActivity(intent);
                } else if(result == RideRequestTask.CANCEL_RESULT) {
                    // TODO THE TAXI DENIED THE REQUEST

                }
            }
        }).execute();
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