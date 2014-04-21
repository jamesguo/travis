package pt.ua.travis.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.location.*;
import android.os.StrictMode;
import android.support.v4.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Lists;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.ua.travis.backend.Taxi;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.*;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public final class Utils {

    private static MessageDigest md;
    private static Geocoder geocoder;
    private static LocationManager locationManager;

    private Utils(){}


    /**
     * Convenience method for creating an appropriately typed empty array map, which
     * also reduces the code length when instantiating a new ArrayMap.
     * For example, using this constructor will convertLater
     *      "new ArrayMap< Object, Pair<Object, String> >();"
     * into
     *      "Utils.newMap();"
     */
    public static <K, V> ArrayMap<K, V> newMap() {
        return new ArrayMap<K, V>();
    }


    /**
     * Convenience method for creating an appropriately typed pair, which
     * also reduces the code length when instantiating a new Pair.
     * For example, using this constructor will convertLater
     *      "new Pair< Object, Pair< Object > >(first, second);"
     * into
     *      "Utils.newPair(first, second);"
     */
    public static <F, S> Pair<F, S> newPair(F first, S second){
        return new Pair<F, S>(first, second);
    }


    /**
     * Convenience method for creating an appropriately typed uno, which
     * also reduces the code length when instantiating a new Pair.
     * For example, using this constructor will convertLater
     *      "new Uno< Map< String, Object > >(value);"
     * into
     *      "Utils.newUno(value);"
     */
    public static <T> Uno<T> newUno(T value) {
        return new Uno<T>(value);
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
    }public static DateBuilder newTime() {
        return new DateBuilder();
    }


    public static class DateBuilder {

        private Calendar calendar;

        private DateBuilder(){
            calendar = Calendar.getInstance();
        }

        public Date toNow(){
            return Utils.dateFromCalendar(calendar);
        }

        public Date withHourAndMinute(int hour, int minute){
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            return Utils.dateFromCalendar(calendar);
        }
    }

    public static Date dateFromCalendar(Calendar calendar){
        return calendar.getTime();
    }

    public static Calendar calendarFromDate(Date date){
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c;
    }

    public static String latLngToString(LatLng latLng){
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(new BigDecimal(latLng.latitude).setScale(2, BigDecimal.ROUND_HALF_UP));
        sb.append("; ");
        sb.append(new BigDecimal(latLng.longitude).setScale(2, BigDecimal.ROUND_HALF_UP));
        sb.append(")");
        return sb.toString();
    }


    public static void sortByProximity(List<Taxi> taxis){

    }


    /**
     * Uses a {@link Geocoder} or, if the Geocoding service is unavailable, the
     * Google Maps APIs to convertLater the provided latitude and longitude values into
     * a list of {@link Address}.
     */
    public static List<Address> addressesFromLocation(final Context context, final double lat, final double lng){
        if(geocoder==null)
            geocoder = new Geocoder(context);

        try {
            return geocoder.getFromLocation(lat, lng, 1);
        } catch (IOException ex){
            return lockThreadAndExecute(new Code<List<Address>>() {
                @Override
                public List<Address> execute() {
                    try {


                        String uri = String.format(Locale.ENGLISH,
                                "http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&sensor=true&language="+Locale.getDefault().getCountry(),
                                lat, lng);

                        HttpGet httpGet = new HttpGet(uri);
                        HttpClient client = new DefaultHttpClient();
                        HttpResponse response = client.execute(httpGet);
                        StringBuilder stringBuilder = new StringBuilder();

                        HttpEntity entity = response.getEntity();
                        InputStream stream = entity.getContent();
                        int b;
                        while ((b = stream.read()) != -1) {
                            stringBuilder.append((char) b);
                        }

                        List<Address> toReturn = Lists.newArrayList();
                        try {
                            JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                            if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
                                JSONArray results = jsonObject.getJSONArray("results");
                                for (int i = 0; i < results.length(); i++) {
                                    JSONObject result = results.getJSONObject(i);
                                    String indiStr = result.getString("formatted_address");
                                    Address addr = new Address(Locale.getDefault());
                                    addr.setAddressLine(0, indiStr);
                                    toReturn.add(addr);
                                }
                            }
                        }catch (JSONException exx) {
                            // all JSON mappings that were used exist, so this exception will not occur
                        }

                        return toReturn;

                    }catch (IOException exx) {
                        Log.e("Utils", "addressesFromLocation", exx);
                        return Lists.newArrayList();
                    }
                }
            });
        }
    }


    /**
     * Uses a {@link Geocoder} or, if the Geocoding service is unavailable, the
     * Google Maps APIs to convertLater the provided string into
     * a list of {@link Address}.
     */
    public static List<Address> addressesFromString(final Context context, final String address){
        if(geocoder==null)
            geocoder = new Geocoder(context);

        try {
            return geocoder.getFromLocationName(address, 5);
        } catch (IOException ex) {
            Log.e("Utils", "addressesFromString", ex);
            return Lists.newArrayList();
        }
    }


    /**
     * Uses a {@link Geocoder} or, if the Geocoding service is unavailable, the
     * Google Maps APIs to convertLater the provided string into latitude and longitude
     * values.
     */
    public static LatLng locationFromString(final Context context, final String address){
        if(geocoder==null)
            geocoder = new Geocoder(context);

        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if(addresses.isEmpty()) {
                return null;
            } else {
                Address a = addresses.get(0);
                return new LatLng(a.getLatitude(), a.getLongitude());
            }
        } catch (IOException ex){
            return lockThreadAndExecute(new Code<LatLng>() {
                @Override
                public LatLng execute() {
                    try {

                        HttpGet httpGet = new HttpGet(
                                "http://maps.google.com/maps/api/geocode/json?address="+address+"&ka&sensor=false");

                        HttpClient client = new DefaultHttpClient();
                        HttpResponse response = client.execute(httpGet);
                        StringBuilder stringBuilder = new StringBuilder();


                        HttpEntity entity = response.getEntity();
                        InputStream stream = entity.getContent();
                        int b;
                        while ((b = stream.read()) != -1) {
                            stringBuilder.append((char) b);
                        }


                        try {
                            JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                            double lng = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                                    .getJSONObject("geometry").getJSONObject("location")
                                    .getDouble("lng");

                            double lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                                    .getJSONObject("geometry").getJSONObject("location")
                                    .getDouble("lat");

                            return new LatLng(lat, lng);

                        } catch (JSONException exx){
                            // all JSON mappings that were used exist, so this exception will not occur
                        }
                    } catch (IOException exx) {
                        Log.e("Utils", "locationFromString", exx);
                    }

                    return null;
                }
            });
        }
    }


    /**
     * Converts an {@link Address} into a more presentable String.
     */
    public static String addressToString(final Address address) {
        if(address == null){
            return CommonRes.get().UNKNOWN_ADDRESS;
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



    public interface Code<T> {
        T execute();
    }

    public static <T> T lockThreadAndExecute(Code<T> code){
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        T toReturn = code.execute();
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().build());
        return toReturn;
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
