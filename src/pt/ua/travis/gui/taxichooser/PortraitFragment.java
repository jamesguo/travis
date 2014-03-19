package pt.ua.travis.gui.taxichooser;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import pt.ua.travis.R;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.gui.ClientMain;
import pt.ua.travis.utils.Validate;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class PortraitFragment extends TaxiChooserFragment {

    private ViewPager taxiSelector;
    private TaxiPagerAdapter taxiAdapter;
    private GoogleMap map;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.taxi_chooser_portrait, null);
    }

    @Override
    public void onStart() {
        super.onStart();

        taxiSelector = (ViewPager) getActivity().findViewById(R.id.pager);
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




        taxiAdapter = new TaxiPagerAdapter(getChildFragmentManager(), convertTaxisToItems(taxis));


        // selector (horizontal list) configurations
        taxiSelector.setOffscreenPageLimit(3);
        taxiSelector.setAdapter(taxiAdapter);
        taxiSelector.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int i) {
                select(i);
                taxiAdapter.setSelectedIndex(i);
            }

            @Override
            public void onPageScrolled(int i, float v, int i2) {}

            @Override
            public void onPageScrollStateChanged(int i) {}
        });

        // sets how much of the next item is shown
        if(Validate.isTablet(getActivity())){
            taxiSelector.setPageMargin(
                    getResources().getDimensionPixelOffset(R.dimen.viewpager_margin));
        } else {
            taxiSelector.setPageMargin(
                    getResources().getDimensionPixelOffset(R.dimen.viewpager_margin));
        }



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
        taxiSelector.setCurrentItem(position, true);
    }

    @Override
    public TaxiPagerAdapter getTaxiAdapter() {
        return taxiAdapter;
    }

    @Override
    public GoogleMap getMap() {
        return map;
    }
}