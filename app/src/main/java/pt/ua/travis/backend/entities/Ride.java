package pt.ua.travis.backend.entities;

import com.google.android.gms.maps.model.LatLng;
import pt.ua.travis.backend.core.CloudEntity;
import pt.ua.travis.utils.Utils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * Builder and wrapper class for the Taxi entity stored in the Cloud Datastore API.
 * Ride relationships are defined as follows:
 *      - HAS-ONE {@link Taxi} that performs the ride
 *      - HAS-ONE {@link Client} that requests the ride
 *      - HAS-ONE origin position
 *      - HAS-ONE destination position (OPTIONAL)
 *      - HAS-MANY performed {@link Ride}
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public final class Ride extends CloudEntityWrapper implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String KIND_NAME = "Ride";

    public static final String TAXI = "taxi";
    public static final String CLIENT = "client";
    public static final String ORIGIN_LAT = "originlat";
    public static final String ORIGIN_LNG = "originlng";
    public static final String DESTINATION_LAT = "destinationlat";
    public static final String DESTINATION_LNG = "destinationlng";
    public static final String SCHEDULED_HOUR = "scheduledhour";
    public static final String SCHEDULED_MINUTE = "scheduledminute";
    public static final String COMPLETED_FLAG = "iscompleted";


    public Ride(){
        super(new CloudEntity(KIND_NAME));
    }

    protected Ride(CloudEntity ce){
        super(ce);
        if(!ce.getKindName().equals(KIND_NAME)){
            throw new IllegalArgumentException("The specified CloudEntity denotes a " +
                    ce.getKindName()+". Should denote a "+KIND_NAME+" entity instead.");
        }
    }

    public boolean isCompleted;


    /**
     * Puts the specified client that requested the ride in the wrapped
     * {@link CloudEntity} parameter "client".
     */
    public Ride setClient(Client client) {
        if(client==null)
            return this;

        ce.put(CLIENT, client.getId());
        return this;
    }


    /**
     * Puts the specified taxi that performs the ride in the wrapped
     * {@link CloudEntity} parameter "taxi".
     */
    public Ride setTaxi(Taxi taxi) {
        if(taxi==null)
            return this;

        ce.put(TAXI, taxi.getId());
        return this;
    }


    /**
     * Puts the specified latitude and longitude in the wrapped {@link CloudEntity}
     * parameter "originlat" and "originlng" respectively.
     */
    public Ride setOriginPosition(double lat, double lng){
        ce.put(ORIGIN_LAT, lat);
        ce.put(ORIGIN_LNG, lng);
        return this;
    }


    /**
     * Puts the specified {@link LatLng} in the wrapped {@link CloudEntity} parameter
     * "originlat" and "originlng" respectively.
     */
    public Ride setOriginPositionFromLatLng(LatLng latLng){
        ce.put(ORIGIN_LAT, latLng.latitude);
        ce.put(ORIGIN_LNG, latLng.longitude);
        return this;
    }


    /**
     * Puts the specified latitude and longitude in the wrapped {@link CloudEntity}
     * parameter "destinationlat" and "destinationlng" respectively.
     */
    public Ride setDestinationPosition(double lat, double lng){
        ce.put(DESTINATION_LAT, lat);
        ce.put(DESTINATION_LNG, lng);
        return this;
    }


    /**
     * Puts the specified {@link LatLng} in the wrapped {@link CloudEntity} parameter
     * "destinationlat" and "destinationlng" respectively.
     */
    public Ride setDestinationPositionFromLatLng(LatLng latLng){
        ce.put(DESTINATION_LAT, latLng.latitude);
        ce.put(DESTINATION_LNG, latLng.longitude);
        return this;
    }

    /**
     * Puts the specified hour and minute in the wrapped {@link CloudEntity} parameters
     * "hour" and "minute" respectively.
     */
    public Ride setScheduledTime(int hour, int minute){
        ce.put(SCHEDULED_HOUR, hour);
        ce.put(SCHEDULED_MINUTE, minute);
        return this;
    }

    /**
     * Puts the specified hour and minute in the wrapped {@link CloudEntity} parameters
     * "hour" and "minute" respectively.
     */
    public Ride setScheduledTime(Calendar time) {
        int hour = time.get(Calendar.HOUR_OF_DAY);
        int minute = time.get(Calendar.MINUTE);

        return setScheduledTime(hour, minute);
    }


    /**
     * Sets this ride as completed by flagging the wrapped {@link CloudEntity} parameter
     * "iscompleted" as true.
     */
    public Ride setAsCompleted(){
        ce.put(COMPLETED_FLAG, Boolean.TRUE);
        return this;
    }


    /**
     * Returns the Client object that corresponds to the wrapped {@link CloudEntity} client id.
     */
    public Client client() {
        return CloudBackendManager.select().clients().from(this);
    }


    /**
     * Returns the Taxi object that corresponds to the wrapped {@link CloudEntity} taxi id.
     */
    public Taxi taxi() {
        return CloudBackendManager.select().taxis().from(this);
    }


    /**
     * Returns the wrapped {@link CloudEntity} origin position's latitude.
     */
    private double originLat(){
        return (Double) ce.get(ORIGIN_LAT);
    }


    /**
     * Returns the wrapped {@link CloudEntity} origin position's longitude.
     */
    private double originLng(){
        return (Double) ce.get(ORIGIN_LNG);
    }


    /**
     * Returns the wrapped {@link CloudEntity} destination position's latitude.
     * This value can be null.
     */
    private double destinationLat(){
        Object obtainedObject = ce.get(DESTINATION_LAT);

        if(obtainedObject == null){
            return 0;
        }
        return (Double) obtainedObject;
    }


    /**
     * Returns the wrapped {@link CloudEntity} destination position's longitude.
     * This value can be null.
     */
    private double destinationLng(){
        Object obtainedObject = ce.get(DESTINATION_LNG);

        if(obtainedObject == null){
            return 0;
        }
        return (Double) obtainedObject;
    }

    /**
     * Returns the wrapped {@link CloudEntity} origin position.
     */
    public LatLng originPosition(){
        double lat = originLat();
        double lng = originLng();
        return new LatLng(lat, lng);
    }


    /**
     * Returns the wrapped {@link CloudEntity} destination position.
     */
    public LatLng destinationPosition(){
        double lat = destinationLat();
        double lng = destinationLng();
        return new LatLng(lat, lng);
    }


    /**
     * Returns the wrapped {@link CloudEntity} origin position in a formatted String.
     */
    public String originPositionToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(new BigDecimal(originLat()).setScale(2, BigDecimal.ROUND_HALF_UP));
        sb.append("; ");
        sb.append(new BigDecimal(originLng()).setScale(2, BigDecimal.ROUND_HALF_UP));
        sb.append(")");
        return sb.toString();
    }


    /**
     * Returns the wrapped {@link CloudEntity} destination position in a formatted String.
     */
    public String destinationPositionToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(new BigDecimal(destinationLat()).setScale(2, BigDecimal.ROUND_HALF_UP));
        sb.append("; ");
        sb.append(new BigDecimal(destinationLng()).setScale(2, BigDecimal.ROUND_HALF_UP));
        sb.append(")");
        return sb.toString();
    }


    /**
     * Returns the wrapped {@link CloudEntity} scheduled time as a {@link Calendar}.
     * The date is set to 1-1-1, only the HOUR_OF_DAY and MINUTE fields are used.
     */
    public Calendar getScheduledTime() {
        int hour = (Integer) ce.get(SCHEDULED_HOUR);
        int minute = (Integer) ce.get(SCHEDULED_MINUTE);

        return Utils.newTime().withHourOfDay(hour).andWithMinute(minute);
    }


    /**
     * Returns the remaining time from toNow until the scheduled time for this ride
     * in a String.
     */
    public String getRemaining() {
        Calendar calendar = Utils.newTime().toNow();
        Date now = calendar.getTime();

        Calendar scheduledC = getScheduledTime();
        Date scheduled = scheduledC.getTime();

        if (now.compareTo(scheduled) >= 0) {
            return "toNow!";
        }

        int hour = scheduledC.get(Calendar.HOUR_OF_DAY);
        int minute = scheduledC.get(Calendar.MINUTE);

        StringBuilder sb = new StringBuilder();
        sb.append("in ");
        sb.append(hour);
        sb.append(" hour");
        if (hour != 1) {
            sb.append("s");
        }
        sb.append(" and ");
        sb.append(minute);
        sb.append(" minute");
        if (minute != 1) {
            sb.append("s");
        }

        return sb.toString();
    }


    /**
     * Checks if the wrapped {@link CloudEntity} contains a "true" flag that defines this
     * Ride as completed.
     */
    public boolean isCompleted() {
        Object obtainedObject = ce.get(COMPLETED_FLAG);

        if(obtainedObject == null)
            return false;

        if(!(obtainedObject instanceof Boolean)){
            return false;
        }

        return (Boolean)obtainedObject;
    }
}
