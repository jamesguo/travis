package pt.ua.travis.gui.taxiridesetup;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.beardedhen.androidbootstrap.BootstrapButton;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import pt.ua.travis.R;
import pt.ua.travis.core.Ride;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.db.PersistenceManager;
import pt.ua.travis.gui.addresspicker.AddressPickerActivity;
import pt.ua.travis.utils.Keys;
import pt.ua.travis.utils.Returner;
import pt.ua.travis.utils.Tools;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideRequestActivity extends SherlockFragmentActivity {

    private static final int MATCH_PARENT = LinearLayout.LayoutParams.MATCH_PARENT;

    private Taxi selectedTaxi;
    private LocalTime scheduledTime;
    private double origLat, origLng, destLat, destLng;
    private String origAddress, destAddress;
    private RideRequestViewPager pager;
    private TextView addressTextView;
    private BootstrapButton doneButton, cancelButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ride_setup);

        Bundle extras = getIntent().getExtras();
        selectedTaxi = (Taxi) extras.getSerializable(Keys.SELECTED_TAXI);
        final int selectedIndex = extras.getInt(Keys.SELECTED_INDEX);

        pager = (RideRequestViewPager) findViewById(R.id.container);
        pager.setPageTransformer(false, new SlidePageTransformer(pager));
        pager.setAdapter(new RideRequestPagerAdapter(getSupportFragmentManager()));
        pager.setOffscreenPageLimit(1);

//        buttonsContainer = (FrameLayout) findViewById(R.id.buttons_fragment_container);
//        optionsContainer = (FrameLayout) findViewById(R.id.options_fragment_container);

//        getSupportFragmentManager()
//                .beginTransaction()
//                .add(R.id.buttons_fragment_container, new RideRequestButtonsFragment())
//                .commit();
//
//        optionsFragment = new RideRequestOptionsFragment();
    }

    public void onHereAndNowButtonClicked(View view){
        // creates a new ride whose time is the taxi's elapsed time of arriving at the target
        scheduledTime = LocalTime.now();

        Location currentLocation = Tools.getCurrentLocation(this);
        String currentAddress = Tools.latlngToAddressString(this,
                currentLocation.getLatitude(), currentLocation.getLongitude());

        origLat = currentLocation.getLatitude();
        origLng = currentLocation.getLongitude();
        origAddress = currentAddress;

        destLat = 0;
        destLng = 0;
        destAddress = "";

        buildAndRequestRide();
    }

    public void onLaterButtonClicked(View view){
        Location currentLocation = Tools.getCurrentLocation(this);
        String currentAddress = Tools.latlngToAddressString(this,
                currentLocation.getLatitude(), currentLocation.getLongitude());

        pager.setCurrentItem(1, true);
        addressTextView = (TextView) findViewById(R.id.origin_address);
        addressTextView.setText(currentAddress);

        TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        LocalTime now = LocalTime.now();
        timePicker.setCurrentHour(now.getHourOfDay());
        timePicker.setCurrentMinute(now.minuteOfHour().get());
    }

    public void onDoneButtonClicked(View view){
        TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
        scheduledTime = new LocalTime(DateTimeZone.forID("GMT"))
                .withHourOfDay(timePicker.getCurrentHour())
                .withMinuteOfHour(timePicker.getCurrentMinute());
        buildAndRequestRide();
    }

    public void onCancelButtonClicked(View view){
        pager.setCurrentItem(0, true);
    }

    public void onOriginButtonClicked(View view){
        Intent intent = new Intent(this, AddressPickerActivity.class);
        startActivityForResult(intent, Keys.REQUEST_ORIGIN_COORDS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Keys.REQUEST_ORIGIN_COORDS && resultCode==RESULT_OK){
            origLat = data.getDoubleExtra(Keys.PICKED_POSITION_LAT, 0);
            origLng = data.getDoubleExtra(Keys.PICKED_POSITION_LNG, 0);
            origAddress = data.getStringExtra(Keys.PICKED_POSITION_ADDRESS);
            addressTextView.setText(origAddress);
        }
    }

    public void buildAndRequestRide(){
        scheduledTime = scheduledTime.plusMinutes(7); // TODO ADD ESTIMATE TIME
        final Ride newRide = new Ride(selectedTaxi, PersistenceManager.selectThisClientAccount(), scheduledTime);
        newRide.originLat = origLat;
        newRide.originLng = origLng;
        newRide.originAddress = origAddress;


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
}