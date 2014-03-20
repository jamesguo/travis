package pt.ua.travis.gui.taxichooser;

import android.support.v4.view.ViewPager;
import com.androidmapsextensions.SupportMapFragment;
import pt.ua.travis.R;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.utils.Validate;

import java.util.List;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TaxiChooserPortraitFragment extends TaxiChooserFragment {

    private ViewPager taxiSelector;
    private TaxiPagerAdapter taxiAdapter;

    @Override
    public void onStart() {
        super.onStart();
        taxiSelector = (ViewPager) getActivity().findViewById(R.id.pager);
        SupportMapFragment mapFragment = (SupportMapFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentById(R.id.map);
        setMap(mapFragment.getExtendedMap());

        List<Taxi> taxis = TaxiChooserActivity.getCurrentTaxiListState();
        taxiAdapter = new TaxiPagerAdapter(getChildFragmentManager(), convertTaxisToItems(taxis));


        // selector (horizontal list) configurations
        taxiSelector.setOffscreenPageLimit(3);
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

        // sets how much of the next item is shown
        if(Validate.isTablet(getActivity())){
            taxiSelector.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.viewpager_margin));
        } else {
            taxiSelector.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.viewpager_margin));
        }


        finalizeOnStart();
    }


    @Override
    void doSelectorAction(int position) {
        taxiSelector.setCurrentItem(position, true);
    }

    @Override
    public TaxiPagerAdapter getTaxiAdapter() {
        return taxiAdapter;
    }
}