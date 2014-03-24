package pt.ua.travis.core;

import android.util.Log;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.io.Serializable;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class Ride extends TravisObject implements Serializable {

    public final Taxi taxi;
    public final Client client;
    public double destinationLat;
    public double destinationLng;
    public String destinationAddress;
    public final LocalTime scheduledTime;

    /**
     * Creates a ride performed by a taxi to get the client at the assigned
     * time and to take him to his destination.
     *
     * @param taxi the taxi that is assigned for this commit
     * @param client the client that arranged this commit
     * @param timeString the time when the taxi will arrive at
     *                   the client's position, in "HH:MM:SS" formatted string.
     */
    public Ride(final Taxi taxi,
                final Client client,
                final String timeString){
        this(taxi, client, LocalTime.parse(timeString));
    }

    /**
     * Creates a ride performed by a taxi to get the client at the assigned
     * time and to take him to his destination.
     *
     * @param taxi the taxi that is assigned for this commit
     * @param client the client that arranged this commit
     * @param scheduledTime the time when the taxi will arrive at
     *                      the client's position.
     */
    public Ride(final Taxi taxi,
                final Client client,
                final LocalTime scheduledTime){
        super();
        this.taxi = taxi;
        this.client = client;
        this.scheduledTime = scheduledTime;
    }

    /**
     * Gets the remaining time from now until the scheduled time for this ride.
     * @return the remaining time in string format
     */
    public String getRemaining(){
        LocalTime startDate = LocalTime.now();

        Period period = new Period(startDate, scheduledTime, PeriodType.dayTime());

        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendHours().appendSuffix(" hour ", " hours ")
                .appendMinutes().appendSuffix(" minute ", " minutes ")
                .toFormatter();

        Log.e("TTTTTTOOOOOOOPPP", scheduledTime.toString());
        Log.e("TTTTTTOOOOOOOPPP", formatter.print(period));
        return "in "+formatter.print(period);
    }
}
