package pt.ua.travis.backend;

import com.google.android.gms.maps.model.LatLng;
import pt.ua.travis.backend.entities.Taxi;

import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public final class Geolocation {

    private static double CURRENT_LAT;
    private static double CURRENT_LNG;

    private Geolocation(){}

    public static void sortByProximity(List<Taxi> taxis){


    }

    public static void setCurrentLocation(double lat, double lng){
        CURRENT_LAT = lat;
        CURRENT_LNG = lng;
    }

    public static LatLng getCurrentPosition(){
        return new LatLng(CURRENT_LAT, CURRENT_LNG);
    }
}
