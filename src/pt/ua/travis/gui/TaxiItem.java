package pt.ua.travis.gui;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.google.android.gms.maps.model.LatLng;
import pt.ua.travis.R;
import pt.ua.travis.core.TravisTaxi;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TaxiItem extends Fragment {

    public static final int HORIZONTAL_MODE = 0;
    public static final int VERTICAL_MODE = 1;

    private final int mode;
    private final String markerID;
    private final TravisTaxi travisTaxi;

    public TaxiItem(int mode, String markerID, TravisTaxi travisTaxi){
        this.mode = mode;
        this.markerID = markerID;
        this.travisTaxi = travisTaxi;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v;
        if(mode == HORIZONTAL_MODE) {
            v = inflater.inflate(R.layout.taxi_list_item, null);
            TextView coors = (TextView) v.findViewById(R.id.latlng);
            coors.setText(travisTaxi.getPositionString());
        } else if(mode == VERTICAL_MODE) {
            v = inflater.inflate(R.layout.taxi_list_item_compact, null);
        } else {
            return null;
        }

        TextView nameView = (TextView) v.findViewById(R.id.text);
        nameView.setText(travisTaxi.getName());

        String imageUrl = travisTaxi.getImageUrl();
        if(imageUrl!= null && !imageUrl.isEmpty()) {
            ImageView photoView = (ImageView) v.findViewById(R.id.image);
            photoView.setImageURI(Uri.parse(imageUrl));
        }

        RatingBar ratingBar = (RatingBar) v.findViewById(R.id.rating);
        ratingBar.setRating(travisTaxi.getRatingAverage());
        
        return v;
    }
    
    public String getMarkerID(){
        return markerID;
    }

    public LatLng getActualPosition() {
        return travisTaxi.getPosition();
    }
}