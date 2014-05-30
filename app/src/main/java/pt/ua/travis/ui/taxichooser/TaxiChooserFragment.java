package pt.ua.travis.ui.taxichooser;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.AsyncTask;
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
import android.widget.TimePicker;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Lists;
import pt.ua.travis.R;
import pt.ua.travis.backend.Callback;
import pt.ua.travis.backend.Client;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Taxi;
import pt.ua.travis.core.TravisApplication;
import pt.ua.travis.ui.customviews.*;
import pt.ua.travis.ui.main.MainClientActivity;
import pt.ua.travis.ui.riderequest.RideRequestPagerAdapter;
import pt.ua.travis.utils.CommonKeys;
import pt.ua.travis.utils.Pair;
import pt.ua.travis.utils.Utils;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


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

    private TransitionViewPager taxiPager;

    private PullToRefreshLayout pullToRefreshLayout;

    private RideRequestViewPager rideOptionspager;

    private SlidingPaneLayout slidingPaneLayout;



    public TaxiChooserFragment() {
        itemToMarkerMappings = Utils.newMap();
    }


//    @Override
//    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        if (lastUsedView != null) {
//            ViewGroup parent = (ViewGroup) lastUsedView.getParent();
//            if (parent != null)
//                parent.removeView(lastUsedView);
//        } else {
//            try {
//                lastUsedView = inflater.inflate(R.layout.fragment_taxi_chooser, null);
//            } catch (InflateException e) {
//                // map is already there, just return view as it is
//            }
//        }
//        return lastUsedView;
//    }


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

        // TODO: SHOW LOADING BAR

        parentActivity.getNearTaxiList(false, new Callback<List<Taxi>>() {
            @Override
            public void onResult(List<Taxi> result) {
                initializeFragmentViews();
            }
        });
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


        taxiPager = (TransitionViewPager) getActivity().findViewById(R.id.taxi_pager);
        taxiPagerAdapter = new TaxiItemAdapter(getChildFragmentManager());
        taxiPager.setAdapter(taxiPagerAdapter);
        taxiPager.setOffscreenPageLimit(5);
        taxiPager.setTransitionEffect(TransitionViewPager.TransitionEffect.Standard);
        taxiPager.setFadeEnabled(true);
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


        pullToRefreshLayout = (PullToRefreshLayout) getActivity().findViewById(R.id.pull_to_refresh_layout);
        ActionBarPullToRefresh.from(getSherlockActivity())
                .options(Options
                        .create()
                        .scrollDistance(3.5f)
                        .build())
                .allChildrenArePullable()
                .useViewDelegate(TransitionViewPager.class, new TransitionViewPagerDelegate())
                .listener(this)
                .setup(pullToRefreshLayout);

//        // sets how much of the next item is shown
//        if(Validate.isTablet(getActivity())){
//            taxiPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.viewpager_margin));
//        } else {
//            taxiPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.viewpager_margin));
//        }

        // selects the item that was selected in the previous fragment
//        int indexToSelect = getArguments().getInt(CommonKeys.SELECTED_INDEX);



        rideOptionspager = (RideRequestViewPager) parentActivity.findViewById(R.id.sliding_pane_pager);
        rideOptionspager.setPageTransformer(false, new SlidingPaneTransformer(rideOptionspager));
        rideOptionspager.setAdapter(new RideRequestPagerAdapter(parentActivity.getSupportFragmentManager()));
        rideOptionspager.setOffscreenPageLimit(1);
        slidingPaneLayout = (SlidingPaneLayout) parentActivity.findViewById(R.id.sliding_pane_layout);
        slidingPaneLayout.setStickTo(SlidingPaneLayout.STICK_TO_BOTTOM);



        Spinner spinner = (Spinner) parentActivity.findViewById(R.id.taxi_sort_spinner);
        spinner.setAdapter(new SortSpinnerAdapter(parentActivity));
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
//                taxiPagerAdapter.update(convertTaxisToItems(result));
//                select(0);
//                setContentShown(true);
//            }
//
//        }.execute();
//
        parentActivity.getCurrentlyShownTaxiList(false, new Callback<List<Taxi>>() {
            @Override
            public void onResult(List<Taxi> result) {
                updateTaxiViews(result);
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
        new AsyncTask<Void, Void, Void>() {

            private List<Taxi> queryResult;


            @Override
            protected Void doInBackground(Void... params) {
                final AtomicReference<Boolean> lock = new AtomicReference<Boolean>(true);

                parentActivity.getCurrentlyShownTaxiList(true, new Callback<List<Taxi>>() {
                    @Override
                    public void onResult(List<Taxi> result) {
                        queryResult = result;
                        lock.set(false);
                    }
                });

                while (lock.get()){
                    // LOCK "DO IN BACKGROUND" UNTIL QUERY IS DONE
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                updateTaxiViews(queryResult);
                select(0);
                pullToRefreshLayout.setRefreshComplete();
            }

        }.execute();
    }

    public final void updateTaxiViews(List<Taxi> taxi) {
        taxiPagerAdapter.itemList = convertTaxisToItems(taxi);
        taxiPagerAdapter.notifyDataSetChanged();
        taxiPager.destroyDrawingCache();
    }

    public final void updateTaxiLocations(List<Taxi> taxis) {
        int selectedIndex = taxiPagerAdapter.currentlySelectedIndex;
        for (Taxi t : taxis) {
            String id = t.id();

            Pair<Marker, TaxiItem> pair = itemToMarkerMappings.get(id);
            Marker oldM = pair.first;
            oldM.setVisible(false);
            oldM.remove();

            Marker newM = map.addMarker(new MarkerOptions()
                    .data(id)
                    .position(t.currentPosition())
                    .title(t.name())
                    .snippet("Tap to select this Taxi")
                    .visible(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)));

            itemToMarkerMappings.put(id, Utils.newPair(newM, pair.second));


            int thisItemIndex = taxiPagerAdapter.getPositionOfItem(pair.second);
            if (thisItemIndex == selectedIndex) {
                moveMapToMarker(newM);
            }
        }
    }


    /**
     * Utility method to add markers for each taxi on the specified list and to link
     * each marker withUser an item fragment, that is displayed on the ViewPager (if the
     * orientation is portrait) or the ListView (if the orientation is landscape).
     *
     * @param taxis the specified taxi list whose markers will be added to the map
     * @return the list of item fragments resultant from this conversion and
     *         marker generation
     */
    protected final List<TaxiItem> convertTaxisToItems(List<Taxi> taxis){
        List<TaxiItem> taxiItemList = Lists.newArrayList();
        Client c = PersistenceManager.getCurrentlyLoggedInUser();

        for (Taxi t : taxis) {

            Marker m = map.addMarker(new MarkerOptions()
                    .data(t.id())
                    .position(t.currentPosition())
                    .title(t.name())
                    .snippet("Tap to select this Taxi")
                    .visible(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)));

            TaxiItem item = TaxiItem.newInstance(c, t);
            taxiItemList.add(item);
            itemToMarkerMappings.put(t.id(), Utils.newPair(m, item));
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
        int position = taxiPagerAdapter.getPositionOfItem(matchedPair.second);
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


    /**
     * Slides the options page into view.
     */
    public void showOptionsPane(Taxi selectedTaxi){
        parentActivity.getRideBuilder().setTaxi(selectedTaxi);
        rideOptionspager.setCurrentItem(0, true);

        BootstrapButton hereAndNowButton = (BootstrapButton) parentActivity.findViewById(R.id.btHereAndNow);
        hereAndNowButton.setEnabled(selectedTaxi.isAvailable());
        hereAndNowButton.setClickable(selectedTaxi.isAvailable());

        LatLng pos = ((TravisApplication) getActivity().getApplication()).getCurrentLocation();
        Address currentAddress = Utils.addressesFromLocation(parentActivity, pos.latitude, pos.longitude).get(0);

        TextView addressTextView = (TextView) parentActivity.findViewById(R.id.origin_address);
        addressTextView.setText(Utils.addressToString(currentAddress));

        TimePicker timePicker = (TimePicker) parentActivity.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minute);

        slidingPaneLayout.openLayer(true);
    }

    /**
     * Shows the specified page (of two).
     */
    public void optionsPaneGoToPage(int page){
        if(page==0 || page==1){
            rideOptionspager.setCurrentItem(page, true);
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
                updateTaxiViews(result);
                select(0);
                setContentShown(true);
            }
        };
        setContentShown(false);

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
            case 3: // Search selected
                break;
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

        private List<TaxiItem> itemList;
        private int currentlySelectedIndex;

        private TaxiItemAdapter(FragmentManager manager){
            super(manager);
            this.itemList = Lists.newArrayList();
            this.currentlySelectedIndex = 0;
        }

        @Override
        public int getItemPosition(Object object) {
//            if(object instanceof TaxiItem) {
//                TaxiItem item = (TaxiItem) object;
//                Client c = PersistenceManager.query().clients().loggedInThisDevice();
//                TaxiItem.paintViewWithTaxi(parentActivity, item.getView(), c, item.getTaxiObject());
////                return getPositionOfItem(item);
//            }
            return POSITION_NONE;
        }

        public int getPositionOfItem(TaxiItem item) {
            int i = 0;
            for (TaxiItem it : itemList) {
                if (it.getTaxiObject().id().equals(item.getTaxiObject().id())) {
                    return i;
                }
                i++;
            }
            return -1;
        }

        public TaxiItem getItem(int position) {
            TaxiItem item = itemList.get(position);
            taxiPager.setObjectForPosition(item, position);
            return item;
        }

        @Override
        public int getCount() {
            return itemList.size();
        }

        public void setCurrentPosition(int position) {
            currentlySelectedIndex = position;
        }

        public int getCurrentPosition(){
            return currentlySelectedIndex;
        }
    }
}