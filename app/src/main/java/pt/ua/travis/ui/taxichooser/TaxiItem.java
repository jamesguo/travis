package pt.ua.travis.ui.taxichooser;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import pt.ua.travis.R;
import pt.ua.travis.backend.Client;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Taxi;
import pt.ua.travis.ui.customviews.CircularImageView;
import pt.ua.travis.utils.CommonKeys;
import pt.ua.travis.utils.CommonRes;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TaxiItem extends Fragment {


    private View currentView;
    private Activity parentActivity;

    private Client currentClient;
    private Taxi taxiToRepresent;

    public static TaxiItem newInstance(Client currentClient, Taxi taxiToRepresent) {
        TaxiItem t = new TaxiItem();

        t.currentClient = currentClient;
        t.taxiToRepresent = taxiToRepresent;

        return t;
    }

    Taxi getTaxiObject(){
        return taxiToRepresent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null){
            final String taxiID = savedInstanceState.getString(CommonKeys.SAVED_TAXI_OBJECT_ID);
            taxiToRepresent = PersistenceManager.getFromCache(taxiID);

            final String clientID = savedInstanceState.getString(CommonKeys.SAVED_CLIENT_OBJECT_ID);
            currentClient = PersistenceManager.getFromCache(clientID);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(CommonKeys.SAVED_TAXI_OBJECT_ID, taxiToRepresent.id());
        PersistenceManager.addToCache(taxiToRepresent);

        outState.putString(CommonKeys.SAVED_CLIENT_OBJECT_ID, currentClient.id());
        PersistenceManager.addToCache(currentClient);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parentActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(currentView==null) {
            currentView = inflater.inflate(R.layout.item_taxi, null);
            TaxiItem.paintViewWithTaxi(parentActivity, currentView, currentClient, taxiToRepresent);
        }
        return currentView;
    }

    public View getCurrentView() {
        return currentView;
    }

    public static void paintViewWithTaxi(Context context, View v, Client clientObject, Taxi taxiObject){

        // set the name
        TextView nameView = (TextView) v.findViewById(R.id.taxi_name);
        nameView.setText(taxiObject.name());

        // set the photo
        String imageUrl = taxiObject.imageUri();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            final CircularImageView photoView = (CircularImageView) v.findViewById(R.id.taxi_photo);
            Picasso.with(context).load(imageUrl).placeholder(R.drawable.placeholder).fit().into(photoView);
            if (taxiObject.isAvailable()) {
                photoView.setBorderColor(CommonRes.get().AVAILABLE_COLOR);
            } else {
                photoView.setBorderColor(CommonRes.get().UNAVAILABLE_COLOR);
            }
        }

        // set the favorite icon
        ImageView favoriteIcon = (ImageView) v.findViewById(R.id.taxi_favorite_flag);
        if(clientObject.taxiIsAFavorite(taxiObject)) {
            favoriteIcon.setImageDrawable(CommonRes.get().FAVORITE_ICON_FILLED);
        }

        // set the rating
        RatingBar ratingBar = (RatingBar) v.findViewById(R.id.taxi_rating);
        ratingBar.setRating(taxiObject.getRatingAverage());

        // set rating text
        TextView ratingTextView = (TextView) v.findViewById(R.id.taxi_rating_text);
        String ratedBy = context.getString(R.string.rated_by_X);
        String users =context.getString(R.string.users);
        ratingTextView.setText(ratedBy + " " + taxiObject.getRatingQuantity() + " " + users);

    }
}