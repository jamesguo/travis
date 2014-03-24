package pt.ua.travis.gui.taxichooser;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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

    private Bundle savedInstanceState;
    private Activity context;
    private List<TaxiItem> itemList;
    private int currentlySelectedIndex;

    TaxiItemListAdapter(Bundle savedInstanceState, Activity context, List<TaxiItem> itemList){
        super();
        this.savedInstanceState = savedInstanceState;
        this.context = context;
        this.itemList = itemList;
        this.currentlySelectedIndex = 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TaxiItem item = itemList.get(position);
        View v = item.onCreateView(vi, parent, savedInstanceState);

        if (position == currentlySelectedIndex) {
            Log.e("VIEWSSS", item.getTaxiObject().name);
            v.setBackgroundColor(context.getResources().getColor(R.color.selectorSelectedBg));
        }

        return v;
    }

    @Override
    public int getItemPosition(Object object) {
        return itemList.indexOf(object);
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
