package pt.ua.travis.gui.taxichooser;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

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
    public int getItemPosition(Object object) {
        return itemList.indexOf(object);
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