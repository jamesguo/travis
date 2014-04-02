package pt.ua.travis.gui.ridelist;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import pt.ua.travis.core.Ride;
import pt.ua.travis.gui.main.MainActivity;

import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideItemListAdapter extends BaseAdapter implements ListAdapter {

    private MainActivity parentActivity;
    private SparseArray<RideItem> itemList;

    RideItemListAdapter(MainActivity parentActivity, int showWhat, List<Ride> rides) {
        this.parentActivity = parentActivity;
        this.itemList = new SparseArray<RideItem>();

        for (Ride r : rides) {
            itemList.put(r.id, RideItem.newInstance(showWhat, r, parentActivity));
        }
    }

    void remove(Ride ride){
        itemList.remove(ride.id);
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
