package pt.ua.travis.ui.ridelist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;
import pt.ua.travis.R;
import pt.ua.travis.backend.Client;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Ride;
import pt.ua.travis.backend.Taxi;
import pt.ua.travis.ui.taxichooser.TaxiItem;
import pt.ua.travis.utils.CommonKeys;
import pt.ua.travis.utils.Utils;

import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideItem extends Fragment {

    public static final int SHOW_CLIENT = 12311;
    public static final int SHOW_TAXI = 12312;

    private View currentView;
    private Activity parentActivity;

    private int showWhat;
    private Ride rideObject;

    public static RideItem newInstance(int showWhat, Ride rideToRepresent) {
        RideItem t = new RideItem();

        t.showWhat = showWhat;
        t.rideObject = rideToRepresent;

        return t;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null){
            final String rideID = savedInstanceState.getString(CommonKeys.SAVED_RIDE_OBJECT_ID);
            rideObject = PersistenceManager.getFromCache(rideID);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(CommonKeys.SAVED_RIDE_OBJECT_ID, rideObject.id());
        PersistenceManager.addToCache(rideObject);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parentActivity = activity;
    }

    public String getUserPhoto() {
        if (showWhat == SHOW_TAXI) {
            return rideObject.taxi().imageUri();
        } else if (showWhat == SHOW_CLIENT) {
            return rideObject.client().imageUri();
        } else {
            return "";
        }
    }

    public String getUserName() {
        if (showWhat == SHOW_TAXI) {
            return rideObject.taxi().name();
        } else if (showWhat == SHOW_CLIENT) {
            return rideObject.client().name();
        } else {
            return "";
        }
    }

    public boolean showFavoriteIcon() {
        return showWhat == SHOW_TAXI && rideObject.client().taxiIsAFavorite(rideObject.taxi());
    }

    public Ride getRideObject() {
        return rideObject;
    }

    public int getUserTypeToShow() {
        return showWhat;
    }
}