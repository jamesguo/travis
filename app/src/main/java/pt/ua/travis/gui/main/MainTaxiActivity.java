package pt.ua.travis.gui.main;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import com.actionbarsherlock.view.Menu;
import pt.ua.travis.R;
import pt.ua.travis.backend.entities.CloudBackendManager;
import pt.ua.travis.backend.entities.Ride;
import pt.ua.travis.backend.entities.Taxi;
import pt.ua.travis.backend.Geolocation;
import pt.ua.travis.gui.ridelist.RideItem;
import pt.ua.travis.gui.ridelist.RideListFragment;
import pt.ua.travis.gui.travel.TravelToOriginActivity;
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
    protected void fillDrawerNavigation(List<DrawerView> drawerViews) {

        Taxi loggedInTaxi = CloudBackendManager.select().taxis().loggedInThisDevice();

        int numOfRides = CloudBackendManager.select().rides().with(loggedInTaxi).uncompleted().execute().size();
        drawerViews.add(new DrawerUser(loggedInTaxi));
        drawerViews.add(new DrawerItem(1, R.string.menu_rides, R.drawable.ic_action_alarms, numOfRides));
        drawerViews.add(new DrawerItem(2, R.string.menu_logout, R.drawable.ic_action_about));
        drawerViews.add(new DrawerSeparator());
        drawerViews.add(new DrawerItem(3, R.string.yes, R.drawable.ic_action_accept));
//        drawerViews.add(new DrawerView(5, R.string.menu_settings, R.drawable.ic_action_settings));
    }

    @Override
    protected void onDrawerItemClick(int itemID) {
        switch (itemID){
            case 1: goToScheduledRidesList(null); break;
            case 2: logout(null); break;
            case 3: onAuthentButtonClicked(null); break;
            default: break;
        }

        super.onDrawerItemClick(itemID);
    }

    public void goToScheduledRidesList(View view){
        Taxi loggedInTaxi = CloudBackendManager.select().taxis().loggedInThisDevice();
        List<Ride> rideList = CloudBackendManager.select()
                .rides()
                .with(loggedInTaxi)
                .uncompleted()
                .sortedByTime()
                .execute();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, RideListFragment.newInstance(RideItem.SHOW_CLIENT, rideList))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDeletedRide(){
        goToScheduledRidesList(null);
    }

    public void onAuthentButtonClicked(View view){
        Context context = MainTaxiActivity.this;
        Taxi loggedInTaxi = CloudBackendManager.select().taxis().loggedInThisDevice();
        List<Ride> rideList = CloudBackendManager.select()
                .rides()
                .with(loggedInTaxi)
                .uncompleted()
                .sortedByTime()
                .execute();

        final Ride thisRide = rideList.get(0);
        thisRide.setOriginPositionFromLatLng(Geolocation.getCurrentPosition());

        Intent resultIntent = new Intent(context, TravelToOriginActivity.class);
        resultIntent.putExtra(CommonKeys.SCHEDULED_RIDE, thisRide);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Because clicking the notification launches a new ("special") activity,
        // there's no need to create an artificial back stack.
        PendingIntent piAccept = PendingIntent.getActivity(context, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent dismissIntent = new Intent(context, MainTaxiActivity.class);
//        dismissIntent.setAction(CommonConstants.ACTION_DISMISS);
        PendingIntent piDecline = PendingIntent.getActivity(context, 0, dismissIntent, 0);

        String clientName = thisRide.client().name();

        String msg = clientName+" requested you a ride "+
                thisRide.getRemaining()+".";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.cabs)
                .setContentTitle("Ride Request from "+clientName)
                .setContentText(msg)
                .setTicker("Ride Request from "+clientName)
//                        .setDefaults(NotificationCompat.DEFAULT_ALL)

                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .addAction (R.drawable.ic_action_accept,
                        getString(R.string.accept), piAccept)
                .addAction (R.drawable.ic_action_cancel,
                        getString(R.string.decline), piDecline);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = builder.build();
        n.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(101, n);
    }

    @Override
    public void logout(View view) {
        super.logout(view);
    }
}