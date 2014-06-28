package pt.ua.travis.ui.ridelist;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.google.common.collect.Lists;
import pt.ua.travis.R;
import pt.ua.travis.backend.Callback;
import pt.ua.travis.backend.Ride;
import pt.ua.travis.core.BaseFragment;
import pt.ua.travis.ui.customviews.CustomViewDelegate;
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
public class RideListFragment extends BaseFragment implements OnRefreshListener {

    private static final String SAVED_SHOW_WHAT = "saved_show_what";
    private static final String SAVED_RIDE_LIST = "saved_ride_list";

    private int showWhat;

    private MainActivity parentActivity;
    private PullToRefreshLayout pullToRefreshLayout;
    private RecyclerView listView;
    private RecyclerView.LayoutManager layoutManager;
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
        listView = (RecyclerView) parentActivity.findViewById(R.id.ride_list);

        // improves performance since changes in content do not change the size of the RecyclerView
        listView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(parentActivity);
        listView.setLayoutManager(layoutManager);




        parentActivity.getRideList(false, new Callback<List<Ride>>() {
            @Override
            public void onResult(List<Ride> result) {
                adapter = new RideListAdapter(parentActivity, RideListFragment.this, convertRidesToItems(showWhat, result));
                listView.setAdapter(adapter);

                pullToRefreshLayout = (PullToRefreshLayout) getActivity().findViewById(R.id.pull_to_refresh_ride_list);
                ActionBarPullToRefresh.from(getSherlockActivity())
                        .options(Options.create()
                                .refreshOnUp(true)
                                .build())
                        .useViewDelegate(RecyclerView.class, new CustomViewDelegate())
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
        pullToRefreshLayout.setRefreshing(true);
        parentActivity.getRideList(true, new Callback<List<Ride>>() {
            @Override
            public void onResult(List<Ride> result) {
                adapter.update(convertRidesToItems(showWhat, result));
                pullToRefreshLayout.setRefreshComplete();
            }
        });
    }
}