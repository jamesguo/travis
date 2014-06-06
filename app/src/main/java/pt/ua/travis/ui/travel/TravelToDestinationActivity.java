package pt.ua.travis.ui.travel;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import pt.ua.travis.R;
import pt.ua.travis.backend.*;
import pt.ua.travis.mapnavigator.Directions;
import pt.ua.travis.mapnavigator.Navigator;
import pt.ua.travis.utils.CommonKeys;
import pt.ua.travis.utils.TravisUtils;

import java.util.Calendar;
import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TravelToDestinationActivity extends SherlockFragmentActivity implements Navigator.OnPathSetListener {

    private String userType;

    private Ride thisRide;

    private GoogleMap map;

    private ListView routeList;

    private Navigator navigator;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);
        getSupportActionBar().hide();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        map = mapFragment.getMap();
//        routeList = (ListView) findViewById(R.id.route_list);

        final Intent intent = getIntent();

        userType = intent.getStringExtra(CommonKeys.USER_TYPE);

        final String rideID = getIntent().getStringExtra(CommonKeys.RIDE_REQUEST_DECLINED_ID);
        thisRide = PersistenceManager.getFromCache(rideID);


        LatLng dest = thisRide.destinationPosition();

        Log.e("BUUUUUUUUUUUUUU", dest.toString());

        if(userType.equals("client")) {
            if(dest.latitude == 0 && dest.longitude == 0) {
                // TODO IF NO DESTINATION -> SHOW BUTTON TO POP DESTINATION AND TO POP PAYMENT
            } else {
                // TODO IF DESTINATION -> SHOW NAVIGATION AND BUTTON TO POP PAYMENT
                getDirection(dest);
            }
        } else if(userType.equals("taxi")) {
            // TODO SEND TAXI_ID TO ACTIVITY TO WAIT FOR CLIENT_ID TO SET A DESTINATION (*1)
            getDirection(dest);
        }

        String incomingRideID = intent.getStringExtra(CommonKeys.NEW_REQUEST_ACCEPTED_DURING_TRAVEL);
        // TODO: SET REQUEST IN FIREBASE AS ACCEPTED

    }

    private void getDirection(LatLng destination){

        navigator = new Navigator(map, thisRide.originPosition(), destination);
        navigator.findDirections(true);
        navigator.setOnPathSetListener(this);
    }

    @Override
    public void onPathSetListener(Directions directions) {
//        List<Route> routes = directions.getRoutes();
//        List<String> routesString = Lists.newArrayList(routes.size());
//        for(Route r : routes){
//            routesString.add("From: "+r.getStartAddress()+"\nTo: "+r.getEndAddress());
//        }
//        routeList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, routesString));


        map.moveCamera(CameraUpdateFactory.newLatLngZoom(navigator.getStartPoint(), 15));
    }

    public void onAuthentButtonClicked(View view){
        thisRide.setAsCompleted();
        PersistenceManager.save(thisRide, new Callback<Ride>() {
            @Override
            public void onResult(Ride result) {
//                if (userType.equals("client")) {
//                    Intent intent = new Intent(TravelToDestinationActivity.this, PaymentActivity.class);
//                    intent.putExtra(CommonKeys.SAVED_TAXI_OBJECT_ID, result.taxi().id());
//                    startActivity(intent);
//                } else if (userType.equals("taxi")) {
//                    Intent intent = new Intent(TravelToDestinationActivity.this, MainTaxiActivity.class);
//                    startActivity(intent);
//                }
            }
        });
    }


    public void onNotificationButtonClicked(View view) {
        final Context context = this;

        // TODO: The following ride is simulated, should be received from Database instead.
        PersistenceManager.query().clients().later(new Callback<List<Client>>() {
            @Override
            public void onResult(List<Client> result) {

                Ride incomingRide = new Ride();
                Taxi t = PersistenceManager.getCurrentlyLoggedInUser();
                incomingRide.setTaxi(t);
                incomingRide.setClient(result.get(0));

                Calendar nowPlusOneHour = Calendar.getInstance();
                nowPlusOneHour.add(Calendar.HOUR_OF_DAY, 1);
                incomingRide.setScheduledTime(TravisUtils.dateFromCalendar(nowPlusOneHour));

                Intent resultIntent = new Intent(context, TravelToDestinationActivity.class);
                resultIntent.putExtra(CommonKeys.USER_TYPE, userType);
                resultIntent.putExtra(CommonKeys.RIDE_REQUEST_DECLINED_ID, thisRide.id());
                resultIntent.putExtra(CommonKeys.NEW_REQUEST_ACCEPTED_DURING_TRAVEL, incomingRide.id());
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                // Because clicking the notification launches a new ("special") activity,
                // there's no need to create an artificial back stack.
                PendingIntent piAccept = PendingIntent.getActivity(context, 0, resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                Intent dismissIntent = new Intent(context, TravelToDestinationActivity.class);
                dismissIntent.putExtra(CommonKeys.USER_TYPE, userType);
                dismissIntent.putExtra(CommonKeys.RIDE_REQUEST_DECLINED_ID, thisRide.id());
//        dismissIntent.setAction(CommonConstants.ACTION_DISMISS);
                PendingIntent piDecline = PendingIntent.getActivity(context, 0, dismissIntent, 0);

                String clientName = incomingRide.client().name();
                String msg = clientName + " requested you a ride " +
                        incomingRide.getRemaining() + ".";
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


                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Notification n = builder.build();
                n.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
                notificationManager.notify(102, n);
            }
        });
    }
}