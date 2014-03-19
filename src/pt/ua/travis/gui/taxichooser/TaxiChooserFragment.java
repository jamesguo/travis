package pt.ua.travis.gui.taxichooser;

import android.util.Pair;
import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import pt.ua.travis.R;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.gui.TaxiItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public abstract class TaxiChooserFragment extends SherlockFragment {

    private Map<String, Pair<Marker, TaxiItem>> listItemMarkerLink;

    protected TaxiChooserFragment() {
        listItemMarkerLink = new HashMap<>();
    }

    protected List<TaxiItem> convertTaxisToItems(List<Taxi> taxis){
        List<TaxiItem> taxiItemList = new ArrayList<>();

        for (Taxi tt : taxis) {
            Marker m = getMap().addMarker(new MarkerOptions()
                    .position(tt.getPosition())
                    .title(tt.getName())
                    .snippet(tt.getRatingAverage() + "")
                    .visible(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)));

            TaxiItem item = null;
            if(this instanceof PortraitFragment)
                item = TaxiItem.newInstance(TaxiItem.HORIZONTAL_MODE, m.getId(), tt);
            else if(this instanceof LandscapeFragment)
                item = TaxiItem.newInstance(TaxiItem.VERTICAL_MODE, m.getId(), tt);

            taxiItemList.add(item);
            listItemMarkerLink.put(m.getId(), new Pair<>(m, item));
        }

        return taxiItemList;
    }

    public void select(int position) {
        TaxiItem selectedItem = (TaxiItem) getTaxiAdapter().getItem(position);
        Pair<Marker, TaxiItem> matchedPair = listItemMarkerLink.get(selectedItem.getMarkerID());
        finishSelect(position, matchedPair.first);
    }

    public void select(Marker marker){
        Pair<Marker, TaxiItem> matchedPair = listItemMarkerLink.get(marker.getId());
        int position = getTaxiAdapter().getItemPosition(matchedPair.second);
        finishSelect(position, marker);
    }

    private void finishSelect(int position, Marker marker){
        doSelectorAction(position);
        getTaxiAdapter().setSelectedIndex(position);
        getTaxiAdapter().notifyDataSetChanged();

        moveMapToMarker(marker);
    }

    private void moveMapToMarker(final Marker marker){
        marker.showInfoWindow();
        getMap().animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 500, null);
    }

    public int getCurrentSelectedIndex() {
        return getTaxiAdapter().getCurrentSelectedIndex();
    }

    abstract TaxiAdapter getTaxiAdapter();

    abstract GoogleMap getMap();

    abstract void doSelectorAction(int position);
}