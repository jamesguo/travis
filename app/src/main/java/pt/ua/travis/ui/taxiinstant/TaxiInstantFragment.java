package pt.ua.travis.ui.taxiinstant;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import pt.ua.travis.R;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Taxi;
import pt.ua.travis.core.TravisApplication;
import pt.ua.travis.ui.customviews.TravisFragment;

import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TaxiInstantFragment extends TravisFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentView(R.layout.fragment_taxi_instant);
    }

    @Override
    public void onStart() {
        super.onStart();
        Button instantButton = (Button) getActivity().findViewById(R.id.instant_button);
        instantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Work in progress", Toast.LENGTH_LONG).show();
//                new Task().execute();
            }
        });
        setContentShown(true);
    }

    private class Task extends AsyncTask<Void, Void, List<Taxi>>{
        @Override
        protected List<Taxi> doInBackground(Void... params) {
            LatLng currentPos = ((TravisApplication) getActivity().getApplication()).getCurrentLocation();
            return PersistenceManager.query().taxis().available().online().near(currentPos).limitNumberOfResultsTo(5).now();
        }
    }
}
