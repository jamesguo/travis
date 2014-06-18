package pt.ua.travis.core;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.location.Criteria;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Lists;
import com.parse.LocationCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Ride;
import pt.ua.travis.utils.CommonRes;
import pt.ua.travis.utils.TravisUtils;
import pt.ua.travis.utils.Validate;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Main application class that acts as a bridge for every activity. Common
 * functionality, like location query and listening and notification handling,
 * is implemented here, so that any activity can access it using the
 * "getApplication()" method.
 *
 * Every backend, API, and common resource is initialized with this application's
 * "onCreate", simply because this is the first component that starts when Travis
 * is launched.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TravisApplication extends Application implements LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private NotificationManager notificationManager;

    private Map<String, Integer> rideIdsToNotificationIds;
    private Random random;

    private List<CurrentLocationListener> listeners;

    private Location currentLocation = new Location("");
    private LocationRequest locationRequest;
    private LocationClient locationClient;

    @Override
    public void onCreate(){

        if(!Validate.hasGooglePlayServices(this)){
            return;
        }

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // starts common resources and backend connections
        CommonRes.init(this);
        PersistenceManager.init(this);

        rideIdsToNotificationIds = TravisUtils.newMap();
        random = new Random(10000);

        listeners = Lists.newArrayList();


        // get a current location right now
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);

        ParseGeoPoint.getCurrentLocationInBackground(60000, criteria, new LocationCallback() {
            @Override
            public void done(ParseGeoPoint point, ParseException ex) {
                if (ex == null) {
                    currentLocation.setLatitude(point.getLatitude());
                    currentLocation.setLongitude(point.getLongitude());
                } else {
                    Log.e("TravisApplication", "Error getting current location", ex);
                }
            }
        });

        // enable a location listener
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(120);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(120);

        // create a new location client, using the enclosing class to handle callbacks.
        locationClient = new LocationClient(this, this, this);
        locationClient.connect();
    }

    @Override
    public void onTerminate() {
        locationClient.disconnect();
        PersistenceManager.logout();
        listeners.clear();
        stopAllRideNotifications();

        super.onTerminate();
    }



    public void startNotificationForRide(Notification n, Ride ride){
        int notificationId = random.nextInt();
        rideIdsToNotificationIds.put(ride.id(), notificationId);
        notificationManager.notify(notificationId, n);
    }

    public void stopNotificationForRide(Ride ride) {
        int notificationId = rideIdsToNotificationIds.get(ride.id());
        notificationManager.cancel(notificationId);
    }

    public void stopAllRideNotifications() {

        // removes all notifications
        for(int id : rideIdsToNotificationIds.values()){
            notificationManager.cancel(id);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        currentLocation = getLocation();
        startPeriodicUpdates();
    }

    @Override
    public void onDisconnected() {
        if (locationClient.isConnected()) {
            stopPeriodicUpdates();
            locationClient.disconnect();
        }
    }

    @Override
    public final void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            locationClient.connect();
        }
    }

    private void startPeriodicUpdates() {
        locationClient.requestLocationUpdates(locationRequest, this);
    }

    private void stopPeriodicUpdates() {
        locationClient.removeLocationUpdates(this);
    }

    private Location getLocation() {
        return locationClient.getLastLocation();
    }

    public void addLocationListener(CurrentLocationListener listener) {
        listeners.add(listener);
    }

    public void clearListeners() {
        listeners.clear();
    }

    @Override
    public final void onLocationChanged(Location newLocation) {
        if(currentLocation!=null){
            ParseGeoPoint currentPoint = new ParseGeoPoint(newLocation.getLatitude(), newLocation.getLongitude());
            ParseGeoPoint lastPoint = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());

            if (currentPoint.distanceInKilometersTo(lastPoint) < 0.01) {
                return;
            }
        }

        currentLocation = newLocation;
        for (CurrentLocationListener l : listeners) {
            l.onCurrentLocationChanged(new LatLng(newLocation.getLatitude(), newLocation.getLongitude()));
        }
    }

    public final LatLng getCurrentLocation() {
        return new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
    }

    public interface CurrentLocationListener{

        void onCurrentLocationChanged(LatLng latLng);

    }
}
