package pt.ua.travis.db;

import com.google.android.gms.maps.model.LatLng;
import pt.ua.travis.core.Client;
import pt.ua.travis.core.Taxi;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public final class TravisDB {
    private TravisDB(){}

    public static Client getClientAccount() {
        Client c = new Client("João Martins", "");
        c.setPosition(new LatLng(40.646908, -8.662523));
        return c;
    }

    public static Taxi getTaxiAccount() {
        Taxi t = new Taxi("Óscar Cardoso", "");
        t.setPosition(new LatLng(40.646008, -8.661923));
        return t;
    }

    public static List<Taxi> getTaxiFavorites(){
        List<Taxi> taxis = getTaxisFromDB();
        taxis.remove(0);
        taxis.remove(1);
        taxis.remove(1);

        return taxis;
    }

    public static List<Taxi> getTaxisFromDB(){
        List<Taxi> taxis = new ArrayList<>();

        Taxi t;

        t = new Taxi("Text #1", "", new ArrayList<Float>());
        t.setPosition(new LatLng(40.646808, -8.662223));
        t.addRating(2.5);
        t.addRating(3);
        t.addRating(4);
        t.addRating(2);
        t.addRating(3);
        t.setAvailable(true);
        taxis.add(t);

        t = new Taxi("Text #2", "", new ArrayList<Float>());
        t.setPosition(new LatLng(40.635606, -8.659305));
        t.addRating(1);
        t.addRating(0.5);
        t.addRating(1);
        t.addRating(2);
        t.addRating(1);
        t.setAvailable(true);
        taxis.add(t);

        t = new Taxi("Text #3", "", new ArrayList<Float>());
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
        t.setAvailable(true);
        taxis.add(t);

        t = new Taxi("Text #5", "", new ArrayList<Float>());
        t.setPosition(new LatLng(40.645631, -8.640480));
        t.addRating(4);
        t.addRating(4);
        t.addRating(2);
        t.addRating(3);
        t.addRating(4);
        t.addRating(3.5);
        t.setAvailable(true);
        taxis.add(t);

        t = new Taxi("Text #4", "", new ArrayList<Float>());
        t.setPosition(new LatLng(40.635411, -8.619823));
        t.addRating(3);
        t.setAvailable(false);
        taxis.add(t);

        return taxis;
    }
}
