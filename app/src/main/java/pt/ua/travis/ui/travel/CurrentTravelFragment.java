package pt.ua.travis.ui.travel;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import pt.ua.travis.R;
import pt.ua.travis.backend.*;
import pt.ua.travis.core.BaseFragment;
import pt.ua.travis.core.BaseMapFragment;
import pt.ua.travis.core.TravisApplication;
import pt.ua.travis.mapnavigator.Navigator;
import pt.ua.travis.ui.main.MainActivity;
import pt.ua.travis.utils.CommonKeys;

import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class CurrentTravelFragment extends BaseFragment {

    private MainActivity parentActivity;

    private AuthenticationDialog authenticationDialog;

    private BaseMapFragment mapFragment;

    private BootstrapButton button;

    private GoogleMap map;

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

        mapFragment = (BaseMapFragment) parentActivity
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


        app.addLocationListener(new TravisApplication.CurrentLocationListener() {
            @Override
            public void onCurrentLocationChanged(LatLng latLng) {
                map.clear();
                map.addMarker(new MarkerOptions().position(latLng));
                map.moveCamera(CameraUpdateFactory.zoomTo(17.0f));
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng), 900, null);
            }
        });

        setContentShown(true);

    }

    public void goToOrigin(final Ride requestedRide) {
        setContentShown(false);

        button.setText(parentActivity.getString(R.string.arrived_at_client));
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("hahaha", "hahaha");
                showAuthentication(requestedRide);
                PersistenceManager.setTaxiArrived(requestedRide);
            }
        });
        TravisApplication app = (TravisApplication) parentActivity.getApplication();

        Navigator nav = new Navigator(map, app.getCurrentLocation(), requestedRide.originPosition());
        nav.findDirections(true);

        setContentShown(true);
    }

    public void showAuthentication(final Ride arrivedRide) {
        final User currentUser = PersistenceManager.getCurrentlyLoggedInUser();

        AuthenticationDialog.OnAuthenticationCompleteListener listener =
                new AuthenticationDialog.OnAuthenticationCompleteListener() {
                    @Override
                    public void onAuthenticationComplete(boolean valid) {
                        authenticationDialog.dismiss();
                        if (valid) {
                            goToDestination(arrivedRide);
                        } else {
                            SimpleDialogFragment.SimpleDialogBuilder builder = SimpleDialogFragment
                                    .createBuilder(parentActivity, getChildFragmentManager())
                                    .setTitle(R.string.state_login_failed);

                            if (currentUser instanceof Client) {
                                builder.setMessage(R.string.dialog_authentication_failed_msg_taxi);
                            } else if (currentUser instanceof Taxi) {
                                builder.setMessage(R.string.dialog_authentication_failed_msg_client);
                            }

                            builder.show();

                        }
                    }
                };
//
        authenticationDialog = AuthenticationDialog.newInstance(parentActivity, arrivedRide, currentUser, listener);
        authenticationDialog.show(getChildFragmentManager(), "AuthDialog");

        setContentShown(false);
    }

    public void goToDestination(final Ride requestedRide) {
        if(authenticationDialog !=null) {
            authenticationDialog.dismiss();
        }

        LatLng destPos = requestedRide.destinationPosition();
        if (destPos != null) {
            TravisApplication app = (TravisApplication) parentActivity.getApplication();
            final Navigator nav = new Navigator(map, app.getCurrentLocation(), destPos);
            nav.findDirections(true);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<Polyline> list = nav.getPathLines();
                    for(Polyline p : list){
                        p.remove();
                    }
                    finishRide(requestedRide);
                }
            });
        } else {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishRide(requestedRide);
                }
            });
        }

        button.setText(parentActivity.getString(R.string.arrived_at_dest));
        button.setVisibility(View.VISIBLE);


        setContentShown(true);
    }

    public void finishRide(final Ride finishedRide) {
        button.setVisibility(View.GONE);

        User u = PersistenceManager.getCurrentlyLoggedInUser();
        if(u instanceof Client) {
            RateAndFavDialog.newInstance(parentActivity, (Client)u, finishedRide.taxi())
                    .show(getChildFragmentManager(), "RateAndFavDialog");

        } else if(u instanceof Taxi) {
            PaymentDialog.newInstance(new PaymentDialog.OnPaymentCompleteListener(){
                @Override
                public void onPaymentComplete(boolean success) {
                    if(success) {
                        PersistenceManager.delete(finishedRide);
                        parentActivity.updateRideList();
                        setContentShown(true);
                    }
                }
            }).show(getChildFragmentManager(), "PayDialog");

        }
    }
}
