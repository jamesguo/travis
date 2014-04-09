package pt.ua.travis.gui.travel;

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
import pt.ua.travis.backend.entities.CloudBackendManager;
import pt.ua.travis.backend.entities.Ride;
import pt.ua.travis.gui.main.MainTaxiActivity;
import pt.ua.travis.mapnavigator.Directions;
import pt.ua.travis.mapnavigator.Navigator;
import pt.ua.travis.utils.CommonKeys;
import pt.ua.travis.utils.Utils;

import java.util.Calendar;

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
        setContentView(R.layout.travel_activity);
        getSupportActionBar().hide();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        map = mapFragment.getMap();
//        routeList = (ListView) findViewById(R.id.route_list);

        Intent intent = getIntent();

        userType = intent.getStringExtra(CommonKeys.USER_TYPE);
        thisRide = (Ride) intent.getSerializableExtra(CommonKeys.SCHEDULED_RIDE);
        LatLng dest = thisRide.destinationPosition();

        Log.e("BUUUUUUUUUUUUUU", dest.toString());

        if(userType.equals("client")) {
            if(dest.latitude == 0 && dest.longitude == 0) {
                // TODO IF NO DESTINATION SHOW BUTTON TO POP DESTINATION AND TO POP PAYMENT
            } else {
                // TODO IF DESTINATION SHOW NAVIGATION AND BUTTON TO POP PAYMENT
                getDirection(dest);
            }
        } else if(userType.equals("taxi")) {
            // TODO SEND TAXI TO ACTIVITY TO WAIT FOR CLIENT TO SET A DESTINATION (*1)
            getDirection(dest);
        }

        Ride incomingRide = (Ride) intent.getSerializableExtra(CommonKeys.NEW_REQUEST_ACCEPTED_DURING_TRAVEL);
        if(incomingRide!=null){
            incomingRide.save();
        }
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
        thisRide.isCompleted = true;  // TODO: Set ride as completed (send to history)
        thisRide.save();

        if(userType.equals("client")) {
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra(CommonKeys.TAXI_TO_PAY, thisRide.taxi());
            startActivity(intent);
        } else if(userType.equals("taxi")){
            Intent intent = new Intent(this, MainTaxiActivity.class);
            startActivity(intent);
        }
    }


    public void onNotificationButtonClicked(View view){

        // TODO: The following ride is simulated, should be received from Database instead.
        Ride incomingRide = new Ride();
        incomingRide.setTaxi(CloudBackendManager.select().taxis().loggedInThisDevice());
        incomingRide.setClient(CloudBackendManager.select().clients().execute().get(0));

        Calendar nowPlusOneHour = Utils.newTime().toNow();
        nowPlusOneHour.add(Calendar.HOUR_OF_DAY, 1);
        incomingRide.setScheduledTime(nowPlusOneHour);

        Intent resultIntent = new Intent(this, TravelToDestinationActivity.class);
        resultIntent.putExtra(CommonKeys.USER_TYPE, userType);
        resultIntent.putExtra(CommonKeys.SCHEDULED_RIDE, thisRide);
        resultIntent.putExtra(CommonKeys.NEW_REQUEST_ACCEPTED_DURING_TRAVEL, incomingRide);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Because clicking the notification launches a new ("special") activity,
        // there's no need to create an artificial back stack.
        PendingIntent piAccept = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent dismissIntent = new Intent(this, TravelToDestinationActivity.class);
        dismissIntent.putExtra(CommonKeys.USER_TYPE, userType);
        dismissIntent.putExtra(CommonKeys.SCHEDULED_RIDE, thisRide);
//        dismissIntent.setAction(CommonConstants.ACTION_DISMISS);
        PendingIntent piDecline = PendingIntent.getActivity(this, 0, dismissIntent, 0);

        String clientName = incomingRide.client().name();
        String msg = clientName+" requested you a ride "+
                incomingRide.getRemaining()+".";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
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
        notificationManager.notify(102, n);
    }
}