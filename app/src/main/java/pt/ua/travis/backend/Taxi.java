package pt.ua.travis.backend;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Lists;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.Iterator;
import java.util.List;

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
//    private static final long serialVersionUID = 1L;

    public static final String OBJECT_NAME = "Taxi";

    // Datastore column keys (DO NOT CHANGE)
    public static final String AVAILABLE_FLAG = "available";
    //    public static final String POSITION_LAT = "position_lat";
//    public static final String CURRENT_LOCATION = "position_lng";
    public static final String CURRENT_LOCATION = "current_location";
    public static final String RATINGS_LIST = "ratings_list";

    public Taxi() {
        this(new ParseObject(OBJECT_NAME));
    }

    Taxi(ParseObject ce){
        super(ce);
        if(!ce.getClassName().equals(OBJECT_NAME)){
            throw new IllegalArgumentException("The specified ParseObject denotes a " +
                    ce.getClassName()+". Should denote a "+ OBJECT_NAME +" entity instead.");
        }
        setAsUnavailable();
    }

    @Override
    protected String thisObjectName() {
        return OBJECT_NAME;
    }


    /**
     * Sets this taxi as available by flagging the wrapped {@link ParseObject} parameter
     * "available" as true.
     */
    public Taxi setAsAvailable(){
        po.put(AVAILABLE_FLAG, true);
        return this;
    }


    /**
     * Sets this taxi as unavailable by flagging the wrapped {@link ParseObject} parameter
     * "available" as true.
     */
    public Taxi setAsUnavailable(){
        po.put(AVAILABLE_FLAG, false);
        return this;
    }


    /**
     * Puts the specified latitude and longitude in the wrapped {@link ParseObject}
     * parameter "positionlat" and "positionlng" respectively.
     */
    public Taxi setCurrentLocation(double lat, double lng){
        ParseGeoPoint point = new ParseGeoPoint(lat, lng);
        po.put(CURRENT_LOCATION, point);
        return this;
    }


    /**
     * Adds a new rating to the ratings list in the wrapped {@link ParseObject}
     * parameter "ratings".
     */
    public Taxi addRating(float rating){
        List<Float> ratings = ratingsList();

        ratings.add(rating);
        po.put(RATINGS_LIST, ratings);

        return this;
    }


//    /**
//     * Removes a rating from the ratings list in the wrapped {@link ParseObject}
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
     * Checks if the wrapped {@link ParseObject} contains a "true" or "false" flag that
     * defines this Taxi as available.
     */
    public boolean isAvailable(){
        return po.getBoolean(AVAILABLE_FLAG);
    }


    /**
     * Returns the wrapped {@link ParseObject} current position.
     */
    public LatLng currentPosition(){
        double lat = latitude();
        double lng = longitude();
        return new LatLng(lat, lng);
    }


    /**
     * Returns the wrapped {@link ParseObject} current position's latitude.
     */
    private double latitude() {
        ParseGeoPoint point = po.getParseGeoPoint(CURRENT_LOCATION);
        return point.getLatitude();
    }


    /**
     * Returns the wrapped {@link ParseObject} current position's longitude.
     */
    private double longitude() {
        ParseGeoPoint point = po.getParseGeoPoint(CURRENT_LOCATION);
        return point.getLongitude();
    }

    /**
     * Returns the wrapped {@link ParseObject} list of stored ratin//.
     */
    private List<Float> ratingsList(){
        List<Float> list = po.getList(RATINGS_LIST);
        if(list==null) {
            return Lists.newArrayList();
        } else{
            return list;
        }
    }

    /**
     * Returns the average of all of the stored ratings in the wrapped {@link ParseObject}.
     */
    public float getRatingAverage() {
        float result = 0;
        List<Float> ratings = ratingsList();

        for (Iterator<Float> iter = ratings.iterator(); iter.hasNext();) {
            Number n = iter.next();
            result = result + n.floatValue();
        }

        return result / ratings.size();
    }

    @Override
    public String toString() {
        return "< "+Taxi.OBJECT_NAME +
                " | "+super.toString()+
                " | "+ id()+" >";
    }
}
