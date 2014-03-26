package pt.ua.travis.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.location.*;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public final class Tools {

    private static MessageDigest md;
    private static Geocoder geocoder;

    private Tools(){}

    public static String formatAddress(final Address address) {
        StringBuilder sb = new StringBuilder();
        final int addressLineSize = address.getMaxAddressLineIndex();
        for (int i = 0; i < addressLineSize; i++) {
            sb.append(address.getAddressLine(i));
            if (i != addressLineSize - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

//    public static Location getCurrentLocation(Context context){
//        // Getting LocationManager object from System Service LOCATION_SERVICE
//        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//
//        // Creating a criteria object to retrieve provider
//        Criteria criteria = new Criteria();
//
//        // Getting the name of the best provider
//        String provider = locationManager.getBestProvider(criteria, true);
//
//        // Getting Current Location
//        return locationManager.getLastKnownLocation(provider);
////
////        if(location!=null) {
////            // Getting latitude of the current location
////            double latitude = location.getLatitude();
////
////            // Getting longitude of the current location
////            double longitude = location.getLongitude();
////
////            // Creating a LatLng object for the current location
////            return new LatLng(latitude, longitude);
////        }
////        return null;
//    }

    public static Location getCurrentLocation(Context context) {
        Location location = new Location("");
        location.setLatitude(40.646908);
        location.setLongitude(-8.662523);

        return location;
    }

    public static String latlngToAddressString(Context context, double lat, double lng){
        if(geocoder==null)
            geocoder = new Geocoder(context);

        try {
            List<Address> address = geocoder.getFromLocation(lat, lng, 1);
            return formatAddress(address.get(0));
        } catch (IOException ex){
            return "";
        }
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int getDeviceDefaultOrientation(Context context) {

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        Configuration config = context.getResources().getConfiguration();

        int rotation = windowManager.getDefaultDisplay().getRotation();

        if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
                config.orientation == Configuration.ORIENTATION_LANDSCAPE)
                || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&
                config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
            return Configuration.ORIENTATION_LANDSCAPE;
        } else {
            return Configuration.ORIENTATION_PORTRAIT;
        }
    }

    public static String passwordToDigestSHA1(String password){
        try {
            if(md==null){
                md = MessageDigest.getInstance("SHA1");
            }
            return new String(md.digest(password.getBytes()));

        }catch (GeneralSecurityException ex){}
        return password;
    }
}
