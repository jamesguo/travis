package pt.ua.travis.ui.taxiinstant;

import android.os.Bundle;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import pt.ua.travis.R;
import pt.ua.travis.ui.customviews.TravisFragment;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TaxiInstantFragment extends TravisFragment {

    private static View lastUsedView;

//    @Override
//    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        if (lastUsedView != null) {
//            ViewGroup parent = (ViewGroup) lastUsedView.getParent();
//            if (parent != null)
//                parent.removeView(lastUsedView);
//        }
//        try {
//            lastUsedView = inflater.inflate(R.layout.fragment_taxi_instant, null);
//        } catch (InflateException e) {
//            // map is already there, just return view as it is
//        }
//        return lastUsedView;
//    }


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
                Toast.makeText(getActivity(), "URRAY", Toast.LENGTH_LONG).show();
            }
        });
        setContentShown(true);
    }
}
