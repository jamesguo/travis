package pt.ua.travis.gui.ridelist;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import pt.ua.travis.R;
import pt.ua.travis.core.Ride;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideItemListAdapter extends BaseAdapter implements ListAdapter {

    private Context context;
    private SparseArray<RideItem> itemList;

    int currentlySelectedIndex;

    RideItemListAdapter(Context context, List<Ride> rides) {
        this.context = context;
        this.itemList = new SparseArray<>();

        for (Ride r : rides) {
            itemList.put(r.id, RideItem.newInstance(r));
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
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = getItem(position).onCreateView(vi, parent, null);

        if (position == currentlySelectedIndex) {
            v.setBackgroundColor(context.getResources().getColor(R.color.selectorSelectedBg));
        }

        return v;
    }
}
