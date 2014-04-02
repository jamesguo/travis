package pt.ua.travis.gui.taxiridesetup;

import android.content.Context;
import android.location.Location;
import org.joda.time.LocalTime;
import pt.ua.travis.core.Ride;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.db.PersistenceManager;
import pt.ua.travis.utils.Tools;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideBuilder {
    private Context context;

    private Taxi selectedTaxi;
    private LocalTime scheduledTime;
    private double origLat, origLng;
    private String origAddress;

    public RideBuilder(Context context){
        this.context = context;
        resetToHereAndNow();
    }

    public void setTaxi(Taxi selectedTaxi){
        this.selectedTaxi = selectedTaxi;
    }

    public void setScheduledTime(LocalTime scheduledTime){
        this.scheduledTime = scheduledTime;
    }

    public void setOrigin(double lat, double lng, String address){
        this.origLat = lat;
        this.origLng = lng;
        this.origAddress = address;
    }

    public void resetToHereAndNow(){
        scheduledTime = LocalTime.now();

        Location currentLocation = Tools.getCurrentLocation(context);
        String currentAddress = Tools.latlngToAddressString(context,
                currentLocation.getLatitude(), currentLocation.getLongitude());

        origLat = currentLocation.getLatitude();
        origLng = currentLocation.getLongitude();
        origAddress = currentAddress;
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if(requestCode==Keys.REQUEST_ORIGIN_COORDS && resultCode==RESULT_OK){
//            origLat = data.getDoubleExtra(Keys.PICKED_POSITION_LAT, 0);
//            origLng = data.getDoubleExtra(Keys.PICKED_POSITION_LNG, 0);
//            origAddress = data.getStringExtra(Keys.PICKED_POSITION_ADDRESS);
//            addressTextView.setText(origAddress);
//        }
//    }

    public Ride build(){
        if(selectedTaxi==null){
            throw new RuntimeException("Tried to build RideBuilder but taxi wasn't set");
        }
        scheduledTime = scheduledTime.plusMinutes(7); // TODO ADD ESTIMATE TIME
        final Ride newRide = new Ride(selectedTaxi, PersistenceManager.selectThisClientAccount(), scheduledTime);
        newRide.originLat = origLat;
        newRide.originLng = origLng;
        newRide.originAddress = origAddress;
        return newRide;
    }
}
