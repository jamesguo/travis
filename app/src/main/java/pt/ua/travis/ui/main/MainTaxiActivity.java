package pt.ua.travis.ui.main;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.google.android.gms.maps.model.LatLng;
import org.apache.commons.io.IOUtils;
import pt.ua.travis.R;
import pt.ua.travis.backend.*;
import pt.ua.travis.core.TravisApplication;
import pt.ua.travis.ui.travel.CurrentTravelFragment;
import pt.ua.travis.ui.navigationdrawer.BlurDrawerItem;
import pt.ua.travis.ui.navigationdrawer.BlurDrawerObject;
import pt.ua.travis.ui.ridelist.RideItem;
import pt.ua.travis.ui.ridelist.RideListFragment;
import pt.ua.travis.utils.CommonKeys;
import pt.ua.travis.utils.TravisUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class MainTaxiActivity extends MainActivity
        implements ActionBar.TabListener {

    private CurrentTravelFragment currentlyShownTravelFragment;
    private int currentlyShownFragmentIndex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Taxi thisTaxi = PersistenceManager.getCurrentlyLoggedInUser();

        PersistenceManager.stopWatchingRides();
        PersistenceManager.startWatchingNewRidesForTaxi(thisTaxi, this);

        View scheduledRidesTab = getLayoutInflater().inflate(R.layout.tab_with_badge, null);


        ActionBar bar = getSupportActionBar();
        bar.addTab(bar.newTab()
                .setIcon(R.drawable.ic_travel)
                .setTabListener(this));
        bar.addTab(bar.newTab()
                .setIcon(R.drawable.ic_scheduled)
                .setCustomView(scheduledRidesTab)
                .setTabListener(this));

        currentlyShownTravelFragment = new CurrentTravelFragment();
        currentlyShownRideListFragment = RideListFragment.newInstance(RideItem.SHOW_CLIENT);
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
        loggedInString += " " + thisTaxi.name();
        showTravisNotification(loggedInString, thisTaxi.imageUri(), NotificationColor.DEFAULT);
    }

    @Override
    protected TabPagerAdapter getTabPagerAdapter() {
        return new TabPagerAdapter() {

            @Override
            public Fragment getFragment(int position) {
                switch (position) {
                    case 0:
                        return currentlyShownTravelFragment;
                    case 1:
                        return currentlyShownRideListFragment;
                }
                return null;
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        getSupportMenuInflater().inflate(R.menu.actionbar_with_search, menu);

//        configureSearchBar(menu);

//        return super.onPrepareOptionsMenu(menu);
        return true;
    }

    /**
     * Populates the drawer navigation menu.
     *
     * @param drawerItems the list that must be populated to translate into
     *                    items or indicators in the drawer navigation menu
     */
    @Override
    protected void fillDrawerNavigation(final List<BlurDrawerObject> drawerItems, final CloseDrawerAction action) {


        BlurDrawerItem item1 = new BlurDrawerItem(this, R.drawable.ic_travel, R.string.current_travel);
        item1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action.closeDrawer();
                goToTab(0);
            }
        });

        BlurDrawerItem item2 = new BlurDrawerItem(this, R.drawable.ic_scheduled, R.string.scheduled_rides);
        item2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action.closeDrawer();
                goToTab(1);
            }
        });

        drawerItems.add(item1);
        drawerItems.add(item2);
    }


//    @Override
//    public boolean dispatchTouchEvent(MotionEvent event) {
//
//        if(currentlyShownTravelFragment.slidingPaneIsOpened()) {
//            // DO NOTHING
//            return true;
//        }
//
//        return super.dispatchTouchEvent(event);
//    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.remove(currentlyShownChooserFragment);
//        ft.commit();
//
//        outState.putInt(CommonKeys.SELECTED_INDEX, currentlyShownChooserFragment.getCurrentSelectedIndex());
//
//        super.onSaveInstanceState(outState);
//    }


    @Override
    public void logout(View view) {
        super.logout(view);
    }

    public void goToTab(int tabIndex) {

        ActionBar bar = getSupportActionBar();
        bar.selectTab(bar.getTabAt(tabIndex));

    }

    public void startTravel(Ride ride) {
        goToTab(0);
        currentlyShownTravelFragment.goToOrigin(ride);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onEvent(final Ride requestRide) {
        // CLIENT REQUEST RIDE ARRIVED !!!
        LatLng latLng = ((TravisApplication) getApplication()).getCurrentLocation();
        requestRide.setOriginLocation(latLng.latitude, latLng.longitude);
        PersistenceManager.addToCache(requestRide);

        Intent acceptIntent = new Intent(this, MainTaxiActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        acceptIntent.putExtra(CommonKeys.RIDE_REQUEST_ACCEPTED_ID, requestRide.id());
        final PendingIntent piAccept = PendingIntent.getActivity(this, 0, acceptIntent, PendingIntent.FLAG_ONE_SHOT);


        Intent dismissIntent = new Intent(this, MainTaxiActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        dismissIntent.putExtra(CommonKeys.RIDE_REQUEST_DECLINED_ID, requestRide.id());
        final PendingIntent piDecline = PendingIntent.getActivity(this, 0, dismissIntent, PendingIntent.FLAG_ONE_SHOT);

        final String clientName = requestRide.client().name();
        final String requestedString = getString(R.string.x_requested);

        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                try {
                    URL url = new URL(requestRide.client().imageUri());

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
                    Resources res = getResources();
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

                String msg = clientName + " " + getString(R.string.x_requested_large) + ".";
                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainTaxiActivity.this)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setAutoCancel(true)
                        .setLargeIcon(bitmap)
                        .setSmallIcon(R.drawable.ic_stat_logo)
                        .setContentTitle(clientName + " " + requestedString)
                        .setContentText(msg)
                        .setTicker(clientName + " " + requestedString)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .addAction(R.drawable.ic_action_accept,
                                getString(R.string.accept), piAccept)
                        .addAction(R.drawable.ic_action_cancel,
                                getString(R.string.decline), piDecline);

                Notification n = builder.build();
                n.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_ONGOING_EVENT;

                TravisApplication app = (TravisApplication) getApplication();
                app.startNotificationForRide(n, requestRide);
            }
        }.execute();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Bundle extras = intent.getExtras();
        String rideID;
        if(extras!=null){
            if ((rideID = extras.getString(CommonKeys.RIDE_REQUEST_ACCEPTED_ID))!=null) {
                Ride r = PersistenceManager.getFromCache(rideID);

                PersistenceManager.setRequestAccepted(r);

                TravisApplication app = (TravisApplication) getApplication();
                app.stopNotificationForRide(r);

                currentlyShownRideListFragment.onRefreshStarted(null);

//                goToTab(0);
//                currentlyShownTravelFragment.goToOrigin(r);

            } else if ((rideID = extras.getString(CommonKeys.RIDE_REQUEST_DECLINED_ID))!=null) {
                Ride r = PersistenceManager.getFromCache(rideID);

                PersistenceManager.setRequestDeclined(r);

                TravisApplication app = (TravisApplication) getApplication();
                app.stopNotificationForRide(r);
            }
        }
    }
}