package pt.ua.travis.ui.taxichooser;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import pt.ua.travis.R;
import pt.ua.travis.backend.Callback;
import pt.ua.travis.backend.Client;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Taxi;
import pt.ua.travis.core.BaseFragment;
import pt.ua.travis.core.BaseMapFragment;
import pt.ua.travis.core.TravisApplication;
import pt.ua.travis.ui.customviews.*;
import pt.ua.travis.ui.main.MainClientActivity;
import pt.ua.travis.utils.CommonKeys;
import pt.ua.travis.utils.Pair;
import pt.ua.travis.utils.TravisUtils;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import java.util.*;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TaxiChooserFragment extends BaseFragment
        implements AdapterView.OnItemSelectedListener, OnRefreshListener {

    private MainClientActivity parentActivity;

    private Map<String, Pair<Marker, TaxiItem>> itemToMarkerMappings;

    private GoogleMap map;

    private GoogleMap.OnMyLocationButtonClickListener myLocationListener;

    private boolean myLocationToggle;

    protected TaxiItemAdapter taxiPagerAdapter;

    private TaxiItemViewPager taxiPager;

    private PullToRefreshLayout pullToRefreshLayout;

    private RideRequestViewPager rideOptionsPager;

    private SlidingPaneLayout slidingPaneLayout;



    public TaxiChooserFragment() {
        itemToMarkerMappings = new LinkedHashMap<String, Pair<Marker, TaxiItem>>();
    }


    /**
     * Collect parent activity.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parentActivity = (MainClientActivity) activity;
    }


    /**
     * When this fragment has a layout, the map and pagers that will be used are configured.
     * The map will show various markers, each one identifying the taxis or the user himself.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentView(R.layout.fragment_taxi_chooser);

        initializeFragmentViews();
    }

    private void initializeFragmentViews() {

        BaseMapFragment mapFragment = (BaseMapFragment) parentActivity
                .getSupportFragmentManager()
                .findFragmentById(R.id.taxi_map);
        map = mapFragment.getExtendedMap();
        map.clear();

        SharedPreferences prefs = parentActivity.getSharedPreferences("TravisPreferences", Context.MODE_PRIVATE);
        int mapType = prefs.getInt(CommonKeys.MAP_TYPE, GoogleMap.MAP_TYPE_TERRAIN);

        map.setMapType(mapType);
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
        myLocationListener = new GoogleMap.OnMyLocationButtonClickListener() {
            private Marker lastMarker;

            @Override
            public boolean onMyLocationButtonClick() {
                if(!myLocationToggle) {
                    Pair<Marker, TaxiItem> matchedPair =
                            itemToMarkerMappings.get(taxiPagerAdapter.getCurrentlySelectedTaxiId());

                    lastMarker = matchedPair.first;
                    LatLng userPosition = ((TravisApplication) getActivity().getApplication()).getCurrentLocation();
                    LatLng realNewPos = new LatLng(userPosition.latitude + 0.002, userPosition.longitude);
                    map.animateCamera(CameraUpdateFactory.newLatLng(realNewPos), 900, null);
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
                String selectedMarkerID = marker.getData();

                Taxi t = itemToMarkerMappings
                        .get(selectedMarkerID)
                        .second
                        .getTaxiObject();

                showOptionsPane(t);
            }
        });
        map.moveCamera(CameraUpdateFactory.zoomTo(17.0f));


        taxiPager = (TaxiItemViewPager) getActivity().findViewById(R.id.taxi_pager);
        taxiPagerAdapter = new TaxiItemAdapter(getChildFragmentManager());
        taxiPager.setAdapter(taxiPagerAdapter);
        taxiPager.setOffscreenPageLimit(3);
        taxiPager.setPageMargin(-60);
        taxiPager.setHorizontalFadingEdgeEnabled(true);
        taxiPager.setFadingEdgeLength(30);
        taxiPager.setOnPageChangeListener(new TaxiItemViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int i) {
                select(i);
            }

            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });


        pullToRefreshLayout = (PullToRefreshLayout) parentActivity.findViewById(R.id.pull_to_refresh_layout);
        ActionBarPullToRefresh.from(parentActivity)
                .options(Options.create()
                        .refreshOnUp(true)
                        .scrollDistance(3.5f)
                        .build())
                .allChildrenArePullable()
                .useViewDelegate(TaxiItemViewPager.class, new CustomViewDelegate())
                .listener(this)
                .setup(pullToRefreshLayout);
        DefaultHeaderTransformer headerTransformer = (DefaultHeaderTransformer) pullToRefreshLayout.getHeaderTransformer();
        headerTransformer.setProgressBarColor(getResources().getColor(R.color.travis_color));

//        // sets how much of the next item is shown
//        if(Validate.isTablet(getActivity())){
//            taxiPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.viewpager_margin));
//        } else {
//            taxiPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.viewpager_margin));
//        }

        // selects the item that was selected in the previous fragment
//        int indexToSelect = getArguments().getInt(CommonKeys.SELECTED_INDEX);



        rideOptionsPager = (RideRequestViewPager) parentActivity.findViewById(R.id.sliding_pane_pager);
        rideOptionsPager.setPageTransformer(false, new SlidingPaneTransformer(rideOptionsPager));
        rideOptionsPager.setAdapter(new RideRequestPagerAdapter(parentActivity.getSupportFragmentManager()));
        rideOptionsPager.setOffscreenPageLimit(1);
        slidingPaneLayout = (SlidingPaneLayout) parentActivity.findViewById(R.id.sliding_pane_layout);
        slidingPaneLayout.setStickTo(SlidingPaneLayout.STICK_TO_BOTTOM);



        Spinner spinner = (Spinner) parentActivity.findViewById(R.id.taxi_sort_spinner);
        spinner.setAdapter(new TaxiFilterSpinnerAdapter(parentActivity));
        spinner.setOnItemSelectedListener(this);

        setRetainInstance(true);

//        new AsyncTask<Void, Void, List<Taxi>>() {
//            @Override
//            protected List<Taxi> doInBackground(Void... params) {
//                return PersistenceManager
//                        .query()
//                        .taxis()
//                        .near(TravisLocation.getCurrentLocation(parentActivity))
//                        .limitNumberOfResultsTo(10)
//                        .now();
//            }
//
//            @Override
//            protected void onPostExecute(List<Taxi> result) {
//                super.onPostExecute(result);
//                taxiPagerAdapter.update(updateMarkers(result));
//                select(0);
//                setContentShown(true);
//            }
//
//        }.execute();
//
        parentActivity.getCurrentlyShownTaxiList(false, new Callback<List<Taxi>>() {
            @Override
            public void onResult(List<Taxi> result) {
                updateTaxis(result);
                setContentShown(true);
            }
        });
    }


    /**
     * This method is invoked when the action bar is pulled.
     */
    @Override
    public void onRefreshStarted(View view) {
        parentActivity.getCurrentlyShownTaxiList(true, new Callback<List<Taxi>>() {
            @Override
            public void onResult(List<Taxi> result) {
                updateTaxis(result);
                pullToRefreshLayout.setRefreshComplete();
            }
        });
    }

    public final void updateTaxis(List<Taxi> newTaxis) {
        Client thisClient = PersistenceManager.getCurrentlyLoggedInUser();

        // remove duplicates
        newTaxis = new ArrayList<Taxi>(new LinkedHashSet<Taxi>(newTaxis));

        map.clear();
        itemToMarkerMappings.clear();
        for (Taxi t : newTaxis) {
            TaxiItem item = TaxiItem.create(parentActivity, thisClient, t);
            Pair<Marker, TaxiItem> pair = TravisUtils.newPair(null, item);
            itemToMarkerMappings.put(t.id(), pair);
        }

        String oldSelectedId = taxiPagerAdapter.currentlySelectedId;

        taxiPagerAdapter = new TaxiItemAdapter(getChildFragmentManager());
        taxiPager.setAdapter(taxiPagerAdapter);
        taxiPagerAdapter.notifyDataSetChanged();
        updateMarkers(newTaxis);

        Pair<Marker, TaxiItem> pair = itemToMarkerMappings.get(oldSelectedId);
        if (pair != null) {
            select(pair.first);
        } else {
            select(0);
        }
    }


    /**
     * Adds markers for each taxi on the specified list and to link
     * each marker with an item fragment, that is displayed on the ViewPager (if the
     * orientation is portrait) or the ListView (if the orientation is landscape).
     *
     * @param taxis the specified taxi list whose markers will be added to the map
     */
    public final void updateMarkers(List<Taxi> taxis){
        String oldSelectedId = taxiPagerAdapter.currentlySelectedId;

        for (Taxi t : taxis) {
            Pair<Marker, TaxiItem> pair = itemToMarkerMappings.get(t.id());
            if(pair == null){
                // taxi to update could not be found, probably because the list was updated again
                continue;
            }
            Marker oldM = pair.first;
            if(oldM != null) {
                oldM.setVisible(false);
                oldM.remove();
            }

            Marker newM = map.addMarker(instantiateMarkerForTaxi(t));
            TaxiItem item = pair.second;
            itemToMarkerMappings.put(t.id(), TravisUtils.newPair(newM, item));

            if (t.id().equals(oldSelectedId)) {
                moveMapToMarker(pair.first);
                taxiPagerAdapter.setCurrentlySelectedTaxiId(t.id());
            }
        }
    }

    private MarkerOptions instantiateMarkerForTaxi(Taxi t) {
        MarkerOptions options = new MarkerOptions()
                .data(t.id())
                .position(t.currentPosition())
                .title(t.name())
                .snippet("Tap to select this Taxi")
                .visible(true);

        if(t.isAvailable()) {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_available));
        } else {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_unavailable));
        }

        return options;
    }

    /**
     * Instructs both the selector object (ViewPager or ListView) to scroll and select
     * the element at the specified positional index.
     *
     * @param position the index of the element that the users wants to scroll to.
     */
    public final void select(int position) {
        int maxSize = taxiPagerAdapter.getCount();
        if(position >= maxSize) {
            Log.e("Position is greater than Adapter size", "Position:" + position + " | AdapterSize:" + maxSize);
            return;
        }
        TaxiItem selectedItem = taxiPagerAdapter.getItem(position);
        String selectedItemID = selectedItem.getTaxiObject().id();

        Pair<Marker, TaxiItem> matchedPair = itemToMarkerMappings.get(selectedItemID);
        taxiPagerAdapter.setCurrentlySelectedTaxiId(matchedPair.second.getTaxiObject().id());
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
        String selectedMarkerID = marker.getData();
        if (selectedMarkerID == null) {
            // its the user marker
            myLocationListener.onMyLocationButtonClick();
            return;
        }

        Pair<Marker, TaxiItem> matchedPair = itemToMarkerMappings.get(selectedMarkerID);
        int position = taxiPagerAdapter.getItemPosition(matchedPair.second);
        taxiPagerAdapter.setCurrentlySelectedTaxiId(matchedPair.second.getTaxiObject().id());
        finishSelect(position, marker);
    }

    /**
     * Method used by both "select" methods to do some common selecting operations.
     * The user must override and implement this method to contain some extra operations
     * associated withUser the selector, whose variable this class has no access to. These extra
     * operations will be executed at every "select" instruction.
     *
     * @param position the position of the selected element
     * @param marker the marker that corresponds to the selected element
     */
    protected void finishSelect(int position, Marker marker){
        taxiPager.setCurrentItem(position, true);
        myLocationToggle = false;
        moveMapToMarker(marker);
    }

    /**
     * Method used by both "select" methods to do some common selecting operations.
     */
    private void moveMapToMarker(final Marker marker){
        marker.showInfoWindow();
        map.moveCamera(CameraUpdateFactory.zoomTo(17.0f));
        LatLng newPos = marker.getPosition();
        LatLng realNewPos = new LatLng(newPos.latitude+0.002, newPos.longitude);
        map.animateCamera(CameraUpdateFactory.newLatLng(realNewPos), 900, null);
    }


    public void attemptCloseSlidingPane(MotionEvent event){
        if(slidingPaneLayout!=null && slidingPaneLayout.isOpened()){

            int location[] = { 0, 0 };
            slidingPaneLayout.getLocationOnScreen(location);

            if(((int)event.getRawY()) < location[1]) {
                slidingPaneLayout.closeLayer(true);
            }
        }
    }

    public void closeSlidingPane() {
        slidingPaneLayout.closeLayer(true);
    }


    /**
     * Slides the options page into view.
     */
    public void showOptionsPane(Taxi selectedTaxi){
        parentActivity.getRideBuilder().setTaxi(selectedTaxi);
        rideOptionsPager.setCurrentItem(0, true);

        BootstrapButton hereAndNowButton = (BootstrapButton) parentActivity.findViewById(R.id.btHereAndNow);
        hereAndNowButton.setEnabled(selectedTaxi.isAvailable());
        hereAndNowButton.setClickable(selectedTaxi.isAvailable());

        LatLng pos = ((TravisApplication) getActivity().getApplication()).getCurrentLocation();
        TravisUtils.addressesFromLocation(parentActivity, pos.latitude, pos.longitude, new Callback<List<Address>>() {
            @Override
            public void onResult(List<Address> result) {
                Address currentAddress = result.get(0);

                TextView addressTextView = (TextView) parentActivity.findViewById(R.id.origin_address);
                addressTextView.setText(TravisUtils.addressToString(currentAddress));

                TextView timeTextView = (TextView) parentActivity.findViewById(R.id.time_picker_text);
                parentActivity.setTimeTextToNow(timeTextView);

                slidingPaneLayout.openLayer(true);
            }
        });
    }

    /**
     * Shows the specified page (of two).
     */
    public void optionsPaneGoToPage(int page){
        if(page==0 || page==1){
            rideOptionsPager.setCurrentItem(page, true);
        }
    }

    /**
     * Callback method to be invoked when an item in the spinner has been
     * selected. This callback is invoked only when the newly selected
     * position is different from the previously selected position or if
     * there was no selected item.
     *
     * @param parent   The AdapterView where the selection happened
     * @param view     The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id       The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Callback<List<Taxi>> callback = new Callback<List<Taxi>>() {
            @Override
            public void onResult(List<Taxi> result) {
                updateTaxis(result);
                pullToRefreshLayout.setRefreshComplete();
            }
        };
        pullToRefreshLayout.setRefreshing(true);

        switch (position){
            case 0: // Near you was selected
                parentActivity.getNearTaxiList(false, callback);
                break;
            case 1: // Highest rated was selected
                parentActivity.getHighestRatedTaxiList(false, callback);
                break;
            case 2: // Your favorites was selected
                parentActivity.getFavoriteTaxiList(false, callback);
                break;
//            case 3: // Search selected
//                SearchManager mgr = ((SearchManager) parentActivity.getSystemService(Context.SEARCH_SERVICE));
//                mgr.setOnDismissListener(new SearchManager.OnDismissListener() {
//                    @Override
//                    public void onDismiss() {
//                        spinner.setSelection(0, true);
//                    }
//                });
//                mgr.startSearch(null, false, parentActivity.getComponentName(), null, false);
//                break;
        }

    }

    /**
     * Callback method to be invoked when the selection disappears from this
     * view. The selection can disappear for instance when touch is activated
     * or when the adapter becomes empty.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    private class TaxiItemAdapter extends FragmentStatePagerAdapter {

        private String currentlySelectedId;

        private TaxiItemAdapter(FragmentManager manager){
            super(manager);
            this.currentlySelectedId = null;
        }

        @Override
        public TaxiItem getItem(int position) {
            int i = 0;
            Collection<Pair<Marker, TaxiItem>> orderedItems = itemToMarkerMappings.values();
            for (Pair<Marker, TaxiItem> pair : orderedItems) {
                if(i == position){
                    return pair.second;
                }
                i++;
            }
            return null;
        }

        @Override
        public int getItemPosition(Object object) {
        /*
         * Purpose of this method is to check whether an item in the adapter
         * still exists in the itemList and where it should show.
         * For each entry in itemList, request its Fragment.
         *
         * If the Fragment is found, return its (new) position. There's
         * no need to return POSITION_UNCHANGED; ViewPager handles it.
         *
         * If the Fragment passed to this method is not found, remove all
         * references and let the ViewPager remove it from display by
         * by returning POSITION_NONE;
         */
            TaxiItem f = (TaxiItem) object;

            for(int i = 0; i < getCount(); i++) {

                TaxiItem item = getItem(i);
                if(item.equals(f)) {
                    // item still exists in itemList; return position
                    return i;
                }
            }

            // if we arrive here, the data-item for which the Fragment was created
            // does not exist anymore.

            // Also, cleanup: remove reference to Fragment from mItems
            for(Map.Entry<String, Pair<Marker, TaxiItem>> entry : itemToMarkerMappings.entrySet()) {
                if(entry.getValue().second.equals(f)) {
                    itemToMarkerMappings.remove(entry.getKey());
                    break;
                }
            }

            // Let ViewPager remove the Fragment by returning POSITION_NONE.
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return itemToMarkerMappings.size();
        }

        public void setCurrentlySelectedTaxiId(String id) {
            currentlySelectedId = id;
        }

        public String getCurrentlySelectedTaxiId(){
            return currentlySelectedId;
        }
    }
}