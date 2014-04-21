package pt.ua.travis.ui.mainscreen;

import android.app.Application;
import android.app.Dialog;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public abstract class GeoLocationActivity extends SherlockFragmentActivity implements LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private Location currentLocation;
    private LocationRequest locationRequest;
    private LocationClient locationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(120);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(120);

        // Create a new location client, using the enclosing class to handle callbacks.
        locationClient = new LocationClient(this, this, this);
    }


    @Override
    public void onStart() {
        super.onStart();

        locationClient.connect();
    }


    @Override
    public void onStop() {
        onDisconnected();

        super.onStop();
    }


    @Override
    public void onConnected(Bundle bundle) {
        currentLocation = getLocation();
        startPeriodicUpdates();
    }


    @Override
    public final void onDisconnected() {
        if (locationClient.isConnected()) {
            stopPeriodicUpdates();
            locationClient.disconnect();
        }
    }


    @Override
    public final void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, ConnectionResult.RESOLUTION_REQUIRED);
            } catch (IntentSender.SendIntentException e) {
            }
        } else {
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
        if (servicesConnected()) {
            return locationClient.getLastLocation();
        } else {
            return null;
        }
    }


    private boolean servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
//                errorFragment.show(getSupportFragmentManager(), Application.APPTAG);
            }
            return false;
        }
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
        onLocationChanged(new LatLng(location.getLatitude(), location.getLongitude()));
    }


    public abstract void onLocationChanged(LatLng latLng);


    public final LatLng getCurrentLocation() {
        if(currentLocation==null){
            currentLocation = locationClient.getLastLocation();
        }
        return new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
    }


    private static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
}
