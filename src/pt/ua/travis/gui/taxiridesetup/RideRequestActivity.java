package pt.ua.travis.gui.taxiridesetup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import pt.ua.travis.R;
import pt.ua.travis.core.Ride;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.db.PersistenceManager;
import pt.ua.travis.utils.Keys;
import pt.ua.travis.utils.Returner;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideRequestActivity extends SherlockFragmentActivity {

    private static final int MATCH_PARENT = LinearLayout.LayoutParams.MATCH_PARENT;

    private Taxi selectedTaxi;
    private LocalTime scheduledTime;
    private double lat, lng;
    private String  destinationAddress;
    private FrameLayout buttonsContainer, optionsContainer;
    private RideRequestOptionsFragment optionsFragment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ride_setup);

        Bundle extras = getIntent().getExtras();
        selectedTaxi = (Taxi) extras.getSerializable(Keys.SELECTED_TAXI);
        final int selectedIndex = extras.getInt(Keys.SELECTED_INDEX);

        buttonsContainer = (FrameLayout) findViewById(R.id.buttons_fragment_container);
        optionsContainer = (FrameLayout) findViewById(R.id.options_fragment_container);

        //getSupportFragmentManager()
        //        .beginTransaction()
         //       .add(R.id.buttons_fragment_container, new TitlesFragment())
          //      .commit();

        optionsFragment = new RideRequestOptionsFragment();

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            public void onBackStackChanged() {
                setLayout();
            }
        });
    }

    public void onHereAndNowClicked(View view){
        // creates a new ride whose time is the taxi's elapsed time of arriving at the target
        LocalTime time = LocalTime.parse("00:07:00");
        scheduledTime = LocalTime.now().plus(new Period(time));

        // --- TODO GET CURRENT USER POSITION AND ADDRESS ---
        lat = 0;
        lng = 0;
        destinationAddress = "";
        // --------------------------------------------------

        buildAndRequestRide();
    }


    public void onDestinationButtonClicked(View view){
        Intent intent = new Intent(this, AddressPickerActivity.class);
        startActivityForResult(intent, Keys.REQUEST_COORDS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Keys.REQUEST_COORDS && resultCode==RESULT_OK){
            lat = data.getDoubleExtra(Keys.PICKED_POSITION_LAT, 0);
            lng = data.getDoubleExtra(Keys.PICKED_POSITION_LNG, 0);
            destinationAddress = data.getStringExtra(Keys.PICKED_POSITION_ADDRESS);
        }
    }

    public void onLaterDoneClicked(View view){

    }

    public void buildAndRequestRide(){
        final Ride newRide = new Ride(selectedTaxi, PersistenceManager.getClientAccount(), scheduledTime);
        newRide.destinationLat = lat;
        newRide.destinationLng = lng;
        newRide.destinationAddress = destinationAddress;


        new RideRequestTask(RideRequestActivity.this, newRide, new Returner() {
            @Override
            public void onResult(int result) {
                if (result == RideRequestTask.OK_RESULT) {
                    PersistenceManager.addRide(newRide);

                    Intent intent = new Intent(
                            RideRequestActivity.this,
                            WaitForTaxiActivity.class);
                    intent.putExtra(Keys.SCHEDULED_RIDE, newRide);
                    startActivity(intent);
                } else if(result == RideRequestTask.CANCEL_RESULT) {
                    // TODO THE TAXI DENIED THE REQUEST
                }
            }
        }).execute();
    }

    private void setLayout() {
        if (!optionsFragment.isAdded()) {
            buttonsContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    MATCH_PARENT, MATCH_PARENT));
            optionsContainer.setLayoutParams(new LinearLayout.LayoutParams(0,
                    MATCH_PARENT));
        } else {
            buttonsContainer.setLayoutParams(new LinearLayout.LayoutParams(0,
                    MATCH_PARENT, 1f));
            optionsContainer.setLayoutParams(new LinearLayout.LayoutParams(0,
                    MATCH_PARENT, 2f));
        }
    }
}