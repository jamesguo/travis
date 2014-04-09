package pt.ua.travis.gui.taxichooser;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.*;
import com.actionbarsherlock.app.SherlockFragment;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Lists;
import pt.ua.travis.R;
import pt.ua.travis.backend.entities.Client;
import pt.ua.travis.backend.entities.Taxi;
import pt.ua.travis.backend.Geolocation;
import pt.ua.travis.backend.entities.CloudBackendManager;
import pt.ua.travis.gui.main.MainClientActivity;
import pt.ua.travis.utils.Utils;

import java.util.List;
import java.util.Map;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public abstract class TaxiChooserFragment extends SherlockFragment {

    private static View lastUsedView;

    private Map<String, Pair<Marker, TaxiItem>> itemToMarkerMappings;

    private GoogleMap map;

    private GoogleMap.OnMyLocationButtonClickListener myLocationListener;

    private boolean myLocationToggle;

    protected TaxiItemAdapter itemAdapter;



    protected TaxiChooserFragment() {
        itemToMarkerMappings = Utils.newArrayMap();
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (lastUsedView != null) {
            ViewGroup parent = (ViewGroup) lastUsedView.getParent();
            if (parent != null)
                parent.removeView(lastUsedView);
        }
        try {
            lastUsedView = inflater.inflate(R.layout.taxi_chooser, null);
        } catch (InflateException e) {
            // map is already there, just return view as it is
        }
        return lastUsedView;
    }


    /**
     * When this fragment starts, the map that will be used is set.
     * This map will show various markers, each one identifying the taxis or the user himself.
     */
    @Override
    public void onStart() {
        super.onStart();
        SupportMapFragment mapFragment = (SupportMapFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentById(R.id.map);
        map = mapFragment.getExtendedMap();
        map.clear();

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
        map.setMyLocationEnabled(true);
        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                Geolocation.setCurrentLocation(location.getLatitude(), location.getLongitude());
            }
        });
        myLocationListener = new GoogleMap.OnMyLocationButtonClickListener() {
            private Marker lastMarker;

            @Override
            public boolean onMyLocationButtonClick() {
                if(!myLocationToggle){
                    TaxiItem selectedItem = itemAdapter.getItem(getCurrentSelectedIndex());
                    String selectedItemID = selectedItem.getTaxiObject().getId();

                    Pair<Marker, TaxiItem> matchedPair = itemToMarkerMappings.get(selectedItemID);

                    lastMarker = matchedPair.first;
                    LatLng userPosition = Geolocation.getCurrentPosition();
                    map.animateCamera(CameraUpdateFactory.newLatLng(userPosition), 500, null);
                    myLocationToggle = true;
                } else {
                    select(lastMarker);
                    lastMarker = null;
                }
                return true;
            }
        };
        map.setOnMyLocationButtonClickListener(myLocationListener);
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                select(marker);
                return true;
            }
        });
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                int selectedMarkerID = marker.getData();

                Taxi t = itemToMarkerMappings
                        .get(selectedMarkerID)
                        .second
                        .getTaxiObject();

                ((MainClientActivity)getActivity()).showOptionsPane(t);
            }
        });
        map.moveCamera(CameraUpdateFactory.zoomTo(15));
    }


    /**
     * Utility method to add markers for each taxi on the specified list and to link
     * each marker with an item fragment, that is displayed on the ViewPager (if the
     * orientation is portrait) or the ListView (if the orientation is landscape).
     *
     * @param taxis the specified taxi list whose markers will be added to the map
     * @return the list of item fragments resultant from this conversion and
     *         marker generation
     */
    protected final List<TaxiItem> convertTaxisToItems(List<Taxi> taxis){
        List<TaxiItem> taxiItemList = Lists.newArrayList();
        Client cc = CloudBackendManager.select().clients().loggedInThisDevice();

        for (Taxi tt : taxis) {

            Marker m = map.addMarker(new MarkerOptions()
                    .data(tt.getId())
                    .position(tt.currentPosition())
                    .title(tt.name())
                    .snippet("Tap to select this Taxi")
                    .visible(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)));

            TaxiItem item = null;

            if(this instanceof TaxiChooserPagerFragment)
                item = TaxiItem.newInstance(cc, tt);
            else if(this instanceof TaxiChooserListFragment)
                item = TaxiItem.newInstance(cc, tt);

            taxiItemList.add(item);
            itemToMarkerMappings.put(tt.getId(), Utils.newPair(m, item));
        }

        return taxiItemList;
    }

    /**
     * Instructs both the selector object (ViewPager or ListView) to scroll and select
     * the element at the specified positional index.
     *
     * @param position the index of the element that the users wants to scroll to.
     */
    public final void select(int position) {
        int maxSize = itemAdapter.getCount();
        if(position >= maxSize) {
            Log.e("Position is greater than Adapter size", "Position:" + position + " | AdapterSize:" + maxSize);
            return;
        }
        TaxiItem selectedItem = itemAdapter.getItem(position);
        String selectedItemID = selectedItem.getTaxiObject().getId();

        Pair<Marker, TaxiItem> matchedPair = itemToMarkerMappings.get(selectedItemID);
        finishSelect(position, matchedPair.first);
    }

    /**
     * Instructs both the selector object (ViewPager or ListView) to scroll and select
     * the element that corresponds to the specified marker.
     *
     * @param marker the marker that corresponds to the element that the users wants
     *               to scroll to
     */
    public final void select(Marker marker){
        int selectedMarkerID = marker.getData();
        if (selectedMarkerID == 0) {
            // its the user marker
            myLocationListener.onMyLocationButtonClick();
            return;
        }

        Pair<Marker, TaxiItem> matchedPair = itemToMarkerMappings.get(selectedMarkerID);
        int position = itemAdapter.getItemPosition(matchedPair.second);
        finishSelect(position, marker);
    }

    /**
     * Method used by both "select" methods to do some common selecting operations.
     * The user must override and implement this method to contain some extra operations
     * associated with the selector, whose variable this class has no access to. These extra
     * operations will be executed at every "select" instruction.
     *
     * @param position the position of the selected element
     * @param marker the marker that corresponds to the selected element
     */
    protected void finishSelect(int position, Marker marker){
        itemAdapter.setSelectedIndex(position);
//        itemAdapter.notifyDataSetChanged();
        moveMapToMarker(marker);
        myLocationToggle = false;
    }

    /**
     * Method used by both "select" methods to do some common selecting operations.
     */
    private void moveMapToMarker(final Marker marker){
        marker.showInfoWindow();
        map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 500, null);
    }

    public final int getCurrentSelectedIndex() {
        return itemAdapter.getSelectedIndex();
    }

}