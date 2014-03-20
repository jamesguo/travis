package pt.ua.travis.gui.taxichooser;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.google.android.gms.maps.model.LatLng;
import pt.ua.travis.R;
import pt.ua.travis.core.Taxi;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TaxiItem extends Fragment {

    public static final int HORIZONTAL_MODE = 1;
    public static final int VERTICAL_MODE = 2;

    private static final String MODE_KEY = "mode";
    private static final String MARKER_ID_KEY = "marker_id";
    private static final String TAXI_OBJECT_KEY = "taxi_object";


    public static TaxiItem newInstance(int mode, String markerID, Taxi taxi) {
        TaxiItem t = new TaxiItem();

        Bundle args = new Bundle();
        args.putInt(MODE_KEY, mode);
        args.putString(MARKER_ID_KEY, markerID);
        args.putSerializable(TAXI_OBJECT_KEY, taxi);
        t.setArguments(args);

        return t;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v;
        int mode = getArguments().getInt(MODE_KEY);
        Taxi taxiObject = getTaxiObject();

        if(mode == HORIZONTAL_MODE) {
            v = inflater.inflate(R.layout.taxi_list_item, null);
            TextView coors = (TextView) v.findViewById(R.id.latlng);
            coors.setText(taxiObject.getPositionString());

        } else if(mode == VERTICAL_MODE) {
            v = inflater.inflate(R.layout.taxi_list_item_compact, null);

        } else {
            Log.e("HERE", "BADUM");
            return null;
        }

        TextView nameView = (TextView) v.findViewById(R.id.text);
        nameView.setText(taxiObject.getName());

        String imageUrl = taxiObject.getImageUrl();
        if(imageUrl!= null && !imageUrl.isEmpty()) {
            ImageView photoView = (ImageView) v.findViewById(R.id.image);
            photoView.setImageURI(Uri.parse(imageUrl));
        }

        RatingBar ratingBar = (RatingBar) v.findViewById(R.id.rating);
        ratingBar.setRating(taxiObject.getRatingAverage());
        
        return v;
    }

    private Taxi getTaxiObject(){
        return (Taxi) getArguments().getSerializable(TAXI_OBJECT_KEY);
    }
    
    public String getMarkerID(){
        return getArguments().getString(MARKER_ID_KEY);
    }

    public LatLng getActualPosition() {
        return getTaxiObject().getPosition();
    }
}