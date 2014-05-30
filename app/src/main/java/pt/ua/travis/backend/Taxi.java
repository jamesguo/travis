package pt.ua.travis.backend;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

/**
 * Builder and wrapper class for the Taxi entity stored in the Cloud Datastore API.
 * A Taxi is a {@link User} that:
 *      - HAS-ONE current position (frequently updated)
 *      - HAS-MANY ratings
 *      - HAS-MANY performed {@link Ride}
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public final class Taxi extends User {
    public static final String OBJECT_NAME = "Taxi";

    // Datastore column keys (DO NOT CHANGE)
    public static final String AVAILABLE_FLAG = "available";
    public static final String ONLINE_FLAG = "online";
    public static final String CURRENT_LOCATION = "current_location";
    public static final String RATING_AVERAGE = "rating_AVERAGE";
    public static final String RATING_QUANTITY = "ratings_QUANTITY";

    public static Taxi create(){
        ParseUser u = new ParseUser();
        u.isAuthenticated();
        u.put(TYPE, OBJECT_NAME);
        return new Taxi(u);
    }

    Taxi(ParseUser ce) {
        super(ce);
        if(!ce.get(TYPE).equals(OBJECT_NAME)){
            throw new IllegalArgumentException("The specified ParseUser denotes a " +
                    ce.get(TYPE)+". Should denote a "+ OBJECT_NAME +" entity instead.");
        }
    }

    @Override
    public String thisObjectName() {
        return OBJECT_NAME;
    }


    /**
     * Sets this taxi as available by flagging the wrapped {@link ParseUser} parameter
     * "isAvailable" as true.
     */
    public Taxi setAsAvailable(){
        po.put(AVAILABLE_FLAG, Boolean.TRUE);
        return this;
    }


    /**
     * Sets this taxi as unavailable by flagging the wrapped {@link ParseUser} parameter
     * "isAvailable" as false.
     */
    public Taxi setAsUnavailable(){
        po.put(AVAILABLE_FLAG, Boolean.FALSE);
        return this;
    }


    /**
     * Sets this taxi as online by flagging the wrapped {@link ParseUser} parameter
     * "isOnline" as true.
     */
    public Taxi setAsOnline(){
        po.put(ONLINE_FLAG, Boolean.TRUE);
        return this;
    }


    /**
     * Sets this taxi as online by flagging the wrapped {@link ParseUser} parameter
     * "isOnline" as false.
     */
    public Taxi setAsOffline(){
        po.put(ONLINE_FLAG, Boolean.FALSE);
        return this;
    }


    /**
     * Puts the specified latitude and longitude in the wrapped {@link ParseUser}
     * parameter "positionlat" and "positionlng" respectively.
     */
    public Taxi setCurrentLocation(double lat, double lng){
        ParseGeoPoint point = new ParseGeoPoint(lat, lng);
        po.put(CURRENT_LOCATION, point);
        return this;
    }


    /**
     * Adds a new rating to the ratings list in the wrapped {@link ParseUser}
     * parameter "ratings".
     */
    public Taxi addRating(float rating) {
        double oldAverage = po.getDouble(RATING_AVERAGE);
        int quantity = Integer.valueOf(po.getString(RATING_QUANTITY));

        double newAverage = ((oldAverage * quantity) + rating) / (quantity + 1);
        String newAverageString = Double.toString(newAverage);
        String quantityString = Integer.toString(quantity + 1);

        po.put(RATING_AVERAGE, newAverage);
        po.put(RATING_QUANTITY, quantityString);
        return this;
    }


//    /**
//     * Removes a rating from the ratings list in the wrapped {@link ParseUser}
//     * parameter "ratings".
//     */
//    public Taxi removeRating(float rating){
//        ArrayList<Float> ratings = ratingsList();
//        ratings.remove(rating);
//        po.put(RATINGS_LIST, ratings);
//
//        return this;
//    }

    /**
     * Checks if the wrapped {@link ParseUser} contains a "true" or "false" flag that
     * defines this Taxi as available.
     */
    public boolean isAvailable() {
        return po.getBoolean(AVAILABLE_FLAG);
    }


    /**
     * Returns the wrapped {@link ParseUser} current position.
     */
    public LatLng currentPosition(){
        double lat = latitude();
        double lng = longitude();
        return new LatLng(lat, lng);
    }


    /**
     * Returns the wrapped {@link ParseUser} current position's latitude.
     */
    private double latitude() {
        ParseGeoPoint point = po.getParseGeoPoint(CURRENT_LOCATION);
        return point.getLatitude();
    }


    /**
     * Returns the wrapped {@link ParseUser} current position's longitude.
     */
    private double longitude() {
        ParseGeoPoint point = po.getParseGeoPoint(CURRENT_LOCATION);
        return point.getLongitude();
    }

    /**
     * Returns the average of all of the stored ratings in the wrapped {@link ParseUser}.
     */
    public float getRatingAverage() {
        return new Double(po.getDouble(RATING_AVERAGE)).floatValue();
    }

    /**
     * Returns the quantity of users that rated the wrapped {@link ParseUser}.
     */
    public int getRatingQuantity() {
        return Integer.valueOf(po.getString(RATING_QUANTITY));
    }

    @Override
    public String toString() {
        return "< "+Taxi.OBJECT_NAME +
                " | "+super.toString()+
                " | "+ id()+" >";
    }
}
