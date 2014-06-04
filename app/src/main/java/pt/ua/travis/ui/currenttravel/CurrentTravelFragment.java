package pt.ua.travis.ui.currenttravel;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import com.todddavies.components.progressbar.ProgressWheel;
import pt.ua.travis.R;
import pt.ua.travis.ui.customviews.SlidingPaneLayout;
import pt.ua.travis.ui.customviews.TravisFragment;
import pt.ua.travis.ui.main.MainActivity;
import pt.ua.travis.ui.main.MainClientActivity;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class CurrentTravelFragment extends TravisFragment {

    private MainActivity parentActivity;

    private SlidingPaneLayout slidingPaneLayout;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentView(R.layout.fragment_current_travel);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parentActivity = (MainActivity) activity;
    }

    @Override
    public void onStart() {
        super.onStart();

        slidingPaneLayout = (SlidingPaneLayout) parentActivity.findViewById(R.id.sliding_pane_travel);
        slidingPaneLayout.setStickTo(SlidingPaneLayout.STICK_TO_BOTTOM);
        slidingPaneLayout.setSlidingEnabled(false);
        slidingPaneLayout.closeLayer(false);
        setContentShown(true);

    }

    public void showAuthentication(){
        slidingPaneLayout.openLayer(true);
    }

    public boolean slidingPaneIsOpened() {
        return slidingPaneLayout.isOpened();
    }
}
