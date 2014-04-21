package pt.ua.travis.ui.taxiridesetup;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import pt.ua.travis.R;
import pt.ua.travis.backend.Callback;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Ride;
import pt.ua.travis.backend.WatchEvent;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideRequestTask extends AsyncTask<Void, Void, Void>{

    public interface OnTaskFinished {
        void onFinished(String response, Ride ride);
    }

    public static final String RESPONSE_ACCEPTED = "accepted";
    public static final String RESPONSE_REFUSED  = "refused";
    public static final String RESPONSE_TIMEOUT  = "timeout";
    public static final String RESPONSE_WAITING  = "waiting_for_request";

    private ProgressDialog progressDialog;
    private Context context;
    private Ride ride;
    private OnTaskFinished onTaskFinished;

    public RideRequestTask(Context context, Ride ride, OnTaskFinished onTaskFinished){
        this.context = context;
        this.ride = ride;
        this.onTaskFinished = onTaskFinished;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("The taxi you selected will now need to accept your request...");
        progressDialog.setCancelable(false);

        CharSequence cancelString = context.getString(R.string.cancel);
        progressDialog.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                cancelString,
                new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AlertDialog.Builder(context)
                                .setTitle("Confirm cancel")
                                .setMessage("Are you sure you wish to cancel this request?")
                                .setCancelable(false)
                                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        PersistenceManager.delete(ride);
                                        RideRequestTask.this.cancel(true);
                                        onTaskFinished.onFinished(RESPONSE_REFUSED, null);
                                    }
                                })
                                .setPositiveButton("No", null)
                                .show();
                    }
                });

        progressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        // saves the ride to request into the database
        PersistenceManager.save(ride, new Callback<Ride>() {
            @Override
            public void onResult(final Ride result) {
                PersistenceManager.waitForRideResponse(result, new WatchEvent<String>() {
                    @Override
                    public void onEvent(String response) {

                        if(response.equals(RESPONSE_ACCEPTED)){
                            progressDialog.dismiss();
                            onTaskFinished.onFinished(RESPONSE_ACCEPTED, result);

                        } else if (response.equals(RESPONSE_REFUSED)){
                            progressDialog.dismiss();
                            PersistenceManager.delete(result);
                            onTaskFinished.onFinished(RESPONSE_REFUSED, null);

                        } else if (response.equals(RESPONSE_TIMEOUT)){
                            progressDialog.dismiss();
                            PersistenceManager.delete(result);
                            onTaskFinished.onFinished(RESPONSE_TIMEOUT, null);

                        }
                    }
                });
            }
        });
        return null;
    }
}
