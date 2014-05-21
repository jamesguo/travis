package pt.ua.travis.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibraryConstants;
import com.parse.ParseGeoPoint;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Taxi;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TravisLocation extends BroadcastReceiver {

    private static LatLng lastLocation;
    private static OnLocationChangedListener listener;


    @Override
    public void onReceive(Context context, Intent intent) {
        final LocationInfo locationInfo =
                (LocationInfo) intent.getSerializableExtra(LocationLibraryConstants.LOCATION_BROADCAST_EXTRA_LOCATIONINFO);

        Log.e("+++++++++++++++++++++++++++++++++++++++++++++++", locationInfo.toString());
        Toast.makeText(context, locationInfo.toString(), Toast.LENGTH_LONG).show();

        double currentLat = Float.valueOf(locationInfo.lastLat).doubleValue();
        double currentLng = Float.valueOf(locationInfo.lastLong).doubleValue();


        SharedPreferences prefs = context.getSharedPreferences("TravisLastKnownLocation", Context.MODE_PRIVATE);
        prefs.edit()
                .putFloat("latitude", locationInfo.lastLat)
                .putFloat("longitude", locationInfo.lastLong)
                .commit();

        if (lastLocation != null) {
            ParseGeoPoint currentPoint = new ParseGeoPoint(currentLat, currentLng);
            ParseGeoPoint lastPoint = new ParseGeoPoint(lastLocation.latitude, lastLocation.longitude);

//            if (currentPoint.distanceInKilometersTo(lastPoint) < 0.01) {
//                return;
//            }
        }

        lastLocation = new LatLng(currentLat, currentLng);


        if(listener!=null){
            listener.onLocationChanged(lastLocation);
        }
    }

    public static void startTaxiLocationListener(final Taxi taxi) {
        stopTaxiLocationListener();
        listener = new OnLocationChangedListener() {
            @Override
            public void onLocationChanged(LatLng currentLocation) {
                taxi.setCurrentLocation(currentLocation.latitude, currentLocation.longitude);
                PersistenceManager.save(taxi, null);
            }
        };
    }

    public static void stopTaxiLocationListener() {
        listener = null;
    }

    public static LatLng getCurrentLocation(Context context) {
        LocationLibrary.forceLocationUpdate(context);
        LocationInfo latestInfo = new LocationInfo(context);
        return new LatLng(latestInfo.lastLat, latestInfo.lastLong);
    }


    private interface OnLocationChangedListener {

        void onLocationChanged(LatLng currentLocation);
    }
}
