package pt.ua.travis.db;

import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import pt.ua.travis.core.Account;
import pt.ua.travis.core.Client;
import pt.ua.travis.core.Ride;
import pt.ua.travis.core.Taxi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public final class PersistenceManager {
    private PersistenceManager(){}

    public static List<Account> accounts = new ArrayList<>();

    private static List<Taxi> taxis;
    private static List<Ride> rides = new ArrayList<>();
    private static boolean addedRides = false;


    static {
        accounts.add(new Account(getTaxiAccount(), "taxi", "taxi"));
        accounts.add(new Account(getClientAccount(), "client", "client"));
    }

    public static Client getClientAccount() {
        Client c = new Client("Joao Martins", "http://www.placecage.com/280/360");
        c.setPosition(new LatLng(40.646908, -8.662523));

        List<Taxi> taxis = getTaxisFromDB();
        c.favorites.add(taxis.get(0).id);
        c.favorites.add(taxis.get(3).id);
        return c;
    }

    public static Taxi getTaxiAccount() {
        Taxi t = new Taxi("Oscar Cardoso","http://www.placecage.com/140/180");
        t.setPosition(new LatLng(40.635411, -8.619823));
        return t;
    }

    public static List<Taxi> getTaxiFavorites(){
        Client c = getClientAccount();
        List<Taxi> taxis = new ArrayList<>();

        for(Taxi tt : getTaxisFromDB()) {
            if (c.favorites.contains(tt.id)) {
                taxis.add(tt);
            }
        }

        return taxis;
    }

    public static void addRide(Ride r){
        rides.add(r);
    }

    public static void removeRide(Ride r){
        rides.remove(r);
    }

    public static List<Ride> getRides(){
        if(addedRides) {
            Collections.sort(rides, new Comparator<Ride>() {
                @Override
                public int compare(Ride r1, Ride r2) {
                    return r1.scheduledTime.compareTo(r2.scheduledTime);
                }
            });
            return rides;
        }

        rides.add(new Ride(getTaxisFromDB().get(3), getClientAccount(), "16:30:24"));
        rides.add(new Ride(getTaxisFromDB().get(4), getClientAccount(), "20:00:02"));
        addedRides = true;
        return getRides();
    }

    public static List<Taxi> getTaxisFromDB(){
        if(taxis!=null)
            return taxis;


        taxis = new ArrayList<>();

        Taxi t;

        t = new Taxi("André Figueiredo", "http://placesheen.com/phpthumb/phpthumb.php?src=../uploads/sheen/33.jpeg&w=140&h=180&zc=1", new ArrayList<Float>());
        t.setPosition(new LatLng(40.646808, -8.662223));
        t.addRating(2.5);
        t.addRating(3);
        t.addRating(4);
        t.addRating(2);
        t.addRating(3);
        t.isAvailable = true;
        taxis.add(t);

        t = new Taxi("Ernesto Abreu", "", new ArrayList<Float>());
        t.setPosition(new LatLng(40.635606, -8.659305));
        t.addRating(1);
        t.addRating(0.5);
        t.addRating(1);
        t.addRating(2);
        t.addRating(1);
        t.isAvailable = true;
        taxis.add(t);

        t = new Taxi("Carlos Oliveira", "http://www.fillmurray.com/g/280/360", new ArrayList<Float>());
        t.setPosition(new LatLng(40.645831, -8.640680));
        t.addRating(3);
        t.addRating(4.5);
        t.addRating(5);
        t.addRating(1.5);
        t.addRating(4);
        t.addRating(3.5);
        t.addRating(4);
        t.addRating(2.5);
        t.addRating(4);
        t.addRating(2);
        t.isAvailable = true;
        taxis.add(t);

        t = new Taxi("Duarte Manolo", "http://www.fillmurray.com/280/360", new ArrayList<Float>());
        t.setPosition(new LatLng(40.645631, -8.640480));
        t.addRating(4);
        t.addRating(4);
        t.addRating(2);
        t.addRating(3);
        t.addRating(4);
        t.addRating(3.5);
        t.isAvailable = true;
        taxis.add(t);

        t = new Taxi("Óscar Cardoso", "http://www.placecage.com/140/180");
        t.setPosition(new LatLng(40.635411, -8.619823));
        t.addRating(3);
        t.isAvailable = false;
        taxis.add(t);

        return taxis;
    }
}
