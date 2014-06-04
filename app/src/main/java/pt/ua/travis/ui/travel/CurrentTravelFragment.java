package pt.ua.travis.ui.travel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import pt.ua.travis.R;
import pt.ua.travis.backend.*;
import pt.ua.travis.ui.customviews.SlidingPaneLayout;
import pt.ua.travis.ui.customviews.TravisFragment;
import pt.ua.travis.ui.customviews.TravisMapFragment;
import pt.ua.travis.ui.main.MainActivity;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class CurrentTravelFragment extends TravisFragment {

    private MainActivity parentActivity;

    private String oldFragmentTag;

    private TravisMapFragment mapFragment;
    private SlidingPaneLayout slidingPaneLayout;
    private FrameLayout container;

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

        container = (FrameLayout) parentActivity.findViewById(R.id.sliding_pane_travel_container);

        setContentShown(true);

    }

    public void showAuthentication(Ride arrivedRide) {
        String newTag = AuthenticationFragment.class.getSimpleName();

        final User currentUser = PersistenceManager.getCurrentlyLoggedInUser();

        AuthenticationFragment.OnAuthenticationCompleteListener listener =
                new AuthenticationFragment.OnAuthenticationCompleteListener() {
            @Override
            public void onAuthenticationComplete(boolean valid) {
                if(valid){
                    showTravel();
                } else {
                    SimpleDialogFragment.SimpleDialogBuilder builder = SimpleDialogFragment
                            .createBuilder(parentActivity, getChildFragmentManager())
                            .setTitle(R.string.state_login_failed);

                    if(currentUser instanceof Client) {
                        builder.setMessage(R.string.dialog_authentication_failed_msg_taxi);
                    } else if (currentUser instanceof Taxi) {
                        builder.setMessage(R.string.dialog_authentication_failed_msg_client);
                    }

                    builder.show();

                }
            }
        };

        Fragment fragment = getChildFragmentManager().findFragmentByTag(oldFragmentTag);
        FragmentTransaction ft = getChildFragmentManager()
                .beginTransaction()
                .add(R.id.sliding_pane_travel_container,
                        AuthenticationFragment.newInstance(arrivedRide, currentUser, listener), newTag)
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        if (fragment != null) {
            ft.remove(getChildFragmentManager().findFragmentByTag(oldFragmentTag));
        }

        oldFragmentTag = newTag;
        ft.commit();

        slidingPaneLayout.openLayer(true);
    }

    public void showPayment() {
        String newTag = PaymentFragment.class.getSimpleName();

        Fragment fragment = getChildFragmentManager().findFragmentByTag(oldFragmentTag);
        FragmentTransaction ft = getChildFragmentManager()
                .beginTransaction()
                .add(R.id.sliding_pane_travel_container, new PaymentFragment(), newTag)
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        if (fragment != null) {
            ft.remove(getChildFragmentManager().findFragmentByTag(oldFragmentTag));
        }

        oldFragmentTag = newTag;
        ft.commit();

        slidingPaneLayout.openLayer(true);
    }

    public void showTravel() {
        String newTag = PaymentFragment.class.getSimpleName();

        Fragment fragment = getChildFragmentManager().findFragmentByTag(oldFragmentTag);
        FragmentTransaction ft = getChildFragmentManager()
                .beginTransaction()
                .add(R.id.sliding_pane_travel_container, new PaymentFragment(), newTag)
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        if (fragment != null) {
            ft.remove(getChildFragmentManager().findFragmentByTag(oldFragmentTag));
        }

        oldFragmentTag = newTag;
        ft.commit();

        slidingPaneLayout.closeLayer(true);
    }

    public boolean slidingPaneIsOpened() {
        return slidingPaneLayout.isOpened();
    }
}
