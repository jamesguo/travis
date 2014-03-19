package pt.ua.travis.gui.taxichooser;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import pt.ua.travis.R;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.gui.ClientMain;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class LandscapeFragment extends TaxiChooserFragment {

    private ListView taxiSelector;
    private TaxiListAdapter taxiAdapter;
    private GoogleMap map;

    private Bundle bundle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.bundle = savedInstanceState;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.taxi_chooser_landscape, null);
    }

    @Override
    public void onStart() {
        super.onStart();

        taxiSelector = (ListView) getActivity().findViewById(R.id.list);
        SupportMapFragment mapFragment = (SupportMapFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentById(R.id.map);
        map = mapFragment.getMap();


        /// SETTING VARIABLES FOR TESTING //////////////////////////////////////////
        final LatLng clientPosition = new LatLng(40.646908, -8.662323);
        final List<Taxi> taxis = new ArrayList<>();
        Taxi t;

        t = new Taxi("Text #1", "", new ArrayList<Float>());
        t.setLatLng(new LatLng(40.646808, -8.662223));
        taxis.add(t);

        t = new Taxi("Text #2", "", new ArrayList<Float>());
        t.setLatLng(new LatLng(40.635606, -8.659305));
        taxis.add(t);

        t = new Taxi("Text #3", "", new ArrayList<Float>());
        t.setLatLng(new LatLng(40.645831, -8.640680));
        taxis.add(t);

        t = new Taxi("Text #4", "", new ArrayList<Float>());
        t.setLatLng(new LatLng(40.635411, -8.619823));
        taxis.add(t);
        /////////////////////////////////////////////////////////////////////////////




        taxiAdapter = new TaxiListAdapter(bundle, getActivity(), convertTaxisToItems(taxis));


        // selector (vertical list) configurations
        taxiSelector.setAdapter(taxiAdapter);
        taxiSelector.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                select(position);
            }
        });




        // map configurations
        map.setMyLocationEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                select(marker);
                return true;
            }
        });


        // selects the item that was selected in the previous fragment
        int indexToSelect = getArguments().getInt(ClientMain.CURRENTLY_SELECTED_INDEX);
        Log.e("MAYBE?", indexToSelect+"");
        select(indexToSelect);
    }

    @Override
    void doSelectorAction(int position) {
        taxiSelector.smoothScrollToPosition(position);
    }

    @Override
    public TaxiListAdapter getTaxiAdapter() {
        return taxiAdapter;
    }

    @Override
    public GoogleMap getMap() {
        return map;
    }
}