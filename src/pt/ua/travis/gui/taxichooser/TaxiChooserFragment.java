package pt.ua.travis.gui.taxichooser;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragment;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import pt.ua.travis.R;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.db.TravisDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public abstract class TaxiChooserFragment extends SherlockFragment {
    private static View lastUsedView;

    private GoogleMap map;
    private boolean onMyLocationButtonToggle;
    private Map<String, Pair<Marker, TaxiItem>> listItemMarkerLink;

    protected TaxiChooserFragment() {
        listItemMarkerLink = new HashMap<>();
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
     * This method sets the map to be used by the fragment and that will show the various markers,
     * each one identifying the taxis or the user himself.
     * @param map the map to be used by the fragment
     */
    protected final void setMap(GoogleMap map){
        this.map = map;
    }


    /**
     * Method that must be run at the end of the {@link android.support.v4.app.Fragment#onStart()}
     * method, that will configure the set map and zoom into the marker corresponding to
     * the first taxi on the list.
     */
    protected final void finalizeOnStart(){
        final Marker userPosition = map.addMarker(new MarkerOptions()
                .position(TravisDB.getClientAccount().getPosition())
                .title("You")
                .visible(true));

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
        onMyLocationButtonToggle = false;
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            private Marker lastMarker;

            @Override
            public boolean onMyLocationButtonClick() {
                if(!onMyLocationButtonToggle){
                    TaxiItemAdapter adapter = getTaxiAdapter();
                    TaxiItem selectedItem = (TaxiItem) adapter.getItem(adapter.getCurrentSelectedIndex());
                    Pair<Marker, TaxiItem> matchedPair = listItemMarkerLink.get(selectedItem.getMarkerID());

                    lastMarker = matchedPair.first;
                    moveMapToMarker(userPosition);
                    onMyLocationButtonToggle = true;
                } else {
                    select(lastMarker);
                    lastMarker = null;
                }
                return true;
            }
        });
        map.moveCamera(CameraUpdateFactory.zoomTo(15));


        // selects the item that was selected in the previous fragment
        int indexToSelect = getArguments().getInt(TaxiChooserActivity.CURRENTLY_SELECTED_INDEX);
        select(indexToSelect);
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
        List<TaxiItem> taxiItemList = new ArrayList<>();

        for (Taxi tt : taxis) {
            Marker m = map.addMarker(new MarkerOptions()
                    .position(tt.getPosition())
                    .title(tt.getName())
                    .snippet(tt.getRatingAverage() + "")
                    .visible(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)));

            TaxiItem item = null;
            if(this instanceof TaxiChooserPortraitFragment)
                item = TaxiItem.newInstance(TaxiItem.HORIZONTAL_MODE, m.getId(), tt);
            else if(this instanceof TaxiChooserLandscapeFragment)
                item = TaxiItem.newInstance(TaxiItem.VERTICAL_MODE, m.getId(), tt);

            taxiItemList.add(item);
            listItemMarkerLink.put(m.getId(), new Pair<>(m, item));
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
        int maxSize = getTaxiAdapter().getCount();
        if(position >= maxSize) {
            Log.e("Position is greater than Adapter size", "Position:"+position+" | AdapterSize:"+maxSize);
            return;
        }
        TaxiItem selectedItem = (TaxiItem) getTaxiAdapter().getItem(position);
        Pair<Marker, TaxiItem> matchedPair = listItemMarkerLink.get(selectedItem.getMarkerID());
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
        Pair<Marker, TaxiItem> matchedPair = listItemMarkerLink.get(marker.getId());
        int position = getTaxiAdapter().getItemPosition(matchedPair.second);
        finishSelect(position, marker);
    }

    /**
     * Method used by both "select" methods to do some common selecting operations.
     */
    private void finishSelect(int position, Marker marker){
        TaxiItemAdapter adapter = getTaxiAdapter();

        doSelectorAction(position);
        adapter.setSelectedIndex(position);
        adapter.notifyDataSetChanged();
        moveMapToMarker(marker);
        onMyLocationButtonToggle = false;
    }

    /**
     * Method used by both "select" methods to do some common selecting operations.
     */
    private void moveMapToMarker(final Marker marker){
        marker.showInfoWindow();
        map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 500, null);
    }

    public final int getCurrentSelectedIndex() {
        return getTaxiAdapter().getCurrentSelectedIndex();
    }

    /**
     * The user must override and implement this method to provide access to the
     * taxi elements container adapter to this class.
     */
    abstract TaxiItemAdapter getTaxiAdapter();

    /**
     * The user must override and implement this method to contain some extra operations
     * associated with the selector, whose variable this class has no access to. These extra
     * operations will be executed at every "select" instruction.
     *
     * @param position the position of the selected element
     */
    abstract void doSelectorAction(int position);
}