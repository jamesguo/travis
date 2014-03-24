package pt.ua.travis.utils;

import android.app.Activity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public final class Keys {
    private Keys(){}

    public static final String SELECTED_INDEX = "selected_index";

    public static final String SELECTED_TAXI = "selected_taxi";

    public static final String SCHEDULED_RIDE = "scheduled_ride";

    public static final String PICKED_POSITION_LAT = "picked_position_lat";
    public static final String PICKED_POSITION_LNG = "picked_position_lng";
    public static final String PICKED_POSITION_ADDRESS = "picked_position_address";

    public static final String SAVED_TAXI_OBJECT = "saved_taxi_object";
    public static final String SAVED_CLIENT_OBJECT = "saved_taxi_object";
    public static final String SAVED_RIDE_OBJECT = "saved_ride_object";


    public static final int REQUEST_COORDS = 10100;


    public static ImageLoader loader;

    public static ImageLoader getLoader(Activity a){
        if(loader==null) {
            loader = ImageLoader.getInstance();
            loader.init(ImageLoaderConfiguration.createDefault(a));
        }
        return loader;
    }


}
