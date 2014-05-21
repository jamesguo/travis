package pt.ua.travis.ui.taxichooser;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import pt.ua.travis.R;

import java.util.List;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TaxiItemPagerAdapter extends FragmentStatePagerAdapter implements TaxiItemAdapter {

    private List<TaxiItem> itemList;
    private int currentlySelectedIndex;

    TaxiItemPagerAdapter(FragmentManager manager, List<TaxiItem> itemList){
        super(manager);
        this.itemList = itemList;
        this.currentlySelectedIndex = 0;
    }

    @Override
    public int getItemPosition(TaxiItem item) {
        return itemList.indexOf(item);
    }

    @Override
    public TaxiItem getItem(int i) {
        return itemList.get(i);
    }

    @Override
    public int getCount() {
        return itemList.size();
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