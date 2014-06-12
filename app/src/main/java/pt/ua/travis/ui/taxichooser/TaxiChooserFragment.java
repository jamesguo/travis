package pt.ua.travis.ui.taxichooser;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
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
import pt.ua.travis.core.TravisApplication;
import pt.ua.travis.core.TravisFragment;
import pt.ua.travis.core.TravisMapFragment;
import pt.ua.travis.ui.customviews.*;
import pt.ua.travis.ui.main.MainClientActivity;
import pt.ua.travis.ui.riderequest.RideRequestPagerAdapter;
import pt.ua.travis.utils.CommonKeys;
import pt.ua.travis.utils.Pair;
import pt.ua.travis.utils.TravisUtils;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TaxiChooserFragment extends TravisFragment
        implements AdapterView.OnItemSelectedListener, OnRefreshListener {

    protected MainClientActivity parentActivity;

    private Map<String, Pair<Marker, TaxiItem>> itemToMarkerMappings;

    private GoogleMap map;

    private GoogleMap.OnMyLocationButtonClickListener myLocationListener;

    private boolean myLocationToggle;

    protected TaxiItemAdapter taxiPagerAdapter;

    private ViewPager taxiPager;

    private PullToRefreshLayout pullToRefreshLayout;

    private RideRequestViewPager rideOptionsPager;

    private SlidingPaneLayout slidingPaneLayout;



    public TaxiChooserFragment() {
        itemToMarkerMappings = TravisUtils.newMap();
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

        TravisMapFragment mapFragment = (TravisMapFragment) parentActivity
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
                if(!myLocationToggle){
                    TaxiItem selectedItem = taxiPagerAdapter.getItem(taxiPagerAdapter.getCurrentPosition());
                    String selectedItemID = selectedItem.getTaxiObject().id();

                    Pair<Marker, TaxiItem> matchedPair = itemToMarkerMappings.get(selectedItemID);

                    lastMarker = matchedPair.first;
                    LatLng userPosition = ((TravisApplication) getActivity().getApplication()).getCurrentLocation();
                    LatLng realNewPos = new LatLng(userPosition.latitude+0.002, userPosition.longitude);
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


        taxiPager = (ViewPager) getActivity().findViewById(R.id.taxi_pager);
        taxiPagerAdapter = new TaxiItemAdapter(getChildFragmentManager(), new ArrayList<Taxi>());
        taxiPager.setAdapter(taxiPagerAdapter);
        taxiPager.setOffscreenPageLimit(3);
//        taxiPager.setTransitionEffect(TransitionViewPager.TransitionEffect.Standard);
//        taxiPager.setFadeEnabled(true);
        taxiPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int i) {
                select(i);
                taxiPagerAdapter.setCurrentPosition(i);
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
                .useViewDelegate(ViewPager.class, new ViewPagerDelegate())
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
                select(0);
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
        itemToMarkerMappings.clear();
        taxiPagerAdapter.update(newTaxis);
        updateMarkers(newTaxis);
    }


    /**
     * Adds markers for each taxi on the specified list and to link
     * each marker with an item fragment, that is displayed on the ViewPager (if the
     * orientation is portrait) or the ListView (if the orientation is landscape).
     *
     * @param taxis the specified taxi list whose markers will be added to the map
     */
    protected final void updateMarkers(List<Taxi> taxis){
        map.clear();

        for (Taxi t : taxis) {
            Marker m = map.addMarker(getMarkerOptions(t));
            TaxiItem item = taxiPagerAdapter.idsToItems.get(t.id());
            itemToMarkerMappings.put(t.id(), TravisUtils.newPair(m, item));
        }

    }


    public final void updateTaxiLocationsOnly(List<Taxi> taxis) {
        int selectedIndex = taxiPagerAdapter.currentlySelectedIndex;
        for (Taxi t : taxis) {
            String id = t.id();

            Pair<Marker, TaxiItem> pair = itemToMarkerMappings.get(id);
            Marker oldM = pair.first;
            oldM.setVisible(false);
            oldM.remove();

            Marker newM = map.addMarker(getMarkerOptions(t));

            itemToMarkerMappings.put(id, TravisUtils.newPair(newM, pair.second));


            int thisItemIndex = taxiPagerAdapter.getItemPosition(pair.second);
            if (thisItemIndex == selectedIndex) {
                moveMapToMarker(newM);
            }
        }
    }

    private MarkerOptions getMarkerOptions(Taxi t) {
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
        taxiPagerAdapter.setCurrentPosition(position);
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
        Address currentAddress = TravisUtils.addressesFromLocation(parentActivity, pos.latitude, pos.longitude).get(0);

        TextView addressTextView = (TextView) parentActivity.findViewById(R.id.origin_address);
        addressTextView.setText(TravisUtils.addressToString(currentAddress));

        TextView timeTextView = (TextView) parentActivity.findViewById(R.id.time_picker_text);
        parentActivity.setTimeTextToNow(timeTextView);

        slidingPaneLayout.openLayer(true);
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
                select(0);
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

        private Map<String, TaxiItem> idsToItems = TravisUtils.newMap();
        private List<Taxi> taxiList;
        private int currentlySelectedIndex;

        private Client thisClient;

        private TaxiItemAdapter(FragmentManager manager, List<Taxi> taxiList){
            super(manager);
            this.taxiList = taxiList;
            this.currentlySelectedIndex = 0;

            thisClient = PersistenceManager.getCurrentlyLoggedInUser();
        }

        public void update(List<Taxi> newTaxiList){
            taxiList.clear();
            idsToItems.clear();
            taxiList.addAll(newTaxiList);
            notifyDataSetChanged();
        }

        @Override
        public TaxiItem getItem(int position) {
            String id = getItemId(position);

            if(idsToItems.containsKey(id)) {
                // caching to prevent multiple instances of the same fragment
                // for the same position/id
                return idsToItems.get(id);
            }

            TaxiItem f = TaxiItem.create(parentActivity, thisClient, taxiList.get(position));
            idsToItems.put(id, f);
            return f;
        }

        public String getItemId(int position) {
            // return a unique id
            return taxiList.get(position).id();
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
            for(Map.Entry<String, TaxiItem> entry : idsToItems.entrySet()) {
                if(entry.getValue().equals(f)) {
                    idsToItems.remove(entry.getKey());
                    break;
                }
            }

            // Let ViewPager remove the Fragment by returning POSITION_NONE.
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return taxiList.size();
        }

        public void setCurrentPosition(int position) {
            currentlySelectedIndex = position;
        }

        public int getCurrentPosition(){
            return currentlySelectedIndex;
        }
    }
}