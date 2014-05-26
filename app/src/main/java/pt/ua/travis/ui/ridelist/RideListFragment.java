package pt.ua.travis.ui.ridelist;

import android.os.Bundle;
import com.actionbarsherlock.app.SherlockListFragment;
import com.google.common.collect.Lists;
import pt.ua.travis.backend.Ride;
import pt.ua.travis.ui.customviews.TravisFragment;
import pt.ua.travis.ui.customviews.TravisListFragment;
import pt.ua.travis.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideListFragment extends TravisListFragment {

    private static final String SAVED_SHOW_WHAT = "saved_show_what";
    private static final String SAVED_RIDE_LIST = "saved_ride_list";

    private int showWhat;

    public static RideListFragment newInstance(int showWhat) {
        RideListFragment instance = new RideListFragment();
        instance.showWhat = showWhat;
        return instance;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RideItemListAdapter adapter = new RideItemListAdapter(((MainActivity)getSherlockActivity()), showWhat);
        setListAdapter(adapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        setContentShown(true);
    }
}