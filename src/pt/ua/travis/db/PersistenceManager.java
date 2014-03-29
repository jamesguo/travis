package pt.ua.travis.db;

import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import org.joda.time.LocalTime;
import pt.ua.travis.core.Client;
import pt.ua.travis.core.Ride;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.core.User;
import pt.ua.travis.utils.Tools;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
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

    private static List<User> users;
    private static List<Ride> rides;




    public static Client selectThisClientAccount() {
        return (Client)users.get(5);
    }

    public static Taxi selectThisTaxiAccount() {
        return (Taxi)users.get(4);
    }

    public static List<Taxi> selectAllTaxis(){
        List<Taxi> selectResult = new ArrayList<Taxi>();

        for(User u : users) {
            if(u instanceof Taxi)
                selectResult.add((Taxi)u);
        }

        return selectResult;
    }


    public static List<User> selectUsersWithPasswordDigest(String passwordDigest) {
        List<User> selectResult = new ArrayList<User>();

        for(User u : users) {
            if(u.passwordDigest.equals(passwordDigest))
                selectResult.add(u);
        }

        return selectResult;
    }

    public static List<Taxi> getFavoritesFromClient(){
        Client c = selectThisClientAccount();
        List<Taxi> taxis = new ArrayList<Taxi>();

        for(Taxi tt : selectAllTaxis()) {
            if (c.favorites.contains(tt.id)) {
                taxis.add(tt);
            }
        }

        return taxis;
    }

    public static void addUser(User u){
        users.add(u);
    }

    public static void addRide(Ride r){
        rides.add(r);
    }

    public static void removeRide(Ride r){
        rides.remove(r);
    }

    public static List<Ride> selectRidesFromClient(){
        Collections.sort(rides, new Comparator<Ride>() {
            @Override
            public int compare(Ride r1, Ride r2) {
                Integer hour1 = r1.scheduledTime.hourOfDay().get();
                Integer hour2 = r2.scheduledTime.hourOfDay().get();
                int hourCompare = hour1.compareTo(hour2);
                Integer minute1 = r1.scheduledTime.minuteOfHour().get();
                Integer minute2 = r2.scheduledTime.minuteOfHour().get();
                int minuteCompare = minute1.compareTo(minute2);

                return hourCompare!=0 ? hourCompare : minuteCompare;
            }
        });
        return rides;
    }

    public static List<Ride> selectRidesFromTaxi(){
        List<Ride> tmp = new ArrayList<Ride>();
        if(rides.size()>1) {
            tmp.add(rides.get(1));
        }
        return tmp;
    }

    static {
        users = new ArrayList<User>();
        Taxi t;
        String password, passwordDigest;


        passwordDigest = Tools.passwordToDigestSHA1("111");
        t = new Taxi("aaa", passwordDigest, "André Figueiredo", "http://placesheen.com/phpthumb/phpthumb.php?src=../uploads/sheen/33.jpeg&w=140&h=180&zc=1");
        t.setPositionFromLatLng(new LatLng(40.646808, -8.662223));
        t.ratings.add(2.5f);
        t.ratings.add(3f);
        t.ratings.add(4f);
        t.ratings.add(2f);
        t.ratings.add(3f);
        t.isAvailable = true;
        users.add(t);


        passwordDigest = Tools.passwordToDigestSHA1("222");
        t = new Taxi("bbb", passwordDigest, "Ernesto Abreu", "http://www.fillmurray.com/140/180");
        t.setPositionFromLatLng(new LatLng(40.635606, -8.659305));
        t.ratings.add(1f);
        t.ratings.add(0.5f);
        t.ratings.add(1f);
        t.ratings.add(2f);
        t.ratings.add(1f);
        t.isAvailable = true;
        users.add(t);


        passwordDigest = Tools.passwordToDigestSHA1("333");
        t = new Taxi("ccc", passwordDigest, "Carlos Oliveira", "http://www.fillmurray.com/g/280/360");
        t.setPositionFromLatLng(new LatLng(40.645831, -8.640680));
        t.ratings.add(3f);
        t.ratings.add(4.5f);
        t.ratings.add(5f);
        t.ratings.add(1.5f);
        t.ratings.add(4f);
        t.ratings.add(3.5f);
        t.ratings.add(4f);
        t.ratings.add(2.5f);
        t.ratings.add(4f);
        t.ratings.add(2f);
        t.isAvailable = true;
        users.add(t);


        passwordDigest = Tools.passwordToDigestSHA1("444");
        Taxi t1 = new Taxi("ddd", passwordDigest, "Duarte Manolo", "http://www.fillmurray.com/280/360");
        t1.setPositionFromLatLng(new LatLng(40.645631, -8.640480));
        t1.ratings.add(4f);
        t1.ratings.add(4f);
        t1.ratings.add(2f);
        t1.ratings.add(3f);
        t1.ratings.add(4f);
        t1.ratings.add(3.5f);
        t1.isAvailable = true;
        users.add(t1);


        passwordDigest = Tools.passwordToDigestSHA1("222");
        t = new Taxi("eee", passwordDigest, "Óscar Cardoso", "http://www.placecage.com/140/180");
        t.setPositionFromLatLng(new LatLng(40.635411, -8.619823));
        t.ratings.add(3f);
        t.isAvailable = false;
        users.add(t);


        passwordDigest = Tools.passwordToDigestSHA1("123");
        Client c = new Client("abc", passwordDigest, "João Martins", "http://www.placecage.com/280/360");
        c.favorites.add(users.get(2).id);
        c.favorites.add(users.get(0).id);
        users.add(c);



        rides = new ArrayList<Ride>();
        LocalTime time = LocalTime.now()
                .withHourOfDay(17)
                .withMinuteOfHour(28);
        rides.add(new Ride(t1, c, time));

        time = LocalTime.now()
                .withHourOfDay(16)
                .withMinuteOfHour(50);
        Ride r = new Ride(t, c, time);
        r.destinationLat = 40.61795936;
        r.destinationLng = -8.642098904;
        rides.add(r);


    }
}
