package pt.ua.travis.gui.taxiridesetup;

import android.widget.LinearLayout;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import com.beardedhen.androidbootstrap.BootstrapButton;
import org.joda.time.LocalTime;
import pt.ua.travis.core.Taxi;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideRequestActivity extends SherlockFragment {

    private static final int MATCH_PARENT = LinearLayout.LayoutParams.MATCH_PARENT;

    private Taxi selectedTaxi;


    private LocalTime scheduledTime;
    private double origLat, origLng, destLat, destLng;
    private String origAddress, destAddress;
    private RideRequestViewPager pager;
    private TextView addressTextView;
    private BootstrapButton doneButton, cancelButton;

    public static RideRequestActivity newInstance(Taxi choosenTaxi) {
        RideRequestActivity r = new RideRequestActivity();
        r.selectedTaxi = choosenTaxi;
        return r;
    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.ride_setup, null);
//
//        return v;
//    }
//
//    public void onHereAndNowButtonClicked(View view){
//        // creates a new ride whose time is the taxi's elapsed time of arriving at the target
//        scheduledTime = LocalTime.now();
//
//        Location currentLocation = Tools.getCurrentLocation(this);
//        String currentAddress = Tools.latlngToAddressString(this,
//                currentLocation.getLatitude(), currentLocation.getLongitude());
//
//        origLat = currentLocation.getLatitude();
//        origLng = currentLocation.getLongitude();
//        origAddress = currentAddress;
//
//        destLat = 0;
//        destLng = 0;
//        destAddress = "";
//
//        buildAndRequestRide();
//    }
//
//    public void onLaterButtonClicked(View view){
//        Location currentLocation = Tools.getCurrentLocation(this);
//        String currentAddress = Tools.latlngToAddressString(this,
//                currentLocation.getLatitude(), currentLocation.getLongitude());
//
//        pager.setCurrentItem(1, true);
//        addressTextView = (TextView) findViewById(R.id.origin_address);
//        addressTextView.setText(currentAddress);
//
//        TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
//        timePicker.setIs24HourView(true);
//        LocalTime now = LocalTime.now();
//        timePicker.setCurrentHour(now.getHourOfDay());
//        timePicker.setCurrentMinute(now.minuteOfHour().get());
//    }
//
//    public void onDoneButtonClicked(View view){
//        TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
//        scheduledTime = new LocalTime(DateTimeZone.forID("GMT"))
//                .withHourOfDay(timePicker.getCurrentHour())
//                .withMinuteOfHour(timePicker.getCurrentMinute());
//        buildAndRequestRide();
//    }
//
//    public void onCancelButtonClicked(View view){
//        pager.setCurrentItem(0, true);
//    }
//
//    public void onOriginButtonClicked(View view){
//        Intent intent = new Intent(this, AddressPickerDialog.class);
//        startActivityForResult(intent, Keys.REQUEST_ORIGIN_COORDS);
//    }
//
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
//
//    public void buildAndRequestRide(){
//        scheduledTime = scheduledTime.plusMinutes(7); // TODO ADD ESTIMATE TIME
//        final Ride newRide = new Ride(selectedTaxi, PersistenceManager.selectThisClientAccount(), scheduledTime);
//        newRide.originLat = origLat;
//        newRide.originLng = origLng;
//        newRide.originAddress = origAddress;
//
//
//        new RideRequestTask(RideRequestActivity.this, newRide, new Returner() {
//            @Override
//            public void onResult(int result) {
//                if (result == RideRequestTask.OK_RESULT) {
//                    PersistenceManager.addRide(newRide);
//
//                    Intent intent = new Intent(
//                            RideRequestActivity.this,
//                            WaitForTaxiActivity.class);
//                    intent.putExtra(Keys.SCHEDULED_RIDE, newRide);
//                    startActivity(intent);
//                } else if(result == RideRequestTask.CANCEL_RESULT) {
//                    // TODO THE TAXI DENIED THE REQUEST
//                }
//            }
//        }).execute();
//    }
}