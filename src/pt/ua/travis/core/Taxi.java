package pt.ua.travis.core;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class Taxi extends User implements Serializable {
    private static final long serialVersionUID = 1L;

    public final List<Float> ratings;
    public boolean isAvailable;
    public double positionLat;
    public double positionLng;

    public Taxi(final String userName,
                final String passwordDigest,
                final String realName,
                final String imageUrl){
        super(userName, passwordDigest, realName, imageUrl);
        this.ratings = new ArrayList<Float>();
    }

    public User setPositionFromLatLng(LatLng latLng){
        positionLat = latLng.latitude;
        positionLng = latLng.longitude;
        return this;
    }

    public String getPositionString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(new BigDecimal(positionLat).setScale(2, BigDecimal.ROUND_HALF_UP));
        sb.append("; ");
        sb.append(new BigDecimal(positionLng).setScale(2, BigDecimal.ROUND_HALF_UP));
        sb.append(")");
        return sb.toString();
    }

    public float getRatingAverage() {
        float result = 0;
        for(Float rate : ratings){
            result = result + rate;
        }

        return result / ratings.size();
    }
}
