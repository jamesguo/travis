package pt.ua.travis.gui.taxichooser;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.squareup.picasso.Picasso;
import pt.ua.travis.R;
import pt.ua.travis.core.Client;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.utils.CommonRes;
import pt.ua.travis.utils.Keys;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TaxiItem extends Fragment {


    private View currentView;
    private Activity parentActivity;

    private Client clientObject;
    private Taxi taxiObject;

    public static TaxiItem newInstance(Client currentClient, Taxi taxiToRepresent) {
        TaxiItem t = new TaxiItem();

        t.clientObject = currentClient;
        t.taxiObject = taxiToRepresent;

        return t;
    }

    Taxi getTaxiObject(){
        return taxiObject;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null){
            taxiObject = (Taxi) savedInstanceState.getSerializable(Keys.SAVED_TAXI_OBJECT);
            clientObject = (Client) savedInstanceState.getSerializable(Keys.SAVED_CLIENT_OBJECT);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(Keys.SAVED_TAXI_OBJECT, taxiObject);
        outState.putSerializable(Keys.SAVED_CLIENT_OBJECT, clientObject);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parentActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(currentView==null) {
            currentView = inflater.inflate(R.layout.taxi_item, null);
            TaxiItem.paintViewWithTaxi(parentActivity, currentView, clientObject, taxiObject);
        }
        return currentView;
    }

    public static void paintViewWithTaxi(Context context, View v, Client clientObject, Taxi taxiObject){

        // set the available status shape at the left side of the photo
        ImageView availableStatus = (ImageView) v.findViewById(R.id.available_status);
        if (taxiObject.isAvailable) {
            availableStatus.setImageDrawable(CommonRes.AVAILABLE_SHAPE);
        } else {
            availableStatus.setImageDrawable(CommonRes.UNAVAILABLE_SHAPE);
        }

        // set the name
        TextView nameView = (TextView) v.findViewById(R.id.text);
        nameView.setText(taxiObject.realName);

        // set the photo
        String imageUrl = taxiObject.imageUri;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            ImageView photoView = (ImageView) v.findViewById(R.id.photo);
            Picasso.with(context).load(imageUrl).fit().into(photoView);
        }

        // set the favorite icon
        ImageView favoriteIcon = (ImageView) v.findViewById(R.id.favorite);
        if(clientObject.favorites.contains(taxiObject.id))
            favoriteIcon.setImageDrawable(CommonRes.FAVORITE_ICON);

        // set the rating
        RatingBar ratingBar = (RatingBar) v.findViewById(R.id.rating);
        ratingBar.setRating(taxiObject.getRatingAverage());
    }
}