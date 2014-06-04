package pt.ua.travis.backend;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseObject;
import pt.ua.travis.utils.Utils;

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
public final class Ride extends ParseObjectWrapper {
//    private static final long serialVersionUID = 1L;

    public static final String OBJECT_NAME = "Ride";

    // Parse data column keys (DO NOT CHANGE)
    public static final String CLIENT_ID = "client_id";
    public static final String TAXI_ID = "taxi_id";
    public static final String ORIGIN_LAT = "origin_lat";
    public static final String ORIGIN_LNG = "origin_lng";
    public static final String DESTINATION_LAT = "destination_lat";
    public static final String DESTINATION_LNG = "destination_lng";
    public static final String SCHEDULED_TIME = "scheduled_time";
    public static final String COMPLETED_FLAG = "is_completed";

    private transient Client client;
    private transient Taxi taxi;

    public Ride(){
        super(new ParseObject(OBJECT_NAME));
        po.put(COMPLETED_FLAG, false);
    }

    Ride(ParseObject po, Client client, Taxi taxi){
        super(po);
        this.client = client;
        this.taxi = taxi;
        if(!po.getClassName().equals(OBJECT_NAME)){
            throw new IllegalArgumentException("The specified ParseObject denotes a " +
                    po.getClassName()+". Should denote a "+ OBJECT_NAME +" entity instead.");
        }
        po.put(COMPLETED_FLAG, false);
    }

    @Override
    public String thisObjectName() {
        return OBJECT_NAME;
    }


    /**
     * Puts the specified client that requested the ride in the wrapped
     * {@link ParseObject} parameter "client_object".
     */
    public Ride setClient(Client client) {
        if(client==null)
            return this;

        po.put(CLIENT_ID, client.id());
        this.client = client;
        return this;
    }


    /**
     * Puts the specified taxi that performs the ride in the wrapped
     * {@link ParseObject} parameter "taxi_object".
     */
    public Ride setTaxi(Taxi taxi) {
        if(taxi==null)
            return this;

        po.put(TAXI_ID, taxi.id());
        this.taxi = taxi;
        return this;
    }


    /**
     * Puts the specified latitude and longitude in the wrapped {@link ParseObject}
     * parameter "originlat" and "originlng" respectively.
     */
    public Ride setOriginLocation(double lat, double lng){
        po.put(ORIGIN_LAT, lat);
        po.put(ORIGIN_LNG, lng);
        return this;
    }


    /**
     * Puts the specified latitude and longitude in the wrapped {@link ParseObject}
     * parameter "destinationlat" and "destinationlng" respectively.
     */
    public Ride setDestinationLocation(double lat, double lng){
        po.put(DESTINATION_LAT, lat);
        po.put(DESTINATION_LNG, lng);
        return this;
    }

    /**
     * Puts the specified hour and minute in the wrapped {@link ParseObject} parameters
     * "hour" and "minute" respectively.
     */
    public Ride setScheduledTime(int hour, int minute){
        Date d = Utils.newTime().withHourAndMinute(hour, minute);
        po.put(SCHEDULED_TIME, d);
//        po.put(SCHEDULED_HOUR, hour);
//        po.put(SCHEDULED_MINUTE, minute);
        return this;
    }

    /**
     * Puts the specified hour and minute in the wrapped {@link ParseObject} parameters
     * "hour" and "minute" respectively.
     */
    public Ride setScheduledTime(Date d) {
        po.put(SCHEDULED_TIME, d);
        return this;
    }


    /**
     * Sets this ride as completed by flagging the wrapped {@link ParseObject} parameter
     * "iscompleted" as true.
     */
    public Ride setAsCompleted(){
        po.put(COMPLETED_FLAG, true);
        return this;
    }


    /**
     * Returns the Client object that corresponds to the wrapped {@link ParseObject} client.
     */
    public Client client() {
        return client;
    }


    /**
     * Returns the Taxi object that corresponds to the wrapped {@link ParseObject} taxi.
     */
    public Taxi taxi() {
        return taxi;
    }


    /**
     * Returns the wrapped {@link ParseObject} origin position's latitude.
     */
    private double originLat() {
        return po.getDouble(ORIGIN_LAT);
    }


    /**
     * Returns the wrapped {@link ParseObject} origin position's longitude.
     */
    private double originLng(){
        return po.getDouble(ORIGIN_LNG);
    }


    /**
     * Returns the wrapped {@link ParseObject} destination position's latitude.
     * This value can be null.
     */
    private double destinationLat(){
        return po.getDouble(DESTINATION_LAT);
    }


    /**
     * Returns the wrapped {@link ParseObject} destination position's longitude.
     * This value can be null.
     */
    private double destinationLng(){
        return po.getDouble(DESTINATION_LNG);
    }

    /**
     * Returns the wrapped {@link ParseObject} origin position.
     */
    public LatLng originPosition(){
        double lat = originLat();
        double lng = originLng();
        return new LatLng(lat, lng);
    }


    /**
     * Returns the wrapped {@link ParseObject} destination position.
     */
    public LatLng destinationPosition(){
        double lat = destinationLat();
        double lng = destinationLng();
        return new LatLng(lat, lng);
    }


    /**
     * Returns the wrapped {@link ParseObject} scheduled time as a {@link Calendar}.
     */
    public Calendar getScheduledTime() {
//        int hour = (Integer) po.get(SCHEDULED_HOUR);
//        int minute = (Integer) po.get(SCHEDULED_MINUTE);
        Date d = po.getDate(SCHEDULED_TIME);
        return Utils.calendarFromDate(d);
    }


    /**
     * Returns the remaining time from toNow until the scheduled time for this ride
     * in a String.
     */
    public String getRemaining() {
        Date now = Calendar.getInstance().getTime();

        Calendar scheduledC = getScheduledTime();
        Date scheduled = scheduledC.getTime();

        if (now.compareTo(scheduled) >= 0) {
            return "now!";
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
     * Checks if the wrapped {@link ParseObject} contains a "true" flag that defines this
     * Ride as completed.
     */
    public boolean isCompleted() {
        return po.getBoolean(COMPLETED_FLAG);
    }

    @Override
    public String toString() {

        return "< "+Ride.OBJECT_NAME +
                " | Completed="+isCompleted()+
                " | Client="+client().name()+
                " | Taxi="+taxi().name()+
                " | "+ id()+" >";
    }

//    @Override
//    protected void writeObject(ObjectOutputStream stream) throws IOException {
//        super.writeObject(stream);
//
//        Log.e("++++++++++++++++++++++++++++++++++++++++++++++++++", "++++++++++++++++++++++++++++++++++++++++++++++++++");
//    }
//
//    @Override
//    protected void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
//        super.readObject(stream);
//
//        Log.e("++++++++++++++++++++++++++++++++++++++++++++++++++", "++++++++++++++++++++++++++++++++++++++++++++++++++");
//
//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
//        try {
//            client = new Client(po.getParseObject(CLIENT_ID).fetch());
//            taxi = new Taxi(po.getParseObject(TAXI_ID).fetch());
//        } catch (ParseException ex) {
//            throw new RuntimeException(ex);
//        }
//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().build());
//    }
}
