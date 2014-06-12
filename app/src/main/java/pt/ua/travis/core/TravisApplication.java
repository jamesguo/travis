package pt.ua.travis.core;

import android.app.Application;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Lists;
import com.parse.LocationCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.squareup.picasso.Picasso;
import pt.ua.travis.R;
import pt.ua.travis.backend.Callback;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.ui.customviews.CircularImageView;
import pt.ua.travis.ui.login.SignUpActivity;
import pt.ua.travis.utils.CommonRes;
import pt.ua.travis.utils.TravisUtils;
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

    private Location currentLocation = new Location("");
    private LocationRequest locationRequest;
    private LocationClient locationClient;

    @Override
    public void onCreate(){

        if(!Validate.hasGooglePlayServices(this)){
            return;
        }

        // starts common resources and backend connections
        CommonRes.init(this);
        PersistenceManager.init(this);

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
