package pt.ua.travis.ui.travel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.actionbarsherlock.app.SherlockActivity;
import pt.ua.travis.R;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Ride;
import pt.ua.travis.utils.CommonKeys;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class AuthenticationTaxiActivity extends SherlockActivity {

    private Ride thisRide;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authent_taxi);
        getSupportActionBar().hide();

        final String rideID = getIntent().getStringExtra(CommonKeys.RIDE_REQUEST_DECLINED_ID);
        thisRide = PersistenceManager.getFromCache(rideID);

    }

    public void onAuthentButtonClicked(View view){
        Intent intent = new Intent(this, TravelToDestinationActivity.class);
        intent.putExtra(CommonKeys.RIDE_REQUEST_DECLINED_ID, thisRide.id());
        intent.putExtra(CommonKeys.USER_TYPE, "taxi");
        startActivity(intent);
    }
}