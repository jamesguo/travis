package pt.ua.travis.ui.riderequest;

import com.google.android.gms.maps.model.LatLng;
import pt.ua.travis.backend.Client;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Ride;
import pt.ua.travis.backend.Taxi;
import pt.ua.travis.core.TravisApplication;

import java.util.Calendar;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideBuilder {

    private Taxi selectedTaxi;
    private Calendar c;
    private LatLng origPos;

    public RideBuilder(TravisApplication contextApp){
        resetToHereAndNow(contextApp);
    }

    public void setTaxi(Taxi selectedTaxi){
        this.selectedTaxi = selectedTaxi;
    }

    public void setScheduledTime(int hour, int minute){
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
    }

    public void setOrigin(LatLng origPos) {
        this.origPos = origPos;
    }

    public void resetToHereAndNow(TravisApplication contextApp) {
        this.c = Calendar.getInstance();
        this.origPos = contextApp.getCurrentLocation();
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
            throw new RuntimeException("Tried to build RideBuilder but Taxi wasn't set");
        }
        c.add(Calendar.MINUTE, 7); // TODO ADD ESTIMATE TIME
        final Ride newRide = new Ride();
        newRide.setTaxi(selectedTaxi);
        Client thisClient = PersistenceManager.getCurrentlyLoggedInUser();
        newRide.setClient(thisClient);
        newRide.setScheduledTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
        newRide.setOriginLocation(origPos.latitude, origPos.longitude);
        return newRide;
    }
}
