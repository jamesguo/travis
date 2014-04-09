package pt.ua.travis.gui.taxiridesetup;

import com.google.android.gms.maps.model.LatLng;
import pt.ua.travis.backend.entities.CloudBackendManager;
import pt.ua.travis.backend.entities.Ride;
import pt.ua.travis.backend.entities.Taxi;
import pt.ua.travis.backend.Geolocation;
import pt.ua.travis.utils.Utils;

import java.util.Calendar;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideBuilder {

    private Taxi selectedTaxi;
    private Calendar scheduledTime;
    private LatLng origPos;

    public RideBuilder(){
        resetToHereAndNow();
    }

    public void setTaxi(Taxi selectedTaxi){
        this.selectedTaxi = selectedTaxi;
    }

    public void setScheduledTime(Calendar scheduledTime){
        this.scheduledTime = scheduledTime;
    }

    public void setOrigin(LatLng origPos) {
        this.origPos = origPos;
    }

    public void resetToHereAndNow() {
        scheduledTime = Utils.newTime().toNow();

        origPos = Geolocation.getCurrentPosition();
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
        scheduledTime.add(Calendar.MINUTE, 7); // TODO ADD ESTIMATE TIME
        final Ride newRide = new Ride();
        newRide.setTaxi(selectedTaxi);
        newRide.setClient(CloudBackendManager.select().clients().loggedInThisDevice());
        newRide.setScheduledTime(scheduledTime);
        newRide.setOriginPositionFromLatLng(origPos);
        return newRide;
    }
}
