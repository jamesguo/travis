package pt.ua.travis.backend.entities;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Lists;
import pt.ua.travis.backend.core.CloudEntity;

import java.io.Serializable;
import java.math.BigDecimal;
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
public final class Taxi extends User implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String KIND_NAME = "Taxi";

    public static final String AVAILABLE_FLAG = "available";
    public static final String POSITION_LAT = "positionlat";
    public static final String POSITION_LNG = "positionlng";
    public static final String RATINGS = "ratings";

    public Taxi() {
        super(new CloudEntity(KIND_NAME));
    }

    Taxi(CloudEntity ce){
        super(ce);
        if(!ce.getKindName().equals(KIND_NAME)){
            throw new IllegalArgumentException("The specified CloudEntity denotes a " +
                    ce.getKindName()+". Should denote a "+KIND_NAME+" entity instead.");
        }
    }


    /**
     * Sets this taxi as available by flagging the wrapped {@link CloudEntity} parameter
     * "available" as true.
     */
    public Taxi setAsAvailable(){
        ce.put(AVAILABLE_FLAG, Boolean.TRUE);
        return this;
    }


    /**
     * Sets this taxi as unavailable by flagging the wrapped {@link CloudEntity} parameter
     * "available" as true.
     */
    public Taxi setAsUnavailable(){
        ce.put(AVAILABLE_FLAG, Boolean.FALSE);
        return this;
    }


    /**
     * Puts the specified latitude and longitude in the wrapped {@link CloudEntity}
     * parameter "positionlat" and "positionlng" respectively.
     */
    public Taxi setCurrentPosition(double lat, double lng){
        ce.put(POSITION_LAT, lat);
        ce.put(POSITION_LNG, lng);
        return this;
    }


    /**
     * Puts the specified {@link LatLng} in the wrapped {@link CloudEntity} parameter
     * "positionlat" and "positionlng" respectively.
     */
    public Taxi setCurrentPositionFromLatLng(LatLng latLng){
        ce.put(POSITION_LAT, latLng.latitude);
        ce.put(POSITION_LNG, latLng.longitude);
        return this;
    }


    /**
     * Adds a new rating to the ratings list in the wrapped {@link CloudEntity}
     * parameter "ratings".
     */
    public Taxi addRating(float rating){
        if(rating>=0f && rating<=5)
            return this;

        List<Float> ratings = ratingsList();
        ratings.add(rating);
        ce.put(RATINGS, ratings);

        return this;
    }


//    /**
//     * Removes a rating from the ratings list in the wrapped {@link CloudEntity}
//     * parameter "ratings".
//     */
//    public Taxi removeRating(float rating){
//        if(rating>=0f && rating<=5)
//            return this;
//
//        List<Float> ratings = ratingsList();
//        ratings.remove(rating);
//
//        return this;
//    }

    /**
     * Checks if the wrapped {@link CloudEntity} contains a "true" or "false" flag that
     * defines this Taxi as available.
     */
    public boolean isAvailable(){
        return (Boolean) ce.get(AVAILABLE_FLAG);
    }


    /**
     * Returns the wrapped {@link CloudEntity} current position.
     */
    public LatLng currentPosition(){
        double lat = latitude();
        double lng = longitude();
        return new LatLng(lat, lng);
    }


    /**
     * Returns the wrapped {@link CloudEntity} current position's latitude.
     */
    private double latitude(){
        return (Double) ce.get(POSITION_LAT);
    }


    /**
     * Returns the wrapped {@link CloudEntity} current position's longitude.
     */
    private double longitude(){
        return (Double) ce.get(POSITION_LNG);
    }

    /**
     * Returns the wrapped {@link CloudEntity} current position in a formatted String.
     */
    public String currentPositionToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(new BigDecimal(latitude()).setScale(2, BigDecimal.ROUND_HALF_UP));
        sb.append("; ");
        sb.append(new BigDecimal(longitude()).setScale(2, BigDecimal.ROUND_HALF_UP));
        sb.append(")");
        return sb.toString();
    }

    /**
     * Returns the wrapped {@link CloudEntity} list of stored ratings.
     */
    public List<Float> ratingsList(){
        Object obtainedObject = ce.get(RATINGS);

        if(obtainedObject==null){
            return Lists.newArrayList();
        } else {
            return  (List<Float>) obtainedObject;
        }
    }

    /**
     * Returns the average of all of the stored ratings in the wrapped {@link CloudEntity}.
     */
    public float getRatingAverage() {
        float result = 0;
        List<Float> ratings = ratingsList();

        for(Float rate : ratings){
            result = result + rate;
        }

        return result / ratings.size();
    }
}
