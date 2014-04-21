package pt.ua.travis.ui.ridelist;

import android.os.Bundle;
import com.actionbarsherlock.app.SherlockListFragment;
import com.google.common.collect.Lists;
import pt.ua.travis.backend.entities.Ride;
import pt.ua.travis.ui.mainscreen.MainActivity;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideListFragment extends SherlockListFragment {

    private static final String SAVED_SHOW_WHAT = "saved_show_what";
    private static final String SAVED_RIDE_LIST = "saved_ride_list";

    private int showWhat;
    private ArrayList<Ride> rideList;

    public static RideListFragment newInstance(int showWhat, List<Ride> rideList) {
        RideListFragment instance = new RideListFragment();
        instance.showWhat = showWhat;
        instance.rideList = Lists.newArrayList();
        return instance;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState!=null) {
            showWhat = savedInstanceState.getInt(SAVED_SHOW_WHAT);
            rideList = (ArrayList<Ride>) savedInstanceState.getSerializable(SAVED_RIDE_LIST);
        }

        RideItemListAdapter adapter = new RideItemListAdapter(((MainActivity)getSherlockActivity()), showWhat, rideList);
        setListAdapter(adapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SAVED_SHOW_WHAT, showWhat);
        outState.putSerializable(SAVED_RIDE_LIST, rideList);
    }
}