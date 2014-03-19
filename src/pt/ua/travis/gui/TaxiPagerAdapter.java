package pt.ua.travis.gui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TaxiPagerAdapter extends FragmentPagerAdapter {

    private List<TaxiItem> itemList;

    TaxiPagerAdapter(FragmentManager manager, List<TaxiItem> itemList){
        super(manager);
        this.itemList = itemList;
    }

    void add(TaxiItem item){
        itemList.add(item);
    }

    @Override
    public int getItemPosition(Object object) {
        return itemList.indexOf(object);
    }

    @Override
    public Fragment getItem(int i) {
        return itemList.get(i);
    }

    @Override
    public int getCount() {
        return itemList.size();
    }
}