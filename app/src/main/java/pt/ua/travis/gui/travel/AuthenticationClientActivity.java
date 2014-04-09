package pt.ua.travis.gui.travel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.actionbarsherlock.app.SherlockActivity;
import pt.ua.travis.R;
import pt.ua.travis.backend.entities.Ride;
import pt.ua.travis.utils.CommonKeys;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class AuthenticationClientActivity extends SherlockActivity {

    private Ride thisRide;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authent_client_activity);
        getSupportActionBar().hide();

        thisRide = (Ride) getIntent().getSerializableExtra(CommonKeys.SCHEDULED_RIDE);
    }

    public void onAuthentButtonClicked(View view){
        Intent intent = new Intent(this, TravelToDestinationActivity.class);
        intent.putExtra(CommonKeys.SCHEDULED_RIDE, thisRide);
        intent.putExtra(CommonKeys.USER_TYPE, "client");
        startActivity(intent);
    }
}