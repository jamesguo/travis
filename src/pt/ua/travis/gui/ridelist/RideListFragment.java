package pt.ua.travis.gui.ridelist;

import android.os.Bundle;
import com.actionbarsherlock.app.SherlockListFragment;
import pt.ua.travis.core.Ride;
import pt.ua.travis.db.PersistenceManager;
import pt.ua.travis.gui.main.MainActivity;
import pt.ua.travis.gui.main.MainClientActivity;

import java.util.List;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideListFragment extends SherlockListFragment {

    private int showWhat;
    private List<Ride> rideList;

    public static RideListFragment newInstance(int showWhat, List<Ride> rideList) {
        RideListFragment instance = new RideListFragment();
        instance.showWhat = showWhat;
        instance.rideList = rideList;
        return instance;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RideItemListAdapter adapter = new RideItemListAdapter(((MainActivity)getSherlockActivity()), showWhat, rideList);
        setListAdapter(adapter);
    }
}