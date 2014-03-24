package pt.ua.travis.gui.taxichooser;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import pt.ua.travis.R;
import pt.ua.travis.core.Client;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.utils.Keys;
import pt.ua.travis.utils.Validate;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TaxiItem extends Fragment {

    private View v;
    private static Activity parentActivity;

    private Drawable availableDrawable, favoriteDrawable;
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

        if (taxiObject.isAvailable) {
            availableDrawable = activity.getResources().getDrawable(R.drawable.available_border);
        } else {
            availableDrawable = activity.getResources().getDrawable(R.drawable.unavailable_border);
        }
        favoriteDrawable = activity.getResources().getDrawable(R.drawable.ic_favorites);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.taxi_item, null);

        // set the available color
        ImageView availableStatus = (ImageView) v.findViewById(R.id.available_status);
        availableStatus.setBackground(availableDrawable);

        // set the name
        TextView nameView = (TextView) v.findViewById(R.id.text);
        nameView.setText(taxiObject.name);

        // set the photo
        String imageUrl = taxiObject.imageUrl;
        if (imageUrl != null && !imageUrl.isEmpty()) {
//            ImageView photoView = (ImageView) v.findViewById(R.id.photo);
//            Log.e("TAAGG", imageUrl);
//            ImageLoader loader = Keys.getLoader(parentActivity);
//            loader.displayImage(imageUrl, photoView);
        }


        ImageView favoriteIcon = (ImageView) v.findViewById(R.id.favorite);
        if(clientObject.favorites.contains(taxiObject.id))
            favoriteIcon.setImageDrawable(favoriteDrawable);

        // set the rating
        RatingBar ratingBar = (RatingBar) v.findViewById(R.id.rating);
        ratingBar.setRating(taxiObject.getRatingAverage());
        
        return v;
    }
}