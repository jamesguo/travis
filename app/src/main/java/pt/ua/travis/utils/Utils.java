package pt.ua.travis.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.location.*;
import android.support.v4.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.Surface;
import android.view.WindowManager;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public final class Utils {

    private static MessageDigest md;
    private static Geocoder geocoder;
    private static LocationManager locationManager;

    private Utils(){}

    public static CalendarBuilderHour newTime() {
        return new CalendarBuilderHour();
    }

    public static class CalendarBuilderHour {

        private Calendar calendar;

        private CalendarBuilderHour(){
            calendar = Calendar.getInstance();
            calendar.set(1, Calendar.JANUARY, 1);
        }

        public Calendar toNow(){
            return calendar;
        }

        public CalendarBuilderMinute withHourOfDay(int hour){
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            return new CalendarBuilderMinute(calendar);
        }

        public static class CalendarBuilderMinute{

            private Calendar calendar;

            private CalendarBuilderMinute(Calendar calendar){
                this.calendar = calendar;
            }

            public Calendar andWithMinute(int minute){
                calendar.set(Calendar.MINUTE, minute);
                return calendar;
            }
        }
    }

    /**
     * Static constructor that reduces the code length when instantiating a new ArrayMap.
     * For example, using this constructor will convert
     *      "new ArrayMap<Object, SparseArray<Object>>();"
     * into
     *      "Utils.newArrayMap();"
     */
    public static <K, V> ArrayMap<K, V> newArrayMap() {
        return new ArrayMap<K, V>();
    }

    /**
     * Static constructor that reduces the code length when instantiating a new Pair.
     * For example, using this constructor will convert
     *      "new Pair<Object, Pair<Object>>();"
     * into
     *      "Utils.newPair();"
     */
    public static <F, S> Pair<F, S> newPair(F first, S second){
        return new Pair<F, S>(first, second);
    }

    /**
     * Converts an {@link Address} into a more presentable String.
     */
    public static String addressToString(final Address address) {
        if(address == null){
            return CommonRes.UNKNOWN_ADDRESS;
        }

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

    /**
     * Uses a {@link android.location.Geocoder} to convert the provided latitude and
     * longitude values into an {@link android.location.Address}.
     */
    public static Address latlngToAddress(Context context, double lat, double lng){
        if(geocoder==null)
            geocoder = new Geocoder(context);

        try {
            List<Address> address = geocoder.getFromLocation(lat, lng, 1);
            if(address.isEmpty())
                return null;
            return address.get(0);
        } catch (IOException ex){
            return null;
        }
    }

    /**
     * Converts a size value in dp scale into pixel scale.
     */
    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * Converts a size value in pixel scale into dp scale.
     */
    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * Obtains the default orientation for the current device.
     */
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

    /**
     * Generates a SHA1 digest from the provided String.
     */
    public static String generateSHA1DigestFromString(String password){
        try {
            if(md==null){
                md = MessageDigest.getInstance("SHA1");
            }
            return new String(md.digest(password.getBytes()));

        }catch (GeneralSecurityException ex){
            // Should never happen since SHA1 is a valid algorithm.
        }
        return password;
    }

//    public interface OnCurrentLocationListener{
//
//        void onCurrentLocationFound(Location location);
//    }

//    public static void getCurrentLocation(Context context, final OnCurrentLocationListener listener){
//        // Getting LocationManager object from System Service LOCATION_SERVICE
//        if(locationManager==null) {
//            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        }
//
////        // Creating a criteria object to retrieve provider
////        Criteria criteria = new Criteria();
////
////        // Getting the name of the best provider
////        String provider = locationManager.getBestProvider(criteria, true);
//
//        // Getting Current Location
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, new LocationListener() {
//            boolean executedListener = false;
//
//            @Override
//            public void onLocationChanged(Location location) {
//                if(!executedListener) {
//                    listener.onCurrentLocationFound(location);
//                    executedListener = true;
//                }
//            }
//
//            @Override
//            public void onStatusChanged(String s, int i, Bundle bundle) {}
//
//            @Override
//            public void onProviderEnabled(String s) {}
//
//            @Override
//            public void onProviderDisabled(String s) {}
//        });
////        return locationManager.getLastKnownLocation(proSider);
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

//    public static Location getCurrentLocation(Context context) {
//        Location location = new Location("");
//        location.setLatitude(40.646908);
//        location.setLongitude(-8.662523);
//
//        return location;
//    }
}
