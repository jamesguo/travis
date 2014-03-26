package pt.ua.travis.gui.taxichooser;

import android.app.Activity;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import pt.ua.travis.R;

import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */

public class TaxiItemListAdapter extends BaseAdapter implements TaxiItemAdapter {

    private Activity context;
    private List<TaxiItem> itemList;
    private int currentlySelectedIndex;
    private SparseArray<View> loadedViews;

    TaxiItemListAdapter(Activity context, List<TaxiItem> itemList){
        super();
        this.context = context;
        this.itemList = itemList;
        this.currentlySelectedIndex = 0;
        this.loadedViews = new SparseArray<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TaxiItem item = itemList.get(position);
        View v = loadedViews.get(item.getTaxiObject().id);

        if(v==null) {
            item.onAttach(context);
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = item.onCreateView(vi, null, null);
            loadedViews.put(item.getTaxiObject().id, v);
        }

        if (position == currentlySelectedIndex) {
            v.setBackgroundColor(context.getResources().getColor(R.color.selectorSelectedBg));
        } else {
            v.setBackgroundColor(context.getResources().getColor(R.color.mainBg));
        }

        return v;
    }

    @Override
    public int getItemPosition(TaxiItem item) {
        return itemList.indexOf(item);
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public TaxiItem getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return itemList.get(position).getId();
    }

    @Override
    public void setSelectedIndex(int position){
        currentlySelectedIndex = position;
    }

    @Override
    public int getSelectedIndex(){
        return currentlySelectedIndex;
    }
}
