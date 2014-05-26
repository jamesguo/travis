package pt.ua.travis.ui.ridelist;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import pt.ua.travis.backend.Callback;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Ride;
import pt.ua.travis.backend.User;
import pt.ua.travis.ui.main.MainActivity;
import pt.ua.travis.utils.Utils;

import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideItemListAdapter extends BaseAdapter implements ListAdapter {

    private MainActivity parentActivity;
    private ArrayMap<String, RideItem> itemList;

    RideItemListAdapter(final MainActivity parentActivity, final int showWhat) {
        this.parentActivity = parentActivity;
        this.itemList = Utils.newMap();

        parentActivity.getRideList(false, new Callback<List<Ride>>() {
            @Override
            public void onResult(List<Ride> result) {
                for (Ride r : result) {
                    itemList.put(r.id(), RideItem.newInstance(showWhat, r, parentActivity));
                }
            }
        });
    }

    void remove(Ride ride){
        itemList.remove(ride.id());
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public RideItem getItem(int position) {
        return itemList.valueAt(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater vi = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RideItem item = getItem(position);
        item.onAttach(parentActivity);
        View v = item.onCreateView(vi, parent, null);

        return v;
    }
}
