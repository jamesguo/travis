package pt.ua.travis.utils;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public final class CommonKeys {
    private CommonKeys(){}

    public static final String DESTROY_THIS = "destroy_this";

    public static final String SELECTED_INDEX = "selected_index";

    public static final String PICKED_POSITION_LAT = "picked_position_lat";
    public static final String PICKED_POSITION_LNG = "picked_position_lng";
    public static final String PICKED_POSITION_ADDRESS = "picked_position_address";

    public static final String SAVED_TAXI_OBJECT_ID = "saved_taxi_object_id";
    public static final String SAVED_CLIENT_OBJECT_ID = "saved_client_object_id";
    public static final String SAVED_RIDE_OBJECT_ID = "saved_ride_object_id";

    public static final String SELECTED_TAXI_ID = "selected_taxi_id";
    public static final String SCHEDULED_RIDE_ID = "scheduled_ride_id";


    public static final int REQUEST_ORIGIN_COORDS = 10111;
    public static final int REQUEST_DESTINATION_COORDS = 10100;
    public static final int WAIT_FOR_DESTINATION = 10100;


    public static final String GO_TO_RIDE_LIST = "go_to_ride_list";


    public static final String USER_TYPE = "user_type";

    public static final String NEW_REQUEST_ACCEPTED_DURING_TRAVEL = "new_request_accepted_during_travel";
}
