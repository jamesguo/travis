package pt.ua.travis.ui.taxiinstant;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import com.todddavies.components.progressbar.ProgressWheel;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import pt.ua.travis.R;
import pt.ua.travis.backend.Client;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Taxi;
import pt.ua.travis.core.TravisApplication;
import pt.ua.travis.ui.customviews.TravisFragment;
import pt.ua.travis.ui.main.MainClientActivity;
import pt.ua.travis.ui.riderequest.RideBuilder;

import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TaxiInstantFragment extends TravisFragment {

    private MainClientActivity parentActivity;
    private ProgressWheel pwSpinner;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentView(R.layout.fragment_taxi_instant);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parentActivity = (MainClientActivity) activity;
    }

    @Override
    public void onStart() {
        super.onStart();
        pwSpinner = (ProgressWheel) parentActivity.findViewById(R.id.pw_spinner);
        pwSpinner.setSpinSpeed(pwSpinner.getSpinSpeed()*2);

        ImageButton instantButton = (ImageButton) parentActivity.findViewById(R.id.instant_button);
        instantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pwSpinner.spin();
                new Task().execute();
            }
        });
        setContentShown(true);

    }

    private class Task extends AsyncTask<Void, Void, List<Taxi>>{
        @Override
        protected List<Taxi> doInBackground(Void... params) {
            LatLng currentPos = ((TravisApplication) parentActivity.getApplication()).getCurrentLocation();
            return PersistenceManager.query().taxis().available().online().near(currentPos).limitNumberOfResultsTo(5).now();
        }

        @Override
        protected void onPostExecute(final List<Taxi> taxis) {
            super.onPostExecute(taxis);
            pwSpinner.stopSpinning();

            if(!taxis.isEmpty()) {
                final Client thisClient = PersistenceManager.getCurrentlyLoggedInUser();

                new TaxiInstantDialog.Listener() {
                    private int i = 0;

                    @Override
                    public void onConfirmedTaxi(Taxi t) {
                        RideBuilder builder = parentActivity.getRideBuilder();
                        builder.resetToHereAndNow((TravisApplication) parentActivity.getApplication());
                        builder.setTaxi(t);
                        parentActivity.requestRideToTaxi(builder.build());
                    }

                    @Override
                    public void onShowNextTaxi() {
                        if(i < taxis.size()) {
                            Taxi t = taxis.get(i);
                            TaxiInstantDialog.show(parentActivity, thisClient, t, this);
                        } else {
                            SimpleDialogFragment.createBuilder(parentActivity, TaxiInstantFragment.this.getChildFragmentManager())
                                    .setTitle(R.string.instant_dialog_title2)
                                    .setMessage(R.string.instant_dialog_msg2)
                                    .setPositiveButtonText(R.string.instant_dialog_positive2)
                                    .show();
                        }
                        i++;
                    }
                }.onShowNextTaxi();
            }


        }
    }
}
