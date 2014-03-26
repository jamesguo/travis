package pt.ua.travis.gui.taxiridesetup;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.SystemClock;
import pt.ua.travis.R;
import pt.ua.travis.core.Ride;
import pt.ua.travis.utils.Returner;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideRequestTask extends AsyncTask<Void, Void, Integer>{

    public static final int OK_RESULT = 111;
    public static final int CANCEL_RESULT = 222;
    public static final int NOT_FOUND_RESULT = 333;

    private ProgressDialog progressDialog;
    private Context context;
    private Ride ride;
    private Returner resultReturner;

    RideRequestTask(Context context, Ride ride, Returner resultReturner){
        this.context = context;
        this.ride = ride;
        this.resultReturner = resultReturner;
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
                                        RideRequestTask.this.cancel(true);
                                        resultReturner.onResult(CANCEL_RESULT);
                                    }
                                })
                                .setPositiveButton("No", null)
                                .show();
                    }
                });

        progressDialog.show();
    }

    @Override
    protected Integer doInBackground(Void... params) {
        SystemClock.sleep(5000);
        return 0;
    }

    @Override
    protected void onPostExecute(Integer result) {
        if(result==0){
            progressDialog.dismiss();
            resultReturner.onResult(OK_RESULT);
        }

        // after completed finished the progressbar
    }
}
