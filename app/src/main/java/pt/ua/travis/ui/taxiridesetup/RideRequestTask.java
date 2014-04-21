package pt.ua.travis.ui.taxiridesetup;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import pt.ua.travis.R;
import pt.ua.travis.backend.entities.Callback;
import pt.ua.travis.backend.entities.PersistenceManager;
import pt.ua.travis.backend.entities.Ride;
import pt.ua.travis.ui.mainscreen.MainClientActivity;
import pt.ua.travis.utils.CommonKeys;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideRequestTask extends AsyncTask<Void, Void, Void>{

    public interface OnTaskFinished {
        void onFinished(int result, Ride ride);
    }

    public static final int OK_RESULT = 111;
    public static final int CANCEL_RESULT = 222;
    public static final int NOT_FOUND_RESULT = 333;

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
                                        onTaskFinished.onFinished(CANCEL_RESULT, null);
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
            public void onResult(Ride result) {

                SystemClock.sleep(5000);

                int resultCode = 0;
                if(resultCode==0){
                    progressDialog.dismiss();
                    onTaskFinished.onFinished(OK_RESULT, result);
                }

                // after completed finished the progressbar
            }
        });
        return null;
    }
}
