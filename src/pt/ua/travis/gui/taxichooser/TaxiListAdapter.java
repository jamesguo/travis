package pt.ua.travis.gui.taxichooser;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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
public class TaxiListAdapter extends BaseAdapter implements TaxiItemAdapter {

    private Bundle savedInstanceState;
    private Activity context;
    private List<TaxiItem> itemList;

    private int mSelectedItem;

    TaxiListAdapter(Bundle savedInstanceState, Activity context, List<TaxiItem> itemList){
        super();
        this.savedInstanceState = savedInstanceState;
        this.context = context;
        this.itemList = itemList;
        this.mSelectedItem = 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = itemList.get(position).onCreateView(vi, parent, savedInstanceState);

        if (position == mSelectedItem) {
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
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return itemList.get(position).getId();
    }


    @Override
    public void setSelectedIndex(int position){
        mSelectedItem = position;
    }

    @Override
    public int getCurrentSelectedIndex(){
        return mSelectedItem;
    }
}
