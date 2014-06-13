package pt.ua.travis.ui.main;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.google.android.gms.maps.model.LatLng;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import org.apache.commons.io.IOUtils;
import pt.ua.travis.R;
import pt.ua.travis.backend.*;
import pt.ua.travis.core.TravisApplication;
import pt.ua.travis.ui.addresspicker.AddressPickerDialog;
import pt.ua.travis.ui.travel.CurrentTravelFragment;
import pt.ua.travis.ui.navigationdrawer.BlurDrawerItem;
import pt.ua.travis.ui.navigationdrawer.BlurDrawerObject;
import pt.ua.travis.ui.ridelist.RideItem;
import pt.ua.travis.ui.ridelist.RideListFragment;
import pt.ua.travis.ui.riderequest.RideBuilder;
import pt.ua.travis.ui.riderequest.RideRequestTask;
import pt.ua.travis.ui.taxichooser.TaxiChooserFragment;
import pt.ua.travis.ui.taxiinstant.TaxiInstantFragment;
import pt.ua.travis.utils.CommonKeys;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class MainClientActivity extends MainActivity
        implements ActionBar.TabListener,
        TimePickerDialog.OnTimeSetListener {

    private static List<Taxi> nearTaxiList;
    private static List<Taxi> highestRatedTaxiList;
    private static List<Taxi> favoriteTaxiList;
    private static String lastSearchQuery;
    private static int currentlyShownTaxiListIndex = 0;

    private TaxiInstantFragment currentlyShownInstantFragment;
    private TaxiChooserFragment currentlyShownChooserFragment;
    private CurrentTravelFragment currentlyShownTravelFragment;
    private int currentlyShownFragmentIndex;

    private RideBuilder rideBuilder;
    public static final String TIMEPICKER_TAG = "timepicker";
    private final Calendar calendar = Calendar.getInstance();
    private final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
            this,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true,
            true);

    private RideRequestTask rideRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Client thisClient = PersistenceManager.getCurrentlyLoggedInUser();

        PersistenceManager.stopWatchingRides();
        PersistenceManager.startWatchingNewRidesForClient(thisClient, this);

        rideBuilder = new RideBuilder((TravisApplication) getApplication());

        View scheduledRidesTab = getLayoutInflater().inflate(R.layout.tab_with_badge, null);




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
////        tabPager.setPagingEnabled(false);
//        tabPager.setFadeEnabled(false);
//        tabPager.setTransitionEffect(TransitionViewPager.TransitionEffect.ZoomIn);


//        int selectedIndex = 0;
//        Intent intent = getIntent();
//        if (savedInstanceState != null) {
//            selectedIndex = savedInstanceState.getInt(CommonKeys.SELECTED_INDEX, 0);
//        } else if (intent != null && intent.getExtras() != null) {
//            selectedIndex = intent.getIntExtra(CommonKeys.SELECTED_INDEX, 0);
//            if (intent.getIntExtra(CommonKeys.GO_TO_RIDE_LIST, 0) == 1) {
//                goToTab(3);
//                return;
//            }
//        }

        String loggedInString = getString(R.string.logged_in_as_X);
        loggedInString += " " + thisClient.name();
        showTravisNotification(loggedInString, thisClient.imageUri(), NotificationColor.DEFAULT);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        getSupportMenuInflater().inflate(R.menu.actionbar_with_search, menu);

//        configureSearchBar(menu);

//        return super.onPrepareOptionsMenu(menu);
        return true;
    }

    private void configureSearchBar(Menu menu){
        final MenuItem searchItem = null;
        SearchView searchView = (SearchView) searchItem.getActionView();

        // if a search item is collapsed, resets the shown taxis
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {

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
     * @param drawerItems the list that must be populated to translate into
     *                    items or indicators in the drawer navigation menu
     */
    @Override
    protected void fillDrawerNavigation(final List<BlurDrawerObject> drawerItems, final CloseDrawerAction action) {

        BlurDrawerItem item1 = new BlurDrawerItem(this, R.drawable.ic_instant, R.string.instant);
        item1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action.closeDrawer();
                goToTab(0);
            }
        });

        BlurDrawerItem item2 = new BlurDrawerItem(this, R.drawable.ic_map, R.string.map_view);
        item2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action.closeDrawer();
                goToTab(1);
            }
        });

        BlurDrawerItem item3 = new BlurDrawerItem(this, R.drawable.ic_travel, R.string.current_travel);
        item3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action.closeDrawer();
                goToTab(2);
            }
        });

        BlurDrawerItem item4 = new BlurDrawerItem(this, R.drawable.ic_scheduled, R.string.scheduled_rides);
        item4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action.closeDrawer();
                goToTab(3);
            }
        });

        drawerItems.add(item1);
        drawerItems.add(item2);
        drawerItems.add(item3);
        drawerItems.add(item4);
    }


    public RideBuilder getRideBuilder() {
        return rideBuilder;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

//        switch (currentlyShownTaxiListIndex){
//            case 0:
//                break;
//            case 1:
                if (currentlyShownChooserFragment != null) {
                    currentlyShownChooserFragment.attemptCloseSlidingPane(event);
                }
//                break;
//            case 2:
//                if (currentlyShownTravelFragment != null && currentlyShownTravelFragment.slidingPaneIsOpened()) {
                    // DO NOTHING
//                    return true;
//                }
//                break;
//            case 3:
//                break;
//        }

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
                    LatLng currentPos = ((TravisApplication) getApplication()).getCurrentLocation();
                    return PersistenceManager.query()
                            .taxis()
                            .online()
                            .near(currentPos)
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
                    LatLng currentPos = ((TravisApplication) getApplication()).getCurrentLocation();
                    return PersistenceManager.query()
                            .taxis()
                            .online()
                            .near(currentPos)
                            .sortedByRating()
                            .now();
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
        if (favoriteTaxiList == null || forceQuery) {
            final Client c = PersistenceManager.getCurrentlyLoggedInUser();

            new AsyncTask<Void, Void, List<Taxi>>() {
                @Override
                protected List<Taxi> doInBackground(Void... params) {
                    return PersistenceManager.query()
                            .taxis()
                            .online()
                            .favoritedBy(c);
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
                currentlyShownChooserFragment.updateTaxiLocationsOnly(changedObjects);
            }
        });
    }




    // --- Taxi chooser sliding pane operations -----------------------------------------------------------------

    public void onHereAndNowButtonClicked(View view) {
        rideBuilder.resetToHereAndNow((TravisApplication) getApplication());
        Ride newRide = rideBuilder.build();

        // sends a request of the created ride to the associated taxi
        requestRideToTaxi(newRide);
    }


    public void onLaterButtonClicked(View view) {
        rideBuilder.resetToHereAndNow((TravisApplication) getApplication());
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

    public void onTimePickerClicked(View view){
        timePickerDialog.setVibrate(true);
        timePickerDialog.setOnTimeSetListener(this);
        timePickerDialog.show(getSupportFragmentManager(), TIMEPICKER_TAG);
    }


    /**
     * Creates the ride based on the parameters set on the options pane.
     */
    public void onDoneButtonClicked(View view){
        Ride newRide = rideBuilder.build();

        // sends a request of created ride to the taxi
        requestRideToTaxi(newRide);
    }


    public void onCancelButtonClicked(View view){
        currentlyShownChooserFragment.optionsPaneGoToPage(0);
    }


    public void requestRideToTaxi(Ride newRide) {

        rideRequest = new RideRequestTask(MainClientActivity.this, getSupportFragmentManager(), newRide, new RideRequestTask.OnRequestAccepted() {
            @Override
            public void onAccepted(Ride ride) {
                Client thisClient = PersistenceManager.getCurrentlyLoggedInUser();
                PersistenceManager.stopWatchingRides();
                PersistenceManager.startWatchingNewRidesForClient(thisClient, MainClientActivity.this);
                currentlyShownRideListFragment.onRefreshStarted(null);
            }
        });
        rideRequest.execute();

        currentlyShownChooserFragment.closeSlidingPane();
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
    public void logout(View view) {
        super.logout(view);
    }

    public void goToTab(int tabIndex) {
        currentlyShownTaxiListIndex = tabIndex;

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
        currentlyShownFragmentIndex = tab.getPosition();
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        rideBuilder.setScheduledTime(hourOfDay, minute);
        TextView timeTextView = (TextView) findViewById(R.id.time_picker_text);
        timeTextView.setText(hourOfDay + ":" + minute);
    }

    public void setTimeTextToNow(TextView timeTextView) {
        timeTextView.setText(calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
    }

    @Override
    public void onEvent(final Ride responseRide) {
        if(responseRide==null)
            return;

        // TAXI FROM RIDE ARRIVED AT LOCATION!!!
        LatLng latLng = ((TravisApplication) getApplication()).getCurrentLocation();
        responseRide.setOriginLocation(latLng.latitude, latLng.longitude);

        Intent resultIntent = new Intent(this, MainClientActivity.class)
                .putExtra(CommonKeys.RIDE_ARRIVED_ID, responseRide.id())
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PersistenceManager.addToCache(responseRide);

//        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

//        // Because clicking the notification launches a new ("special") activity,
//        // there's no need to create an artificial back stack.
//        final PendingIntent piAccept = PendingIntent.getActivity(this, 0, resultIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//
//        Intent dismissIntent = new Intent(this, MainClientActivity.class);
////                dismissIntent.setAction(CommonConstants.ACTION_DISMISS);
//        final PendingIntent piDecline = PendingIntent.getActivity(this, 0, dismissIntent, 0);

        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_ONE_SHOT);

        final String taxiName = responseRide.taxi().name();
        final String arrivedString = getString(R.string.x_arrived);

        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                try {
                    URL url = new URL(responseRide.taxi().imageUri());

                    //create the new connection
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    //set up the connection
                    urlConnection.setRequestMethod("GET");
//                    urlConnection.setDoOutput(true);
                    urlConnection.connect();

                    //this will be used in reading the data from the internet
                    InputStream inputStream = urlConnection.getInputStream();
                    byte[] array = IOUtils.toByteArray(inputStream);
                    Bitmap originalBitmap = BitmapFactory.decodeByteArray(array, 0, array.length);
                    Resources res = MainClientActivity.this.getResources();
                    int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
                    int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
                    return Bitmap.createScaledBitmap(originalBitmap, width, height, false);

                } catch (IOException ex) {
                    Log.e("NotificationRideArrived", "Error getting image", ex);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);

                String msg = taxiName + " " + getString(R.string.x_arrived_large) + ".";
                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainClientActivity.this)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setAutoCancel(true)
                        .setLargeIcon(bitmap)
                        .setSmallIcon(R.drawable.ic_stat_logo)
                        .setContentTitle(taxiName + " " + arrivedString)
                        .setContentText(msg)
                        .setTicker(taxiName + " " + arrivedString)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg));
//                        .addAction(R.drawable.ic_action_accept,
//                                getString(R.string.accept), piAccept)
//                        .addAction(R.drawable.ic_action_cancel,
//                                getString(R.string.decline), piDecline);




                Notification n = builder.build();
                n.contentIntent = contentIntent;
                n.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_ONGOING_EVENT;

                TravisApplication app = (TravisApplication) getApplication();
                app.startNotificationForRide(n, responseRide);
            }
        }.execute();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Bundle extras = intent.getExtras();
        String rideID;
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Toast.makeText(this, tag.toString(), Toast.LENGTH_LONG).show();

        } else if(extras!=null && (rideID = extras.getString(CommonKeys.RIDE_ARRIVED_ID))!=null) {
            // Received intent from notification of taxi arrival

            Ride r = PersistenceManager.getFromCache(rideID);

            TravisApplication app = (TravisApplication) getApplication();
            app.stopNotificationForRide(r);

            goToTab(2);
            currentlyShownTravelFragment.showAuthentication(r);
        }
    }
}