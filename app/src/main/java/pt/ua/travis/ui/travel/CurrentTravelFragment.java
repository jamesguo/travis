package pt.ua.travis.ui.travel;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import com.androidmapsextensions.Circle;
import com.androidmapsextensions.CircleOptions;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import pt.ua.travis.R;
import pt.ua.travis.backend.*;
import pt.ua.travis.core.TravisApplication;
import pt.ua.travis.mapnavigator.Navigator;
import pt.ua.travis.ui.customviews.SlidingPaneLayout;
import pt.ua.travis.ui.customviews.TravisFragment;
import pt.ua.travis.ui.customviews.TravisMapFragment;
import pt.ua.travis.ui.main.MainActivity;
import pt.ua.travis.utils.CommonKeys;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class CurrentTravelFragment extends TravisFragment {

    private MainActivity parentActivity;

    private String oldFragmentTag;

    private TravisMapFragment mapFragment;

    private BootstrapButton button;

    private GoogleMap map;

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

        button = (BootstrapButton) parentActivity.findViewById(R.id.go_to_next_step_button);
        button.setVisibility(View.GONE);

        mapFragment = (TravisMapFragment) parentActivity
                .getSupportFragmentManager()
                .findFragmentById(R.id.travel_map);
        map = mapFragment.getMap();
        map.clear();

        SharedPreferences prefs = parentActivity.getSharedPreferences("TravisPreferences", Context.MODE_PRIVATE);
        int mapType = prefs.getInt(CommonKeys.MAP_TYPE, GoogleMap.MAP_TYPE_TERRAIN);

        TravisApplication app = (TravisApplication) parentActivity.getApplication();
        map.setMapType(mapType);
        map.setMyLocationEnabled(false);
        map.moveCamera(CameraUpdateFactory.zoomTo(17.0f));
        LatLng latLng = app.getCurrentLocation();
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng), 900, null);
        map.addMarker(new MarkerOptions()
                        .position(latLng)
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_available))
        );


        slidingPaneLayout = (SlidingPaneLayout) parentActivity.findViewById(R.id.sliding_pane_travel);
        slidingPaneLayout.setStickTo(SlidingPaneLayout.STICK_TO_BOTTOM);
        slidingPaneLayout.setSlidingEnabled(true);
        slidingPaneLayout.closeLayer(false);

        container = (FrameLayout) parentActivity.findViewById(R.id.sliding_pane_travel_container);

        app.addLocationListener(new TravisApplication.CurrentLocationListener() {
            @Override
            public void onCurrentLocationChanged(LatLng latLng) {
                map.moveCamera(CameraUpdateFactory.zoomTo(17.0f));
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng), 900, null);
                map.addMarker(new MarkerOptions()
                .position(latLng)
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_available))
                );
            }
        });

        setContentShown(true);

    }

    public void goToOrigin(final Ride requestedRide) {
        setContentShown(false);

        button.setText("Arrived");
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersistenceManager.setTaxiArrived(requestedRide);
                showAuthentication(requestedRide);
            }
        });
        TravisApplication app = (TravisApplication) parentActivity.getApplication();

        Navigator nav = new Navigator(map, app.getCurrentLocation(), requestedRide.originPosition());
        nav.findDirections(true);
        slidingPaneLayout.closeLayer(true);

        setContentShown(true);
    }

    public void goToDestination(Ride requestedRide) {
        button.setText("Arrived");
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPayment();
            }
        });
        TravisApplication app = (TravisApplication) parentActivity.getApplication();

        Navigator nav = new Navigator(map, app.getCurrentLocation(), requestedRide.destinationPosition());
        nav.findDirections(true);
        slidingPaneLayout.closeLayer(true);
    }

    public void showAuthentication(final Ride arrivedRide) {
        String newTag = AuthenticationFragment.class.getSimpleName();

        final User currentUser = PersistenceManager.getCurrentlyLoggedInUser();

        AuthenticationFragment.OnAuthenticationCompleteListener listener =
                new AuthenticationFragment.OnAuthenticationCompleteListener() {
            @Override
            public void onAuthenticationComplete(boolean valid) {
                if(valid){
                    goToDestination(arrivedRide);
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

    public boolean slidingPaneIsOpened() {
        return slidingPaneLayout.isOpened();
    }
}
