package pt.ua.travis.gui.ridelist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.squareup.picasso.Picasso;
import pt.ua.travis.R;
import pt.ua.travis.core.Client;
import pt.ua.travis.core.Ride;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.db.PersistenceManager;
import pt.ua.travis.gui.taxichooser.TaxiItem;
import pt.ua.travis.utils.Keys;

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
            rideObject = (Ride) savedInstanceState.getSerializable(Keys.SAVED_RIDE_OBJECT);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(Keys.SAVED_RIDE_OBJECT, rideObject);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parentActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(currentView==null) {

            Client clientObject = rideObject.client;
            Taxi taxiObject = rideObject.taxi;

            if(showWhat == SHOW_TAXI) {
                currentView = inflater.inflate(R.layout.ride_taxi_item, null);

                TaxiItem.paintViewWithTaxi(parentActivity, currentView, clientObject, taxiObject);

            } else if(showWhat == SHOW_CLIENT){
                currentView = inflater.inflate(R.layout.ride_taxi_item, null);

                // set the name
                TextView nameView = (TextView) currentView.findViewById(R.id.text);
                nameView.setText(clientObject.realName);

                // set the photo
                String imageUrl = clientObject.imageUri;
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    ImageView photoView = (ImageView) currentView.findViewById(R.id.photo);
                    Picasso.with(parentActivity).load(imageUrl).fit().into(photoView);
                }
            }

            TextView timeToRide = (TextView) currentView.findViewById(R.id.time_to_ride);
            timeToRide.setText(rideObject.getRemaining());

            TextView destination = (TextView) currentView.findViewById(R.id.destination_label);
            destination.setText(rideObject.destinationAddress);

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
                                    PersistenceManager.removeRide(rideObject);
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