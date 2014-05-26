package pt.ua.travis.ui.riderequest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.ContextThemeWrapper;
import eu.inmite.android.lib.dialogs.ProgressDialogFragment;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import pt.ua.travis.R;
import pt.ua.travis.backend.Callback;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Ride;
import pt.ua.travis.backend.WatchEvent;
import pt.ua.travis.ui.main.MainClientActivity;
import pt.ua.travis.utils.CommonKeys;

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
    private MainClientActivity context;
    private FragmentManager fragmentManager;
    private Ride ride;
    private OnTaskFinished onTaskFinished;

    public RideRequestTask(MainClientActivity context, FragmentManager fragmentManager, Ride ride, OnTaskFinished onTaskFinished){
        this.context = context;
        this.fragmentManager = fragmentManager;
        this.ride = ride;
        this.onTaskFinished = onTaskFinished;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.ride_request_please_wait));
        progressDialog.setCancelable(false);

        CharSequence cancelString = context.getString(R.string.cancel);
        progressDialog.setButton(
                DialogInterface.BUTTON_NEGATIVE, cancelString,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.confirm_cancel)
                                .setMessage(R.string.confirm_cancel_msg)
                                .setCancelable(false)
                                .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        PersistenceManager.delete(ride);
                                        RideRequestTask.this.cancel(true);
                                        onTaskFinished.onFinished(RESPONSE_REFUSED, null);
                                    }
                                })
                                .setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        progressDialog.show();
                                    }
                                })
                                .show();
                    }
                }
        );

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
