package pt.ua.travis.gui;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import pt.ua.travis.R;
import pt.ua.travis.core.TravisTaxi;
import pt.ua.travis.utils.Validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class PortraitFragment extends TravisFragment {

    private ViewPager taxiSelector;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private Map<String, Pair<Marker, TaxiItem>> listItemMarkerLink;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.taxi_chooser_portrait, null);
    }

    @Override
    public void onStart() {
        super.onStart();

        taxiSelector = (ViewPager) getActivity().findViewById(R.id.pager);
        mapFragment = (SupportMapFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentById(R.id.map);
        map = mapFragment.getMap();
        listItemMarkerLink = new HashMap<>();


        /// SETTING VARIABLES FOR TESTING
        final LatLng clientPosition = new LatLng(40.646908, -8.662323);
        final List<TravisTaxi> taxis = new ArrayList<>();
        TravisTaxi t;

        t = new TravisTaxi("Text #1", "", new ArrayList<Float>());
        t.setLatLng(new LatLng(40.646808, -8.662223));
        taxis.add(t);

        t = new TravisTaxi("Text #2", "", new ArrayList<Float>());
        t.setLatLng(new LatLng(40.635606, -8.659305));
        taxis.add(t);

        t = new TravisTaxi("Text #3", "", new ArrayList<Float>());
        t.setLatLng(new LatLng(40.645831, -8.640680));
        taxis.add(t);

        t = new TravisTaxi("Text #4", "", new ArrayList<Float>());
        t.setLatLng(new LatLng(40.635411, -8.619823));
        taxis.add(t);




        /// CONVERSION OF TAXIS TO PAIR<LISTITEM, MARKER>
        boolean firstTime = true;
        List<TaxiItem> taxiItemList = new ArrayList<>();

        for (TravisTaxi tt : taxis) {
            Marker m = map.addMarker(new MarkerOptions()
                    .position(tt.getPosition())
                    .title(tt.getName())
                    .snippet(tt.getRatingAverage() + "")
                    .visible(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)));
            if(firstTime){
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 15));
                m.showInfoWindow();
                firstTime = false;
            }

            TaxiItem item = new TaxiItem(TaxiItem.HORIZONTAL_MODE, m.getId(), tt);
            taxiItemList.add(item);
            listItemMarkerLink.put(m.getId(), new Pair<>(m, item));
        }
        final TaxiPagerAdapter mAdapter =
                new TaxiPagerAdapter(getActivity().getSupportFragmentManager(), taxiItemList);




        /////////////////////// TAXI LIST

        /// CONFIGS SELECTOR (HORIZONTAL LIST)
        taxiSelector.setOffscreenPageLimit(3);
        taxiSelector.setAdapter(mAdapter);
        taxiSelector.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                Log.e("HERE", "PUNTZ");
                TaxiItem selectedItem = (TaxiItem)mAdapter.getItem(i);
                Pair<Marker, TaxiItem> matchedPair = listItemMarkerLink.get(selectedItem.getMarkerID());

                moveMapToMarker(matchedPair.first);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        /// SETS HOW MUCH OF THE NEXT ITEM IS SHOWN
        if(Validate.isTablet(getActivity())){
            taxiSelector.setPageMargin(
                    getResources().getDimensionPixelOffset(R.dimen.viewpager_margin));
        } else {
            taxiSelector.setPageMargin(
                    getResources().getDimensionPixelOffset(R.dimen.viewpager_margin));
        }





        /////////////////////// MAP


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
                moveMapToMarker(marker);

                Pair<Marker, TaxiItem> matchedPair = listItemMarkerLink.get(marker.getId());
                int position = mAdapter.getItemPosition(matchedPair.second);
                taxiSelector.setCurrentItem(position, true);

                return true;
            }
        });


    }

    private void moveMapToMarker(final Marker marker){
        map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 500, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                marker.showInfoWindow();
            }

            @Override
            public void onCancel() {

            }
        });
    }
}