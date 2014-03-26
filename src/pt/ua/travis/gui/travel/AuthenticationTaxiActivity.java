package pt.ua.travis.gui.travel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.actionbarsherlock.app.SherlockActivity;
import pt.ua.travis.R;
import pt.ua.travis.core.Ride;
import pt.ua.travis.utils.Keys;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class AuthenticationTaxiActivity extends SherlockActivity {

    private Ride thisRide;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authent_taxi_activity);
        getSupportActionBar().hide();

        thisRide = (Ride) getIntent().getSerializableExtra(Keys.SCHEDULED_RIDE);
    }

    public void onAuthentButtonClicked(View view){
        Intent intent = new Intent(this, TravelToDestinationActivity.class);
        intent.putExtra(Keys.SCHEDULED_RIDE, thisRide);
        intent.putExtra(Keys.USER_TYPE, "taxi");
        startActivity(intent);
    }
}