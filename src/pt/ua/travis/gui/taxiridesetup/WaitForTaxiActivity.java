package pt.ua.travis.gui.taxiridesetup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.beardedhen.androidbootstrap.BootstrapButton;
import pt.ua.travis.R;
import pt.ua.travis.core.Ride;
import pt.ua.travis.gui.main.MainClientActivity;
import pt.ua.travis.gui.ridelist.RideItem;
import pt.ua.travis.utils.Keys;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class WaitForTaxiActivity extends SherlockFragmentActivity {

    private Ride newRide;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newRide = (Ride) getIntent().getSerializableExtra(Keys.SCHEDULED_RIDE);

        refreshActivity(false);
    }

    private void refreshActivity(boolean hasDestination){
        setContentView(R.layout.wait_for_taxi);

        BootstrapButton btDestination = (BootstrapButton) findViewById(R.id.btDestination);
        if(hasDestination)
            btDestination.setBootstrapType("default");

        RideItem rideItem = RideItem.newInstance(newRide);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.ride_item_container, rideItem)
                .commit();
    }

    public void onDestinationButtonClicked(View view){
        Intent intent = new Intent(this, AddressPickerActivity.class);
        startActivityForResult(intent, Keys.REQUEST_COORDS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Keys.REQUEST_COORDS && resultCode==RESULT_OK){
            double lat = data.getDoubleExtra(Keys.PICKED_POSITION_LAT, 0);
            double lng = data.getDoubleExtra(Keys.PICKED_POSITION_LNG, 0);
            String address = data.getStringExtra(Keys.PICKED_POSITION_ADDRESS);

            if((lat==0 && lng!=0) || (lat!=0 && lng==0) || (lat!=0 && lng!=0)) {
                newRide.destinationLat = lat;
                newRide.destinationLng = lng;
                newRide.destinationAddress = address;

                refreshActivity(true);
                // TODO SET THIS CHANGE IN THE DATABASE
            }
        }
    }

    public void onAnotherTaxiButtonClicked(View view){
        Intent intent = new Intent(this, MainClientActivity.class);
        startActivity(intent);
    }
}