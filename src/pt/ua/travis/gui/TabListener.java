package pt.ua.travis.gui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.TabHost;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TabListener<T extends Fragment> implements ActionBar.TabListener {
    private TabHost host;
    private Fragment fragment;
    private final SherlockFragmentActivity mainActivity;
    private final String goToFragmentTag;
    private final String goToFragmentClassName;

    /** Constructor used each time a new tab is created.
     * @param mainActivity  The host Activity, used to instantiate the fragment
     * @param tag  The identifier tag for the fragment
     * @param clz  The fragment's Class, used to instantiate the fragment
     */
    public TabListener(SherlockFragmentActivity mainActivity, String tag, Class<? extends T> clz) {
        this.mainActivity = mainActivity;
        this.goToFragmentTag = tag;
        this.goToFragmentClassName = clz.getName();
    }

    /* The following are each of the ActionBar.TabListener callbacks */

    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        // Check if the fragment is already initialized
        if (fragment == null) {
            // If not, instantiate and add it to the activity
            fragment = SherlockFragment.instantiate(mainActivity, goToFragmentClassName);
//            fragment = host.setup();
            ft.add(android.R.id.content, fragment, goToFragmentTag);
        } else {
            // If it exists, simply attach it in order to show it
            ft.attach(fragment);
        }
    }

    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        if (fragment != null) {
            // Detach the fragment, because another one is being attached
            ft.detach(fragment);
        }
    }

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        // User selected the already selected tab. Usually do nothing.
    }
}
