package pt.ua.travis.gui.taxichooser;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.androidmapsextensions.SupportMapFragment;
import pt.ua.travis.R;
import pt.ua.travis.core.Taxi;

import java.util.List;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TaxiChooserLandscapeFragment extends TaxiChooserFragment {

    private ListView taxiSelector;
    private TaxiListAdapter taxiAdapter;

    private Bundle bundle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.bundle = savedInstanceState;
    }

    @Override
    public void onStart() {
        super.onStart();
        taxiSelector = (ListView) getActivity().findViewById(R.id.list);
        SupportMapFragment mapFragment = (SupportMapFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentById(R.id.map);
        setMap(mapFragment.getExtendedMap());

        List<Taxi> taxis = TaxiChooserActivity.getCurrentTaxiListState();
        taxiAdapter = new TaxiListAdapter(bundle, getActivity(), convertTaxisToItems(taxis));


        // selector (vertical list) configurations
        taxiSelector.setAdapter(taxiAdapter);
        taxiSelector.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                select(position);
            }
        });



        finalizeOnStart();
    }


    @Override
    void doSelectorAction(int position) {
        taxiSelector.smoothScrollToPosition(position);
    }

    @Override
    public TaxiListAdapter getTaxiAdapter() {
        return taxiAdapter;
    }
}