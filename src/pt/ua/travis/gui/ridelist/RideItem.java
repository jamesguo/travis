package pt.ua.travis.gui.ridelist;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import pt.ua.travis.R;
import pt.ua.travis.core.Client;
import pt.ua.travis.core.Ride;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.utils.Keys;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideItem extends Fragment {

    private View v;
    private static Activity parentActivity;

    private Drawable availableDrawable, favoriteDrawable;
    private Ride rideObject;

    public static RideItem newInstance(Ride rideToRepresent) {
        RideItem t = new RideItem();

        t.rideObject = rideToRepresent;

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

        if (rideObject.taxi.isAvailable) {
            availableDrawable = activity.getResources().getDrawable(R.drawable.available_border);
        } else {
            availableDrawable = activity.getResources().getDrawable(R.drawable.unavailable_border);
        }
        favoriteDrawable = activity.getResources().getDrawable(R.drawable.ic_favorites);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.ride_item, null);

        Client clientObject = rideObject.client;
        Taxi taxiObject = rideObject.taxi;

        // set the available color
        ImageView availableStatus = (ImageView) v.findViewById(R.id.available_status);
        availableStatus.setBackground(availableDrawable);

        // set the name
        TextView nameView = (TextView) v.findViewById(R.id.text);
        nameView.setText(taxiObject.name);

        // set the photo
        String imageUrl = taxiObject.imageUrl;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            ImageView photoView = (ImageView) v.findViewById(R.id.photo);
            Log.e("TAAGG", imageUrl);
            ImageLoader loader = Keys.getLoader(parentActivity);
            loader.displayImage(imageUrl, photoView);
        }


        ImageView favoriteIcon = (ImageView) v.findViewById(R.id.favorite);
        if(clientObject.favorites.contains(taxiObject.id))
            favoriteIcon.setImageDrawable(favoriteDrawable);

        // set the rating
        RatingBar ratingBar = (RatingBar) v.findViewById(R.id.rating);
        ratingBar.setRating(taxiObject.getRatingAverage());

        TextView timeToRide = (TextView) v.findViewById(R.id.time_to_ride);
        timeToRide.setText(rideObject.getRemaining()); // TODO GET THIS TIME FROM ESTIMATE OF TAXI ARRIVING


        TextView destination = (TextView) v.findViewById(R.id.destination_label);
        destination.setText(rideObject.destinationAddress);

        return v;
    }

    Ride getRideObject() {
        return rideObject;
    }
}