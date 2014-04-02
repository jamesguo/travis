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
import com.tyczj.mapnavigator.Directions;
import com.tyczj.mapnavigator.Navigator;
import org.joda.time.LocalTime;
import pt.ua.travis.R;
import pt.ua.travis.core.Ride;
import pt.ua.travis.db.PersistenceManager;
import pt.ua.travis.gui.addresspicker.AddressPickerDialog;
import pt.ua.travis.gui.main.MainTaxiActivity;
import pt.ua.travis.utils.Keys;


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

        userType = intent.getStringExtra(Keys.USER_TYPE);
        thisRide = (Ride) intent.getSerializableExtra(Keys.SCHEDULED_RIDE);
        double destLat = thisRide.destinationLat;
        double destLng = thisRide.destinationLng;

        Log.e("BUUUUUUUUUUUUUU", destLat + " - " + destLng);

        if(userType.equals("client")) {
            if(destLat == 0 && destLng == 0) {
                Intent newIntent = new Intent(this, AddressPickerDialog.class);
                startActivityForResult(newIntent, Keys.REQUEST_DESTINATION_COORDS);
            } else {
                getDirection(new LatLng(destLat, destLng));
            }
        } else if(userType.equals("taxi")) {
            // TODO SEND TAXI TO ACTIVITY TO WAIT FOR CLIENT TO SET A DESTINATION (*1)
            getDirection(new LatLng(destLat, destLng));
        }

        Ride incomingRide = (Ride) intent.getSerializableExtra(Keys.NEW_REQUEST_ACCEPTED_DURING_TRAVEL);
        if(incomingRide!=null){
            PersistenceManager.addRide(incomingRide);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Keys.REQUEST_DESTINATION_COORDS && resultCode==RESULT_OK){
            double lat = data.getDoubleExtra(Keys.PICKED_POSITION_LAT, 0);
            double lng = data.getDoubleExtra(Keys.PICKED_POSITION_LNG, 0);

            if((lat==0 && lng!=0) || (lat!=0 && lng==0) || (lat!=0 && lng!=0)) {
                getDirection(new LatLng(lat, lng));
            }
        } else if(requestCode==Keys.WAIT_FOR_DESTINATION){
            // TODO (*1)
        }
    }

    private void getDirection(LatLng destination){
        LatLng origin = new LatLng(thisRide.originLat, thisRide.originLng);

        navigator = new Navigator(map, origin, destination);
        navigator.findDirections(true);
        navigator.setOnPathSetListener(this);
    }

    @Override
    public void onPathSetListener(Directions directions) {
//        List<Route> routes = directions.getRoutes();
//        List<String> routesString = new ArrayList<>(routes.size());
//        for(Route r : routes){
//            routesString.add("From: "+r.getStartAddress()+"\nTo: "+r.getEndAddress());
//        }
//        routeList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, routesString));


        map.moveCamera(CameraUpdateFactory.newLatLngZoom(navigator.getStartPoint(), 15));
    }

    public void onAuthentButtonClicked(View view){
        PersistenceManager.removeRide(thisRide);

        if(userType.equals("client")) {
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra(Keys.TAXI_TO_PAY, thisRide.taxi);
            startActivity(intent);
        } else if(userType.equals("taxi")){
            Intent intent = new Intent(this, MainTaxiActivity.class);
            startActivity(intent);
        }
    }


    public void onNotificationButtonClicked(View view){

        Ride incomingRide = new Ride(
                PersistenceManager.selectThisTaxiAccount(),
                PersistenceManager.selectThisClientAccount(),
                LocalTime.now().plusHours(1));

        Intent resultIntent = new Intent(this, TravelToDestinationActivity.class);
        resultIntent.putExtra(Keys.USER_TYPE, userType);
        resultIntent.putExtra(Keys.SCHEDULED_RIDE, thisRide);
        resultIntent.putExtra(Keys.NEW_REQUEST_ACCEPTED_DURING_TRAVEL, incomingRide);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Because clicking the notification launches a new ("special") activity,
        // there's no need to create an artificial back stack.
        PendingIntent piAccept = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent dismissIntent = new Intent(this, TravelToDestinationActivity.class);
        dismissIntent.putExtra(Keys.USER_TYPE, userType);
        dismissIntent.putExtra(Keys.SCHEDULED_RIDE, thisRide);
//        dismissIntent.setAction(CommonConstants.ACTION_DISMISS);
        PendingIntent piDecline = PendingIntent.getActivity(this, 0, dismissIntent, 0);

        String msg = incomingRide.client.realName+" requested you a ride "+
                incomingRide.getRemaining()+".";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.cabs)
                .setContentTitle("Ride Request from "+incomingRide.client.realName)
                .setContentText(msg)
                .setTicker("Ride Request from "+incomingRide.client.realName)
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