package pt.ua.travis.ui.main;

import android.support.v4.app.FragmentTransaction;
import com.actionbarsherlock.app.ActionBar;
import pt.ua.travis.core.TravisFragment;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public abstract class TravisTabListener<T extends TravisFragment> implements ActionBar.TabListener {
    private T mFragment;
    private final MainActivity mActivity;
    private final String mTag;
    private final Class<T> mClass;
    private final int mfragmentContainerId;

    public TravisTabListener(MainActivity activity, String tag, Class<T> clz) {
        mActivity = activity;
        mTag = tag;
        mClass = clz;
        mfragmentContainerId = android.R.id.content;
    }

    public TravisTabListener(int fragmentContainerId, MainActivity activity,
                             String tag, Class<T> clz) {
        mActivity = activity;
        mTag = tag;
        mClass = clz;
        mfragmentContainerId = fragmentContainerId;
    }

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        // Check if the fragment is already initialized
        if (mFragment == null) {
            // If not, instantiate and add it to the activity
            mFragment = (T) TravisFragment.instantiate(mActivity, mClass.getName());
            ft.add(mfragmentContainerId, mFragment, mTag);
        } else {
            // If it exists, simply attach it in order to show it
            ft.attach(mFragment);
        }

        afterTabSelected(mFragment);
    }

    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        if (mFragment != null) {
            // Detach the fragment, because another one is being attached
            ft.detach(mFragment);
        }
    }

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    // User selected the already selected tab. Usually do nothing.
    }

    public abstract void afterTabSelected(T currentlyShownFragment);
}