package pt.ua.travis.core;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class User extends TravisObject implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String name;
    public final String imageUrl;
    private double positionLat;
    private double positionLng;


    protected User(final String name, final String imageUrl){
        super();
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public User setPosition(LatLng latLng){
        positionLat = latLng.latitude;
        positionLng = latLng.longitude;
        return this;
    }

    public LatLng position(){
        return new LatLng(positionLat, positionLng);
    }

    public String getPositionString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(new BigDecimal(position().latitude).setScale(2, BigDecimal.ROUND_HALF_UP));
        sb.append("; ");
        sb.append(new BigDecimal(position().longitude).setScale(2, BigDecimal.ROUND_HALF_UP));
        sb.append(")");
        return sb.toString();
    }
}
