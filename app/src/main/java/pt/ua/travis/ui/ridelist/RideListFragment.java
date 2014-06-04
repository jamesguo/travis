package pt.ua.travis.ui.ridelist;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.google.common.collect.Lists;
import pt.ua.travis.R;
import pt.ua.travis.backend.Callback;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Ride;
import pt.ua.travis.ui.customviews.TravisFragment;
import pt.ua.travis.ui.main.MainActivity;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import java.util.List;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideListFragment extends TravisFragment implements OnRefreshListener {

    private static final String SAVED_SHOW_WHAT = "saved_show_what";
    private static final String SAVED_RIDE_LIST = "saved_ride_list";

    private int showWhat;

    private MainActivity parentActivity;
    private PullToRefreshLayout pullToRefreshLayout;
    private SwipeListView listView;
    private RideListAdapter adapter;

    public static RideListFragment newInstance(int showWhat) {
        RideListFragment instance = new RideListFragment();
        instance.showWhat = showWhat;
        return instance;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parentActivity = (MainActivity) activity;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentView(R.layout.fragment_ride_list);
        listView = (SwipeListView) parentActivity.findViewById(R.id.ride_list);

        parentActivity.getRideList(false, new Callback<List<Ride>>() {
            @Override
            public void onResult(List<Ride> result) {
                adapter = new RideListAdapter(parentActivity, convertRidesToItems(showWhat, result));
                listView.setAdapter(adapter);

                pullToRefreshLayout = (PullToRefreshLayout) getActivity().findViewById(R.id.pull_to_refresh_ride_list);
                ActionBarPullToRefresh.from(getSherlockActivity())
                        .options(Options.create()
                                .refreshOnUp(true)
                                .scrollDistance(3.5f)
                                .build())
                        .allChildrenArePullable()
                        .listener(RideListFragment.this)
                        .setup(pullToRefreshLayout);

                setContentShown(true);
            }
        });


    }

    private static List<RideItem> convertRidesToItems(int showWhat, List<Ride> rides) {
        List<RideItem> items = Lists.newArrayList();
        for (Ride r : rides) {
            items.add(RideItem.newInstance(showWhat, r));
        }
        return items;
    }

    @Override
    public void onRefreshStarted(View view) {
        parentActivity.getRideList(true, new Callback<List<Ride>>() {
            @Override
            public void onResult(List<Ride> result) {
                adapter.update(convertRidesToItems(showWhat, result));
                pullToRefreshLayout.setRefreshComplete();
            }
        });
    }
}