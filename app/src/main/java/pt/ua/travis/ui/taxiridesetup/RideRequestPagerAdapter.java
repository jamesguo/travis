package pt.ua.travis.ui.taxiridesetup;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideRequestPagerAdapter extends FragmentStatePagerAdapter {

    private RideRequestButtonsFragment buttonsFragment;
    private RideRequestOptionsFragment optionsFragment;

    public RideRequestPagerAdapter(FragmentManager fragmentManager){
        super(fragmentManager);
        buttonsFragment = new RideRequestButtonsFragment();
        optionsFragment = new RideRequestOptionsFragment();
    }

    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:  return buttonsFragment;
            case 1:  return optionsFragment;
            default: return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
