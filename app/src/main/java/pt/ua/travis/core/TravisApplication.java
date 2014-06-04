package pt.ua.travis.core;

import android.app.Application;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Lists;
import com.parse.ParseGeoPoint;
import pt.ua.travis.R;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.ui.login.SignUpActivity;
import pt.ua.travis.utils.CommonRes;
import pt.ua.travis.utils.Validate;

import java.io.IOException;
import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TravisApplication extends Application implements LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private List<CurrentLocationListener> listeners;

    private Location currentLocation;
    private LocationRequest locationRequest;
    private LocationClient locationClient;

    @Override
    public void onCreate(){

        if(!Validate.hasGooglePlayServices(this)){
            return;
        }

        CommonRes.init(this);
        PersistenceManager.init(this);

        // TODO: REMOVE THIS
//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
//        PersistenceManager.attemptNormalLogin("cr7@gmail.com", "123");

        listeners = Lists.newArrayList();

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(120);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(120);

        // Create a new location client, using the enclosing class to handle callbacks.
        locationClient = new LocationClient(this, this, this);
        locationClient.connect();

        listeners.add(new CurrentLocationListener() {
            @Override
            public void onCurrentLocationChanged(LatLng latLng) {
                Toast.makeText(TravisApplication.this, latLng.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        locationClient.disconnect();
        PersistenceManager.logout();
//        TravisLocation.stopTaxiLocationListener();
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
//            try {
//                connectionResult.startResolutionForResult(this, ConnectionResult.RESOLUTION_REQUIRED);
//            } catch (IntentSender.SendIntentException e) {
//            }
//        } else {
//            showErrorDialog(connectionResult.getErrorCode());
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
    public final void onLocationChanged(Location location) {
        if(currentLocation!=null){
            ParseGeoPoint currentPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
            ParseGeoPoint lastPoint = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());

            if (currentPoint.distanceInKilometersTo(lastPoint) < 0.01) {
                return;
            }
        }

        currentLocation = location;
        for (CurrentLocationListener l : listeners) {
            l.onCurrentLocationChanged(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }

    public final LatLng getCurrentLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        String strLocationProvider = lm.getBestProvider(criteria, true);

        currentLocation = lm.getLastKnownLocation(strLocationProvider);
        return new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
    }

    public interface CurrentLocationListener{

        void onCurrentLocationChanged(LatLng latLng);

    }
}
