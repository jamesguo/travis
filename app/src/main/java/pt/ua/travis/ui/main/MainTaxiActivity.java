package pt.ua.travis.ui.main;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import com.actionbarsherlock.view.Menu;
import com.google.android.gms.maps.model.LatLng;
import pt.ua.travis.R;
import pt.ua.travis.backend.*;
import pt.ua.travis.core.TravisLocation;
import pt.ua.travis.ui.navigationdrawer.DrawerItem;
import pt.ua.travis.ui.navigationdrawer.DrawerSeparator;
import pt.ua.travis.ui.navigationdrawer.DrawerUser;
import pt.ua.travis.ui.navigationdrawer.DrawerView;
import pt.ua.travis.ui.ridelist.RideItem;
import pt.ua.travis.ui.ridelist.RideListFragment;
import pt.ua.travis.ui.currenttravel.TravelToOriginActivity;
import pt.ua.travis.utils.CommonKeys;

import java.util.List;

// TODO: BACK STACK NEEDS WORK!

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class MainTaxiActivity extends MainActivity {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        goToScheduledRidesList(null);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        getSupportMenuInflater().inflate(R.menu.actionbar_with_search, menu);

        return super.onPrepareOptionsMenu(menu);
    }


    /**
     * Populates the drawer navigation menu.
     *
     * @param drawerViews the list that must be populated to translate into
     *                    items or indicators in the drawer navigation menu
     */
    @Override
    protected void fillDrawerNavigation(final List<DrawerView> drawerViews) {

        final Taxi loggedInTaxi = PersistenceManager.query().taxis().loggedInThisDevice();

        drawerViews.add(new DrawerUser(loggedInTaxi));
        drawerViews.add(new DrawerItem(2, R.string.menu_logout, R.drawable.ic_logout));
        drawerViews.add(new DrawerSeparator());
        drawerViews.add(new DrawerItem(3, R.string.menu_settings, R.drawable.ic_settings));
    }


    @Override
    protected void onDrawerItemClick(int itemID) {
        switch (itemID){
            case 1: goToScheduledRidesList(null); break;
            case 2: logout(null); break;
            default: break;
        }

        super.onDrawerItemClick(itemID);
    }


    public void goToScheduledRidesList(View view){
        final Context context = MainTaxiActivity.this;
        final Taxi loggedInTaxi = PersistenceManager.query().taxis().loggedInThisDevice();

        PersistenceManager.query().rides().withUser(loggedInTaxi).scheduled().sortedByTime().later(new Callback<List<Ride>>() {
            @Override
            public void onResult(List<Ride> result) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.tab_pager, RideListFragment.newInstance(RideItem.SHOW_CLIENT))
                        .addToBackStack(null)
                        .commit();
            }
        });

        PersistenceManager.stopWatchingRides();
        PersistenceManager.startWatchingNewRidesForTaxi(loggedInTaxi, new WatchEvent<Ride>() {
            @Override
            public void onEvent(Ride newRide) {

                LatLng latLng = TravisLocation.getCurrentLocation(MainTaxiActivity.this);
                newRide.setOriginLocation(latLng.latitude, latLng.longitude);

                Intent resultIntent = new Intent(context, TravelToOriginActivity.class);
                resultIntent.putExtra(CommonKeys.SCHEDULED_RIDE_ID, newRide.id());
                PersistenceManager.addToCache(newRide);

                resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                // Because clicking the notification launches a new ("special") activity,
                // there's no need to create an artificial back stack.
                PendingIntent piAccept = PendingIntent.getActivity(context, 0, resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                Intent dismissIntent = new Intent(context, MainTaxiActivity.class);
//                dismissIntent.setAction(CommonConstants.ACTION_DISMISS);
                PendingIntent piDecline = PendingIntent.getActivity(context, 0, dismissIntent, 0);

                String clientName = newRide.client().name();

                String msg = clientName + " requested you a ride " +
                        newRide.getRemaining() + ".";
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.cabs)
                        .setContentTitle("Ride Request from " + clientName)
                        .setContentText(msg)
                        .setTicker("Ride Request from " + clientName)
//                        .setDefaults(NotificationCompat.DEFAULT_ALL)

                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .addAction(R.drawable.ic_action_accept,
                                getString(R.string.accept), piAccept)
                        .addAction(R.drawable.ic_action_cancel,
                                getString(R.string.decline), piDecline);


                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Notification n = builder.build();
                n.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
                notificationManager.notify(101, n);
            }
        });
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