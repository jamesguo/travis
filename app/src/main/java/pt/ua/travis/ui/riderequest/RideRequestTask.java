package pt.ua.travis.ui.riderequest;

import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import pt.ua.travis.R;
import pt.ua.travis.backend.Callback;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Ride;
import pt.ua.travis.backend.WatchEvent;
import pt.ua.travis.ui.main.MainActivity;
import pt.ua.travis.ui.main.MainClientActivity;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideRequestTask extends AsyncTask<Void, Void, Void>{

    public interface OnRequestAccepted {
        void onAccepted(Ride ride);
    }

    public static final String RESPONSE_ACCEPTED = "accepted";
    public static final String RESPONSE_DECLINED = "refused";
    public static final String RESPONSE_TIMEOUT  = "timeout";
    public static final String RESPONSE_WAITING  = "waiting_for_request";

    private MainClientActivity context;
    private FragmentManager fragmentManager;
    private Ride ride;
    private OnRequestAccepted onRequestAccepted;

    public RideRequestTask(MainClientActivity context, FragmentManager fragmentManager, Ride ride, OnRequestAccepted onRequestAccepted){
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.ride = ride;
        this.onRequestAccepted = onRequestAccepted;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        String requestedString = context.getString(R.string.request_sent);
        context.showTravisNotification(requestedString, ride.taxi().imageUri(), MainActivity.NotificationColor.DEFAULT);
    }

    @Override
    protected Void doInBackground(Void... params) {
        // saves the ride to request into the database
        PersistenceManager.save(ride, new Callback<Ride>() {
            @Override
            public void onResult(final Ride result) {
                PersistenceManager.setRequestWaiting(result);
                PersistenceManager.waitForRideResponse(result, new WatchEvent<String>() {
                    @Override
                    public void onEvent(String response) {

                        if(response.equals(RESPONSE_ACCEPTED)){
                            String acceptedString = context.getString(R.string.request_accepted);
                            context.showTravisNotification(acceptedString, ride.taxi().imageUri(), MainActivity.NotificationColor.GREEN);

                            onRequestAccepted.onAccepted(result);

                        } else if (response.equals(RESPONSE_DECLINED)){
                            String acceptedString = context.getString(R.string.request_declined);
                            context.showTravisNotification(acceptedString, ride.taxi().imageUri(), MainActivity.NotificationColor.RED);

                            PersistenceManager.delete(result);

                        } else if (response.equals(RESPONSE_TIMEOUT)){
                            String acceptedString = context.getString(R.string.request_timed_out);
                            context.showTravisNotification(acceptedString, ride.taxi().imageUri(), MainActivity.NotificationColor.RED);

                            PersistenceManager.delete(result);
                        }
                    }
                });
            }
        });
        return null;
    }
}
