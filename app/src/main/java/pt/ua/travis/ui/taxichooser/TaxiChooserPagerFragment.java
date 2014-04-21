package pt.ua.travis.ui.taxichooser;

import android.support.v4.view.ViewPager;
import com.androidmapsextensions.Marker;
import pt.ua.travis.R;
import pt.ua.travis.backend.Taxi;
import pt.ua.travis.ui.mainscreen.MainClientActivity;
import pt.ua.travis.utils.CommonKeys;

import java.util.List;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TaxiChooserPagerFragment extends TaxiChooserFragment {

    private ViewPager taxiSelector;


    @Override
    public void onStart() {
        super.onStart();
        List<Taxi> taxis = MainClientActivity.getCurrentTaxiListState();

        taxiSelector = (ViewPager) getActivity().findViewById(R.id.pager);
        final TaxiItemPagerAdapter taxiAdapter = new TaxiItemPagerAdapter(getChildFragmentManager(), convertTaxisToItems(taxis));
        this.itemAdapter = taxiAdapter;

        // selector (horizontal list) configurations
        taxiSelector.setOffscreenPageLimit(5);
        taxiSelector.setAdapter(taxiAdapter);
        taxiSelector.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int i) {
                select(i);
                taxiAdapter.setSelectedIndex(i);
            }

            @Override
            public void onPageScrolled(int i, float v, int i2) {}

            @Override
            public void onPageScrollStateChanged(int i) {}
        });

//        // sets how much of the next item is shown
//        if(Validate.isTablet(getActivity())){
//            taxiSelector.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.viewpager_margin));
//        } else {
//            taxiSelector.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.viewpager_margin));
//        }

        // selects the item that was selected in the previous fragment
        int indexToSelect = getArguments().getInt(CommonKeys.SELECTED_INDEX);
        select(indexToSelect);
    }


    @Override
    protected void finishSelect(int position, Marker marker) {
        taxiSelector.setCurrentItem(position, true);
        super.finishSelect(position, marker);
    }

}