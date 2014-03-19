package pt.ua.travis.core;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Time;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TravisCommit extends TravisObject {

    private final TravisTaxi taxi;
    private final TravisClient client;
    private final LatLng destination;
    private final Time scheduledTime;

    /**
     * Creates a commit for a taxi to get the client at the assigned time and
     * to take him to his destination.
     *
     * @param taxi the taxi that is assigned for this commit
     * @param client the client that arranged this commit
     * @param timeString A String representing the time value in JDBC escape
     *                   format: {@code hh:mm:ss}.
     */
    public TravisCommit(final TravisTaxi taxi,
                        final TravisClient client,
                        final LatLng destination,
                        final String timeString){
        super();
        this.taxi = taxi;
        this.client = client;
        this.destination = destination;
        this.scheduledTime = Time.valueOf(timeString);
    }

    public TravisTaxi getTaxi() {
        return taxi;
    }

    public TravisClient getClient() {
        return client;
    }

    public LatLng getDestination() {
        return destination;
    }

    public Time getScheduledTime() {
        return scheduledTime;
    }
}
