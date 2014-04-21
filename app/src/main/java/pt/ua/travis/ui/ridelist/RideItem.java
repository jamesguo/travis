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
import pt.ua.travis.backend.entities.*;
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
    private RideDeletedListener deletedListener;

    public static RideItem newInstance(int showWhat, Ride rideToRepresent, RideDeletedListener deletedListener) {
        RideItem t = new RideItem();

        t.showWhat = showWhat;
        t.rideObject = rideToRepresent;
        t.deletedListener = deletedListener;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(currentView==null) {

            Client clientObject = rideObject.client();
            Taxi taxiObject = rideObject.taxi();

            if(showWhat == SHOW_TAXI) {
                currentView = inflater.inflate(R.layout.item_ride_taxi, null);

                TaxiItem.paintViewWithTaxi(parentActivity, currentView, clientObject, taxiObject);

            } else if(showWhat == SHOW_CLIENT){
                currentView = inflater.inflate(R.layout.item_ride_taxi, null);

                // set the name
                TextView nameView = (TextView) currentView.findViewById(R.id.text);
                nameView.setText(clientObject.name());

                // set the photo
                String imageUrl = clientObject.imageUri();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    ImageView photoView = (ImageView) currentView.findViewById(R.id.photo);
                    Picasso.with(parentActivity).load(imageUrl).fit().into(photoView);
                }
            }

            TextView timeToRide = (TextView) currentView.findViewById(R.id.time_to_ride);
            timeToRide.setText(rideObject.getRemaining());


            LatLng pos = rideObject.destinationPosition();
            List<Address> addressList = Utils.addressesFromLocation(parentActivity, pos.latitude, pos.longitude);
            if(addressList!=null && !addressList.isEmpty()) {
                TextView destination = (TextView) currentView.findViewById(R.id.destination_label);
                Address destinationAddress = addressList.get(0);
                destination.setText(Utils.addressToString(destinationAddress));
            }

            final BootstrapButton deleteButton = (BootstrapButton) currentView.findViewById(R.id.delete_button);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
                    builder.setTitle("Confirm delete")
                            .setMessage("Are you sure you wish to delete this ride?")
                            .setCancelable(false)
                            .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    rideObject.setAsCompleted();
                                    deletedListener.onDeletedRide();
                                }
                            })
                            .setPositiveButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            });
        }
        return currentView;
    }
}